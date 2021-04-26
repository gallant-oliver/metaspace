package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.modifiermanage.*;

import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.QualifierDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class QualifierService {
    private static final Logger LOG = LoggerFactory.getLogger(QualifierService.class);
    @Autowired
    private QualifierDAO qualifierDAO;

    //批量添加修饰词
    public String addQualifier(List<Data> dataList, String tenantId) throws AtlasBaseException {
        try {
            List<Qualifier> qualifiers = new ArrayList<>();
            for (Data data : dataList) {
                Qualifier qualifier = new Qualifier();
                qualifier.setId(UUID.randomUUID().toString());
                //判断mark是否是数字、汉字、英文且长度在0-32
                //if (data.getMark().matches("[\u4e00-\u9fa5_a-zA-Z0-9_]{0,32}")) {
                qualifier.setMark(data.getMark());
                //} else {
                //    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修饰词名称不符合规范，仅支持数字、英文、和汉字且不超过32个字符");
                //}
                //判断name是否是数字、英文小写、下划线且长度在0-32
                // if (data.getMark().matches("[a-z0-9_]{0,32}")) {
                qualifier.setName(data.getName());
                //} else {
                //    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修饰词名称不符合规范，仅支持数字、英文、和汉字且不超过32个字符");
                // }
                qualifier.setCreator(AdminUtils.getUserData().getUsername());
                qualifier.setCreateTime(DateUtils.currentTimestamp());
                qualifier.setUpdateUser(AdminUtils.getUserData().getUsername());
                qualifier.setUpdateTime(DateUtils.currentTimestamp());
                qualifier.setDesc(data.getDesc());
                qualifier.setTypeId(data.getTypeId());
                qualifiers.add(qualifier);
            }
            qualifierDAO.saveQualifier(qualifiers, tenantId);
            return "success";
        } catch (Exception e) {
            LOG.error("添加修饰词失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加修饰词失败");
        }
    }

    public List<Qualifier> getIdByMark(List<String> markList, String typeId, String tenantId) throws AtlasBaseException {
        return qualifierDAO.getIdByMark(markList, typeId, tenantId);
    }

    public List<Qualifier> getIdByName(List<String> nameList, String typeId, String tenantId) throws AtlasBaseException {
        return qualifierDAO.getIdByName(nameList, typeId, tenantId);
    }

    //批量删除修饰词
    @Transactional(rollbackFor = Exception.class)
    public int deleteQualifier(Data idList) throws AtlasBaseException {
        try {
            List<String> ids = idList.getIds();
            if (ids == null || ids.size() == 0) {
                return 0;
            }
            int num = qualifierDAO.deleteQualifierByIds(ids);
            return num;
        } catch (Exception e) {
            LOG.error("删除修饰词失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除修饰词失败");
        }
    }

    //编辑修饰词
    public void editQualifier(Data data, String tenantId) throws AtlasBaseException {
        try {
            Qualifier qualifier = new Qualifier();
            qualifier.setId(data.getId());
            qualifier.setName(data.getName());
            qualifier.setMark(data.getMark());
            qualifier.setUpdateUser(AdminUtils.getUserData().getUsername());
            qualifier.setUpdateTime(DateUtils.currentTimestamp());
            qualifier.setDesc(data.getDesc());
            qualifier.setTypeId(data.getTypeId());
            qualifierDAO.updateQualifierByIds(qualifier, tenantId);
        } catch (Exception e) {
            LOG.error("编辑修饰词失败", e);
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "编辑修饰词失败");
        }
    }

    public List<Qualifier> getIdsByNameOrMark(String id, String name, String mark, String typeId, String tenantId) throws AtlasBaseException {
        return qualifierDAO.getIdsByNameOrMark(id, name, mark, typeId, tenantId);
    }

    //修饰词列表
    public PageResult<Data> getQualifierList(QualifierParameters parameters, String tenantId) throws AtlasBaseException {
        try {
            List<Data> list = qualifierDAO.getQualifierList(parameters, tenantId);
            long totalSize = 0;
            if (list.size() != 0) {
                totalSize = list.get(0).getTotal();
            }
            PageResult<Data> result = new PageResult<>();
            result.setTotalSize(totalSize);
            result.setCurrentSize(list.size());
            result.setLists(list);
            return result;
        } catch (Exception e) {
            LOG.error("获取修饰词列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取修饰词列表失败");
        }
    }

    //修饰词列表
    public PageResult<Data> getAllQualifierList(QualifierParameters parameters, String tenantId) throws AtlasBaseException {
        try {
            List<Data> list = qualifierDAO.getAllQualifierList(parameters, tenantId);
            long totalSize = 0;
            if (list.size() != 0) {
                totalSize = list.get(0).getTotal();
            }
            PageResult<Data> result = new PageResult<>();
            result.setTotalSize(totalSize);
            result.setCurrentSize(list.size());
            result.setLists(list);
            return result;
        } catch (Exception e) {
            LOG.error("获取全部修饰词失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取全部修饰词失败");
        }
    }


    //获取修饰词引用列表
    public PageResult<ReferenceIndex> getQualifierRelationList(String id, String tenantId, int offset, int limit) throws AtlasBaseException {
        try {
            List<ReferenceIndex> list = qualifierDAO.getQualifierRelationListById(id, tenantId, limit, offset);
            long totalSize = 0;
            if (list.size() != 0) {
                totalSize = list.get(0).getTotal();
            }
            PageResult<ReferenceIndex> result = new PageResult<>();
            result.setTotalSize(totalSize);
            result.setCurrentSize(list.size());
            result.setLists(list);
            return result;
        } catch (Exception e) {
            LOG.error("获取修饰词引用列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取修饰词引用列表失败");
        }
    }

    //添加修饰词类型
    public List<Data> addQualifierType(Data data, String tenantId) throws AtlasBaseException {
        try {
            QualifierType qualifierType = new QualifierType();
            qualifierType.setQualifierTypeId(UUID.randomUUID().toString());
            qualifierType.setQualifierTypeMark(data.getMark());
            qualifierType.setQualifierTypeName(data.getName());
            qualifierType.setCreator(AdminUtils.getUserData().getUsername());
            qualifierType.setCreateTime(DateUtils.currentTimestamp());
            qualifierType.setUpdateUser(AdminUtils.getUserData().getUsername());
            qualifierType.setUpdateTime(DateUtils.currentTimestamp());
            qualifierType.setQualifierTypeDesc(data.getDesc());
            //添加修饰词类型
            qualifierDAO.addQualifierType(qualifierType, tenantId);
            List<Data> dataList = qualifierDAO.getQualifierTypeById(qualifierType.getQualifierTypeId(), tenantId);
            return dataList;
        } catch (
                Exception e) {
            LOG.error("添加修饰词类型失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加修饰词类型失败");
        }
    }

    //判断名字是否重复
    public List<QualifierType> getIdByTypeNameOrMark(String name, String mark, String tenantId) throws AtlasBaseException {
        return qualifierDAO.getIdByTypeNameOrMark(name, mark, tenantId);
    }

    //编辑修饰词类型
    public void editQualifierType(Data data, String tenantId) throws AtlasBaseException {
        try {
            QualifierType qualifierType = new QualifierType();
            qualifierType.setQualifierTypeId(data.getId());
            qualifierType.setQualifierTypeName(data.getName());
            qualifierType.setQualifierTypeMark(data.getMark());
            qualifierType.setUpdateUser(AdminUtils.getUserData().getUsername());
            qualifierType.setUpdateTime(DateUtils.currentTimestamp());
            qualifierType.setQualifierTypeDesc(data.getDesc());
            qualifierDAO.updateQualifierTypeByIds(qualifierType, tenantId);
        } catch (Exception e) {
            LOG.error("编辑修饰词类型失败", e);
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "编辑修饰词类型失败");
        }
    }

    public List<QualifierType> getIdsByTypeNameOrMark(String id, String name, String mark, String tenantId) throws AtlasBaseException {
        return qualifierDAO.getIdsByTypeNameOrMark(id, name, mark, tenantId);
    }

    //获取修饰词目录
    public List<Data> getQualifierTypeList(String tenantId) throws AtlasBaseException {
        try {
            List<Data> qualifierlist = qualifierDAO.getQualifierTypeList(tenantId);
            qualifierlist.forEach(qualifier -> {
                if (qualifier.getCount() == null) {
                    qualifier.setCount(0);
                }
            });
            Result result = new Result();
            result.setData(qualifierlist);
            return qualifierlist;
        } catch (Exception e) {
            LOG.error("获取修饰词目录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取修饰词目录失败");
        }
    }

    //删除修饰词类型
    @Transactional(rollbackFor = Exception.class)
    public int deleteQualifierType(Data idList, String tenantId) throws AtlasBaseException {
        try {
            List<String> ids = idList.getIds();
            if (ids == null || ids.size() == 0) {
                return 0;
            }
            qualifierDAO.removeQualifierByIds(ids, tenantId);
            int num = qualifierDAO.deleteQualifierTypeByIds(ids, tenantId);
            return num;
        } catch (Exception e) {
            LOG.error("删除修饰词类型失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除修饰词类型失败");
        }
    }
}
