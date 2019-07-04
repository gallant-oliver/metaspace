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
 * @date 2019/6/27 12:02
 */
package io.zeta.metaspace.model.share;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/*
 * @description
 * @author sunhaoning
 * @date 2019/6/27 12:02
 */
@XmlRegistry
public class ObjectFactory {

    @XmlElementDecl(name = "xmlMap")
    public JAXBElement<Object> createXmlMap(String key, Object value) {
        QName name = new QName(key);
        JAXBElement<Object> ele = new JAXBElement<>(name, Object.class, value);
        return ele;
    }

}
