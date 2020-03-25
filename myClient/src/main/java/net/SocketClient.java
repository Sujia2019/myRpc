package net;

import protocal.RpcRequest;
import protocal.RpcResponse;
import utils.SerializableUtil;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

public class SocketClient {

    private Selector selector = null;
    private SocketChannel sc = null;
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private static SocketClient client = new SocketClient();

    private SocketClient(){
    }

    public static SocketClient getInstance(){
        return client;
    }

    public SocketClient init(String ip,int port){

        try {
            selector = Selector.open();
            sc = SocketChannel.open(new InetSocketAddress(ip,port));
            //非阻塞式
            sc.configureBlocking(false);
            //将SocketChannel对象注册到指定selector
            int op = SelectionKey.OP_READ|SelectionKey.OP_WRITE;
            sc.register(selector,op);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //获取代理
    public Object getRemoteProxy(final Class<?> clazz){
        //动态产生实体类
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String methodName = method.getName();
                String clazzName = clazz.getSimpleName();
                Class<?>[] parameterTypes = method.getParameterTypes();

                RpcRequest request = new RpcRequest();
                request.setClassName(clazzName);
                request.setMethodName(methodName);
                request.setParameterTypes(parameterTypes);
                request.setRequestId("20200325");
                request.setParameters(args);

                System.out.println("id:"+request.getRequestId());
                Object result = null;

                sc.write(ByteBuffer.wrap(SerializableUtil.toByteArray(request)));
//                if(args == null || args.length == 0){
//                    //没有参数
//                    sc.write(ByteBuffer.wrap((clazzName+"/"+methodName+"()").getBytes()));
//                }else{
//                    int size = args.length;
//                    String[] types = new String[size];
//                    StringBuilder content = new StringBuilder(clazzName).
//                            append("/").append(methodName).append("(");
//                    for(int i=0;i<size;i++){
//                        types[i]=args[i].getClass().getName();
//                        content.append(types[i]).append(":").append(args[i]);
//                        if(i!=size-1){
//                            content.append(",");
//                        }
//                    }
//                    content.append(")");
//                    sc.write(ByteBuffer.wrap(content.toString().getBytes()));
//                }
                result = getResult();
                return result;
            }
        });
    }

    private Object getResult(){
        try{
            while (selector.select()>0){
                for(SelectionKey sk : selector.selectedKeys()){
                    selector.selectedKeys().remove(sk);
                    if(sk.isReadable()){
                        SocketChannel sc = (SocketChannel)sk.channel();
                        buffer.clear();
                        sc.read(buffer);
                        byte[] bs = buffer.array();

                        RpcResponse response =(RpcResponse)SerializableUtil.getObject(bs);
                        System.out.println("id:"+response.getRequestId()+"  result:"+response.getResult());
                        return response;
//                        return SerializableUtil.

//                        int p = buffer.position();
//                        String result = new String(buffer.array(),0,p);
//                        result = result.trim();
//                        buffer.clear();
//                        if(result.endsWith("null")||result.endsWith("NULL")){
//                            return null;
//                        }
//                        String[] typeValue = result.split(":");
//                        String type = typeValue[0];
//                        String value = typeValue[1];
//                        if(type.contains("Integer")||type.contains("int"))
//                            return Integer.parseInt(value);
//                        else if(type.contains("Float")||type.contains("float"))
//                            return Float.parseFloat(value);
//                        else if(type.contains("Long")||type.contains("long"))
//                            return Long.parseLong(value);
//                        else
//                            return value;

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
