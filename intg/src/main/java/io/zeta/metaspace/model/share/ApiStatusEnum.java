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
 * @Data 2020/8/14 15:47
 */
public enum ApiStatusEnum {
    DRAFT("draft","草稿"),
    UP("up","上架"),
    DOWN("down","下架"),
    AUDIT("audit","审核中");
    private String name;
    private String str;

    ApiStatusEnum(String name, String str) {
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

    public static ApiStatusEnum getApiStatusEnum(String name) {
        for (ApiStatusEnum value : ApiStatusEnum.values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }
}
