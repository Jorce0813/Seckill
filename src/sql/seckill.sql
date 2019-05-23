-- 秒杀操作的存储过程
-- in:需要客户端输入的值, out:执行完存储过程后输出的值
-- modify_count:修改(update / insert)操作影响的行数
-- row_count(): 返回上一条修改类型sql(update / insert / delete)的影响行数
-- row_count(): 0:未修改数据; 1:修改了一行数据; -1:sql执行出错

delimiter $$ --console(换行) ; 转化为 $$

create procedure seckill.execute_seckill
    (in v_seckill_id bigint, in v_seckill_phone bigint, in v_kill_time timestamp ,
     out r_result int)
    begin
        declare modify_count int default 0;
        start transaction ;
        insert ignore into success_kill(seckill_id,user_phone)
        values (v_seckill_id,v_seckill_phone);
        select row_count() into modify_count;
        if(modify_count = 0) then
            rollback ;
            set r_result = -1;
        elseif(modify_count < 0) then
            rollback ;
            set r_result = -2;
        else -- 开始执行减库存操作
            update seckill set number = number - 1
            where seckill_id = v_seckill_id
            and start_time <= v_kill_time
            and end_time >= v_kill_time
            and number > 0;
            select row_count() into modify_count;
            if(modify_count = 0) then
                rollback ;
                set r_result = 0;
            elseif(modify_count < 0) then
                rollback ;
                set r_result = -2;
            else
                commit ;
                set r_result = 1;
            end if;
        end if;
    end;
$$
--存储过程定义结束

delimiter ;
set @r_result = -3;
-- 调用存储过程
call execute_seckill(1001,15627216311,now(),@r_result);
--获取结果
select @r_result;


--存储过程
-- 1.存储过程的优化:优化了事务行级锁的持有时间
-- 2.不要再逻辑复杂的程序中使用存储过程
-- 3.简单的数据库逻辑控制可以使用存储过程
-- 4.seckill使用存储过程后,一个秒杀单的QPS可以达到 6000/qps