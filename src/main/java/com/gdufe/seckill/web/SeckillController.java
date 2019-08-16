package com.gdufe.seckill.web;

import com.gdufe.seckill.Enum.SeckillStateEnum;
import com.gdufe.seckill.dto.Exposer;
import com.gdufe.seckill.dto.SeckillExcution;
import com.gdufe.seckill.dto.SeckillResult;
import com.gdufe.seckill.entity.Seckill;
import com.gdufe.seckill.exception.RepeatException;
import com.gdufe.seckill.exception.SeckillCloseException;
import com.gdufe.seckill.exception.SeckillException;
import com.gdufe.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * @ClassName SeckillController
 * @Description MVC控制层
 * @Author Lu Hengxun
 * @Date 2019/5/3 10:31
 * @Version 1.0
 **/
@Controller
@RequestMapping("/seckill")
public class SeckillController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private HttpServletRequest request; //可用于获取前端的类似于IP地址的信息

    /**
     * @return java.lang.String
     * @Description 获取秒杀列表
     * @Date 2019/5/3
     * @Param [model]
     **/
    @RequestMapping(value = "/seckillList", method = RequestMethod.GET)
    public String getSeckillList(Model model) {
        //1.通过service层获取秒杀列表
        List<Seckill> seckillList = seckillService.getSeckillList();

        //2.将秒杀列表数据存入model中
        model.addAttribute("seckillList", seckillList);

        //3.返回字符串结果
        return "seckillList";
    }

    /**
     * @return java.lang.String
     * @Description 获取指定ID的秒杀商品的详细信息
     * @Date 2019/5/3
     * @Param [seckillId, model]
     **/
    @RequestMapping(value = "/{seckillId}/seckillDetail", method = RequestMethod.GET)
    public String getSeckillById(@PathVariable Long seckillId, Model model) {

        if (seckillId == null) { //前端传过来的Id为空
            return "redirect:/seckill/seckillList";
        }

        //1.通过service层根据秒杀ID获取详情信息
        Seckill seckill = seckillService.getSeckillById(seckillId);

        if (seckill == null) { //Id不存在查询不到数据
            return "forword:/seckill/seckillList";
        }

        //2.将秒杀信息存入model中
        model.addAttribute("seckill", seckill);

        //3.返回字符串结果
        return "seckillDetail";
    }

    /**
     * @return com.gdufe.seckill.dto.SeckillResult<com.gdufe.seckill.dto.Exposer>
     * @Description 暴露秒杀接口, 将秒杀的接口信息封装成json数据；返回给前端调用
     * @Date 2019/5/3
     * @Param [seckillId]
     **/
    @RequestMapping(value = "/{seckillId}/seckillExposer",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Exposer> exposeSeckillUrl(@PathVariable Long seckillId) {
        SeckillResult<Exposer> result;
        try {
            //获取访客的ip
            String remoteAddr = request.getRemoteAddr();
            Exposer exposer = seckillService.exportSeckillUrl(seckillId, remoteAddr);
            result = new SeckillResult<Exposer>(true, exposer);
        } catch (Exception e) {
            logger.error(e.getMessage());
            result = new SeckillResult<Exposer>(false, e.getMessage());
        }
        return result;
    }


    /**
     * @Description 执行秒杀
     * @Date 2019/5/8
     * @Param [seckillId, md5, phone]
     * @return com.gdufe.seckill.dto.SeckillResult<com.gdufe.seckill.dto.SeckillExcution>
     **/
    @RequestMapping(value = "/{seckillId}/{md5}/execution",
                    method = RequestMethod.POST,
                    produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExcution> executeSeckill(@PathVariable("seckillId") Long seckillId,
                                                         @PathVariable("md5") String md5,
                                                         @CookieValue(value = "killPhone", required = false)
                                                                     Long phone) {

        //1. 验证Cookie中电话号码是否存在
        if (phone == null) {
            return new SeckillResult<SeckillExcution>(false, "没有电话号码!");
        }
        //2. 调用执行秒杀并返回相应的json数据
        try {
            //1. 原来通过调用executeSeckill()接口时会接收秒杀的各种错误信息并处理
            // SeckillExcution seckillExcution = seckillService.executeSeckill(seckillId, phone, md5);
            //2. 调用executeSeckillByProcedure()接口不会去处理单一的错误信息,统一封装后返回给前端处理
            SeckillExcution seckillExcution = seckillService.executeSeckillByProcedure(seckillId, phone, md5);
            return new SeckillResult<SeckillExcution>(true, seckillExcution);

        } catch (RepeatException e) {
            SeckillExcution seckillExcution = new SeckillExcution(seckillId, SeckillStateEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExcution>(true, seckillExcution);
        } catch (SeckillCloseException e) {
            SeckillExcution seckillExcution = new SeckillExcution(seckillId, SeckillStateEnum.END);
            return new SeckillResult<SeckillExcution>(true, seckillExcution);
        } catch (SeckillException e) {
            logger.error(e.getMessage(), e);
            SeckillExcution seckillExcution = new SeckillExcution(seckillId, SeckillStateEnum.INNER_ERROR);
            return new SeckillResult<SeckillExcution>(true, seckillExcution);
        }
    }

    /**
     * @Description 获取系统服务器当前的时间
     * @Date 2019/5/8
     * @Param []
     * @return com.gdufe.seckill.dto.SeckillResult<java.lang.Long>
     **/
    @RequestMapping(value = "/time/now",
                    method = RequestMethod.GET,
                    produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Long> getTime() {
        Date now = new Date();
        return new SeckillResult<Long>(true, now.getTime());
    }

}
