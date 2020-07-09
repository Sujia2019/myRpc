package com.sujia.rpc.util;

public class RpcException extends RuntimeException{
    private static final long serialVersionUID=4321L;

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }
}
