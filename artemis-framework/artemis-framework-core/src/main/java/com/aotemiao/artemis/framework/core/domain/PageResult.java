package com.aotemiao.artemis.framework.core.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 与具体框架无关的分页结果：当前页内容、总条数、总页数。
 *
 * @param <T> 行数据类型
 */
public record PageResult<T>(List<T> content, long total, int totalPages) implements Serializable {

    public PageResult {
        content = content != null ? Collections.unmodifiableList(new ArrayList<>(content)) : Collections.emptyList();
        if (totalPages < 0 && !content.isEmpty()) totalPages = (int) ((total + content.size() - 1) / content.size());
        if (totalPages < 0) totalPages = total == 0 ? 0 : 1;
    }

    /** 总条数。 */
    public long total() {
        return total;
    }

    /** 当前页内容（行数据）。 */
    public List<T> content() {
        return new ArrayList<>(content);
    }

    /** 总页数。 */
    public int totalPages() {
        return totalPages;
    }

    /** content 的别名，用于向后兼容。 */
    public List<T> rows() {
        return new ArrayList<>(content);
    }

    public static <T> PageResult<T> of(long total, List<T> rows) {
        List<T> list = rows != null ? rows : Collections.emptyList();
        int pageSize = Math.max(1, list.size());
        int totalPages = total == 0 ? 0 : (int) ((total + pageSize - 1) / pageSize);
        return new PageResult<>(list, total, totalPages);
    }

    public static <T> PageResult<T> of(long total, List<T> content, int pageSize) {
        int totalPages = pageSize <= 0 ? 0 : (int) ((total + pageSize - 1) / pageSize);
        return new PageResult<>(content != null ? content : Collections.emptyList(), total, totalPages);
    }
}
