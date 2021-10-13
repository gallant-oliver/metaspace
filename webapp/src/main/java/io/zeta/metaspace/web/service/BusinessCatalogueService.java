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
 * @date 2018/11/19 20:10
 */
package io.zeta.metaspace.web.service;

/*
 * @description
 * @author fanjiajia
 * @date 2021/9/27
 */

import com.google.common.collect.Lists;
import io.zeta.metaspace.model.approve.ApproveItem;
import io.zeta.metaspace.model.approve.ApproveOperate;
import io.zeta.metaspace.model.approve.ApproveType;
import io.zeta.metaspace.model.business.BussinessCatalogueInput;
import io.zeta.metaspace.model.enums.BusinessType;
import io.zeta.metaspace.model.enums.CategoryPrivateStatus;
import io.zeta.metaspace.model.enums.Status;
import io.zeta.metaspace.model.metadata.CategoryExport;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.result.*;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.service.Approve.Approvable;
import io.zeta.metaspace.web.service.Approve.ApproveService;
import io.zeta.metaspace.web.util.*;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryDeleteReturn;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.commons.lang.StringUtils;
import org.apache.directory.api.util.Strings;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Service
public class BusinessCatalogueService implements Approvable {
    private static final Logger LOG = LoggerFactory.getLogger(BusinessCatalogueService.class);
    @Autowired
    UserDAO userDAO;
    @Autowired
    TenantService tenantService;
    @Autowired
    RoleDAO roleDao;
    @Autowired
    CategoryDAO categoryDao;
    @Autowired
    UserGroupDAO userGroupDAO;
    @Autowired
    UserGroupService userGroupService;
    @Autowired
    ApproveService approveServiceImp;

    @Autowired
    private CategoryDAO categoryDAO;

    @Autowired
    RelationDAO relationDao;

    @Autowired
    BusinessDAO businessDAO;

    int dataStandType = 3;

