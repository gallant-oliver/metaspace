package io.zeta.metaspace.model.sourceinfo.derivetable.vo;

import lombok.Data;

/**
 * @ClassName SourceColumnEntity
 * @Descroption TODO
 * @Author Lvmengliang
 * @Date 2021/7/15 18:25
 * @Version 1.0
 */
@Data
public class SourceColumnEntity {

    private String sourceColumnGuid;

    private String sourceColumnNameEn;

    private String sourceColumnNameZh;

    private String sourceColumnType;

    private String sourceTableGuid;

    private String sourceTableNameEn;

    private String sourceTableNameZh;

    private String dataBaseName;

}
