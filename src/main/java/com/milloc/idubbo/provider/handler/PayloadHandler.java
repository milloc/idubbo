package com.milloc.idubbo.provider.handler;

import com.milloc.idubbo.domain.Payload;
import com.milloc.idubbo.domain.Type;

/**
 * Handler
 *
 * @author gongdeming
 * @date 2021-07-15
 */
public interface PayloadHandler {
    /**
     * 处理某种类型
     *
     * @return 类型
     */
    Type supports();

    /**
     * 处理Event
     *
     * @param payload event
     * @return 结果
     */
    Payload<?> handle(Payload<?> payload);
}
