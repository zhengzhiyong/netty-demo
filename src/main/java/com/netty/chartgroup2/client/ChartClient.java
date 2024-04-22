package com.netty.chartgroup2.client;

import com.netty.chartgroup2.codec.MessageSharableCodec;
import com.netty.chartgroup2.codec.ProtocolFrameDecoder;
import com.netty.chartgroup2.message.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChartClient {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicBoolean login = new AtomicBoolean();
        ChannelFuture channelFuture = new Bootstrap().group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        //pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                        pipeline.addLast(new ProtocolFrameDecoder());
                        pipeline.addLast(new MessageSharableCodec());
                        //====================心跳检测==================================
                        // 写超时设置为3秒，即客户端如果3秒没写出数据，则触发WRITER_IDLE，此刻我们需要补一条心跳数据
                        pipeline.addLast(new IdleStateHandler(0, 3, 0));
                        pipeline.addLast(new ChannelDuplexHandler() {
                            @Override
                            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                if (evt instanceof IdleStateEvent) {
                                    IdleStateEvent event = (IdleStateEvent) evt;
                                    if (IdleState.WRITER_IDLE.equals(event.state())) {
                                        // 触发写空闲超时事件, 向服务端发送ping,服务端可以回复pong(这块没加)
                                        ctx.channel().writeAndFlush(new PingMessage());
                                    }
                                }
                            }
                        });
                        //====================心跳检测==================================

                        pipeline.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info("msg:{}", msg);
                                System.out.println(msg +"\n");
                                if (msg instanceof LoginResponseMessage) {
                                    LoginResponseMessage loginResponseMessage = (LoginResponseMessage) msg;
                                    if (loginResponseMessage.isSuccess()) {
                                        login.set(true);
                                    }
                                    countDownLatch.countDown();
                                }
                            }

                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                new Thread(() -> {
                                    Scanner scanner = new Scanner(System.in);
                                    System.out.println("请输入账号：");
                                    String name = scanner.nextLine();
                                    System.out.println("请输入密码：");
                                    String password = scanner.nextLine();
                                    // 将账号密码发给服务端，校验账号是否正确
                                    LoginRequestMessage message = new LoginRequestMessage(name, password);
                                    ctx.writeAndFlush(message);
                                    System.out.println("等待后续操作....");
                                    try {
                                        // 阻塞当前线程，直到收到登录响应，再放通
                                        countDownLatch.await();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if (!login.get()) {
                                        // 登录失败 直接关闭channel
                                        ctx.channel().close();
                                        return;
                                    }
                                    while (true) {
                                        System.out.println("==================================");
                                        System.out.println("send [username] [content]");
                                        System.out.println("gsend [group name] [content]");
                                        System.out.println("gcreate [group name] [m1,m2,m3...]");
                                        System.out.println("gmembers [group name]");
                                        System.out.println("gjoin [group name]");
                                        System.out.println("gquit [group name]");
                                        System.out.println("quit");
                                        System.out.println("==================================");
                                        String command = scanner.nextLine();
                                        String[] s = command.split(" ");
                                        switch (s[0]) {
                                            case "send":
                                                ctx.writeAndFlush(new ChatRequestMessage(name, s[1], s[2]));
                                                break;
                                            case "gsend":
                                                ctx.writeAndFlush(new GroupChatRequestMessage(name, s[1], s[2]));
                                                break;
                                            case "gcreate":
                                                Set<String> numbers = new HashSet<>(Arrays.asList(s[2].split(",")));
                                                ctx.writeAndFlush(new GroupCreateRequestMessage(s[1], numbers));
                                                break;
                                            case "gmembers":
                                                ctx.writeAndFlush(new GroupMembersRequestMessage(s[1]));
                                                break;
                                            case "gjoin":
                                                ctx.writeAndFlush(new GroupJoinRequestMessage(name, s[1]));
                                                break;
                                            case "gquit":
                                                ctx.writeAndFlush(new GroupQuitRequestMessage(name, s[1]));
                                                break;
                                            case "quit":
                                                ctx.channel().close();
                                                break;
                                            default:
                                                System.out.println("指令不正确");
                                        }
                                    }
                                }).start();
                            }
                        });
                    }
                })
                .connect(new InetSocketAddress("127.0.0.1", 8080));
        Channel channel = channelFuture.sync().channel();
        channel.closeFuture().sync();
    }
}

