package com.gdufe.seckill.service;

import com.gdufe.seckill.dto.Exposer;
import com.gdufe.seckill.dto.SeckillExcution;
import com.gdufe.seckill.entity.Seckill;
import com.gdufe.seckill.exception.RepeatException;
import com.gdufe.seckill.exception.SeckillCloseException;
import com.gdufe.seckill.exception.SeckillException;

import java.util.List;

/**
 * @ClassName SeckillService
 * @Description 业务处理接口:站在"使用者" 的角度去设计接口，从以下3个方面去考虑:
 * 1) 接口方法的粒度-->粒度不要太小,重在逻辑,不在实现
 * 2) 参数-->参数个数少
 * 3) 返回类型: return / 异常
 * @Author Lu Hengxun
 * @Date 2019/4/26 11:11
 * @Version 1.0
 **/

public interface SeckillService {

    /**
     * 获取所有的秒杀单
     * @Date 2019/4/26
     * @Param []
     * @return java.util.List<com.gdufe.seckill.entity.Seckill>
     **/
    List<Seckill> getSeckillList();

    /**
     * 查看某个具体的秒杀活动
     * @Date 2019/4/26
     * @Param [seckillId]
     * @return com.gdufe.seckill.entity.Seckill
     **/
    Seckill getSeckillById(long seckillId);

    /***
     * 暴露某个具体秒杀活动的的URL
     * 1) 若秒杀开启则暴露秒杀接口的地址
     * 2) 若秒杀未开启则输出系统时间和秒杀时间
     * @Date 2019/4/26
     * @Param [seckillId]
     * @return com.gdufe.seckill.dto.Exposer
     **/
    Exposer exportSeckillUrl(long seckillId,String remoteAddr);

    /**
     * RepeatException 和 SeckillCloseException 都是继承自 SeckillException,
     * 抛出不同的异常是告诉用户具体是什么异常
     * @Description 执行秒杀操作
     * @Date 2019/4/26
     * @Param [seckillId, userPhone, md5]
     * @return com.gdufe.seckill.dto.SeckillExcution
     **/
    SeckillExcution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatException, SeckillCloseException;

    /**
     * @Description 执行秒杀操作 By 存储过程
     * @Date 2019/4/26
     * @Param [seckillId, userPhone, md5]
     * @return com.gdufe.seckill.dto.SeckillExcution
     **/
    SeckillExcution executeSeckillByProcedure(long seckillId, long userPhone, String md5);

}
