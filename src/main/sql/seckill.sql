-- 秒杀执行的存储过程
DELIMITER $$ --console ;转换成$$
-- 定义存储过程
-- 参数：in 输入参数 out输出参数
-- row_count():返回上一条修改类型sql(insert,delete,update)的影响行数
-- row_count(): 0:未返回数据；>0：表示修改行数；<0:sql错误/未执行sql
CREATE PROCEDURE `seckill`.`execute_seckill`
    (in v_seckill_id bigint, in v_phone bigint, in v_killtime timestamp, out r_result int)
    BEGIN
      DECLARE insert_count int DEFAULT 0;
      START TRANSACTION;
      INSERT ignore INTO success_killed(seckill_id,user_phone,create_time)
        VALUES (v_seckill_id,v_phone,v_killtime);
      SELECT ROW_COUNT() INTO insert_count;
      IF (insert_count = 0) THEN
        ROLLBACK;
        SET r_result = -1;
      ELSEIF (insert_count < 0) THEN
        ROLLBACK;
        SET r_result = -2;
      ELSE
        UPDATE seckill
          SET number = number - 1
          WHERE seckill_id = v_seckill_id
          AND start_time < v_killtime
          AND end_time >= v_killtime
          AND number > 0;
        SELECT ROW_COUNT() INTO insert_count;
        IF(insert_count = 0) THEN
          ROLLBACK;
          SET r_result = 0;
        ELSEIF (insert_count < 0) THEN
          ROLLBACK;
          SET r_result = -2;
        ELSE
          COMMIT;
          SET r_result = 1;
        END IF;
      END IF;
    END
$$
--存储过程定义结束
DELIMITER ;
SET @r_result = -3;
--执行存储过程
call execute_seckill(1009,13251000123,now(),@r_result);
--获取结果
SELECT @r_result;


--打开MySQL console端
show CREATE PROCEDURE execute_seckill \G;
--删除存储过程
DROP PROCEDURE execute_seckill;

-- 存储过程（场景：银行 互联网公司很少用）
-- 1:存储过程优化：事务行级锁持有的时间
-- 2:不要过度依赖存储过程
-- 3:简单的逻辑可以应用存储过程
-- 4:QPS:一个秒杀单6000/qps
