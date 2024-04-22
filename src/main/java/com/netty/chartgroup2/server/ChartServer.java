package com.netty.chartgroup2.server;

import com.netty.chartgroup2.codec.MessageSharableCodec;
import com.netty.chartgroup2.codec.ProtocolFrameDecoder;
import com.netty.chartgroup2.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

public class ChartServer {
    public static void main(String[] args) {
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);
        MessageSharableCodec messageSharableCodec = new MessageSharableCodec();
        LoginRequestHandler loginRequestHandler = new LoginRequestHandler();
        ChatRequestHandler chatRequestHandler = new ChatRequestHandler();
        GroupCreateRequestHandler groupCreateRequestHandler = new GroupCreateRequestHandler();
        GroupChatRequestHandler groupChatRequestHandler = new GroupChatRequestHandler();
        GroupJoinRequestHandler groupJoinRequestHandler = new GroupJoinRequestHandler();
        GroupQuitRequestHandler groupQuitRequestHandler = new GroupQuitRequestHandler();
        GroupMemberRequestHandler groupMemberRequestHandler = new GroupMemberRequestHandler();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup(4))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ProtocolFrameDecoder());
                        pipeline.addLast(loggingHandler);
                        pipeline.addLast(messageSharableCodec);


                        //====================心跳检测==================================
                        // 设置空闲检测，读超时设置为5秒，即server端如果5秒没收到数据，则会触发READER_IDLE
                        pipeline.addLast(new IdleStateHandler(5, 0, 0));
                        // 空闲检测可能涉及到入站和出站，可以实现ChannelDuplexHandler子类实现
                        pipeline.addLast(new ChannelDuplexHandler() {
                            @Override
                            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                if (evt instanceof IdleStateEvent) {
                                    IdleStateEvent event = (IdleStateEvent) evt;
                                    if (IdleState.READER_IDLE.equals(event.state())) {
                                        // 触发读空闲超时事件
                                        ctx.channel().close();
                                    }
                                }
                            }
                        });
                        //====================心跳检测==================================

                        pipeline.addLast(loginRequestHandler);
                        pipeline.addLast(chatRequestHandler);
                        pipeline.addLast(groupCreateRequestHandler);
                        pipeline.addLast(groupChatRequestHandler);
                        pipeline.addLast(groupJoinRequestHandler);
                        pipeline.addLast(groupQuitRequestHandler);
                        pipeline.addLast(groupMemberRequestHandler);

                        pipeline.addLast(messageSharableCodec);

                    }
                })
                .bind(8080);
    }
}

