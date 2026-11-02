# AI 按量付费（Machine Pay / A2M）模板代码

本目录随 shop-builder 分发，包含可直接并入后端 `ai-shop-system` 的 Java 模板。

## 文件
| 文件 | 说明 |
| --- | --- |
| `AlipayA2mConfig.java` | 读取沙箱/生产配置（APPID、应用私钥 PKCS#8、支付宝公钥、网关、`serviceId`、`sellerId`），构建 `AlipayConfig` |
| `A2MPaymentController.java` | 核心 402 控制器，实现 probe → complete → ack：`GET /a2m/resource` 无凭证返回 `402 + Payment-Needed`；有凭证则 `payment.verify` 验付、幂等履约、`fulfillment.confirm` 回执、返回资源 + `Payment-Validation` |
| `InMemoryOrderRepository.java` | 本地联调用内存仓储；生产须替换为 MyBatis-Plus 实现（带事务与唯一约束） |

## 使用
```bash
python scripts/scaffold_shop_builder.py --target <输出目录> --with-a2m
```
脚本会把以上模板写入 `ai-shop-system/src/main/java/com/ai/shop/pay/a2m/`，并在 `pom.xml` 注入 `alipay-sdk-java` 依赖。

## 关键协议
- `HTTP 402 Payment Required` + `Payment-Needed`（Base64URL 账单）
- `Payment-Proof`（请求头） → `alipay.aipay.agent.payment.verify`
- `Payment-Validation`（响应头）
- `alipay.aipay.agent.fulfillment.confirm`（履约回执 ack）

## 配置
在生成的后端根目录放置 `.alipay-sandbox.json`（加入 `.gitignore`）：
`appId`、`appPrivateKey`（PKCS#8）、`appPublicKey`、`alipayPublicKey`、`gateway`、`serviceId`、`sellerId`（2088 商户号）。

## 安全
私钥只存服务端，禁止写入前端、日志、公共仓库；沙箱与生产的 APPID/网关/密钥严格隔离。
