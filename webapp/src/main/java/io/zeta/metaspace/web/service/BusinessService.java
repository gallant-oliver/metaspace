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
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.dao.BusinessDAO;
import io.zeta.metaspace.web.dao.BusinessRelationDAO;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.PrivilegeDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
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

    private static final int FINISHED_STATUS = 1;
    private static final int BUSINESS_MODULE = 3;
    private static final int TECHNICAL_MODULE = 4;

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

            int insertFlag =  businessDao.insertBusinessInfo(info);

            //更新business编辑状态
            businessDao.updateBusinessStatus(businessId, FINISHED_STATUS);
            //更新technical编辑状态
            businessDao.updateTechnicalStatus(businessId, TechnicalStatus.BLANK.code);

            BusinessRelationEntity entity = new BusinessRelationEntity();
            //relationshiGuid
            String relationGuid = UUID.randomUUID().toString();
            entity.setRelationshipGuid(relationGuid);
            //path
            String qualifiedName = categoryDao.queryQualifiedName(categoryId);
            if (Objects.nonNull(qualifiedName)) {
                qualifiedName += "." + info.getName();
            }
            entity.setPath(qualifiedName);
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
            boolean editTechnical = privilegeDao.queryModulePrivilegeByUser(userId, TECHNICAL_MODULE) == 0 ? false:true;

            info.setEditBusiness(editBusiness);
            info.setEditTechnical(editTechnical);

            List<BusinessInfo.Table> tables = businessDao.queryTablesByBusinessId(businessId);
            info.setTables(tables);
            return info;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    public List<BusinessInfo> getBusinessListByCategoryId(String categoryId, int limit, int offset) throws AtlasBaseException {
        try {
            return businessDao.queryBusinessByCatetoryIdWithLimit(categoryId, limit, offset);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    public PageResult<BusinessInfoHeader> getBusinessListByName(String categoryId, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<BusinessInfoHeader> pageResult = new PageResult<>();
            String businessName = parameters.getQuery();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<BusinessInfoHeader> businessInfoList = null;
            if(limit != -1) {
                businessInfoList = businessDao.queryBusinessByNameWithLimit(categoryId, businessName, limit, offset);
            } else {
                businessInfoList = businessDao.queryBusinessByName(categoryId, businessName, offset);
            }
            String path = getCategoryPath(categoryId);
            String[] pathArr = path.split("\\.");
            String level2Category = "";
            if(pathArr.length >= 2)
                level2Category = pathArr[1];
            for(BusinessInfoHeader infoHeader : businessInfoList) {
                infoHeader.setPath(path);
                infoHeader.setLevel2Category(level2Category);
            }

            long businessCount = businessDao.queryBusinessCountByName(categoryId, businessName);
            pageResult.setSum(businessCount);
            pageResult.setLists(businessInfoList);
            pageResult.setCount(businessInfoList.size());
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    public String getCategoryPath(String categoryId) throws AtlasBaseException {
        try {
            String pathStr = categoryDao.queryPathByGuid(categoryId);
            String path = pathStr.substring(1, pathStr.length()-1);
            path = path.replace(",",".");
       /* List<String> pathList = new ArrayList<>();
        try {
            CategoryEntityV2 entity = categoryDao.queryByGuid(categoryId);
            String categoryName = entity.getName();
            pathList.add(categoryName);
            String parentCategoryGuid = entity.getParentCategoryGuid();
            while(Objects.nonNull(parentCategoryGuid)) {
                entity = categoryDao.queryByGuid(parentCategoryGuid);
                parentCategoryGuid = entity.getParentCategoryGuid();
                categoryName = entity.getName();
                pathList.add(categoryName);
            }
            StringBuilder path = new StringBuilder();
            for(int i=pathList.size()-1; i>=0; i--) {
                path.append(pathList.get(i));
                if(i != 0)
                    path.append(".");
            }*/
            return path;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

    public PageResult<BusinessInfoHeader> getBusinessListByCondition(BusinessQueryParameter parameter) throws AtlasBaseException {
        try {
            PageResult<BusinessInfoHeader> pageResult = new PageResult<>();
            String status = parameter.getStatus();
            String ticketNumber = parameter.getTicketNumber();
            String businessName = parameter.getBusinessName();
            String level2Category = parameter.getLevel2Category();
            String  submitter = parameter.getSubmitter();
            int limit = parameter.getLimit();
            int offset = parameter.getOffset();
            Integer technicalStatus = TechnicalStatus.getCodeByDesc(status);
            List<BusinessInfoHeader> businessInfoList = null;
            if(limit != -1) {
                businessInfoList = businessDao.queryBusinessByConditionWithLimit(technicalStatus, ticketNumber, businessName, level2Category, submitter, limit, offset);
            } else {
                businessInfoList = businessDao.queryBusinessByCondition(technicalStatus, ticketNumber, businessName, level2Category, submitter);
            }
            for(BusinessInfoHeader infoHeader : businessInfoList) {
                String categoryId = businessDao.queryCategoryIdByBusinessId(infoHeader.getBusinessId());
                String path = getCategoryPath(categoryId);
                infoHeader.setPath(path);
                String[] pathArr = path.split("\\.");
                if(pathArr.length >= 2)
                    infoHeader.setLevel2Category(pathArr[1]);
            }
            pageResult.setLists(businessInfoList);
            long businessCount = businessDao.queryBusinessCountByCondition(technicalStatus, ticketNumber, businessName, level2Category, submitter);
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
