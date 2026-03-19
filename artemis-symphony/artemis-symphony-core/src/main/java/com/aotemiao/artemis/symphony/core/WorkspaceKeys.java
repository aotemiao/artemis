package com.aotemiao.artemis.symphony.core;

/**
 * Workspace key sanitization per SPEC Section 4.2: replace any character not in [A-Za-z0-9._-] with _.
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
