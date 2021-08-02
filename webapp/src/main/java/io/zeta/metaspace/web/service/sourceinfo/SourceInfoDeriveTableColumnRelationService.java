package io.zeta.metaspace.web.service.sourceinfo;

import io.zeta.metaspace.web.dao.SourceInfoDeriveTableColumnRelationDAO;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableColumnRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 衍生表和字段的关联关系 服务实现类
 * </p>
 *
 * @author Echo
 * @since 2021-07-13
 */
@Service
public class SourceInfoDeriveTableColumnRelationService {

    @Autowired
    private SourceInfoDeriveTableColumnRelationDAO sourceInfoDeriveTableColumnRelationDao;

    public boolean deleteDeriveTableColumnRelationByTableId(String tableId) {
        int i = sourceInfoDeriveTableColumnRelationDao.deleteDeriveTableColumnRelationByTableId(tableId);
        return true;
    }

    public boolean deleteByTableGuids(List<String> tableGuids) {
        sourceInfoDeriveTableColumnRelationDao.deleteByTableGuids(tableGuids);
        return true;
    }

    public boolean saveBatch(List<SourceInfoDeriveTableColumnRelation> sourceInfoDeriveTableColumnRelationList) {
        int sum = sourceInfoDeriveTableColumnRelationList.stream().mapToInt(sourceInfoDeriveTableColumnRelationDao::add).sum();
        return sum == sourceInfoDeriveTableColumnRelationList.size();
    }

    public boolean saveOrUpdateBatch(List<SourceInfoDeriveTableColumnRelation> sourceInfoDeriveTableColumnRelationList) {
        int sum = sourceInfoDeriveTableColumnRelationList.stream().mapToInt(sourceInfoDeriveTableColumnRelationDao::upsert).sum();
        return sum == sourceInfoDeriveTableColumnRelationList.size();
    }
}
