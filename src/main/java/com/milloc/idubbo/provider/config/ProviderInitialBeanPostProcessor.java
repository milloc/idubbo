package com.milloc.idubbo.provider.config;

import com.milloc.idubbo.provider.Provider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

/**
 * ProviderInitialBeanPostProcessor
 *
 * @author gongdeming
 * @date 2021-07-15
 */
@Component
@Slf4j
public class ProviderInitialBeanPostProcessor implements BeanPostProcessor {
    private final ProviderContext providerContext;

    public ProviderInitialBeanPostProcessor(ProviderContext providerContext) {
        this.providerContext = providerContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Provider annotation = AnnotationUtils.findAnnotation(bean.getClass(), Provider.class);
        if (annotation != null) {
            providerContext.addService(bean, beanName);
        }
        return bean;
    }
}
