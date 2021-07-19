package com.milloc.idubbo.client.config;

/**
 * ProvidersHolder
 *
 * @author gongdeming
 * @date 2021-07-15
 */
public class ProvidersHolder {
    private static ProvidersContext providersContext = null;

    static void init(ProvidersContext providersContext) {
        ProvidersHolder.providersContext = providersContext;
    }

    public static ProvidersContext getContext() {
        return ProvidersHolder.providersContext;
    }
}
