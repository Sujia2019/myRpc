package com.sujia.rpc.util;

import lombok.Data;
@Data
public class INFO {
    private static INFO info;

    public String server;
    public Integer port;
    public String redisServer;
    public Integer redisPort;

    public INFO(){
        redisServer = PropertiesUtil.get("redis.ip");
        redisPort = PropertiesUtil.getInteger("redis.port");
        server = PropertiesUtil.get("server.ip");
        port = PropertiesUtil.getInteger("server.port");
    }

    public static INFO getInstance(){
        if(info == null ){
            synchronized (INFO.class){
                if(info==null){
                    info = new INFO();
                }
            }
        }
        return info;
    }

}
