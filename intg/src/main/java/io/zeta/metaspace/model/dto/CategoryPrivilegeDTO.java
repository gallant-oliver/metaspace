package io.zeta.metaspace.model.dto;

import io.zeta.metaspace.model.result.CategoryPrivilege;
import lombok.Data;

/**
 * @author fanjiajia
 * @Description
 * @date 2021/12/2 14:15
 */
@Data
public class CategoryPrivilegeDTO {

    private String guid;
    private String name;
    private Boolean read;
    private Boolean editCategory;
    private Boolean editItem;
    private CategoryPrivilege.Privilege privilege;

}
