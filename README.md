# 🛒 悦选 AI 购物平台

SpringCloud 微服务全栈电商项目 — 广东海洋大学 信息系统综合实训

## 🏗 技术架构

```
┌─────────────────────────────────────────────┐
│                  前端层                       │
│  yuexuan-app (Vue3)    yuexuan-admin (Vue3) │
│  Vite + Axios + Router   Element Plus        │
└──────┬──────────────────────────┬───────────┘
       │ :9000                    │ :5173 (dev)
┌──────▼──────────────────────────▼───────────┐
│          Gateway 网关 (9000)                 │
│   路由转发 · JWT 鉴权 · Sentinel 流控       │
│   /yolo/** → ngrok → 算法层                  │
└──┬──────┬──────┬──────┬──────┬──────┬──────┘
   │      │      │      │      │      │
┌──▼──┐ ┌─▼──┐ ┌─▼──┐ ┌─▼──┐ ┌─▼──┐ ┌──────▼───┐
│Auth │ │User│ │Prod│ │Order│ │Pay │ │ YOLO     │
│8101 │ │8111│ │8081│ │8091│ │8121│ │ :5000    │
└─────┘ └────┘ └────┘ └────┘ └────┘ │ ngrok    │
   │      │      │      │      │    │ 公网穿透  │
┌──▼──────▼──────▼──────▼──────▼────┴──────────┐
│      Nacos (8848) · MySQL (3306)              │
│      Sentinel (8080)                          │
└──────────────────────────────────────────────┘
```

## 📦 项目结构

```
application/
├── shop-common/         公共模块（JWT、ResultData、UserContext）
├── shop-auth/           认证服务（登录/注册）          :8101
├── shop-user/           用户服务（收货地址）            :8111
├── shop-product-api/    商品 Feign 接口
├── shop-product-server/ 商品服务                       :8081
├── shop-order-api/      订单 Feign 接口
├── shop-order-server/   订单服务                       :8091
├── shop-payment/        支付服务                       :8121
├── shop-gateway/        网关（路由+鉴权+流控）          :9000
├── nacos-config-*.yaml  Nacos 配置中心文件
└── pom.xml              父工程（依赖版本统一管理）
frontend/
├── yuexuan-app/         用户端（Vue3 + Vite + Axios）
└── yuexuan-admin/       管理后台（Vue3 + Element Plus）
algorithm/
├── app.py               YOLO 推理服务（FastAPI :5000）
├── requirements.txt
└── models/              *.pt 模型文件（不入库）
scripts/
├── build.bat            Maven 构建脚本
└── start_services.bat   一键启动 6 个微服务
```

## 🚀 快速启动

### 环境要求

| 组件 | 版本 | 端口 |
|------|------|------|
| JDK | 17 | - |
| MySQL | 8.0+ | 3306 |
| Nacos | 2.x | 8848 |
| Sentinel | 1.8.9 | 8080 |
| Node.js | 18+ | - |
| Maven | 3.8+ | - |

### 1. 启动基础设施

```bash
# 启动 Nacos（单机模式）
startup.cmd -m standalone

# 启动 Sentinel Dashboard
java -jar sentinel-dashboard-1.8.9.jar

# 创建数据库
mysql> CREATE DATABASE yuexuan DEFAULT CHARSET utf8mb4;
```

### 2. 导入 Nacos 配置

将 `application/` 下的 `nacos-config-*.yaml` 导入 Nacos 控制台 (http://localhost:8848/nacos)。

### 3. 启动后端服务（按顺序）

```bash
cd application
mvn clean install -DskipTests

# 1. 基础服务
cd shop-auth          && mvn spring-boot:run
cd shop-user          && mvn spring-boot:run
cd shop-product-server && mvn spring-boot:run

# 2. 业务服务
cd shop-order-server  && mvn spring-boot:run
cd shop-payment       && mvn spring-boot:run

# 3. 网关（最后启动）
cd shop-gateway       && mvn spring-boot:run
```

### 4. 启动前端

```bash
# 用户端 (localhost:5173)
cd frontend/yuexuan-app
npm install && npm run dev

# 管理后台 (localhost:9528)
cd frontend/yuexuan-admin
npm install && npm run dev
```

## 🔗 API 路由

| 路径 | 目标服务 | 说明 |
|------|---------|------|
| `/a/login`, `/a/register` | auth-service | 登录/注册 |
| `/a/address/**` | user-service | 收货地址 |
| `/a/products/**`, `/admin/**` | product-service | 商品浏览/管理 |
| `/a/orders/**` | order-service | 下单/查单 |
| `/a/payment/**` | payment-service | 支付 |
| `/yolo/**` | YOLO 算法层 (ngrok) | AI 商品识别 |
| `/m/**` | order-service | 商家端 |
| `/platform/**` | order-service | 平台端 |

## 🤖 算法层对接（ngrok）

```bash
# 合作者机器上：
cd algorithm
pip install -r requirements.txt
python app.py                    # 启动推理服务 → :5000
ngrok http 5000                  # 暴露公网 → https://xxx.ngrok-free.app

# 应用层配置（二选一）：
# 方式 1: 环境变量
set YOLO_SERVICE_URL=https://xxx.ngrok-free.app

# 方式 2: 直接改 application.yml
uri: https://xxx.ngrok-free.app
```

## 🛠 技术栈

| 分类 | 技术 |
|------|------|
| 微服务框架 | SpringCloud 2021.0.5 + Alibaba 2021.0.5.0 |
| 注册/配置中心 | Nacos |
| 网关 | SpringCloud Gateway |
| 流控/熔断 | Sentinel |
| 远程调用 | OpenFeign |
| ORM | MyBatis Plus 3.5.3.1 |
| 数据库 | MySQL 8.0 |
| 前端 | Vue3 + Vite + Element Plus（管理后台） |
| 认证 | JWT + Gateway 全局过滤器 |
