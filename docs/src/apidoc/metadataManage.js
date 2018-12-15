/*
元数据管理
 */
/**
 * @api {post} /api/metaspace/metadata/search/database 元数据管理跳转
 * @apiName 元数据管理跳转
 * @apiGroup metadataManage
 * @apiDescription 发送请求时携带一个通用的"查询参数对象",该接口只用提供databaseOffset和databaseLimit两个属性
 该接口会返回请求数量的数据库及其所有表的列表
 点击加载更多的功能所需的数据由第一个接口一次全部返回，前端去分页，这样点击时加载更快。
 放到后端做分页，由于该数据存放在hbase中并未排序，只是作为库对象的属性在存放，无法根据偏移量读取，只能先全部取出到内存，再根据创建时间排序，每次取对应偏移量的表信息返给前端，这样后台重复工作很多，效率很低。
 * @apiParam {String} tableOffset 偏移量
 * @apiParam {String} tableLimit 页面显示个数
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiSuccessExample {json} 返回示例:
 *     HTTP/1.1 200 OK
 *     {
    "offset": 0,
    "count": 10,
    "sum": 43,
    "lists": [
        {
            "databaseId": "b9f63af4-c68e-4b3c-9809-535d75e48081",
            "databaseName": "default",
            "databaseDescription": "Default Hive database",
            "status": "ACTIVE",
            "tableList": [
                {
                    "tableId": "0a65d4d6-d607-4922-817d-e8ebdc91eeac",
                    "tableName": "ww_customers",
                    "business": null,
                    "relations": null,
                    "owner": null,
                    "createTime": null,
                    "category": null,
                    "databaseId": "b9f63af4-c68e-4b3c-9809-535d75e48081",
                    "databaseName": "default",
                    "tableLife": null,
                    "partitionKey": null,
                    "partitionTable": null,
                    "partitionLife": null,
                    "format": null,
                    "location": null,
                    "description": "知春路甲",
                    "topic": null,
                    "type": null,
                    "virtualTable": null,
                    "status": "ACTIVE",
                    "tablePermission": null,
                    "columns": null
                },
                {
                    "tableId": "30e122b1-ce2f-4bb2-9bf1-3899c879bf59",
                    "tableName": "complex4",
                    "business": null,
                    "relations": null,
                    "owner": null,
                    "createTime": null,
                    "category": null,
                    "databaseId": "b9f63af4-c68e-4b3c-9809-535d75e48081",
                    "databaseName": "default",
                    "tableLife": null,
                    "partitionKey": null,
                    "partitionTable": null,
                    "partitionLife": null,
                    "format": null,
                    "location": null,
                    "description": "null",
                    "topic": null,
                    "type": null,
                    "virtualTable": null,
                    "status": "ACTIVE",
                    "tablePermission": null,
                    "columns": null
                },
                {
                    "tableId": "66ce06e2-9985-47a1-83b9-62042441c310",
                    "tableName": "fd",
                    "business": null,
                    "relations": null,
                    "owner": null,
                    "createTime": null,
                    "category": null,
                    "databaseId": "b9f63af4-c68e-4b3c-9809-535d75e48081",
                    "databaseName": "default",
                    "tableLife": null,
                    "partitionKey": null,
                    "partitionTable": null,
                    "partitionLife": null,
                    "format": null,
                    "location": null,
                    "description": "null",
                    "topic": null,
                    "type": null,
                    "virtualTable": null,
                    "status": "ACTIVE",
                    "tablePermission": null,
                    "columns": null
                },
                {
                    "tableId": "7684c7bf-0e20-43f1-95c8-7476bf84073b",
                    "tableName": "extabletest_05",
                    "business": null,
                    "relations": null,
                    "owner": null,
                    "createTime": null,
                    "category": null,
                    "databaseId": "b9f63af4-c68e-4b3c-9809-535d75e48081",
                    "databaseName": "default",
                    "tableLife": null,
                    "partitionKey": null,
                    "partitionTable": null,
                    "partitionLife": null,
                    "format": null,
                    "location": null,
                    "description": "null",
                    "topic": null,
                    "type": null,
                    "virtualTable": null,
                    "status": "ACTIVE",
                    "tablePermission": null,
                    "columns": null
                }
            ]
        },
        {
            "databaseId": "b339b584-334f-462a-a176-553a5db93a6c",
            "databaseName": "data1",
            "databaseDescription": "null",
            "status": "ACTIVE",
            "tableList": [
                {
                    "tableId": "39e81aee-b233-4297-a135-f31c6214cafa",
                    "tableName": "mhaw_test",
                    "business": null,
                    "relations": null,
                    "owner": null,
                    "createTime": null,
                    "category": null,
                    "databaseId": "b339b584-334f-462a-a176-553a5db93a6c",
                    "databaseName": "data1",
                    "tableLife": null,
                    "partitionKey": null,
                    "partitionTable": null,
                    "partitionLife": null,
                    "format": null,
                    "location": null,
                    "description": "???",
                    "topic": null,
                    "type": null,
                    "virtualTable": null,
                    "status": "ACTIVE",
                    "tablePermission": null,
                    "columns": null
                },
                {
                    "tableId": "6c5f58aa-51be-4089-9ddf-f1769bbb857e",
                    "tableName": "mhaw_tesg",
                    "business": null,
                    "relations": null,
                    "owner": null,
                    "createTime": null,
                    "category": null,
                    "databaseId": "b339b584-334f-462a-a176-553a5db93a6c",
                    "databaseName": "data1",
                    "tableLife": null,
                    "partitionKey": null,
                    "partitionTable": null,
                    "partitionLife": null,
                    "format": null,
                    "location": null,
                    "description": "null",
                    "topic": null,
                    "type": null,
                    "virtualTable": null,
                    "status": "ACTIVE",
                    "tablePermission": null,
                    "columns": null
                }
            ]
        },
        {
            "databaseId": "fc1663ad-3a83-420e-a93d-d468b0ee9dbc",
            "databaseName": "data2",
            "databaseDescription": "null",
            "status": "ACTIVE",
            "tableList": []
        },
        {
            "databaseId": "03d8291c-5acb-4cb0-bce2-6f4d3c0fb476",
            "databaseName": "data3",
            "databaseDescription": "null",
            "status": "ACTIVE",
            "tableList": []
        },
        {
            "databaseId": "69b1a934-82f3-4ba3-8161-165a9371dc51",
            "databaseName": "data4",
            "databaseDescription": "null",
            "status": "ACTIVE",
            "tableList": [
                {
                    "tableId": "6aed39e1-0d20-4fb8-b1ac-859993599aae",
                    "tableName": "tw",
                    "business": null,
                    "relations": null,
                    "owner": null,
                    "createTime": null,
                    "category": null,
                    "databaseId": "69b1a934-82f3-4ba3-8161-165a9371dc51",
                    "databaseName": "data4",
                    "tableLife": null,
                    "partitionKey": null,
                    "partitionTable": null,
                    "partitionLife": null,
                    "format": null,
                    "location": null,
                    "description": "null",
                    "topic": null,
                    "type": null,
                    "virtualTable": null,
                    "status": "DELETED",
                    "tablePermission": null,
                    "columns": null
                }
            ]
        },
        {
            "databaseId": "73588931-80cc-4c45-abd1-13e6b9f71109",
            "databaseName": "data5",
            "databaseDescription": "null",
            "status": "ACTIVE",
            "tableList": [
                {
                    "tableId": "45dc5302-f6a6-45b0-84d0-473cd841f8f6",
                    "tableName": "test1",
                    "business": null,
                    "relations": null,
                    "owner": null,
                    "createTime": null,
                    "category": null,
                    "databaseId": "73588931-80cc-4c45-abd1-13e6b9f71109",
                    "databaseName": "data5",
                    "tableLife": null,
                    "partitionKey": null,
                    "partitionTable": null,
                    "partitionLife": null,
                    "format": null,
                    "location": null,
                    "description": "null",
                    "topic": null,
                    "type": null,
                    "virtualTable": null,
                    "status": "ACTIVE",
                    "tablePermission": null,
                    "columns": null
                }
            ]
        },
        {
            "databaseId": "9035b029-484c-43aa-b55f-fdbfb17c619a",
            "databaseName": "data6",
            "databaseDescription": "null",
            "status": "ACTIVE",
            "tableList": []
        },
        {
            "databaseId": "46858caa-e6d4-417a-ad66-c2470f11206b",
            "databaseName": "data7",
            "databaseDescription": "null",
            "status": "ACTIVE",
            "tableList": []
        },
        {
            "databaseId": "bb3ad18e-8d1a-4304-8b97-b8f24c87abb6",
            "databaseName": "data8",
            "databaseDescription": "null",
            "status": "ACTIVE",
            "tableList": []
        },
        {
            "databaseId": "03c6b3ef-26f4-4798-a6aa-a74fca5b90fe",
            "databaseName": "data9",
            "databaseDescription": "null",
            "status": "ACTIVE",
            "tableList": [
                {
                    "tableId": "31dae78e-c36d-4ac2-bf63-69ed30e12d59",
                    "tableName": "test2",
                    "business": null,
                    "relations": null,
                    "owner": null,
                    "createTime": null,
                    "category": null,
                    "databaseId": "03c6b3ef-26f4-4798-a6aa-a74fca5b90fe",
                    "databaseName": "data9",
                    "tableLife": null,
                    "partitionKey": null,
                    "partitionTable": null,
                    "partitionLife": null,
                    "format": null,
                    "location": null,
                    "description": "null",
                    "topic": null,
                    "type": null,
                    "virtualTable": null,
                    "status": "ACTIVE",
                    "tablePermission": null,
                    "columns": null
                },
                {
                    "tableId": "fb5389cd-295d-499d-b95e-0053fd1e79cb",
                    "tableName": "test3",
                    "business": null,
                    "relations": null,
                    "owner": null,
                    "createTime": null,
                    "category": null,
                    "databaseId": "03c6b3ef-26f4-4798-a6aa-a74fca5b90fe",
                    "databaseName": "data9",
                    "tableLife": null,
                    "partitionKey": null,
                    "partitionTable": null,
                    "partitionLife": null,
                    "format": null,
                    "location": null,
                    "description": "null",
                    "topic": null,
                    "type": null,
                    "virtualTable": null,
                    "status": "ACTIVE",
                    "tablePermission": null,
                    "columns": null
                }
            ]
        }
    ]
}

 */

