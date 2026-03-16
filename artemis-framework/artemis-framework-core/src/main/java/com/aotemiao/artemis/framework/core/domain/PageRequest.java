package com.aotemiao.artemis.framework.core.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 与具体框架无关的分页请求：页码、每页条数及可选排序。
 */
public record PageRequest(int page, int size, List<SortOrder> sort) implements Serializable {

    public PageRequest {
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        sort = sort != null ? List.copyOf(sort) : List.of();
    }

    public PageRequest(int page, int size) {
        this(page, size, List.of());
    }

    /** 从 0 开始的页码。 */
    public int getPage() {
        return page;
    }

    /** 每页条数。 */
    public int getSize() {
        return size;
    }

    /** 排序（属性、方向）。 */
    public List<SortOrder> getSort() {
        return sort;
    }

    /** 单条排序。 */
    public record SortOrder(String property, boolean ascending) implements Serializable {}
}
