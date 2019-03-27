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
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/3/26 19:42
 */
package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.share.APIInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface DataShareDAO {

    @Insert({" <script>",
             " insert info apiInfo(guid,name,tableGuid,dbGuid,groupGuid,keeper,maxRowNumber,fields,",
             " version,description,protocol,requestMode,returnType,path,generateTime,updater,updateTime",
             " )values(",
             " #{guid},#{name},#{tableGuid},#{dbGuid},#{groupGuid},#{keeper},#{maxRowNumber},#{fields,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg},",
             " #{version},#{description},#{protocol},#{requestMode},#{returnType},#{path},#{generateTime},#{updater},#{updateTime}",
             ")"})
    public int insertAPIInfo(APIInfo info);

    @Update("update apiInfo set star=#{star} where guid=#{guid}")
    public int updateStarStatus(@Param("guid")String guid, @Param("star")Integer starStatus);

    @Update({" <script>",
             " update apiInfo set publish=#{publish} where guid in",
             " <foreach item='guid' index='index' collection='guidList' separator=',' open='(' close=')'>" ,
             " #{guid}",
             " </foreach>",
             " </script>"})
    public int updatePublishStatus(@Param("guidList")List<String> guidList, @Param("publish")Integer publishStatus);
}
