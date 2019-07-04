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
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/3/28 11:01
 */
package io.zeta.metaspace.model.share;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/28 11:01
 */
@XmlRootElement(name="root")
public class XmlQueryResult extends QueryResult implements Serializable {
    private QueryData datas;

    public QueryData getDatas() {
        return datas;
    }

    public void setDatas(QueryData datas) {
        this.datas = datas;
    }

    public static class QueryData {
        private List<LinkedHashMap<String, Object>> data;

        @XmlJavaTypeAdapter(MapAdapter.class)
        public List<LinkedHashMap<String, Object>> getData() {
            return data;
        }

        public void setData(List<LinkedHashMap<String, Object>> data) {
            this.data = data;
        }
    }

}
