package com.ai.shop.pay.a2m;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayAipayAgentFulfillmentConfirmModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayAipayAgentFulfillmentConfirmRequest;
import com.alipay.api.request.AlipayAipayAgentPaymentVerifyRequest;
import com.alipay.api.response.AlipayAipayAgentFulfillmentConfirmResponse;
import com.alipay.api.response.AlipayAipayAgentPaymentVerifyResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

/**
 * 支付宝 AI 按量付费（Machine Pay / A2M）接入控制器。
 *
 * 协议流程：
 *  GET /a2m/resource
 *   ├─ 无 Payment-Proof Header → 返回 HTTP 402 + Payment-Needed Header（Base64URL 账单）
 *   └─ 有 Payment-Proof Header → 验付 → 幂等履约 → 返回资源 + Payment-Validation Header
 *
 * 依赖：
 *  - 绑定真实持久化实现 {@link OrderRepository}（禁止内存订单，生产用 MyBatis-Plus）
 *  - 支付宝 Java SDK（alipay-sdk-java），见 pom 片段
 */
@RestController
@RequestMapping("/a2m")
public class A2MPaymentController {

    private final AlipayClient alipayClient;
    private final String serviceId;
    private final String sellerId;
    private final OrderRepository orderRepository;

    private static final String RESOURCE_PATH = "/a2m/resource";

