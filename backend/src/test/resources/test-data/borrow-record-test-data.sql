-- Create tables for H2 database

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS borrow_record;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(100) NOT NULL,
  nickname VARCHAR(50),
  email VARCHAR(100),
  phone VARCHAR(20),
  status TINYINT DEFAULT 1,
  deleted TINYINT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
CREATE TABLE sys_role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_code VARCHAR(50) NOT NULL,
  role_name VARCHAR(50) NOT NULL,
  description VARCHAR(200),
  deleted TINYINT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
CREATE TABLE sys_user_role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL
);

-- ----------------------------
-- Table structure for book
-- ----------------------------
CREATE TABLE book (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(100) NOT NULL,
  author VARCHAR(50) NOT NULL,
  isbn VARCHAR(20) NOT NULL,
  category VARCHAR(50),
  price DECIMAL(10,2),
  stock INT DEFAULT 0,
  description TEXT,
  cover VARCHAR(255),
  deleted TINYINT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------
-- Table structure for borrow_record
-- ----------------------------
CREATE TABLE borrow_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  borrow_date TIMESTAMP NOT NULL,
  return_date TIMESTAMP,
  due_date TIMESTAMP NOT NULL,
  status TINYINT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------
-- Insert test data
-- ----------------------------

-- Roles
INSERT INTO sys_role (id, role_code, role_name, description) VALUES 
(1, 'ROLE_ADMIN', '管理员', '系统管理员'),
(2, 'ROLE_USER', '普通用户', '普通借阅用户');

-- Users (password is '123456' with {noop} prefix
INSERT INTO sys_user (id, username, password, nickname, email, phone, status) VALUES 
(1, 'admin', '{noop}123456', '管理员', 'admin@library.com', '13800138000', 1),
(2, 'testuser', '{noop}123456', '测试用户', 'test@library.com', '13800138001', 1);

-- User roles
INSERT INTO sys_user_role (id, user_id, role_id) VALUES 
(1, 1, 1),
(2, 2, 2);

-- Books with different stock levels
INSERT INTO book (id, title, author, isbn, category, price, stock, description) VALUES 
(1, '三体', '刘慈欣', '9787536692930', '科幻', 23.00, 10, '科幻小说'),
(2, '活着', '余华', '9787506365437', '文学', 20.00, 5, '文学作品'),
(3, 'Java编程思想', 'Bruce Eckel', '9787111213826', '计算机', 108.00, 3, '编程书籍');

-- Borrow records (5 records with different statuses)
-- Status: 0 = BORROWED, 1 = RETURNED
INSERT INTO borrow_record (id, user_id, book_id, borrow_date, return_date, due_date, status) VALUES 
-- User 2 (testuser) borrowed book 1 - BORROWED
(1, 2, 1, TIMESTAMPADD(DAY, -5, CURRENT_TIMESTAMP), NULL, TIMESTAMPADD(DAY, 25, CURRENT_TIMESTAMP), 0),
-- User 2 borrowed book 2 - BORROWED
(2, 2, 2, TIMESTAMPADD(DAY, -3, CURRENT_TIMESTAMP), NULL, TIMESTAMPADD(DAY, 27, CURRENT_TIMESTAMP), 0),
-- User 2 returned book 3 - RETURNED  
(3, 2, 3, TIMESTAMPADD(DAY, -20, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -10, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, 10, CURRENT_TIMESTAMP), 1),
-- User 1 (admin) borrowed book 1 - BORROWED
(4, 1, 1, TIMESTAMPADD(DAY, -2, CURRENT_TIMESTAMP), NULL, TIMESTAMPADD(DAY, 28, CURRENT_TIMESTAMP), 0),
-- User 1 returned book 2 - RETURNED
(5, 1, 2, TIMESTAMPADD(DAY, -15, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -5, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, 15, CURRENT_TIMESTAMP), 1);
