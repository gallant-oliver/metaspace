package io.zeta.metaspace.model.result;

import lombok.Data;

/**
 * @author fanjiajia
 * @Description
 * @date 2021/10/13 14:18
 */
@Data
public class UserGroupPrivilege {

    private Boolean read;
    private Boolean editCategory;
    private Boolean editItem;
}
