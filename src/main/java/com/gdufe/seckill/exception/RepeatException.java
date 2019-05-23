package com.gdufe.seckill.exception;

/**
 * @ClassName RepeatException
 * @Description 重复秒杀的异常
 * 学习Java中的异常类型: 运行期异常 & 编译器异常
 * @Author Lu Hengxun
 * @Date 2019/4/26 15:45
 * @Version 1.0
 **/
public class RepeatException extends SeckillException{

    public RepeatException(String message) {
        super(message);
    }

    public RepeatException(String message, Throwable cause) {
        super(message, cause);
    }
}
