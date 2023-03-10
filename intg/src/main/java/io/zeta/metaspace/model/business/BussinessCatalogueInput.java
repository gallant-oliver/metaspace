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
 * @date 2018/11/19 13:47
 */
package io.zeta.metaspace.model.business;

import lombok.Data;

/*
 * @description
 * @author fanjiajia
 * @date 2021/9/28
 */

@Data
public class BussinessCatalogueInput {

    private String guid;
    private String name;
    private String description;
    private String parentCategoryGuid;
    private String direction;
    private Integer categoryType;
    private Boolean publish;
    private String approveGroupId;
    private String information;

}
