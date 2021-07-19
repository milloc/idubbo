package com.milloc.idubbo.provider.publish;

import com.milloc.idubbo.provider.config.ProviderContext;

/**
 * ProviderPublisher
 *
 * @author gongdeming
 * @date 2021-07-14
 */
public interface PublishRule {
    /**
     * 发布
     */
    void publish(ProviderContext providerContext);
}
