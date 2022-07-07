package io.zeta.metaspace.model.sourceinfo.derivetable.vo;

import lombok.Data;

import java.util.List;

/**
 * 衍生表差异对比
 *
 * @author w
 */
@Data
public class SourceInfoDeriveTableColumnContrastVO {
    /**
     * 当前字段
     */
    private List<DeriveTableColumnContrastVO> currentMetadata;
    /**
     * 变更后字段
     */
    private List<DeriveTableColumnContrastVO> oldMetadata;
}
