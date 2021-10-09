package io.zeta.metaspace.web.service;

import com.google.gson.reflect.TypeToken;
import io.zeta.metaspace.model.privilege.SSOAccount;
import io.zeta.metaspace.utils.GsonUtils;
import io.zeta.metaspace.utils.OKHttpClient;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供sso的查询接口
 */
@Service
public class SSORemoteService {
    private static Configuration conf;
    private static final Logger log = LoggerFactory.getLogger(SSORemoteService.class);
    private final static String SSO_PREFIX_URL;
    private final static String SSO_ALL_ACCOUNTS = "/api/v5/accounts";
    private final static String SSO_MATCH_QUERY = "/api/v6/queryVagueUserInfo";
    //重试次数
    private final static int times = 2;

    static {
        try {
            conf = ApplicationProperties.get();
            SSO_PREFIX_URL = conf.getString("sso.prefix.url");
            log.info("SSO url: {}",SSO_PREFIX_URL);
            if (StringUtils.isBlank(SSO_PREFIX_URL)) {
                throw new RuntimeException(new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "sso.prefix.url未正确配置"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /*
     * 获取SSO 全部账户信息，支持分页，参数endTime不为空时获取更新时间在此前的账户
     */
    public List<SSOAccount> queryAllAccounts(int currentPage,int pageSize){
        log.info("分页查询sso侧的所有账户信息");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String endTime = sdf.format(System.currentTimeMillis());

        Map<String,String> queryParamMap = new HashMap<>();
        queryParamMap.put("endTime",endTime);
        queryParamMap.put("currentPage",currentPage+"");
        queryParamMap.put("pageSize",pageSize+"");
        Map<String,String> headerMap = new HashMap<>();
        String responseStr = OKHttpClient.doGet(SSO_PREFIX_URL+SSO_ALL_ACCOUNTS,queryParamMap,headerMap,times);
        log.info("sso query {} ok.",SSO_ALL_ACCOUNTS);

        Map<String, Object> resultMap = GsonUtils.getInstance()
                .fromJson(responseStr, new TypeToken<Map<String, Object>>() {}.getType());
        if("0.0".equals(resultMap.getOrDefault("errorCode","").toString())){
            log.info("获取账户信息数据成功");
            String jsonString = GsonUtils.getInstance().toJson(resultMap.get("data"));
            List<SSOAccount> list = GsonUtils.getInstance()
                    .fromJson(jsonString, new TypeToken<List<SSOAccount>>() {}.getType());
            return list;
        }else {
            log.info("获取账户信息数据失败: {}",resultMap.getOrDefault("message","获取SSO全部账户失败"));
            return Collections.emptyList();
        }
    }

    /**
     * 根据用户名 displayName字段 模糊匹配
     */
    public List<SSOAccount> queryVagueUserInfo(int currentPage,int pageSize,String name){
        Map<String,String> queryParamMap = new HashMap<>();
        queryParamMap.put("displayName",name);
        queryParamMap.put("currentPage",currentPage+"");
        queryParamMap.put("pageSize",pageSize+"");

        Map<String,String> headerMap = new HashMap<>();
        headerMap.put("Content-Type","application/json");
        makeHeaderInfo(headerMap);
        String responseStr = OKHttpClient.doGet(SSO_PREFIX_URL+SSO_MATCH_QUERY,queryParamMap,headerMap,times);
        log.info("sso query {} ok.",SSO_MATCH_QUERY);

        Map<String, Object> resultMap = GsonUtils.getInstance()
                .fromJson(responseStr, new TypeToken<Map<String, Object>>() {}.getType());
        if("0.0".equals(resultMap.getOrDefault("errorCode","").toString())){
            log.info("获取账户信息数据成功");
            String jsonString = GsonUtils.getInstance().toJson(resultMap.get("data"));
            Map<String, Object> dataMap = GsonUtils.getInstance()
                    .fromJson(jsonString, new TypeToken<Map<String, Object>>() {}.getType());
            String rowsJsonString = GsonUtils.getInstance().toJson(resultMap.get("rows"));
            List<SSOAccount> list = GsonUtils.getInstance()
                    .fromJson(rowsJsonString, new TypeToken<List<SSOAccount>>() {}.getType());
            return list;
        }else {
            log.info("获取账户信息数据失败: {}",resultMap.getOrDefault("message","模糊匹配获取SSO账户失败"));
            return Collections.emptyList();
        }
    }

    private void makeHeaderInfo(Map<String, String> header){
        String puk = conf.getString("sso.encryption.public.key");
        String prk = conf.getString("sso.encryption.private.key");
        StringBuffer buffer = new StringBuffer();
        //私钥
        buffer.append("&key=");
        buffer.append(prk);

        //时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = sdf.format(System.currentTimeMillis());
        buffer.append("&time=");
        buffer.append(date);

        //随机字符串
        buffer.append("&nonce_str=");
        String randomString = RandomStringUtils.randomAlphanumeric(10);
        buffer.append(randomString);

        //md5加密并转换成大写
        String str = DigestUtils.md5Hex(buffer.toString()).toUpperCase();
        String authentication = date + "_" + puk + "_" + str;
        header.put("authentication", authentication);
        header.put("nonce_str", randomString);
    }
}
