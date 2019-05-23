package com.gdufe.seckill.service;

import com.gdufe.seckill.dto.Exposer;
import com.gdufe.seckill.dto.SeckillExcution;
import com.gdufe.seckill.entity.Seckill;
import com.gdufe.seckill.exception.RepeatException;
import com.gdufe.seckill.exception.SeckillCloseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"})

public class SeckillServiceTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // @Resource 也可以实现注解功能的正常测试
    // 二者的联系&区别: https://www.cnblogs.com/xiaoxi/p/5935009.html
    @Autowired
    private SeckillService seckillService;


    @Test
    public void getSeckillList() {
        List<Seckill> seckillList = seckillService.getSeckillList();
        for (Seckill seckill :
                seckillList) {
            logger.info("seckillList = {}", seckill);
        }
    }

    @Test
    public void getSeckillById() {
        Seckill seckill = seckillService.getSeckillById(1000L);
        logger.info("seckill = {}", seckill);
    }

    /**
     * @return void
     * @Description 集成测试秒杀的逻辑运行
     * @Date 2019/4/30
     * @Param []
     **/
    @Test
    public void testExposerLogic() {
        long seckillId = 1001L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        long userPhone = 15629812540L;
        String md5 = exposer.getMd5();
        logger.info("Exposer = {}", exposer);
        if (exposer.isExposed()) {
            try {
                SeckillExcution seckillExcution = seckillService.executeSeckill(seckillId, userPhone, md5);
                logger.info("Excution = {}", seckillExcution);

                //执行这两个catch是将异常信息抛出在测试类中而不是往上抛出异常
            } catch (RepeatException e) {
                logger.error(e.getMessage());
            } catch (SeckillCloseException e) {
                logger.error(e.getMessage());
            }
        } else {
            //秒杀未开启
            logger.warn("exposer = {}", exposer);
        }

    }

    @Test
    public void testexecuteSeckillByProcedure() {
        long seckillId = 1001L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        long userPhone = 15629812540L;
        String md5 = exposer.getMd5();
        logger.info("Exposer = {}", exposer);
        if (exposer.isExposed()) {
            SeckillExcution seckillExcution = seckillService.executeSeckillByProcedure(seckillId, userPhone, md5);
            logger.info(seckillExcution.getStateInfo());
        } else {
            //秒杀未开启
            logger.warn("exposer = {}", exposer);
        }
    }

}