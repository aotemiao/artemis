package com.aotemiao.artemis.resource.domain.gateway.config;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.resource.domain.model.config.OssConfig;
import java.util.Optional;

/** 对象存储配置 Gateway。 */
public interface OssConfigGateway {

    OssConfig save(OssConfig ossConfig);

    Optional<OssConfig> findById(Long id);

    Optional<OssConfig> findByConfigKey(String configKey);

    PageResult<OssConfig> findPage(PageRequest pageRequest);

    void deleteById(Long id);

    void clearDefaultExcept(Long id);
}
