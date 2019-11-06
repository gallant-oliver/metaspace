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

package io.zeta.metaspace.model.datastandard;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.NotBlank;

import java.sql.Timestamp;

/**
 * @author lixiang03
 * @Data 2019/10/30 15:30
 */
public class DataStandardHead {
    private String id;
    @NotBlank
    //@Pattern(regexp = "^[A-Z0-9]+$", message = "编号内容格式错误，请输入大写英文字母或数字")
    private String number;
    @NotBlank
    private String content;
    private String description;
    private String categoryId;
    private String[] pathIds;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String[] getPathIds() {
        return pathIds;
    }

    public void setPathIds(String[] pathIds) {
        this.pathIds = pathIds;
    }
}
