package protocal;

import java.io.Serializable;

public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 123L;
    private String requestId;             //请求id
    private String className;            //类名
    private String methodName;           //方法名
    private Class<?>[] parameterTypes;   //参数类型
    private Object[] parameters;         //参数

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String rquestId) {
        this.requestId = rquestId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
