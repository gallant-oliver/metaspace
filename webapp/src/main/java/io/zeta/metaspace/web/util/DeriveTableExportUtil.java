package io.zeta.metaspace.web.util;

import io.zeta.metaspace.model.sourceinfo.derivetable.vo.SourceInfoDeriveColumnDTO;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.SourceInfoDeriveColumnVO;
import io.zeta.metaspace.model.table.column.tag.ColumnTag;
import lombok.Data;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author huangrongwen
 * @Description: 衍生表导出工具类
 * @date 2022/1/615:35
 */
@Data
public class DeriveTableExportUtil {
    private final static String FILE_PATH = System.getProperty("atlas.home");
    private final static String DERIVE_TABLE_EXPORT_FILE_NAME = "deriveTableExport.xlsx";
    private final static String DERIVE_IMPORT_TEMPLATE = "deriveTableImport.xlsx";
    private final static String SPACE = File.separator + "conf" + File.separator;
    private final static String DERIVE_IMPORT_TEMPLATE_NAME = "衍生表登记模板.xlsx";

    public static String getDeriveImportTemplate() {
        return DERIVE_IMPORT_TEMPLATE_NAME;
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
    public static String deriveTableExcelPathName(String tableName) {
        return FILE_PATH + SPACE + "衍生表_" + tableName + ".xlsx";
    }

    /**
     * @return
     */
    public static String deriveTableExcelName(String tableName) {
        return "衍生表_" + tableName + ".xlsx";
    }
    public static List<SourceInfoDeriveColumnDTO> getPojo(List<SourceInfoDeriveColumnVO> sourceInfoDeriveColumnVOS) {
        List<SourceInfoDeriveColumnDTO> list = new ArrayList<>();
        for (SourceInfoDeriveColumnVO vo1 : sourceInfoDeriveColumnVOS) {
            SourceInfoDeriveColumnDTO sourceInfoDeriveColumnDTO = new SourceInfoDeriveColumnDTO();
            BeanUtils.copyProperties(vo1, sourceInfoDeriveColumnDTO);
            sourceInfoDeriveColumnDTO.setGroupField(vo1.isGroupField()? "是":"否");
            sourceInfoDeriveColumnDTO.setRemoveSensitive(vo1.isRemoveSensitive()? "是":"否");
            sourceInfoDeriveColumnDTO.setImportant(vo1.isImportant()? "是":"否");
            sourceInfoDeriveColumnDTO.setPermissionField(vo1.isPermissionField()? "是":"否");
            sourceInfoDeriveColumnDTO.setSecret(vo1.isSecret()? "是":"否");
            sourceInfoDeriveColumnDTO.setPrimaryKey(vo1.isPrimaryKey()? "是":"否");
            sourceInfoDeriveColumnDTO.setTags(vo1.getTags().stream().map(ColumnTag::getName).collect(Collectors.joining(",")));
            list.add(sourceInfoDeriveColumnDTO);
        }
        return list;
    }
}
