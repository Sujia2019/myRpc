package client;

import client.protocol.RpcRequest;
import client.protocol.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;


public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);

    private ConcurrentHashMap<String,RPCFuture> pendingRPC = new ConcurrentHashMap<>();

    private volatile Channel channel;
    private SocketAddress remotePeer;

    public Channel getChannel(){
        return channel;
    }

    public SocketAddress getRemotePeer(){
        return remotePeer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        String requestId = msg.getRequestId();
        RPCFuture rpcFuture = pendingRPC.get(requestId);
        if(rpcFuture!=null){
            pendingRPC.remove(requestId);
            rpcFuture.done(msg);
        }
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }
    public void close(){
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public RPCFuture sendRequest(RpcRequest request){
        final CountDownLatch latch = new CountDownLatch(1);
        RPCFuture rpcFuture = new RPCFuture(request);
        pendingRPC.put(request.getRequestId(),rpcFuture);
        channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                latch.countDown();
            }
        });
        try{
            latch.await();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
        return rpcFuture;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("entity caught exception",cause);
        ctx.close();
    }
}
