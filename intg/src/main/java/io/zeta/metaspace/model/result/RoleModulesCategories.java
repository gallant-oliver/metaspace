package io.zeta.metaspace.model.result;

import io.zeta.metaspace.model.metadata.CategoryEntity;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.privilege.Privilege;

import java.util.List;

public class RoleModulesCategories {
    private List<CategoryEntity> technicalCategories;
    private List<CategoryEntity> businessCategories;
    private Privilege privilege;

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

    public Privilege getPrivilege() {
        return privilege;
    }

    public void setPrivilege(Privilege privilege) {
        this.privilege = privilege;
    }
}
