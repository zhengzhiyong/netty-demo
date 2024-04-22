package com.netty.chartgroup2.user;

import com.netty.chartgroup2.user.service.UserService;

public abstract class UserServiceFactory {

    private static UserService userService = new UserServiceMemoryImpl();

    public static UserService getUserService() {
        return userService;
    }
}

