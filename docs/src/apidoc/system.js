/*
* 系统级的定义
*/

/**
 * @apiDefine AuthHeader
 *
 * @apiHeader (认证) {String} X-Gridsum-FullTicketId 国双SSO认证票据,用于标识发送请求的用户，如果缺乏目标资源的权限，返回403没有权限错误 ; 如果不是SSO票据则认为是AppKey。
 *
 */

/**
 * @apiDefine DataResponse
 *
 * @apiSuccess {Boolean} success 操作是否成功
 * @apiSuccess {String} url 请求路径和请求方法
 * @apiSuccess {String} message 具体错误详情,success为true的时候，不显示
 * @apiSuccess {String} createTime 响应时间
 *
 * @apiSuccessExample {json} 成功返回示例:
 * {
 *     "success": true,
 *     "url": "/cube/cube_16566549897_1cb07c54 -DELETE",
 *     "createTime": "2018-10-20 18:10:09"
 * }
 *
 * @apiErrorExample  {json} 失败返回示例:
 * {
 *     "success": false,
 *     "url": "/cube/cube_16566549897_1cdb07c54s -DELETE",
 *     "message": "[ResourceNotFoundException] : can not find the CUBE_OWNER[%]",
 *     "createTime": "2018-10-20 18:24:07"
 * }
 */

/**
 * @apiDefine ErrorResponse
 *
 * @apiSuccess {Boolean} success 操作是否成功
 * @apiSuccess {String} url 请求路径和请求方法
 * @apiSuccess {String} message 具体错误详情,success为true的时候，不显示
 * @apiSuccess {String} createTime 响应时间
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