/**
 * @api {post} /api/metaspace/metadata/search/table?refreshCache=true 表查询
 * @apiName 表查询
 * @apiGroup metadataManage
 * @apiDescription
 * @apiParam {String=null} [query] 表名
 * @apiParam {String} tableOffset 偏移量
 * @apiParam {String} tableLimit 页面显示个数
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiParamExample {json} Request:
 *  {
            "query":"",
            "offset":0,
            "limit":10
            }

 * @apiSuccessExample {json} 返回示例:
 *     HTTP/1.1 200 OK
 *
 {
     "offset": 0,
     "count": 10,
     "sum": 46,
     "lists": [
         {
             "tableId": "aa6471d4-8367-4eea-94bd-e081242f9d77",
             "tableName": "t",
             "business": null,
             "relations": null,
             "owner": null,
             "createTime": null,
             "category": null,
             "databaseId": "b9f63af4-c68e-4b3c-9809-535d75e48081",
             "databaseName": "default",
             "tableLife": null,
             "partitionKey": null,
             "partitionTable": null,
             "partitionLife": null,
             "format": null,
             "location": null,
             "description": "null",
             "topic": null,
             "type": null,
             "tablePermission": null,
             "columns": null
         },
         {
             "tableId": "b70dcf38-a4aa-4bc6-8fa1-d69b1e7bc292",
             "tableName": "words",
             "business": null,
             "relations": null,
             "owner": null,
             "createTime": null,
             "category": null,
             "databaseId": "b9f63af4-c68e-4b3c-9809-535d75e48081",
             "databaseName": "default",
             "tableLife": null,
             "partitionKey": null,
             "partitionTable": null,
             "partitionLife": null,
             "format": null,
             "location": null,
             "description": "null",
             "topic": null,
             "type": null,
             "tablePermission": null,
             "columns": null
         }
     ]
 }

 */

