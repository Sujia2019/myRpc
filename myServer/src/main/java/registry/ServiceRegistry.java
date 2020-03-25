package registry;

import redis.clients.jedis.Jedis;
import utils.RedisUtil;
import utils.SerializableUtil;

import java.io.IOException;

public class ServiceRegistry {


    public static void regist(String iface,String impName){
        Jedis jedis = RedisUtil.getJedis();
        if (jedis != null) {
            jedis.set(iface,impName);
        }
    }

    public static String getRegistClass(String key)  {
        Jedis jedis = RedisUtil.getJedis();
        String res;
        if (jedis != null) {
            res = jedis.get(key);
            return res;
        }
        return null;
    }

}
