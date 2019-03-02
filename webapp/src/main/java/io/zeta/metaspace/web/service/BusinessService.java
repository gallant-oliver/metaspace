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
 * @date 2019/2/12 14:56
 */
package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.business.BusinessQueryParameter;
import io.zeta.metaspace.model.business.BusinessRelationEntity;
import io.zeta.metaspace.model.business.TechnicalStatus;
import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.web.dao.BusinessDAO;
import io.zeta.metaspace.web.dao.BusinessRelationDAO;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.PrivilegeDAO;
import io.zeta.metaspace.web.dao.RoleDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/12 14:56
 */
@Service
public class BusinessService {

    @Autowired
    BusinessDAO businessDao;
    @Autowired
    BusinessRelationDAO relationDao;
    @Autowired
    CategoryDAO categoryDao;
    @Autowired
    PrivilegeDAO privilegeDao;
    @Autowired
    RoleDAO roleDao;
    @Autowired
    RoleService roleService;

    private static final int FINISHED_STATUS = 1;
    private static final int BUSINESS_MODULE = 3;
    private static final int TECHNICAL_MODULE = 4;
    private static final int BUSINESS_TYPE = 1;

    @Transactional
    public int addBusiness(String categoryId, BusinessInfo info) throws AtlasBaseException {
        try {
            //departmentId(categoryId)
            info.setDepartmentId(categoryId);
            //submitter && businessOperator
            String userName = AdminUtils.getUserName();
            info.setSubmitter(userName);
            info.setBusinessOperator(userName);
            //businessId
            String businessId = UUID.randomUUID().toString();
            info.setBusinessId(businessId);
            //submissionTime && businessLastUpdate && ticketNumber
            long timestamp = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(timestamp);
            info.setSubmissionTime(time);
            info.setBusinessLastUpdate(time);
            info.setTicketNumber(String.valueOf(timestamp));
            //level2CategoryId
            String pathStr = categoryDao.queryGuidPathByGuid(categoryId);
            String path = pathStr.substring(1, pathStr.length()-1);
            path = path.replace("\"", "");
            String[] pathArr = path.split(",");
            String level2CategoryId = "";
            if(pathArr.length >= 2) {
                level2CategoryId = pathArr[1];
            }
            info.setLevel2CategoryId(level2CategoryId);
            int insertFlag =  businessDao.insertBusinessInfo(info);

            //更新business编辑状态
            businessDao.updateBusinessStatus(businessId, FINISHED_STATUS);
            //更新technical编辑状态
            businessDao.updateTechnicalStatus(businessId, TechnicalStatus.BLANK.code);

            BusinessRelationEntity entity = new BusinessRelationEntity();
            //relationshiGuid
            String relationGuid = UUID.randomUUID().toString();
            entity.setRelationshipGuid(relationGuid);



            entity.setBusinessId(businessId);
            entity.setCategoryGuid(categoryId);
            int relationFlag = relationDao.addRelation(entity);
            return insertFlag & relationFlag;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加失败");
        }
    }

    public int updateBusiness(String businessId, BusinessInfo info) throws AtlasBaseException {
        try {
            String userName = AdminUtils.getUserName();
            long timestamp = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(timestamp);
            info.setBusinessOperator(userName);
            info.setBusinessLastUpdate(time);
            info.setBusinessId(businessId);
            return businessDao.updateBusinessInfo(info);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修改失败");
        }
    }

