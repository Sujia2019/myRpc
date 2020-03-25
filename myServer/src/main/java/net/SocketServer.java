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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class SocketServer {
    private int port;
    private static final int BUF_SIZE = 1024;

    public SocketServer(int port){
        this.port = port;
    }

    public void init() {
        ServerSocketChannel server = null;
        //获取选择器
        Selector select = null;

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
//                        SocketChannel client = (SocketChannel)selectionKey.channel();
//                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//                        int len = 0;
//                        while ((len=client.read(byteBuffer))>0){
//                        byteBuffer.flip();
                            //接收rpc请求
//                        RpcRequest request = (RpcRequest) SerializableUtil.getObject(client.socket().getInputStream());
//                        System.out.println("收到请求: 来自id"+ request.getRequestId()+" 请求方法: "+request.getMethodName());
//                            System.out.println("收到客户端数据： " + new String(byteBuffer.array(), 0, len));                        //获取这个接口对应的实现类, 创建实例
//                        //获取这个接口对应的实现类, 创建实例
//                        Object o = getObject(request.getClass());

                            //执行这个方法,并获取返回值
//                      Object resp = executeMethod(o,request.getMethodName(),request.getParameters());

                            //准备Rpc响应返回
//                        RpcResponse response = invokeResponse(request.getRequestId(),o,request.getMethodName(),request.getParameters());
                            //放进bytebuffer
//                        byteBuffer.put(SerializableUtil.toByteArray(response));
                            //写入通道
//                            client.write(byteBuffer);
                            //清除
//                        byteBuffer.clear();
//                        }
//                    }
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
        Object objR = null;
        Class<?>[] cs = new Class[params.length];
        for(int i=0;i<params.length;i++){
            Object param = params[i];
            cs[i] = param.getClass();
        }
        try{
            Method m =o.getClass().getMethod(methodName);
            objR = m.invoke(o,params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return objR;
    }

    private RpcResponse invokeResponse(RpcRequest request){
        RpcResponse res = new RpcResponse();
        res.setError("应该没错");
        res.setRequestId(request.getRequestId());
        try {
            Object obj = ServiceRegistry.getRegistClass(request.getClassName()).newInstance();
            res.setResult(executeMethod(obj,request.getMethodName(),request.getParameters()));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
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
        RpcRequest request ;
        RpcResponse response ;

        request = (RpcRequest) SerializableUtil.getObject(bs);
        System.out.println("收到请求: 来自id"+ request.getRequestId()+" 请求方法: "+request.getMethodName());
//        System.out.println("收到客户端数据： " + new String(buf.array(), 0, len));
//          Object o = getObject(request.getClassName());
//          Object obj = ServiceRegistry.getRegistClass(request.getClassName()).newInstance();
        response=invokeResponse(request);
        handlerWrite(key,response);

        if(len == -1){
            sc.close();
        }
    }
    private void handlerWrite(SelectionKey key,RpcResponse response) throws IOException {
        ByteBuffer buf = (ByteBuffer) key.attachment();
        buf.flip();
        SocketChannel sc = (SocketChannel)key.channel();
        while(buf.hasRemaining()){
            buf.put(SerializableUtil.toByteArray(response));
            sc.write(buf);
        }
        buf.compact();
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













}
