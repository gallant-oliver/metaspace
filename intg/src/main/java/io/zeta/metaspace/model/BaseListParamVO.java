package io.zeta.metaspace.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.util.ObjectUtils;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("分页baseDTO")
public class BaseListParamVO implements Serializable {
    private static int DEFAULT_PAGE_SIZE = 10;
    private static int DEFAULT_PAGE_NO = 1;

    @NotNull(message = "每页记录数不可为空")
    @ApiModelProperty("每页记录数")
    private Integer pageSize = DEFAULT_PAGE_SIZE;
    @NotNull(message = "当前页不可为空")
    @ApiModelProperty("当前页")
    private Integer pageNo = DEFAULT_PAGE_NO;
    @ApiModelProperty(hidden = true)
    private Integer offset;
    @ApiModelProperty(hidden = true)
    private Integer size;

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        if (!ObjectUtils.isEmpty(pageNo) && pageNo < 1) {
            pageNo = DEFAULT_PAGE_NO;
        }
        this.pageNo = pageNo;
    }

    public Integer getOffset() {
        offset = this.getPageSize() * (this.getPageNo() - 1);
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getSize() {
        if (ObjectUtils.isEmpty(size)) {
            size = pageSize;
        }
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "BaseListParamDto{" +
                "pageSize=" + pageSize +
                ", pageNo=" + pageNo +
                '}';
    }

}
