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
package io.zeta.metaspace.web.service.dataquality;

import com.google.common.base.Joiner;
import io.zeta.metaspace.model.dataquality2.Warning;
import io.zeta.metaspace.model.dataquality2.WarningStatusEnum;
import io.zeta.metaspace.web.dao.dataquality.WarningDAO;
import io.zeta.metaspace.web.dao.dataquality.WarningGroupDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class WarningService {

    private static final Logger LOG = LoggerFactory.getLogger(WarningService.class);

    @Autowired
    private WarningDAO warningDAO;

    @Autowired
    private WarningGroupDAO warningGroupDAO;


    public List<Warning> list(WarningStatusEnum warningStatusEnum) throws AtlasBaseException {
        try {
            List<Warning> warningList = warningDAO.taskWaringLog(warningStatusEnum.getCode()).stream()
                    .map(warning -> {
                        String taskExecuteId = warning.getTaskExecuteId();
                        List<Map<String, Object>> taskRuleWarningLogs = warningDAO.taskRuleWarningLog(taskExecuteId);
                        warning.setWarningReceivers(warningReceivers(taskRuleWarningLogs));
                        Timestamp createTime = (Timestamp) taskRuleWarningLogs.get(0).get("create_time");
                        warning.setLastWarningTime(createTime);
                        return warning;
                    }).collect(Collectors.toList());
            return warningList;
        } catch (Exception e) {
            LOG.error("获取告警列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取告警列表失败");
        }
    }

    private String warningReceivers(List<Map<String, Object>> taskRuleWarningLogs) {
        List<String> contacts = taskRuleWarningLogs.stream().flatMap(taskRuleWarningLog -> {
            List<String> yellowWarningGroupId = Arrays.asList(taskRuleWarningLog.get("yellowWarningGroupId").toString().split(","));
            List<String> redWarningGroupId = Arrays.asList(taskRuleWarningLog.get("redWarningGroupId").toString().split(","));
            yellowWarningGroupId.addAll(redWarningGroupId);
            return yellowWarningGroupId.stream();
        }).distinct().flatMap(warningId -> {
            return Arrays.stream(warningGroupDAO.getById(warningId).getContacts().split(","));
        }).distinct().collect(Collectors.toList());
        return Joiner.on(",").join(contacts);
    }

    public void close(List<String> idList) throws AtlasBaseException {
        String closer = AdminUtils.getUserData().getUserId();
        warningDAO.closeByIdList(idList, closer);
    }

}
