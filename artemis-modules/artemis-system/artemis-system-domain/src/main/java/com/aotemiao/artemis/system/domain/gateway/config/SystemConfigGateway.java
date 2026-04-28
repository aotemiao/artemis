package com.aotemiao.artemis.system.domain.gateway.config;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.model.config.SystemConfig;
import java.util.Optional;

/** 系统参数配置 Gateway。 */
public interface SystemConfigGateway {

    SystemConfig save(SystemConfig systemConfig);

    Optional<SystemConfig> findById(Long id);

    Optional<SystemConfig> findByConfigKey(String configKey);

    PageResult<SystemConfig> findPage(PageRequest pageRequest);

    void deleteById(Long id);
}
