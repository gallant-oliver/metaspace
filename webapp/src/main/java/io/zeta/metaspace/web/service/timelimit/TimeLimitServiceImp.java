package io.zeta.metaspace.web.service.timelimit;


import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.timelimit.TimeLimitRequest;
import io.zeta.metaspace.model.timelimit.TimeLimitSearch;
import io.zeta.metaspace.model.timelimit.TimelimitEntity;

import java.util.List;

/**
 * 时间限定业务接口类型
 */
public interface TimeLimitServiceImp {
    /**
     * 添加时间限定
      * @param req
     * @param tenantId
     */
     void addTimeLimit(TimeLimitRequest req, String tenantId);

     void delTimeLimit(TimeLimitRequest req, String tenantId);


     void editTimeLimit(TimeLimitRequest req, String tenantId);


     PageResult<TimelimitEntity> search(TimeLimitSearch search , String tenantId);



}
