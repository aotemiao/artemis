package com.aotemiao.artemis.symphony.config;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 解析环境变量 {@code $VAR_NAME} 与路径中的 {@code ~}（用户主目录）。见 SPEC 第 6.1 节。 */
public final class ConfigResolver {

    private static final Pattern ENV_VAR = Pattern.compile("\\$([A-Za-z_][A-Za-z0-9_]*)");

    private ConfigResolver() {}

    /**
     * 解析取值：将 {@code $VAR} 展开为环境变量；不对 URI 等随意字符串做额外展开。用于 api_key、路径类字段等。
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

    /** 将路径形式值中的 {@code ~} 展开为用户主目录。 */
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
