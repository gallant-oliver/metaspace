/**
 * @api {post} /table/create/form 表单模式
 * @apiName 表单模式
 * @apiGroup offlineTable
 * @apiDescription
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiParam {String} database 数据库
 * @apiParam {String} tableName 表名
 * @apiParam {Integer=1,2} tableType 表类型(1-外部表，2-内部表)
 * @apiParam {String} expireDate 表过期时间（单位：天，-1表示永不过期）
 * @apiParam {String} [location] HDFS路径(外部表需要)
 * @apiParam {String} fields 表字段
 * @apiParam {Boolean} isPartition 是否分区
 * @apiParam {String} [partitionFields] 分区字段（分区需要）
 * @apiParam {String} [storedFormat] hdfs文件存储格式
 * @apiParam {String} [hdfsPath] hdfs文件路径
 * @apiParam {String} [fieldsTerminated] 字段分隔符
 * @apiParam {String} [lineTerminated] 行分隔符
 * @apiParamExample {json} 请求示例:
 * {
	"database":"test",
	"tableName":"tvd",
	"tableType":"1",
	"expireDate":"-1",
	"hdfsPath":"/user/gridsum",
	"fields":[
		{"columnName":"name","description":"姓名","type":"string"},
		{"columnName":"gender","description":"性别","type":"string"}
	],
	"isPartition":true,
	"partitionFields":["gender"],
	"storedFormat":"parquet",
	"fieldsTerminated":",",
	"lineTerminated":"\n"
}
 * @apiSuccessExample {json} 返回示例:
 *     HTTP/1.1 200 OK
 {
  "tableId":"123"
}
 * @apiUse ErrorResponse
 */

/**
 * @api {post} /table/create/sql SQL模式
 * @apiName SQL模式
 * @apiGroup offlineTable
 * @apiDescription
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiParam {String} sql sql语句
 * @apiParamExample {json} 请求示例:
 * {
	'sql':'CREATE  TABLE default.tvd101 (gender string COMMENT '性别') PARTITIONED BY (name string COMMENT '姓名') STORED AS AVRO'
}

 * @apiSuccessExample {json} 返回示例:
 *     HTTP/1.1 200 OK
 {
  "tableId":"123"
}
 * @apiUse ErrorResponse
 */
/**
 * @api {post} /table/sql/format 表单转sql
 * @apiName 表单转sql
 * @apiGroup offlineTable
 * @apiDescription
 * @apiVersion 1.1.0
 * @apiUse AuthHeader
 * @apiParam {String} database 数据库
 * @apiParam {String} tableName 表名
 * @apiParam {Integer=1,2} tableType 表类型(1-外部表，2-内部表)
 * @apiParam {String} expireDate 表过期时间（单位：天，-1表示永不过期）
 * @apiParam {String} [location] HDFS路径(外部表需要)
 * @apiParam {String} fields 表字段
 * @apiParam {Boolean} isPartition 是否分区
 * @apiParam {String} [partitionFields] 分区字段（分区需要）
 * @apiParam {String} [storedFormat] hdfs文件存储格式
 * @apiParam {String} [hdfsPath] hdfs文件路径
 * @apiParam {String} [fieldsTerminated] 字段分隔符
 * @apiParam {String} [lineTerminated] 行分隔符
 * @apiParamExample {json} 请求示例:
 * {
	"database":"test",
	"tableName":"tvd",
	"tableType":"1",
	"expireDate":"-1",
	"hdfsPath":"/user/gridsum",
	"fields":[
		{"columnName":"name","description":"姓名","type":"string"},
		{"columnName":"gender","description":"性别","type":"string"}
	],
	"isPartition":true,
	"partitionFiled":["gender"],
	"storedFormat":"parquet",
	"fieldsTerminated":",",
	"lineTerminated":"\n"
}
 * @apiSuccessExample {json} 返回示例:
 *     HTTP/1.1 200 OK
 CREATE EXTERNAL TABLE default.person (name string,age int) COMMENT 'desc person' PARTITIONED BY (gender int COMMENT 'sex') ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n" STORED AS parquet LOCATION '/usr/local/person'
 * @apiUse ErrorResponse
 */