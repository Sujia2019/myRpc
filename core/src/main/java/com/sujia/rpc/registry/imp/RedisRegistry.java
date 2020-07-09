package com.sujia.rpc.registry.imp;

import com.sujia.rpc.registry.ServiceRegistry;
import com.sujia.rpc.util.RedisUtil;

import java.util.*;

public class RedisRegistry extends ServiceRegistry {
    RedisUtil redisUtil = new RedisUtil();
    @Override
    public boolean registry(Set<String> keys, String value) {
        while (keys.iterator().hasNext()){
            String  key = keys.iterator().next();
            redisUtil.lSet(key,value);
        }
        return true;
    }

    @Override
    public boolean registry(String key, String value) {
        redisUtil.lSet(key, value);
        return true;
    }

    @Override
    public boolean remove(Set<String> keys, String value) {
        while (keys.iterator().hasNext()){
            String  key = keys.iterator().next();
            if(redisUtil.hasKey(key)){
                redisUtil.lRemove(key,1,value);
            }
        }
        return false;
    }

    @Override
    public boolean remove(String key, String value) {
        try{
            redisUtil.lRemove(key,1,value);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public TreeSet<String> discovery(String key) {
        //获得所有service对应的服务器
        List<String> list = redisUtil.lGet(key,0,-1);
        list.toArray();
        return new TreeSet<>(list);
    }

    @Override
    public Map<String, TreeSet<String>> discovery(Set<String> keys) {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
