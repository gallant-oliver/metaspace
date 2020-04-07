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
 * @author sunhaoning
 * @date 2018/11/19 20:10
 */

import static io.zeta.metaspace.web.service.DataShareService.METASPACE_MOBIUS_ADDRESS;

import com.google.gson.Gson;
import io.zeta.metaspace.SSOConfig;
import io.zeta.metaspace.discovery.MetaspaceGremlinQueryService;
import io.zeta.metaspace.model.metadata.CategoryEntity;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.DataOwner;
import io.zeta.metaspace.model.metadata.DataOwnerHeader;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.RelationQuery;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.TableOwner;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.pojo.TableRelation;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.share.APIDataOwner;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.share.Organization;
import io.zeta.metaspace.model.table.Tag;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.utils.OKHttpClient;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.DateUtils;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.directory.api.util.Strings;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import io.zeta.metaspace.model.role.SystemRole;

@Service
public class DataManageService {
    private static final Logger LOG = LoggerFactory.getLogger(DataManageService.class);

    @Autowired
    CategoryDAO categoryDao;
    @Autowired
    RelationDAO relationDao;
    @Autowired
    RoleDAO roleDao;
    @Autowired
    RoleService roleService;
    @Autowired
    TableDAO tableDAO;
    @Autowired
    DataShareDAO shareDAO;
    @Autowired
    MetaspaceGremlinQueryService metaspaceEntityService;
    @Autowired
    UserDAO userDAO;
    @Autowired
    OrganizationDAO organizationDAO;
    @Autowired
    TableTagDAO tableTagDAO;
    @Autowired
    ColumnDAO columnDAO;
    @Autowired
    MetadataSubscribeDAO metadataSubscribeDAO;
    @Autowired
    UserGroupService userGroupService;
    @Autowired
    UserGroupDAO userGroupDAO;
    @Autowired
    TenantService tenantService;

    private static final String ORGANIZATION_FIRST_PID = "sso.organization.first.pid";

    /**
     * 获取用户有权限的全部目录
     *
     * @param type
     * @return
     * @throws AtlasBaseException
     */
    //独立部署
    public List<CategoryPrivilege> getAll(int type) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            List<Role> roles = roleDao.getRoleByUsersId(user.getUserId());
            List<CategoryPrivilege> valueList = null;
            if (roles.stream().anyMatch(role -> SystemRole.ADMIN.getCode().equals(role.getRoleId()))){
                valueList = roleService.getUserCategory(SystemRole.ADMIN.getCode(), type);
            }else{
                Map<String,CategoryPrivilege> valueMap = new HashMap<>();
                if (roles.stream().allMatch(role -> role.getStatus()==0)){
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
                }
                for (Role role:roles){
                    if (role.getStatus() == 0){
                        continue;
                    }
                    String roleId = role.getRoleId();
                    for (CategoryPrivilege categoryPrivilege:roleService.getUserCategory(roleId, type)){
                        if (valueMap.containsKey(categoryPrivilege.getGuid())&&valueMap.get(categoryPrivilege.getGuid())!=null){
                            valueMap.get(categoryPrivilege.getGuid()).getPrivilege().mergePrivilege(categoryPrivilege.getPrivilege());
                        }else{
                            valueMap.put(categoryPrivilege.getGuid(),categoryPrivilege);
                        }
                    }
                }
                valueList = new ArrayList<>(valueMap.values());
            }

