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
package io.zeta.metaspace.web.service.dataquality;

import com.gridsum.gdp.library.commons.utils.UUIDUtils;

import io.zeta.metaspace.model.dataquality2.WarningGroup;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.dataquality.WarningGroupDAO;
import io.zeta.metaspace.web.service.CategoryRelationUtils;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.BeansUtil;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WarningGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(WarningGroupService.class);

    @Autowired
    private WarningGroupDAO warningGroupDAO;

    public int insert(WarningGroup warningGroup) throws AtlasBaseException {
        warningGroup.setId(UUIDUtils.alphaUUID());
        warningGroup.setCreateTime(DateUtils.currentTimestamp());
        warningGroup.setUpdateTime(DateUtils.currentTimestamp());
        warningGroup.setCreator(AdminUtils.getUserData().getUserId());
        warningGroup.setDelete(false);
        return warningGroupDAO.insert(warningGroup);
    }

    public WarningGroup getById(String id) throws AtlasBaseException {
        WarningGroup warningGroup = warningGroupDAO.getById(id);
        String path = CategoryRelationUtils.getPath(warningGroup.getCategoryId());
        warningGroup.setPath(path);
        return warningGroup;
    }

    public WarningGroup getByName(String name) throws AtlasBaseException {
        WarningGroup warningGroup = warningGroupDAO.getByName(name);
        String path = CategoryRelationUtils.getPath(warningGroup.getCategoryId());
        warningGroup.setPath(path);
        return warningGroup;
    }

    public void deleteById(String number) throws AtlasBaseException {
        warningGroupDAO.deleteById(number);
    }

    public void deleteByIdList(List<String> numberList) throws AtlasBaseException {
        warningGroupDAO.deleteByIdList(numberList);
    }

    public int update(WarningGroup warningGroup) throws AtlasBaseException {
        warningGroup.setUpdateTime(DateUtils.currentTimestamp());
        WarningGroup old = getById(warningGroup.getId());
        BeansUtil.copyPropertiesIgnoreNull(warningGroup, old);
        return warningGroupDAO.update(old);
    }

    public PageResult<WarningGroup> search(Parameters parameters) {
        List<WarningGroup> list = warningGroupDAO.search(parameters).stream()
                .map(warningGroup -> {
                    String path = null;
                    try {
                        path = CategoryRelationUtils.getPath(warningGroup.getCategoryId());
                    } catch (AtlasBaseException e) {
                        LOG.error(e.getMessage(), e);
                    }
                    warningGroup.setPath(path);
                    return warningGroup;
                }).collect(Collectors.toList());
        PageResult<WarningGroup> pageResult = new PageResult<>();
        long sum = warningGroupDAO.countBySearch(parameters.getQuery());
        pageResult.setOffset(parameters.getOffset());
        pageResult.setSum(sum);
        pageResult.setCount(list.size());
        pageResult.setLists(list);
        return pageResult;
    }

}
