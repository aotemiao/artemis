package com.aotemiao.artemis.symphony.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.core.model.WorkflowDefinition;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WorkspaceManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void createForIssue_acceptsWorkspaceRootContainingDotSegment() throws Exception {
        String rootWithDotSegment =
                tempDir.resolve(".").resolve("symphony_workspaces").toString();
        ServiceConfig config = new ServiceConfig(
                new WorkflowDefinition(Map.of("workspace", Map.of("root", rootWithDotSegment)), "prompt"));
        WorkspaceManager manager = new WorkspaceManager(() -> config);

        WorkspaceManager.Result<com.aotemiao.artemis.symphony.core.model.Workspace> result =
                manager.createForIssue("AOT-5");

        assertTrue(result.isSuccess(), () -> "expected success but was " + result.errorCode());
        Path expected = tempDir.resolve("symphony_workspaces").resolve("AOT-5");
        assertEquals(expected, result.value().path());
        assertTrue(Files.isDirectory(expected));
    }

    @Test
    void createForIssue_supportsRemoteWorkerViaSshWrapper() throws Exception {
        Path fakeSsh = tempDir.resolve("fake-ssh.sh");
        Files.writeString(
                fakeSsh,
                """
                #!/usr/bin/env bash
                set -euo pipefail
                while [[ $# -gt 0 ]]; do
                  case "$1" in
                    -T)
                      shift
                      ;;
                    -p|-F)
                      shift 2
                      ;;
                    *)
                      if [[ -z "${destination:-}" ]]; then
                        destination="$1"
                        shift
                      else
                        break
                      fi
                      ;;
                  esac
                done
                exec bash -lc "$1"
                """);
        fakeSsh.toFile().setExecutable(true);

        String oldExecutable = System.getProperty("symphony.ssh.executable");
        System.setProperty("symphony.ssh.executable", fakeSsh.toString());
        try {
            Path remoteRoot = tempDir.resolve("remote-root");
            ServiceConfig config = new ServiceConfig(new WorkflowDefinition(
                    Map.of(
                            "workspace",
                            Map.of("root", remoteRoot.toString()),
                            "hooks",
                            Map.of("after_create", "touch created.txt")),
                    "prompt"));
            WorkspaceManager manager = new WorkspaceManager(() -> config);

            WorkspaceManager.Result<com.aotemiao.artemis.symphony.core.model.Workspace> result =
                    manager.createForIssue("AOT-remote", "fake-worker");

            assertTrue(result.isSuccess(), () -> "expected success but was " + result.errorCode());
            Path expected = remoteRoot.resolve("AOT-remote");
            assertTrue(Files.isSameFile(expected, result.value().path()));
            assertTrue(Files.isDirectory(expected));
            assertTrue(Files.exists(expected.resolve("created.txt")));

            manager.removeWorkspace(expected, "fake-worker");
            assertTrue(Files.notExists(expected));
        } finally {
            if (oldExecutable == null) {
                System.clearProperty("symphony.ssh.executable");
            } else {
                System.setProperty("symphony.ssh.executable", oldExecutable);
            }
        }
    }

    @Test
    void createForIssue_expandsRemoteTildeWorkspaceRoot() throws Exception {
        Path fakeSsh = tempDir.resolve("fake-ssh-home.sh");
        Files.writeString(
                fakeSsh,
                """
                #!/usr/bin/env bash
                set -euo pipefail
                while [[ $# -gt 0 ]]; do
                  case "$1" in
                    -T)
                      shift
                      ;;
                    -p|-F)
                      shift 2
                      ;;
                    *)
                      if [[ -z "${destination:-}" ]]; then
                        destination="$1"
                        shift
                      else
                        break
                      fi
                      ;;
                  esac
                done
                exec bash -lc "$1"
                """);
        fakeSsh.toFile().setExecutable(true);

        String oldExecutable = System.getProperty("symphony.ssh.executable");
        System.setProperty("symphony.ssh.executable", fakeSsh.toString());
        try {
            String tildeRoot = "~/.symphony_remote_workspace_test";
            ServiceConfig config = new ServiceConfig(
                    new WorkflowDefinition(Map.of("workspace", Map.of("root", tildeRoot)), "prompt"));
            WorkspaceManager manager = new WorkspaceManager(() -> config);

            WorkspaceManager.Result<com.aotemiao.artemis.symphony.core.model.Workspace> result =
                    manager.createForIssue("AOT-home", "fake-worker");

            assertTrue(result.isSuccess(), () -> "expected success but was " + result.errorCode());
            Path expected =
                    Path.of(System.getProperty("user.home"), ".symphony_remote_workspace_test", "AOT-home");
            assertTrue(Files.isSameFile(expected, result.value().path()));
            assertTrue(Files.isDirectory(expected));

            manager.removeWorkspace(expected, "fake-worker");
            assertTrue(Files.notExists(expected));
        } finally {
            if (oldExecutable == null) {
                System.clearProperty("symphony.ssh.executable");
            } else {
                System.setProperty("symphony.ssh.executable", oldExecutable);
            }
        }
    }
}
