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

import io.zeta.metaspace.discovery.MetaspaceGremlinService;
import io.zeta.metaspace.model.business.TechnicalStatus;
import io.zeta.metaspace.model.homepage.*;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.HomePageDAO;
import io.zeta.metaspace.web.util.DateUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/4 9:56
 */
@Service
public class HomePageService {
    private static final Logger LOG = LoggerFactory.getLogger(HomePageService.class);
    @Autowired
    private HomePageDAO homePageDAO;
    @Autowired
    private MetaspaceGremlinService metaspaceGremlinService;

    private static final String sourceLayerCategoryGuid = "1";

    public void statisticsJob() throws AtlasBaseException {
        long yesterday = DateUtils.getYesterday().getTime();
        product(yesterday);
    }

    private void product(long date) throws AtlasBaseException {
        homePageDAO.deleteStatistical(date);
        List<Long> dbTotal = metaspaceGremlinService.getDBTotal();
        List<Long> tbTotal = metaspaceGremlinService.getTBTotal();
        long businessCount = homePageDAO.getBusinessCount();
        long addedBusinessCount = homePageDAO.getAddedBusinessCount();
        long noAddedBusinessCount = homePageDAO.getNoAddedBusinessCount();
        String uuid = UUID.randomUUID().toString();
        homePageDAO.addStatistical(uuid, date, dbTotal.get(0), SystemStatistical.DB_TOTAL.getCode());
        uuid = UUID.randomUUID().toString();
        homePageDAO.addStatistical(uuid, date, tbTotal.get(0), SystemStatistical.TB_TOTAL.getCode());
        uuid = UUID.randomUUID().toString();
        homePageDAO.addStatistical(uuid, date, businessCount, SystemStatistical.BUSINESS_TOTAL.getCode());
        uuid = UUID.randomUUID().toString();
        homePageDAO.addStatistical(uuid, date, addedBusinessCount, SystemStatistical.BUSINESSE_ADD.getCode());
        uuid = UUID.randomUUID().toString();
        homePageDAO.addStatistical(uuid, date, noAddedBusinessCount, SystemStatistical.BUSINESSE_NO_ADD.getCode());
    }

    public void testProduct(String date) throws AtlasBaseException {
        try {
            long aLong = DateUtils.getLong(date);
            product(aLong);
        } catch (Exception e) {
            LOG.error("异常了", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "异常了");
        }

    }

