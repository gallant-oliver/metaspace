package io.zeta.metaspace.model.sourceinfo.derivetable.vo;

import lombok.Data;

/**
 * @ClassName CategoryGuidPath
 * @Descroption TODO
 * @Author Lvmengliang
 * @Date 2021/8/5 18:52
 * @Version 1.0
 */
@Data
public class CategoryGuidPath {

    private String guid;

    private String name;

    private String parentCategoryGuid;

    private String path;
}
