package com.ai.shop.pay.a2m;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayConfig;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 支付宝 AI 按量付费（Machine Pay / A2M）配置读取。
 *
 * <p>优先读取项目根目录或 classpath 下的 {@code .alipay-sandbox.json}（沙箱），
 * 生产部署时改为读取 {@code .alipay-prod.json} 或环境变量，并显式切换网关。
 *
 * <p>私钥、支付宝公钥等敏感信息只从外部配置文件读取，禁止硬编码到源码。
 */
public class AlipayA2mConfig {

    private final AlipayConfig alipayConfig;
    private final String serviceId;
    private final String sellerId;
    private final String gateway;

    public AlipayA2mConfig() {
        this(".alipay-sandbox.json");
    }

    public AlipayA2mConfig(String configFileName) {
        JSONObject cfg = load(configFileName);

        this.gateway = cfg.getString("gateway");
        this.serviceId = cfg.getString("serviceId");
        this.sellerId = cfg.getString("sellerId");

        AlipayConfig ac = new AlipayConfig();
        ac.setServerUrl(gateway);
        ac.setAppId(cfg.getString("appId"));
        ac.setPrivateKey(cfg.getString("appPrivateKey")); // Java 使用 PKCS#8 私钥
        ac.setAlipayPublicKey(cfg.getString("alipayPublicKey"));
        ac.setFormat("json");
        ac.setCharset("UTF-8");
        ac.setSignType("RSA2");
        this.alipayConfig = ac;
    }

    private JSONObject load(String fileName) {
        try {
            // 1) 优先读取工作目录下的文件
            java.nio.file.Path p = Paths.get(fileName);
            String text;
            if (Files.exists(p)) {
                text = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
            } else {
                // 2) 回退到 classpath 资源（打包后可放 resources 根目录，记得 gitignore）
                ClassPathResource res = new ClassPathResource(fileName);
                try (InputStream in = res.getInputStream()) {
                    text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
            return JSONObject.parseObject(text);
        } catch (Exception e) {
            throw new IllegalStateException("无法加载支付宝配置文件: " + fileName, e);
        }
    }

    public AlipayConfig getAlipayConfig() {
        return alipayConfig;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getGateway() {
        return gateway;
    }
}
