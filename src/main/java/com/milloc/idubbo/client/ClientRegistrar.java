package com.milloc.idubbo.client;

import com.milloc.idubbo.client.config.ClientProxyFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * AutoIDubboConfig
 *
 * @author gongdeming
 * @date 2021-07-12
 */
@Slf4j
public class ClientRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableIDubboClient.class.getName());
        Objects.requireNonNull(attributes, "basePackages cannot be null");
        String[] basePackages = (String[]) attributes.get("basePackages");

        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false) {
            @Override
            protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
                Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
                for (BeanDefinitionHolder definitionHolder : beanDefinitionHolders) {
                    BeanDefinition beanDefinition = definitionHolder.getBeanDefinition();
                    GenericBeanDefinition definition = (GenericBeanDefinition) beanDefinition;
                    String originClazz = Objects.requireNonNull(beanDefinition.getBeanClassName());
                    // 设置为代理bean
                    definition.setBeanClass(ClientProxyFactoryBean.class);
                    definition.setPrimary(true);
                    // 代理bean需要接口类型
                    definition.getConstructorArgumentValues().addGenericArgumentValue(originClazz);
                    log.debug("add IDubboClient {}", originClazz);
                }
                return beanDefinitionHolders;
            }

            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                AnnotationMetadata metadata = beanDefinition.getMetadata();
                return metadata.isInterface();
            }
        };
        scanner.addIncludeFilter(new AnnotationTypeFilter(Client.class));
        scanner.scan(basePackages);

        // load client component
        ClassPathBeanDefinitionScanner componentScanner = new ClassPathBeanDefinitionScanner(registry);
        componentScanner.scan(this.getClass().getPackage().getName());
    }
}
