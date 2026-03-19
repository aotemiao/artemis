package com.aotemiao.artemis.symphony.config;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves $VAR_NAME from environment and ~ for home in path-like values. SPEC 6.1.
 */
public final class ConfigResolver {

    private static final Pattern ENV_VAR = Pattern.compile("\\$([A-Za-z_][A-Za-z0-9_]*)");

    private ConfigResolver() {}

    /**
     * Resolve a value: expand $VAR from environment; do not expand URIs or arbitrary strings.
     * Used for api_key and path fields.
     */
    public static String resolveEnv(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (!value.contains("$")) {
            return value;
        }
        StringBuffer sb = new StringBuffer();
        Matcher m = ENV_VAR.matcher(value);
        while (m.find()) {
            String varName = m.group(1);
            String env = System.getenv(varName);
            m.appendReplacement(sb, Matcher.quoteReplacement(env != null ? env : ""));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Expand ~ to user home for path-like values. Path separators trigger expansion.
     */
    public static String expandHome(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        if ("~".equals(path) || path.startsWith("~/") || path.startsWith("~\\")) {
            String home = System.getProperty("user.home", "");
            return home + path.substring(1);
        }
        return path;
    }

    @SuppressWarnings("unchecked")
    public static Object getNested(Map<String, Object> config, String keyPath) {
        if (config == null || keyPath == null || keyPath.isEmpty()) {
            return null;
        }
        String[] parts = keyPath.split("\\.");
        Object current = config;
        for (String part : parts) {
            if (current instanceof Map<?, ?> map) {
                current = ((Map<String, Object>) map).get(part);
            } else {
                return null;
            }
        }
        return current;
    }
}
