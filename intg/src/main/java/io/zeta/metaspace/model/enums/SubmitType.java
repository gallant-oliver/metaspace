package io.zeta.metaspace.model.enums;

import lombok.Getter;

/**
 * @author : wuqianhe
 * @className : SubmitType
 * @package: io.zeta.metaspace.model.enums
 * @date : 2021/7/19 15:35
 */
@Getter
public enum SubmitType {
    /**
     * 新建
     */
    SUBMIT_ONLY(1),
    /**
     * 新建并发布
     */
    SUBMIT_AND_PUBLISH(2);

    private final int intValue;

    private SubmitType(int intValue) {
        this.intValue = intValue;
    }
}
