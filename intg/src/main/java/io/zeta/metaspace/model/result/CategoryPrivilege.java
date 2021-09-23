package io.zeta.metaspace.model.result;

import io.zeta.metaspace.MetaspaceConfig;
import org.apache.atlas.model.metadata.CategoryEntityV2;

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
    private String privateStatus;
    private String downBrotherCategoryGuid;
    private String description;
    private Privilege privilege;

    public String getPrivateStatus() {
        return privateStatus;
    }

    public void setPrivateStatus(String privateStatus) {
        this.privateStatus = privateStatus;
    }

    private int level;
    private Integer objectCount;
    private String safe;
    private String code;
    private Integer sort;
    private Boolean read;
    private Boolean editCategory;
    private Boolean editItem;


    public CategoryPrivilege(CategoryPrivilegeV2 category){
        this.guid=category.getGuid();
        this.name=category.getName();
        this.parentCategoryGuid=category.getParentCategoryGuid();
        this.upBrotherCategoryGuid=category.getUpBrotherCategoryGuid();
        this.downBrotherCategoryGuid=category.getDownBrotherCategoryGuid();
        this.description=category.getDescription();
        this.level=category.getLevel();
        this.count=category.getCount();
        this.code=category.getCode();
        this.sort=category.getSort();
    };
    public CategoryPrivilege(CategoryEntityV2 categoryEntityV2){
        this.guid = categoryEntityV2.getGuid();
        this.name=categoryEntityV2.getName();
        this.parentCategoryGuid = categoryEntityV2.getParentCategoryGuid();
        this.upBrotherCategoryGuid=categoryEntityV2.getUpBrotherCategoryGuid();
        this.downBrotherCategoryGuid=categoryEntityV2.getDownBrotherCategoryGuid();
        this.description=categoryEntityV2.getDescription();
        this.level=categoryEntityV2.getLevel();
        this.objectCount=0;
    }


    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSafe() {
        return safe;
    }

    public void setSafe(String safe) {
        this.safe = safe;
    }

    public int getLevel() {
        return level;
    }

    public void mergeCount(int count){
        if (count>this.count){
            this.count=count;
        }
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getEditCategory() {
        return editCategory;
    }

    public void setEditCategory(Boolean editCategory) {
        this.editCategory = editCategory;
    }

    public Boolean getEditItem() {
        return editItem;
    }

    public void setEditItem(Boolean editItem) {
        this.editItem = editItem;
    }

    public CategoryPrivilege(RoleModulesCategories.Category category) {
        this.guid = category.getGuid();
        this.name = category.getName();
        this.parentCategoryGuid = category.getParentCategoryGuid();
        this.upBrotherCategoryGuid = category.getUpBrotherCategoryGuid();
        this.downBrotherCategoryGuid = category.getDownBrotherCategoryGuid();
        this.description = category.getDescription();
        this.level = category.getLevel();
        this.safe=category.getSafe();
        this.count=category.getCount();
        this.code=category.getCode();
        this.sort=category.getSort();
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

    public Integer getObjectCount() {
        return objectCount;
    }

    public void setObjectCount(Integer objectCount) {
        this.objectCount = objectCount;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static class Privilege{
        //目录是否隐藏
        private boolean hide;
        //目录是否置灰
        private boolean ash;
        //添加同级目录
        private boolean addSibling;
        //添加子集目录
        private boolean addChildren;
        //删除目录
        private boolean delete;
        //编辑目录
        private boolean edit;
        //是否只能编辑安全等级
        private boolean editSafe;
        //添加关联
        private boolean createRelation;
        //删除关联
        private boolean deleteRelation;
        //添加owner
        private boolean addOwner;
        //是否可以迁移
        private boolean move;

        public boolean isMove() {
            return move;
        }

        public void setMove(boolean move) {
            this.move = move;
        }

        public boolean isEditSafe() {
            return editSafe;
        }

        public void setEditSafe(boolean editSafe) {
            this.editSafe = editSafe;
        }

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

        public boolean isEdit() {
            return edit;
        }

        public void setEdit(boolean edit) {
            this.edit = edit;
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

        public Privilege(Privilege privilege) {
            this.hide = privilege.isHide();
            this.ash = privilege.isAsh();
            this.addSibling = privilege.isAddSibling();
            this.addChildren = privilege.isAddChildren();
            this.createRelation = privilege.isCreateRelation();
            this.delete = privilege.isDelete();
            this.deleteRelation = privilege.isDeleteRelation();
            this.addOwner = privilege.isAddOwner();
            this.edit=privilege.isEdit();
            this.editSafe=privilege.isEditSafe();
            this.move=privilege.move;
        }

        public Privilege(boolean hide, boolean ash, boolean addSibling, boolean addChildren, boolean createRelation, boolean delete, boolean deleteRelation, boolean addOwner,boolean edit,boolean editSafe) {
            this.hide = hide;
            this.ash = ash;
            this.addSibling = addSibling;
            this.addChildren = addChildren;
            this.delete = delete;
            this.edit = edit;
            this.editSafe=editSafe;
            this.createRelation = createRelation;
            this.deleteRelation = deleteRelation;
            this.addOwner = addOwner;
            this.move=addSibling;
        }
        public void mergePrivilege(Privilege  privilege){
            this.ash=this.ash&&privilege.ash;
            this.hide = this.hide&&privilege.hide;
            this.addSibling=this.addSibling||privilege.addSibling;
            this.addChildren=this.addChildren||privilege.addChildren;
            this.delete=this.delete||privilege.delete;
            this.edit=this.edit||privilege.edit;
            this.editSafe=this.editSafe||privilege.editSafe;
            this.createRelation=this.createRelation||privilege.createRelation;
            this.deleteRelation=this.deleteRelation||privilege.deleteRelation;
            this.addOwner=this.addOwner||privilege.addOwner;
            this.move=this.move||privilege.move;

        }
        public void adminPrivilege(String guid){
            this.hide=false;
            this.addSibling=true;
            this.addChildren=true;
            this.delete=true;
            this.edit=true;
            if (MetaspaceConfig.systemCategory.contains(guid)) {
                this.delete=false;
                this.edit=false;
            }
        }
    }
}
