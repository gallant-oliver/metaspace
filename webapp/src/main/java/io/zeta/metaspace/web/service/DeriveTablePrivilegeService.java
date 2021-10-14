package io.zeta.metaspace.web.service;

import com.gridsum.gdp.library.commons.utils.UUIDUtils;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.enums.PrivilegeType;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.RDBMSColumn;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForList;
import io.zeta.metaspace.model.sourceinfo.derivetable.relation.CreateRequest;
import io.zeta.metaspace.model.sourceinfo.derivetable.relation.GroupDeriveTableInfo;
import io.zeta.metaspace.model.sourceinfo.derivetable.relation.GroupDeriveTableRelation;
import io.zeta.metaspace.model.table.column.tag.ColumnTag;
import io.zeta.metaspace.model.table.column.tag.ColumnTagRelation;
import io.zeta.metaspace.web.dao.ColumnTagDAO;
import io.zeta.metaspace.web.dao.GroupDeriveTableRelationDAO;
import io.zeta.metaspace.web.util.ParamUtil;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DeriveTablePrivilegeService {


    @Autowired
    private GroupDeriveTableRelationDAO relationDAO;

    @Transactional
    public Result createRelation(String tenantId, CreateRequest request) {
        List<GroupDeriveTableRelation> relationList = this.buildGroupDeriveTableRelation(tenantId,request);

        if (PrivilegeType.IMPORTANCE.equals(request.getPrivilegeType())){
            relationDAO.updateImportancePrivilegeNullByUserGroupId(request.getUserGroupId(),tenantId);
            relationDAO.updateDeriveTableImportancePrivilege(relationList);
        }else if(PrivilegeType.SECURITY.equals(request.getPrivilegeType())){
            relationDAO.updateSecurityPrivilegeNullByUserGroupId(request.getUserGroupId(),tenantId);
            relationDAO.updateDeriveTableSecurityPrivilege(relationList);
        }else{
            throw new AtlasBaseException("无法识别的权限的类型"+request.getPrivilegeType().name());
        }

        return ReturnUtil.success();
    }

    private List<GroupDeriveTableRelation> buildGroupDeriveTableRelation(String tenantId,CreateRequest request){
        List<GroupDeriveTableRelation> relations = new ArrayList<>();

        request.getTableList().forEach(tableId->{
            GroupDeriveTableRelation relation = new GroupDeriveTableRelation();
            relation.setGroupTableRelationId(UUIDUtils.uuid());
            relation.setDeriveTableId(tableId);
            relation.setUserGroupId(request.getUserGroupId());
            relation.setTenantId(tenantId);
            relations.add(relation);
        });
        return relations;
    }

    public Result deleteRelations(List<String> ids){
        relationDAO.deleteRelation(ids);
        return ReturnUtil.success();
    }
    public Result getDeriveTableRelations(String tenantId,PrivilegeType privilegeType,
                                          String userGroupId,Boolean registerType,
                                          String tableName,int limit,int offset){
        List<GroupDeriveTableInfo> groupDeriveTableInfos=relationDAO.getRelationInfos(tenantId,privilegeType,userGroupId,registerType,tableName,limit,offset);
        PageResult<GroupDeriveTableInfo> pageResult=new PageResult<>(groupDeriveTableInfos);
        if (Boolean.TRUE.equals(ParamUtil.isNull(groupDeriveTableInfos))){
            pageResult.setCurrentSize(0);
            pageResult.setTotalSize(0);
        }else{
            pageResult.setCurrentSize(groupDeriveTableInfos.size());
            pageResult.setTotalSize(groupDeriveTableInfos.get(0).getTotal());
        }
        return ReturnUtil.success(pageResult);
    }
}
