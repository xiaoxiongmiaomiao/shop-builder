package com.ai.shop.pay.a2m;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地联调用的内存实现，仅用于跑通沙箱测试。
 *
 * ⚠️ 生产环境禁止使用：必须用 MyBatis-Plus Mapper + 数据库唯一约束实现，
 * 保证 orderRepository 的 createPending / prepareFulfillment / markFulfilled
 * 在事务内完成，防止并发重复生成资源。
 */
@Repository
public class InMemoryOrderRepository implements A2MPaymentController.OrderRepository {

    private final Map<String, A2MPaymentController.OrderSnapshot> store = new ConcurrentHashMap<>();
    private final Map<String, String> fulfilled = new ConcurrentHashMap<>();

    @Override
    public void createPending(A2MPaymentController.OrderSnapshot order) {
        store.put(order.outTradeNo, order);
    }

    @Override
    public A2MPaymentController.OrderSnapshot findByOutTradeNo(String outTradeNo) {
        return store.get(outTradeNo);
    }

    @Override
    public A2MPaymentController.FulfillmentPreparation prepareFulfillment(
            A2MPaymentController.FulfillmentRequest request) {
        // 首次进入：生成一次资源；重复调用（履约重试）直接返回已保存结果
        if (fulfilled.containsKey(request.outTradeNo)) {
            return new A2MPaymentController.FulfillmentPreparation("FULFILLED", fulfilled.get(request.outTradeNo));
        }
        String resource = request.createResource.get();
        fulfilled.put(request.outTradeNo, resource);
        return new A2MPaymentController.FulfillmentPreparation("PENDING_CONFIRM", resource);
    }

    @Override
    public void markFulfilled(String outTradeNo, String tradeNo) {
        // 内存实现仅打标，生产中改为更新订单履约状态
    }
}
