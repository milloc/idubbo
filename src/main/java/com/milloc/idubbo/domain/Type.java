package com.milloc.idubbo.domain;

/**
 * Type
 *
 * @author gongdeming
 * @date 2021-07-14
 */
public enum Type {
    /**
     * 发布
     */
    PUBLISH("Hello, I am a Provider, this is my service port", ProviderId.class),
    /**
     * 订阅
     */
    SUBSCRIBE("Hello, I am a client, I want to subscribe", Void.class),
    /**
     * 发布提供的服务
     */
    PROVIDERS("I am a Provider, there are my services", ProviderInfo.class),
    /**
     * 请求提供方执行
     */
    PROVIDER_EXEC("I am a Client, please execute this method", ExecArgs.class),
    /**
     * 请求执行返回参数
     */
    PROVIDER_EXEC_RESULT("I am a Provider, this is your result", ExecResult.class),
    ;

    public final String desc;

    public final Class<?> contentType;

    Type(String desc, Class<?> contentType) {
        this.desc = desc;
        this.contentType = contentType;
    }

    public static Type of(String event) {
        Type[] types = Type.values();
        for (Type type : types) {
            if (type.desc.equals(event)) {
                return type;
            }
        }
        return null;
    }
}
