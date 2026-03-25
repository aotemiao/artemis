package com.aotemiao.artemis.symphony.live;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.symphony.config.WorkflowLoadResult;
import com.aotemiao.artemis.symphony.config.WorkflowLoader;
import com.aotemiao.artemis.symphony.core.SshClient;
import com.aotemiao.artemis.symphony.core.model.Issue;
import com.aotemiao.artemis.symphony.orchestrator.AgentRunner;
import com.aotemiao.artemis.symphony.orchestrator.SymphonyRuntimeHolder;
import com.aotemiao.artemis.symphony.orchestrator.SymphonyRuntimeSnapshot;
import com.aotemiao.artemis.symphony.workspace.WorkspaceManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 真实 Linear / Codex / SSH 集成演练，对齐官方 Symphony live e2e。 */
@Tag("live_e2e")
@EnabledIfEnvironmentVariable(named = "SYMPHONY_RUN_LIVE_E2E", matches = "1")
@Timeout(value = 20, unit = TimeUnit.MINUTES)
class SymphonyLiveE2EIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(SymphonyLiveE2EIT.class);

    private static final String DEFAULT_TEAM_KEY = "SYME2E";
    private static final String DEFAULT_DOCKER_AUTH_JSON =
            Path.of(System.getProperty("user.home"), ".codex", "auth.json").toString();
    private static final int DOCKER_WORKER_COUNT = 2;
    private static final String RESULT_FILE = "LIVE_E2E_RESULT.txt";

    @TempDir
    Path tempDir;

    @Test
    void localWorker_liveIssueFlowCompletes() throws Exception {
        runLiveIssueFlow(Backend.LOCAL);
    }

    @Test
    void sshWorker_liveIssueFlowCompletes() throws Exception {
        runLiveIssueFlow(Backend.SSH);
    }

    private void runLiveIssueFlow(Backend backend) throws Exception {
        requireCommand("codex");

        String runId = "symphony-live-e2e-" + backend.name().toLowerCase() + "-" + System.nanoTime();
        Path testRoot = tempDir.resolve(runId);
        Path workflowFile = testRoot.resolve("WORKFLOW.md");
        Files.createDirectories(testRoot);

        LinearLiveTestClient linear = new LinearLiveTestClient(requiredEnv("LINEAR_API_KEY"));
        LiveWorkerSetup workerSetup = null;
        LinearLiveTestClient.ProjectStatus completedProjectStatus = null;
        LinearLiveTestClient.Project project = null;

        try {
            workerSetup = liveWorkerSetup(backend, runId, testRoot);

            LinearLiveTestClient.Team team = linear.fetchTeam(liveTeamKey());
            LinearLiveTestClient.WorkflowState activeState = team.activeState();
            completedProjectStatus = linear.completedProjectStatus();

            project = linear.createProject(
                    team.id(), "Symphony Live E2E " + backend.name().toLowerCase() + " " + System.nanoTime());
            Issue issue = linear.createIssue(
                    team.id(),
                    project.id(),
                    activeState.id(),
                    "Symphony live e2e " + backend.name().toLowerCase() + " issue for " + project.name());

            writeWorkflowFile(workflowFile, project.slugId(), team, workerSetup);
            SymphonyRuntimeSnapshot snapshot = loadSnapshot(workflowFile);
            WorkspaceManager workspaceManager = new WorkspaceManager(() -> snapshot.config());
            AgentRunner agentRunner =
                    new AgentRunner(() -> snapshot.config(), workspaceManager, snapshot::trackerClient);
            Path eventsLog = testRoot.resolve(backend.name().toLowerCase() + "-codex-events.log");

            AtomicBoolean success = new AtomicBoolean(false);
            AtomicReference<String> failure = new AtomicReference<>();
            AtomicReference<AgentRunner.RuntimeInfo> runtimeInfoRef = new AtomicReference<>();

            agentRunner.runAttempt(
                    issue,
                    null,
                    workerSetup.primaryWorkerHost(),
                    event -> appendCodexEvent(eventsLog, event),
                    runtimeInfoRef::set,
                    () -> success.set(true),
                    failure::set);

            assertThat(failure.get()).as("live e2e failure").isNull();
            assertThat(success.get()).as("agent run success").isTrue();

            AgentRunner.RuntimeInfo runtimeInfo = runtimeInfoRef.get();
            assertThat(runtimeInfo).as("worker runtime info").isNotNull();

            assertThat(normalizeWorkerResult(readWorkerResult(runtimeInfo, RESULT_FILE)))
                    .isEqualTo(expectedResult(issue.identifier(), project.slugId()));

            LinearLiveTestClient.IssueDetails issueDetails =
                    waitForIssueOutcome(linear, issue.id(), expectedComment(issue.identifier(), project.slugId()));
            assertThat(issueDetails.completed()).isTrue();
            assertThat(issueDetails.comments().stream().map(SymphonyLiveE2EIT::normalizeCommentBody).toList())
                    .contains(normalizeCommentBody(expectedComment(issue.identifier(), project.slugId())));
        } finally {
            if (project != null && completedProjectStatus != null) {
                try {
                    linear.completeProject(project.id(), completedProjectStatus.id());
                } catch (Exception e) {
                    LOGGER.warn("Live e2e project finalization failed projectId={}", project.id(), e);
                }
            }
            if (workerSetup != null) {
                if (keepLiveArtifacts()) {
                    persistDebugArtifacts(runId, testRoot);
                    LOGGER.info("Keeping live e2e worker artifacts for inspection backend={} root={}", backend, testRoot);
                } else {
                    workerSetup.cleanup();
                }
            }
            if (keepLiveArtifacts()) {
                LOGGER.info("Keeping live e2e filesystem artifacts for inspection root={}", testRoot);
            } else {
                deleteRecursively(testRoot);
            }
        }
    }

    private LiveWorkerSetup liveWorkerSetup(Backend backend, String runId, Path testRoot) throws Exception {
        return switch (backend) {
            case LOCAL -> liveLocalWorkerSetup(testRoot);
            case SSH -> {
                List<String> hosts = configuredLiveSshWorkerHosts();
                yield hosts.isEmpty() ? liveDockerWorkerSetup(runId, testRoot) : liveExternalSshWorkerSetup(runId, hosts);
            }
        };
    }

    private LiveWorkerSetup liveLocalWorkerSetup(Path testRoot) {
        return new LiveWorkerSetup(
                "codex app-server",
                testRoot.resolve("workspaces").toString(),
                List.of(),
                null,
                () -> {});
    }

    private LiveWorkerSetup liveExternalSshWorkerSetup(String runId, List<String> workerHosts) throws Exception {
        String remoteHome = sharedRemoteHome(workerHosts);
        String remoteTestRoot = remoteHome + "/." + runId;
        String workspaceRoot = "~/.%s/workspaces".formatted(runId);
        return new LiveWorkerSetup(
                "codex app-server",
                workspaceRoot,
                workerHosts,
                workerHosts.get(0),
                () -> cleanupRemoteTestRoot(remoteTestRoot, workerHosts));
    }

    private LiveWorkerSetup liveDockerWorkerSetup(String runId, Path testRoot) throws Exception {
        requireCommand("ssh-keygen");
        List<String> composeCommand = dockerComposeCommand();
        String authJsonPath = DEFAULT_DOCKER_AUTH_JSON;
        if (!Files.isRegularFile(Path.of(authJsonPath))) {
            throw new IllegalStateException("docker worker mode requires auth file at " + authJsonPath);
        }

        Path sshRoot = testRoot.resolve("live-docker-ssh");
        Path keyPath = sshRoot.resolve("id_ed25519");
        Path configPath = sshRoot.resolve("config");
        List<Integer> workerPorts = reserveTcpPorts(DOCKER_WORKER_COUNT);
        List<String> workerHosts = workerPorts.stream().map(port -> "localhost:" + port).toList();
        String projectName = dockerProjectName(runId);
        String previousSshConfig = System.getProperty("symphony.ssh.config");
        Map<String, String> env = dockerComposeEnv(workerPorts, authJsonPath, keyPath + ".pub");

        try {
            Files.createDirectories(sshRoot);
            generateSshKeypair(keyPath);
            writeDockerSshConfig(configPath, keyPath);
            System.setProperty("symphony.ssh.config", configPath.toString());

            runLocalCommand(
                    composeCommandWith(composeCommand, projectName, "up", "-d", "--build"),
                    dockerSupportDir(),
                    env,
                    "failed to start live docker workers");
            waitForSshHosts(workerHosts, Duration.ofMinutes(1));

            String remoteHome = sharedRemoteHome(workerHosts);
            String remoteTestRoot = remoteHome + "/." + runId;
            String workspaceRoot = "~/.%s/workspaces".formatted(runId);

            return new LiveWorkerSetup(
                    "codex app-server",
                    workspaceRoot,
                    workerHosts,
                    workerHosts.get(0),
                    () -> {
                        cleanupRemoteTestRoot(remoteTestRoot, workerHosts);
                        dockerComposeDown(composeCommand, projectName, env);
                        restoreSystemProperty("symphony.ssh.config", previousSshConfig);
                    });
        } catch (Exception e) {
            dockerComposeDown(composeCommand, projectName, env);
            restoreSystemProperty("symphony.ssh.config", previousSshConfig);
            throw e;
        }
    }

    private SymphonyRuntimeSnapshot loadSnapshot(Path workflowFile) {
        WorkflowLoadResult loadResult = WorkflowLoader.load(workflowFile);
        if (loadResult instanceof WorkflowLoadResult.Error error) {
            throw new IllegalStateException(
                    "workflow load failed: " + error.code() + " / " + error.message());
        }
        return SymphonyRuntimeHolder.buildSnapshot(((WorkflowLoadResult.Success) loadResult).definition());
    }

    private void writeWorkflowFile(
            Path workflowFile, String projectSlug, LinearLiveTestClient.Team team, LiveWorkerSetup workerSetup)
            throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("---\n");
        builder.append("tracker:\n");
        builder.append("  kind: linear\n");
        builder.append("  api_key: ").append(yamlQuote("$LINEAR_API_KEY")).append('\n');
        builder.append("  project_slug: ").append(yamlQuote(projectSlug)).append('\n');
        appendYamlList(builder, "  active_states", team.activeStateNames());
        appendYamlList(builder, "  terminal_states", team.terminalStateNames());
        builder.append("workspace:\n");
        builder.append("  root: ").append(yamlQuote(workerSetup.workspaceRoot())).append('\n');
        if (!workerSetup.sshWorkerHosts().isEmpty()) {
            builder.append("worker:\n");
            appendYamlList(builder, "  ssh_hosts", workerSetup.sshWorkerHosts());
        }
        builder.append("agent:\n");
        builder.append("  max_turns: 3\n");
        builder.append("codex:\n");
        builder.append("  command: ").append(yamlQuote(workerSetup.codexCommand())).append('\n');
        builder.append("  approval_policy: never\n");
        builder.append("  turn_timeout_ms: 180000\n");
        builder.append("  stall_timeout_ms: 180000\n");
        builder.append("---\n\n");
        builder.append(livePrompt(projectSlug)).append('\n');
        Files.writeString(workflowFile, builder.toString(), StandardCharsets.UTF_8);
    }

    private LinearLiveTestClient.IssueDetails waitForIssueOutcome(
            LinearLiveTestClient linear, String issueId, String expectedCommentBody) throws Exception {
        long deadline = System.nanoTime() + Duration.ofMinutes(1).toNanos();
        LinearLiveTestClient.IssueDetails latest = null;
        Exception lastError = null;
        String normalizedExpectedComment = normalizeCommentBody(expectedCommentBody);
        while (System.nanoTime() < deadline) {
            try {
                latest = linear.fetchIssueDetails(issueId);
                boolean hasExpectedComment = latest.comments().stream()
                        .map(SymphonyLiveE2EIT::normalizeCommentBody)
                        .anyMatch(normalizedExpectedComment::equals);
                if (latest.completed() && hasExpectedComment) {
                    return latest;
                }
                lastError = null;
            } catch (Exception e) {
                lastError = e;
            }
            Thread.sleep(1_000L);
        }
        if (lastError != null) {
            throw new IllegalStateException("timed out waiting for issue outcome after transient errors", lastError);
        }
        throw new IllegalStateException("timed out waiting for issue completion/comment: " + latest);
    }

    private String readWorkerResult(AgentRunner.RuntimeInfo runtimeInfo, String resultFile) throws Exception {
        Path resultPath = Path.of(runtimeInfo.workspacePath()).resolve(resultFile);
        if (runtimeInfo.workerHost() == null || runtimeInfo.workerHost().isBlank()) {
            return Files.readString(resultPath);
        }
        SshClient.CommandResult result =
                SshClient.run(runtimeInfo.workerHost(), "cat " + SshClient.shellEscape(resultPath.toString()));
        if (result.exitCode() != 0) {
            throw new IllegalStateException(
                    "failed to read remote result from " + runtimeInfo.workerHost() + ": " + result.output());
        }
        return result.output();
    }

    private List<String> configuredLiveSshWorkerHosts() {
        String raw = System.getenv("SYMPHONY_LIVE_SSH_WORKER_HOSTS");
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        List<String> hosts = new ArrayList<>();
        for (String part : raw.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                hosts.add(trimmed);
            }
        }
        return List.copyOf(hosts);
    }

    private String liveTeamKey() {
        String teamKey = System.getenv("SYMPHONY_LIVE_LINEAR_TEAM_KEY");
        if (teamKey == null || teamKey.isBlank()) {
            return DEFAULT_TEAM_KEY;
        }
        return teamKey.trim();
    }

    private Path dockerSupportDir() {
        return repositoryRoot().resolve("artemis-symphony").resolve("test-support").resolve("live-e2e-docker");
    }

    private Path repositoryRoot() {
        String property = System.getProperty("maven.multiModuleProjectDirectory");
        if (property != null && !property.isBlank()) {
            return Path.of(property).toAbsolutePath().normalize();
        }
        Path cursor = Path.of("").toAbsolutePath().normalize();
        while (cursor != null) {
            if (Files.isDirectory(cursor.resolve(".git"))) {
                return cursor;
            }
            cursor = cursor.getParent();
        }
        throw new IllegalStateException("unable to resolve repository root");
    }

    private List<String> dockerComposeCommand() {
        if (commandRuns(List.of("docker", "compose", "version"))) {
            ensureDockerDaemon();
            return List.of("docker", "compose");
        }
        if (commandRuns(List.of("docker-compose", "version"))) {
            return List.of("docker-compose");
        }
        throw new IllegalStateException("docker compose is required for live docker ssh workers");
    }

    private boolean commandRuns(List<String> command) {
        try {
            CommandResult result = runLocalCommand(command, repositoryRoot(), Map.of(), null);
            return result.exitCode() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void requireCommand(String command) {
        if (!commandRuns(List.of("bash", "-lc", "command -v " + command + " >/dev/null 2>&1"))) {
            throw new IllegalStateException("required command is not available on PATH: " + command);
        }
    }

    private void ensureDockerDaemon() {
        if (!commandRuns(List.of("docker", "info"))) {
            throw new IllegalStateException("docker daemon is not running for live docker ssh workers");
        }
    }

    private void generateSshKeypair(Path keyPath) throws Exception {
        deleteRecursively(keyPath);
        deleteRecursively(Path.of(keyPath + ".pub"));
        Files.createDirectories(keyPath.getParent());
        CommandResult result = runLocalCommand(
                List.of("ssh-keygen", "-q", "-t", "ed25519", "-N", "", "-f", keyPath.toString()),
                repositoryRoot(),
                Map.of(),
                "failed to generate live docker ssh key");
        if (result.exitCode() != 0) {
            throw new IllegalStateException("failed to generate ssh key: " + result.output());
        }
    }

    private void writeDockerSshConfig(Path configPath, Path keyPath) throws IOException {
        String contents = """
                Host localhost 127.0.0.1
                  User root
                  IdentityFile %s
                  IdentitiesOnly yes
                  StrictHostKeyChecking no
                  UserKnownHostsFile /dev/null
                  LogLevel ERROR
                """
                .formatted(keyPath.toString());
        Files.createDirectories(configPath.getParent());
        Files.writeString(configPath, contents, StandardCharsets.UTF_8);
    }

    private String sharedRemoteHome(List<String> workerHosts) throws Exception {
        String home = remoteHome(workerHosts.get(0));
        for (int i = 1; i < workerHosts.size(); i++) {
            String other = remoteHome(workerHosts.get(i));
            if (!home.equals(other)) {
                throw new IllegalStateException("expected all live SSH workers to share one home directory");
            }
        }
        return home;
    }

    private String remoteHome(String workerHost) throws Exception {
        SshClient.CommandResult result = SshClient.run(workerHost, "printf '%s\\n' \"$HOME\"");
        if (result.exitCode() != 0) {
            throw new IllegalStateException("failed to resolve remote home for " + workerHost + ": " + result.output());
        }
        String home = result.output().trim();
        if (home.isBlank()) {
            throw new IllegalStateException("expected non-empty remote home for " + workerHost);
        }
        return home;
    }

    private void cleanupRemoteTestRoot(String testRoot, List<String> workerHosts) {
        for (String workerHost : workerHosts) {
            try {
                SshClient.run(workerHost, "rm -rf " + SshClient.shellEscape(testRoot));
            } catch (Exception e) {
                LOGGER.warn("failed to cleanup remote live e2e root host={} root={}", workerHost, testRoot, e);
            }
        }
    }

    private void waitForSshHosts(List<String> workerHosts, Duration timeout) throws Exception {
        long deadline = System.nanoTime() + timeout.toNanos();
        for (String workerHost : workerHosts) {
            waitForSshHost(workerHost, deadline);
        }
    }

    private void waitForSshHost(String workerHost, long deadlineNanos) throws Exception {
        while (System.nanoTime() < deadlineNanos) {
            try {
                SshClient.CommandResult result = SshClient.run(workerHost, "printf ready");
                if (result.exitCode() == 0 && "ready".equals(result.output())) {
                    return;
                }
            } catch (Exception ignored) {
                // 继续等待 ssh 就绪
            }
            Thread.sleep(1_000L);
        }
        throw new IllegalStateException("timed out waiting for SSH worker " + workerHost);
    }

    private List<Integer> reserveTcpPorts(int count) throws IOException {
        List<Integer> ports = new ArrayList<>();
        while (ports.size() < count) {
            int port = reserveTcpPort();
            if (!ports.contains(port)) {
                ports.add(port);
            }
        }
        return ports;
    }

    private int reserveTcpPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private String dockerProjectName(String runId) {
        return runId.toLowerCase().replaceAll("[^a-z0-9_-]", "-");
    }

    private Map<String, String> dockerComposeEnv(List<Integer> workerPorts, String authJsonPath, String authorizedKey) {
        Map<String, String> env = new LinkedHashMap<>();
        env.put("SYMPHONY_LIVE_DOCKER_AUTH_JSON", authJsonPath);
        env.put("SYMPHONY_LIVE_DOCKER_AUTHORIZED_KEY", authorizedKey);
        env.put("SYMPHONY_LIVE_DOCKER_WORKER_1_PORT", Integer.toString(workerPorts.get(0)));
        env.put("SYMPHONY_LIVE_DOCKER_WORKER_2_PORT", Integer.toString(workerPorts.get(1)));
        return env;
    }

    private List<String> composeCommandWith(List<String> baseCommand, String projectName, String... extraArgs) {
        List<String> command = new ArrayList<>(baseCommand);
        command.add("-f");
        command.add(dockerSupportDir().resolve("docker-compose.yml").toString());
        command.add("-p");
        command.add(projectName);
        for (String extraArg : extraArgs) {
            command.add(extraArg);
        }
        return command;
    }

    private void dockerComposeDown(List<String> composeCommand, String projectName, Map<String, String> env) {
        try {
            List<String> command = new ArrayList<>(composeCommand);
            command.add("-f");
            command.add(dockerSupportDir().resolve("docker-compose.yml").toString());
            command.add("-p");
            command.add(projectName);
            command.add("down");
            command.add("-v");
            command.add("--remove-orphans");
            runLocalCommand(command, dockerSupportDir(), env, null);
        } catch (Exception e) {
            LOGGER.warn("failed to shutdown live docker workers project={}", projectName, e);
        }
    }

    private CommandResult runLocalCommand(
            List<String> command, Path cwd, Map<String, String> env, String errorContext) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        if (cwd != null) {
            processBuilder.directory(cwd.toFile());
        }
        processBuilder.redirectErrorStream(true);
        if (env != null && !env.isEmpty()) {
            processBuilder.environment().putAll(env);
        }
        Process process = processBuilder.start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();
        if (errorContext != null && exitCode != 0) {
            throw new IllegalStateException(errorContext + " (status " + exitCode + "): " + output);
        }
        return new CommandResult(output, exitCode);
    }

    private static void restoreSystemProperty(String name, String previousValue) {
        if (previousValue == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, previousValue);
        }
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("required environment variable is missing: " + name);
        }
        return value;
    }

    private static boolean keepLiveArtifacts() {
        String value = System.getenv("SYMPHONY_LIVE_E2E_KEEP_ARTIFACTS");
        return "1".equals(value) || "true".equalsIgnoreCase(value);
    }

    private void persistDebugArtifacts(String runId, Path sourceRoot) {
        try {
            Path debugRoot = repositoryRoot()
                    .resolve("artemis-symphony")
                    .resolve("artemis-symphony-start")
                    .resolve("target")
                    .resolve("live-e2e-debug")
                    .resolve(runId);
            if (Files.exists(debugRoot)) {
                deleteRecursively(debugRoot);
            }
            Files.createDirectories(debugRoot);
            if (!Files.exists(sourceRoot)) {
                return;
            }
            try (var stream = Files.walk(sourceRoot)) {
                for (Path source : stream.toList()) {
                    Path relative = sourceRoot.relativize(source);
                    Path target = debugRoot.resolve(relative.toString());
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        Files.copy(source, target);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("failed to persist live e2e debug artifacts runId={} sourceRoot={}", runId, sourceRoot, e);
        }
    }

    private static void appendYamlList(StringBuilder builder, String key, List<String> values) {
        builder.append(key).append(":\n");
        for (String value : values) {
            builder.append("    - ").append(yamlQuote(value)).append('\n');
        }
    }

    private static String yamlQuote(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    private static String livePrompt(String projectSlug) {
        return """
                You are running a real Symphony end-to-end test.

                The current working directory is the workspace root.

                Step 1:
                Create a file named %1$s in the current working directory by running exactly:

                ```sh
                cat > %1$s <<'EOF'
                identifier={{ issue.identifier }}
                project_slug=%2$s
                EOF
                ```

                Then verify it by running:

                ```sh
                cat %1$s
                ```

                The file content must be exactly:
                identifier={{ issue.identifier }}
                project_slug=%2$s

                The newline between those two lines is required.

                Step 2:
                You must use the `linear_graphql` tool to query the current issue by `{{ issue.id }}` and read:
                - existing comments
                - team workflow states

                A turn that only creates the file is incomplete. Do not stop after Step 1.

                If the exact comment body below is not already present, post exactly one comment on the current issue with this exact body:
                %3$s

                Use these exact GraphQL operations:

                ```graphql
                query IssueContext($id: String!) {
                  issue(id: $id) {
                    comments(first: 20) {
                      nodes {
                        body
                      }
                    }
                    team {
                      states(first: 50) {
                        nodes {
                          id
                          name
                          type
                        }
                      }
                    }
                  }
                }
                ```

                ```graphql
                mutation AddComment($issueId: String!, $body: String!) {
                  commentCreate(input: {issueId: $issueId, body: $body}) {
                    success
                  }
                }
                ```

                Step 3:
                Use the same issue-context query result to choose a workflow state whose `type` is `completed`.
                Then move the current issue to that state with this exact mutation:

                ```graphql
                mutation CompleteIssue($id: String!, $stateId: String!) {
                  issueUpdate(id: $id, input: {stateId: $stateId}) {
                    success
                  }
                }
                ```

                Step 4:
                Verify all outcomes with one final `linear_graphql` query against `{{ issue.id }}`:
                - the exact comment body is present
                - the issue state type is `completed`

                Do not ask for approval.
                Stop only after all three conditions are true:
                1. the file exists with the exact contents above
                2. the Linear comment exists with the exact body above
                3. the Linear issue is in a completed terminal state
                """
                .formatted(RESULT_FILE, projectSlug, expectedComment("{{ issue.identifier }}", projectSlug));
    }

    private static String expectedResult(String issueIdentifier, String projectSlug) {
        return "identifier=%s%nproject_slug=%s%n".formatted(issueIdentifier, projectSlug);
    }

    private static String expectedComment(String issueIdentifier, String projectSlug) {
        return "Symphony live e2e comment%nidentifier=%s%nproject_slug=%s".formatted(issueIdentifier, projectSlug);
    }

    private static String normalizeWorkerResult(String actual) {
        String normalized = normalizeIdentifierProjectBlock(actual);
        if (!normalized.endsWith("\n")) {
            normalized = normalized + "\n";
        }
        return normalized;
    }

    private static String normalizeCommentBody(String actual) {
        return normalizeIdentifierProjectBlock(actual).stripTrailing();
    }

    private static String normalizeIdentifierProjectBlock(String actual) {
        String normalized = actual.replace("\r\n", "\n").replace('\r', '\n');
        return normalized.replaceFirst("(?m)^(identifier=[^\\n]+)(project_slug=)", "$1\n$2");
    }

    private static void appendCodexEvent(Path logFile, com.aotemiao.artemis.symphony.core.model.CodexUpdateEvent event) {
        String line = "%s %s %s%n"
                .formatted(
                        event.timestamp(),
                        event.event(),
                        event.payload() != null ? event.payload() : Map.of());
        try {
            Files.writeString(
                    logFile,
                    line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // 事件日志仅用于诊断，不阻断 live e2e
        }
    }

    private static void deleteRecursively(Path path) {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try {
            if (Files.isDirectory(path)) {
                try (var children = Files.list(path)) {
                    for (Path child : children.toList()) {
                        deleteRecursively(child);
                    }
                }
            }
            Files.deleteIfExists(path);
        } catch (Exception e) {
            LOGGER.warn("failed to delete live e2e temp path {}", path, e);
        }
    }

    private enum Backend {
        LOCAL,
        SSH
    }

    private record LiveWorkerSetup(
            String codexCommand,
            String workspaceRoot,
            List<String> sshWorkerHosts,
            String primaryWorkerHost,
            ThrowingRunnable cleanupAction) {

        private void cleanup() throws Exception {
            cleanupAction.run();
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private record CommandResult(String output, int exitCode) {}
}
