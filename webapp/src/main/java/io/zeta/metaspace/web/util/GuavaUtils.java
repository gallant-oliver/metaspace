package io.zeta.metaspace.web.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import io.zeta.metaspace.SSOConfig;
import io.zeta.metaspace.utils.OKHttpClient;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GuavaUtils {
    private static Cache<String, Map> ticketCache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(10, TimeUnit.SECONDS).build();
    private static String infoURL = SSOConfig.getInfoURL();
    public static Cache<String, Map> getTicketCache() {
        return ticketCache;
    }
    private static Logger LOG = Logger.getLogger(GuavaUtils.class);

    public static Map getUserInfo(String ticket) throws ExecutionException {
        Gson gson = new Gson();
        HashMap<String, String> header = new HashMap<>();
        header.put("ticket", ticket);
        Map data = ticketCache.get(ticket, new Callable<Map>() {

            public Map call() throws Exception {
                String errorCode = null;
                String message = null;
                String proper = "0.0";
                String s = OKHttpClient.doGet(infoURL, null, header);
                JSONObject jsonObject = gson.fromJson(s, JSONObject.class);
                errorCode = Objects.toString(jsonObject.get("errorCode"));
                if (!proper.equals(errorCode)){
                    message=Objects.toString(jsonObject.get("message"));
                    StringBuffer detail = new StringBuffer();
                    detail.append("sso返回错误码:");
                    detail.append(errorCode);
                    detail.append("错误信息:");
                    detail.append(message);
                    throw new AtlasBaseException(detail.toString(), AtlasErrorCode.BAD_REQUEST, "sso获取用户详情出错");
                }
                Map data = (Map) jsonObject.get("data");
                ticketCache.put(ticket, data);
                return data;
            }
        });
        return data;
    }
}
