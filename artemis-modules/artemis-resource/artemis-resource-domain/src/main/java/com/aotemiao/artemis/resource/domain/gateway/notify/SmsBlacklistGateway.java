package com.aotemiao.artemis.resource.domain.gateway.notify;

public interface SmsBlacklistGateway {

    boolean contains(String phone);

    void add(String phone);

    void remove(String phone);
}
