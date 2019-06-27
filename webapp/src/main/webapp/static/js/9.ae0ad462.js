webpackJsonp([9],{2028:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var s=a(2029),i=a(2038),n=a(107)(s.a,i.a,null,null,null);t.default=n.exports},2029:function(e,t,a){"use strict";var s=a(65),i=a.n(s),n=a(772),r=a(2030),o=a(774),l=a(808),c=a(787),u=a(2031),d=a(2034),g=a(776),p=a(771),f=a(2037),h=(a.n(f),a(781).a.editBusinessInfo),m=!1;t.a={name:"BusinessObj",components:{DataTable:o.a,TwoColumnLayout:l.a,ActionComfirm:c.a,AddBusiness:u.a,EditCategory:d.a,BreadCrumb:g.a},mixins:[r.a],data:function(){return{keyword:"",page:1,pageSize:10,selecteds:[],singleDeleting:null}},computed:i()({},Object(n.c)("business",{tables:"tables",searchTables:"searchTables",tablesStatus:"tablesStatus"}),Object(n.c)("user",{modules:"modules"}),{tableData:function(){var e=this.tables,t=this.searchTables,a=this.isSearchMode,s=this.activeCategory,i=[];return i=a?t:e,s.categoryGuid||a?i:{total:0,list:[]}},tableProps:function(){var e=this.tableData;return{total:e.total,data:e.list}},paginationProps:function(){var e=this.tableProps.total||0;return{current:this.page,pageSize:this.pageSize,total:e}},isBusinessLoading:function(){return Object(p.d)(this.tablesStatus)},isDisabled:function(){var e=this.modules;return!(e.modules&&e.modules.some(function(e){return e.moduleId===h}))}}),watch:{activeCategory:function(e){e&&e.categoryGuid&&(this.page=1,this.getCategoryBusiness())}},beforeRouteEnter:function(e,t,a){m=e.meta.keepAlive,a()},beforeRouteLeave:function(e,t,a){t.meta.keepAlive=!1,a()},activated:function(){!1===m&&(this.keyword="",this.isSearchMode=!1,this.activeCategory={},this.getTopLevelCategory())},mounted:function(){this.getTopLevelCategory()},methods:i()({},Object(n.b)("business",["getTableList","findTableList","deleteBusiness"]),{setActive:function(e){var t=this.activeCategory;t&&t.categoryGuid||e&&e.categoryGuid&&(this.activeCategory=e)},afterDeleteCategory:function(e,t){var a=t.$node.parent,s=a?a.data.children:this.categories.list,i=s.findIndex(function(t){return t.categoryGuid===e});s.splice(i,1),this.activeCategory.categoryGuid===e&&(this.activeCategory={}),setTimeout(function(){},50)},query:function(){this.isSearchMode?this.findTableList({query:this.keyword,offset:(this.page-1)*this.pageSize,limit:this.pageSize}):this.getCategoryBusiness()},getCategoryBusiness:function(){var e=this.activeCategory;if(e&&e.categoryGuid)return this.getTableList({id:e.categoryGuid,param:{offset:(this.page-1)*this.pageSize,limit:this.pageSize,query:this.keyword}})},handlePageChange:function(e){this.page=e,this.query()},handlePageSizeChange:function(e){this.page=1,this.pageSize=e,this.query()},handleSearch:function(e){this.page=1,this.keyword=e,this.isSearchMode=!!e,this.query()},handleSelectionChange:function(e){this.selecteds=e},toDetail:function(e){this.$router.push({path:"/main/business/detail/"+e})},handleDelBusiness:function(e){var t=this,a=e.businessId;if(!e||this.singleDeleting!==e){var s=function(){t.singleDeleting=null};e&&(this.singleDeleting=e),this.deleteBusiness(a).then(function(){s(),t.$Message.success("删除成功"),t.query()},s)}},handleNodeToggle:function(e,t){}})}},2030:function(e,t,a){"use strict";var s=a(65),i=a.n(s),n=a(772),r=a(815),o=a(771),l=function(e){var t=e.privilege||{};return[{label:"在前添加同级目录",disabled:!t.addSibling,value:0},{label:"在后添加同级目录",disabled:!t.addSibling,value:1},{label:"添加子目录",disabled:!t.addChildren,value:2},{label:"编辑",disabled:!t.edit,value:3},{label:"删除",disabled:!t.delete,value:4}]};t.a={components:{SideTree:r.a},data:function(){return{isSearchMode:!1,visible:!1,actionType:-1,editingCategory:{},actions:l,activeCategory:{}}},computed:i()({},Object(n.c)("business",{categories:"categories"}),Object(n.c)("user",{modules:"modules"}),{isLoadingCategory:function(){return Object(o.d)(this.categories.status)},treeProps:function(){var e=this.categories,t=this.modules;return{data:e.list,expandOnFilterMatch:!0,filterNodeMethod:function(e,t){return!e||-1!==t.name.indexOf(e)},props:{label:"name",value:"categoryGuid"},disabled:"1"!==t.role.roleId}}}),watch:{},mounted:function(){},methods:i()({},Object(n.b)("business",["getTopLevelCategory","deleteCategory"]),{handleCategorySearch:function(e){this.$refs.sideTree.$refs.tree.filter(e)},handleSelectCategory:function(e){this.isSearchMode=!1,this.activeCategory=e},handleNodeAction:function(e,t,a){switch(t){case 0:this.editCategory(t,{guid:e.categoryGuid,direction:"up"});break;case 1:this.editCategory(t,{guid:e.categoryGuid,direction:"down"});break;case 2:this.editCategory(t,{guid:e.categoryGuid,parentCategoryGuid:e.categoryGuid});break;case 3:this.editCategory(t,{name:e.name,description:e.description,guid:e.categoryGuid});break;case 4:this.removeCategory(e,a)}},editCategory:function(e){var t=arguments.length>1&&void 0!==arguments[1]?arguments[1]:{};this.actionType=e,this.visible=!0,this.editingCategory=t},removeCategory:function(e,t){var a=this,s=e.name,i=e.categoryGuid;this.$Modal.confirm({title:"确定要删除 "+s+" 目录吗？",onOk:function(){a.deleteCategory(i).then(function(){a.$Message.success("删除成功"),a.afterDeleteCategory&&a.afterDeleteCategory(i,t)})}})}})}},2031:function(e,t,a){"use strict";var s=a(2032),i=a(2033),n=a(107)(s.a,i.a,null,null,null);t.a=n.exports},2032:function(e,t,a){"use strict";var s=a(65),i=a.n(s),n=a(772),r=a(771);t.a={props:{categoryGuid:{type:String,default:""},categoryGuname:{type:String,default:""},disabled:{type:Boolean,default:!1}},data:function(){return{visible:!1,businessForm:{name:"",module:"",description:"",owner:"",manager:"",maintainer:"",dataAssets:""},rules:{name:[{required:!0,trigger:"blur",max:200,message:"请输入业务对象名称，且长度不能超过200字符"}],module:[{required:!0,trigger:"blur",max:200,message:"请输入业务模块，且长度不能超过200字符"}],description:[{required:!0,trigger:"blur",max:200,message:"请输入描述信息，且长度不能超过200字符"}],owner:[{required:!0,trigger:"blur",max:200,message:"请输入所有者，且长度不能超过200字符"}],manager:[{required:!0,trigger:"blur",max:200,message:"请输入管理者，且长度不能超过200字符"}],maintainer:[{required:!0,trigger:"blur",max:200,message:"请输入维护者，且长度不能超过200字符"}],dataAssets:[{required:!0,trigger:"blur",max:200,message:"请填写相关数据资产，且长度不能超过200字符"}]}}},computed:i()({},Object(n.c)("business",{tables:"tables",createStatus:"tableItemCreateStatus"}),{isCreating:function(){return Object(r.d)(this.createStatus)}}),watch:{visible:function(e){e&&this.$refs.businessForm.resetFields()}},methods:i()({},Object(n.b)("business",["createTableItem"]),{showModal:function(){this.visible=!0},handleConfirm:function(){var e=this;this.$refs.businessForm.validate(function(t){t&&e.createTableItem({param:e.businessForm,id:e.categoryGuid}).then(function(t){e.$Message.success("添加业务对象成功"),e.$emit("create-success"),e.close()})})},close:function(){this.visible=!1}})}},2033:function(e,t,a){"use strict";var s={render:function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",{staticClass:"add-relation-box"},[a("gs-button",{attrs:{disabled:e.disabled,type:"primary"},on:{click:e.showModal}},[e._v("添加业务对象")]),e._v(" "),a("gs-modal",{staticClass:"add-business-modal",attrs:{width:500,top:56,title:"添加业务对象","has-form":""},model:{value:e.visible,callback:function(t){e.visible=t},expression:"visible"}},[a("gs-form",{ref:"businessForm",staticStyle:{width:"95%"},attrs:{model:e.businessForm,rules:e.rules,"label-width":"100px"}},[a("gs-form-item",{attrs:{label:"业务部门"}},[e._v("\n        "+e._s(e.categoryGuname)+"\n      ")]),e._v(" "),a("gs-form-item",{attrs:{prop:"name",label:"业务对象名称"}},[a("gs-input",{attrs:{placeholder:"请输入业务对象名称"},model:{value:e.businessForm.name,callback:function(t){e.$set(e.businessForm,"name",t)},expression:"businessForm.name"}})],1),e._v(" "),a("gs-form-item",{attrs:{prop:"module",label:"业务模块"}},[a("gs-input",{attrs:{placeholder:"请输入业务模块"},model:{value:e.businessForm.module,callback:function(t){e.$set(e.businessForm,"module",t)},expression:"businessForm.module"}})],1),e._v(" "),a("gs-form-item",{attrs:{prop:"description",label:"业务描述"}},[a("gs-textarea",{attrs:{placeholder:"请输入描述信息"},model:{value:e.businessForm.description,callback:function(t){e.$set(e.businessForm,"description",t)},expression:"businessForm.description"}})],1),e._v(" "),a("gs-form-item",{attrs:{prop:"owner",label:"所有者"}},[a("gs-input",{attrs:{placeholder:"请输入所有者"},model:{value:e.businessForm.owner,callback:function(t){e.$set(e.businessForm,"owner",t)},expression:"businessForm.owner"}})],1),e._v(" "),a("gs-form-item",{attrs:{prop:"manager",label:"管理者"}},[a("gs-input",{attrs:{placeholder:"请输入管理者"},model:{value:e.businessForm.manager,callback:function(t){e.$set(e.businessForm,"manager",t)},expression:"businessForm.manager"}})],1),e._v(" "),a("gs-form-item",{attrs:{prop:"maintainer",label:"维护者"}},[a("gs-input",{attrs:{placeholder:"请输入维护者"},model:{value:e.businessForm.maintainer,callback:function(t){e.$set(e.businessForm,"maintainer",t)},expression:"businessForm.maintainer"}})],1),e._v(" "),a("gs-form-item",{attrs:{prop:"dataAssets",label:"相关数据资产"}},[a("gs-textarea",{attrs:{placeholder:"请填写相关数据资产"},model:{value:e.businessForm.dataAssets,callback:function(t){e.$set(e.businessForm,"dataAssets",t)},expression:"businessForm.dataAssets"}})],1)],1),e._v(" "),a("template",{slot:"footer"},[a("gs-button",{attrs:{loading:e.isCreating,type:"primary"},on:{click:e.handleConfirm}},[e._v("确定")]),e._v(" "),a("gs-button",{attrs:{disabled:e.isCreating},on:{click:e.close}},[e._v("取消")])],1)],2)],1)},staticRenderFns:[]};t.a=s},2034:function(e,t,a){"use strict";var s=a(2035),i=a(2036),n=a(107)(s.a,i.a,null,null,null);t.a=n.exports},2035:function(e,t,a){"use strict";var s=a(65),i=a.n(s),n=a(772),r=a(771);t.a={props:{isShow:{type:Boolean,default:!1},type:{type:Number,default:1},category:{type:Object,default:function(){return{}}}},data:function(){return{rules:{name:[{required:!0,max:64,message:"目录名称不能为空且不能大于64个字符"}]},visible:this.isShow,formData:this.category}},computed:i()({},Object(n.c)("business",{createStatus:"categoryCreateStatus",updateStatus:"categoryUpdateStatus"}),{loading:function(){return Object(r.d)(this.createStatus)||Object(r.d)(this.updateStatus)},title:function(){var e="";switch(this.type){case 0:e="在前添加一级目录";break;case 1:e="在后添加一级目录";break;case 2:e="添加子目录";break;case 3:e="编辑目录"}return e}}),watch:{isShow:function(e){this.visible=e},category:function(e){this.formData=e},visible:function(e){e&&this.$refs.form.resetFields(),this.$emit("update:isShow",e)}},methods:i()({},Object(n.b)("business",["createCategory","updateCategory"]),{setVisible:function(e){this.visible=e},handleOk:function(){var e=this;this.$refs.form.validate(function(t){if(t){var a=e.type,s=e.formData,i=void 0,n="添加成功";0!==a&&1!==a&&2!==a||(i=e.createCategory(s)),3===a&&(i=e.updateCategory(s),n="修改成功"),i.then(function(){e.$Message.success(n),e.setVisible(!1)})}})}})}},2036:function(e,t,a){"use strict";var s={render:function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("gs-modal",{staticClass:"category-ceate-modal",attrs:{title:e.title},model:{value:e.visible,callback:function(t){e.visible=t},expression:"visible"}},[a("gs-form",{ref:"form",attrs:{model:e.formData,rules:e.rules,"label-width":"70px"}},[a("gs-form-item",{attrs:{label:"目录名称",prop:"name"}},[a("gs-input",{attrs:{placeholder:"请输入目录名称，最大长度64个字符"},model:{value:e.formData.name,callback:function(t){e.$set(e.formData,"name",t)},expression:"formData.name"}})],1),e._v(" "),a("gs-form-item",{attrs:{label:"描述",prop:"description"}},[a("gs-textarea",{attrs:{placeholder:"请输入描述信息，最大长度20个字符"},model:{value:e.formData.description,callback:function(t){e.$set(e.formData,"description",t)},expression:"formData.description"}})],1)],1),e._v(" "),a("template",{slot:"footer"},[a("gs-button",{attrs:{loading:e.loading,type:"primary"},on:{click:e.handleOk}},[e._v("确定")]),e._v(" "),a("gs-button",{attrs:{disabled:e.loading},on:{click:function(t){return e.setVisible(!1)}}},[e._v("取消")])],1)],2)},staticRenderFns:[]};t.a=s},2037:function(e,t){},2038:function(e,t,a){"use strict";var s={render:function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",{staticClass:"table-list-page"},[a("gs-search",{staticClass:"search-table",attrs:{placeholder:"请输入业务对象名称"},on:{search:e.handleSearch}}),e._v(" "),a("div",{staticClass:"content-header"},[a("bread-crumb",{attrs:{"current-level":"业务对象"}})],1),e._v(" "),a("two-column-layout",[a("side-tree",{ref:"sideTree",attrs:{slot:"left",loading:e.isLoadingCategory,active:e.activeCategory,actions:e.actions,"tree-props":e.treeProps},on:{search:e.handleCategorySearch,"node-click":e.handleSelectCategory,"node-action":e.handleNodeAction,"create-category":function(t){return e.editCategory(1)},"set-active":e.setActive,"node-toggle":e.handleNodeToggle},slot:"left"}),e._v(" "),a("template",{slot:"right"},[a("div",{staticClass:"action-group"},[a("add-business",{attrs:{"category-guid":e.activeCategory.categoryGuid,"category-guname":e.activeCategory.name,disabled:e.isDisabled||!e.activeCategory.categoryGuid},on:{"create-success":e.query}})],1),e._v(" "),a("data-table",{staticClass:"business-table",attrs:{loading:e.isBusinessLoading,"table-props":e.tableProps,"pagination-props":e.paginationProps},on:{"selection-change":e.handleSelectionChange,"page-change":e.handlePageChange,"page-size-change":e.handlePageSizeChange}},[a("el-table-column",{attrs:{type:"selection",width:"55"}}),e._v(" "),a("el-table-column",{attrs:{prop:"name",label:"业务对象名称","show-overflow-tooltip":""},scopedSlots:e._u([{key:"default",fn:function(t){return[a("a",{attrs:{title:"查看业务对象名称详情",href:"javascript:void(0);"},on:{click:function(a){return e.toDetail(t.row.businessId)}}},[e._v("\n              "+e._s(t.row.name)+"\n            ")])]}}])}),e._v(" "),a("el-table-column",{attrs:{prop:"level2Category",label:"所属二级部门","show-overflow-tooltip":""},scopedSlots:e._u([{key:"default",fn:function(t){return[e._v("\n            "+e._s(t.row.level2Category||"-")+"\n          ")]}}])}),e._v(" "),a("el-table-column",{attrs:{prop:"path",label:"路径","show-overflow-tooltip":""}}),e._v(" "),a("el-table-column",{attrs:{prop:"businessStatus",label:"业务信息状态"},scopedSlots:e._u([{key:"default",fn:function(t){return[a("span",{class:{"status-text":!0,"status-finished":"1"===t.row.businessStatus}},[e._v("\n              "+e._s("1"===t.row.businessStatus?"已补充":"待补充")+"\n            ")])]}}])}),e._v(" "),a("el-table-column",{attrs:{prop:"technicalStatus",label:"技术信息状态"},scopedSlots:e._u([{key:"default",fn:function(t){return[a("span",{class:{"status-text":!0,"status-finished":"1"===t.row.technicalStatus}},[e._v("\n              "+e._s("1"===t.row.technicalStatus?"已补充":"待补充")+"\n            ")])]}}])}),e._v(" "),a("el-table-column",{attrs:{label:"操作"},scopedSlots:e._u([{key:"default",fn:function(t){return[a("gs-button",{attrs:{type:"text-primary"},on:{click:function(a){return e.toDetail(t.row.businessId)}}},[e._v("详情")]),e._v(" "),e.isDisabled?a("gs-button",{attrs:{type:"text-primary",disabled:""}},[e._v("删除")]):a("action-comfirm",{attrs:{title:"确定删除吗？"},on:{comfirm:function(a){return e.handleDelBusiness(t.row)}}},[a("gs-button",{attrs:{slot:"reference",type:"text-primary"},slot:"reference"},[e._v("\n                "+e._s(e.singleDeleting===t.row?"正在删除...":"删除")+"\n              ")])],1)]}}])})],1)],1)],2),e._v(" "),a("edit-category",{attrs:{"is-show":e.visible,type:e.actionType,category:e.editingCategory},on:{"update:isShow":function(t){e.visible=t},"update:is-show":function(t){e.visible=t}}})],1)},staticRenderFns:[]};t.a=s},774:function(e,t,a){"use strict";var s=a(782);t.a=s.a},776:function(e,t,a){"use strict";var s=a(777),i=a(780);a.n(i);t.a=s.a},777:function(e,t,a){"use strict";var s=a(778),i=a(779),n=a(107)(s.a,i.a,null,null,null);t.a=n.exports},778:function(e,t,a){"use strict";t.a={name:"BreadCrumb",props:{currentLevel:{type:String,required:!0},upperLevel:{type:String,default:""},backPath:{type:String,default:""}},data:function(){return{}},computed:{isHideNav:function(){return!!this.$router.currentRoute.query.nav}},methods:{backBtnClick:function(){this.$emit("back")}}}},779:function(e,t,a){"use strict";var s={render:function(){var e=this,t=e.$createElement,a=e._self._c||t;return!e.upperLevel||e.isHideNav?a("div",{staticClass:"content-header-single"},[e._t("header",[e._v("\n    "+e._s(e.currentLevel)+"\n  ")])],2):a("div",{staticClass:"content-header-both"},[a("div",{staticClass:"back-link"},[a("router-link",{attrs:{to:e.backPath}},[a("gs-button",{staticClass:"back-btn",attrs:{type:"primary"},on:{click:e.backBtnClick}},[a("i",{staticClass:"gs-icon gs-icon-revoke"})])],1)],1),e._v(" "),a("div",{staticClass:"nav-name"},[a("div",{staticClass:"current-nav"},[e._v(e._s(e.currentLevel))]),e._v(" "),a("div",{staticClass:"upper-nav"},[e._v(e._s(e.upperLevel))])])])},staticRenderFns:[]};t.a=s},780:function(e,t){},782:function(e,t,a){"use strict";var s=a(783),i=a(784),n=a(107)(s.a,i.a,null,null,null);t.a=n.exports},783:function(e,t,a){"use strict";t.a={props:{tableProps:{type:Object,default:function(){return{}}},paginationProps:{type:Object,default:function(){return{}}},loading:{type:Boolean,default:!1}},methods:{sortChange:function(e){var t=e.column,a=e.prop,s=e.order;this.$emit("sort-change",{column:t,prop:a,order:s})},handleSelectionChange:function(e){this.$emit("selection-change",e)},pageChange:function(e){this.$emit("page-change",e)},pageSizeChange:function(e){this.$emit("page-size-change",e)},clearSelection:function(){this.$refs.dataTable.clearSelection()},getTable:function(){return this.$refs.dataTable},doLayout:function(){this.$refs.dataTable.doLayout()}}}},784:function(e,t,a){"use strict";var s={render:function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",{staticClass:"data-list"},[a("el-table",e._b({directives:[{name:"loading",rawName:"v-loading",value:e.loading,expression:"loading"}],ref:"dataTable",on:{"sort-change":e.sortChange,"selection-change":e.handleSelectionChange}},"el-table",e.tableProps,!1),[e._t("default")],2),e._v(" "),a("gs-pagination",e._b({attrs:{"page-sizes":[10,20,30,40,50,100],layout:e.paginationProps.layout||"range,total,pagesizes,pager,jumper,jumpbtn"},on:{"page-change":e.pageChange,"page-size-change":e.pageSizeChange}},"gs-pagination",e.paginationProps,!1))],1)},staticRenderFns:[]};t.a=s},787:function(e,t,a){"use strict";var s=a(799),i=a(802);a.n(i);t.a=s.a},799:function(e,t,a){"use strict";var s=a(800),i=a(801),n=a(107)(s.a,i.a,null,null,null);t.a=n.exports},800:function(e,t,a){"use strict";t.a={props:{title:{type:String,default:""},popoverProps:{type:Object,default:function(){return{}}}},data:function(){return{visible:!1}},methods:{onComfirm:function(){this.visible=!1,this.$emit("comfirm")},onCancel:function(){this.visible=!1,this.$emit("cancel")}}}},801:function(e,t,a){"use strict";var s={render:function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("gs-popover",e._b({ref:"popover",staticClass:"action-comfirm-popover",attrs:{supernatant:"",placement:"top",trigger:"click"},model:{value:e.visible,callback:function(t){e.visible=t},expression:"visible"}},"gs-popover",e.popoverProps,!1),[a("div",{staticClass:"action-comfirm"},[e._t("default",[a("gs-icon",{staticClass:"action-comfirm-tip-icon",attrs:{name:"exclamation-circle"}}),e._v(e._s(e.title)+"\n    ")])],2),e._v(" "),a("div",{staticClass:"action-comfirm-footer"},[a("gs-button",{attrs:{type:"text-primary"},on:{click:e.onComfirm}},[e._v("确定")]),e._v(" "),a("gs-button",{attrs:{type:"text"},on:{click:e.onCancel}},[e._v("取消")])],1),e._v(" "),e._t("reference",null,{slot:"reference"})],2)},staticRenderFns:[]};t.a=s},802:function(e,t){},808:function(e,t,a){"use strict";var s=a(820),i=a(823);a.n(i);t.a=s.a},815:function(e,t,a){"use strict";var s=a(816),i=a(819);a.n(i);t.a=s.a},816:function(e,t,a){"use strict";var s=a(817),i=a(818),n=a(107)(s.a,i.a,null,null,null);t.a=n.exports},817:function(e,t,a){"use strict";t.a={props:{loading:{type:Boolean,default:!1},searchProps:{type:Object,default:function(){return{}}},treeProps:{type:Object,default:function(){return{}}},keyword:{type:String,default:""},actions:{type:[Array,Function],default:function(){return[]}},active:{type:Object,default:function(){return{}}},hasAction:{type:Boolean,default:!0},defaultExpanded:{type:Boolean,default:!0},defaultExpandedKeys:{type:Array,default:function(){return[]}},preicon:{type:String,default:"iconfont icon-wenjianjia1"},searchPlaceholder:{type:String,default:"请输入目录名称"},emptyBtnText:{type:String,default:"点击添加目录"}},data:function(){return{searchVal:this.keyword,expandedKeys:[]}},computed:{isEmpty:function(){return this.loading||0===this.treeProps.data.length},expand:function(){return this.defaultExpandedKeys&&this.defaultExpandedKeys.length?this.defaultExpandedKeys:this.expandedKeys}},watch:{keyword:function(e){this.searchVal=e},searchVal:function(e){this.$emit("search",e)},treeProps:function(e){e.data&&e.data.length&&this.defaultExpanded&&this.getExpandedKeys(e.data)},expandedKeys:function(e){var t=this.treeProps.data||[];if(e&&e.length){var a=t.filter(function(t){return t.guid===e[0]});if(!a||!a.length)return;if(a[0].privilege&&!a[0].privilege.ash)return void this.$emit("set-active",a[0]);var s=(a[0].children||[]).filter(function(e){return e.privilege&&!e.privilege.ash});if(!s||!s.length)return;this.$emit("set-active",s[0])}else{if(!t||!t.length)return;this.$emit("set-active",t[0])}}},methods:{getExpandedKeys:function(e){this.expandedKeys=function(e,t){var a=[];return function e(s){s.map(function(s){s.privilege&&s.privilege.ash===t&&(a.push(s.guid),s.children&&s.children&&e(s.children))})}(e),a}(e,!0)},renderTreeNode:function(e,t){var a,s=t.node,i=t.data,n=this.actions,r=this.treeProps,o=this.active,l=this.hasAction,c=r.props,u=c.label,d=c.value,g=c.deleting,p=void 0===g?"deleting":g,f="side-tree-node-label";return"function"==typeof n&&(n=n(i)),f+=o[d]===i[d]?" is-active":"",f+=i.disabled?" is-disabled":"",f+=i[p]?" is-deleting":"",e("div",{class:"side-tree-node-content"},[i[p]?e("span",{class:f},[e("gs-icon",{class:"side-tree-node-icon",attrs:{title:"正在删除",name:"loading"}}),i[u]]):e("span",{attrs:{title:i[u]},class:f,on:{click:this.handleClick.bind(this,i)}},[e("i",(a={class:"side-tree-node-icon"},a.class=this.preicon,a)),i[u]]),l?e("gs-dropdown",{class:"side-tree-action",attrs:{trigger:"click"},on:{select:this.handleAction.bind(this,{node:s,data:i})}},[e("gs-icon",{class:"gs-dropdown-link",attrs:{name:"ellipsis",mode:"button"}}),e("gs-dropdown-menu",{attrs:{align:"right"},slot:"dropdown"},[e("gs-dropdown-items",{attrs:{options:n}})])]):null])},handleNodeToggle:function(e,t){this.$emit("node-toggle",e.data,t)},handleClick:function(e){e.disabled||this.$emit("node-click",e)},handleAction:function(e,t){var a=e.node,s=e.data;this.$emit("node-action",s,t,a)},createCategory:function(){this.$emit("create-category")}}}},818:function(e,t,a){"use strict";var s={render:function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",{directives:[{name:"loading",rawName:"v-loading",value:e.loading,expression:"loading"}],staticClass:"side-tree"},[e.isEmpty?e._e():a("gs-search",e._b({ref:"search",attrs:{placeholder:e.searchPlaceholder},model:{value:e.searchVal,callback:function(t){e.searchVal=t},expression:"searchVal"}},"gs-search",e.searchProps,!1)),e._v(" "),e.isEmpty?e._e():a("gs-tree",e._b({ref:"tree",attrs:{"render-content":e.renderTreeNode,"default-expanded-keys":e.expand,"node-key":"guid"},on:{"node-toggle":e.handleNodeToggle}},"gs-tree",e.treeProps,!1)),e._v(" "),!e.loading&&e.isEmpty&&e.hasAction?a("gs-button",{attrs:{disabled:e.treeProps.disabled,type:"primary"},on:{click:e.createCategory}},[e._v(e._s(e.emptyBtnText))]):e._e()],1)},staticRenderFns:[]};t.a=s},819:function(e,t){},820:function(e,t,a){"use strict";var s=a(821),i=a(822),n=a(107)(s.a,i.a,null,null,null);t.a=n.exports},821:function(e,t,a){"use strict";t.a={props:{leftProps:{type:Object,default:function(){return{span:6,xs:{span:24}}}},rightProps:{type:Object,default:function(){return{span:18,xs:{span:24}}}}}}},822:function(e,t,a){"use strict";var s={render:function(){var e=this.$createElement,t=this._self._c||e;return t("div",{staticClass:"two-column-layout"},[t("div",{staticClass:"left-col"},[this._t("left")],2),this._v(" "),t("div",{staticClass:"right-col"},[this._t("right")],2)])},staticRenderFns:[]};t.a=s},823:function(e,t){}});
//# sourceMappingURL=9.ae0ad462.js.map