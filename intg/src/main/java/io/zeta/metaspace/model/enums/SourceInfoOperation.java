package io.zeta.metaspace.model.enums;

import lombok.Getter;

/**
 * @author : wuqianhe
 * @className : SubmitType
 * @package: io.zeta.metaspace.model.enums
 * @date : 2021/7/19 15:35
 */
@Getter
public enum SourceInfoOperation {
    /**
     * 发布
     */
    PUBLISH(0),
    /**
     * 删除
     */
    DELETE(1),
    /**
     * 新建
     */
    CREATE(2),
    /**
     * 编辑
     */
    UPDATE(3),
    /**
     * 撤销
     */
    REVOKE(4);

    private final int intValue;

    private SourceInfoOperation(int intValue) {
        this.intValue = intValue;
    }
}
