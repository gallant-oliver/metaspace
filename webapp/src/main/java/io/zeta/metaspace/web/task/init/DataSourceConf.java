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

package io.zeta.metaspace.web.task.init;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.zeta.metaspace.repository.util.HbaseUtils;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasConfiguration;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lixiang03
 * @Data 2020/7/2 10:43
 */
@Configuration
public class DataSourceConf {
    public String driverClassName;
    public String jdbcUrl;
    public String username;
    public String password;
    public int minimumIdle;
    public int maximumPoolSize;
    public int idleTimeout;
    public long connectionTimeout;
    public DataSourceConf() throws AtlasBaseException {
        try{
            org.apache.commons.configuration.Configuration configuration = ApplicationProperties.get();
            driverClassName=configuration.getString("metaspace.database.driverClassName");
            jdbcUrl=configuration.getString("metaspace.database.url");
            username=configuration.getString("metaspace.database.username");
            password=configuration.getString("metaspace.database.password");
            minimumIdle=configuration.getInt("metaspace.database.minPoolSize");
            maximumPoolSize=configuration.getInt("metaspace.database.maxPoolSize");
            idleTimeout=configuration.getInt("metaspace.database.maxIdleTime");
            connectionTimeout=configuration.getLong("metaspace.database.checkoutTimeout");
        }catch (Exception e){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "初始化表失败：" + e.getMessage());
        }
    }
    @Bean("conf")
    public DataSourceConf getDataSource(){
        return this;
    }
}
