package net;

public class RpcReferenceBean {
    private Class<?> iface;
    private SocketClient client;

    public RpcReferenceBean(Class<?> iface,String address,int port){
        this.iface = iface;
        client = SocketClient.getInstance();
        client.init(address,port);
    }

    public Object getObject(){
        return client.getRemoteProxy(iface);
    }


}