    /**
     * 创建业务目录
     *
     * @param input
     * @param tenantId
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public CategoryPrivilege createCategory(BussinessCatalogueInput input, String tenantId) throws Exception {
        try {
            String currentCategoryGuid = input.getGuid();
            boolean publish = input.getPublish();
            Integer type=input.getCategoryType();
            LOG.info("publish="+publish);
            String approveGroupId=input.getApproveGroupId();
            CategoryEntityV2 entity = new CategoryEntityV2();
            String newCategoryGuid = UUID.randomUUID().toString();
            String name = input.getName();
            String creatorId = AdminUtils.getUserData().getUserId();
            if (Objects.isNull(name)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录名不能为空");
            }
            //判断独立部署和多租户
            List<Module> moduleByUserId = TenantService.defaultTenant.equals(tenantId) ? userDAO.getModuleByUserId(creatorId) : tenantService.getModule(tenantId);
            List<Integer> modules = new ArrayList<>();
            for (Module module : moduleByUserId) {
                modules.add(module.getModuleId());
            }
            //guid
            entity.setGuid(newCategoryGuid);
            //name
            entity.setName(name);
            entity.setPrivateStatus(CategoryPrivateStatus.PRIVATE);
            //创建人
            entity.setCreator(creatorId);
            //createtime
            entity.setCreateTime(io.zeta.metaspace.utils.DateUtils.currentTimestamp());
            //description
            entity.setDescription(input.getDescription());
            entity.setCategoryType(type);
            entity.setSafe("1");
            entity.setPublish(false);
            entity.setInformation(input.getInformation());

            String approveId = UUID.randomUUID().toString();
            if (publish) {
                entity.setPublish(true);
                entity.setApprovalId(approveId);
            }else{
                entity.setApprovalId(null);
            }

            String parentCategoryGuid = categoryDAO.getParentIdByGuid(currentCategoryGuid,tenantId);
            int currentCategorySort = 0;
            if (currentCategoryGuid != null) {
                currentCategorySort = categoryDAO.getCategorySortById(currentCategoryGuid, tenantId);
            }
            if ("up".equals(input.getDirection())) {
                categoryDao.updateSort(currentCategorySort, parentCategoryGuid, tenantId);
                entity.setSort(currentCategorySort);
            } else if ("down".equals(input.getDirection())) {
                categoryDao.updateSort(currentCategorySort + 1, parentCategoryGuid, tenantId);
                entity.setSort(currentCategorySort + 1);
            } else {
                int maxSort = categoryDao.getMaxSortByParentGuid(input.getParentCategoryGuid(), tenantId);
                entity.setSort(maxSort);
            }

            //创建一级目录
            if (StringUtils.isEmpty(currentCategoryGuid)) {
                if (TenantService.defaultTenant.equals(tenantId)) {
                    List<Role> roles = roleDao.getRoleByUsersId(creatorId);
                    if (!roles.stream().anyMatch(role -> SystemRole.ADMIN.getCode().equals(role.getRoleId()))) {
                        throw new AtlasBaseException(AtlasErrorCode.PERMISSION_DENIED, "当前用户没有创建目录权限");
                    }
                } else {
                    boolean bool = type == 1;
                    if (!modules.contains(ModuleEnum.AUTHORIZATION.getId()) && bool) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有目录授权模块权限，无法创建一级目录");
                    }
                }

                CategoryPrivilege oneLevelCategory = null;
                if (categoryDao.ifExistCategory(type, tenantId) > 0) {
                    oneLevelCategory = createOneLevelCategory(entity, type, tenantId);
                } else {
                    oneLevelCategory = createFirstCategory(entity, type, tenantId);
                }
                if (!Objects.isNull(oneLevelCategory)) {
                    oneLevelCategory.setCode(entity.getCode());
                }

                //目录是否需要发布，如果需要发布，则需要选择审批组,记录审批信息
                if (publish) {
                    LOG.info("发起审批:" + approveGroupId);
                    this.approveItems(approveId,tenantId, entity, ApproveType.PUBLISH.getCode(), BusinessType.BUSINESSCATALOGUE_PUBLISH, approveGroupId);
                }
                return oneLevelCategory;
            }else {
                if (Objects.isNull(categoryDao.queryByGuidV2(currentCategoryGuid, tenantId))) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前目录已被删除，请刷新后重新操作");
                }
                CategoryPrivilege.Privilege privilege = createOtherCategory(entity, type, input, tenantId);
                categoryDao.add(entity, tenantId);
                CategoryPrivilege returnEntity = categoryDao.queryByGuidV2(newCategoryGuid, tenantId);

                boolean isAdmin = modules.contains(ModuleEnum.AUTHORIZATION.getId());
                //无当前目录权限
                boolean isPrivilege = !userGroupService.isPrivilegeCategory(creatorId, newCategoryGuid, tenantId, type);
                boolean typeBoolean = type == 1;
                if (isAdmin) {
                    privilege.adminPrivilege(returnEntity.getGuid());
                }
                if (typeBoolean && isAdmin && isPrivilege) {
                    privilege.setAsh(true);
                }

                returnEntity.setPrivilege(privilege);

                //目录是否需要发布，如果需要发布，则需要选择审批组,记录审批信息
                if (publish) {
                    LOG.info("发起审批:" + approveGroupId);
                    this.approveItems(approveId,tenantId, entity, ApproveType.PUBLISH.getCode(), BusinessType.BUSINESSCATALOGUE_PUBLISH, approveGroupId);
                }

                return returnEntity;
            }
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("创建业务目录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "创建业务目录失败");
        }
    }


    private CategoryPrivilege createOneLevelCategory(CategoryEntityV2 entity, int type, String tenantId) throws AtlasBaseException {
        int count = categoryDao.querySameNameOne(entity.getName(), type, tenantId);
        if (count > 0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在相同的目录名");
        String lastCategoryId = categoryDao.queryLastCategory(type, tenantId);
        StringBuffer qualifiedName = new StringBuffer();
        qualifiedName.append(entity.getName());
        entity.setQualifiedName(qualifiedName.toString());
        entity.setLevel(1);
        entity.setUpBrotherCategoryGuid(lastCategoryId);
        categoryDao.add(entity, tenantId);
        categoryDao.updateDownBrotherCategoryGuid(lastCategoryId, entity.getGuid(), tenantId);
        CategoryPrivilege returnEntity = new CategoryPrivilege();
        returnEntity.setGuid(entity.getGuid());
        returnEntity.setName(entity.getName());
        returnEntity.setDescription(entity.getDescription());
        returnEntity.setLevel(1);
        returnEntity.setParentCategoryGuid(null);
        CategoryPrivilege.Privilege privilege = null;
        if (type == 1) {
            privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true, false);
        }
        returnEntity.setPrivilege(privilege);
        return returnEntity;
    }

    private CategoryPrivilege createFirstCategory(CategoryEntityV2 entity, int type, String tenantId) throws AtlasBaseException {
        StringBuffer qualifiedName = new StringBuffer();
        //qualifiedName
        qualifiedName.append(entity.getName());
        entity.setQualifiedName(qualifiedName.toString());
        entity.setLevel(1);
        categoryDao.add(entity, tenantId);
        CategoryPrivilege returnEntity = new CategoryPrivilege();
        returnEntity.setGuid(entity.getGuid());
        returnEntity.setName(entity.getName());
        returnEntity.setDescription(entity.getDescription());
        returnEntity.setLevel(1);
        returnEntity.setParentCategoryGuid(null);
        returnEntity.setUpBrotherCategoryGuid(null);
        returnEntity.setDownBrotherCategoryGuid(null);
        CategoryPrivilege.Privilege privilege = null;
        if (type == 1) {
            privilege = new CategoryPrivilege.Privilege(false, true, true, true, true, true, true, true, true, false);
        } else {
            privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true, false);
        }
        returnEntity.setPrivilege(privilege);
        return returnEntity;
    }

    private CategoryPrivilege.Privilege createOtherCategory(CategoryEntityV2 entity, int type, BussinessCatalogueInput info, String tenantId) throws SQLException, AtlasBaseException {
        StringBuffer qualifiedName = new StringBuffer();
        String newCategoryGuid = entity.getGuid();
        String newCategoryParentGuid = info.getParentCategoryGuid();
        //获取当前catalogue
        CategoryEntityV2 currentEntity = categoryDao.queryByGuid(info.getGuid(), tenantId);
        String parentQualifiedName = null;
        String parentGuid = null;
        int currentLevel = categoryDao.getCategoryLevel(info.getGuid(), tenantId);
        //创建子目录
        if (StringUtils.isNotEmpty(newCategoryParentGuid)) {
            parentGuid = info.getGuid();
            entity.setParentCategoryGuid(info.getGuid());
            parentQualifiedName = currentEntity.getQualifiedName();
            entity.setLevel(currentLevel + 1);
        } else {
            //创建同级目录
            parentGuid = currentEntity.getParentCategoryGuid();
            entity.setLevel(currentLevel);
            if (StringUtils.isNotEmpty(parentGuid)) {
                entity.setParentCategoryGuid(parentGuid);
                CategoryEntityV2 currentCatalogParentEntity = categoryDao.queryByGuid(parentGuid, tenantId);
                parentQualifiedName = currentCatalogParentEntity.getQualifiedName();
            }
        }
        if (StringUtils.isNotEmpty(parentQualifiedName) && parentQualifiedName.length() > 0)
            qualifiedName.append(parentQualifiedName + ".");
        qualifiedName.append(entity.getName());
        int count = categoryDao.querySameNameNum(entity.getName(), parentGuid, type, tenantId);
        if (count > 0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在相同的目录名");
        //qualifiedName
        entity.setQualifiedName(qualifiedName.toString());

        //子目录
        if (StringUtils.isNotEmpty(newCategoryParentGuid)) {
            String lastChildGuid = categoryDao.queryLastChildCategory(info.getGuid(), tenantId);
            if (StringUtils.isNotEmpty(lastChildGuid)) {
                entity.setUpBrotherCategoryGuid(lastChildGuid);
                categoryDao.updateDownBrotherCategoryGuid(lastChildGuid, newCategoryGuid, tenantId);
            }
        } else {
            //同级目录
            String up = "up";
            String down = "down";
            if (StringUtils.isNotEmpty(info.getGuid()) && Strings.equals(info.getDirection(), up)) {
                entity.setDownBrotherCategoryGuid(info.getGuid());
                String upBrotherGuid = currentEntity.getUpBrotherCategoryGuid();
                if (StringUtils.isNotEmpty(upBrotherGuid)) {
                    entity.setUpBrotherCategoryGuid(upBrotherGuid);
                    categoryDao.updateDownBrotherCategoryGuid(upBrotherGuid, newCategoryGuid, tenantId);
                }
                categoryDao.updateUpBrotherCategoryGuid(info.getGuid(), newCategoryGuid, tenantId);
            } else if (StringUtils.isNotEmpty(info.getGuid()) && Strings.equals(info.getDirection(), down)) {
                entity.setUpBrotherCategoryGuid(info.getGuid());
                String downBrotherGuid = currentEntity.getDownBrotherCategoryGuid();
                if (StringUtils.isNotEmpty(downBrotherGuid)) {
                    entity.setDownBrotherCategoryGuid(downBrotherGuid);
                    categoryDao.updateUpBrotherCategoryGuid(downBrotherGuid, newCategoryGuid, tenantId);
                }
                categoryDao.updateDownBrotherCategoryGuid(info.getGuid(), newCategoryGuid, tenantId);
            }
        }

        if (parentGuid == null) {
            return new CategoryPrivilege.Privilege(false, true, true, true, false, true, false, false, true, false);
        }
        List<GroupPrivilege> parentPrivilege = userGroupDAO.getCategoryGroupPrivileges(entity.getParentCategoryGuid(), tenantId);
        parentPrivilege.forEach(privilege -> privilege.setCategoryId(entity.getGuid()));
        if (parentPrivilege.size() != 0) {
            userGroupDAO.addUserGroupPrivileges(parentPrivilege);
        }
        List<CategoryPrivilege> userCategories = userGroupService.getUserCategories(type, tenantId);
        for (CategoryPrivilege categoryPrivilege : userCategories) {
            if (categoryPrivilege.getGuid().equals(parentGuid)) {
                return categoryPrivilege.getPrivilege();
            }
        }
        return null;
    }

    /**
     * 将审批对象送审
     * @param approveId 审批记录id
     * @param tenantId 租户id
     * @param entity 业务目录对象
     * @param approveType 审批类型
     * @param approveGroupId 审核组id
     * @param businessType 业务类型
     */
    private void approveItems(String approveId,String tenantId, CategoryEntityV2 entity,String approveType,BusinessType businessType,String approveGroupId){
        ApproveItem approveItem = new ApproveItem();
        approveItem.setId(approveId);
        approveItem.setObjectId(entity.getGuid());
        approveItem.setObjectName(entity.getName());
        approveItem.setApproveType(approveType);
        approveItem.setApproveGroup(approveGroupId);
        approveItem.setBusinessType(businessType.getTypeCode());
        approveItem.setBusinessTypeText(businessType.getTypeText());
        approveItem.setSubmitter(AdminUtils.getUserData().getUserId());
        approveItem.setCommitTime(Timestamp.valueOf(LocalDateTime.now()));
        approveItem.setModuleId(String.valueOf(ModuleEnum.BUSINESSCATALOGUE.getId()));
        approveItem.setVersion(1);
        approveItem.setTenantId(tenantId);
        approveServiceImp.addApproveItem(approveItem);

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
        try {
            CategoryEntityV2 currentCatalog = categoryDao.queryByGuid(objectId, tenantId);
            return currentCatalog;
        }catch (Exception e){
            LOG.error("查询目录信息异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询目录信息异常："+e.getMessage());
        }
    }

