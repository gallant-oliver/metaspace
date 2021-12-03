package io.zeta.metaspace.model.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
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

    ATOMIC_INDEX("1","原子指标", SourceSystem.INDICATORS_LIB),
    DERIVE_INDEX("2","衍生指标", SourceSystem.INDICATORS_LIB),
    COMPOSITE_INDEX("3","复合指标", SourceSystem.INDICATORS_LIB),
    TIME_LIMIT("8","时间限定", SourceSystem.INDICATORS_LIB),
    BUSINESS_INDEX("9","业务指标", SourceSystem.INDICATORS_LIB),
    DATABASE_INFO_REGISTER("4","数据库登记", SourceSystem.METASPACE),
    BUSINESSCATALOGUE("5","业务目录", SourceSystem.METASPACE),
    BUSINESS_OBJECT("6","业务对象", SourceSystem.METASPACE),
    INDEXCATALOGUE("7","指标目录", SourceSystem.INDICATORS_LIB);



    private final String typeCode;

    private final String typeText;

    private final SourceSystem sourceSystem;

    BusinessType(String typeCode, String typeText, SourceSystem sourceSystem) {
        this.typeCode = typeCode;
        this.typeText = typeText;
        this.sourceSystem = sourceSystem;
    }

    public static String getTextByCode(String code){
        List<BusinessType> businessTypes=Arrays.stream(BusinessType.values()).filter(b->b.getTypeCode().equals(code)).collect(Collectors.toList());
        if (businessTypes.isEmpty()){
            return null;
        }
        return businessTypes.get(0).getTypeText();
    }
    public static SourceSystem getSystem(String code){
        List<BusinessType> businessTypes=Arrays.stream(BusinessType.values()).filter(b->b.getTypeCode().equals(code)).collect(Collectors.toList());
        if (businessTypes.isEmpty()){
            return null;
        }
        return businessTypes.get(0).getSourceSystem();
    }
}
