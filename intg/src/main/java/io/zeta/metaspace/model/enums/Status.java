package io.zeta.metaspace.model.enums;

import lombok.Getter;

/**
 * @author : wuqianhe
 * @className : SubmitType
 * @package: io.zeta.metaspace.model.enums
 * @date : 2021/7/19 15:35
 */
@Getter
public enum Status {
    /**
     * 待发布
     */
    FOUNDED(0),
    /**
     * 待审批
     */
    AUDITING(1),
    /**
     * 审核不通过
     */
    REJECT(2),
    /**
     * 审核通过
     */
    ACTIVE(3);

    private final int intValue;

    private Status(int intValue) {
        this.intValue = intValue;
    }

    public static String getStatusByValue(String intValue){
        switch (intValue){
            case "0":
                return Status.FOUNDED.name();
            case "1":
                return Status.AUDITING.name();
            case "2":
                return Status.REJECT.name();
            case "3":
                return Status.ACTIVE.name();
            default:
                return Status.FOUNDED.name();
        }
    }
}
