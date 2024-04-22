package com.netty.chartgroup2.handler;

import com.netty.chartgroup2.message.GroupJoinRequestMessage;
import com.netty.chartgroup2.message.GroupJoinResponseMessage;
import com.netty.chartgroup2.group.Group;
import com.netty.chartgroup2.group.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class GroupJoinRequestHandler extends SimpleChannelInboundHandler<GroupJoinRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupJoinRequestMessage msg) throws Exception {
        // 查询群聊是否存在，如果存在，将自己添加到组成员中，向每个成员发送新成员进群消息
        Group group = GroupSessionFactory.getGroupSession().joinMember(msg.getGroupName(), msg.getUsername());
        if (group == null) {
            ctx.writeAndFlush(new GroupJoinResponseMessage(false, "群聊不存在"));
            return;
        }

        List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(msg.getGroupName());
        for (Channel channel : membersChannel) {
            if (channel.equals(ctx.channel())) {
                continue;
            }
            channel.writeAndFlush(new GroupJoinResponseMessage(true, msg.getUsername() + "进入群聊"));
        }
        ctx.writeAndFlush(new GroupJoinResponseMessage(true, "加入群聊成功"));
    }
}

