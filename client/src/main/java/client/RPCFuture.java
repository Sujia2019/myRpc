package client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

import client.protocol.RpcRequest;
import client.protocol.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RPCFuture implements Future<Object> {

    private static final Logger logger = LoggerFactory.getLogger(RPCFuture.class);
    private Sync sync;
    private RpcRequest request;
    private RpcResponse response;
    private long startTime;
    private long responseTimeThreshold = 5000;
    private ReentrantLock lock = new ReentrantLock();

    private List<AsyncRPCCallback> pendingCallbacks = new ArrayList<>();

    RPCFuture(RpcRequest request){
        this.sync = new Sync();
        this.request = request;
        this.startTime = System.currentTimeMillis();
    }

    public void done(RpcResponse response){
        this.response = response;
        sync.release(1);
//        in
    }
    private void invokeCallbacks(){
        lock.lock();
        try{
            for(final AsyncRPCCallback callback : pendingCallbacks){
                runCallback(callback);
            }
        }finally {
            lock.unlock();
        }
    }
    public RPCFuture addCallback(AsyncRPCCallback callback) {
        lock.lock();
        try {
            if (isDone()) {
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    private void runCallback(final AsyncRPCCallback callback){
        final RpcResponse res = this.response;
//        RpcClient.submit(new Runnable(){
//              @Override
//              public void run(){
//                  if(!res.isError()){
//                      callback.success(res.getResult());
//                  }else{
//                      callback.fall(new RuntimeException("Response error", new Throwable(res.getError())));
//                  }
//              }
//        });
    }


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(-1);
        if(this.response!=null){
            return this.response.getResult();
        }else{
            return null;
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(-1,unit.toNanos(timeout));
        if (success) {
            if(this.response!=null){
                return this.response.getResult();
            }else{
                return null;
            }
        }else{
            throw new RuntimeException("Timeout exception,Request id:" +this.request.getRequestId()+
                    ".Request class name: " +this.request.getClassName()+
                    ".Request method: "+this.request.getMethodName());
        }
    }

    static class Sync extends AbstractQueuedSynchronizer{
        private static final long serialVersionUID = 1L;

        private final int done = 1;
        private final int pending = 0;

        @Override
        protected boolean tryAcquire(int arg){
            return getState() == done;
        }

        @Override
        protected boolean tryRelease(int arg){
            if(getState()==pending){
                if(compareAndSetState(pending,done)){
                    return true;
                }else{
                    return false;
                }
            }else{
                return true;
            }
        }
        public boolean isDone(){
            getState();
            return getState() == done;
        }

    }



}
