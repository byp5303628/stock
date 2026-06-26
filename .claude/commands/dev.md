---
description: 启动本地开发环境。支持启动后端、前端、数据库或全部。用法：/dev [backend|frontend|db|all]
---

# Dev — 本地开发环境

启动本地开发环境的核心组件。自动检测项目结构，按需启动后端 Spring Boot 服务、前端 UmiJS 开发服务器、MySQL 数据库。

## 参数

```
/dev [backend|frontend|db|all]
```

| 参数 | 说明 |
|------|------|
| `backend` | 启动 Spring Boot 后端（默认端口 8080） |
| `frontend` | 启动前端开发服务器（默认端口 8000） |
| `db` | 检查 MySQL 数据库状态，如未启动则拉起 |
| `all` | 依次启动 db → backend → frontend（默认值） |

省略参数时默认 `all`。

## 执行流程

### Step 1: 检测环境

| 组件 | 检测方式 | 预期状态 |
|------|---------|---------|
| MySQL | `mysql --version` + `brew services list` | 运行中，端口 3306 |
| Java | `java -version` | >= 8 |
| Maven | `mvn --version` | 已安装 |
| Node.js | `node --version` | >= 12 |
| 数据库 | `mysql -u root -p123456 stock -e "SELECT 1"` | stock 库可达 |

### Step 2: 按参数启动

#### db — 启动数据库

```bash
# 检查 MySQL 状态
brew services list | grep mysql

# 如果未启动则拉起
brew services start mysql

# 验证连接
mysql -u root -p123456 -e "SELECT 'DB OK' AS status"
```

#### backend — 启动后端

```bash
# 编译并启动（test profile 连接本地数据库）
cd stock-web
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

#### frontend — 启动前端

```bash
cd stock-frontend
npm run start:no-mock
```

### Step 3: 健康检查

组件启动后执行健康检查，确认服务可用：

| 组件 | 健康检查 | 预期 |
|------|---------|------|
| Backend | `curl -s http://localhost:8080/api/stock-strategy/list.json` | 返回 JSON |
| Frontend | `curl -s -o /dev/null -w "%{http_code}" http://localhost:8000` | 200 |
| DB | `mysql -u root -p123456 stock -e "SHOW TABLES"` | 表列表非空 |

## 输出示例

```
✅ 开发环境已就绪！

📊 组件状态:
  - MySQL:    ✅ 运行中 (port 3306)
  - Backend:  ✅ 启动中 (port 8080, profile=test)
  - Frontend: ✅ 启动中 (port 8000)

📌 访问地址:
  后端 API: http://localhost:8080
  前端页面: http://localhost:8000
  数据库:   jdbc:mysql://localhost:3306/stock

💡 常用命令:
  停止后端:   Ctrl+C
  停止前端:   Ctrl+C
  停止 MySQL: brew services stop mysql
```
