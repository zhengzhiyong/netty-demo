package com.netty.chartgroup.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Scanner;

public class ClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("客户端接收的请求来自："+ctx.channel().remoteAddress()+",消息内容："+msg);
        System.out.println("请向服务端发送一条消息：");
        String sendMsg = new Scanner(System.in).nextLine();
        ctx.channel().writeAndFlush(sendMsg);
    }
}
