package io.zeta.metaspace.web.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import io.zeta.metaspace.SSOConfig;
import io.zeta.metaspace.utils.SSLClient;
import org.json.simple.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GuavaUtils {
    private static Cache<String, Map> ticketCache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(10, TimeUnit.SECONDS).build();
    private static String infoURL = SSOConfig.getInfoURL();
    public static Cache<String, Map> getTicketCache() {
        return ticketCache;
    }

    public static Map getUserInfo(String ticket) throws ExecutionException {

        HashMap<String, String> header = new HashMap<>();
        header.put("ticket", ticket);
        Map data = ticketCache.get(ticket, new Callable<Map>() {

            public Map call() throws Exception {
                String s = SSLClient.doGet(infoURL, header);
                Gson gson = new Gson();
                JSONObject jsonObject = gson.fromJson(s, JSONObject.class);
                Object message = jsonObject.get("message");
                if (message == null || (!message.toString().equals("Success"))) {
                    throw new Exception();
                }
                Map data = (Map) jsonObject.get("data");
                ticketCache.put(ticket, data);
                return data;
            }
        });
        return data;
    }
}
