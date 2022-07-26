package io.zeta.metaspace.web.dao.fileinfo;

import io.zeta.metaspace.model.fileinfo.FileInfo;
import io.zeta.metaspace.model.fileinfo.FileInfoVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件归档DAO
 *
 * @author w
 */
public interface FileInfoDAO {
    int deleteByPrimaryKey(String id);

    int insert(FileInfo record);

    List<FileInfoVO> getFileInfoList(@Param("name") String name, @Param("limit") int limit, @Param("offset") int offset);

    int insertSelective(FileInfo record);

    FileInfo selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(FileInfo record);

    int updateByPrimaryKey(FileInfo record);
}