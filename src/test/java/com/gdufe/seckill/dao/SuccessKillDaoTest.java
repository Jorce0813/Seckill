package com.gdufe.seckill.dao;

import com.gdufe.seckill.entity.SuccessKill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})

public class SuccessKillDaoTest {

    @Resource
    private SuccessKillDao successKillDao;

    /**
     * 第一次执行的结果:insertCount = 1
     * 第二次执行的结果:insertCount = 0 但没有抛出异常:
     *  · insertCount=0是因为success_kill表使用了联合主键,所以插入失败
     *  · 没有抛出异常是因为在 Mybatis 处理的SQL 为: insert ignore into ...
     **/
    @Test
    public void insertSuccessKill() {
        long id = 1000L;
        long userphone = 15629812543L;
        int insertCount = successKillDao.insertSuccessKill(id,userphone);
        System.out.println("insertCount = " + insertCount);
    }

    /*
     * 测试结果:
     * SuccessKill{
     * seckillId=1000,
     * userPhone=15629812543,
     * state=-1,
     * createTime=Tue Apr 23 10:53:05 CST 2019,
     * seckill=Seckill{
     * seckillId=1000,
     * name='1000元秒杀iphone6',
     * number=99,
     * startTime=Sun Apr 21 00:00:00 CST 2019,
     * endTime=Tue Apr 23 00:00:00 CST 2019,
     * createTime=Sat Apr 20 21:04:46 CST 2019}
     * }
     **/

    @Test
    public void queryByIdWithSeckill() {
        long id = 1000L;
        long userphone = 15629812543L;
        SuccessKill successKill = successKillDao.queryByIdWithSeckill(id,userphone);
        System.out.println(successKill);

    }
}