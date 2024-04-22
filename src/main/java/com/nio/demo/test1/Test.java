package com.nio.demo.test1;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Test {
    public static void main(String[] args) throws Exception {
        RandomAccessFile file = new RandomAccessFile("D:\\个人\\工作文档\\深入理解Java虚拟机：JVM高级特性与最佳实践-周志明.pdf","rw");
        FileChannel channel = file.getChannel();
        ByteBuffer allocate = ByteBuffer.allocate(1024);
        int read = channel.read(allocate);
        if (read != -1){
            System.out.println("读取了："+ read);
            allocate.flip();
            while (allocate.hasArray()) {
                System.out.println();
            }
        }
    }
}
