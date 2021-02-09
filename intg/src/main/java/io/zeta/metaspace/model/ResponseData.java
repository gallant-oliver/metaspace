package io.zeta.metaspace.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseData<T> implements Serializable {
    private int code;
    private String message;
    private String detail;
    private T data;
    private String requestId;

    public ResponseData(int code, String message, String detail, T data) {
        this.code = code;
        this.message = message;
        this.detail = detail;
        this.data = data;
    }

    public ResponseData(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ResponseData(int code, String message) {
        this.code = code;
        this.message = message;
    }
    public ResponseData(T data) {
        this.code = 200;
        this.data = data;
        this.message = "请求成功";
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

}
