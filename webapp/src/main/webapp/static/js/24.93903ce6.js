webpackJsonp([24],{2082:function(t,n,s){"use strict";Object.defineProperty(n,"__esModule",{value:!0});var e=s(2083),a=s(2089),i=s(107)(e.a,a.a,null,null,null);n.default=i.exports},2083:function(t,n,s){"use strict";var e=s(2084);n.a={components:{page404:e.a},data:function(){var t=this;return{statePageConfig:{imgUrl:s(2088),msg:{title:"404",content:"提示文字可自定义",notice:""},myClass:"myClass",buttonConfig:{name:"返回首页",callback:function(){t.$router.push({path:"/home"})}}}}},methods:{handTrigger:function(){this.$router.push({path:"/home"})}}}},2084:function(t,n,s){"use strict";var e=s(2085),a=s(2087),i=s(107)(e.a,a.a,null,null,null);n.a=i.exports},2085:function(t,n,s){"use strict";var e=s(2086);s.n(e);n.a={props:{imgUrl:{type:String,required:!0,default:""},msg:{type:Object,default:function(){return{title:null,content:null,notice:null}}},myClass:{type:String,default:""},buttonConfig:{type:Object,default:function(){return{}}}},data:function(){return{}}}},2086:function(t,n){},2087:function(t,n,s){"use strict";var e={render:function(){var t=this,n=t.$createElement,s=t._self._c||n;return s("div",{staticClass:"page-400",class:t.myClass},[s("div",{staticClass:"center-box"},[s("div",{staticClass:"left"},[s("img",{attrs:{src:t.imgUrl,alt:"404图片"}})]),t._v(" "),s("div",{staticClass:"right"},[s("h1",{staticClass:"note-error"},[t._v(t._s(t.msg.title||"404"))]),t._v(" "),s("h2",{staticClass:"note-result"},[t._v(t._s(t.msg.content||"页面不存在"))]),t._v(" "),s("h4",{staticClass:"note-link"},[t._v(t._s(t.msg.notice||"如有问题请联系管理员"))]),t._v(" "),t.buttonConfig?s("gs-button",{staticClass:"btn",attrs:{type:"primary"},on:{click:t.buttonConfig.callback}},[t._v("\n        "+t._s(t.buttonConfig.name||"返回首页")+"\n      ")]):t._e()],1)])])},staticRenderFns:[]};n.a=e},2088:function(t,n,s){t.exports=s.p+"static/images/404.a57b6f31fa77c50f14d756711dea4158.png"},2089:function(t,n,s){"use strict";var e={render:function(){var t=this.$createElement;return(this._self._c||t)("page404",{attrs:{"img-url":this.statePageConfig.imgUrl,msg:this.statePageConfig.msg,"my-class":this.statePageConfig.myClass,"button-config":this.statePageConfig.buttonConfig}})},staticRenderFns:[]};n.a=e}});
//# sourceMappingURL=24.93903ce6.js.map