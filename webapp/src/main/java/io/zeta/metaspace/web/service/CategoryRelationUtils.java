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
 * @date 2019/3/2 11:21
 */
package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.web.dao.CategoryDAO;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/2 11:21
 */
@Component
public class CategoryRelationUtils {
    private static final Logger LOG = LoggerFactory.getLogger(CategoryRelationUtils.class);
    @Autowired
    CategoryDAO categoryDAO;
    @Autowired
    RoleService roleService;

    private static CategoryRelationUtils utils;

    @PostConstruct
    public void init() {
        utils = this;
    }

    public static List<String> getPermissionCategoryList(String roleId, int categoryType) {
        Map<String, RoleModulesCategories.Category> userCategorys = null;
        List<String> categoryIds = new ArrayList<>();
        if(SystemRole.ADMIN.getCode().equals(roleId)) {
            categoryIds = utils.categoryDAO.getAllCategory(categoryType);

        } else {
            userCategorys = utils.roleService.getUserStringCategoryMap(roleId, categoryType);
            Collection<RoleModulesCategories.Category> valueCollection = userCategorys.values();
            List<RoleModulesCategories.Category> valueList = new ArrayList<>(valueCollection);
            for(RoleModulesCategories.Category category : valueList) {
                if(category.isShow()) {
                    categoryIds.add(category.getGuid());
                }
            }
        }
        return categoryIds;
    }

    public static String getPath(String categoryId) throws AtlasBaseException {
        try {
            String pathStr = utils.categoryDAO.queryPathByGuid(categoryId);
            String path = pathStr.substring(1, pathStr.length()-1);
            path = path.replace(",", "/").replace("\"", "");
            return path;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询路径失败");
        }
    }

    public static void cleanInvalidBrother(List<RoleModulesCategories.Category> valueList) {
        for (RoleModulesCategories.Category currentCategory : valueList) {
            String upBrotherCategoryGuid = currentCategory.getUpBrotherCategoryGuid();
            String downBrotherCategoryGuid = currentCategory.getDownBrotherCategoryGuid();
            boolean hasUpBrother = false;
            boolean hasDownBrother = false;
            for (RoleModulesCategories.Category brotherCategory : valueList) {
                String guid = brotherCategory.getGuid();
                if (Objects.nonNull(upBrotherCategoryGuid)) {
                    if (guid.equals(upBrotherCategoryGuid)) {
                        hasUpBrother = true;
                    }
                }
                if (Objects.nonNull(downBrotherCategoryGuid)) {
                    if (guid.equals(downBrotherCategoryGuid)) {
                        hasDownBrother = true;
                        if (hasUpBrother && hasDownBrother) {
                            break;
                        }
                    }
                }
            }
            if (!hasUpBrother && Objects.nonNull(currentCategory.getUpBrotherCategoryGuid())) {
                currentCategory.setUpBrotherCategoryGuid(null);
            }
            if (!hasDownBrother && Objects.nonNull(currentCategory.getDownBrotherCategoryGuid())) {
                currentCategory.setDownBrotherCategoryGuid(null);
            }

        }
    }
}
