package com.milloc.idubbo.provider.config;

import com.milloc.idubbo.provider.publish.BroadcastPublish;
import com.milloc.idubbo.provider.publish.PublishRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ProviderConfig
 *
 * @author gongdeming
 * @date 2021-07-15
 */
@Configuration
public class ProviderConfig {
    @Autowired
    private ProviderProperties providerProperties;

    @Bean
    @ConditionalOnMissingBean(PublishRule.class)
    public PublishRule publishRule() {
        return new BroadcastPublish(providerProperties.getBroadcastPort());
    }

    @Bean
    public BeanPostProcessor providerHolderInitialProcessor(ProviderContext providerContext) {
        return new ProviderInitialBeanPostProcessor(providerContext);
    }
}
