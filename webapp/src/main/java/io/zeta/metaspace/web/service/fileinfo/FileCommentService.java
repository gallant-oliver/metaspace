package io.zeta.metaspace.web.service.fileinfo;

import io.zeta.metaspace.model.fileinfo.FileComment;
import io.zeta.metaspace.model.fileinfo.FileCommentVO;
import io.zeta.metaspace.model.fileinfo.FileInfo;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.fileinfo.FileCommentDAO;
import io.zeta.metaspace.web.dao.fileinfo.FileInfoDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * 文件备注Service
 *
 * @author w
 */
@Service
public class FileCommentService {

    private static final Logger LOG = LoggerFactory.getLogger(FileCommentService.class);

    @Autowired
    private FileCommentDAO fileCommentDAO;
    @Autowired
    private FileInfoDAO fileInfoDAO;

    /**
     * 获取当前文件对应的备注列表
     *
     * @param fileId 当前文件ID
     * @return 返回备注列表
     * @throws AtlasBaseException 抛出对应信息
     */
    public List<FileCommentVO> getFileCommentList(String fileId) throws AtlasBaseException {
        if (StringUtils.isEmpty(fileId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前文件ID为空");
        }
        List<FileCommentVO> fileComments;
        try {
            User user = AdminUtils.getUserData();
            fileComments = fileCommentDAO.selectByFileId(fileId);
            if (CollectionUtils.isNotEmpty(fileComments)) {
                for (FileCommentVO fileComment : fileComments) {
                    fileComment.setCanDel(fileComment.getCreateUser().equals(user.getAccount()));
                }
            }
        } catch (AtlasBaseException e) {
            LOG.error("查询文件备注失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询文件备注失败");
        }
        return fileComments;
    }

    /**
     * 添加对应文件的备注信息
     *
     * @param fileComment 文件备注信息
     * @throws AtlasBaseException 抛出对应信息
     */
    public void addFileComment(FileComment fileComment) throws AtlasBaseException {
        if (fileComment == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加文件备注不能为空！");
        }
        if (StringUtils.isBlank(fileComment.getName())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加文件备注内容不能为空！");
        }
        try {
            FileInfo fileInfo = fileInfoDAO.selectByPrimaryKey(fileComment.getFileId());
            if (fileInfo == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前文件不存在");
            }
            User user = AdminUtils.getUserData();
            fileComment.setId(UUID.randomUUID().toString());
            fileComment.setCreateUser(user.getAccount());
            fileComment.setCreateTime(new Timestamp(System.currentTimeMillis()));
            fileCommentDAO.insert(fileComment);
        } catch (AtlasBaseException e) {
            LOG.error("添加文件备注失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加文件备注失败！");
        }
    }

    /**
     * 移除文件归档备注（谁创建谁删除）
     *
     * @param fileCommentId 文件备注ID
     * @throws AtlasBaseException 抛出对应信息
     */
    public void deleteFileComment(String fileCommentId) throws AtlasBaseException {
        if (StringUtils.isBlank(fileCommentId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件备注为空！");
        }
        User user = AdminUtils.getUserData();
        FileComment fileComment = fileCommentDAO.selectByPrimaryKey(fileCommentId);
        if (fileComment == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件已删除！");
        }
        if (!fileComment.getCreateUser().equals(user.getAccount())) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有权限删除！");
        }
        fileCommentDAO.deleteByPrimaryKey(fileCommentId);
    }
}
