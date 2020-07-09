package com.sujia.rpc.router.imp;

import java.util.Collections;
import java.util.TreeSet;

public class Test {
    public static void main(String[] args) {
        TreeSet<String> address = new TreeSet<String>();
        address.add("192.168.0.1");
        address.add("192.168.0.2");
        address.add("192.168.0.3");
        address.add("192.168.0.4");
        RandomLoadBalance rlb = new RandomLoadBalance();
        for(int i=0;i<10;i++){
            String x = rlb.route(address);
            System.out.println(x);
        }
    }
}
