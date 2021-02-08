package io.zeta.metaspace.web.service.timelimit;


import io.zeta.metaspace.model.timelimit.TimeLimitRequest;
import io.zeta.metaspace.model.timelimit.TimeLimitSearch;
import io.zeta.metaspace.model.timelimit.TimelimitEntity;

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

     TimelimitEntity search(TimeLimitSearch search ,String tenantId);



}
