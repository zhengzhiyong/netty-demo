package com.netty.chartgroup2.group;

import com.netty.chartgroup2.group.service.GroupSession;

public abstract class GroupSessionFactory {

    private static GroupSession session = new GroupSessionMemoryImpl();

    public static GroupSession getGroupSession() {
        return session;
    }
}

