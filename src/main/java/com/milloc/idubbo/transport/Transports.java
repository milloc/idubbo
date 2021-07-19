package com.milloc.idubbo.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Transports
 *
 * @author gongdeming
 * @date 2021-07-16
 */
@Slf4j
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

    public static void listen(ChannelHandler channelHandler, int port) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        serverBootstrap.group(boss, worker)
                .option(ChannelOption.SO_BACKLOG, 1000)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelHandler);

        ChannelFuture channelFuture = serverBootstrap.bind(port);
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
