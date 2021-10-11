package io.zeta.metaspace.web.rest.dataquality;

import io.zeta.metaspace.model.dataquality2.RuleStatistics;
import io.zeta.metaspace.web.service.dataquality.RuleService;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 统一监控
 */
@Singleton
@Service
@Path("/dataquality/monitor")
public class MonitorREST {

    @Autowired
    private RuleService ruleService;

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
}
