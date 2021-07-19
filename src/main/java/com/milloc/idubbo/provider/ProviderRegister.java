package com.milloc.idubbo.provider;


import com.milloc.idubbo.provider.config.ProviderConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Map;
import java.util.Objects;

/**
 * ProviderConfig
 *
 * @author gongdeming
 * @date 2021-07-13
 */
@Slf4j
@Import(ProviderConfig.class)
public class ProviderRegister implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // load provider
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableIDubboProvider.class.getName());
        Objects.requireNonNull(attributes, "basePackages cannot be null");
        String[] basePackages = (String[]) attributes.get("basePackages");
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Provider.class));
        scanner.scan(basePackages);
        // load other component
        ClassPathBeanDefinitionScanner providerComponent = new ClassPathBeanDefinitionScanner(registry);
        providerComponent.scan(this.getClass().getPackage().getName());
    }

}
