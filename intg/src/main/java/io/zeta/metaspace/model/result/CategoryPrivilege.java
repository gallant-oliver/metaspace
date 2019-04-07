package io.zeta.metaspace.model.result;

import java.security.PublicKey;

/*
{
    "guid": "sit consequat ullamco deserunt",
    "name": "laboris aliquip mollit laborum nostrud",
    "description": "enim quis",
    "upBrotherCategoryGuid": null,
    "downBrotherCategoryGuid": null,
    "parentCategoryGuid": "Excepteur exercitation",
	"privilege":{
	"hide": false,
	"show": true,
	"addSibling":true,
	"addChildren":true,
	"createRelation":true,
	"delete":true,
	"deleteRelation":true,
	"addOwner":""
	}
  }
]
 */
public class CategoryPrivilege {
    private String guid;
    private String name;
    private String parentCategoryGuid;
    private String upBrotherCategoryGuid;
    private String downBrotherCategoryGuid;
    private String description;
    private Privilege privilege;
    private int level;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public CategoryPrivilege(RoleModulesCategories.Category category) {
        this.guid = category.getGuid();
        this.name = category.getName();
        this.parentCategoryGuid = category.getParentCategoryGuid();
        this.upBrotherCategoryGuid = category.getUpBrotherCategoryGuid();
        this.downBrotherCategoryGuid = category.getDownBrotherCategoryGuid();
        this.description = category.getDescription();
        this.level = category.getLevel();
    }



    public CategoryPrivilege() {

    }

    public Privilege getPrivilege() {
        return privilege;
    }

    public void setPrivilege(Privilege privilege) {
        this.privilege = privilege;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentCategoryGuid() {
        return parentCategoryGuid;
    }

    public void setParentCategoryGuid(String parentCategoryGuid) {
        this.parentCategoryGuid = parentCategoryGuid;
    }

    public String getUpBrotherCategoryGuid() {
        return upBrotherCategoryGuid;
    }

    public void setUpBrotherCategoryGuid(String upBrotherCategoryGuid) {
        this.upBrotherCategoryGuid = upBrotherCategoryGuid;
    }

    public String getDownBrotherCategoryGuid() {
        return downBrotherCategoryGuid;
    }

    public void setDownBrotherCategoryGuid(String downBrotherCategoryGuid) {
        this.downBrotherCategoryGuid = downBrotherCategoryGuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    public static class Privilege{
        private boolean hide;
        private boolean ash;
        private boolean addSibling;
        private boolean addChildren;
        private boolean createRelation;
        private boolean delete;
        private boolean deleteRelation;
        private boolean addOwner;

        public Privilege() {

        }

        public boolean isHide() {
            return hide;
        }

        public void setHide(boolean hide) {
            this.hide = hide;
        }

        public boolean isAsh() {
            return ash;
        }

        public void setAsh(boolean ash) {
            this.ash = ash;
        }

        public boolean isAddSibling() {
            return addSibling;
        }

        public void setAddSibling(boolean addSibling) {
            this.addSibling = addSibling;
        }

        public boolean isAddChildren() {
            return addChildren;
        }

        public void setAddChildren(boolean addChildren) {
            this.addChildren = addChildren;
        }

        public boolean isCreateRelation() {
            return createRelation;
        }

        public void setCreateRelation(boolean createRelation) {
            this.createRelation = createRelation;
        }

        public boolean isDelete() {
            return delete;
        }

        public void setDelete(boolean delete) {
            this.delete = delete;
        }

        public boolean isDeleteRelation() {
            return deleteRelation;
        }

        public void setDeleteRelation(boolean deleteRelation) {
            this.deleteRelation = deleteRelation;
        }

        public boolean isAddOwner() {
            return addOwner;
        }

        public void setAddOwner(boolean addOwner) {
            this.addOwner = addOwner;
        }

        public Privilege(boolean hide, boolean ash, boolean addSibling, boolean addChildren, boolean createRelation, boolean delete, boolean deleteRelation, boolean addOwner) {
            this.hide = hide;
            this.ash = ash;
            this.addSibling = addSibling;
            this.addChildren = addChildren;
            this.createRelation = createRelation;
            this.delete = delete;
            this.deleteRelation = deleteRelation;
            this.addOwner = addOwner;
        }
    }
}
