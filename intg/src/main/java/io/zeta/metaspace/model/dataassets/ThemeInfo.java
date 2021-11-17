package io.zeta.metaspace.model.dataassets;

import lombok.Data;

/**
 * @author fanjiajia
 * @Description
 * @date 2021/11/11 10:37
 */
@Data
public class ThemeInfo {
    //主题id
    private String themeId;
    //主题名
    private String themeName;
    //业务对象数量
    private Integer bussinessObjectNum;
    //业务对象数量
    private Integer tableNum;
    //租户id
    private String tenantId;
}
