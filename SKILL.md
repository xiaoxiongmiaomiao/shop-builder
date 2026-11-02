---
name: shop-builder
description: This skill should be used when developing the 一键店铺小程序 (one-click shop WeChat mini-program) full-stack project — including the WeChat mini-program (Taro/Weapp + TypeScript/JavaScript), the Java Spring Boot backend (Spring Boot 4.1 / MyBatis-Plus / MySQL 8 / Redis 7), and the Vue3 + Vben 5.0 admin frontend — or when following the shop-builder multi-agent development workflow and conventions defined by product manager and acceptance owner 小青, mini-program engineer 小张, backend engineer 小王, and frontend engineer 小刘.
agent_created: true
version: 1.0.0
author: 深圳市青九风网络科技有限公司
license: MIT
tags:
  - 微信小程序
  - 电商
  - 全栈开发
  - 开发规范
---

# 店铺生成器 · 一键店铺小程序全栈开发规范

> 品牌标识：`assets/logo/shop-builder-logo.png`（科技蓝紫渐变 · 立体店招 + 屋顶闪电，寓意「一键即达，开店而生」）。

## 目的
将一份完整的「一键店铺小程序」全栈开发手册转化为可复用的开发规范，使 WorkBuddy 在开发该项目的微信小程序、Java 后端、Vue 管理后台时，自动遵循统一的架构、接口约定、编码规范与多智能体协作流程。

## 适用场景
- 新建或迭代该项目的微信小程序、Java 后端、Vue 管理后台任一子系统；
- 需要按「小青（需求/统筹/验收/发布）→ 小张（小程序）→ 小王（后端）→ 小刘（管理前端）」串行推进；
- 需要遵循统一的返回结构、单号规则、请求封装、数据库与编码约定。

## 使用方式
1. **先加载完整规范**：`references/manual.md` 是权威来源，包含行为准则、四类开发角色（产品经理小青负责统筹、验收与发布）、小程序端全部页面与数据结构、后端全部表结构与接口清单、管理后台规范。执行具体任务前先阅读对应章节。后端 Java 实现的全局编码约束另见 `references/java-action-rules.md`（与小王规范互补，优先以之为准）。
2. **按角色分工推进**：用户直接说「帮我生成小程序」时，按完整流程开发（小程序 + 后端 + 管理前端），可交由三个不同 Agent 分别执行——小张（小程序）、小王（后端接口）、小刘（前端页面），由小青（产品）统筹，三端完成后由小青验收并发布。
3. **一键脚手架**：用户说「帮我生成小程序工程」「初始化项目骨架」或类似需求时，运行本 skill 内置脚本快速落地初始结构：
   ```bash
   python scripts/scaffold_shop_builder.py --target <输出目录> --mini-framework taro   # 或 weapp
   ```
   脚本生成三类可运行骨架：后端 `ai-shop-system`（Spring Boot + MyBatis-Plus，含 BaseEntity / OrderUtils / 示例 SysUser / 多环境 yml）、小程序 `shop-mini`（Taro 或 Weapp，含 `requestUtils` 统一请求封装与四个 TabBar 页面）、管理后台 `shop-admin`（Vben 5.0 目录与模块接口/页面占位）。支持 `--no-backend` / `--no-admin` / `--no-mini` 选择性生成。
4. **严格遵守行为准则**（详见 manual.md 第一节）：行动前先思考、先读现有文件；输出简洁但推理充分；优先编辑现有代码而非重写；变更后必须验证再宣称完成；用户指令优先于本规范。

## AI 按量付费改造（402 / Machine Pay）

用户提出「接入支付宝 AI 付 / 按量付费 / 402 收款 / AI 智能体调用我的 API 收费」等诉求时，触发本能力。即为**一键店铺**后端**只读查询类接口**接入支付宝 AI 按量付费，让 AI 智能体按次付费调用 API。

### probe → pay → complete → ack 生命周期
| 阶段 | 服务端动作 | 协议要素 |
| --- | --- | --- |
| **probe**（探测） | 无 `Payment-Proof` 时返回未支付信号 | `HTTP 402` + `Payment-Needed` 头（Base64URL 账单，含 `protocol` / `method` 层） |
| **pay**（支付） | 智能体凭账单拉起支付，服务端不参与 | 沙箱 `service_id=api_mock_service_id`；生产用服务市场真实 `serviceId` |
| **complete**（完成） | 携带 `Payment-Proof` 重试 → 验付 → 交付资源 | 调 `alipay.aipay.agent.payment.verify`，校验 `active=true`、金额/`out_trade_no`/`resource_id` 一致后返回 `200` + `Payment-Validation` |
| **ack**（回执） | 调用履约回执并标记订单完成 | `alipay.aipay.agent.fulfillment.confirm`；失败允许同一凭证重试 ack |

### 支付依赖包
```xml
<dependency>
    <groupId>com.alipay.sdk</groupId>
    <artifactId>alipay-sdk-java</artifactId>
    <version>4.40.0.ALL</version>
</dependency>
```

