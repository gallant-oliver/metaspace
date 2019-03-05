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
 * @date 2019/3/4 9:56
 */
package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.homepage.BrokenLine;
import io.zeta.metaspace.model.homepage.SystemStatistical;
import io.zeta.metaspace.model.homepage.TimeDBTB;

import io.zeta.metaspace.utils.MetaspaceGremlin3QueryProvider;
import io.zeta.metaspace.utils.MetaspaceGremlinQueryProvider;
import io.zeta.metaspace.web.dao.HomePageDAO;
import io.zeta.metaspace.web.util.DateUtils;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.repository.graphdb.AtlasGraph;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/4 9:56
 */
public class HomePageService {
    @Autowired
    private HomePageDAO homePageDAO;
    @Autowired
    private AtlasGraph atlasGraph;
    @Autowired
    private MetaspaceGremlinQueryProvider metaspaceGremlinQueryProvider;
    public void statisticsJob() throws AtlasBaseException {
        String gremlinQuery = metaspaceGremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.DB_TOTAL_NUM_BY_QUERY);
        List<Long> DBCount = (List) atlasGraph.executeGremlinScript(String.format(gremlinQuery, ""), false);
        String countQuery = metaspaceGremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.TABLE_COUNT_BY_QUEERY);
        List<Long> TBcount = (List) atlasGraph.executeGremlinScript(String.format(countQuery, ""), false);
        long businessCount = homePageDAO.getBusinessCount();
        long addedBusinessCount = homePageDAO.getAddedBusinessCount();
        long noAddedBusinessCount = homePageDAO.getNoAddedBusinessCount();
        String uuid = UUID.randomUUID().toString();
        long yesterday = DateUtils.getYestoday().getTime();
        homePageDAO.addStatistical(uuid,yesterday,DBCount.get(0),SystemStatistical.DB_TOTAL.getCode());
        homePageDAO.addStatistical(uuid,yesterday,TBcount.get(0),SystemStatistical.TB_TOTAL.getCode());
        homePageDAO.addStatistical(uuid,yesterday,businessCount,SystemStatistical.BUSINESS_TOTAL.getCode());
        homePageDAO.addStatistical(uuid,yesterday,addedBusinessCount,SystemStatistical.BUSINESSE_ADD.getCode());
        homePageDAO.addStatistical(uuid,yesterday,noAddedBusinessCount,SystemStatistical.BUSINESSE_NO_ADD.getCode());
    }
    public TimeDBTB getTimeDbTb(){


    };
    public BrokenLine getDBTotals() {
        long time = DateUtils.getYestoday().getTime();
        long db = homePageDAO.getStatisticalByDateType(time,SystemStatistical.DB_TOTAL.getCode());
    }
    public BrokenLine getTBTotals() {
        long time = DateUtils.getYestoday().getTime();
        long tb = homePageDAO.getStatisticalByDateType(time,SystemStatistical.TB_TOTAL.getCode());
    }
}
