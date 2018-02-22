package org.seckill.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"})
public class SeckillServiceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() throws Exception {
        List<Seckill> seckills = seckillService.getSeckillList();
        System.out.println(seckills);
    }

    @Test
    public void getBySeckillId() throws Exception {
        long seckillId = 1000;
        Seckill seckill = seckillService.getBySeckillId(seckillId);
        System.out.println(seckill);
    }

    @Test
    public void exportSeckillUrl() throws Exception {
        long seckillId = 1004;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        System.out.println(exposer);
    }

    @Test
    public void excuteSeckill() throws Exception {
        long seckillId = 1004;
        long userPhone = 13251000261L;
        String md5 = "5cce0fddb271576d10b7f8bc6d2ad18a";
        try {
            SeckillExecution seckillExecution = seckillService.excuteSeckill(seckillId,userPhone,md5);
            System.out.println(seckillExecution);
        }catch (RepeatKillException e1){
            e1.printStackTrace();
        }catch (SeckillCloseException e2){
            e2.printStackTrace();
        }
    }
    @Test
    public void testSeckillLogic() throws Exception {
        long seckillId=1004;
        Exposer exposer=seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed())
        {
            System.out.println(exposer);
            long userPhone=13251000261L;
            String md5=exposer.getMd5();
            try {
                SeckillExecution seckillExecution = seckillService.excuteSeckill(seckillId, userPhone, md5);
                System.out.println(seckillExecution);
            }catch (RepeatKillException e)
            {
                e.printStackTrace();
            }catch (SeckillCloseException e1)
            {
                e1.printStackTrace();
            }
        }else {
            //秒杀未开启
            System.out.println(exposer);
        }
    }

    @Test
    public void excuteSeckillProcedure(){
        long seckillId = 1009;
        long phone = 13269935216L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()){
            String md5 = exposer.getMd5();
            SeckillExecution execution = seckillService.excuteSeckillProcedure(seckillId,
                    phone,md5);
            logger.info(execution.getStateInfo());
        }
    }
}