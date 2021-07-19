package com.milloc.idubbo.provider.config;

import com.milloc.idubbo.domain.ProviderId;
import com.milloc.idubbo.domain.ProviderInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ProviderListHolder
 *
 * @author gongdeming
 * @date 2021-07-14
 */
@Slf4j
@Component
public class ProviderContext implements InitializingBean {
    private final Map<String, String> serviceMap = new HashMap<>();
    private int port;
    private String ip;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ProviderProperties providerProperties;

    public void addService(Object serviceProvider, String beanName) {
        Class<?> targetClass = AopUtils.getTargetClass(serviceProvider);
        Class<?>[] interfaces = targetClass.getInterfaces();
        if (interfaces.length != 1) {
            log.error("cannot find unique interface from {}", targetClass);
            return;
        }
        Class<?> service = interfaces[0];
        if (service == null) {
            log.error("cannot find provider service {}", beanName);
        } else {
            String serviceName = service.getName();
            if (serviceMap.containsKey(serviceName)) {
                if (!serviceMap.get(serviceName).equals(beanName)) {
                    log.error("duplicated service {}", service);
                }
            } else {
                log.info("find service {} {}", serviceName, beanName);
                serviceMap.put(serviceName, beanName);
            }
        }
    }

    public Object findService(String serviceName) {
        String beanName = serviceMap.get(serviceName);
        Objects.requireNonNull(beanName);
        Object bean = applicationContext.getBean(beanName);
        return Objects.requireNonNull(bean);
    }

    public ProviderInfo getCurrentProviderInfo() {
        ProviderId providerId = new ProviderId();
        providerId.setPort(port);
        providerId.setIp(ip);

        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.setProviderId(providerId);
        providerInfo.setServices(new ArrayList<>(serviceMap.keySet()));
        return providerInfo;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.ip = Inet4Address.getLocalHost().getHostAddress();
        this.port = providerProperties.getListenPort();
        ProviderHolder.providerContext = this;
    }
}
