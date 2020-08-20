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

package io.zeta.metaspace.model.apigroup;

/**
 * @author lixiang03
 * @Data 2020/8/13 10:28
 */
public enum ApiGroupLogEnum {
    INSERT("insert","%s创建了api分组"),
    UPDATE("update","%s编辑了api分组"),
    UPLEVEL("uplevel","%s升级了api"),
    DELETE("delete","%s删除了api"),
    PUBLISH("publish","%s发布了api分组"),
    UNPUBLISH("unpublish","%s撤销发布api分组");

    private String name;
    private String str;

    ApiGroupLogEnum(String name, String str) {
        this.name = name;
        this.str = str;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public static String getStr(String name){
        for (ApiGroupLogEnum module : ApiGroupLogEnum.values()) {
            if(module.name.equals(name))
                return module.getStr();
        }
        return null;
    }

    public static ApiGroupLogEnum getApiLog(String name){
        for (ApiGroupLogEnum module : ApiGroupLogEnum.values()) {
            if(module.name.equals(name))
                return module;
        }
        return null;
    }
}
