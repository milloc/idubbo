package com.milloc.idubbo.example.provider;

import com.milloc.idubbo.provider.EnableIDubboProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Provider
 *
 * @author gongdeming
 * @date 2021-07-19
 */
@EnableIDubboProvider(basePackages = "com.milloc.idubbo.example.provider")
@SpringBootApplication
public class Provider {
    public static void main(String[] args) {
        SpringApplication.run(Provider.class, args);
    }
}
