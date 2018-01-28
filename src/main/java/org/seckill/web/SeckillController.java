package org.seckill.web;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * User: chanson-pro
 * Date-Time: 2018-1-26 12:27
 * Description:
 * Controller中的每一个方法都对应我们系统中的一个资源URL，其设计应该遵循Restful接口的设计风格。
 * 在包下创建一个web包用于放web层Controller开发的代码
 */
@Component
@RequestMapping("/seckill")//url:模块/资源/{}/细分
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    //1.用于访问我们商品的列表页
    //list.jsp+mode=ModelAndView
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public String list(Model model){
        List<Seckill> list = seckillService.getSeckillList();
        model.addAttribute("list",list);
        return "list";
    }

    //2.访问商品的详情页
    @RequestMapping(value = "/{seckillId}/detail",method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId")Long seckillId,Model model){
        if(seckillId == null){
            return "redirect:/seckill/list";
        }
        Seckill seckill = seckillService.getBySeckillId(seckillId);
        if (seckill == null){
            return "forward:/seckill/list";
        }
        model.addAttribute("seckill",seckill);
        return "detail";
    }
    //ajax ,json暴露秒杀接口的方法
    //3.用于返回一个json数据,封装了我们商品的秒杀地址
    @RequestMapping(value = "/{seckillId}/excute",method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Exposer> excute(Long seckillId){
        SeckillResult<Exposer> result;
        try {
            Exposer exposer =  seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true,exposer);
        }catch (Exception e){
            e.printStackTrace();
            result = new SeckillResult<Exposer>(false,e.getMessage());
        }
        return result;
    }
    //4.封装用户是否秒杀成功的信息
    @RequestMapping(value = "/{seckillId}/{md5}/excution",method = RequestMethod.POST,produces =
            {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> excute(@PathVariable("seckillId") Long seckillId,
                                                  String md5,
                                                  @CookieValue(value = "killPhone",required =
                                                          false)Long phone) {
        if (phone == null) {
            return new SeckillResult<SeckillExecution>(false, "手机未注册");
        }
        SeckillResult<SeckillExecution> result;
        try {
            SeckillExecution execution = seckillService.excuteSeckill(seckillId, phone, md5);
            return new SeckillResult<SeckillExecution>(true, execution);

        } catch (RepeatKillException e2) {
            SeckillExecution seckillExecution = new SeckillExecution(seckillId, SeckillStatEnum
                    .REPEAT_KILL);
            return new SeckillResult<SeckillExecution>(true, seckillExecution);
        } catch (SeckillCloseException e1) {
            SeckillExecution seckillExecution = new SeckillExecution(seckillId, SeckillStatEnum.END);
            return new SeckillResult<SeckillExecution>(true, seckillExecution);
        } catch (SeckillException e) {
            SeckillExecution seckillExecution = new SeckillExecution(seckillId, SeckillStatEnum
                    .INNER_ERROR);
            return new SeckillResult<SeckillExecution>(true, seckillExecution);
        }
    }
    //获取系统时间
    public SeckillResult<Long> time(){
        Date date = new Date();
        return new SeckillResult<Long>(true,date.getTime());
    }
}
