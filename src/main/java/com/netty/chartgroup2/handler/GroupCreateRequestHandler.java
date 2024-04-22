package com.netty.chartgroup2.handler;

import com.netty.chartgroup2.message.GroupCreateRequestMessage;
import com.netty.chartgroup2.message.GroupCreateResponseMessage;
import com.netty.chartgroup2.group.Group;
import com.netty.chartgroup2.group.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class GroupCreateRequestHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage msg) throws Exception {

        // 根据名称创建群聊，如果已存在则返回已存在的群聊信息
        // 需要将创建人也拉入群聊,这里没处理，需要在client端输入members时加上自己
        Group group = GroupSessionFactory.getGroupSession().createGroup(msg.getGroupName(), msg.getMembers());
        if (group == null) {
            // 创建成功, 向每一个人回复进群消息，向创建人回复创建成功消息
            List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(msg.getGroupName());
            for (Channel channel : membersChannel) {
                if (!channel.equals(ctx.channel())) {
                    channel.writeAndFlush(new GroupCreateResponseMessage(true, "你已进入" + msg.getGroupName() + "群聊"));
                }
            }
            ctx.writeAndFlush(new GroupCreateResponseMessage(true, "群聊创建成功"));
        } else {
            ctx.writeAndFlush(new GroupCreateResponseMessage(false, "群聊已存在"));
        }
    }
}

