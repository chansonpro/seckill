--创建数据库
CREATE DATABASE seckill;
--使用数据库
use seckill;
--创建数据表
CREATE TABLE seckill(
seckill_id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '商品库存表',
name VARCHAR(120) NOT NULL COMMENT '商品名称',
number int(11) NOT NULL COMMENT '库存数量',
create_time TIMESTAMP NOT NULL DEFAULT current_timestamp COMMENT '创建时间',
start_time TIMESTAMP NOT NULL COMMENT '秒杀开始时间',
end_time TIMESTAMP NOT NULL COMMENT '秒杀结束时间',
PRIMARY KEY (seckill_id),
KEY idx_start_time(start_time),
KEY idx_end_time(end_time),
KEY idx_create_time(create_time)
)ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET =utf8 COMMENT ='秒杀库存表';
--初始化数据
INSERT INTO seckill(name,number,start_time,end_time)
VALUES
('3000元秒杀iPhoneX',100,'2017-10-27 00:00:00','2019-10-28 00:00:00'),
('2000元秒杀OPPOR11',200,'2017-10-27 00:00:00','2019-10-28 00:00:00'),
('1000元秒杀LENOVO',300,'2017-10-27 00:00:00','2019-10-28 00:00:00'),
('500元秒杀ipad',400,'2018-02-01 00:00:00','2019-10-28 00:00:00');
--创建秒杀成功记录表
CREATE TABLE success_killed(
seckill_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '秒杀商品的id',
user_phone BIGINT NOT NULL COMMENT '用户手机号码',
state TINYINT NOT NULL DEFAULT -1 COMMENT '状态，-1表示无效，0表示成功，1表示已付款，2表示已发货',
create_time TIMESTAMP NOT NULL COMMENT '创建时间',
PRIMARY KEY (seckill_id,user_phone),/*联合主键*/
KEY idx_create_time(create_time)
)ENGINE=InnoDB DEFAULT CHARSET =utf8 COMMENT ='秒杀成功记录表';

--手写ddl
--上线v1.1
ALTER TABLE seckill
DROP INDDEX idx_create_time,
ADD INDEX idx_c_s(start_time,end_time);


INSERT INTO seckill(name,number,start_time,end_time)
VALUES
('6000元秒杀mac电脑',10,'2018-01-07 00:00:00','2018-10-28 00:00:00');