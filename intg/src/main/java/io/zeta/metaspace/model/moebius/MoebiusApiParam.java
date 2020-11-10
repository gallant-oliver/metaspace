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

import lombok.Data;

/**
 * @author lixiang03
 * @Data 2020/10/15 16:54
 */
@Data
public class MoebiusApiParam {
    //body参数
    private String paramBody;
    //query参数
    private String paramQuery;
    //header参数
    private String paramHeaders;
    //返回参数
    private String resBody;
    //备注
    private String note;

    @Data
    public static class BodyParam{
        private String type;
        private String mock;
        private String description;
    }

    @Data
    public static class HeaderParam{
        private String headerName;
        private int isrequired;
        private String headerType;
        private String headerValue;
        private String description;
    }

    @Data
    public static class QueryParam{
        private String queryName;
        private int isrequired;
        private String queryType;
        private String queryExample;
        private String description;
    }
}
