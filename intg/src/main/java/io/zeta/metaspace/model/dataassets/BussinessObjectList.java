package io.zeta.metaspace.model.dataassets;

import io.zeta.metaspace.model.result.PageResult;
import lombok.Data;

/**
 * @author fanjiajia
 * @Description
 * @date 2021/11/15 10:40
 */
@Data
public class BussinessObjectList {
    //主题id
    private String themeId;
    //主题名
    private String themeName;
    //目录路径
    private String path;
    //描述
    private String description;
    //业务对象列表
    private PageResult<BussinessObject> objectPageResult;

}
