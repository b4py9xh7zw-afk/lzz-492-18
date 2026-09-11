SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `scaffolding_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `scaffolding_db`;

-- 文件信息表
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) DEFAULT '0' COMMENT '文件大小（字节）',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `file_extension` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
  `upload_user_id` bigint(20) DEFAULT NULL COMMENT '上传人ID',
  `upload_user_name` varchar(50) DEFAULT NULL COMMENT '上传人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_upload_user_id` (`upload_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';

-- 工作管理表
DROP TABLE IF EXISTS `work`;
CREATE TABLE `work` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_name` varchar(100) NOT NULL COMMENT '工作名称',
  `work_content` text COMMENT '工作内容',
  `work_status` varchar(20) DEFAULT 'pending' COMMENT '工作状态（pending-待处理，in_progress-进行中，completed-已完成，cancelled-已取消）',
  `work_time` datetime DEFAULT NULL COMMENT '工作时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `priority` varchar(20) DEFAULT 'normal' COMMENT '优先级（low-低，normal-普通，high-高，urgent-紧急）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_work_status` (`work_status`),
  KEY `idx_work_time` (`work_time`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作管理表';

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名（账号）',
  `password` varchar(100) NOT NULL COMMENT '密码（不加密）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认admin账号
INSERT INTO `user` (`username`, `password`, `nickname`) VALUES ('admin', '123456', '管理员');

SET FOREIGN_KEY_CHECKS = 1;

-- ==================== 高温作业保障模块 ====================

-- 高温保障策略表（全局配置，启用一条）
DROP TABLE IF EXISTS `heat_policy`;
CREATE TABLE `heat_policy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `temp_threshold` decimal(4,1) NOT NULL DEFAULT '35.0' COMMENT '高温阈值（℃），室外岗位气温达到即触发保障',
  `allowance_per_day` decimal(10,2) NOT NULL DEFAULT '15.00' COMMENT '高温津贴标准（元/人/天）',
  `rest_frequency` varchar(200) DEFAULT '每2小时休息15分钟，避开12:00-15:00高温时段' COMMENT '休息频次说明',
  `salt_pill_qty` int(11) DEFAULT '2' COMMENT '盐丸发放量（粒/人/天）',
  `ice_sleeve_qty` int(11) DEFAULT '1' COMMENT '冰袖发放量（副/人/天）',
  `water_voucher_qty` int(11) DEFAULT '2' COMMENT '饮水券发放量（张/人/天）',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用（1-启用，0-停用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='高温保障策略表';

-- 排班表
DROP TABLE IF EXISTS `schedule`;
CREATE TABLE `schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `worker_name` varchar(50) NOT NULL COMMENT '工人姓名',
  `position` varchar(100) NOT NULL COMMENT '岗位名称',
  `work_env` varchar(20) NOT NULL DEFAULT 'outdoor' COMMENT '作业环境（outdoor-室外，indoor-室内）',
  `work_date` date NOT NULL COMMENT '排班日期',
  `shift_name` varchar(20) DEFAULT '白班' COMMENT '班次（白班/夜班等）',
  `temperature` decimal(4,1) DEFAULT NULL COMMENT '当日气温（℃）',
  `work_hours` decimal(5,2) DEFAULT '8.00' COMMENT '工时（小时）',
  `hourly_wage` decimal(10,2) DEFAULT '25.00' COMMENT '时薪（元/小时）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_worker_name` (`worker_name`),
  KEY `idx_work_date` (`work_date`),
  KEY `idx_work_env` (`work_env`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班表';

-- 防暑物资发放记录表
DROP TABLE IF EXISTS `supply_distribution`;
CREATE TABLE `supply_distribution` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `worker_name` varchar(50) NOT NULL COMMENT '工人姓名',
  `schedule_id` bigint(20) DEFAULT NULL COMMENT '关联排班ID',
  `distribute_date` date NOT NULL COMMENT '发放日期',
  `salt_pill_qty` int(11) DEFAULT '0' COMMENT '盐丸数量（粒）',
  `ice_sleeve_qty` int(11) DEFAULT '0' COMMENT '冰袖数量（副）',
  `water_voucher_qty` int(11) DEFAULT '0' COMMENT '饮水券数量（张）',
  `status` varchar(20) DEFAULT 'pending' COMMENT '发放状态（pending-待发放，issued-已发放）',
  `keeper_name` varchar(50) DEFAULT NULL COMMENT '仓管确认人',
  `confirm_time` datetime DEFAULT NULL COMMENT '确认时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_worker_name` (`worker_name`),
  KEY `idx_status` (`status`),
  KEY `idx_distribute_date` (`distribute_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='防暑物资发放记录表';

-- 结算单表（津贴与普通工时分列）
DROP TABLE IF EXISTS `settlement`;
CREATE TABLE `settlement` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `worker_name` varchar(50) NOT NULL COMMENT '工人姓名',
  `period` varchar(10) NOT NULL COMMENT '结算周期（yyyy-MM）',
  `normal_hours` decimal(8,2) DEFAULT '0.00' COMMENT '普通工时（小时）',
  `hourly_wage` decimal(10,2) DEFAULT '0.00' COMMENT '时薪（元/小时）',
  `normal_amount` decimal(12,2) DEFAULT '0.00' COMMENT '普通工时工资（元）',
  `heat_days` int(11) DEFAULT '0' COMMENT '高温津贴天数（天）',
  `allowance_per_day` decimal(10,2) DEFAULT '0.00' COMMENT '津贴标准（元/天）',
  `allowance_amount` decimal(12,2) DEFAULT '0.00' COMMENT '高温津贴合计（元）',
  `total_amount` decimal(12,2) DEFAULT '0.00' COMMENT '应发合计（元）',
  `status` varchar(20) DEFAULT 'pending' COMMENT '结算状态（pending-待结算，settled-已结算）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_worker_period` (`worker_name`, `period`),
  KEY `idx_period` (`period`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算单表';

-- 默认高温策略
INSERT INTO `heat_policy` (`temp_threshold`, `allowance_per_day`, `rest_frequency`, `salt_pill_qty`, `ice_sleeve_qty`, `water_voucher_qty`, `enabled`)
VALUES (35.0, 15.00, '每2小时休息15分钟，避开12:00-15:00高温时段', 2, 1, 2, 1);

-- 示例排班数据（2026-09，含高温室外岗位）
INSERT INTO `schedule` (`worker_name`, `position`, `work_env`, `work_date`, `shift_name`, `temperature`, `work_hours`, `hourly_wage`) VALUES
('张三', '钢筋工', 'outdoor', '2026-09-08', '白班', 36.5, 8.00, 30.00),
('张三', '钢筋工', 'outdoor', '2026-09-09', '白班', 37.0, 8.00, 30.00),
('张三', '钢筋工', 'outdoor', '2026-09-10', '白班', 34.0, 8.00, 30.00),
('李四', '塔吊司机', 'outdoor', '2026-09-09', '白班', 37.0, 8.00, 35.00),
('李四', '塔吊司机', 'outdoor', '2026-09-10', '白班', 34.0, 8.00, 35.00),
('王五', '仓库管理员', 'indoor', '2026-09-09', '白班', 37.0, 8.00, 25.00);

-- 示例物资发放记录（与高温排班对应）
INSERT INTO `supply_distribution` (`worker_name`, `schedule_id`, `distribute_date`, `salt_pill_qty`, `ice_sleeve_qty`, `water_voucher_qty`, `status`, `keeper_name`, `confirm_time`) VALUES
('张三', 1, '2026-09-08', 2, 1, 2, 'issued', '王五', '2026-09-08 07:30:00'),
('张三', 2, '2026-09-09', 2, 1, 2, 'issued', '王五', '2026-09-09 07:30:00'),
('李四', 4, '2026-09-09', 2, 1, 2, 'pending', NULL, NULL);
