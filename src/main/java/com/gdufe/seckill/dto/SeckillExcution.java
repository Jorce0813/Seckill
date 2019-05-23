package com.gdufe.seckill.dto;

import com.gdufe.seckill.Enum.SeckillStateEnum;
import com.gdufe.seckill.entity.SuccessKill;

/**
 * @ClassName SeckillExcution
 * @Description 用来封装秒杀执行的结果
 * @Author Lu Hengxun
 * @Date 2019/4/26 15:32
 * @Version 1.0
 **/
public class SeckillExcution {

    private long seckillId;

    private int state;

    private String stateInfo;

    private SuccessKill successKill;

    public SeckillExcution(long seckillId, SeckillStateEnum seckillStateEnum, SuccessKill successKill) {
        this.seckillId = seckillId;
        this.state = seckillStateEnum.getState();
        this.stateInfo = seckillStateEnum.getStateInfo();
        this.successKill = successKill;
    }

    public SeckillExcution(long seckillId, SeckillStateEnum seckillStateEnum) {
        this.seckillId = seckillId;
        this.state = seckillStateEnum.getState();
        this.stateInfo = seckillStateEnum.getStateInfo();
    }

    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public void setStateInfo(String stateInfo) {
        this.stateInfo = stateInfo;
    }

    public SuccessKill getSuccessKill() {
        return successKill;
    }

    public void setSuccessKill(SuccessKill successKill) {
        this.successKill = successKill;
    }

    @Override
    public String toString() {
        return "SeckillExcution{" +
                "seckillId=" + seckillId +
                ", state=" + state +
                ", stateInfo='" + stateInfo + '\'' +
                ", successKill=" + successKill +
                '}';
    }
}
