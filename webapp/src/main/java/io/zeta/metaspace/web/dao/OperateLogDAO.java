// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.metadata.OperateLogRequest;
import io.zeta.metaspace.model.operatelog.OperateLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OperateLogDAO {

    @Select({"<script>",
             " select a.id,a.number,a.userid,b.username,a.type,a.module,a.content,a.result,a.ip,a.createtime," +
                     " count(*) over() as total",
             " from operate_log a inner join users b on a.userid=b.userid where tenantid=#{tenantId}",
             " <if test=\"request.query.type != null and request.query.type!=''\"> ",
             " and a.type=#{request.query.type} ",
             " </if> ",
             " <if test=\"request.query.starttime != null and request.query.starttime!=''\"> ",
             " and a.createtime <![CDATA[>=]]> TO_TIMESTAMP(#{request.query.starttime},'YYYY-MM-DD HH24:MI:SS') ",
             " </if> ",
             " <if test=\"request.query.endtime != null and request.query.endtime!=''\"> ",
             " and a.createtime <![CDATA[<=]]> TO_TIMESTAMP(#{request.query.endtime},'YYYY-MM-DD HH24:MI:SS') ",
             " </if> ",
             " <if test=\"request.query.result != null and request.query.result!=''\"> ",
             " and a.result=#{request.query.result} ",
             " </if> ",
             " <if test=\"request.query.keyword != null and request.query.keyword!=''\"> ",
             " and ( b.username like concat('%',#{request.query.keyword},'%') ESCAPE '/' or a.number like concat('%',#{request.query.keyword},'%') ESCAPE '/' or a.ip like concat('%',#{request.query.keyword},'%') ESCAPE '/' )",
             " </if>",
             " <if test=\"request.query.module != null and request.query.module!='' and request.query.module != 'overview' \"> ",
             " and a.module=#{request.query.module}",
             " </if>",
             " order by a.createtime desc ",
             " <if test='request.limit != -1'>",
             " limit #{request.limit} offset #{request.offset} ",
             " </if>",
             " </script>"})
    List<OperateLog> search(@Param("request") OperateLogRequest request,@Param("tenantId")String tenantId);

    @Insert("insert into operate_log values(#{operateLog.id},(select to_char((select COALESCE(max(cast(number as integer)) + 1, 1) from operate_log where tenantid=#{tenantId}),'00000000')),#{operateLog.userid},#{operateLog.type},#{operateLog.module},#{operateLog.content},#{operateLog.result},#{operateLog.ip},#{operateLog.createtime},#{tenantId})")
    int insert(@Param("operateLog") OperateLog operateLog,@Param("tenantId")String tenantId);
}
