/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80028 (8.0.28)
 Source Host           : localhost:3306
 Source Schema         : badminton

 Target Server Type    : MySQL
 Target Server Version : 80028 (8.0.28)
 File Encoding         : 65001

 Date: 22/02/2026 16:06:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for biz_booking
-- ----------------------------
DROP TABLE IF EXISTS `biz_booking`;
CREATE TABLE `biz_booking`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `booking_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预约单号（唯一，自动生成，格式：BK+日期+序号，如BK20250118001）',
  `member_id` bigint NOT NULL COMMENT '会员ID（外键关联sys_member）',
  `court_id` bigint NOT NULL COMMENT '场地ID（外键关联sys_court）',
  `booking_date` date NOT NULL COMMENT '预约日期',
  `start_time` time NOT NULL COMMENT '开始时间（预约开始时间）',
  `end_time` time NOT NULL COMMENT '结束时间（预约结束时间）',
  `actual_start_time` datetime NULL DEFAULT NULL COMMENT '实际开始时间（可为空）',
  `actual_end_time` datetime NULL DEFAULT NULL COMMENT '实际结束时间（可为空）',
  `duration` int NULL DEFAULT NULL COMMENT '预约时长（小时）',
  `order_amount` decimal(10, 2) NOT NULL COMMENT '订单金额',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式（CASH-现金，ALIPAY-支付宝，WECHAT-微信，BALANCE-余额）',
  `payment_status` tinyint(1) NULL DEFAULT 0 COMMENT '支付状态（0-未支付，1-已支付，2-已退款）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '预约状态（0-已取消，1-待支付，2-已支付，3-进行中，4-已完成）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `cancel_policy_id` bigint NULL DEFAULT NULL COMMENT '取消政策ID',
  `cancel_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '取消手续费',
  `refund_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '实际退款金额',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `booking_no`(`booking_no` ASC) USING BTREE,
  UNIQUE INDEX `idx_booking_no`(`booking_no` ASC) USING BTREE,
  INDEX `idx_member_id`(`member_id` ASC) USING BTREE,
  INDEX `idx_court_id`(`court_id` ASC) USING BTREE,
  INDEX `idx_booking_date`(`booking_date` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_booking_date_status`(`booking_date` ASC, `status` ASC) USING BTREE,
  INDEX `idx_court_date_status`(`court_id` ASC, `booking_date` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `biz_booking_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `sys_member` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `biz_booking_ibfk_2` FOREIGN KEY (`court_id`) REFERENCES `sys_court` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '场地预约表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_booking
-- ----------------------------
INSERT INTO `biz_booking` VALUES (1, 'BK20250115001', 1, 28, '2026-02-13', '18:30:00', '21:30:00', NULL, NULL, 3, 15.00, 'BALANCE', 1, 2, '', '2025-01-10 10:00:00', '2026-02-11 22:38:35', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (2, 'BK20250115002', 2, 2, '2025-01-15', '14:00:00', '16:00:00', NULL, NULL, 2, 160.00, 'WECHAT', 1, 2, NULL, '2025-01-10 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (3, 'BK20250115003', 3, 3, '2025-01-15', '18:00:00', '20:00:00', NULL, NULL, 2, 160.00, 'BALANCE', 1, 2, NULL, '2025-01-10 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (4, 'BK20250116001', 4, 4, '2025-01-16', '10:00:00', '12:00:00', NULL, NULL, 2, 200.00, 'ALIPAY', 1, 2, NULL, '2025-01-11 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (5, 'BK20250116002', 5, 5, '2025-01-16', '15:00:00', '17:00:00', NULL, NULL, 2, 200.00, 'WECHAT', 1, 2, NULL, '2025-01-11 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (6, 'BK20250117001', 6, 6, '2025-01-17', '09:00:00', '11:00:00', NULL, NULL, 2, 180.00, 'BALANCE', 1, 2, NULL, '2025-01-12 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (7, 'BK20250117002', 7, 7, '2025-01-17', '14:00:00', '16:00:00', NULL, NULL, 2, 180.00, 'ALIPAY', 1, 2, NULL, '2025-01-12 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (8, 'BK20250118001', 8, 8, '2025-01-18', '19:00:00', '21:00:00', NULL, NULL, 2, 180.00, 'WECHAT', 1, 2, NULL, '2025-01-13 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (9, 'BK20250119001', 9, 9, '2025-01-19', '10:00:00', '12:00:00', NULL, NULL, 2, 170.00, 'BALANCE', 1, 2, NULL, '2025-01-14 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (10, 'BK20250120001', 10, 10, '2025-01-20', '15:00:00', '17:00:00', NULL, NULL, 2, 170.00, 'ALIPAY', 1, 2, NULL, '2025-01-15 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (11, 'BK20250128001', 1, 1, '2025-01-28', '09:00:00', '11:00:00', NULL, NULL, 2, 160.00, 'ALIPAY', 1, 4, NULL, '2025-01-25 10:00:00', '2026-02-14 12:59:47', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (12, 'BK20250128002', 2, 2, '2025-01-28', '10:00:00', '12:00:00', NULL, NULL, 2, 160.00, 'WECHAT', 1, 4, NULL, '2025-01-25 10:00:00', '2026-02-14 12:59:47', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (13, 'BK20250128003', 3, 28, '2026-02-13', '18:30:00', '21:30:00', NULL, NULL, 3, 15.00, 'BALANCE', 1, 2, '', '2025-01-25 10:00:00', '2026-02-11 22:37:36', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (14, 'BK20250128004', 4, 4, '2025-01-28', '15:00:00', '17:00:00', NULL, NULL, 2, 200.00, 'ALIPAY', 1, 2, NULL, '2025-01-25 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (15, 'BK20250128005', 5, 5, '2025-01-28', '18:00:00', '20:00:00', NULL, NULL, 2, 200.00, 'WECHAT', 1, 2, NULL, '2025-01-25 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (16, 'BK20250128006', 6, 6, '2025-01-28', '19:00:00', '21:00:00', NULL, NULL, 2, 180.00, 'BALANCE', 1, 2, NULL, '2025-01-25 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (17, 'BK20250128007', 7, 7, '2025-01-28', '20:00:00', '22:00:00', NULL, NULL, 2, 180.00, 'ALIPAY', 1, 2, NULL, '2025-01-25 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (18, 'BK20250121001', 11, 8, '2025-01-21', '09:00:00', '11:00:00', NULL, NULL, 2, 180.00, 'WECHAT', 1, 4, NULL, '2025-01-16 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (19, 'BK20250121002', 12, 9, '2025-01-21', '14:00:00', '16:00:00', NULL, NULL, 2, 170.00, 'BALANCE', 1, 4, NULL, '2025-01-16 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (20, 'BK20250122001', 13, 10, '2025-01-22', '10:00:00', '12:00:00', NULL, NULL, 2, 170.00, 'ALIPAY', 1, 4, NULL, '2025-01-17 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (21, 'BK20250122002', 14, 11, '2025-01-22', '15:00:00', '17:00:00', NULL, NULL, 2, 170.00, 'WECHAT', 1, 4, NULL, '2025-01-17 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (22, 'BK20250123001', 15, 12, '2025-01-23', '09:00:00', '11:00:00', NULL, NULL, 2, 176.00, 'BALANCE', 1, 4, NULL, '2025-01-18 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (23, 'BK20250123002', 16, 13, '2025-01-23', '14:00:00', '16:00:00', NULL, NULL, 2, 176.00, 'ALIPAY', 1, 4, NULL, '2025-01-18 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (24, 'BK20250124001', 17, 14, '2025-01-24', '10:00:00', '12:00:00', NULL, NULL, 2, 176.00, 'WECHAT', 1, 4, NULL, '2025-01-19 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (25, 'BK20250124002', 18, 15, '2025-01-24', '15:00:00', '17:00:00', NULL, NULL, 2, 150.00, 'BALANCE', 1, 4, NULL, '2025-01-19 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (26, 'BK20250125001', 19, 1, '2025-01-25', '09:00:00', '11:00:00', NULL, NULL, 2, 160.00, 'ALIPAY', 1, 4, NULL, '2025-01-20 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (27, 'BK20250125002', 20, 2, '2025-01-25', '14:00:00', '16:00:00', NULL, NULL, 2, 160.00, 'WECHAT', 1, 4, NULL, '2025-01-20 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (28, 'BK20250126001', 21, 3, '2025-01-26', '10:00:00', '12:00:00', NULL, NULL, 2, 160.00, 'BALANCE', 1, 4, NULL, '2025-01-21 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (29, 'BK20250126002', 22, 4, '2025-01-26', '15:00:00', '17:00:00', NULL, NULL, 2, 200.00, 'ALIPAY', 1, 4, NULL, '2025-01-21 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (30, 'BK20250127001', 23, 5, '2025-01-27', '09:00:00', '11:00:00', NULL, NULL, 2, 200.00, 'WECHAT', 2, 0, NULL, '2025-01-22 10:00:00', '2026-02-09 22:50:02', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (31, 'BK20250127002', 24, 6, '2025-01-27', '14:00:00', '16:00:00', NULL, NULL, 2, 180.00, 'BALANCE', 1, 4, NULL, '2025-01-22 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (32, 'BK20250127003', 25, 7, '2025-01-27', '18:00:00', '20:00:00', NULL, NULL, 2, 180.00, 'ALIPAY', 1, 4, NULL, '2025-01-22 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (33, 'BK20260209001', 45, 28, '2026-02-10', '18:30:00', '21:30:00', NULL, NULL, 3, 15.00, NULL, NULL, 3, '带一些朋友', '2026-02-09 23:06:10', '2026-02-09 23:49:04', NULL, NULL, 0.00, 0.00, 1);
INSERT INTO `biz_booking` VALUES (36, 'BK20260209002', 45, 28, '2026-02-13', '18:30:00', '21:30:00', NULL, NULL, 3, 15.00, NULL, NULL, 4, '', '2026-02-09 23:57:07', '2026-02-14 12:50:45', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (37, 'BK20260214001', 45, 28, '2026-02-14', '13:00:00', '13:03:00', NULL, NULL, 1, 15.00, 'BALANCE', 2, 0, '', '2026-02-14 13:01:55', '2026-02-14 13:04:19', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_booking` VALUES (38, 'BK20260214002', 45, 28, '2026-02-14', '13:06:00', '13:07:00', NULL, NULL, 1, 15.00, 'BALANCE', 2, 0, '', '2026-02-14 13:06:21', '2026-02-14 13:07:55', NULL, NULL, 0.00, 0.00, 0);

-- ----------------------------
-- Table structure for biz_cancel_policy
-- ----------------------------
DROP TABLE IF EXISTS `biz_cancel_policy`;
CREATE TABLE `biz_cancel_policy`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `policy_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '政策名称',
  `venue_id` bigint NULL DEFAULT NULL COMMENT '场馆ID（为空表示全局政策）',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务类型（BOOKING/COURSE/EQUIPMENT/TOURNAMENT）',
  `hours_before` int NOT NULL COMMENT '提前多少小时',
  `refund_rate` decimal(5, 2) NOT NULL COMMENT '退款比例（0.00-1.00）',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_venue_business`(`venue_id` ASC, `business_type` ASC) USING BTREE,
  INDEX `idx_active`(`is_active` ASC) USING BTREE,
  CONSTRAINT `biz_cancel_policy_ibfk_1` FOREIGN KEY (`venue_id`) REFERENCES `sys_venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '取消政策配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_cancel_policy
-- ----------------------------
INSERT INTO `biz_cancel_policy` VALUES (1, '提前24小时取消', NULL, 'BOOKING', 24, 1.00, 1, '2026-02-17 13:31:19', '2026-02-17 13:31:19');
INSERT INTO `biz_cancel_policy` VALUES (2, '提前12小时取消', NULL, 'BOOKING', 12, 0.80, 1, '2026-02-17 13:31:19', '2026-02-17 13:31:19');
INSERT INTO `biz_cancel_policy` VALUES (3, '提前6小时取消', NULL, 'BOOKING', 6, 0.50, 1, '2026-02-17 13:31:19', '2026-02-17 13:31:19');
INSERT INTO `biz_cancel_policy` VALUES (4, '6小时内取消', NULL, 'BOOKING', 0, 0.00, 1, '2026-02-17 13:31:19', '2026-02-17 13:31:19');
INSERT INTO `biz_cancel_policy` VALUES (5, '提前24小时取消', NULL, 'COURSE', 24, 1.00, 1, '2026-02-17 13:31:19', '2026-02-17 13:31:19');
INSERT INTO `biz_cancel_policy` VALUES (6, '提前12小时取消', NULL, 'COURSE', 12, 0.80, 1, '2026-02-17 13:31:19', '2026-02-17 13:31:19');
INSERT INTO `biz_cancel_policy` VALUES (7, '提前6小时取消', NULL, 'COURSE', 6, 0.50, 1, '2026-02-17 13:31:19', '2026-02-17 13:31:19');
INSERT INTO `biz_cancel_policy` VALUES (8, '6小时内取消', NULL, 'COURSE', 0, 0.00, 1, '2026-02-17 13:31:19', '2026-02-17 13:31:19');

-- ----------------------------
-- Table structure for biz_course
-- ----------------------------
DROP TABLE IF EXISTS `biz_course`;
CREATE TABLE `biz_course`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称',
  `coach_id` bigint NOT NULL COMMENT '教练ID（外键关联sys_coach）',
  `court_id` bigint NULL DEFAULT NULL COMMENT '场地ID（外键关联sys_court，可为空）',
  `course_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '课程内容',
  `course_price` decimal(10, 2) NOT NULL COMMENT '课程价格',
  `course_duration` int NULL DEFAULT NULL COMMENT '课程时长（分钟）',
  `course_date` date NOT NULL COMMENT '课程日期',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `max_students` int NULL DEFAULT 10 COMMENT '最大学员数',
  `current_students` int NULL DEFAULT 0 COMMENT '当前报名人数',
  `version` int NULL DEFAULT 0 COMMENT '版本号（乐观锁，用于名额扣减并发控制）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态（0-已取消，1-报名中，2-进行中，3-已结束）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_coach_id`(`coach_id` ASC) USING BTREE,
  INDEX `idx_court_id`(`court_id` ASC) USING BTREE,
  INDEX `idx_course_date`(`course_date` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_course_version`(`id` ASC, `version` ASC) USING BTREE,
  INDEX `idx_course_date_status`(`course_date` ASC, `status` ASC) USING BTREE,
  INDEX `idx_coach_date`(`coach_id` ASC, `course_date` ASC) USING BTREE,
  CONSTRAINT `biz_course_ibfk_1` FOREIGN KEY (`coach_id`) REFERENCES `sys_coach` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `biz_course_ibfk_2` FOREIGN KEY (`court_id`) REFERENCES `sys_court` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_course
-- ----------------------------
INSERT INTO `biz_course` VALUES (1, '专业羽毛球基础班', 1, 1, '学习羽毛球基本技术，包括握拍、发球、接球等', 200.00, 90, '2025-01-15', '09:00:00', '10:30:00', 10, 8, 0, 3, '2024-12-01 10:00:00', '2026-02-15 10:40:28', 0);
INSERT INTO `biz_course` VALUES (2, '专业羽毛球提高班', 1, 2, '提升技术水平，学习高级技术和战术', 200.00, 90, '2025-01-15', '14:00:00', '15:30:00', 10, 6, 0, 3, '2024-12-01 10:00:00', '2026-02-15 10:40:28', 0);
INSERT INTO `biz_course` VALUES (3, '青少年启蒙班', 2, 3, '适合6-12岁儿童，培养兴趣和基本技能', 150.00, 60, '2025-01-16', '10:00:00', '11:00:00', 12, 10, 0, 3, '2024-12-01 10:00:00', '2026-02-15 10:40:28', 0);
INSERT INTO `biz_course` VALUES (4, '技术提升班', 3, 4, '纠正技术动作，提升技术水平', 180.00, 90, '2025-01-17', '15:00:00', '16:30:00', 8, 5, 0, 3, '2024-12-01 10:00:00', '2026-02-15 10:40:28', 0);
INSERT INTO `biz_course` VALUES (5, '初学入门班', 4, 6, '零基础学员入门课程', 120.00, 60, '2025-01-18', '09:00:00', '10:00:00', 15, 12, 0, 3, '2024-12-01 10:00:00', '2026-02-15 10:40:28', 0);
INSERT INTO `biz_course` VALUES (6, '比赛训练班', 5, 7, '针对比赛的专业训练', 250.00, 120, '2025-01-19', '14:00:00', '16:00:00', 6, 4, 0, 3, '2024-12-01 10:00:00', '2026-02-15 10:40:28', 0);
INSERT INTO `biz_course` VALUES (7, '成人培训班', 6, 10, '成人羽毛球培训课程', 160.00, 90, '2025-01-20', '19:00:00', '20:30:00', 10, 7, 0, 3, '2024-12-01 10:00:00', '2026-02-15 10:40:28', 0);
INSERT INTO `biz_course` VALUES (8, '体能训练班', 7, 11, '专业体能训练和运动康复', 140.00, 60, '2025-01-21', '18:00:00', '19:00:00', 12, 9, 0, 3, '2024-12-01 10:00:00', '2026-02-15 10:40:28', 0);
INSERT INTO `biz_course` VALUES (9, '双打训练班', 8, 13, '双打配合和战术训练', 170.00, 90, '2025-01-22', '16:00:00', '17:30:00', 8, 6, 0, 3, '2024-12-01 10:00:00', '2026-02-15 10:40:28', 0);
INSERT INTO `biz_course` VALUES (10, '混双训练班', 9, 14, '混双专项训练', 165.00, 90, '2025-01-23', '17:00:00', '18:30:00', 8, 5, 0, 3, '2024-12-01 10:00:00', '2026-02-15 10:40:28', 0);
INSERT INTO `biz_course` VALUES (11, '基础训练班', 10, 15, '基础技术训练，适合初学者', 110.00, 60, '2025-01-24', '10:00:00', '11:00:00', 15, 11, 0, 3, '2024-12-01 10:00:00', '2026-02-15 10:40:28', 0);
INSERT INTO `biz_course` VALUES (12, '专业羽毛球基础班', 1, 1, '学习羽毛球基本技术', 200.00, 90, '2024-12-15', '09:00:00', '10:30:00', 10, 8, 0, 3, '2024-11-01 10:00:00', NULL, 0);
INSERT INTO `biz_course` VALUES (13, '青少年启蒙班', 2, 3, '适合6-12岁儿童', 150.00, 60, '2024-12-16', '10:00:00', '11:00:00', 12, 10, 0, 3, '2024-11-01 10:00:00', NULL, 0);
INSERT INTO `biz_course` VALUES (14, '初学入门班', 4, 6, '零基础学员入门课程', 120.00, 60, '2024-12-18', '09:00:00', '10:00:00', 15, 12, 0, 3, '2024-11-01 10:00:00', NULL, 0);
INSERT INTO `biz_course` VALUES (15, '成人培训班', 6, 10, '成人羽毛球培训课程', 160.00, 90, '2024-12-20', '19:00:00', '20:30:00', 10, 7, 0, 3, '2024-11-01 10:00:00', NULL, 0);
INSERT INTO `biz_course` VALUES (16, '双打训练班', 14, 28, NULL, 50.00, 1, '2026-02-15', '10:44:00', '10:45:00', 10, NULL, 0, 3, '2026-02-15 10:44:33', '2026-02-15 10:50:09', 0);
INSERT INTO `biz_course` VALUES (17, '双打训练班', 14, 28, NULL, 50.00, 7, '2026-02-15', '10:44:00', '10:51:00', 5, 0, 0, 0, '2026-02-15 10:50:32', '2026-02-15 10:51:22', 1);
INSERT INTO `biz_course` VALUES (18, '双打训练班', 14, 28, NULL, 50.00, 8, '2026-02-15', '10:44:00', '10:52:00', 10, 0, 0, 1, '2026-02-15 10:50:53', '2026-02-15 10:51:02', 1);
INSERT INTO `biz_course` VALUES (19, '双打训练班', 14, 28, NULL, 50.00, 8, '2026-02-15', '10:44:00', '10:52:00', 10, 0, 0, 3, '2026-02-15 10:51:12', '2026-02-15 10:51:23', 1);
INSERT INTO `biz_course` VALUES (20, '双打训练班', 14, 28, NULL, 50.00, 1, '2026-02-15', '10:52:00', '10:53:00', 3, 3, 0, 3, '2026-02-15 10:51:36', '2026-02-16 22:03:20', 0);

-- ----------------------------
-- Table structure for biz_course_booking
-- ----------------------------
DROP TABLE IF EXISTS `biz_course_booking`;
CREATE TABLE `biz_course_booking`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程预约ID',
  `booking_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预约单号（唯一，自动生成，格式：CB+日期+序号，如CB20250118001）',
  `member_id` bigint NOT NULL COMMENT '会员ID（外键关联sys_member）',
  `course_id` bigint NOT NULL COMMENT '课程ID（外键关联biz_course）',
  `order_amount` decimal(10, 2) NOT NULL COMMENT '订单金额',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式',
  `payment_status` tinyint(1) NULL DEFAULT 0 COMMENT '支付状态（0-未支付，1-已支付，2-已退款）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '预约状态（0-已取消，1-待支付，2-已支付，3-进行中，4-已完成）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `cancel_policy_id` bigint NULL DEFAULT NULL COMMENT '取消政策ID',
  `cancel_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '取消手续费',
  `refund_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '实际退款金额',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `booking_no`(`booking_no` ASC) USING BTREE,
  UNIQUE INDEX `idx_booking_no`(`booking_no` ASC) USING BTREE,
  INDEX `idx_member_id`(`member_id` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `biz_course_booking_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `sys_member` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `biz_course_booking_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `biz_course` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程预约表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_course_booking
-- ----------------------------
INSERT INTO `biz_course_booking` VALUES (1, 'CB20250115001', 1, 1, 200.00, 'ALIPAY', 1, 4, NULL, '2025-01-10 10:00:00', '2026-02-16 19:22:34', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (2, 'CB20250115002', 2, 1, 200.00, 'WECHAT', 1, 4, NULL, '2025-01-10 10:00:00', '2026-02-16 19:22:34', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (3, 'CB20250115003', 3, 2, 200.00, 'BALANCE', 1, 4, NULL, '2025-01-10 10:00:00', '2026-02-16 19:22:34', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (4, 'CB20250116001', 4, 3, 150.00, 'ALIPAY', 1, 4, NULL, '2025-01-11 10:00:00', '2026-02-16 19:22:34', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (5, 'CB20250116002', 5, 3, 150.00, 'WECHAT', 1, 4, NULL, '2025-01-11 10:00:00', '2026-02-16 19:22:34', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (6, 'CB20250117001', 6, 4, 180.00, 'BALANCE', 1, 4, NULL, '2025-01-12 10:00:00', '2026-02-16 19:22:34', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (7, 'CB20250118001', 7, 5, 120.00, 'ALIPAY', 1, 4, NULL, '2025-01-13 10:00:00', '2026-02-16 19:22:34', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (8, 'CB20250119001', 8, 6, 250.00, 'WECHAT', 1, 4, NULL, '2025-01-14 10:00:00', '2026-02-16 19:22:34', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (9, 'CB20250120001', 9, 7, 160.00, 'BALANCE', 1, 4, NULL, '2025-01-15 10:00:00', '2026-02-16 19:22:34', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (10, 'CB20250121001', 10, 8, 140.00, 'ALIPAY', 1, 4, NULL, '2025-01-16 10:00:00', '2026-02-16 19:22:34', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (11, 'CB20250122001', 11, 9, 170.00, 'WECHAT', 1, 4, NULL, '2025-01-17 10:00:00', '2026-02-16 19:22:34', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (12, 'CB20250123001', 12, 10, 165.00, 'BALANCE', 1, 4, NULL, '2025-01-18 10:00:00', '2026-02-16 19:22:34', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (13, 'CB20250124001', 13, 11, 110.00, 'ALIPAY', 1, 4, NULL, '2025-01-19 10:00:00', '2026-02-16 19:22:34', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (14, 'CB20250125001', 14, 1, 200.00, 'WECHAT', 1, 4, NULL, '2025-01-20 10:00:00', '2026-02-16 19:22:34', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (15, 'CB20250126001', 15, 2, 200.00, 'BALANCE', 1, 4, NULL, '2025-01-21 10:00:00', '2026-02-16 19:22:34', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (16, 'CB20241215001', 1, 12, 200.00, 'ALIPAY', 1, 4, NULL, '2024-11-01 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (17, 'CB20241215002', 2, 12, 200.00, 'WECHAT', 1, 4, NULL, '2024-11-01 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (18, 'CB20241216001', 3, 13, 150.00, 'BALANCE', 1, 4, NULL, '2024-11-01 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (19, 'CB20241218001', 4, 14, 120.00, 'ALIPAY', 1, 4, NULL, '2024-11-01 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (20, 'CB20241220001', 5, 15, 160.00, 'WECHAT', 1, 4, NULL, '2024-11-01 10:00:00', NULL, NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (21, 'CB20260216001', 45, 20, 500.00, NULL, 0, 1, NULL, '2026-02-16 19:35:50', '2026-02-16 19:37:59', NULL, NULL, 0.00, 0.00, 1);
INSERT INTO `biz_course_booking` VALUES (25, 'CB20260216002', 45, 20, 500.00, 'BALANCE', 2, 0, NULL, '2026-02-16 19:43:42', '2026-02-16 22:03:10', NULL, NULL, 0.00, 0.00, 1);
INSERT INTO `biz_course_booking` VALUES (26, 'CB20260216003', 1, 20, 500.00, 'BALANCE', 1, 4, NULL, '2026-02-16 21:46:48', '2026-02-16 21:49:01', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (27, 'CB20260216004', 3, 20, 500.00, 'BALANCE', 1, 4, NULL, '2026-02-16 21:49:22', '2026-02-16 21:50:01', NULL, NULL, 0.00, 0.00, 0);
INSERT INTO `biz_course_booking` VALUES (28, 'CB20260216005', 45, 20, 500.00, 'BALANCE', 1, 4, NULL, '2026-02-16 22:03:20', '2026-02-16 22:04:01', NULL, NULL, 0.00, 0.00, 0);

-- ----------------------------
-- Table structure for biz_equipment_rental
-- ----------------------------
DROP TABLE IF EXISTS `biz_equipment_rental`;
CREATE TABLE `biz_equipment_rental`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '器材租借ID',
  `rental_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '租借单号（唯一，自动生成，格式：ER+日期+序号，如ER20250118001）',
  `member_id` bigint NOT NULL COMMENT '会员ID（外键关联sys_member）',
  `equipment_id` bigint NOT NULL COMMENT '器材ID（外键关联sys_equipment）',
  `quantity` int NOT NULL COMMENT '租借数量',
  `rental_date` date NOT NULL COMMENT '租借日期',
  `expected_return_date` date NULL DEFAULT NULL COMMENT '预计归还日期',
  `return_date` datetime NULL DEFAULT NULL COMMENT '实际归还日期（可为空，归还时填写）',
  `rental_amount` decimal(10, 2) NOT NULL COMMENT '租借金额',
  `unit_price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '租借单价快照（按小时）',
  `deposit_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '押金总额',
  `duration_hours` int NULL DEFAULT NULL COMMENT '计费小时数（向上取整）',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式',
  `payment_status` tinyint(1) NULL DEFAULT 0 COMMENT '支付状态（0-未支付，1-已支付，2-已退款）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '租借状态（0-已取消，1-租借中，2-已归还，3-逾期）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  `venue_id` bigint NULL DEFAULT NULL COMMENT '所属场馆ID（冗余，便于场馆级查询）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `rental_no`(`rental_no` ASC) USING BTREE,
  UNIQUE INDEX `idx_rental_no`(`rental_no` ASC) USING BTREE,
  INDEX `idx_member_id`(`member_id` ASC) USING BTREE,
  INDEX `idx_equipment_id`(`equipment_id` ASC) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  INDEX `idx_rental_date`(`rental_date` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_rental_expected_return`(`expected_return_date` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `biz_equipment_rental_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `sys_member` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `biz_equipment_rental_ibfk_2` FOREIGN KEY (`equipment_id`) REFERENCES `sys_equipment` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `biz_equipment_rental_ibfk_3` FOREIGN KEY (`venue_id`) REFERENCES `sys_venue` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '器材租借表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_equipment_rental
-- ----------------------------
INSERT INTO `biz_equipment_rental` VALUES (1, 'ER20250115001', 1, 1, 1, '2025-01-15', '2025-01-22', '2025-01-15 00:00:00', 30.00, 0.00, 0.00, NULL, 'ALIPAY', 1, 2, NULL, '2025-01-15 09:00:00', NULL, 0, 1);
INSERT INTO `biz_equipment_rental` VALUES (2, 'ER20250115002', 2, 3, 2, '2025-01-15', '2025-01-22', '2025-01-15 00:00:00', 10.00, 0.00, 0.00, NULL, 'WECHAT', 1, 2, NULL, '2025-01-15 10:00:00', NULL, 0, 1);
INSERT INTO `biz_equipment_rental` VALUES (3, 'ER20250116001', 3, 2, 1, '2025-01-16', '2025-01-23', '2025-01-16 00:00:00', 35.00, 0.00, 0.00, NULL, 'BALANCE', 1, 2, NULL, '2025-01-16 09:00:00', NULL, 0, 1);
INSERT INTO `biz_equipment_rental` VALUES (4, 'ER20250117001', 4, 6, 1, '2025-01-17', '2025-01-24', '2025-01-17 00:00:00', 28.00, 0.00, 0.00, NULL, 'ALIPAY', 1, 2, NULL, '2025-01-17 09:00:00', NULL, 0, 2);
INSERT INTO `biz_equipment_rental` VALUES (5, 'ER20250118001', 5, 8, 3, '2025-01-18', '2025-01-25', '2025-01-18 00:00:00', 13.50, 0.00, 0.00, NULL, 'WECHAT', 1, 2, NULL, '2025-01-18 09:00:00', NULL, 0, 2);
INSERT INTO `biz_equipment_rental` VALUES (6, 'ER20250119001', 6, 10, 1, '2025-01-19', '2025-01-26', '2025-01-19 00:00:00', 25.00, 0.00, 0.00, NULL, 'BALANCE', 1, 2, NULL, '2025-01-19 09:00:00', NULL, 0, 3);
INSERT INTO `biz_equipment_rental` VALUES (7, 'ER20250120001', 7, 12, 4, '2025-01-20', '2025-01-27', '2025-01-20 00:00:00', 16.00, 0.00, 0.00, NULL, 'ALIPAY', 1, 2, NULL, '2025-01-20 09:00:00', NULL, 0, 3);
INSERT INTO `biz_equipment_rental` VALUES (8, 'ER20250121001', 8, 14, 1, '2025-01-21', '2025-01-28', '2025-01-21 00:00:00', 20.00, 0.00, 0.00, NULL, 'WECHAT', 1, 2, NULL, '2025-01-21 09:00:00', '2026-02-14 13:12:25', 0, 4);
INSERT INTO `biz_equipment_rental` VALUES (9, 'ER20250122001', 9, 17, 1, '2025-01-22', '2025-01-29', '2025-01-22 00:00:00', 18.00, 0.00, 0.00, NULL, 'BALANCE', 1, 2, NULL, '2025-01-22 09:00:00', NULL, 0, 5);
INSERT INTO `biz_equipment_rental` VALUES (10, 'ER20250123001', 10, 18, 2, '2025-01-23', '2025-01-30', '2025-01-23 00:00:00', 6.00, 0.00, 0.00, NULL, 'ALIPAY', 1, 2, NULL, '2025-01-23 09:00:00', NULL, 0, 5);
INSERT INTO `biz_equipment_rental` VALUES (11, 'ER20250124001', 11, 1, 1, '2025-01-24', '2025-01-31', '2026-02-14 13:11:45', 30.00, 0.00, 0.00, NULL, 'WECHAT', 1, 2, NULL, '2025-01-24 09:00:00', '2026-02-14 13:11:45', 0, 1);
INSERT INTO `biz_equipment_rental` VALUES (12, 'ER20250125001', 12, 3, 2, '2025-01-25', '2025-02-01', '2026-02-14 13:11:44', 10.00, 0.00, 0.00, NULL, 'BALANCE', 1, 2, NULL, '2025-01-25 09:00:00', '2026-02-14 13:11:43', 0, 1);
INSERT INTO `biz_equipment_rental` VALUES (13, 'ER20250126001', 13, 6, 1, '2025-01-26', '2025-02-02', '2026-02-14 13:11:43', 28.00, 0.00, 0.00, NULL, 'ALIPAY', 1, 2, NULL, '2025-01-26 09:00:00', '2026-02-14 13:11:42', 0, 2);
INSERT INTO `biz_equipment_rental` VALUES (14, 'ER20250127001', 14, 10, 1, '2025-01-27', '2025-02-03', '2026-02-14 13:11:42', 25.00, 0.00, 0.00, NULL, 'WECHAT', 1, 2, NULL, '2025-01-27 09:00:00', '2026-02-14 13:11:42', 0, 3);
INSERT INTO `biz_equipment_rental` VALUES (15, 'ER20250128001', 15, 14, 1, '2025-01-28', '2025-02-04', '2026-02-14 13:11:41', 20.00, 0.00, 0.00, NULL, 'BALANCE', 1, 2, NULL, '2025-01-28 09:00:00', '2026-02-14 13:12:26', 0, 4);
INSERT INTO `biz_equipment_rental` VALUES (16, 'ER20260213001', 45, 27, 1, '2026-02-16', '2026-02-23', '2026-02-16 21:30:00', 60.00, 20.00, 5.00, 1, NULL, 0, 2, NULL, '2026-02-13 17:24:15', '2026-02-13 17:24:53', 1, NULL);
INSERT INTO `biz_equipment_rental` VALUES (18, 'ER20260213002', 45, 27, 1, '2026-02-16', '2026-02-23', '2026-02-16 21:30:00', 60.00, 20.00, 5.00, 1, 'BALANCE', 1, 2, NULL, '2026-02-13 17:28:27', '2026-02-14 13:08:23', 0, NULL);
INSERT INTO `biz_equipment_rental` VALUES (19, 'ER20260217001', 45, 3, 1, '2026-02-18', '2026-02-25', '2026-02-18 14:06:00', 5.00, 5.00, 5.00, 1, 'BALANCE', 1, 2, NULL, '2026-02-17 14:06:42', '2026-02-17 14:08:10', 0, NULL);
INSERT INTO `biz_equipment_rental` VALUES (20, 'ER20260217002', 45, 6, 1, '2026-02-17', '2026-02-24', '2026-02-17 14:14:00', 28.00, 28.00, 0.00, 1, 'BALANCE', 1, 2, NULL, '2026-02-17 14:14:16', '2026-02-17 14:15:12', 0, NULL);
INSERT INTO `biz_equipment_rental` VALUES (21, 'ER20260217003', 45, 7, 1, '2026-02-17', '2026-02-24', '2026-02-17 14:18:00', 32.00, 32.00, 0.00, 1, 'BALANCE', 1, 2, NULL, '2026-02-17 14:18:08', '2026-02-17 14:18:33', 0, NULL);
INSERT INTO `biz_equipment_rental` VALUES (22, 'ER20260217004', 45, 7, 1, '2026-02-17', '2026-02-24', '2026-02-17 14:21:00', 32.00, 32.00, 0.00, 1, 'BALANCE', 1, 2, NULL, '2026-02-17 14:20:53', '2026-02-17 14:22:17', 0, NULL);
INSERT INTO `biz_equipment_rental` VALUES (23, 'ER20260217005', 45, 27, 1, '2026-02-17', '2026-02-24', '2026-02-18 00:00:00', 480.00, 20.00, 5.00, 1, 'BALANCE', 1, 2, NULL, '2026-02-17 14:22:36', '2026-02-17 17:51:45', 0, NULL);
INSERT INTO `biz_equipment_rental` VALUES (24, 'ER20260217006', 45, 27, 1, '2026-02-18', '2026-02-25', '2026-02-19 00:00:00', 480.00, 20.00, 5.00, 1, 'BALANCE', 1, 2, NULL, '2026-02-17 17:52:10', '2026-02-18 12:49:43', 0, NULL);
INSERT INTO `biz_equipment_rental` VALUES (25, 'ER20260218001', 45, 27, 1, '2026-02-19', '2026-02-26', '2026-02-20 00:00:00', 480.00, 20.00, 5.00, 1, 'BALANCE', 1, 2, NULL, '2026-02-18 12:49:59', '2026-02-18 12:50:41', 0, NULL);

-- ----------------------------
-- Table structure for biz_feedback
-- ----------------------------
DROP TABLE IF EXISTS `biz_feedback`;
CREATE TABLE `biz_feedback`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??',
  `user_id` bigint NULL DEFAULT NULL COMMENT '??ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '????',
  `contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '????',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `biz_feedback_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '?????' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_feedback
-- ----------------------------

-- ----------------------------
-- Table structure for biz_finance
-- ----------------------------
DROP TABLE IF EXISTS `biz_finance`;
CREATE TABLE `biz_finance`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '财务ID',
  `finance_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '财务单号（唯一，自动生成，格式：FN+日期+序号，如FN20250118001）',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务类型（BOOKING-场地预约，COURSE-课程预约，EQUIPMENT-器材租借，TOURNAMENT-赛事报名，RECHARGE-会员充值，OTHER-其他）',
  `business_id` bigint NULL DEFAULT NULL COMMENT '业务ID（关联对应的业务表ID）',
  `income_expense_type` tinyint(1) NOT NULL COMMENT '收支类型（0-支出，1-收入）',
  `amount` decimal(10, 2) NOT NULL COMMENT '金额',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式',
  `venue_id` bigint NULL DEFAULT NULL COMMENT '所属场馆ID（便于场馆级数据过滤）',
  `operator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID（关联sys_user）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `record_source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'AUTO' COMMENT '记录来源（AUTO-自动生成，MANUAL-手动添加）',
  `is_reconciled` tinyint(1) NULL DEFAULT 0 COMMENT '是否已对账（0-未对账，1-已对账）',
  `reconcile_time` datetime NULL DEFAULT NULL COMMENT '对账时间',
  `reconcile_user_id` bigint NULL DEFAULT NULL COMMENT '对账人ID（关联sys_user）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `finance_no`(`finance_no` ASC) USING BTREE,
  UNIQUE INDEX `idx_finance_no`(`finance_no` ASC) USING BTREE,
  INDEX `idx_business_type`(`business_type` ASC) USING BTREE,
  INDEX `idx_business_id`(`business_id` ASC) USING BTREE,
  INDEX `idx_income_expense_type`(`income_expense_type` ASC) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_record_source`(`record_source` ASC) USING BTREE,
  INDEX `idx_is_reconciled`(`is_reconciled` ASC) USING BTREE,
  INDEX `idx_reconcile_time`(`reconcile_time` ASC) USING BTREE,
  INDEX `fk_finance_reconcile_user`(`reconcile_user_id` ASC) USING BTREE,
  CONSTRAINT `biz_finance_ibfk_1` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `biz_finance_ibfk_2` FOREIGN KEY (`venue_id`) REFERENCES `sys_venue` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_finance_reconcile_user` FOREIGN KEY (`reconcile_user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 122 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '财务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_finance
-- ----------------------------
INSERT INTO `biz_finance` VALUES (1, 'FN20250115001', 'BOOKING', 1, 1, 160.00, 'ALIPAY', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-15 09:00:00', 0);
INSERT INTO `biz_finance` VALUES (2, 'FN20250115002', 'BOOKING', 2, 1, 160.00, 'WECHAT', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-15 14:00:00', 0);
INSERT INTO `biz_finance` VALUES (3, 'FN20250115003', 'BOOKING', 3, 1, 160.00, 'BALANCE', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-15 18:00:00', 0);
INSERT INTO `biz_finance` VALUES (4, 'FN20250116001', 'BOOKING', 4, 1, 200.00, 'ALIPAY', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-16 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (5, 'FN20250116002', 'BOOKING', 5, 1, 200.00, 'WECHAT', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-16 15:00:00', 0);
INSERT INTO `biz_finance` VALUES (6, 'FN20250117001', 'BOOKING', 6, 1, 180.00, 'BALANCE', 2, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-17 09:00:00', 0);
INSERT INTO `biz_finance` VALUES (7, 'FN20250117002', 'BOOKING', 7, 1, 180.00, 'ALIPAY', 2, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-17 14:00:00', 0);
INSERT INTO `biz_finance` VALUES (8, 'FN20250118001', 'BOOKING', 8, 1, 180.00, 'WECHAT', 2, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-18 19:00:00', 0);
INSERT INTO `biz_finance` VALUES (9, 'FN20250119001', 'BOOKING', 9, 1, 170.00, 'BALANCE', 3, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-19 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (10, 'FN20250120001', 'BOOKING', 10, 1, 170.00, 'ALIPAY', 3, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-20 15:00:00', 0);
INSERT INTO `biz_finance` VALUES (11, 'FN20250128001', 'BOOKING', 11, 1, 160.00, 'ALIPAY', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-28 09:00:00', 0);
INSERT INTO `biz_finance` VALUES (12, 'FN20250128002', 'BOOKING', 12, 1, 160.00, 'WECHAT', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-28 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (13, 'FN20250128003', 'BOOKING', 13, 1, 160.00, 'BALANCE', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-28 14:00:00', 0);
INSERT INTO `biz_finance` VALUES (14, 'FN20250128004', 'BOOKING', 14, 1, 200.00, 'ALIPAY', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-28 15:00:00', 0);
INSERT INTO `biz_finance` VALUES (15, 'FN20250128005', 'BOOKING', 15, 1, 200.00, 'WECHAT', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-28 18:00:00', 0);
INSERT INTO `biz_finance` VALUES (16, 'FN20250128006', 'BOOKING', 16, 1, 180.00, 'BALANCE', 2, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-28 19:00:00', 0);
INSERT INTO `biz_finance` VALUES (17, 'FN20250128007', 'BOOKING', 17, 1, 180.00, 'ALIPAY', 2, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-28 20:00:00', 0);
INSERT INTO `biz_finance` VALUES (18, 'FN20250115004', 'COURSE', 1, 1, 200.00, 'ALIPAY', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-15 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (19, 'FN20250115005', 'COURSE', 2, 1, 200.00, 'WECHAT', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-15 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (20, 'FN20250115006', 'COURSE', 3, 1, 200.00, 'BALANCE', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-15 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (21, 'FN20250116003', 'COURSE', 4, 1, 150.00, 'ALIPAY', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-16 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (22, 'FN20250116004', 'COURSE', 5, 1, 150.00, 'WECHAT', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-16 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (23, 'FN20250115007', 'EQUIPMENT', 1, 1, 30.00, 'ALIPAY', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-15 09:00:00', 0);
INSERT INTO `biz_finance` VALUES (24, 'FN20250115008', 'EQUIPMENT', 2, 1, 10.00, 'WECHAT', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-15 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (25, 'FN20250116005', 'EQUIPMENT', 3, 1, 35.00, 'BALANCE', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-16 09:00:00', 0);
INSERT INTO `biz_finance` VALUES (26, 'FN20250117003', 'EQUIPMENT', 4, 1, 28.00, 'ALIPAY', 2, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-17 09:00:00', 0);
INSERT INTO `biz_finance` VALUES (27, 'FN20250118002', 'EQUIPMENT', 5, 1, 13.50, 'WECHAT', 2, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-18 09:00:00', 0);
INSERT INTO `biz_finance` VALUES (28, 'FN20250105001', 'TOURNAMENT', 1, 1, 100.00, 'ALIPAY', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-05 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (29, 'FN20250106001', 'TOURNAMENT', 2, 1, 100.00, 'WECHAT', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-06 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (30, 'FN20250107001', 'TOURNAMENT', 3, 1, 100.00, 'BALANCE', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-07 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (31, 'FN20250305001', 'TOURNAMENT', 6, 1, 80.00, 'BALANCE', 2, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-03-05 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (32, 'FN20250306001', 'TOURNAMENT', 7, 1, 80.00, 'ALIPAY', 2, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-03-06 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (33, 'FN20250115009', 'OTHER', 1, 1, 50.00, 'ALIPAY', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-15 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (34, 'FN20250116006', 'OTHER', 2, 1, 60.00, 'WECHAT', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-16 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (35, 'FN20250117004', 'OTHER', 3, 1, 55.00, 'BALANCE', 2, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-17 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (36, 'FN20250118003', 'OTHER', 4, 1, 30.00, 'ALIPAY', 2, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-18 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (37, 'FN20250119002', 'OTHER', 5, 1, 45.00, 'WECHAT', 3, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-19 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (38, 'FN20250101001', 'RECHARGE', NULL, 1, 500.00, 'ALIPAY', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-01 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (39, 'FN20250102001', 'RECHARGE', NULL, 1, 300.00, 'WECHAT', 1, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-02 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (40, 'FN20250103001', 'RECHARGE', NULL, 1, 200.00, 'BALANCE', 2, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-03 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (41, 'FN20250104001', 'RECHARGE', NULL, 1, 1000.00, 'ALIPAY', 2, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-04 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (42, 'FN20250105002', 'RECHARGE', NULL, 1, 800.00, 'WECHAT', 3, '系统', 1, NULL, 'AUTO', 0, NULL, NULL, '2025-01-05 10:00:00', 0);
INSERT INTO `biz_finance` VALUES (43, 'FN20260207001', 'RECHARGE', 6, 1, 300.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：钱十一 - ', 'AUTO', 0, NULL, NULL, '2026-02-07 16:13:32', 0);
INSERT INTO `biz_finance` VALUES (44, 'FN20260207002', 'RECHARGE', 7, 1, 350.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：钱十一 - ', 'AUTO', 0, NULL, NULL, '2026-02-07 16:13:37', 0);
INSERT INTO `biz_finance` VALUES (45, 'FN20260207003', 'RECHARGE', 8, 1, 200.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：涂家乐 - ', 'AUTO', 0, NULL, NULL, '2026-02-07 16:15:29', 0);
INSERT INTO `biz_finance` VALUES (46, 'FN20260207004', 'RECHARGE', 9, 1, 200.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：涂家乐 - ', 'AUTO', 0, NULL, NULL, '2026-02-07 16:15:42', 0);
INSERT INTO `biz_finance` VALUES (47, 'FN20260207005', 'RECHARGE', 10, 1, 200.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：涂家乐 - ', 'AUTO', 0, NULL, NULL, '2026-02-07 16:15:44', 0);
INSERT INTO `biz_finance` VALUES (48, 'FN20260207006', 'RECHARGE', 11, 1, 200.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：涂家乐 - ', 'AUTO', 0, NULL, NULL, '2026-02-07 16:15:46', 0);
INSERT INTO `biz_finance` VALUES (49, 'FN20260207007', 'RECHARGE', 12, 1, 200.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：涂家乐 - ', 'AUTO', 0, NULL, NULL, '2026-02-07 16:15:48', 0);
INSERT INTO `biz_finance` VALUES (50, 'FN20260207008', 'RECHARGE', 13, 1, 200.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：涂家乐 - ', 'AUTO', 0, NULL, NULL, '2026-02-07 16:17:41', 0);
INSERT INTO `biz_finance` VALUES (51, 'FN20260207009', 'RECHARGE', 14, 1, 200.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：萧三七 - ', 'AUTO', 0, NULL, NULL, '2026-02-07 16:21:35', 0);
INSERT INTO `biz_finance` VALUES (52, 'FN20260207010', 'RECHARGE', 15, 1, 200.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：涂家乐 - ', 'AUTO', 0, NULL, NULL, '2026-02-07 16:32:24', 0);
INSERT INTO `biz_finance` VALUES (53, 'FN20260207011', 'RECHARGE', 16, 1, 300.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：涂家乐 - ', 'AUTO', 0, NULL, NULL, '2026-02-07 16:32:28', 0);
INSERT INTO `biz_finance` VALUES (54, 'FN20260207012', 'RECHARGE', 17, 1, 200.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：测试用户001 - ', 'AUTO', 0, NULL, NULL, '2026-02-07 16:32:58', 0);
INSERT INTO `biz_finance` VALUES (55, 'FN20260207013', 'RECHARGE', 18, 1, 200.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：测试用户001 - ', 'AUTO', 0, NULL, NULL, '2026-02-07 16:33:02', 0);
INSERT INTO `biz_finance` VALUES (56, 'FN20260207014', 'RECHARGE', 19, 1, 200.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：测试用户001 - ', 'AUTO', 0, NULL, NULL, '2026-02-07 16:33:06', 0);
INSERT INTO `biz_finance` VALUES (57, 'FN20260207015', 'RECHARGE', 20, 1, 200.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：测试用户001 - ', 'AUTO', 0, NULL, NULL, '2026-02-07 16:33:09', 0);
INSERT INTO `biz_finance` VALUES (58, 'FN20260207016', 'RECHARGE', 21, 1, 200.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：测试用户001 - ', 'AUTO', 0, NULL, NULL, '2026-02-07 16:33:12', 0);
INSERT INTO `biz_finance` VALUES (59, 'FN20260209001', 'RECHARGE', 22, 1, 400.00, 'CASH', NULL, 'admin', 1, '会员充值：涂家乐', 'AUTO', 0, NULL, NULL, '2026-02-09 13:28:47', 0);
INSERT INTO `biz_finance` VALUES (60, 'FN20260209002', 'RECHARGE', 23, 1, 300.00, 'CASH', NULL, 'admin', 1, '会员充值：涂家乐 - ', 'AUTO', 0, NULL, NULL, '2026-02-09 13:29:06', 0);
INSERT INTO `biz_finance` VALUES (61, 'FN20260209003', 'RECHARGE', 24, 1, 350.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：孙七', 'AUTO', 0, NULL, NULL, '2026-02-09 13:33:12', 0);
INSERT INTO `biz_finance` VALUES (62, 'FN20260209004', 'RECHARGE', 25, 1, 1000.00, 'CASH', NULL, 'admin', 1, '会员充值：孙七', 'AUTO', 0, NULL, NULL, '2026-02-09 13:33:48', 0);
INSERT INTO `biz_finance` VALUES (63, 'FN20260209005', 'BOOKING', 30, 0, 200.00, 'WECHAT', 1, 'admin', 1, '场地预约退款：BK20250127001', 'AUTO', 0, NULL, NULL, '2026-02-09 22:50:01', 0);
INSERT INTO `biz_finance` VALUES (64, 'FN20260209006', 'BOOKING', 33, 1, 15.00, 'WECHAT', 6, 'admin', 1, '场地预约支付：BK20260209001', 'AUTO', 0, NULL, NULL, '2026-02-09 23:08:55', 0);
INSERT INTO `biz_finance` VALUES (65, 'FN20260209007', 'BOOKING', 33, 1, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约支付：BK20260209001', 'AUTO', 0, NULL, NULL, '2026-02-09 23:10:01', 0);
INSERT INTO `biz_finance` VALUES (66, 'FN20260209008', 'BOOKING', 33, 0, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约退款：BK20260209001', 'AUTO', 0, NULL, NULL, '2026-02-09 23:13:48', 0);
INSERT INTO `biz_finance` VALUES (67, 'FN20260209009', 'BOOKING', 33, 1, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约支付：BK20260209001', 'AUTO', 0, NULL, NULL, '2026-02-09 23:14:19', 0);
INSERT INTO `biz_finance` VALUES (68, 'FN20260209010', 'RECHARGE', 26, 1, 15.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：涂家乐', 'AUTO', 0, NULL, NULL, '2026-02-09 23:14:55', 0);
INSERT INTO `biz_finance` VALUES (69, 'FN20260209011', 'BOOKING', 33, 0, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约退款：BK20260209001', 'AUTO', 0, NULL, NULL, '2026-02-09 23:17:05', 0);
INSERT INTO `biz_finance` VALUES (70, 'FN20260209012', 'BOOKING', 33, 1, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约支付：BK20260209001', 'AUTO', 0, NULL, NULL, '2026-02-09 23:17:32', 0);
INSERT INTO `biz_finance` VALUES (71, 'FN20260209013', 'BOOKING', 33, 0, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约退款：BK20260209001', 'AUTO', 0, NULL, NULL, '2026-02-09 23:17:51', 0);
INSERT INTO `biz_finance` VALUES (72, 'FN20260209014', 'BOOKING', 33, 1, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约支付：BK20260209001', 'AUTO', 0, NULL, NULL, '2026-02-09 23:18:28', 0);
INSERT INTO `biz_finance` VALUES (73, 'FN20260209015', 'BOOKING', 33, 1, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约支付：BK20260209001', 'AUTO', 0, NULL, NULL, '2026-02-09 23:25:33', 0);
INSERT INTO `biz_finance` VALUES (74, 'FN20260209016', 'BOOKING', 33, 0, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约退款：BK20260209001', 'AUTO', 0, NULL, NULL, '2026-02-09 23:25:46', 0);
INSERT INTO `biz_finance` VALUES (75, 'FN20260209017', 'BOOKING', 33, 1, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约支付：BK20260209001', 'AUTO', 0, NULL, NULL, '2026-02-09 23:26:05', 0);
INSERT INTO `biz_finance` VALUES (76, 'FN20260209018', 'BOOKING', 36, 1, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约支付：BK20260209002', 'AUTO', 0, NULL, NULL, '2026-02-09 23:57:11', 0);
INSERT INTO `biz_finance` VALUES (77, 'FN20260211001', 'BOOKING', 13, 1, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约支付：BK20250128003', 'AUTO', 0, NULL, NULL, '2026-02-11 22:37:36', 0);
INSERT INTO `biz_finance` VALUES (78, 'FN20260211002', 'BOOKING', 1, 1, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约支付：BK20250115001', 'AUTO', 0, NULL, NULL, '2026-02-11 22:38:35', 0);
INSERT INTO `biz_finance` VALUES (79, 'FN20260214001', 'BOOKING', 36, 1, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约支付：BK20260209002', 'AUTO', 0, NULL, NULL, '2026-02-14 12:44:22', 0);
INSERT INTO `biz_finance` VALUES (80, 'FN20260214002', 'BOOKING', 36, 0, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约退款：BK20260209002', 'AUTO', 0, NULL, NULL, '2026-02-14 12:44:32', 0);
INSERT INTO `biz_finance` VALUES (81, 'FN20260214003', 'BOOKING', 36, 1, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约支付：BK20260209002', 'AUTO', 0, NULL, NULL, '2026-02-14 12:46:16', 0);
INSERT INTO `biz_finance` VALUES (82, 'FN20260214004', 'EQUIPMENT', 18, 1, 60.00, 'BALANCE', 6, 'admin', 1, '器材租借支付：ER20260213002', 'AUTO', 0, NULL, NULL, '2026-02-14 12:54:17', 0);
INSERT INTO `biz_finance` VALUES (83, 'FN20260214005', 'BOOKING', 37, 1, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约支付：BK20260214001', 'AUTO', 0, NULL, NULL, '2026-02-14 13:02:01', 0);
INSERT INTO `biz_finance` VALUES (84, 'FN20260214006', 'BOOKING', 37, 0, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约退款：BK20260214001', 'AUTO', 0, NULL, NULL, '2026-02-14 13:04:19', 0);
INSERT INTO `biz_finance` VALUES (85, 'FN20260214007', 'BOOKING', 38, 1, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约支付：BK20260214002', 'AUTO', 0, NULL, NULL, '2026-02-14 13:06:24', 0);
INSERT INTO `biz_finance` VALUES (86, 'FN20260214008', 'BOOKING', 38, 0, 15.00, 'BALANCE', 6, 'admin', 1, '场地预约退款：BK20260214002', 'AUTO', 0, NULL, NULL, '2026-02-14 13:07:55', 0);
INSERT INTO `biz_finance` VALUES (87, 'FN20260214009', 'STRINGING', 10, 0, 35.00, 'WECHAT', 5, 'admin', 1, '穿线服务退款：SR20250124001', 'AUTO', 0, NULL, NULL, '2026-02-14 13:52:18', 0);
INSERT INTO `biz_finance` VALUES (88, 'FN20260214010', 'STRINGING', 5, 0, 45.00, 'WECHAT', 3, 'admin', 1, '穿线服务退款：SR20250119001', 'AUTO', 0, NULL, NULL, '2026-02-14 13:52:32', 0);
INSERT INTO `biz_finance` VALUES (89, 'FN20260214011', 'STRINGING', 24, 1, 60.00, 'BALANCE', 6, 'admin', 1, '穿线服务支付：SR20260214002', 'AUTO', 0, NULL, NULL, '2026-02-14 14:13:38', 0);
INSERT INTO `biz_finance` VALUES (90, 'FN20260214012', 'STRINGING', 24, 0, 60.00, 'BALANCE', 6, 'admin', 1, '穿线服务退款：SR20260214002', 'AUTO', 0, NULL, NULL, '2026-02-14 14:14:04', 0);
INSERT INTO `biz_finance` VALUES (91, 'FN20260216001', 'COURSE', 25, 1, 500.00, 'BALANCE', 6, 'admin', 1, '课程预约支付：CB20260216002', 'AUTO', 0, NULL, NULL, '2026-02-16 21:31:28', 0);
INSERT INTO `biz_finance` VALUES (93, 'FN20260216002', 'COURSE', 25, 0, 500.00, 'BALANCE', 6, 'admin', 1, '课程预约退款：CB20260216002', 'AUTO', 0, NULL, NULL, '2026-02-16 21:45:15', 0);
INSERT INTO `biz_finance` VALUES (94, 'FN20260216003', 'COURSE', 25, 1, 500.00, 'BALANCE', 6, 'admin', 1, '课程预约支付：CB20260216002', 'AUTO', 0, NULL, NULL, '2026-02-16 21:45:33', 0);
INSERT INTO `biz_finance` VALUES (95, 'FN20260216004', 'RECHARGE', 27, 1, 1000.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：张三', 'AUTO', 0, NULL, NULL, '2026-02-16 21:47:12', 0);
INSERT INTO `biz_finance` VALUES (96, 'FN20260216005', 'RECHARGE', 28, 1, 1000.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：张三', 'AUTO', 0, NULL, NULL, '2026-02-16 21:47:40', 0);
INSERT INTO `biz_finance` VALUES (97, 'FN20260216006', 'RECHARGE', 29, 1, 200.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：张三 - ', 'AUTO', 0, NULL, NULL, '2026-02-16 21:48:04', 0);
INSERT INTO `biz_finance` VALUES (98, 'FN20260216007', 'COURSE', 26, 1, 500.00, 'BALANCE', 6, 'admin', 1, '课程预约支付：CB20260216003', 'AUTO', 0, NULL, NULL, '2026-02-16 21:48:40', 0);
INSERT INTO `biz_finance` VALUES (99, 'FN20260216008', 'RECHARGE', 30, 1, 1000.00, 'CASH', NULL, 'admin', 1, '会员充值：王五 - ', 'AUTO', 0, NULL, NULL, '2026-02-16 21:49:37', 0);
INSERT INTO `biz_finance` VALUES (100, 'FN20260216009', 'COURSE', 27, 1, 500.00, 'BALANCE', 6, 'admin', 1, '课程预约支付：CB20260216004', 'AUTO', 0, NULL, NULL, '2026-02-16 21:49:53', 0);
INSERT INTO `biz_finance` VALUES (101, 'FN20260216010', 'COURSE', 25, 0, 500.00, 'BALANCE', 6, 'admin', 1, '课程预约退款：CB20260216002', 'AUTO', 0, NULL, NULL, '2026-02-16 21:50:28', 0);
INSERT INTO `biz_finance` VALUES (102, 'FN20260216011', 'COURSE', 28, 1, 500.00, 'BALANCE', 6, 'admin', 1, '课程预约支付：CB20260216005', 'AUTO', 0, NULL, NULL, '2026-02-16 22:03:22', 0);
INSERT INTO `biz_finance` VALUES (103, 'FN20260217001', 'RECHARGE', 31, 1, 1000.00, 'CASH', NULL, 'admin', 1, '会员充值：涂家乐 - ', 'AUTO', 0, NULL, NULL, '2026-02-17 14:04:11', 0);
INSERT INTO `biz_finance` VALUES (104, 'FN20260217002', 'EQUIPMENT', 19, 1, 5.00, 'BALANCE', 1, 'admin', 1, '器材租借支付：ER20260217001', 'AUTO', 0, NULL, NULL, '2026-02-17 14:06:45', 0);
INSERT INTO `biz_finance` VALUES (105, 'FN20260217003', 'EQUIPMENT', 20, 1, 28.00, 'BALANCE', 2, 'admin', 1, '器材租借支付：ER20260217002', 'AUTO', 0, NULL, NULL, '2026-02-17 14:14:19', 0);
INSERT INTO `biz_finance` VALUES (106, 'FN20260217004', 'EQUIPMENT', 21, 1, 32.00, 'BALANCE', 2, 'admin', 1, '器材租借支付：ER20260217003', 'AUTO', 0, NULL, NULL, '2026-02-17 14:18:09', 0);
INSERT INTO `biz_finance` VALUES (107, 'FN20260217005', 'EQUIPMENT', 22, 1, 32.00, 'BALANCE', 2, 'admin', 1, '器材租借支付：ER20260217004', 'AUTO', 0, NULL, NULL, '2026-02-17 14:21:33', 0);
INSERT INTO `biz_finance` VALUES (108, 'FN20260217006', 'EQUIPMENT', 23, 1, 480.00, 'BALANCE', 6, 'admin', 1, '器材租借支付：ER20260217005', 'AUTO', 0, NULL, NULL, '2026-02-17 14:22:42', 0);
INSERT INTO `biz_finance` VALUES (109, 'FN20260217007', 'EQUIPMENT', 24, 1, 480.00, 'BALANCE', 6, 'admin', 1, '器材租借支付：ER20260217006', 'AUTO', 0, NULL, NULL, '2026-02-17 18:09:16', 0);
INSERT INTO `biz_finance` VALUES (110, 'FN20260217008', 'RECHARGE', 32, 1, 1000.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：涂家乐 - ', 'AUTO', 0, NULL, NULL, '2026-02-17 18:10:28', 0);
INSERT INTO `biz_finance` VALUES (111, 'FN20260218001', 'EQUIPMENT', 25, 1, 480.00, 'BALANCE', 6, 'admin', 1, '器材租借支付：ER20260218001', 'AUTO', 0, NULL, NULL, '2026-02-18 12:50:25', 0);
INSERT INTO `biz_finance` VALUES (112, 'FN20260218002', 'TOURNAMENT', 36, 1, 0.00, 'BALANCE', 6, 'admin', 1, '赛事报名支付：TR20260218002', 'AUTO', 0, NULL, NULL, '2026-02-18 14:00:18', 0);
INSERT INTO `biz_finance` VALUES (113, 'FN20260218003', 'TOURNAMENT', 35, 1, 0.00, 'BALANCE', 6, 'admin', 1, '赛事报名支付：TR20260218001', 'AUTO', 0, NULL, NULL, '2026-02-18 14:00:21', 0);
INSERT INTO `biz_finance` VALUES (114, 'FN20260218004', 'TOURNAMENT', 29, 1, 100.00, 'BALANCE', 1, 'admin', 1, '赛事报名支付：TR20250602003', 'AUTO', 0, NULL, NULL, '2026-02-18 14:09:15', 0);
INSERT INTO `biz_finance` VALUES (115, 'FN20260218005', 'TOURNAMENT', 31, 1, 100.00, 'CASH', 1, 'admin', 1, '赛事报名支付：TR20250602005', 'AUTO', 0, NULL, NULL, '2026-02-18 14:10:12', 0);
INSERT INTO `biz_finance` VALUES (116, 'FN20260218006', 'RECHARGE', 33, 1, 5000.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：涂家乐', 'AUTO', 0, NULL, NULL, '2026-02-18 14:53:18', 0);
INSERT INTO `biz_finance` VALUES (117, 'FN20260218007', 'RECHARGE', 34, 1, 300.00, 'WECHAT', NULL, 'admin', 1, '会员充值：涂家乐 - ', 'AUTO', 0, NULL, NULL, '2026-02-18 14:54:02', 0);
INSERT INTO `biz_finance` VALUES (118, 'FN20260221001', 'RECHARGE', 35, 1, 2000.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：涂家乐', 'AUTO', NULL, NULL, NULL, '2026-02-21 12:04:58', 0);
INSERT INTO `biz_finance` VALUES (119, 'FN20260222001', 'RECHARGE', 36, 1, 1000.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：涂家乐 - ', 'AUTO', NULL, NULL, NULL, '2026-02-22 15:31:23', 0);
INSERT INTO `biz_finance` VALUES (120, 'FN20260222002', 'RECHARGE', 37, 1, 200.00, 'ALIPAY', NULL, 'admin', 1, '会员充值：黄依静 - ', 'AUTO', NULL, NULL, NULL, '2026-02-22 15:36:58', 0);
INSERT INTO `biz_finance` VALUES (121, 'FN20260222003', 'RECHARGE', 38, 1, 1000.00, 'BALANCE', NULL, '黄依静', 14, '会员充值：黄依静', 'AUTO', NULL, NULL, NULL, '2026-02-22 15:39:00', 0);

-- ----------------------------
-- Table structure for biz_finance_audit_log
-- ----------------------------
DROP TABLE IF EXISTS `biz_finance_audit_log`;
CREATE TABLE `biz_finance_audit_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '审计日志ID',
  `finance_id` bigint NULL DEFAULT NULL COMMENT '财务记录ID（关联biz_finance，删除时可能为空）',
  `finance_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '财务单号（冗余存储，便于查询已删除记录）',
  `operation_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型（CREATE-创建，UPDATE-修改，DELETE-删除，RECONCILE-对账）',
  `operator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人姓名',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID（关联sys_user）',
  `operation_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `before_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '操作前数据（JSON格式，UPDATE时记录）',
  `after_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '操作后数据（JSON格式，CREATE/UPDATE时记录）',
  `change_summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '变更摘要（简要描述变更内容）',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户代理信息',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_finance_id`(`finance_id` ASC) USING BTREE,
  INDEX `idx_finance_no`(`finance_no` ASC) USING BTREE,
  INDEX `idx_operation_type`(`operation_type` ASC) USING BTREE,
  INDEX `idx_operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_operation_time`(`operation_time` ASC) USING BTREE,
  INDEX `idx_audit_finance_time`(`finance_id` ASC, `operation_time` DESC) USING BTREE,
  CONSTRAINT `biz_finance_audit_log_ibfk_1` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '财务审计日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_finance_audit_log
-- ----------------------------

-- ----------------------------
-- Table structure for biz_finance_reconciliation
-- ----------------------------
DROP TABLE IF EXISTS `biz_finance_reconciliation`;
CREATE TABLE `biz_finance_reconciliation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '对账记录ID',
  `reconcile_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对账单号（格式：RC+日期+序号，如RC20250202001）',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务类型（BOOKING/COURSE/EQUIPMENT/TOURNAMENT/RECHARGE/STRINGING/OTHER）',
  `start_date` date NOT NULL COMMENT '对账开始日期',
  `end_date` date NOT NULL COMMENT '对账结束日期',
  `venue_id` bigint NULL DEFAULT NULL COMMENT '场馆ID（场馆级对账时使用）',
  `business_count` int NULL DEFAULT 0 COMMENT '业务记录数量',
  `finance_count` int NULL DEFAULT 0 COMMENT '财务记录数量',
  `business_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '业务总金额',
  `finance_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '财务总金额',
  `count_diff` int NULL DEFAULT 0 COMMENT '数量差异（业务-财务）',
  `amount_diff` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '金额差异（业务-财务）',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '对账状态（0-有差异，1-已平账，2-差异已处理）',
  `diff_detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '差异明细（JSON格式，记录具体差异项）',
  `operator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '对账人姓名',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '对账人ID（关联sys_user）',
  `reconcile_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '对账时间',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '差异处理时间',
  `handle_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '差异处理说明',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `reconcile_no`(`reconcile_no` ASC) USING BTREE,
  UNIQUE INDEX `idx_reconcile_no`(`reconcile_no` ASC) USING BTREE,
  INDEX `idx_business_type`(`business_type` ASC) USING BTREE,
  INDEX `idx_date_range`(`start_date` ASC, `end_date` ASC) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_reconcile_time`(`reconcile_time` ASC) USING BTREE,
  INDEX `idx_operator_id`(`operator_id` ASC) USING BTREE,
  CONSTRAINT `biz_finance_reconciliation_ibfk_1` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `biz_finance_reconciliation_ibfk_2` FOREIGN KEY (`venue_id`) REFERENCES `sys_venue` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '财务对账记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_finance_reconciliation
-- ----------------------------

-- ----------------------------
-- Table structure for biz_member_consume_record
-- ----------------------------
DROP TABLE IF EXISTS `biz_member_consume_record`;
CREATE TABLE `biz_member_consume_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消费记录ID',
  `member_id` bigint NOT NULL COMMENT '会员ID（外键关联sys_member）',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务类型（BOOKING/COURSE/EQUIPMENT/TOURNAMENT/OTHER）',
  `business_id` bigint NULL DEFAULT NULL COMMENT '业务ID（关联对应的业务表ID）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消费描述（如：预约A1场地 2小时）',
  `amount` decimal(10, 2) NOT NULL COMMENT '金额（正数-消费，负数-退款/冲正）',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式（沿用业务表定义，含BALANCE）',
  `payment_status` tinyint(1) NULL DEFAULT 0 COMMENT '支付状态（0-未支付，1-已支付，2-已退款）',
  `before_balance` decimal(10, 2) NULL DEFAULT NULL COMMENT '扣款前余额（仅余额相关时记录）',
  `venue_id` bigint NULL DEFAULT NULL COMMENT '所属场馆ID（业务所属场馆，便于场馆级查询）',
  `after_balance` decimal(10, 2) NULL DEFAULT NULL COMMENT '扣款后余额（仅余额相关时记录）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_member_id`(`member_id` ASC) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  INDEX `idx_business`(`business_type` ASC, `business_id` ASC) USING BTREE,
  INDEX `idx_payment_status`(`payment_status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_consume_member_time`(`member_id` ASC, `create_time` DESC) USING BTREE,
  CONSTRAINT `biz_member_consume_record_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `sys_member` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `biz_member_consume_record_ibfk_2` FOREIGN KEY (`venue_id`) REFERENCES `sys_venue` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 64 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员消费记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_member_consume_record
-- ----------------------------
INSERT INTO `biz_member_consume_record` VALUES (1, 1, 'BOOKING', 1, '场地预约 - 订单号: 1', 160.00, 'BALANCE', 1, 480.00, 1, 320.00, NULL, '2025-01-15 09:00:00');
INSERT INTO `biz_member_consume_record` VALUES (2, 1, 'BOOKING', 11, '场地预约 - 订单号: 11', 160.00, 'BALANCE', 1, 320.00, 1, 160.00, NULL, '2025-01-28 09:00:00');
INSERT INTO `biz_member_consume_record` VALUES (3, 2, 'COURSE', 1, '课程预约 - 订单号: 1', 200.00, 'BALANCE', 1, 380.00, 1, 180.00, NULL, '2025-01-15 10:00:00');
INSERT INTO `biz_member_consume_record` VALUES (4, 3, 'BOOKING', 3, '场地预约 - 订单号: 3', 160.00, 'BALANCE', 1, 280.00, 1, 120.00, NULL, '2025-01-15 18:00:00');
INSERT INTO `biz_member_consume_record` VALUES (5, 3, 'EQUIPMENT', 1, '器材租借 - 订单号: 1', 30.00, 'BALANCE', 1, 120.00, 1, 90.00, NULL, '2025-01-15 09:00:00');
INSERT INTO `biz_member_consume_record` VALUES (6, 4, 'BOOKING', 4, '场地预约 - 订单号: 4', 200.00, 'BALANCE', 1, 250.00, 1, 50.00, NULL, '2025-01-16 10:00:00');
INSERT INTO `biz_member_consume_record` VALUES (7, 5, 'COURSE', 2, '课程预约 - 订单号: 2', 200.00, 'BALANCE', 1, 200.00, 1, 0.00, NULL, '2025-01-15 10:00:00');
INSERT INTO `biz_member_consume_record` VALUES (8, 6, 'BOOKING', 6, '场地预约 - 订单号: 6', 180.00, 'BALANCE', 1, 680.00, 2, 500.00, NULL, '2025-01-17 09:00:00');
INSERT INTO `biz_member_consume_record` VALUES (9, 7, 'EQUIPMENT', 2, '器材租借 - 订单号: 2', 10.00, 'BALANCE', 1, 360.00, 1, 350.00, NULL, '2025-01-15 10:00:00');
INSERT INTO `biz_member_consume_record` VALUES (10, 8, 'BOOKING', 8, '场地预约 - 订单号: 8', 180.00, 'BALANCE', 1, 300.00, 2, 120.00, NULL, '2025-01-18 19:00:00');
INSERT INTO `biz_member_consume_record` VALUES (11, 9, 'COURSE', 3, '课程预约 - 订单号: 3', 150.00, 'BALANCE', 1, 200.00, 1, 50.00, NULL, '2025-01-16 10:00:00');
INSERT INTO `biz_member_consume_record` VALUES (12, 10, 'BOOKING', 10, '场地预约 - 订单号: 10', 170.00, 'BALANCE', 1, 190.00, 3, 20.00, NULL, '2025-01-20 15:00:00');
INSERT INTO `biz_member_consume_record` VALUES (13, 11, 'TOURNAMENT', 1, '赛事报名 - 订单号: 1', 100.00, 'BALANCE', 1, 500.00, 1, 400.00, NULL, '2025-01-05 10:00:00');
INSERT INTO `biz_member_consume_record` VALUES (14, 12, 'BOOKING', 21, '场地预约 - 订单号: 21', 160.00, 'BALANCE', 1, 400.00, 1, 240.00, NULL, '2025-01-26 10:00:00');
INSERT INTO `biz_member_consume_record` VALUES (15, 13, 'COURSE', 4, '课程预约 - 订单号: 4', 180.00, 'BALANCE', 1, 300.00, 1, 120.00, NULL, '2025-01-17 12:00:00');
INSERT INTO `biz_member_consume_record` VALUES (16, 14, 'EQUIPMENT', 3, '器材租借 - 订单号: 3', 35.00, 'BALANCE', 1, 250.00, 1, 215.00, NULL, '2025-01-16 09:00:00');
INSERT INTO `biz_member_consume_record` VALUES (17, 15, 'BOOKING', 23, '场地预约 - 订单号: 23', 200.00, 'BALANCE', 1, 350.00, 1, 150.00, NULL, '2025-01-27 09:00:00');
INSERT INTO `biz_member_consume_record` VALUES (18, 16, 'TOURNAMENT', 4, '赛事报名 - 订单号: 4', 100.00, 'BALANCE', 1, 400.00, 1, 300.00, NULL, '2025-01-06 10:00:00');
INSERT INTO `biz_member_consume_record` VALUES (19, 17, 'COURSE', 5, '课程预约 - 订单号: 5', 150.00, 'BALANCE', 1, 300.00, 1, 150.00, NULL, '2025-01-16 10:00:00');
INSERT INTO `biz_member_consume_record` VALUES (20, 18, 'BOOKING', 24, '场地预约 - 订单号: 24', 180.00, 'BALANCE', 1, 280.00, 2, 100.00, NULL, '2025-01-27 14:00:00');
INSERT INTO `biz_member_consume_record` VALUES (21, 19, 'EQUIPMENT', 4, '器材租借 - 订单号: 4', 28.00, 'BALANCE', 1, 350.00, 2, 322.00, NULL, '2025-01-17 09:00:00');
INSERT INTO `biz_member_consume_record` VALUES (22, 20, 'BOOKING', 25, '场地预约 - 订单号: 25', 160.00, 'BALANCE', 1, 300.00, 1, 140.00, NULL, '2025-01-25 14:00:00');
INSERT INTO `biz_member_consume_record` VALUES (23, 45, 'BOOKING', 33, '场地预约 - 订单号: 33', 15.00, 'BALANCE', 1, 2200.00, 6, 2185.00, '场地预约支付：BK20260209001', '2026-02-09 23:10:01');
INSERT INTO `biz_member_consume_record` VALUES (24, 45, 'BOOKING', 33, '场地预约 - 订单号: 33', -15.00, 'BALANCE', 2, 2200.00, NULL, 2215.00, '场地预约退款：BK20260209001', '2026-02-09 23:13:48');
INSERT INTO `biz_member_consume_record` VALUES (25, 45, 'BOOKING', 33, '场地预约 - 订单号: 33', 15.00, 'BALANCE', 1, 2215.00, 6, 2200.00, '场地预约支付：BK20260209001', '2026-02-09 23:14:19');
INSERT INTO `biz_member_consume_record` VALUES (26, 45, 'BOOKING', 33, '场地预约 - 订单号: 33', -15.00, 'BALANCE', 2, 2230.00, NULL, 2245.00, '场地预约退款：BK20260209001', '2026-02-09 23:17:05');
INSERT INTO `biz_member_consume_record` VALUES (27, 45, 'BOOKING', 33, '场地预约 - 订单号: 33', 15.00, 'BALANCE', 1, 2245.00, 6, 2230.00, '场地预约支付：BK20260209001', '2026-02-09 23:17:32');
INSERT INTO `biz_member_consume_record` VALUES (28, 45, 'BOOKING', 33, '场地预约 - 订单号: 33', -15.00, 'BALANCE', 2, 2245.00, NULL, 2260.00, '场地预约退款：BK20260209001', '2026-02-09 23:17:51');
INSERT INTO `biz_member_consume_record` VALUES (29, 45, 'BOOKING', 33, '场地预约 - 订单号: 33', 15.00, 'BALANCE', 1, 2260.00, 6, 2245.00, '场地预约支付：BK20260209001', '2026-02-09 23:18:28');
INSERT INTO `biz_member_consume_record` VALUES (30, 45, 'BOOKING', 33, '场地预约 - 订单号: 33', 15.00, 'BALANCE', 1, 2260.00, 6, 2245.00, '场地预约支付：BK20260209001', '2026-02-09 23:25:33');
INSERT INTO `biz_member_consume_record` VALUES (31, 45, 'BOOKING', 33, '场地预约 - 订单号: 33', -15.00, 'BALANCE', 2, 2245.00, NULL, 2260.00, '场地预约退款：BK20260209001', '2026-02-09 23:25:46');
INSERT INTO `biz_member_consume_record` VALUES (32, 45, 'BOOKING', 33, '场地预约 - 订单号: 33', 15.00, 'BALANCE', 1, 2260.00, 6, 2245.00, '场地预约支付：BK20260209001', '2026-02-09 23:26:05');
INSERT INTO `biz_member_consume_record` VALUES (33, 45, 'BOOKING', 36, '场地预约 - 订单号: 36', 15.00, 'BALANCE', 1, 2245.00, 6, 2230.00, '场地预约支付：BK20260209002', '2026-02-09 23:57:11');
INSERT INTO `biz_member_consume_record` VALUES (34, 3, 'BOOKING', 13, '场地预约 - 订单号: 13', 15.00, 'BALANCE', 1, 120.00, 6, 105.00, '场地预约支付：BK20250128003', '2026-02-11 22:37:36');
INSERT INTO `biz_member_consume_record` VALUES (35, 1, 'BOOKING', 1, '场地预约 - 订单号: 1', 15.00, 'BALANCE', 1, 320.00, 6, 305.00, '场地预约支付：BK20250115001', '2026-02-11 22:38:35');
INSERT INTO `biz_member_consume_record` VALUES (36, 45, 'BOOKING', 36, '场地预约 - 订单号: 36', 15.00, 'BALANCE', 1, 2230.00, 6, 2215.00, '场地预约支付：BK20260209002', '2026-02-14 12:44:22');
INSERT INTO `biz_member_consume_record` VALUES (37, 45, 'BOOKING', 36, '场地预约 - 订单号: 36', -15.00, 'BALANCE', 2, 2215.00, NULL, 2230.00, '场地预约退款：BK20260209002', '2026-02-14 12:44:32');
INSERT INTO `biz_member_consume_record` VALUES (38, 45, 'BOOKING', 36, '场地预约 - 订单号: 36', 15.00, 'BALANCE', 1, 2230.00, 6, 2215.00, '场地预约支付：BK20260209002', '2026-02-14 12:46:16');
INSERT INTO `biz_member_consume_record` VALUES (39, 45, 'EQUIPMENT', 18, '器材租借 - 订单号: 18', 60.00, 'BALANCE', 1, 2215.00, 5, 2155.00, '器材租借支付：ER20260213002', '2026-02-14 12:54:17');
INSERT INTO `biz_member_consume_record` VALUES (40, 45, 'BOOKING', 37, '场地预约 - 订单号: 37', 15.00, 'BALANCE', 1, 2155.00, 6, 2140.00, '场地预约支付：BK20260214001', '2026-02-14 13:02:01');
INSERT INTO `biz_member_consume_record` VALUES (41, 45, 'BOOKING', 37, '场地预约 - 订单号: 37', -15.00, 'BALANCE', 2, 2140.00, NULL, 2155.00, '场地预约退款：BK20260214001', '2026-02-14 13:04:19');
INSERT INTO `biz_member_consume_record` VALUES (42, 45, 'BOOKING', 38, '场地预约 - 订单号: 38', 15.00, 'BALANCE', 1, 2155.00, 6, 2140.00, '场地预约支付：BK20260214002', '2026-02-14 13:06:24');
INSERT INTO `biz_member_consume_record` VALUES (43, 45, 'BOOKING', 38, '场地预约 - 订单号: 38', -15.00, 'BALANCE', 2, 2140.00, NULL, 2155.00, '场地预约退款：BK20260214002', '2026-02-14 13:07:55');
INSERT INTO `biz_member_consume_record` VALUES (44, 45, 'STRINGING', 24, '其他消费 - 订单号: 24', 60.00, 'BALANCE', 1, 2155.00, 6, 2095.00, '穿线服务支付：SR20260214002', '2026-02-14 14:13:38');
INSERT INTO `biz_member_consume_record` VALUES (45, 45, 'STRINGING', 24, '其他消费 - 订单号: 24', -60.00, 'BALANCE', 2, 2095.00, NULL, 2155.00, '穿线服务退款：SR20260214002', '2026-02-14 14:14:04');
INSERT INTO `biz_member_consume_record` VALUES (46, 45, 'COURSE', 25, '课程预约 - 订单号: 25', 500.00, 'BALANCE', 1, 2155.00, NULL, 1655.00, '课程预约支付：CB20260216002', '2026-02-16 21:31:27');
INSERT INTO `biz_member_consume_record` VALUES (48, 45, 'COURSE', 25, '课程预约 - 订单号: 25', -500.00, 'BALANCE', 2, 1655.00, NULL, 2155.00, '课程预约退款：CB20260216002', '2026-02-16 21:45:15');
INSERT INTO `biz_member_consume_record` VALUES (49, 45, 'COURSE', 25, '课程预约 - 订单号: 25', 500.00, 'BALANCE', 1, 2155.00, NULL, 1655.00, '课程预约支付：CB20260216002', '2026-02-16 21:45:33');
INSERT INTO `biz_member_consume_record` VALUES (50, 1, 'COURSE', 26, '课程预约 - 订单号: 26', 500.00, 'BALANCE', 1, 2505.00, NULL, 2005.00, '课程预约支付：CB20260216003', '2026-02-16 21:48:40');
INSERT INTO `biz_member_consume_record` VALUES (51, 3, 'COURSE', 27, '课程预约 - 订单号: 27', 500.00, 'BALANCE', 1, 1105.00, NULL, 605.00, '课程预约支付：CB20260216004', '2026-02-16 21:49:53');
INSERT INTO `biz_member_consume_record` VALUES (52, 45, 'COURSE', 25, '课程预约 - 订单号: 25', -500.00, 'BALANCE', 2, 1655.00, NULL, 2155.00, '课程预约退款：CB20260216002', '2026-02-16 21:50:28');
INSERT INTO `biz_member_consume_record` VALUES (53, 45, 'COURSE', 28, '课程预约 - 订单号: 28', 500.00, 'BALANCE', 1, 2155.00, NULL, 1655.00, '课程预约支付：CB20260216005', '2026-02-16 22:03:22');
INSERT INTO `biz_member_consume_record` VALUES (54, 45, 'EQUIPMENT', 19, '器材租借 - RSL 66球线', 5.00, 'BALANCE', 1, 2655.00, 5, 2650.00, '器材租借支付：ER20260217001', '2026-02-17 14:06:45');
INSERT INTO `biz_member_consume_record` VALUES (55, 45, 'EQUIPMENT', 20, '器材租借 - YONEX NANORAY 羽毛球拍', 28.00, 'BALANCE', 1, 2650.00, 1, 2622.00, '器材租借支付：ER20260217002', '2026-02-17 14:14:19');
INSERT INTO `biz_member_consume_record` VALUES (56, 45, 'EQUIPMENT', 21, '器材租借 - VICTOR POWER 羽毛球', 32.00, 'BALANCE', 1, 2622.00, 2, 2590.00, '器材租借支付：ER20260217003', '2026-02-17 14:18:09');
INSERT INTO `biz_member_consume_record` VALUES (57, 45, 'EQUIPMENT', 22, '器材租借 - LI-NING 高级球线', 32.00, 'BALANCE', 1, 2590.00, 3, 2558.00, '器材租借支付：ER20260217004', '2026-02-17 14:21:33');
INSERT INTO `biz_member_consume_record` VALUES (58, 45, 'EQUIPMENT', 23, '器材租借 - 订单号: 23', 480.00, 'BALANCE', 1, 2558.00, NULL, 2078.00, '器材租借支付：ER20260217005', '2026-02-17 14:22:42');
INSERT INTO `biz_member_consume_record` VALUES (59, 45, 'EQUIPMENT', 24, '器材租借 - NRZSP 羽毛球拍（ER20260217006）', 480.00, 'BALANCE', 1, 2078.00, NULL, 1598.00, '器材租借支付：ER20260217006', '2026-02-17 18:09:16');
INSERT INTO `biz_member_consume_record` VALUES (60, 45, 'EQUIPMENT', 25, '器材租借 - NRZSP 羽毛球拍（ER20260218001）', 480.00, 'BALANCE', 1, 2598.00, NULL, 2118.00, '器材租借支付：ER20260218001', '2026-02-18 12:50:25');
INSERT INTO `biz_member_consume_record` VALUES (61, 1, 'TOURNAMENT', 36, '赛事报名 - 订单号: 36', 0.00, 'BALANCE', 1, 2005.00, NULL, 2005.00, '赛事报名支付：TR20260218002', '2026-02-18 14:00:18');
INSERT INTO `biz_member_consume_record` VALUES (62, 45, 'TOURNAMENT', 35, '赛事报名 - 订单号: 35', 0.00, 'BALANCE', 1, 2118.00, NULL, 2118.00, '赛事报名支付：TR20260218001', '2026-02-18 14:00:21');
INSERT INTO `biz_member_consume_record` VALUES (63, 15, 'TOURNAMENT', 29, '赛事报名 - 订单号: 29', 100.00, 'BALANCE', 1, 450.00, NULL, 350.00, '赛事报名支付：TR20250602003', '2026-02-18 14:09:15');

-- ----------------------------
-- Table structure for biz_recharge_record
-- ----------------------------
DROP TABLE IF EXISTS `biz_recharge_record`;
CREATE TABLE `biz_recharge_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '充值记录ID',
  `recharge_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '充值单号（格式：RC+日期+序号，如RC20250202001）',
  `member_id` bigint NOT NULL COMMENT '会员ID（外键关联sys_member）',
  `user_id` bigint NULL DEFAULT NULL COMMENT '操作用户ID（外键关联sys_user，可为空，用户自己充值时记录）',
  `recharge_amount` decimal(10, 2) NOT NULL COMMENT '充值金额',
  `recharge_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '充值方式（CASH-现金，ALIPAY-支付宝，WECHAT-微信，BANK-银行转账）',
  `recharge_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '充值时间',
  `operator_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型（USER-用户自己充值，ADMIN-管理员充值）',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID（管理员充值时记录管理员ID）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `is_upgraded` tinyint(1) NULL DEFAULT 0 COMMENT '是否触发升级（0-否，1-是）',
  `is_level_upgraded` tinyint(1) NULL DEFAULT 0 COMMENT '是否等级升级（0-否，1-是，即本笔充值使会员等级Lv1~Lv5提升）',
  `venue_id` bigint NULL DEFAULT NULL COMMENT '所属场馆ID（操作人或业务关联，便于场馆级查询）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `recharge_no`(`recharge_no` ASC) USING BTREE,
  INDEX `idx_recharge_no`(`recharge_no` ASC) USING BTREE,
  INDEX `idx_member_id`(`member_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  INDEX `idx_recharge_time`(`recharge_time` ASC) USING BTREE,
  INDEX `idx_is_upgraded`(`is_upgraded` ASC) USING BTREE,
  INDEX `idx_recharge_member_time`(`member_id` ASC, `recharge_time` DESC) USING BTREE,
  CONSTRAINT `biz_recharge_record_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `sys_member` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `biz_recharge_record_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `biz_recharge_record_ibfk_3` FOREIGN KEY (`venue_id`) REFERENCES `sys_venue` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '充值记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_recharge_record
-- ----------------------------
INSERT INTO `biz_recharge_record` VALUES (1, 'RC20250101001', 1, 6, 500.00, 'ALIPAY', '2025-01-01 10:00:00', 'USER', 6, NULL, 0, 0, 1, '2025-01-01 10:00:00');
INSERT INTO `biz_recharge_record` VALUES (2, 'RC20250102001', 2, 7, 300.00, 'WECHAT', '2025-01-02 10:00:00', 'USER', 7, NULL, 0, 0, 1, '2025-01-02 10:00:00');
INSERT INTO `biz_recharge_record` VALUES (3, 'RC20250103001', 3, 8, 200.00, 'BALANCE', '2025-01-03 10:00:00', 'USER', 8, NULL, 0, 0, 2, '2025-01-03 10:00:00');
INSERT INTO `biz_recharge_record` VALUES (4, 'RC20250104001', 4, NULL, 1000.00, 'ALIPAY', '2025-01-04 10:00:00', 'ADMIN', 1, NULL, 0, 0, 2, '2025-01-04 10:00:00');
INSERT INTO `biz_recharge_record` VALUES (5, 'RC20250105001', 5, NULL, 800.00, 'WECHAT', '2025-01-05 10:00:00', 'ADMIN', 1, NULL, 0, 0, 3, '2025-01-05 10:00:00');
INSERT INTO `biz_recharge_record` VALUES (6, 'RC20260207001', 9, NULL, 300.00, 'ALIPAY', '2026-02-07 16:13:32', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-07 16:13:32');
INSERT INTO `biz_recharge_record` VALUES (7, 'RC20260207002', 9, NULL, 350.00, 'ALIPAY', '2026-02-07 16:13:37', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-07 16:13:37');
INSERT INTO `biz_recharge_record` VALUES (8, 'RC20260207003', 45, NULL, 200.00, 'ALIPAY', '2026-02-07 16:15:29', 'ADMIN', 1, '', 1, 0, NULL, '2026-02-07 16:15:29');
INSERT INTO `biz_recharge_record` VALUES (9, 'RC20260207004', 45, NULL, 200.00, 'ALIPAY', '2026-02-07 16:15:42', 'ADMIN', 1, '', 1, 0, NULL, '2026-02-07 16:15:42');
INSERT INTO `biz_recharge_record` VALUES (10, 'RC20260207005', 45, NULL, 200.00, 'ALIPAY', '2026-02-07 16:15:44', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-07 16:15:44');
INSERT INTO `biz_recharge_record` VALUES (11, 'RC20260207006', 45, NULL, 200.00, 'ALIPAY', '2026-02-07 16:15:46', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-07 16:15:46');
INSERT INTO `biz_recharge_record` VALUES (12, 'RC20260207007', 45, NULL, 200.00, 'ALIPAY', '2026-02-07 16:15:48', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-07 16:15:48');
INSERT INTO `biz_recharge_record` VALUES (13, 'RC20260207008', 45, NULL, 200.00, 'ALIPAY', '2026-02-07 16:17:41', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-07 16:17:41');
INSERT INTO `biz_recharge_record` VALUES (14, 'RC20260207009', 35, NULL, 200.00, 'ALIPAY', '2026-02-07 16:21:35', 'ADMIN', 1, '', 1, 0, NULL, '2026-02-07 16:21:35');
INSERT INTO `biz_recharge_record` VALUES (15, 'RC20260207010', 45, NULL, 200.00, 'ALIPAY', '2026-02-07 16:32:24', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-07 16:32:24');
INSERT INTO `biz_recharge_record` VALUES (16, 'RC20260207011', 45, NULL, 300.00, 'ALIPAY', '2026-02-07 16:32:28', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-07 16:32:28');
INSERT INTO `biz_recharge_record` VALUES (17, 'RC20260207012', 43, NULL, 200.00, 'ALIPAY', '2026-02-07 16:32:58', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-07 16:32:58');
INSERT INTO `biz_recharge_record` VALUES (18, 'RC20260207013', 43, NULL, 200.00, 'ALIPAY', '2026-02-07 16:33:02', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-07 16:33:02');
INSERT INTO `biz_recharge_record` VALUES (19, 'RC20260207014', 43, NULL, 200.00, 'ALIPAY', '2026-02-07 16:33:06', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-07 16:33:06');
INSERT INTO `biz_recharge_record` VALUES (20, 'RC20260207015', 43, NULL, 200.00, 'ALIPAY', '2026-02-07 16:33:09', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-07 16:33:09');
INSERT INTO `biz_recharge_record` VALUES (21, 'RC20260207016', 43, NULL, 200.00, 'ALIPAY', '2026-02-07 16:33:12', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-07 16:33:12');
INSERT INTO `biz_recharge_record` VALUES (22, 'RC20260209001', 45, NULL, 400.00, 'CASH', '2026-02-09 13:28:47', 'ADMIN', 1, NULL, 0, 0, NULL, '2026-02-09 13:28:47');
INSERT INTO `biz_recharge_record` VALUES (23, 'RC20260209002', 45, NULL, 300.00, 'CASH', '2026-02-09 13:29:06', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-09 13:29:06');
INSERT INTO `biz_recharge_record` VALUES (24, 'RC20260209003', 5, NULL, 350.00, 'ALIPAY', '2026-02-09 13:33:12', 'ADMIN', 1, NULL, 0, 0, NULL, '2026-02-09 13:33:12');
INSERT INTO `biz_recharge_record` VALUES (25, 'RC20260209004', 5, NULL, 1000.00, 'CASH', '2026-02-09 13:33:48', 'ADMIN', 1, NULL, 0, 0, NULL, '2026-02-09 13:33:48');
INSERT INTO `biz_recharge_record` VALUES (26, 'RC20260209005', 45, NULL, 15.00, 'ALIPAY', '2026-02-09 23:14:55', 'ADMIN', 1, NULL, 0, 0, NULL, '2026-02-09 23:14:55');
INSERT INTO `biz_recharge_record` VALUES (27, 'RC20260216001', 1, NULL, 1000.00, 'ALIPAY', '2026-02-16 21:47:12', 'ADMIN', 1, NULL, 0, 0, NULL, '2026-02-16 21:47:12');
INSERT INTO `biz_recharge_record` VALUES (28, 'RC20260216002', 1, NULL, 1000.00, 'ALIPAY', '2026-02-16 21:47:40', 'ADMIN', 1, NULL, 0, 0, NULL, '2026-02-16 21:47:40');
INSERT INTO `biz_recharge_record` VALUES (29, 'RC20260216003', 1, NULL, 200.00, 'ALIPAY', '2026-02-16 21:48:04', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-16 21:48:04');
INSERT INTO `biz_recharge_record` VALUES (30, 'RC20260216004', 3, NULL, 1000.00, 'CASH', '2026-02-16 21:49:37', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-16 21:49:37');
INSERT INTO `biz_recharge_record` VALUES (31, 'RC20260217001', 45, NULL, 1000.00, 'CASH', '2026-02-17 14:04:11', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-17 14:04:11');
INSERT INTO `biz_recharge_record` VALUES (32, 'RC20260217002', 45, NULL, 1000.00, 'ALIPAY', '2026-02-17 18:10:28', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-17 18:10:28');
INSERT INTO `biz_recharge_record` VALUES (33, 'RC20260218001', 45, NULL, 5000.00, 'ALIPAY', '2026-02-18 14:53:18', 'ADMIN', 1, NULL, 0, 0, NULL, '2026-02-18 14:53:18');
INSERT INTO `biz_recharge_record` VALUES (34, 'RC20260218002', 45, NULL, 300.00, 'WECHAT', '2026-02-18 14:54:02', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-18 14:54:02');
INSERT INTO `biz_recharge_record` VALUES (35, 'RC20260221001', 45, NULL, 2000.00, 'ALIPAY', '2026-02-21 12:04:58', 'ADMIN', 1, NULL, 0, 0, NULL, '2026-02-21 12:04:58');
INSERT INTO `biz_recharge_record` VALUES (36, 'RC20260222001', 45, NULL, 1000.00, 'ALIPAY', '2026-02-22 15:31:23', 'ADMIN', 1, '', 0, 0, NULL, '2026-02-22 15:31:23');
INSERT INTO `biz_recharge_record` VALUES (37, 'RC20260222002', 54, NULL, 200.00, 'ALIPAY', '2026-02-22 15:36:58', 'ADMIN', 1, '', 1, 0, NULL, '2026-02-22 15:36:58');
INSERT INTO `biz_recharge_record` VALUES (38, 'RC20260222003', 54, 14, 1000.00, 'BALANCE', '2026-02-22 15:39:00', 'USER', NULL, NULL, 0, 0, NULL, '2026-02-22 15:39:00');

-- ----------------------------
-- Table structure for biz_stringing_service
-- ----------------------------
DROP TABLE IF EXISTS `biz_stringing_service`;
CREATE TABLE `biz_stringing_service`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '穿线服务ID',
  `service_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务单号（唯一，自动生成，格式：SR+日期+序号，如SR20250125001）',
  `member_id` bigint NULL DEFAULT NULL COMMENT '会员ID（外键关联sys_member，可为空，支持非会员申请）',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID（外键关联sys_user，记录申请用户）',
  `venue_id` bigint NOT NULL COMMENT '所属场馆ID（外键关联sys_venue，必填）',
  `racket_brand` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '球拍品牌',
  `racket_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '球拍型号',
  `racket_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '球拍描述（可选）',
  `string_id` bigint NULL DEFAULT NULL COMMENT '线材ID（外键关联sys_equipment，可为空，如果从系统选择）',
  `string_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '线材名称（如果用户自带线材，填写线材名称）',
  `is_own_string` tinyint(1) NULL DEFAULT 0 COMMENT '是否自带线材（0-否，1-是，默认0）',
  `pound` decimal(4, 1) NULL DEFAULT NULL COMMENT '磅数（支持小数，如：26.5）',
  `stringing_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '穿线法（TWO_SECTION-两节，FOUR_SECTION-四节，AUTO-视球拍而定）',
  `has_breakage` tinyint(1) NULL DEFAULT 0 COMMENT '是否存在断裂（0-否，1-是，默认0）',
  `has_collapse` tinyint(1) NULL DEFAULT 0 COMMENT '是否存在塌陷（0-否，1-是，默认0）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '服务状态（0-已取消，1-等待穿线，2-正在穿线，3-已完成，默认1）',
  `service_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '服务价格（包含线材价格和手工费）',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式（CASH-现金，ALIPAY-支付宝，WECHAT-微信，BALANCE-余额）',
  `payment_status` tinyint(1) NULL DEFAULT 0 COMMENT '支付状态（0-未支付，1-已支付，2-已退款，默认0）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始穿线时间（管理员点击\"开始穿线\"时记录）',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `service_no`(`service_no` ASC) USING BTREE,
  UNIQUE INDEX `idx_service_no`(`service_no` ASC) USING BTREE,
  INDEX `idx_member_id`(`member_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `string_id`(`string_id` ASC) USING BTREE,
  CONSTRAINT `biz_stringing_service_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `sys_member` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `biz_stringing_service_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `biz_stringing_service_ibfk_3` FOREIGN KEY (`venue_id`) REFERENCES `sys_venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `biz_stringing_service_ibfk_4` FOREIGN KEY (`string_id`) REFERENCES `sys_equipment` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '穿线服务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_stringing_service
-- ----------------------------
INSERT INTO `biz_stringing_service` VALUES (1, 'SR20250115001', 1, 6, 1, 'YONEX', 'ARC11', NULL, 4, 'YONEX BG65', 0, 26.0, 'TWO_SECTION', 0, 0, 3, 50.00, 'ALIPAY', 1, NULL, '2025-01-15 10:00:00', NULL, NULL, 0);
INSERT INTO `biz_stringing_service` VALUES (2, 'SR20250116001', 2, 7, 1, 'YONEX', 'ASTROX 99', NULL, 5, 'YONEX BG80', 0, 27.0, 'FOUR_SECTION', 0, 0, 3, 60.00, 'WECHAT', 1, NULL, '2025-01-16 10:00:00', NULL, NULL, 0);
INSERT INTO `biz_stringing_service` VALUES (3, 'SR20250117001', 3, 8, 2, 'VICTOR', 'JS-12', NULL, 9, 'VICTOR VBS-66N', 0, 25.5, 'AUTO', 0, 0, 3, 55.00, 'BALANCE', 1, NULL, '2025-01-17 10:00:00', NULL, NULL, 0);
INSERT INTO `biz_stringing_service` VALUES (4, 'SR20250118001', 4, 9, 2, 'VICTOR', 'THRUSTER F', NULL, NULL, '自带线材', 1, 26.0, 'TWO_SECTION', 0, 0, 3, 30.00, 'ALIPAY', 1, NULL, '2025-01-18 10:00:00', NULL, NULL, 0);
INSERT INTO `biz_stringing_service` VALUES (5, 'SR20250119001', 5, 10, 3, 'LI-NING', 'N90', NULL, 13, 'LI-NING 1号线', 0, 24.0, 'AUTO', 0, 0, 3, 45.00, 'WECHAT', 2, NULL, '2025-01-19 10:00:00', '2026-02-14 13:52:32', NULL, 0);
INSERT INTO `biz_stringing_service` VALUES (6, 'SR20250120001', 6, NULL, 1, 'YONEX', 'ARC11', NULL, 4, 'YONEX BG65', 0, 26.0, 'TWO_SECTION', 0, 0, 2, 50.00, 'ALIPAY', 1, NULL, '2025-01-20 10:00:00', NULL, NULL, 0);
INSERT INTO `biz_stringing_service` VALUES (7, 'SR20250121001', 7, NULL, 2, 'VICTOR', 'JS-12', NULL, 9, 'VICTOR VBS-66N', 0, 25.5, 'AUTO', 0, 0, 2, 55.00, 'WECHAT', 1, NULL, '2025-01-21 10:00:00', NULL, NULL, 0);
INSERT INTO `biz_stringing_service` VALUES (8, 'SR20250122001', 8, NULL, 3, 'LI-NING', 'N7', NULL, 13, 'LI-NING 1号线', 0, 24.0, 'AUTO', 0, 0, 1, 45.00, 'BALANCE', 1, NULL, '2025-01-22 10:00:00', NULL, NULL, 0);
INSERT INTO `biz_stringing_service` VALUES (9, 'SR20250123001', 9, NULL, 4, 'KAWASAKI', '9900', NULL, 16, 'KAWASAKI K-66', 0, 25.0, 'TWO_SECTION', 0, 0, 1, 40.00, 'ALIPAY', 1, NULL, '2025-01-23 10:00:00', NULL, NULL, 0);
INSERT INTO `biz_stringing_service` VALUES (10, 'SR20250124001', 10, NULL, 5, 'RSL', 'X5', NULL, 19, 'RSL 66球线', 0, 24.5, 'AUTO', 0, 0, 1, 35.00, 'WECHAT', 2, NULL, '2025-01-24 10:00:00', '2026-02-14 13:52:18', NULL, 0);
INSERT INTO `biz_stringing_service` VALUES (11, 'SR20260214001', NULL, 1, 6, '尤尼克斯', 'NRZSP', '', 5, '', 0, 30.0, 'FOUR_SECTION', 0, 0, 0, 80.00, NULL, 0, '', '2026-02-14 13:21:56', '2026-02-14 13:22:43', NULL, 1);
INSERT INTO `biz_stringing_service` VALUES (24, 'SR20260214002', 45, 1, 6, '尤尼克斯', 'NRZSP', '', 5, '', 0, 30.0, 'FOUR_SECTION', 0, 0, 0, 60.00, 'BALANCE', 2, '', '2026-02-14 14:13:13', '2026-02-14 14:14:04', '2026-02-14 14:13:52', 0);

-- ----------------------------
-- Table structure for biz_tournament
-- ----------------------------
DROP TABLE IF EXISTS `biz_tournament`;
CREATE TABLE `biz_tournament`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '赛事ID',
  `tournament_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '赛事名称',
  `tournament_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '赛事类型（SINGLE-单打，DOUBLE-双打，MIXED-混双，TEAM-团体）',
  `venue_id` bigint NOT NULL COMMENT '举办场馆ID（外键关联sys_venue）',
  `max_participants` int NULL DEFAULT 32 COMMENT '参赛人数上限',
  `current_participants` int NULL DEFAULT 0 COMMENT '当前报名人数',
  `registration_start` datetime NULL DEFAULT NULL COMMENT '报名开始时间',
  `registration_end` datetime NULL DEFAULT NULL COMMENT '报名结束时间',
  `tournament_start` datetime NULL DEFAULT NULL COMMENT '比赛开始时间',
  `tournament_end` datetime NULL DEFAULT NULL COMMENT '比赛结束时间',
  `entry_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '报名费',
  `prize_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '奖品信息',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '赛事描述',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态（0-已取消，1-报名中，2-报名结束，3-进行中，4-已结束）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tournament_type`(`tournament_type` ASC) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  INDEX `idx_registration_start`(`registration_start` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `biz_tournament_ibfk_1` FOREIGN KEY (`venue_id`) REFERENCES `sys_venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '赛事表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_tournament
-- ----------------------------
INSERT INTO `biz_tournament` VALUES (1, '2025年春季羽毛球公开赛', 'DOUBLE', 1, 32, 24, '2025-01-01 00:00:00', '2025-02-15 23:59:59', '2025-02-20 09:00:00', '2025-02-23 18:00:00', 100.00, '冠军：5000元+奖杯，亚军：3000元+奖牌，季军：2000元+奖牌', '春季大型羽毛球赛事，欢迎各水平选手参加', 3, '2024-12-01 10:00:00', '2026-02-17 13:01:47', 0);
INSERT INTO `biz_tournament` VALUES (2, '2025年夏季羽毛球挑战赛', 'SINGLE', 2, 64, 48, '2025-03-01 00:00:00', '2025-04-15 23:59:59', '2025-04-20 09:00:00', '2025-04-22 18:00:00', 80.00, '冠军：3000元+奖杯，亚军：2000元+奖牌，季军：1000元+奖牌', '夏季单打挑战赛', 3, '2025-01-01 10:00:00', '2026-02-17 13:01:47', 0);
INSERT INTO `biz_tournament` VALUES (3, '2025年混双友谊赛', 'MIXED', 3, 32, 28, '2025-02-01 00:00:00', '2025-03-10 23:59:59', '2025-03-15 09:00:00', '2025-03-17 18:00:00', 120.00, '冠军：4000元+奖杯，亚军：2500元+奖牌', '混双专项赛事', 3, '2024-12-15 10:00:00', '2026-02-17 13:01:47', 0);
INSERT INTO `biz_tournament` VALUES (4, '2025年青少年锦标赛', 'SINGLE', 4, 48, 36, '2025-04-01 00:00:00', '2025-05-15 23:59:59', '2025-05-20 09:00:00', '2025-05-22 18:00:00', 60.00, '冠军：2000元+奖杯，亚军：1500元+奖牌，季军：1000元+奖牌', '青少年专项赛事', 3, '2025-02-01 10:00:00', '2026-02-17 13:01:47', 0);
INSERT INTO `biz_tournament` VALUES (5, '2024年冬季羽毛球联赛', 'TEAM', 5, 16, 16, '2024-10-01 00:00:00', '2024-11-15 23:59:59', '2024-11-20 09:00:00', '2024-11-25 18:00:00', 500.00, '冠军：10000元+奖杯，亚军：6000元+奖牌', '团队联赛，已结束', 4, '2024-09-01 10:00:00', NULL, 0);
INSERT INTO `biz_tournament` VALUES (6, '2025年满员测试赛事', 'SINGLE', 1, 10, 10, '2025-05-01 00:00:00', '2025-06-30 23:59:59', '2025-07-01 09:00:00', '2025-07-03 18:00:00', 50.00, '测试赛事奖品', '用于测试名额检查功能的满员赛事', 3, '2025-01-20 10:00:00', '2026-02-17 13:01:47', 0);
INSERT INTO `biz_tournament` VALUES (7, '2025年待支付测试赛事', 'DOUBLE', 1, 32, 2, '2025-05-01 00:00:00', '2025-06-30 23:59:59', '2025-07-01 09:00:00', '2025-07-03 18:00:00', 100.00, '测试赛事奖品', '用于测试支付功能的赛事', 3, '2025-01-20 10:00:00', '2026-02-18 14:00:58', 0);
INSERT INTO `biz_tournament` VALUES (8, '2025年人数联动测试赛事', 'SINGLE', 2, 20, 3, '2025-05-01 00:00:00', '2025-06-30 23:59:59', '2025-07-01 09:00:00', '2025-07-03 18:00:00', 60.00, '测试赛事奖品', '用于测试当前参赛人数联动功能的赛事', 3, '2025-01-20 10:00:00', '2026-02-17 13:01:47', 0);
INSERT INTO `biz_tournament` VALUES (9, '2025年空赛事测试', 'SINGLE', 3, 15, 0, '2025-05-01 00:00:00', '2025-06-30 23:59:59', '2025-07-01 09:00:00', '2025-07-03 18:00:00', 50.00, '测试赛事奖品', '用于测试从0开始的人数联动功能（创建第一个报名后人数应变为1，取消后应变为0）', 3, '2025-01-20 10:00:00', '2026-02-17 13:01:47', 0);
INSERT INTO `biz_tournament` VALUES (10, '2026年新春羽毛球比赛', NULL, 6, 16, 0, '2026-02-17 13:23:00', '2026-02-17 13:23:50', '2026-02-17 13:24:00', '2026-02-17 13:24:50', 10.00, NULL, NULL, 3, '2026-02-17 13:24:23', '2026-02-17 13:25:21', 1);
INSERT INTO `biz_tournament` VALUES (11, '2026新春羽毛球比赛', 'DOUBLE', 6, 16, 0, '2026-02-17 13:27:00', '2026-02-17 13:28:00', '2026-02-17 13:29:00', '2026-02-17 13:30:00', 10.00, NULL, NULL, 3, '2026-02-17 13:27:54', '2026-02-17 13:33:11', 0);
INSERT INTO `biz_tournament` VALUES (12, '2026新春羽毛球比赛', 'SINGLE', 6, 2, 0, '2026-02-18 00:00:00', '2026-02-18 22:00:00', '2026-02-19 00:00:00', '2026-02-20 00:00:00', 0.00, NULL, NULL, 0, '2026-02-18 13:46:45', '2026-02-18 14:09:05', 0);

-- ----------------------------
-- Table structure for biz_tournament_registration
-- ----------------------------
DROP TABLE IF EXISTS `biz_tournament_registration`;
CREATE TABLE `biz_tournament_registration`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '赛事报名ID',
  `registration_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报名单号（唯一，自动生成，格式：TR+日期+序号，如TR20250118001）',
  `tournament_id` bigint NOT NULL COMMENT '赛事ID（外键关联biz_tournament）',
  `member_id` bigint NOT NULL COMMENT '会员ID（外键关联sys_member）',
  `partner_id` bigint NULL DEFAULT NULL COMMENT '搭档会员ID（双打/混双时使用，可为空）',
  `entry_fee` decimal(10, 2) NOT NULL COMMENT '报名费',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式',
  `payment_status` tinyint(1) NULL DEFAULT 0 COMMENT '支付状态（0-未支付，1-已支付，2-已退款）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '报名状态（0-已取消，1-待支付，2-已支付，3-已参赛，4-已退赛）',
  `match_result` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '比赛成绩（如：冠军、亚军、八强等）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `registration_no`(`registration_no` ASC) USING BTREE,
  UNIQUE INDEX `idx_registration_no`(`registration_no` ASC) USING BTREE,
  INDEX `idx_tournament_id`(`tournament_id` ASC) USING BTREE,
  INDEX `idx_member_id`(`member_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `partner_id`(`partner_id` ASC) USING BTREE,
  CONSTRAINT `biz_tournament_registration_ibfk_1` FOREIGN KEY (`tournament_id`) REFERENCES `biz_tournament` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `biz_tournament_registration_ibfk_2` FOREIGN KEY (`member_id`) REFERENCES `sys_member` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `biz_tournament_registration_ibfk_3` FOREIGN KEY (`partner_id`) REFERENCES `sys_member` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '赛事报名表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_tournament_registration
-- ----------------------------
INSERT INTO `biz_tournament_registration` VALUES (1, 'TR20250101001', 1, 1, 2, 100.00, 'ALIPAY', 1, 3, NULL, NULL, '2025-01-05 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (2, 'TR20250101002', 1, 3, 4, 100.00, 'WECHAT', 1, 3, NULL, NULL, '2025-01-06 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (3, 'TR20250101003', 1, 5, 6, 100.00, 'BALANCE', 1, 3, NULL, NULL, '2025-01-07 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (4, 'TR20250101004', 1, 7, 8, 100.00, 'ALIPAY', 1, 3, NULL, NULL, '2025-01-08 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (5, 'TR20250101005', 1, 9, 10, 100.00, 'WECHAT', 1, 3, NULL, NULL, '2025-01-09 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (6, 'TR20250301001', 2, 11, NULL, 80.00, 'BALANCE', 1, 3, NULL, NULL, '2025-03-05 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (7, 'TR20250301002', 2, 12, NULL, 80.00, 'ALIPAY', 1, 3, NULL, NULL, '2025-03-06 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (8, 'TR20250301003', 2, 13, NULL, 80.00, 'WECHAT', 1, 3, NULL, NULL, '2025-03-07 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (9, 'TR20250201001', 3, 14, 15, 120.00, 'ALIPAY', 1, 3, NULL, NULL, '2025-02-05 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (10, 'TR20250201002', 3, 16, 17, 120.00, 'WECHAT', 1, 3, NULL, NULL, '2025-02-06 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (11, 'TR20250201003', 3, 18, 19, 120.00, 'BALANCE', 1, 3, NULL, NULL, '2025-02-07 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (12, 'TR20250401001', 4, 20, NULL, 60.00, 'ALIPAY', 1, 3, NULL, NULL, '2025-04-05 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (13, 'TR20250401002', 4, 21, NULL, 60.00, 'WECHAT', 1, 3, NULL, NULL, '2025-04-06 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (14, 'TR20250401003', 4, 22, NULL, 60.00, 'BALANCE', 1, 3, NULL, NULL, '2025-04-07 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (15, 'TR20241001001', 5, 23, NULL, 500.00, 'ALIPAY', 1, 3, NULL, NULL, '2024-10-05 10:00:00', NULL, 0);
INSERT INTO `biz_tournament_registration` VALUES (16, 'TR20241001002', 5, 24, NULL, 500.00, 'WECHAT', 1, 3, NULL, NULL, '2024-10-06 10:00:00', NULL, 0);
INSERT INTO `biz_tournament_registration` VALUES (17, 'TR20250601001', 6, 1, NULL, 50.00, 'ALIPAY', 1, 3, NULL, NULL, '2025-06-01 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (18, 'TR20250601002', 6, 2, NULL, 50.00, 'WECHAT', 1, 3, NULL, NULL, '2025-06-01 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (19, 'TR20250601003', 6, 3, NULL, 50.00, 'BALANCE', 1, 3, NULL, NULL, '2025-06-01 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (20, 'TR20250601004', 6, 4, NULL, 50.00, 'ALIPAY', 1, 3, NULL, NULL, '2025-06-01 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (21, 'TR20250601005', 6, 5, NULL, 50.00, 'WECHAT', 1, 3, NULL, NULL, '2025-06-01 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (22, 'TR20250601006', 6, 6, NULL, 50.00, 'BALANCE', 1, 3, NULL, NULL, '2025-06-01 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (23, 'TR20250601007', 6, 7, NULL, 50.00, 'ALIPAY', 1, 3, NULL, NULL, '2025-06-01 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (24, 'TR20250601008', 6, 8, NULL, 50.00, 'WECHAT', 1, 3, NULL, NULL, '2025-06-01 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (25, 'TR20250601009', 6, 9, NULL, 50.00, 'BALANCE', 1, 3, NULL, NULL, '2025-06-01 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (26, 'TR20250601010', 6, 10, NULL, 50.00, 'ALIPAY', 1, 3, NULL, NULL, '2025-06-01 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (27, 'TR20250602001', 7, 11, 12, 100.00, NULL, 0, 0, NULL, NULL, '2025-06-02 10:00:00', '2026-02-18 14:00:57', 0);
INSERT INTO `biz_tournament_registration` VALUES (28, 'TR20250602002', 7, 13, 14, 100.00, NULL, 0, 0, NULL, NULL, '2025-06-02 10:00:00', '2026-02-18 14:00:56', 0);
INSERT INTO `biz_tournament_registration` VALUES (29, 'TR20250602003', 7, 15, 16, 100.00, 'BALANCE', 1, 3, NULL, NULL, '2025-06-02 10:00:00', '2026-02-18 14:09:25', 0);
INSERT INTO `biz_tournament_registration` VALUES (30, 'TR20250602004', 7, 17, 18, 100.00, NULL, 0, 0, NULL, NULL, '2025-06-02 10:00:00', '2026-02-18 14:00:58', 0);
INSERT INTO `biz_tournament_registration` VALUES (31, 'TR20250602005', 7, 19, 20, 100.00, 'CASH', 1, 3, NULL, NULL, '2025-06-02 10:00:00', '2026-02-18 14:10:25', 0);
INSERT INTO `biz_tournament_registration` VALUES (32, 'TR20250603001', 8, 21, NULL, 60.00, 'ALIPAY', 1, 3, NULL, NULL, '2025-06-03 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (33, 'TR20250603002', 8, 22, NULL, 60.00, 'WECHAT', 1, 3, NULL, NULL, '2025-06-03 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (34, 'TR20250603003', 8, 23, NULL, 60.00, 'BALANCE', 1, 3, NULL, NULL, '2025-06-03 10:00:00', '2026-02-18 12:54:09', 0);
INSERT INTO `biz_tournament_registration` VALUES (35, 'TR20260218001', 12, 45, NULL, 0.00, 'BALANCE', 1, 0, NULL, NULL, '2026-02-18 13:55:58', '2026-02-18 14:00:51', 0);
INSERT INTO `biz_tournament_registration` VALUES (36, 'TR20260218002', 12, 1, NULL, 0.00, 'BALANCE', 1, 0, NULL, NULL, '2026-02-18 13:59:12', '2026-02-18 14:00:52', 0);

-- ----------------------------
-- Table structure for sys_coach
-- ----------------------------
DROP TABLE IF EXISTS `sys_coach`;
CREATE TABLE `sys_coach`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '教练ID',
  `coach_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `gender` tinyint(1) NULL DEFAULT NULL COMMENT '性别（0-女，1-男）',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '身份证号',
  `specialty` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业特长',
  `experience` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '教学经验',
  `hourly_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '课时费（每小时）',
  `rating` decimal(3, 2) NULL DEFAULT 0.00 COMMENT '评分（0-5分）',
  `total_students` int NULL DEFAULT 0 COMMENT '累计学员数',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态（0-停用，1-正常）',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `venue_id` bigint NOT NULL COMMENT '所属场馆ID（外键关联sys_venue）',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID（外键关联sys_user，COACH角色绑定）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_rating`(`rating` ASC) USING BTREE,
  INDEX `idx_coach_venue_status`(`venue_id` ASC, `status` ASC) USING BTREE,
  UNIQUE INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `sys_coach_ibfk_1` FOREIGN KEY (`venue_id`) REFERENCES `sys_venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `sys_coach_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '教练表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_coach
-- ----------------------------
INSERT INTO `sys_coach` VALUES (1, '王教练', 1, '13810001001', '110101198001011001', '专业羽毛球训练', '国家一级运动员，从事羽毛球教学15年，培养多名优秀学员', 200.00, 4.90, 86, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 1, NULL, '2024-01-15 10:00:00', NULL, 0);
INSERT INTO `sys_coach` VALUES (2, '李教练', 0, '13810001002', '110101198002022002', '青少年培训', '专业青少年羽毛球教练，擅长儿童启蒙教学', 150.00, 4.80, 72, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 1, NULL, '2024-01-15 10:00:00', NULL, 0);
INSERT INTO `sys_coach` VALUES (3, '张教练', 1, '13810001003', '110101198003033003', '技术提升', '前省队队员，擅长技术细节纠正和战术指导', 180.00, 4.70, 65, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 1, NULL, '2024-01-15 10:00:00', NULL, 0);
INSERT INTO `sys_coach` VALUES (4, '刘教练', 0, '13810001004', '110101198004044004', '初学入门', '耐心细致，适合零基础学员', 120.00, 4.60, 48, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 2, NULL, '2024-02-20 09:00:00', NULL, 0);
INSERT INTO `sys_coach` VALUES (5, '陈教练', 1, '13810001005', '110101198005055005', '比赛训练', '前国家队队员，擅长比赛心理和战术训练', 250.00, 5.00, 95, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 2, NULL, '2024-02-20 09:00:00', NULL, 0);
INSERT INTO `sys_coach` VALUES (6, '杨教练', 0, '13810001006', '110101198006066006', '成人培训', '专注成人羽毛球培训，经验丰富', 160.00, 4.70, 58, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 3, NULL, '2024-03-10 08:00:00', NULL, 0);
INSERT INTO `sys_coach` VALUES (7, '黄教练', 1, '13810001007', '110101198007077007', '体能训练', '专业体能教练，擅长运动康复', 140.00, 4.50, 42, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 3, NULL, '2024-03-10 08:00:00', NULL, 0);
INSERT INTO `sys_coach` VALUES (8, '林教练', 0, '13810001008', '110101198008088008', '双打训练', '双打专项教练，擅长配合训练', 170.00, 4.80, 68, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 4, NULL, '2024-04-15 10:00:00', NULL, 0);
INSERT INTO `sys_coach` VALUES (9, '徐教练', 1, '13810001009', '110101198009099009', '混双训练', '混双专项教练，经验丰富', 165.00, 4.60, 55, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 4, NULL, '2024-04-15 10:00:00', NULL, 0);
INSERT INTO `sys_coach` VALUES (10, '朱教练', 0, '13810001010', '110101198010101010', '基础训练', '基础训练专家，适合初学者', 110.00, 4.40, 38, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 5, NULL, '2024-05-20 09:00:00', NULL, 0);
INSERT INTO `sys_coach` VALUES (11, '陆教练', 1, '13810001011', '110101198011111011', '高级训练', '已停用', 200.00, 4.50, 50, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 1, NULL, '2024-06-01 10:00:00', '2026-02-15 10:20:11', 0);
INSERT INTO `sys_coach` VALUES (12, '吴教练', 0, '13810001012', '110101198012121012', '初级训练', '已停用', 100.00, 4.00, 20, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 2, NULL, '2024-06-15 10:00:00', '2026-02-15 10:20:15', 0);
INSERT INTO `sys_coach` VALUES (13, '涂家乐', 1, NULL, NULL, NULL, NULL, 200.00, 5.00, 50, 1, '/api/uploads/avatars/52083bd8b9464ad4b27514480041b298.jpg', 6, NULL, '2026-02-15 10:14:28', '2026-02-15 10:16:41', 1);
INSERT INTO `sys_coach` VALUES (14, '涂家乐', 1, NULL, NULL, NULL, NULL, 200.00, 5.00, 50, 1, '/api/uploads/avatars/52083bd8b9464ad4b27514480041b298.jpg', 6, NULL, '2026-02-15 10:16:50', '2026-02-15 10:16:50', 0);

-- ----------------------------
-- Table structure for sys_court
-- ----------------------------
DROP TABLE IF EXISTS `sys_court`;
CREATE TABLE `sys_court`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '场地ID',
  `court_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '场地编号（如：A1、B2）',
  `venue_id` bigint NOT NULL COMMENT '所属场馆ID（外键关联sys_venue）',
  `court_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '场地名称',
  `billing_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'HOUR' COMMENT '计费方式（HOUR-按小时，TIME-按次）',
  `price_per_hour` decimal(10, 2) NOT NULL COMMENT '每小时价格或每次价格',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态（0-维护中，1-空闲，2-预约中，3-使用中）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_venue_court`(`venue_id` ASC, `court_code` ASC) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  INDEX `idx_court_code`(`court_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_court_venue_status`(`venue_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_venue_status`(`venue_id` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `sys_court_ibfk_1` FOREIGN KEY (`venue_id`) REFERENCES `sys_venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '场地表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_court
-- ----------------------------
INSERT INTO `sys_court` VALUES (1, 'A01', 1, 'A区1号场地', 'HOUR', 80.00, 1, '2024-01-15 10:00:00', '2026-02-14 12:59:47', 0);
INSERT INTO `sys_court` VALUES (2, 'A02', 1, 'A区2号场地', 'HOUR', 80.00, 2, '2024-01-15 10:00:00', '2026-02-14 12:59:47', 0);
INSERT INTO `sys_court` VALUES (3, 'A03', 1, 'A区3号场地', 'HOUR', 80.00, 1, '2024-01-15 10:00:00', '2026-02-11 22:34:32', 0);
INSERT INTO `sys_court` VALUES (4, 'B01', 1, 'B区1号场地', 'HOUR', 100.00, 1, '2024-01-15 10:00:00', '2026-02-09 23:52:22', 0);
INSERT INTO `sys_court` VALUES (5, 'B02', 1, 'B区2号场地', 'HOUR', 100.00, 1, '2024-01-15 10:00:00', '2026-02-09 22:50:01', 0);
INSERT INTO `sys_court` VALUES (6, 'C01', 2, 'C区1号场地', 'HOUR', 90.00, 1, '2024-02-20 09:00:00', NULL, 0);
INSERT INTO `sys_court` VALUES (7, 'C02', 2, 'C区2号场地', 'HOUR', 90.00, 1, '2024-02-20 09:00:00', NULL, 0);
INSERT INTO `sys_court` VALUES (8, 'C03', 2, 'C区3号场地', 'HOUR', 90.00, 1, '2024-02-20 09:00:00', NULL, 0);
INSERT INTO `sys_court` VALUES (9, 'VIP01', 2, 'VIP1号场地', 'HOUR', 150.00, 1, '2024-02-20 09:00:00', NULL, 0);
INSERT INTO `sys_court` VALUES (10, 'D01', 3, 'D区1号场地', 'HOUR', 85.00, 1, '2024-03-10 08:00:00', NULL, 0);
INSERT INTO `sys_court` VALUES (11, 'D02', 3, 'D区2号场地', 'HOUR', 85.00, 1, '2024-03-10 08:00:00', NULL, 0);
INSERT INTO `sys_court` VALUES (12, 'D03', 3, 'D区3号场地', 'HOUR', 85.00, 1, '2024-03-10 08:00:00', NULL, 0);
INSERT INTO `sys_court` VALUES (13, 'E01', 3, 'E区1号场地', 'HOUR', 95.00, 1, '2024-03-10 08:00:00', NULL, 0);
INSERT INTO `sys_court` VALUES (14, 'E02', 3, 'E区2号场地', 'HOUR', 95.00, 1, '2024-03-10 08:00:00', NULL, 0);
INSERT INTO `sys_court` VALUES (15, 'F01', 4, 'F区1号场地', 'HOUR', 88.00, 1, '2024-04-15 10:00:00', NULL, 0);
INSERT INTO `sys_court` VALUES (16, 'F02', 4, 'F区2号场地', 'HOUR', 88.00, 1, '2024-04-15 10:00:00', NULL, 0);
INSERT INTO `sys_court` VALUES (17, 'F03', 4, 'F区3号场地', 'HOUR', 88.00, 1, '2024-04-15 10:00:00', NULL, 0);
INSERT INTO `sys_court` VALUES (18, 'VIP02', 4, 'VIP2号场地', 'HOUR', 160.00, 1, '2024-04-15 10:00:00', NULL, 0);
INSERT INTO `sys_court` VALUES (19, 'G01', 5, 'G区1号场地', 'HOUR', 75.00, 1, '2024-05-20 09:00:00', NULL, 0);
INSERT INTO `sys_court` VALUES (20, 'G02', 5, 'G区2号场地', 'HOUR', 75.00, 1, '2024-05-20 09:00:00', NULL, 0);
INSERT INTO `sys_court` VALUES (21, 'G03', 5, 'G区3号场地', 'HOUR', 75.00, 1, '2024-05-20 09:00:00', NULL, 0);
INSERT INTO `sys_court` VALUES (22, 'A04', 1, 'A区4号场地', 'HOUR', 80.00, 1, '2024-05-20 09:00:00', '2026-02-09 23:52:19', 0);
INSERT INTO `sys_court` VALUES (23, 'C04', 2, 'C区4号场地', 'HOUR', 90.00, 0, '2024-05-20 09:00:00', NULL, 0);
INSERT INTO `sys_court` VALUES (24, 'B03', 1, 'B区3号场地', 'HOUR', 100.00, 1, '2024-05-20 09:00:00', '2026-02-09 23:52:24', 0);
INSERT INTO `sys_court` VALUES (25, 'D04', 3, 'D区4号场地', 'HOUR', 85.00, 1, '2024-05-20 09:00:00', '2026-02-09 23:48:40', 0);
INSERT INTO `sys_court` VALUES (26, 'E03', 3, 'E区3号场地', 'HOUR', 95.00, 1, '2024-05-20 09:00:00', '2026-02-09 23:48:43', 0);
INSERT INTO `sys_court` VALUES (27, 'F04', 4, 'F区4号场地', 'HOUR', 88.00, 1, '2024-05-20 09:00:00', '2026-02-09 23:48:32', 0);
INSERT INTO `sys_court` VALUES (28, '1', 6, '1号场地', 'TIME', 15.00, 1, '2026-02-06 17:02:05', '2026-02-14 13:07:54', 0);
INSERT INTO `sys_court` VALUES (33, '2', 6, '2号场地', 'TIME', 15.00, 1, '2026-02-06 17:06:58', '2026-02-09 23:11:08', 0);
INSERT INTO `sys_court` VALUES (34, '3', 6, '3号场地', 'TIME', 15.00, 1, '2026-02-06 17:07:10', '2026-02-09 23:09:31', 0);
INSERT INTO `sys_court` VALUES (35, '4', 6, '4号场地', 'TIME', 15.00, 1, '2026-02-06 17:09:47', '2026-02-06 17:09:47', 0);
INSERT INTO `sys_court` VALUES (36, '5', 6, '5号场地', 'TIME', 15.00, 1, '2026-02-06 17:09:57', '2026-02-06 17:09:57', 0);

-- ----------------------------
-- Table structure for sys_equipment
-- ----------------------------
DROP TABLE IF EXISTS `sys_equipment`;
CREATE TABLE `sys_equipment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '器材ID',
  `equipment_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '器材编号',
  `equipment_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '器材图片URL（主图，保留字段，向后兼容）',
  `equipment_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '器材名称',
  `equipment_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '器材类型（RACKET-球拍，SHUTTLE-羽毛球，STRING-球线）',
  `brand` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品牌',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '单价',
  `rental_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '租借价格（每小时或每次）',
  `rental_deposit` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '租借押金',
  `total_quantity` int NULL DEFAULT 0 COMMENT '总数量',
  `available_quantity` int NULL DEFAULT 0 COMMENT '可用数量',
  `version` int NULL DEFAULT 0 COMMENT '版本号（乐观锁，用于库存扣减并发控制）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态（0-停用，1-正常）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '器材描述',
  `venue_id` bigint NOT NULL COMMENT '所属场馆ID（外键关联sys_venue）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `equipment_code`(`equipment_code` ASC) USING BTREE,
  UNIQUE INDEX `idx_equipment_code`(`equipment_code` ASC) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  INDEX `idx_equipment_type`(`equipment_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_equipment_version`(`id` ASC, `version` ASC) USING BTREE,
  INDEX `idx_equipment_venue_status`(`venue_id` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `sys_equipment_ibfk_1` FOREIGN KEY (`venue_id`) REFERENCES `sys_venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '器材表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_equipment
-- ----------------------------
INSERT INTO `sys_equipment` VALUES (1, 'EQ001', NULL, 'YONEX ARC11 羽毛球拍', 'RACKET', 'YONEX', 1200.00, 30.00, 0.00, 10, 9, 0, 1, '专业级羽毛球拍，适合中高级选手', 1, '2024-01-15 10:00:00', '2026-02-14 13:11:45', 0);
INSERT INTO `sys_equipment` VALUES (2, 'EQ002', NULL, 'YONEX ASTROX 99 羽毛球拍', 'RACKET', 'YONEX', 1500.00, 35.00, 0.00, 8, 6, 0, 1, '进攻型羽毛球拍，适合力量型选手', 1, '2024-01-15 10:00:00', NULL, 0);
INSERT INTO `sys_equipment` VALUES (3, 'EQ003', NULL, 'YONEX AS-50 羽毛球', 'SHUTTLE', 'YONEX', 180.00, 5.00, 0.00, 50, 47, 2, 1, '比赛级羽毛球，飞行稳定', 1, '2024-01-15 10:00:00', '2026-02-17 14:08:10', 0);
INSERT INTO `sys_equipment` VALUES (4, 'EQ004', NULL, 'YONEX BG65 球线', 'STRING', 'YONEX', 50.00, 0.00, 0.00, 100, 95, 0, 1, '经典球线，耐用性好', 1, '2024-01-15 10:00:00', NULL, 0);
INSERT INTO `sys_equipment` VALUES (5, 'EQ005', NULL, 'YONEX BG80 球线', 'STRING', 'YONEX', 60.00, 0.00, 0.00, 80, 75, 0, 1, '高弹性球线，适合进攻型打法', 1, '2024-01-15 10:00:00', NULL, 0);
INSERT INTO `sys_equipment` VALUES (6, 'EQ006', NULL, 'VICTOR JS-12 羽毛球拍', 'RACKET', 'VICTOR', 1100.00, 28.00, 0.00, 12, 11, 2, 1, '速度型羽毛球拍，适合快速打法', 2, '2024-02-20 09:00:00', '2026-02-17 14:15:12', 0);
INSERT INTO `sys_equipment` VALUES (7, 'EQ007', NULL, 'VICTOR THRUSTER F 羽毛球拍', 'RACKET', 'VICTOR', 1300.00, 32.00, 0.00, 10, 8, 4, 1, '平衡型羽毛球拍，适合全面型选手', 2, '2024-02-20 09:00:00', '2026-02-17 14:22:17', 0);
INSERT INTO `sys_equipment` VALUES (8, 'EQ008', NULL, 'VICTOR MASTER ACE 羽毛球', 'SHUTTLE', 'VICTOR', 160.00, 4.50, 0.00, 60, 55, 0, 1, '训练级羽毛球，性价比高', 2, '2024-02-20 09:00:00', NULL, 0);
INSERT INTO `sys_equipment` VALUES (9, 'EQ009', NULL, 'VICTOR VBS-66N 球线', 'STRING', 'VICTOR', 55.00, 0.00, 0.00, 90, 85, 0, 1, '高弹性球线，手感好', 2, '2024-02-20 09:00:00', NULL, 0);
INSERT INTO `sys_equipment` VALUES (10, 'EQ010', NULL, 'LI-NING N90 羽毛球拍', 'RACKET', 'LI-NING', 1000.00, 25.00, 0.00, 15, 13, 0, 1, '国产品牌，性价比高', 3, '2024-03-10 08:00:00', '2026-02-14 13:11:42', 0);
INSERT INTO `sys_equipment` VALUES (11, 'EQ011', NULL, 'LI-NING N7 羽毛球拍', 'RACKET', 'LI-NING', 900.00, 22.00, 0.00, 12, 10, 0, 1, '入门级羽毛球拍，适合初学者', 3, '2024-03-10 08:00:00', NULL, 0);
INSERT INTO `sys_equipment` VALUES (12, 'EQ012', NULL, 'LI-NING A+60 羽毛球', 'SHUTTLE', 'LI-NING', 140.00, 4.00, 0.00, 70, 65, 0, 1, '训练级羽毛球，耐用', 3, '2024-03-10 08:00:00', NULL, 0);
INSERT INTO `sys_equipment` VALUES (13, 'EQ013', NULL, 'LI-NING 1号线', 'STRING', 'LI-NING', 45.00, 0.00, 0.00, 120, 115, 0, 1, '基础球线，适合初学者', 3, '2024-03-10 08:00:00', NULL, 0);
INSERT INTO `sys_equipment` VALUES (14, 'EQ014', NULL, 'KAWASAKI 9900 羽毛球拍', 'RACKET', 'KAWASAKI', 800.00, 20.00, 0.00, 10, 10, 0, 1, '入门级羽毛球拍', 4, '2024-04-15 10:00:00', '2026-02-14 13:12:50', 0);
INSERT INTO `sys_equipment` VALUES (15, 'EQ015', NULL, 'KAWASAKI 大师3号 羽毛球', 'SHUTTLE', 'KAWASAKI', 120.00, 3.50, 0.00, 80, 75, 0, 1, '训练级羽毛球', 4, '2024-04-15 10:00:00', NULL, 0);
INSERT INTO `sys_equipment` VALUES (16, 'EQ016', NULL, 'KAWASAKI K-66 球线', 'STRING', 'KAWASAKI', 40.00, 0.00, 0.00, 100, 95, 0, 1, '基础球线', 4, '2024-04-15 10:00:00', NULL, 0);
INSERT INTO `sys_equipment` VALUES (17, 'EQ017', NULL, 'RSL X5 羽毛球拍', 'RACKET', 'RSL', 750.00, 18.00, 0.00, 8, 6, 0, 1, '入门级羽毛球拍', 5, '2024-05-20 09:00:00', NULL, 0);
INSERT INTO `sys_equipment` VALUES (18, 'EQ018', NULL, 'RSL CLASSIC 羽毛球', 'SHUTTLE', 'RSL', 110.00, 3.00, 0.00, 60, 55, 0, 1, '训练级羽毛球', 5, '2024-05-20 09:00:00', NULL, 0);
INSERT INTO `sys_equipment` VALUES (19, 'EQ019', NULL, 'RSL 66球线', 'STRING', 'RSL', 35.00, 0.00, 0.00, 90, 85, 0, 1, '基础球线', 5, '2024-05-20 09:00:00', NULL, 0);
INSERT INTO `sys_equipment` VALUES (20, 'EQ020', NULL, 'YONEX NANORAY 羽毛球拍', 'RACKET', 'YONEX', 1100.00, 28.00, 0.00, 5, 0, 0, 0, '已停用，库存不足', 1, '2024-06-01 10:00:00', NULL, 0);
INSERT INTO `sys_equipment` VALUES (21, 'EQ021', NULL, 'VICTOR POWER 羽毛球', 'SHUTTLE', 'VICTOR', 150.00, 4.00, 0.00, 0, 0, 0, 0, '已停用，缺货', 2, '2024-06-15 10:00:00', NULL, 0);
INSERT INTO `sys_equipment` VALUES (22, 'EQ022', NULL, 'LI-NING 高级球线', 'STRING', 'LI-NING', 70.00, 0.00, 0.00, 0, 0, 0, 0, '已停用，更新产品', 3, '2024-06-20 10:00:00', NULL, 0);
INSERT INTO `sys_equipment` VALUES (23, 'XR001', NULL, 'NRZSP 羽毛球拍', 'RACKET', NULL, NULL, NULL, 0.00, 5, 5, 0, 1, NULL, 6, '2026-02-12 12:43:41', '2026-02-12 13:52:10', 1);
INSERT INTO `sys_equipment` VALUES (27, 'XR002', NULL, 'NRZSP 羽毛球拍', 'RACKET', NULL, NULL, 20.00, 5.00, 5, 5, 6, 1, NULL, 6, '2026-02-12 14:01:44', '2026-02-18 12:50:41', 0);

-- ----------------------------
-- Table structure for sys_equipment_image
-- ----------------------------
DROP TABLE IF EXISTS `sys_equipment_image`;
CREATE TABLE `sys_equipment_image`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `equipment_id` bigint NOT NULL COMMENT '器材ID（外键关联sys_equipment）',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片URL',
  `image_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'DETAIL' COMMENT '图片类型（MAIN-主图，DETAIL-详情图）',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_equipment_id`(`equipment_id` ASC) USING BTREE,
  INDEX `idx_image_type`(`image_type` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  CONSTRAINT `sys_equipment_image_ibfk_1` FOREIGN KEY (`equipment_id`) REFERENCES `sys_equipment` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '器材图片表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_equipment_image
-- ----------------------------
INSERT INTO `sys_equipment_image` VALUES (1, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-01-15 10:00:00');
INSERT INTO `sys_equipment_image` VALUES (2, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-01-15 10:00:00');
INSERT INTO `sys_equipment_image` VALUES (3, 2, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-01-15 10:00:00');
INSERT INTO `sys_equipment_image` VALUES (4, 2, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-01-15 10:00:00');
INSERT INTO `sys_equipment_image` VALUES (5, 3, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-01-15 10:00:00');
INSERT INTO `sys_equipment_image` VALUES (6, 3, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-01-15 10:00:00');
INSERT INTO `sys_equipment_image` VALUES (7, 4, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-01-15 10:00:00');
INSERT INTO `sys_equipment_image` VALUES (8, 4, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-01-15 10:00:00');
INSERT INTO `sys_equipment_image` VALUES (9, 5, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-01-15 10:00:00');
INSERT INTO `sys_equipment_image` VALUES (10, 5, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-01-15 10:00:00');
INSERT INTO `sys_equipment_image` VALUES (11, 6, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-02-20 09:00:00');
INSERT INTO `sys_equipment_image` VALUES (12, 6, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-02-20 09:00:00');
INSERT INTO `sys_equipment_image` VALUES (13, 7, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-02-20 09:00:00');
INSERT INTO `sys_equipment_image` VALUES (14, 7, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-02-20 09:00:00');
INSERT INTO `sys_equipment_image` VALUES (15, 8, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-02-20 09:00:00');
INSERT INTO `sys_equipment_image` VALUES (16, 8, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-02-20 09:00:00');
INSERT INTO `sys_equipment_image` VALUES (17, 9, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-02-20 09:00:00');
INSERT INTO `sys_equipment_image` VALUES (18, 9, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-02-20 09:00:00');
INSERT INTO `sys_equipment_image` VALUES (19, 10, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-03-10 08:00:00');
INSERT INTO `sys_equipment_image` VALUES (20, 10, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-03-10 08:00:00');
INSERT INTO `sys_equipment_image` VALUES (21, 11, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-03-10 08:00:00');
INSERT INTO `sys_equipment_image` VALUES (22, 11, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-03-10 08:00:00');
INSERT INTO `sys_equipment_image` VALUES (23, 12, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-03-10 08:00:00');
INSERT INTO `sys_equipment_image` VALUES (24, 12, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-03-10 08:00:00');
INSERT INTO `sys_equipment_image` VALUES (25, 13, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-03-10 08:00:00');
INSERT INTO `sys_equipment_image` VALUES (26, 13, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-03-10 08:00:00');
INSERT INTO `sys_equipment_image` VALUES (27, 14, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-04-15 10:00:00');
INSERT INTO `sys_equipment_image` VALUES (28, 14, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-04-15 10:00:00');
INSERT INTO `sys_equipment_image` VALUES (29, 15, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-04-15 10:00:00');
INSERT INTO `sys_equipment_image` VALUES (30, 15, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-04-15 10:00:00');
INSERT INTO `sys_equipment_image` VALUES (31, 16, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-04-15 10:00:00');
INSERT INTO `sys_equipment_image` VALUES (32, 16, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-04-15 10:00:00');
INSERT INTO `sys_equipment_image` VALUES (33, 17, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-05-20 09:00:00');
INSERT INTO `sys_equipment_image` VALUES (34, 17, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-05-20 09:00:00');
INSERT INTO `sys_equipment_image` VALUES (35, 18, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-05-20 09:00:00');
INSERT INTO `sys_equipment_image` VALUES (36, 18, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-05-20 09:00:00');
INSERT INTO `sys_equipment_image` VALUES (37, 19, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-05-20 09:00:00');
INSERT INTO `sys_equipment_image` VALUES (38, 19, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-05-20 09:00:00');

-- ----------------------------
-- Table structure for sys_member
-- ----------------------------
DROP TABLE IF EXISTS `sys_member`;
CREATE TABLE `sys_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID（外键关联sys_user，可为空）',
  `member_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `gender` tinyint(1) NULL DEFAULT NULL COMMENT '性别（0-女，1-男）',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '身份证号',
  `member_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NORMAL' COMMENT '会员类型（NORMAL-普通用户，MEMBER-会员）',
  `member_level` int NULL DEFAULT 1 COMMENT '会员等级（1-5级，用于会员等级划分）',
  `register_time` datetime NULL DEFAULT NULL COMMENT '注册时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '到期时间（仅会员MEMBER需要，普通用户NORMAL可为空）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态（0-冻结，1-正常，2-到期）',
  `total_consumption` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '累计消费金额',
  `balance` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '账户余额',
  `version` int NULL DEFAULT 0 COMMENT '版本号（乐观锁，用于余额扣减并发控制）',
  `total_recharge` decimal(10, 2) NULL DEFAULT 0.00,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记（0-正常，1-删除）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_id_card`(`id_card` ASC) USING BTREE,
  INDEX `idx_member_type`(`member_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_member_version`(`id` ASC, `version` ASC) USING BTREE,
  INDEX `idx_member_balance`(`balance` ASC) USING BTREE,
  CONSTRAINT `sys_member_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 55 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_member
-- ----------------------------
INSERT INTO `sys_member` VALUES (1, NULL, '张三', NULL, '13900001001', '110101199001011001', 'MEMBER', 5, NULL, '2025-06-01 10:00:00', 1, 500.00, 2005.00, 1, 2200.00, '2024-06-01 10:00:00', '2026-02-18 14:00:18', 0);
INSERT INTO `sys_member` VALUES (2, 7, '李四', 0, '13900001002', '110101199002022002', 'MEMBER', 4, '2024-06-02 10:00:00', '2025-06-02 10:00:00', 1, 4520.00, 180.00, 0, 0.00, '2024-06-02 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (3, 8, '王五', 1, '13900001003', '110101199003033003', 'MEMBER', 5, '2024-06-03 10:00:00', '2025-06-03 10:00:00', 1, 3795.00, 605.00, 0, 1000.00, '2024-06-03 10:00:00', '2026-02-16 21:49:53', 0);
INSERT INTO `sys_member` VALUES (4, 9, '赵六', 0, '13900001004', '110101199004044004', 'MEMBER', 2, '2024-06-04 10:00:00', '2025-06-04 10:00:00', 1, 2150.00, 50.00, 0, 0.00, '2024-06-04 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (5, 10, '孙七', 1, '13900001005', '110101199005055005', 'MEMBER', 5, '2024-06-05 10:00:00', '2025-06-05 10:00:00', 1, 980.00, 1370.00, 0, 1350.00, '2024-06-05 10:00:00', '2026-02-09 13:33:48', 0);
INSERT INTO `sys_member` VALUES (6, NULL, '周八', 1, '13900001006', '110101199006066006', 'MEMBER', 5, '2024-07-01 10:00:00', '2025-07-01 10:00:00', 1, 7200.00, 500.00, 0, 0.00, '2024-07-01 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (7, NULL, '吴九', 0, '13900001007', '110101199007077007', 'MEMBER', 4, '2024-07-02 10:00:00', '2025-07-02 10:00:00', 1, 5800.00, 350.00, 0, 0.00, '2024-07-02 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (8, NULL, '郑十', 1, '13900001008', '110101199008088008', 'MEMBER', 4, '2024-07-03 10:00:00', '2025-07-03 10:00:00', 1, 5200.00, 280.00, 0, 0.00, '2024-07-03 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (9, NULL, '钱十一', NULL, '13900001009', '110101199009099009', 'MEMBER', 5, NULL, '2025-07-04 10:00:00', 1, NULL, 800.00, 0, 0.00, '2024-07-04 10:00:00', '2026-02-07 16:13:51', 0);
INSERT INTO `sys_member` VALUES (10, NULL, '孙十二', 1, '13900001010', '110101199010101010', 'MEMBER', 3, '2024-07-05 10:00:00', '2025-07-05 10:00:00', 1, 3600.00, 100.00, 0, 0.00, '2024-07-05 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (11, NULL, '李十三', 0, '13900001011', '110101199011111011', 'MEMBER', 2, '2024-07-06 10:00:00', '2025-07-06 10:00:00', 1, 2400.00, 80.00, 0, 0.00, '2024-07-06 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (12, NULL, '王十四', 1, '13900001012', '110101199012121012', 'MEMBER', 2, '2024-07-07 10:00:00', '2025-07-07 10:00:00', 1, 2200.00, 60.00, 0, 0.00, '2024-07-07 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (13, NULL, '张十五', 0, '13900001013', '110101199013131013', 'MEMBER', 1, '2024-07-08 10:00:00', '2025-07-08 10:00:00', 1, 1200.00, 30.00, 0, 0.00, '2024-07-08 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (14, NULL, '刘十六', 1, '13900001014', '110101199014141014', 'MEMBER', 1, '2024-07-09 10:00:00', '2025-07-09 10:00:00', 1, 1000.00, 25.00, 0, 0.00, '2024-07-09 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (15, NULL, '陈十七', 0, '13900001015', '110101199015151015', 'MEMBER', 5, '2024-07-10 10:00:00', '2025-07-10 10:00:00', 1, 6900.00, 350.00, 1, 0.00, '2024-07-10 10:00:00', '2026-02-18 14:09:15', 0);
INSERT INTO `sys_member` VALUES (16, NULL, '杨十八', 1, '13900001016', '110101199016161016', 'MEMBER', 4, '2024-07-11 10:00:00', '2025-07-11 10:00:00', 1, 5400.00, 320.00, 0, 0.00, '2024-07-11 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (17, NULL, '黄十九', 0, '13900001017', '110101199017171017', 'MEMBER', 3, '2024-07-12 10:00:00', '2025-07-12 10:00:00', 1, 4000.00, 200.00, 0, 0.00, '2024-07-12 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (18, NULL, '林二十', 1, '13900001018', '110101199018181018', 'MEMBER', 2, '2024-07-13 10:00:00', '2025-07-13 10:00:00', 1, 2600.00, 90.00, 0, 0.00, '2024-07-13 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (19, NULL, '徐二一', 0, '13900001019', '110101199019191019', 'MEMBER', 1, '2024-07-14 10:00:00', '2025-07-14 10:00:00', 1, 1400.00, 40.00, 0, 0.00, '2024-07-14 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (20, NULL, '朱二二', 1, '13900001020', '110101199020202020', 'MEMBER', 5, '2024-08-01 10:00:00', '2025-08-01 10:00:00', 1, 7500.00, 600.00, 0, 0.00, '2024-08-01 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (21, NULL, '马二三', 0, '13900001021', '110101199021212021', 'MEMBER', 4, '2024-08-02 10:00:00', '2025-08-02 10:00:00', 1, 6000.00, 400.00, 0, 0.00, '2024-08-02 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (22, NULL, '胡二四', 1, '13900001022', '110101199022222022', 'MEMBER', 3, '2024-08-03 10:00:00', '2025-08-03 10:00:00', 1, 4200.00, 180.00, 0, 0.00, '2024-08-03 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (23, NULL, '郭二五', 0, '13900001023', '110101199023232023', 'MEMBER', 2, '2024-08-04 10:00:00', '2025-08-04 10:00:00', 1, 2800.00, 100.00, 0, 0.00, '2024-08-04 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (24, NULL, '何二六', 1, '13900001024', '110101199024242024', 'MEMBER', 1, '2024-08-05 10:00:00', '2025-08-05 10:00:00', 1, 1600.00, 50.00, 0, 0.00, '2024-08-05 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (25, NULL, '高二七', 0, '13900001025', '110101199025252025', 'MEMBER', 5, '2024-08-06 10:00:00', '2025-08-06 10:00:00', 1, 8000.00, 700.00, 0, 0.00, '2024-08-06 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (26, NULL, '罗二八', 1, '13900001026', '110101199026262026', 'MEMBER', 4, '2024-08-07 10:00:00', '2025-08-07 10:00:00', 1, 6200.00, 380.00, 0, 0.00, '2024-08-07 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (27, NULL, '梁二九', 0, '13900001027', '110101199027272027', 'MEMBER', 3, '2024-08-08 10:00:00', '2025-08-08 10:00:00', 1, 4400.00, 220.00, 0, 0.00, '2024-08-08 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (28, NULL, '宋三十', 1, '13900001028', '110101199028282028', 'MEMBER', 2, '2024-08-09 10:00:00', '2025-08-09 10:00:00', 1, 3000.00, 110.00, 0, 0.00, '2024-08-09 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (29, NULL, '唐三一', 0, '13900001029', '110101199029292029', 'MEMBER', 1, '2024-08-10 10:00:00', '2025-08-10 10:00:00', 1, 1800.00, 60.00, 0, 0.00, '2024-08-10 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (30, NULL, '许三二', 1, '13900001030', '110101199030303030', 'MEMBER', 5, '2024-09-01 10:00:00', '2025-09-01 10:00:00', 1, 8500.00, 800.00, 0, 0.00, '2024-09-01 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (31, NULL, '韩三三', 0, '13900001031', '110101199031313031', 'MEMBER', 4, '2024-09-02 10:00:00', '2025-09-02 10:00:00', 1, 6600.00, 420.00, 0, 0.00, '2024-09-02 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (32, NULL, '冯三四', 1, '13900001032', '110101199032323032', 'MEMBER', 3, '2024-09-03 10:00:00', '2025-09-03 10:00:00', 1, 4600.00, 240.00, 0, 0.00, '2024-09-03 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (33, NULL, '于三五', 0, '13900001033', '110101199033333033', 'MEMBER', 2, '2024-09-04 10:00:00', '2025-09-04 10:00:00', 1, 3200.00, 120.00, 0, 0.00, '2024-09-04 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (34, NULL, '董三六', 1, '13900001034', '110101199034343034', 'MEMBER', 1, '2024-09-05 10:00:00', '2025-09-05 10:00:00', 1, 2000.00, 70.00, 0, 0.00, '2024-09-05 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (35, NULL, '萧三七', 0, '13900001035', '110101199035353035', 'MEMBER', 1, '2024-10-01 10:00:00', '2027-02-07 16:21:35', 1, 500.00, 200.00, 0, 0.00, '2024-10-01 10:00:00', '2026-02-07 16:21:34', 0);
INSERT INTO `sys_member` VALUES (36, NULL, '程三八', 1, '13900001036', '110101199036363036', 'NORMAL', 1, '2024-10-02 10:00:00', NULL, 1, 300.00, 0.00, 0, 0.00, '2024-10-02 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (37, NULL, '曹三九', 0, '13900001037', '110101199037373037', 'NORMAL', 1, '2024-10-03 10:00:00', NULL, 1, 200.00, 0.00, 0, 0.00, '2024-10-03 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (38, NULL, '袁四十', 1, '13900001038', '110101199038383038', 'NORMAL', 1, '2024-10-04 10:00:00', NULL, 1, 150.00, 0.00, 0, 0.00, '2024-10-04 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (39, NULL, '邓四一', NULL, '13900001039', '110101199039393039', 'MEMBER', 5, NULL, NULL, 1, NULL, 500.00, 0, 0.00, '2024-10-05 10:00:00', '2026-02-07 16:12:47', 0);
INSERT INTO `sys_member` VALUES (40, NULL, '谢四二', 1, '13900001040', '110101199040404040', 'MEMBER', 3, '2023-06-01 10:00:00', '2024-06-01 10:00:00', 2, 3500.00, 0.00, 0, 0.00, '2023-06-01 10:00:00', NULL, 0);
INSERT INTO `sys_member` VALUES (41, NULL, '叶四三', NULL, '13900001041', '110101199041414041', 'MEMBER', 2, NULL, '2024-07-01 10:00:00', 1, NULL, 0.00, 0, 0.00, '2023-07-01 10:00:00', '2026-02-07 16:13:00', 0);
INSERT INTO `sys_member` VALUES (42, NULL, '吕四四', NULL, '13900001042', '110101199042424042', 'MEMBER', 4, NULL, '2025-05-01 10:00:00', 1, NULL, 0.00, 0, 0.00, '2024-05-01 10:00:00', '2026-02-07 16:13:04', 0);
INSERT INTO `sys_member` VALUES (43, 12, '测试用户001', 1, '13900009999', '110101199001011234', 'MEMBER', 5, '2025-01-20 10:00:00', '2026-01-20 10:00:00', 1, 500.00, 1100.00, 0, 1000.00, '2025-01-20 10:00:00', '2026-02-07 16:33:11', 0);
INSERT INTO `sys_member` VALUES (44, 13, '涂家乐', NULL, '17378406511', '511723200604093712', 'NORMAL', NULL, '2026-02-06 16:47:32', NULL, 1, 0.00, 0.00, 0, 0.00, '2026-02-06 16:47:32', '2026-02-07 16:14:08', 1);
INSERT INTO `sys_member` VALUES (45, NULL, '涂家乐', NULL, '17378406511', '511723200604093712', 'MEMBER', 5, NULL, '2027-02-07 16:15:42', 1, 3322.00, 10418.00, 12, 11515.00, '2026-02-07 16:14:32', '2026-02-22 15:31:22', 0);
INSERT INTO `sys_member` VALUES (54, 14, '黄依静', NULL, '', '', 'MEMBER', 5, '2026-02-22 15:23:48', '2027-02-22 15:36:58', 1, 0.00, 1200.00, 2, 1200.00, '2026-02-22 15:23:48', '2026-02-22 15:38:59', 0);

-- ----------------------------
-- Table structure for sys_notification
-- ----------------------------
DROP TABLE IF EXISTS `sys_notification`;
CREATE TABLE `sys_notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `publisher_id` bigint NOT NULL,
  `publisher_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `venue_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_publisher_id`(`publisher_id` ASC) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `sys_notification_ibfk_1` FOREIGN KEY (`publisher_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `sys_notification_ibfk_2` FOREIGN KEY (`venue_id`) REFERENCES `sys_venue` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notification
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '身份证号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '???MALE/FEMALE/OTHER?',
  `signature` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '????',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER' COMMENT '角色（PRESIDENT协会会长，VENUE_MANAGER场馆管理者，USER普通用户）',
  `venue_id` bigint NULL DEFAULT NULL COMMENT '场馆ID（外键关联sys_venue，仅场馆管理者需要，协会会长和普通用户为空）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_id_card`(`id_card` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  CONSTRAINT `fk_sys_user_venue` FOREIGN KEY (`venue_id`) REFERENCES `sys_venue` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '17378406511', '511723200604093712', '/api/uploads/avatars/b267beaeb9244675846ae04620f575b5.jpg', 'MALE', '你好，我是admin', 'PRESIDENT', NULL, 1, '2026-02-03 17:44:54', '2026-02-15 10:13:10', '2026-02-22 15:39:11');
INSERT INTO `sys_user` VALUES (2, 'venue_manager_1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800001001', '110101198001011001', NULL, NULL, NULL, 'VENUE_MANAGER', 1, 1, '2024-01-15 10:00:00', NULL, '2026-02-06 16:00:32');
INSERT INTO `sys_user` VALUES (3, 'venue_manager_2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800001002', '110101198002022002', NULL, NULL, NULL, 'VENUE_MANAGER', 2, 1, '2024-02-20 09:00:00', NULL, NULL);
INSERT INTO `sys_user` VALUES (4, 'venue_manager_3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800001003', '110101198003033003', NULL, NULL, NULL, 'VENUE_MANAGER', 3, 1, '2024-03-10 08:00:00', NULL, NULL);
INSERT INTO `sys_user` VALUES (5, 'venue_manager_4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800001004', '110101198004044004', NULL, NULL, NULL, 'VENUE_MANAGER', 4, 1, '2024-04-15 10:00:00', NULL, NULL);
INSERT INTO `sys_user` VALUES (6, 'venue_manager_5', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800001005', '110101198005055005', NULL, NULL, NULL, 'VENUE_MANAGER', 5, 1, '2024-05-20 09:00:00', NULL, NULL);
INSERT INTO `sys_user` VALUES (7, 'user001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13900001001', '110101199001011001', NULL, NULL, NULL, 'USER', NULL, 1, '2024-06-01 10:00:00', NULL, '2026-02-22 15:22:17');
INSERT INTO `sys_user` VALUES (8, 'user002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13900001002', '110101199002022002', NULL, NULL, NULL, 'USER', NULL, 1, '2024-06-02 10:00:00', NULL, NULL);
INSERT INTO `sys_user` VALUES (9, 'user003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13900001003', '110101199003033003', NULL, NULL, NULL, 'USER', NULL, 1, '2024-06-03 10:00:00', NULL, NULL);
INSERT INTO `sys_user` VALUES (10, 'user004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13900001004', '110101199004044004', NULL, NULL, NULL, 'USER', NULL, 1, '2024-06-04 10:00:00', NULL, NULL);
INSERT INTO `sys_user` VALUES (11, 'user005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13900001005', '110101199005055005', NULL, NULL, NULL, 'USER', NULL, 1, '2024-06-05 10:00:00', NULL, NULL);
INSERT INTO `sys_user` VALUES (12, 'testuser001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13900009999', '110101199001011234', NULL, NULL, NULL, 'USER', NULL, 1, '2025-01-20 10:00:00', NULL, NULL);
INSERT INTO `sys_user` VALUES (13, '涂家乐', '$2a$10$YuBJgc8W4VYOMQKkFdBBROblGiKzqFq1CH7ylDWADoz7xLacJYdDq', '17378406511', '511723200604093712', NULL, NULL, NULL, 'VENUE_MANAGER', 6, 1, '2026-02-06 16:47:32', '2026-02-06 16:47:32', '2026-02-22 15:22:28');
INSERT INTO `sys_user` VALUES (14, '黄依静', '$2a$10$9RSrXJXP6ixxoK6pH/0aZua2wGoa84/tHMbw3L9UQvl.XikfVsLPi', '', '', NULL, NULL, NULL, 'MEMBER', NULL, 1, '2026-02-22 15:23:48', '2026-02-22 15:36:58', '2026-02-22 15:40:42');

-- ----------------------------
-- Table structure for sys_user_setting
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_setting`;
CREATE TABLE `sys_user_setting`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??',
  `user_id` bigint NOT NULL COMMENT '??ID',
  `setting_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '???',
  `setting_value` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '???',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_setting`(`user_id` ASC, `setting_key` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `sys_user_setting_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 49 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '?????' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_setting
-- ----------------------------
INSERT INTO `sys_user_setting` VALUES (46, 1, 'loginAlert', 'true');
INSERT INTO `sys_user_setting` VALUES (47, 1, 'strangeDevice', 'true');
INSERT INTO `sys_user_setting` VALUES (48, 1, 'siteMessage', 'true');

-- ----------------------------
-- Table structure for sys_venue
-- ----------------------------
DROP TABLE IF EXISTS `sys_venue`;
CREATE TABLE `sys_venue`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '场馆ID',
  `venue_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '场馆名称',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人',
  `business_hours` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '营业时间（如：06:00-24:00）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '场馆描述',
  `venue_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '场馆图片URL',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度（预留，暂不使用）',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度（预留，暂不使用）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态（0-关闭，1-营业中，2-暂停）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_venue_name`(`venue_name` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '场馆表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_venue
-- ----------------------------
INSERT INTO `sys_venue` VALUES (1, '北京朝阳羽毛球馆', '北京市朝阳区建国路88号', '010-88888888', '张经理', '06:30-21:30', '位于朝阳区核心地段，交通便利，拥有8片标准场地，配备专业灯光和空调系统。', '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', NULL, NULL, 1, '2024-01-15 10:00:00', '2026-02-06 16:42:48');
INSERT INTO `sys_venue` VALUES (2, '上海浦东羽毛球中心', '上海市浦东新区世纪大道1000号', '021-66666666', '李经理', '07:00-23:00', '现代化羽毛球馆，设施齐全，提供专业教练服务，适合各年龄段球友。', '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', NULL, NULL, 1, '2024-02-20 09:00:00', NULL);
INSERT INTO `sys_venue` VALUES (3, '广州天河体育中心', '广州市天河区体育西路123号', '020-55555555', '王经理', '08:00-22:00', '大型综合性体育场馆，羽毛球场地宽敞明亮，定期举办各类赛事。', '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', NULL, NULL, 1, '2024-03-10 08:00:00', NULL);
INSERT INTO `sys_venue` VALUES (4, '深圳南山羽毛球馆', '深圳市南山区科技园南路456号', '0755-44444444', '赵经理', '06:00-24:00', '新装修的现代化场馆，配备最新设备，提供会员专属服务。', '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', NULL, NULL, 2, '2024-04-15 10:00:00', NULL);
INSERT INTO `sys_venue` VALUES (5, '杭州西湖羽毛球俱乐部', '杭州市西湖区文三路789号', '0571-33333333', '刘经理', '07:00-23:00', '环境优雅，设施完善，是杭州地区知名的羽毛球运动场所。', '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', NULL, NULL, 0, '2024-05-20 09:00:00', NULL);
INSERT INTO `sys_venue` VALUES (6, '开江县星锐羽毛球馆', '开江县东大街93号附1号', '17378406511', '涂家乐', '06:30-21:30', '开江县第一家专业羽毛球馆', '/api/uploads/venues/6abe8db1db464fe7bd7c5e244dbdb3fe.jpg', NULL, NULL, 1, '2026-02-06 16:46:44', '2026-02-06 16:46:44');

-- ----------------------------
-- Table structure for sys_venue_image
-- ----------------------------
DROP TABLE IF EXISTS `sys_venue_image`;
CREATE TABLE `sys_venue_image`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID（外键关联sys_venue）',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片URL',
  `image_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'DETAIL' COMMENT '图片类型（MAIN-主图，DETAIL-详情图）',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  INDEX `idx_image_type`(`image_type` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  CONSTRAINT `sys_venue_image_ibfk_1` FOREIGN KEY (`venue_id`) REFERENCES `sys_venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '场馆图片表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_venue_image
-- ----------------------------
INSERT INTO `sys_venue_image` VALUES (1, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-01-15 10:00:00');
INSERT INTO `sys_venue_image` VALUES (2, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 0, '2024-01-15 10:00:00');
INSERT INTO `sys_venue_image` VALUES (3, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 1, '2024-01-15 10:00:00');
INSERT INTO `sys_venue_image` VALUES (4, 1, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-01-15 10:00:00');
INSERT INTO `sys_venue_image` VALUES (5, 2, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-02-20 09:00:00');
INSERT INTO `sys_venue_image` VALUES (6, 2, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-02-20 09:00:00');
INSERT INTO `sys_venue_image` VALUES (7, 2, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 3, '2024-02-20 09:00:00');
INSERT INTO `sys_venue_image` VALUES (8, 3, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-03-10 08:00:00');
INSERT INTO `sys_venue_image` VALUES (9, 3, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-03-10 08:00:00');
INSERT INTO `sys_venue_image` VALUES (10, 3, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 3, '2024-03-10 08:00:00');
INSERT INTO `sys_venue_image` VALUES (11, 3, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 4, '2024-03-10 08:00:00');
INSERT INTO `sys_venue_image` VALUES (12, 3, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 5, '2024-03-10 08:00:00');
INSERT INTO `sys_venue_image` VALUES (13, 4, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-04-15 10:00:00');
INSERT INTO `sys_venue_image` VALUES (14, 4, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-04-15 10:00:00');
INSERT INTO `sys_venue_image` VALUES (15, 4, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 3, '2024-04-15 10:00:00');
INSERT INTO `sys_venue_image` VALUES (16, 5, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'MAIN', 1, '2024-05-20 09:00:00');
INSERT INTO `sys_venue_image` VALUES (17, 5, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 2, '2024-05-20 09:00:00');
INSERT INTO `sys_venue_image` VALUES (18, 5, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 3, '2024-05-20 09:00:00');
INSERT INTO `sys_venue_image` VALUES (19, 5, '/api/uploads/venues/e920b4906e7a40e591216cf720ee2091.png', 'DETAIL', 4, '2024-05-20 09:00:00');
INSERT INTO `sys_venue_image` VALUES (20, 6, '/api/uploads/venues/4b5e5c3ea519489c8b72ebc54a031341.jpg', 'DETAIL', 0, '2026-02-06 16:46:44');
INSERT INTO `sys_venue_image` VALUES (21, 6, '/api/uploads/venues/3a6b9f880f254528851c8eb51bf435a0.jpg', 'DETAIL', 1, '2026-02-06 16:46:44');
INSERT INTO `sys_venue_image` VALUES (22, 6, '/api/uploads/venues/fd475eccb70648059ff2b41eabf2da1b.jpg', 'DETAIL', 2, '2026-02-06 16:46:44');

-- ----------------------------
-- Table structure for sys_venue_schedule
-- ----------------------------
DROP TABLE IF EXISTS `sys_venue_schedule`;
CREATE TABLE `sys_venue_schedule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '时间表ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID（外键关联sys_venue）',
  `schedule_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '时间类型（WORKDAY-工作日，WEEKEND-周末，HOLIDAY-节假日）',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用（0-禁用，1-启用）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  INDEX `idx_schedule_type`(`schedule_type` ASC) USING BTREE,
  INDEX `idx_is_active`(`is_active` ASC) USING BTREE,
  CONSTRAINT `sys_venue_schedule_ibfk_1` FOREIGN KEY (`venue_id`) REFERENCES `sys_venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '场馆营业时间表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_venue_schedule
-- ----------------------------
INSERT INTO `sys_venue_schedule` VALUES (4, 2, 'WORKDAY', '07:00:00', '23:00:00', 1, '2024-02-20 09:00:00', NULL);
INSERT INTO `sys_venue_schedule` VALUES (5, 2, 'WEEKEND', '07:00:00', '23:00:00', 1, '2024-02-20 09:00:00', NULL);
INSERT INTO `sys_venue_schedule` VALUES (6, 2, 'HOLIDAY', '08:00:00', '22:00:00', 1, '2024-02-20 09:00:00', NULL);
INSERT INTO `sys_venue_schedule` VALUES (7, 3, 'WORKDAY', '08:00:00', '22:00:00', 1, '2024-03-10 08:00:00', NULL);
INSERT INTO `sys_venue_schedule` VALUES (8, 3, 'WEEKEND', '08:00:00', '22:00:00', 1, '2024-03-10 08:00:00', NULL);
INSERT INTO `sys_venue_schedule` VALUES (9, 3, 'HOLIDAY', '09:00:00', '21:00:00', 1, '2024-03-10 08:00:00', NULL);
INSERT INTO `sys_venue_schedule` VALUES (10, 4, 'WORKDAY', '06:00:00', '23:59:59', 1, '2024-04-15 10:00:00', NULL);
INSERT INTO `sys_venue_schedule` VALUES (11, 4, 'WEEKEND', '06:00:00', '23:59:59', 1, '2024-04-15 10:00:00', NULL);
INSERT INTO `sys_venue_schedule` VALUES (12, 4, 'HOLIDAY', '08:00:00', '22:00:00', 1, '2024-04-15 10:00:00', NULL);
INSERT INTO `sys_venue_schedule` VALUES (13, 5, 'WORKDAY', '07:00:00', '23:00:00', 1, '2024-05-20 09:00:00', NULL);
INSERT INTO `sys_venue_schedule` VALUES (14, 5, 'WEEKEND', '07:00:00', '23:00:00', 1, '2024-05-20 09:00:00', NULL);
INSERT INTO `sys_venue_schedule` VALUES (15, 5, 'HOLIDAY', '08:00:00', '22:00:00', 1, '2024-05-20 09:00:00', NULL);
INSERT INTO `sys_venue_schedule` VALUES (27, 1, 'WORKDAY', '06:30:00', '21:30:00', 1, '2026-02-06 16:42:49', '2026-02-06 16:42:49');
INSERT INTO `sys_venue_schedule` VALUES (28, 1, 'WEEKEND', '06:30:00', '21:30:00', 1, '2026-02-06 16:42:49', '2026-02-06 16:42:49');
INSERT INTO `sys_venue_schedule` VALUES (29, 1, 'HOLIDAY', '06:30:00', '21:30:00', 1, '2026-02-06 16:42:49', '2026-02-06 16:42:49');
INSERT INTO `sys_venue_schedule` VALUES (30, 6, 'WORKDAY', '06:30:00', '21:30:00', 1, '2026-02-06 16:46:44', '2026-02-06 16:46:44');
INSERT INTO `sys_venue_schedule` VALUES (31, 6, 'WEEKEND', '06:30:00', '21:30:00', 1, '2026-02-06 16:46:44', '2026-02-06 16:46:44');
INSERT INTO `sys_venue_schedule` VALUES (32, 6, 'HOLIDAY', '06:30:00', '21:30:00', 1, '2026-02-06 16:46:44', '2026-02-06 16:46:44');

-- ----------------------------
-- Table structure for sys_venue_status_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_venue_status_log`;
CREATE TABLE `sys_venue_status_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID（外键关联sys_venue）',
  `old_status` tinyint(1) NULL DEFAULT NULL COMMENT '原状态（0-关闭，1-营业中，2-暂停）',
  `new_status` tinyint(1) NOT NULL COMMENT '新状态（0-关闭，1-营业中，2-暂停）',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID（关联sys_user）',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人姓名',
  `change_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '变更原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  INDEX `idx_new_status`(`new_status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `sys_venue_status_log_ibfk_1` FOREIGN KEY (`venue_id`) REFERENCES `sys_venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '场馆状态变更日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_venue_status_log
-- ----------------------------
INSERT INTO `sys_venue_status_log` VALUES (1, 1, 1, 0, NULL, 'venue_manager_1', '状态变更：营业中 -> 关闭', '2026-02-06 16:42:43');
INSERT INTO `sys_venue_status_log` VALUES (2, 1, 0, 1, NULL, 'venue_manager_1', '状态变更：关闭 -> 营业中', '2026-02-06 16:42:48');

SET FOREIGN_KEY_CHECKS = 1;
