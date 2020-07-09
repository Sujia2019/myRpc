package com.sujia.rpc.router;

import java.util.TreeSet;

public abstract class RpcLoadBalance {

    public abstract String route(TreeSet<String> addressKey);


}
