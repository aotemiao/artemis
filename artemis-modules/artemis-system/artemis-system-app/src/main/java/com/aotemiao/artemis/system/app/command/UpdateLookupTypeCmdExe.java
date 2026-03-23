package com.aotemiao.artemis.system.app.command;

import com.aotemiao.artemis.system.domain.gateway.LookupTypeGateway;
import com.aotemiao.artemis.system.domain.model.LookupItem;
import com.aotemiao.artemis.system.domain.model.LookupType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UpdateLookupTypeCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final LookupTypeGateway lookupTypeGateway;

    public UpdateLookupTypeCmdExe(LookupTypeGateway lookupTypeGateway) {
        this.lookupTypeGateway = lookupTypeGateway;
    }

    public LookupType execute(UpdateLookupTypeCmd cmd) {
        LookupType t = lookupTypeGateway
                .findById(cmd.id())
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