            return valueList;
        } catch (MyBatisSystemException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    //多租户
    public List<CategoryPrivilege> getAllByUserGroup(int type,String tenantId) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(),tenantId);
            List<CategoryPrivilege> valueList = null;
            List<Module> modules = tenantService.getModule(tenantId);
            if (type!=1&&type!=0){
                valueList = userGroupService.getUserCategory(null, type,modules,tenantId);
            }else if ((userGroups==null||userGroups.size()==0)//无用户组
                      &&modules.stream().anyMatch(module -> ModuleEnum.AUTHORIZATION.getId()==module.getModuleId())//目录管理权限
            ){
                valueList = userGroupService.getAdminCategory(type,tenantId);
            }else{
                Map<String,CategoryPrivilege> valueMap = new HashMap<>();
                if (userGroups==null||userGroups.size()==0){
                    return new ArrayList<>();
                }
                for (UserGroup userGroup:userGroups){
                    String userGroupId = userGroup.getId();
                    for (CategoryPrivilege categoryPrivilege:userGroupService.getUserCategory(userGroupId, type,modules,tenantId)){
                        if (valueMap.containsKey(categoryPrivilege.getGuid())&&valueMap.get(categoryPrivilege.getGuid())!=null){
                            valueMap.get(categoryPrivilege.getGuid()).getPrivilege().mergePrivilege(categoryPrivilege.getPrivilege());
                        }else{
                            valueMap.put(categoryPrivilege.getGuid(),categoryPrivilege);
                        }
                    }
                }
                valueList = new ArrayList<>(valueMap.values());
            }
            if (modules.stream().anyMatch(module -> ModuleEnum.AUTHORIZATION.getId()==module.getModuleId())){
                for (CategoryPrivilege categoryPrivilege:valueList){
                    categoryPrivilege.getPrivilege().adminPrivilege();
                }
            }
            return valueList;
        } catch (MyBatisSystemException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            throw e;
        }
    }
    /**
     * 创建业务目录
     *
     * @param info
     * @param type
     * @return
     * @throws Exception
     */
    @Transactional
    public CategoryPrivilege createCategory(CategoryInfoV2 info, Integer type,String tenantId) throws Exception {
        try {
            String currentCategoryGuid = info.getGuid();
            CategoryEntityV2 entity = new CategoryEntityV2();
            StringBuffer qualifiedName = new StringBuffer();
            String newCategoryGuid = UUID.randomUUID().toString();
            String name = info.getName();
            if (Objects.isNull(name)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录名不能为空");
            }
            User user = AdminUtils.getUserData();
            //判断独立部署和多租户
            List<Module> moduleByUserId = TenantService.defaultTenant.equals(tenantId) ? userDAO.getModuleByUserId(user.getUserId()) :tenantService.getModule(tenantId);
            List<Integer> modules = new ArrayList<>();
            for (Module module : moduleByUserId) {
                modules.add(module.getModuleId());
            }
            //guid
            entity.setGuid(newCategoryGuid);
            //name
            entity.setName(name);
            //description
            entity.setDescription(info.getDescription());
            entity.setCategoryType(type);
            entity.setSafe(info.getSafe());
            if (StringUtils.isEmpty(entity.getSafe())){
                entity.setSafe("1");
            }

            //创建第一个目录
            if (StringUtils.isEmpty(currentCategoryGuid)) {
                if(categoryDao.ifExistCategory(type,tenantId) > 0) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在首个目录，请刷新后重新操作");
                }
                if (TenantService.defaultTenant.equals(tenantId)){
                    List<Role> roles = roleDao.getRoleByUsersId(user.getUserId());
                    if (!roles.stream().anyMatch(role -> SystemRole.ADMIN.getCode().equals(role.getRoleId()))){
                        throw new AtlasBaseException(AtlasErrorCode.PERMISSION_DENIED, "当前用户没有创建目录权限");
                    }

                }else if (!modules.contains(ModuleEnum.AUTHORIZATION.getId())&&type==1){
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有目录授权模块权限，无法创建一级业务目录");
                }
                //qualifiedName
                qualifiedName.append(name);
                entity.setQualifiedName(qualifiedName.toString());
                entity.setLevel(1);
                categoryDao.add(entity,tenantId);
                CategoryPrivilege returnEntity = new CategoryPrivilege();
                returnEntity.setGuid(newCategoryGuid);
                returnEntity.setName(name);
                returnEntity.setDescription(info.getDescription());
                returnEntity.setLevel(1);
                returnEntity.setParentCategoryGuid(null);
                returnEntity.setUpBrotherCategoryGuid(null);
                returnEntity.setDownBrotherCategoryGuid(null);
                CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege(false,true,true,true,true,true,true,true,true,false);
                if(type==0){
                    privilege.setDeleteRelation(false);
                }
                returnEntity.setPrivilege(privilege);
                return returnEntity;
            }
            if(Objects.isNull(categoryDao.queryByGuidV2(currentCategoryGuid))) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前目录已被删除，请刷新后重新操作");
            }

            String newCategoryParentGuid = info.getParentCategoryGuid();
            //获取当前catalog
            CategoryEntityV2 currentEntity = categoryDao.queryByGuid(currentCategoryGuid);
            String parentQualifiedName = null;
            String parentGuid = null;
            int currentLevel = categoryDao.getCategoryLevel(currentCategoryGuid);
            //创建子目录
            if (StringUtils.isNotEmpty(newCategoryParentGuid)) {
                parentGuid = currentCategoryGuid;
                entity.setParentCategoryGuid(currentCategoryGuid);
                parentQualifiedName = currentEntity.getQualifiedName();
                entity.setLevel(currentLevel + 1);
            } else {
                //创建同级目录
                parentGuid = currentEntity.getParentCategoryGuid();
                entity.setLevel(currentLevel);
                if (StringUtils.isNotEmpty(parentGuid)) {
                    entity.setParentCategoryGuid(parentGuid);
                    CategoryEntityV2 currentCatalogParentEntity = categoryDao.queryByGuid(parentGuid);
                    parentQualifiedName = currentCatalogParentEntity.getQualifiedName();
                }
            }
            if (StringUtils.isNotEmpty(parentQualifiedName) && parentQualifiedName.length() > 0)
                qualifiedName.append(parentQualifiedName + ".");
            qualifiedName.append(name);
            int count = categoryDao.querySameNameNum(name, parentGuid, type,tenantId);
            if (count > 0)
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在相同的目录名");
            //qualifiedName
            entity.setQualifiedName(qualifiedName.toString());

            //子目录
            if (StringUtils.isNotEmpty(newCategoryParentGuid)) {
                String lastChildGuid = categoryDao.queryLastChildCategory(currentCategoryGuid,tenantId);
                if (StringUtils.isNotEmpty(lastChildGuid)) {
                    entity.setUpBrotherCategoryGuid(lastChildGuid);
                    categoryDao.updateDownBrotherCategoryGuid(lastChildGuid, newCategoryGuid);
                }
            } else {
                //同级目录
                if (StringUtils.isNotEmpty(currentCategoryGuid) && Strings.equals(info.getDirection(), "up")) {
                    entity.setDownBrotherCategoryGuid(currentCategoryGuid);
                    String upBrotherGuid = currentEntity.getUpBrotherCategoryGuid();
                    if (StringUtils.isNotEmpty(upBrotherGuid)) {
                        entity.setUpBrotherCategoryGuid(upBrotherGuid);
                        categoryDao.updateDownBrotherCategoryGuid(upBrotherGuid, newCategoryGuid);
                    }
                    categoryDao.updateUpBrotherCategoryGuid(currentCategoryGuid, newCategoryGuid);
                } else if (StringUtils.isNotEmpty(currentCategoryGuid) && Strings.equals(info.getDirection(), "down")) {
                    entity.setUpBrotherCategoryGuid(info.getGuid());
                    String downBrotherGuid = currentEntity.getDownBrotherCategoryGuid();
                    if (StringUtils.isNotEmpty(downBrotherGuid)) {
                        entity.setDownBrotherCategoryGuid(downBrotherGuid);
                        categoryDao.updateUpBrotherCategoryGuid(downBrotherGuid, newCategoryGuid);
                    }
                    categoryDao.updateDownBrotherCategoryGuid(currentCategoryGuid, newCategoryGuid);
                }
            }
            categoryDao.add(entity,tenantId);
            CategoryPrivilege returnEntity = categoryDao.queryByGuidV2(newCategoryGuid);
            CategoryPrivilege.Privilege privilege =null;
            if(type==0) {
                if(modules.contains(ModuleEnum.TECHNICALEDIT.getId())) {
                    privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true,false);
                }else{
                    privilege =new CategoryPrivilege.Privilege(false, false, true, true, false, true, false, false, true,false);
                }
            }else{
                if(modules.contains(ModuleEnum.BUSINESSEDIT.getId())) {
                    privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true,false);
                }else{
                    privilege =new CategoryPrivilege.Privilege(false, false, true, true, false, true, false, false, true,false);
                }
            }
            if (modules.contains(ModuleEnum.AUTHORIZATION.getId())&&//有目录权限管理模块权限，可以随意建目录
                !userGroupService.isPrivilegeCategory(user.getUserId(),newCategoryGuid,tenantId,type)){//无当前目录权限
                privilege.setAsh(true);
            }
            returnEntity.setPrivilege(privilege);
            return returnEntity;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("创建业务目录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "创建业务目录失败");
        }
    }
    /**
     * 获取目录信息
     * @param guid
     * @return
     * @throws SQLException
     */
    public CategoryEntityV2 getCategory(String guid) throws SQLException {
        return categoryDao.queryByGuid(guid);
    }

    /**
     * 删除目录
     *
     * @param guid
     * @return
     * @throws Exception
     */
    @Transactional
    public int deleteCategory(String guid,String tenantId) throws Exception {
        try {
            int childrenNum = categoryDao.queryChildrenNum(guid);
            if (childrenNum > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前目录下存在子目录");
            }
            int relationNum = relationDao.queryRelationNumByCategoryGuid(guid);
            int businessRelationNum = relationDao.queryBusinessRelationNumByCategoryGuid(guid);
            if (relationNum > 0 || businessRelationNum > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前目录下存在关联关系");
            }
            CategoryEntityV2 currentCatalog = categoryDao.queryByGuid(guid);
            if (Objects.isNull(currentCatalog)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取当前目录信息失败");
            }
            String upBrotherCategoryGuid = currentCatalog.getUpBrotherCategoryGuid();
            String downBrotherCategoryGuid = currentCatalog.getDownBrotherCategoryGuid();
            if (StringUtils.isNotEmpty(upBrotherCategoryGuid)) {
                categoryDao.updateDownBrotherCategoryGuid(upBrotherCategoryGuid, downBrotherCategoryGuid);
            }
            if (StringUtils.isNotEmpty(downBrotherCategoryGuid)) {
                categoryDao.updateUpBrotherCategoryGuid(downBrotherCategoryGuid, upBrotherCategoryGuid);
            }
            if (TenantService.defaultTenant.equals(tenantId)){
                User user = AdminUtils.getUserData();
                List<Role> roles = roleDao.getRoleByUsersId(user.getUserId());
                if (roles.stream().allMatch(role -> role.getStatus()==0)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
                }
                roleDao.deleteRole2categoryByUserId(guid);
            }else{
                userGroupDAO.deleteCategoryGroupRelationByCategory(guid);
            }
            return categoryDao.delete(guid);
        } catch (SQLException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("删除目录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除目录失败");
        }
    }

    /**
     * 更新目录
     *
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    public String updateCategory(CategoryInfoV2 info, int type,String tenantId) throws AtlasBaseException {
        try {
            String guid = info.getGuid();
            String name = info.getName();
            if (Objects.isNull(name)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录名不能为空");
            }
            CategoryEntityV2 currentEntity = categoryDao.queryByGuid(guid);
            String parentQualifiedName = null;
            StringBuffer qualifiedName = new StringBuffer();
            if (StringUtils.isNotEmpty(currentEntity.getParentCategoryGuid()))
                parentQualifiedName = categoryDao.queryQualifiedName(currentEntity.getParentCategoryGuid());
            if (StringUtils.isNotEmpty(parentQualifiedName))
                qualifiedName.append(parentQualifiedName + ".");
            qualifiedName.append(name);
            int count = categoryDao.querySameNameNum(name, currentEntity.getParentCategoryGuid(), type,tenantId);
            if (count > 0 && !currentEntity.getName().equals(info.getName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在相同的目录名");
            }
            CategoryEntity entity = new CategoryEntity();
            entity.setGuid(info.getGuid());
            entity.setName(info.getName());
            entity.setQualifiedName(qualifiedName.toString());
            entity.setDescription(info.getDescription());
            entity.setSafe(info.getSafe());
            categoryDao.updateCategoryInfo(entity);
            return "success";
        } catch (SQLException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (Exception e) {
            LOG.error("更新目录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新目录失败");
        }
    }

    /**
     * 添加关联
     *
     * @param categoryGuid
     * @param relations
     * @throws AtlasBaseException
     */
    @Transactional
    public void assignTablesToCategory(String categoryGuid, List<RelationEntityV2> relations) throws AtlasBaseException {
        try {
            long time = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String generateTime = format.format(time);
            for (RelationEntityV2 relation : relations) {
                //删除旧的
                relationDao.deleteByTableGuid(relation.getTableGuid());
                relation.setCategoryGuid(categoryGuid);
                relation.setGenerateTime(generateTime);
                if(relationDao.ifRelationExists(categoryGuid,relation.getTableGuid())==0) {
                    addRelation(relation);
                }
            }
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("添加关联失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加关联失败");
        }
    }

    @Transactional
    public int addRelation(RelationEntityV2 relationEntity) throws AtlasBaseException {
        try {
            String relationshiGuid = UUID.randomUUID().toString();
            relationEntity.setRelationshipGuid(relationshiGuid);
            return relationDao.add(relationEntity);
        } catch (SQLException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        }
    }



    //1.4删除表关联，取消子目录表关联关系时数据表自动回到一级目录
    @Transactional
    public void removeRelationAssignmentFromTablesV2(List<RelationEntityV2> relationshipList) throws AtlasBaseException {
        try {
            if (Objects.nonNull(relationshipList)) {
                for (RelationEntityV2 relationship : relationshipList) {
                    String relationshipGuid = relationship.getRelationshipGuid();
                    RelationEntityV2 relationInfo = relationDao.getRelationInfoByGuid(relationshipGuid);
                    relationDao.delete(relationInfo.getRelationshipGuid());
                    String topGuid = relationDao.getTopGuidByGuid(relationInfo.getCategoryGuid());
                    //当贴源层没有关联该表时
                    if(relationDao.ifRelationExists(topGuid,relationInfo.getTableGuid())==0){
                        TableRelation tableRelation = new TableRelation();
                        tableRelation.setRelationshipGuid(UUID.randomUUID().toString());
                        tableRelation.setCategoryGuid(topGuid);
                        tableRelation.setTableGuid(relationInfo.getTableGuid());
                        tableRelation.setGenerateTime(DateUtils.getNow());
                        relationDao.addRelation(tableRelation);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("取消关联失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "取消关联失败");
        }
    }

    /**
     * @param categoryGuid
     * @param query
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<RelationEntityV2> getRelationsByCategoryGuid(String categoryGuid, RelationQuery query,String tenantId) throws AtlasBaseException {
        try {
            int limit = query.getLimit();
            int offset = query.getOffset();
            PageResult<RelationEntityV2> pageResult = new PageResult<>();
            List<RelationEntityV2> relations = new ArrayList<>();
            int totalNum = 0;
            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)){
                relations = relationDao.queryRelationByCategoryGuid(categoryGuid, limit, offset);
            } else{
                User user = AdminUtils.getUserData();
                List<String> databases = tenantService.getDatabaseByUser(user.getAccount(), tenantId);
                if (databases!=null&&databases.size()!=0)
                    relations = relationDao.queryRelationByCategoryGuidV2(categoryGuid, limit, offset,databases);
            }
            for(RelationEntityV2 entity : relations) {
                String tableGuid = entity.getTableGuid();
                List<Tag> tableTageList = tableTagDAO.getTable2Tag(tableGuid);
                List<String> tableTagNameList = tableTageList.stream().map(tag -> tag.getTagName()).collect(Collectors.toList());
                entity.setTableTagList(tableTagNameList);
                List<DataOwnerHeader> ownerHeaders = tableDAO.getDataOwnerList(tableGuid);
                entity.setDataOwner(ownerHeaders);
            }
            //totalNum = relationDao.queryTotalNumByCategoryGuid(categoryGuid);
            if (relations.size()!=0){
                totalNum = relations.get(0).getTotal();
            }
            getPath(relations);
            pageResult.setCurrentSize(relations.size());
            pageResult.setLists(relations);
            //pageResult.setOffset(query.getOffset());
            pageResult.setTotalSize(totalNum);
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取关联失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取关联失败");
        }
    }
    /**
     * @param categoryGuid
     * @param query
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<RelationEntityV2> getRelationsByCategoryGuidFilter(String categoryGuid, RelationQuery query,String tenantId) throws AtlasBaseException {
        try {

            int limit = query.getLimit();
            int offset = query.getOffset();
            PageResult<RelationEntityV2> pageResult = new PageResult<>();
            List<RelationEntityV2> relations = new ArrayList<>();
            int totalNum = 0;
            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)){
                relations = relationDao.queryRelationByCategoryGuidFilter(categoryGuid, limit, offset);
            }else {
                User user = AdminUtils.getUserData();
                List<String> databases = tenantService.getDatabaseByUser(user.getAccount(), tenantId);
                if (databases!=null&&databases.size()!=0)
                    relations = relationDao.queryRelationByCategoryGuidFilterV2(categoryGuid, limit, offset,databases);
            }
            //totalNum = relationDao.queryTotalNumByCategoryGuidFilter(categoryGuid);
            if (relations.size()!=0){
                totalNum = relations.get(0).getTotal();
            }
            getPath(relations);
            pageResult.setCurrentSize(relations.size());
            pageResult.setLists(relations);
            //pageResult.setOffset(query.getOffset());
            pageResult.setTotalSize(totalNum);
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取关联失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取关联失败");
        }
    }
    public PageResult<RelationEntityV2> getRelationsByTableName(RelationQuery query, int type,String tenantId) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            List<String> categoryIds = new ArrayList<>();
            String tableName = query.getFilterTableName();
            String tag = query.getTag();
            int limit = query.getLimit();
            int offset = query.getOffset();
            List<RelationEntityV2> list=new ArrayList<>();
            PageResult<RelationEntityV2> pageResult = new PageResult<>();
            if(StringUtils.isNotEmpty(tableName))
                tableName = tableName.replaceAll("%", "/%").replaceAll("_", "/_");
            if(StringUtils.isNotEmpty(tag))
                tag = tag.replaceAll("%", "/%").replaceAll("_", "/_");
            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)){
                List<Role> roles = roleDao.getRoleByUsersId(user.getUserId());
                if (roles.stream().allMatch(role -> role.getStatus()==0)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
                }
                if (roles.stream().anyMatch(role -> SystemRole.ADMIN.getCode().equals(role.getRoleId()))){
                    categoryIds = CategoryRelationUtils.getPermissionCategoryList(SystemRole.ADMIN.getCode(), type);
                }else{
                    for (Role role:roles){
                        if (role.getStatus() == 0){
                            continue;
                        }
                        String roleId = role.getRoleId();
                        List<String> category = CategoryRelationUtils.getPermissionCategoryList(roleId, type);
                        for (String categoryId  : category){
                            if (!categoryIds.contains(categoryId)){
                                categoryIds.add(categoryId);
                            }
                        }
                    }
                }
                list = categoryIds.size()!=0 ? new ArrayList<>() : relationDao.queryByTableName(tableName, tag, categoryIds, limit, offset);
            }else{
                List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(),tenantId);
                for (UserGroup userGroup:userGroups){
                    String userGroupId = userGroup.getId();
                    List<String> category = CategoryRelationUtils.getPermissionCategoryListV2(userGroupId, type,tenantId);
                    for (String categoryId  : category){
                        if (!categoryIds.contains(categoryId)){
                            categoryIds.add(categoryId);
                        }
                    }
                }
                List<String> databases = tenantService.getDatabaseByUser(user.getAccount(), tenantId);

                if (databases!=null&&databases.size()!=0&&categoryIds.size()!=0)
                    list  = relationDao.queryByTableNameV2(tableName, tag, categoryIds, limit, offset,databases);
            }
            //tag
            list.forEach(entity -> {
                List<Tag> tableTageList = tableTagDAO.getTable2Tag(entity.getTableGuid());
                List<String> tableTagNameList = tableTageList.stream().map(tableTag -> tableTag.getTagName()).collect(Collectors.toList());
                entity.setTableTagList(tableTagNameList);
            });

            //path
            getPath(list);
            //dataOwner
            for(RelationEntityV2 entity : list) {
                String tableGuid = entity.getTableGuid();
                List<DataOwnerHeader> ownerHeaders = tableDAO.getDataOwnerList(tableGuid);
                entity.setDataOwner(ownerHeaders);
            }
            long totalNum = 0;
            if (list.size()!=0){
                totalNum = list.get(0).getTotal();
            }
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            pageResult.setTotalSize(totalNum);
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("搜索关联表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "搜索关联表失败");
        }
    }
    public PageResult<RelationEntityV2> getRelationsByTableNameFilter(RelationQuery query, int type,String tenantId) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            List<String> categoryIds = new ArrayList<>();
            String tableName = query.getFilterTableName();
            String tag = query.getTag();
            int limit = query.getLimit();
            int offset = query.getOffset();
            PageResult<RelationEntityV2> pageResult = new PageResult<>();
            if(StringUtils.isNotEmpty(tableName))
                tableName = tableName.replaceAll("%", "/%").replaceAll("_", "/_");
            if(StringUtils.isNotEmpty(tag))
                tag = tag.replaceAll("%", "/%").replaceAll("_", "/_");
            List<RelationEntityV2> list=new ArrayList<>();

            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)){
                List<Role> roles = roleDao.getRoleByUsersId(user.getUserId());
                if (roles.stream().allMatch(role -> role.getStatus()==0)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
                }
                if (roles.stream().anyMatch(role -> SystemRole.ADMIN.getCode().equals(role.getRoleId()))){
                    categoryIds = CategoryRelationUtils.getPermissionCategoryList(SystemRole.ADMIN.getCode(), type);
                }else{
                    for (Role role:roles){
                        if (role.getStatus() == 0){
                            continue;
                        }
                        String roleId = role.getRoleId();
                        List<String> category = CategoryRelationUtils.getPermissionCategoryList(roleId, type);
                        for (String categoryId  : category){
                            if (!categoryIds.contains(categoryId)){
                                categoryIds.add(categoryId);
                            }
                        }
                    }
                }
                list = categoryIds.size()!=0 ? new ArrayList<>() : relationDao.queryByTableNameFilter(tableName, tag, categoryIds, limit, offset);
            }else{
                List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(),tenantId);
                for (UserGroup userGroup:userGroups){
                    String userGroupId = userGroup.getId();
                    List<String> category = CategoryRelationUtils.getPermissionCategoryListV2(userGroupId, type,tenantId);
                    for (String categoryId  : category){
                        if (!categoryIds.contains(categoryId)){
                            categoryIds.add(categoryId);
                        }
                    }
                }
                List<String> databases = tenantService.getDatabaseByUser(user.getAccount(), tenantId);
                if (databases!=null&&databases.size()!=0&&categoryIds.size()!=0)
                    list = relationDao.queryByTableNameFilterV2(tableName, tag, categoryIds, limit, offset,databases);
            }

            getPath(list);
            long totalNum = 0;
            if (list.size()!=0){
                totalNum=list.get(0).getTotal();
            }
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            pageResult.setTotalSize(totalNum);
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取关联表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取关联表失败");
        }
    }
    public void getPath(List<RelationEntityV2> list) throws AtlasBaseException {
        for (RelationEntityV2 entity : list) {
            String path = CategoryRelationUtils.getPath(entity.getCategoryGuid());
            entity.setPath(path);
        }
    }

    @Transactional
    public void updateStatus(List<AtlasEntity> entities) {
        for (AtlasEntity entity : entities) {
            String guid = entity.getGuid();
            String typeName = entity.getTypeName();
            if (typeName.contains("table")) {
                relationDao.updateTableStatus(guid, "DELETED");
            }
            if (typeName.contains("hive_db")) {
                relationDao.updateDatabaseStatus(guid, "DELETED");
            }
            if (typeName.contains("hive_column")) {
                columnDAO.updateColumnStatus(guid, "DELETED");
            }
        }
    }

    public Set<CategoryEntityV2> getAllDepartments(int type,String tenantId) throws AtlasBaseException {
        try {
            return categoryDao.getAllDepartments(type,tenantId);
        } catch (SQLException e) {
            LOG.error("获取数据失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }

    @Transactional
    public int addTableOwner(TableOwner tableOwner,String tenantId) throws AtlasBaseException {
        try {
            List<String> tableList = tableOwner.getTables();
            //api
            List<APIInfoHeader> apiList = shareDAO.getAPIByRelatedTable(tableList,tenantId);
            List<TableOwner.Owner> tableOwners = tableOwner.getOwners();
            if(Objects.nonNull(apiList) && apiList.size()>0) {
                sendToMobius(apiList, tableOwners);
            }
            //删除旧关系
            categoryDao.deleteDataOwner(tableOwner.getTables());
            //修改人
            String keeper = AdminUtils.getUserData().getUserId();
            //修改时间
            String generateTIme = DateUtils.getNow();
            //列表
            List<DataOwner> table2OwnerList = new ArrayList<>();
            for(String tableGuid : tableList) {
                for(TableOwner.Owner owner : tableOwners) {
                    DataOwner dOnwer = new DataOwner();
                    dOnwer.setTableGuid(tableGuid);
                    dOnwer.setOwnerId(owner.getId());
                    dOnwer.setPkId(owner.getPkid());
                    dOnwer.setKeeper(keeper);
                    dOnwer.setGenerateTime(generateTIme);
                    table2OwnerList.add(dOnwer);
                }
            }
            return categoryDao.addDataOwner(table2OwnerList);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("添加组织架构失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加组织架构失败");
        }
    }

    public void sendToMobius(List<APIInfoHeader> apiList, List<TableOwner.Owner> tableOwners) throws AtlasException, AtlasBaseException {
        Configuration configuration = ApplicationProperties.get();
        String mobiusURL = configuration.getString(METASPACE_MOBIUS_ADDRESS)  + "/reviews/user";
        //organization
        List<APIDataOwner.Organization> organizations = new ArrayList<>();
        for (TableOwner.Owner owner : tableOwners) {
            APIDataOwner.Organization organization = new APIDataOwner.Organization();
            organization.setOrganization(owner.getPkid());
            organization.setOrganization_type(owner.getType());
            organizations.add(organization);
        }

        Gson gson = new Gson();
        //向云平台发请求
        for(int i=0, len=apiList.size(); i<len; i++) {
            String apiGuid = apiList.get(i).getGuid();
            String manager = apiList.get(i).getManager();
            List<String> apiGuidList = new ArrayList<>();
            List<String> ownerList = new ArrayList<>();
            ownerList.add(manager);
            apiGuidList.add(apiGuid);
            APIDataOwner dataOwner = new APIDataOwner();
            dataOwner.setApi_id_list(apiGuidList);
            dataOwner.setOrganization_list(organizations);
            dataOwner.setApi_owner(ownerList);
            String jsonStr = gson.toJson(dataOwner, APIDataOwner.class);

            int retryCount = 0;
            String error_id = null;
            String error_reason = null;
            while(retryCount < 3) {
                String res = OKHttpClient.doPut(mobiusURL, jsonStr);
                LOG.info(res);
                if(StringUtils.isNotEmpty(res)) {
                    Map response = gson.fromJson(res, Map.class);
                    error_id = String.valueOf(response.get("error-id"));
                    error_reason = String.valueOf(response.get("reason"));
                    if ("0.0".equals(error_id)) {
                        break;
                    } else {
                        retryCount++;
                    }
                } else {
                    retryCount++;
                }
            }
            if(!"0.0".equals(error_id)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "云平台修改表owner失败：" + error_reason);
            }
        }


    }

    public PageResult<Organization> getOrganizationByPid(String pId, Parameters parameters) throws AtlasBaseException {
        try {
            String query = parameters.getQuery();
            if(StringUtils.isNotEmpty(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            Integer limit = parameters.getLimit();
            Integer offset = parameters.getOffset();
            List<Organization> list = organizationDAO.getOrganizationByPid(pId, query, limit, offset);
            Configuration configuration = ApplicationProperties.get();
            String firstPid = configuration.getString(ORGANIZATION_FIRST_PID);
            for(Organization organization : list) {
                String pathStr = organizationDAO.getPathById(firstPid, organization.getId());
                String path = pathStr.replace(",", ".").replace("\"", "").replace("{", "").replace("}", "");
                organization.setPath(path);
            }
            long totalSize = 0;
            if (list.size()!=0){
                totalSize = list.get(0).getTotal();
            }
            long count = list.size();
            PageResult pageResult = new PageResult();
            pageResult.setLists(list);
            pageResult.setCurrentSize(count);
            pageResult.setTotalSize(totalSize);
            return pageResult;
        } catch (Exception e) {
            LOG.error("查询失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    public PageResult<Organization> getOrganizationByName(Parameters parameters) throws AtlasBaseException {
        try {
            String query = parameters.getQuery();
            if(StringUtils.isNotEmpty(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            Integer limit = parameters.getLimit();
            Integer offset = parameters.getOffset();
            List<Organization> list = organizationDAO.getOrganizationByName(query, limit, offset);
            Configuration configuration = ApplicationProperties.get();
            String firstPid = configuration.getString(ORGANIZATION_FIRST_PID);
            for(Organization organization : list) {
                String pathStr = organizationDAO.getPathById(firstPid, organization.getId());
                String path = pathStr.replace(",", ".").replace("\"", "").replace("{", "").replace("}", "");
                organization.setPath(path);
            }
            long totalSize = 0;
            if (list.size()!=0){
                totalSize = list.get(0).getTotal();
            }
            long count = list.size();
            PageResult pageResult = new PageResult();
            //pageResult.setOffset(offset);
            pageResult.setLists(list);
            pageResult.setCurrentSize(count);
            pageResult.setTotalSize(totalSize);
            return pageResult;
        } catch (Exception e) {
            LOG.error("查询失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    public List getOrganization() throws AtlasBaseException {
        try {
            String organizationURL = SSOConfig.getOrganizationURL();
            String organizationCountURL = SSOConfig.getOrganizationCountURL();
            long currentTime = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String endTime = sdf.format(currentTime);

            HashMap<String, String> header = new HashMap<>();
            Gson gson = new Gson();
            Map<String, String> queryCountParamMap = new HashMap<>();
            queryCountParamMap.put("endTime", endTime);
            List data = new ArrayList();
            int retryCount = 0;
            while(retryCount < 3) {
                String countSession = OKHttpClient.doGet(organizationCountURL, queryCountParamMap, header);
                if(Objects.isNull(countSession)) {
                    retryCount++;
                    continue;
                }
                Map countBody = gson.fromJson(countSession, Map.class);
                Map countData = (Map) countBody.get("data");
                double count = (Double) countData.get("count");

                Map<String, String> queryDataParamMap = new HashMap<>();
                queryDataParamMap.put("currentPage", "0");
                queryDataParamMap.put("pageSize", String.valueOf((int)count));
                queryDataParamMap.put("endTime", endTime);
                String session = OKHttpClient.doGet(organizationURL, queryDataParamMap, header);
                if(Objects.isNull(session)) {
                    retryCount++;
                    continue;
                }
                Map body = gson.fromJson(session, Map.class);
                data = (List) body.get("data");
                break;
            }
            return data;
        } catch (Exception e) {
            LOG.error("获取组织架构失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取组织架构失败");
        }
    }

    @Transactional
    public void updateOrganization() throws AtlasBaseException {
        try {
            organizationDAO.deleteOrganization();
            List<Map> data = getOrganization();
            List<Organization> list = toOrganization(data);
            int size = list.size();
            int startIndex = 0;
            int endIndex = startIndex + 200;
            List insertList = null;
            while(endIndex < size) {
                insertList = list.subList(startIndex, endIndex);
                if(Objects.nonNull(insertList) && insertList.size()>0) {
                    organizationDAO.addOrganizations(insertList);
                }
                startIndex = endIndex;
                endIndex += 200;
            }
            insertList = list.subList(startIndex, size);
            if(Objects.nonNull(insertList) && insertList.size()>0) {
                organizationDAO.addOrganizations(insertList);
            }
        } catch (Exception e) {
            LOG.error("更新组织架构失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新组织架构失败");
        }
    }

    public List<Organization> toOrganization(List<Map> dataList) {
        List<Organization> list = new ArrayList<>();
        for(Map data : dataList) {
            Organization organization = parseMap2Object(data, Organization.class);
            list.add(organization);
        }
        return list;
    }

    public static <T> T parseMap2Object(Map paramMap, Class<T> cls) {
        Gson gson = new Gson();
        return gson.fromJson(gson.toJson(paramMap),cls);
    }

    @Transactional
    public void addEntity(List<AtlasEntity> entities) throws AtlasBaseException {
        try {
            //添加到tableinfo
            for (AtlasEntity entity : entities) {
                String typeName = entity.getTypeName();
                if (("hive_table").equals(typeName)) {
                    if (entity.getAttribute("temporary") == null || entity.getAttribute("temporary").toString().equals("false")) {
                        String guid = entity.getGuid();
                        String name = getEntityAttribute(entity, "name");
                        if (tableDAO.ifTableExists(guid) == 0) {
                            TableInfo tableInfo = new TableInfo();
                            tableInfo.setTableGuid(guid);
                            tableInfo.setTableName(name);
                            Object createTime = entity.getAttribute("createTime");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String formatDateStr = sdf.format(createTime);
                            tableInfo.setCreateTime(formatDateStr);
                            tableInfo.setStatus(entity.getStatus().name());
                            AtlasRelatedObjectId relatedDB = getRelatedDB(entity);
                            tableInfo.setDatabaseGuid(relatedDB.getGuid());
                            tableInfo.setDbName(relatedDB.getDisplayText());
                            tableInfo.setDatabaseStatus(relatedDB.getEntityStatus().name());
                            tableDAO.addTable(tableInfo);
                        }
                    }
                } else if (("hive_column").equals(typeName)) {
                    AtlasRelatedObjectId table = (AtlasRelatedObjectId) entity.getRelationshipAttribute("table");
                    String tableGuid = table.getGuid();
                    String guid = entity.getGuid();
                    String name = entity.getAttribute("name").toString();
                    String type = entity.getAttribute("type").toString();
                    String status = entity.getStatus().name();
                    String updateTime = DateUtils.date2String(entity.getUpdateTime());
                    Column column = new Column();
                    column.setTableId(tableGuid);
                    column.setColumnId(guid);
                    column.setColumnName(name);
                    column.setType(type);
                    column.setStatus(status);
                    column.setDisplayNameUpdateTime(updateTime);
                    List<Column> columnList = new ArrayList<>();
                    columnList.add(column);
                    columnDAO.addColumnDisplayInfo(columnList);
                }
            }
            addFullRelation();
        } catch (Exception e) {
            LOG.error("添加entity失败", e);
        }
    }

    @Transactional
    public void addFullRelation() {
        //添加关联
        List<String> newTable = tableDAO.getNewTable();
        List<TableRelation> tableRelations = new ArrayList<>();
        for (String s : newTable) {
            TableRelation tableRelation = new TableRelation();
            tableRelation.setCategoryGuid("1");
            tableRelation.setGenerateTime(DateUtils.getNow());
            tableRelation.setRelationshipGuid(UUID.randomUUID().toString());
            tableRelation.setTableGuid(s);
            tableRelations.add(tableRelation);
        }
        if (tableRelations.size() > 0)
            tableDAO.addRelations(tableRelations);
    }

    @Transactional
    public void supplementTable(String tenantId) throws AtlasBaseException {
        try {
            PageResult<Table> tableNameAndDbNameByQuery;
            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)){
                tableNameAndDbNameByQuery = metaspaceEntityService.getTableNameAndDbNameByQuery("", false, 0, -1);
            }else{
                List<String> dbs = tenantService.getDatabase(tenantId);
                String dbsToString = dbsToString(dbs);
                tableNameAndDbNameByQuery = metaspaceEntityService.getTableNameAndDbNameByQuery("", false, 0, -1,dbsToString);

            }
            List<Table> lists = tableNameAndDbNameByQuery.getLists();
            for (Table list : lists) {
                String tableId = list.getTableId();
                if (tableDAO.ifTableExists(tableId) == 0) {
                    TableInfo tableInfo = new TableInfo();
                    tableInfo.setTableGuid(tableId);
                    tableInfo.setTableName(list.getTableName());
                    tableInfo.setCreateTime(list.getCreateTime());
                    tableInfo.setStatus(list.getStatus());
                    tableInfo.setDatabaseGuid(list.getDatabaseId());
                    tableInfo.setDbName(list.getDatabaseName());
                    tableInfo.setDatabaseStatus(list.getDatabaseStatus());
                    tableDAO.addTable(tableInfo);
                }
            }
            addFullRelation();
        } catch (Exception e) {
            LOG.error("补充元数据失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "补充元数据失败");
        }
    }

    @Transactional
    public void updateEntityInfo(List<AtlasEntity> entities) {
        try {
            Configuration configuration = ApplicationProperties.get();
            Boolean enableEmail = configuration.getBoolean("metaspace.mail.enable", false);
            for (AtlasEntity entity : entities) {
                String typeName = entity.getTypeName();
                if (typeName.equals("hive_table")) {
                    if (entity.getAttribute("temporary") == null || entity.getAttribute("temporary").toString().equals("false")) {
                        TableInfo tableInfo = new TableInfo();
                        tableInfo.setTableGuid(entity.getGuid());
                        tableInfo.setTableName(getEntityAttribute(entity, "name"));
                        AtlasRelatedObjectId relatedDB = getRelatedDB(entity);
                        tableInfo.setDbName(relatedDB.getDisplayText());
                        tableDAO.updateTable(tableInfo);
                        if(enableEmail) {
                            sendMetadataChangedMail(entity.getGuid());
                        }
                    }
                } else if (typeName.equals("hive_column")) {
                    String guid = entity.getGuid();
                    String name = entity.getAttribute("name").toString();
                    String type = entity.getAttribute("type").toString();
                    String status = entity.getStatus().name();
                    columnDAO.updateColumnBasicInfo(guid, name, type, status);
                }
            }
        } catch (Exception e) {
            LOG.error("更新tableinfo表失败", e);
        }
    }

    public AtlasRelatedObjectId getRelatedDB(AtlasEntity entity) {
        AtlasRelatedObjectId objectId = null;
        String store="db";
        if(entity.getTypeName().equals("hbase_table")){
            store="namespace";
        }
        if (entity.hasRelationshipAttribute(store) && Objects.nonNull(entity.getRelationshipAttribute(store))) {
            Object obj = entity.getRelationshipAttribute(store);
            if (obj instanceof AtlasRelatedObjectId) {
                objectId = (AtlasRelatedObjectId) obj;
            }
        }
        return objectId;
    }

    public String getEntityAttribute(AtlasEntity entity, String attributeName) {
        if (entity.hasAttribute(attributeName) && Objects.nonNull(entity.getAttribute(attributeName))) {
            return entity.getAttribute(attributeName).toString();
        } else {
            return null;
        }
    }

    public String getCategoryNameById(String guid) {
        return categoryDao.getCategoryNameById(guid);
    }

    public String getCategoryNameByRelationId(String guid) {
        return categoryDao.getCategoryNameByRelationId(guid);
    }

    public void sendMetadataChangedMail(String tableGuid) throws AtlasBaseException {
        try {
            Table info = tableDAO.getDbAndTableName(tableGuid);
            String sendMessage = "数据库[" + info.getDatabaseName() + "]下的表[" + info.getTableName() + "]元数据发生变更";
            String subject = "元数据变更提醒";
            List<String> emails = userDAO.getUsersEmail(tableGuid);
            sendMail(emails, subject, sendMessage);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("发送邮件失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "发送邮件失败");
        }
    }

    public void sendMail(List<String> toList, String subject, String content) throws AtlasBaseException {
        try {
            StringJoiner mailJoiner = new StringJoiner(",");
            toList.forEach(to -> mailJoiner.add(to));

            Configuration configuration = ApplicationProperties.get();

            String user = configuration.getString("metaspace.mail.user");
            String passwd = configuration.getString("metaspace.mail.password");

            if(user==null || passwd==null) {
                LOG.warn("发件邮箱用户名和密码不能为空");
            }

            Properties props = new Properties();
            Iterator<String> mailKeys = configuration.getKeys("metaspace.mail.service");
            while(mailKeys.hasNext()) {
                String mailKey = mailKeys.next();
                String key = mailKey.replace("metaspace.mail.service.", "");
                String value = configuration.getString(mailKey);
                props.setProperty(key, value);
            }

            Session session = Session.getInstance(props);
            session.setDebug(true);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, mailJoiner.toString());
            message.setSubject(subject);
            message.setText(content);
            message.saveChanges();

            Transport ts = session.getTransport();
            ts.connect(user, passwd);
            ts.sendMessage(message, message.getAllRecipients());
            ts.close();
        } catch (Exception e) {
            LOG.error("获取邮件发送方失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }
    public String dbsToString(List<String> dbs){
        if (dbs==null||dbs.size()==0){
            return "";
        }
        StringBuffer str = new StringBuffer();
        for (String db:dbs){
            str.append("'");
            str.append(db.replaceAll("'", "\\\\'"));
            str.append("'");
            str.append(",");
        }
        str.deleteCharAt(str.length()-1);
        return str.toString();
    }
}
