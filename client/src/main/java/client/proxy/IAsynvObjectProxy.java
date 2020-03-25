package client.proxy;

import client.RPCFuture;

public interface IAsynvObjectProxy {
    public RPCFuture call(String funcName,Object...args);
}
