package net;

import protocal.RpcRequest;
import protocal.RpcResponse;
import test.api.UserDTO;
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
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

public class SocketClient {

    private static Selector selector = null;
    private static SocketChannel sc = null;
    private static SocketClient client = new SocketClient();
    private FutureTask<RpcResponse> future ;

    private SocketClient(){
    }

    public static SocketClient getInstance(){
        return client;
    }

    public void init(String ip,int port){

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
    }

    //获取代理
    public Object getRemoteProxy(final Class<?> clazz){
        //动态产生实体类
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String methodName = method.getName();
                String clazzName = clazz.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();

                RpcRequest request = new RpcRequest();

                System.out.println(clazzName);

                request.setClassName(clazzName);
                request.setMethodName(methodName);
                request.setParameterTypes(parameterTypes);
                request.setRequestId("20200325");
                request.setParameters(args);

                System.out.println("id:"+request.getRequestId());

                //写
                sc.write(ByteBuffer.wrap(SerializableUtil.toByteArray(request)));
                TimeUnit.SECONDS.sleep(2);

//                future = new FutureTask<>(new Task());

//                pool.execute(future);



                return getResponse().getResult();
            }
        });
    }

//    private class

    private RpcResponse getResponse(){
        try{
            System.out.println("reading...");
            while (selector.select()>0){
                for(SelectionKey sk : selector.selectedKeys()){
                    selector.selectedKeys().remove(sk);
                    if(sk.isReadable()){
                        SocketChannel sc = (SocketChannel) sk.channel();
                        ByteBuffer buf = ByteBuffer.allocate(1024);
                        int len = sc.read(buf);
                        byte[] bs = new byte[1024];
                        int i=0;
                        while(len>0){
                            buf.flip();
                            //这里是什么意思?
                            while (buf.hasRemaining()){
//                              System.out.print((char)buf.get());
                                bs[i]=buf.get();
                                i++;
                            }
                            System.out.println();
                            buf.clear();
                            len = sc.read(buf);
                        }
                        RpcResponse response =(RpcResponse)SerializableUtil.getObject(bs);
                        System.out.println("id:"+response.getRequestId()+"  result:"+response.getResult());
                        return response;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    private static ThreadPoolExecutor pool =
            new ThreadPoolExecutor(60, 300,
                    60L, TimeUnit.SECONDS,new ArrayBlockingQueue<>(1000));


//    private class Task implements Callable<RpcResponse> {
//
//        @Override
//        public RpcResponse call() {
//            try {
//                System.out.println("call");
//                while (selector.select() > 0) {
//                    // 遍历每个有可用IO操作Channel对应的SelectionKey
//                    for (SelectionKey sk : selector.selectedKeys()) {
//                        // 删除正在处理的SelectionKey
//                        selector.selectedKeys().remove(sk);
//                        // 如果该SelectionKey对应的Channel中有可读的数据
//                        if (sk.isReadable()) {
//                            // 使用NIO读取Channel中的数据
//                            SocketChannel sc = (SocketChannel) sk.channel();
//                            ByteBuffer buff = ByteBuffer.allocate(1024);
//                            int len = sc.read(buff);
//                            int i=0;
//                            byte[] bs = new byte[1024];
//                            while(len>0){
//                                buff.flip();
//                                while(buff.hasRemaining()){
//                                    System.out.print((char)buff.get());
////                                    bs[i]=buff.get();
////                                    i++;
//                                }
//                                buff.clear();
//                                len = sc.read(buff);
//                            }
//                            RpcResponse response =(RpcResponse)SerializableUtil.getObject(bs);
//                            System.out.println("id:"+response.getRequestId()+"  result:"+response.getResult());
//                            return response;
//                        }
//                    }
//                }
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//            return null;
//        }
//    }

}
