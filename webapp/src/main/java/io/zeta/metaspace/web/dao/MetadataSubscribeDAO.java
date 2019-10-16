package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.metadata.SubscriptionInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MetadataSubscribeDAO {

    @Insert({"<script>",
            "insert into metadata_subscribe(user_id, table_guid, create_time)values(",
            "#{info.userId},#{info.tableGuid},#{info.createTime})",
            "</script>"})
    public int addMetadataSubscription(@Param("info")SubscriptionInfo info);

    @Delete({"<script>",
            "delete from metadata_subscribe where user_id=#{userId} and table_guid=#{tableGuid}",
            "</script>"})
    public int removeMetadataSubscription(@Param("userId")String userId, @Param("tableGuid")String tableGuid);

    @Select("select user_id from metadata_subscribe where table_guid=#{tableGuid}")
    public List<String> getSubscribeUserIdList(@Param("tableGuid")String tableGuid);
}