    @Override
    public void changeObjectStatus(String approveResult, String tenantId, List<ApproveItem> items) throws Exception {
        String updater = AdminUtils.getUserData().getUserId();
        if (!CollectionUtils.isEmpty(items)) {
            for (ApproveItem item : items) {
                String objectId = item.getObjectId();
                String approveType = item.getApproveType();

                if (approveType.equals(ApproveType.PUBLISH.getCode())) {
                    //发布
                    if (approveResult.equals(ApproveOperate.APPROVE.getCode())) {
                        //通过
                        categoryDao.updateCataloguePrivateStatus(objectId,CategoryPrivateStatus.PUBLIC.name(),true,tenantId,updater);
                    } else if (approveResult.equals(ApproveOperate.REJECTED.getCode())) {
                        //驳回
                        categoryDao.updateCataloguePrivateStatus(objectId,CategoryPrivateStatus.PRIVATE.name(),false,tenantId,updater);
                        LOG.info("目录审批被驳回");
                    }else if(ApproveOperate.CANCEL.getCode().equals(approveResult)){
                        //撤回
                        categoryDao.updateCataloguePrivateStatus(objectId,CategoryPrivateStatus.PRIVATE.name(),false,tenantId,updater);
                        LOG.info("目录审批被撤回");
                    }
                } else if (approveType.equals(ApproveType.CLOSE_PUBLISH.getCode())) {
                    //发布
                    if (approveResult.equals(ApproveOperate.APPROVE.getCode())) {
                        //通过
                        categoryDao.updateCataloguePrivateStatus(objectId,CategoryPrivateStatus.PRIVATE.name(),false,tenantId,updater);
                    } else if (approveResult.equals(ApproveOperate.REJECTED.getCode())) {
                        //驳回
                        categoryDao.updateCataloguePrivateStatus(objectId,CategoryPrivateStatus.PUBLIC.name(),true,tenantId,updater);
                        LOG.info("目录审批被驳回");
                    }else if(ApproveOperate.CANCEL.getCode().equals(approveResult)){
                        //撤回
                        LOG.info("目录审批被撤回");
                    }
                }
            }
        }
    }


