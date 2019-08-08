// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/7/11 18:10
 */
package io.zeta.metaspace.web.filter;

import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.utils.PageUtils;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/11 18:10
 */
@Component
public class PrivilegeCheckAdvisor extends AbstractPointcutAdvisor {

    private final StaticMethodMatcherPointcut pointcut = new StaticMethodMatcherPointcut() {
        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            boolean postAnnotation = method.isAnnotationPresent(POST.class);
            boolean getAnnotation = method.isAnnotationPresent(GET.class);
            boolean putAnnotation = method.isAnnotationPresent(PUT.class);
            boolean delAnnotation = method.isAnnotationPresent(DELETE.class);
            return postAnnotation||getAnnotation||putAnnotation||delAnnotation;
        }
    };

    private final PrivilegeCheckInterceptor interceptor;

    @Inject
    public PrivilegeCheckAdvisor(PrivilegeCheckInterceptor interceptor) {
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
