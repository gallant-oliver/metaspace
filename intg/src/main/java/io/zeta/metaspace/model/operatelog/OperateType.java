package io.zeta.metaspace.model.operatelog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

/**
 * 只有被标注的接口才会记录操作日志
 * @see io.zeta.metaspace.web.filter.OperateLogInterceptor
 */
public @interface OperateType {

    public OperateTypeEnum value();

}
