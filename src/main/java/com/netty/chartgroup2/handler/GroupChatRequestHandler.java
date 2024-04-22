package com.netty.chartgroup2.handler;

import com.netty.chartgroup2.message.GroupChatRequestMessage;
import com.netty.chartgroup2.message.GroupChatResponseMessage;
import com.netty.chartgroup2.group.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class GroupChatRequestHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupChatRequestMessage msg) throws Exception {
        // 获取群成员，向每个成员的channel发送消息
        List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(msg.getGroupName());
        if (membersChannel == null) {
            // 不存在
            ctx.channel().writeAndFlush(new GroupChatResponseMessage(false, "群聊不存在"));
            return;
        }
        for (Channel channel : membersChannel) {
            if (channel.equals(ctx.channel())) {
                continue;
            }
            channel.writeAndFlush(new GroupChatResponseMessage(msg.getFrom(), msg.getContent()));
        }
    }
}

