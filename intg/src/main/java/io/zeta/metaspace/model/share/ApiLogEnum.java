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

package io.zeta.metaspace.model.share;

/**
 * @author lixiang03
 * @Data 2020/6/5 18:29
 */
public enum  ApiLogEnum {
    INSERT("insert","%s创建了api"),
    UPDATE("update","%s编辑了api"),
    MOVE("move","%s迁移了api"),
    DELETE("delete","%s删除了api"),
    UPSTATUS("upstatus","%s上架了api"),
    DOWNSTATUS("downstatus","%s下架了api"),
    SUBMIT("submit","%s提交了api"),
    UNSUBMIT("unsubmit","%s撤销提交api请求");

    private String name;
    private String str;

    ApiLogEnum(String name, String str) {
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
        for (ApiLogEnum module : ApiLogEnum.values()) {
            if(module.name.equals(name))
                return module.getStr();
        }
        return null;
    }

    public static String getName(String str){
        if (str==null){
            return null;
        }
        for (ApiLogEnum module : ApiLogEnum.values()) {
            if(module.str.contains(str))
                return module.getName();
        }
        return null;
    }

    public static ApiLogEnum getApiLog(String name){
        for (ApiLogEnum module : ApiLogEnum.values()) {
            if(module.name.equals(name))
                return module;
        }
        return null;
    }
}
