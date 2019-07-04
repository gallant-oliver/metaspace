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
 * @date 2019/6/27 11:37
 */
package io.zeta.metaspace.model.share;


import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;

/*
 * @description
 * @author sunhaoning
 * @date 2019/6/27 11:37
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlMap {

    @XmlElementRef(name = "xmlMap")
    private List<JAXBElement<Object>> elements = new ArrayList<>();
    private static ObjectFactory objectFactory = new ObjectFactory();

    public void put(String key, Object value) {
        JAXBElement<Object> ele = objectFactory.createXmlMap(key, value);
        this.elements.add(ele);
    }

}
