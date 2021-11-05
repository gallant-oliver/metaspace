package io.zeta.metaspace.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author wangjianjun
 */
@ApiModel("返回通用结果")
public class RestReturn<T> {

    @ApiModelProperty("code错误码")
    private Integer code;

    @ApiModelProperty("外带数据信息")
    private T data;

    @ApiModelProperty("前端进行页面展示的信息")
    private String message;

    @ApiModelProperty("错误信息")
    private String detail;

    /**
     *构造函数（无参数）
     */
    public RestReturn() {
    }

    /**
     *构造函数（有参数）
     */
    public RestReturn(Integer code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    @Override
    public String toString() {
        return "RestReturn{" +
                "code=" + code +
                ", data=" + data +
                ", message='" + message + '\'' +
                ", detail='" + detail + '\'' +
                '}';
    }


    public RestReturn<T> success(T data) {
        this.code = 10000;
        this.data = data;
        this.message = "请求成功";
        return this;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