/**
 * @api {post} /api/metaspace/metadata/search/column?refreshCache=true 字段查询
 * @apiName 字段查询
 * @apiGroup metadataManage
 * @apiDescription
 * @apiParam {String=null} [query] 表名
 * @apiParam {String} tableOffset 偏移量
 * @apiParam {String} tableLimit 页面显示个数
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiParamExample {json} Request:
 *  {
            "query":"",
            "offset":0,
            "limit":10
            }

 * @apiSuccessExample {json} 返回示例:
 *     HTTP/1.1 200 OK
 *
 {
    "offset": 0,
    "count": 10,
    "sum": 83,
    "lists": [
        {
            "columnId": "1de6e0c7-c9cd-47f4-88e2-446a3c2b18c0",
            "columnName": "id",
            "tableId": "280ae0e8-0e73-484e-93e4-8f5719cc8fa2",
            "tableName": "us_customers",
            "databaseId": "b9f63af4-c68e-4b3c-9809-535d75e48081",
            "databaseName": "default",
            "type": null,
            "description": null,
            "partitionKey": null
        },
        {
            "columnId": "0b707d34-239f-40c4-8569-291ba770db27",
            "columnName": "id",
            "tableId": "0a65d4d6-d607-4922-817d-e8ebdc91eeac",
            "tableName": "ww_customers",
            "databaseId": "b9f63af4-c68e-4b3c-9809-535d75e48081",
            "databaseName": "default",
            "type": null,
            "description": null,
            "partitionKey": null
        },
        {
            "columnId": "1f2127de-8da6-4662-a2e6-7b9ee02a5197",
            "columnName": "id",
            "tableId": "e4a8e85d-5336-4d8b-88b9-6b47b8549097",
            "tableName": "cost_savings",
            "databaseId": "b9f63af4-c68e-4b3c-9809-535d75e48081",
            "databaseName": "default",
            "type": null,
            "description": null,
            "partitionKey": null
        },
        {
            "columnId": "f0569df6-36ae-4f1e-9ceb-477dcee52901",
            "columnName": "id",
            "tableId": "d72b1e39-9174-495e-8f0f-41cc563c31c3",
            "tableName": "tax_2015",
            "databaseId": "b9f63af4-c68e-4b3c-9809-535d75e48081",
            "databaseName": "default",
            "type": null,
            "description": null,
            "partitionKey": null
        },
        {
            "columnId": "b906825e-3f50-4476-9bd4-1ea3c0678cfd",
            "columnName": "id",
            "tableId": "4ee530fa-55b0-431c-acc2-a10ff3bffcf3",
            "tableName": "eu_countries",
            "databaseId": "b9f63af4-c68e-4b3c-9809-535d75e48081",
            "databaseName": "default",
            "type": null,
            "description": null,
            "partitionKey": null
        },
        {
            "columnId": "cae75d7f-3c9b-4267-a6d1-f30fb387ae24",
            "columnName": "id",
            "tableId": "c6d9d177-0844-4afa-a6e4-dd81dd4815bf",
            "tableName": "tax_2009",
            "databaseId": "b9f63af4-c68e-4b3c-9809-535d75e48081",
            "databaseName": "default",
            "type": null,
            "description": null,
            "partitionKey": null
        },
        {
            "columnId": "2b628719-14f3-4fa2-8509-df1712fc1e35",
            "columnName": "id",
            "tableId": "73f2ddf7-78c5-424e-9c5a-95a50e38c10d",
            "tableName": "tax_2010",
            "databaseId": "b9f63af4-c68e-4b3c-9809-535d75e48081",
            "databaseName": "default",
            "type": null,
            "description": null,
            "partitionKey": null
        },
        {
            "columnId": "7f93b951-995a-4424-8b6f-3c391aa419d3",
            "columnName": "id",
            "tableId": "536a4a84-56e7-448b-8fee-1b91601c3812",
            "tableName": "claim",
            "databaseId": "b9f63af4-c68e-4b3c-9809-535d75e48081",
            "databaseName": "default",
            "type": null,
            "description": null,
            "partitionKey": null
        },
        {
            "columnId": "214633bd-92c9-4626-8f5c-752d05acd849",
            "columnName": "text",
            "tableId": "aa6471d4-8367-4eea-94bd-e081242f9d77",
            "tableName": "t",
            "databaseId": "b9f63af4-c68e-4b3c-9809-535d75e48081",
            "databaseName": "default",
            "type": null,
            "description": null,
            "partitionKey": null
        },
        {
            "columnId": "f87d2e8d-87b8-40ae-a7a6-0d8f9a414eec",
            "columnName": "id",
            "tableId": "aa6471d4-8367-4eea-94bd-e081242f9d77",
            "tableName": "t",
            "databaseId": "b9f63af4-c68e-4b3c-9809-535d75e48081",
            "databaseName": "default",
            "type": null,
            "description": null,
            "partitionKey": null
        }
    ]
}
 */

