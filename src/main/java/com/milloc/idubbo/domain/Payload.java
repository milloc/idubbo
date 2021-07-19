package com.milloc.idubbo.domain;

import lombok.Data;

/**
 * 传输实体
 * 
 * @author gongdeming
 * @date 2021-07-15
 */
@Data
public class Payload<T> {
    private Type type;
    private T content;

    public static <T> Payload<T> of(Type type) {
        Payload<T> payload = new Payload<>();
        payload.setType(type);
        return payload;
    }

    public static <T> Payload<T> of(Type type, T content) {
        Payload<T> payload = new Payload<>();
        payload.setType(type);
        payload.setContent(content);
        return payload;
    }
}
