CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `status` int DEFAULT 1,
  `deleted` int DEFAULT 0,
  `create_time` timestamp DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) NOT NULL,
  `role_name` varchar(50) NOT NULL,
  `description` varchar(200) DEFAULT NULL,
  `deleted` int DEFAULT 0,
  `create_time` timestamp DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `book` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `author` varchar(50) NOT NULL,
  `isbn` varchar(20) NOT NULL,
  `category` varchar(50) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `stock` int DEFAULT 0,
  `description` clob,
  `cover` varchar(255) DEFAULT NULL,
  `deleted` int DEFAULT 0,
  `create_time` timestamp DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `borrow_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `book_id` bigint NOT NULL,
  `borrow_date` timestamp NOT NULL,
  `return_date` timestamp DEFAULT NULL,
  `due_date` timestamp NOT NULL,
  `status` int DEFAULT 0,
  `create_time` timestamp DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

DELETE FROM `borrow_record`;
DELETE FROM `sys_user_role`;
DELETE FROM `sys_user`;
DELETE FROM `sys_role`;
DELETE FROM `book`;

ALTER TABLE `borrow_record` ALTER COLUMN `id` RESTART WITH 1;
ALTER TABLE `sys_user_role` ALTER COLUMN `id` RESTART WITH 1;
ALTER TABLE `sys_user` ALTER COLUMN `id` RESTART WITH 1;
ALTER TABLE `sys_role` ALTER COLUMN `id` RESTART WITH 1;
ALTER TABLE `book` ALTER COLUMN `id` RESTART WITH 1;

INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `deleted`, `create_time`, `update_time`) VALUES
  (1, 'ROLE_ADMIN', '管理员', '系统管理员', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 'ROLE_USER', '普通用户', '普通借阅用户', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`, `phone`, `status`, `deleted`, `create_time`, `update_time`) VALUES
  (1, 'admin', '$2a$10$encodedpassword', '管理员', 'admin@library.com', '13800138000', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 'testuser', '$2a$10$encodedpassword', '测试用户', 'test@library.com', '13800138001', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`) VALUES
  (1, 1, 1),
  (2, 2, 2);

INSERT INTO `book` (`id`, `title`, `author`, `isbn`, `category`, `price`, `stock`, `description`, `deleted`, `create_time`, `update_time`) VALUES
  (1, '三体', '刘慈欣', '9787536692930', '科幻', 23.00, 9, '文化大革命如火如荼进行的同时，军方探寻外星文明的绝秘计划', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, '活着', '余华', '9787506365437', '文学', 20.00, 4, '地主少爷福贵嗜赌成性，终于赌光了家业', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 'Java编程思想', 'Bruce Eckel', '9787111213826', '计算机技术', 108.00, 0, 'Java领域的圣经', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO `borrow_record` (`id`, `user_id`, `book_id`, `borrow_date`, `return_date`, `due_date`, `status`, `create_time`, `update_time`) VALUES
  (1, 2, 1, '2025-01-10 10:00:00', NULL, '2025-02-10 10:00:00', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 2, 2, '2025-01-05 14:30:00', '2025-01-20 09:00:00', '2025-02-05 14:30:00', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 1, 1, '2025-01-15 08:00:00', NULL, '2025-02-15 08:00:00', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, 1, 3, '2025-01-08 16:00:00', '2025-01-25 11:00:00', '2025-02-08 16:00:00', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, 2, 3, '2024-12-20 10:00:00', NULL, '2025-01-20 10:00:00', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
