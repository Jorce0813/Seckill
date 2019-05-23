package com.gdufe.seckill.dao.cache;

import com.gdufe.seckill.entity.Seckill;
import io.protostuff.LinkBuffer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

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

}
