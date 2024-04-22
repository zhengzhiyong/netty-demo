package com.nio.demo.chartgroup.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class ChatServer {

    public void startServer() throws Exception {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8000));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器已经启动...");

        for (; ; ) {
            int readChannels = selector.select();

            if (readChannels == 0) {
                continue;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    acceptOperator(serverSocketChannel, selector);
                }

                if (selectionKey.isReadable()) {
                    readOperator(selector, selectionKey);
                }

                iterator.remove();
            }
        }
    }

    private void readOperator(Selector selector, SelectionKey selectionKey) throws Exception {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int readLength = socketChannel.read(byteBuffer);
        String message = "";
        if (readLength > 0) {
            byteBuffer.flip();
            message += StandardCharsets.UTF_8.decode(byteBuffer);

        }
        socketChannel.register(selector, SelectionKey.OP_READ);

        if (!message.isEmpty()) {
            System.out.println(message);
            castOtherClient(message, selector, socketChannel);
        }
    }

    private void castOtherClient(String message, Selector selector, SocketChannel socketChannel) throws Exception {
        Set<SelectionKey> keys = selector.keys();
        for (SelectionKey key : keys) {
            SelectableChannel targetChannel = key.channel();
            if (targetChannel instanceof SocketChannel && targetChannel != socketChannel) {
                ((SocketChannel) targetChannel).write(StandardCharsets.UTF_8.encode(message));
            }
        }
    }

    private void acceptOperator(ServerSocketChannel serverSocketChannel, Selector selector) throws Exception {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        socketChannel.write(StandardCharsets.UTF_8.encode("您已经进入聊天室，请注意发言.."));
    }

    public static void main(String[] args) throws Exception {
        new ChatServer().startServer();
    }
}
