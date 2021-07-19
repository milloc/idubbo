# Netty实战：实现spring下的RPC

## 环境

- jdk 1.8
- maven 3.6

## demo

- 参考[src/main/java/com/milloc/idubbo/example](src/main/java/com/milloc/idubbo/example)

## 基础

1. 自动注入
    - [ClientRegistrar](src/main/java/com/milloc/idubbo/client/ClientRegistrar.java)
    - [ProviderRegister](src/main/java/com/milloc/idubbo/provider/ProviderRegister.java)
    - 以上两个类，分别实现了`@Client`的动态代理和`@Provider`的注册用以提供服务
2. 通讯序列图
   ![通讯序列图](https://github.com/milloc/images/raw/master/github/idubbo/idubbo%E8%B0%83%E7%94%A8%E5%BA%8F%E5%88%97%E5%9B%BE.jpg)
3. [Payload](src/main/java/com/milloc/idubbo/domain/Payload.java)定义了通讯的内容

```java
public class Payload<T> {
    private Type type; // 类型
    private T content; // 具体内容
}
```

每种类型与对应返回内容如下

|发起者|类型|功能|内容|
|---|---|---|---|
|Provider|PUBLISH|发布服务,告知自己的服务地址|ProviderId|
|Client|SUBSCRIBE|向Provider请求Service列表|Void|
|Provider|PROVIDERS|向Client返回Service列表|ProviderInfo|
|Client|PROVIDER_EXEC|请求调用Service方法|ExecArgs|
|Provider|PROVIDER_EXEC_RESULT|Provider返回调用结果|ExecResult|

## Netty通讯

1. [EventCodec](src/main/java/com/milloc/idubbo/transport/codc/EventCodec.java)实现了通讯报文结构

```text
   Idubbo\n // 头部
   {Type}\n // 类型
   {Content}\n // 内容
   end // 结束
```

2. [ProviderListener](src/main/java/com/milloc/idubbo/provider/config/ProviderListener.java)实现了Provider服务功能的实现

```java
public class ProviderListener implements InitializingBean {
    public void listen() {
        ChannelInitializer<NioSocketChannel> channelInitializer = new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new LoggingHandler());
                // 使用 EventCodec 编码转码
                pipeline.addLast(new EventCodec());
                // 处理 Payload
                pipeline.addLast(new SimpleChannelInboundHandler<Payload<?>>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Payload<?> msg) {
                        Type type = msg.getType();
                        PayloadHandler payloadHandler = handlerMap.get(type);
                        if (payloadHandler == null) {
                            // discard
                            log.error("unsupported msg {}", msg);
                        } else {
                            // 根据具体的类型分发给对应的 handler 来处理
                            Payload result = payloadHandler.handle(msg);
                            log.info("exec msg input={}, res={}", msg, result);
                            // 处理完成后，写入结果
                            ctx.writeAndFlush(result);
                        }
                    }
                });
            }
        };

        Transports.listen(channelInitializer, providerProperties.getListenPort());
    }

    @Override
    public void afterPropertiesSet() {
        // start listen
        listen();
    }
}

public class Transports {
    // 监听端口
    public static void listen(ChannelHandler channelHandler, int port) {
        // 创建启动类型
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        // 创建工作组
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        serverBootstrap.group(boss, worker)
                // 定义最大支持请求数
                .option(ChannelOption.SO_BACKLOG, 1000)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelHandler);

        // 绑定端口
        ChannelFuture channelFuture = serverBootstrap.bind(port);
        // 使用 addListener 的方式，不阻塞主进程
        channelFuture.addListener((f) -> {
            if (f.isSuccess()) {
                channelFuture.channel().closeFuture().addListener(cf -> {
                    boss.shutdownGracefully();
                    worker.shutdownGracefully();
                });
            } else {
                boss.shutdownGracefully();
                worker.shutdownGracefully();
            }
        });
    }
}
```

3. [BroadcastPublish](src/main/java/com/milloc/idubbo/provider/publish/BroadcastPublish.java)实现了Provider的广播发布

```java

@Slf4j
public class BroadcastPublish implements PublishRule {
    @Override
    public void publish(ProviderContext providerContext) {
        InetSocketAddress recipient = new InetSocketAddress("255.255.255.255", port);
        ProviderInfo currentProviderInfo = providerContext.getCurrentProviderInfo();
        ProviderId providerId = currentProviderInfo.getProviderId();

        ChannelInitializer<NioDatagramChannel> publishChannelHandler = new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new LoggingHandler());
                // 转化为 广播的报文 255.255.255.255 ，指定只有 port 端口的才能接收
                pipeline.addLast("encoder", new ChannelOutboundHandlerAdapter() {
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                        ByteBuf byteBuf = (ByteBuf) msg;
                        DatagramPacket datagramPacket = new DatagramPacket(byteBuf, recipient);
                        ctx.writeAndFlush(datagramPacket);
                    }
                });
                pipeline.addLast(new EventCodec());
                pipeline.addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) {
                        ctx.executor().parent().execute(() -> {
                            Payload<ProviderId> publishPayload = Payload.of(Type.PUBLISH, providerId);
                            while (true) {
                                try {
                                    ctx.writeAndFlush(publishPayload);
                                    TimeUnit.SECONDS.sleep(5);
                                } catch (InterruptedException e) {
                                    log.error("publish err", e);
                                }
                            }
                        });
                    }
                });
            }
        };
        // 指定本地端口 0 表示任意一个可用端口，进行发送
        Transports.broadcast(publishChannelHandler, 0);
    }
}

public class Transports {
    @SneakyThrows
    public static void broadcast(ChannelHandler channelHandler, int port) {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(channelHandler);
        ChannelFuture bind = bootstrap.bind(port);
        bind.addListener(bf -> {
            if (bf.isSuccess()) {
                bind.channel().closeFuture().addListener(cf -> group.shutdownGracefully());
            } else {
                group.shutdownGracefully();
            }
        });
    }
}

```

4. [Requests](src/main/java/com/milloc/idubbo/transport/Requests.java)实现了Provider与Client之间的通信

```java
public class Requests {
    public static <T> Payload<T> request(Payload<?> input, String host, int port) {
        ChannelInitializer<SocketChannel> channelHandler = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new LoggingHandler())
                        .addLast(new EventCodec())
                        // 添加返回内容的获取
                        .addLast(new PayloadResponse());
            }
        };

        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(channelHandler);
            ChannelFuture connect = bootstrap.connect(host, port).sync();
            // 写入请求内容
            ChannelFuture request = connect.channel().writeAndFlush(input).sync();
            PayloadResponse response = (PayloadResponse) request.channel().pipeline().last();
            request.addListener(rf -> request.channel().closeFuture());
            // 会阻塞直到有请求返回
            return (Payload<T>) response.getOutput();
        } finally {
            group.shutdownGracefully();
        }
    }
}

public class PayloadResponse extends ChannelInboundHandlerAdapter {
    // 使用同步队列
    private final SynchronousQueue<Payload<?>> synchronousQueue = new SynchronousQueue<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 当请求返回内容，添加到队列中
        synchronousQueue.put((Payload<?>) msg);
    }

    @SneakyThrows
    public Payload<?> getOutput() {
        // 阻塞获取同步队列内容
        return synchronousQueue.take();
    }
}

```

## 更多内容

1. [PublishRule](src/main/java/com/milloc/idubbo/provider/publish/PublishRule.java)
   与[SubscribeRule](src/main/java/com/milloc/idubbo/client/subscribe/SubscribeRule.java)可自定义服务发布策略
2. [LoadBalanceRule](src/main/java/com/milloc/idubbo/client/loadblance/LoadBalanceRule.java)可定义Client负载策略