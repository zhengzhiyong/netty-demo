package com.netty.chartgroup.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Scanner;

public class ServerHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // 通过ctx获取客户端的IP和端口号，并打印出客户端发来的消息
        System.out.println("服务端接收的请求来自："+ctx.channel().remoteAddress()+",消息内容："+msg);
        System.out.println("请向客户端发送一条消息：");
        String sendMsg = new Scanner(System.in).nextLine();
        ctx.channel().writeAndFlush(sendMsg);
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        ctx.writeAndFlush("第一条消息...");
    }
}
