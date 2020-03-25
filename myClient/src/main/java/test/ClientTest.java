package test;

import net.RpcProxy;
import net.RpcProxyFactory;
import net.RpcReferenceBean;
import test.api.Demo;

public class ClientTest {
    public static void main(String[] args) {
//        RpcProxy sc = new RpcProxy("localhost",8888, Demo.class);

//        Demo demo = RpcProxyFactory.create(Demo.class,"localhost",8888);

        Demo demo = (Demo) new RpcReferenceBean(Demo.class,"localhost",8888).getObject();

        System.out.println(demo.sayHello("sujia"));

//        sc.sendRequest();
    }
}