### 落地资产
- 规范：`references/a2m-pay-integration.md`
- 模板代码：`assets/a2m/AlipayA2mConfig.java`、`assets/a2m/A2MPaymentController.java`
- 一键生成：`python scripts/scaffold_shop_builder.py --target <dir> --with-a2m`（写入后端 `ai-shop-system` 并自动补依赖）
- 网关：沙箱 `https://openapi-sandbox.dl.alipaydev.com/gateway.do`；生产 `https://openapi.alipay.com/gateway.do`；签名 `RSA2`（Java 私钥用 PKCS#8）

## Java 开发行动规则（后端实现全局约束）

开发 `ai-shop-system` 后端时，除遵循 manual.md 中小王（后端工程师）的规范外，**必须**遵守 `references/java-action-rules.md` 中的全局实现约束（共 50+ 条，执行后端任务前建议先加载阅读）。要点速览：

- 禁止 `@RequestMapping`，必须用 `@PostMapping/@GetMapping/...` 写全路径且前缀加 `/private`；Controller 收参禁止 `Map`/`HttpRequest`，返回禁止直接 `Entity`（须转 VO）；
- 入参超过 2 个字段必须用 DTO（继承 `BaseDTO`，字段注释含含义与取值范围）；状态/类型字段强制枚举类；
- 批量操作一律批处理，禁止 `for/while/do-while` 操作数据库；ServiceImpl 只注入 Service 接口、禁止注入 Mapper；
- 注入用 `@RequiredArgsConstructor` + `private final`，禁止 `@Autowired/@Resource`；
- 变更类方法必须 `@Transactional`（含 `rollbackFor`/`noRollbackFor`）；日志走 AOP 切面；
- JDK17 禁止 `javax.*` 须用 `jakarta.*`；基本类型用包装类；时间字段加 `@JsonFormat`；
- Excel 导入/导出超量必须批处理；微服务/第三方接口须在 api 模块或独立包实现（ServiceImpl 只放业务逻辑）；
- 并发创建/更新/删除优先 Redis 原子锁；写完代码执行 `mvn -pl compile` 真编译验证。

> 完整条款见 `references/java-action-rules.md`。

## 关键约定（速查）
- **技术栈**：小程序 Taro/Weapp + TS/JS + Taro UI（请求前缀 `:9001`）；后端 Spring Boot 4.1 + JDK 17 + MyBatis-Plus + MySQL 8 + Redis 7（端口 `:9002`）；管理端 Vue3 + TS + Vben 5.0 + Axios。
- **统一返回**：后端接口返回 `Response<DataResult<T>>` 或 `Response<PageResult<T>>`。
- **单号生成**：统一使用 `OrderUtils.getOrderNo("前缀")`，格式为 `前缀 + MMDD + 5位随机数 + 校验码`。
- **网络请求**：小程序端封装 `requestUtils`，提供 get/post/put/delete/download/upload，统一多环境前缀与鉴权。
- **价格精度**：订单相关金额统一 `decimal(20,2)`。
- **批量操作**：后端批量插入/更新/删除必须批处理，禁止 for/while 循环实现。
- **环境隔离**：后端 `application-dev / prd / test.yml`，默认 dev；数据库连接、Redis、七牛云等配置需逐项注释。
- **主题色**：小程序视觉（背景/按钮/字体/TabBar 图标）按用户指定主题色生成。
- **交付物**：接口完成后整理 SQL 文件；小程序与管理端各附 README（启动/编译/关闭教程）。

## 各角色职责速览
- **产品经理·小青**：需求拆解、任务分发、统筹；用户说「帮我生成小程序」时按完整流程统筹三个 Agent，三端完成后由其统一验收并发布。
- **验收与发布**：由产品经理小青在三端交付完成后统一执行验收，通过则发布；不通过退回对应负责人修正。
- **小程序工程师·小张**：询问主题色/名称/logo/目标目录；定义框架与 `requestUtils`；实现首页、分类、详情、购物车、个人中心、地址、订单、开票等页面与数据结构。
- **后端工程师·小王**：确认项目目录/包名（默认 `com.ai.shop`、`ai-shop-system`）；搭建 Spring Boot 单体；实现系统管理、商品中心、订单中心、小程序专属接口与定时任务；产出 SQL。
- **前端工程师·小刘**：基于 Vben 5.0；按后端接口与实体实现管理界面；分页独立 `index.vue`、增删改用分离式 dialog、模块化目录。

## 注意事项
- 单一商户场景，不引入 SaaS 多租户。
- 定时任务涉及多数据场景时，采用多线程多路复用。
- 缺接口时由前端向后端索要，不自造接口。
- 所有实体继承 `BaseEntity`，含 id（uuid/雪花算法）、createTime/updateTime、操作人、delete、version 等公共字段。

完整字段定义、接口清单与数据结构示例一律以 `references/manual.md` 为准。
