package com.aotemiao.artemis.system.adapter.web.dto.lookup;

import java.io.Serializable;

public record LookupItemDTO(Long id, String value, String label, Integer sortOrder) implements Serializable {}
