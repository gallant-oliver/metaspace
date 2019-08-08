package io.zeta.metaspace.web.filter;

import com.gridsum.gdp.library.commons.utils.UUIDUtils;

import io.zeta.metaspace.model.operatelog.OperateLog;
import io.zeta.metaspace.model.operatelog.OperateResultEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.service.OperateLogService;
import io.zeta.metaspace.web.util.AdminUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

/**
 * 记录操作日志
 * 使用方法:
 * 1.在接口的方法用加注解OperateType; value是对应的操作类型:插入、更新、删除等
 *
 * @see io.zeta.metaspace.model.operatelog.OperateType
 * 2.在接口方法的HttpServletRequest里添加attribute; key是`operatelog.object`, value是操作内容
 * @see io.zeta.metaspace.web.rest.DataStandardREST
 */
@Component
public class OperateLogInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(OperateLogInterceptor.class);

    /**
     * 接口需要在request的attribute里set操作对象
     */
    public static String OPERATELOG_MODULE = "operatelog_module";
    public static String OPERATELOG_CONTENT= "operatelog_content";

    @Autowired
    private OperateLogService operateLogService;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        OperateLog operateLog = new OperateLog();
        Method method = invocation.getMethod();
        OperateType operateType = method.getAnnotation(OperateType.class);
        operateLog.setType(operateType.value().getEn());
        try {
            Object response = invocation.proceed();
            operateLog.setResult(OperateResultEnum.SUCCESS.getEn());
            return response;
        } catch (Throwable e) {
            operateLog.setResult(OperateResultEnum.FAILED.getEn());
            throw e;
        } finally {
            Object content = request.getAttribute(OPERATELOG_CONTENT);
            if (content != null) {
                operateLog.setContent(content.toString());
            }
            Object module = request.getAttribute(OPERATELOG_MODULE);
            if (null != module) {
                operateLog.setModule(module.toString());
            }
            operateLog.setId(UUIDUtils.uuid());
            User userData = AdminUtils.getUserData();
            operateLog.setUserid(userData.getUserId());
            operateLog.setUsername(userData.getUsername());
            operateLog.setIp(request.getRemoteAddr());
            operateLog.setCreatetime(DateUtils.currentTimestamp());
            operateLogService.insert(operateLog);
        }
    }

}
