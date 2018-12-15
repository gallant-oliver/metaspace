/*
* 数据管理
*/
/**
 * @api {get} /metadata/category 获取全部目录层级
 * @apiName 元数据管理跳转
 * @apiGroup dataManage
 * @apiDescription
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiSuccessExample {json} 返回示例:
 *     HTTP/1.1 200 OK
 *     [
 {
     "guid": "9f099914-444c-4eb3-8954-c9f8565a424a",
     "qualifiedName": "dir_6",
     "name": "dir_6",
     "description": "dir_6_desc",
     "upBrotherCategoryGuid": "6ee3c9c6-b34e-471a-a593-2623eeefd7e4",
     "downBrotherCategoryGuid": null,
     "parentCategoryGuid": null
 },
 {
     "guid": "6ee3c9c6-b34e-471a-a593-2623eeefd7e4",
     "qualifiedName": "dir_5",
     "name": "dir_5",
     "description": "dir_5_desc",
     "upBrotherCategoryGuid": "e437775e-00b8-458f-ae8f-4cb6c04d61c7",
     "downBrotherCategoryGuid": "9f099914-444c-4eb3-8954-c9f8565a424a",
     "parentCategoryGuid": null
 },
 {
     "guid": "6e8b021c-5e7f-4a28-aaae-4de331ccdbab",
     "qualifiedName": "dir_6.dir_6_child_update",
     "name": "dir_6_child_update",
     "description": "测试更新",
     "upBrotherCategoryGuid": null,
     "downBrotherCategoryGuid": null,
     "parentCategoryGuid": "9f099914-444c-4eb3-8954-c9f8565a424a"
 },
 {
     "guid": "d11c99f2-f4d4-402d-b40e-c67694f8989f",
     "qualifiedName": "dir_2",
     "name": "dir_2",
     "description": "dir_2_desc",
     "upBrotherCategoryGuid": "e864c3c8-932e-4ba0-bb2d-56e10444659f",
     "downBrotherCategoryGuid": "5145030c-dec8-48a5-940f-e8e511fd3662",
     "parentCategoryGuid": null
 },
 {
     "guid": "e864c3c8-932e-4ba0-bb2d-56e10444659f",
     "qualifiedName": "dir_1",
     "name": "dir_1",
     "description": "dir_1_desc",
     "upBrotherCategoryGuid": null,
     "downBrotherCategoryGuid": "d11c99f2-f4d4-402d-b40e-c67694f8989f",
     "parentCategoryGuid": null
 },
 {
     "guid": "5145030c-dec8-48a5-940f-e8e511fd3662",
     "qualifiedName": "dir_3",
     "name": "dir_3",
     "description": "dir_3_desc",
     "upBrotherCategoryGuid": "d11c99f2-f4d4-402d-b40e-c67694f8989f",
     "downBrotherCategoryGuid": "e437775e-00b8-458f-ae8f-4cb6c04d61c7",
     "parentCategoryGuid": null
 },
 {
     "guid": "65653d21-8ab5-4911-acb8-c38029ba9b2d",
     "qualifiedName": "dir_3.dir_3_child_2",
     "name": "dir_3_child_2",
     "description": "dir_3_child_2_desc",
     "upBrotherCategoryGuid": "dc4b037c-f819-4f43-8f68-2146f0890c52",
     "downBrotherCategoryGuid": null,
     "parentCategoryGuid": "5145030c-dec8-48a5-940f-e8e511fd3662"
 },
 {
     "guid": "e437775e-00b8-458f-ae8f-4cb6c04d61c7",
     "qualifiedName": "dir_4",
     "name": "dir_4",
     "description": "dir_4_desc",
     "upBrotherCategoryGuid": "5145030c-dec8-48a5-940f-e8e511fd3662",
     "downBrotherCategoryGuid": "6ee3c9c6-b34e-471a-a593-2623eeefd7e4",
     "parentCategoryGuid": null
 },
 {
     "guid": "dc4b037c-f819-4f43-8f68-2146f0890c52",
     "qualifiedName": "dir_3.dir_3_child",
     "name": "dir_3_child",
     "description": "dir_3_child_desc",
     "upBrotherCategoryGuid": null,
     "downBrotherCategoryGuid": "65653d21-8ab5-4911-acb8-c38029ba9b2d",
     "parentCategoryGuid": "5145030c-dec8-48a5-940f-e8e511fd3662"
 }
 ]

 */

