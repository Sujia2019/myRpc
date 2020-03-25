package client.proxy;

import client.RPCFuture;
import client.protocol.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class ObjectProxy<T> implements InvocationHandler,IAsynvObjectProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectProxy.class);
    private Class<T> clazz;

    public ObjectProxy(Class<T> clazz){
        this.clazz = clazz;
    }



    @Override
    public RPCFuture call(String funcName, Object... args) {
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(Object.class==method.getDeclaringClass()){
            String name = method.getName();
            if("equals".equals(name)){
                return proxy == args[0];
            }else if("hashCode".equals(name)){
                return System.identityHashCode(proxy);
            }else if("toString".equals(name)){
                return proxy.getClass().getName()+"@"+
                        Integer.toHexString(System.identityHashCode(proxy))+
                        ",with InvocationHandler "+this;
            }else{
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);

        //debug
        LOGGER.debug(method.getDeclaringClass().getName());
        LOGGER.debug(method.getName());
        for(int i=0;i<method.getParameterTypes().length;++i){
            LOGGER.debug(method.getParameterTypes()[i].getName());
        }
        for(int i=0;i<args.length;++i){
            LOGGER.debug(args[i].toString());
        }

//        Rpc


        return null;
    }
}
