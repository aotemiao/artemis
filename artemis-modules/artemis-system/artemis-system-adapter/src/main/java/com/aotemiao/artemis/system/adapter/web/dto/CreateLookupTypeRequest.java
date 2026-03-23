package com.aotemiao.artemis.system.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateLookupTypeRequest(
        @NotBlank String code, String name, String description, List<LookupItemRequest> items) {

    public CreateLookupTypeRequest {
        items = items != null ? List.copyOf(items) : null;
    }

    public record LookupItemRequest(String value, String label, Integer sortOrder) {}
}
