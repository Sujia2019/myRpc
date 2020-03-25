package utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
    public static JedisPoolConfig config = new JedisPoolConfig();
    private static JedisPool pool ;
    private static Jedis jedis;

    private RedisUtil(){
        config.setMaxIdle(8);
        config.setMaxIdle(18);
        pool = new JedisPool(config,"127.0.0.1");
        jedis = pool.getResource();

    }

    //向redis注册服务
    public static void registService(String key,Object obj){
//        jedis.set(key,);
    }


}
