package com.milloc.idubbo.client;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * EnableIDubbo
 *
 * @author gongdeming
 * @date 2021-07-12
 */
@Import(ClientRegistrar.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableIDubboClient {
    String[] basePackages() default "";
}
