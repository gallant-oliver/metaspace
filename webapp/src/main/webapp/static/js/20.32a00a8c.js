webpackJsonp([20],{1004:function(e,t,a){"use strict";t.b=function(e){return n.a.post("/privilege/users",e)},t.a=function(e){return n.a.get("/privilege/users/"+e)};var n=a(773)},2089:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var n=a(2090),i=a(2092),o=a(107)(n.a,i.a,null,null,null);t.default=o.exports},2090:function(e,t,a){"use strict";var n=a(2091),i=(a.n(n),a(1004)),o=a(774);t.a={components:{dataTable:o.a},data:function(){return{keyword:"",tableData:[],pagination:{total:0,pageSize:10,pageSizes:[10,20,30],current:1},loading:!1}},computed:{},created:function(){this.fetchUserList(1)},methods:{fetchUserList:function(){var e=this;1===(arguments.length>0&&void 0!==arguments[0]?arguments[0]:0)&&(this.pagination.current=1),this.loading=!0,i.b({offset:(this.pagination.current-1)*this.pagination.pageSize,limit:this.pagination.pageSize,query:this.keyword}).then(function(t){t.lists.map(function(e){return e.roles=[e.roleName],e}),e.tableData=t.lists,e.pagination.total=t.sum,e.loading=!1}).catch(function(t){e.loading=!1})},pageChange:function(e){this.pagination.current=e,this.fetchUserList()},sizeChange:function(e){this.pagination.pageSize=e,this.fetchUserList(1)},checkDetail:function(e){var t=e.row;t.userId&&this.$router.push("/main/auth/userManagement/"+t.userId)}}}},2091:function(e,t){},2092:function(e,t,a){"use strict";var n={render:function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",{staticClass:"user-mangement"},[a("div",{staticClass:"search-box"},[a("gs-search",{attrs:{placeholder:"请输入姓名或帐号"},on:{clear:function(t){e.fetchUserList(1)},search:e.fetchUserList},model:{value:e.keyword,callback:function(t){e.keyword=t},expression:"keyword"}})],1),e._v(" "),a("data-table",{attrs:{loading:e.loading,"table-props":{data:e.tableData},"pagination-props":e.pagination},on:{"page-change":e.pageChange,"page-size-change":e.sizeChange}},[a("el-table-column",{attrs:{prop:"account",label:"帐号"}}),e._v(" "),a("el-table-column",{attrs:{prop:"username",label:"姓名"}}),e._v(" "),a("el-table-column",{attrs:{prop:"roles",label:"角色"},scopedSlots:e._u([{key:"default",fn:function(t){return e._l(t.row.roles,function(t){return a("gs-tag",{key:t},[e._v("\n          "+e._s(t))])})}}])}),e._v(" "),a("el-table-column",{attrs:{label:"操作",width:"100",fixed:"right"},scopedSlots:e._u([{key:"default",fn:function(t){return[a("gs-button",{attrs:{type:"text-primary"},on:{click:function(a){e.checkDetail(t)}}},[e._v("详情")])]}}])})],1)],1)},staticRenderFns:[]};t.a=n},774:function(e,t,a){"use strict";var n=a(782);t.a=n.a},782:function(e,t,a){"use strict";var n=a(783),i=a(784),o=a(107)(n.a,i.a,null,null,null);t.a=o.exports},783:function(e,t,a){"use strict";t.a={props:{tableProps:{type:Object,default:function(){return{}}},paginationProps:{type:Object,default:function(){return{}}},loading:{type:Boolean,default:!1}},methods:{sortChange:function(e){var t=e.column,a=e.prop,n=e.order;this.$emit("sort-change",{column:t,prop:a,order:n})},handleSelectionChange:function(e){this.$emit("selection-change",e)},pageChange:function(e){this.$emit("page-change",e)},pageSizeChange:function(e){this.$emit("page-size-change",e)},clearSelection:function(){this.$refs.dataTable.clearSelection()},getTable:function(){return this.$refs.dataTable},doLayout:function(){this.$refs.dataTable.doLayout()}}}},784:function(e,t,a){"use strict";var n={render:function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",{staticClass:"data-list"},[a("el-table",e._b({directives:[{name:"loading",rawName:"v-loading",value:e.loading,expression:"loading"}],ref:"dataTable",on:{"sort-change":e.sortChange,"selection-change":e.handleSelectionChange}},"el-table",e.tableProps,!1),[e._t("default")],2),e._v(" "),a("gs-pagination",e._b({attrs:{"page-sizes":[10,20,30,40,50,100],layout:e.paginationProps.layout||"range,total,pagesizes,pager,jumper,jumpbtn"},on:{"page-change":e.pageChange,"page-size-change":e.pageSizeChange}},"gs-pagination",e.paginationProps,!1))],1)},staticRenderFns:[]};t.a=n}});
//# sourceMappingURL=20.32a00a8c.js.map