package io.zeta.metaspace.model.share;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author huangrongwen
 * @Description: 测试api返回值
 * @date 2022/8/3011:20
 */
@Data
public class ApiTestResultVO {
    //返回结果
    private List<LinkedHashMap<String, Object>> queryResult;
    //返回结果条数
    private Long queryCount;
}
