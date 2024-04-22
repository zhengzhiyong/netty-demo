package com.netty.chartgroup2.handler;

import com.netty.chartgroup2.message.LoginRequestMessage;
import com.netty.chartgroup2.message.LoginResponseMessage;
import com.netty.chartgroup2.session.SessionFactory;
import com.netty.chartgroup2.user.UserServiceFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class LoginRequestHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
        boolean login = UserServiceFactory.getUserService().login(msg.getUsername(), msg.getPassword());
        LoginResponseMessage loginResponseMessage;
        if (login) {
            loginResponseMessage = new LoginResponseMessage(true, "登录成功");
            // 登录成功，记录session,即账号与channel的绑定关系
            SessionFactory.getSession().bind(ctx.channel(), msg.getUsername());
        } else {
            loginResponseMessage = new LoginResponseMessage(false, "账号或密码不正确");
        }
        ctx.writeAndFlush(loginResponseMessage);
    }
}
