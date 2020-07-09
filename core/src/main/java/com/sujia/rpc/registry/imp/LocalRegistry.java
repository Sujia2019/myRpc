package com.sujia.rpc.registry.imp;

import com.sujia.rpc.registry.ServiceRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地注册方法
 */
public class LocalRegistry extends ServiceRegistry {
    private Map<String,TreeSet<String>>localMap1N;
    @Override
    public boolean registry(Set<String> keys, String value) {
        if (null==keys || keys.size()==0 ||null==value ||value.length()==0) {
            return false;
        }
        for (String key: keys) {
            localMap1N.computeIfAbsent(key, k -> new TreeSet<>());
        }
        return true;
    }

    @Override
    public boolean registry(String key, String value) {
        return false;
    }

    @Override
    public boolean remove(Set<String> keys, String value) {
        if (keys==null||keys.size()==0||value==null||value.length()==0) {
            return false;
        }
        for (String key: keys) {
            TreeSet<String> values=localMap1N.get(key);
            if (values != null) {
                values.remove(value);
            }
        }
        return true;
    }

    @Override
    public boolean remove(String key, String value) {
        return false;
    }

    @Override
    public TreeSet<String> discovery(String key) {
        return localMap1N.get(key);
    }

    @Override
    public Map<String, TreeSet<String>> discovery(Set<String> keys) {
        if(keys==null||keys.size()==0){
            return null;
        }else{
            Map<String,TreeSet<String>> registryDataTmp=new HashMap<>();
            for(String key:keys){
                TreeSet<String> valueSetTmp=discovery(key);
                if (valueSetTmp != null) {
                    registryDataTmp.put(key,valueSetTmp);
                }
            }
        }
        return null;
    }

    @Override
    public void start() {
        localMap1N = new ConcurrentHashMap<>();
    }

    @Override
    public void stop(){
        localMap1N.clear();
    }

}
