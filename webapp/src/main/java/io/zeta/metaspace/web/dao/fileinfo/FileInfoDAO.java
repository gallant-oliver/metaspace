package io.zeta.metaspace.web.dao.fileinfo;

import io.zeta.metaspace.model.fileinfo.FileInfo;

/**
 * 文件归档DAO
 *
 * @author w
 */
public interface FileInfoDAO {
    int deleteByPrimaryKey(String id);

    int insert(FileInfo record);

    int insertSelective(FileInfo record);

    FileInfo selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(FileInfo record);

    int updateByPrimaryKey(FileInfo record);
}