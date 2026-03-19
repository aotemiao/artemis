package com.aotemiao.artemis.framework.jdbc.support;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** 在框架无关分页（core）与 Spring Data 分页之间转换。 */
public final class PageConversion {

    private PageConversion() {}

    /** 将 core PageRequest 转为 Spring Data Pageable。 */
    public static Pageable toPageable(PageRequest request) {
        if (request == null) {
            return Pageable.unpaged();
        }
        Sort sort = request.getSort().isEmpty()
                ? Sort.unsorted()
                : Sort.by(request.getSort().stream()
                        .map(o -> o.ascending() ? Sort.Order.asc(o.property()) : Sort.Order.desc(o.property()))
                        .toList());
        return org.springframework.data.domain.PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    /** 将 Spring Data Page 转为 core PageResult。 */
    public static <T> PageResult<T> toPageResult(Page<T> page) {
        if (page == null) {
            return PageResult.of(0, List.of());
        }
        return PageResult.of(page.getTotalElements(), page.getContent(), page.getSize());
    }
}
