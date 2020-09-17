package io.zeta.metaspace.model.share;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HiveQueryParameter extends QueryParameter {

    private String tableGuid;
    private String dbGuid;

}