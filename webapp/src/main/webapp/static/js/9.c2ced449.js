webpackJsonp([9],{1244:function(t,e,a){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var r=a(1245),s=a(1256),n=a(389)(r.a,s.a,null,null,null);e.default=n.exports},1245:function(t,e,a){"use strict";var r=a(40),s=a.n(r),n=a(402),i=a(425),l=a(1246),o=a(1252),c=a(1255);a.n(c);e.a={components:{ActionComfirm:i.a,FormMode:l.a,SqlMode:o.a},data:function(){return{isCreating:!1,activeName:"form",formData:{database:"",tableName:"",tableType:1,lifecycle:1,expireDate:-1,hdfsPath:"",fields:[],isPartition:!1,partitionFields:[],storedFormat:"",fieldsTerminated:"",lineTerminated:"\\n"},sqlData:{database:"",sql:""}}},computed:s()({},Object(n.c)("metadata",{metadatas:"metadatas"})),watch:{activeName:function(t){var e=this,a=this.formData;"sql"===t&&this.transformForm2Sql(a).then(function(t){e.sqlData={database:a.database,sql:t}})},metadatas:function(t){var e=t.list;if(e&&e.length>0){var a=e[0].label;this.formData.database=a,this.sqlData.database=a}}},mounted:function(){this.getMetadatas({query:"",offset:0,limit:-1})},methods:s()({},Object(n.b)("table",["createTableByForm","createTableBySql","transformForm2Sql"]),Object(n.b)("metadata",["getMetadatas"]),{form2sql:function(){var t=this.formData,e=t.tableName,a=t.type,r=t.hdfsPath,s=t.fields,n=t.isPartition,i=t.partitionFields,l=t.storedFormat,o="CREATE "+(1===a?"EXTERNAL ":"")+"TABLE "+e+" \n",c=this.createFieldStatement(s),u=l?"STORED AS "+l+" \n":"",d=r?"LOCATION "+r+";":"";return[o,c,n&&i.length>0?"PARTITIONED BY \n"+this.createFieldStatement(i):"",u,d].join("")},createFieldStatement:function(t){var e=t.map(function(t){var e=t.columnName,a=t.desc,r="    "+e+" "+t.type;return a&&(r+=" COMMENT '"+a+"'"),r}).join("\n");return t&&0!==t.length?"(\n"+e+"\n)\n":""},handleScan:function(){this.activeName="sql"},createTable:function(){var t=this,e=function(e){t.$Message.success("新建成功，正在跳转到表信息页...",1.5),setTimeout(function(){t.$router.push({path:"/main/metadata/detail/"+e.tableId})},1500)},a=function(){t.isCreating=!1};"form"===this.activeName?this.$refs.formMode.$refs.form.validate(function(r){r&&(t.isCreating=!0,t.createTableByForm(t.formData).then(e,a))}):(this.isCreating=!0,this.createTableBySql({sql:this.sqlData.sql}).then(e,a))},cancelCreate:function(){this.$router.push({path:"/main/metadata"})}})}},1246:function(t,e,a){"use strict";var r=a(1247),s=a(1251),n=a(389)(r.a,s.a,null,null,null);e.a=n.exports},1247:function(t,e,a){"use strict";var r=a(1248),s=/^[a-zA-Z$_][a-zA-Z\d_]*$/;e.a={components:{FieldTable:r.a},props:{value:{type:Object,default:function(){return{}}},databases:{type:Array,default:function(){return{}}}},data:function(){var t=function(t,e,a){if(e&&0!==e.length){for(var r=0;r<e.length;r++){var n=e[r].columnName;if(!n)return void a(new Error("第"+(r+1)+"行的列名不能为空"));if(!s.test(n))return void a(new Error("第"+(r+1)+"行的列名不合法"))}a()}else a(new Error("字段不能为空"))};return{formData:this.value,fileTypes:["avro","parquet","textfile","sequencefile","orc"],requiredTerminated:!0,rules:{tableName:[{validator:function(t,e,a){e?s.test(e)?a():a(new Error("请输入合法的表名")):a(new Error("表名不能为空"))}}],hdfsPath:[{message:"HDFS路径不能为空",required:!0}],fields:[{validator:t}],partitionFields:[{validator:t}],fieldsTerminated:[{message:"字段分隔符不能为空",required:!0,validator:function(t,e,a){!e||/\\n/.test(e)?a(new Error("字段分隔符不能为换行符")):a()}}]}}},watch:{value:function(t){this.formData=t}},mounted:function(){var t=this;this.$watch("formData",function(){t.$emit("input",t.formData)},{deep:!0})},methods:{handleTableTypeChange:function(t){this.rules.fieldsTerminated[0].required=1===t,2===t&&this.$refs.form.validateField("fieldsTerminated")}}}},1248:function(t,e,a){"use strict";var r=a(1249),s=a(1250),n=a(389)(r.a,s.a,null,null,null);e.a=n.exports},1249:function(t,e,a){"use strict";var r=a(10),s=a(425);e.a={components:{ActionComfirm:s.a},mixins:[r.a],props:{value:{type:Array,default:function(){return[]}}},data:function(){return{fieldTypes:["BOOLEAN","TINYINT","SMALLINT","INT","BIGINT","FLOAT","DOUBLE","DECIMAL","STRING","BINARY","TIMESTAMP","DATE"],dataSources:this.value}},watch:{value:function(t){this.dataSources=t}},methods:{isValid:function(t){return t&&/^[a-zA-Z$_][a-zA-Z\d_]*$/.test(t)},addField:function(){var t=this;this.dataSources.push({columnName:"",description:"",type:this.fieldTypes[0]}),this.$emit("input",this.dataSources),this.$nextTick(function(){t.$refs.columnInput.focus()})},deleteField:function(t){this.dataSources.splice(t.$index,1),this.$emit("input",this.dataSources),this.dispatch("GsFormItem","gs.form.change",this.dataSources)},handleColumnInput:function(t){var e=t.$index,a=t.row;this.dataSources[e]=a,this.$emit("input",this.dataSources)}}}},1250:function(t,e,a){"use strict";var r={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"field-table"},[a("el-table",{attrs:{data:t.dataSources,border:""}},[a("el-table-column",{attrs:{prop:"columnName",label:"列名",width:"180"},scopedSlots:t._u([{key:"default",fn:function(e){return[a("gs-input",{ref:"columnInput",attrs:{"validate-event":!t.isValid(e.row.columnName),placeholder:"请输入列名"},on:{change:function(a){t.handleColumnInput(e)}},model:{value:e.row.columnName,callback:function(a){t.$set(e.row,"columnName",a)},expression:"scope.row.columnName"}})]}}])}),t._v(" "),a("el-table-column",{attrs:{prop:"description",label:"描述",width:"180"},scopedSlots:t._u([{key:"default",fn:function(e){return[a("gs-input",{attrs:{"validate-event":!1,placeholder:"请输入字段描述"},on:{change:function(a){t.handleColumnInput(e)}},model:{value:e.row.description,callback:function(a){t.$set(e.row,"description",a)},expression:"scope.row.description"}})]}}])}),t._v(" "),a("el-table-column",{attrs:{prop:"type",label:"类型"},scopedSlots:t._u([{key:"default",fn:function(e){return[a("gs-select",{attrs:{"validate-event":!1,placeholder:"请选择数据库"},on:{change:function(a){t.handleColumnInput(e)}},model:{value:e.row.type,callback:function(a){t.$set(e.row,"type",a)},expression:"scope.row.type"}},t._l(t.fieldTypes,function(t,e){return a("gs-option",{key:e,attrs:{label:t,value:t}})}))]}}])}),t._v(" "),a("el-table-column",{attrs:{prop:"id",label:"操作"},scopedSlots:t._u([{key:"default",fn:function(e){return[a("action-comfirm",{attrs:{title:"确定删除该字段吗？"},on:{comfirm:function(a){t.deleteField(e)}}},[a("span",{staticClass:"remove-icon",attrs:{slot:"reference"},slot:"reference"},[a("gs-icon",{attrs:{name:"close"}}),t._v("删除\n          ")],1)])]}}])})],1),t._v(" "),a("span",{staticClass:"add-icon",on:{click:t.addField}},[a("gs-icon",{attrs:{name:"plus-circle"}}),a("span",[t._v("添加")])],1)],1)},staticRenderFns:[]};e.a=r},1251:function(t,e,a){"use strict";var r={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"form-mode"},[a("gs-form",{ref:"form",attrs:{model:t.formData,rules:t.rules,"label-width":"90px"}},[a("gs-form-item",{attrs:{label:"数据库",prop:"database",required:""}},[a("gs-select",{attrs:{placeholder:"请选择数据库"},model:{value:t.formData.database,callback:function(e){t.$set(t.formData,"database",e)},expression:"formData.database"}},t._l(t.databases,function(t,e){return a("gs-option",{key:e,attrs:{label:t.label,value:t.label}})}))],1),t._v(" "),a("gs-form-item",{attrs:{label:"表名称",prop:"tableName",required:""}},[a("gs-input",{attrs:{placeholder:"请输入表名称"},model:{value:t.formData.tableName,callback:function(e){t.$set(t.formData,"tableName",e)},expression:"formData.tableName"}})],1),t._v(" "),a("gs-form-item",{attrs:{label:"类型",prop:"tableType"}},[a("gs-radio-group",{on:{change:t.handleTableTypeChange},model:{value:t.formData.tableType,callback:function(e){t.$set(t.formData,"tableType",e)},expression:"formData.tableType"}},[a("gs-radio",{attrs:{label:1}},[t._v("外部表")]),t._v(" "),a("gs-radio",{attrs:{label:2}},[t._v("内部表")])],1)],1),t._v(" "),1===t.formData.tableType?a("gs-form-item",{key:"hdfsPath",attrs:{label:"HDFS路径",prop:"hdfsPath"}},[a("gs-input",{attrs:{placeholder:"请输入HDFS路径"},model:{value:t.formData.hdfsPath,callback:function(e){t.$set(t.formData,"hdfsPath",e)},expression:"formData.hdfsPath"}})],1):t._e(),t._v(" "),a("gs-form-item",{key:"fieldsTerminated",attrs:{label:"字段分隔符",prop:"fieldsTerminated"}},[a("gs-input",{attrs:{placeholder:"请输入字段分隔符"},model:{value:t.formData.fieldsTerminated,callback:function(e){t.$set(t.formData,"fieldsTerminated",e)},expression:"formData.fieldsTerminated"}})],1),t._v(" "),a("gs-form-item",{key:"lineTerminated",attrs:{label:"行分隔符",prop:"lineTerminated"}},[a("gs-input",{attrs:{placeholder:"请输入行分隔符"},model:{value:t.formData.lineTerminated,callback:function(e){t.$set(t.formData,"lineTerminated",e)},expression:"formData.lineTerminated"}})],1),t._v(" "),a("gs-form-item",{staticClass:"field-table-item",attrs:{label:"普通字段",prop:"fields",required:""}},[a("field-table",{model:{value:t.formData.fields,callback:function(e){t.$set(t.formData,"fields",e)},expression:"formData.fields"}})],1),t._v(" "),a("gs-form-item",{attrs:{label:"分区表",prop:"isPartition"}},[a("gs-checkbox-group",{model:{value:t.formData.isPartition,callback:function(e){t.$set(t.formData,"isPartition",e)},expression:"formData.isPartition"}},[a("gs-checkbox")],1)],1),t._v(" "),t.formData.isPartition?a("gs-form-item",{staticClass:"field-table-item",attrs:{label:"分区字段",prop:"partitionFields",required:""}},[a("field-table",{model:{value:t.formData.partitionFields,callback:function(e){t.$set(t.formData,"partitionFields",e)},expression:"formData.partitionFields"}})],1):t._e(),t._v(" "),a("gs-form-item",{attrs:{label:"HDFS文件格式",prop:"storedFormat"}},[a("gs-select",{attrs:{placeholder:"请选择HDFS文件格式"},model:{value:t.formData.storedFormat,callback:function(e){t.$set(t.formData,"storedFormat",e)},expression:"formData.storedFormat"}},t._l(t.fileTypes,function(t,e){return a("gs-option",{key:e,attrs:{label:t,value:t}})}))],1)],1)],1)},staticRenderFns:[]};e.a=r},1252:function(t,e,a){"use strict";var r=a(1253),s=a(1254),n=a(389)(r.a,s.a,null,null,null);e.a=n.exports},1253:function(t,e,a){"use strict";e.a={props:{value:{type:Object,default:function(){return{}}},databases:{type:Array,default:function(){return{}}}},data:function(){return{sqlData:this.value,cmOptions:{readOnly:"nocursor",tabSize:4,mode:"text/x-hive",styleActiveLine:!0,lineNumbers:!0,line:!0}}},watch:{value:function(t){this.sqlData=t}}}},1254:function(t,e,a){"use strict";var r={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"sql-mode"},[a("gs-form",{ref:"form",attrs:{model:t.sqlData,"label-width":"80px"}},[a("gs-form-item",{attrs:{label:"数据库",prop:"database"}},[a("gs-select",{attrs:{placeholder:"请选择数据库",disabled:""},model:{value:t.sqlData.database,callback:function(e){t.$set(t.sqlData,"database",e)},expression:"sqlData.database"}},t._l(t.databases,function(t,e){return a("gs-option",{key:e,attrs:{label:t.label,value:t.label}})}))],1)],1),t._v(" "),a("div",{staticClass:"codemirror-input"},[a("label",{staticClass:"codemirror-input-label"},[t._v("建表语句")]),t._v(" "),a("codemirror",{attrs:{options:t.cmOptions},model:{value:t.sqlData.sql,callback:function(e){t.$set(t.sqlData,"sql",e)},expression:"sqlData.sql"}})],1)],1)},staticRenderFns:[]};e.a=r},1255:function(t,e){},1256:function(t,e,a){"use strict";var r={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"table-create-page"},[a("h3",{staticClass:"table-create-title"},[t._v("新建离线表")]),t._v(" "),a("gs-tabs",{attrs:{type:"content-tab"},model:{value:t.activeName,callback:function(e){t.activeName=e},expression:"activeName"}},[a("gs-tab-pane",{attrs:{label:"表单模式",name:"form"}},[a("form-mode",{ref:"formMode",attrs:{databases:t.metadatas.list},model:{value:t.formData,callback:function(e){t.formData=e},expression:"formData"}})],1),t._v(" "),a("gs-tab-pane",{attrs:{label:"SQL模式",name:"sql"}},[a("sql-mode",{ref:"sqlMode",attrs:{databases:t.metadatas.list,value:t.sqlData}})],1)],1),t._v(" "),a("div",{staticClass:"sql-scan"},["sql"!==t.activeName?a("gs-button",{attrs:{type:"text-primary"},on:{click:t.handleScan}},[t._v("预览")]):t._e()],1),t._v(" "),a("div",{staticClass:"action-btns"},[a("action-comfirm",{attrs:{title:"确定新建离线表吗？"},on:{comfirm:t.createTable}},[a("gs-button",{attrs:{slot:"reference",loading:t.isCreating,type:"primary"},slot:"reference"},[t._v("确定")])],1),t._v(" "),a("action-comfirm",{attrs:{title:"确定取消新建离线表吗？"},on:{comfirm:t.cancelCreate}},[a("gs-button",{attrs:{slot:"reference"},slot:"reference"},[t._v("取消")])],1)],1)],1)},staticRenderFns:[]};e.a=r},425:function(t,e,a){"use strict";var r=a(437),s=a(440);a.n(s);e.a=r.a},437:function(t,e,a){"use strict";var r=a(438),s=a(439),n=a(389)(r.a,s.a,null,null,null);e.a=n.exports},438:function(t,e,a){"use strict";e.a={props:{title:{type:String,default:""},popoverProps:{type:Object,default:function(){return{}}}},data:function(){return{visible:!1}},methods:{onComfirm:function(){this.visible=!1,this.$emit("comfirm")},onCancel:function(){this.visible=!1,this.$emit("cancel")}}}},439:function(t,e,a){"use strict";var r={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("gs-popover",t._b({ref:"popover",staticClass:"action-comfirm-popover",attrs:{supernatant:"",placement:"top",trigger:"click"},model:{value:t.visible,callback:function(e){t.visible=e},expression:"visible"}},"gs-popover",t.popoverProps,!1),[a("div",{staticClass:"action-comfirm"},[t._t("default",[a("gs-icon",{staticClass:"action-comfirm-tip-icon",attrs:{name:"exclamation-circle"}}),t._v(t._s(t.title)+"\n    ")])],2),t._v(" "),a("div",{staticClass:"action-comfirm-footer"},[a("gs-button",{attrs:{type:"text-primary"},on:{click:t.onComfirm}},[t._v("确定")]),t._v(" "),a("gs-button",{attrs:{type:"text"},on:{click:t.onCancel}},[t._v("取消")])],1),t._v(" "),t._t("reference",null,{slot:"reference"})],2)},staticRenderFns:[]};e.a=r},440:function(t,e){}});
//# sourceMappingURL=9.c2ced449.js.map