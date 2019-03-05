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

import io.zeta.metaspace.model.business.TechnicalStatus;
import io.zeta.metaspace.model.homepage.CategoryDBInfo;
import io.zeta.metaspace.model.homepage.DataDistribution;
import io.zeta.metaspace.model.homepage.RoleUseInfo;
import io.zeta.metaspace.model.homepage.TableUseInfo;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.HomePageDAO;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/4 9:56
 */
@Service
public class HomePageService {

    @Autowired
    HomePageDAO homePageDAO;

    private static final String sourceLayerCategoryGuid = "1";


    public PageResult<TableUseInfo> getTableRelatedInfo(Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<TableUseInfo> pageResult = new PageResult<>();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<TableUseInfo> tableList = homePageDAO.getTableRelatedInfo(limit, offset);
            long total = homePageDAO.getTotalTableUserTimes();
            DecimalFormat df = new DecimalFormat("0.00");
            tableList.stream().forEach(info -> info.setProportion(String.valueOf(df.format((float) info.getTimes()/total))));
            long sum = homePageDAO.getCountBusinessRelatedTable();

            pageResult.setLists(tableList);
            pageResult.setCount(tableList.size());
            pageResult.setSum(sum);
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询异常");
        }
    }

    public PageResult<RoleUseInfo> getRoleRelatedInfo(Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<RoleUseInfo> pageResult = new PageResult<>();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<RoleUseInfo> roleList = homePageDAO.getRoleRelatedInfo(limit, offset);
            //long total = roleList.stream().map(RoleUseInfo::getNumber).reduce(Long::sum).get();
            long total = homePageDAO.getTotalUserNumber();
            DecimalFormat df = new DecimalFormat("0.00");
            roleList.stream().forEach(info -> info.setProportion(String.valueOf(df.format((float) info.getNumber()/total))));
            long sum = homePageDAO.getCountRole();
            pageResult.setLists(roleList);
            pageResult.setCount(roleList.size());
            pageResult.setSum(sum);
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询异常");
        }
    }

    public List<Role> getAllRole() throws AtlasBaseException {
        try {
            return homePageDAO.getAllRole();
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询异常");
        }
    }

    public PageResult<User> getUserListByRoleId(String roleId, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<User> pageResult = new PageResult<>();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<User> userList = homePageDAO.getUserListByRoleId(roleId, limit, offset);
            long sum = homePageDAO.getCountUserRelatedRole(roleId);
            pageResult.setLists(userList);
            pageResult.setCount(userList.size());
            pageResult.setSum(sum);
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询异常");
        }
    }

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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询异常");
        }
    }

    public PageResult<CategoryDBInfo> getCategoryRelatedDB(Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<CategoryDBInfo> pageResult = new PageResult<>();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();

            List<CategoryDBInfo> categoryDBInfoList = homePageDAO.getCategoryRelatedDBCount(sourceLayerCategoryGuid, limit, offset);
            pageResult.setLists(categoryDBInfoList);
            pageResult.setCount(categoryDBInfoList.size());
            long sum = homePageDAO.getCountCategory(sourceLayerCategoryGuid);
            pageResult.setSum(sum);
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询异常");
        }
    }

    public PageResult<CategoryDBInfo> getChildCategoryRelatedDB(String categoryGuid, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<CategoryDBInfo> pageResult = new PageResult<>();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<CategoryDBInfo> categoryDBInfoList = homePageDAO.getChildSystemDBCount(categoryGuid, limit, offset);
            pageResult.setLists(categoryDBInfoList);
            pageResult.setCount(categoryDBInfoList.size());
            long sum = homePageDAO.getCountCategory(categoryGuid);
            pageResult.setSum(sum);
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询异常");
        }
    }
}
