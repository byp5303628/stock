package com.ethanpark.stock.web;

import com.ethanpark.stock.biz.config.StockBizConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * @author: baiyunpeng04
 * @since: 2024/11/5
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.ethanpark.stock")
@Import(StockBizConfig.class)
public class WebStarter {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(WebStarter.class);
        application.run(args);
    }
}
