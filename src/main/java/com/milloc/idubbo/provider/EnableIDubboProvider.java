package com.milloc.idubbo.provider;

import com.milloc.idubbo.provider.ProviderRegister;
import com.milloc.idubbo.provider.config.ProviderProperties;
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
@Import({ProviderRegister.class, ProviderProperties.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableIDubboProvider {
    String[] basePackages() default "";
}
