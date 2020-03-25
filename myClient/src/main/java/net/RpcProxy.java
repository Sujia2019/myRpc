package net;


import protocal.RpcRequest;
import utils.SerializableUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RpcProxy implements InvocationHandler, Serializable {

    private static final long serialVersionUID = 1L;
    private int port;
    private String ip;
    private Class<?> clz;

//    public static

    public RpcProxy(String ip, int port, Class<?> clz){
        this.ip = ip;
        this.port = port ;
        this.clz = clz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

//        Object obj ;
//        SocketChannel client = SocketChannel.open(new InetSocketAddress(ip, port));
//        client.configureBlocking(false);
//        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//        RpcRequest request = new RpcRequest("20200324",clz.getName(),
//                method.getName(),method.getParameterTypes(),args);
//        System.out.println(request.getMethodName());
//        byteBuffer.put(SerializableUtil.toByteArray(request));
//        System.out.println(SerializableUtil.toByteArray(request));
//        client.write(byteBuffer);


        return null;
    }
}
