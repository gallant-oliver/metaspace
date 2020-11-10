// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================

package io.zeta.metaspace.model.moebius;

import io.zeta.metaspace.model.share.ApiInfoV2;
import lombok.Data;

/**
 * @author lixiang03
 * @Data 2020/10/15 16:48
 */
@Data
public class MoebiusApiData {
    public MoebiusApiData(){

    }

    public MoebiusApiData(ApiInfoV2 apiInfoV2){
        this.name=apiInfoV2.getName();
        this.desc=apiInfoV2.getDescription();
        StringBuffer urlBuffer = new StringBuffer();
        urlBuffer.append("/api/metaspace/dataservice/");
        urlBuffer.append(apiInfoV2.getGuid());
        urlBuffer.append("/");
        urlBuffer.append(apiInfoV2.getVersion());
        if (apiInfoV2!=null&&apiInfoV2.getPath()!=null&&!apiInfoV2.getPath().startsWith("/")){
            urlBuffer.append("/");
        }
        urlBuffer.append(apiInfoV2.getPath());
        this.url = urlBuffer.toString();
        this.method=apiInfoV2.getRequestMode();
        this.version=apiInfoV2.getVersion();
    }
    //名字
    private String name;
    //描述
    private String desc;
    //路径
    private String url;
    //方法
    private String method;
    //地址
    private String upstream;
    //版本
    private String version;
    //来源
    private String origin="metaspace";
}
