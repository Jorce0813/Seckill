package com.gdufe.seckill.exception;

/**
 * @ClassName SeckillException
 * @Description 所有秒杀活动的异常
 * @Author Lu Hengxun
 * @Date 2019/4/26 15:50
 * @Version 1.0
 **/
public class SeckillException extends RuntimeException{

    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
