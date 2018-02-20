package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.logging.Logger;

/**
 * User: chanson-pro
 * Date-Time: 2018-2-20 21:28
 * Description:
 */
public class RedisDao {
    private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
    private final JedisPool jedisPool;

    //动态的将class文件的字节码，传递到schema，不需要认为的创建schema了
    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public RedisDao(String ip, int port) {
        this.jedisPool = new JedisPool(ip,port);
    }
    //从缓存中获取
    public Seckill getSeckill(long seckillId){
        // redis操作的逻辑
        try {
            Jedis jedis = jedisPool.getResource();//相当于建立连接
            try {
                String key = "seckill:"+seckillId;
                //redis并没有实现内部序列化，redis存储都是二进制数组，因此get得到的是byte[]，
                //get->byte[]->反序列化->拿到Object（Seckill）
                // https://github.com/eishay/jvm-serializers/wiki
                //采用自定义的方式，进行序列化，然后传递给redis数组缓存起来，这样的好处要比使用原始的
                //序列化jdk无论是空间还是时间上，都有很大的改善，而且更节省cpu
                byte[] bytes = jedis.get(key.getBytes());//传递字节数组，
                if (bytes != null){//字节数组不为空，说明获取到了
                    //获取到之后需要protostuff来转换，先利用schema创建一个空对象
                    Seckill seckill = schema.newMessage();
                    ProtobufIOUtil.mergeFrom(bytes,seckill,schema);//这句之后，空对象将被赋值
                    return seckill;
                }
            } finally {
                jedis.close();//关闭，Jedis相当于connection
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }
    //缓存不存在，获取得到后put到cache
    public String putSeckill(Seckill seckill){
        // set Object->序列化->byte[],将对象序列化成字节数组的过程。
        try {
            Jedis jedis = jedisPool.getResource();//创建链接
            try {
                String key = "seckill:"+seckill.getSeckillId();
                byte[] bytes = ProtobufIOUtil.toByteArray(seckill,schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                int timeout = 60 * 60;//单位s，缓存1小时
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                return result;
            } finally {
                jedis.close();//关闭
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }
}
