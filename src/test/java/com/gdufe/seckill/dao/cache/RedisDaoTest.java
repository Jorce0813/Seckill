package com.gdufe.seckill.dao.cache;

import com.gdufe.seckill.dao.SeckillDao;
import com.gdufe.seckill.entity.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
//告诉Junit Spring的配置文件在哪里
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDaoTest {

    long seckillId = 1001;

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private RedisDao redisDao;

    @Test
    public void testSeckill() {
        // 1. 从DB从查询seckillId的对象
        Seckill seckill = seckillDao.queryById(seckillId);

        // 2. 对象 != null 则put到Redis中,再从Redis中拿出数据对象
        if (seckill != null) {
            String result = redisDao.putSeckill(seckill);
            System.out.println(result);
            seckill = redisDao.getSeckill(seckillId);
            System.out.println(seckill);
        }
    }

}