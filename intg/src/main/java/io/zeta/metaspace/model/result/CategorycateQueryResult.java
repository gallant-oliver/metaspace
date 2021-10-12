package io.zeta.metaspace.model.result;

import lombok.Data;

/**
 * @author fanjiajia
 * @Description
 * @date 2021/10/11 12:21
 */
@Data
public class CategorycateQueryResult {
    private String guid;
    private String name;
    private String parentCategoryGuid;
    private String upBrotherCategoryGuid;
    private String downBrotherCategoryGuid;
    private String description;
    private int level;
    private String code;
    private int sort;
    private String status;
    private String privateStatus;
    private Boolean publish;
    private CategoryPrivilege.Privilege privilege;
    private Boolean read;
    private Boolean editCategory;
    private Boolean editItem;
    private String creator;
}
