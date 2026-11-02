# 店铺生成器 (Shop Builder)

> 一键店铺小程序全栈开发规范 + 多智能体协作工作流。一份指令，生成微信小程序、Java 后端、Vue3 管理后台三端可运行工程。

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![License](https://img.shields.io/badge/license-MIT-green)
![Author](https://img.shields.io/badge/author-深圳市青九风网络科技有限公司-orange)

## 简介

**店铺生成器（shop-builder）** 是一个 WorkBuddy 技能，把一份完整的「一键店铺小程序」全栈开发手册沉淀为可复用规范。它让 AI 在开发该项目的微信小程序、Java 后端、Vue 管理后台时，自动遵循统一的架构、接口约定、编码规范与多智能体协作流程——核心不是「生成能编译的代码」，而是「生成能跑起来的工程」。

## ✨ 特性

- **三端一键生成**：小程序（Taro/Weapp）、Spring Boot 后端、Vue3 管理前端，统一约定、同源契约。
- **多智能体流水线**：产品小青统筹验收、小程序小张、后端小王、管理前端小刘，四角色串行推进，三端契约一致。
- **统一接口契约**：`/mini`（小程序端）与 `/private`（管理端）双前缀；统一 `Response<DataResult<T>>` / `Response<PageResult<T>>` 返回；单号统一 `OrderUtils.getOrderNo`。
- **脚手架脚本**：`scripts/scaffold_shop_builder.py` 一键落地初始骨架，支持按端选择性生成。
- **AI 按量付费（402 / Machine Pay）**：把后端只读接口接入支付宝 AI 付，支持 `probe → pay → complete → ack` 全生命周期，可被 AI 智能体按次付费调用。
- **Java 行动规则**：50+ 条后端全局实现约束（禁用 `@RequestMapping`、强制 DTO/枚举、批量禁循环、构造器注入、`jakarta.*` 等），降低 AI 生成代码的「一眼假」。

## 🧱 技术栈

| 端 | 技术 | 端口 |
| --- | --- | --- |
| 微信小程序 | Taro / Weapp + TypeScript/JavaScript + Taro UI | `:9001` |
| 后端 | Spring Boot 4.1 + JDK 17 + MyBatis-Plus + MySQL 8 + Redis 7 | `:9002` |
| 管理前端 | Vue3 + TypeScript + Vben 5.0 + Axios | — |

## 📁 目录结构

```
shop-builder/
├── SKILL.md                      # 技能定义（权威入口）
├── assets/
│   ├── a2m/                      # 支付宝 AI 按量付费模板代码
│   │   ├── A2MPaymentController.java
│   │   ├── AlipayA2mConfig.java
│   │   ├── InMemoryOrderRepository.java
│   │   └── README.md
│   └── logo/
│       └── shop-builder-logo.png
├── references/
│   ├── manual.md                 # 完整开发手册（权威数据源）
│   ├── java-action-rules.md      # 后端 50+ 条全局约束
│   └── a2m-pay-integration.md    # AI 按量付费集成规范
└── scripts/
    └── scaffold_shop_builder.py  # 一键脚手架
```

## 🚀 快速开始

```bash
# 生成完整三端骨架（Taro 小程序）
python scripts/scaffold_shop_builder.py --target <输出目录> --mini-framework taro

# 只生成后端 + 管理端，跳过小程序
python scripts/scaffold_shop_builder.py --target <输出目录> --mini-framework weapp --no-mini

# 生成并附带 AI 按量付费后端
python scripts/scaffold_shop_builder.py --target <输出目录> --mini-framework taro --with-a2m
```

脚本产出：`ai-shop-system`（Spring Boot 后端）、`shop-mini`（小程序）、`shop-admin`（Vben 管理前端）。

## 👥 多智能体工作流

| 角色 | 职责 |
| --- | --- |
| 小青（产品经理） | 需求拆解、任务分发、统筹；三端完成后统一验收并发布 |
| 小张（小程序） | 确认主题色/名称/logo/目录，封装 `requestUtils`，实现各页面与数据结构 |
| 小王（后端） | 搭建 Spring Boot 单体，实现系统/商品/订单/小程序接口与定时任务，产出 SQL |
| 小刘（管理前端） | 基于 Vben 5.0，按接口与实体实现管理界面 |

## 💰 AI 按量付费（402 / Machine Pay）

为后端只读查询类接口接入支付宝 AI 付，让外部 AI 智能体按次付费调用：

| 阶段 | 服务端动作 |
| --- | --- |
| **probe** | 无 `Payment-Proof` 时返回 `HTTP 402` + `Payment-Needed` 头（Base64URL 账单） |
| **pay** | 智能体凭账单拉起支付，服务端不参与 |
| **complete** | 携带 `Payment-Proof` 重试 → 验付 → 交付资源（`200` + `Payment-Validation`） |
| **ack** | 调用履约回执并标记订单完成 |

## 📐 后端开发规范（速览）

完整条款见 `references/java-action-rules.md`。要点：

- 禁止 `@RequestMapping`，用 `@PostMapping/@GetMapping` 写全路径且前缀加 `/private`；收参禁 `Map`/`HttpRequest`，返回禁直接 `Entity`（须转 VO）。
- 入参超 2 字段须用 DTO（继承 `BaseDTO`）；状态/类型字段强制枚举。
- 批量操作一律批处理，禁止 `for/while` 操作数据库；ServiceImpl 只注入 Service 接口。
- 注入用 `@RequiredArgsConstructor` + `private final`，禁 `@Autowired/@Resource`。
- 变更类方法必须 `@Transactional`；JDK17 用 `jakarta.*`；时间字段加 `@JsonFormat`。
- 写完执行 `mvn -pl compile` 真编译验证。

## 📄 许可证

[MIT](https://opensource.org/licenses/MIT) © 深圳市青九风网络科技有限公司
