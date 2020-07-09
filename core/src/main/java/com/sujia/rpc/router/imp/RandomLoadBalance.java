package com.sujia.rpc.router.imp;

import com.sujia.rpc.router.RpcLoadBalance;

import java.util.Random;
import java.util.TreeSet;

public class RandomLoadBalance extends RpcLoadBalance {
    private Random random=new Random();

    @Override
    public String route(TreeSet<String> addressSet) {
        //array
        Object[] addressArr = addressSet.toArray();
        //random
        return addressArr[random.nextInt(addressSet.size())].toString();
    }
}
