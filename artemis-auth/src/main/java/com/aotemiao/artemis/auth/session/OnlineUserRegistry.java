package com.aotemiao.artemis.auth.session;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/** 当前认证实例内的在线用户索引。 */
@Component
public class OnlineUserRegistry {

    private final ConcurrentMap<Long, OnlineUser> onlineUsers = new ConcurrentHashMap<>();

    public void put(OnlineUser onlineUser) {
        onlineUsers.put(onlineUser.userId(), onlineUser);
    }

    public void remove(Long userId) {
        if (userId != null) {
            onlineUsers.remove(userId);
        }
    }

    public List<OnlineUser> list(String username, String ipaddr) {
        return onlineUsers.values().stream()
                .filter(user -> contains(user.username(), username))
                .filter(user -> contains(user.ipaddr(), ipaddr))
                .sorted(Comparator.comparing(OnlineUser::loginTime).reversed())
                .toList();
    }

    private static boolean contains(String value, String expected) {
        return expected == null || expected.isBlank() || (value != null && value.contains(expected));
    }
}
