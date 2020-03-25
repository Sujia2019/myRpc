package client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectManage {
    private static final Logger logger = LoggerFactory.getLogger(ConnectManage.class);
    private volatile static ConnectManage connectManage;

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16,16,
            600L, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(65536));
    private CopyOnWriteArrayList<RpcClientHandler> connectHandlers = new CopyOnWriteArrayList<>();
    private Map<InetSocketAddress,RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();

    private ReentrantLock lock = new ReentrantLock();
    private Condition connected = lock.newCondition();
    private long connectTimeoutMillis = 6000;
    private AtomicInteger roundRobin = new AtomicInteger(0);
    private volatile boolean isRunning = true;

    private ConnectManage(){

    }

    public static ConnectManage getInstance(){
        if(connectManage == null){
            synchronized (ConnectManage.class){
                if(connectManage == null){
                    connectManage = new ConnectManage();
                }
            }
        }
        return connectManage;
    }

    public void updateConnectedServer(List<String> allServerAddress){
        if(allServerAddress != null){
            if(allServerAddress.size() > 0){
                HashSet<InetSocketAddress> newAllServerNodeSet = new HashSet<>();
                for(int i=0;i<allServerAddress.size();++i){
                    String[] array = allServerAddress.get(i).split(":");
                    if(array.length == 2){
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);
                        final InetSocketAddress remotePeer = new InetSocketAddress(host,port);
                        newAllServerNodeSet.add(remotePeer);
                    }
                }

                //添加新的服务结点
                for(final InetSocketAddress serverNodeAddress : newAllServerNodeSet){
                    if(!connectedServerNodes.keySet().contains(serverNodeAddress)){
                        connectServerNode(serverNodeAddress);
                    }
                }

                //关闭并移出不合法的服务结点
                for(int i=0;i<connectHandlers.size();++i){
                    RpcClientHandler connectedServerHandler = connectHandlers.get(i);
                    SocketAddress remotePeer = connectedServerHandler.getRemotePeer();
                    if(!newAllServerNodeSet.contains(remotePeer)){
                        logger.info("Remove invalid server node "+ remotePeer);
                        RpcClientHandler handler = connectedServerNodes.get(remotePeer);
                        if(handler != null){
                            handler.close();
                        }
                        connectedServerNodes.remove(remotePeer);
                        connectHandlers.remove(connectedServerHandler);
                    }
                }
            }else{
                logger.error("No available server node.All server nodes are down !!");
                for(final RpcClientHandler connectedServerHandler : connectHandlers){
                    SocketAddress remotePeer = connectedServerHandler.getRemotePeer();
                    RpcClientHandler handler = connectedServerNodes.get(remotePeer);
                    handler.close();;
                    connectedServerNodes.remove(connectedServerHandler);
                }
                connectHandlers.clear();
            }
        }
    }

    public void reconnect(final RpcClientHandler handler,final SocketAddress remotePeer){
        if(handler!=null){
            connectHandlers.remove(handler);
            connectedServerNodes.remove(handler.getRemotePeer());
        }
        connectServerNode((InetSocketAddress) remotePeer);
    }

    private void connectServerNode(final InetSocketAddress remotePeer) {
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Bootstrap b = new Bootstrap();
                b.group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new RpcClientInitializer());

                ChannelFuture channelFuture = b.connect(remotePeer);
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if(channelFuture.isSuccess()){
                            logger.debug("Successfully connect to remote server.remote peer = "+remotePeer);
                            RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                            addHandler(handler);
                        }
                    }
                });
            }
        });

    }

    private void addHandler(RpcClientHandler handler) {
        connectHandlers.add(handler);
        InetSocketAddress remoteAddress = (InetSocketAddress)handler.getChannel().remoteAddress();
        connectedServerNodes.put(remoteAddress,handler);
        signalAvailableHandler();
    }

    private void signalAvailableHandler() {
        lock.lock();
        try{
            connected.signalAll();
        }finally {
            lock.unlock();
        }
    }

    private boolean waitingForHandler() throws InterruptedException{
        lock.lock();
        try{
            return connected.await(this.connectTimeoutMillis,TimeUnit.MILLISECONDS);
        }finally {
            lock.unlock();
        }
    }

    public RpcClientHandler chooesHandler(){
        int size = connectHandlers.size();
        while(isRunning && size <= 0){
            try{
                boolean available = waitingForHandler();
                if(available){
                    size = connectHandlers.size();
                }
            } catch (InterruptedException e) {
                logger.error("Waiting for available node is interrupted!",e);
                throw new RuntimeException("Can't connect any servers!",e);
            }
        }
        int index = (roundRobin.getAndAdd(1)+size)%size;
        return connectHandlers.get(index);
    }

    public void stop(){
        isRunning = false;
        for(int i=0;i<connectHandlers.size();i++){
            RpcClientHandler connectedServerHandler = connectHandlers.get(i);
            connectedServerHandler.close();
        }
        signalAvailableHandler();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();

    }
}
