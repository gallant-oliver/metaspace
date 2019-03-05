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
 * @date 2019/3/4 9:55
 */
package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.homepage.BrokenLine;
import io.zeta.metaspace.model.homepage.TimeDBTB;
import io.zeta.metaspace.web.service.HomePageService;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/4 9:55
 */
@Path("home")
@Singleton
public class HomePageREST {
    @Autowired
    HomePageService homePageService;
    /**
     * 获取时间和总量
     *
     * @return List<Database>
     */
    @GET
    @Path("/total")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TimeDBTB getTimeDbTb() {

    }

    /**
     * 获取数据库数量趋势
     *
     * @return List<Database>
     */
    @GET
    @Path("/total")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BrokenLine getDBTotals() {

    }

    /**
     * 获取数据表数量趋势
     *
     * @return List<Database>
     */
    @GET
    @Path("/total")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BrokenLine getTBTotals() {

    }

    /**
     * 获取业务对象数量趋势
     *
     * @return List<Database>
     */
    @GET
    @Path("/total")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BrokenLine getBusinessTotals() {

    }
}
