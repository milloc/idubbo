package com.milloc.idubbo.transport;

import com.milloc.idubbo.domain.Payload;
import com.milloc.idubbo.transport.codc.EventCodec;
import com.milloc.idubbo.transport.codc.PayloadResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.SneakyThrows;

/**
 * 请求调用
 *
 * @author gongdeming
 * @date 2021-07-16
 */
@SuppressWarnings("unchecked")
public class Requests {
    @SneakyThrows
    public static <T> Payload<T> request(Payload<?> input, String host, int port) {
        ChannelInitializer<SocketChannel> channelHandler = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new LoggingHandler())
                        .addLast(new EventCodec())
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
            ChannelFuture request = connect.channel().writeAndFlush(input).sync();
            PayloadResponse response = (PayloadResponse) request.channel().pipeline().last();
            request.addListener(rf -> request.channel().closeFuture());
            return (Payload<T>) response.getOutput();
        } finally {
            group.shutdownGracefully();
        }
    }
}
