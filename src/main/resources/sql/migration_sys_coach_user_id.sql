-- 增量迁移：为 sys_coach 表增加 user_id 字段（COACH 角色账号与教练档案绑定）
-- 适用场景：已有数据库，无需重建表。新环境请直接使用 badminton.sql。
-- 执行前请确认 sys_user 表已存在。

ALTER TABLE `sys_coach`
  ADD COLUMN `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID（外键关联sys_user，COACH角色绑定）' AFTER `venue_id`;

CREATE UNIQUE INDEX `idx_user_id` ON `sys_coach` (`user_id` ASC);

ALTER TABLE `sys_coach`
  ADD CONSTRAINT `sys_coach_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT;
