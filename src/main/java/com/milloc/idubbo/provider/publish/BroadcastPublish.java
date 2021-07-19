package com.milloc.idubbo.provider.publish;

import com.milloc.idubbo.domain.ProviderId;
import com.milloc.idubbo.domain.ProviderInfo;
import com.milloc.idubbo.provider.config.ProviderContext;
import com.milloc.idubbo.transport.Transports;
import com.milloc.idubbo.domain.Payload;
import com.milloc.idubbo.transport.codc.EventCodec;
import com.milloc.idubbo.domain.Type;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * 广播形式
 * <p>
 * 请求格式
 * type
 * content
 *
 * @author gongdeming
 * @date 2021-07-14
 */
@Slf4j
public class BroadcastPublish implements PublishRule {
    private final int port;

    public BroadcastPublish(int port) {
        this.port = port;
    }

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
        Transports.broadcast(publishChannelHandler, 0);
    }
}
