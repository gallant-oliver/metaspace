package io.zeta.metaspace.web.service.indexmanager;

import io.zeta.metaspace.model.dto.indices.*;
import io.zeta.metaspace.model.po.indices.IndexInfoPO;
import io.zeta.metaspace.web.service.Approve.Approvable;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * @author T480
 * @version 1.0
 * @InterfaceName IndexService
 * @Description TODO
 * @date 2021/3/16 19:44
 **/
public interface IndexService extends Approvable {
    /**
     * 删除指定指标域下的指标
     *
     * @param guids    指标域id
     * @param tenantId 租户id
     */
    @Transactional(rollbackFor = Exception.class)
    void deleteIndexByIndexFieldId(List<String> guids, String tenantId);


    /**
     * 将指定指标域下的指标迁移到另一个指标域
     *
     * @param sourceGuids 指标域id
     * @param tenantId    租户id
     * @param targetGuid  指标域id
     */
    @Transactional(rollbackFor = Exception.class)
    void removeIndexToAnotherIndexField(List<String> sourceGuids, String tenantId, String targetGuid);

    /**
     * 获取指标域详情
     *
     * @param categoryId   指标域id
     * @param tenantId     租户id
     * @param categoryType 目录类型
     * @return
     */
    IndexFieldDTO getIndexFieldInfo(String categoryId, String tenantId, int categoryType) throws SQLException;

    /**
     * 添加指标
     *
     * @param indexDTO
     * @param tenantId
     * @return
     */
    IndexResposeDTO addIndex(IndexDTO indexDTO, String tenantId) throws Exception;

    /**
     * 编辑指标域
     *
     * @param indexDTO
     * @param tenantId
     * @return
     */
    IndexResposeDTO editIndex(IndexDTO indexDTO, String tenantId);

    /**
     * 删除指标
     *
     * @param deleteList
     * @param tenantId
     */
    List<IndexInfoPO> deleteIndex(List<DeleteIndexInfoDTO> deleteList, String tenantId);

    /**
     * 获取可选指标
     *
     * @param indexType    指标类型
     * @param categoryType 目录类型
     * @param tenantId     租户id
     * @return
     */
    List<OptionalIndexDTO> getOptionalIndex(int indexType, int categoryType, String tenantId);

    /**
     * 获取可选数据源
     *
     * @param tenantId 租户id
     * @return
     */
    List<OptionalDataSourceDTO> getOptionalDataSource(String tenantId);

    /**
     * 获取可选数据库
     *
     * @param dataSourceId 数据源id
     * @param tenantId     租户id
     * @return
     */
    List<String> getOptionalDb(String dataSourceId, String tenantId);

    /**
     * 获取可选的表
     *
     * @param dataSourceId 数据源id
     * @param dbName       数据库名称
     * @return
     */
    List<OptionalTableDTO> getOptionalTable(String dataSourceId, String dbName);

    /**
     * 获取表字段信息
     *
     * @param tableId
     * @return
     */
    List<OptionalColumnDTO> getOptionalColumn(String tableId);

    /**
     * 获取指标详情
     *
     * @param indexId
     * @param indexType
     * @param tenantId
     * @return
     */
    IndexInfoDTO getIndexInfo(String indexId, int indexType, int version, int categoryType, String tenantId);

    /**
     * 发布指标
     *
     * @param dtoList
     * @param tenantId
     */
    void indexSendApprove(List<PublishIndexDTO> dtoList, String tenantId) throws AtlasBaseException;

    /**
     * 发布历史
     *
     * @param indexId      指标id
     * @param pageQueryDTO 分页查询参数
     * @param categoryType 目录类型
     * @param tenantId     租户id
     * @return
     */
    List<IndexInfoDTO> publishHistory(String indexId, PageQueryDTO pageQueryDTO, int categoryType, String tenantId);

    /**
     * 指标分页查询
     *
     * @param pageQueryDTO 查询条件
     * @param categoryType 目录类型
     * @param tenantId     租户id
     * @return
     */
    List<IndexInfoDTO> pageQuery(PageQueryDTO pageQueryDTO, int categoryType, String tenantId) throws Exception;

    List<String> getIndexIds(List<String> indexFields, String tenantId, int state1, int state2);

    /**
     * 获取指标链路
     */
    @Transactional(rollbackFor = Exception.class)
    IndexLinkDto getIndexLink(String indexId, int indexType, String version, String tenantId);

    /**
     * 原子指标
     *
     * @param tenantId
     * @return
     */
    XSSFWorkbook downLoadExcelAtom(String tenantId);

    /**
     * 派生指标
     *
     * @param tenantId
     * @return
     */
    XSSFWorkbook downLoadExcelDerive(String tenantId);

    /**
     * 下载复合指标excel模板
     *
     * @param tenantId
     * @return
     */
    XSSFWorkbook downLoadExcelComposite(String tenantId);

    /**
     * 上传原子指标excel
     *
     * @param tenantId
     * @param file
     */
    String uploadExcelAtom(String tenantId, File file) throws Exception;

    /**
     * 上传派生指标excel
     *
     * @param tenantId
     * @param file
     * @return
     * @throws Exception
     */
    String uploadExcelDerive(String tenantId, File file) throws Exception;

    /**
     * 导入原子指标模板数据
     *
     * @param file
     * @param tenantId
     * @throws Exception
     */
    void importBatchAtomIndex(File file, String tenantId) throws Exception;

    /**
     * 导入派生指标模板数据
     *
     * @param file
     * @param tenantId
     * @throws Exception
     */
    void importBatchDeriveIndex(File file, String tenantId) throws Exception;

    /**
     * 上传复合指标excel
     *
     * @param file
     * @return
     * @throws Exception
     */
    String uploadExcelComposite(String tenantId, File file) throws Exception;

    /**
     * 导入复合指标模板数据
     * @param file
     * @param tenantId
     * @throws Exception
     */
    void importBatchCompositeIndex(File file, String tenantId) throws Exception;

}
