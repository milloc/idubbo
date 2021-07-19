package com.milloc.idubbo.provider.handler;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milloc.idubbo.domain.ExecArgs;
import com.milloc.idubbo.domain.ExecResult;
import com.milloc.idubbo.domain.Payload;
import com.milloc.idubbo.domain.Type;
import com.milloc.idubbo.provider.config.ProviderHolder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Exec
 *
 * @author gongdeming
 * @date 2021-07-15
 */
@Component
@Slf4j
@SuppressWarnings({"unchecked"})
public class Exec implements PayloadHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Type supports() {
        return Type.PROVIDER_EXEC;
    }

    @Override
    @SneakyThrows
    public Payload<ExecResult> handle(Payload<?> payload) {
        Object res = null;
        Exception err = null;
        try {
            Payload<ExecArgs> execPayload = (Payload<ExecArgs>) payload;
            ExecArgs execArgs = execPayload.getContent();
            Object bean = ProviderHolder.providerContext.findService(execArgs.getServiceName());
            String methodName = execArgs.getMethodName();

            Method targetMethod = null;
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            for (Class<?> anInterface : targetClass.getInterfaces()) {
                for (Method method : anInterface.getMethods()) {
                    if (method.toGenericString().equals(methodName)) {
                        targetMethod = method;
                    }
                }
            }
            Objects.requireNonNull(targetMethod);

            String[] strArgs = execArgs.getArgs();
            Class<?>[] types = targetMethod.getParameterTypes();
            if (types.length > 0) {
                Object[] args = new Object[types.length];
                for (int i = 0; i < types.length; i++) {
                    JavaType javaType = objectMapper.constructType(types[i]);
                    args[i] = objectMapper.readValue(strArgs[i], javaType);
                }
                res = targetMethod.invoke(bean, args);
            } else {
                res = targetMethod.invoke(bean);
            }
        } catch (Exception e) {
            log.error("invocation err", e);
            err = e;
        }

        ExecResult result = new ExecResult();
        result.setResult(objectMapper.writeValueAsString(res));
        if (err != null) {
            result.setOk(false);
            if (err instanceof InvocationTargetException) {
                result.setMsg(((InvocationTargetException) err).getTargetException().getMessage());
            } else {
                result.setMsg(err.getMessage());
            }
        } else {
            result.setOk(true);
        }
        return Payload.of(Type.PROVIDER_EXEC_RESULT, result);
    }
}
