-- P2 上线前性能验证脚本（需在包含至少 5000 名教练关联学员的脱敏数据集执行）
-- 本仓库不伪造生产实测结果；DBA 应保存 EXPLAIN ANALYZE 输出并确认主要查询 < 500ms。

SET @coach_id = 1;
SET @member_id = 1;

EXPLAIN ANALYZE
SELECT DISTINCT m.id
FROM biz_course_booking cb
INNER JOIN biz_course c ON cb.course_id = c.id AND c.coach_id = @coach_id
INNER JOIN sys_member m ON cb.member_id = m.id
WHERE cb.del_flag = 0 AND c.del_flag = 0 AND m.del_flag = 0
  AND c.status != 0
  AND (cb.status IN (2, 3, 4) OR COALESCE(cb.attendance_status, 0) IN (1, 2, 3))
  AND TIMESTAMP(c.course_date, c.end_time) >= DATE_SUB(NOW(), INTERVAL 180 DAY)
ORDER BY m.id DESC
LIMIT 20;

EXPLAIN ANALYZE
SELECT r.id, r.amount, r.create_time
FROM biz_member_consume_record r
INNER JOIN biz_course_booking cb ON r.business_id = cb.id AND cb.del_flag = 0
INNER JOIN biz_course c ON cb.course_id = c.id AND c.del_flag = 0
WHERE r.business_type = 'COURSE'
  AND r.member_id = @member_id
  AND c.coach_id = @coach_id
ORDER BY r.create_time DESC
LIMIT 20;
