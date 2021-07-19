package com.milloc.idubbo.client.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milloc.idubbo.domain.ExecArgs;
import com.milloc.idubbo.domain.ExecResult;
import com.milloc.idubbo.domain.ProviderId;
import com.milloc.idubbo.transport.Requests;
import com.milloc.idubbo.domain.Payload;
import com.milloc.idubbo.domain.Type;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * IDubboClientFactoryBean
 *
 * @author gongdeming
 * @date 2021-07-12
 */
@Slf4j
public class ClientProxyFactoryBean implements FactoryBean<Object>{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final Class<?> innerInterface;

    public ClientProxyFactoryBean(Class<?> innerInterface) {
        this.innerInterface = innerInterface;
    }

    @Override
    public Object getObject() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{innerInterface}, new RemoteProviderExecHandler());
    }

    @Override
    public Class<?> getObjectType() {
        return this.innerInterface;
    }

    private class RemoteProviderExecHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            ProvidersContext context = ProvidersHolder.getContext();
            String serviceName = innerInterface.getName();

            log.info("proxy exec method {}", method);
            log.info("args {}", args);
            log.info("remote provider exec");
            log.info("serviceName {}", serviceName);
            ProviderId provider = context.chooseProvider(serviceName);
            log.info("chosen provider {}", provider);

            ExecArgs execArgs = new ExecArgs();
            execArgs.setServiceName(serviceName);
            execArgs.setMethodName(method.toGenericString());
            if (args != null && args.length > 0) {
                String[] stringArgs = new String[args.length];
                for (int i = 0; i < args.length; i++) {
                    stringArgs[i] = OBJECT_MAPPER.writeValueAsString(args[i]);
                }
                execArgs.setArgs(stringArgs);
            } else {
                execArgs.setArgs(new String[0]);
            }
            Payload<ExecArgs> execArgsPayload = Payload.of(Type.PROVIDER_EXEC, execArgs);

            log.info("remote exec {}", execArgs);
            Payload<ExecResult> execResult = Requests.request(execArgsPayload, provider.getIp(), provider.getPort());
            log.info("remote exec result {}", execResult);
            ExecResult res = execResult.getContent();

            if (res.isOk()) {
                Class<?> returnType = method.getReturnType();
                if (returnType == Void.class) {
                    log.info("proxy exec return void");
                    return null;
                } else {
                    String result = res.getResult();
                    JavaType javaType = OBJECT_MAPPER.constructType(returnType);
                    Object proxyValue = OBJECT_MAPPER.readValue(result, javaType);
                    log.info("proxy exec return {}", proxyValue);
                    return proxyValue;
                }
            } else {
                log.error("proxy exec err {}", res.getMsg());
                throw new RuntimeException(res.getMsg());
            }
        }
    }
}
