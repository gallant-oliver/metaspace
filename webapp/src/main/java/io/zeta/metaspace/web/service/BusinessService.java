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

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import io.zeta.metaspace.model.approve.ApproveItem;
import io.zeta.metaspace.model.approve.ApproveOperate;
import io.zeta.metaspace.model.approve.ApproveType;
import io.zeta.metaspace.model.business.*;
import io.zeta.metaspace.model.dataquality2.HiveNumericType;
import io.zeta.metaspace.model.enums.BusinessType;
import io.zeta.metaspace.model.enums.Status;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.*;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.CategorycateQueryResult;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.share.ApiHead;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
import io.zeta.metaspace.model.sourceinfo.derivetable.relation.GroupDeriveTableRelation;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.utils.AbstractMetaspaceGremlinQueryProvider;
import io.zeta.metaspace.utils.MetaspaceGremlin3QueryProvider;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.service.Approve.Approvable;
import io.zeta.metaspace.web.service.Approve.ApproveService;
import io.zeta.metaspace.web.util.*;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.atlas.repository.graphdb.AtlasGraph;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static io.zeta.metaspace.web.util.PoiExcelUtils.XLSX;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/12 14:56
 */
@Service
public class BusinessService implements Approvable {
    private static final Logger LOG = LoggerFactory.getLogger(BusinessService.class);
    @Autowired
    BusinessDAO businessDao;
    @Autowired
    CategoryDAO categoryDao;

    @Autowired
    SourceInfoDeriveTableInfoDAO sourceInfoDeriveTableInfoDAO;
    @Autowired
    PrivilegeDAO privilegeDao;
    @Autowired
    RoleDAO roleDao;

    @Autowired
    GroupDeriveTableRelationDAO groupDeriveTableRelationDAO;
    @Autowired
    UserGroupDAO userGroupDAO;
    @Autowired
    TenantService tenantService;
    @Autowired
    ColumnPrivilegeDAO columnPrivilegeDAO;
    @Autowired
    MetaDataService metaDataService;
    @Autowired
    DataShareDAO shareDAO;
    @Autowired
    DataShareService shareService;
    @Autowired
    UserGroupService userGroupService;

    @Inject
    protected AtlasGraph graph;

    @Autowired
    private ColumnDAO columnDAO;
    @Autowired
    private CategoryDAO categoryDAO;

    @Autowired
    ApproveService approveServiceImp;

    @Autowired
    BusinessCatalogueService businessCatalogueService;

    @Autowired
    private PublicService publicService;

    @Autowired
    private DataManageService dataManageService;

    private AbstractMetaspaceGremlinQueryProvider gremlinQueryProvider = AbstractMetaspaceGremlinQueryProvider.INSTANCE;

    private static final int FINISHED_STATUS = 1;
    private static final int BUSINESS_TYPE = 1;

    @Transactional(rollbackFor = Exception.class)
    public int addBusiness(String categoryId, BusinessInfo info, String tenantId) throws AtlasBaseException {
        try {
            int count = businessDao.sameNameCount(info.getName(), tenantId);
            if (count > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同的业务对象名称");
            }
            //departmentId(categoryId)
            info.setDepartmentId(categoryId);
            //submitter && businessOperator
            String userId = AdminUtils.getUserData().getUserId();
            info.setSubmitter(userId);
            info.setBusinessOperator(userId);
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
            String pathStr = categoryDao.queryGuidPathByGuid(categoryId, tenantId);
            String path = pathStr.substring(1, pathStr.length() - 1);
            path = path.replace("\"", "");
            String[] pathArr = path.split(",");
            String level2CategoryId = "";
            int length = 2;
            if (pathArr.length >= length) {
                level2CategoryId = pathArr[1];
            }
            info.setLevel2CategoryId(level2CategoryId);

            // 如果发布开关打开，则更新状态为待审核；否则更新为待发布
            if (info.getPublish()) {
                info.setStatus(Status.AUDITING.getIntValue() + "");
            }
            else {
                info.setStatus(Status.FOUNDED.getIntValue() + "");
            }

            // 默认私密状态为私密
            info.setPrivateStatus("PRIVATE");

            // 手动添加方式：0手动添加，1上传文件
            info.setCreateMode(0);

            // 添加 “创建人可见” 逻辑
            info.setSubmitterRead(true);

            int insertFlag = businessDao.insertBusinessInfo(info, tenantId);

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

            // 如果发布开关打开，则发送审核
            if (info.getPublish()) {
                approveItems(tenantId, info, ApproveType.PUBLISH.getCode());
            }

            return insertFlag & relationFlag;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("添加业务对象失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加业务对象失败");
        }
    }

