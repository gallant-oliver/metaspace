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
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.pojo.TableRelation;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.privilege.SystemModule;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.share.APIDataOwner;
import io.zeta.metaspace.model.share.Organization;
import io.zeta.metaspace.model.table.Tag;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.utils.OKHttpClient;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.ColumnDAO;
import io.zeta.metaspace.web.dao.DataShareDAO;
import io.zeta.metaspace.web.dao.OrganizationDAO;
import io.zeta.metaspace.web.dao.RelationDAO;
import io.zeta.metaspace.web.dao.RoleDAO;
import io.zeta.metaspace.web.dao.TableDAO;
import io.zeta.metaspace.web.dao.TableTagDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.DateUtils;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
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

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private static final String ORGANIZATION_FIRST_PID = "sso.organization.first.pid";

    /**
     * 获取用户有权限的全部目录
     *
     * @param type
     * @return
     * @throws AtlasBaseException
     */
    public List<CategoryPrivilege> getAll(int type) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            Role role = roleDao.getRoleByUsersId(user.getUserId());
            if (role.getStatus() == 0)
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
            String roleId = role.getRoleId();
            List<CategoryPrivilege> valueList = roleService.getUserCategory(roleId, type);
            return valueList;
        } catch (MyBatisSystemException e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
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
    public CategoryPrivilege createCategory(CategoryInfoV2 info, Integer type) throws Exception {
        try {
            String currentCategoryGuid = info.getGuid();
            CategoryEntityV2 entity = new CategoryEntityV2();
            StringBuffer qualifiedName = new StringBuffer();
            String newCategoryGuid = UUID.randomUUID().toString();
            String name = info.getName();
            if (Objects.isNull(name)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录名不能为空");
            }
            //guid
            entity.setGuid(newCategoryGuid);
            //name
            entity.setName(name);
            //description
            entity.setDescription(info.getDescription());
            entity.setCategoryType(type);

            //创建第一个目录
            if (StringUtils.isEmpty(currentCategoryGuid)) {
                if(categoryDao.ifExistCategory(type) > 0) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在首个目录，请刷新后重新操作");
                }
                User user = AdminUtils.getUserData();
                Role role = roleDao.getRoleByUsersId(user.getUserId());
                if (!"1".equals(role.getRoleId())) {
                    throw new AtlasBaseException(AtlasErrorCode.PERMISSION_DENIED, "当前用户没有创建目录权限");
                }
                //qualifiedName
                qualifiedName.append(name);
                entity.setQualifiedName(qualifiedName.toString());
                entity.setLevel(1);
                categoryDao.add(entity);
                CategoryPrivilege returnEntity = new CategoryPrivilege();
                returnEntity.setGuid(newCategoryGuid);
                returnEntity.setName(name);
                returnEntity.setDescription(info.getDescription());
                returnEntity.setLevel(1);
                returnEntity.setParentCategoryGuid(null);
                returnEntity.setUpBrotherCategoryGuid(null);
                returnEntity.setDownBrotherCategoryGuid(null);
                CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege(false,false,true,true,true,true,true,true,true);
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
            int count = categoryDao.querySameNameNum(name, parentGuid, type);
            if (count > 0)
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在相同的目录名");
            //qualifiedName
            entity.setQualifiedName(qualifiedName.toString());

            //子目录
            if (StringUtils.isNotEmpty(newCategoryParentGuid)) {
                String lastChildGuid = categoryDao.queryLastChildCategory(currentCategoryGuid);
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
            categoryDao.add(entity);
            CategoryPrivilege returnEntity = categoryDao.queryByGuidV2(newCategoryGuid);
            User user = AdminUtils.getUserData();
            List<Module> moduleByUserId = userDAO.getModuleByUserId(user.getUserId());
            List<Integer> modules = new ArrayList<>();
            for (Module module : moduleByUserId) {
                modules.add(module.getModuleId());
            }
            CategoryPrivilege.Privilege privilege =null;
            if(type==0) {
                if(modules.contains(SystemModule.TECHNICAL_OPERATE.getCode())) {
                    privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true);
                }else{
                    privilege =new CategoryPrivilege.Privilege(false, false, true, true, false, true, false, false, true);
                }
                }else{
                if(modules.contains(SystemModule.BUSINESSE_OPERATE.getCode())) {
                    privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true);
                }else{
                    privilege =new CategoryPrivilege.Privilege(false, false, true, true, false, true, false, false, true);
                }
            }
            returnEntity.setPrivilege(privilege);
            return returnEntity;
        } catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "操作异常");
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
    public int deleteCategory(String guid) throws Exception {
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
            User user = AdminUtils.getUserData();
            Role role = roleDao.getRoleByUsersId(user.getUserId());
            if (role.getStatus() == 0)
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
            roleDao.deleteRole2categoryByUserId(guid);
            return categoryDao.delete(guid);
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "操作异常");
        }
    }

    /**
     * 更新目录
     *
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    public String updateCategory(CategoryInfoV2 info, int type) throws AtlasBaseException {
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
            int count = categoryDao.querySameNameNum(name, currentEntity.getParentCategoryGuid(), type);
            if (count > 0 && !currentEntity.getName().equals(info.getName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在相同的目录名");
            }
            CategoryEntity entity = new CategoryEntity();
            entity.setGuid(info.getGuid());
            entity.setName(info.getName());
            entity.setQualifiedName(qualifiedName.toString());
            entity.setDescription(info.getDescription());
            categoryDao.updateCategoryInfo(entity);
            return "success";
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
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
            LOG.error(e.getMessage());
            throw e;
        }
    }

    @Transactional
    public int addRelation(RelationEntityV2 relationEntity) throws AtlasBaseException {
        try {
            String relationshiGuid = UUID.randomUUID().toString();
            relationEntity.setRelationshipGuid(relationshiGuid);
            return relationDao.add(relationEntity);
        } catch (SQLException e) {
            LOG.error(e.getMessage());
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
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "取消关联出错");
        }
    }

    /**
     * @param categoryGuid
     * @param query
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<RelationEntityV2> getRelationsByCategoryGuid(String categoryGuid, RelationQuery query) throws AtlasBaseException {
        try {
            int limit = query.getLimit();
            int offset = query.getOffset();
            PageResult<RelationEntityV2> pageResult = new PageResult<>();
            List<RelationEntityV2> relations = null;
            int totalNum = 0;
            relations = relationDao.queryRelationByCategoryGuid(categoryGuid, limit, offset);
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
            LOG.error(e.getMessage());
            throw e;
        }
    }
    /**
     * @param categoryGuid
     * @param query
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<RelationEntityV2> getRelationsByCategoryGuidFilter(String categoryGuid, RelationQuery query) throws AtlasBaseException {
        try {
            int limit = query.getLimit();
            int offset = query.getOffset();
            PageResult<RelationEntityV2> pageResult = new PageResult<>();
            List<RelationEntityV2> relations = null;
            int totalNum = 0;
            relations = relationDao.queryRelationByCategoryGuidFilter(categoryGuid, limit, offset);
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
            LOG.error(e.getMessage());
            throw e;
        }
    }
    public PageResult<RelationEntityV2> getRelationsByTableName(RelationQuery query, int type) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            Role role = roleDao.getRoleByUsersId(user.getUserId());
            if (role.getStatus() == 0)
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
            String roleId = role.getRoleId();
            String tableName = query.getFilterTableName();
            String tag = query.getTag();
            List<String> categoryIds = CategoryRelationUtils.getPermissionCategoryList(roleId, type);
            int limit = query.getLimit();
            int offset = query.getOffset();
            PageResult<RelationEntityV2> pageResult = new PageResult<>();
            if(StringUtils.isNotEmpty(tableName))
                tableName = tableName.replaceAll("%", "/%").replaceAll("_", "/_");
            if(StringUtils.isNotEmpty(tag))
                tag = tag.replaceAll("%", "/%").replaceAll("_", "/_");
            List<RelationEntityV2> list = relationDao.queryByTableName(tableName, tag, categoryIds, limit, offset);
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
            //long totalNum = relationDao.queryTotalNumByName(tableName, tag, categoryIds);
            long totalNum = 0;
            if (list.size()!=0){
                totalNum = list.get(0).getTotal();
            }
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            //pageResult.setOffset(query.getOffset());
            pageResult.setTotalSize(totalNum);
            return pageResult;
        } catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw e;
        }
    }
    public PageResult<RelationEntityV2> getRelationsByTableNameFilter(RelationQuery query, int type) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            Role role = roleDao.getRoleByUsersId(user.getUserId());
            if (role.getStatus() == 0)
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
            String roleId = role.getRoleId();
            String tableName = query.getFilterTableName();
            String tag = query.getTag();
            List<String> categoryIds = CategoryRelationUtils.getPermissionCategoryList(roleId, type);
            int limit = query.getLimit();
            int offset = query.getOffset();
            PageResult<RelationEntityV2> pageResult = new PageResult<>();
            if(StringUtils.isNotEmpty(tableName))
                tableName = tableName.replaceAll("%", "/%").replaceAll("_", "/_");
            if(StringUtils.isNotEmpty(tag))
                tag = tag.replaceAll("%", "/%").replaceAll("_", "/_");
            List<RelationEntityV2> list = relationDao.queryByTableNameFilter(tableName, tag, categoryIds, limit, offset);

            getPath(list);
            //long totalNum = relationDao.queryTotalNumByNameFilter(tableName, tag, categoryIds);
            long totalNum = 0;
            if (list.size()!=0){
                totalNum=list.get(0).getTotal();
            }
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            //pageResult.setOffset(query.getOffset());
            pageResult.setTotalSize(totalNum);
            return pageResult;
        } catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw e;
        }
    }
    public void getPath(List<RelationEntityV2> list) throws AtlasBaseException {
        for (RelationEntityV2 entity : list) {
            String path = CategoryRelationUtils.getPath(entity.getCategoryGuid());
            //joiner.add(path).add(entity.getTableName());
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

    public Set<CategoryEntityV2> getAllDepartments(int type) throws AtlasBaseException {
        try {
            return categoryDao.getAllDepartments(type);
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }

    @Transactional
    public int addTableOwner(TableOwner tableOwner) throws AtlasBaseException {
        try {
            Configuration configuration = ApplicationProperties.get();
            String mobiusURL = configuration.getString(METASPACE_MOBIUS_ADDRESS)  + "/reviews/user";
            List<String> tableList = tableOwner.getTables();
            APIDataOwner dataOwner = new APIDataOwner();
            //api
            List<String> apiList = shareDAO.getAPIByRelatedTable(tableList);
            List<TableOwner.Owner> tableOwners = tableOwner.getOwners();
            if(Objects.nonNull(apiList) && apiList.size()>0) {
                dataOwner.setApi_id_list(apiList);
                //organization
                List<APIDataOwner.Organization> organizations = new ArrayList<>();
                for (TableOwner.Owner owner : tableOwners) {
                    APIDataOwner.Organization organization = new APIDataOwner.Organization();
                    organization.setOrganization(owner.getPkid());
                    organization.setOrganization_type(owner.getType());
                    organizations.add(organization);
                }
                //owner
                dataOwner.setOrganization_list(organizations);
                List<String> api_owner = new ArrayList<>();
                dataOwner.setApi_owner(api_owner);
                Gson gson = new Gson();
                String jsonStr = gson.toJson(dataOwner, APIDataOwner.class);
                //向云平台发请求

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
            LOG.error(e.toString());
            throw e;
        } catch (Exception e) {
            LOG.error(e.toString());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加失败");
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
            //long totalSize = organizationDAO.countOrganizationByPid(pId, query);
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

            //long totalSize = organizationDAO.countOrganizationByName(query);
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新失败");
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
    public void addEntity(List<AtlasEntity> entities) {
        //添加到tableinfo
        for (AtlasEntity entity : entities) {
            String typeName = entity.getTypeName();
            if (("hive_table").equals(typeName)) {
                if(entity.getAttribute("temporary")==null||entity.getAttribute("temporary").toString().equals("false")){
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
            } else if(("hive_column").equals(typeName)) {
                AtlasRelatedObjectId table = (AtlasRelatedObjectId)entity.getRelationshipAttribute("table");
                String tableGuid = table.getGuid();
                String guid = entity.getGuid();
                String name = entity.getAttribute("name").toString();
                String type = entity.getAttribute("type").toString();
                String status = entity.getStatus().name();
                String updateTime = entity.getUpdateTime().toString();
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
    public void supplementTable() throws AtlasBaseException {
        PageResult<Table> tableNameAndDbNameByQuery = metaspaceEntityService.getTableNameAndDbNameByQuery("", 0, -1);
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
    }

    @Transactional
    public void updateEntityInfo(List<AtlasEntity> entities) {
        for (AtlasEntity entity : entities) {
            String typeName = entity.getTypeName();
            if (("hive_table").equals(typeName)) {
                if(entity.getAttribute("temporary")==null||entity.getAttribute("temporary").toString().equals("false")) {
                    TableInfo tableInfo = new TableInfo();
                    tableInfo.setTableGuid(entity.getGuid());
                    tableInfo.setTableName(getEntityAttribute(entity, "name"));
                    AtlasRelatedObjectId relatedDB = getRelatedDB(entity);
                    tableInfo.setDbName(relatedDB.getDisplayText());
                    tableDAO.updateTable(tableInfo);
                }
            } else if(("hive_column").equals(typeName)) {
                String guid = entity.getGuid();
                String name = entity.getAttribute("name").toString();
                String type = entity.getAttribute("type").toString();
                String status = entity.getStatus().name();
                columnDAO.updateColumnBasicInfo(guid, name, type, status);
            }
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
}
