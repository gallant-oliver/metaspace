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

import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.RoleDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.model.metadata.RelationEntityV2;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.RelationDAO;
import org.apache.directory.api.util.Strings;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.zeta.metaspace.model.metadata.CategoryEntity;
import io.zeta.metaspace.model.metadata.RelationQuery;
import io.zeta.metaspace.model.result.PageResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

@Service
public class DataManageService {
    @Autowired
    CategoryDAO categoryDao;
    @Autowired
    RelationDAO relationDao;
    @Autowired
    RoleDAO roleDao;
    @Autowired
    RoleService roleService;

    /**
     * 获取用户有权限的全部目录
     * @param type
     * @return
     * @throws AtlasBaseException
     */
    public List<RoleModulesCategories.Category> getAll(int type) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            Role role = roleDao.getRoleByUsersId(user.getUserId());
            if(role.getStatus() == 0)
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
            String roleId = role.getRoleId();
            Map<String, RoleModulesCategories.Category> userCategorys = roleService.getUserCategory(roleId, type);
            Collection<RoleModulesCategories.Category> valueCollection = userCategorys.values();
            List<RoleModulesCategories.Category> valueList = new ArrayList<>(valueCollection);

            CategoryRelationUtils.cleanInvalidBrother(valueList);


            return valueList;
        } catch (MyBatisSystemException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            throw e;
        }
    }


    /**
     * 创建业务目录
     * @param info
     * @param type
     * @return
     * @throws Exception
     */
    @Transactional
    public CategoryEntityV2 createCategory(CategoryInfoV2 info, Integer type) throws Exception {
        try {
            String currentCategoryGuid = info.getGuid();
            CategoryEntityV2 entity = new CategoryEntityV2();
            StringBuffer qualifiedName = new StringBuffer();
            String newCategoryGuid = UUID.randomUUID().toString();
            String name = info.getName();
            if(Objects.isNull(name)) {
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
            if(Objects.isNull(currentCategoryGuid)) {
                User user = AdminUtils.getUserData();
                Role role = roleDao.getRoleByUsersId(user.getUserId());
                if("1".equals(role.getRoleId())) {
                    throw new AtlasBaseException(AtlasErrorCode.PERMISSION_DENIED, "当前用户没有创建目录权限");
                }
                //qualifiedName
                qualifiedName.append(name);
                entity.setQualifiedName(qualifiedName.toString());
                entity.setLevel(1);
                categoryDao.add(entity);
                return categoryDao.queryByGuid(newCategoryGuid);
            }

            String newCategoryParentGuid = info.getParentCategoryGuid();
            //获取当前catalog
            CategoryEntityV2 currentEntity = categoryDao.queryByGuid(currentCategoryGuid);
            String parentQualifiedName = null;
            String parentGuid = null;
            int currentLevel = categoryDao.getCategoryLevel(currentCategoryGuid);
            //创建子目录
            if( Objects.nonNull(newCategoryParentGuid)) {
                parentGuid = currentCategoryGuid;
                entity.setParentCategoryGuid(currentCategoryGuid);
                parentQualifiedName = currentEntity.getQualifiedName();
                entity.setLevel(currentLevel+1);
            } else {
                //创建同级目录
                parentGuid = currentEntity.getParentCategoryGuid();
                entity.setLevel(currentLevel);
                if(Objects.nonNull(parentGuid)) {
                    entity.setParentCategoryGuid(parentGuid);
                    CategoryEntityV2 currentCatalogParentEntity = categoryDao.queryByGuid(parentGuid);
                    parentQualifiedName = currentCatalogParentEntity.getQualifiedName();
                }
            }
            if(Objects.nonNull(parentQualifiedName) && parentQualifiedName.length() > 0)
                qualifiedName.append(parentQualifiedName + ".");
            qualifiedName.append(name);
            int count = categoryDao.querySameNameNum(name, parentGuid, type);
            if(count > 0)
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在相同的目录名");
            //qualifiedName
            entity.setQualifiedName(qualifiedName.toString());

            //子目录
            if( Objects.nonNull(newCategoryParentGuid)) {
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
            CategoryEntityV2 returnEntity =  categoryDao.queryByGuid(newCategoryGuid);
            returnEntity.setShow(true);
            returnEntity.setStatus(2);
            return returnEntity;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "操作异常");
        }
    }

    /**
     * 删除目录
     * @param guid
     * @return
     * @throws Exception
     */
    @Transactional
    public int deleteCategory(String guid) throws Exception {
        try {
            int childrenNum = categoryDao.queryChildrenNum(guid);
            if(childrenNum > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前目录下存在子目录");
            }
            int relationNum = relationDao.queryRelationNumByCategoryGuid(guid);
            int businessRelationNum = relationDao.queryBusinessRelationNumByCategoryGuid(guid);
            if(relationNum > 0 || businessRelationNum > 0) {
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
            if(role.getStatus() == 0)
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
            roleDao.deleteRole2categoryByUserId(guid);
            return categoryDao.delete(guid);
        } catch (SQLException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "操作异常");
        }
    }

    /**
     * 更新目录
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    public CategoryEntityV2 updateCategory(CategoryInfoV2 info, int type) throws AtlasBaseException {
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
            CategoryEntityV2 returnEntity =  categoryDao.queryByGuid(guid);
            returnEntity.setShow(true);
            returnEntity.setStatus(2);
            return returnEntity;
        } catch (SQLException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        }
    }

    /**
     * 添加关联
     * @param categoryGuid
     * @param relations
     * @throws AtlasBaseException
     */
    @Transactional
    public void assignTablesToCategory(String categoryGuid, List<RelationEntityV2> relations) throws AtlasBaseException {
        try {
            for (RelationEntityV2 relation : relations) {
                relation.setCategoryGuid(categoryGuid);
                addRelation(relation);
            }
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    @Transactional
    public int addRelation(RelationEntityV2 relationEntity) throws AtlasBaseException {
        try {
            String relationshiGuid = UUID.randomUUID().toString();
            relationEntity.setRelationshipGuid(relationshiGuid);

            int count = relationDao.queryTableInfo(relationEntity.getTableGuid());
            if(count == 0) {
                relationDao.addTableInfo(relationEntity);
            }
            return relationDao.add(relationEntity);
        } catch (SQLException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        }
    }

    /**
     * 删除表关联
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "取消关联出错");
        }
    }

    /**
     *
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
            throw e;
        }
    }

    public PageResult<RelationEntityV2> getRelationsByTableName(RelationQuery query, int type) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            Role role = roleDao.getRoleByUsersId(user.getUserId());
            if(role.getStatus() == 0)
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
            String roleId = role.getRoleId();
            String tableName = query.getFilterTableName();
            tableName = (Objects.nonNull(tableName)? tableName: "");
            String tag = query.getTag();
            tag = (Objects.nonNull(tag)? tag: "");
            List<String> categoryIds = CategoryRelationUtils.getPermissionCategoryList(roleId, type);
            int limit = query.getLimit();
            int offset = query.getOffset();
            PageResult<RelationEntityV2> pageResult = new PageResult<>();
            List<RelationEntityV2> list = relationDao.queryByTableName(tableName, tag, categoryIds, limit, offset);
            getPath(list);
            int totalNum = relationDao.queryTotalNumByName(tableName, tag, categoryIds);
            pageResult.setCount(list.size());
            pageResult.setLists(list);
            pageResult.setOffset(query.getOffset());
            pageResult.setSum(totalNum);
            return pageResult;
        } catch (Exception e) {
            throw e;
        }
    }

    public void getPath(List<RelationEntityV2> list) throws AtlasBaseException {
        for (RelationEntityV2 entity : list) {
                StringJoiner joiner = new StringJoiner(("."));
                String path = CategoryRelationUtils.getPath(entity.getCategoryGuid());
                joiner.add(path).add(entity.getTableName());
                entity.setPath(joiner.toString());
            }
    }

    @Transactional
    public void updateStatus(List<AtlasEntity> entities) {
        for (AtlasEntity entity : entities) {
            String guid = entity.getGuid();
            String typeName = entity.getTypeName();
            if(typeName.contains("table"))
                relationDao.updateTableStatus(guid, "DELETED");
        }
    }

    public Set<CategoryEntityV2> getAllDepartments(int type) throws AtlasBaseException {
        try {
            return categoryDao.getAllDepartments(type);
        } catch (SQLException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }
}
