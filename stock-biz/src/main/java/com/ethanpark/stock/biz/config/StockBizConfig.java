package com.ethanpark.stock.biz.config;

import com.ethanpark.stock.core.config.StockCoreConfig;
import com.ethanpark.stock.remote.remote.StockRemoteConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/8
 */
@Configuration
@ComponentScan("com.ethanpark.stock.biz")
@Import({StockCoreConfig.class, StockRemoteConfig.class})
public class StockBizConfig {

}
