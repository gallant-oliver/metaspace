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
 * @date 2019/5/29 20:03
 */
package io.zeta.metaspace.web.task.init;

import io.zeta.metaspace.web.service.DataManageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/*
 * @description
 * @author sunhaoning
 * @date 2019/5/29 20:03
 */
@Component
public class OrganizationJob {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationJob.class);
    @Autowired
    DataManageService dataManageService;
    @PostConstruct
    public void springBeanJobFactory() throws AtlasBaseException {
        try {
            dataManageService.updateOrganization();
        } catch (Exception e) {
            LOG.info(e.toString());
        }
    }
}
