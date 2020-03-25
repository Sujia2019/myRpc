package client;

import client.proxy.IAsynvObjectProxy;
import client.proxy.ObjectProxy;

import java.lang.reflect.Proxy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcClient {

    private String serverAddress;
    //private ServiceDiscovery serviceDiscovery;
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            16,16,600L, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(65535)
    );

    public RpcClient(String serverAddress){
        this.serverAddress = serverAddress;
    }
//    public RpcClient()

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> interfaceClass){
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ObjectProxy<T>(interfaceClass)
        );
    }

    public static <T>IAsynvObjectProxy createAsync(Class<T> interfaceClass){
        return new ObjectProxy<T>(interfaceClass);
    }
    public static void submit(Runnable task){
        threadPoolExecutor.submit(task);
    }

    public void stop(){
        threadPoolExecutor.shutdown();
        //
        ConnectManage.getInstance().stop();
    }

}
