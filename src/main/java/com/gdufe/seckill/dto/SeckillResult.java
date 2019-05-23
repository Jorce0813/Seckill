package com.gdufe.seckill.dto;

/**
 * @ClassName SeckillResult
 * @Description 前端Ajax接口请求返回的数据类型, 用于封装Json数据
 * @Author Lu Hengxun
 * @Date 2019/5/3 14:21
 * @Version 1.0
 **/
public class SeckillResult<T> {

    private boolean success; //表示请求有没有成功,并不是表示秒杀的结果

    private T data;

    private String error;

    public SeckillResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public SeckillResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
