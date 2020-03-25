package net;

import protocal.RpcRequest;
import protocal.RpcResponse;
import redis.clients.jedis.Jedis;
import registry.ServiceRegistry;
import utils.SerializableUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SocketServer {
    private int port;
    private static final int BUF_SIZE = 1024;
    private static ServerSocketChannel server ;
    private static Selector select;

    public SocketServer(int port){
        this.port = port;
    }

    public void init() {
        //获取选择器
        try{
            server=ServerSocketChannel.open();
            select=Selector.open();

            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));

            //注册接受事件
            server.register(select, SelectionKey.OP_ACCEPT);
            //如果有准备就绪的事件
            while (select.select()>0){
                //获取就绪事件
                Set<SelectionKey> selectionKeySet = select.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeySet.iterator();
                while (iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    //如果是客户端连接事件
                    if(selectionKey.isAcceptable()){
                        handlerAccept(selectionKey);
                    }
                    //如果是读事件
                    if(selectionKey.isReadable()) {
                        handlerRead(selectionKey);
                    }
                    if(selectionKey.isWritable()&& selectionKey.isValid()){
                        handlerWrite(selectionKey);
                    }
                    if(selectionKey.isConnectable()){
                        System.out.println("isConnectable = true");
                    }
                    iterator.remove();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                if(select!=null){
                    select.close();
                }
                if(server !=null){
                    server.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    /**
     *
     * @param o 传进来的实例
     * @param methodName 要执行的方法名
     * @param params 参数
     * @return 返回的对象(执行结果)
     */
    private Object executeMethod(Object o,String methodName,Object[] params){
        Object objR;
        Class<?>[] cs = new Class[params.length];
        for(int i=0;i<params.length;i++){
            Object param = params[i];
            cs[i] = param.getClass();
        }
        try{
            Method m =o.getClass().getMethod(methodName,cs);
            objR = m.invoke(o,params);
            System.out.println(objR);
            return objR;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private RpcResponse invokeResponse(RpcRequest request){
        RpcResponse res = new RpcResponse();
        res.setError("应该没错");
        res.setRequestId(request.getRequestId());
        try {
            //获取对应的实现类名
            String className = ServiceRegistry.getRegistClass(request.getClassName());
            //创建实体
            System.out.println(className);
            Object obj = Class.forName(className).newInstance();
            //实现方法
            res.setResult(executeMethod(obj,request.getMethodName(),request.getParameters()));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
//        res.setResult(executeMethod(o,methodName,params));
        return res;
    }

    private void handlerAccept(SelectionKey key) throws IOException {
        ServerSocketChannel ssChannel = (ServerSocketChannel)key.channel();
        SocketChannel sc = ssChannel.accept();
        sc.configureBlocking(false);
        sc.register(key.selector(),SelectionKey.OP_READ, ByteBuffer.allocateDirect(BUF_SIZE));
    }

    private void handlerRead(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel)key.channel();
        ByteBuffer buf = (ByteBuffer)key.attachment();
        int len = sc.read(buf);
        byte[] bs = new byte[1024];
        int i=0;
        while(len>0){
            buf.flip();
            //这里是什么意思?
            while (buf.hasRemaining()){
//                System.out.print((char)buf.get());
                bs[i]=buf.get();
                i++;
            }
            System.out.println();
            buf.clear();
            len = sc.read(buf);
        }
//        if(len == -1){
//            sc.close();
//        }
        RpcRequest request ;
        RpcResponse response ;
        request = (RpcRequest) SerializableUtil.getObject(bs);
        System.out.println("收到请求... 来自id:"+ request.getRequestId()+" 请求方法: "+request.getMethodName());
        response=invokeResponse(request);
        System.out.println("response:"+response.getRequestId());

        handlerWrite(key,sc,response);
    }
    private void handlerWrite(SelectionKey sk,
                              SocketChannel sc,RpcResponse response) throws IOException {
        sc.write(ByteBuffer.wrap(SerializableUtil.toByteArray(response)));
        sk.interestOps(SelectionKey.OP_READ);
    }

    private void handlerWrite(SelectionKey key) throws IOException {
        ByteBuffer buf = (ByteBuffer) key.attachment();
        buf.flip();
        SocketChannel sc = (SocketChannel)key.channel();
        while(buf.hasRemaining()){
            sc.write(buf);
        }
        buf.compact();
    }

    private static ThreadPoolExecutor pool =
            new ThreadPoolExecutor(60, 300,
                    60L, TimeUnit.SECONDS,new LinkedBlockingQueue<>(1000), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "rpc"+"--serverHandlerPool--" + r.hashCode());
                }
            });











}
