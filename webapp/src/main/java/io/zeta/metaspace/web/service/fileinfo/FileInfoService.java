package io.zeta.metaspace.web.service.fileinfo;

import io.zeta.metaspace.model.enums.FileInfoPath;
import io.zeta.metaspace.model.fileinfo.FileInfoVO;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.dao.fileinfo.FileInfoDAO;
import io.zeta.metaspace.web.model.CommonConstant;
import io.zeta.metaspace.web.util.RedisUtil;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.zeta.metaspace.model.fileinfo.FileInfo;
import io.zeta.metaspace.web.service.HdfsService;
import io.zeta.metaspace.web.util.AdminUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;


/**
 * 文件归档Service层
 *
 * @author w
 */
@Service
public class FileInfoService {
    @Autowired
    private FileInfoDAO fileInfoDAO;
    @Autowired
    private HdfsService hdfsService;
    @Autowired
    private RedisUtil redisUtil;


    @Transactional(rollbackFor = Exception.class)
    public void createFileuploadRecord(String path, String fileName,FileInfoPath fileInfoPath) {
        try {
            Long fileSize = hdfsService.getFileSize(path);
            FileInfo fileInfo = new FileInfo();
            fileInfo.setId(UUID.randomUUID().toString());
            fileInfo.setFileSize(fileSize);
            fileInfo.setFileName(fileName);
            fileInfo.setFilePath(path);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            fileInfo.setCreateTime(timestamp);
            fileInfo.setUpdateTime(timestamp);
            fileInfo.setDelete(false);
            fileInfo.setBusinessPath(fileInfoPath.getPath());
            fileInfo.setCreateUser(AdminUtils.getUserData().getAccount());
            fileInfo.setFileType(FilenameUtils.getExtension(fileName));
            fileInfoDAO.insert(fileInfo);
        } catch (IOException | AtlasBaseException e) {
            throw new AtlasBaseException(e);
        }
    }

    public PageResult getList(String name, int limit, int offset) {
        List<FileInfoVO> fileInfoList = fileInfoDAO.getFileInfoList(name, limit, offset);
        PageResult pageResult = new PageResult();
        pageResult.setLists(fileInfoList);
        pageResult.setTotalSize(CollectionUtils.isEmpty(fileInfoList) ? 0 : fileInfoList.get(0).getTotal());
        pageResult.setCurrentSize(fileInfoList.size());
        pageResult.setOffset(offset);
        return pageResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createFileRecord(String upload, FileInfoPath fileInfoPath, File file) {
        try {
            String fileName = redisUtil.get(upload);
            String path = hdfsService.uploadFile(new FileInputStream(file), fileName, upload);
            Long fileSize = hdfsService.getFileSize(path);
            FileInfo fileInfo = new FileInfo();
            fileInfo.setId(UUID.randomUUID().toString());
            fileInfo.setFileSize(fileSize);
            fileInfo.setFileName(fileName);
            fileInfo.setFilePath(path);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            fileInfo.setCreateTime(timestamp);
            fileInfo.setUpdateTime(timestamp);
            fileInfo.setDelete(false);
            fileInfo.setCreateUser(AdminUtils.getUserData().getAccount());
            fileInfo.setBusinessPath(fileInfoPath.getPath());
            fileInfo.setFileType(FilenameUtils.getExtension(fileName));
            fileInfoDAO.insert(fileInfo);
        } catch (IOException | AtlasBaseException e) {
            throw new AtlasBaseException("文件归档失败", e);
        } finally {
            redisUtil.delKey(upload);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void categoryCreateFileRecord(String upload, int type,File file) throws IOException {
        if (type == CommonConstant.INDICATORS_CATEGORY_TYPE) {
            createFileRecord(upload, FileInfoPath.INDICATORS_CATEGORY, file);
        }
        if (type == CommonConstant.BUSINESS_CATEGORY_TYPE) {
            createFileRecord(upload, FileInfoPath.BUSINESS_CATEGORY, file);
        }
    }
}
