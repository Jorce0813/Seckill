package com.gdufe.seckill.dao;

import com.gdufe.seckill.entity.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/*
 * 配置Spring 和 Junit的整合，使得Junit在启动时加载Spring-IOC容器
 * 关联的Jar: spring-test,junit
 **/
@RunWith(SpringJUnit4ClassRunner.class)
//告诉Junit Spring的配置文件在哪里
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {

    //注入DAO实现类的依赖--@Resource标签告知从IOC容器中获取seckillDao的实现类Mapper
    @Resource
    private SeckillDao seckillDao;

    @Test
    public void reduceNumber() throws Exception{
        Date date = new Date();
        int updateCount = seckillDao.reduceNumber(1000,date);
        System.out.println("UpdateCount: " + updateCount);
    }

    @Test
    public void queryById() throws Exception{
        long id = 1000;
        Seckill seckill = seckillDao.queryById(id);
        System.out.println(seckill.getName());
        System.out.println(seckill);
    }

    /**
     * 首次运行时出现以下错误：
     * Caused by: org.apache.ibatis.binding.BindingException:
     * Parameter 'offset' not found. Available parameters are [arg1, arg0, param1, param2]
     * List<Seckill> queryAll(int offset,int limit);
     *
     * 原因：Java没有保存形参的记录,即 queryAll(int offset,int limit) 在MyBatis执行时会变成
     *                              queryAll(args0,args1)
     *
     * 提供2中解决方案:
     *  方案1:修改SeckillDao接口的参数形式说明[参考：https://www.cnblogs.com/future-liu1121/p/7768750.html]
     *  方案2:修改SeckillDao.xml的
     *      limit #{offset},#{limit} 为:
     *      limit #{0},#{1}
     *      [参考:https://www.cnblogs.com/ctony/articles/5352338.html]
     *
     *  本例采用方案1的方法
     **/
    @Test
    public void queryAll() throws Exception{
        List<Seckill> seckills = seckillDao.queryAll(0,10);
        for (Seckill seckill : seckills) {
            System.out.println(seckill);
        }
    }
}