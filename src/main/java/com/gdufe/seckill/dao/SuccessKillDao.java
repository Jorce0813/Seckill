package com.gdufe.seckill.dao;

import com.gdufe.seckill.entity.SuccessKill;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SuccessKillDao {

    /**
     * @Description 将秒杀成功的明细插入到秒杀成功明细表中
     * @Date 2019/4/21
     * @Param [seckillId, userPhone]
     * @return int
     **/
    int insertSuccessKill(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

    /**
     * @Description 根据seckillId查询秒杀成功的产品对象
     * @Date 2019/4/21
     * @Param [seckillId]
     * @return com.gdufe.seckill.entity.SuccessKill
     **/
    SuccessKill queryByIdWithSeckill(@Param("seckillId") long seckillId,@Param("userPhone") long userPhone);

}
