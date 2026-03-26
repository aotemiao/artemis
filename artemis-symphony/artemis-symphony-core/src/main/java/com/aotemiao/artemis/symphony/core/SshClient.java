package com.aotemiao.artemis.symphony.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** 通过本机 OpenSSH 与远端 worker 建立一次性命令执行或长连 stdio 会话。 */
public final class SshClient {

    private SshClient() {}

    public static CommandResult run(String host, String command) throws IOException, InterruptedException {
        Process process =
                processBuilder(host, command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();
        return new CommandResult(output, exitCode);
    }

    public static ProcessBuilder processBuilder(String host, String command) {
        String executable =
                firstNonBlank(System.getProperty("symphony.ssh.executable"), System.getenv("SYMPHONY_SSH_EXECUTABLE"));
        if (executable == null || executable.isBlank()) {
            executable = "ssh";
        }
        ParsedTarget target = parseTarget(host);
        List<String> args = new ArrayList<>();
        args.add(executable);
        String configPath =
                firstNonBlank(System.getProperty("symphony.ssh.config"), System.getenv("SYMPHONY_SSH_CONFIG"));
        if (configPath != null && !configPath.isBlank()) {
            args.add("-F");
            args.add(configPath);
        }
        args.add("-T");
        if (target.port() != null) {
            args.add("-p");
            args.add(target.port());
        }
        args.add(target.destination());
        args.add(remoteShellCommand(command));
        return new ProcessBuilder(args);
    }

    public static String remoteShellCommand(String command) {
        return "bash -lc " + shellEscape(command);
    }

    public static String shellEscape(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    static ParsedTarget parseTarget(String target) {
        String trimmed = target == null ? "" : target.trim();
        int lastColon = trimmed.lastIndexOf(':');
        if (lastColon > 0 && lastColon < trimmed.length() - 1) {
            String destination = trimmed.substring(0, lastColon);
            String port = trimmed.substring(lastColon + 1);
            if (port.chars().allMatch(Character::isDigit) && validPortDestination(destination)) {
                return new ParsedTarget(destination, port);
            }
        }
        return new ParsedTarget(trimmed, null);
    }

    private static boolean validPortDestination(String destination) {
        return !destination.isBlank() && (!destination.contains(":") || bracketedHost(destination));
    }

    private static boolean bracketedHost(String destination) {
        return destination.contains("[") && destination.contains("]");
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return (second != null && !second.isBlank()) ? second : null;
    }

    public record CommandResult(String output, int exitCode) {}

    record ParsedTarget(String destination, String port) {}
}
