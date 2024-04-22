package com.netty.chartgroup2.handler;

import com.netty.chartgroup2.message.ChatRequestMessage;
import com.netty.chartgroup2.message.ChatResponseMessage;
import com.netty.chartgroup2.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class ChatRequestHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        // 找到对方所在的channel
        String to = msg.getTo();
        Channel channel = SessionFactory.getSession().getChannel(to);
        if (channel != null){
            // 在线 向对方channel写入消息
            channel.writeAndFlush(new ChatResponseMessage(msg.getFrom(),msg.getContent()));
        } else {
            // 对方channel不存在，向自己的chanel写入失败的原因
            ctx.writeAndFlush(new ChatResponseMessage(false,"对方不存在或不在线"));
        }
    }
}

