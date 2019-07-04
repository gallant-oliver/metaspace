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
 * @date 2019/6/27 11:39
 */
package io.zeta.metaspace.model.share;

import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/*
 * @description
 * @author sunhaoning
 * @date 2019/6/27 11:39
 */
public class MapAdapter extends XmlAdapter<XmlMap, Map<String, Object>> {

    @Override
    public Map<String, Object> unmarshal(XmlMap v) throws Exception {
        return null;
    }

    @Override
    public XmlMap marshal(Map<String, Object> v) throws Exception {
        if (v != null) {
            XmlMap xmlMap = new XmlMap();
            for (Map.Entry<String, Object> entry : v.entrySet()) {
                xmlMap.put(entry.getKey(), entry.getValue());
            }
            return xmlMap;
        }
        return null;
    }

}