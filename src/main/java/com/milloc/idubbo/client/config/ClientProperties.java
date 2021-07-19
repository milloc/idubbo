package com.milloc.idubbo.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClientProperties
 *
 * @author gongdeming
 * @date 2021-07-16
 */
@Data
@Component
@ConfigurationProperties(prefix = "idubbo.client")
public class ClientProperties {
    /**
     * 广播监听地址
     */
    private int broadcastPort = 8001;
}
