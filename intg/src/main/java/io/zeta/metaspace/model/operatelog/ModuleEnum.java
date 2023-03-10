package io.zeta.metaspace.model.operatelog;

import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.security.RoleResource;
import io.zeta.metaspace.model.user.UserInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * @author zhuxuetong
 * @date 2019-08-07 18:07
 *
 * 2021-09-15
 */
public enum ModuleEnum {
    //概览
    OVERVIEW(1,"概览","全部",1,"全部", "overview", "overview",1, 1),

    //数据资产
    TECHNICAL(2,"数据资产","技术目录",2,"技术目录", "technical", "technical",1, 2),
    INDEX(2,"数据资产","指标目录",47,"指标目录", "index", "index",1, 47),
    BUSINESS(2,"数据资产","业务目录",3,"业务对象", "business", "business",1, 3),
    BUSINESSCATALOGUE(2,"数据资产","业务目录",48,"业务目录", "businesscatalogue", "businesscatalogue",0, 48),
    BUSINESSMANAGE(2,"数据资产","业务目录",4,"业务对象挂载", "businessmanage", "businessmanage",1, 4),
    COLUMNDOWN(2,"数据资产","业务目录",5,"字段下载", "columndown", "columndown",0, 5),
    BUSINESSEDIT(2,"数据资产","业务目录",6,"数据编辑", "businessedit", "businessedit",0, 6),
//    METADATA(2,"数据资产","元数据管理",7,"元数据管理", "metadata", "metadata",1, 7),
    METADATASHOW(2,"数据资产","元数据管理",54,"元数据展示", "metadataShow", "metadataShow",1, 54),
    METADATACOLLECTION(2,"数据资产","元数据管理",55,"元数据采集", "metadataCollection", "metadataCollection",1, 55),
    DATABASEREGISTER(2,"数据资产","源信息登记",38,"数据库登记", "databaseregister", "databaseregister",1, 38),
    DERIVEDTABLESREGISTER(2,"数据资产","源信息登记",39,"衍生表登记", "derivedtablesregister", "derivedtablesregister",1, 39),
//    MYAPPLICATION(2,"数据资产","资产审批管理",18,"我的申请", "myapplication", "myapplication",1, 18),
//    APPROVALPENDING(2,"数据资产","资产审批管理",40,"待审批", "approvalpending", "approvalpending",1, 40),
//    ALREADYAPPROVE(2,"数据资产","资产审批管理",41,"已审批", "alreadyapprove", "alreadyapprove",1, 41),

    //以下数据资产在安全中心不存在
    TECHNICALEDIT(2,"数据资产","技术数据关联",8,"技术数据关联", "technicaledit", "technicaledit",0, 8),
    TECHNICALADMIN(2,"数据资产","技术目录编辑",9,"技术目录编辑", "technicaladmin", "technicaladmin",0, 9),
//    DATAEDIT(2,"数据资产","数据管理",10,"数据管理","data","dataedit",0, 10),
    BUSINESSADMIN(2,"数据资产","业务目录编辑",11,"业务目录编辑", "businessadmin", "businessadmin",0, 11),
    DATASHARE(2,"数据资产","数据分享",12,"数据分享", "datashare", "datashare",0, 12),

    //指标设计
    BUSINESSINDEX(3,"指标设计","业务指标",42,"业务指标", "businessindex", "businessindex",1, 13),
    TECHNICALINDEX (3,"指标设计","技术指标",43,"技术指标", "technicalindex", "technicalindex",1, 14),
    INDEXDIMENSION (3,"指标设计","指标维度",44,"指标维度", "indexdimension", "indexdimension",1, 14),
    NORMDESIGN(3,"指标设计","指标管理",13,"指标设计", "normdesign", "normdesign",1, 13),
    MODIFIER (3,"指标设计","指标管理",14,"修饰词", "modifier", "modifier",1, 14),
    TIMELIMIT(3,"指标设计","指标管理",15,"时间限定", "timelimit", "timelimit",1, 15),
//    APPROVALMANAGE(3,"规范定义","审批中心",19,"审批管理", "approvalmanage", "approvalmanage",1, 19),

    //以下指标设计在安全中心不存在
    ATOMICINDEX(3,"指标设计","原子指标",57,"原子指标", "atomicindex", "atomicindex",1, 57),
    DERIVEINDEX(3,"指标设计","衍生指标",58,"衍生指标", "deriveindex", "deriveindex",1, 58),
    COMPLEXINDEX(3,"指标设计","复合指标",59,"复合指标", "complexindex", "complexindex",1, 59),

    //审批管理
    APPROVERMANAGE(9,"审批管理","审批人管理",17,"审批人管理", "approvermanage", "approvermanage",1, 17),
    STATUS(9,"审批管理","审批状况",45,"审批状况", "status", "status",1, 17),
    APPLICATIONS(9,"审批管理","我的申请",46,"我的申请", "applications", "applications",1, 17),
    AUDIT(9, "审批管理", "API审核中心", 28, "API审核中心", "audit", "audit", 1, 28),

