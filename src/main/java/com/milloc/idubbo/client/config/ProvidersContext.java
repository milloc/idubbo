package com.milloc.idubbo.client.config;

import com.milloc.idubbo.client.loadblance.LoadBalanceRule;
import com.milloc.idubbo.domain.Payload;
import com.milloc.idubbo.domain.ProviderId;
import com.milloc.idubbo.domain.ProviderInfo;
import com.milloc.idubbo.domain.Type;
import com.milloc.idubbo.transport.Requests;
import lombok.SneakyThrows;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ProvidersHolder
 *
 * @author gongdeming
 * @date 2021-07-14
 */
@Component
public class ProvidersContext implements InitializingBean {
    private final Set<ProviderId> registeredProviderIds = new HashSet<>();
    private final Map<String, List<ProviderId>> serviceMap = new HashMap<>();
    private final ReentrantLock sync = new ReentrantLock();
    private final Condition serviceRegistry = sync.newCondition();
    @Autowired
    private LoadBalanceRule loadBalanceRule;

    @SneakyThrows
    public List<ProviderId> findAllProviders(String serviceName) {
        sync.lock();
        try {
            while (CollectionUtils.isEmpty(serviceMap.get(serviceName))) {
                serviceRegistry.await();
            }
            return serviceMap.get(serviceName);
        } finally {
            sync.unlock();
        }
    }

    public void tryRegister(ProviderId providerId) {
        if (!registeredProviderIds.contains(providerId)) {
            fetchProviderInfo(providerId);
        }
    }

    public void fetchProviderInfo(ProviderId providerId) {
        Payload<?> subscribe = Payload.of(Type.SUBSCRIBE);
        Payload<ProviderInfo> request = Requests.request(subscribe, providerId.getIp(), providerId.getPort());
        register(request.getContent());
    }

    @SneakyThrows
    public void register(ProviderInfo providerInfo) {
        ProviderId providerId = providerInfo.getProviderId();
        sync.lock();
        try {
            if (!registeredProviderIds.contains(providerId)) {
                registeredProviderIds.add(providerId);
                for (String service : providerInfo.getServices()) {
                    serviceMap.computeIfAbsent(service, (k) -> new ArrayList<>()).add(providerId);
                }
                serviceRegistry.signal();
            }
        } finally {
            sync.unlock();
        }
    }

    @Override
    public void afterPropertiesSet() {
        ProvidersHolder.init(this);
    }

    public ProviderId chooseProvider(String serviceName) {
        return loadBalanceRule.chooseProvider(serviceName);
    }
}
