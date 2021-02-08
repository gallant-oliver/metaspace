package io.zeta.metaspace.web.service.timelimit;

import io.zeta.metaspace.model.timelimit.TimeLimitRequest;
import io.zeta.metaspace.model.timelimit.TimeLimitSearch;
import io.zeta.metaspace.model.timelimit.TimelimitEntity;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.ApiGroupDAO;
import io.zeta.metaspace.web.dao.TimeLimitDAO;
import io.zeta.metaspace.web.service.ApiGroupService;
import io.zeta.metaspace.web.util.AdminUtils;
import kafka.api.ApiUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class TimeLimitService implements TimeLimitServiceImp{

    private static final Logger LOG = LoggerFactory.getLogger(ApiGroupService.class);

    @Autowired
    TimeLimitDAO timeLimitDAO;

    @Override
    public void addTimeLimit(TimeLimitRequest req, String tenantId) {
        try {
            //重名检查
            List<String> timeLimitByName = timeLimitDAO.getTimeLimitByName(req.getName(), tenantId);
            if(timeLimitByName!=null && timeLimitByName.size()>0){ //添加的时候不应该有重名，新版本可能name重复，在发布端不检测重名
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"时间限定重名");
            }
            User userData = AdminUtils.getUserData(); //获取当前用户信息
            TimelimitEntity timelimitEntity = new TimelimitEntity(); //数据实体
            timelimitEntity.setCreateTime(new Timestamp(new Date().getTime()));
            timelimitEntity.setCreator(userData.getUserId());
            timelimitEntity.setId(UUID.randomUUID().toString());
            timelimitEntity.setStartTime(req.getStartTimeTimestamp());
            timelimitEntity.setEndTime(req.getEndTimeTimestamp());
            timelimitEntity.setName(req.getName());
            timelimitEntity.setState("1");//新建
            timelimitEntity.setType(req.getGrade());
            timelimitEntity.setUpdater(userData.getUserId());
            timelimitEntity.setUpdateTime(new Timestamp(new Date().getTime()));
            timelimitEntity.setDesc(req.getDesc());
            timelimitEntity.setTenantId(tenantId);


            timeLimitDAO.addTimeLimit(timelimitEntity, tenantId);
        }catch (AtlasBaseException e){
            LOG.error(e.getMessage());
            throw e;
        }
        catch (Exception e){
            LOG.error(e.getMessage());
            throw e;
        }

    }

    @Override
    public TimelimitEntity search(TimeLimitSearch search, String tenantId) {
        // TODO: 2021-02-07 实现时间限定的搜索业务
        return null;
    }
}
