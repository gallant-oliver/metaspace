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

import io.zeta.metaspace.repository.util.HbaseUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lixiang03
 * @Data 2019/10/22 15:46
 */

@Configuration
public class HbaseConfig {
    @Bean
    public String createTableStat() throws AtlasBaseException {
        try{
            HbaseUtils.createTableStat();
            return "success";
        }catch (Exception e){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "初始化表失败：" + e.getMessage());
        }
    }
}
