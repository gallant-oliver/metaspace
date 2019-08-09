package io.zeta.metaspace;

import io.zeta.metaspace.web.filter.OperateLogInterceptor;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhuxuetong
 * @date 2019-08-07 17:40
 */
public class HttpRequestContext {

    private static final ThreadLocal<HttpRequestContext> CURRENT_CONTEXT = new ThreadLocal<>();
    private HttpServletRequest request;

    public static HttpRequestContext get() {
        HttpRequestContext context = CURRENT_CONTEXT.get();
        if (null == context) {
            context = new HttpRequestContext();
            CURRENT_CONTEXT.set(context);
        }
        return context;
    }

    public void auditLog(String module, String content) {
        request.setAttribute(OperateLogInterceptor.OPERATELOG_MODULE, module);
        request.setAttribute(OperateLogInterceptor.OPERATELOG_CONTENT, content);
    }


    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
}
