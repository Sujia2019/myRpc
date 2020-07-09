package com.sujia.rpc.util;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class RedisConfig {
    private static RedisConfig redisConfig;
    private static INFO info = INFO.getInstance();
    private static RedisCommands<String,String> redisCommands;
    public RedisConfig(){

    }
    public static RedisCommands<String,String> getInstance(){
        if(redisConfig==null){
            synchronized (RedisConfig.class){
                if(redisConfig==null){
                    redisConfig = new RedisConfig();
                    redisCommands= getConnection();
                }
            }
        }
        return redisCommands;
    }
    private static RedisCommands<String,String> getConnection(){
        RedisURI uri = RedisURI.builder()
                .withHost(info.redisServer)
                .withPort(info.redisPort)
                .withTimeout(Duration.of(10, ChronoUnit.SECONDS))
                .build();                                         //创建单机连接信息

        RedisClient client = RedisClient.create(uri);             //创建客户端
        return client.connect().sync();                           //创建线程安全
    }
}
