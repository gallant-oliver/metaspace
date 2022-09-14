package io.zeta.metaspace.model.share.apisix;

import io.zeta.metaspace.model.moebius.MoebiusApiData;
import io.zeta.metaspace.model.share.ApiInfoV2;
import lombok.Data;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author huangrongwen
 * @Description: apisix实体
 * @date 2022/8/2417:47
 */
@Data
public class ApiSixCreateInfo {

    private String[] uris;
    private String upstream_id;
    private String[] methods;
    private String desc;

    //接口参数为数组，当前需求只需要单个
    public ApiSixCreateInfo(ApiInfoV2 apiInfoV2) {
        this.uris = new String[]{regularNewPath(apiInfoV2)};
        this.upstream_id = getUpstreamId();
        this.methods = new String[]{apiInfoV2.getRequestMode()};
        this.desc = apiInfoV2.getName();
    }

    //上游id是来源于配置
    private String getUpstreamId() {
        try {
            Configuration configuration = ApplicationProperties.get();
            return configuration.getString("apisix.upstream.id");
        } catch (AtlasException e) {
            throw new AtlasBaseException("未配置apiSix的上游地址id");
        }
    }

    public static String newPath(ApiInfoV2 apiInfoV2) {
        StringBuilder url = new StringBuilder();
        url.append("/api/metaspace/dataservice/");
        url.append(apiInfoV2.getGuid());
        url.append("/");
        url.append(apiInfoV2.getVersion());
        if (apiInfoV2.getPath() != null && !apiInfoV2.getPath().startsWith("/")) {
            url.append("/");
        }
        if (apiInfoV2.getPath() != null) {
            url.append(apiInfoV2.getPath());
        }
        return url.toString();
    }

    //api的id加版本构成唯一性，所以apiSix系统上只需要匹配唯一前缀进行转发(高效，且不用担心path参数匹配问题)
    private static String regularNewPath(ApiInfoV2 apiInfoV2) {
        return "/api/metaspace/dataservice/" + apiInfoV2.getGuid() + "/" + apiInfoV2.getVersion() +"*";
    }
}
