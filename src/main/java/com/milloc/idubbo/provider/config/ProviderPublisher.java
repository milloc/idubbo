package com.milloc.idubbo.provider.config;

import com.milloc.idubbo.provider.publish.PublishRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ProviderPublisher implements ApplicationListener<ApplicationStartedEvent> {
    @Autowired
    private PublishRule publishRule;
    @Autowired
    private ProviderContext providerContext;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent ignore) {
        publishRule.publish(providerContext);
    }
}
