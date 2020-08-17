package org.apache.atlas.util;

import io.zeta.metaspace.web.model.TemplateEnum;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class PoiExcelUtilsTest {

    @Test
    public void testCreateTemplate() throws AtlasBaseException, IOException, AtlasException {
        try (InputStream inputStream = PoiExcelUtils.getTemplateInputStream(TemplateEnum.CATEGORY_TEMPLATE)) {

        }
        try (InputStream inputStream = PoiExcelUtils.getTemplateInputStream(TemplateEnum.DATA_STANDARD_TEMPLATE)) {

        }
        try (InputStream inputStream = PoiExcelUtils.getTemplateInputStream(TemplateEnum.BUSINESS_TEMPLATE)) {

        }
    }
}
