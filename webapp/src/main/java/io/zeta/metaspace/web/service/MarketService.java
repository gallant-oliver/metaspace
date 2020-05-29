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
 * @date 2019/5/28 14:55
 */
package io.zeta.metaspace.web.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.DataOwnerHeader;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.APIInfo;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.BusinessDAO;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.ColumnDAO;
import io.zeta.metaspace.web.dao.DataShareDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.mybatis.spring.MyBatisSystemException;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/*
 * @description
 * @author sunhaoning
 * @date 2019/5/28 14:55
 */
@Service
public class MarketService {

    private static final Logger LOG = LoggerFactory.getLogger(MarketService.class);

    @Autowired
    CategoryDAO categoryDao;
    @Autowired
    BusinessDAO businessDao;
    @Autowired
    DataShareDAO shareDao;
    @Autowired
    MetaDataService metaDataService;
    @Autowired
    UserDAO userDao;
    @Autowired
    ColumnDAO columnDAO;
    @Autowired
    DataShareService shareService;


    /**
     * 获取用户有权限的全部目录
     *
     * @param type
     * @return
     * @throws AtlasBaseException
     */
    public Set<CategoryEntityV2> getAll(int type,String tenantId) throws AtlasBaseException {
        try {
            Set<CategoryEntityV2> valueList = categoryDao.getAll(type,tenantId);
            return valueList;
        } catch (MyBatisSystemException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (SQLException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"数据库服务异常");
        }
    }


    public PageResult<BusinessInfoHeader> getBusinessListByName(Parameters parameters,String tenantId) throws AtlasBaseException {
        try {
            PageResult<BusinessInfoHeader> pageResult = new PageResult<>();
            String businessName = parameters.getQuery();
            businessName = (businessName == null ? "":businessName);
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<BusinessInfoHeader> businessInfoList = null;
            if(Objects.nonNull(businessName))
                businessName = businessName.replaceAll("%", "/%").replaceAll("_", "/_");
            businessInfoList = businessDao.queryBusinessByNameWithoutPrivilege(businessName, limit, offset,tenantId);

            for(BusinessInfoHeader infoHeader : businessInfoList) {
                String path = CategoryRelationUtils.getPath(infoHeader.getCategoryGuid(),tenantId);
                StringJoiner joiner = new StringJoiner(".");
                joiner.add(path);
                infoHeader.setPath(joiner.toString());
                String[] pathArr = path.split("/");
                String level2Category = "";
                if(pathArr.length >= 2)
                    level2Category = pathArr[1];
                infoHeader.setLevel2Category(level2Category);
            }
            //long businessCount = businessDao.queryBusinessCountByNameWithoutPrivilege(businessName);
            long businessCount = 0;
            if (businessInfoList.size()!=0){
                businessCount = businessInfoList.get(0).getTotal();
            }
            pageResult.setTotalSize(businessCount);
            pageResult.setLists(businessInfoList);
            pageResult.setCurrentSize(businessInfoList.size());
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    public TechnologyInfo getRelatedTableList(String businessId) throws AtlasBaseException {
        try {
            TechnologyInfo info = businessDao.queryTechnologyInfoByBusinessId(businessId);
            if(Objects.isNull(info)) {
                return info;
            }
            info.setEditTechnical(false);
            //tables
            List<TechnologyInfo.Table> tables = businessDao.queryTablesByBusinessId(businessId);
            tables.forEach(table -> {
                String displayName = columnDAO.getTableDisplayInfoByGuid(table.getTableGuid());
                if(Objects.nonNull(displayName)) {
                    table.setDisplayName(displayName);
                } else {
                    table.setDisplayName(table.getTableName());
                }
            });

            String trustTableGuid = businessDao.getTrustTableGuid(businessId);
            if(Objects.nonNull(trustTableGuid)) {
                TechnologyInfo.Table trustTable = tables.stream().filter(table -> table.getTableGuid().equals(trustTableGuid)).findFirst().get();
                if(Objects.nonNull(trustTable)) {
                    tables.remove(trustTable);
                    trustTable.setTrust(true);
                    tables.add(0, trustTable);
                } else {
                    tables.stream().findFirst().get().setTrust(true);
                }
            }
            info.setTables(tables);
            //businessId
            info.setBusinessId(businessId);
            return info;
        } catch (Exception e) {
            LOG.error("获取失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    public BusinessInfo getBusinessInfo(String businessId,String tenantId) throws AtlasBaseException {
        try {
            BusinessInfo info = businessDao.queryBusinessByBusinessId(businessId);
            info.setEditBusiness(false);
            String categoryGuid = info.getDepartmentId();
            String departmentName = categoryDao.queryNameByGuid(categoryGuid,tenantId);
            info.setDepartmentName(departmentName);
            return info;
        } catch (Exception e) {
            LOG.error("获取失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }


    public PageResult<APIInfoHeader> getBusinessTableRelatedAPI(String businessGuid, Parameters parameters,String tenantId) throws AtlasBaseException {
        try {
            TechnologyInfo technologyInfo = getRelatedTableList(businessGuid);
            PageResult<APIInfoHeader> pageResult = new PageResult<>();
            if(Objects.isNull(technologyInfo)) {
                return pageResult;
            }
            List<TechnologyInfo.Table> tableHeaderList = technologyInfo.getTables();
            List<String> tableList = new ArrayList<>();
            tableHeaderList.stream().forEach(table -> tableList.add(table.getTableGuid()));
            Integer limit = parameters.getLimit();
            Integer offset = parameters.getOffset();
            List<APIInfoHeader> apiList = new ArrayList<>();

            int apiCount = 0;
            if(Objects.nonNull(tableList) && tableList.size()>0) {
                apiList = shareDao.getTableRelatedAPI(tableList, limit, offset,tenantId);
                for (APIInfoHeader api : apiList) {
                    String displayName = api.getTableDisplayName();
                    if(Objects.isNull(displayName) || "".equals(displayName)) {
                        api.setTableDisplayName(api.getTableName());
                    }
                    List<DataOwnerHeader> dataOwner = metaDataService.getDataOwner(api.getTableGuid());
                    List<String> dataOwnerName = new ArrayList<>();
                    if(Objects.nonNull(dataOwner) && dataOwner.size()>0) {
                        dataOwner.stream().forEach(owner -> dataOwnerName.add(owner.getName()));
                    }
                    api.setDataOwner(dataOwnerName);
                }
                //apiCount = shareDao.countTableRelatedAPI(tableList);
                if (apiList.size()!=0){
                    apiCount = apiList.get(0).getTotal();
                }
            }
            pageResult.setTotalSize(apiCount);
            pageResult.setLists(apiList);
            pageResult.setCurrentSize(apiList.size());
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("查询失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    /**
     * API详情
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    public APIInfo getAPIInfo(String guid) throws AtlasBaseException {
        try {
            APIInfo info = shareService.getAPIInfo(guid);
            info.setStar(false);
            info.setEdit(false);
            return info;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取信息失败");
        }
    }
}
