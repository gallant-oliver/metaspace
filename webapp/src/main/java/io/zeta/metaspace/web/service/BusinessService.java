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
import io.zeta.metaspace.web.dao.BusinessDAO;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/12 14:56
 */
public class BusinessService {

    @Autowired
    BusinessDAO businessDao;

    public int addBusiness(BusinessInfo info) throws AtlasBaseException {
        try {
            return businessDao.insertBusinessInfo(info);
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
}
