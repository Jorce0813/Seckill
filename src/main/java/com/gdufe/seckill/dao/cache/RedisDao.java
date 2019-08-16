package com.gdufe.seckill.dao.cache;

import com.gdufe.seckill.entity.Seckill;
import io.protostuff.LinkBuffer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @ClassName RedisDao
 * @Description Redis操作的DAO, 用于缓存DB的数据
 * @Author Lu Hengxun
 * @Date 2019/5/18 9:15
 * @Version 1.0
 **/
public class RedisDao {

    private final JedisPool jedisPool;

    //TODO
    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public RedisDao(String host, int port) {
        jedisPool = new JedisPool(host, port);
    }

    /**
     * @return com.gdufe.seckill.entity.Seckill
     * @Description Jedis查询Redis-Cache中对象
     * @Date 2019/5/18
     * @Param [seckillId]
     **/
    public Seckill getSeckill(long seckillId) {
        // 1. 获取jedis对象
        // 2. 通过jedis获取cache中的数据
        //数据存在的话将对象反序列化[跟Redis存储的是二进制数据相关]后返回
        //数据不存在
        try {
            Jedis jedis = jedisPool.getResource();
            String key = "seckill:" + seckillId;
            try {
                byte[] bytes = jedis.get(key.getBytes());
                if (bytes != null) { //redis缓存中存在查询的对象
                    Seckill seckill = schema.newMessage(); //构造一个Seckill空对象
                    //将seckill对象的字节码序列反序列化成 PO
                    ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);
                    return seckill;
                }
            } finally { //关闭Jedis连接
                jedis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return java.lang.String
     * @Description //将Seckill对象序列化并缓存到Redis中
     * @Date 2019/5/18
     * @Param [seckill]
     **/
    public String putSeckill(Seckill seckill) {
        // 1. 将seckill通过protostuff工具序列化成Byte[]字节码
        // 2. 将字节码缓存到redis中
        try {
            Jedis jedis = jedisPool.getResource();
            String key = "seckill:" + seckill.getSeckillId();
            try {
                //将seckill对象按照schema模式转化成字节码,默认缓存大小
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema,
                        LinkedBuffer.allocate(LinkBuffer.DEFAULT_BUFFER_SIZE));
                int timeout = 60 * 60;
                //超时缓存 --> 对象在redis缓存中的生命周期
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                return result; //返回缓存后的成功 / 失败信息
            } finally {
                jedis.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
        Jedis jedis = jedisPool.getResource();
        //2.初始化key && argv[limit + timeout]
        List<String> keys = Collections.singletonList(ip);
        List<String> argvs = Arrays.asList(String.valueOf(limit), String.valueOf(timeout));

        //3.调用lua脚本返回redis服务器判断的结果
        //Object object = 1;
        //System.out.println(keys + " , " + argvs);
        //boolean flag = object == jedis.eval(loadScriptString("redis.lua"), keys, argvs);

        Object result = 0;
        try {
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
/*            if (count > 4) {
                System.out.println("到达访问上限啦");
                return false;
            } else {*/
            result = jedis.eval(lua, keys, argvs);
/*                count++;
              }*/
            System.out.println(result);
            System.out.println("Are you true / false : ");
            System.out.println(result == (Object) 1);
            //System.out.println(String.valueOf(result).equals(String.valueOf(1)));
            //System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }
        //return result == (Object) 1;
        return String.valueOf(result).equals(String.valueOf(1));
    }

}
