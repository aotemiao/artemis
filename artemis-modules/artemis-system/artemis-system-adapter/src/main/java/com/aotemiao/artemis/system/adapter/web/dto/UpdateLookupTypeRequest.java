package com.aotemiao.artemis.system.adapter.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateLookupTypeRequest(
        @NotNull Long id,
        String code,
        String name,
        String description,
        List<CreateLookupTypeRequest.LookupItemRequest> items) {}