    public A2MPaymentController(OrderRepository orderRepository, AlipayA2mConfig a2mConfig) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "必须绑定项目真实 OrderRepository");
        this.serviceId = Objects.requireNonNull(a2mConfig.getServiceId(), "serviceId 不能为空");
        this.sellerId = Objects.requireNonNull(a2mConfig.getSellerId(), "sellerId(2088) 不能为空");
        try {
            this.alipayClient = new DefaultAlipayClient(a2mConfig.getAlipayConfig());
        } catch (AlipayApiException e) {
            throw new RuntimeException("初始化支付宝客户端失败", e);
        }
    }

    /**
     * 项目持久化端口。prepareFulfillment 必须在同一事务内校验订单并且只生成一次资源。
     * 生产实现用 MyBatis-Plus Mapper + 数据库唯一约束，禁止替换为进程内 Map。
     */
    public interface OrderRepository {
        void createPending(OrderSnapshot order);

        OrderSnapshot findByOutTradeNo(String outTradeNo);

        FulfillmentPreparation prepareFulfillment(FulfillmentRequest request);

        void markFulfilled(String outTradeNo, String tradeNo);
    }

    public static final class OrderSnapshot {
        public final String outTradeNo;
        public final String amount;
        public final String currency;
        public final String resourceId;
        public final String goodsName;
        public final String payBefore;
        public final String orderStatus;
        public final String fulfillStatus;

        public OrderSnapshot(String outTradeNo, String amount, String currency, String resourceId,
                             String goodsName, String payBefore, String orderStatus, String fulfillStatus) {
            this.outTradeNo = outTradeNo;
            this.amount = amount;
            this.currency = currency;
            this.resourceId = resourceId;
            this.goodsName = goodsName;
            this.payBefore = payBefore;
            this.orderStatus = orderStatus;
            this.fulfillStatus = fulfillStatus;
        }
    }

    public static final class FulfillmentRequest {
        public final String outTradeNo;
        public final String tradeNo;
        public final String expectedAmount;
        public final String expectedResourceId;
        public final Supplier<String> createResource;

        public FulfillmentRequest(String outTradeNo, String tradeNo, String expectedAmount,
                                  String expectedResourceId, Supplier<String> createResource) {
            this.outTradeNo = outTradeNo;
            this.tradeNo = tradeNo;
            this.expectedAmount = expectedAmount;
            this.expectedResourceId = expectedResourceId;
            this.createResource = createResource;
        }
    }

    public static final class FulfillmentPreparation {
        public final String state;
        public final String serviceResult;

        public FulfillmentPreparation(String state, String serviceResult) {
            this.state = state;
            this.serviceResult = serviceResult;
        }
    }

    @GetMapping("/resource")
    public ResponseEntity<?> getResource(
            @RequestHeader(value = "Payment-Proof", required = false) String paymentProof) {
        if (paymentProof == null || paymentProof.trim().isEmpty()) {
            return createPaymentRequiredResponse();
        }
        return verifyPaymentAndDeliverResource(paymentProof);
    }

    private ResponseEntity<?> createPaymentRequiredResponse() {
        try {
            String outTradeNo = "ORDER_" + System.currentTimeMillis() + "_"
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            String amount = "0.01"; // 固定单价（元）；上线前改为真实计费金额
            String currency = "CNY";
            String resourceId = RESOURCE_PATH;
            String goodsName = "一键店铺 API 调用";

            ZonedDateTime payBefore = ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).plusMinutes(30);
            String payBeforeStr = payBefore.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            Map<String, String> signParams = new LinkedHashMap<>();
            signParams.put("amount", amount);
            signParams.put("currency", currency);
            signParams.put("goods_name", goodsName);
            signParams.put("out_trade_no", outTradeNo);
            signParams.put("pay_before", payBeforeStr);
            signParams.put("resource_id", resourceId);
            signParams.put("seller_id", sellerId);
            signParams.put("service_id", serviceId);

            String privateKey = alipayClientConfigPrivateKey();
            String sellerSignature = generateSellerSignature(signParams, privateKey);

            orderRepository.createPending(new OrderSnapshot(
                    outTradeNo, normalizeAmount(amount), currency, resourceId, goodsName, payBeforeStr,
                    "PENDING_PAYMENT", "UNFULFILLED"));

            JSONObject protocol = new JSONObject();
            protocol.put("out_trade_no", outTradeNo);
            protocol.put("amount", amount);
            protocol.put("currency", currency);
            protocol.put("resource_id", resourceId);
            protocol.put("pay_before", payBeforeStr);
            protocol.put("seller_signature", sellerSignature);
            protocol.put("seller_sign_type", "RSA2");
            protocol.put("seller_unique_id", sellerId);

            JSONObject method = new JSONObject();
            method.put("seller_name", "一键店铺商户");
            method.put("seller_id", sellerId);
            method.put("seller_app_id", alipayClientConfigAppId());
            method.put("goods_name", goodsName);
            method.put("seller_unique_id_key", "seller_id");
            method.put("service_id", serviceId);

            JSONObject paymentNeeded = new JSONObject();
            paymentNeeded.put("protocol", protocol);
            paymentNeeded.put("method", method);

            String encoded = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(paymentNeeded.toJSONString().getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Payment-Needed", encoded);

            JSONObject body = new JSONObject();
            body.put("code", "Payment-Needed");
            body.put("message", "需要支付");
            body.put("out_trade_no", outTradeNo);
            body.put("amount", amount);
            body.put("currency", currency);
            body.put("goods_name", goodsName);

            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).headers(headers).body(body);
        } catch (AlipayApiException e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SIGN_ERROR", "签名失败: " + e.getMessage());
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "CREATE_ORDER_ERROR", "创建订单失败: " + e.getMessage());
        }
    }

    private ResponseEntity<?> verifyPaymentAndDeliverResource(String paymentProof) {
        try {
            String paymentProofValue = null;
            String tradeNo = null;
            String clientSession = null;
            try {
                String decoded = new String(Base64.getUrlDecoder().decode(paymentProof), StandardCharsets.UTF_8);
                JSONObject proofJson = JSONObject.parseObject(decoded);
                JSONObject protocol = proofJson.getJSONObject("protocol");
                if (protocol != null) {
                    paymentProofValue = protocol.getString("payment_proof");
                    tradeNo = protocol.getString("trade_no");
                }
                JSONObject method = proofJson.getJSONObject("method");
                if (method != null) {
                    clientSession = method.getString("client_session");
                }
                if (!hasText(paymentProofValue) || !hasText(tradeNo)) {
                    return createPaymentRequiredResponse();
                }
            } catch (Exception e) {
                return createPaymentRequiredResponse();
            }

            AlipayAipayAgentPaymentVerifyRequest verifyRequest = new AlipayAipayAgentPaymentVerifyRequest();
            JSONObject verifyBiz = new JSONObject();
            verifyBiz.put("payment_proof", paymentProofValue);
            verifyBiz.put("trade_no", tradeNo);
            if (hasText(clientSession)) {
                verifyBiz.put("client_session", clientSession);
            }
            verifyRequest.setBizContent(verifyBiz.toJSONString());
            AlipayAipayAgentPaymentVerifyResponse verifyResponse = alipayClient.execute(verifyRequest);

            if (!verifyResponse.isSuccess()) {
                return createPaymentRequiredResponse();
            }

            String returnedTradeNo = emptyIfNull(verifyResponse.getTradeNo());
            String verifyOutTradeNo = emptyIfNull(verifyResponse.getOutTradeNo());
            String returnedAmount = emptyIfNull(verifyResponse.getAmount());
            String returnedResourceId = emptyIfNull(verifyResponse.getResourceId());
            Boolean active = verifyResponse.getActive();

            OrderSnapshot order = hasText(verifyOutTradeNo) ? orderRepository.findByOutTradeNo(verifyOutTradeNo) : null;
            boolean sandboxMode = isExactSandboxMode();
            String verifyTradeNo = hasText(returnedTradeNo) ? returnedTradeNo : (sandboxMode ? tradeNo : "");
            String verifyAmount = hasText(returnedAmount) ? returnedAmount : (sandboxMode && order != null ? order.amount : "");
            String resourceIdVerified = hasText(returnedResourceId) ? returnedResourceId
                    : (sandboxMode && order != null ? order.resourceId : "");

            if (!Boolean.TRUE.equals(active)
                    || !hasText(verifyTradeNo) || !verifyTradeNo.equals(tradeNo)
                    || !hasText(verifyOutTradeNo) || !hasText(resourceIdVerified)) {
                return createPaymentRequiredResponse();
            }

            boolean amountMatches = order != null && amountsEqual(order.amount, verifyAmount);
            boolean resourceMatches = order != null
                    && RESOURCE_PATH.equals(order.resourceId) && order.resourceId.equals(resourceIdVerified);
            boolean fulfillmentInProgress = order != null
                    && Arrays.asList("PENDING_CONFIRM", "FULFILLED").contains(order.fulfillStatus);
            boolean orderUsable = order != null
                    && "CNY".equals(order.currency)
                    && Arrays.asList("PENDING_PAYMENT", "PAID").contains(order.orderStatus)
                    && Arrays.asList("UNFULFILLED", "PENDING_CONFIRM", "FULFILLED").contains(order.fulfillStatus)
                    && (fulfillmentInProgress || isFuture(order.payBefore));
            if (!amountMatches || !resourceMatches || !orderUsable) {
                return createPaymentRequiredResponse();
            }

            FulfillmentPreparation fulfillment = orderRepository.prepareFulfillment(new FulfillmentRequest(
                    verifyOutTradeNo, verifyTradeNo, normalizeAmount(verifyAmount), resourceIdVerified,
                    () -> generateServiceResource(resourceIdVerified)));
            if (fulfillment == null
                    || !("PENDING_CONFIRM".equals(fulfillment.state) || "FULFILLED".equals(fulfillment.state))
                    || !hasText(fulfillment.serviceResult)) {
                throw new IllegalStateException("OrderRepository.prepareFulfillment 未返回已持久化的履约结果");
            }

            if ("FULFILLED".equals(fulfillment.state)) {
                return sendSuccessfulResource(verifyTradeNo, verifyOutTradeNo, resourceIdVerified, fulfillment.serviceResult, true);
            }

            boolean confirmed = sendFulfillmentConfirm(verifyTradeNo);
            if (!confirmed) {
                return errorResponse(HttpStatus.BAD_GATEWAY, "FULFILLMENT_CONFIRM_FAILED",
                        "资源已生成但履约确认失败，请稍后使用同一 Payment-Proof 重试");
            }

            orderRepository.markFulfilled(verifyOutTradeNo, verifyTradeNo);
            return sendSuccessfulResource(verifyTradeNo, verifyOutTradeNo, resourceIdVerified, fulfillment.serviceResult, false);
        } catch (AlipayApiException e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "VERIFY_FAILED", "支付凭证验证失败: " + e.getErrMsg());
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "FULFILLMENT_ERROR", "履约处理失败: " + e.getMessage());
        }
    }

    private ResponseEntity<?> sendSuccessfulResource(String tradeNo, String outTradeNo,
                                                     String resourceId, String serviceResult, boolean alreadyFulfilled) {
        JSONObject validation = new JSONObject();
        validation.put("trade_no", tradeNo);
        validation.put("out_trade_no", outTradeNo);
        validation.put("validated", true);
        validation.put("resource_id", resourceId);
        String validationEncoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(validation.toJSONString().getBytes(StandardCharsets.UTF_8));

        JSONObject content = new JSONObject();
        content.put("resource_id", resourceId);
        content.put("content", serviceResult);
        content.put("trade_no", tradeNo);
        content.put("out_trade_no", outTradeNo);
        content.put("already_fulfilled", alreadyFulfilled);
        content.put("fulfillment_confirmed", true);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Payment-Validation", validationEncoded);
        return ResponseEntity.ok().headers(headers).body(content);
    }

    /** 生成资源内容：此处演示返回一键店铺 API 调用结果，实际可替换为任意数字服务。 */
    private String generateServiceResource(String resourceId) {
        JSONObject result = new JSONObject();
        result.put("status", "success");
        result.put("service_type", "SHOP_API_CALL");
        result.put("resource_id", resourceId);
        result.put("content", "一键店铺 API 本次调用返回数据示例");
        result.put("generated_at", ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).toString());
        return result.toJSONString();
    }

    private boolean sendFulfillmentConfirm(String tradeNo) {
        if (tradeNo == null || tradeNo.trim().isEmpty()) {
            return false;
        }
        try {
            AlipayAipayAgentFulfillmentConfirmRequest request = new AlipayAipayAgentFulfillmentConfirmRequest();
            AlipayAipayAgentFulfillmentConfirmModel model = new AlipayAipayAgentFulfillmentConfirmModel();
            model.setTradeNo(tradeNo);
            request.setBizModel(model);
            AlipayAipayAgentFulfillmentConfirmResponse response = alipayClient.execute(request);
            return response.isSuccess();
        } catch (AlipayApiException e) {
            return false;
        }
    }

    // ==================== 工具方法 ====================

    private String alipayClientConfigPrivateKey() {
        return ((com.alipay.api.AlipayConfig) alipayClient.getAlipayConfig()).getPrivateKey();
    }

    private String alipayClientConfigAppId() {
        return ((com.alipay.api.AlipayConfig) alipayClient.getAlipayConfig()).getAppId();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String emptyIfNull(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeAmount(String value) {
        BigDecimal amount = parseAmount(value);
        if (amount == null) {
            throw new IllegalArgumentException("金额格式非法");
        }
        return amount.setScale(2).toPlainString();
    }

    private static boolean amountsEqual(String left, String right) {
        BigDecimal l = parseAmount(left);
        BigDecimal r = parseAmount(right);
        return l != null && r != null && l.compareTo(r) == 0;
    }

    private static BigDecimal parseAmount(String value) {
        if (!hasText(value) || !value.trim().matches("\\d+(?:\\.\\d{1,2})?")) {
            return null;
        }
        try {
            BigDecimal amount = new BigDecimal(value.trim());
            return amount.signum() >= 0 ? amount : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isFuture(String value) {
        try {
            return hasText(value) && ZonedDateTime.parse(value).isAfter(ZonedDateTime.now());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isExactSandboxMode() {
        return "api_mock_service_id".equals(serviceId)
                && alipayClientConfigAppId().startsWith("9021");
    }

    private String generateSellerSignature(Map<String, String> params, String privateKey) throws AlipayApiException {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            String v = params.get(k);
            if (v != null && !v.trim().isEmpty()) {
                sb.append(k).append("=").append(v);
                if (i < keys.size() - 1) {
                    sb.append("&");
                }
            }
        }
        return AlipaySignature.rsaSign(sb.toString(), privateKey, "UTF-8", "RSA2");
    }

    private static ResponseEntity<?> errorResponse(HttpStatus status, String code, String message) {
        JSONObject body = new JSONObject();
        body.put("code", code);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
