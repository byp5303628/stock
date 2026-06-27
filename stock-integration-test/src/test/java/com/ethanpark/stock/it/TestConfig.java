package com.ethanpark.stock.it;

import com.ethanpark.stock.biz.config.StockBizConfig;
import com.ethanpark.stock.core.config.StockCoreConfig;
import com.ethanpark.stock.remote.remote.StockRemoteConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 集成测试专用 Spring Boot 配置。
 *
 * <p>通过 @Import 显式导入所有 config 完成组件扫描，避免
 * @ComponentScan("com.ethanpark.stock") 导致 StockBizConfig 被注册两次。
 *
 * @author baiyunpeng04
 */
@SpringBootApplication(scanBasePackages = {"com.ethanpark.stock.web", "com.ethanpark.stock.core", "com.ethanpark.stock.remote"})
@Import({StockBizConfig.class, StockCoreConfig.class, StockRemoteConfig.class})
public class TestConfig {
}
