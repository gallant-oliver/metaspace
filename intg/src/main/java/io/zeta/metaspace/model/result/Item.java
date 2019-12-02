package io.zeta.metaspace.model.result;

import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.role.Role;

import java.util.List;

public class Item {
    private List<Module> modules;
    private List<Role> roles;

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