/**
 * @api {post} /api/metaspace/metadata/table/preview 表详情_数据预览
 * @apiName 表详情_数据预览
 * @apiGroup metadataManage
 * @apiDescription
 * @apiParam {String} guid 表id
 * @apiParam {Integer} count 偏移量
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiParamExample {json} Request:
 *  {
"guid":"96808458-a96e-4639-91e5-53e5887fc1ac",
"count":10
}

 * @apiSuccessExample {json} 返回示例:
 *     HTTP/1.1 200 OK
 *
 {
    "tableId": "96808458-a96e-4639-91e5-53e5887fc1ac",
    "columnNames": [
        "ext_table.id",
        "ext_table.name"
    ],
    "lines": [
        {
            "ext_table.name": "a",
            "ext_table.id": "1"
        }
    ]
}
 */

/**
 * @api {get} /api/metaspace/metadata/table/sql/:tableid 表详情_建表语句
 * @apiName 表详情_建表语句
 * @apiGroup metadataManage
 * @apiDescription
 * @apiParam {String} tableid 表id
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiSampleRequest http://localhost:21000/api/metaspace/metadata/table/sql/123

 * @apiSuccessExample {json} 返回示例:
 *     HTTP/1.1 200 OK
 *
 {
    "tableId": "d72b1e39-9174-495e-8f0f-41cc563c31c3",
    "sql": "CREATE TABLE `tax_2015`(  `id` int)ROW FORMAT SERDE   'org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe' STORED AS INPUTFORMAT   'org.apache.hadoop.mapred.TextInputFormat' OUTPUTFORMAT   'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'LOCATION  'hdfs://node1:9000/user/hive/warehouse/tax_2015'TBLPROPERTIES (  'transient_lastDdlTime'='1539329877')"
}

 */


