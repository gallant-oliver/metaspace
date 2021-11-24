package io.zeta.metaspace.model;

import lombok.Data;

@Data
public class HealthCheckVo<T> {

    private String status;

    private T details;
}
