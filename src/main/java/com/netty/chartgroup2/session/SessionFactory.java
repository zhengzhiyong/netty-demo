package com.netty.chartgroup2.session;

import com.netty.chartgroup2.session.service.Session;

public abstract class SessionFactory {

    private static Session session = new SessionMemoryImpl();

    public static Session getSession() {
        return session;
    }
}

