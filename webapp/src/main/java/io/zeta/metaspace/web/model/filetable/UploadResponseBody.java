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

package io.zeta.metaspace.web.model.filetable;


import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.zeta.metaspace.web.common.filetable.UploadPreview;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


@JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class UploadResponseBody {

    private String requestId;
    private UploadPreview uploadPreview;

    public String getRequestId() {
        return requestId;
    }

    public UploadResponseBody setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public UploadPreview getUploadPreview() {
        return uploadPreview;
    }

    public UploadResponseBody setUploadPreview(UploadPreview uploadPreview) {
        this.uploadPreview = uploadPreview;
        return this;
    }

}

