package com.aotemiao.artemis.resource.infra.gateway.notify;

import com.aotemiao.artemis.resource.domain.gateway.notify.SmsBlacklistGateway;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemorySmsBlacklistGateway implements SmsBlacklistGateway {

    private final Set<String> phones = ConcurrentHashMap.newKeySet();

    @Override
    public boolean contains(String phone) {
        return phones.contains(phone);
    }

    @Override
    public void add(String phone) {
        phones.add(phone);
    }

    @Override
    public void remove(String phone) {
        phones.remove(phone);
    }
}
