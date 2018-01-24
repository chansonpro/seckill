package org.seckill.dao;

import org.seckill.entity.SuccessKilled;

/**
 * User: chanson-pro
 * Date-Time: 2017-12-24 15:30
 * Description:
 */
public interface SuccessKilledDao {
    /**
     * 插入购买明细,可过滤重复
     * @param seckillId
     * @param userPhone
     * @return 插入的行数
     */
    int insertSuccessKilled(long seckillId,long userPhone);


    /**
     * 根据秒杀商品的id查询明细SuccessKilled对象(该对象携带了Seckill秒杀产品对象)
     * @param seckillId
     * @return
     */
    SuccessKilled queryByIdWithSeckill(long seckillId, long userPhone);
}
