package com.netty.chartgroup2.handler;

import com.netty.chartgroup2.message.GroupQuitRequestMessage;
import com.netty.chartgroup2.message.GroupQuitResponseMessage;
import com.netty.chartgroup2.group.Group;
import com.netty.chartgroup2.group.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class GroupQuitRequestHandler extends SimpleChannelInboundHandler<GroupQuitRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupQuitRequestMessage msg) throws Exception {

        // 移除本身，并向群成员发消息
        Group group = GroupSessionFactory.getGroupSession().removeMember(msg.getGroupName(), msg.getUsername());
        if (group == null) {
            ctx.writeAndFlush(new GroupQuitResponseMessage(false, "群聊不存在"));
            return;
        }

        List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(msg.getGroupName());
        for (Channel channel : membersChannel) {
            if (channel.equals(ctx.channel())) {
                continue;
            }
            channel.writeAndFlush(new GroupQuitResponseMessage(true, msg.getUsername() + "退出群聊"));
        }
        ctx.writeAndFlush(new GroupQuitResponseMessage(true, "退出群聊成功"));
    }
}

