package com.milloc.idubbo.client.loadblance;

import com.milloc.idubbo.domain.ProviderId;

/**
 * 负载
 *
 * @author gongdeming
 * @date 2021-07-15
 */
public interface LoadBalanceRule {
    /**
     * 选择Provider
     *
     * @param serviceName 服务
     * @return provider
     */
    ProviderId chooseProvider(String serviceName);
}