    public int updateBusiness(String businessId, BusinessInfo info, String tenantId) throws AtlasBaseException {
        try {
            BusinessInfo currentInfo = businessDao.queryBusinessByBusinessId(businessId);
            int count = businessDao.sameNameCount(info.getName(), tenantId);

            if (count > 0 && !currentInfo.getName().equals(info.getName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同的业务对象名称");
            }
            String userId = AdminUtils.getUserData().getUserId();
            long timestamp = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(timestamp);
            info.setBusinessOperator(userId);
            info.setBusinessLastUpdate(time);
            info.setBusinessId(businessId);

            // 如果开关变动，则需要审批
            if (currentInfo.getPublish() != info.getPublish()) {
                // 修改审批状态
                info.setStatus(Status.AUDITING.getIntValue() + "");

                approveItems(tenantId, info, info.getPublish() ? ApproveType.PUBLISH.getCode() : ApproveType.OFFLINE.getCode());
            }

            int result = businessDao.updateBusinessInfo(info);

            // 如果发布状态
            return result;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("修改业务对象信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修改业务对象信息失败");
        }
    }

    public BusinessInfo getBusinessInfo(String businessId, String tenantId) throws AtlasBaseException {
        try {
            BusinessInfo info = businessDao.queryBusinessByBusinessId(businessId);
            List<String> categoryIds = categoryDAO.getCategoryGuidByBusinessGuid(businessId, tenantId);
            boolean edit = false;
            if (categoryIds.size() > 0) {
                int count = userGroupDAO.useCategoryPrivilege(AdminUtils.getUserData().getUserId(), categoryIds, tenantId);
                if (count > 0) {
                    edit = true;
                }
            }

            info.setEditBusiness(edit);
            String submitter = userGroupDAO.getUserNameById(info.getSubmitter());
            String operator = userGroupDAO.getUserNameById(info.getBusinessOperator());
            if (submitter != null) {
                info.setSubmitter(submitter);
            }
            if (operator != null) {
                info.setBusinessOperator(operator);
            }
            String categoryGuid = info.getDepartmentId();
            String departmentName = categoryDao.queryNameByGuid(categoryGuid, tenantId);
            info.setDepartmentName(departmentName);
            return info;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取业务对象信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取业务对象信息失败");
        }
    }

    public TechnologyInfo getRelatedTableList(String businessId, String tenantId) throws AtlasBaseException {
        try {
            //technicalLastUpdate && technicalOperator
            TechnologyInfo info = businessDao.queryTechnologyInfoByBusinessId(businessId);
            //editTechnical
            if (Objects.isNull(info))
                info = new TechnologyInfo();

            String operator = userGroupDAO.getUserNameById(info.getTechnicalOperator());
            if (operator != null) {
                info.setTechnicalOperator(operator);
            }
            info.setGlobal(publicService.isGlobal());
            User user = AdminUtils.getUserData();
            String userId = user.getUserId();
//                List<Module> modules = tenantService.getModule(tenantId);
//                boolean editTechnical = modules.stream().anyMatch(module-> ModuleEnum.TECHNICALEDIT.getId()==module.getModuleId());
            info.setEditTechnical(true);
            //tables
            List<TechnologyInfo.Table> tables = buildTablesByBusinessId(businessId, tenantId,null,null);

            // 查询表是否可跳转‘元数据管理’
            if (CollectionUtils.isNotEmpty(tables)) {
                List<String> tableGuids = tables.stream().map(t -> t.getTableGuid()).collect(Collectors.toList());
                // 当前用户是否同时有表所在数据库和数据源的查看权限
                List<String> jumpTableGuids = businessDao.getTableJump(tableGuids, userId, tenantId);
                for (TechnologyInfo.Table table : tables) {
                    if (jumpTableGuids.contains(table.getTableGuid())){
                        table.setJump(true);
                    }
                    else {
                        table.setJump(false);
                    }
                }
            }

            info.setTables(tables);
            //businessId
            info.setBusinessId(businessId);
            return info;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取关联表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取关联表失败");
        }
    }


    /**
     * 公共租户
     * @param businessId
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    public TechnologyInfo getRelatedTableListGlobal(String businessId, String tenantId) throws AtlasBaseException {
        try {
            //technicalLastUpdate && technicalOperator
            TechnologyInfo info = businessDao.queryTechnologyInfoByBusinessId(businessId);
            //editTechnical
            if (Objects.isNull(info))
                info = new TechnologyInfo();

            String operator = userGroupDAO.getUserNameById(info.getTechnicalOperator());
            if (operator != null) {
                info.setTechnicalOperator(operator);
            }
            User user = AdminUtils.getUserData();
            String userId = user.getUserId();
//                List<Module> modules = tenantService.getModule(tenantId);
//                boolean editTechnical = modules.stream().anyMatch(module-> ModuleEnum.TECHNICALEDIT.getId()==module.getModuleId());
            info.setEditTechnical(true);
            //tables
            List<TechnologyInfo.Table> tables = buildTablesByBusinessIdGlobal(businessId, tenantId,null,null);
            if (publicService.isGlobal()) {
                for (TechnologyInfo.Table table : tables) {
                    table.setJump(true);
                }
            } else {
                // 查询表是否可跳转‘元数据管理’
                if (CollectionUtils.isNotEmpty(tables)) {
                    List<String> tableGuids = tables.stream().map(t -> t.getTableGuid()).collect(Collectors.toList());
                    // 当前用户是否同时有表所在数据库和数据源的查看权限
                    List<String> jumpTableGuids = businessDao.getTableJump(tableGuids, userId, tenantId);
                    for (TechnologyInfo.Table table : tables) {
                        if (jumpTableGuids.contains(table.getTableGuid())) {
                            table.setJump(true);
                        } else {
                            table.setJump(false);
                        }
                    }
                }
            }

            info.setTables(tables);
            //businessId
            info.setBusinessId(businessId);
            return info;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取关联表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取关联表失败");
        }
    }

    /**
     * 获取挂载信息-全局用户
     * @param businessId
     * @return
     * @throws AtlasBaseException
     */
    public TechnologyInfo getRelatedTableListGlobal(String businessId) throws AtlasBaseException {
        try {
            TechnologyInfo info = businessDao.selectTechnologyInfoByBusinessId(businessId);
            if (Objects.isNull(info)) {
                info = new TechnologyInfo();
            }

            String operator = userGroupDAO.getUserNameById(info.getTechnicalOperator());
            if (operator != null) {
                info.setTechnicalOperator(operator);
            }
            info.setEditTechnical(false);
            //tables
            List<TechnologyInfo.Table> tables = buildTablesByBusinessIdGlobal(businessId, info.getTrustTable());
            info.setTables(tables);
            //businessId
            info.setBusinessId(businessId);
            return info;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取关联表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取关联表失败");
        }
    }

    private List<TechnologyInfo.Table> buildTablesByBusinessId(String businessId, String tenantId, String trustTableGuid, List<TechnologyInfo.Table> tables) {
        if(tables == null ){
            tables = businessDao.queryAllTablesByBusinessId(businessId, tenantId);
        }
        User user = AdminUtils.getUserData();
        List<String> userGroupIds = userGroupDAO.getuserGroupByUsersId(user.getUserId(),tenantId).stream().map(UserGroup::getId).collect(Collectors.toList());

        tables.forEach(table -> {
            if (Objects.nonNull(table.getDisplayName())) {
                table.setDisplayName(table.getDisplayName());
            } else {
                table.setDisplayName(table.getTableName());
            }
            //table
            SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo = sourceInfoDeriveTableInfoDAO.getByNameAndDbGuid(table.getTableGuid(),tenantId);
            if (Boolean.FALSE.equals(ParamUtil.isNull(sourceInfoDeriveTableInfo))){
                if (Boolean.FALSE.equals(ParamUtil.isNull(userGroupIds))) {
                    Boolean importancePrivilege = Boolean.TRUE;
                    Boolean securityPrivilege = Boolean.TRUE;
                    List<GroupDeriveTableRelation> relations = groupDeriveTableRelationDAO.getByTableIdAndGroups(table.getTableGuid(), userGroupIds, tenantId);
                    GroupDeriveTableRelation relation = new GroupDeriveTableRelation();
                    boolean ifSecurityNull=relations.stream().allMatch(r-> r.getSecurityPrivilege()==null);
                    boolean ifImportanceNull=relations.stream().allMatch(r-> r.getImportancePrivilege()==null);
                    relation.setSecurityPrivilege(ifSecurityNull?null:relations.stream().anyMatch(GroupDeriveTableRelation::getSecurityPrivilege));
                    relation.setImportancePrivilege(ifImportanceNull?null:relations.stream().anyMatch(GroupDeriveTableRelation::getImportancePrivilege));
                    if (Boolean.TRUE.equals(sourceInfoDeriveTableInfo.getImportance()) &&
                            (Boolean.TRUE.equals(ParamUtil.isNull(relation)) ||relation.getImportancePrivilege() == null || Boolean.FALSE.equals(relation.getImportancePrivilege()))) {
                        importancePrivilege = Boolean.FALSE;

                    }
                    if (Boolean.TRUE.equals(sourceInfoDeriveTableInfo.getSecurity()) &&
                            (Boolean.TRUE.equals(ParamUtil.isNull(relation)) || relation.getSecurityPrivilege() == null || Boolean.FALSE.equals(relation.getSecurityPrivilege()))) {
                        securityPrivilege = Boolean.FALSE;
                    }
                    if (sourceInfoDeriveTableInfo.getImportance()==null ||Boolean.FALSE.equals(sourceInfoDeriveTableInfo.getImportance())){
                        importancePrivilege = null;                    }
                    if (sourceInfoDeriveTableInfo.getSecurity()==null ||Boolean.FALSE.equals(sourceInfoDeriveTableInfo.getSecurity())){
                        securityPrivilege = null;                    }
                    table.setImportancePrivilege(importancePrivilege);
                    table.setSecurityPrivilege(securityPrivilege);
                }else{
                    table.setImportancePrivilege(!sourceInfoDeriveTableInfo.getImportance());
                    table.setSecurityPrivilege(!sourceInfoDeriveTableInfo.getSecurity());
                }
            }
        });
        if(trustTableGuid ==null || trustTableGuid.isEmpty()){
            trustTableGuid = businessDao.getTrustTableGuid(businessId);
        }
        if (Objects.nonNull(trustTableGuid)) {
            String finalTrustTableGuid = trustTableGuid;
            tables.stream().filter(t->finalTrustTableGuid.equals(t.getTableGuid())).forEach(table -> table.setTrust(Boolean.TRUE));
        }
        tables.sort(Comparator.comparing(TechnologyInfo.Table::isTrust).reversed());
        return tables;
    }


    private List<TechnologyInfo.Table> buildTablesByBusinessIdGlobal(String businessId, String tenantId, String trustTableGuid, List<TechnologyInfo.Table> tables) {
        if(tables == null ){
            tables = businessDao.queryAllTablesByBusinessId(businessId, tenantId);
        }
        User user = AdminUtils.getUserData();
        List<String> userGroupIds = userGroupDAO.getuserGroupByUsersId(user.getUserId(),tenantId).stream().map(UserGroup::getId).collect(Collectors.toList());

        tables.forEach(table -> {
            if (Objects.nonNull(table.getDisplayName())) {
                table.setDisplayName(table.getDisplayName());
            } else {
                table.setDisplayName(table.getTableName());
            }
            if(publicService.isGlobal()){
                table.setImportancePrivilege(true);
                table.setSecurityPrivilege(true);
            }else {
                //table
                SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo = sourceInfoDeriveTableInfoDAO.getByNameAndDbGuid(table.getTableGuid(),tenantId);
                if (Boolean.FALSE.equals(ParamUtil.isNull(sourceInfoDeriveTableInfo))){
                    if (Boolean.FALSE.equals(ParamUtil.isNull(userGroupIds))) {
                        Boolean importancePrivilege = Boolean.TRUE;
                        Boolean securityPrivilege = Boolean.TRUE;
                        List<GroupDeriveTableRelation> relations = groupDeriveTableRelationDAO.getByTableIdAndGroups(table.getTableGuid(), userGroupIds, tenantId);
                        GroupDeriveTableRelation relation = new GroupDeriveTableRelation();
                        boolean ifSecurityNull=relations.stream().allMatch(r-> r.getSecurityPrivilege()==null);
                        boolean ifImportanceNull=relations.stream().allMatch(r-> r.getImportancePrivilege()==null);
                        relation.setSecurityPrivilege(ifSecurityNull?null:relations.stream().anyMatch(GroupDeriveTableRelation::getSecurityPrivilege));
                        relation.setImportancePrivilege(ifImportanceNull?null:relations.stream().anyMatch(GroupDeriveTableRelation::getImportancePrivilege));
                        if (Boolean.TRUE.equals(sourceInfoDeriveTableInfo.getImportance()) &&
                                (Boolean.TRUE.equals(ParamUtil.isNull(relation)) ||relation.getImportancePrivilege() == null || Boolean.FALSE.equals(relation.getImportancePrivilege()))) {
                            importancePrivilege = Boolean.FALSE;

                        }
                        if (Boolean.TRUE.equals(sourceInfoDeriveTableInfo.getSecurity()) &&
                                (Boolean.TRUE.equals(ParamUtil.isNull(relation)) || relation.getSecurityPrivilege() == null || Boolean.FALSE.equals(relation.getSecurityPrivilege()))) {
                            securityPrivilege = Boolean.FALSE;
                        }
                        if (sourceInfoDeriveTableInfo.getImportance()==null ||Boolean.FALSE.equals(sourceInfoDeriveTableInfo.getImportance())){
                            importancePrivilege = null;                    }
                        if (sourceInfoDeriveTableInfo.getSecurity()==null ||Boolean.FALSE.equals(sourceInfoDeriveTableInfo.getSecurity())){
                            securityPrivilege = null;                    }
                        table.setImportancePrivilege(importancePrivilege);
                        table.setSecurityPrivilege(securityPrivilege);
                    }else{
                        table.setImportancePrivilege(!sourceInfoDeriveTableInfo.getImportance());
                        table.setSecurityPrivilege(!sourceInfoDeriveTableInfo.getSecurity());
                    }
                }
            }
        });
        if(trustTableGuid ==null || trustTableGuid.isEmpty()){
            trustTableGuid = businessDao.getTrustTableGuid(businessId);
        }
        if (Objects.nonNull(trustTableGuid)) {
            String finalTrustTableGuid = trustTableGuid;
            tables.stream().filter(t->finalTrustTableGuid.equals(t.getTableGuid())).forEach(table -> table.setTrust(Boolean.TRUE));
        }
        tables.sort(Comparator.comparing(TechnologyInfo.Table::isTrust).reversed());
        return tables;
    }

    private List<TechnologyInfo.Table> buildTablesByBusinessIdGlobal(String businessId, String trustTableGuid) {
        List<TechnologyInfo.Table> tables =  businessDao.queryTablesByBusinessIdAndTenantId(businessId);
        if(CollectionUtils.isEmpty(tables)){
            return new ArrayList<>();
        }
        tables.forEach(table -> {
            if (Objects.nonNull(table.getDisplayName())) {
                table.setDisplayName(table.getDisplayName());
            } else {
                table.setDisplayName(table.getTableName());
            }
        });
        if (Objects.nonNull(trustTableGuid)) {
            tables.stream().filter(t->trustTableGuid.equals(t.getTableGuid())).forEach(table -> table.setTrust(Boolean.TRUE));
        }
        tables.sort(Comparator.comparing(TechnologyInfo.Table::isTrust).reversed());
        return tables;
    }

    public PageResult<BusinessInfoHeader> getBusinessListByCategoryId(String categoryId, Parameters parameters, String tenantId) throws AtlasBaseException {
        try {
            PageResult<BusinessInfoHeader> pageResult = new PageResult<>();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            String userId = AdminUtils.getUserData().getUserId();

            List<BusinessInfoHeader> list = businessDao.queryAuthBusinessByCategoryId(categoryId, limit, offset, tenantId, userId);
            String path = CategoryRelationUtils.getPath(categoryId, tenantId);
            StringJoiner joiner = null;
            String[] pathArr = path.split("/");
            String level2Category = "";
            int length = 2;
            if (pathArr.length >= length)
                level2Category = pathArr[1];
            for (BusinessInfoHeader infoHeader : list) {
                joiner = new StringJoiner(".");
                //path
                joiner.add(path);
                infoHeader.setPath(joiner.toString());
                //level2Category
                infoHeader.setLevel2Category(level2Category);
                buildTablesByBusinessId(infoHeader.getBusinessId(), tenantId,infoHeader.getTrustTable(),infoHeader.getTables()==null?new ArrayList<>():infoHeader.getTables());
                if (CollectionUtils.isEmpty(infoHeader.getTables())) {
                    infoHeader.setTechnicalStatus("0");
                } else {
                    infoHeader.setTechnicalStatus("1");
                }
            }
            Long totalSize = 0L;
            if (list.size() != 0) {
                totalSize = Long.valueOf(list.get(0).getTotal());
            }
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


    public PageResult<BusinessInfoHeader> getBusinessListByCategoryIdGlobal(String categoryId, Parameters parameters, String tenantId) throws AtlasBaseException {
        try {
            PageResult<BusinessInfoHeader> pageResult = new PageResult<>();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<BusinessInfoHeader> list = businessDao.selectByCategoryIdGlobal(categoryId, limit, offset, tenantId);
            String path = CategoryRelationUtils.getPath(categoryId, tenantId);
            StringJoiner joiner = null;
            String[] pathArr = path.split("/");
            String level2Category = "";
            int length = 2;
            if (pathArr.length >= length)
                level2Category = pathArr[1];
            for (BusinessInfoHeader infoHeader : list) {
                joiner = new StringJoiner(".");
                //path
                joiner.add(path);
                infoHeader.setPath(joiner.toString());
                //level2Category
                infoHeader.setLevel2Category(level2Category);
                List<TechnologyInfo.Table> tables = buildTablesByBusinessId(infoHeader.getBusinessId(), tenantId, infoHeader.getTrustTable(), null);
                infoHeader.setTables(tables);
                if (CollectionUtils.isEmpty(infoHeader.getTables())) {
                    infoHeader.setTechnicalStatus("0");
                } else {
                    infoHeader.setTechnicalStatus("1");
                }
            }
            Long totalSize = 0L;
            if (list.size() != 0) {
                totalSize = Long.valueOf(list.get(0).getTotal());
            }
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

    public PageResult<BusinessInfoHeader> getBusinessListByName(Parameters parameters, String tenantId) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            PageResult<BusinessInfoHeader> pageResult = new PageResult<>();
            String businessName = parameters.getQuery();
            businessName = (businessName == null ? "" : businessName);
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<BusinessInfoHeader> businessInfoList = null;
            List<String> categoryIds = new ArrayList<>();

            if (StringUtils.isEmpty(tenantId)) {
                List<CategoryEntityV2> categoryBusiness = businessCatalogueService.getCategoryBusiness(1);
                if (CollectionUtils.isNotEmpty(categoryBusiness)) {
                    categoryIds = categoryBusiness.stream().map(c -> c.getGuid()).collect(Collectors.toList());
                }
            } else {
                List<CategorycateQueryResult> allCategories = businessCatalogueService.getAllCategories(BUSINESS_TYPE, tenantId);
                if (CollectionUtils.isNotEmpty(allCategories)) {
                    categoryIds = allCategories.stream().map(c -> c.getGuid()).collect(Collectors.toList());
                }
            }
            if (Objects.isNull(categoryIds) || categoryIds.size() == 0) {
                return pageResult;
            }
            if (Objects.nonNull(businessName))
                businessName = businessName.replaceAll("%", "/%").replaceAll("_", "/_");
            try {
                businessInfoList = businessDao.queryAuthBusinessByName(businessName, categoryIds, limit, offset, tenantId, user.getUserId());
            } catch (SQLException e) {
                LOG.error("SQL执行异常", e);
                businessInfoList = new ArrayList<>();
            }

            for (BusinessInfoHeader infoHeader : businessInfoList) {
                if(StringUtils.isEmpty(tenantId)){
                    tenantId = infoHeader.getTenantId();
                }
                String path = CategoryRelationUtils.getPath(infoHeader.getCategoryGuid(), tenantId);
                StringJoiner joiner = new StringJoiner(".");
                joiner.add(path);
                infoHeader.setPath(joiner.toString());
                String[] pathArr = path.split("/");
                String level2Category = "";
                int length = 2;
                if (pathArr.length >= length)
                    level2Category = pathArr[1];
                infoHeader.setLevel2Category(level2Category);
            }
            long businessTotal = 0;
            if (businessInfoList.size() != 0) {
                businessTotal = businessInfoList.get(0).getTotal();
            }
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

    /**
     * 获取业务对象-全局查询
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<BusinessInfoHeader> getBusinessListByNameGlobal(Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<BusinessInfoHeader> pageResult = new PageResult<>();
            String businessName = parameters.getQuery() == null ? "" : parameters.getQuery();
            businessName = businessName.replaceAll("%", "\\\\%").replaceAll("_", "\\\\_");
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<BusinessInfoHeader> businessInfoList = businessDao.selectBusinessByNameGlobal(businessName, limit, offset);
            if (CollectionUtils.isEmpty(businessInfoList)) {
                pageResult.setTotalSize(0);
                pageResult.setLists(new ArrayList<>());
                pageResult.setCurrentSize(0);
                return pageResult;
            }
            for (BusinessInfoHeader infoHeader : businessInfoList) {
                String path = CategoryRelationUtils.getPath(infoHeader.getCategoryGuid(), infoHeader.getTenantId());
                StringJoiner joiner = new StringJoiner(".");
                joiner.add(path);
                infoHeader.setPath(joiner.toString());
                String[] pathArr = path.split("/");
                String level2Category = "";
                int length = 2;
                if (pathArr.length >= length)
                    level2Category = pathArr[1];
                infoHeader.setLevel2Category(level2Category);
            }
            pageResult.setTotalSize(businessInfoList.get(0).getTotal());
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

    public PageResult<BusinessInfoHeader> getBusinessListByCondition(BusinessQueryParameter parameter, String tenantId) throws AtlasBaseException {
        User user = AdminUtils.getUserData();
        PageResult<BusinessInfoHeader> pageResult = new PageResult<>();
        String status = parameter.getStatus();
        String ticketNumber = parameter.getTicketNumber();
        ticketNumber = (ticketNumber == null ? "" : ticketNumber);
        String businessName = parameter.getName();
        businessName = (businessName == null ? "" : businessName);
        String level2CategoryId = parameter.getLevel2CategoryId();
        String submitter = parameter.getSubmitter();
        submitter = (submitter == null ? "" : submitter);
        int limit = parameter.getLimit();
        int offset = parameter.getOffset();
        Integer technicalStatus = TechnicalStatus.getCodeByDesc(status);
        List<String> categoryIds = new ArrayList<>();

        if (StringUtils.isEmpty(tenantId)) {
            List<CategoryEntityV2> categoryBusiness = businessCatalogueService.getCategoryBusiness(1);
            if (CollectionUtils.isNotEmpty(categoryBusiness)) {
                categoryIds = categoryBusiness.stream().map(c -> c.getGuid()).collect(Collectors.toList());
            }
        } else {
            List<CategorycateQueryResult> allCategories = businessCatalogueService.getAllCategories(BUSINESS_TYPE, tenantId);
            if (CollectionUtils.isNotEmpty(allCategories)) {
                categoryIds = allCategories.stream().map(c -> c.getGuid()).collect(Collectors.toList());
            }
        }
        if (categoryIds.size() > 0) {
            if (Objects.nonNull(businessName))
                businessName = businessName.replaceAll("%", "/%").replaceAll("_", "/_");
            if (Objects.nonNull(ticketNumber))
                ticketNumber = ticketNumber.replaceAll("%", "/%").replaceAll("_", "/_");
            if (Objects.nonNull(submitter))
                submitter = submitter.replaceAll("%", "/%").replaceAll("_", "/_");
            List<BusinessInfoHeader> businessInfoList = businessDao.queryAuthBusinessByCondition(categoryIds, technicalStatus, ticketNumber, businessName, level2CategoryId, submitter, limit, offset, tenantId, user.getUserId());
            for (BusinessInfoHeader infoHeader : businessInfoList) {
                String path = CategoryRelationUtils.getPath(infoHeader.getDepartmentId(), tenantId);
                infoHeader.setPath(path + "." + infoHeader.getName());
                String[] pathArr = path.split("/");
                int length = 2;
                if (pathArr.length >= length)
                    infoHeader.setLevel2Category(pathArr[1]);
                buildTablesByBusinessId(infoHeader.getBusinessId(), tenantId,infoHeader.getTrustTable(),infoHeader.getTables()==null?new ArrayList<>():infoHeader.getTables());
            }
            pageResult.setLists(businessInfoList);
            long totalsize = 0;
            if (businessInfoList.size() != 0) {
                totalsize = businessInfoList.get(0).getTotal();
            }
            pageResult.setTotalSize(totalsize);
            pageResult.setCurrentSize(businessInfoList.size());
        }
        return pageResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addBusinessAndTableRelation(String businessId, BusinessTableList tableIdList) throws AtlasBaseException {
        List<BusinessTable> tableList = tableIdList.getTableList();
        String trustTable = tableIdList.getTrust();
        try {
            String userId = AdminUtils.getUserData().getUserId();
            long timestamp = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(timestamp);
            businessDao.updateTechnicalInfo(businessId, userId, time);
            //更新technical编辑状态
            if (CollectionUtils.isNotEmpty(tableList)) {
                businessDao.updateTechnicalStatus(businessId, TechnicalStatus.ADDED.code);
            } else {
                businessDao.updateTechnicalStatus(businessId, TechnicalStatus.BLANK.code);
            }
            businessDao.deleteRelationByBusinessId(businessId);

            businessDao.updateTrustTable(businessId);

            if (CollectionUtils.isNotEmpty(tableList)) {
                // 关联类型：0通过业务对象挂载功能挂载到该业务对象的表；1通过衍生表登记模块登记关联到该业务对象上的表
                businessDao.insertTableRelation(businessId, tableList, 0);
                if (Objects.isNull(trustTable)) {
                    trustTable = tableList.get(0).getTableGuid();
                }
            }
            if (Objects.nonNull(trustTable)) {
                businessDao.setBusinessTrustTable(businessId, trustTable);
            }

        } catch (Exception e) {
            LOG.error("更新技术信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新技术信息失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBusiness(String businessId) throws AtlasBaseException {
        try {
            businessDao.deleteBusinessById(businessId);
            businessDao.deleteRelationByBusinessId(businessId);
            businessDao.deleteRelationById(businessId);
            businessDao.deleteGroupRelationByBusinessIds(Lists.newArrayList(businessId));
        } catch (Exception e) {
            LOG.error("删除业务对象失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除业务对象失败");
        }
    }


    public int addColumnPrivilege(ColumnPrivilege privilege) throws AtlasBaseException {
        try {
            String columnName = privilege.getName();
            int count = columnPrivilegeDAO.queryNameCount(columnName);
            if (count > 0) {
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
            PGobject pGobject = (PGobject) columnObject;
            String value = pGobject.getValue();
            List<String> values = gson.fromJson(value, List.class);
            return values;
        } catch (Exception e) {
            LOG.error("获取权限字段取值失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取权限字段取值失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int addColumnPrivilegeRelation(ColumnPrivilegeRelation relation) throws AtlasBaseException {
        try {
            int columnPrivilegeGuid = relation.getColumnPrivilegeGuid();
            ColumnPrivilegeRelation columnRelation = columnPrivilegeDAO.queryPrivilegeRelation(columnPrivilegeGuid);
            if (Objects.nonNull(columnRelation)) {
                return columnPrivilegeDAO.updateColumnPrivilegeRelation(relation);
            } else {
                return columnPrivilegeDAO.addColumnPrivilegeRelation(relation);
            }
        } catch (Exception e) {
            LOG.error("添加权限字段关联失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加权限字段关联失败");
        }
    }


    public PageResult<APIInfoHeader> getBusinessTableRelatedAPI(String businessGuid, Parameters parameters, String tenantId) throws AtlasBaseException {
        try {
            TechnologyInfo technologyInfo = getRelatedTableList(businessGuid, tenantId);
            List<TechnologyInfo.Table> tableHeaderList = technologyInfo.getTables();
            List<String> tableList = new ArrayList<>();
            tableHeaderList.stream().forEach(table -> tableList.add(table.getTableGuid()));
            Integer limit = parameters.getLimit();
            Integer offset = parameters.getOffset();
            List<APIInfoHeader> apiList = new ArrayList<>();
            PageResult<APIInfoHeader> pageResult = new PageResult<>();
            int totalSize = 0;
            if (Objects.nonNull(tableList) && tableList.size() > 0) {
                apiList = shareDAO.getTableRelatedAPI(tableList, limit, offset, tenantId);
                for (APIInfoHeader api : apiList) {
                    String displayName = api.getTableDisplayName();
                    if (Objects.isNull(displayName) || "".equals(displayName)) {
                        api.setTableDisplayName(api.getTableName());
                    }
                    List<DataOwnerHeader> dataOwner = metaDataService.getDataOwner(api.getTableGuid());
                    List<String> dataOwnerName = new ArrayList<>();
                    if (Objects.nonNull(dataOwner) && dataOwner.size() > 0) {
                        dataOwner.stream().forEach(owner -> dataOwnerName.add(owner.getName()));
                    }
                    api.setDataOwner(dataOwnerName);
                }
                if (apiList.size() != 0) {
                    totalSize = apiList.get(0).getTotal();
                }
            }
            pageResult.setTotalSize(totalSize);
            pageResult.setLists(apiList);
            pageResult.setCurrentSize(apiList.size());
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取关联API失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取关联API失败");
        }
    }

    public PageResult<ApiHead> getBusinessTableRelatedDataServiceAPI(String businessGuid, Parameters parameters, boolean isNew, boolean up, boolean down, String tenantId) throws AtlasBaseException {
        TechnologyInfo technologyInfo = getRelatedTableList(businessGuid, tenantId);
        List<TechnologyInfo.Table> tableHeaderList = technologyInfo.getTables();
        List<String> tableList = new ArrayList<>();
        tableHeaderList.stream().forEach(table -> tableList.add(table.getTableGuid()));
        Integer limit = parameters.getLimit();
        Integer offset = parameters.getOffset();
        List<ApiHead> apiList = new ArrayList<>();
        List<ApiHead> apiTempList = new ArrayList<>();
        PageResult<ApiHead> pageResult = new PageResult<>();
        int totalSize = 0;
        //查询HIVE数据表关联的API
        if (Objects.nonNull(tableList) && tableList.size() > 0) {
            apiList = shareDAO.getTableRelatedDataServiceAPI(tableList, limit, offset, tenantId, up, down, isNew);
            for (ApiHead api : apiList) {
                String displayName = api.getTableDisplayName();
                if (Objects.isNull(displayName) || "".equals(displayName)) {
                    api.setTableDisplayName(api.getTableName());
                }
                List<DataOwnerHeader> dataOwner = metaDataService.getDataOwner(api.getTableGuid());
                List<String> dataOwnerName = new ArrayList<>();
                if (Objects.nonNull(dataOwner) && dataOwner.size() > 0) {
                    dataOwner.stream().forEach(owner -> dataOwnerName.add(owner.getName()));
                }
                api.setDataOwner(dataOwnerName);
            }
            if (apiList.size() != 0) {
                totalSize = apiList.get(0).getTotal();
            }
        }
        //查询其他类型数据表关联的API
        if (CollectionUtils.isNotEmpty(tableHeaderList)) {
            for (TechnologyInfo.Table table : tableHeaderList) {
                ApiHead apiHead = shareDAO.getTableRelatedDataServiceAPIByTableName(table.getSourceId(), table.getDbName(), table.getTableName(), table.getTableGuid(), tenantId, up, down, isNew);
                if (null == apiHead) {
                    continue;
                }
                String tableId = table.getTableGuid();
                String displayName = apiHead.getTableDisplayName();
                if (Objects.isNull(displayName) || "".equals(displayName)) {
                    apiHead.setTableDisplayName(apiHead.getTableName());
                }
                List<DataOwnerHeader> dataOwner = metaDataService.getDataOwner(tableId);
                List<String> dataOwnerName = new ArrayList<>();
                if (Objects.nonNull(dataOwner) && dataOwner.size() > 0) {
                    dataOwner.stream().forEach(owner -> dataOwnerName.add(owner.getName()));
                }
                apiHead.setTableGuid(tableId);
                apiHead.setDataOwner(dataOwnerName);
                apiTempList.add(apiHead);
            }
        }
        int tempSize = apiTempList.size();
        apiList.addAll(apiTempList);

        pageResult.setTotalSize(totalSize + tempSize);
        pageResult.setLists(apiList);
        pageResult.setCurrentSize(apiList.size());
        return pageResult;
    }

    @Transactional(rollbackFor = Exception.class)
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
            query = (query == null ? "" : query);

            if (Objects.nonNull(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");

            Integer limit = parameters.getLimit();
            Integer offset = parameters.getOffset();
            List<TableHeader> tableHeaderList = businessDao.getBusinessRelatedTableList(businessId, query, limit, offset);

            tableHeaderList.forEach(tableHeader -> {
                String displayName = tableHeader.getDisplayName();
                String tableName = tableHeader.getTableName();
                if (Objects.isNull(displayName) || "".equals(displayName.trim())) {
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

    public PageResult getTableColumnList(String tableGuid, Parameters parameters, String sortColumn, String sortOrder, boolean isNumber) throws AtlasBaseException {
        try {
            boolean existOnPg = columnDAO.tableColumnExist(tableGuid) > 0 ? true : false;
            PageResult pageResult = new PageResult();
            if (!existOnPg) {
                //JanusGraph中取出column信息
                String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.COLUMN_INFO_MAP);
                String columnQuery = String.format(query, tableGuid);
                List<Map> columnMapList = (List<Map>) graph.executeGremlinScript(columnQuery, false);
                List<Column> columnInfoList = convertMapToColumnInfoList(tableGuid, columnMapList);
                if (Objects.isNull(columnInfoList) || columnInfoList.size() == 0) {
                    return pageResult;
                }
                columnDAO.addColumnDisplayInfo(columnInfoList);
            }
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            String queryText = parameters.getQuery();
            if (Objects.nonNull(queryText))
                queryText = queryText.replaceAll("%", "/%").replaceAll("_", "/_");
            String sqlSortOrder = Objects.nonNull(sortOrder) ? sortOrder.toLowerCase() : "asc";
            String sqlsortColumn = (Objects.nonNull(sortColumn) && "updatetime".equals(sortColumn.toLowerCase())) ? "display_updatetime" : "column_name";

            //过滤数值型字段
            List<String> columnType = null;
            if (isNumber) {
                columnType = Arrays.stream(HiveNumericType.values()).filter(type -> type.getCode() != 7).map(HiveNumericType::getName).collect(Collectors.toList());
            }
            List<Column> resultColumnInfoList = columnDAO.getTableColumnList(tableGuid, queryText, sqlsortColumn, sqlSortOrder, limit, offset, columnType);
            int totalCount = 0;
            if (resultColumnInfoList.size() != 0) {
                totalCount = resultColumnInfoList.get(0).getTotal();
            }
            resultColumnInfoList.get(0).setDescription("abc");

            resultColumnInfoList.forEach(column -> {
                if (Objects.isNull(column.getDisplayName()) || "".equals(column.getColumnName().trim())) {
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

            if (existOnPg) {
                for (Column column : columns) {
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
            boolean existOnPg = columnDAO.tableColumnExist(tableGuid) > 0 ? true : false;
            //字段信息
            List<Column> columnInfoList = null;

            if (existOnPg) {
                column.setDisplayNameOperator(userId);
                column.setDisplayNameUpdateTime(time);
                columnDAO.updateColumnInfo(column);
            } else {

                //JanusGraph中取出column信息
                String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.COLUMN_INFO_MAP);
                String columnQuery = String.format(query, tableGuid);
                List<Map> columnMapList = (List<Map>) graph.executeGremlinScript(columnQuery, false);
                columnInfoList = convertMapToColumnInfoList(tableGuid, columnMapList);
                List<String> editColumnList = new ArrayList<>();
                String editColumnId = column.getColumnId();
                columnInfoList.forEach(col -> {
                    if (col.getColumnId().equals(editColumnId)) {
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
            for (Column column : columnwithDisplayList) {
                String columnName = column.getColumnName();
                columnCheckInfo = new ColumnCheckMessage.ColumnCheckInfo();
                columnCheckInfo.setRow(index++);
                columnCheckInfo.setColumnName(columnName);
                //是否为重复字段
                if (recordColumnList.contains(columnName)) {
                    columnCheckInfo.setErrorMessage("导入重复字段");
                    errorColumnList.add(columnName);
                    errorColumnCount++;
                    columnCheckMessageList.add(columnCheckInfo);

                    //表中是否存在当前字段
                } else if (columnList.contains(columnName)) {
                    columnCheckInfo.setErrorMessage("匹配成功");
                    String displayText = column.getDisplayName();
                    //未填写别名默认为字段名
                    if (Objects.isNull(displayText) || Objects.equals(displayText.trim(), "")) {
                        columnCheckInfo.setDisplayText(columnName);
                    } else {
                        if (displayText.length() > 64) {
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
            if (errorColumnCount == 0) {

                List<Column> nonEditColumnInfoList = null;
                if (!existOnPg) {
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
                    if (Objects.isNull(col.getDisplayName()) || "".equals(col.getDisplayName().trim())) {
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
        for (Map obj : columnInfoList) {
            List<String> guidList = (List) obj.get("__guid");
            List<String> nameList = (List) obj.get("Asset.name");
            String guid = null;
            String name = null;
            if (Objects.nonNull(guidList) && guidList.size() > 0) {
                guid = guidList.get(0);
            }
            if (Objects.nonNull(nameList) && nameList.size() > 0) {
                name = nameList.get(0);
            }
            if (Objects.nonNull(name) && Objects.nonNull(guid)) {
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
            boolean existOnPg = columnDAO.tableColumnExist(tableGuid) > 0 ? true : false;
            //字段信息
            List<Column> columnInfoList = null;
            if (existOnPg) {
                //pg中取出column信息
                columnInfoList = columnDAO.getColumnInfoList(tableGuid);
            } else {
                //JanusGraph中取出column信息
                String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.COLUMN_INFO_MAP);
                String columnQuery = String.format(query, tableGuid);
                List<Map> columnMapList = (List<Map>) graph.executeGremlinScript(columnQuery, false);
                columnInfoList = convertMapToColumnInfoList(tableGuid, columnMapList);
            }
            return checkColumnName(tableGuid, columnInfoList, columnAndDisplayMap, existOnPg);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }

    public List<Column> convertMapToColumnInfoList(String tableGuid, List<Map> columnMapList) {
        List<Column> columnInfoList = new ArrayList<>();
        for (Map obj : columnMapList) {
            List<String> guidList = (List) obj.get("__guid");
            List<String> nameList = (List) obj.get("Asset.name");
            List<String> typeList = (List) obj.get("hive_column.type");
            List<String> stateList = (List) obj.get("__state");
            List<Long> modifyTimeList = (List) obj.get("__modificationTimestamp");
            List<String> commonList = (List) obj.get("hive_column.comment");
            String guid = null;
            String name = null;
            String type = null;
            String state = null;
            String updateTime = null;
            String description = null;

            if (Objects.nonNull(guidList) && guidList.size() > 0) {
                guid = guidList.get(0);
            }
            if (Objects.nonNull(nameList) && nameList.size() > 0) {
                name = nameList.get(0);
            }
            if (Objects.nonNull(typeList) && typeList.size() > 0) {
                type = typeList.get(0);
            }
            if (Objects.nonNull(stateList) && stateList.size() > 0) {
                state = stateList.get(0);
            }
            if (Objects.nonNull(modifyTimeList) && modifyTimeList.size() > 0) {
                Long time = modifyTimeList.get(0);
                updateTime = DateUtils.date2String(new Date(time));
            }
            if (Objects.nonNull(commonList) && commonList.size() > 0) {
                description = commonList.get(0);
            }

            Column column = new Column();
            column.setTableId(tableGuid);
            column.setColumnId(guid);
            column.setColumnName(name);
            column.setType(type);
            column.setStatus(state);
            column.setDisplayNameUpdateTime(updateTime);
            column.setDescription(description);
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
            key = Objects.nonNull(keyCell) ? keyCell.getStringCellValue() : "";
            value = Objects.nonNull(valueCell) ? valueCell.getStringCellValue() : "";
            String header1 = "字段名称";
            String header2 = "显示名称";
            if (!header1.equals(key) || !header2.equals(value)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Excel表头错误，表头名称应为【字段名称】和【显示名称】");
            }
            for (int i = 1; i < rowNum; i++) {
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
                if (valueCell == null) {
                    value = "";
                } else {
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
            boolean existOnPg = columnDAO.tableColumnExist(tableGuid) > 0 ? true : false;
            List<Map> columnMapList = null;
            List<String> columnList = new ArrayList<>();
            List<Column> columnInfoList = null;
            if (existOnPg) {
                columnInfoList = columnDAO.getColumnNameWithDisplayList(tableGuid);
            } else {
                String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.COLUMN_NAME_LIST);
                String columnQuery = String.format(query, tableGuid);
                columnMapList = (List<Map>) graph.executeGremlinScript(columnQuery, false);
                columnMapList.forEach(obj -> {
                    List<String> nameList = (List) obj.get("Asset.name");
                    if (Objects.nonNull(nameList) && nameList.size() > 0) {
                        columnList.add(nameList.get(0));
                    }
                });
            }
            List<String> attributes = new ArrayList<>();
            attributes.add("字段名称");
            attributes.add("显示名称");

            List<List<String>> datas = new ArrayList<>();
            List<String> data = null;
            if (existOnPg) {
                for (Column column : columnInfoList) {
                    data = new ArrayList<>();
                    String displayName = column.getDisplayName();
                    data.add(column.getColumnName());
                    data.add(Objects.nonNull(displayName) ? displayName : String.valueOf(""));
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

    public Object getTableInfoById(String guid, String tenantId) throws AtlasBaseException {
        try {
            if (metaDataService.isHiveTable(guid)) {
                Table table = metaDataService.getTableInfoById(guid, tenantId);
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
            } else {
                RDBMSTable table = metaDataService.getRDBMSTableInfoById(guid, tenantId,null);
                String tableName = table.getTableName();
                String tableDisplayName = table.getDisplayName();
                if (Objects.isNull(tableDisplayName) || "".equals(tableDisplayName.trim())) {
                    table.setDisplayName(tableName);
                }
                List<RDBMSColumn> columnList = table.getColumns();
                columnList.forEach(column -> {
                    String columnName = column.getColumnName();
                    String displayName = column.getDisplayName();
                    if (Objects.isNull(displayName) || "".equals(displayName.trim())) {
                        column.setDisplayName(columnName);
                    }
                });
                return table;
            }

        } catch (Exception e) {
            LOG.error("获取表信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取表信息失败");
        }
    }

    public File exportExcelColumn(String tableId) throws IOException, AtlasBaseException {
        List<Column> data = getColumnByTable(tableId);
        Workbook workbook = columnData2workbook(data);
        return workbook2file(workbook, "column");
    }

    public List<Column> getColumnByTable(String guid) throws AtlasBaseException {
        ColumnQuery columnQuery = new ColumnQuery();
        columnQuery.setGuid(guid);
        List<Column> columnInfoById = metaDataService.getColumnInfoById(columnQuery, true);
        return columnInfoById;
    }

    private Workbook columnData2workbook(List<Column> list) {
        Workbook workbook = new XSSFWorkbook();
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);
        List<List<String>> dataList = list.stream().map(column -> {
            List<String> data = Lists.newArrayList(column.getColumnName(), column.getType(), column.getDescription(), column.getTableName(), column.getDatabaseName());
            return data;
        }).collect(Collectors.toList());
        ArrayList<String> attributes = Lists.newArrayList("字段名称", "字段类型", "字段描述", "表名称", "库名称");
        PoiExcelUtils.createSheet(workbook, "业务对象", attributes, dataList, cellStyle, 12);
        return workbook;
    }

    public File exportExcelBusiness(List<String> ids, String categoryId, String tenantId) throws IOException {
        List<BusinessInfo> data;
        if (ids == null) {
            String userId = AdminUtils.getUserData().getUserId();
            data = businessDao.queryAllAuthBusinessByCategoryId(categoryId, tenantId, userId);
        } else if (ids.size() == 0) {
            data = new ArrayList<>();
        } else {
            data = businessDao.getBusinessByIds(ids, categoryId, tenantId);
        }
        String path = null;
        String pathStr = categoryDao.queryPathByGuid(categoryId, tenantId);
        if (pathStr != null) {
            path = pathStr.substring(1, pathStr.length() - 1).replace(",", "/").replace("\"", "");
        } else {
            path = categoryDao.getCategoryNameById(categoryId, tenantId);
        }
        Workbook workbook = data2workbook(data, path);
        return workbook2file(workbook, "business");
    }

    private Workbook data2workbook(List<BusinessInfo> list, String path) {
        Workbook workbook = new XSSFWorkbook();
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);
        //AtomicInteger index = new AtomicInteger(1);
        List<List<String>> dataList = list.stream().map(businessInfo -> {
            List<String> data = Lists.newArrayList(businessInfo.getName(), businessInfo.getModule(), businessInfo.getDescription(),
                    businessInfo.getOwner(), businessInfo.getManager(), businessInfo.getMaintainer(), businessInfo.getDataAssets());
            return data;
        }).collect(Collectors.toList());
        ArrayList<String> attributes = Lists.newArrayList("业务对象名称", "业务模块", "业务描述", "所有者", "管理者", "维护者", "相关数据资产");
        PoiExcelUtils.createSheet(workbook, "业务对象", attributes, dataList, cellStyle, 12);
        return workbook;
    }

    private File workbook2file(Workbook workbook, String name) throws IOException {
        File tmpFile = File.createTempFile(name, ".xlsx");
        try (FileOutputStream output = new FileOutputStream(tmpFile)) {
            workbook.write(output);
            output.flush();
            output.close();
        }
        return tmpFile;
    }

    public Map<String, Object> uploadBusiness(File fileInputStream, String tenantId) throws Exception {
        List<BusinessInfo> business;
        List<String> error = new ArrayList<>();
        try {
            business = file2Data(fileInputStream, new ArrayList<>(), error, tenantId);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("数据转换失败", e);
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "文件异常");
        }
        HashMap<String, Object> map;
        if (error.size() == 0) {
            String upload = ExportDataPathUtils.transferTo(fileInputStream);
            map = new HashMap<String, Object>() {{
                put("upload", upload);
            }};
        } else {
            StringBuilder detail = new StringBuilder();
            for (String err : error) {
                detail.append(err);
                detail.append("\n");
            }
            String substring = detail.substring(0, detail.length() - 1);
            throw new AtlasBaseException(substring, AtlasErrorCode.BAD_REQUEST, "文件异常");
        }
        return map;
    }

    /**
     * 文件转化为业务对象
     *
     * @param file
     * @return
     * @throws Exception
     */
    private List<BusinessInfo> file2Data(File file, List<CategoryEntityV2> systemCategory, List<String> error, String tenantId) throws Exception {
        String cellFormat = "第%d页，第%d个业务对象错误，原因：%s";
        String sheetFormat = "第%d页错误，原因：%s";
        List<BusinessInfo> business = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file)) {
            int numberOfSheets = workbook.getNumberOfSheets();
            List<String> fileNames = new ArrayList<>();
            List<String> businessNames = businessDao.getBusinessNames(tenantId);
            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowNum = sheet.getLastRowNum() + 1;
                //文件格式校验
                Row first = sheet.getRow(0);
                ArrayList<String> strings = Lists.newArrayList("业务对象名称", "业务模块", "业务描述", "所有者", "管理者", "维护者", "相关数据资产");
                for (int j = 0; j < strings.size(); j++) {
                    Cell cell = first.getCell(j);
                    if (!strings.get(j).equals(cell.getStringCellValue())) {
                        error.add(String.format(sheetFormat, i, "文件内容不正确"));
                        break;
                    }
                }

                for (int j = 1; j < rowNum; j++) {
                    Row row = sheet.getRow(j);
                    BusinessInfo businessInfo = new BusinessInfo();

                    Cell nameCell = row.getCell(0);
                    if (Objects.isNull(nameCell)) {
                        error.add(String.format(cellFormat, i + 1, j, "业务名称不能为空"));
                        continue;
                    }
                    String name = PoiExcelUtils.getCellValue(nameCell);
                    if (fileNames.contains(name)) {
                        error.add(String.format(cellFormat, i + 1, j, "与文件内业务对象重名"));
                        continue;
                    } else if (businessNames.contains(name)) {
                        error.add(String.format(cellFormat, i + 1, j, "与已有业务对象重名"));
                        continue;
                    }
                    businessInfo.setName(name);

                    Cell moduleCell = row.getCell(1);
                    if (Objects.isNull(moduleCell)) {
                        error.add(String.format(cellFormat, i + 1, j, "业务模块不能为空"));
                        continue;
                    }
                    businessInfo.setModule(PoiExcelUtils.getCellValue(moduleCell));

                    Cell discriptionCell = row.getCell(2);
                    if (Objects.isNull(discriptionCell)) {
                        businessInfo.setDescription("");
                    } else {
                        businessInfo.setDescription(PoiExcelUtils.getCellValue(discriptionCell));
                    }

                    Cell ownerCell = row.getCell(3);
                    businessInfo.setOwner(PoiExcelUtils.getCellValue(ownerCell));

                    Cell mangerCell = row.getCell(4);
                    businessInfo.setManager(PoiExcelUtils.getCellValue(mangerCell));

                    Cell maintainerCell = row.getCell(5);
                    businessInfo.setMaintainer(PoiExcelUtils.getCellValue(maintainerCell));

                    Cell dataAssetsCell = row.getCell(6);
                    businessInfo.setDataAssets(PoiExcelUtils.getCellValue(dataAssetsCell));
                    business.add(businessInfo);
                    fileNames.add(name);
                }
            }
            return business;
        }
    }

    public List<String> getNamesByIds(List<String> ids, String tenantId) {
        if (ids == null || ids.size() == 0) {
            return new ArrayList<>();
        }
        List<String> businessNames = businessDao.getBusinessNamesByIds(ids, tenantId);
        return businessNames;
    }

    @Transactional(rollbackFor = Exception.class)
    public void importBusiness(File fileInputStream, String categoryId, String tenantId) throws Exception {
        if (!fileInputStream.exists()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件丢失，请重新上传");
        }
        List<BusinessInfo> business;
        List<String> error = new ArrayList<>();
        try {
            business = file2Data(fileInputStream, new ArrayList<>(), error, tenantId);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("数据转换失败", e);
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "文件异常");
        }
        if (error.size() != 0) {
            throw new AtlasBaseException(error.toString(), AtlasErrorCode.BAD_REQUEST, "数据异常");
        }
        if (business == null || business.size() == 0) {
            return;
        }
        insertBusinesses(business, categoryId, tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void insertBusinesses(List<BusinessInfo> business, String categoryId, String tenantId) throws AtlasBaseException {
        String userId = AdminUtils.getUserData().getUserId();
        long timestamp = System.currentTimeMillis();
        String time = DateUtils.getNow();
        //level2CategoryId
        String pathStr = categoryDao.queryGuidPathByGuid(categoryId, tenantId);
        String path = pathStr.substring(1, pathStr.length() - 1);
        path = path.replace("\"", "");
        String[] pathArr = path.split(",");
        String level2CategoryId = "";
        int length = 2;
        if (pathArr.length >= length) {
            level2CategoryId = pathArr[1];
        }
        List<BusinessRelationEntity> entitys = new ArrayList<>();
        for (BusinessInfo info : business) {
            //departmentId(categoryId)
            info.setDepartmentId(categoryId);
            //submitter && businessOperator

            info.setSubmitter(userId);
            info.setBusinessOperator(userId);
            //businessId
            String businessId = UUID.randomUUID().toString();
            info.setBusinessId(businessId);
            //submissionTime && businessLastUpdate && ticketNumber

            info.setSubmissionTime(time);
            info.setBusinessLastUpdate(time);
            info.setTicketNumber(String.valueOf(timestamp));

            info.setLevel2CategoryId(level2CategoryId);

            // 文件导入方式，默认发布开关关闭
            info.setPublish(false);
            info.setStatus(Status.FOUNDED.getIntValue() + "");
            info.setPrivateStatus("PRIVATE");
            // 上传文件添加方式：0手动添加，1上传文件
            info.setCreateMode(1);
            // 创建人可见
            info.setSubmitterRead(true);

            BusinessRelationEntity entity = new BusinessRelationEntity();
            //relationshiGuid
            String relationGuid = UUID.randomUUID().toString();
            entity.setRelationshipGuid(relationGuid);

            entity.setBusinessId(businessId);
            entity.setCategoryGuid(categoryId);
            entity.setGenerateTime(time);
            entitys.add(entity);
        }
        businessDao.insertBusinessInfos(business, tenantId);
        businessDao.addRelations(entitys);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteBusinesses(List<String> ids) throws AtlasBaseException {
        try {
            if (ids == null || ids.size() == 0) {
                return 0;
            }
            int num = businessDao.deleteBusinessesByIds(ids);
            businessDao.deleteRelationByBusinessIds(ids);
            businessDao.deleteRelationByIds(ids);
            businessDao.deleteGroupRelationByBusinessIds(ids);
            return num;
        } catch (Exception e) {
            LOG.error("删除业务对象失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除业务对象失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int moveBusinesses(CategoryItem categoryItem) throws AtlasBaseException {
        try {
            int num = businessDao.updateBusinessInfoCategory(categoryItem.getIds(), categoryItem.getCategoryId());
            businessDao.updateBusinessRelation(categoryItem.getIds(), categoryItem.getCategoryId());
            return num;
        } catch (Exception e) {
            LOG.error("迁移业务对象失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "迁移业务对象失败");
        }
    }

    public PageResult<Table> checkTable(String tenantId, int limit, int offset) throws AtlasBaseException {
        PageResult<Table> pageResult = new PageResult<>();
        BusinessQueryParameter parameter = new BusinessQueryParameter();
        parameter.setOffset(0);
        parameter.setLimit(-1);
        parameter.setStatus("added");
        PageResult<BusinessInfoHeader> businessListByCondition = getBusinessListByCondition(parameter, tenantId);
        if (businessListByCondition.getLists() == null || businessListByCondition.getLists().size() == 0) {
            pageResult.setLists(new ArrayList<>());
            return pageResult;
        }
        List<String> businessIds = businessListByCondition.getLists().stream().map(businessInfoHeader -> businessInfoHeader.getBusinessId()).collect(Collectors.toList());
        List<Table> tables = businessDao.getTablesByBusiness(businessIds, limit, offset);
        if (tables == null || tables.size() == 0) {
            pageResult.setLists(new ArrayList<>());
            return pageResult;
        }
        pageResult.setLists(tables);
        pageResult.setTotalSize(tables.get(0).getTotal());
        pageResult.setCurrentSize(tables.size());
        return pageResult;
    }

    public PageResult<Table> checkColumn(String tenantId, int limit, int offset) throws AtlasBaseException {
        PageResult<Table> pageResult = new PageResult<>();
        BusinessQueryParameter parameter = new BusinessQueryParameter();
        parameter.setOffset(0);
        parameter.setLimit(-1);
        parameter.setStatus("added");
        PageResult<BusinessInfoHeader> businessListByCondition = getBusinessListByCondition(parameter, tenantId);
        if (businessListByCondition.getLists() == null || businessListByCondition.getLists().size() == 0) {
            pageResult.setLists(new ArrayList<>());
            return pageResult;
        }
        List<String> businessIds = businessListByCondition.getLists().stream().map(businessInfoHeader -> businessInfoHeader.getBusinessId()).collect(Collectors.toList());
        List<Table> tables = businessDao.getTablesByBusinessAndColumn(businessIds, limit, offset);
        if (tables == null || tables.size() == 0) {
            pageResult.setLists(new ArrayList<>());
            return pageResult;
        }
        List<Column> columns = columnDAO.checkDescriptionColumnByTableIds(tables);
        Map<String, List<Column>> map = columns.stream().collect(Collectors.groupingBy(column -> column.getTableId()));
        tables.stream().forEach(table -> table.setColumns(map.get(table.getTableId())));
        pageResult.setLists(tables);
        pageResult.setTotalSize(tables.get(0).getTotal());
        pageResult.setCurrentSize(tables.size());
        return pageResult;
    }

    /**
     * 业务对象表描述空值检查下载
     *
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     * @throws IOException
     */
    public File checkData2File(String tenantId) throws AtlasBaseException, IOException {
        //获取检测数据
        PageResult<Table> tablePageResult = checkTable(tenantId, -1, 0);
        PageResult<Table> columnPageResult = checkColumn(tenantId, -1, 0);
        Workbook workbook = new XSSFWorkbook();
        //标题格式
        CellStyle cellStyle = workbook.createCellStyle();
        //表头格式
        CellStyle cellStyle2 = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);
        cellStyle2.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        //合并单元格
        CellRangeAddress tableRegion = new CellRangeAddress(0, 0, 0, 2);
        Sheet sheet = workbook.createSheet("总览");
        sheet.addMergedRegion(tableRegion);
        //标题
        Row tableRow = sheet.createRow(0);
        Cell tableCell = tableRow.createCell(0);
        tableCell.setCellStyle(cellStyle);
        tableCell.setCellValue("表描述空值检查");
        //表头
        ArrayList<String> firstAttributes = Lists.newArrayList("表名称", "库名称", "表状态");
        setAttributeRow(sheet, firstAttributes, 1, cellStyle2);
        int startIndex = 2;
        for (Table table : tablePageResult.getLists()) {
            Row row = sheet.createRow(startIndex++);
            // 添加数据
            row.createCell(0).setCellValue(table.getTableName());
            row.createCell(1).setCellValue(table.getDatabaseName());
            row.createCell(2).setCellValue(table.getStatus());
        }
        //合并单元格
        CellRangeAddress columnRegion = new CellRangeAddress(startIndex, startIndex, 0, 2);
        sheet.addMergedRegion(columnRegion);
        Row columnRow = sheet.createRow(startIndex++);
        Cell columnCell = columnRow.createCell(0);
        //标题
        columnCell.setCellStyle(cellStyle);
        columnCell.setCellValue("列描述空值检查");
        //表头
        setAttributeRow(sheet, firstAttributes, startIndex++, cellStyle2);
        //列描述空值详情
        ArrayList<String> attributes = Lists.newArrayList("列名称", "表名称", "库名称", "列状态", "列类型");
        for (Table table : columnPageResult.getLists()) {
            Row row = sheet.createRow(startIndex++);
            // 添加数据
            row.createCell(0).setCellValue(table.getTableName());
            row.createCell(1).setCellValue(table.getDatabaseName());
            row.createCell(2).setCellValue(table.getStatus());
            setColumnSheet(workbook, table, attributes, cellStyle2);
        }
        return workbook2file(workbook, "business");
    }

    /**
     * 列名描述sheet
     *
     * @param workbook
     * @param table
     * @param attributes
     * @param cellStyle
     */
    public void setColumnSheet(Workbook workbook, Table table, List<String> attributes, CellStyle cellStyle) {
        String tableName = table.getTableName();
        Sheet columnSheet = workbook.getSheet(tableName);
        if (columnSheet == null) {
            columnSheet = workbook.createSheet(CustomStringUtils.handleExcelName(tableName));
        }
        setAttributeRow(columnSheet, attributes, 0, cellStyle);
        int columnIndex = 1;
        for (Column column : table.getColumns()) {
            Row columnRow = columnSheet.createRow(columnIndex++);
            columnRow.createCell(0).setCellValue(column.getColumnName());
            columnRow.createCell(1).setCellValue(table.getTableName());
            columnRow.createCell(2).setCellValue(table.getDatabaseName());
            columnRow.createCell(3).setCellValue(table.getStatus());
            columnRow.createCell(4).setCellValue(column.getType());
        }
    }

    /**
     * 插入表头
     *
     * @param sheet
     * @param attributes
     * @param index
     * @param cellStyle
     */
    public void setAttributeRow(Sheet sheet, List<String> attributes, int index, CellStyle cellStyle) {
        Row columnFirstRow = sheet.createRow(index);
        for (int i = 0; i < attributes.size(); i++) {
            Cell cell = columnFirstRow.createCell(i);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(attributes.get(i).trim());
        }
    }

    /**
     * 将审批对象送审
     * @param tenantId 租户id
     * @param info 要被送审的信息对象
     * @param approveType 审批类型（发布或下线）
     */
    private void approveItems(String tenantId, BusinessInfo info, String approveType){
        String approveGroupId = info.getApproveGroupId();
        if (StringUtils.isEmpty(approveGroupId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "审批组不能为空");
        }
        ApproveItem approveItem = buildApproveItem(info, approveGroupId, approveType, tenantId);
        businessDao.updateApproveIdAndApproveGroupId(info.getBusinessId(), approveItem.getId());
        approveServiceImp.addApproveItem(approveItem);
    }

    /**
     * 构建审批对象
     * @param info 信息对象
     * @param approveGroupId 审核组id
     * @param approveType 审批类型（发布或下线）
     * @param tenantId 租户id
     * @return 审批对象
     */
    private ApproveItem buildApproveItem(Object info, String approveGroupId, String approveType, String tenantId){

        ApproveItem approveItem = new ApproveItem();

        String approveId = UUID.randomUUID().toString();
        approveItem.setId(approveId);

        String id = "";
        String name = "";
        if (info instanceof BusinessInfo){
            id = ((BusinessInfo) info).getBusinessId();
            name = ((BusinessInfo) info).getName();

            approveItem.setBusinessType(BusinessType.BUSINESS_OBJECT.getTypeCode());
            approveItem.setBusinessTypeText(BusinessType.BUSINESS_OBJECT.getTypeText());
            approveItem.setModuleId(ModuleEnum.BUSINESS.getId() + "");
        }

        Integer maxVersion = businessDao.getMaxVersionById(id);

        approveItem.setObjectId(id);
        approveItem.setObjectName(name);
        approveItem.setApproveType(approveType);
        approveItem.setApproveGroup(approveGroupId);

        approveItem.setSubmitter(AdminUtils.getUserData().getUserId());
        approveItem.setCommitTime(Timestamp.valueOf(LocalDateTime.now()));

        approveItem.setVersion((maxVersion == null ? 0 : maxVersion) + 1);
        approveItem.setTenantId(tenantId);

        return approveItem;
    }

    /**
     * 获取被审批的对象详情接口实现
     * @param objectId  对象ID
     * @param type 业务对象类型
     * @param version 查看版本
     * @param tenantId 租户id
     * @return 被审批对象
     */
    @Override
    public Object getObjectDetail(String objectId, String type, int version, String tenantId) {
        BusinessInfoBO businessInfo = businessDao.getBusinessApproveDetails(objectId, tenantId);
        businessInfo.setTheme(CategoryRelationUtils.getPath(businessInfo.getDepartmentId(), tenantId));
        return businessInfo;
    }

    /**
     *  修改被审批对象的审批状态的接口实现
     * @param approveResult 审核结果
     * @param tenantId 租户id
     * @param items 被审核对象
     * @throws Exception
     */
    @Override
    public void changeObjectStatus(String approveResult, String tenantId, List<ApproveItem> items) {
        List<String> idList = items.stream().map(ApproveItem::getObjectId).collect(Collectors.toList());

        // 发布开关状态
        List<BusinessInfo> publishStatus = businessDao.getBusinessPublicStatus(idList, tenantId);

        for (BusinessInfo businessInfo : publishStatus) {
            // 审批通过
            if (ApproveOperate.APPROVE.getCode().equals(approveResult)) {
                // 更新审批状态
                businessInfo.setStatus(Status.ACTIVE.getIntValue() + "");

                // 修改私密状态
                if (businessInfo.getPublish()) {
                    businessInfo.setPrivateStatus("PUBLIC");
                }
                else {
                    businessInfo.setPrivateStatus("PRIVATE");
                }
            }
            else {
                // 审批不通过
                // 更新审批状态
                businessInfo.setStatus(Status.REJECT.getIntValue() + "");

                // 还原开关状态：开关为开，还原为关；开关为关，还原为开
                if (businessInfo.getPublish()) {
                    businessInfo.setPublish(false);
                }
                else {
                    businessInfo.setPublish(true);
                }
            }
        }

        // 更新
        businessDao.updateBusinessPublicStatus(publishStatus);
    }

    /**
     * 查询目录中未关联当前业务对象的表
     *
     * @param categoryGuids
     * @param businessId
     * @param tenantId
     */
    public PageResult<RelationEntityV2> getCategoryRelationFilter(List<String> categoryGuids, String businessId, RelationQuery query, String tenantId) {
        try {
            if (CollectionUtils.isEmpty(categoryGuids)) {
                List<CategoryPrivilege> allCategories = dataManageService.getTechnicalCategory(tenantId);
                if (CollectionUtils.isNotEmpty(allCategories)) {
                    categoryGuids = allCategories.stream().map(c -> c.getGuid()).collect(Collectors.toList());
                }
            }

            int limit = query.getLimit();
            int offset = query.getOffset();
            PageResult<RelationEntityV2> pageResult = new PageResult<>();
            int totalNum = 0;
            String tableName = query.getFilterTableName();
            if (org.apache.commons.lang.StringUtils.isNotBlank(tableName)) {
                tableName = tableName.replaceAll("%", "\\\\%").replaceAll("_", "\\\\_");
            }

            List<RelationEntityV2> relations = new ArrayList<>();

            User user = AdminUtils.getUserData();
            List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
            if (CollectionUtils.isNotEmpty(userGroups) && CollectionUtils.isNotEmpty(categoryGuids)) {
                List<String> userGroupIds = userGroups.stream().map(UserGroup::getId).collect(Collectors.toList());

                relations = businessDao.queryRelationByCategoryGuidAndBusinessIdFilterV2(categoryGuids, businessId, tenantId, limit, offset, tableName, userGroupIds);
                if (!org.springframework.util.CollectionUtils.isEmpty(relations)) {
                    totalNum = relations.get(0).getTotal();
                }
                getPath(relations, tenantId);
            }
            pageResult.setCurrentSize(relations.size());
            pageResult.setLists(relations);
            pageResult.setTotalSize(totalNum);
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取关联失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取关联失败");
        }
    }

    public void getPath(List<RelationEntityV2> list, String tenantId) throws AtlasBaseException {
        for (RelationEntityV2 entity : list) {
            String path = CategoryRelationUtils.getPath(entity.getCategoryGuid(), tenantId);
            entity.setPath(path);
        }
    }

    public List<CategorycateQueryResult> getBusinessPlaceCategories(Integer type, String tenantId) {
        List<CategorycateQueryResult> result = new ArrayList<>();
        List<CategorycateQueryResult> allCategories = businessCatalogueService.getAllCategories(1, tenantId);
        if (CollectionUtils.isNotEmpty(allCategories)) {
            // 取出有编辑权限的目录
            result = allCategories.stream().filter(c -> c.getEditItem() != null && c.getEditItem()).collect(Collectors.toList());
        }

        return result;
    }
}
