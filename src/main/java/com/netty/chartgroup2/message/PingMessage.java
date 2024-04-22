package com.netty.chartgroup2.message;

public class PingMessage extends Message{

    @Override
    public int getMessageType() {
        return PingMessage;
    }
}

