package com.milloc.idubbo.provider.config;

import com.milloc.idubbo.transport.Transports;
import com.milloc.idubbo.domain.Payload;
import com.milloc.idubbo.transport.EventCodec;
import com.milloc.idubbo.domain.Type;
import com.milloc.idubbo.provider.handler.PayloadHandler;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ProviderListener
 *
 * @author gongdeming
 * @date 2021-07-15
 */
@Slf4j
@Component
public class ProviderListener implements InitializingBean {
    @Autowired
    private ProviderProperties providerProperties;
    private Map<Type, PayloadHandler> handlerMap;

    @Autowired
    public void loadHandlers(List<PayloadHandler> payloadHandlers) {
        handlerMap = new HashMap<>();
        for (PayloadHandler payloadHandler : payloadHandlers) {
            Type type = payloadHandler.supports();
            if (handlerMap.containsKey(type)) {
                log.error("duplicated handler type {}", type);
            } else {
                handlerMap.put(type, payloadHandler);
            }
        }
    }

    public void listen() {
        ChannelInitializer<NioSocketChannel> channelInitializer = new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new LoggingHandler());
                pipeline.addLast(new EventCodec());
                pipeline.addLast(new SimpleChannelInboundHandler<Payload<?>>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Payload<?> msg) {
                        Type type = msg.getType();
                        PayloadHandler payloadHandler = handlerMap.get(type);
                        if (payloadHandler == null) {
                            // discard
                            log.error("unsupported msg {}", msg);
                        } else {
                            Payload result = payloadHandler.handle(msg);
                            log.info("exec msg input={}, res={}", msg, result);
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
