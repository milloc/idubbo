package com.milloc.idubbo.transport;

import com.milloc.idubbo.domain.Payload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.SneakyThrows;

import java.util.concurrent.SynchronousQueue;

/**
 * EventRequest
 *
 * @author gongdeming
 * @date 2021-07-14
 */
public class EventResponse extends ChannelInboundHandlerAdapter {
    private final SynchronousQueue<Payload<?>> synchronousQueue = new SynchronousQueue<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        synchronousQueue.put((Payload<?>) msg);
    }

    @SneakyThrows
    public Payload<?> getOutput() {
        return synchronousQueue.take();
    }
}
