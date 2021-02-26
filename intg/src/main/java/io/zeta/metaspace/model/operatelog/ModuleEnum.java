package io.zeta.metaspace.model.operatelog;

import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.security.RoleResource;
import io.zeta.metaspace.model.user.UserInfo;

/**
 * @author zhuxuetong
 * @date 2019-08-07 18:07
 */
public enum ModuleEnum {
    OVERVIEW(1,"概览","全部",15,"概览", "overview", "overview",1,0),
    TECHNICAL(2,"数据资产","查看技术数据",1,"技术数据", "technical", "technicalview",1,0),
    TECHNICALEDIT(2,"数据资产","技术数据关联",3,"技术数据关联", "technicaledit", "technicaledit",0,0),
    TECHNICALADMIN(2,"数据资产","技术目录编辑",8,"技术目录编辑", "technicaladmin", "technicaladmin",0,0),
    METADATA(2,"数据资产","元数据管理",7,"元数据管理", "metadata", "metadata",1,0),
    BUSINESS(2,"数据资产","查看业务对象",2,"业务对象", "business", "businessview",1,0),
    BUSINESSEDIT(2,"数据资产","业务对象编辑",4,"业务对象编辑", "businessedit", "businessedit",0,0),
    BUSINESSMANAGE(2,"数据资产","业务对象管理",5,"业务对象管理", "businessmanage", "businessmanage",1,0),
    BUSINESSADMIN(2,"数据资产","业务目录编辑",9,"业务目录编辑", "businessadmin", "businessadmin",0,0),
    DATASHARE(2,"数据资产","数据分享",10,"数据分享", "datashare", "datashare",1,0),
    DATAQUALITY(3,"数据质量","查看",13,"数据质量", "dataquality", "dataquality",1,0),
    DATASTANDARD(4,"数据标准","全部",11,"数据标准", "datastandard", "datastandard",1,0),
    USER(5,"系统管理","用户管理",18,"用户管理", "user", "user",1,0),
    ROLE(6,"角色管理","查看",17,"角色管理", "role", "role",0,0),
    USERGROUP(5,"系统管理","用户组管理",16,"用户组管理","usergroup","usergroup",1,0),
    DATASOURCE(5,"系统管理","数据源管理",14,"数据源管理","dataSource","datasource",1,0),
    PRIVILEGE(7,"权限","查看",6,"权限", "privilege", "privilege",0,0),
    OPERATELOG(5,"系统管理","日志审计",12,"日志审计","operatelog","operatelog",1,0),
    AUTHORIZATION(5,"系统管理","目录授权管理",19,"目录授权管理","authorization","authorization",1,0),
    RULETEMPLATE(3,"数据质量","问题库",20,"问题库","ruletemplate","ruletemplate",1,0),
    RULEMANAGE(3,"数据质量","规则管理",21,"规则管理","rulemanage","rulemanage",1,0),
    WARNINGGROUP(3,"数据质量","告警管理",22,"告警管理","warninggroup","warninggroup",1,0),
    TASKMANAGE(3,"数据质量","任务管理",23,"任务管理","taskmanage","taskmanage",1,0),
    DATAEDIT(2,"数据资产","数据管理",24,"数据管理","data","dataedit",1,0),
    COLUMNDOWN(2,"数据资产","下载管理",25,"下载管理","columns","columndown",1,0),
    AUDIT(8,"数据服务","审核中心",26,"审核中心","audit","audit",1,0),
    APIMANAGE(8,"数据服务","API项目管理",27,"API项目管理","apimanage","apimanage",1,0),
    DESENSITIZATION(8,"数据服务","脱敏管理",28,"脱敏管理","desensitization","desensitization",1,0),
    IPRESTRICTION(8,"数据服务","黑白名单管理",29,"黑白名单管理","iprestriction","iprestriction",1,0),
    INDEXDESIGN(9,"指标管理","指标设计",30,"指标设计","indexdesign","indexdesign",1,1),
    TIMELIMIT(9,"指标管理","时间限定",31,"时间限定","timelimit","timelimit",1,0),
    QUALIFIER(9,"指标管理","修饰词",32,"修饰词","qualifier","qualifier",1,0),
    APPROVEGROUP(10,"审批管理","审批组管理",33,"审批组管理","approvegroup","approvegroup",1,0)
    ;

    private int id;
    private String name;
    private String alias;
    private int type;
    private String tenantModule;
    private int groupId;
    private String groupName;
    private String show;

    public int getApprove() {
        return approve;
    }

    public void setApprove(int approve) {
        this.approve = approve;
    }

    private int approve;  //是否是可审批模块 0：不可 1：有审批业务

    ModuleEnum(int groupId,String groupName,String show,int id,String name, String alias,String tenantModule,int type,int approve) {
        this.groupId=groupId;
        this.groupName=groupName;
        this.show = show;
        this.id=id;
        this.name = name;
        this.alias = alias;
        this.type=type;
        this.tenantModule=tenantModule;
        this.approve = approve;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTenantModule() {
        return tenantModule;
    }

    public void setTenantModule(String tenantModule) {
        this.tenantModule = tenantModule;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Module getModule(){
        Module module = new Module();
        module.setModuleId(id);
        module.setModuleName(name);
        module.setType(type);
        module.setApprove(approve);
        return module;
    }
    public UserInfo.Module getUserInfoModule(){
        UserInfo.Module module = new UserInfo.Module();
        module.setModuleId(id);
        module.setModuleName(name);
        module.setType(type);
        module.setGroupId(groupId);
        module.setGroupName(groupName);
        module.setShow(show);
        return module;
    }

    public static ModuleEnum getModuleEnum(RoleResource roleResource){
        for (ModuleEnum module : ModuleEnum.values()) {
            if(module.tenantModule.equalsIgnoreCase(roleResource.getRoleName()))
                return module;
        }
        return null;
    }

    public static String getModuleName(int moduleId){
        for (ModuleEnum module : ModuleEnum.values()) {
            if(module.id==moduleId)
                return module.toString();
        }
        return null;
    }

    public static String getModuleShowName(int moduleId){
        for (ModuleEnum module : ModuleEnum.values()) {
            if(module.id==moduleId)
                return module.getName();
        }
        return null;
    }


}
