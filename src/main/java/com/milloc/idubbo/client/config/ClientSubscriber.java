package com.milloc.idubbo.client.config;

import com.milloc.idubbo.client.subscribe.SubscribeRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * ClientSubscriber
 *
 * @author gongdeming
 * @date 2021-07-16
 */
@Component
public class ClientSubscriber implements ApplicationListener<ApplicationStartedEvent> {
    @Autowired
    private SubscribeRule subscribeRule;
    @Autowired
    private ProvidersContext providersContext;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent ignore) {
        subscribeRule.subscribe(providersContext);
    }
}
