webpackJsonp([4],{1305:function(t,e,a){"use strict";var i=a(5),n=a.n(i),s=a(60),r=a(615),l=a(1306),o=a(1309),c=a(17),u=a(499),p=a(1312);a.n(p);e.a={components:{ActionGroup:l.a,SearchForm:o.a,DataTable:r.a,BreadCrumb:u.a},filters:{formatSize:function(t){return"Directory"===t.type?"-":t.size}},data:function(){return{loading:!1,searchMode:!1,filePath:"/",formData:{},page:1,pageSize:15,orderBy:"fileName",sortType:"descending"}},computed:n()({},Object(s.c)("file",{list:"list"}),{tableProps:function(){return{defaultSort:{prop:this.orderBy,order:this.sortType},data:this.list.data}},paginationProps:function(){return{current:this.page,pageSize:this.pageSize,total:this.list.total}},commonParams:function(){var t=this.orderBy,e=this.sortType,a=this.page,i=this.pageSize;return{orderBy:t,sortType:{ascending:"asc",descending:"desc"}[e],offset:(a-1)*i,limit:i}}}),watch:{"list.status":function(t){this.loading=Object(c.d)(t)}},mounted:function(){this.queryFileList(this.filePath)},methods:n()({},Object(s.b)("file",["getFileList","searchFileList","downloadFile"]),{queryFileList:function(t){this.filePath=t,this.searchMode&&(this.searchMode=!1,this.page=1),this.getFileList(n()({filePath:t},this.commonParams))},findFileList:function(){this.searchFileList(n()({},this.formData,this.commonParams))},handleSearch:function(t){this.filePath="/",this.formData=t,this.page=1,t.fileName||t.modifyDate||t.owner?(this.searchMode=!0,this.findFileList()):(this.searchMode=!1,this.queryFileList(this.filePath))},queryOrFindFileList:function(){this.searchMode?this.findFileList():this.queryFileList(this.filePath)},handleSortChange:function(t){var e=t.order;this.sortType!==e&&(this.sortType=e,this.queryOrFindFileList())},handlePageChange:function(t){this.page=t,this.queryOrFindFileList()},handlePageSizeChange:function(t){this.pageSize=t,this.queryOrFindFileList()},handleFileClick:function(t){var e=t.row,a=e.filePath;"Directory"===e.type?(this.page=1,this.queryFileList(a)):this.downloadFile(a)}})}},1306:function(t,e,a){"use strict";var i=a(1307),n=a(1308),s=a(47)(i.a,n.a,null,null,null);e.a=s.exports},1307:function(t,e,a){"use strict";var i=a(5),n=a.n(i),s=a(60);e.a={props:{filePath:{type:String,default:"/"}},data:function(){return{loading:!1,fileList:[],visible:!1,labelSpan:5,cntSpan:19}},computed:{paths:function(){var t=this.filePath.split("/").filter(function(t){return!!t});return t.unshift("/"),t}},watch:{visible:function(t){t||(this.fileList=[])}},methods:n()({},Object(s.b)("file",["uploadFile"]),{open:function(){this.visible=!0},close:function(){this.visible=!1},handleUpload:function(){var t=this,e=this.filePath,a=this.fileList;if(0===a.length)return this.$Message.warning("请选择要上传的文件！",1.5),!1;var i=a[0].origin;this.loading=!0,this.uploadFile({filePath:(e+"/"+i.name).replace(/\/\//g,"/"),file:i}).then(function(){t.loading=!1,t.visible=!1,t.fileList=[],t.$emit("upload-success"),t.$Message.success("上传成功",1.5)},function(){t.loading=!1})},deleteFile:function(t){this.fileList.splice(t,1)},beforeUpload:function(t){return t.status="waiting",this.fileList=[t],!1},jumpPath:function(t){for(var e=this.paths,a=e[0],i=1;i<=t;i++)a+="/"+e[i];this.$emit("get-file-list",a.replace(/\/\//g,"/"))}})}},1308:function(t,e,a){"use strict";var i={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"action-group"},[t.paths.length>1?a("gs-button",{on:{click:function(e){t.jumpPath(t.paths.length-2)}}},[t._v("返回上一级")]):t._e(),t._v(" "),a("gs-button",{attrs:{type:"primary"},on:{click:t.open}},[t._v("上传文件")]),t._v(" "),a("span",{staticClass:"file-path-text"},[t._v(t._s(t.filePath))]),t._v(" "),a("gs-modal",{staticClass:"file-management-upload-modal",attrs:{title:"上传文件"},model:{value:t.visible,callback:function(e){t.visible=e},expression:"visible"}},[a("gs-row",[a("gs-col",{staticClass:"upload-label",attrs:{span:t.labelSpan}},[t._v("选择文件：")]),t._v(" "),a("gs-col",{attrs:{span:t.cntSpan}},[a("gs-upload",{attrs:{"before-upload":t.beforeUpload}},[a("gs-button",{attrs:{type:"primary"}},[t._v("选择文件")])],1),t._v(" "),a("ul",{staticClass:"upload-files"},t._l(t.fileList,function(e,i){return a("li",{key:i,staticClass:"upload-file-item"},[t._v("\n            "+t._s(e.name)+"\n            "),t.loading?t._e():a("gs-icon",{attrs:{name:"close",type:"primary",mode:"button"},on:{click:function(e){t.deleteFile(i)}}})],1)}))],1)],1),t._v(" "),a("gs-row",[a("gs-col",{staticClass:"path-label",attrs:{span:t.labelSpan}},[t._v("位置：")]),t._v(" "),a("gs-col",{attrs:{span:t.cntSpan}},[a("span",[t._v(t._s(t.filePath||"/"))])])],1),t._v(" "),a("template",{slot:"footer"},[a("gs-button",{attrs:{loading:t.loading,type:"primary"},on:{click:t.handleUpload}},[t._v("确定")]),t._v(" "),a("gs-button",{attrs:{disabled:t.loading},on:{click:t.close}},[t._v("取消")])],1)],2)],1)},staticRenderFns:[]};e.a=i},1309:function(t,e,a){"use strict";var i=a(1310),n=a(1311),s=a(47)(i.a,n.a,null,null,null);e.a=s.exports},1310:function(t,e,a){"use strict";var i=a(5),n=a.n(i),s=a(0),r=a.n(s);e.a={props:{query:{type:Object,default:function(){return{}}}},data:function(){var t=this.query.modifyDate;return{params:n()({},this.query),modifyDate:t?r()(t):null}},methods:{handleSearch:function(){var t=this.modifyDate;this.$emit("search",n()({},this.params,{modifyDate:t?t.format("YYYY-MM-DD"):null}))}}}},1311:function(t,e,a){"use strict";var i={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"query-form"},[a("gs-form",{ref:"queryForm",attrs:{model:t.query,inline:""}},[a("gs-form-item",{attrs:{label:"文件名称",prop:"fileName"}},[a("gs-input",{attrs:{placeholder:"请输入文件名称"},model:{value:t.params.fileName,callback:function(e){t.$set(t.params,"fileName",e)},expression:"params.fileName"}})],1),t._v(" "),a("gs-form-item",{attrs:{label:"修改时间",prop:"modifyDate"}},[a("gs-date-picker",{attrs:{"input-type":"input",type:"date"},model:{value:t.modifyDate,callback:function(e){t.modifyDate=e},expression:"modifyDate"}})],1),t._v(" "),a("gs-form-item",{attrs:{label:"持有人",prop:"owner"}},[a("gs-input",{attrs:{placeholder:"请输入持有人"},model:{value:t.params.owner,callback:function(e){t.$set(t.params,"owner",e)},expression:"params.owner"}})],1),t._v(" "),a("gs-form-item",[a("gs-button",{attrs:{type:"primary"},on:{click:t.handleSearch}},[t._v("查询")])],1)],1)],1)},staticRenderFns:[]};e.a=i},1312:function(t,e){},1313:function(t,e,a){"use strict";var i={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"file-management-page"},[a("div",{staticClass:"content-header"},[a("bread-crumb",{attrs:{"current-level":"文件管理"}})],1),t._v(" "),a("div",{staticClass:"content-body"},[a("action-group",{attrs:{"file-path":t.filePath},on:{"get-file-list":t.queryFileList,"upload-success":function(e){t.queryFileList(t.filePath)}}}),t._v(" "),a("search-form",{attrs:{query:t.formData},on:{search:t.handleSearch}}),t._v(" "),a("data-table",{attrs:{loading:t.loading,"table-props":t.tableProps,"pagination-props":t.paginationProps},on:{"sort-change":t.handleSortChange,"page-change":t.handlePageChange,"page-size-change":t.handlePageSizeChange}},[a("el-table-column",{attrs:{prop:"fileName",label:"名称",sortable:""},scopedSlots:t._u([{key:"default",fn:function(e){return["Directory"===e.row.type?a("a",{attrs:{title:"点击打开"},on:{click:function(a){t.handleFileClick(e)}}},[a("gs-icon",{attrs:{name:" iconfont icon-wenjianjia1"}}),t._v(t._s(e.row.fileName)+"\n          ")],1):a("span",{staticClass:"file-name",attrs:{title:"点击下载"},on:{click:function(a){t.handleFileClick(e)}}},[a("gs-icon",{attrs:{name:" iconfont icon-file"}}),t._v(t._s(e.row.fileName)+"\n          ")],1)]}}])}),t._v(" "),a("el-table-column",{attrs:{prop:"size",label:"大小"},scopedSlots:t._u([{key:"default",fn:function(e){return[t._v("\n          "+t._s(t._f("formatSize")(e.row))+"\n        ")]}}])}),t._v(" "),a("el-table-column",{attrs:{prop:"modificationTime",label:"最后修改时间",width:"180"}}),t._v(" "),a("el-table-column",{attrs:{prop:"owner",label:"持有人",align:"right"}})],1)],1)])},staticRenderFns:[]};e.a=i},473:function(t,e,a){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var i=a(1305),n=a(1313),s=a(47)(i.a,n.a,null,null,null);e.default=s.exports},499:function(t,e,a){"use strict";var i=a(500),n=a(503);a.n(n);e.a=i.a},500:function(t,e,a){"use strict";var i=a(501),n=a(502),s=a(47)(i.a,n.a,null,null,null);e.a=s.exports},501:function(t,e,a){"use strict";e.a={name:"BreadCrumb",props:{currentLevel:{type:String,required:!0},upperLevel:{type:String,default:""},backPath:{type:String,default:""}},data:function(){return{}},computed:{isHideNav:function(){return!!this.$router.currentRoute.query.nav}}}},502:function(t,e,a){"use strict";var i={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return!t.upperLevel||t.isHideNav?a("div",{staticClass:"content-header-single"},[t._t("header",[t._v("\n    "+t._s(t.currentLevel)+"\n  ")])],2):a("div",{staticClass:"content-header-both"},[a("div",{staticClass:"back-link"},[a("router-link",{attrs:{to:t.backPath}},[a("gs-button",{staticClass:"back-btn",attrs:{type:"primary"}},[a("i",{staticClass:"gs-icon gs-icon-revoke"})])],1)],1),t._v(" "),a("div",{staticClass:"nav-name"},[a("div",{staticClass:"current-nav"},[t._v(t._s(t.currentLevel))]),t._v(" "),a("div",{staticClass:"upper-nav"},[t._v(t._s(t.upperLevel))])])])},staticRenderFns:[]};e.a=i},503:function(t,e){},615:function(t,e,a){"use strict";var i=a(616);e.a=i.a},616:function(t,e,a){"use strict";var i=a(617),n=a(618),s=a(47)(i.a,n.a,null,null,null);e.a=s.exports},617:function(t,e,a){"use strict";e.a={props:{tableProps:{type:Object,default:function(){return{}}},paginationProps:{type:Object,default:function(){return{}}},loading:{type:Boolean,default:!1}},methods:{sortChange:function(t){var e=t.column,a=t.prop,i=t.order;this.$emit("sort-change",{column:e,prop:a,order:i})},handleSelectionChange:function(t){this.$emit("selection-change",t)},pageChange:function(t){this.$emit("page-change",t)},pageSizeChange:function(t){this.$emit("page-size-change",t)}}}},618:function(t,e,a){"use strict";var i={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"data-list"},[a("el-table",t._b({directives:[{name:"loading",rawName:"v-loading",value:t.loading,expression:"loading"}],on:{"sort-change":t.sortChange,"selection-change":t.handleSelectionChange}},"el-table",t.tableProps,!1),[t._t("default")],2),t._v(" "),a("gs-pagination",t._b({attrs:{layout:"pagesizes,pager,jumper,jumpbtn"},on:{"page-change":t.pageChange,"page-size-change":t.pageSizeChange}},"gs-pagination",t.paginationProps,!1))],1)},staticRenderFns:[]};e.a=i}});
//# sourceMappingURL=4.b9dc524c.js.map