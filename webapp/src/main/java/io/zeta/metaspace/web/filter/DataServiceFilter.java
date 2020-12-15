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

package io.zeta.metaspace.web.filter;

import com.google.gson.Gson;
import io.zeta.metaspace.model.share.QueryResult;
import io.zeta.metaspace.web.util.DataServiceUtil;
import io.zeta.metaspace.web.util.FilterUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.type.BaseAtlasType;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

/**
 * @author lixiang03
 * @Data 2020/8/19 14:00
 */
@Component
public class DataServiceFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String requestURL = httpServletRequest.getRequestURL().toString();
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        Gson gson = new Gson();
        QueryResult queryResult= null;
        if (FilterUtils.isDataService(requestURL)){
            response.setContentType("application/json;charset=utf-8");
            request.setCharacterEncoding("utf-8");
            try(PrintWriter printWriter = response.getWriter()){
                try {
                    queryResult = DataServiceUtil.queryApiData(httpServletRequest);
                } catch (AtlasBaseException e) {
                    Map<String, String> errorJsonMap = new LinkedHashMap<>();
                    AtlasErrorCode errorCode = e.getAtlasErrorCode();
                    errorJsonMap.put("errorCode", errorCode.getErrorCode());
                    errorJsonMap.put("errorMessage", e.getMessage());

                    if (e.getDetail() != null){
                        errorJsonMap.put("detail", e.getDetail());
                    }
                    if (e.getCause() != null) {
                        errorJsonMap.put("errorCause", e.getCause().getMessage());
                    }
                    httpServletResponse.setStatus(errorCode.getHttpCode().getStatusCode());
                    String errorJson = gson.toJson(errorJsonMap);
                    printWriter.print(errorJson);
                    return;
                }
                String jsonStr = gson.toJson(queryResult);

                printWriter.print(jsonStr);
            }
        }else{
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
