-- 教练学员查看功能 P1：独立考勤字段、索引与可恢复历史回填
-- MySQL 8.x；脚本可重复执行。上线前先 dry-run，并由 DBA 评估 online DDL。

SET @schema_name = DATABASE();

SET @ddl = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = @schema_name AND table_name = 'biz_course_booking'
       AND column_name = 'attendance_status') = 0,
    'ALTER TABLE biz_course_booking ADD COLUMN attendance_status TINYINT NOT NULL DEFAULT 0 COMMENT ''考勤状态：0未登记、1已签到、2已完成、3缺席'' AFTER status',
    'SELECT ''attendance_status already exists''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = @schema_name AND table_name = 'biz_course_booking'
       AND column_name = 'actual_checkin_time') = 0,
    'ALTER TABLE biz_course_booking ADD COLUMN actual_checkin_time DATETIME NULL COMMENT ''实际签到时间'' AFTER attendance_status',
    'SELECT ''actual_checkin_time already exists''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = @schema_name AND table_name = 'biz_course_booking'
       AND column_name = 'actual_finish_time') = 0,
    'ALTER TABLE biz_course_booking ADD COLUMN actual_finish_time DATETIME NULL COMMENT ''实际完成时间'' AFTER actual_checkin_time',
    'SELECT ''actual_finish_time already exists''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = @schema_name AND table_name = 'biz_course_booking'
       AND index_name = 'idx_cb_course_member_status') = 0,
    'CREATE INDEX idx_cb_course_member_status ON biz_course_booking (course_id, member_id, status, attendance_status, del_flag)',
    'SELECT ''idx_cb_course_member_status already exists''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = @schema_name AND table_name = 'biz_course_booking'
       AND index_name = 'idx_cb_member_attendance') = 0,
    'CREATE INDEX idx_cb_member_attendance ON biz_course_booking (member_id, attendance_status, status, del_flag, course_id)',
    'SELECT ''idx_cb_member_attendance already exists''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = @schema_name AND table_name = 'biz_course'
       AND index_name = 'idx_c_coach_date_range') = 0,
    'CREATE INDEX idx_c_coach_date_range ON biz_course (coach_id, course_date, start_time, end_time, status, del_flag)',
    'SELECT ''idx_c_coach_date_range already exists''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = @schema_name AND table_name = 'biz_course_booking'
       AND index_name = 'idx_cb_course_attendance') = 0,
    'CREATE INDEX idx_cb_course_attendance ON biz_course_booking (course_id, attendance_status, status, del_flag)',
    'SELECT ''idx_cb_course_attendance already exists''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 不为 LIKE '%keyword%' 的姓名或手机号创建普通 BTree 索引。

CREATE TABLE IF NOT EXISTS coach_student_attendance_backfill_checkpoint (
    migration_key VARCHAR(64) PRIMARY KEY,
    last_id BIGINT NOT NULL DEFAULT 0,
    affected_rows BIGINT NOT NULL DEFAULT 0,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='教练学员考勤迁移断点';

INSERT INTO coach_student_attendance_backfill_checkpoint (migration_key, last_id, affected_rows)
VALUES ('booking_status4_attendance2', 0, 0)
ON DUPLICATE KEY UPDATE migration_key = VALUES(migration_key);

-- 每次执行只处理一批；重复执行直至 @backfill_batch_end_id 为 NULL。
SET @backfill_batch_size = 2000;
SET @backfill_last_id = (
    SELECT last_id FROM coach_student_attendance_backfill_checkpoint
    WHERE migration_key = 'booking_status4_attendance2');
SET @backfill_batch_end_id = (
    SELECT MAX(batch_ids.id)
    FROM (
        SELECT id FROM biz_course_booking
        WHERE id > @backfill_last_id
        ORDER BY id
        LIMIT 2000
    ) AS batch_ids);

UPDATE biz_course_booking
SET attendance_status = 2,
    actual_finish_time = COALESCE(actual_finish_time, update_time)
WHERE @backfill_batch_end_id IS NOT NULL
  AND id > @backfill_last_id
  AND id <= @backfill_batch_end_id
  AND del_flag = 0
  AND status = 4
  AND COALESCE(attendance_status, 0) = 0;

SET @backfill_affected_rows = ROW_COUNT();
UPDATE coach_student_attendance_backfill_checkpoint
SET last_id = COALESCE(@backfill_batch_end_id, last_id),
    affected_rows = affected_rows + @backfill_affected_rows
WHERE migration_key = 'booking_status4_attendance2';

-- 验证：取消记录绝不能被迁移成缺席。
SELECT status, attendance_status, COUNT(*)
FROM biz_course_booking
WHERE del_flag = 0
GROUP BY status, attendance_status;

SELECT * FROM coach_student_attendance_backfill_checkpoint
WHERE migration_key = 'booking_status4_attendance2';
