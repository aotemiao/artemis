package com.aotemiao.artemis.system.app.command;

import com.aotemiao.artemis.system.domain.gateway.LookupTypeGateway;
import com.aotemiao.artemis.system.domain.model.LookupItem;
import com.aotemiao.artemis.system.domain.model.LookupType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CreateLookupTypeCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final LookupTypeGateway lookupTypeGateway;

    public CreateLookupTypeCmdExe(LookupTypeGateway lookupTypeGateway) {
        this.lookupTypeGateway = lookupTypeGateway;
    }

    public LookupType execute(CreateLookupTypeCmd cmd) {
        LookupType t = new LookupType();
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
