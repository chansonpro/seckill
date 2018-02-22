package org.seckill.dao.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dao.SeckillDao;
import org.seckill.entity.Seckill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
/**
 *
 * 配置spring和junit整合，这样junit在启动时就会加载spring容器
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDaoTest {
    private long id = 1009;
    @Autowired
    private RedisDao redisDao;
    @Autowired
     private SeckillDao seckillDao;
    @Test
    public void testSeckill() throws Exception {
        //get and set
        Seckill seckill = redisDao.getSeckill(id);
        if(seckill == null){//缓存为空
            seckill = seckillDao.queryById(id);//从db获取
            if (seckill != null){
                String result = redisDao.putSeckill(seckill);
                System.out.println(result);
                //从缓存中获取，查看
                Seckill seckill1 = redisDao.getSeckill(id);
                System.out.println(seckill);
            }
        }
    }


}