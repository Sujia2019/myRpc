package com.sujia.rpc.registry;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
    服务注册的抽象类
    多种不同的注册中心都可以继承
 */
public abstract class ServiceRegistry {

    /**
     * 服务注册
     */
    public abstract boolean registry(Set<String> keys, String value);
    public abstract boolean registry(String key,String value);
    /**
     * 服务注销
     */
    public abstract boolean remove(Set<String> keys,String value);
    public abstract boolean remove(String key,String value);
    /**
     * 服务发现
     */
    public abstract TreeSet<String> discovery(String key);

    public abstract Map<String, TreeSet<String>> discovery(Set<String> keys);


    /**
     * start
     */
    public abstract void start();
    /**
     * stop
     */
    public abstract void stop();

}
