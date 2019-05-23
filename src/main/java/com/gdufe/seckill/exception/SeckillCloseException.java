package com.gdufe.seckill.exception;

/**
 * @ClassName SeckillCloseException
 * @Description 秒杀活动关闭异常: 秒杀时间结束 / 秒杀的商品已经被抢购完
 * @Author Lu Hengxun
 * @Date 2019/4/26 15:49
 * @Version 1.0
 **/
public class SeckillCloseException extends SeckillException{

    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
