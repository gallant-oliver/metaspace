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
package io.zeta.metaspace.web.service;

import com.google.common.base.Enums;
import com.gridsum.gdp.library.commons.utils.UUIDUtils;

import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.model.metadata.OperateLogRequest;
import io.zeta.metaspace.model.operatelog.OperateEnum;
import io.zeta.metaspace.model.operatelog.OperateLog;
import io.zeta.metaspace.model.operatelog.OperateModule;
import io.zeta.metaspace.model.operatelog.OperateResultEnum;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.dao.OperateLogDAO;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperateLogService {

    @Autowired
    private OperateLogDAO operateLogDAO;

    public PageResult<OperateLog> search(OperateLogRequest operateLogRequest, String tenantId) {
        List<OperateLog> list = operateLogDAO.search(operateLogRequest, tenantId)
                .stream().map(operateLog -> {
                    operateLog.setType(OperateTypeEnum.of(operateLog.getType()).getCn());
                    operateLog.setResult(OperateResultEnum.of(operateLog.getResult()).getCn());
                    ModuleEnum currentEnum = Enums.getIfPresent(ModuleEnum.class,operateLog.getModule().toUpperCase()).orNull();
                    operateLog.setModule(currentEnum == null ? "" : currentEnum.getName());
                    return operateLog;
                }).collect(Collectors.toList());
        PageResult<OperateLog> pageResult = new PageResult<>();
        long total = 0;
        if (CollectionUtils.isNotEmpty(list)) {
            total = list.get(0).getTotal();
        }
        pageResult.setTotalSize(total);
        pageResult.setCurrentSize(list.size());
        pageResult.setLists(list);
        return pageResult;
    }

    public void insert(OperateLog operateLog, String tenantId) {
        operateLog.setId(UUIDUtils.uuid());
        operateLogDAO.insert(operateLog, tenantId);
    }

    public List<OperateEnum> typeList() {
        return Arrays.stream(OperateTypeEnum.values()).map(type -> new OperateEnum(type.getEn(), type.getCn())).collect(Collectors.toList());
    }

    public List<OperateEnum> resultList() {
        return Arrays.stream(OperateResultEnum.values()).map(result -> new OperateEnum(result.getEn(), result.getCn())).collect(Collectors.toList());
    }

    public List<OperateModule> moduleList() {
        return Arrays.stream(ModuleEnum.values()).filter(module -> module.getType() != 0)
                .filter(module -> MetaspaceConfig.getDataService() ? !ModuleEnum.DATASHARE.equals(module) : !(ModuleEnum.AUDIT.equals(module) || ModuleEnum.APIMANAGE.equals(module)))
                .filter(module -> !(MetaspaceConfig.getOperateLogModuleMoon() && (ModuleEnum.NORMDESIGN.equals(module) || ModuleEnum.MODIFIER.equals(module) || ModuleEnum.TIMELIMIT.equals(module) || ModuleEnum.APPROVERMANAGE.equals(module))))
                .map(module -> new OperateModule(module.getName(), module.getAlias())).collect(Collectors.toList());
    }
}
