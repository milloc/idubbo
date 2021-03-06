package com.milloc.idubbo;

import com.milloc.idubbo.client.EnableIDubboClient;
import com.milloc.idubbo.provider.EnableIDubboProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * App
 *
 * @author gongdeming
 * @date 2021-07-12
 */
@EnableIDubboClient(basePackages = "com.milloc.idubbo.test")
@EnableIDubboProvider(basePackages = "com.milloc.idubbo.test")
@SpringBootApplication(scanBasePackages = "com.milloc.idubbo.test")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }
}
