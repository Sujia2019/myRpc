package utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
    public static JedisPoolConfig config = new JedisPoolConfig();
    private static JedisPool pool ;

    static {
        config.setMinIdle(8);
        config.setMaxIdle(18);
        pool = new JedisPool(config,"127.0.0.1");
    }

    public static Jedis getJedis(){
        try{
            if(pool!=null){
                return pool.getResource();
            }else{
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }



}
