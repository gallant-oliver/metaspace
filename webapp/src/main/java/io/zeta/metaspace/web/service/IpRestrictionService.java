package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.ip.restriction.IpRestriction;
import io.zeta.metaspace.model.ip.restriction.IpRestrictionType;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.ApiPolyInfo;
import io.zeta.metaspace.web.dao.IpRestrictionDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasConfiguration;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class IpRestrictionService {

    @Autowired
    private IpRestrictionDAO ipRestrictionDAO;

    public void checkDuplicateName(String id, String name, String tenantId) {
        if (ipRestrictionDAO.countByName(id, name, tenantId) != 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "黑白名单策略名称已存在");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public int createIpRestriction(IpRestriction ipRestriction, String tenantId) {
        try {
            IpRestrictionType algorithm = ipRestriction.getType();
            if (algorithm == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数类型不正确");
            }

            if (CollectionUtils.isEmpty(ipRestriction.getIpList())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "ip 列表不能为空");
            }

            for (String ip : ipRestriction.getIpList()) {
                try {
                    new IpAddressMatcher(ip);
                } catch (Exception e) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "ip 格式错误：" + e.getMessage());
                }
            }

            String userId = AdminUtils.getUserData().getUserId();
            return ipRestrictionDAO.insert(userId, ipRestriction, tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "黑白名单策略创建失败 : " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateIpRestriction(IpRestriction ipRestriction, String tenantId) {
        try {
            if (StringUtils.isEmpty(ipRestriction.getId()) || StringUtils.isEmpty(ipRestriction.getName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数不正确");
            }
            if (ipRestrictionDAO.getIpRestriction(ipRestriction.getId()) == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "黑白名单策略不存在");
            }
            checkDuplicateName(ipRestriction.getId(), ipRestriction.getName(), tenantId);

            IpRestrictionType algorithm = ipRestriction.getType();
            if (algorithm == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数类型不正确");
            }


            if (CollectionUtils.isEmpty(ipRestriction.getIpList())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "ip 列表不能为空");
            }
            for (String ip : ipRestriction.getIpList()) {
                try {
                    new IpAddressMatcher(ip);
                } catch (Exception e) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "ip 格式错误：" + e.getMessage());
                }
            }

            return ipRestrictionDAO.update(ipRestriction);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "黑白名单策略更新失败 : " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateIpRestrictionEnable(String ipRestrictionId, Boolean enable, String tenantId) {
        try {
            if (StringUtils.isEmpty(ipRestrictionId) || enable == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "参数不正确");
            }
            IpRestriction rule = ipRestrictionDAO.getIpRestriction(ipRestrictionId);
            if (rule == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "黑白名单策略不存在");
            }

            return ipRestrictionDAO.updateEnable(ipRestrictionId, enable);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "黑白名单策略更新启用禁用失败 : " + e.getMessage());
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public int deletedIpRestriction(List<String> ipRestrictionIds, String tenantId) {
        try {
            if (ipRestrictionIds != null && !ipRestrictionIds.isEmpty()) {
                for (String ipRestrictionId : ipRestrictionIds) {
                    if (ipRestrictionDAO.getIpRestriction(ipRestrictionId) == null) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "黑白名单策略不存在 id : " + ipRestrictionId);
                    }
                }
            }
            return ipRestrictionDAO.delete(ipRestrictionIds);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "黑白名单策略删除失败 : " + e.getMessage());
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public IpRestriction getIpRestriction(String id) {
        try {
            IpRestriction ipRestriction = ipRestrictionDAO.getIpRestriction(id);
            if (ipRestriction == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "黑白名单策略不存在 id : " + id);
            }
            return ipRestriction;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "黑白名单策略获取失败 : " + e.getMessage());
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public PageResult<IpRestriction> getIpRestrictionList(Parameters parameters, Boolean enable, IpRestrictionType type, String tenantId) {
        try {
            PageResult<IpRestriction> pageResult = new PageResult<>();
            List<IpRestriction> result = ipRestrictionDAO.getIpRestrictions(parameters, enable, type, tenantId);
            pageResult.setCurrentSize(result.size());
            pageResult.setLists(result);
            pageResult.setTotalSize(result.size() == 0 ? 0 : result.get(0).getTotal());
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "黑白名单策略获取失败 : " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public PageResult<ApiPolyInfo> getIpRestrictionApiPolyInfoList(String ipRestrictionId, Parameters parameters, String status, String tenantId) {
        try {
            PageResult<ApiPolyInfo> pageResult = new PageResult<>();
            List<ApiPolyInfo> result = ipRestrictionDAO.getApiPolyInfoList(ipRestrictionId, AdminUtils.getUserData().getUserId(), tenantId, parameters, status, AtlasConfiguration.METASPACE_API_POLY_EFFECTIVE_TIME.getLong());
            pageResult.setCurrentSize(result.size());
            pageResult.setLists(result);
            pageResult.setTotalSize(result.size() == 0 ? 0 : result.get(0).getTotal());
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "黑白名单策略获取关联API失败 : " + e.getMessage());
        }
    }


}
