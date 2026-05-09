# 图书管理系统 (Library Management System)

基于 Spring Boot 3 + Vue 3 + TypeScript 的全栈图书管理系统。

## 🛠 技术栈

### 前端
- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **语言**: TypeScript
- **状态管理**: Pinia
- **路由**: Vue Router
- **UI 组件库**: Element Plus
- **HTTP 客户端**: Axios

### 后端
- **框架**: Spring Boot 3.x
- **ORM**: MyBatis-Plus
- **数据库**: MySQL 8
- **缓存**: Redis
- **认证**: JWT + RBAC

## 🚀 快速开始

### 前置要求
- Docker Desktop (推荐) 或 Docker Engine + Docker Compose

### 启动项目
1. 在项目根目录下打开终端。
2. 运行以下命令构建并启动所有服务：
   ```powershell
   docker compose up --build
   ```
3. 等待容器启动完成。

### 访问地址
- **前端页面**: http://localhost:3000
- **后端接口**: http://localhost:8080
- **数据库管理**: 可通过 Docker 连接 `db` 容器，端口映射为 `3306`。

### 默认账号
- **管理员**:
  - 用户名: `admin`
  - 密码: `123456`

## 📂 项目结构

```
.
├── backend/            # 后端源码 (Spring Boot)
│   ├── sql/            # 数据库初始化脚本
│   └── src/            # Java 源码
├── frontend/           # 前端源码 (Vue 3)
│   ├── src/            # Vue 组件与逻辑
│   └── nginx.conf      # Nginx 配置文件
├── docker-compose.yml  # Docker 编排文件
└── README.md           # 项目文档
```

## ✨ 功能模块
- **用户认证**: 登录、JWT 令牌校验、RBAC 权限控制。
- **图书管理**: 图书的增删改查、库存管理。
- **借阅管理**: 借书、还书、逾期记录。
- **系统设置**: 用户管理、角色管理。

## 📝 开发指南
- **后端**: 修改 `backend` 目录下的代码，Docker 会自动重启（需配置热更，当前配置需重启容器）。
- **前端**: 修改 `frontend` 目录下的代码，Docker 会自动构建（生产环境模式）。建议本地运行 `npm run dev` 进行开发。

---
**注意**: 如果修改了 `init.sql`，请先执行 `docker compose down -v` 清除旧数据卷，再重新启动以应用新的种子数据。
