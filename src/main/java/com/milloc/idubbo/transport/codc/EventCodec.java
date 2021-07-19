package com.milloc.idubbo.transport.codc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.milloc.idubbo.domain.Payload;
import com.milloc.idubbo.domain.Type;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.util.*;

/**
 * EventCodec
 *
 * @author gongdeming
 * @date 2021-07-14
 */
@Slf4j
public class EventCodec extends MessageToMessageCodec<ByteBuf, Payload<?>> {
    public static final String MSG_BEGIN = "Idubbo";
    public static final String MSG_END = "end";

    private final StringBuffer stringBuffer;
    private final ObjectMapper objectMapper;

    {
        stringBuffer = new StringBuffer();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Payload<?> msg, List<Object> out) throws Exception {
        String encodedMsg = eventToString(msg);
        log.info("send msg \n{}", encodedMsg);
        out.add(Unpooled.copiedBuffer(encodedMsg, Charset.defaultCharset()));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        int readableBytes = buf.readableBytes();
        if (readableBytes == 0) {
            return;
        }
        byte[] bytes = new byte[readableBytes];
        buf.readBytes(bytes);
        String msg = new String(bytes);

        if (stringBuffer.length() == 0 && !StringUtils.startsWith(msg, MSG_BEGIN)) {
            log.info("illegal msg {}", msg);
            return;
        } else {
            stringBuffer.append(msg);
        }

        if (StringUtils.endsWith(msg, MSG_END)) {
            String s = stringBuffer.toString();
            Payload<Object> payload = stringToEvent(s);
            out.add(payload);
            log.info("receive msg {}", payload);
        }
    }

    private String eventToString(Payload<?> msg) throws JsonProcessingException {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(MSG_BEGIN);
        joiner.add(msg.getType().desc);
        if (msg.getContent() != null) {
            joiner.add(objectMapper.writeValueAsString(msg.getContent()));
        }
        joiner.add(MSG_END);
        return joiner.toString();
    }

    private Payload<Object> stringToEvent(String s) throws com.fasterxml.jackson.core.JsonProcessingException {
        String[] parts = s.split("\n");
        Preconditions.checkArgument(parts.length >= 2);
        String type = parts[1];
        Type eventType = Type.of(type);
        Objects.requireNonNull(eventType);
        Class<?> contentClazz = eventType.contentType;
        Objects.requireNonNull(contentClazz);
        Payload<Object> payload = new Payload<>();
        payload.setType(eventType);
        if (contentClazz != Void.class) {
            String content = parts[2];
            Object o = objectMapper.readValue(content, contentClazz);
            payload.setContent(o);
        }
        return payload;
    }
}

