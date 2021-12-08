package io.zeta.metaspace.model.dto.requirements;

import lombok.Data;

/**
 * @author fanjiajia
 * @Description
 * @date 2021/12/8 14:35
 */
@Data
public class DealDetailDTO {

    //结果： 1 同意  2 拒绝
    private String result;

    //用户组
    private String group;

    //用户
    private String user;

    //处理说明
    private String description;
}
