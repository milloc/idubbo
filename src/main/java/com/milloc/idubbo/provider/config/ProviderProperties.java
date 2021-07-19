package com.milloc.idubbo.provider.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ProviderProperties
 *
 * @author gongdeming
 * @date 2021-07-15
 */
@Data
@Component
@ConfigurationProperties(prefix = "idubbo.provider")
public class ProviderProperties {
    /**
     * 广播发送地址
     */
    private int broadcastPort = 8001;
    /**
     * 服务监听地址
     */
    private int listenPort = 8002;
}
