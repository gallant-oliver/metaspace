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
 * @date 2019/2/12 14:56
 */
package io.zeta.metaspace.web.service;

import com.google.gson.Gson;
import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.business.BusinessQueryParameter;
import io.zeta.metaspace.model.business.BusinessRelationEntity;
import io.zeta.metaspace.model.business.BusinessTableList;
import io.zeta.metaspace.model.business.ColumnCheckMessage;
import io.zeta.metaspace.model.business.ColumnPrivilege;
import io.zeta.metaspace.model.business.ColumnPrivilegeRelation;
import io.zeta.metaspace.model.business.TechnicalStatus;
import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.ColumnQuery;
import io.zeta.metaspace.model.metadata.DataOwnerHeader;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.TableHeader;
import io.zeta.metaspace.model.privilege.SystemModule;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.utils.MetaspaceGremlin3QueryProvider;
import io.zeta.metaspace.utils.MetaspaceGremlinQueryProvider;
import io.zeta.metaspace.web.dao.BusinessDAO;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.ColumnPrivilegeDAO;
import io.zeta.metaspace.web.dao.DataShareDAO;
import io.zeta.metaspace.web.dao.PrivilegeDAO;
import io.zeta.metaspace.web.dao.RoleDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import io.zeta.metaspace.web.util.DateUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.RequestContext;
import org.apache.atlas.exception.AtlasBaseException;

import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.repository.graphdb.AtlasGraph;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.repository.store.graph.v1.DeleteHandlerV1;
import org.apache.atlas.repository.store.graph.v2.AtlasEntityChangeNotifier;
import org.apache.atlas.repository.store.graph.v2.AtlasEntityStoreV2;
import org.apache.atlas.repository.store.graph.v2.AtlasTypeDefGraphStoreV2;
import org.apache.atlas.repository.store.graph.v2.EntityGraphMapper;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.postgresql.util.PGobject;

import static io.zeta.metaspace.web.util.PoiExcelUtils.XLSX;
import static org.mockito.Mockito.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;


import javax.inject.Inject;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/12 14:56
 */
@Service
public class BusinessService {
    private static final Logger LOG = LoggerFactory.getLogger(BusinessService.class);
    @Autowired
    BusinessDAO businessDao;
    @Autowired
    CategoryDAO categoryDao;
    @Autowired
    PrivilegeDAO privilegeDao;
    @Autowired
    RoleDAO roleDao;
    @Autowired
    RoleService roleService;
    @Autowired
    ColumnPrivilegeDAO columnPrivilegeDAO;
    @Autowired
    MetaDataService metaDataService;
    @Autowired
    DataShareDAO shareDAO;
    @Autowired
    DataShareService shareService;

    @Inject
    AtlasTypeRegistry typeRegistry;

    @Inject
    AtlasEntityStore entityStore;
    @Inject
    DeleteHandlerV1 deleteHandler;
    @Inject
    private EntityGraphMapper graphMapper;
    private AtlasEntityChangeNotifier mockChangeNotifier = mock(AtlasEntityChangeNotifier.class);

    @Inject
    protected AtlasGraph graph;

    @Inject
    protected AtlasTypeDefGraphStoreV2 typeDefStore;


    private MetaspaceGremlinQueryProvider gremlinQueryProvider = MetaspaceGremlinQueryProvider.INSTANCE;

    private static final int FINISHED_STATUS = 1;
    private static final int BUSINESS_TYPE = 1;

