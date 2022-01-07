package io.zeta.metaspace.model.sourceinfo.derivetable.pojo;

import lombok.Data;

/**
 * column_tag_relation_to_column表实体类
 * @author ..
 */
@Data
public class ColumnTagRelationToColumn {
    private String id;
    private String columnId;
    private String tagId;
}
