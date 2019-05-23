package com.gdufe.seckill.dao;

import com.gdufe.seckill.entity.Seckill;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SeckillDao {

    /**
     * @Description 减库存操作
     * @Date 2019/4/21
     * @Param [seckillId, killTime]
     * @return int
     **/
    int reduceNumber(@Param("seckillId") long seckillId, @Param("killTime") Date killTime);

    /**
     * @Description 根据商品id号查询秒杀商品信息
     * @Date 2019/4/21
     * @Param [seckillId]
     * @return com.gdufe.seckill.entity.Seckill
     **/
    Seckill queryById(long seckillId);

    /**
     * @Description 查询所有的秒杀商品列表
     * @Date 2019/4/21
     * @Param [offset, limit]
     * @return java.util.List<com.gdufe.seckill.entity.Seckill>
     **/
    List<Seckill> queryAll(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * @Description 通过存储过程执行秒杀
     * @Date 2019/5/20
     * @Param [paramMap]
     * @return void
     **/
    void killByProcedure(Map<String,Object> paramMap);

}
