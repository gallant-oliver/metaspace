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

package io.zeta.metaspace.web.util;

import io.zeta.metaspace.model.share.QueryResult;
import io.zeta.metaspace.web.service.DataShareService;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

/**
 * @author lixiang03
 * @Data 2020/8/19 16:38
 */
@Component
public class DataServiceUtil {
    @Autowired
    public DataShareService dataShareService;
    private static DataServiceUtil utils;
    public static String mobiusUrl;
    static{
        try {
            Configuration configuration = ApplicationProperties.get();
            mobiusUrl=configuration.getString("metaspace.mobius.url");
        } catch (AtlasException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void init() {
        utils = this;
    }

    public static QueryResult queryApiData(HttpServletRequest request) throws AtlasBaseException {
        return utils.dataShareService.queryApiDataV2(request);
    }
}