    /**
     * 编辑目录
     *
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    public String updateCategory(BussinessCatalogueInput info, int type, String tenantId) throws AtlasBaseException {
        try {
            String guid = info.getGuid();
            String name = info.getName();
            String approveGroupId = info.getApproveGroupId();
            if (Objects.isNull(name)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录名不能为空");
            }
            CategoryEntityV2 currentEntity = categoryDao.queryByGuid(guid, tenantId);
            Boolean oldisPublish=currentEntity.getPublish();
            Boolean newisPublish=info.getPublish();
            String approveId = UUID.randomUUID().toString();
            CategoryEntityV2 entity = new CategoryEntityV2();
            if(oldisPublish!=newisPublish){
                if (StringUtils.isBlank(approveGroupId)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "审批组不能为空");
                }
                BusinessType businessType=null;
                String approveType="";
                if(oldisPublish && !newisPublish){
                    businessType= BusinessType.BUSINESSCATALOGUE_PUBLISH_CLOSE;
                    approveType=ApproveType.CLOSE_PUBLISH.getCode();
                    entity.setPublish(false);
                }
                if(!oldisPublish && newisPublish){
                    businessType=BusinessType.BUSINESSCATALOGUE_PUBLISH;
                    approveType=ApproveType.PUBLISH.getCode();
                    entity.setPublish(true);
                }
                currentEntity.setName(name);
                //如果目录隐私状态如果申请改变，则需要发审批请求
                this.approveItems(approveId,tenantId, currentEntity, approveType,businessType,approveGroupId);
            }else {
                approveId=null;
            }

            String parentQualifiedName = null;
            StringBuffer qualifiedName = new StringBuffer();
            if (StringUtils.isNotEmpty(currentEntity.getParentCategoryGuid()))
                parentQualifiedName = categoryDao.queryQualifiedName(currentEntity.getParentCategoryGuid(), tenantId);
            if (StringUtils.isNotEmpty(parentQualifiedName))
                qualifiedName.append(parentQualifiedName + ".");
            qualifiedName.append(name);
            int count = categoryDao.querySameNameNum(name, currentEntity.getParentCategoryGuid(), type, tenantId);
            if (count > 0 && !currentEntity.getName().equals(info.getName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在相同的目录名");
            }
            entity.setGuid(info.getGuid());
            entity.setName(info.getName());
            entity.setQualifiedName(qualifiedName.toString());
            entity.setDescription(info.getDescription());
            entity.setInformation(info.getInformation());
            entity.setApprovalId(approveId);
            User user = AdminUtils.getUserData();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            categoryDao.updateCategoryV2Info(entity, tenantId, user.getUserId(), timestamp);
            return "success";
        } catch (SQLException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (Exception e) {
            LOG.error("更新目录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }


    public String uploadAllCategory(File fileInputStream, int type, String tenantId) throws Exception {
        Set<CategoryEntityV2> all = categoryDao.getAll(type, tenantId);
        if (all.size() != 0 && type != 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在目录，无法全局导入");
        }
        List<CategoryEntityV2> categories;
        try {
            categories = file2AllData(fileInputStream, type, new ArrayList<>());
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("数据转换失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "文件异常：" + e.getMessage());
        }
        return ExportDataPathUtils.transferTo(fileInputStream);
    }

    /**
     * 文件转化为目录
     *
     * @param file
     * @return
     * @throws Exception
     */
    private List<CategoryEntityV2> file2AllData(File file, int type, List<CategoryEntityV2> systemCategory) throws Exception {
        List<CategoryEntityV2> categories = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(file);
        Sheet sheet = workbook.getSheetAt(0);
        int rowNum = sheet.getLastRowNum() + 1;

        List<String> systemCategoryGuids;
        if (type == dataStandType) {
            systemCategoryGuids = new ArrayList<>(CategoryUtil.initDataStandardCategoryId);
        } else {
            systemCategoryGuids = new ArrayList<>();
        }
        Timestamp createTime = io.zeta.metaspace.utils.DateUtils.currentTimestamp();

        //文件格式校验
        Row first = sheet.getRow(0);
        ArrayList<String> strings = Lists.newArrayList("目录id", "目录名字", "目录描述", "同级的上方目录id", "同级的下方目录id", "父目录id", "全名称", "级别", "排序");
        for (int i = 0; i < strings.size(); i++) {
            Cell cell = first.getCell(i);
            if (Objects.isNull(cell)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件内部格式错误，请导入正确的文件");
            } else {
                if (!strings.get(i).equals(cell.getStringCellValue())) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件内部格式错误，请导入正确的文件");
                }
            }
        }

        for (int i = 1; i < rowNum; i++) {
            Row row = sheet.getRow(i);
            CategoryEntityV2 category = new CategoryEntityV2();

            Cell guidCell = row.getCell(0);
            if (Objects.isNull(guidCell)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "guid不能为空");
            }
            category.setGuid(guidCell.getStringCellValue());

            Cell nameCell = row.getCell(1);
            if (Objects.isNull(nameCell)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录名称不能为空");
            }
            category.setName(nameCell.getStringCellValue());

            Cell discriptionCell = row.getCell(2);
            if (Objects.isNull(discriptionCell)) {
                category.setDescription("");
            } else {
                category.setDescription(discriptionCell.getStringCellValue());
            }

            Cell upCell = row.getCell(3);
            if (Objects.isNull(upCell) || upCell.getStringCellValue().length() == 0) {
                category.setUpBrotherCategoryGuid(null);
            } else {
                category.setUpBrotherCategoryGuid(upCell.getStringCellValue());
            }

            Cell downCell = row.getCell(4);
            if (Objects.isNull(downCell) || downCell.getStringCellValue().length() == 0) {
                category.setDownBrotherCategoryGuid(null);
            } else {
                category.setDownBrotherCategoryGuid(downCell.getStringCellValue());
            }

            Cell parentCell = row.getCell(5);
            if (Objects.isNull(parentCell) || parentCell.getStringCellValue().length() == 0) {
                category.setParentCategoryGuid(null);
            } else {
                category.setParentCategoryGuid(parentCell.getStringCellValue());
            }

            Cell qualifiedNameCell = row.getCell(6);
            if (Objects.isNull(qualifiedNameCell)) {
                category.setQualifiedName(null);
            } else {
                category.setQualifiedName(qualifiedNameCell.getStringCellValue());
            }

