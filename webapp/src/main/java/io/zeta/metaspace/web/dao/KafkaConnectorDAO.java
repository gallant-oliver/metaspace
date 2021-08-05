package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.kafkaconnector.KafkaConnector;
import org.apache.atlas.notification.rdbms.KafkaConnectorUtil;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface KafkaConnectorDAO {

    @Insert("insert into connector (id, name, connector_class, db_type, db_ip, db_port, db_name, db_user, db_password, tasks_max, db_fetch_size, start_scn)" +
            "values(#{connector.id}, #{connector.name}, #{connector.config.connectorClass}, #{connector.config.dbType}, #{connector.config.dbIp}, " +
            "#{connector.config.dbPort}, #{connector.config.dbName}, #{connector.config.dbUser}, #{connector.config.dbPassword}, #{connector.config.tasksMax}," +
            "#{connector.config.dbFetchSize}, #{connector.config.startScn})")
    void insertConnector(@Param("connector") KafkaConnector connector);

    @Select("select id, name from connector where name = #{name} and is_deleted = false")
    @Results(id="connectorMap",
            value= {
                    @Result(id=true,column="id",property="id"),
                    @Result(column="name",property="name"),
                    @Result(column="id", property="config",one = @One(select = "selectConnectorConfig")),
            })
    KafkaConnector selectConnectorByName(@Param("name") String name);

    @Select("select id, name from connector where db_ip = #{dbIp} and db_port = #{dbPort} and db_name = #{dbName} and is_deleted = false")
    @ResultMap("connectorMap")
    KafkaConnector selectConnector(@Param("dbIp")String dbIp, @Param("dbPort")int dbPort, @Param("dbName")String dbName);

    /**
     * 查询KafkaConnector配置，该接口在selectConnectorByName中mybatis中的反射调用，切勿删除！！！
     * @param id
     * @return
     */
    @Select("select name, connector_class, db_type, db_ip, db_port, db_name, db_user, db_password, tasks_max, db_fetch_size, start_scn " +
            "from connector where id = #{id} and is_deleted = false")
    KafkaConnector.Config selectConnectorConfig(@Param("id") String id);

    @Select("select id, name from connector where is_deleted = false")
    @ResultMap("connectorMap")
    List<KafkaConnector> selectConnectors();

    @Update("update connector set is_deleted = true where name = #{name}")
    void deleteConnector(@Param("name") String name);
}
