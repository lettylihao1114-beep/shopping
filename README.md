# 🛒 悦选 AI 购物平台

SpringCloud 微服务全栈电商项目 — 商品浏览 · 购物车 · 下单支付 · YOLO 识物 · DIN 智能推荐 · 知识图谱智能客服

> 广东海洋大学 信息系统综合实训 · 2人协作（[lettylihao1114-beep](https://github.com/lettylihao1114-beep) + K1taharaSetsuna）

---

## 🎯 功能亮点

| 模块 | 功能 | 说明 |
|------|------|------|
| 🏠 **首页** | 类目导航 · 轮播Banner · 用户卡片 · DIN 智能推荐入口 | 商品按类目筛选，未登录可浏览 |
| 🔍 **商品浏览** | 列表 · 详情 · 分类/关键词搜索 | 4图轮播展示，库存状态实时 |
| 🛒 **购物车** | 加入购物车 · 数量修改 · 批量结算 | 前端 Pinia store 管理 |
| 📝 **下单支付** | 订单确认 → 提交 → 支付 → 查单 | 完整订单生命周期 |
| 🤖 **AI 识物** | YOLOv8 商品目标检测 | 拍照/上传 → 识别商品类别 |
| 🧠 **DIN 推荐** | Deep Interest Network 个性化推荐 | 基于用户行为序列的 Attention 推荐 |
| 💬 **智能客服** | RAG4Pro 知识图谱问答 — Trie实体链接 + 意图识别 + Cypher模板引擎 | 查价格/库存、退换货、同类推荐、品牌对比、属性过滤 |
| 📦 **订单管理** | 我的订单 · 取消 · 确认收货 | C端完整流程 |
| 🛠 **管理后台** | 商品CRUD · 订单发货 · 数据看板 | 商家/管理员角色 |
| 🔐 **认证鉴权** | JWT 登录注册 · Gateway 全局过滤 · 角色权限 | user / shop / admin / platform |

---

## 🏗 技术架构

```
┌──────────────────────────────────────────────────────────────┐
│                        前端层                                 │
│  yuexuan-app (Vue3 + TS)          yuexuan-admin (Vue3 + JS)  │
│  Vite · Axios · Element Plus · Pinia · Hash Router            │
└────────────┬────────────────────────────┬────────────────────┘
             │ /api (Vite proxy)          │ /api (Vite proxy)
┌────────────▼────────────────────────────▼────────────────────┐
│                Gateway 网关 (9000)                             │
│    路由转发 · JWT 全局鉴权 · Sentinel 流控熔断                 │
└──────┬──────┬──────┬──────┬──────┬───────────────────────────┘
       │      │      │      │      │
┌──────▼──┐ ┌▼────┐ ┌▼────┐ ┌▼────┐ ┌▼───────┐
│  Auth   │ │User │ │Prod │ │Order│ │Payment │
│  :8101  │ │:8111│ │:8081│ │:8091│ │ :8121  │
│ 登录注册│ │地址 │ │商品  │ │订单  │ │ 支付   │
└────┬────┘ └──┬──┘ └──┬──┘ └──┬──┘ └───┬────┘
     └─────────┴───────┴───────┴─────────┘
                     │
┌────────────────────▼─────────────────────────────────────────┐
│          基础设施 & 算法层                                     │
│  Nacos (:8848) · Sentinel (:8080) · MySQL (:3306)             │
│  Neo4j (:7687) · YOLO 推理 (:5000) · DIN 推荐 (:5001)                         │
└──────────────────────────────────────────────────────────────┘
```

---

## 📦 项目结构

```
shopping/
├── day08/                          # SpringCloud 微服务后端
│   ├── shop-common/                # 公共模块（JWT、ResultData、UserContext）
│   ├── shop-auth/                  # 认证服务 :8101
│   ├── shop-user/                  # 用户服务（收货地址）:8111
│   ├── shop-product-api/           # 商品 Feign 接口定义
│   ├── shop-product-server/        # 商品服务 :8081
│   ├── shop-order-api/             # 订单 Feign 接口定义
│   ├── shop-order-server/          # 订单服务 :8091
│   ├── shop-payment/               # 支付服务 :8121
│   ├── shop-qa-service/            # 智能客服 RAG4Pro（Neo4j + Trie + 意图 + Cypher模板）:8131  ← new
│   │   └── qa/engine/
│   │       ├── Trie.java             # Trie树 品牌/类目实体匹配
│   │       ├── EntityLinker.java     # 实体链接器（品牌→类目→属性映射）
│   │       ├── IntentRecognizer.java # 意图识别（query_attr / relation / compare / filter）
│   │       ├── CypherTemplates.java  # Cypher模板库（4类意图 10条模板）
│   │       └── QueryEngine.java      # 问答引擎（意图→实体→模板→Cypher→格式化）
│   │       ├── service/
│   │       ├── KnowledgeGraphService.java  # 知识图谱问答服务
│   │       └── GraphBuilderService.java    # MySQL → Neo4j 图谱构建器
│   │       └── resources/data/
│   │           └── ChineseEcomQA.jsonl     # 中文电商QA数据集（1800条）
│   ├── shop-gateway/               # 网关（路由 + JWT鉴权 + 流控）:9000
│   ├── nacos-config-*.yaml         # Nacos 配置中心文件
│   ├── init.sql                    # 数据库初始化脚本
│   └── pom.xml                     # 父工程（SpringBoot 2.6.13 + Cloud 2021.0.5）
│
├── algorithm/                      # YOLO 商品识别推理服务
│   ├── app.py                      # FastAPI 推理服务 (:5000)
│   ├── requirements.txt            # ultralytics, fastapi, opencv-python...
│   └── models/                     # 模型文件（需自行放入）
│       ├── yolov8n.pt              # YOLOv8 nano 通用检测
│       └── best.pt                 # 零食/商品定制模型
│
├── din/                            # DIN 深度兴趣网络推荐
│   ├── din_model.py                # DIN 模型（Attention Pooling + Dice）
│   ├── train.py                    # 模型训练脚本
│   ├── preprocess.py               # 数据预处理
│   ├── eda.py / eda_deep.py       # 数据分析
│   ├── recommend_server.py         # Flask 推荐 API (:5001)
│   ├── combined_server.py          # YOLO + DIN 合并服务  ← new
│   ├── test_flask.py               # API 测试
│   └── test_recommend.py           # 推荐效果测试
│
├── frontend/
│   ├── yuexuan-app/                # 用户端 (Vue3 + TypeScript + Element Plus)
│   │   └── src/views/
│   │       ├── HomePage.vue        # 首页（类目+轮播+推荐入口）
│   │       ├── ProductDetail.vue   # 商品详情
│   │       ├── Cart.vue            # 购物车
│   │       ├── ConfirmOrder.vue    # 确认订单
│   │       ├── PayResult.vue       # 支付结果
│   │       ├── Orders.vue          # 我的订单
│   │       ├── Recognize.vue       # AI 拍照识物
│   │       ├── ServiceView.vue     # 智能客服聊天  ← new
│   │       ├── Dashboard.vue       # 管理看板
│   │       ├── ProductManage.vue   # 商品管理
│   │       └── OrderManage.vue     # 订单管理
│   │
│   └── yuexuan-admin/              # 独立管理后台 (Vue3 + JS + Element Plus)
│       └── src/views/
│           ├── dashboard/          # 数据看板
│           ├── product/            # 商品管理
│           ├── order/              # 订单管理
│           └── login/              # 登录页
│
├── build_day08.bat                 # Maven 一键构建脚本
├── start_services.bat              # 6 服务一键启动脚本 (含健康检查)
└── .gitignore
```

---

## 🚀 快速启动

### 环境要求

| 组件 | 版本 | 端口 | 说明 |
|------|------|------|------|
| JDK | 17 | — | 后端编译运行 |
| Maven | 3.8+ | — | 后端构建 |
| MySQL | 8.0+ | 3306 | 数据库 |
| Neo4j | 4.x+ | 7687 | 知识图谱（智能客服） |
| Nacos | 2.x | 8848 | 注册 + 配置中心 |
| Sentinel | 1.8.9 | 8080 | 流控 Dashboard |
| Node.js | 18+ | — | 前端 |
| Python | 3.10+ | — | 算法服务 (YOLO + DIN) |
| PyTorch | 2.x | — | DIN 模型推理 |

### 1. 初始化数据库

```bash
mysql -u root -p < day08/init.sql
```

### 2. 初始化 Neo4j（智能客服）

```bash
# 启动 Neo4j 后，浏览器打开 http://localhost:7474
# 导入知识图谱数据（QA对、商品关系等）
```

### 3. 启动基础设施

```bash
# Nacos（单机模式）
startup.cmd -m standalone

# Sentinel Dashboard
java -jar sentinel-dashboard-1.8.9.jar
```

### 4. 导入 Nacos 配置

将 `day08/` 下的 `nacos-config-*.yaml` 文件导入 Nacos 控制台 (http://localhost:8848/nacos)。

### 5. 构建 & 启动后端

```bash
# 一键构建
build_day08.bat

# 一键启动 6 个服务 + 健康检查
start_services.bat
```

或手动按顺序启动：

```bash
cd day08
mvn clean install -DskipTests

# 启动顺序：基础服务 → 业务服务 → 网关
# 1) 基础服务
cd shop-auth && mvn spring-boot:run          # :8101
cd shop-user && mvn spring-boot:run          # :8111
cd shop-product-server && mvn spring-boot:run # :8081
cd shop-qa-service && mvn spring-boot:run    # :8131

# 2) 业务服务
cd shop-order-server && mvn spring-boot:run  # :8091
cd shop-payment && mvn spring-boot:run       # :8121

# 3) 网关（最后启动）
cd shop-gateway && mvn spring-boot:run       # :9000
```

### 6. 启动算法服务（可选）

```bash
# YOLO 商品识别 (:5000)
cd algorithm
pip install -r requirements.txt
mkdir models  # 放入 yolov8n.pt 和 best.pt
python app.py

# 公网暴露（前端 Recognize 需要）
ngrok http 5000
```

```bash
# DIN 推荐服务 (:5001)
cd din
python recommend_server.py

# 或使用合并服务（YOLO + DIN 合一）
python combined_server.py
```

### 7. 启动前端

```bash
# 用户端 (localhost:5173)
cd frontend/yuexuan-app
npm install && npm run dev

# 管理后台 (localhost:9528)
cd frontend/yuexuan-admin
npm install && npm run dev
```

---

## 🔗 API 路由表

| 前缀 | 目标服务 | 示例 | 说明 |
|------|---------|------|------|
| `/a/login`, `/a/register` | auth-service | `POST /a/login` | 登录/注册 |
| `/a/current` | auth-service | `GET /a/current` | 获取当前用户 |
| `/a/address/**` | user-service | `GET /a/address` | 收货地址 CRUD |
| `/a/products/**` | product-service | `GET /a/products?category=数码` | 商品浏览 |
| `/admin/**` | product-service | `POST /admin/products` | 商品管理 (商家) |
| `/a/orders/**` | order-service | `POST /a/orders/submit` | 下单/查单/取消 |
| `/a/payment/**` | payment-service | `POST /a/payment/pay` | 支付 |
| `/m/**` | order-service | `POST /m/orders/{id}/delivery` | 商家发货 |
| `/platform/**` | order-service | `GET /platform/dashboard` | 平台管理 |
| `/qa/**` | qa-service (:8131) | `GET /qa/ask?q=小米多少钱` | 智能客服 |
| `POST /recommend` | DIN server (:5001) | 见下方 | 智能推荐 |
| `POST /detect` | YOLO server (:5000) | 见下方 | AI 商品识别 |

### 智能客服 API

**问答** — `GET /qa/ask?q=小米多少钱`

匹配优先级：QA知识库 → 商品信息 → 兜底回复

**按意图查询** — `GET /qa/intent/{intent}` （price / stock / return / shipping）

**同类推荐** — `GET /qa/similar/{pid}`

### 算法 API

**YOLO 识别** — `POST /detect` (algorithm/app.py)

| 参数 | 类型 | 说明 |
|------|------|------|
| `file` | multipart | 上传的图片 |
| `model` | query | `yolov8n`（通用）或 `snack`（零食定制），默认 yolov8n |
| `conf` | query | 置信度阈值，默认 0.25 |

**DIN 推荐** — `POST /recommend` (din/recommend_server.py)

```json
// 请求
{ "username": "admin" }

// 响应
{
  "user_id": 98047837,
  "username": "admin",
  "recommendations": [
    { "item_id": 1234, "category": 3, "score": 0.95 }
  ]
}
```

---

## 🛠 技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 微服务框架 | SpringCloud + SpringCloud Alibaba | 2021.0.5 |
| Spring Boot | 父工程统一管理 | 2.6.13 |
| 注册/配置中心 | Nacos | 2.x |
| 网关 | SpringCloud Gateway | — |
| 流控/熔断 | Sentinel | 1.8.9 |
| 远程调用 | OpenFeign + LoadBalancer | — |
| ORM | MyBatis Plus | 3.5.3.1 |
| 数据库 | MySQL | 8.0 |
| 图数据库 | Neo4j (知识图谱智能客服) | 4.x |
| 认证 | JWT (jjwt 0.9.1) + Gateway 全局过滤器 | — |
| 前端 | Vue3 · Vite · Element Plus · Pinia | — |
| 目标检测 | YOLOv8 (ultralytics) · OpenCV | — |
| 推荐算法 | DIN (Deep Interest Network) · PyTorch | — |
| 内网穿透 | ngrok | — |

---

## 👥 角色权限

| 角色 | 权限 |
|------|------|
| `user` | 浏览商品 · 购物车 · 下单支付 · 查看订单 · AI识物 · 智能推荐 |
| `shop` | 商家 — 管理商品 · 发货 · 查看订单 |
| `admin` | 管理员 — 商品管理 · 订单管理 · 数据看板 |
| `platform` | 平台 — 全部权限 |

---

## 🧪 演示账号

| 账号 | 角色 | 说明 |
|------|------|------|
| `admin` | admin | 管理员（同时也是 DIN 推荐 demo 用户） |
| `dafei` | user | 普通用户（DIN 推荐 demo） |
| `shoe_shop` | shop | 运动鞋商家 |
| `cloth_shop` | shop | 服装商家 |

---

## 📝 项目笔记

- **分支策略**: `main` (稳定) · `feature/application` (后端) · `feature/algorithm` (算法+QA)
- **后端 10 模块** + **前端 2 端** + **算法 3 服务** = 完整全栈闭环
- YOLO 识别通过 ngrok 暴露公网 → 前端 `Recognize.vue` 调用
- DIN 推荐在首页左侧导航栏底部入口，根据登录用户返回个性化推荐
- 智能客服升级为 **RAG4Pro** 架构：Trie实体链接 → 意图识别（4类意图 + 关键词权重） → Cypher模板引擎（10条模板） → QueryEngine 四阶段流水线，支持 1800 条中文电商 QA 数据集 + Neo4j 知识图谱查询 + 同类推荐三层策略
- `combined_server.py` 合并了 YOLO + DIN 两个服务，方便一键部署
- Gateway 统一入口 `:9000`，前端 Vite proxy `/api` → Gateway
- 订单流程: 确认(ConfirmToken预占库存) → 提交 → 支付 → 发货 → 收货
