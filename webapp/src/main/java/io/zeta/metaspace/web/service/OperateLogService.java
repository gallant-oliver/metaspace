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

import io.zeta.metaspace.model.metadata.OperateLogRequest;
import io.zeta.metaspace.model.operatelog.OperateEnum;
import io.zeta.metaspace.model.operatelog.OperateLog;
import io.zeta.metaspace.model.operatelog.OperateResultEnum;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.dao.OperateLogDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperateLogService {

    @Autowired
    private OperateLogDAO operateLogDAO;

    public PageResult<OperateLog> search(OperateLogRequest operateLogRequest) {
        List<OperateLog> list = operateLogDAO.search(operateLogRequest)
                .stream().map(operateLog -> {
                    operateLog.setType(OperateTypeEnum.of(operateLog.getType()).getCn());
                    operateLog.setResult(OperateResultEnum.of(operateLog.getResult()).getCn());
                    return operateLog;
                }).collect(Collectors.toList());
        PageResult<OperateLog> pageResult = new PageResult<>();
        long sum = operateLogDAO.queryCountBySearch(operateLogRequest);
        pageResult.setOffset(operateLogRequest.getOffset());
        pageResult.setSum(sum);
        pageResult.setCount(list.size());
        pageResult.setLists(list);
        return pageResult;
    }

    public void insert(OperateLog operateLog) {
        operateLogDAO.insert(operateLog);
    }

    public List<OperateEnum> typeList() {
        return Arrays.stream(OperateTypeEnum.values()).map(type -> new OperateEnum(type.getEn(), type.getCn())).collect(Collectors.toList());
    }

    public List<OperateEnum> resultList() {
        return Arrays.stream(OperateResultEnum.values()).map(result -> new OperateEnum(result.getEn(), result.getCn())).collect(Collectors.toList());
    }
}
