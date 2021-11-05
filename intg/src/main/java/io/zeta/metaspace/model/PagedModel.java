package io.zeta.metaspace.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "分页返回信息")
public class PagedModel<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "总共多少条数据")
    private long totalElements;
    @ApiModelProperty(value = "总共多少页数据")
    private int totalPages;
    @ApiModelProperty(value = "每页的数据")
    private List<T> content;
    @ApiModelProperty("当前页")
    private int pageNo;
    @ApiModelProperty("每页记录数")
    private int pageSize;

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "PagedModel{" +
                "totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", content=" + content +
                '}';
    }

    public static <T> PagedModel<T> pageInfo(List<T> data, int totalElements, int pageNo, int pageSize){
        PagedModel<T> pagedModel = new PagedModel<>();
        pagedModel.setPageNo(pageNo);
        pagedModel.setPageSize(pageSize);
        pagedModel.setContent(data);
        pagedModel.setTotalPages(totalElements / pageSize + 1);
        pagedModel.setTotalElements(totalElements);
        return pagedModel;
    }
}
