package io.zeta.metaspace.model.operatelog;

import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.security.RoleResource;
import io.zeta.metaspace.model.user.UserInfo;

/**
 * @author zhuxuetong
 * @date 2019-08-07 18:07
 */
public enum ModuleEnum {
    OVERVIEW(1,"概览","全部",15,"概览", "overview", "overview",1),
    TECHNICAL(2,"数据资产","查看技术数据",1,"技术数据", "technical", "technicalview",1),
    TECHNICALEDIT(2,"数据资产","技术数据关联",3,"技术数据关联", "technicaledit", "technicaledit",0),
    TECHNICALADMIN(2,"数据资产","技术目录编辑",8,"技术目录编辑", "technicaladmin", "technicaladmin",0),
    METADATA(2,"数据资产","元数据管理",7,"元数据管理", "metadata", "metadata",1),
    BUSINESS(2,"数据资产","查看业务对象",2,"业务对象", "business", "businessview",1),
    BUSINESSEDIT(2,"数据资产","业务对象编辑",4,"业务对象编辑", "businessedit", "businessedit",0),
    BUSINESSMANAGE(2,"数据资产","业务对象管理",5,"业务对象管理", "businessmanage", "businessmanage",0),
    BUSINESSADMIN(2,"数据资产","业务目录编辑",9,"业务目录编辑", "businessadmin", "businessadmin",1),
    DATASHARE(2,"数据资产","数据分享",10,"数据分享", "datashare", "datashare",1),
    DATAQUALITY(3,"数据质量","查看",13,"数据质量", "dataquality", "dataquality",1),
    DATASTANDARD(4,"数据标准","全部",11,"数据标准", "datastandard", "datastandard",1),
    USER(5,"系统管理","用户管理",18,"用户管理", "user", "user",1),
    ROLE(6,"角色管理","查看",17,"角色管理", "role", "role",1),
    USERGROUP(5,"系统管理","用户组管理",16,"用户组管理","usergroup","usergroup",1),
    DATASOURCE(5,"系统管理","数据源管理",14,"数据源管理","dataSource","datasource",1),
    PRIVILEGE(7,"权限","查看",6,"权限", "privilege", "privilege",1),
    OPERATELOG(5,"系统管理","日志审计",12,"日志审计","operatelog","operatelog",1),
    AUTHORIZATION(5,"系统管理","目录授权管理",19,"目录授权管理","authorization","authorization",1),
    RULETEMPLATE(3,"数据质量","问题库",20,"问题库","ruletemplate","ruletemplate",1),
    RULEMANAGE(3,"数据质量","规则管理",21,"规则管理","rulemanage","rulemanage",1),
    WARNINGGROUP(3,"数据质量","告警管理",22,"告警管理","warninggroup","warninggroup",1),
    TASKMANAGE(3,"数据质量","任务管理",23,"任务管理","taskmanage","taskmanage",1),
    DATAEDIT(2,"数据资产","数据管理",24,"数据管理","data","dataedit",1),
    COLUMNDOWN(2,"数据资产","下载管理",25,"下载管理","columns","columndown",1),
    APIMANAGE(8,"数据服务","api项目管理",26,"api项目管理","apimanage","apimanage",1),
    AUDITCENTER(8,"数据服务","审核中心",27,"审核中心","audit","audit",1),
    ;

    private int id;
    private String name;
    private String alias;
    private int type;
    private String tenantModule;
    private int groupId;
    private String groupName;
    private String show;

    ModuleEnum(int groupId,String groupName,String show,int id,String name, String alias,String tenantModule,int type) {
        this.groupId=groupId;
        this.groupName=groupName;
        this.show = show;
        this.id=id;
        this.name = name;
        this.alias = alias;
        this.type=type;
        this.tenantModule=tenantModule;
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
                return module.getAlias();
        }
        return null;
    }
}
