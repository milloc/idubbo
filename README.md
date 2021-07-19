# Netty实战：实现spring下的RPC

---

## demo

1. 启动类上开启注解

```java

@EnableIDubboClient(basePackages = "com.milloc.idubbo.test") // 启动消费方
@EnableIDubboProvider(basePackages = "com.milloc.idubbo.test") // 启动提供方
@SpringBootApplication(scanBasePackages = "com.milloc.idubbo.test")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }
}
```

2. 定义业务接口

```java
public interface TestClient {
    String hello(String username);

    int hello(int a);

    void eeee(String fff, String[] aaa);

    String err();
}
```

3. 服务提供方实现业务接口

```java

@Provider // 使用该注解表示是服务方
@Slf4j
public class TestClientImpl implements TestClient {
    @Override
    public String hello(String username) {
        return "hello " + username;
    }

    @Override
    public int hello(int a) {
        return 222 + a;
    }

    @Override
    public void eeee(String fff, String[] aaa) {
        log.info("{} {}", fff, Arrays.deepToString(aaa));
    }

    @Override
    public String err() {
        throw new NullPointerException("this is a err");
    }
}
```

4. 消费方添加注解到业务接口上，实现自动代理

```java

@Client
public interface TestClient {

}
```

## 原理

1. 自动注入
    - [ClientRegistrar](src/main/java/com/milloc/idubbo/client/ClientRegistrar.java)
    - [ProviderRegister](src/main/java/com/milloc/idubbo/provider/ProviderRegister.java)
    - 以上两个类，分别实现了`@Client`的动态代理和`@Provider`的注册用以提供服务
2. 通信
```mermaid

```
3. Netty通信