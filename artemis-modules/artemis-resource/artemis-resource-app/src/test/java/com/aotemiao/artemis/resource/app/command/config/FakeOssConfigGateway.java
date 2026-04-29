package com.aotemiao.artemis.resource.app.command.config;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.resource.domain.gateway.config.OssConfigGateway;
import com.aotemiao.artemis.resource.domain.model.config.OssConfig;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

final class FakeOssConfigGateway implements OssConfigGateway {

    private final Map<Long, OssConfig> storage = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public OssConfig save(OssConfig ossConfig) {
        if (ossConfig.getId() == null) {
            ossConfig.setId(nextId++);
        }
        storage.put(ossConfig.getId(), ossConfig);
        return ossConfig;
    }

    @Override
    public Optional<OssConfig> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<OssConfig> findByConfigKey(String configKey) {
        return storage.values().stream()
                .filter(config -> config.getConfigKey().equals(configKey))
                .findFirst();
    }

    @Override
    public PageResult<OssConfig> findPage(PageRequest pageRequest) {
        return PageResult.of(storage.size(), storage.values().stream().toList(), 1);
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }

    @Override
    public void clearDefaultExcept(Long id) {
        storage.values().stream()
                .filter(config -> !config.getId().equals(id))
                .forEach(config -> config.setDefaultFlag(0));
    }
}
