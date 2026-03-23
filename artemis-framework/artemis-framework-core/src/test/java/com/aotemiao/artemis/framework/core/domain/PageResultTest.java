package com.aotemiao.artemis.framework.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PageResultTest {

    @Test
    void constructor_createsDefensiveUnmodifiableCopy() {
        List<String> source = new ArrayList<>(List.of("A", "B"));

        PageResult<String> pageResult = PageResult.of(2, source, 10);
        source.add("C");
        List<String> returned = pageResult.content();
        returned.add("D");

        assertEquals(List.of("A", "B"), pageResult.content());
        assertEquals(List.of("A", "B"), pageResult.rows());
    }
}
