package io.zeta.metaspace.web.service.indexmanager;

import io.zeta.metaspace.model.dto.indices.*;
import io.zeta.metaspace.web.service.Approve.Approvable;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.transaction.annotation.Transactional;

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
     * @param guid 指标域id
     * @param tenantId 租户id
     */
    @Transactional(rollbackFor = Exception.class)
    void deleteIndexByIndexFieldId(String guid, String tenantId);



    /**
     * 将指定指标域下的指标迁移到另一个指标域
     * @param sourceGuid 指标域id
     * @param tenantId 租户id
     * @param targetGuid 指标域id
     */
    @Transactional(rollbackFor = Exception.class)
    void removeIndexToAnotherIndexField(String sourceGuid, String tenantId, String targetGuid);

    /**
     * 获取指标域详情
     * @param categoryId 指标域id
     * @param tenantId 租户id
     * @param categoryType 目录类型
     * @return
     */
    IndexFieldDTO getIndexFieldInfo(String categoryId, String tenantId, int categoryType) throws SQLException;

    /**
     * 添加指标
     * @param indexDTO
     * @param tenantId
     * @return
     */
    IndexResposeDTO addIndex(IndexDTO indexDTO, String tenantId) throws Exception;

    /**
     * 编辑指标域
     * @param indexDTO
     * @param tenantId
     * @return
     */
    IndexResposeDTO editIndex(IndexDTO indexDTO, String tenantId) throws SQLException;

    /**
     * 删除指标
     * @param deleteList
     * @param tenantId
     */
    void deleteIndex(RequestDTO<DeleteIndexInfoDTO> deleteList, String tenantId);

    /**
     * 获取可选指标
     * @param indexType 指标类型
     * @param categoryType 目录类型
     * @param tenantId 租户id
     * @return
     */
    List<OptionalIndexDTO> getOptionalIndex(int indexType, int categoryType, String tenantId);

    /**
     * 获取可选数据源
     * @param tenantId 租户id
     * @return
     */
    List<OptionalDataSourceDTO> getOptionalDataSource(String tenantId);

    /**
     * 获取可选数据库
     * @param dataSourceId 数据源id
     * @param tenantId  租户id
     * @return
     */
    List<String> getOptionalDb(String dataSourceId, String tenantId);

    /**
     * 获取可选的表
     * @param dataSourceId 数据源id
     * @param dbName 数据库名称
     * @return
     */
    List<OptionalTableDTO> getOptionalTable(String dataSourceId, String dbName);

    /**
     * 获取表字段信息
     * @param tableId
     * @return
     */
    List<OptionalColumnDTO> getOptionalColumn(String tableId);

    /**
     * 获取指标详情
     * @param indexId
     * @param indexType
     * @param tenantId
     * @return
     */
    IndexInfoDTO getIndexInfo(String indexId, int indexType,int version, int categoryType, String tenantId);

    /**
     * 发布指标
     * @param dtoList
     * @param tenantId
     */
    void indexSendApprove(List<PublishIndexDTO> dtoList, String tenantId)  throws AtlasBaseException;

    /**
     * 发布历史
     * @param indexId 指标id
     * @param indexType 指标类型
     * @param categoryType 目录类型
     * @param tenantId 租户id
     * @return
     */
    List<IndexInfoDTO> publishHistory(String indexId, int indexType, int categoryType, String tenantId);

    /**
     * 指标分页查询
     * @param pageQueryDTO 查询条件
     * @param categoryType 目录类型
     * @param tenantId 租户id
     * @return
     */
    List<IndexInfoDTO> pageQuery(PageQueryDTO pageQueryDTO, int categoryType, String tenantId);
}
