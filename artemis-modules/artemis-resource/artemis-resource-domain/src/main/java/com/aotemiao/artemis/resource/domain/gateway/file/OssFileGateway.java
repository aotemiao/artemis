package com.aotemiao.artemis.resource.domain.gateway.file;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.resource.domain.model.file.OssFile;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** OSS 文件记录 Gateway。 */
public interface OssFileGateway {

    OssFile save(OssFile ossFile);

    Optional<OssFile> findById(Long id);

    List<OssFile> findByIds(Collection<Long> ids);

    PageResult<OssFile> findPage(PageRequest pageRequest);

    void deleteById(Long id);
}