    public List<BusinessInfo> getBusinessList(String departmentId, int limit, int offset) throws AtlasBaseException {
        try {
            return businessDao.queryBusinessByDemparmentId(departmentId, limit, offset);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    public BusinessInfo getBusinessInfo(String businessId) throws AtlasBaseException {
        try {
            BusinessInfo info = businessDao.queryBusinessByBusinessId(businessId);
            String userId = AdminUtils.getUserData().getUserId();
            boolean editBusiness = privilegeDao.queryModulePrivilegeByUser(userId, BUSINESS_MODULE) == 0 ? false:true;
            info.setEditBusiness(editBusiness);
            String categoryGuid = info.getDepartmentId();
            String departmentName = categoryDao.queryNameByGuid(categoryGuid);
            info.setDepartmentName(departmentName);

            return info;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    public TechnologyInfo getRelatedTableList(String businessId) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            //technicalLastUpdate && technicalOperator
            TechnologyInfo info = businessDao.queryTechnologyInfoByBusinessId(businessId);
            //editTechnical
            if(Objects.isNull(info))
                info = new TechnologyInfo();
            boolean editTechnical = privilegeDao.queryModulePrivilegeByUser(userId, TECHNICAL_MODULE) == 0 ? false:true;
            info.setEditTechnical(editTechnical);
            //tables
            List<TechnologyInfo.Table> tables = businessDao.queryTablesByBusinessId(businessId);
            info.setTables(tables);
            //businessId
            info.setBusinessId(businessId);
            return info;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    public PageResult<BusinessInfoHeader> getBusinessListByCategoryId(String categoryId, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<BusinessInfoHeader> pageResult = new PageResult<>();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<BusinessInfoHeader>  list = businessDao.queryBusinessByCatetoryIdWithLimit(categoryId, limit, offset);
            String path = CategoryRelationUtils.getPath(categoryId);
            StringJoiner joiner = new StringJoiner(".");
            String[] pathArr = path.split("\\.");
            String level2Category = "";
            if(pathArr.length >= 2)
                level2Category = pathArr[1];
            for(BusinessInfoHeader infoHeader : list) {
                //path
                joiner.add(path).add(infoHeader.getName());
                infoHeader.setPath(joiner.toString());
                //level2Category
                infoHeader.setLevel2Category(level2Category);
            }
            long sum = businessDao.queryBusinessCountByByCatetoryId(categoryId);
            pageResult.setSum(sum);
            pageResult.setCount(list.size());
            pageResult.setLists(list);
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    public PageResult<BusinessInfoHeader> getBusinessListByName(Parameters parameters) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            String roleId = roleDao.getRoleIdByUserId(userId);
            PageResult<BusinessInfoHeader> pageResult = new PageResult<>();
            String businessName = parameters.getQuery();
            businessName = (businessName == null ? "":businessName);
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<BusinessInfoHeader> businessInfoList = null;
            List<String> categoryIds = CategoryRelationUtils.getPermissionCategoryList(roleId, BUSINESS_TYPE);
            businessInfoList = businessDao.queryBusinessByName(businessName, categoryIds, limit, offset);

            for(BusinessInfoHeader infoHeader : businessInfoList) {
                String path = CategoryRelationUtils.getPath(infoHeader.getCategoryGuid());
                StringJoiner joiner = new StringJoiner(".");
                joiner.add(path).add(infoHeader.getName());
                infoHeader.setPath(joiner.toString());
                String[] pathArr = path.split("\\.");
                String level2Category = "";
                if(pathArr.length >= 2)
                    level2Category = pathArr[1];
                infoHeader.setLevel2Category(level2Category);
            }
            long businessCount = businessDao.queryBusinessCountByName(businessName, categoryIds);
            pageResult.setSum(businessCount);
            pageResult.setLists(businessInfoList);
            pageResult.setCount(businessInfoList.size());
            return pageResult;
        } catch (AtlasBaseException e) {
          throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }


    public PageResult<BusinessInfoHeader> getBusinessListByCondition(BusinessQueryParameter parameter) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            String roleId = roleDao.getRoleIdByUserId(userId);
            PageResult<BusinessInfoHeader> pageResult = new PageResult<>();
            String status = parameter.getStatus();
            String ticketNumber = parameter.getTicketNumber();
            ticketNumber = (ticketNumber==null ? "":ticketNumber);
            String businessName = parameter.getBusinessName();
            businessName = (businessName==null ? "":businessName);
            String level2CategoryId = parameter.getLevel2CategoryId();
            String  submitter = parameter.getSubmitter();
            submitter = (submitter==null ? "":submitter);
            int limit = parameter.getLimit();
            int offset = parameter.getOffset();
            Integer technicalStatus = TechnicalStatus.getCodeByDesc(status);
            List<String> categoryIds = CategoryRelationUtils.getPermissionCategoryList(roleId, BUSINESS_TYPE);
            List<BusinessInfoHeader> businessInfoList = businessDao.queryBusinessByCondition(categoryIds, technicalStatus, ticketNumber, businessName, level2CategoryId, submitter, limit, offset);
            for(BusinessInfoHeader infoHeader : businessInfoList) {
                String categoryId = businessDao.queryCategoryIdByBusinessId(infoHeader.getBusinessId());
                String path = CategoryRelationUtils.getPath(categoryId);
                infoHeader.setPath(path + "." + infoHeader.getName());
                String[] pathArr = path.split("\\.");
                if(pathArr.length >= 2)
                    infoHeader.setLevel2Category(pathArr[1]);
            }
            pageResult.setLists(businessInfoList);
            long businessCount = businessDao.queryBusinessCountByCondition(categoryIds, technicalStatus, ticketNumber, businessName, level2CategoryId, submitter);
            pageResult.setSum(businessCount);
            pageResult.setCount(businessInfoList.size());
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    @Transactional
    public void addBusinessAndTableRelation(String businessId, List<String> tableIdList) throws AtlasBaseException {
        try {
            String userName = AdminUtils.getUserName();
            long timestamp = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(timestamp);
            businessDao.updateTechnicalInfo(businessId, userName, time);

            //更新technical编辑状态
            businessDao.updateTechnicalStatus(businessId, TechnicalStatus.ADDED.code);
            businessDao.deleteRelationByBusinessId(businessId);
            for(String guid : tableIdList) {
                businessDao.insertTableRelation(businessId, guid);
            }
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }
}
