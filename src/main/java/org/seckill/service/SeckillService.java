package org.seckill.service;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;

import java.util.List;

/**
 * User: chanson-pro
 * Date-Time: 2018-1-25 14:30
 * Description:
 */
public interface SeckillService {
    /**
     * 查询所有的秒杀记录
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 根据seckillId查询单个秒杀记录
     * @param seckillId
     * @return
     */
    Seckill getBySeckillId(long seckillId);

    //再往下，是我们最重要的行为的一些接口,后两个方法返回的对象与业务不相关，
    // 这两个对象我们用于封装service和web层传递的数据

    /**
     * 在秒杀开启时输出秒杀接口的地址，否则输出系统时间和秒杀时间
     * @param seckillId
     * @return
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作，有可能失败，有可能成功，所以要抛出我们允许的异常
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     * @throws SeckillException
     * @throws RepeatKillException
     * @throws SeckillCloseException
     */
    SeckillExecution excuteSeckill(long seckillId, long userPhone, String md5) throws
            SeckillException,
            RepeatKillException,SeckillCloseException;


}
