package io.zeta.metaspace.web.filter;

import io.zeta.metaspace.model.datastandard.DataStandard;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * 字段校验拦截器
 * 使用方法:
 * 1.要校验的方法需要加注解 @Valid
 * 2.bean的字段上加相关注解
 * @see io.zeta.metaspace.web.rest.DataStandardREST#insert(DataStandard)
 */
@Component
public class FieldValidatorInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(FieldValidatorInterceptor.class);

    private Validator validator;

    public FieldValidatorInterceptor() {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        List<String> errorMessages = new ArrayList<String>();

        for (Object parameter : invocation.getArguments()) {
            Set<ConstraintViolation<Object>> violations = validator.validate(parameter);
            for (ConstraintViolation<Object> violation : violations) {
                errorMessages.add(violation.getPropertyPath().toString() + ":" + violation.getMessage());
            }
        }

        if (!errorMessages.isEmpty()) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(errorMessages)
                            .build());
        }

        return invocation.proceed();
    }

}
