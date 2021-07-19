package com.milloc.idubbo.example.client;

import com.milloc.idubbo.client.EnableIDubboClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Client
 *
 * @author gongdeming
 * @date 2021-07-19
 */
@EnableIDubboClient(basePackages = "com.milloc.idubbo.example.client")
@SpringBootApplication
public class Client implements ApplicationRunner {
    @Autowired
    private TestClient testClient;

    public static void main(String[] args) {
        SpringApplication.run(Client.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String res = testClient.hello("hello world");
        System.out.println(res);
    }
}
