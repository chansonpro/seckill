package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: chanson-pro
 * Date-Time: 2018-1-25 15:52
 * Description:
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    //日志对象
    private Logger logger= LoggerFactory.getLogger(this.getClass());

    //加入一个混淆字符串(秒杀接口)的salt，为了我避免用户猜出我们的md5值，值任意给，越复杂越好
    private final String salt="as,k;s'd';h[qw231]cgyg]/.,;'#sd";

    //注入service依赖
    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private SuccessKilledDao successKilledDao;


    /**
     * 查询所有的秒杀记录
     *
     * @return
     */
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    /**
     * 根据seckillId查询单个秒杀记录
     *
     * @param seckillId
     * @return
     */
    public Seckill getBySeckillId(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    /**
     * 在秒杀开启时输出秒杀接口的地址，否则输出系统时间和秒杀时间
     *
     * @param seckillId
     * @return
     */
    public Exposer exportSeckillUrl(long seckillId) {
        //由于每个用户都要访问这个秒杀接口，因此用redis缓存起来，进行优化
        //通过超时来维护一致性！！。当然还有其他的方式
        //1.判断是否有缓存
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null){//不存在
            //2.访问数据库
            seckill = seckillDao.queryById(seckillId);
            //查不到这个秒杀产品的记录
            if (seckill==null){
                return new Exposer(false,seckillId);
            }else {
                //3.放入数据库
                redisDao.putSeckill(seckill);
            }
        }
        //若是秒杀未开启
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        //系统当前时间
        Date nowTime = new Date();
        if (startTime.getTime()>nowTime.getTime() || endTime.getTime()<nowTime.getTime()){
            return new Exposer(false,seckillId,nowTime.getTime(),
                    startTime.getTime(),endTime.getTime());
        }
        //秒杀开启，返回秒杀商品的id、用给接口加密的md5
        String md5 = getMd5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    private String getMd5(long seckillId) {
        String base = seckillId + "/" + salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    /**
     * 执行秒杀操作，有可能失败，有可能成功，所以要抛出我们允许的异常
     * 秒杀是否成功，成功:减库存，增加明细；失败:抛出异常，事务回滚
     *
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     * @throws SeckillException
     * @throws RepeatKillException
     * @throws SeckillCloseException
     */

    /**
     * 使用注解控制事务方法的优点:
     * 1.开发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求或者剥离到事务方法外部
     * 3.不是所有的方法都需要事务，如只有一条修改操作、只读操作不要事务控制
     */
    @Transactional
    public SeckillExecution excuteSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5==null || !md5.equals(getMd5(seckillId))){
            throw new SeckillException("seckill data rewrite");//秒杀数据重复了
        }
        //执行秒杀逻辑:减库存+增加购买明细
        Date nowTime = new Date();
        try {
            //1.记录秒杀,增加明细
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            //看是否该明细被重复插入，即用户是否重复秒杀
            if (insertCount <= 0) {
                throw new RepeatKillException("seckill repeated");
            }else {
                //2.减库存，热点商品竞争在此处
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                //没有更新库存记录，说明秒杀结束，rollback
                if (updateCount <= 0) {
                    throw new SeckillCloseException("seckill is closed");
                } else {
                    // commit
                    //秒杀成功,得到成功插入的明细记录,并返回成功秒杀的信息
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId,
                            userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCEESS, successKilled);
                }
            }
        }catch(SeckillCloseException e1){
            throw e1;
        }catch (SeckillException e2){
            throw e2;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            //所以编译期异常转化为运行期异
            throw new SeckillException("seckill inner error :"+e.getMessage());
        }
    }

    /**
     * 执行秒杀操作 by 存储过程
     *
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     */
    public SeckillExecution excuteSeckillProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getMd5(seckillId))){
            return new SeckillExecution(seckillId,SeckillStatEnum.DATE_REWRITE);
        }
        Date killTime = new Date();
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",killTime);
        map.put("result",null);
        try {
            //执行存储过程，result被赋值
            seckillDao.killByProcedure(map);
            //获取result
            int result = MapUtils.getInteger(map,"result",-2);
            if (result == 1){//秒杀成功
                SuccessKilled sk = successKilledDao.
                        queryByIdWithSeckill(seckillId,userPhone);
                return new SeckillExecution(seckillId,SeckillStatEnum.SUCCEESS,sk);
            }else {//如果不成功，根据result判断类型
                return new SeckillExecution(seckillId,SeckillStatEnum.stateOf(result));
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return new SeckillExecution(seckillId,SeckillStatEnum.INNER_ERROR);//内部异常
        }
    }
}
