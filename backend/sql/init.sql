SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS library_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE library_db;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
  `deleted` tinyint(4) DEFAULT 0 COMMENT '逻辑删除: 1-已删除, 0-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `deleted` tinyint(4) DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ----------------------------
-- Table structure for book
-- ----------------------------
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL COMMENT '书名',
  `author` varchar(50) NOT NULL COMMENT '作者',
  `isbn` varchar(20) NOT NULL COMMENT 'ISBN',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `price` decimal(10,2) DEFAULT NULL COMMENT '价格',
  `stock` int(11) DEFAULT 0 COMMENT '库存',
  `description` text COMMENT '简介',
  `cover` varchar(255) DEFAULT NULL COMMENT '封面图片URL',
  `deleted` tinyint(4) DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_isbn` (`isbn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- ----------------------------
-- Table structure for borrow_record
-- ----------------------------
DROP TABLE IF EXISTS `borrow_record`;
CREATE TABLE `borrow_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `book_id` bigint(20) NOT NULL COMMENT '图书ID',
  `borrow_date` datetime NOT NULL COMMENT '借阅日期',
  `return_date` datetime DEFAULT NULL COMMENT '归还日期',
  `due_date` datetime NOT NULL COMMENT '应还日期',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态: 0-借阅中, 1-已归还, 2-已逾期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- ----------------------------
-- Records
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'ROLE_ADMIN', '管理员', '系统管理员', 0, NOW(), NOW());
INSERT INTO `sys_role` VALUES (2, 'ROLE_USER', '普通用户', '普通借阅用户', 0, NOW(), NOW());

-- Password is '123456' with {noop} prefix for default Spring Security delegation
INSERT INTO `sys_user` VALUES (1, 'admin', '{noop}123456', '管理员', 'admin@library.com', '13800138000', 1, 0, NOW(), NOW());
INSERT INTO `sys_user` VALUES (2, 'testuser', '{noop}123456', '测试用户', 'test@library.com', '13800138001', 1, 0, NOW(), NOW());

INSERT INTO `sys_user_role` VALUES (1, 1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2, 2);

-- Initial Books
INSERT INTO `book` (title, author, isbn, category, price, stock, description) VALUES 
('三体', '刘慈欣', '9787536692930', '科幻', 23.00, 10, '文化大革命如火如荼进行的同时，军方探寻外星文明的绝秘计划“红岸工程”取得了突破性进展。'),
('活着', '余华', '9787506365437', '文学', 20.00, 5, '地主少爷福贵嗜赌成性，终于赌光了家业，一贫如洗。'),
('Java编程思想', 'Bruce Eckel', '9787111213826', '计算机技术', 108.00, 3, 'Java领域的圣经。'),
('百年孤独', '加西亚·马尔克斯', '9787544253994', '外国文学', 39.50, 8, '描写了布恩迪亚家族七代人的传奇故事。');
