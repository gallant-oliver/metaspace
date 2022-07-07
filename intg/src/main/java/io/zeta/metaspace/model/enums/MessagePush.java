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
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/1/17 14:39
 */
package io.zeta.metaspace.model.enums;

public enum MessagePush {

    // 资源审核信息——推送
    RESOURCE_AUDIT_INFO_DATABASE(0, "资源审核信息", "您有名称为【数据库登记名称】的数据库登记发布待审批" , "数据资产/数据库登记"),
    RESOURCE_AUDIT_INFO_BUSINESS_DIR_RELEASE(0, "资源审核信息", "您有名称为【业务目录名称】的业务目录【发布】待审批" , "数据资产/业务目录"),
    RESOURCE_AUDIT_INFO_BUSINESS_DIR_OFFLINE(0, "资源审核信息", "您有名称为【业务目录名称】的业务目录【下线】待审批" , "数据资产/业务目录"),
    RESOURCE_AUDIT_INFO_BUSINESS_OBJECT_RELEASE(0, "资源审核信息", "您有名称为【业务对象名称】的业务对象【发布】待审批" , "数据资产/业务目录/业务对象"),
    RESOURCE_AUDIT_INFO_BUSINESS_OBJECT_OFFLINE(0, "资源审核信息", "您有名称为【业务对象名称】的业务对象【下线】待审批" , "数据资产/业务目录/业务对象"),
    RESOURCE_AUDIT_INFO_BUSINESS_INDEX_RELEASE(0, "资源审核信息", "您有名称为【业务指标名称】的业务指标【发布】待审批" , "数据资产/业务指标"),
    RESOURCE_AUDIT_INFO_BUSINESS_INDEX_OFFLINE(0, "资源审核信息", "您有名称为【业务指标名称】的业务指标【下线】待审批" , "数据资产/业务指标"),
    RESOURCE_AUDIT_INFO_ATOM_INDEX_RELEASE(0, "资源审核信息", "您有名称为【原子指标名称】的原子指标【发布】待审批" , "数据资产/原子指标"),
    RESOURCE_AUDIT_INFO_ATOM_INDEX_OFFLINE(0, "资源审核信息", "您有名称为【原子指标名称】的原子指标【下线】待审批" , "数据资产/原子指标"),
    RESOURCE_AUDIT_INFO_DERIVATIVE_INDEX_RELEASE(0, "资源审核信息", "您有名称为【衍生表名称】的衍生指标【发布】待审批" , "数据资产/业务指标/衍生表指标"),
    RESOURCE_AUDIT_INFO_DERIVATIVE_INDEX_OFFLINE(0, "资源审核信息", "您有名称为【衍生表名称】的衍生指标【下线】待审批" , "数据资产/业务指标/衍生表指标"),
    RESOURCE_AUDIT_INFO_REVIEW_INDEX_RELEASE(0, "资源审核信息", "您有名称为【复合指标名称】的复合指标【发布】待审批" , "数据资产/业务指标/复合指标"),
    RESOURCE_AUDIT_INFO_REVIEW_INDEX_OFFLINE(0, "资源审核信息", "您有名称为【复合指标名称】的复合指标【下线】待审批" , "数据资产/业务指标/复合指标"),
    RESOURCE_AUDIT_INFO_INDEX_DIR_RELEASE(0, "资源审核信息", "您有名称为【指标目录名称】的指标目录【发布】待审批" , "数据资产/指标目录"),
    RESOURCE_AUDIT_INFO_INDEX_DIR_OFFLINE(0, "资源审核信息", "您有名称为【指标目录名称】的指标目录【下线】待审批" , "数据资产/指标目录"),



    USER_GROUP_INFO_(0, "资源审核信息", "您有名称为【数据库登记名称】的数据库登记发布待审批" , "数据资产/数据库登记");
    /**
     * 消息类型数字编号
     */
    public int type;
    /**
     * 消息类型名称
     */
    public String typeCn;
    /**
     * 标题内容
     */
    public String name;
    /**
     * 所属模块
     */
    public String module;

    MessagePush(int type, String typeCn, String name, String module) {
        this.type = type;
        this.typeCn = typeCn;
        this.name = name;
        this.module = module;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTypeCn() {
        return typeCn;
    }

    public void setTypeCn(String typeCn) {
        this.typeCn = typeCn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }
}
