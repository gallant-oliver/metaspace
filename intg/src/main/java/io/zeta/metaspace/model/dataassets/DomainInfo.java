package io.zeta.metaspace.model.dataassets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * @author fanjiajia
 * @Description
 * @date 2021/11/10 17:02
 */

@Data
public class DomainInfo {

    //主题域id
    private String domainId;

    //主题域名称
    private String domainName;

    //主题数量
    private Integer themeNum;

    //租户id
    private String tenantId;

    //租户名称
    private String tenantName;

    //目录公开私密状态
    @JsonIgnore
    private String privateStatus;

}
