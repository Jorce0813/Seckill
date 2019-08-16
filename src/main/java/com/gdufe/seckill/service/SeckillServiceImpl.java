package com.gdufe.seckill.service;

import com.gdufe.seckill.Enum.SeckillStateEnum;
import com.gdufe.seckill.dao.SeckillDao;
import com.gdufe.seckill.dao.SuccessKillDao;
import com.gdufe.seckill.dao.cache.JedisClusterClient;
import com.gdufe.seckill.dao.cache.RedisDao;
import com.gdufe.seckill.dto.Exposer;
import com.gdufe.seckill.dto.SeckillExcution;
import com.gdufe.seckill.entity.Seckill;
import com.gdufe.seckill.entity.SuccessKill;
import com.gdufe.seckill.exception.RepeatException;
import com.gdufe.seckill.exception.SeckillCloseException;
import com.gdufe.seckill.exception.SeckillException;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName SeckillServiceImpl
 * @Description 秒杀的业务实现层
 * @Author Lu Hengxun
 * @Date 2019/4/28 19:25
 * @Version 1.0
 * <p>
 * Spring采用注解方式实现将Bean注入到Spring容器中的常用注解:
 * https://www.cnblogs.com/xiaoxi/p/5935009.html
 **/
@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKillDao successKillDao;

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private JedisClusterClient jedisClusterClient;


    //MD5的盐值字符串,用于混淆MD5,使得MD5的过程不可逆
    private final String slat = "fasdf/.fasdf*f&%$#@^fasd464565fads//&^&^&``";

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    public Seckill getSeckillById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId, String remoteAddr) {
        /*
         * 点击[详情]进入秒杀详情页时->
         * 在秒杀开启前/秒杀进行中/秒杀结束时(秒杀服务器没停止服务)->
         * 有攻击者用脚本进行页面的循环机械刷新访问redis服务器时->
         * 做redis限流控制,防止redis服务器被类似的请求拖垮
         */
        //start-限流
        int limit = 5; //timeout时间内限制访问次数
        int timeout = 60; //设置过期时间,单位:s
        boolean result = false;
        try {
            System.out.println("fuck redis1");
            //result = redisDao.accessLimit(remoteAddr, limit, timeout);
            //20190816接入redis集群
            result = jedisClusterClient.accessLimit(remoteAddr,limit,timeout);
            System.out.println("fuck redis");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!result) { //代表访问受限
            System.out.println("Please stop Attack!!!");
            //return new Exposer(false, seckillId);
            return null;
        }
        //end-限流

        //利用Redis进行缓存的优化:超时维护数据的一致性???
        /*
         * if (redis存在seckillId的对象){
         *      获取seckill对象
         * }else{
         *      从DB中获取seckill对象
         *      if(seckill!=null){
         *           将seckill对象放到Redis中缓存
         *      }else{
         *          抛出异常信息
         *      }
         * }
         */
        //Seckill seckill = redisDao.getSeckill(seckillId);
        //20190816接入redis集群
        //Seckill seckill = redisDao.getSeckill(seckillId);
        Seckill seckill = jedisClusterClient.get(seckillId + "");

        if (seckill == null) {
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {
                return new Exposer(false, seckillId);
            } else {
               // redisDao.putSeckill(seckill);
                jedisClusterClient.set(seckill);
            }
        }

        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();

        // getTime()是用于将时间格式 Date 转化为 毫秒(ms)的形式表示
        // Date.getTime()是返回距离 1970.01.01 的毫秒数
        if (nowTime.getTime() < startTime.getTime() ||
                nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(),
                    startTime.getTime(), endTime.getTime());
        }

        //转化为字符串的过程,不可逆
        //在该系统中,首次使用MD5加密(在此处)是为了防止用户在秒杀开启前获取商品的ID后在知道秒杀的接口提前进行秒杀,
        //使用MD5后,用户不知道MD5的拼接规则(跟slat有关),即使知道了接口也无法进行秒杀。
        String md5 = getMD5(seckillId); //获取MD5

        return new Exposer(true, seckillId, md5);
    }

    /*
     * 关于MD5: https://www.cnblogs.com/second-tomorrow/p/9129043.html
     *          https://www.cnblogs.com/whyhappy/p/5337044.html
     * @Description 产生MD5加密字符串
     * @Date 2019/4/28
     * @Param [seckillId]
     * @return java.lang.String
     **/
    private String getMD5(long seckillId) {
        String base = seckillId + "/&^*" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    /**
     * 在该方法中使用MD5加密进行秒杀的执行是防止用户在执行秒杀时数据在传输过程中被篡改,
     * 类似于用户在支付过程中信息被第三方恶意篡改的情况,详细了解CLICK上面的LINK
     *
     * @return com.gdufe.seckill.dto.SeckillExcution
     * @Description
     * @Date 2019/4/28
     * @Param [seckillId, userPhone, md5]
     * <p>
     * 采用@Transactional注解来声明该方式是一个事务方法,其由Spring的声明式事务控制
     * 使用注解控制事务方法的优点:
     * 1. 开发团队达成一致的约定,明确标注事务方法的编程风格
     * 2. 在事务方法中执行时间尽量短(只涉及数据库的数据操作),不要穿插其他的网络操作 RPC/HTTP,若需要,将相关的处理剥离到
     * 事务方法的外部,再通过传参的方式传给事务方法
     * 3. 不是所有的方法都需要事务,比如只有一个修改操作(单线程),数据库的读取不需要事务控制(跟事务的隔离级别<4种>有关)
     **/
    @Transactional
    public SeckillExcution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatException, SeckillCloseException {
        //1. 验证MD5是否存在 || 被篡改了,是则抛出异常
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            throw new SeckillException("MD5 is rewrited!");
        }

        try {
            //2. 执行秒杀的两个步骤: 减库存 + 插入秒杀记录
            //2-2 插入秒杀记录: 若插入失败(重复秒杀了),抛出RepeatException
            int insertCount = successKillDao.insertSuccessKill(seckillId, userPhone);
            if (insertCount <= 0) {
                throw new RepeatException("Seckill can not be repeat for one user !");
            } else {
                //2-1 减库存: 库存没有了 || 秒杀时间结束了则停止秒杀,抛出SeckillCloseException
                int updateCount = seckillDao.reduceNumber(seckillId, new Date());
                if (updateCount <= 0) { //rollback
                    throw new SeckillCloseException("Seckill is closed!");
                } else { //commit
                    SuccessKill successKill = successKillDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExcution(seckillId, SeckillStateEnum.SUCCESS_KILL, successKill);
                }
            }

        } catch (SeckillCloseException e1) {
            throw e1;
        } catch (RepeatException e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //将所有的编译期异常转化为运行期异常(SeckillException 继承自 RuntimeException)
            //这样做是为了Spring声明式事务会帮助进行事务的回滚Rollback
            throw new SeckillException("Seckill inner Error ..." + e.getMessage());
        }

    }

    /**
     * @Description //执行秒杀操作 By 存储过程,执行效果跟上述函数一样,但是效率不一样
     * @Date 2019/5/19
     * @Param [seckillId, userPhone, md5]
     * @return com.gdufe.seckill.dto.SeckillExcution
     **/
    public SeckillExcution executeSeckillByProcedure(long seckillId, long userPhone, String md5) {
        //1. 验证MD5是否存在 || 被篡改了,是则抛出异常
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            return new SeckillExcution(seckillId, SeckillStateEnum.DATA_REWRITE);
        }
        //2. 传递参数并调用存储过程
        Date killTime = new Date();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);

        //3. 根据result的结果返回不同的SeckillExcution对象
        try {
            seckillDao.killByProcedure(map);
            int result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) { //秒杀成功,封装一个SeckillExcution返回
                SuccessKill successKill = successKillDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExcution(seckillId, SeckillStateEnum.SUCCESS_KILL, successKill);
            } else { //秒杀失败,封装对应的SeckillExcution返回
                return new SeckillExcution(seckillId, SeckillStateEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExcution(seckillId,SeckillStateEnum.INNER_ERROR);
        }

    }

}
