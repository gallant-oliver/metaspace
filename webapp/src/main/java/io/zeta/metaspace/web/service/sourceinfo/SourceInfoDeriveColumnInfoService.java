package io.zeta.metaspace.web.service.sourceinfo;

import io.zeta.metaspace.web.dao.SourceInfoDeriveColumnInfoDAO;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveColumnInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 衍生表对应的字段 服务实现类
 * </p>
 *
 * @author Echo
 * @since 2021-07-13
 */
@Service
public class SourceInfoDeriveColumnInfoService {

    @Autowired
    private SourceInfoDeriveColumnInfoDAO sourceInfoDeriveColumnInfoDao;

    public List<SourceInfoDeriveColumnInfo> getDeriveColumnInfoListByTableId(String tableId) {
        return sourceInfoDeriveColumnInfoDao.getDeriveColumnInfoListByTableId(tableId);
    }

    /**
     * 根据tableGuid查询一张表所有的历史版本的列
     *
     * @param tableGuid
     * @return
     */
    public List<SourceInfoDeriveColumnInfo> getDeriveColumnInfoListByTableGuid(String tableGuid) {
        return sourceInfoDeriveColumnInfoDao.getDeriveColumnInfoListByTableGuid(tableGuid);
    }

    public boolean deleteDeriveColumnInfoByTableId(String tableId, String tableGuid) {
        int i = sourceInfoDeriveColumnInfoDao.deleteDeriveColumnInfoByTableId(tableId, tableGuid);
        return true;
    }

    public boolean deleteByTableGuids(List<String> tableGuids) {
        int i = sourceInfoDeriveColumnInfoDao.deleteByTableGuids(tableGuids);
        return true;
    }

    public boolean saveBatch(List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos) {
        int sum = sourceInfoDeriveColumnInfos.stream().mapToInt(sourceInfoDeriveColumnInfoDao::add).sum();
        return sum == sourceInfoDeriveColumnInfos.size();
    }

    public boolean saveOrUpdateBatch(List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos) {
        int sum = sourceInfoDeriveColumnInfos.stream().mapToInt(sourceInfoDeriveColumnInfoDao::upsert).sum();
        return sum == sourceInfoDeriveColumnInfos.size();
    }
}
