package com.aotemiao.artemis.system.app.command;

import java.util.List;

public record CreateLookupTypeCmd(String code, String name, String description, List<LookupItemCmd> items) {

    public CreateLookupTypeCmd {
        items = items != null ? List.copyOf(items) : null;
    }

    public record LookupItemCmd(String value, String label, Integer sortOrder) {}
}
