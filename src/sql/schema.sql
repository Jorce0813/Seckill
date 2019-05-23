-- 数据库初始化脚本

-- 创建数据库
create database seckill;
-- 使用数据库
use seckill;
-- 创建秒杀系统库存表
/* 在不同的Mysql版本中,对属性名称能否加上''有不同的规定，mysql5.5加上''会报错 */
create table seckill(
seckill_id bigint not null auto_increment comment '商品id',
name varchar(100) not null comment '商品名称',
number int not null comment '商品库存的数量',
start_time timestamp not null comment '秒杀开始时间',
end_time timestamp not null comment '秒杀结束时间',
create_time timestamp not null default current_timestamp comment '创建时间',
primary key (seckill_id),
key idx_start_time(start_time),
key idx_end_time(end_time),
key idx_create_time(create_time)
)engine=InnoDB auto_increment=1000 default charset=utf8 comment='秒杀库存表';

-- 初始化数据
insert into seckill(name,number,start_time,end_time)
values ('1000元秒杀iphone6','100','2019-04-21 00:00:00','2019-04-22 00:00:00');
insert into seckill(name,number,start_time,end_time)
values ('1500元秒杀honor8x','200','2019-04-21 00:00:00','2019-04-22 00:00:00');
insert into seckill(name,number,start_time,end_time)
values ('3499元秒杀huaweip30','500','2019-04-21 00:00:00','2019-04-22 00:00:00');
insert into seckill(name,number,start_time,end_time)
values ('899秒杀mi6','300','2019-04-21 00:00:00','2019-04-22 00:00:00');

-- 秒杀成功明细表
create table success_kill(
seckill_id bigint not null comment '商品id',
user_phone bigint not null comment '用户手机号',
state tinyint not null default -1 comment '秒杀状态的标识,-1:无效 0:秒杀成功 1:已付款 2:已发货',
create_time timestamp not null default current_timestamp comment '秒杀成功时间',
primary key(seckill_id,user_phone), /* 使用联合主键 */
key idx_create_time(create_time)
)engine=InnoDB default charset=utf8 comment='秒杀成功明细表';

-- 本机登录Mysql:
-- 1)进入cmd
-- 2)执行net start mysql 打开数据库服务
-- 3)进入mysql安装目录的bin目录，执行 mysql -hlocalhost -uroot -p,输入用户密码成功登录到数据库中
-- 4)执行以上DDL

-- 查看数据库建表的DDL:
-- 1)use seckill;
-- 2)show tables;
-- 3)show create table success_kill\G /* \G后面不要加分号 */

