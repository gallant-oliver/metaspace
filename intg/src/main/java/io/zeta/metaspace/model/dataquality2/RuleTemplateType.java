// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
package io.zeta.metaspace.model.dataquality2;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class RuleTemplateType {

    /**
     * 规则模版分类:1-空值校验,2-表体积,3-唯一值校验,4-重复值校验,5-数值型校验
     */
    private String ruleType;
    /**
     * 分类名
     */
    private String name;
    /**
     * 分类所属规则模版数
     */
    private long count;
    /**
     * 统一监控展示名
     */
    private String statisticsDisplayName;

    private static final Map<String, RuleTemplateType> ALL = new LinkedHashMap<String, RuleTemplateType>(){{
        put("rule_1", new RuleTemplateType("rule_1", "表体积", "表体积异常"));
        put("rule_2", new RuleTemplateType("rule_2", "空值校验", "空值异常"));
        put("rule_3", new RuleTemplateType("rule_3", "唯一值校验", "唯一值异常"));
        put("rule_4", new RuleTemplateType("rule_4", "重复值校验", "重复值异常"));
        put("rule_5", new RuleTemplateType("rule_5", "数值型校验", "数据值型异常"));
        put("rule_6", new RuleTemplateType("rule_6", "一致性校验", "一致性异常"));
    }};

    public RuleTemplateType(String ruleType, String name, String statisticsDisplayName) {
        this.ruleType = ruleType;
        this.name = name;
        this.statisticsDisplayName = statisticsDisplayName;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getStatisticsDisplayName() {
        return statisticsDisplayName;
    }

    public void setStatisticsDisplayName(String statisticsDisplayName) {
        this.statisticsDisplayName = statisticsDisplayName;
    }

    public static List<RuleTemplateType> all() {
        return ALL.values().stream().collect(Collectors.toList());
    }

    public static RuleTemplateType getRuleTemplate(String ruleType) {
        return ALL.get(ruleType);
    }

    @Override
    public String toString() {
        return "RuleTemplateType{" +
                "ruleType='" + ruleType + '\'' +
                ", name='" + name + '\'' +
                ", count=" + count +
                ", statisticsDisplayName='" + statisticsDisplayName + '\'' +
                '}';
    }
}
