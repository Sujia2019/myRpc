package registry;

import redis.clients.jedis.Jedis;
import utils.RedisUtil;
import utils.SerializableUtil;

import java.io.IOException;

public class ServiceRegistry {


    public static void regist(String name,Class<?> obj){
        Jedis jedis = RedisUtil.getJedis();
        byte[] bs =SerializableUtil.toByteArray(obj);
        if (jedis != null) {
            jedis.set(name,obj.getName());
        }
    }

    public static Class<?> getRegistClass(String key)  {
        Jedis jedis = RedisUtil.getJedis();
        byte[] bs = new byte[0];
        if (jedis != null) {
            bs = jedis.get(key.getBytes());
        }
        return (Class<?>) SerializableUtil.getObject(bs);
    }

}
