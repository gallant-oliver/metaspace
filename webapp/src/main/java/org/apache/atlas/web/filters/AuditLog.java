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

package org.apache.atlas.web.filters;

import org.apache.atlas.web.util.DateTimeHelper;

import java.util.Date;

import javax.servlet.http.HttpServletResponse;

/**
 * 请求处理日志
 *
 * @author zhuxt
 * @create 2018-12-24
 **/
public class AuditLog {
    private static final char FIELD_SEP = '|';

    private final String userName;
    private final String fromAddress;
    private final String requestMethod;
    private final String requestUrl;
    private final Date requestTime;
    private       int    httpStatus;
    private       long   timeTaken;

    public AuditLog(String userName, String fromAddress, String requestMethod, String requestUrl) {
        this(userName, fromAddress, requestMethod, requestUrl, new Date());
    }

    public AuditLog(String userName, String fromAddress, String requestMethod, String requestUrl, Date requestTime) {
        this(userName, fromAddress, requestMethod, requestUrl, requestTime, HttpServletResponse.SC_OK, 0);
    }

    public AuditLog(String userName, String fromAddress, String requestMethod, String requestUrl, Date requestTime, int httpStatus, long timeTaken) {
        this.userName      = userName;
        this.fromAddress   = fromAddress;
        this.requestMethod = requestMethod;
        this.requestUrl    = requestUrl;
        this.requestTime   = requestTime;
        this.httpStatus    = httpStatus;
        this.timeTaken     = timeTaken;
    }

    public void setHttpStatus(int httpStatus) { this.httpStatus = httpStatus; }

    public void setTimeTaken(long timeTaken) { this.timeTaken = timeTaken; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("requestTime->").append(DateTimeHelper.formatDateUTC(requestTime))
                .append(FIELD_SEP).append("userName->").append(userName)
                .append(FIELD_SEP).append("fromAddress->").append(fromAddress)
                .append(FIELD_SEP).append("requestMethod->").append(requestMethod)
                .append(FIELD_SEP).append("requestUrl->").append(requestUrl)
                .append(FIELD_SEP).append("responseStatus->").append(httpStatus)
                .append(FIELD_SEP).append("timeTaken->").append(timeTaken).append("ms");

        return sb.toString();
    }
}
