/**
 * @api {get} /api/metaspace/user/logout 注销
 * @apiName 注销
 * @apiGroup system
 * @apiDescription
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiSampleRequest http://localhost:21000/api/metaspace/user/logout
 * @apiErrorExample {json} Error-Response:
 HTTP/1.1 404 Not Found
 *     {
 *       "errorCode": "METASPACE-500-00-000",
 *       "errorMessage": "服务器内部错误"
 *     }
 * @apiErrorExample {json} Error-Response:
 HTTP/1.1 506 Not Found
 *     {
 *       "errorCode": "METASPACE-506-00-001",
 *       "errorMessage": "服务器配置错误:xxx"
 *     }
 */

/**
 * @api {get} /api/metaspace/user/info 个人信息
 * @apiName 个人信息
 * @apiGroup system
 * @apiDescription
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiSampleRequest http://localhost:21000/api/metaspace/user/info

 * @apiSuccessExample {json} 返回示例:
 *     HTTP/1.1 200 OK
 *
 {
    "errorCode": 0,
    "data": {
        "LoginEmail": "xxx@gridsum.com",
        "DisplayName": "xxx",
        "AccountGuid": "xxxx",
        "extended": {
            "company": "Gridsum",
            "department": "技术部",
            "role": "Unknown"
        }
    },
    "message": "Success"
}
 */