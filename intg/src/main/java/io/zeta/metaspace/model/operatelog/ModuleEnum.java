package io.zeta.metaspace.model.operatelog;

import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.security.RoleResource;
import io.zeta.metaspace.model.user.UserInfo;

/**
 * @author zhuxuetong
 * @date 2019-08-07 18:07
 */
public enum ModuleEnum {
    OVERVIEW(1,"概览","查看",15,"概览", "overview", "overview",1),
    TECHNICAL(2,"技术数据","查看技术数据",1,"技术数据", "technical", "technicalview",1),
    TECHNICALEDIT(2,"技术数据","编辑技术数据",3,"编辑技术数据", "technicaledit", "technicaledit",0),
    TECHNICALADMIN(2,"技术数据","管理技术数据",8,"管理技术数据", "technicaladmin", "technicaladmin",0),
    METADATA(3,"元数据管理","查看",7,"元数据管理", "metadata", "metadata",1),
    BUSINESS(4,"业务对象","查看业务对象",2,"业务对象", "business", "businessview",1),
    BUSINESSEDIT(4,"业务对象","编辑业务对象",4,"编辑业务对象", "businessedit", "businessedit",0),
    BUSINESSMANAGE(4,"业务对象","业务对象管理",5,"业务对象管理", "businessmanage", "businessmanage",0),
    BUSINESSADMIN(5,"管理业务对象","查看",9,"管理业务对象", "businessadmin", "businessadmin",1),
    DATASHARE(6,"数据分享","查看",10,"数据分享", "datashare", "datashare",1),
    DATAQUALITY(7,"数据质量","查看",13,"数据质量", "dataquality", "dataquality",1),
    DATASTANDARD(8,"数据标准","查看",11,"数据标准", "datastandard", "datastandard",1),
    USER(9,"用户管理","查看",18,"用户管理", "user", "user",1),
    ROLE(10,"角色管理","查看",17,"角色管理", "role", "role",1),
    USERGROUP(11,"用户组管理","查看",16,"用户组管理","usergroup","usergroup",1),
    DATASOURCE(12,"数据源管理","查看",14,"数据源管理","dataSource","datasource",1),
    PRIVILEGE(13,"权限","查看",6,"权限", "privilege", "privilege",1),
    OPERATELOG(14,"日志审计","查看",12,"日志审计","operatelog","operatelog",1),
    AUTHORIZATION(15,"目录授权管理","查看",19,"目录授权管理","authorization","authorization",1),
    RULETEMPLATE(7,"数据质量","问题库",20,"问题库","ruletemplate","ruletemplate",1),
    RULEMANAGE(7,"数据质量","规则管理",21,"规则管理","rulemanage","rulemanage",1),
    WARNINGGROUP(7,"数据质量","告警管理",22,"告警管理","warninggroup","warninggroup",1),
    TASKMANAGE(7,"数据质量","任务管理",23,"任务管理","taskmanage","taskmanage",1),
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
