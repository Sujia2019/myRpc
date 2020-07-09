package com.sujia.rpc.net;

import com.sujia.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyEncoder extends MessageToByteEncoder<Object> {
    private Class<?> genericClass;
    private Serializer serializer;

    public NettyEncoder(Class<?> genericClass, Serializer serializer) {
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (genericClass.isInstance(msg)){
            byte[] data=serializer.serializer(msg);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