    //数据质量
    RULEMANAGE(4,"数据质量","规则管理",20,"规则管理", "rulemanage", "rulemanage",1, 20),
    TASKMANAGE(4,"数据质量","任务管理",21,"任务管理", "taskmanage", "taskmanage",1, 21),
    WARNINGGROUP(4,"数据质量","告警处理",22,"告警处理", "warninggroup", "warninggroup",1, 22),
    RULETEMPLATE(4,"数据质量","报告归档",23,"报告归档", "ruletemplate", "ruletemplate",1, 23),
    DATASTANDARD(4,"数据质量","数据标准",16,"数据标准", "datastandard", "datastandard",1,16),
    //以下数据质量在安全中心不存在
    DATAQUALITY(4,"数据质量","查看",24,"数据质量", "dataquality", "dataquality",1, 24),

    //数据服务
    APIMANAGE(5,"数据服务","API项目管理",25,"API项目管理", "apimanage", "apimanage",1, 25),
    IPRESTRICTION(5,"数据服务","黑白名单管理",26,"黑白名单管理","iprestriction","iprestriction",1, 26),
    DESENSITIZATION(5,"数据服务","脱敏管理",27,"脱敏管理","desensitization","desensitization",1, 27),
    REQUIREMENTMANAGEMENT(5, "数据服务", "需求管理", 61, "需求管理", "requirementManagement", "requirementManagement", 1, 61),

    //角色管理和权限在安全中心不存在
    ROLE(6,"角色管理","查看",29,"角色管理", "role", "role",0, 29),
    PRIVILEGE(7,"权限","查看",30,"权限", "privilege", "privilege",0, 30),

    //系统管理
    USER(8,"系统管理","用户管理",31,"用户管理", "user", "user",1, 31),
    USERGROUP(8,"系统管理","用户组管理",32,"用户组管理", "usergroup", "usergroup",1, 32),
    ALARMGROUPMANAGE(8,"系统管理","告警组管理",33,"告警组管理", "alarmgroupmanage", "alarmgroupmanage",1, 33),
    DATASOURCE(8,"系统管理","数据源管理",34,"数据源管理", "dataSource", "dataSource",1, 34),
    OPERATELOG(8,"系统管理","日志审计",35,"日志审计", "operatelog", "operatelog",1, 36),
    AUTHORIZATION(8,"系统管理","目录管理",36,"目录管理", "authorization", "authorization",1, 35),
//    INDEXAREAAUTH(8,"系统管理","指标域授权",37,"指标域授权", "indexareaauth", "indexareaauth",1, 37);

    //公共租户-租户资产
    TECHNICALALL(10,"租户资产","技术目录",49,"技术目录", "technicalAll", "technicalAll",1, 49),
    BUSINESSALL(10,"租户资产","业务目录",50,"业务目录", "businessAll", "businessAll",1, 50),
    INDEXALL(10,"租户资产","指标目录",51,"指标目录", "indexAll", "indexAll",1, 51),
    METADATAALL(10,"租户资产","元数据管理",52,"元数据管理", "metadataAll", "metadataAll",1, 52),
    //公共租户-权限管理
    USERALL(11,"权限管理","用户管理",53,"用户管理", "userAll", "userAll",1, 53),
    //数据资产检索
    DATAASSERTSEARCH(12, "数据资产检索", "数据资产检索", 56, "数据资产检索", "dataAssertSearch", "dataAssertSearch", 1, 56),
    //公共租户-需求管理
    REQUIREMENTMANAGEMENTPUBLIC(13, "需求管理", "需求管理", 60, "需求管理", "requirementManagementPublic", "requirementManagementPublic", 1, 60),

    //文件归档
    ARCHIVEDFILE(14,"文件归档","文件归档",70,"文件归档", "archivedFile", "archivedFile",1, 70);

    private int id;
    private String name;
    private String alias;
    private int type;
    private String tenantModule;
    private int groupId;
    private String groupName;
    private String show;
    private int order;

    ModuleEnum(int groupId,String groupName,String show,int id,String name, String alias,String tenantModule,int type, int order) {
        this.groupId=groupId;
        this.groupName=groupName;
        this.show = show;
        this.id=id;
        this.name = name;
        this.alias = alias;
        this.type=type;
        this.tenantModule=tenantModule;
        this.order = order;
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

    public int getGroupId() {
        return groupId;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Module getModule(){
        Module module = new Module();
        module.setModuleId(id);
        module.setModuleName(name);
        module.setType(type);
        module.setOrder(order);
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

    /**
     * 获取可审批模块，后续迭代到数据库中维护，以字段区分
     * @return
     */
    public static List<ModuleEnum> getApproveModuleEnum(){
        List<ModuleEnum> result = new LinkedList<>();
        result.add(ModuleEnum.DATABASEREGISTER);
        result.add(ModuleEnum.BUSINESSCATALOGUE);
        result.add(ModuleEnum.BUSINESS);
        result.add(ModuleEnum.BUSINESSINDEX);
        result.add(ModuleEnum.ATOMICINDEX);
        result.add(ModuleEnum.DERIVEINDEX);
        result.add(ModuleEnum.COMPLEXINDEX);
        result.add(ModuleEnum.INDEXALL);
        return result;
    }

    public static String getModuleName(int moduleId){
        for (ModuleEnum module : ModuleEnum.values()) {
            if(module.id==moduleId)
                return module.toString();
        }
        return null;
    }

    public static ModuleEnum getModuleById(int moduleId){
        for (ModuleEnum module : ModuleEnum.values()) {
            if(module.id==moduleId)
                return module;
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