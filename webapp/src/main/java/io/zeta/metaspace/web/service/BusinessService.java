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
import io.zeta.metaspace.model.business.BusinessRelationEntity;
import io.zeta.metaspace.model.business.TechnicalStatus;
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
            entity.setCategoryGuid(categoryId);
            entity.setRelationshipId(relationGuid);
            entity.setPath(qualifiedName);
            int relationFlag = relationDao.addRelation(entity);

            return insertFlag & relationFlag;
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

    public List<BusinessInfo> getBusinessListByName(String businessName, int limit, int offset) throws AtlasBaseException {
        try {
            return businessDao.queryBusinessByName(businessName, limit, offset);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    public List<BusinessInfo> getBusinessListByCondition(String status, String ticketNumber, String businessName,
                                                         String level2Category, String submitter,int limit, int offset) throws AtlasBaseException {
        try {
            Integer technicalStatus = TechnicalStatus.getCodeByDesc(status);

            return businessDao.queryBusinessByCondition(technicalStatus, ticketNumber, businessName, level2Category, submitter, limit, offset);
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
