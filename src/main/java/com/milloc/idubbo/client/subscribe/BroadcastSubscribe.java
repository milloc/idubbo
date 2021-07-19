package com.milloc.idubbo.client.subscribe;

import com.milloc.idubbo.client.config.ProvidersContext;
import com.milloc.idubbo.domain.Payload;
import com.milloc.idubbo.domain.ProviderId;
import com.milloc.idubbo.transport.Transports;
import com.milloc.idubbo.transport.codc.EventCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 广播来获取服务
 *
 * @author gongdeming
 * @date 2021-07-15
 */
@Slf4j
@SuppressWarnings("unchecked")
public class BroadcastSubscribe implements SubscribeRule {
    private final int broadcastPort;

    public BroadcastSubscribe(int broadcastPort) {
        this.broadcastPort = broadcastPort;
    }

    @Override
    public void subscribe(ProvidersContext providersHolder) {
        ChannelInitializer<NioDatagramChannel> channelHandler = new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new LoggingHandler());
                pipeline.addLast(new SimpleChannelInboundHandler<DatagramPacket>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
                        ByteBuf byteBuf = msg.copy().content();
                        ctx.fireChannelRead(byteBuf);
                    }
                });
                pipeline.addLast(new EventCodec());
                pipeline.addLast(new SimpleChannelInboundHandler<Payload<?>>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Payload<?> msg) throws Exception {
                        switch (msg.getType()) {
                            case PUBLISH:
                                log.info("receive publish {}", msg);
                                providersHolder.tryRegister(((Payload<ProviderId>) msg).getContent());
                                break;
                            default:
                                // ignore other
                        }
                    }
                });
            }
        };
        Transports.broadcast(channelHandler, broadcastPort);
    }
}
