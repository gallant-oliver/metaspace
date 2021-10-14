package io.zeta.metaspace.model.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author : wuqianhe
 * @className : SubmitType
 * @package: io.zeta.metaspace.model.enums
 * @date : 2021/7/19 15:35
 */
@Getter
public enum BusinessType {
    /**
     * 新建
     */

    ATOMIC_INDEX("1","原子指标"),
    DERIVE_INDEX("2","派生指标"),
    COMPOSITE_INDEX("3","复合指标"),
    DATABASE_INFO_REGISTER("4","数据库登记"),
    BUSINESSCATALOGUE_PUBLISH("5","业务目录发布"),
    BUSINESSCATALOGUE_PUBLISH_CLOSE("6","业务目录关闭发布"),
    BUSINESS_OBJECT("7","业务对象");



    private final String typeCode;

    private final String typeText;

    BusinessType(String typeCode, String typeText) {
        this.typeCode = typeCode;
        this.typeText = typeText;
    }

    public static String getTextByCode(String code){
        List<BusinessType> businessTypes=Arrays.stream(BusinessType.values()).filter(b->b.getTypeCode().equals(code)).collect(Collectors.toList());
        if (businessTypes.isEmpty()){
            return null;
        }
        return businessTypes.get(0).getTypeText();
    }
}
