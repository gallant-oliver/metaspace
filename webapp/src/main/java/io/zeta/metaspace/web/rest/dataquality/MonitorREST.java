package io.zeta.metaspace.web.rest.dataquality;

import io.zeta.metaspace.model.PagedModel;
import io.zeta.metaspace.model.RestReturn;
import io.zeta.metaspace.model.dataquality2.RuleStatistics;
import io.zeta.metaspace.model.dto.AlertRequest;
import io.zeta.metaspace.web.model.TemplateEnum;
import io.zeta.metaspace.web.service.dataquality.RuleService;
import io.zeta.metaspace.web.service.dataquality.WarningGroupService;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.io.IOUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 统一监控
 */
@Singleton
@Service
@Path("/dataquality/monitor")
public class MonitorREST {

    @Autowired
    private RuleService ruleService;

    @Autowired
    private WarningGroupService warningGroupService;

    @Context
    private HttpServletResponse response;

    /**
     * 获取数据质量监控
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    @GET
    @Path("")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<RuleStatistics> getStatistics(@QueryParam("startTime") String startTime, @QueryParam("endTime") String endTime) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            // 选择的结束日期需包含在内，应该要加一天
            Date endDate = format.parse(endTime);
            Timestamp realEndDate = new Timestamp(endDate.getTime() + 86400000);
            return ruleService.getRuleExecuteStatistics(new Timestamp(format.parse(startTime).getTime()), realEndDate);
        } catch (ParseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "时间格式错误");
        }
    }

    /**
     * 获取数据质量告警列表
     */
    @POST
    @Path("/alertInfo")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public RestReturn<PagedModel> getAlertInfo(@RequestBody AlertRequest request) {

        if (StringUtils.isNotBlank(request.getAlertType()) && !"数据质量告警".equals(request.getAlertType())) {
            return new RestReturn<PagedModel>().success(new PagedModel());
        }

        if (StringUtils.isNotBlank(request.getAlertLevel()) && !"2级".equals(request.getAlertLevel())) {
            return new RestReturn<PagedModel>().success(new PagedModel());
        }

        return new RestReturn<PagedModel>().success(warningGroupService.getAlert(request));
    }

    /**
     * 获取数据质量告警列表
     */
    @POST
    @Path("/alertTotalByTenant")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public RestReturn<Map<String,Long>> getAlertsByTenant(@RequestBody AlertRequest request) {

        return new RestReturn<Map<String,Long>>().success(warningGroupService.getAlertTotalByTenant(request));
    }

    /**
     * 下载告警列表
     */
    @GET
    @Path("/alert/download")
    @Valid
    public void download(@QueryParam("startTime") String startTime, @QueryParam("endTime") String endTime) throws Exception {
        Workbook workbook = warningGroupService.getAlertDownloadWorkbook(startTime, endTime);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("content-type:octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + UUID.randomUUID().toString() + ".xlsx");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        workbook.write(response.getOutputStream());
    }
}
