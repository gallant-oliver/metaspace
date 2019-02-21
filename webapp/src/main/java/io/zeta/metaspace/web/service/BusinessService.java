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
import io.zeta.metaspace.model.business.BusinessQueryParameter;
import io.zeta.metaspace.model.business.BusinessRelationEntity;
import io.zeta.metaspace.model.business.TechnicalStatus;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.dao.BusinessDAO;
import io.zeta.metaspace.web.dao.BusinessRelationDAO;
import io.zeta.metaspace.web.dao.CategoryDAO;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public int addBusiness(String categoryId, BusinessInfo info) throws AtlasBaseException {
        try {
            String businessId = UUID.randomUUID().toString();
            info.setBusinessId(businessId);
            info.setDepartmentId(categoryId);
            int insertFlag =  businessDao.insertBusinessInfo(info);

            BusinessRelationEntity entity = new BusinessRelationEntity();
            String relationGuid = UUID.randomUUID().toString();

            String qualifiedName = categoryDao.queryQualifiedName(categoryId);
            if (Objects.nonNull(qualifiedName)) {
                qualifiedName += "." + info.getName();
            }
            entity.setBusinessId(businessId);
            entity.setCategoryGuid(categoryId);
            entity.setRelationshipGuid(relationGuid);
            entity.setPath(qualifiedName);
            int relationFlag = relationDao.addRelation(entity);

            return insertFlag & relationFlag;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加失败");
        }
    }

    public int updateBusiness(String businessId, BusinessInfo info) throws AtlasBaseException {
        try {
            info.setBusinessId(businessId);
            return businessDao.updateBusinessInfo(info);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加失败");
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

    public PageResult<BusinessInfo> getBusinessListByName(String categoryId, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<BusinessInfo> pageResult = new PageResult<>();
            String businessName = parameters.getQuery();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<BusinessInfo> businessInfoList = null;
            if(limit != -1) {
                businessInfoList = businessDao.queryBusinessByNameWithLimit(categoryId, businessName, limit, offset);
            } else {
                businessInfoList = businessDao.queryBusinessByName(categoryId, businessName);
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

    public PageResult<BusinessInfo> getBusinessListByCondition(BusinessQueryParameter parameter) throws AtlasBaseException {
        try {
            PageResult<BusinessInfo> pageResult = new PageResult<>();
            String status = parameter.getStatus();
            String ticketNumber = parameter.getTicketNumber();
            String businessName = parameter.getBusinessName();
            String level2Category = parameter.getLevel2Category();
            String  submitter = parameter.getSubmitter();
            int limit = parameter.getLimit();
            int offset = parameter.getOffset();
            Integer technicalStatus = TechnicalStatus.getCodeByDesc(status);
            List<BusinessInfo> businessInfoList = null;
            if(limit != -1) {
                businessInfoList = businessDao.queryBusinessByConditionWithLimit(technicalStatus, ticketNumber, businessName, level2Category, submitter, limit, offset);
            } else {
                businessInfoList = businessDao.queryBusinessByCondition(technicalStatus, ticketNumber, businessName, level2Category, submitter);
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
            businessDao.deleteRelationByBusinessId(businessId);
            for(String guid : tableIdList) {
                businessDao.insertTableRelation(businessId, guid);
            }
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }
}
