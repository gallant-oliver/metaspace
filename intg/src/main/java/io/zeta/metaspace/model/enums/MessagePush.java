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

import java.text.MessageFormat;
import java.util.Arrays;

public enum MessagePush {

    // 资源审核信息——推送
    RESOURCE_AUDIT_INFO_DATABASE(0, "资源审核信息", "您有名称为【%s】的数据库登记发布待审批", "数据资产/数据库登记"),
    RESOURCE_AUDIT_INFO_BUSINESS_DIR(0, "资源审核信息", "您有名称为【%s】的业务目录【%s】待审批", "数据资产/业务目录"),
    RESOURCE_AUDIT_INFO_BUSINESS_OBJECT(0, "资源审核信息", "您有名称为【%s】的业务对象【%s】待审批", "数据资产/业务目录/业务对象"),
    RESOURCE_AUDIT_INFO_INDEX_DESIGN(0, "资源审核信息", "您有名称为【%s】的【%s】-【%s】审批【%s】", "数据资产/%s"),
    RESOURCE_AUDIT_INFO_INDEX_DESIGN_CANCEL(0, "资源审核信息", "您有名称为【%s】的【%s】-【%s】已撤回申请", "数据资产/%s"),

    //用户组管理
    USER_GROUP_USER_MEMBER_ADD(1,"用户组信息","您已经被添加到【%s】用户组中","系统管理/用户组管理"),
    USER_GROUP_USER_MEMBER_REMOVE(1,"用户组信息","您已经被用户组【%s】移除","系统管理/用户组管理"),
    USER_GROUP_PERMISSION_TECHNICAL_CATEGORY_REMOVE(1,"用户组信息","您所在的用户组【%s】用户组的【%s】技术目录权限被移除","系统管理/用户组管理"),
    USER_GROUP_PERMISSION_TECHNICAL_CATEGORY_CHANGE(1,"用户组信息","您所在的用户组【%s】用户组的【%s】技术目录发生权限变更","系统管理/用户组管理"),
    USER_GROUP_PERMISSION_BUSINESS_CATEGORY_REMOVE(1,"用户组信息","您所在的用户组【%s】用户组的【%s】业务目录权限被移除","系统管理/用户组管理"),
    USER_GROUP_PERMISSION_BUSINESS_CATEGORY_CHANGE(1,"用户组信息","您所在的用户组【%s】用户组的【%s】业务目录发生权限变更","系统管理/用户组管理"),
    USER_GROUP_PERMISSION_DATA_SOURCE_ADD(1,"用户组信息","您所在的用户组【%s】新增了【%s】数据源权限","系统管理/用户组管理"),
    USER_GROUP_PERMISSION_DATA_SOURCE_REMOVE(1,"用户组信息","您所在的用户组【%s】移除了【%s】数据源权限","系统管理/用户组管理"),
    USER_GROUP_PERMISSION_DATA_SOURCE_CHANGE(1,"用户组信息","您所在的用户组【%s】的【%s】数据源权限发生了变更","系统管理/用户组管理"),
    USER_GROUP_PERMISSION_DATA_BASE_ADD(1,"用户组信息","您所在的用户组【%s】新增了【%s】数据库权限","系统管理/用户组管理"),
    USER_GROUP_PERMISSION_DATA_BASE_REMOVE(1,"用户组信息","您所在的用户组【%s】移除了【%s】数据库权限","系统管理/用户组管理"),
    USER_GROUP_PERMISSION_INDICATOR_CATEGORY_REMOVE(1,"用户组信息","您所在的用户组【%s】用户组的【%s】业务目录权限被移除","系统管理/用户组管理"),
    USER_GROUP_PERMISSION_INDICATOR_CATEGORY_CHANGE(1,"用户组信息","您所在的用户组【%s】用户组的【%s】业务目录发生权限变更","系统管理/用户组管理"),
    USER_GROUP_PERMISSION_API_PROJECT_BASE_ADD(1,"用户组信息","您所在的用户组【%s】新增了【%s】项目权限","系统管理/用户组管理"),
    USER_GROUP_PERMISSION_API_PROJECT_REMOVE(1,"用户组信息","您所在的用户组【%s】移除了【%s】项目权限","系统管理/用户组管理"),
    USER_GROUP_PERMISSION_IMPORT_TABLE_ADD(1,"用户组信息","您所在的用户组【%s】新增了【%s】重要表权限","系统管理/用户组管理"),
    USER_GROUP_PERMISSION_IMPORT_TABLE_REMOVE(1,"用户组信息","您所在的用户组【%s】移除了【%s】重要表权限","系统管理/用户组管理"),
    USER_GROUP_PERMISSION_SECURITY_TABLE_ADD(1,"用户组信息","您所在的用户组【%s】新增了【%s】保密表权限","系统管理/用户组管理"),
    USER_GROUP_PERMISSION_SECURITY_TABLE_REMOVE(1,"用户组信息","您所在的用户组【%s】移除了【%s】保密表权限","系统管理/用户组管理"),


    // 数据服务审核——推送
    DATA_SERVICE_AUDIT_START(2, "数据服务审核", "您有一条【%s】的API待审核", "数据服务/API项目管理/API管理"),
    DATA_SERVICE_AUDIT_FINISH(2, "数据服务审核", "您创建的【%s】的API已由管理员审核【%s】", "数据服务/API项目管理/API管理"),

    NEED_AUDIT_START_MANAGER(3, "需求审批", "您有一条【%s】的需求待处理", "数据服务/需求管理"),
    NEED_AUDIT_DEAL_PEOPLE(3, "需求审批", "您有一条【%s】的需求需要进行处理", "数据服务/需求管理"),
    NEED_AUDIT_FINISH(3, "需求审批", "您发起的【%s】的需求已有相关人员处理并反馈", "数据服务/需求管理");

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
    /**
     * 流程进度(0-已审批、1-未审批、2-已授权、3-已移除、4-已处理、5-已反馈、6-待处理
     */
    public int process;

    public final static String PASS = "通过";
    public final static String REJECT = "被驳回";
    public final static String RELEASE = "发布";
    public final static String OFFLINE = "下线";

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

    public static String getFormattedMessageName(String name, String parameter) {

        return String.format(name, parameter);
    }

    public static String getFormattedMessageName(String name, String parameter, String parameter1) {

        return String.format(name, parameter, parameter1);
    }

    public static String getFormattedMessageName(String name, String parameter, String parameter1, String parameter2) {

        return String.format(name, parameter, parameter1, parameter2);
    }

    public static String getFormattedMessageName(String name, String parameter, String parameter1, String parameter2, String parameter3) {

        return String.format(name, parameter, parameter1, parameter2, parameter3);
    }


}
