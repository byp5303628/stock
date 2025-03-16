
-- 初始化数据后复权数据捞取任务
insert into task (external_serial_no, task_type, context, status, result_msg)
select 0,
       'HfqHistoryRegressionTaskHandler',
       concat('{"code": "', code, '", "endDate": "2025-12-31", "startDate": "2025-01-01"}'),
       'INIT',
       '初始化'
from stock_info;

-- 初始化指标计算任务
insert into task (external_serial_no, task_type, context, status, result_msg)
select 0,
       'HistoryStrategyTaskHandler',
       concat('{"code": "', code, '"}'),
       'INIT',
       '初始化'
from stock_info;