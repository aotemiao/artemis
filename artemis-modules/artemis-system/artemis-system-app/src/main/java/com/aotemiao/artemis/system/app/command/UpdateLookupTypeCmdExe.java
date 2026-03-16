package com.aotemiao.artemis.system.app.command;

import com.aotemiao.artemis.system.domain.gateway.LookupTypeGateway;
import com.aotemiao.artemis.system.domain.model.LookupItem;
import com.aotemiao.artemis.system.domain.model.LookupType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UpdateLookupTypeCmdExe {

    private final LookupTypeGateway lookupTypeGateway;

    public UpdateLookupTypeCmdExe(LookupTypeGateway lookupTypeGateway) {
        this.lookupTypeGateway = lookupTypeGateway;
    }

    public LookupType execute(UpdateLookupTypeCmd cmd) {
        LookupType t = lookupTypeGateway.findById(cmd.id())
                .orElseThrow(() -> new IllegalArgumentException("LookupType not found: " + cmd.id()));
        t.setCode(cmd.code());
        t.setName(cmd.name());
        t.setDescription(cmd.description());
        if (cmd.items() != null) {
            List<LookupItem> items = new ArrayList<>();
            for (var i : cmd.items()) {
                LookupItem item = new LookupItem();
                item.setValue(i.value());
                item.setLabel(i.label());
                item.setSortOrder(i.sortOrder() != null ? i.sortOrder() : 0);
                items.add(item);
            }
            t.setItems(items);
        }
        return lookupTypeGateway.save(t);
    }
}
