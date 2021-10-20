package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.dto.UserPermissionRequest;
import io.zeta.metaspace.model.privilege.SSOAccount;
import io.zeta.metaspace.model.privilege.UserPermission;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.dao.UserPermissionDAO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserPermissionService {
    private final Logger logger  = LoggerFactory.getLogger(UserPermissionService.class);
    @Autowired
    private UserPermissionDAO userPermissionDAO;
    @Autowired
    private SSORemoteService ssoRemoteService;

    public PageResult<UserPermission> queryUserPermissionPageList(String name, int offset,int limit,String sortType,String orderBy){
        List<UserPermission> result = userPermissionDAO.getUserPermissionPageList(name, offset, limit,sortType,orderBy);
        PageResult<UserPermission> pageResult = new PageResult<>();
        if(CollectionUtils.isEmpty(result)){
            logger.info("没有查找到数据");
            result = Collections.emptyList();
            pageResult.setCurrentSize(0);
            pageResult.setTotalSize(0);
        }else{
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pageResult.setCurrentSize(result.size());
            pageResult.setTotalSize(result.get(0).getTotal());
            result.stream().forEach(v->{
                try {
                    String formatDate = formatter.format(formatter.parse(v.getCreateTime()));
                    v.setCreateTime(formatDate);
                } catch (ParseException e) {
                    logger.error("日期解析出错:{}",e);
                }
            });
        }
        pageResult.setOffset(offset);
        pageResult.setLists(result);
        return pageResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public int removeUserPermission(String userId){
        int count = userPermissionDAO.deleteByUserId(userId);
        logger.info("删除用户信息:{}",count);
        return count;
    }

    /**
     * 查询sso接口，获取用户列表 （模糊查询包含总数记录）
     * @param offset
     * @param pageSize
     * @param name
     * @return
     */
    public PageResult<UserPermission> querySSOUsers(int offset,int pageSize,String name){
        int currentPage = offset/pageSize + 1;
        logger.info("获取当前页码是:{}",currentPage);
        PageResult<UserPermission> pageResult = new PageResult<>();
        List<SSOAccount> accountList = querySSOAccount(currentPage,pageSize,name);
        if(CollectionUtils.isEmpty(accountList) ){
            logger.info("sso查询没有获取到用户信息.");
            pageResult.setCurrentSize(0);
            pageResult.setTotalSize(0);
            pageResult.setOffset(offset);
            pageResult.setLists(Collections.emptyList());
            return pageResult;
        }

        List<UserPermission> result = new ArrayList<>();
        while(CollectionUtils.isNotEmpty(accountList)){
            //排除已配置过全局权限的用户信息
            List<String> existList = userPermissionDAO.getByUserIdList(accountList.stream().map(SSOAccount::getAccountGuid)
                    .collect(Collectors.toList()));

            UserPermission item = null;
            for(SSOAccount account : accountList){
                String userId = account.getAccountGuid();
                if(existList != null && existList.contains(userId)){
                    continue;
                }
                item = new UserPermission();
                item.setUserId(userId);
                item.setAccount(account.getLoginEmail());
                item.setUsername(account.getDisplayName());
                result.add(item);
            }
            if(result.size() < pageSize){
                logger.info("获取下一页的sso数据.");
                currentPage = currentPage + 1;
                accountList = querySSOAccount(currentPage,pageSize,name);
            }else{
                logger.info("当前页数据满足页大小，返回需要的大小.");
                result = result.subList(0,pageSize);
                break;
            }
        }

        pageResult.setCurrentSize(result.size());
        pageResult.setTotalSize(0);
        pageResult.setOffset(offset);
        pageResult.setLists(result);
        return pageResult;
    }

    private List<SSOAccount> querySSOAccount(int currentPage,int pageSize,String name){
        if(StringUtils.isBlank(name)){
            logger.info("获取sso的全部账户信息");
            return ssoRemoteService.queryAllAccounts(currentPage, pageSize);
        }else{
            logger.info("模糊匹配获取sso的账户信息");
            return ssoRemoteService.queryVagueUserInfo(currentPage, pageSize,name);
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public int savePermission(List<UserPermissionRequest> userPermissionList){
        List<UserPermission> lists = new ArrayList<>();
        UserPermission item = null;
        for(UserPermissionRequest param : userPermissionList){
            item = new UserPermission();
            item.setUserId(param.getUserId());
            item.setAccount(param.getAccount());
            item.setUsername(param.getUsername());
            item.setPermissions(true);
            lists.add(item);
        }
        return userPermissionDAO.batchSave(lists);
    }
}
