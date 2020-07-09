package com.sujia.rpc.router.imp;

import com.sujia.rpc.router.RpcLoadBalance;

import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 轮询，顺序访问
 */
public class RoundLoadBalance extends RpcLoadBalance {
    private ConcurrentHashMap<String,Integer> map = new ConcurrentHashMap<>();
    private int time = 0;
    private int size = 0;
    public RoundLoadBalance(TreeSet<String> addressKey){
        size = addressKey.size();
        while (addressKey.iterator().hasNext()){
            String key = addressKey.iterator().next();
            map.put(key,time);
        }
    }
    private void count(){

    }
    @Override
    public String route(TreeSet<String> addressKey) {

        return null;
    }
}
