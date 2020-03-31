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

import com.google.gson.Gson;
import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.business.BusinessQueryParameter;
import io.zeta.metaspace.model.business.BusinessRelationEntity;
import io.zeta.metaspace.model.business.BusinessTableList;
import io.zeta.metaspace.model.business.ColumnCheckMessage;
import io.zeta.metaspace.model.business.ColumnPrivilege;
import io.zeta.metaspace.model.business.ColumnPrivilegeRelation;
import io.zeta.metaspace.model.business.TechnicalStatus;
import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.DataOwnerHeader;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.TableHeader;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.privilege.SystemModule;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.utils.MetaspaceGremlin3QueryProvider;
import io.zeta.metaspace.utils.MetaspaceGremlinQueryProvider;
import io.zeta.metaspace.web.dao.BusinessDAO;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.ColumnDAO;
import io.zeta.metaspace.web.dao.ColumnPrivilegeDAO;
import io.zeta.metaspace.web.dao.DataShareDAO;
import io.zeta.metaspace.web.dao.PrivilegeDAO;
import io.zeta.metaspace.web.dao.RoleDAO;
import io.zeta.metaspace.web.dao.UserGroupDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import io.zeta.metaspace.web.util.DateUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.repository.graphdb.AtlasGraph;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.postgresql.util.PGobject;

import static io.zeta.metaspace.web.util.PoiExcelUtils.XLSX;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;


import javax.inject.Inject;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/12 14:56
 */
@Service
public class BusinessService {
    private static final Logger LOG = LoggerFactory.getLogger(BusinessService.class);
    @Autowired
    BusinessDAO businessDao;
    @Autowired
    CategoryDAO categoryDao;
    @Autowired
    PrivilegeDAO privilegeDao;
    @Autowired
    RoleDAO roleDao;
    @Autowired
    UserGroupDAO userGroupDAO;
    @Autowired
    TenantService tenantService;
    @Autowired
    RoleService roleService;
    @Autowired
    ColumnPrivilegeDAO columnPrivilegeDAO;
    @Autowired
    MetaDataService metaDataService;
    @Autowired
    DataShareDAO shareDAO;
    @Autowired
    DataShareService shareService;

    @Inject
    protected AtlasGraph graph;

    @Autowired
    private ColumnDAO columnDAO;


    private MetaspaceGremlinQueryProvider gremlinQueryProvider = MetaspaceGremlinQueryProvider.INSTANCE;

    private static final int FINISHED_STATUS = 1;
    private static final int BUSINESS_TYPE = 1;

    @Transactional
    public int addBusiness(String categoryId, BusinessInfo info,String tenantId) throws AtlasBaseException {
        try {
            int count = businessDao.sameNameCount(info.getName(),tenantId);
            if(count > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同的业务对象名称");
            }
            //departmentId(categoryId)
            info.setDepartmentId(categoryId);
            //submitter && businessOperator
            String userName = AdminUtils.getUserData().getUsername();
            info.setSubmitter(userName);
            info.setBusinessOperator(userName);
            //businessId
            String businessId = UUID.randomUUID().toString();
            info.setBusinessId(businessId);
            //submissionTime && businessLastUpdate && ticketNumber
            long timestamp = System.currentTimeMillis();
            String time = DateUtils.getNow();
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
            int insertFlag =  businessDao.insertBusinessInfo(info,tenantId);

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
            entity.setGenerateTime(time);
            int relationFlag = businessDao.addRelation(entity);
            return insertFlag & relationFlag;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("添加业务对象失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加业务对象失败");
        }
    }

