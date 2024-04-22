package com.nio.demo.chartgroup.client;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class ClientThread implements Runnable {
    private Selector selector;

    public ClientThread(Selector selector) {
        this.selector = selector;
    }
    @Override
    public void run() {
        for (;;) {
            try {
                int readChannels = selector.select();

                if (readChannels == 0) {
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();


                    if (selectionKey.isReadable()) {
                        readOperator(selector,selectionKey);
                    }

                    iterator.remove();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void readOperator(Selector selector, SelectionKey selectionKey) throws Exception{
        SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int readLength = socketChannel.read(byteBuffer);
        String message = "";
        if (readLength > 0 ) {
            byteBuffer.flip();
            message += StandardCharsets.UTF_8.decode(byteBuffer);

        }
        socketChannel.register(selector,SelectionKey.OP_READ);

        if (message.length() > 0) {
            System.out.println(message);
        }
    }
}