    @Cacheable(value = "TimeAndDbCache", key = "'TimeAndDbCache'")
    public TimeDBTB getTimeDbTb() throws AtlasBaseException {
        try {
            String date = DateUtils.getNow2();
            TimeDBTB timeDBTB = new TimeDBTB();

            CompletableFuture<List<Long>> dbTotalFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return metaspaceGremlinService.getDBTotal();
                } catch (Exception e) {
                    LOG.error("查询库总量失败", e);
                }
                return null;
            });

            CompletableFuture<List<Long>> tbTotalFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return metaspaceGremlinService.getTBTotal();
                } catch (Exception e) {
                    LOG.error("查询表总量失败", e);
                }
                return null;
            });

            CompletableFuture<Long> subSystemFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return homePageDAO.getSubSystemTotal(sourceLayerCategoryGuid);
                } catch (Exception e) {
                    LOG.error("查询贴源层子系统数量失败", e);
                }
                return null;
            });

            CompletableFuture<List<CategoryDBInfo>> categoryRelatedDBCountFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return homePageDAO.getCategoryRelatedDBCount(sourceLayerCategoryGuid, -1, 0);
                } catch (Exception e) {
                    LOG.error("查询贴源层逻辑库和实体库数量失败", e);
                }
                return null;
            });
            CompletableFuture<Void> future = CompletableFuture.allOf(dbTotalFuture, tbTotalFuture, subSystemFuture, categoryRelatedDBCountFuture);
            future.join();
            List<Long> dbTotal = dbTotalFuture.get();
            List<Long> tbTotal = tbTotalFuture.get();
            Long subSystemTotal = subSystemFuture.get();
            List<CategoryDBInfo> categoryRelatedDBCount = categoryRelatedDBCountFuture.get();
            long entityDBTotal=0;
            long logicDBTotal=0;
            for (CategoryDBInfo categoryDBInfo : categoryRelatedDBCount) {
                entityDBTotal+=categoryDBInfo.getEntityDBTotal();
                logicDBTotal+=categoryDBInfo.getLogicDBTotal();
            }
            timeDBTB.setDate(date);
            timeDBTB.setDatabaseTotal(dbTotal.get(0));
            timeDBTB.setTableTotal(tbTotal.get(0));
            timeDBTB.setSubsystemTotal(subSystemTotal);
            timeDBTB.setSourceEntityDBTotal(entityDBTotal);
            timeDBTB.setSourceLogicDBTotal(logicDBTotal);
            return timeDBTB;
        } catch (Exception e) {
            LOG.error("获取统计信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取统计信息失败");
        }
    }

    @Cacheable(value = "DbTotalCache", key = "'DbTotalCache'")
    public BrokenLine getDBTotals() throws AtlasBaseException {
        BrokenLine brokenLine = new BrokenLine();
        addBrokenLine(brokenLine, SystemStatistical.DB_TOTAL);
        return brokenLine;
    }


    private void addBrokenLine(BrokenLine brokenLine, SystemStatistical systemStatistical) throws AtlasBaseException {
        List<String> dates = brokenLine.getDate() == null ? new ArrayList<>() : brokenLine.getDate();
        List<String> names = brokenLine.getName() == null ? new ArrayList<>() : brokenLine.getName();
        List<List<Long>> data = brokenLine.getData() == null ? new ArrayList<>() : brokenLine.getData();
        names.add(systemStatistical.getDesc());
        long startDate = DateUtils.get29day().getTime();
        long endDate = DateUtils.getYesterday().getTime();
        if (dates.size() < 30) {
            for (long time = startDate; time <= DateUtils.getToday().getTime(); time = DateUtils.getNext(time).getTime()) {
                dates.add(DateUtils.getDate(time));
            }
        }
        ArrayList<Long> list = new ArrayList<>();

        List<DateStatistical> statisticalByDateType = homePageDAO.getStatisticalByDateType(startDate, endDate, systemStatistical.getCode());
        Map<Long, Long> map = new HashMap<>();
        for (DateStatistical dateStatistical : statisticalByDateType) {
            map.put(dateStatistical.getDate(), dateStatistical.getStatistical());
        }
        for (long time = startDate; time < DateUtils.getToday().getTime(); time = DateUtils.getNext(time).getTime()) {
            long date = time;
            if (map.containsKey(date)) {
                list.add(map.get(date));
            } else {
                list.add(getYesterdayStatistical(map, time, startDate));
            }

        }


        switch (systemStatistical) {
            case DB_TOTAL: {
                long aLong = metaspaceGremlinService.getDBTotal().get(0);
                switchCase(startDate, list, statisticalByDateType, map, aLong);
                break;
            }
            case TB_TOTAL: {
                long aLong = metaspaceGremlinService.getTBTotal().get(0);
                switchCase(startDate, list, statisticalByDateType, map, aLong);
                break;
            }
            case BUSINESS_TOTAL: {
                long aLong = homePageDAO.getBusinessCount();
                switchCase(startDate, list, statisticalByDateType, map, aLong);
                break;
            }
            case BUSINESSE_ADD: {
                long aLong = homePageDAO.getAddedBusinessCount();
                switchCase(startDate, list, statisticalByDateType, map, aLong);
                break;
            }
            case BUSINESSE_NO_ADD: {
                long aLong = homePageDAO.getNoAddedBusinessCount();
                switchCase(startDate, list, statisticalByDateType, map, aLong);
                break;
            }
        }
        data.add(list);
        brokenLine.setData(data);
        brokenLine.setDate(dates);
        brokenLine.setName(names);
    }

    private void switchCase(long startDate, ArrayList<Long> list, List<DateStatistical> statisticalByDateType, Map<Long, Long> map, Long aLong) throws AtlasBaseException {
        try {
            list.add(aLong);
        } catch (Exception e) {
            if (statisticalByDateType.size() != 0)
                list.add(getYesterdayStatistical(map, DateUtils.getToday().getTime(), startDate));
            else
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取统计信息失败");
        }
        return;
    }

    private long getYesterdayStatistical(Map<Long, Long> statisticalByDateType, long date, long startTime) {
        long statistical = 0;
        while (statistical == 0 && date >= startTime) {
            date = DateUtils.getlast(date).getTime();
            if (statisticalByDateType.containsKey(date)) {
                return statisticalByDateType.get(date);
            }
        }
        return statistical;
    }

    @Cacheable(value = "TbTotalCache", key = "'TbTotalCache'")
    public BrokenLine getTBTotals() throws AtlasBaseException {
        BrokenLine brokenLine = new BrokenLine();
        addBrokenLine(brokenLine, SystemStatistical.TB_TOTAL);
        return brokenLine;
    }

    @Cacheable(value = "BusinessTotalCache", key = "'BusinessTotalCache'")
    public BrokenLine getBusinessTotals() throws AtlasBaseException {
        BrokenLine brokenLine = new BrokenLine();
        addBrokenLine(brokenLine, SystemStatistical.BUSINESS_TOTAL);
        addBrokenLine(brokenLine, SystemStatistical.BUSINESSE_ADD);
        addBrokenLine(brokenLine, SystemStatistical.BUSINESSE_NO_ADD);
        return brokenLine;
    }

    /**
     * 获取数据表使用次数与占比topN
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @Cacheable(value = "TableUseProportionCache", key = "'TableUseProportionCache' + #parameters.limit + #parameters.offset")
    public PageResult<TableUseInfo> getTableRelatedInfo(Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<TableUseInfo> pageResult = new PageResult<>();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<TableUseInfo> tableList = homePageDAO.getTableRelatedInfo(limit, offset);
            tableList.forEach(table -> {
                String displayName = table.getDisplayName();
                if(Objects.isNull(displayName) || "".equals(displayName.trim())) {
                    table.setDisplayName(table.getTableName());
                }
            });
            Map<String, Long> totalQuery = homePageDAO.getTotalInfo();
            long total = totalQuery.get("totalbusiness");
            DecimalFormat df = new DecimalFormat("0.00");
            tableList.stream().forEach(info -> info.setProportion(String.valueOf(df.format((float) info.getTimes() / total))));
            long sum = totalQuery.get("totaltable");
            pageResult.setLists(tableList);
            pageResult.setCurrentSize(tableList.size());
            pageResult.setTotalSize(sum);
            return pageResult;
        } catch (Exception e) {
            LOG.error("查询异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询异常");
        }
    }

    /**
     * 获取已补充/未补充技术信息的业务对象占比
     * @return
     * @throws AtlasBaseException
     */
    @Cacheable(value = "TechnicalSupplementProportionCache", key = "'TechnicalSupplementProportionCache'")
    public List<DataDistribution> getDataDistribution() throws AtlasBaseException {
        try {
            List<DataDistribution> dataDistributionList = new ArrayList<>();
            DataDistribution addedData = new DataDistribution();
            long addedNumber = homePageDAO.getTechnicalStatusNumber(TechnicalStatus.ADDED.code);
            addedData.setName("已补充技术信息");
            addedData.setValue(addedNumber);
            dataDistributionList.add(addedData);

            DataDistribution blankData = new DataDistribution();
            long blankNumber = homePageDAO.getTechnicalStatusNumber(TechnicalStatus.BLANK.code);
            blankData.setName("未补充技术信息");
            blankData.setValue(blankNumber);
            dataDistributionList.add(blankData);
            return dataDistributionList;
        } catch (Exception e) {
            LOG.error("查询异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询异常");
        }
    }

    /**
     * 获取贴源层系统列表
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @Cacheable(value = "SourceLayerListCache", key = "'SourceLayerListCache' + #parameters.limit + #parameters.offset")
    public PageResult<CategoryDBInfo> getCategoryRelatedDB(Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<CategoryDBInfo> pageResult = new PageResult<>();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();

            List<CategoryDBInfo> categoryDBInfoList = homePageDAO.getCategoryRelatedDBCount(sourceLayerCategoryGuid, limit, offset);
            pageResult.setLists(categoryDBInfoList);
            pageResult.setCurrentSize(categoryDBInfoList.size());
            long sum = homePageDAO.getCountCategory(sourceLayerCategoryGuid);
            pageResult.setTotalSize(sum);
            return pageResult;
        } catch (Exception e) {
            LOG.error("查询异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询异常");
        }
    }

    /**
     * 获取贴源层子系统列表
     * @param categoryGuid
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @Cacheable(value = "SourceChildLayerListCache", key = "'SourceChildLayerListCache' + #categoryGuid + #parameters.limit + #parameters.offset")
    public PageResult<CategoryDBInfo> getChildCategoryRelatedDB(String categoryGuid, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<CategoryDBInfo> pageResult = new PageResult<>();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<CategoryDBInfo> categoryDBInfoList = homePageDAO.getChildSystemDBCount(categoryGuid, limit, offset);
            pageResult.setLists(categoryDBInfoList);
            pageResult.setCurrentSize(categoryDBInfoList.size());
            long sum = homePageDAO.getCountCategory(categoryGuid);
            pageResult.setTotalSize(sum);
            return pageResult;
        } catch (Exception e) {
            LOG.error("查询异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询异常");
        }
    }


    @CacheEvict(value = {"TableByDBCache", "TableUseProportionCache", "RoleUseProportionCache", "TechnicalSupplementProportionCache", "SourceLayerListCache",
            "SourceChildLayerListCache", "TimeAndDbCache", "DbTotalCache", "TbTotalCache", "BusinessTotalCache", "RoleUserListCache", "RoleCache"}, allEntries = true)
    public void refreshCache() throws AtlasBaseException {

    }
}