    public int updateBusiness(String businessId, BusinessInfo info,String tenantId) throws AtlasBaseException {
        try {
            BusinessInfo currentInfo = businessDao.queryBusinessByBusinessId(businessId);
            int count = businessDao.sameNameCount(info.getName(),tenantId);

            if(count > 0 && !currentInfo.getName().equals(info.getName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同的业务对象名称");
            }
            String userName = AdminUtils.getUserData().getUsername();
            long timestamp = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(timestamp);
            info.setBusinessOperator(userName);
            info.setBusinessLastUpdate(time);
            info.setBusinessId(businessId);
            return businessDao.updateBusinessInfo(info);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("修改业务对象信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修改业务对象信息失败");
        }
    }

    public BusinessInfo getBusinessInfo(String businessId,String tenantId) throws AtlasBaseException {
        try {
            BusinessInfo info = businessDao.queryBusinessByBusinessId(businessId);
            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)){
                User user = AdminUtils.getUserData();
                List<Role> roles = roleDao.getRoleByUsersId(user.getUserId());
                if(roles.stream().allMatch(role -> role.getStatus() == 0))
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
                String userId = user.getUserId();
                boolean editBusiness = privilegeDao.queryModulePrivilegeByUser(userId, SystemModule.BUSINESSE_OPERATE.getCode()) == 0 ? false : true;
                info.setEditBusiness(editBusiness);
            }else{
                List<Module> modules = tenantService.getModule(tenantId);
                boolean editBusiness = modules.stream().anyMatch(module-> ModuleEnum.BUSINESSEDIT.getId()==module.getModuleId());
                info.setEditBusiness(editBusiness);
            }
            String categoryGuid = info.getDepartmentId();
            String departmentName = categoryDao.queryNameByGuid(categoryGuid);
            info.setDepartmentName(departmentName);
            return info;
        } catch (Exception e) {
            LOG.error("获取业务对象信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取业务对象信息失败");
        }
    }

    public TechnologyInfo getRelatedTableList(String businessId,String tenantId) throws AtlasBaseException {
        try {

            //technicalLastUpdate && technicalOperator
            TechnologyInfo info = businessDao.queryTechnologyInfoByBusinessId(businessId);
            //editTechnical
            if(Objects.isNull(info))
                info = new TechnologyInfo();
            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)){
                User user = AdminUtils.getUserData();
                List<Role> roles = roleDao.getRoleByUsersId(user.getUserId());
                if(roles.stream().allMatch(role -> role.getStatus() == 0))
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
                String userId = user.getUserId();
                boolean editTechnical = privilegeDao.queryModulePrivilegeByUser(userId, SystemModule.TECHNICAL_OPERATE.getCode()) == 0 ? false : true;
                info.setEditTechnical(editTechnical);
            }else{
                List<Module> modules = tenantService.getModule(tenantId);
                boolean editTechnical = modules.stream().anyMatch(module-> ModuleEnum.TECHNICALEDIT.getId()==module.getModuleId());
                info.setEditTechnical(editTechnical);
            }
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
            LOG.error("获取关联表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取关联表失败");
        }
    }

    public PageResult<BusinessInfoHeader> getBusinessListByCategoryId(String categoryId, Parameters parameters,String tenantId) throws AtlasBaseException {
        try {
            PageResult<BusinessInfoHeader> pageResult = new PageResult<>();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<BusinessInfoHeader>  list = businessDao.queryBusinessByCatetoryId(categoryId, limit, offset,tenantId);
            String path = CategoryRelationUtils.getPath(categoryId);
            StringJoiner joiner = null;
            String[] pathArr = path.split("/");
            String level2Category = "";
            if(pathArr.length >= 2)
                level2Category = pathArr[1];
            for(BusinessInfoHeader infoHeader : list) {
                joiner = new StringJoiner(".");
                //path
                //joiner.add(path).add(infoHeader.getName());
                joiner.add(path);
                infoHeader.setPath(joiner.toString());
                //level2Category
                infoHeader.setLevel2Category(level2Category);
            }
            //long totalSize = businessDao.queryBusinessCountByByCatetoryId(categoryId);
            long totalSize = 0;
            if(list.size()!=0){
                totalSize = list.get(0).getTotal();
            }
            //pageResult.setOffset(offset);
            pageResult.setTotalSize(totalSize);
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取业务对象列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取业务对象列表失败");
        }
    }

    public PageResult<BusinessInfoHeader> getBusinessListByName(Parameters parameters,String tenantId) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            PageResult<BusinessInfoHeader> pageResult = new PageResult<>();
            String businessName = parameters.getQuery();
            businessName = (businessName == null ? "":businessName);
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<BusinessInfoHeader> businessInfoList = null;
            List<String> categoryIds = new ArrayList<>();

            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)){
                List<Role> roles = roleDao.getRoleByUsersId(user.getUserId());
                if(roles.stream().allMatch(role -> role.getStatus() == 0))
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
                for (Role role:roles){
                    if (role.getStatus() == 0){
                        continue;
                    }
                    String roleId = role.getRoleId();
                    List<String> category = CategoryRelationUtils.getPermissionCategoryList(roleId, BUSINESS_TYPE);
                    for (String categoryId  : category){
                        if (!categoryIds.contains(categoryId)){
                            categoryIds.add(categoryId);
                        }
                    }
                }
            }else{
                List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(),tenantId);
                for (UserGroup userGroup :userGroups){
                    String userGroupId = userGroup.getId();
                    List<String> category = CategoryRelationUtils.getPermissionCategoryListV2(userGroupId, BUSINESS_TYPE,tenantId);
                    for (String categoryId  : category){
                        if (!categoryIds.contains(categoryId)){
                            categoryIds.add(categoryId);
                        }
                    }
                }
            }
            if(Objects.isNull(categoryIds) || categoryIds.size()==0) {
                return  pageResult;
            }
            if(Objects.nonNull(businessName))
                businessName = businessName.replaceAll("%", "/%").replaceAll("_", "/_");
            businessInfoList = businessDao.queryBusinessByName(businessName, categoryIds, limit, offset,tenantId);

            for(BusinessInfoHeader infoHeader : businessInfoList) {
                String path = CategoryRelationUtils.getPath(infoHeader.getCategoryGuid());
                StringJoiner joiner = new StringJoiner(".");
                //joiner.add(path).add(infoHeader.getName());
                joiner.add(path);
                infoHeader.setPath(joiner.toString());
                String[] pathArr = path.split("/");
                String level2Category = "";
                if(pathArr.length >= 2)
                    level2Category = pathArr[1];
                infoHeader.setLevel2Category(level2Category);
            }
            //long businessCount = businessDao.queryBusinessCountByName(businessName, categoryIds);
            long businessTotal = 0;
            if(businessInfoList.size()!=0){
                businessTotal = businessInfoList.get(0).getTotal();
            }
            //pageResult.setOffset(offset);
            pageResult.setTotalSize(businessTotal);
            pageResult.setLists(businessInfoList);
            pageResult.setCurrentSize(businessInfoList.size());
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("搜索业务对象失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "搜索业务对象失败");
        }
    }

    public PageResult<BusinessInfoHeader> getBusinessListByCondition(BusinessQueryParameter parameter,String tenantId) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            PageResult<BusinessInfoHeader> pageResult = new PageResult<>();
            String status = parameter.getStatus();
            String ticketNumber = parameter.getTicketNumber();
            ticketNumber = (ticketNumber==null ? "":ticketNumber);
            String businessName = parameter.getName();
            businessName = (businessName==null ? "":businessName);
            String level2CategoryId = parameter.getLevel2CategoryId();
            String  submitter = parameter.getSubmitter();
            submitter = (submitter==null ? "":submitter);
            int limit = parameter.getLimit();
            int offset = parameter.getOffset();
            Integer technicalStatus = TechnicalStatus.getCodeByDesc(status);
            List<String> categoryIds = new ArrayList<>();

            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)){
                List<Role> roles = roleDao.getRoleByUsersId(user.getUserId());
                if(roles.stream().allMatch(role -> role.getStatus() == 0))
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
                for (Role role:roles){
                    if (role.getStatus() == 0){
                        continue;
                    }
                    String roleId = role.getRoleId();
                    List<String> category = CategoryRelationUtils.getPermissionCategoryList(roleId, BUSINESS_TYPE);
                    for (String categoryId  : category){
                        if (!categoryIds.contains(categoryId)){
                            categoryIds.add(categoryId);
                        }
                    }
                }
            }else{
                List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(),tenantId);
                for (UserGroup userGroup :userGroups){
                    String userGroupId = userGroup.getId();
                    List<String> category = CategoryRelationUtils.getPermissionCategoryListV2(userGroupId, BUSINESS_TYPE,tenantId);
                    for (String categoryId  : category){
                        if (!categoryIds.contains(categoryId)){
                            categoryIds.add(categoryId);
                        }
                    }
                }
            }
            if(Objects.nonNull(categoryIds) && categoryIds.size() > 0) {
                if(Objects.nonNull(businessName))
                    businessName = businessName.replaceAll("%", "/%").replaceAll("_", "/_");
                if(Objects.nonNull(ticketNumber))
                    ticketNumber = ticketNumber.replaceAll("%", "/%").replaceAll("_", "/_");
                if(Objects.nonNull(submitter))
                    submitter = submitter.replaceAll("%", "/%").replaceAll("_", "/_");
                List<BusinessInfoHeader> businessInfoList = businessDao.queryBusinessByCondition(categoryIds, technicalStatus, ticketNumber, businessName, level2CategoryId, submitter, limit, offset,tenantId);
                for (BusinessInfoHeader infoHeader : businessInfoList) {
                    String categoryId = businessDao.queryCategoryIdByBusinessId(infoHeader.getBusinessId());
                    String path = CategoryRelationUtils.getPath(categoryId);
                    infoHeader.setPath(path + "." + infoHeader.getName());
                    String[] pathArr = path.split("/");
                    if (pathArr.length >= 2)
                        infoHeader.setLevel2Category(pathArr[1]);
                }
                //pageResult.setOffset(offset);
                pageResult.setLists(businessInfoList);
                //long businessCount = businessDao.queryBusinessCountByCondition(categoryIds, technicalStatus, ticketNumber, businessName, level2CategoryId, submitter);
                long totalsize = 0;
                if(businessInfoList.size()!=0){
                    totalsize = businessInfoList.get(0).getTotal();
                }
                pageResult.setTotalSize(totalsize);
                pageResult.setCurrentSize(businessInfoList.size());
            }
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("搜索业务对象失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "搜索业务对象失败");
        }
    }

    @Transactional
    public void addBusinessAndTableRelation(String businessId, BusinessTableList tableIdList) throws AtlasBaseException {
        List<String> list = tableIdList.getList();
        String trustTable = tableIdList.getTrust();
        try {
            String userName = AdminUtils.getUserData().getUsername();
            long timestamp = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(timestamp);
            businessDao.updateTechnicalInfo(businessId, userName, time);
            //更新technical编辑状态
            if(Objects.nonNull(list) && list.size() > 0) {
                businessDao.updateTechnicalStatus(businessId, TechnicalStatus.ADDED.code);
            } else {
                businessDao.updateTechnicalStatus(businessId, TechnicalStatus.BLANK.code);
            }
            businessDao.deleteRelationByBusinessId(businessId);

            businessDao.updateTrustTable(businessId);

            if(Objects.nonNull(list) && list.size()>0) {
                businessDao.insertTableRelation(businessId, list);
                if(Objects.isNull(trustTable)) {
                    trustTable = list.get(0);
                }
            }
            if(Objects.nonNull(trustTable)) {
                businessDao.setBusinessTrustTable(businessId, trustTable);
            }

        } catch (Exception e) {
            LOG.error("更新技术信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新技术信息失败");
        }
    }

    @Transactional
    public void deleteBusiness(String businessId) throws AtlasBaseException {
        try {
            businessDao.deleteBusinessById(businessId);
            businessDao.deleteRelationByBusinessId(businessId);
            businessDao.deleteRelationById(businessId);
        } catch (Exception e) {
            LOG.error("删除业务对象失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除业务对象失败");
        }
    }


    public int addColumnPrivilege(ColumnPrivilege privilege) throws AtlasBaseException {
        try {
            String columnName = privilege.getName();
            int count = columnPrivilegeDAO.queryNameCount(columnName);
            if(count > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同权限字段");
            }
            return columnPrivilegeDAO.addColumnPrivilege(privilege);
        } catch (Exception e) {
            LOG.error("添加权限字段失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加权限字段失败");
        }
    }

    public int deleteColumnPrivilege(Integer guid) throws AtlasBaseException {
        try {
            return columnPrivilegeDAO.deleteColumnPrivilege(guid);
        } catch (Exception e) {
            LOG.error("删除权限字段失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除权限字段失败");
        }
    }

    public int updateColumnPrivilege(ColumnPrivilege privilege) throws AtlasBaseException {
        try {
            return columnPrivilegeDAO.updateColumnPrivilege(privilege);
        } catch (Exception e) {
            LOG.error("更新失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新失败");
        }
    }

    public List<ColumnPrivilege> getColumnPrivilegeList() throws AtlasBaseException {
        try {
            return columnPrivilegeDAO.getColumnPrivilegeList();
        } catch (Exception e) {
            LOG.error("更新权限字段失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新权限字段失败");
        }
    }

    public List<String> getColumnPrivilegeValue(Integer guid) throws AtlasBaseException {
        try {
            Gson gson = new Gson();
            Object columnObject = columnPrivilegeDAO.queryColumnPrivilege(guid);
            PGobject pGobject = (PGobject)columnObject;
            String value = pGobject.getValue();
            List<String> values = gson.fromJson(value, List.class);
            return values;
        } catch (Exception e) {
            LOG.error("获取权限字段取值失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取权限字段取值失败");
        }
    }

    @Transactional
    public int addColumnPrivilegeRelation(ColumnPrivilegeRelation relation) throws AtlasBaseException {
        try {
            int columnPrivilegeGuid = relation.getColumnPrivilegeGuid();
            ColumnPrivilegeRelation columnRelation = columnPrivilegeDAO.queryPrivilegeRelation(columnPrivilegeGuid);
            if(Objects.nonNull(columnRelation)) {
                return columnPrivilegeDAO.updateColumnPrivilegeRelation(relation);
            } else {
                return columnPrivilegeDAO.addColumnPrivilegeRelation(relation);
            }
        } catch (Exception e) {
            LOG.error("添加权限字段关联失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加权限字段关联失败");
        }
    }


    public PageResult<APIInfoHeader> getBusinessTableRelatedAPI(String businessGuid, Parameters parameters,String tenantId) throws AtlasBaseException {
        try {
            TechnologyInfo technologyInfo = getRelatedTableList(businessGuid,tenantId);
            List<TechnologyInfo.Table> tableHeaderList = technologyInfo.getTables();
            List<String> tableList = new ArrayList<>();
            tableHeaderList.stream().forEach(table -> tableList.add(table.getTableGuid()));
            Integer limit = parameters.getLimit();
            Integer offset = parameters.getOffset();
            List<APIInfoHeader> APIList = new ArrayList<>();
            PageResult<APIInfoHeader> pageResult = new PageResult<>();
            int totalSize = 0;
            if(Objects.nonNull(tableList) && tableList.size()>0) {
                APIList = shareDAO.getTableRelatedAPI(tableList, limit, offset,tenantId);
                for (APIInfoHeader api : APIList) {
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
                //totalSize = shareDAO.countTableRelatedAPI(tableList);
                if (APIList.size()!=0) {
                    totalSize = APIList.get(0).getTotal();
                }
            }
            //pageResult.setOffset(offset);
            pageResult.setTotalSize(totalSize);
            pageResult.setLists(APIList);
            pageResult.setCurrentSize(APIList.size());
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取关联API失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取关联API失败");
        }
    }

    @Transactional
    public void updateBusinessTrustTable(String tenantId) throws AtlasBaseException {
        try {
            List<String> nonTrustBusinessList = businessDao.getNonTrustBusiness(tenantId);
            for (String businessId : nonTrustBusinessList) {
                List<TechnologyInfo.Table> tableList = businessDao.queryTablesByBusinessId(businessId);
                if (Objects.nonNull(tableList) && tableList.size() > 0) {
                    String tableGuid = tableList.get(0).getTableGuid();
                    businessDao.setBusinessTrustTable(businessId, tableGuid);
                }
            }
        } catch (Exception e) {
            LOG.error("更新唯一信任数据失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新唯一信任数据失败");
        }
    }

    public PageResult getPermissionBusinessRelatedTableList(String businessId, Parameters parameters) throws AtlasBaseException {
        try {
            String query = parameters.getQuery();
            query = (query == null ? "":query);

            if(Objects.nonNull(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");

            Integer limit = parameters.getLimit();
            Integer offset = parameters.getOffset();
            List<TableHeader> tableHeaderList = businessDao.getBusinessRelatedTableList(businessId, query, limit, offset);

            tableHeaderList.forEach(tableHeader -> {
                String displayName = tableHeader.getDisplayName();
                String tableName = tableHeader.getTableName();
                if(Objects.isNull(displayName) || "".equals(displayName.trim())) {
                    tableHeader.setDisplayName(tableName);
                } else {
                    tableHeader.setDisplayName(displayName);
                }
            });
            long count = businessDao.getCountBusinessRelatedTable(businessId, query);
            PageResult pageResult = new PageResult();
            pageResult.setLists(tableHeaderList);
            pageResult.setCurrentSize(tableHeaderList.size());
            pageResult.setTotalSize(count);
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取业务对象关联表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取业务对象关联表失败");
        }
    }

    public PageResult getTableColumnList(String tableGuid, Parameters parameters, String sortColumn, String sortOrder) throws AtlasBaseException {
        try {
            boolean existOnPg = columnDAO.tableColumnExist(tableGuid)>0?true:false;
            PageResult pageResult = new PageResult();
            if(!existOnPg) {
                //JanusGraph中取出column信息
                String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.COLUMN_INFO_MAP);
                String columnQuery = String.format(query, tableGuid);
                List<Map> columnMapList = (List<Map>) graph.executeGremlinScript(columnQuery, false);
                List<Column> columnInfoList = ConvertMapToColumnInfoList(tableGuid, columnMapList);
                if(Objects.isNull(columnInfoList) || columnInfoList.size()==0) {
                    return pageResult;
                }
                columnDAO.addColumnDisplayInfo(columnInfoList);
            }
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            String queryText = parameters.getQuery();
            if(Objects.nonNull(queryText))
                queryText = queryText.replaceAll("%", "/%").replaceAll("_", "/_");
            String sqlSortOrder = Objects.nonNull(sortOrder)? sortOrder.toLowerCase(): "asc";
            String sqlsortColumn = (Objects.nonNull(sortColumn) && "updatetime".equals(sortColumn.toLowerCase()))?"display_updatetime":"column_name";

            List<Column> resultColumnInfoList = columnDAO.getTableColumnList(tableGuid, queryText, sqlsortColumn, sqlSortOrder, limit, offset);
            //int totalCount = columnDAO.countTableColumnList(tableGuid, queryText);
            int totalCount = 0;
            if (resultColumnInfoList.size()!=0){
                totalCount=resultColumnInfoList.get(0).getTotal();
            }

            resultColumnInfoList.forEach(column -> {
                if(Objects.isNull(column.getDisplayName()) || "".equals(column.getColumnName().trim())) {
                    column.setDisplayName(column.getColumnName());
                }
            });

            pageResult.setLists(resultColumnInfoList);
            pageResult.setCurrentSize(resultColumnInfoList.size());
            pageResult.setTotalSize(totalCount);
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取表字段列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取表字段列表失败");
        }
    }

    public void editTableColumnDisplayName(List<Column> columns, List<String> editColumnList, boolean existOnPg) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            String time = DateUtils.getNow();

            if(existOnPg) {
                for(Column column: columns) {
                    column.setDisplayNameOperator(userId);
                    column.setDisplayNameUpdateTime(time);
                    columnDAO.updateColumnInfo(column);
                }
            } else {
                for (Column column : columns) {
                    column.setStatus("ACTIVE");
                    String columnGuid = column.getColumnId();
                    String displayText = column.getDisplayName();
                    if (Objects.isNull(columnGuid) || Objects.isNull(displayText)) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "字段Id或别名不能为空");
                    }
                    if (editColumnList.contains(column.getColumnName())) {
                        column.setDisplayNameOperator(userId);
                        column.setDisplayNameUpdateTime(time);
                    }
                }
                columnDAO.addColumnDisplayInfo(columns);
            }
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("编辑字段别名失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "编辑字段别名失败");
        }
    }

    public void editSingleColumnDisplayName(String tableGuid, Column column) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            String time = DateUtils.getNow();
            //判断是否已经存在于pg中
            boolean existOnPg = columnDAO.tableColumnExist(tableGuid)>0?true:false;
            //字段信息
            List<Column> columnInfoList = null;

            if(existOnPg) {
                column.setDisplayNameOperator(userId);
                column.setDisplayNameUpdateTime(time);
                columnDAO.updateColumnInfo(column);
            } else {

                //JanusGraph中取出column信息
                String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.COLUMN_INFO_MAP);
                String columnQuery = String.format(query, tableGuid);
                List<Map> columnMapList = (List<Map>) graph.executeGremlinScript(columnQuery, false);
                columnInfoList = ConvertMapToColumnInfoList(tableGuid, columnMapList);
                List<String> editColumnList = new ArrayList<>();
                String editColumnId = column.getColumnId();
                columnInfoList.forEach(col -> {
                    if(col.getColumnId().equals(editColumnId)) {
                        editColumnList.add(col.getColumnName());
                        col.setDisplayName(column.getDisplayName());
                        col.setDisplayNameOperator(userId);
                        col.setDisplayNameUpdateTime(time);
                    } else {
                        col.setDisplayName(col.getColumnName());
                    }
                });

                editTableColumnDisplayName(columnInfoList, editColumnList, existOnPg);
            }

        } catch (Exception e) {
            LOG.error("编辑字段别名失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "编辑字段别名失败");
        }
    }

    public void editTableDisplayName(TableHeader tableHeader) throws AtlasBaseException {
        try {
            String tableGuid = tableHeader.getTableId();
            String displayText = tableHeader.getDisplayName();
            String userId = AdminUtils.getUserData().getUserId();
            String time = DateUtils.getNow();
            columnDAO.updateTableDisplay(tableGuid, displayText, userId, time);
        } catch (AtlasBaseException e) {
            LOG.error("编辑表别名失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "编辑表别名失败");
        }
    }

    public ColumnCheckMessage checkColumnName(String tableGuid, List<Column> columnInfoList, List<Column> columnwithDisplayList, boolean existOnPg) throws AtlasBaseException {
        try {
            //全部字段名称
            List<String> columnList = columnInfoList.stream().map(columnInfo -> columnInfo.getColumnName()).collect(Collectors.toList());
            //当前需要编辑的字段名称
            List<String> editColumnList = columnwithDisplayList.stream().map(column -> column.getColumnName()).collect(Collectors.toList());

            ColumnCheckMessage columnCheckMessage = new ColumnCheckMessage();
            int errorColumnCount = 0;
            List<String> errorColumnList = new ArrayList<>();
            List<ColumnCheckMessage.ColumnCheckInfo> columnCheckMessageList = new ArrayList<>();
            List<String> recordColumnList = new ArrayList<>();
            ColumnCheckMessage.ColumnCheckInfo columnCheckInfo = null;
            int index = 0;
            for(Column column : columnwithDisplayList) {
                String columnName = column.getColumnName();
                columnCheckInfo = new ColumnCheckMessage.ColumnCheckInfo();
                columnCheckInfo.setRow(index++);
                columnCheckInfo.setColumnName(columnName);
                //是否为重复字段
                if(recordColumnList.contains(columnName)) {
                    columnCheckInfo.setErrorMessage("导入重复字段");
                    errorColumnList.add(columnName);
                    errorColumnCount++;
                    columnCheckMessageList.add(columnCheckInfo);

                    //表中是否存在当前字段
                } else if(columnList.contains(columnName)) {
                    columnCheckInfo.setErrorMessage("匹配成功");
                    String displayText = column.getDisplayName();
                    //未填写别名默认为字段名
                    if(Objects.isNull(displayText) || Objects.equals(displayText.trim(), "")) {
                        columnCheckInfo.setDisplayText(columnName);
                    } else {
                        if(displayText.length() > 64) {
                            columnCheckInfo.setErrorMessage("别名超出允许最大长度64字符");
                            errorColumnList.add(columnName);
                            errorColumnCount++;
                        } else {
                            columnCheckInfo.setDisplayText(displayText);
                        }
                    }
                    columnCheckMessageList.add(columnCheckInfo);
                } else {
                    columnCheckInfo.setErrorMessage("数据表中未找到该字段");
                    errorColumnList.add(columnName);
                    errorColumnCount++;
                    columnCheckMessageList.add(columnCheckInfo);
                }
                recordColumnList.add(columnName);
            }
            columnCheckMessage.setColumnCheckInfoList(columnCheckMessageList);
            columnCheckMessage.setErrorColumnList(errorColumnList);
            columnCheckMessage.setTotalSize(columnCheckMessageList.size());
            columnCheckMessage.setErrorCount(errorColumnCount);
            if(errorColumnCount == 0) {

                List<Column> nonEditColumnInfoList = null;
                if(!existOnPg) {
                    //nonEditColumnInfoList用于第一次添加别名时补充未填写字段信息
                    nonEditColumnInfoList = columnInfoList.stream().filter(column -> !editColumnList.contains(column.getColumnName())).collect(Collectors.toList());

                    //取出数据类型
                    Map<String, String> columnId2Type = new HashMap<>();
                    columnInfoList.forEach(column -> {
                        columnId2Type.put(column.getColumnId(), column.getType());
                    });

                    columnwithDisplayList.forEach(column -> {
                        column.setType(columnId2Type.get(column.getColumnId()));
                    });

                    //未编辑向在第一次写入pg时一同写入
                    nonEditColumnInfoList.forEach(column -> column.setDisplayName(column.getColumnName()));
                    columnwithDisplayList.addAll(nonEditColumnInfoList);
                }
                //取出字段guid
                Map<String, String> columnName2GuidMap = new HashMap<>();
                columnInfoList.forEach(column -> {
                    String columnName = column.getColumnName();
                    String columnId = column.getColumnId();
                    columnName2GuidMap.put(columnName, columnId);
                });
                columnwithDisplayList.stream().forEach(column -> {
                    String columnName = column.getColumnName();
                    String guid = columnName2GuidMap.get(columnName);
                    column.setColumnId(guid);
                    column.setTableId(tableGuid);
                });
                columnwithDisplayList.forEach(col -> {
                    if(Objects.isNull(col.getDisplayName()) || "".equals(col.getDisplayName().trim())) {
                        col.setDisplayName(col.getColumnName());
                    }
                });
                editTableColumnDisplayName(columnwithDisplayList, editColumnList, existOnPg);
                columnCheckMessage.setStatus(ColumnCheckMessage.Status.SUCCESS);
            } else {
                columnCheckMessage.setStatus(ColumnCheckMessage.Status.FAILURE);
            }
            return columnCheckMessage;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }

    public Map<String, String> getColumnName2GuidMap(String tableGuid) throws AtlasBaseException {
        String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.COLUMN_INFO);
        String columnQuery = String.format(query, tableGuid);
        List<Map> columnInfoList = (List<Map>) graph.executeGremlinScript(columnQuery, false);
        Map name2GuidMap = new HashMap();
        for(Map obj : columnInfoList) {
            List<String> guidList = (List) obj.get("__guid");
            List<String> nameList = (List) obj.get("Asset.name");
            String guid = null;
            String name = null;
            if(Objects.nonNull(guidList) && guidList.size()>0) {
                guid = guidList.get(0);
            }
            if(Objects.nonNull(nameList) && nameList.size()>0) {
                name  = nameList.get(0);
            }
            if(Objects.nonNull(name) && Objects.nonNull(guid)) {
                name2GuidMap.put(name, guid);
            }
        }
        return name2GuidMap;
    }

    public ColumnCheckMessage importColumnWithDisplayText(String tableGuid, File file) throws AtlasBaseException {
        try {
            //提取excel数据
            List<Column> columnAndDisplayMap = convertExceltoMap(file);
            //判断是否已经存在于pg中
            boolean existOnPg = columnDAO.tableColumnExist(tableGuid)>0?true:false;
            //字段信息
            List<Column> columnInfoList = null;
            if(existOnPg) {
                //pg中取出column信息
                columnInfoList = columnDAO.getColumnInfoList(tableGuid);
            } else {
                //JanusGraph中取出column信息
                String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.COLUMN_INFO_MAP);
                String columnQuery = String.format(query, tableGuid);
                List<Map> columnMapList = (List<Map>) graph.executeGremlinScript(columnQuery, false);
                columnInfoList = ConvertMapToColumnInfoList(tableGuid, columnMapList);
            }
            return checkColumnName(tableGuid, columnInfoList, columnAndDisplayMap, existOnPg);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }

    public List<Column> ConvertMapToColumnInfoList(String tableGuid, List<Map> columnMapList) {
        List<Column> columnInfoList = new ArrayList<>();
        for(Map obj : columnMapList) {
            List<String> guidList = (List) obj.get("__guid");
            List<String> nameList = (List) obj.get("Asset.name");
            List<String> typeList = (List) obj.get("hive_column.type");
            List<String> stateList = (List) obj.get("__state");
            List<Long> modifyTimeList = (List)obj.get("__modificationTimestamp");
            String guid = null;
            String name = null;
            String type = null;
            String state = null;
            String updateTime = null;

            if(Objects.nonNull(guidList) && guidList.size()>0) {
                guid = guidList.get(0);
            }
            if(Objects.nonNull(nameList) && nameList.size()>0) {
                name  = nameList.get(0);
            }
            if(Objects.nonNull(typeList) && typeList.size()>0) {
                type  = typeList.get(0);
            }
            if(Objects.nonNull(stateList) && stateList.size()>0) {
                state  = stateList.get(0);
            }
            if(Objects.nonNull(modifyTimeList) && modifyTimeList.size()>0) {
                Long time = modifyTimeList.get(0);
                updateTime = DateUtils.date2String(new Date(time));
            }

            Column column = new Column();
            column.setTableId(tableGuid);
            column.setColumnId(guid);
            column.setColumnName(name);
            column.setType(type);
            column.setStatus(state);
            column.setDisplayNameUpdateTime(updateTime);
            columnInfoList.add(column);
        }
        return columnInfoList;
    }


    public List<Column> convertExceltoMap(File file) throws AtlasBaseException {
        try {
            Workbook workbook = new WorkbookFactory().create(file);
            Sheet sheet = workbook.getSheetAt(0);
            int rowNum = sheet.getLastRowNum() + 1;
            Row row = null;
            Cell keyCell = null;
            Cell valueCell = null;
            String key = null;
            String value = null;
            List resultList = new ArrayList();
            Column column = null;
            row = sheet.getRow(0);
            keyCell = row.getCell(0);
            valueCell = row.getCell(1);
            key = Objects.nonNull(keyCell)?keyCell.getStringCellValue():"";
            value = Objects.nonNull(valueCell)?valueCell.getStringCellValue():"";
            if(!"字段名称".equals(key) || !"显示名称".equals(value)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Excel表头错误，表头名称应为【字段名称】和【显示名称】");
            }
            for(int i=1; i<rowNum; i++) {
                row = sheet.getRow(i);
                keyCell = row.getCell(0);
                switch (keyCell.getCellTypeEnum()) {
                    case NUMERIC:
                        key = String.valueOf(keyCell.getNumericCellValue());
                        break;
                    case BOOLEAN:
                        key = String.valueOf(keyCell.getBooleanCellValue());
                        break;
                    case STRING:
                        key = keyCell.getStringCellValue();
                        break;
                    case BLANK:
                        key = "";
                        break;
                    case FORMULA:
                        key = keyCell.getCellFormula();
                        break;
                    default:
                        value = "";
                }

                valueCell = row.getCell(1);
                switch (valueCell.getCellTypeEnum()) {
                    case NUMERIC:
                        value = String.valueOf(valueCell.getNumericCellValue());
                        break;
                    case BOOLEAN:
                        value = String.valueOf(valueCell.getBooleanCellValue());
                        break;
                    case STRING:
                        value = valueCell.getStringCellValue();
                        break;
                    case BLANK:
                        value = "";
                        break;
                    case FORMULA:
                        value = valueCell.getCellFormula();
                        break;
                    default:
                        value = "";
                }
                column = new Column();
                column.setColumnName(key);
                column.setDisplayName(value);
                resultList.add(column);
            }
            return resultList;
        } catch (Exception e) {
            LOG.error("转换失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "转换失败");
        }
    }


    public File exportExcel(String tableGuid) throws AtlasBaseException {
        try {
            boolean existOnPg = columnDAO.tableColumnExist(tableGuid)>0?true:false;
            List<Map> columnMapList = null;
            List<String> columnList = new ArrayList<>();
            List<Column> columnInfoList = null;
            if(existOnPg) {
                columnInfoList = columnDAO.getColumnNameWithDisplayList(tableGuid);
            } else {
                String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.COLUMN_NAME_LIST);
                String columnQuery = String.format(query, tableGuid);
                columnMapList = (List<Map>) graph.executeGremlinScript(columnQuery, false);
                columnMapList.forEach(obj -> {
                    List<String> nameList = (List) obj.get("Asset.name");
                    if(Objects.nonNull(nameList) && nameList.size()>0) {
                        columnList.add(nameList.get(0));
                    }
                });
            }
            List<String> attributes = new ArrayList<>();
            attributes.add("字段名称");
            attributes.add("显示名称");

            List<List<String>> datas = new ArrayList<>();
            List<String> data = null;
            if(existOnPg) {
                for (Column column : columnInfoList) {
                    data = new ArrayList<>();
                    String displayName = column.getDisplayName();
                    data.add(column.getColumnName());
                    data.add(Objects.nonNull(displayName)?displayName:String.valueOf(""));
                    datas.add(data);
                }
            } else {
                for (String columnName : columnList) {
                    data = new ArrayList<>();
                    data.add(columnName);
                    datas.add(data);
                }
            }

            TableHeader tableHeader = columnDAO.getTableHeaderInfo(tableGuid);
            String dbName = tableHeader.getDatabaseName();
            String tableName = tableHeader.getTableName();
            StringJoiner joiner = new StringJoiner("_");
            joiner.add(dbName).add(tableName).add("columns");
            Workbook workbook = PoiExcelUtils.createExcelFile(attributes, datas, XLSX);
            File file = new File(joiner.toString() + ".xlsx");
            FileOutputStream output = new FileOutputStream(file);
            workbook.write(output);
            output.flush();
            output.close();

            return file;
        } catch (Exception e) {
            LOG.error("导出Excel失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "导出Excel失败");
        }
    }

    public Table getTableInfoById(String guid,String tenantId) throws AtlasBaseException {
        try {
            Table table = metaDataService.getTableInfoById(guid,tenantId);
            String tableName = table.getTableName();
            String tableDisplayName = table.getDisplayName();
            if (Objects.isNull(tableDisplayName) || "".equals(tableDisplayName.trim())) {
                table.setDisplayName(tableName);
            }
            List<Column> columnList = table.getColumns();
            columnList.forEach(column -> {
                String columnName = column.getColumnName();
                String displayName = column.getDisplayName();
                if (Objects.isNull(displayName) || "".equals(displayName.trim())) {
                    column.setDisplayName(columnName);
                }
            });
            return table;
        } catch (Exception e) {
            LOG.error("获取表信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取表信息失败");
        }
    }

}
