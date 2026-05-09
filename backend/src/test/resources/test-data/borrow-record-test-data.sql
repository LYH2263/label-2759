-- Drop tables if they exist
DROP TABLE IF EXISTS borrow_record;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;

-- Create tables for H2 database
CREATE TABLE sys_role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_code VARCHAR(50) NOT NULL,
  role_name VARCHAR(50) NOT NULL,
  description VARCHAR(200),
  deleted TINYINT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_username UNIQUE (username)
);

CREATE TABLE sys_user_role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

CREATE TABLE book (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(100) NOT NULL,
  author VARCHAR(50) NOT NULL,
  isbn VARCHAR(20) NOT NULL,
  category VARCHAR(50),
  price DECIMAL(10,2),
  stock INT DEFAULT 0,
  description CLOB,
  cover VARCHAR(255),
  deleted TINYINT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_isbn UNIQUE (isbn)
);

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

-- Insert roles
INSERT INTO sys_role (id, role_code, role_name, description) VALUES (1, 'ROLE_ADMIN', '管理员', '系统管理员');
INSERT INTO sys_role (id, role_code, role_name, description) VALUES (2, 'ROLE_USER', '普通用户', '普通借阅用户');

-- Insert users: 1 admin, 1 regular user
INSERT INTO sys_user (id, username, password, nickname, email, phone, status) 
VALUES (1, 'admin', '{noop}123456', '管理员', 'admin@library.com', '13800138000', 1);

INSERT INTO sys_user (id, username, password, nickname, email, phone, status) 
VALUES (2, 'testuser', '{noop}123456', '测试用户', 'test@library.com', '13800138001', 1);

-- Assign roles
INSERT INTO sys_user_role (id, user_id, role_id) VALUES (1, 1, 1);
INSERT INTO sys_user_role (id, user_id, role_id) VALUES (2, 2, 2);

-- Insert 3 books with different stock levels
INSERT INTO book (id, title, author, isbn, category, price, stock, description) 
VALUES (1, '三体', '刘慈欣', '9787536692930', '科幻', 23.00, 10, '科幻小说');

INSERT INTO book (id, title, author, isbn, category, price, stock, description) 
VALUES (2, '活着', '余华', '9787506365437', '文学', 20.00, 5, '文学作品');

INSERT INTO book (id, title, author, isbn, category, price, stock, description) 
VALUES (3, 'Java编程思想', 'Bruce Eckel', '9787111213826', '计算机技术', 108.00, 3, '技术书籍');

-- Insert 5 borrow records with different statuses (BORROWED=0, RETURNED=1)
-- Admin has borrowed book 1 and 2 (one borrowed, one returned)
INSERT INTO borrow_record (id, user_id, book_id, borrow_date, return_date, due_date, status) 
VALUES (1, 1, 1, DATEADD('DAY', -5, CURRENT_TIMESTAMP), NULL, DATEADD('DAY', 25, CURRENT_TIMESTAMP), 0);

INSERT INTO borrow_record (id, user_id, book_id, borrow_date, return_date, due_date, status) 
VALUES (2, 1, 2, DATEADD('DAY', -20, CURRENT_TIMESTAMP), DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('DAY', 10, CURRENT_TIMESTAMP), 1);

-- Regular user has borrowed book 2, 3, and 1 (two borrowed, one returned)
INSERT INTO borrow_record (id, user_id, book_id, borrow_date, return_date, due_date, status) 
VALUES (3, 2, 2, DATEADD('DAY', -3, CURRENT_TIMESTAMP), NULL, DATEADD('DAY', 27, CURRENT_TIMESTAMP), 0);

INSERT INTO borrow_record (id, user_id, book_id, borrow_date, return_date, due_date, status) 
VALUES (4, 2, 3, DATEADD('DAY', -15, CURRENT_TIMESTAMP), NULL, DATEADD('DAY', 15, CURRENT_TIMESTAMP), 0);

INSERT INTO borrow_record (id, user_id, book_id, borrow_date, return_date, due_date, status) 
VALUES (5, 2, 1, DATEADD('DAY', -30, CURRENT_TIMESTAMP), DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('DAY', 0, CURRENT_TIMESTAMP), 1);
