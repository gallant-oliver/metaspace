package io.zeta.metaspace.model.result;

import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.role.Role;

import java.util.List;

public class Item {
    private List<Module> modules;

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }
}
