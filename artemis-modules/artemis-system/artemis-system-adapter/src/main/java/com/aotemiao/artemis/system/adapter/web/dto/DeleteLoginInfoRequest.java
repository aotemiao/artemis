package com.aotemiao.artemis.system.adapter.web.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class DeleteLoginInfoRequest {

    @NotEmpty(message = "ids must not be empty")
    private List<Long> ids = List.of();

    public List<Long> ids() {
        return List.copyOf(ids);
    }

    public void setIds(List<Long> ids) {
        this.ids = ids == null ? List.of() : List.copyOf(ids);
    }
}