/**
 * @api {post} /metadata/category 在前/后添加同级目录
 * @apiName 在前/后添加同级目录
 * @apiGroup dataManage
 * @apiDescription
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiParam {string} guid 当前选中目录层级
 * @apiParam {string} name 目录名称
 * @apiParam {string} description 目录描述信息
 * @apiParam {string="up","down"} direction 创建方向
 * @apiParamExample {json} Request:
 * {
    "guid":"9f099914-444c-4eb3-8954-c9f8565a424a",
    "name":"dir_7",
    "description":"dir_7_desc",
    "direction":"down"
}

 * @apiSuccessExample {json} 返回示例:
 *     HTTP/1.1 200 OK
 *     {
    "guid": "bf32b8ed-71c0-41f7-ba40-1c08904cd76f",
    "qualifiedName": "dir_6.dir_11_child",
    "name": "dir_11_child",
    "description": "dir_10_child_desc",
    "upBrotherCategoryGuid": "8eef21b9-0888-4a60-bf57-20d03d2b078b",
    "downBrotherCategoryGuid": null,
    "parentCategoryGuid": "9f099914-444c-4eb3-8954-c9f8565a424a"
}

 */

/**
 * @api {post} /metadata/category 添加子目录
 * @apiName 添加子目录
 * @apiGroup dataManage
 * @apiDescription
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiParam {string} guid 当前选中目录层级
 * @apiParam {string} name 目录名称
 * @apiParam {string} description 目录描述信息
 * @apiParam {string} parentCategoryGuid 父目录guid
 * @apiParamExample {json} Request:
 * {
    "guid":"e3f3f519-e698-47f4-9ede-4d5d13f5b7d2",
    "name":"dir_7_child",
    "description":"dir_7_child_desc",
    "parentCategoryGuid":"e3f3f519-e698-47f4-9ede-4d5d13f5b7d2"
}
 * @apiSuccessExample {json} 返回示例:
 *     HTTP/1.1 200 OK
 *     {
    "guid": "bf32b8ed-71c0-41f7-ba40-1c08904cd76f",
    "qualifiedName": "dir_6.dir_11_child",
    "name": "dir_11_child",
    "description": "dir_10_child_desc",
    "upBrotherCategoryGuid": "8eef21b9-0888-4a60-bf57-20d03d2b078b",
    "downBrotherCategoryGuid": null,
    "parentCategoryGuid": "9f099914-444c-4eb3-8954-c9f8565a424a"
}


 */


/**
 * @api {post} /metadata/update/category 编辑目录
 * @apiName 编辑目录
 * @apiGroup dataManage
 * @apiDescription
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiParam {string} guid 当前选中目录层级
 * @apiParam {string} name 目录名称
 * @apiParam {string} description 目录描述信息
 * @apiParamExample {json} Request:
 * {
    "guid":"e3f3f519-e698-47f4-9ede-4d5d13f5b7d2",
    "name":"dir_7_desc",
    "description":"测试更新"
}

 * @apiSuccessExample {json} 返回示例:
 *     HTTP/1.1 200 OK
 *     {
    "guid": "bf32b8ed-71c0-41f7-ba40-1c08904cd76f",
    "qualifiedName": "dir_6.dir_11_child",
    "name": "dir_11_child",
    "description": "dir_10_child_desc",
    "upBrotherCategoryGuid": "8eef21b9-0888-4a60-bf57-20d03d2b078b",
    "downBrotherCategoryGuid": null,
    "parentCategoryGuid": "9f099914-444c-4eb3-8954-c9f8565a424a"
}

 */

