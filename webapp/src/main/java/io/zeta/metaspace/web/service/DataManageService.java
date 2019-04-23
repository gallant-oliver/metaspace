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

import com.google.gson.Gson;
import io.zeta.metaspace.SSOConfig;
import io.zeta.metaspace.discovery.MetaspaceGremlinQueryService;
import io.zeta.metaspace.model.metadata.CategoryEntity;
import io.zeta.metaspace.model.metadata.RelationQuery;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.TableOwner;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.pojo.TableRelation;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.privilege.SystemModule;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserInfo;
import io.zeta.metaspace.utils.SSLClient;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.DateUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.directory.api.util.Strings;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    MetaspaceGremlinQueryService metaspaceEntityService;
    @Autowired
    UserDAO userDAO;

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
            List<CategoryPrivilege> valueList = roleService.getUserCategory2(roleId, type);
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
            if (Objects.isNull(currentCategoryGuid)) {
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
                CategoryPrivilege returnEntity = categoryDao.queryByGuidV2(newCategoryGuid);
                CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege(false,false,true,true,true,true,true,true,true);
                if(type==0){
                    privilege.setDeleteRelation(false);
                }
                returnEntity.setPrivilege(privilege);
                return returnEntity;
            }

            String newCategoryParentGuid = info.getParentCategoryGuid();
            //获取当前catalog
            CategoryEntityV2 currentEntity = categoryDao.queryByGuid(currentCategoryGuid);
            String parentQualifiedName = null;
            String parentGuid = null;
            int currentLevel = categoryDao.getCategoryLevel(currentCategoryGuid);
            //创建子目录
            if (Objects.nonNull(newCategoryParentGuid)) {
                parentGuid = currentCategoryGuid;
                entity.setParentCategoryGuid(currentCategoryGuid);
                parentQualifiedName = currentEntity.getQualifiedName();
                entity.setLevel(currentLevel + 1);
            } else {
                //创建同级目录
                parentGuid = currentEntity.getParentCategoryGuid();
                entity.setLevel(currentLevel);
                if (Objects.nonNull(parentGuid)) {
                    entity.setParentCategoryGuid(parentGuid);
                    CategoryEntityV2 currentCatalogParentEntity = categoryDao.queryByGuid(parentGuid);
                    parentQualifiedName = currentCatalogParentEntity.getQualifiedName();
                }
            }
            if (Objects.nonNull(parentQualifiedName) && parentQualifiedName.length() > 0)
                qualifiedName.append(parentQualifiedName + ".");
            qualifiedName.append(name);
            int count = categoryDao.querySameNameNum(name, parentGuid, type);
            if (count > 0)
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在相同的目录名");
            //qualifiedName
            entity.setQualifiedName(qualifiedName.toString());

            //子目录
            if (Objects.nonNull(newCategoryParentGuid)) {
                String lastChildGuid = categoryDao.queryLastChildCategory(currentCategoryGuid);
                if (Objects.nonNull(lastChildGuid)) {
                    entity.setUpBrotherCategoryGuid(lastChildGuid);
                    categoryDao.updateDownBrotherCategoryGuid(lastChildGuid, newCategoryGuid);
                }
            } else {
                //同级目录
                if (Objects.nonNull(currentCategoryGuid) && Strings.equals(info.getDirection(), "up")) {
                    entity.setDownBrotherCategoryGuid(currentCategoryGuid);
                    String upBrotherGuid = currentEntity.getUpBrotherCategoryGuid();
                    if (Objects.nonNull(upBrotherGuid)) {
                        entity.setUpBrotherCategoryGuid(upBrotherGuid);
                        categoryDao.updateDownBrotherCategoryGuid(upBrotherGuid, newCategoryGuid);
                    }
                    categoryDao.updateUpBrotherCategoryGuid(currentCategoryGuid, newCategoryGuid);
                } else if (Objects.nonNull(currentCategoryGuid) && Strings.equals(info.getDirection(), "down")) {
                    entity.setUpBrotherCategoryGuid(info.getGuid());
                    String downBrotherGuid = currentEntity.getDownBrotherCategoryGuid();
                    if (Objects.nonNull(downBrotherGuid)) {
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
                if(modules.contains(SystemModule.TECHNICAL_EDIT.getCode())) {
                    privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true);
                }else{
                    privilege =new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, false, true);
                }
                }else{
                if(modules.contains(SystemModule.BUSINESSE_EDIT.getCode())) {
                    privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true);
                }else{
                    privilege =new CategoryPrivilege.Privilege(false, false, true, true, false, true, true, false, true);
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
            if (Objects.nonNull(upBrotherCategoryGuid)) {
                categoryDao.updateDownBrotherCategoryGuid(upBrotherCategoryGuid, downBrotherCategoryGuid);
            }
            if (Objects.nonNull(downBrotherCategoryGuid)) {
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
            if (Objects.nonNull(currentEntity.getParentCategoryGuid()))
                parentQualifiedName = categoryDao.queryQualifiedName(currentEntity.getParentCategoryGuid());
            if (Objects.nonNull(parentQualifiedName))
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
                String topGuid = relationDao.getTopGuidByGuid(categoryGuid);
                relationDao.deleteByTableGuid(topGuid,relation.getTableGuid());
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

    /**
     * 删除表关联
     *
     * @param relationshipList
     * @throws AtlasBaseException
     */
    @Transactional
    public void removeRelationAssignmentFromTables(List<RelationEntityV2> relationshipList) throws AtlasBaseException {
        try {
            if (Objects.nonNull(relationshipList)) {
                for (RelationEntityV2 relationship : relationshipList) {
                    String relationShipGuid = relationship.getRelationshipGuid();
                    RelationEntityV2 entity = categoryDao.getRelationByGuid(relationShipGuid);
                    String categoryGuid = entity.getCategoryGuid();
                    String tableGuid = entity.getTableGuid();
                    List<String> childrenCategoryList = categoryDao.queryChildrenCategoryId(categoryGuid);
                    childrenCategoryList.add(categoryGuid);
                    categoryDao.deleteChildrenRelation(tableGuid, childrenCategoryList);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "取消关联出错");
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
            totalNum = relationDao.queryTotalNumByCategoryGuid(categoryGuid);
            getPath(relations);
            pageResult.setCount(relations.size());
            pageResult.setLists(relations);
            pageResult.setOffset(query.getOffset());
            pageResult.setSum(totalNum);
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
            tableName = tableName.replaceAll("%", "/%").replaceAll("_", "/_");
            tag = tag.replaceAll("%", "/%").replaceAll("_", "/_");
            List<RelationEntityV2> list = relationDao.queryByTableName(tableName, tag, categoryIds, limit, offset);

            getPath(list);
            int totalNum = relationDao.queryTotalNumByName(tableName, tag, categoryIds);
            pageResult.setCount(list.size());
            pageResult.setLists(list);
            pageResult.setOffset(query.getOffset());
            pageResult.setSum(totalNum);
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
            StringJoiner joiner = new StringJoiner(("."));
            String path = CategoryRelationUtils.getPath(entity.getCategoryGuid());
            //joiner.add(path).add(entity.getTableName());
            joiner.add(path);
            entity.setPath(joiner.toString());
        }
    }

    @Transactional
    public void updateStatus(List<AtlasEntity> entities) {
        for (AtlasEntity entity : entities) {
            String guid = entity.getGuid();
            String typeName = entity.getTypeName();
            if (typeName.contains("table"))
                relationDao.updateTableStatus(guid, "DELETED");
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

    public int addTableOwner(TableOwner tableOwner) throws AtlasBaseException {
        try {
            return categoryDao.addTableOwners(tableOwner);
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "SQL 异常");
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加失败");
        }
    }

    public List getOrganization() {
        String organizationURL = SSOConfig.getOrganizationURL();
        HashMap<String, String> header = new HashMap<>();
        String session = SSLClient.doGet(organizationURL, header);
        Gson gson = new Gson();
        Map body = gson.fromJson(session, Map.class);
        List data = (List) body.get("data");
        return data;
    }


    /*public PageResult<TableInfo> getTableByDBWithQuery(String databaseId, String query, long offset, long limit) throws AtlasBaseException {
        try {

        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }*/


    @Transactional
    public void addTable(List<AtlasEntity> entities) {
        //添加到tableinfo
        for (AtlasEntity entity : entities) {
            String typeName = entity.getTypeName();
            if (typeName.contains("table")) {
                String guid = entity.getGuid();
                if (tableDAO.ifTableExists(guid).size() == 0) {
                    TableInfo tableInfo = new TableInfo();
                    tableInfo.setTableGuid(guid);
                    tableInfo.setTableName(getEntityAttribute(entity, "name"));
                    Object createTime = entity.getAttribute("createTime");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formatDateStr = sdf.format(createTime);
                    tableInfo.setCreateTime(formatDateStr);
                    tableInfo.setStatus(entity.getStatus().name());
                    AtlasRelatedObjectId relatedDB = getRelatedDB(entity);
                    tableInfo.setDatabaseGuid(relatedDB.getGuid());
                    tableInfo.setDbName(relatedDB.getDisplayText());
                    tableDAO.addTable(tableInfo);
                }
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
            if (tableDAO.ifTableExists(tableId).size() == 0) {
                TableInfo tableInfo = new TableInfo();
                tableInfo.setTableGuid(tableId);
                tableInfo.setTableName(list.getTableName());
                tableInfo.setCreateTime(list.getCreateTime());
                tableInfo.setStatus(list.getStatus());
                tableInfo.setDatabaseGuid(list.getDatabaseId());
                tableInfo.setDbName(list.getDatabaseName());
                tableDAO.addTable(tableInfo);
            }
        }
        addFullRelation();
    }

    @Transactional
    public void updateTable(List<AtlasEntity> entities) {
        for (AtlasEntity entity : entities) {
            String typeName = entity.getTypeName();
            if (typeName.contains("table")) {
                TableInfo tableInfo = new TableInfo();
                tableInfo.setTableGuid(entity.getGuid());
                tableInfo.setTableName(getEntityAttribute(entity, "name"));
                AtlasRelatedObjectId relatedDB = getRelatedDB(entity);
                tableInfo.setDbName(relatedDB.getDisplayText());
                tableDAO.updateTable(tableInfo);
            }
        }
    }


    public AtlasRelatedObjectId getRelatedDB(AtlasEntity entity) {
        AtlasRelatedObjectId objectId = null;
        if (entity.hasRelationshipAttribute("db") && Objects.nonNull(entity.getRelationshipAttribute("db"))) {
            Object obj = entity.getRelationshipAttribute("db");
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
}
