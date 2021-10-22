package io.zeta.metaspace.web.service;

import com.google.gson.Gson;
import io.zeta.metaspace.SSOConfig;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.utils.OKHttpClient;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Service
public class RoleService {
    private static final Logger LOG = LoggerFactory.getLogger(RoleService.class);

    public User getUserInfo(String userId) throws AtlasBaseException {
        String errorCode = null;
        String message = null;
        String proper = "0.0";
        String userInfoURL = SSOConfig.getUserInfoURL();
        HashMap<String, String> header = new HashMap<>();
        Map<String, String> queryDataParamMap = new HashMap<>();
        queryDataParamMap.put("id", userId);
        String userSession = OKHttpClient.doGet(userInfoURL, queryDataParamMap, header);
        Gson gson = new Gson();
        Map userBody = gson.fromJson(userSession, Map.class);
        String data = "data";
        if (StringUtils.isEmpty(userBody.get(data).toString())) {
            return null;
        }
        errorCode = Objects.toString(userBody.get("errorCode"));
        if (!proper.equals(errorCode)){
            message=Objects.toString(userBody.get("message"));
            StringBuffer detail = new StringBuffer();
            detail.append("sso返回错误码:");
            detail.append(errorCode);
            detail.append("错误信息:");
            detail.append(message);
            throw new AtlasBaseException(detail.toString(),AtlasErrorCode.BAD_REQUEST, "sso获取用户详情出错");
        }
        Map userData = (Map) userBody.get(data);
        String email = userData.get("loginEmail").toString();
        String name = userData.get("displayName").toString();
        User user = new User();
        user.setUserId(userId);
        user.setUsername(name);
        user.setAccount(email);
        return user;
    }

}
