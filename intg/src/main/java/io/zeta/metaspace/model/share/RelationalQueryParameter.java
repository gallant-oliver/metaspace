package io.zeta.metaspace.model.share;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RelationalQueryParameter extends QueryParameter {

    private String sourceId;
    private String schemaName;
    private String tableName;
}