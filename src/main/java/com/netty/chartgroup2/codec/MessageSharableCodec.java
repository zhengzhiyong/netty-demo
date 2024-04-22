package com.netty.chartgroup2.codec;

import com.netty.chartgroup2.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

@Slf4j
@ChannelHandler.Sharable
public class MessageSharableCodec extends MessageToMessageCodec<ByteBuf, Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        // 1. 4个字节的魔数
        out.writeBytes(new byte[] {1, 2, 3, 4});
        // 2. 1字节的版本
        out.writeByte(1);
        // 3. 1字节的序列化方式，0-jdk 1-json
        out.writeByte(0);
        // 4. 1字节表示消息类型，比如登录、退出..
        out.writeByte(msg.getMessageType());
        // 5. 4字节表示消息请求序号
        out.writeInt(msg.getSequenceId());
        // 由于4+1+1+1+4+4=15,我们再加一字节用于补齐，无意义
        out.writeByte(0XFF);
        // 6. 4字节表示消息的长度
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(msg);
        byte[] bytes = os.toByteArray();
        out.writeInt(bytes.length);

        // 7.消息正文
        out.writeBytes(bytes);
        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNum = in.readInt();
        byte version = in.readByte();
        byte serializerType = in.readByte();
        byte type = in.readByte();
        int sequenceId = in.readInt();
        in.readByte();

        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Message message = (Message) objectInputStream.readObject();
        out.add(message);
    }
}

