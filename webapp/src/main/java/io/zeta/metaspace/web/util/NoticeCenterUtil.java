package io.zeta.metaspace.web.util;

import com.google.gson.Gson;
import com.gridsum.gdp.library.commons.utils.UUIDUtils;
import io.zeta.metaspace.utils.OKHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 通知工具类 邮件
 */
@Slf4j
public class NoticeCenterUtil {
    //定义邮件通知的接口
    private final static String NOTICE_EMAIL_API = "/notice-center/api/v1/missions";

    public static boolean sendEmail(String attachmentBase64content,String fileName,String content,String[] contacts){
        String emailUrlPrefix = "";
        try {
            emailUrlPrefix =  ApplicationProperties.get().getString("notice.email.url");
        } catch (AtlasException e) {
            log.error("获取通知url前缀失败", e);
            throw new AtlasBaseException(e);
        }

        if(StringUtils.isBlank(emailUrlPrefix)){
            log.error("properties ENV param [notice.email.url] is not setting");
            return false;
        }

        //组装请求接口参数
        HashMap<String,Object> headerMap =  new HashMap<String, Object>(3){
            private static final long serialVersionUID = 1L;
            {
                put("Content-Type","application/json");
                put("X-SSO-FullticketId", AdminUtils.getSSOTicket());
                put("X-Authenticated-Userid", AdminUtils.getUserData().getUserId());
            }
        };
        Map<String,Object> queryParamMap = new HashMap<String, Object>(1){
            private static final long serialVersionUID = 10L;
            {
                put("cmd","CreateMessageMission");
            }
        };
        HashMap<String,Object> jsonMap = new HashMap<>();
        jsonMap.put("event_id","notice__"+ UUIDUtils.alphaUUID());
        jsonMap.put("channel_instance_id",3);
        jsonMap.put("priority", 0);
        jsonMap.put("template_id", 0);
        //邮件地址列表去重
        List<String> contactList = Stream.of(contacts).distinct().collect(Collectors.toList());
        contacts = contactList.toArray(new String[contactList.size()]);
        jsonMap.put("contacts", contacts);
        jsonMap.put("datas", new HashMap<String,String>(1){{
            put("content",content);
        }});
        jsonMap.put("attachment", new HashMap<String,String>(2){{
            put("payload",attachmentBase64content);
            put("file_name",fileName);
        }});
        String json = new JSONObject(jsonMap).toString();
        String responseStr = OKHttpClient.doPost(emailUrlPrefix+NOTICE_EMAIL_API,headerMap,queryParamMap,json,3);
        log.info("通知返回响应:: {}",responseStr);
        if(StringUtils.isBlank(responseStr)){
            log.error("请求email服务失败");
            return false;
        }
        Gson gson = new Gson();
        Map map = gson.fromJson(responseStr, HashMap.class);
        Object status =  map.getOrDefault("error_code","-1");
        if( "0".equals(status) ){
            log.info("发送email成功");
            return true;
        }
        return false;
    }

}
