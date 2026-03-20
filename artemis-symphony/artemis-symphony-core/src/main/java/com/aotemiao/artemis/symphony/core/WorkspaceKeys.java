package com.aotemiao.artemis.symphony.core;

/**
 * 工作区目录键清理（SPEC 第 4.2 节）：将不在 [A-Za-z0-9._-] 内的字符替换为 {@code _}。
 */
public final class WorkspaceKeys {

    private WorkspaceKeys() {}

    public static String sanitize(String issueIdentifier) {
        if (issueIdentifier == null || issueIdentifier.isEmpty()) {
            return "unknown";
        }
        StringBuilder sb = new StringBuilder(issueIdentifier.length());
        for (int i = 0; i < issueIdentifier.length(); i++) {
            char c = issueIdentifier.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '.' || c == '_' || c == '-') {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        return sb.toString();
    }
}
