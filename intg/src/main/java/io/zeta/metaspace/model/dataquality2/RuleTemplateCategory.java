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

import com.google.common.collect.Lists;

import java.util.List;


public class RuleTemplateCategory {

    /**
     * 规则模版分类:1-空值校验,2-表体积,3-唯一值校验,4-重复值校验,5-数值型校验
     */
    private String categoryId;
    /**
     * 分类名
     */
    private String name;
    /**
     * 分类所属规则模版数
     */
    private long count;

    public RuleTemplateCategory(String categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
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

    public static List<RuleTemplateCategory> all() {
        return Lists.newArrayList(new RuleTemplateCategory("1", "表体积"),
                                  new RuleTemplateCategory("2", "空值校验"),
                                  new RuleTemplateCategory("3", "唯一值校验"),
                                  new RuleTemplateCategory("4", "重复值校验"),
                                  new RuleTemplateCategory("5", "数值型校验"));
    }
}
