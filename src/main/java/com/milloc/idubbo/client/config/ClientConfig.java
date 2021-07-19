package com.milloc.idubbo.client.config;

import com.milloc.idubbo.client.loadblance.LoadBalanceRule;
import com.milloc.idubbo.client.loadblance.Random;
import com.milloc.idubbo.client.subscribe.BroadcastSubscribe;
import com.milloc.idubbo.client.subscribe.SubscribeRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClientConfig
 *
 * @author gongdeming
 * @date 2021-07-15
 */
@Configuration
public class ClientConfig {
    @Autowired
    private ClientProperties clientProperties;

    @Bean
    @ConditionalOnMissingBean(SubscribeRule.class)
    public SubscribeRule broadcastRule() {
        return new BroadcastSubscribe(clientProperties.getBroadcastPort());
    }

    @Bean
    @ConditionalOnMissingBean(LoadBalanceRule.class)
    public LoadBalanceRule loadBalanceRule() {
        return new Random();
    }
}
