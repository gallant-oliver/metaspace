package io.zeta.metaspace.web.filter;

import io.zeta.metaspace.model.operatelog.OperateType;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import javax.inject.Inject;

@Component
public class OperateLogAdvisor extends AbstractPointcutAdvisor {

    private final StaticMethodMatcherPointcut pointcut = new StaticMethodMatcherPointcut() {
        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            boolean annotationPresent = method.isAnnotationPresent(OperateType.class);
            return annotationPresent;
        }
    };

    private final OperateLogInterceptor interceptor;

    @Inject
    public OperateLogAdvisor(OperateLogInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public Advice getAdvice() {
        return interceptor;
    }

}
