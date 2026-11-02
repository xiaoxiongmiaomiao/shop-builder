# AI 按量付费（Machine Pay / A2M）集成规范

> 本文件是 shop-builder 的付费改造依据。为一键店铺后端「只读查询类接口」接入支付宝 AI 按量付费，让 AI 智能体按次付费调用 API。

## 一、协议概览：HTTP 402 瀑布式收款

服务端不依赖任何页面跳转。智能体请求付费资源时，服务端用 HTTP 状态码与自定义 Header 完成「索要账单 → 支付 → 验付 → 交付 → 回执」闭环。

| 要素 | 说明 |
| --- | --- |
| `HTTP 402 Payment Required` | 未携带有效支付凭证时的响应状态 |
| `Payment-Needed` (Request/Response Header) | Base64URL 编码账单，含 protocol / method 两层结构 |
| `Payment-Proof` (Request Header) | 支付完成后智能体携带的支付凭证 |
| `Payment-Validation` (Response Header) | 服务端交付资源时返回的验付证明 |

## 二、生命周期 probe → pay → complete → ack

| 阶段 | 触发方 | 服务端动作 | 协议要点 |
| --- | --- | --- | --- |
| **probe** 探测 | 智能体首次请求付费接口 | 判定无 `Payment-Proof` → 返回 `402` + `Payment-Needed` | 账单须先持久化本地订单（`out_trade_no`、`resource_id`、金额、状态、`pay_before`） |
| **pay** 支付 | 智能体凭账单拉起支付 | （不参与；由支付宝侧完成） | 沙箱 `service_id=api_mock_service_id`；生产用服务市场真实 `serviceId` |
| **complete** 完成 | 智能体携带 `Payment-Proof` 重试原接口 | 调 `alipay.aipay.agent.payment.verify` 验付 → 金额/订单/资源/幂等校验 → 生成资源 → 返回 `200` + `Payment-Validation` | 必须校验 `active=true`、金额一致、`out_trade_no` 一致、`resource_id` 一致、未被重复履约 |
| **ack** 回执 | 服务端 | 调 `alipay.aipay.agent.fulfillment.confirm` 发送履约回执，再标记订单完成 | 回执失败允许同一 `Payment-Proof` 重试 ack；禁止资源已生成却返回成功交付 |

## 三、Payment-Needed 账单结构

`protocol` 层（必填）：`out_trade_no`、`amount`、`currency=CNY`、`resource_id`、`pay_before`、`seller_signature`、`seller_sign_type=RSA2`、`seller_unique_id`。

`method` 层（必填）：`seller_name`、`seller_id`、`seller_app_id`、`goods_name`、`seller_unique_id_key=seller_id`、`service_id`。

商家签名（`seller_signature`）按 key 字典序拼接以下字段后 RSA2 签名（应用私钥，Java 用 PKCS#8）：
`amount`、`currency`、`goods_name`、`out_trade_no`、`pay_before`、`resource_id`、`seller_id`、`service_id`。

## 四、Payment-Proof 处理

解码后读取：`protocol.payment_proof`、`protocol.trade_no`、`method.client_session`（可选）。
调用 `alipay.aipay.agent.payment.verify`，业务入参即上述三项。

## 五、依赖包（支付依赖）

```xml
<dependency>
    <groupId>com.alipay.sdk</groupId>
    <artifactId>alipay-sdk-java</artifactId>
    <version>4.40.0.ALL</version>
</dependency>
```

## 六、网关与签名

| 环境 | 网关 |
| --- | --- |
| 沙箱 | `https://openapi-sandbox.dl.alipaydev.com/gateway.do` |
| 生产 | `https://openapi.alipay.com/gateway.do` |

- 签名算法：`RSA2`（2048 位）。
- Java 语言私钥格式：`PKCS#8`；非 Java 用 `PKCS#1`。
- 私钥只存服务端加密配置或密钥管理，**禁止写入前端、日志、公共仓库**。

## 七、代码布局与脚手架

- 模板代码：`assets/a2m/AlipayA2mConfig.java`、`assets/a2m/A2MPaymentController.java`。
- 脚手架生成：`python scripts/scaffold_shop_builder.py --target <dir> --with-a2m`，把上述模板与依赖写入后端 `ai-shop-system`。
- 说明详见 `assets/a2m/README.md`。

## 八、上线前检查

- 网关 / APPID / 密钥已由沙箱切换为生产，且三者属于同一套应用。
- `serviceId` 已从 `api_mock_service_id` 替换为真实服务 ID。
- 只对**只读查询类**接口收费；下单/支付等写操作不开放给外部智能体。
- `OrderRepository` 用 MyBatis-Plus + 数据库唯一约束实现 `createPending` / `prepareFulfillment` / `markFulfilled` 事务，禁止内存 Map。
