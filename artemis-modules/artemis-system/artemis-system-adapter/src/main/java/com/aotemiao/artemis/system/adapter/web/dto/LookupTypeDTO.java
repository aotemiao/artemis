package com.aotemiao.artemis.system.adapter.web.dto;

import java.io.Serializable;
import java.util.List;

public record LookupTypeDTO(Long id, String code, String name, String description, List<LookupItemDTO> items)
        implements Serializable {

    public LookupTypeDTO {
        items = items != null ? List.copyOf(items) : null;
    }
}
