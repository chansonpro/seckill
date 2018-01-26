package org.seckill.exception;

/**
 * User: chanson-pro
 * Date-Time: 2018-1-25 15:47
 * Description:秒杀关闭异常，当秒杀结束时用户还要进行秒杀就会出现
 */
public class SeckillCloseException extends SeckillException{


    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
