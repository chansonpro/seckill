package org.seckill.exception;

/**
 * User: chanson-pro
 * Date-Time: 2018-1-25 15:45
 * Description:创建我们在秒杀业务过程中允许的异常，重复秒杀异常
 * Mysql只支持运行期异常的回滚操作
 */
public class RepeatKillException extends SeckillException{


    public RepeatKillException(String message) {
        super(message);
    }


    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
