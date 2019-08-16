package com.gdufe.seckill.dao;

import com.gdufe.seckill.entity.Seckill;

/**
 * Jedis连接redis的客户端操作接口
 **/
public interface JedisClient {

    /**
     * 根据key获取信息
     * @param key
     * @return String
     */
    Seckill get(String key);

    /**
     * 设置信息
     * @param seckill
     * @return String
     */
    String set(Seckill seckill);

    /**
     * 设置信息带过期时间
     * @param key
     * @param value
     * @param expire
     * @return String
     */
    String set(String key, String value, int expire);

    /**
     * hset 带多key值
     * @param hkey
     * @param key
     * @return String
     */
    String hget(String hkey, String key);

    /**
     * hset 带多key值和value值
     * @param hkey
     * @param key
     * @param value
     * @return long
     */
    long hset(String hkey, String key, String value);

    /**
     * Incr键值+1
     * @param key
     * @return long
     */
    long incr(String key);

    /**
     * 设置过期时间
     * @param key
     * @param second
     * @return long
     */
    long expire(String key, int second);

    /**
     * 查看过期时间
     * @param key
     * @return long
     */
    long ttl(String key);

    /**
     * 删除对应key值
     * @param key
     * @return long
     */
    long del(String key);

    /**
     * 删除hkey和key
     * @param hkey
     * @param key
     * @return long
     */
    long hdel(String hkey, String key);
}
