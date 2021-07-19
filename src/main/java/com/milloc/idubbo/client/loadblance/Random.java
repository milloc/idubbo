package com.milloc.idubbo.client.loadblance;

import com.milloc.idubbo.client.config.ProvidersHolder;
import com.milloc.idubbo.domain.ProviderId;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机
 *
 * @author gongdeming
 * @date 2021-07-16
 */
public class Random implements LoadBalanceRule {
    @Override
    public ProviderId chooseProvider(String serviceName) {
        List<ProviderId> provider = ProvidersHolder.getContext().findAllProviders(serviceName);
        int random = ThreadLocalRandom.current().nextInt(provider.size());
        return provider.get(random);
    }
}
