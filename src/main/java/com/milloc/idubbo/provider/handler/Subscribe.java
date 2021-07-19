package com.milloc.idubbo.provider.handler;

import com.milloc.idubbo.domain.Payload;
import com.milloc.idubbo.domain.ProviderInfo;
import com.milloc.idubbo.domain.Type;
import com.milloc.idubbo.provider.config.ProviderHolder;
import org.springframework.stereotype.Component;

/**
 * 订阅
 *
 * @author gongdeming
 * @date 2021-07-15
 */
@Component
public class Subscribe implements PayloadHandler {
    @Override
    public Type supports() {
        return Type.SUBSCRIBE;
    }

    @Override
    public Payload<ProviderInfo> handle(Payload<?> ignore) {
        return Payload.of(Type.PROVIDERS, ProviderHolder.providerContext.getCurrentProviderInfo());
    }
}
