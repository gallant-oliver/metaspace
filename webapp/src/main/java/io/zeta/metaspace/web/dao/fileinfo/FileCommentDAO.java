package io.zeta.metaspace.web.dao.fileinfo;

import io.zeta.metaspace.model.fileinfo.FileComment;
import io.zeta.metaspace.model.fileinfo.FileCommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    /**
     * 获取当我文件ID对应的备注列表
     *
     * @param fileId 文件ID
     * @return 返回对应的备注列表
     */
    List<FileCommentVO> selectByFileId(@Param("fileId") String fileId);

    int updateByPrimaryKeySelective(FileComment record);

    int updateByPrimaryKey(FileComment record);
}