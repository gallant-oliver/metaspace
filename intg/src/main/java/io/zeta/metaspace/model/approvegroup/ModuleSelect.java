package io.zeta.metaspace.model.approvegroup;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModuleSelect {

    private String moduleId;

    private String moduleName;

    private boolean selected;

}
