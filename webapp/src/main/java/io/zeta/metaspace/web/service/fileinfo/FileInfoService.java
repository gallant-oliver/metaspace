package io.zeta.metaspace.web.service.fileinfo;

import io.zeta.metaspace.model.fileinfo.FileInfoVO;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.sourceinfo.Annex;
import io.zeta.metaspace.web.dao.fileinfo.FileInfoDAO;
import io.zeta.metaspace.web.model.CommonConstant;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.zeta.metaspace.model.fileinfo.FileInfo;
import io.zeta.metaspace.web.service.HdfsService;
import io.zeta.metaspace.web.util.AdminUtils;

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

    public void uploadFile(File file, String tenantId) {
        //1.上传hdfs文件
        try {
            String id = UUID.randomUUID().toString();
            String fileName = CommonConstant.FILE_CONCURRENT_HASH_MAP.get(file.getName());
            InputStream fileInputStream = new FileInputStream(file);
            String path = hdfsService.uploadFile(fileInputStream, file.getName(), tenantId);
            //创建记录
            FileInfo fileInfo = new FileInfo();
            fileInfo.setId(id);
            fileInfo.setFileSize(hdfsService.getFileSize(path));
            fileInfo.setFileName(fileName);
            fileInfo.setFilePath(path);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            fileInfo.setCreateTime(timestamp);
            fileInfo.setUpdateTime(timestamp);
            fileInfo.setDelete(false);
            fileInfo.setCreateUser(AdminUtils.getUserData().getAccount());
            fileInfo.setFileType(FilenameUtils.getExtension(fileName));
            fileInfoDAO.insert(fileInfo);
            CommonConstant.FILE_CONCURRENT_HASH_MAP.remove(file.getName());
        } catch (IOException | AtlasBaseException e) {
            throw new AtlasBaseException(e);
        }
    }

    public void createFileuploadRecord(Annex annexParam) {
        try {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setId(UUID.randomUUID().toString());
            fileInfo.setFileSize(annexParam.getFileSize());
            fileInfo.setFileName(annexParam.getFileName());
            fileInfo.setFilePath(annexParam.getPath());
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            fileInfo.setCreateTime(timestamp);
            fileInfo.setUpdateTime(timestamp);
            fileInfo.setDelete(false);
            fileInfo.setCreateUser(AdminUtils.getUserData().getAccount());
            fileInfo.setFileType(annexParam.getFileType());
            fileInfoDAO.insert(fileInfo);
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(e);
        }
    }

    public void createFileuploadRecord(String path, String fileName) {
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
}
