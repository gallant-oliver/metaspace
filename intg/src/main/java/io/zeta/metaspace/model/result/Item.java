package io.zeta.metaspace.model.result;

import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.role.Role;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class Item {
    private Set<Module> modules;
    private List<Role> roles;
}