            Cell levelNameCell = row.getCell(7);
            if (Objects.isNull(levelNameCell)) {
                category.setLevel(0);
            } else {
                category.setLevel(Integer.parseInt(levelNameCell.getStringCellValue()));
            }
            Cell sortCell = row.getCell(8);
            if (Objects.isNull(sortCell)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "sort不能为空");
            }
            category.setSort(Integer.parseInt(sortCell.getStringCellValue()));
            category.setPublish(false);
            category.setPrivateStatus(CategoryPrivateStatus.PRIVATE);

            category.setCategoryType(type);
            category.setCreateTime(createTime);
            User user = AdminUtils.getUserData();
            category.setCreator(user.getUserId());
            if (systemCategoryGuids.contains(category.getGuid())) {
                systemCategoryGuids.remove(category.getGuid());
                systemCategory.add(category);
            } else {
                categories.add(category);
            }
        }
        if (systemCategoryGuids.size() != 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件内容不合规范，不包含初始目录，请使用全局导出的文件");
        }
        //全局导入时，目录id新赋值
        for(CategoryEntityV2 cate: categories){
            String guid=cate.getGuid();
            String newGuid= UUID.randomUUID().toString();
            for(CategoryEntityV2 cate2: categories){
                if(guid.equals(cate2.getGuid())){
                    cate2.setGuid(newGuid);
                }
                if(guid.equals(cate2.getParentCategoryGuid())){
                    cate2.setParentCategoryGuid(newGuid);
                }
                if(guid.equals(cate2.getUpBrotherCategoryGuid())){
                    cate2.setUpBrotherCategoryGuid(newGuid);
                }
                if(guid.equals(cate2.getDownBrotherCategoryGuid())){
                    cate2.setDownBrotherCategoryGuid(newGuid);
                }
            }
        }
        return categories;
    }

    public String uploadCategory(String categoryId, String direction, File fileInputStream, int type, String tenantId) throws Exception {
        List<CategoryExport> categoryExports;
        try {
            categoryExports = file2Data(fileInputStream);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("数据转换失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件异常：" + e.getMessage());
        }
        checkSameName(categoryId, direction, categoryExports, type, tenantId);
        if (categoryExports.isEmpty()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "上传数据为空或全部重名");
        }
        return ExportDataPathUtils.transferTo(fileInputStream);
    }

    /**
     * 文件转化为目录
     *
     * @param file
     * @return
     * @throws Exception
     */
    private List<CategoryExport> file2Data(File file) throws Exception {
        List<String> names = new ArrayList<>();
        List<CategoryExport> categoryExports = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(file);
        Sheet sheet = workbook.getSheetAt(0);

        //文件格式校验
        Row first = sheet.getRow(0);
        ArrayList<String> strings = Lists.newArrayList("目录名字", "目录描述");
        ArrayList<String> allStrings = Lists.newArrayList("目录id", "目录名字", "目录描述", "同级的上方目录id", "同级的下方目录id", "父目录id", "全名称", "级别", "排序");

        for (int i = 0; i < strings.size(); i++) {
            Cell cell = first.getCell(i);
            if (Objects.isNull(cell)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件内部格式错误，请导入正确的文件");
            } else {
                if (!strings.get(i).equals(cell.getStringCellValue())) {
                    if (allStrings.get(i).equals(cell.getStringCellValue())) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "全局导出文件只能用于全局导入，请导入正确的文件");
                    }
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件内部格式错误，请导入正确的文件");
                }
            }
        }

        int rowNum = sheet.getLastRowNum() + 1;
        for (int i = 1; i < rowNum; i++) {
            Row row = sheet.getRow(i);
            CategoryExport category = new CategoryExport();
            Cell nameCell = row.getCell(0);
            if (Objects.isNull(nameCell)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录名称不能为空");
            }
            if (names.contains(nameCell.getStringCellValue())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件中存在相同目录名");
            }
            category.setName(nameCell.getStringCellValue());

            Cell discriptionCell = row.getCell(1);
            if (Objects.isNull(discriptionCell)) {
                category.setDescription("");
            } else {
                category.setDescription(discriptionCell.getStringCellValue());
            }

            String guid = UUID.randomUUID().toString();
            category.setGuid(guid);
            categoryExports.add(category);
            names.add(nameCell.getStringCellValue());
        }
        return categoryExports;
    }

    /**
     * 同名校验
     *
     * @param categoryId
     * @param categoryExports
     */
    public void checkSameName(String categoryId, String direction, List<CategoryExport> categoryExports, int type, String tenantId) throws SQLException {
        List<String> childCategoryName;
        if (categoryId != null && categoryId.length() != 0) {
            String up = "up";
            String down = "down";
            if (up.equals(direction) || down.equals(direction)) {
                String parentCategoryGuid = categoryDao.queryByGuid(categoryId, tenantId).getParentCategoryGuid();
                childCategoryName = categoryDao.getChildCategoryName(parentCategoryGuid, tenantId);
            } else {
                childCategoryName = categoryDao.getChildCategoryName(categoryId, tenantId);
            }

        } else {
            childCategoryName = categoryDao.getChildCategoryNameByType(type, tenantId);
        }

        List<CategoryExport> categoryExportList = new ArrayList<>(categoryExports);
        for (CategoryExport categoryExport : categoryExportList) {
            if (!childCategoryName.contains(categoryExport.getName())) {
                continue;
            }
            categoryExports.remove(categoryExport);
        }

    }

    public String getCategoryNameById(String guid, String tenantId) {
        return categoryDao.getCategoryNameById(guid, tenantId);
    }

    /**
     * 导入目录
     *
     * @param fileInputStream
     * @param type
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void importAllCategory(File fileInputStream, Integer type, String tenantId) throws Exception {
        if (null==type) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录类型不能为空");
        }
        if (!fileInputStream.exists()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件丢失，请重新上传");
        }

        Set<CategoryEntityV2> all = categoryDao.getAll(type, tenantId);
        if (all != null) {
            int size = all.size();
            if (size != 0 && type != dataStandType) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在目录，无法全局导入");
            }
        }

        List<CategoryEntityV2> categories;
        List<CategoryEntityV2> systemCategory = new ArrayList<>();
        try {
            categories = file2AllData(fileInputStream, type, systemCategory);

        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("数据转换失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "文件异常：" + e.getMessage());
        }

        if (systemCategory.size() != 0) {
            categoryDao.updateCategoryEntityV2Tree(systemCategory, tenantId);
        }
        try {
            if (categories.size() != 0) {
                categoryDao.addAll(categories, tenantId);
            }
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "禁止不同类型的目录互相导入，请选择正确的文件导入");
        }

        fileInputStream.delete();
    }


    /**
     * 导入目录
     *
     * @param categoryId
     * @param fileInputStream
     * @param type
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public List<CategoryPrivilege> importCategory(String categoryId, String direction, File fileInputStream, boolean authorized, int type, String tenantId) throws Exception {
        if (!fileInputStream.exists()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件丢失，请重新上传");
        }
        List<CategoryExport> categoryExports;
        try {
            categoryExports = file2Data(fileInputStream);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("数据转换失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件异常：" + e.getMessage());
        }
        checkSameName(categoryId, direction, categoryExports, type, tenantId);
        if (categoryExports.isEmpty()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "上传数据为空或全部重名");
        }
        Map<String, CategoryEntityV2> newCategorys = new HashMap<>();

        //是否是导入一级目录
        String upGuid;
        String downGuid;
        String parentQualifiedName = null;
        String parentCategoryGuid = null;
        int level;
        String down = "down";
        String up = "up";
        if (categoryId != null && categoryId.length() != 0) {
            if (up.equals(direction)) {
                CategoryEntityV2 currentEntity = categoryDao.queryByGuid(categoryId, tenantId);
                downGuid = categoryId;
                upGuid = currentEntity.getUpBrotherCategoryGuid();
                level = currentEntity.getLevel() - 1;
                if (currentEntity.getParentCategoryGuid() != null && currentEntity.getParentCategoryGuid().length() > 0) {
                    CategoryEntityV2 categoryEntityV2 = categoryDao.queryByGuid(currentEntity.getParentCategoryGuid(), tenantId);
                    parentQualifiedName = categoryEntityV2.getQualifiedName();
                    parentCategoryGuid = currentEntity.getParentCategoryGuid();
                }
            } else if (down.equals(direction)) {
                CategoryEntityV2 currentEntity = categoryDao.queryByGuid(categoryId, tenantId);
                upGuid = categoryId;
                downGuid = currentEntity.getDownBrotherCategoryGuid();
                level = currentEntity.getLevel() - 1;
                if (currentEntity.getParentCategoryGuid() != null && currentEntity.getParentCategoryGuid().length() > 0) {
                    CategoryEntityV2 categoryEntityV2 = categoryDao.queryByGuid(currentEntity.getParentCategoryGuid(), tenantId);
                    parentQualifiedName = categoryEntityV2.getQualifiedName();
                    parentCategoryGuid = currentEntity.getParentCategoryGuid();
                }
            } else {
                parentCategoryGuid = categoryId;
                upGuid = categoryDao.queryLastChildCategory(categoryId, tenantId);
                downGuid = null;
                CategoryEntityV2 currentEntity = categoryDao.queryByGuid(categoryId, tenantId);
                parentQualifiedName = currentEntity.getQualifiedName();
                level = currentEntity.getLevel();
            }
        } else {
            upGuid = categoryDao.queryLastCategory(type, tenantId);
            downGuid = null;
            parentQualifiedName = null;
            level = 0;
        }

        CategoryEntityV2 upChild = categoryDao.queryByGuid(upGuid, tenantId);
        if (upChild == null) {
            upChild = new CategoryEntityV2();
        }
        newCategorys.put(upGuid, upChild);
        Timestamp timestamp = io.zeta.metaspace.utils.DateUtils.currentTimestamp();
        User user = AdminUtils.getUserData();
        String upId = upGuid;
        for (CategoryExport categoryExport : categoryExports) {
            StringBuffer qualifiedName = new StringBuffer();
            String name = categoryExport.getName();
            CategoryEntityV2 categoryEntityV2 = new CategoryEntityV2();
            categoryEntityV2.setName(name);
            categoryEntityV2.setSafe("1");
            categoryEntityV2.setCategoryType(type);
            categoryEntityV2.setDescription(categoryExport.getDescription());
            categoryEntityV2.setGuid(categoryExport.getGuid());
            categoryEntityV2.setParentCategoryGuid(parentCategoryGuid);
            categoryEntityV2.setLevel(level + 1);
            categoryEntityV2.setCreateTime(timestamp);
            categoryEntityV2.setCreator(user.getUserId());

            if (StringUtils.isNotEmpty(parentQualifiedName) && parentQualifiedName.length() > 0)
                qualifiedName.append(parentQualifiedName + ".");
            qualifiedName.append(name);

            categoryEntityV2.setUpBrotherCategoryGuid(upId);
            newCategorys.get(upId).setDownBrotherCategoryGuid(categoryExport.getGuid());
            upId = categoryEntityV2.getGuid();

            newCategorys.put(categoryEntityV2.getGuid(), categoryEntityV2);
        }
        newCategorys.remove(upGuid);
        if (newCategorys.get(upId) != null) {
            newCategorys.get(upId).setDownBrotherCategoryGuid(downGuid);
        }

        if (upGuid != null) {
            categoryDao.updateDownBrotherCategoryGuid(upGuid, upChild.getDownBrotherCategoryGuid(), tenantId);
        }
        if (downGuid != null) {
            categoryDao.updateUpBrotherCategoryGuid(downGuid, upId, tenantId);
        }

        CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege();
        ArrayList<CategoryEntityV2> categoryEntityV2s = new ArrayList<>(newCategorys.values());
        List<GroupPrivilege> parentPrivilege = userGroupDAO.getCategoryGroupPrivileges(parentCategoryGuid, tenantId);
//        if (parentPrivilege.size() != 0 && categoryEntityV2s != null) {
//            userGroupDAO.addUserGroupCategoryPrivileges(parentPrivilege, categoryEntityV2s);
//        }

        AtomicInteger maxSort = new AtomicInteger(categoryDao.getMaxSortByParentGuid(parentCategoryGuid, tenantId));
        categoryEntityV2s.forEach(c -> {
            c.setPrivateStatus(CategoryPrivateStatus.PRIVATE);
            c.setPublish(false);
            c.setSort(maxSort.get());
            maxSort.getAndIncrement();
        });

        categoryDao.addAll(categoryEntityV2s, tenantId);
        if (level != 0 || !authorized) {
            fileInputStream.delete();
            GroupPrivilege groupPrivilege = new GroupPrivilege();
            groupPrivilege.setRead(true);
            groupPrivilege.setEditCategory(true);
            groupPrivilege.setEditItem(true);
            parentPrivilege.forEach(category -> {
                groupPrivilege.setRead(category.getRead() || groupPrivilege.getRead());
                groupPrivilege.setEditCategory(category.getEditCategory() || groupPrivilege.getEditCategory());
                groupPrivilege.setEditItem(category.getEditItem() || groupPrivilege.getEditItem());
            });
            privilege = getCategoryPrivilege(groupPrivilege.getRead(), groupPrivilege.getEditItem(), groupPrivilege.getEditCategory());
        } else {
            privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true, false);
        }
        List<CategoryPrivilege> categoryPrivileges = new ArrayList<>();
        for (CategoryEntityV2 categoryEntityV2 : categoryEntityV2s) {
            CategoryPrivilege categoryPrivilege = new CategoryPrivilege(categoryEntityV2);
            categoryPrivilege.setPrivilege(privilege);
            categoryPrivilege.setSort(categoryEntityV2.getSort());
            categoryPrivilege.setPrivateStatus(categoryEntityV2.getPrivateStatus().name());
            categoryPrivileges.add(categoryPrivilege);
        }
        return categoryPrivileges;
    }

    public CategoryPrivilege.Privilege getCategoryPrivilege(boolean read, boolean editItem, boolean editCategory) {
        CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege();
        if (read) {
            privilege.setHide(false);
            privilege.setAsh(false);
        } else {
            privilege.setHide(false);
            privilege.setAsh(true);
        }
        if (editCategory) {
            privilege.setAddSibling(true);
            privilege.setDelete(true);
        } else {
            privilege.setAddSibling(false);
            privilege.setDelete(false);
        }
        if (editCategory) {
            privilege.setAddChildren(true);
            privilege.setEdit(true);
        } else {
            privilege.setAddChildren(false);
            privilege.setEdit(false);
        }
        if (editItem) {
            privilege.setCreateRelation(true);
            privilege.setDeleteRelation(true);
            privilege.setAddOwner(true);
        } else {
            privilege.setCreateRelation(false);
            privilege.setDeleteRelation(false);
            privilege.setAddOwner(false);
        }
        return privilege;
    }

    /**
     * 全局导出
     *
     * @param categoryType
     * @return
     * @throws IOException
     * @throws AtlasBaseException
     */
    public File exportExcelAll(int categoryType, String tenantId) throws IOException, SQLException {
        String userId = AdminUtils.getUserData().getUserId();
        List<String> userGroupIds = userGroupDAO.getuserGroupByUsersId(userId, tenantId).stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
        List<CategorycateQueryResult> data;
        data = userGroupDAO.getAllCategory(userGroupIds,categoryType,tenantId,userId);
        Workbook workbook = allData2workbook(userDAO, categoryType, data);
        return workbook2file(workbook);
    }

    //全局导出
    private Workbook allData2workbook(UserDAO userDAO, int categoryType, List<CategorycateQueryResult> list) {
        Workbook workbook = new XSSFWorkbook();
        List<List<String>> dataList = list.stream().map(categoryEntityV2 -> {
        List<String> data = Lists.newArrayList(categoryEntityV2.getGuid(), categoryEntityV2.getName(), categoryEntityV2.getDescription(), categoryEntityV2.getUpBrotherCategoryGuid(), categoryEntityV2.getDownBrotherCategoryGuid(), categoryEntityV2.getParentCategoryGuid(), categoryEntityV2.getQualifiedName(), new Integer(categoryEntityV2.getLevel()).toString(),categoryEntityV2.getSort().toString());
            return data;
        }).collect(Collectors.toList());
        PoiExcelUtils.createSheet(workbook, "目录", Lists.newArrayList("目录id", "目录名字", "目录描述", "同级的上方目录id", "同级的下方目录id", "父目录id", "全名称", "级别", "排序"), dataList);
        return workbook;
    }

    private File workbook2file(Workbook workbook) throws IOException {
        File tmpFile = File.createTempFile("CategoryExport", ".xlsx");
        try (FileOutputStream output = new FileOutputStream(tmpFile)) {
            workbook.write(output);
            output.flush();
            output.close();
        }
        return tmpFile;
    }


    /**
     * 目录转化为文件
     *
     * @param ids
     * @param categoryType
     * @return
     * @throws IOException
     * @throws AtlasBaseException
     */
    public File exportExcel(List<String> ids, int categoryType, String tenantId) throws IOException, AtlasBaseException {
        List<CategoryExport> data = queryByIds(ids, categoryType, tenantId);
        Workbook workbook = data2workbook(data);
        return workbook2file(workbook);
    }

    private List<CategoryExport> queryByIds(List<String> ids, int categoryType, String tenantId) throws AtlasBaseException {
        List<CategoryExport> data = userGroupDAO.getCategoryByIds(ids, categoryType, tenantId);
        int i = 0;
        List<CategoryExport> sortData = new ArrayList<>();
        sortData.addAll(data);
        Map<String, Integer> idMap = new HashMap<>();
        for (String id : ids) {
            idMap.put(id, i);
            i++;
        }
        for (CategoryExport categoryExport : data) {
            Integer integer = idMap.get(categoryExport.getGuid());
            if (integer != null) {
                sortData.set(integer, categoryExport);
            }
        }
        return sortData;
    }

    private Workbook data2workbook(List<CategoryExport> list) {
        List<List<String>> dataList = list.stream().map(categoryExport -> {
            List<String> data = Lists.newArrayList(categoryExport.getName(), categoryExport.getDescription());
            return data;
        }).collect(Collectors.toList());

        Workbook workbook = new XSSFWorkbook();
        PoiExcelUtils.createSheet(workbook, "目录", Lists.newArrayList("目录名字", "目录描述"), dataList);
        return workbook;
    }

    public List<CategorycateQueryResult> getAllCategories(int type, String tenantId) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            List<String> userGroupIds = userGroupDAO.getuserGroupByUsersId(userId, tenantId).stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
            List<Module> modules = tenantService.getModule(tenantId);
            //是否无用户组
            boolean isUserGroup = userGroupIds == null || userGroupIds.size() == 0;
            //目录管理权限
            boolean isAdmin = modules.stream().anyMatch(module -> ModuleEnum.AUTHORIZATION.getId() == module.getModuleId());
            List<CategorycateQueryResult> valuesList=new ArrayList<>();
            List<CategorycateQueryResult> categories=userGroupDAO.getAllCategory(userGroupIds,type,tenantId,userId);

            String status1=String.valueOf(Status.AUDITING.getIntValue());
            for(CategorycateQueryResult result:categories){
                boolean delete=false;
                boolean edit=false;
                String guid=result.getGuid();
                String privateStatus=result.getPrivateStatus();
                String status=result.getStatus();
                int count=businessDAO.getBusinessCountByCategoryId(guid,tenantId,userId);
                result.setCount(count);
                if(CategoryPrivateStatus.PUBLIC.name().equals(privateStatus)){
                    if(String.valueOf(Status.REJECT.getIntValue()).equals(status) ||String.valueOf(Status.ACTIVE.getIntValue()).equals(status) ){
                        edit=true;
                    }
                    CategoryPrivilege.Privilege privilege=new CategoryPrivilege.Privilege(false, false, true, true, false, delete, false, false, edit, false);
                    result.setPrivilege(privilege);
                    valuesList.add(result);
                }else {
                    UserGroupPrivilege userGroupPrivilege=getCataPrivilege(userGroupIds,guid);
                    if(null !=userGroupPrivilege){
                        result.setEditCategory(userGroupPrivilege.getEditCategory());
                        result.setEditItem(userGroupPrivilege.getEditItem());
                        result.setRead(userGroupPrivilege.getRead());
                        if(userGroupPrivilege.getEditCategory()){
                            delete=true;
                            edit=true;
                        }else {
                            edit=true;
                        }
                    }else {
                        delete=true;
                        edit=true;
                        //如果当前用户创建的目录已加入其他用户组（其中无当前用户），则该用户不可见
                        int cnt=userGroupDAO.getCateUserGroupRelationNum(guid);
                        if(cnt>0){
                            continue;
                        }
                    }
                    if(status1.equals(status)){
                        delete=false;
                        edit=false;
                    }
                    CategoryPrivilege.Privilege privilege=new CategoryPrivilege.Privilege(false, false, true, true, false, delete, false, false, edit, false);
                    result.setPrivilege(privilege);
                    valuesList.add(result);
                }
            }
            //删除查看权限不足的目录
            removeNoParentCategory(valuesList);
            return valuesList;
        } catch (MyBatisSystemException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常:"+e.getMessage());
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    /**
     * @Author fanjiajia
     * @Description 公共租户查询业务目录
     **/
    public List<CategoryEntityV2> getCategoryBusiness(Integer type) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            List<String> userGroupIds = userGroupDAO.getAlluserGroupByUsersId(userId).stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
            List<CategoryEntityV2> valuesList=new ArrayList<>();
            List<CategoryEntityV2> categories=userGroupDAO.getAllCategoryByCommonTenant(userGroupIds,type,userId);
            for(CategoryEntityV2 result:categories){
                String guid=result.getGuid();
                String privateStatus=result.getPrivateStatus().name();
                if(CategoryPrivateStatus.PUBLIC.name().equals(privateStatus)){
                    valuesList.add(result);
                }else {
                    UserGroupPrivilege userGroupPrivilege=getCataPrivilege(userGroupIds,guid);
                    if(null ==userGroupPrivilege){
                        //如果当前用户创建的目录已加入其他用户组（其中无当前用户），则该用户不可见
                        int cnt=userGroupDAO.getCateUserGroupRelationNum(guid);
                        if(cnt>0){
                            continue;
                        }
                    }
                    valuesList.add(result);
                }
            }

            //删除查看权限不足的目录
            removeNoParentCategory2(valuesList);
            return valuesList;
        } catch (MyBatisSystemException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常:"+e.getMessage());
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    public UserGroupPrivilege getCataPrivilege(List<String> userGroupIds, String guid){
        UserGroupPrivilege  privilege=new UserGroupPrivilege();
        List<UserGroupPrivilege>  list=userGroupDAO.getCataUserGroupPrivilege(guid,userGroupIds);
        if(list==null || list.size()==0 ){
            return  null;
        }
        for(UserGroupPrivilege cp:list){
            privilege.setRead(false);
            privilege.setEditCategory(false);
            privilege.setEditItem(false);
            if(cp.getEditCategory()){
                privilege.setEditCategory(true);
            }
            if(cp.getEditItem()){
                privilege.setEditItem(true);
            }
            if(cp.getRead()){
                privilege.setRead(true);
            }
        }
        return privilege;
    }


    /**
     * 删除有父目录，但是父目录不存在的目录
     * @param categoryList
     */
    private void removeNoParentCategory(List<CategorycateQueryResult> categoryList){
        Map<String,String> map = categoryList.stream().collect(HashMap::new,(m,v)->m.put(v.getGuid(),v.getParentCategoryGuid()),HashMap::putAll);
        categoryList.removeIf(categoryPrivilege ->
                this.checkParentIfExist(map, categoryPrivilege.getParentCategoryGuid(), categoryList));
    }

    private boolean checkParentIfExist(Map<String,String> map,String parentId,List<CategorycateQueryResult> categoryPrivilegeList){
        if (StringUtils.isEmpty(parentId)){
            return false;
        }
        if (map.containsKey(parentId)){
            Optional<CategorycateQueryResult> result=categoryPrivilegeList.stream().filter(c->parentId.equals(c.getGuid())).findFirst();
            if (result.isPresent()){
                return checkParentIfExist(map,result.get().getParentCategoryGuid(),categoryPrivilegeList);
            }
        }
        return true;
    }

    private void removeNoParentCategory2(List<CategoryEntityV2> categoryList){
        Map<String,String> map = categoryList.stream().collect(HashMap::new,(m,v)->m.put(v.getGuid(),v.getParentCategoryGuid()),HashMap::putAll);
        categoryList.removeIf(categoryPrivilege ->
                this.checkParentIfExist2(map, categoryPrivilege.getParentCategoryGuid(), categoryList));
    }

    private boolean checkParentIfExist2(Map<String,String> map,String parentId,List<CategoryEntityV2> categoryPrivilegeList){
        if (StringUtils.isEmpty(parentId)){
            return false;
        }
        if (map.containsKey(parentId)){
            Optional<CategoryEntityV2> result=categoryPrivilegeList.stream().filter(c->parentId.equals(c.getGuid())).findFirst();
            if (result.isPresent()){
                return checkParentIfExist2(map,result.get().getParentCategoryGuid(),categoryPrivilegeList);
            }
        }
        return true;
    }

    /**
     * 删除目录
     *
     * @param guid
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public CategoryDeleteReturn deleteCategory(String guid, String tenantId, int type) throws Exception {
        List<String> categoryIds = categoryDao.queryChildrenCategoryId(guid, tenantId);
        categoryIds.add(guid);
        int item = 0;
        if (type == 1) {
            List<String> businessIds = relationDao.getBusinessIdsByCategoryGuid(categoryIds);
            if (businessIds == null || businessIds.size() == 0) {
                item = 0;
            } else {
                item = businessDAO.deleteBusinessesByIds(businessIds);
                businessDAO.deleteRelationByBusinessIds(businessIds);
                businessDAO.deleteRelationByIds(businessIds);
            }
        }
        CategoryEntityV2 currentCatalog = categoryDao.queryByGuid(guid, tenantId);
        if (Objects.isNull(currentCatalog)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取当前目录信息失败");
        }
        String upBrotherCategoryGuid = currentCatalog.getUpBrotherCategoryGuid();
        String downBrotherCategoryGuid = currentCatalog.getDownBrotherCategoryGuid();
        if (StringUtils.isNotEmpty(upBrotherCategoryGuid)) {
            categoryDao.updateDownBrotherCategoryGuid(upBrotherCategoryGuid, downBrotherCategoryGuid, tenantId);
        }
        if (StringUtils.isNotEmpty(downBrotherCategoryGuid)) {
            categoryDao.updateUpBrotherCategoryGuid(downBrotherCategoryGuid, upBrotherCategoryGuid, tenantId);
        }
        userGroupDAO.deleteCategoryGroupRelationByCategory(guid);
        int category = categoryDao.deleteCategoryByIds(categoryIds, tenantId);
        CategoryDeleteReturn deleteReturn = new CategoryDeleteReturn();
        deleteReturn.setCategory(category);
        deleteReturn.setItem(item);
        return deleteReturn;
    }

}
