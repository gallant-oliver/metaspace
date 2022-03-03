package io.zeta.metaspace.model.dto.api;

import lombok.Data;

/**
 * @author huangrongwen
 * @Description: api测试的对象类
 * @date 2022/3/116:42
 */
@Data
public class ApiTestDTO {
    private String apiId;
    private String version;
    private String apiStatus;
    private boolean apiGroupPublish;
    private String apiMobiusId;
    private String apiGroupId;
    private String apiGroupIdMobiusId;
}
