(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-29df7351"],{"0290":function(e,t,a){"use strict";var n=function(){var e=this,t=e.$createElement,a=e._self._c||t;return"timestamp"===e.type?a("gs-date-picker",{attrs:{value:e.valueCache,disabled:e.disabled,type:"date-time"},on:{change:e.change}}):"boolean"===e.type?a("gs-radio-group",{attrs:{value:e.valueCache,disabled:e.disabled},on:{change:e.change}},[a("gs-radio",{attrs:{label:!0}}),a("gs-radio",{attrs:{label:!1}})],1):a("gs-input",{staticClass:"fileds-input",attrs:{placeholder:e.isNumber?"请输入数字":"",value:e.valueCache,disabled:e.disabled},on:{change:e.change}})},i=[],s=(a("b1b1"),a("f559"),a("c5f6"),a("c1df")),o=a.n(s),l=a("ed08"),r={name:"FieldsInput",props:{value:{type:[String,Number,Object,Array,Boolean],default:""},disabled:{type:Boolean,default:!1},type:{type:String,default:""}},data:function(){return{numberType:l["b"]}},computed:{isNumber:function(){var e=this;return this.numberType.some(function(t){return e.type.startsWith(t)})},valueCache:function(){return"timestamp"===this.type&&""!==this.value?o()(this.value):this.value}},methods:{change:function(e){var t="";t="timestamp"===this.type?e.valueOf():this.isNumber&&Number.isSafeInteger(parseFloat(e))?parseFloat(e):e,this.$emit("input",t)}}},u=r,c=(a("43f5"),a("2877")),d=Object(c["a"])(u,n,i,!1,null,null,null);t["a"]=d.exports},"0a49":function(e,t,a){var n=a("9b43"),i=a("626a"),s=a("4bf8"),o=a("9def"),l=a("cd1c");e.exports=function(e,t){var a=1==e,r=2==e,u=3==e,c=4==e,d=6==e,f=5==e||d,m=t||l;return function(t,l,p){for(var h,g,b=s(t),v=i(b),y=n(l,p,3),N=o(v.length),k=0,x=a?m(t,N):r?m(t,0):void 0;N>k;k++)if((f||k in v)&&(h=v[k],g=y(h,k,b),e))if(a)x[k]=g;else if(g)switch(e){case 3:return!0;case 5:return h;case 6:return k;case 2:x.push(h)}else if(c)return!1;return d?-1:u||c?c:x}}},1169:function(e,t,a){var n=a("2d95");e.exports=Array.isArray||function(e){return"Array"==n(e)}},"20d6":function(e,t,a){"use strict";var n=a("5ca1"),i=a("0a49")(6),s="findIndex",o=!0;s in[]&&Array(1)[s](function(){o=!1}),n(n.P+n.F*o,"Array",{findIndex:function(e){return i(this,e,arguments.length>1?arguments[1]:void 0)}}),a("9c6c")(s)},"28a5":function(e,t,a){"use strict";var n=a("aae3"),i=a("cb7c"),s=a("ebd6"),o=a("0390"),l=a("9def"),r=a("5f1b"),u=a("520a"),c=a("79e5"),d=Math.min,f=[].push,m="split",p="length",h="lastIndex",g=4294967295,b=!c(function(){RegExp(g,"y")});a("214f")("split",2,function(e,t,a,c){var v;return v="c"=="abbc"[m](/(b)*/)[1]||4!="test"[m](/(?:)/,-1)[p]||2!="ab"[m](/(?:ab)*/)[p]||4!="."[m](/(.?)(.?)/)[p]||"."[m](/()()/)[p]>1||""[m](/.?/)[p]?function(e,t){var i=String(this);if(void 0===e&&0===t)return[];if(!n(e))return a.call(i,e,t);var s,o,l,r=[],c=(e.ignoreCase?"i":"")+(e.multiline?"m":"")+(e.unicode?"u":"")+(e.sticky?"y":""),d=0,m=void 0===t?g:t>>>0,b=new RegExp(e.source,c+"g");while(s=u.call(b,i)){if(o=b[h],o>d&&(r.push(i.slice(d,s.index)),s[p]>1&&s.index<i[p]&&f.apply(r,s.slice(1)),l=s[0][p],d=o,r[p]>=m))break;b[h]===s.index&&b[h]++}return d===i[p]?!l&&b.test("")||r.push(""):r.push(i.slice(d)),r[p]>m?r.slice(0,m):r}:"0"[m](void 0,0)[p]?function(e,t){return void 0===e&&0===t?[]:a.call(this,e,t)}:a,[function(a,n){var i=e(this),s=void 0==a?void 0:a[t];return void 0!==s?s.call(a,i,n):v.call(String(i),a,n)},function(e,t){var n=c(v,e,this,t,v!==a);if(n.done)return n.value;var u=i(e),f=String(this),m=s(u,RegExp),p=u.unicode,h=(u.ignoreCase?"i":"")+(u.multiline?"m":"")+(u.unicode?"u":"")+(b?"y":"g"),y=new m(b?u:"^(?:"+u.source+")",h),N=void 0===t?g:t>>>0;if(0===N)return[];if(0===f.length)return null===r(y,f)?[f]:[];var k=0,x=0,C=[];while(x<f.length){y.lastIndex=b?x:0;var _,A=r(y,b?f:f.slice(x));if(null===A||(_=d(l(y.lastIndex+(b?0:x)),f.length))===k)x=o(f,x,p);else{if(C.push(f.slice(k,x)),C.length===N)return C;for(var w=1;w<=A.length-1;w++)if(C.push(A[w]),C.length===N)return C;x=k=_}}return C.push(f.slice(k)),C}]})},3598:function(e,t,a){},"36bd":function(e,t,a){"use strict";var n=a("4bf8"),i=a("77f1"),s=a("9def");e.exports=function(e){var t=n(this),a=s(t.length),o=arguments.length,l=i(o>1?arguments[1]:void 0,a),r=o>2?arguments[2]:void 0,u=void 0===r?a:i(r,a);while(u>l)t[l++]=e;return t}},3846:function(e,t,a){a("9e1e")&&"g"!=/./g.flags&&a("86cc").f(RegExp.prototype,"flags",{configurable:!0,get:a("0bfb")})},"43f5":function(e,t,a){"use strict";var n=a("e932"),i=a.n(n);i.a},6853:function(e,t,a){"use strict";a("6762"),a("2fdb"),a("6c7b"),a("ac6a"),a("456d"),a("6b54");var n=a("f63f"),i=a("bc3a"),s=a.n(i),o=a("ed08"),l=Math.random().toString(36).slice(-8);t["a"]={data:function(){return{testData:[],limit:10,offset:0,btnLoading:!1,activeName:"json",testRes:[],numberType:o["b"]}},computed:{tableHeader:function(){var e=this.testRes;return e&&e[0]?Object.keys(e[0]):[]},testResStringify:function(){return JSON.stringify(this.testRes,null,4)}},methods:{checkTestInput:function(){var e=this;if(""===this.offset||""===this.limit)return this.$Message.error("请填入limit和offset参数。"),!1;var t=this.testData.filter(function(e){return e.fill&&!e.check});if(t&&t.length){var a=t.map(function(e){return e.columnName}).join(",");return this.$Message.error("".concat(a,"字段为必填字段，请选中并输入数据")),!1}var n=this.testData.filter(function(e){return e.check}).filter(function(t){return!!(e.numberType.includes(t.type)&&""===t.value||"timestamp"===t.type&&""===t.value||"boolean"===t.type&&""===t.value)}).map(function(e){return e.columnName}).join(",");return!n||(this.$Message.error("".concat(n,"字段缺少过滤值内容，请输入")),!1)},stopTest:function(){var e=this;this.source.cancel(),n["stopTestApi"](l).then(function(t){e.$Message.success("停止成功")})},startTest:function(){var e=this;if(this.checkTestInput()){var t=this.detailData,a=t.tableGuid,i=t.dbGuid,o=t.maxRowNumber,r=this.testData.filter(function(e){return e.check}).map(function(e){return"timestamp"===e.type&&e.value?{columnName:e.columnName,value:[e.value.unix()]}:{columnName:e.columnName,value:[e.value]}}),u=this.detailData.fields.map(function(e){return e.columnName}),c={tableGuid:a,dbGuid:i,maxRowNumber:o,parameter:r,queryFields:u,limit:this.limit,offset:this.offset};l=Math.random().toString(36).slice(-8),this.btnLoading=!0;var d=s.a.CancelToken;this.source=d.source(),n["testApi"]({params:c,randomName:l},this.source.token).then(function(t){e.testRes=t,e.btnLoading=!1,e.$Message.success("测试完成！")}).catch(function(t){e.btnLoading=!1,console.log("Request canceled",t.message)})}}}}},"6b54":function(e,t,a){"use strict";a("3846");var n=a("cb7c"),i=a("0bfb"),s=a("9e1e"),o="toString",l=/./[o],r=function(e){a("2aba")(RegExp.prototype,o,e,!0)};a("79e5")(function(){return"/a/b"!=l.call({source:"a",flags:"b"})})?r(function(){var e=n(this);return"/".concat(e.source,"/","flags"in e?e.flags:!s&&e instanceof RegExp?i.call(e):void 0)}):l.name!=o&&r(function(){return l.call(this)})},"6c7b":function(e,t,a){var n=a("5ca1");n(n.P,"Array",{fill:a("36bd")}),a("9c6c")("fill")},7514:function(e,t,a){"use strict";var n=a("5ca1"),i=a("0a49")(5),s="find",o=!0;s in[]&&Array(1)[s](function(){o=!1}),n(n.P+n.F*o,"Array",{find:function(e){return i(this,e,arguments.length>1?arguments[1]:void 0)}}),a("9c6c")(s)},"9c12":function(e,t,a){var n=a("d3f4"),i=Math.floor;e.exports=function(e){return!n(e)&&isFinite(e)&&i(e)===e}},b1b1:function(e,t,a){var n=a("5ca1"),i=a("9c12"),s=Math.abs;n(n.S,"Number",{isSafeInteger:function(e){return i(e)&&s(e)<=9007199254740991}})},cb52:function(e,t,a){"use strict";a.r(t);var n=function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",{staticClass:"create-api"},[a("gs-button",{staticClass:"revoke",attrs:{type:"primary",icon:"revoke"},on:{click:e.returnList}}),a("gs-steps",{attrs:{active:e.activeStep,"align-center":"",space:"160px",size:"small"}},[a("template",{slot:"header"},e._l(e.stepsTitle,function(e,t){return a("gs-step",{key:t,attrs:{title:e}})}),1),a("template",{slot:"content"},[a("div",{directives:[{name:"loading",rawName:"v-loading",value:e.loading,expression:"loading"}],staticClass:"steps-content"},[a("transition",{attrs:{name:"fade-transform-right",mode:"out-in"}},[a("keep-alive",[e.hasData?a(e.component,{tag:"component",attrs:{"is-edit":e.isEdit,data:e.apiData,"active-step":e.activeStep},on:{next:e.handleNext,back:e.handlePrev}}):e._e()],1)],1)],1)])],2)],1)},i=[],s=(a("3598"),function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",{directives:[{name:"loading",rawName:"v-loading",value:e.loading,expression:"loading"}],staticClass:"data-source"},[a("gs-form",{ref:"form",attrs:{model:e.form,rules:e.rules,"label-position":"top"}},[a("gs-form-item",{attrs:{label:"数据库",prop:"dbGuid"}},[a("gs-select",{attrs:{disabled:e.columnLoading||e.tableLoading,searchable:""},on:{change:e.dbSelectChange},model:{value:e.form.dbGuid,callback:function(t){e.$set(e.form,"dbGuid",t)},expression:"form.dbGuid"}},e._l(e.databaseOptions,function(e){return a("gs-option",{key:e.databaseId,attrs:{label:e.databaseName,value:e.databaseId}})}),1)],1),a("gs-form-item",{attrs:{label:"数据表",prop:"tableGuid"}},[a("gs-select",{attrs:{disabled:e.columnLoading,searchable:""},on:{"visible-change":e.tableSelectVisibleChange},model:{value:e.form.tableGuid,callback:function(t){e.$set(e.form,"tableGuid",t)},expression:"form.tableGuid"}},e._l(e.tableOptions,function(e){return a("gs-option",{key:e.tableGuid,attrs:{label:e.displayName+"（"+e.tableName+"）",value:e.tableGuid}})}),1)],1),a("gs-form-item",{attrs:{label:"最大结果行数",prop:"maxRowNumber"}},[a("gs-input-number",{attrs:{min:1,max:2e3},model:{value:e.form.maxRowNumber,callback:function(t){e.$set(e.form,"maxRowNumber",t)},expression:"form.maxRowNumber"}})],1),a("gs-form-item",{attrs:{label:"字段",prop:"column"}},[a("div",{staticClass:"column"},[a("gs-modal-select",{attrs:{loading:e.columnLoading,"right-selected-render":e.rightSelectedRender,flatten:""},on:{change:e.handleCheckedColumnChange},model:{value:e.form.column,callback:function(t){e.$set(e.form,"column",t)},expression:"form.column"}},e._l(e.columnOptions,function(e){return a("gs-option",{key:e.columnId,attrs:{label:e.displayName+"（"+e.columnName+"）",value:e.columnName,desc:e.columnId}})}),1),a("div",{staticClass:"line"}),a("div",{staticClass:"desc-label"},[e._v("\n          描述\n        ")])],1)])],1),a("div",{directives:[{name:"show",rawName:"v-show",value:e.columnOptions&&e.columnOptions.length,expression:"columnOptions && columnOptions.length"}],staticClass:"choose-all-fileds"},[a("gs-checkbox",{attrs:{indeterminate:e.isIndeterminate},on:{change:e.handleCheckAllChange},model:{value:e.checkAll,callback:function(t){e.checkAll=t},expression:"checkAll"}},[e._v("\n      全选\n    ")])],1),a("div",{staticClass:"operation"},[a("gs-button",{attrs:{type:"primary"},on:{click:e.handleNext}},[e._v("\n      下一步\n    ")])],1)],1)}),o=[],l=(a("6762"),a("2fdb"),a("db72")),r=(a("20d6"),a("7514"),a("ac6a"),a("456d"),a("2f62")),u={computed:Object(l["a"])({},Object(r["c"])("datashare",["storeApiMsg"])),methods:Object(l["a"])({},Object(r["b"])("datashare",["saveApiMsg"]))},c=a("f63f"),d={mixins:[u],props:{data:{type:Object,default:function(){return{}}}},data:function(){var e=function(e,t,a){""===t&&a(new Error("请填写最大结果行数")),t&&/\d/.test(t)?a():a(new Error("最大结果行数必须为数字且不能为0"))};return{form:{dbGuid:"",tableGuid:"",column:[],maxRowNumber:10},loading:!1,tableOptions:[],databaseOptions:[],columnOptions:[],columnLoading:!1,tableLoading:!1,rules:{dbGuid:[{required:!0,trigger:"change",message:"请选择数据库"}],tableGuid:[{required:!0,trigger:"change",message:"请选择数据表"}],column:[{required:!0,trigger:"change",message:"请选择字段",type:"array"}],maxRowNumber:[{required:!0,trigger:"blur",validator:e}]},isIndeterminate:!1,checkAll:!1}},watch:{"form.tableGuid":{handler:function(e){var t=this;e&&(this.form.column=[],this.$nextTick(function(e){t.$refs.form.clearValidate("column")}),this.fetchColumn())}}},created:function(){var e=this;this.fetchDatabase().then(function(t){e.data&&Object.keys(e.data).length&&e.setData()})},methods:{setData:function(){var e=this,t=this.data,a=t.dbGuid,n=t.tableGuid,i=t.maxRowNumber,s=t.fields,o=s.map(function(e){return e.columnName});this.form={dbGuid:a,tableGuid:n,maxRowNumber:i,column:[]},this.$nextTick(function(t){e.$refs.form.clearValidate("column")});var l=this.databaseOptions.find(function(t){return t.databaseId===e.form.dbGuid});l||(this.form.dbGuid="",this.form.tableGuid="",this.$Message.warning("无该库权限")),this.tableSelectVisibleChange(!0),l&&this.fetchColumn().then(function(t){e.$nextTick(function(t){e.form.column=o})})},fetchDatabase:function(){var e=this;return this.loading=!0,c["getDataBase"]({offset:0,limit:-1,query:""}).then(function(t){e.databaseOptions=t.lists,e.loading=!1})},fetchTable:function(){var e=this;return this.tableLoading=!0,c["getTable"]({dbGuid:this.form.dbGuid,offset:0,limit:-1,query:""}).then(function(t){e.tableOptions=t.lists,e.tableLoading=!1;var a=e.tableOptions.find(function(t){return t.tableGuid===e.form.tableGuid});a||(e.form.tableGuid="")})},fetchColumn:function(){var e=this;return this.columnLoading=!0,c["getColumn"](this.form.tableGuid).then(function(t){return e.columnOptions=t,e.columnLoading=!1,t})},tableSelectVisibleChange:function(e){e&&(this.form.dbGuid?(this.fetchTable(),this.handleCheckedColumnChange([])):this.$refs.form.validateField("dbGuid"))},rightSelectedRender:function(e,t){var a=this.columnOptions.find(function(e){return t.value===e.columnName});return e("div",[e("span",{class:"label",attrs:{title:a.displayName+"（"+a.tableName+"）"}},[a.displayName+"（"+a.tableName+"）"]),e("span",{class:"desc",attrs:{title:a.description}},[a.description||"-"]),e("i",{class:"gs-icon-close",on:{click:this.unselectOption.bind(this,t)}})])},unselectOption:function(e){var t=this.form.column.findIndex(function(t){return t===e.value});this.form.column.splice(t,1)},handleNext:function(){var e=this;this.$refs.form.validate(function(t){t&&(e.saveApiMsg(Object(l["a"])({},e.form,{column:e.columnOptions.filter(function(t){return e.form.column.includes(t.columnName)})})),e.$emit("next"))})},dbSelectChange:function(){this.form.tableGuid="",this.form.column=[],this.columnOptions=[]},handleCheckAllChange:function(e){this.form.column=e.target.checked?this.columnOptions.map(function(e){return e.columnName}):[],this.isIndeterminate=!1},handleCheckedColumnChange:function(e){var t=e.length;this.checkAll=t>0,this.isIndeterminate=t>0&&t<this.columnOptions.length}}},f=d,m=a("2877"),p=Object(m["a"])(f,s,o,!1,null,null,null),h=p.exports,g=function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",{directives:[{name:"loading",rawName:"v-loading",value:e.loading,expression:"loading"}],staticClass:"base-info"},[a("gs-form",{ref:"form",attrs:{model:e.form,rules:e.rules,"label-position":"top"}},[a("gs-form-item",{attrs:{label:"API名称",prop:"name"}},[a("gs-input",{attrs:{placeholder:"请填写API名称"},model:{value:e.form.name,callback:function(t){e.$set(e.form,"name",t)},expression:"form.name"}})],1),a("gs-form-item",{attrs:{label:"API分组",prop:"groupGuid"}},[a("gs-select",{attrs:{placeholder:"请选择API分组",searchable:""},model:{value:e.form.groupGuid,callback:function(t){e.$set(e.form,"groupGuid",t)},expression:"form.groupGuid"}},e._l(e.groupOptions,function(e){return a("gs-option",{key:e.guid,attrs:{label:e.name,value:e.guid}})}),1)],1),a("gs-form-item",{attrs:{label:"版本",prop:"version"}},[a("gs-input-number",{attrs:{min:0,placeholder:"请填写API版本"},model:{value:e.form.version,callback:function(t){e.$set(e.form,"version",t)},expression:"form.version"}})],1),a("gs-form-item",{attrs:{label:"描述",prop:"description"}},[a("gs-textarea",{attrs:{placeholder:"请填写描述信息"},model:{value:e.form.description,callback:function(t){e.$set(e.form,"description",t)},expression:"form.description"}})],1)],1),a("div",{staticClass:"info"},[a("div",[a("span",[e._v("协议类型")]),a("span",[e._v(e._s(e.form.protocol))])]),a("div",[a("span",[e._v("请求方式")]),a("span",[e._v(e._s(e.form.requestMode))])]),a("div",[a("span",[e._v("返回类型")]),a("span",[e._v(e._s(e.form.returnType))])]),a("div",[a("span",[e._v("API路径")]),a("span",[e._v(e._s(e.form.path))])])]),a("div",{staticClass:"operation"},[a("gs-button",{on:{click:e.handleBack}},[e._v("\n      上一步\n    ")]),a("gs-button",{attrs:{type:"primary"},on:{click:e.handleNext}},[e._v("\n      下一步\n    ")])],1)],1)},b=[],v=(a("c5f6"),a("7f7f"),a("28a5"),a("6b54"),{mixins:[u],props:{data:{type:Object,default:function(){return{}}}},data:function(){return{form:{name:"",groupGuid:"",version:1,description:"",path:"",returnType:"JSON | XML",protocol:"HTTP",requestMode:"POST"},getRandString:(new Date).getTime().toString(36),rules:{name:[{required:!0,trigger:"blur",message:"请填写API名称"}],groupGuid:[{required:!0,trigger:"change",message:"请选择API分组"}],version:[{validator:function(e,t,a){console.log(t),t||0===t?t>1e4?a(new Error("API版本不能大于 10000 ！")):a():a(new Error("请填写API版本"))}}]}}},computed:Object(l["a"])({},Object(r["c"])("datashare",["sourceGroupTree"]),{groupOptions:{set:function(e){return e},get:function(){return this.sourceGroupTree.length?this.sourceGroupTree.filter(function(e){return e.parentGuid}):[]}}}),watch:{"form.version":{handler:function(e){if(this.$route.query.isEdit){var t=this.data.path.split("/").reverse()[0];this.form.path="/api/v"+e+"/share/"+t}else this.form.path="/api/v"+e+"/share/"+this.getRandString},immediate:!0}},created:function(){this.fetchGroupList(),this.data&&Object.keys(this.data).length&&this.setData()},methods:{setData:function(){var e=this.data,t=e.name,a=e.groupGuid,n=e.protocol,i=e.requestMode,s=e.returnType,o=e.version,r=e.description;this.form=Object(l["a"])({},this.form,{name:this.$route.query.isEdit?t:"",groupGuid:a,version:Number(o),description:r,returnType:s,protocol:n,requestMode:i})},fetchGroupList:function(){var e=this;this.loading=!0,this.sourceGroupTree&&this.sourceGroupTree.length?(this.groupOptions=this.sourceGroupTree.filter(function(e){return e.parentGuid}),this.loading=!1):c["getGroupList"]().then(function(t){e.groupOptions=t.filter(function(e){return e.parentGuid}),e.loading=!1})},handleBack:function(){this.$emit("back")},handleNext:function(){var e=this;this.$refs.form.validate(function(t){t&&(e.data&&Object.keys(e.data).length&&e.form.name===e.data.name&&e.$route.query.isEdit?(e.saveApiMsg(e.form),e.$emit("next")):c["checkName"]({name:e.form.name}).then(function(t){t?e.$Message.error("已存在该名称"):(e.saveApiMsg(e.form),e.$emit("next"))}))})}}}),y=v,N=Object(m["a"])(y,g,b,!1,null,null,null),k=N.exports,x=function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",{staticClass:"api-config"},[a("div",{staticClass:"content"},[a("div",{staticClass:"all-fileds"},[a("div",{staticClass:"all-fileds-header"},[a("h4",{staticClass:"title"},[e._v("\n          已选字段\n        ")]),a("div",{directives:[{name:"show",rawName:"v-show",value:e.storeApiMsg.column&&e.storeApiMsg.column.length,expression:"storeApiMsg.column && storeApiMsg.column.length"}],staticClass:"choose-all-fileds"},[a("gs-checkbox",{attrs:{indeterminate:e.isIndeterminate},on:{change:e.handleCheckAllChange},model:{value:e.checkAll,callback:function(t){e.checkAll=t},expression:"checkAll"}},[e._v("\n            全选\n          ")])],1)]),a("ul",{staticClass:"container"},e._l(e.storeApiMsg.column||[],function(t,n){return a("li",{key:n,class:e.fieldsName.includes(t.columnName)?"active":"",on:{click:function(a){return e.chooseFields(t)}}},[e.fieldsName.includes(t.columnName)?a("i",{staticClass:"gs-icon-check"}):e._e(),a("span",{attrs:{title:t.displayName+"（"+t.columnName+"）"}},[e._v(e._s(t.displayName+"（"+t.columnName+"）"))]),a("i",{staticClass:"gs-icon-close",on:{click:function(a){return a.stopPropagation(),e.deleteField(t.columnName,n)}}})])}),0)]),a("div",{staticClass:"filter-fileds"},[a("h4",{staticClass:"title"},[e._v("\n        过滤条件字段\n      ")]),a("div",{staticClass:"container"},[a("el-table",{staticStyle:{width:"100%"},attrs:{data:e.fields}},[a("el-table-column",{attrs:{prop:"columnName",label:"名称","show-overflow-tooltip":""},scopedSlots:e._u([{key:"default",fn:function(t){return[a("span",[e._v(e._s(t.row.displayName+"（"+t.row.columnName+"）"))])]}}])}),a("el-table-column",{attrs:{prop:"fill",label:"必传","show-overflow-tooltip":""},scopedSlots:e._u([{key:"default",fn:function(t){return[a("span",[e._v(e._s(t.row.fill?"是":"否"))])]}}])}),a("el-table-column",{attrs:{prop:"type",label:"类型","show-overflow-tooltip":""}}),a("el-table-column",{attrs:{prop:"defaultValue",label:"默认值","show-overflow-tooltip":""},scopedSlots:e._u([{key:"default",fn:function(t){return[a("span",[e._v(e._s(t.row.defaultValue||"无"))])]}}])}),a("el-table-column",{attrs:{label:"操作"},scopedSlots:e._u([{key:"default",fn:function(t){return[a("div",[a("gs-button",{attrs:{type:"text-primary"},on:{click:function(a){return e.editFilterField(t)}}},[e._v("\n                  编辑\n                ")]),a("gs-button",{attrs:{type:"text-primary"},on:{click:function(a){return e.deleteField(t.row.columnName)}}},[e._v("\n                  删除\n                ")])],1)]}}])})],1)],1)])]),a("div",{staticClass:"operation"},[a("gs-button",{on:{click:e.handleBack}},[e._v("\n      上一步\n    ")]),a("gs-button",{attrs:{type:"primary"},on:{click:e.handleNext}},[e._v("\n      下一步\n    ")])],1),a("add-default-value",{attrs:{show:e.modalVisible,data:e.form},on:{"update:show":function(t){e.modalVisible=t},confirm:e.confirmEditField}})],1)},C=[],_=a("75fc"),A=function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("gs-modal",{attrs:{title:"过滤条件字段"},on:{confirm:e.confirmEditField,cancel:e.cancel},model:{value:e.modalVisible,callback:function(t){e.modalVisible=t},expression:"modalVisible"}},[a("gs-form",{attrs:{model:e.form,"label-width":"120px"}},[a("gs-form-item",{attrs:{label:"字段名称",prop:"columnName"}},[a("span",[e._v(e._s(e.form.displayName+"（"+e.form.columnName+"）"))])]),a("gs-form-item",{attrs:{label:"字段类型",prop:"type"}},[a("span",[e._v(e._s(e.form.type))])]),a("gs-form-item",{attrs:{label:"必传",prop:"fill"}},[a("gs-radio-group",{model:{value:e.form.fill,callback:function(t){e.$set(e.form,"fill",t)},expression:"form.fill"}},[a("gs-radio",{attrs:{label:!0}},[e._v("\n          是\n        ")]),a("gs-radio",{attrs:{label:!1}},[e._v("\n          否\n        ")])],1)],1),e.form.fill?e._e():a("gs-form-item",{attrs:{label:"是否有缺省默认值",prop:"useDefaultValue"}},[a("gs-radio-group",{on:{change:e.useDefaultValueChange},model:{value:e.form.useDefaultValue,callback:function(t){e.$set(e.form,"useDefaultValue",t)},expression:"form.useDefaultValue"}},[a("gs-radio",{attrs:{label:!0}},[e._v("\n          是\n        ")]),a("gs-radio",{attrs:{label:!1}},[e._v("\n          否\n        ")])],1)],1),e.form.fill?e._e():a("gs-form-item",{attrs:{label:"默认值",prop:"defaultValue"}},[a("fields-input",{attrs:{type:e.form.type,disabled:!e.form.useDefaultValue},model:{value:e.form.defaultValue,callback:function(t){e.$set(e.form,"defaultValue",t)},expression:"form.defaultValue"}})],1)],1)],1)},w=[],$=a("0290"),O={components:{fieldsInput:$["a"]},props:{show:{type:Boolean,default:!1},data:{type:Object,default:function(){}}},data:function(){return{modalVisible:!1,form:{}}},watch:{data:function(e){this.form=e},show:function(e){this.modalVisible=e}},methods:{confirmEditField:function(){this.$emit("confirm",this.form)},cancel:function(){this.$emit("update:show",!1)},useDefaultValueChange:function(e){e||(this.form.defaultValue="")}}},G=O,I=Object(m["a"])(G,A,w,!1,null,null,null),S=I.exports,M={components:{addDefaultValue:S},mixins:[u],props:{data:{type:Object,default:function(){return{}}}},data:function(){return{fields:[],form:{},modalVisible:!1,editIndex:-1,isIndeterminate:!1,checkAll:!1}},computed:{fieldsName:function(){return this.fields.map(function(e){return e.columnName})}},watch:{"storeApiMsg.column":{handler:function(e,t){var a=JSON.stringify(e)===JSON.stringify(t);a||(this.fields=[],this.changeCheckAllStatus())},deep:!0}},created:function(){this.data&&Object.keys(this.data).length&&this.setData()},methods:{setData:function(){var e=this.storeApiMsg.column.map(function(e){return e.columnName});this.fields=(this.data.fields||[]).filter(function(t){return t.filter&&e.includes(t.columnName)}),this.changeCheckAllStatus()},handleNext:function(){var e=this,t=[];this.storeApiMsg.column.map(function(a){e.fieldsName.includes(a.columnName)||t.push({columnName:a.columnName,filter:!1,type:a.type})}),this.saveApiMsg({fields:[].concat(t,Object(_["a"])(this.fields))}),this.$emit("next")},handleBack:function(){this.$emit("back")},chooseFields:function(e){this.fieldsName.includes(e.columnName)||(this.fields.push({columnName:e.columnName,displayName:e.displayName,filter:!0,fill:!1,defaultValue:"",type:e.type,useDefaultValue:!1}),this.changeCheckAllStatus())},deleteField:function(e,t){var a=this.fields.findIndex(function(t){return e===t.columnName});-1!==a&&(this.fields.splice(a,1),this.changeCheckAllStatus())},editFilterField:function(e){this.modalVisible=!0,this.form=JSON.parse(JSON.stringify(e.row)),this.editIndex=e.$index},confirmEditField:function(e){this.$set(this.fields,this.editIndex,e),this.modalVisible=!1},handleCheckAllChange:function(e){this.fields=e.target.checked?this.storeApiMsg.column.map(function(e){return{columnName:e.columnName,displayName:e.displayName,filter:!0,fill:!1,defaultValue:"",type:e.type,useDefaultValue:!1}}):[],this.isIndeterminate=!1},changeCheckAllStatus:function(){var e=this.fields.length;this.checkAll=e>0,this.isIndeterminate=e>0&&e<this.storeApiMsg.column.length}}},D=M,T=Object(m["a"])(D,x,C,!1,null,null,null),V=T.exports,R=function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",{staticClass:"test-api"},[a("div",{staticClass:"content"},[a("p",{staticClass:"title"},[a("span",[e._v("API地址")]),a("span",{staticClass:"path"},[e._v(e._s(e.storeApiMsg.path))])]),a("div",{staticClass:"req-msg"},[a("h5",{staticClass:"title"},[e._v("\n        请求信息\n      ")]),a("div",{staticClass:"request"},[e._l(e.testData,function(t,n){return a("div",{key:n,staticClass:"fields"},[a("gs-checkbox",{staticClass:"name",model:{value:t.check,callback:function(a){e.$set(t,"check",a)},expression:"item.check"}},[a("span",{attrs:{title:t.displayName+"（"+t.columnName+"）"}},[e._v(e._s(t.displayName+"（"+t.columnName+"）"))])]),a("span",[e._v("=")]),a("span",{staticClass:"value"},[a("fields-input",{attrs:{type:t.type},model:{value:t.value,callback:function(a){e.$set(t,"value",a)},expression:"item.value"}})],1)],1)}),a("div",{staticClass:"fields"},[a("span",{staticClass:"name"},[e._v("limit")]),a("span",[e._v("=")]),a("span",{staticClass:"value"},[a("gs-input-number",{attrs:{precision:0,min:0,max:100},model:{value:e.limit,callback:function(t){e.limit=t},expression:"limit"}})],1)]),a("div",{staticClass:"fields"},[a("span",{staticClass:"name"},[e._v("offset")]),a("span",[e._v("=")]),a("span",{staticClass:"value"},[a("gs-input-number",{attrs:{precision:0,min:0},model:{value:e.offset,callback:function(t){e.offset=t},expression:"offset"}})],1)])],2),a("div",{staticClass:"test-operation"},[a("gs-button",{attrs:{disabled:!e.btnLoading},on:{click:e.stopTest}},[e._v("\n          停止测试\n        ")]),a("gs-button",{attrs:{loading:e.btnLoading,type:"primary"},on:{click:e.startTest}},[e._v("\n          开始测试\n        ")])],1)]),a("div",{staticClass:"rep-res"},[a("div",{staticClass:"response"},[a("gs-tabs",{attrs:{type:"content-tab"},model:{value:e.activeName,callback:function(t){e.activeName=t},expression:"activeName"}},[a("gs-tab-pane",{attrs:{label:"编码模式",name:"json"}},[a("gs-textarea",{attrs:{value:e.testResStringify,autosize:{minRows:8,maxRows:12},readonly:""}})],1),a("gs-tab-pane",{attrs:{label:"表格模式",name:"table"}},[a("el-table",{attrs:{data:e.testRes}},e._l(e.tableHeader,function(t,n){return a("el-table-column",{key:n,attrs:{label:t,prop:t},scopedSlots:e._u([{key:"default",fn:function(a){return[e._v("\n                  "+e._s(a.row[t]+"")+"\n                ")]}}],null,!0)})}),1)],1)],1)],1)])]),a("div",{staticClass:"operation"},[a("gs-button",{on:{click:e.handleBack}},[e._v("\n      上一步\n    ")]),a("gs-button",{attrs:{type:"primary"},on:{click:e.handleNext}},[e._v("\n      完成\n    ")])],1)])},q=[],j=(a("6c7b"),a("6853")),E=a("bc3a"),L=a.n(E),F=Math.random().toString(36).slice(-8),P={components:{fieldsInput:$["a"]},mixins:[u,j["a"]],data:function(){return{}},computed:{},activated:function(){var e=this;this.testData=(this.storeApiMsg.fields||[]).filter(function(e){return e.filter}).map(function(t){var a=(e.storeApiMsg.column||[]).find(function(e){return e.columnName===t.columnName});return{columnName:t.columnName,displayName:t.displayName,value:t.defaultValue||"",check:!0,type:a.type,fill:t.fill}})},methods:{handleNext:function(){this.$emit("next")},handleBack:function(){this.$emit("back")},startTest:function(){var e=this;if(this.checkTestInput()){var t=this.storeApiMsg,a=t.tableGuid,n=t.dbGuid,i=t.maxRowNumber,s=this.testData.filter(function(t){var a=(e.storeApiMsg.fields||[]).find(function(e){return e.columnName===t.columnName});return t.check||!t.fill&&""!==a.defaultValue}).map(function(e){return{columnName:e.columnName,value:[e.value]}}),o=this.storeApiMsg.column.map(function(e){return e.columnName}),l={tableGuid:a,dbGuid:n,maxRowNumber:i,parameter:s,queryFields:o,limit:this.limit,offset:this.offset};F=Math.random().toString(36).slice(-8),this.btnLoading=!0;var r=L.a.CancelToken;this.source=r.source(),c["testApi"]({params:l,randomName:F},this.source.token).then(function(t){e.testRes=t,e.btnLoading=!1,e.$Message.success("测试完成！")}).catch(function(t){e.btnLoading=!1,console.log("Request canceled",t.message)})}}}},B=P,J=Object(m["a"])(B,R,q,!1,null,null,null),H=J.exports,z=["选择数据源","基本信息","API配置信息","测试"],W={components:{dataSource:h,baseInfo:k,apiConfig:V,apiTest:H},mixins:[u],data:function(){return{activeStep:0,stepsTitle:z,components:["dataSource","baseInfo","apiConfig","apiTest"],isEdit:!1,apiData:null,allDone:!1,loading:!1}},computed:{component:function(){return this.components[this.activeStep]},hasData:function(){return!this.$route.query.apiId||null!==this.apiData}},created:function(){this.fetchApiDetail()},methods:{handleNext:function(){var e=this;if(this.activeStep<3)this.activeStep=this.activeStep+1;else{var t=JSON.parse(JSON.stringify(this.storeApiMsg));delete t.column;var a=this.$route.query,n=a.isEdit,i=a.apiId;this.loading=!0,n&&i?c["editApi"]({params:t,apiId:i}).then(function(t){e.$Message.success("修改成功"),e.allDone=!0,e.loading=!1,e.$router.push({name:"/main/data-share",query:{groupId:e.$route.query.groupId}})}).catch(function(t){e.loading=!1}):c["createApi"](t).then(function(t){e.allDone=!0,e.loading=!1,e.$Message.success("创建成功"),e.$router.push({name:"/main/data-share",query:{groupId:e.$route.query.groupId}})}).catch(function(t){e.loading=!1})}},handlePrev:function(){this.activeStep=this.activeStep-1},fetchApiDetail:function(){var e=this;this.$route.query.apiId&&c["getApiDetail"](this.$route.query.apiId).then(function(t){e.apiData=t})},returnList:function(){this.$router.push({path:"/main/data-share/list",query:{groupId:this.$route.query.groupId}})}}},X=W,K=Object(m["a"])(X,n,i,!1,null,null,null);t["default"]=K.exports},cd1c:function(e,t,a){var n=a("e853");e.exports=function(e,t){return new(n(e))(t)}},e853:function(e,t,a){var n=a("d3f4"),i=a("1169"),s=a("2b4c")("species");e.exports=function(e){var t;return i(e)&&(t=e.constructor,"function"!=typeof t||t!==Array&&!i(t.prototype)||(t=void 0),n(t)&&(t=t[s],null===t&&(t=void 0))),void 0===t?Array:t}},e932:function(e,t,a){}}]);
//# sourceMappingURL=chunk-29df7351.1900bd4e.js.map