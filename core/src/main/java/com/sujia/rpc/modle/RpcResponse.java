package com.sujia.rpc.modle;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 11L;
    private String requestId;
    private String errorMsg;
    private Object result;


}
