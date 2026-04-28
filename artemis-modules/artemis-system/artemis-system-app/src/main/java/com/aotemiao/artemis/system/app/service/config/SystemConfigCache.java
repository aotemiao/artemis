package com.aotemiao.artemis.system.app.service.config;

import com.aotemiao.artemis.system.domain.gateway.config.SystemConfigGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** 系统参数本地缓存，后续可替换为 Redis 缓存实现。 */
@Component
public class SystemConfigCache {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this cache does not expose it.")
    private final SystemConfigGateway systemConfigGateway;

    private final Map<String, String> values = new ConcurrentHashMap<>();

    public SystemConfigCache(SystemConfigGateway systemConfigGateway) {
        this.systemConfigGateway = systemConfigGateway;
    }

    public Optional<String> getValue(String configKey) {
        if (configKey == null || configKey.isBlank()) {
            return Optional.empty();
        }
        String cached = values.get(configKey);
        if (cached != null) {
            return Optional.of(cached);
        }
        Optional<String> value = systemConfigGateway.findByConfigKey(configKey).map(config -> config.getConfigValue());
        value.ifPresent(v -> values.put(configKey, v));
        return value;
    }

    public void evict(String configKey) {
        if (configKey != null) {
            values.remove(configKey);
        }
    }

    public void refresh() {
        values.clear();
    }
}