    @Transactional
    public int addBusiness(String categoryId, BusinessInfo info) throws AtlasBaseException {
        try {
            int count = businessDao.sameNameCount(info.getName());
            if(count > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同的业务对象名称");
            }
            //departmentId(categoryId)
            info.setDepartmentId(categoryId);
            //submitter && businessOperator
            String userName = AdminUtils.getUserData().getUsername();
            info.setSubmitter(userName);
            info.setBusinessOperator(userName);
            //businessId
            String businessId = UUID.randomUUID().toString();
            info.setBusinessId(businessId);
            //submissionTime && businessLastUpdate && ticketNumber
            long timestamp = System.currentTimeMillis();
            String time = DateUtils.getNow();
            info.setSubmissionTime(time);
            info.setBusinessLastUpdate(time);
            info.setTicketNumber(String.valueOf(timestamp));
            //level2CategoryId
            String pathStr = categoryDao.queryGuidPathByGuid(categoryId);
            String path = pathStr.substring(1, pathStr.length()-1);
            path = path.replace("\"", "");
            String[] pathArr = path.split(",");
            String level2CategoryId = "";
            if(pathArr.length >= 2) {
                level2CategoryId = pathArr[1];
            }
            info.setLevel2CategoryId(level2CategoryId);
            int insertFlag =  businessDao.insertBusinessInfo(info);

            //更新business编辑状态
            businessDao.updateBusinessStatus(businessId, FINISHED_STATUS);
            //更新technical编辑状态
            businessDao.updateTechnicalStatus(businessId, TechnicalStatus.BLANK.code);

            BusinessRelationEntity entity = new BusinessRelationEntity();
            //relationshiGuid
            String relationGuid = UUID.randomUUID().toString();
            entity.setRelationshipGuid(relationGuid);

            entity.setBusinessId(businessId);
            entity.setCategoryGuid(categoryId);
            entity.setGenerateTime(time);
            int relationFlag = businessDao.addRelation(entity);
            return insertFlag & relationFlag;
        } catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加失败");
        }
    }

    public int updateBusiness(String businessId, BusinessInfo info) throws AtlasBaseException {
        try {
            BusinessInfo currentInfo = businessDao.queryBusinessByBusinessId(businessId);
            int count = businessDao.sameNameCount(info.getName());

            if(count > 0 && !currentInfo.getName().equals(info.getName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同的业务对象名称");
            }
            String userName = AdminUtils.getUserData().getUsername();
            long timestamp = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(timestamp);
            info.setBusinessOperator(userName);
            info.setBusinessLastUpdate(time);
            info.setBusinessId(businessId);
            return businessDao.updateBusinessInfo(info);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修改失败");
        }
    }

    public BusinessInfo getBusinessInfo(String businessId) throws AtlasBaseException {
        try {
            BusinessInfo info = businessDao.queryBusinessByBusinessId(businessId);
            User user = AdminUtils.getUserData();
            Role role = roleDao.getRoleByUsersId(user.getUserId());
            if(role.getStatus() == 0)
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
            String userId = user.getUserId();
            boolean editBusiness = privilegeDao.queryModulePrivilegeByUser(userId, SystemModule.BUSINESSE_OPERATE.getCode()) == 0 ? false:true;
            info.setEditBusiness(editBusiness);
            String categoryGuid = info.getDepartmentId();
            String departmentName = categoryDao.queryNameByGuid(categoryGuid);
            info.setDepartmentName(departmentName);
            return info;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    public TechnologyInfo getRelatedTableList(String businessId) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            Role role = roleDao.getRoleByUsersId(user.getUserId());
            if(role.getStatus() == 0)
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
            String userId = user.getUserId();

            //technicalLastUpdate && technicalOperator
            TechnologyInfo info = businessDao.queryTechnologyInfoByBusinessId(businessId);
            //editTechnical
            if(Objects.isNull(info))
                info = new TechnologyInfo();
            boolean editTechnical = privilegeDao.queryModulePrivilegeByUser(userId, SystemModule.TECHNICAL_OPERATE.getCode()) == 0 ? false : true;
            info.setEditTechnical(editTechnical);

            //tables
            List<TechnologyInfo.Table> tables = businessDao.queryTablesByBusinessId(businessId);

            for(int i=0; i<tables.size(); i++) {
                String tableGuid = tables.get(i).getTableGuid();
                AtlasEntity.AtlasEntityWithExtInfo entityWithExtInfo = entityStore.getById(tableGuid);
                if(Objects.isNull(entityWithExtInfo)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未查询到表详情");
                }
                AtlasEntity entity = entityWithExtInfo.getEntity();
                if (entity.hasAttribute("displayChineseText") && Objects.nonNull(entity.getAttribute("displayChineseText"))) {
                    String displayName = entity.getAttribute("displayChineseText").toString();
                    tables.get(i).setDisplayName(displayName);
                }
            }


            String trustTableGuid = businessDao.getTrustTableGuid(businessId);
            if(Objects.nonNull(trustTableGuid)) {
                TechnologyInfo.Table trustTable = tables.stream().filter(table -> table.getTableGuid().equals(trustTableGuid)).findFirst().get();
                if(Objects.nonNull(trustTable)) {
                    tables.remove(trustTable);
                    trustTable.setTrust(true);
                    tables.add(0, trustTable);
                } else {
                    tables.stream().findFirst().get().setTrust(true);
                }
            }
            info.setTables(tables);
            //businessId
            info.setBusinessId(businessId);
            return info;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    public PageResult<BusinessInfoHeader> getBusinessListByCategoryId(String categoryId, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult<BusinessInfoHeader> pageResult = new PageResult<>();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<BusinessInfoHeader>  list = businessDao.queryBusinessByCatetoryId(categoryId, limit, offset);
            String path = CategoryRelationUtils.getPath(categoryId);
            StringJoiner joiner = null;
            String[] pathArr = path.split("/");
            String level2Category = "";
            if(pathArr.length >= 2)
                level2Category = pathArr[1];
            for(BusinessInfoHeader infoHeader : list) {
                joiner = new StringJoiner(".");
                //path
                //joiner.add(path).add(infoHeader.getName());
                joiner.add(path);
                infoHeader.setPath(joiner.toString());
                //level2Category
                infoHeader.setLevel2Category(level2Category);
            }
            long sum = businessDao.queryBusinessCountByByCatetoryId(categoryId);
            pageResult.setOffset(offset);
            pageResult.setSum(sum);
            pageResult.setCount(list.size());
            pageResult.setLists(list);
            return pageResult;
        } catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    public PageResult<BusinessInfoHeader> getBusinessListByName(Parameters parameters) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            Role role = roleDao.getRoleByUsersId(user.getUserId());
            if(role.getStatus() == 0)
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
            String roleId = role.getRoleId();
            PageResult<BusinessInfoHeader> pageResult = new PageResult<>();
            String businessName = parameters.getQuery();
            businessName = (businessName == null ? "":businessName);
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            List<BusinessInfoHeader> businessInfoList = null;
            List<String> categoryIds = CategoryRelationUtils.getPermissionCategoryList(roleId, BUSINESS_TYPE);
            if(Objects.nonNull(businessName))
                businessName = businessName.replaceAll("%", "/%").replaceAll("_", "/_");
            businessInfoList = businessDao.queryBusinessByName(businessName, categoryIds, limit, offset);

            for(BusinessInfoHeader infoHeader : businessInfoList) {
                String path = CategoryRelationUtils.getPath(infoHeader.getCategoryGuid());
                StringJoiner joiner = new StringJoiner(".");
                //joiner.add(path).add(infoHeader.getName());
                joiner.add(path);
                infoHeader.setPath(joiner.toString());
                String[] pathArr = path.split("/");
                String level2Category = "";
                if(pathArr.length >= 2)
                    level2Category = pathArr[1];
                infoHeader.setLevel2Category(level2Category);
            }
            long businessCount = businessDao.queryBusinessCountByName(businessName, categoryIds);
            pageResult.setOffset(offset);
            pageResult.setSum(businessCount);
            pageResult.setLists(businessInfoList);
            pageResult.setCount(businessInfoList.size());
            return pageResult;
        } catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
          throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    public PageResult<BusinessInfoHeader> getBusinessListByCondition(BusinessQueryParameter parameter) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            Role role = roleDao.getRoleByUsersId(user.getUserId());
            if(role.getStatus() == 0)
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
            String roleId = role.getRoleId();
            PageResult<BusinessInfoHeader> pageResult = new PageResult<>();
            String status = parameter.getStatus();
            String ticketNumber = parameter.getTicketNumber();
            ticketNumber = (ticketNumber==null ? "":ticketNumber);
            String businessName = parameter.getName();
            businessName = (businessName==null ? "":businessName);
            String level2CategoryId = parameter.getLevel2CategoryId();
            String  submitter = parameter.getSubmitter();
            submitter = (submitter==null ? "":submitter);
            int limit = parameter.getLimit();
            int offset = parameter.getOffset();
            Integer technicalStatus = TechnicalStatus.getCodeByDesc(status);
            List<String> categoryIds = CategoryRelationUtils.getPermissionCategoryList(roleId, BUSINESS_TYPE);
            if(Objects.nonNull(categoryIds) && categoryIds.size() > 0) {
                if(Objects.nonNull(businessName))
                    businessName = businessName.replaceAll("%", "/%").replaceAll("_", "/_");
                if(Objects.nonNull(ticketNumber))
                    ticketNumber = ticketNumber.replaceAll("%", "/%").replaceAll("_", "/_");
                if(Objects.nonNull(submitter))
                    submitter = submitter.replaceAll("%", "/%").replaceAll("_", "/_");
                List<BusinessInfoHeader> businessInfoList = businessDao.queryBusinessByCondition(categoryIds, technicalStatus, ticketNumber, businessName, level2CategoryId, submitter, limit, offset);
                for (BusinessInfoHeader infoHeader : businessInfoList) {
                    String categoryId = businessDao.queryCategoryIdByBusinessId(infoHeader.getBusinessId());
                    String path = CategoryRelationUtils.getPath(categoryId);
                    infoHeader.setPath(path + "." + infoHeader.getName());
                    String[] pathArr = path.split("/");
                    if (pathArr.length >= 2)
                        infoHeader.setLevel2Category(pathArr[1]);
                }
                pageResult.setOffset(offset);
                pageResult.setLists(businessInfoList);
                long businessCount = businessDao.queryBusinessCountByCondition(categoryIds, technicalStatus, ticketNumber, businessName, level2CategoryId, submitter);
                pageResult.setSum(businessCount);
                pageResult.setCount(businessInfoList.size());
            }
            return pageResult;
        } catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    @Transactional
    public void addBusinessAndTableRelation(String businessId, BusinessTableList tableIdList) throws AtlasBaseException {
        List<String> list = tableIdList.getList();
        String trustTable = tableIdList.getTrust();
        try {
            String userName = AdminUtils.getUserData().getUsername();
            long timestamp = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(timestamp);
            businessDao.updateTechnicalInfo(businessId, userName, time);
            //更新technical编辑状态
            if(Objects.nonNull(list) && list.size() > 0) {
                businessDao.updateTechnicalStatus(businessId, TechnicalStatus.ADDED.code);
            } else {
                businessDao.updateTechnicalStatus(businessId, TechnicalStatus.BLANK.code);
            }
            businessDao.deleteRelationByBusinessId(businessId);

            businessDao.updateTrustTable(businessId);

            if(Objects.nonNull(list) && list.size()>0) {
                businessDao.insertTableRelation(businessId, list);
                if(Objects.isNull(trustTable)) {
                    trustTable = list.get(0);
                }
            }
            if(Objects.nonNull(trustTable)) {
                businessDao.setBusinessTrustTable(businessId, trustTable);
            }

        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    @Transactional
    public void deleteBusiness(String businessId) throws AtlasBaseException {
        try {
            businessDao.deleteBusinessById(businessId);
            businessDao.deleteRelationByBusinessId(businessId);
            businessDao.deleteRelationById(businessId);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库异常");
        }
    }


    public int addColumnPrivilege(ColumnPrivilege privilege) throws AtlasBaseException {
        try {
            String columnName = privilege.getName();
            int count = columnPrivilegeDAO.queryNameCount(columnName);
            if(count > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同权限字段");
            }
            return columnPrivilegeDAO.addColumnPrivilege(privilege);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加失败");
        }
    }

    public int deleteColumnPrivilege(Integer guid) throws AtlasBaseException {
        try {
            return columnPrivilegeDAO.deleteColumnPrivilege(guid);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除失败");
        }
    }

    public int updateColumnPrivilege(ColumnPrivilege privilege) throws AtlasBaseException {
        try {
            return columnPrivilegeDAO.updateColumnPrivilege(privilege);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新失败");
        }
    }

    public List<ColumnPrivilege> getColumnPrivilegeList() throws AtlasBaseException {
        try {
            return columnPrivilegeDAO.getColumnPrivilegeList();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新失败");
        }
    }

    public List<String> getColumnPrivilegeValue(Integer guid) throws AtlasBaseException {
        try {
            Gson gson = new Gson();
            Object columnObject = columnPrivilegeDAO.queryColumnPrivilege(guid);
            PGobject pGobject = (PGobject)columnObject;
            String value = pGobject.getValue();
            List<String> values = gson.fromJson(value, List.class);
            return values;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败失败");
        }
    }

    @Transactional
    public int addColumnPrivilegeRelation(ColumnPrivilegeRelation relation) throws AtlasBaseException {
        try {
            int columnPrivilegeGuid = relation.getColumnPrivilegeGuid();
            ColumnPrivilegeRelation columnRelation = columnPrivilegeDAO.queryPrivilegeRelation(columnPrivilegeGuid);
            if(Objects.nonNull(columnRelation)) {
                return columnPrivilegeDAO.updateColumnPrivilegeRelation(relation);
            } else {
                return columnPrivilegeDAO.addColumnPrivilegeRelation(relation);
            }
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加失败");
        }
    }

    public ColumnPrivilegeRelation queryRelatedColumn(int columnGuid) throws AtlasBaseException {
        try {
            return columnPrivilegeDAO.queryPrivilegeRelation(columnGuid);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加失败");
        }
    }

    public Table getTableInfoById(String guid) throws AtlasBaseException {
        Table table =  metaDataService.getTableInfoById(guid);
        /*List<Column> columns = table.getColumns();
        for(Column column : columns) {
            String columnId = column.getColumnId();
            ColumnPrivilegeRelation relation = columnPrivilegeDAO.queryPrivilegeRelationByColumnGuid(columnId);
            if(Objects.nonNull(relation)) {
                column.setColumnPrivilege(relation.getName());
                column.setColumnPrivilegeGuid(relation.getColumnPrivilegeGuid());
            }
        }*/
        return table;
    }

    public PageResult<APIInfoHeader> getBusinessTableRelatedAPI(String businessGuid, Parameters parameters) throws AtlasBaseException {
        try {
            TechnologyInfo technologyInfo = getRelatedTableList(businessGuid);
            List<TechnologyInfo.Table> tableHeaderList = technologyInfo.getTables();
            List<String> tableList = new ArrayList<>();
            tableHeaderList.stream().forEach(table -> tableList.add(table.getTableGuid()));
            Integer limit = parameters.getLimit();
            Integer offset = parameters.getOffset();
            List<APIInfoHeader> APIList = new ArrayList<>();
            PageResult<APIInfoHeader> pageResult = new PageResult<>();
            int apiCount = 0;
            if(Objects.nonNull(tableList) && tableList.size()>0) {
                APIList = shareDAO.getTableRelatedAPI(tableList, limit, offset);
                for (APIInfoHeader api : APIList) {
                    List<DataOwnerHeader> dataOwner = metaDataService.getDataOwner(api.getTableGuid());
                    List<String> dataOwnerName = new ArrayList<>();
                    if(Objects.nonNull(dataOwner) && dataOwner.size()>0) {
                        dataOwner.stream().forEach(owner -> dataOwnerName.add(owner.getName()));
                    }
                    api.setDataOwner(dataOwnerName);
                }
                apiCount = shareDAO.countTableRelatedAPI(tableList);
            }
            pageResult.setOffset(offset);
            pageResult.setSum(apiCount);
            pageResult.setLists(APIList);
            pageResult.setCount(APIList.size());
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    @Transactional
    public void updateBusinessTrustTable() {
        List<String> nonTrustBusinessList = businessDao.getNonTrustBusiness();
        for(String businessId : nonTrustBusinessList) {
            List<TechnologyInfo.Table> tableList = businessDao.queryTablesByBusinessId(businessId);
            if(Objects.nonNull(tableList) && tableList.size()>0) {
                String tableGuid = tableList.get(0).getTableGuid();
                businessDao.setBusinessTrustTable(businessId, tableGuid);
            }
        }
    }

    public PageResult getPermissionBusinessRelatedTableList(String businessId, Parameters parameters) throws AtlasBaseException {
        try {
            /*Parameters businessParam = new Parameters();
            businessParam.setQuery("");
            businessParam.setOffset(0);
            businessParam.setLimit(-1);
            PageResult businessInfo = getBusinessListByName(businessParam);
            List<BusinessInfoHeader> businessInfoHeaderList = businessInfo.getLists();
            List<String> businessList = new ArrayList<>();
            businessInfoHeaderList.stream().forEach(info -> businessList.add(info.getBusinessId()));*/

            String tableName = parameters.getQuery();
            tableName = (tableName == null ? "":tableName);

            if(Objects.nonNull(tableName))
                tableName = tableName.replaceAll("%", "/%").replaceAll("_", "/_");

            Integer limit = parameters.getLimit();
            Integer offset = parameters.getOffset();
            List<TableHeader> tableHeaderList = businessDao.getBusinessRelatedTableList(businessId, tableName, limit, offset);
            for(int i=0; i<tableHeaderList.size(); i++) {
                String tableGuid = tableHeaderList.get(i).getTableId();
                AtlasEntity.AtlasEntityWithExtInfo entityWithExtInfo = entityStore.getById(tableGuid);
                if(Objects.isNull(entityWithExtInfo)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未查询到表详情");
                }
                AtlasEntity entity = entityWithExtInfo.getEntity();
                if (entity.hasAttribute("displayChineseText") && Objects.nonNull(entity.getAttribute("displayChineseText"))) {
                    String displayName = entity.getAttribute("displayChineseText").toString();
                    tableHeaderList.get(i).setDisplayName(displayName);
                }
            }

            long count = businessDao.getCountBusinessRelatedTable(businessId, tableName);
            PageResult pageResult = new PageResult();
            pageResult.setLists(tableHeaderList);
            pageResult.setCount(tableHeaderList.size());
            pageResult.setSum(count);
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

    public PageResult getTableColumnList(String tableGuid, Parameters parameters) throws AtlasBaseException {
        try {
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            ColumnQuery columnQuery = new ColumnQuery();
            columnQuery.setGuid(tableGuid);
            List<Column> columnList = metaDataService.getColumnInfoById(columnQuery, true);
            PageResult pageResult = new PageResult();
            int total = columnList.size();
            int start = 0;
            int end = total;
            if(offset < total) {
                start = offset;
            } else {
                return pageResult;
            }
            if((start+limit) < total) {
                end = start + limit;
            } else {
                end = total;
            }
            columnList.sort(Comparator.comparing(Column::getColumnName).thenComparing(Column::getColumnName));
            List<Column> limitList = columnList.subList(start, end);
            pageResult.setLists(limitList);
            pageResult.setCount(limitList.size());
            pageResult.setSum(total);
            return pageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }

    public void editTableColumnDisplayName(List<Column> columns) throws AtlasBaseException {
        try {
            entityStore = new AtlasEntityStoreV2(deleteHandler, typeRegistry, mockChangeNotifier, graphMapper);
            for(Column column : columns) {
                String columnGuid = column.getColumnId();
                String displayText = column.getDisplayName();
                if(Objects.isNull(columnGuid) || Objects.isNull(displayText)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "字段Id或别名不能为空");
                }
                try {
                    entityStore.updateEntityAttributeByGuid(columnGuid, "displayChineseText", displayText);
                    graph.commit();
                } catch (AtlasBaseException ex) {
                    throw ex;
                }
            }
            RequestContext.clear();
        }catch (AtlasBaseException e) {
            throw e;
        }
    }

    public void editTableDisplayName(TableHeader tableHeader) throws AtlasBaseException {
        try {
            entityStore = new AtlasEntityStoreV2(deleteHandler, typeRegistry, mockChangeNotifier, graphMapper);
            String tableGuid = tableHeader.getTableId();
            String displayText = tableHeader.getDisplayName();
            entityStore.updateEntityAttributeByGuid(tableGuid, "displayChineseText", displayText);
            graph.commit();
            RequestContext.clear();
        }catch (AtlasBaseException e) {
            throw e;
        }
    }


    /*public List<ColumnCheckMessage> checkColumnName(String tableGuid, List<String> columnList) throws AtlasBaseException {
        try {
            AtlasEntity.AtlasEntityWithExtInfo entityWithExtInfo = entityStore.getById(tableGuid);
            if(Objects.isNull(entityWithExtInfo)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未查询到表字段信息");
            }
            List<Column> columnInfoList = metaDataService.extractColumnInfo(entityWithExtInfo, tableGuid);
            List<String> tableColumnList = new ArrayList<>();
            List<ColumnCheckMessage> columnCheckMessageList = new ArrayList<>();
            columnInfoList.stream().forEach(column -> tableColumnList.add(column.getColumnName()));
            for(int i=0; i<columnList.size(); i++) {
                String columnName = columnList.get(i);
                ColumnCheckMessage columnCheckMessage = new ColumnCheckMessage();
                columnCheckMessage.setRow(i);
                columnCheckMessage.setColumnName(columnName);
                if(tableColumnList.contains(columnName)) {
                    columnCheckMessage.setErrorMessage("success");
                } else {
                    columnCheckMessage.setErrorMessage("not find column name");
                }
                columnCheckMessageList.add(columnCheckMessage);
            }
            return columnCheckMessageList;
        }catch (AtlasBaseException e) {
            throw e;
        }
    }*/

    public ColumnCheckMessage checkColumnName(String tableGuid, List<String> columnList, List<Column> columnwithDisplayList) throws AtlasBaseException {
        try {
            ColumnCheckMessage columnCheckMessage = new ColumnCheckMessage();
            int errorColumnCount = 0;
            List<String> errorColumnList = new ArrayList<>();
            List<ColumnCheckMessage.ColumnCheckInfo> columnCheckMessageList = new ArrayList<>();
            List<String> recordColumnList = new ArrayList<>();
            ColumnCheckMessage.ColumnCheckInfo columnCheckInfo = null;
            int index = 0;
            for(Column column : columnwithDisplayList) {
                String columnName = column.getColumnName();
                columnCheckInfo = new ColumnCheckMessage.ColumnCheckInfo();
                columnCheckInfo.setRow(index++);
                columnCheckInfo.setColumnName(columnName);
                //是否为重复字段
                if(recordColumnList.contains(columnName)) {
                    columnCheckInfo.setErrorMessage("导入重复字段");
                    errorColumnList.add(columnName);
                    errorColumnCount++;
                    columnCheckMessageList.add(columnCheckInfo);

                //表中是否存在当前字段
                } else if(columnList.contains(columnName)) {

                    columnCheckInfo.setErrorMessage("匹配成功");
                    String displayText = column.getDisplayName();
                    //未填写别名默认为字段名
                    if(Objects.isNull(displayText) || Objects.equals(displayText.trim(), "")) {
                        columnCheckInfo.setDisplayText(columnName);
                    } else {
                        if(displayText.length() > 64) {
                            columnCheckInfo.setErrorMessage("别名超出允许最大长度");
                            errorColumnList.add(columnName);
                            errorColumnCount++;
                        } else {
                            columnCheckInfo.setDisplayText(displayText);
                        }
                    }
                    columnCheckMessageList.add(columnCheckInfo);
                } else {
                    columnCheckInfo.setErrorMessage("数据表中未找到该字段");
                    errorColumnList.add(columnName);
                    errorColumnCount++;
                    columnCheckMessageList.add(columnCheckInfo);
                }
                recordColumnList.add(columnName);
            }
            columnCheckMessage.setColumnCheckInfoList(columnCheckMessageList);
            columnCheckMessage.setErrorColumnList(errorColumnList);
            columnCheckMessage.setTotalSize(columnCheckMessageList.size());
            columnCheckMessage.setErrorCount(errorColumnCount);
            if(errorColumnCount == 0) {

                Map<String, String> columnName2GuidMap = getColumnName2GuidMap(tableGuid);

                columnwithDisplayList.stream().forEach(column -> {
                    String columnName = column.getColumnName();
                    String guid = columnName2GuidMap.get(columnName);
                    column.setColumnId(guid);
                });

                editTableColumnDisplayName(columnwithDisplayList);
                columnCheckMessage.setStatus(ColumnCheckMessage.Status.SUCCESS);
            } else {
                columnCheckMessage.setStatus(ColumnCheckMessage.Status.FAILURE);
            }
            return columnCheckMessage;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }

    public Map<String, String> getColumnName2GuidMap(String tableGuid) throws AtlasBaseException {
        String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.COLUMN_INFO);
        String columnQuery = String.format(query, tableGuid);
        List<Map> columnInfoList = (List<Map>) graph.executeGremlinScript(columnQuery, false);
        Map name2GuidMap = new HashMap();
        for(Map obj : columnInfoList) {
            List<String> guidList = (List) obj.get("__guid");
            List<String> nameList = (List) obj.get("Asset.name");
            String guid = null;
            String name = null;
            if(Objects.nonNull(guidList) && guidList.size()>0) {
                guid = guidList.get(0);
            }
            if(Objects.nonNull(nameList) && nameList.size()>0) {
                name  = nameList.get(0);
            }
            if(Objects.nonNull(name) && Objects.nonNull(guid)) {
                name2GuidMap.put(name, guid);
            }
        }
        return name2GuidMap;
    }

    public ColumnCheckMessage importColumnWithDisplayText(String tableGuid, File file) throws AtlasBaseException {
        try {
            List<Column> columnAndDisplayMap = convertExceltoMap(file);
            String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.COLUMN_NAME);
            String columnQuery = String.format(query, tableGuid);
            List<String> columnList = (List<String>) graph.executeGremlinScript(columnQuery, false);
            return checkColumnName(tableGuid, columnList, columnAndDisplayMap);
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    public List<Column> convertExceltoMap(File file) throws AtlasBaseException {
        try {
            Workbook workbook = new WorkbookFactory().create(file);
            Sheet sheet = workbook.getSheetAt(0);
            int rowNum = sheet.getLastRowNum() + 1;
            Row row = null;
            String key = null;
            String value = null;
            List resultList = new ArrayList();
            Column column = null;
            for(int i=1; i<rowNum; i++) {
                row = sheet.getRow(i);
                key = row.getCell(0).getStringCellValue();
                value = row.getCell(1).getStringCellValue();
                column = new Column();
                column.setColumnName(key);
                column.setDisplayName(value);
                resultList.add(column);
            }
            return resultList;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }


    public File exportExcel(String tableGuid) throws AtlasBaseException {
        try {
            String query = gremlinQueryProvider.getQuery(MetaspaceGremlin3QueryProvider.MetaspaceGremlinQuery.COLUMN_NAME);
            String columnQuery = String.format(query, tableGuid);
            List<String> columnList = (List<String>) graph.executeGremlinScript(columnQuery, false);

            List<String> attributes = new ArrayList<>();
            attributes.add("字段名称");
            attributes.add("显示名称");

            List<List<String>> datas = new ArrayList<>();
            List<String> data = null;
            for(String columnName : columnList) {
                data = new ArrayList<>();
                data.add(columnName);
                datas.add(data);
            }
            Workbook workbook = PoiExcelUtils.createExcelFile(attributes, datas, XLSX);
            File file = new File("columns" + ".xlsx");
            FileOutputStream output = new FileOutputStream(file);
            workbook.write(output);
            output.flush();
            output.close();

            return file;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "导出Excel失败");
        }
    }

}
