webpackJsonp([22],{2098:function(t,e,n){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var r=n(2099),a=n(2101),s=n(107)(r.a,a.a,null,null,null);e.default=s.exports},2099:function(t,e,n){"use strict";var r=n(2100),a=(n.n(r),n(775));e.a={components:{breadcrumb:a.a},data:function(){return{}},computed:{title:function(){var t=this.$route.path;switch(t.substring(t.lastIndexOf("/")+1,t.length)){case"userManagement":return"用户管理";case"roleManagement":return"角色管理";case"authManagement":return"权限管理";default:return""}},backPath:function(){return this.$route.meta.backPath||""},currentLevel:function(){return this.$route.meta.currentLevel||""},upperLevel:function(){return this.$route.meta.upperLevel||""}},methods:{}}},2100:function(t,e){},2101:function(t,e,n){"use strict";var r={render:function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticClass:"auth"},[t.title?n("div",{staticClass:"title"},[t._v("\n    "+t._s(t.title)+"\n  ")]):n("breadcrumb",{attrs:{"back-path":t.backPath,"current-level":t.currentLevel,"upper-level":t.upperLevel}}),t._v(" "),n("div",{staticClass:"wrap"},[n("div",{staticClass:"content"},[n("router-view")],1)])],1)},staticRenderFns:[]};e.a=r},775:function(t,e,n){"use strict";var r=n(777),a=n(780);n.n(a);e.a=r.a},777:function(t,e,n){"use strict";var r=n(778),a=n(779),s=n(107)(r.a,a.a,null,null,null);e.a=s.exports},778:function(t,e,n){"use strict";e.a={name:"BreadCrumb",props:{currentLevel:{type:String,required:!0},upperLevel:{type:String,default:""},backPath:{type:String,default:""}},data:function(){return{}},computed:{isHideNav:function(){return!!this.$router.currentRoute.query.nav}},methods:{backBtnClick:function(){this.$emit("back")}}}},779:function(t,e,n){"use strict";var r={render:function(){var t=this,e=t.$createElement,n=t._self._c||e;return!t.upperLevel||t.isHideNav?n("div",{staticClass:"content-header-single"},[t._t("header",[t._v("\n    "+t._s(t.currentLevel)+"\n  ")])],2):n("div",{staticClass:"content-header-both"},[n("div",{staticClass:"back-link"},[n("router-link",{attrs:{to:t.backPath}},[n("gs-button",{staticClass:"back-btn",attrs:{type:"primary"},on:{click:t.backBtnClick}},[n("i",{staticClass:"gs-icon gs-icon-revoke"})])],1)],1),t._v(" "),n("div",{staticClass:"nav-name"},[n("div",{staticClass:"current-nav"},[t._v(t._s(t.currentLevel))]),t._v(" "),n("div",{staticClass:"upper-nav"},[t._v(t._s(t.upperLevel))])])])},staticRenderFns:[]};e.a=r},780:function(t,e){}});
//# sourceMappingURL=22.63169012.js.map