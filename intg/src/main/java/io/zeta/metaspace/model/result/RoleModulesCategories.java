package io.zeta.metaspace.model.result;

import io.zeta.metaspace.model.metadata.CategoryEntity;
import io.zeta.metaspace.model.privilege.Module;

import java.util.List;

public class RoleModulesCategories {
    private List<CategoryEntity> technicalCategories;
    private List<CategoryEntity> businessCategories;
    private List<Module> modules;

    public List<CategoryEntity> getTechnicalCategories() {
        return technicalCategories;
    }

    public void setTechnicalCategories(List<CategoryEntity> technicalCategories) {
        this.technicalCategories = technicalCategories;
    }

    public List<CategoryEntity> getBusinessCategories() {
        return businessCategories;
    }

    public void setBusinessCategories(List<CategoryEntity> businessCategories) {
        this.businessCategories = businessCategories;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }
}
