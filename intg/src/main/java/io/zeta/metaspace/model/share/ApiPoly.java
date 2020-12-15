package io.zeta.metaspace.model.share;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Api 策略，记录历史策略
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiPoly {
    private String id;
    private String apiId;
    private String apiVersion;
    private ApiPolyEntity poly;
    private AuditStatusEnum status = AuditStatusEnum.NEW;
    private LocalDateTime createTime = LocalDateTime.now();
    private LocalDateTime updateTime = LocalDateTime.now();

    public ApiPoly(String apiId, String apiVersion, ApiPolyEntity poly) {
        this.id = UUID.randomUUID().toString();
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.poly = poly;
    }
}
