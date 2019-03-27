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
 * @date 2019/3/26 19:56
 */
package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.share.APIInfo;
import io.zeta.metaspace.web.dao.DataShareDAO;
import org.apache.atlas.AtlasBaseClient;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/26 19:56
 */
@Service
public class DataShareService {

    private static final Logger LOG = LoggerFactory.getLogger(DataShareService.class);

    @Autowired
    DataShareDAO shareDAO;


    public int insertAPIInfo(APIInfo info) throws AtlasBaseException {
        try {
            return shareDAO.insertAPIInfo(info);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "插入失败");
        }
    }


    public int updateStarStatus(String apiGuid, Integer status) throws AtlasBaseException {
        try {
            return shareDAO.updateStarStatus(apiGuid, status);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新收藏状态失败");
        }
    }

    public int updatePublishStatus(List<String> apiGuid, Integer status) throws AtlasBaseException {
        try {
            return shareDAO.updatePublishStatus(apiGuid, status);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新发布状态失败");
        }
    }
}