/**
 * @api {delete} /metadata/category/categoryGuid 删除目录层级
 * @apiName 删除目录层级
 * @apiGroup dataManage
 * @apiDescription
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiParam {string} categoryGuid 目录guid
 * @apiSampleRequest http://localhost:31000/api/metaspace/metadata/category/6ba2320c-85f3-44a2-ae62-3f908c2397d9
 */

/**
 * @api {post} /metadata/category/{categoryGuid}/assignedEntities 添加关联
 * @apiName 添加关联
 * @apiGroup dataManage
 * @apiDescription
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiParam {string} categoryGuid 子层级唯一标识
 * @apiParam {string} tableGuid 数据表唯一标识
 * @apiParam {string} tableName 数据表名
 * @apiParam {string} dbName 数据库名
 * @apiParamExample {json} 请求示例:
 * [{
	"tableGuid":"b70dcf38-a4aa-4bc6-8fa1-d69b1e7bc292",
	"tableName":"words",
	"dbName":"default",
        "status":"ACTIVE"
}]

 */

/**
 * @api {post} /metadata/category/relations/:categoryGuid 获取关联关系
 * @apiName 获取关联关系
 * @apiGroup dataManage
 * @apiDescription
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiParamExample {json} 请求示例:
 * {
	"offset":0,
	"limit":2
}
 * @apiSuccessExample {json} 返回示例:
 * {
    "offset": 0,
    "count": 1,
    "sum": 1,
    "lists": [
        {
            "relationshipGuid": "8439e764-78ad-4a7c-8a40-3df04d3098b7",
            "categoryGuid": "e864c3c8-932e-4ba0-bb2d-56e10444659f",
            "categoryName": "dir_1",
            "tableName": "words",
            "dbName": "default",
            "tableGuid": "b70dcf38-a4aa-4bc6-8fa1-d69b1e7bc292",
            "path": "dir_1.words",
            "status": "ACTIVE"
        }
    ]
}
 */

/**
 * @api {delete} /metadata/category/relation 删除关联关系
 * @apiName 添加关联
 * @apiGroup dataManage
 * @apiDescription
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiParamExample {json} 请求示例:
 * [
 {
     "relationshipGuid":"7ee53379-2b8a-4ee1-8ade-f9d4379022db"
 }
 ]
 * @apiSuccessExample {json} 返回示例:
 * success

 */
/**
 * @api {post} /metadata/table/relations/ 获取表关联
 * @apiName 获取表关联
 * @apiGroup dataManage
 * @apiDescription
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiParamExample {json} 请求示例:
 * {
	"filterTableName":"word",
	"offset":0,
	"limit":2
}

 * @apiSuccessExample {json} 返回示例:
 * {
    "offset": 0,
    "count": 2,
    "sum": 2,
    "lists": [
        {
            "relationshipGuid": "8439e764-78ad-4a7c-8a40-3df04d3098b7",
            "categoryGuid": "e864c3c8-932e-4ba0-bb2d-56e10444659f",
            "categoryName": "dir_1",
            "tableName": "words",
            "dbName": "default",
            "tableGuid": "b70dcf38-a4aa-4bc6-8fa1-d69b1e7bc292",
            "path": "dir_1.words",
            "status": "ACTIVE"
        },
        {
            "relationshipGuid": "04804b0e-b9b6-4f59-9262-8f3755c44955",
            "categoryGuid": "e3f3f519-e698-47f4-9ede-4d5d13f5b7d2",
            "categoryName": "dir_7",
            "tableName": "words",
            "dbName": "default",
            "tableGuid": "b70dcf38-a4aa-4bc6-8fa1-d69b1e7bc292",
            "path": "dir_7_desc.words",
            "status": "ACTIVE"
        }
    ]
}

 */