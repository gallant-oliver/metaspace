package io.zeta.metaspace.web.filter;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.validation.Valid;

@Component
public class FieldValidatorAdvisor extends AbstractPointcutAdvisor {

    private final StaticMethodMatcherPointcut pointcut = new StaticMethodMatcherPointcut() {
        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            boolean annotationPresent = method.isAnnotationPresent(Valid.class);
            return annotationPresent;
        }
    };

    private final FieldValidatorInterceptor interceptor;

    @Inject
    public FieldValidatorAdvisor(FieldValidatorInterceptor interceptor) {
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
