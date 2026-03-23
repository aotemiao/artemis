package com.aotemiao.artemis.system.app.command;

import java.util.List;

public record UpdateLookupTypeCmd(
        Long id, String code, String name, String description, List<CreateLookupTypeCmd.LookupItemCmd> items) {

    public UpdateLookupTypeCmd {
        items = items != null ? List.copyOf(items) : null;
    }
}
