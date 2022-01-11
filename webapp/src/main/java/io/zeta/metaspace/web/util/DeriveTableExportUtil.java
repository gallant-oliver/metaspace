package io.zeta.metaspace.web.util;

import io.zeta.metaspace.model.sourceinfo.derivetable.vo.SourceInfoDeriveColumnDTO;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.SourceInfoDeriveColumnVO;
import lombok.Data;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * @author huangrongwen
 * @Description: 衍生表导出工具类
 * @date 2022/1/615:35
 */
@Data
public class DeriveTableExportUtil {
    private final static String FILE_PATH = System.getProperty("atlas.home");
    private final static String DERIVE_TABLE_EXPORT_FILE_NAME = "deriveTableExport.xlsx";
    private final static String DERIVE_IMPORT_TEMPLATE = "衍生表批量登记模板_.xlsx";
    private final static String SPACE = "\\conf\\";

    public static String getDeriveImportTemplate() {
        return DERIVE_IMPORT_TEMPLATE;
    }

    /**
     * 获取导出衍生表的模板名字
     * @return
     */
    public static String deriveTableTemplate() {
        return FILE_PATH + SPACE + DERIVE_TABLE_EXPORT_FILE_NAME;
    }

    /**
     * 获取衍生表导入模板
     * @return
     */
    public static File deriveTableImportTemplate() {
        File resource = new File(FILE_PATH + SPACE + DERIVE_IMPORT_TEMPLATE);
        if (!resource.exists()) {
            throw new AtlasBaseException("找不到导出模板表");
        }
        return resource;
    }

    /**
     * @return
     */
    public static File deriveTableExport(String tableName) {
        File file = new File(tableName);
        if (!file.exists()) {
            throw new AtlasBaseException("生成衍生表excel失败");
        }
        return file;
    }

    /**
     * @return
     */
    public static String deriveTableExcelName(String tableName) {
        return FILE_PATH + SPACE + "衍生表_" + tableName + ".xlsx";
    }

    public static List<SourceInfoDeriveColumnDTO> getPojo(List<SourceInfoDeriveColumnVO> sourceInfoDeriveColumnVOS) {
        List<SourceInfoDeriveColumnDTO> list = new ArrayList<>();
        for (SourceInfoDeriveColumnVO vo1 : sourceInfoDeriveColumnVOS) {
            SourceInfoDeriveColumnDTO sourceInfoDeriveColumnDTO = new SourceInfoDeriveColumnDTO();
            BeanUtils.copyProperties(vo1, sourceInfoDeriveColumnDTO);
            sourceInfoDeriveColumnDTO.setGroupField(getTrueOrFalse(vo1.isGroupField()));
            sourceInfoDeriveColumnDTO.setRemoveSensitive(getTrueOrFalse(vo1.isRemoveSensitive()));
            sourceInfoDeriveColumnDTO.setImportant(getTrueOrFalse(vo1.isImportant()));
            sourceInfoDeriveColumnDTO.setPermissionField(getTrueOrFalse(vo1.isPermissionField()));
            sourceInfoDeriveColumnDTO.setSecret(getTrueOrFalse(vo1.isSecret()));
            sourceInfoDeriveColumnDTO.setPrimaryKey(getTrueOrFalse(vo1.isPrimaryKey()));
            list.add(sourceInfoDeriveColumnDTO);
        }
        return list;
    }

    private static String getTrueOrFalse(boolean f) {
        if (f) {
            return "是";
        } else {
            return "否";
        }
    }
}
