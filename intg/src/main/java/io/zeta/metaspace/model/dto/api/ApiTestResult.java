package io.zeta.metaspace.model.dto.api;

import lombok.Data;

import java.util.List;

/**
 * @author huangrongwen
 * @Description: api测试结果数据
 * @date 2022/6/298:55
 */
@Data
public class ApiTestResult {
    private List<Object> data;
    private String totalCount;
    private List<Object> datas;
    private String errorCode;
    private String errorMessage;
}
