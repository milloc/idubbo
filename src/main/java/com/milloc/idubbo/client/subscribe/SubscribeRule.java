package com.milloc.idubbo.client.subscribe;

import com.milloc.idubbo.client.config.ProvidersContext;

/**
 * 服务订阅
 *
 * @author gongdeming
 * @date 2021-07-16
 */
public interface SubscribeRule {
    /**
     * 订阅服务
     *
     * @param providersHolder providersHolder
     */
    void subscribe(ProvidersContext providersHolder);
}
