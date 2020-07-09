package com.sujia.rpc.modle;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 11L;
    //RequestId
    private String requestId;
    //创建时间
    private long createMillisTime;
    //验证签名
    private String accessToken;
    //请求类名
    private String className;
    //方法名
    private String methodName;
    //请求参数类型
    private Class<?>[] parameterTypes;
    //请求参数
    private Object[] parameters;
    //版本号
    private String version;
}
