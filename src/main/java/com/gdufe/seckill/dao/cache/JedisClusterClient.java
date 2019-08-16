package com.gdufe.seckill.dao.cache;

import com.gdufe.seckill.dao.JedisClient;
import com.gdufe.seckill.entity.Seckill;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @ClassName JedisClusterClient
 * @Description 集群的操作接口
 * @Author Lu Hengxun
 * @Date 2019/8/16 10:43
 * @Version 1.0
 **/
public class JedisClusterClient implements JedisClient {

    @Autowired
    private JedisCluster jedisCluster;

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public Seckill get(String key) {
        key = "seckill:" + key;
        byte[] bytes = jedisCluster.get(key.getBytes());
        if (bytes != null) { //redis缓存中存在查询的对象
            Seckill seckill = schema.newMessage(); //构造一个Seckill空对象
            //将seckill对象的字节码序列反序列化成 PO
            ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);
            return seckill;
        }
        return null;
    }

    public String set(Seckill seckill) {
        String key = "seckill:" + seckill.getSeckillId();
        System.out.println(jedisCluster.setex(key, 3600, seckill.toString()));
        return jedisCluster.setex(key, 3600, seckill.toString());
    }

    /**
     * 1. 通过计算同一个IP在timeout时间内访问redis服务器的次数是否超过限制的次数limit来实现限流操作
     * 2. 防止攻击者使用脚本进行服务器的攻击拖垮redis服务器
     * @param ip
     * @param limit
     * @param timeout
     * @return
     */
    public boolean accessLimit(String ip, int limit, int timeout) throws IOException {
        //1.获取jedis连接
        //Jedis jedis = jedisPool.getResource();
        //2.初始化key && argv[limit + timeout]
        List<String> keys = Collections.singletonList(ip);
        List<String> argvs = Arrays.asList(String.valueOf(limit), String.valueOf(timeout));

        //3.调用lua脚本返回redis服务器判断的结果
        //Object object = 1;
        //System.out.println(keys + " , " + argvs);
        //boolean flag = object == jedis.eval(loadScriptString("redis.lua"), keys, argvs);

        Object result = 0;
        String lua =
                "local key = \"rate.limit:\" ..KEYS[1]\n" +
                        "local limit = tonumber(ARGV[1])\n" +
                        "local expire_time = ARGV[2]\n" +
                        "local is_exists = redis.call(\"EXISTS\",key)\n" +
                        "if is_exists == 1 then\n" +
                        "\tif redis.call(\"INCR\",key) > limit then\n" +
                        "\t\treturn 0\n" +
                        "\telse\n" +
                        "\t\treturn 1\n" +
                        "\tend\n" +
                        "else\n" +
                        "\tredis.call(\"SET\",key,1)\n" +
                        "\tredis.call(\"EXPIRE\",key,expire_time)\n" +
                        "\treturn 1\n" +
                        "end";
        System.out.println(jedisCluster);
        System.out.println("llld2");
        System.out.println(jedisCluster.get("str2"));
        System.out.println("llld");
        result = jedisCluster.eval(lua, keys, argvs);
        System.out.println(result);
        return String.valueOf(result).equals(String.valueOf(1));
    }

    public String set(String key, String value, int expire) {
        return null;
    }

    public String hget(String hkey, String key) {
        return null;
    }

    public long hset(String hkey, String key, String value) {
        return 0;
    }

    public long incr(String key) {
        return 0;
    }

    public long expire(String key, int second) {
        return 0;
    }

    public long ttl(String key) {
        return 0;
    }

    public long del(String key) {
        return 0;
    }

    public long hdel(String hkey, String key) {
        return 0;
    }
}