/**
 * @api {get} /table/stat/today/:tableId 表当天的统计信息
 * @apiName 表当天的统计信息
 * @apiGroup metadataManage
 * @apiDescription
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiParam {String} tableId 表id
 * @apiSampleRequest http://localhost:31000/metaspace/table/stat/today/123

 * @apiSuccessExample {json} 返回示例:
 *     HTTP/1.1 200 OK
 *
 {
  "date": "2018-10-15",
  "fieldNum": 23,
  "fileNum": 2,
  "dataVolume": "192.8",
  "dataIncrement": "23.5",
  "sourceTable": [
    {
      "database": "default",
      "tableName": "test1",
      "tableId":"123"
    },
    {
      "database": "default",
      "tableName": "test2",
      "tableId":"123"
    }
  ]
}
 * @apiUse ErrorResponse
 */
/**
 * @api {post} /table/stat/history 表的历史统计信息
 * @apiName 表的历史统计信息
 * @apiGroup metadataManage
 * @apiDescription
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiParam {String} tableId 表id
 * @apiParam {String="1","2","3"} dataType 日期类型（1-日,2-月,3-年）
 * @apiParam {String} fromDate 开始日期(包含)
 * @apiParam {String} endDate 结束日期(包含)
 * @apiParam {Integer} offset 开始位置
 * @apiParam {Integer} limit 条数限制
 * @apiParamExample {json} 请求示例:
 * {
	"tableId":"123",
        "offset":0,
        "limit":15,
	"dateType":"1",
	"fromDate":"2018-10-10",
	"endDate":"2018-10-15"
}
 * @apiSuccessExample {json} 返回示例:
 *     HTTP/1.1 200 OK
 {
  "offset": 0,
  "total": 100,
  "list": [
    {
      "date": "2018-10-15",
      "fieldNum": 23,
      "fileNum": 2,
      "dataVolume": "192.8KB",
      "dataIncrement": "23.5MB",
      "dataVolumeNum":12.34
      "dataVolumeNumUnit":"KB"
      "dataIncrementNum":45.54
      "dataIncrementNumUnit":"MB"
      "sourceTable": [
        {
          "database": "default",
          "tableName": "test1",
          "tableId": "123"
        },
        {
          "database": "default",
          "tableName": "test2",
          "tableId": "123"
        }
      ]
    },
    {
      "date": "2018-10-16",
      "fieldNum": 23,
      "fileNum": 2,
      "dataVolume": "192.8KB",
      "dataIncrement": "23.5MB",
      "sourceTable": [
        {
          "database": "default",
          "tableName": "test1",
          "tableId": "123"
        },
        {
          "database": "default",
          "tableName": "test2",
          "tableId": "123"
        }
      ]
    }
  ]
}
 * @apiUse ErrorResponse
 */

/**
 * @api {post} /table/stat/sourceTableCount 数据来源表个数统计
 * @apiName 数据来源表个数统计
 * @apiGroup metadataManage
 * @apiDescription
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiParam {String} tableId 表id
 * @apiParam {String="1","2","3"} dataType 日期类型（1-日,2-月,3-年）
 * @apiParam {String} fromDate 开始日期(包含)
 * @apiParam {String} endDate 结束日期(包含)
 * @apiParam {Integer} offset 开始位置
 * @apiParam {Integer} limit 条数限制
 * @apiParamExample {json} 请求示例:
 * {
	"tableId":"123",
        "offset":0,
        "limit":15,
	"dateType":"1",
	"fromDate":"2018-10-10",
	"endDate":"2018-10-15"
}
 * @apiSuccessExample {json} 返回示例:
 *     HTTP/1.1 200 OK
 [
 {
 "tableName": "default.table1",
 "count": 5
},
 {
   "tableName": "default.table2",
   "count": 2
 }
 ]
 * @apiUse ErrorResponse
 */

