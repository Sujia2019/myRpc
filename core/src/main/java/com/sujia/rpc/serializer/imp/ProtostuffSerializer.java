package com.sujia.rpc.serializer.imp;

import com.sujia.rpc.serializer.Serializer;
import com.sujia.rpc.util.RpcException;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtostuffSerializer extends Serializer {
    private static final Objenesis OBJENESIS=new ObjenesisStd(true);

    private static Map<Class<?>, Schema<?>> cachedSchema=new ConcurrentHashMap<Class<?>, Schema<?>>();

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> clazz){
        return (Schema<T>) cachedSchema.computeIfAbsent(clazz,RuntimeSchema::createFrom);
    }
    @SuppressWarnings("unchecked")
    public <T> byte[] serializer(T obj) {
        System.out.println("obj   "+obj);
        Class<T> clazz= (Class<T>) obj.getClass();
        System.out.println("class  "+clazz);
        LinkedBuffer buffer=LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        System.out.println("buffer  "+buffer);
        try {
            Schema<T> schema=getSchema(clazz);
            System.out.println("schema   "+schema);
            return ProtostuffIOUtil.toByteArray(obj,schema,buffer);
        } catch (Exception e) {
            throw new RpcException(e);
        }finally {
            buffer.clear();
        }
    }

    public <T> Object deserializer(byte[] bytes, Class<T> clazz) {
        System.out.println("bytes  "+bytes.toString());
        System.out.println("class   "+clazz);
        try {
            T message=OBJENESIS.newInstance(clazz);
            Schema<T> schema=getSchema(clazz);
            ProtostuffIOUtil.mergeFrom(bytes,message,schema);
            System.out.println(message);
            return message;
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }
}
