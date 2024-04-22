package com.netty.chartgroup2.message;

public class PongMessage extends Message{

    @Override
    public int getMessageType() {
        return PongMessage;
    }
}

