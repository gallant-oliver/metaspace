package io.zeta.metaspace.web.dao.fileinfo;

import io.zeta.metaspace.model.fileinfo.FileComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件归档备注DAO
 *
 * @author w
 */
@Mapper
public interface FileCommentDAO {
    int deleteByPrimaryKey(String id);

    int insert(FileComment record);

    int insertSelective(FileComment record);

    FileComment selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(FileComment record);

    int updateByPrimaryKey(FileComment record);
}