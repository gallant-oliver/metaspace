(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-55bede26"],{"0a49":function(e,t,i){var n=i("9b43"),o=i("626a"),r=i("4bf8"),a=i("9def"),s=i("cd1c");e.exports=function(e,t){var i=1==e,l=2==e,u=3==e,c=4==e,d=6==e,f=5==e||d,p=t||s;return function(t,s,g){for(var m,h,v=r(t),b=o(v),y=n(s,g,3),x=a(b.length),k=0,C=i?p(t,x):l?p(t,0):void 0;x>k;k++)if((f||k in b)&&(m=b[k],h=y(m,k,v),e))if(i)C[k]=h;else if(h)switch(e){case 3:return!0;case 5:return m;case 6:return k;case 2:C.push(m)}else if(c)return!1;return d?-1:u||c?c:C}}},1169:function(e,t,i){var n=i("2d95");e.exports=Array.isArray||function(e){return"Array"==n(e)}},"138d":function(e,t,i){"use strict";var n=function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("div",{staticClass:"data-list"},[i("el-table",e._b({directives:[{name:"loading",rawName:"v-loading",value:e.loading,expression:"loading"}],ref:"dataTable",on:{"sort-change":e.sortChange,"selection-change":e.handleSelectionChange}},"el-table",e.tableProps,!1),[e._t("default")],2),i("gs-pagination",e._b({attrs:{"page-sizes":[10,20,30,40,50,100],layout:e.paginationProps.layout||"range,total,pagesizes,pager,jumper,jumpbtn"},on:{"page-change":e.pageChange,"page-size-change":e.pageSizeChange}},"gs-pagination",e.paginationProps,!1))],1)},o=[],r={props:{tableProps:{type:Object,default:function(){return{}}},paginationProps:{type:Object,default:function(){return{}}},loading:{type:Boolean,default:!1}},methods:{sortChange:function(e){var t=e.column,i=e.prop,n=e.order;this.$emit("sort-change",{column:t,prop:i,order:n})},handleSelectionChange:function(e){this.$emit("selection-change",e)},pageChange:function(e){this.$emit("page-change",e)},pageSizeChange:function(e){this.$emit("page-size-change",e)},clearSelection:function(){this.$refs.dataTable.clearSelection()},getTable:function(){return this.$refs.dataTable},doLayout:function(){this.$refs.dataTable.doLayout()}}},a=r,s=i("2877"),l=Object(s["a"])(a,n,o,!1,null,null,null),u=l.exports;t["a"]=u},"20d6":function(e,t,i){"use strict";var n=i("5ca1"),o=i("0a49")(6),r="findIndex",a=!0;r in[]&&Array(1)[r](function(){a=!1}),n(n.P+n.F*a,"Array",{findIndex:function(e){return o(this,e,arguments.length>1?arguments[1]:void 0)}}),i("9c6c")(r)},"2b0b":function(e,t,i){"use strict";i.d(t,"f",function(){return o}),i.d(t,"b",function(){return r}),i.d(t,"e",function(){return a}),i.d(t,"d",function(){return s}),i.d(t,"a",function(){return l}),i.d(t,"c",function(){return u}),i.d(t,"g",function(){return c});var n=i("751a");function o(e){return n["a"].post("/privilege/privileges",e)}function r(e){return n["a"].delete("/privilege/".concat(e))}function a(e){return n["a"].get("/privilege/".concat(e))}function s(){return n["a"].get("/privilege/modules")}function l(e){return n["a"].post("/privilege",e)}function u(e,t){return n["a"].put("/privilege/".concat(e),t)}function c(e){return n["a"].post("/privilege/roles",e)}},"900a":function(e,t,i){},"9c83":function(e,t,i){"use strict";i.r(t);var n=function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("div",{staticClass:"auth-mangement"},[i("div",{staticClass:"search-box"},[i("gs-button",{attrs:{type:"primary"},on:{click:e.openAddAuthModal}},[e._v("\n      添加方案\n    ")]),i("gs-search",{attrs:{placeholder:"请输入方案名称"},on:{clear:function(t){return e.fetchAuthList(1)},search:e.fetchAuthList},model:{value:e.keyword,callback:function(t){e.keyword=t},expression:"keyword"}})],1),i("data-table",{attrs:{loading:e.loading,"table-props":{data:e.tableData},"pagination-props":e.pagination},on:{"page-change":e.pageChange,"page-size-change":e.sizeChange}},[i("el-table-column",{attrs:{prop:"privilegeName",label:"方案名称"}}),i("el-table-column",{attrs:{prop:"description",label:"描述"}}),i("el-table-column",{attrs:{prop:"roles",label:"授权角色"},scopedSlots:e._u([{key:"default",fn:function(t){return e._l(t.row.role||[],function(t){return i("gs-tag",{key:t},[e._v("\n          "+e._s(t)+"\n        ")])})}}])}),i("el-table-column",{attrs:{label:"操作",width:"100",fixed:"right"},scopedSlots:e._u([{key:"default",fn:function(t){return[i("gs-button",{attrs:{disabled:!t.row.edit,type:"text-primary"},on:{click:function(i){return e.openEditAuthModal(t)}}},[e._v("\n          编辑\n        ")]),i("gs-button",{attrs:{disabled:!t.row.delete,type:"text-primary"},on:{click:function(i){return e.deleteAuth(t)}}},[e._v("\n          删除\n        ")])]}}])})],1),i("add-auth",{attrs:{"privilege-id":e.editPrivilegeId},on:{addPrivilege:e.addPrivilege,editPrivilege:e.editPrivilege},model:{value:e.editModalVisible,callback:function(t){e.editModalVisible=t},expression:"editModalVisible"}})],1)},o=[],r=(i("900a"),i("2b0b")),a=function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("gs-modal",{staticClass:"add-auth",attrs:{title:e.title,top:"60px"},on:{cancel:function(t){return e.$emit("input",!1)},confirm:e.confirm,"visible-change":e.visibleChange},model:{value:e.visible,callback:function(t){e.visible=t},expression:"visible"}},[i("gs-loading",{attrs:{"is-show":e.loading}}),i("gs-form",{ref:"form",attrs:{model:e.form,rules:e.rules,"label-width":"80px"}},[i("gs-form-item",{attrs:{label:"方案名称",prop:"privilegeName"}},[i("gs-input",{model:{value:e.form.privilegeName,callback:function(t){e.$set(e.form,"privilegeName",t)},expression:"form.privilegeName"}})],1),i("gs-form-item",{attrs:{label:"描述",prop:"description"}},[i("gs-textarea",{model:{value:e.form.description,callback:function(t){e.$set(e.form,"description",t)},expression:"form.description"}})],1),i("gs-form-item",{attrs:{label:"授权内容",prop:"module"}},[i("div",{staticClass:"modules"},[i("div",{staticClass:"module"},[i("label",{staticClass:"title"},[e._v("授权模块")]),i("gs-checkbox-group",{on:{change:e.authModuleCheckboxChange},model:{value:e.form.modules,callback:function(t){e.$set(e.form,"modules",t)},expression:"form.modules"}},e._l(e.authModules,function(t){return i("gs-checkbox",{key:t.moduleId,attrs:{label:t.moduleId}},[e._v("\n              "+e._s(t.moduleName)+"\n            ")])}),1)],1),i("div",{staticClass:"bussiness-obj-ope"},[i("label",{staticClass:"title"},[e._v("管理权限")]),i("gs-checkbox-group",{on:{change:e.opeModuleCheckboxChange},model:{value:e.form.modules,callback:function(t){e.$set(e.form,"modules",t)},expression:"form.modules"}},e._l(e.opeModules,function(t){return i("gs-checkbox",{key:t.moduleId,attrs:{label:t.moduleId}},[e._v("\n              "+e._s(t.moduleName)+"\n            ")])}),1)],1)])]),i("gs-form-item",{attrs:{label:"授权角色",prop:"roles"}},[i("gs-select",{attrs:{multiple:""},model:{value:e.form.roles,callback:function(t){e.$set(e.form,"roles",t)},expression:"form.roles"}},e._l(e.roleOptions,function(e){return i("gs-option",{key:e.roleId,attrs:{label:e.roleName,title:e.roleName,value:e.roleId}})}),1)],1)],1)],1)},s=[],l=(i("20d6"),i("75fc")),u=(i("4f7f"),i("1c4c"),i("6762"),i("2fdb"),i("ac6a"),i("5df3"),i("ed08")),c=u["a"].techData,d=u["a"].businessObject,f=u["a"].manageTechCagatory,p=u["a"].editTechInfo,g=u["a"].manageBusinessCagatory,m=u["a"].editBusinessInfo,h={props:{privilegeId:{type:String,default:""},value:{type:Boolean,default:!1}},data:function(){return{roleOptions:[],form:{modules:[],description:"",privilegeName:"",roles:[]},loading:!1,rules:{privilegeName:[{required:!0,max:64,message:"方案名称不能为空且不能大于64个字符"}]},opeModules:[],authModules:[]}},computed:{visible:{get:function(){return this.value},set:function(e){return e}},title:function(){return this.privilegeId?"编辑方案":"添加方案"}},methods:{confirm:function(){var e=this;this.$refs.form.validate(function(t){t&&(e.privilegeId?e.$emit("editPrivilege",{id:e.privilegeId,params:e.form}):e.$emit("addPrivilege",e.form))})},visibleChange:function(e){var t=this;if(e){this.loading=!0,this.$refs.form.clearValidate();var i=function(){return r["d"]().then(function(e){t.opeModules=e.filter(function(e){return 0===e.type}),t.authModules=e.filter(function(e){return 1===e.type})})},n=function(){return r["g"]({limit:-1,query:""}).then(function(e){t.roleOptions=e.lists})};this.form={modules:[],description:"",privilegeName:"",roles:[]},Promise.all([i(),n()]).then(function(e){t.privilegeId?t.getPrivilegeDetail().then(function(e){t.loading=!1}):t.loading=!1})}},getPrivilegeDetail:function(){var e=this;return r["e"](this.privilegeId).then(function(t){var i=t.description,n=t.privilegeName,o=t.modules,r=t.roles;e.form={description:i,privilegeName:n,modules:o.map(function(e){return e.moduleId}),roles:r.map(function(e){return e.roleId})}})},opeModuleCheckboxChange:function(e){Array.isArray(e)&&(this.form.modules=e.includes(p)?Array.from(new Set([c,p].concat(Object(l["a"])(this.form.modules)))):this.form.modules,this.form.modules=e.includes(f)?Array.from(new Set([c,f].concat(Object(l["a"])(this.form.modules)))):this.form.modules,this.form.modules=e.includes(m)?Array.from(new Set([d,m].concat(Object(l["a"])(this.form.modules)))):this.form.modules,this.form.modules=e.includes(g)?Array.from(new Set([d,g].concat(Object(l["a"])(this.form.modules)))):this.form.modules)},authModuleCheckboxChange:function(e){if(!e.includes(c)){var t=this.form.modules.findIndex(function(e){return e===p});-1!==t&&this.form.modules.splice(t,1);var i=this.form.modules.findIndex(function(e){return e===f});-1!==i&&this.form.modules.splice(i,1)}if(!e.includes(2)){var n=this.form.modules.findIndex(function(e){return e===m});-1!==n&&this.form.modules.splice(n,1);var o=this.form.modules.findIndex(function(e){return e===g});-1!==o&&this.form.modules.splice(o,1)}}}},v=h,b=i("2877"),y=Object(b["a"])(v,a,s,!1,null,null,null),x=y.exports,k=i("138d"),C={components:{addAuth:x,dataTable:k["a"]},data:function(){return{keyword:"",tableData:[{userId:"1",username:"1",account:"in",roles:["amet irure","222"]}],pagination:{total:0,pageSize:10,pageSizes:[10,20,30],current:1},loading:!1,editPrivilegeId:"",editModalVisible:!1}},computed:{},created:function(){this.fetchAuthList(1)},methods:{fetchAuthList:function(){var e=this,t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:0;1===t&&(this.pagination.current=1),this.loading=!0,r["f"]({offset:(this.pagination.current-1)*this.pagination.pageSize,limit:this.pagination.pageSize,query:this.keyword}).then(function(t){t.lists.map(function(e){return e.role=e.roles.map(function(e){return e.roleName}),e}),e.tableData=t.lists,e.pagination.total=t.sum,e.loading=!1}).catch(function(t){e.loading=!1})},pageChange:function(e){this.pagination.current=e,this.fetchAuthList()},sizeChange:function(e){this.pagination.pageSize=e,this.fetchAuthList(1)},addPrivilege:function(e){var t=this;r["a"](e).then(function(e){t.fetchAuthList(1),t.$Message.success("添加方案成功！"),t.editModalVisible=!1})},editPrivilege:function(e){var t=this,i=e.id,n=e.params;r["c"](i,n).then(function(e){t.fetchAuthList(),t.$Message.success("修改方案成功！"),t.editModalVisible=!1})},openEditAuthModal:function(e){var t=e.row;this.editPrivilegeId=t.privilegeId,this.editModalVisible=!0},deleteAuth:function(e){var t=this,i=e.row;i.privilegeId&&(!i.roles||i.roles&&!i.roles.length)?this.$Modal.confirm({title:"警告",content:"是否删除该权限方案？删除后将无法恢复",onOk:function(){r["b"](i.privilegeId).then(function(e){t.$Message.success("删除权限方案成功"),t.fetchAuthList()})}}):this.$Message.warning("请先全清空使用该权限方案的角色！")},openAddAuthModal:function(){this.editPrivilegeId="",this.editModalVisible=!0}}},A=C,I=Object(b["a"])(A,n,o,!1,null,null,null);t["default"]=I.exports},cd1c:function(e,t,i){var n=i("e853");e.exports=function(e,t){return new(n(e))(t)}},e853:function(e,t,i){var n=i("d3f4"),o=i("1169"),r=i("2b4c")("species");e.exports=function(e){var t;return o(e)&&(t=e.constructor,"function"!=typeof t||t!==Array&&!o(t.prototype)||(t=void 0),n(t)&&(t=t[r],null===t&&(t=void 0))),void 0===t?Array:t}}}]);
//# sourceMappingURL=chunk-55bede26.43fec43a.js.map