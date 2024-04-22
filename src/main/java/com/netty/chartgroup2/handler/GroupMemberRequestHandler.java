package com.netty.chartgroup2.handler;

import com.netty.chartgroup2.message.GroupMembersRequestMessage;
import com.netty.chartgroup2.message.GroupMembersResponseMessage;
import com.netty.chartgroup2.group.GroupSessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;

@ChannelHandler.Sharable
public class GroupMemberRequestHandler extends SimpleChannelInboundHandler<GroupMembersRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupMembersRequestMessage msg) throws Exception {
        Set<String> members = GroupSessionFactory.getGroupSession().getMembers(msg.getGroupName());
        ctx.channel().writeAndFlush(new GroupMembersResponseMessage(members));
    }
}

