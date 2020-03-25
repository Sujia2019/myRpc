package test;

import net.SocketServer;
import registry.ServiceRegistry;
import test.api.Demo;
import test.imp.DemoImp;

import java.io.IOException;

public class ServerTest {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //接口 对应的实现注册进redis中
        ServiceRegistry.regist(Demo.class.getName(),DemoImp.class.getName());

        SocketServer ss = new SocketServer(8888);
        ss.init();
    }
}
