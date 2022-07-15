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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(FileInfoService.class);

    public void createFileuploadRecord(String path, String fileName, FileInfoPath fileInfoPath) {
        try {
            Long fileSize = hdfsService.getFileSize(path);
            FileInfo fileInfo = createPojo(fileName, path, fileInfoPath);
            fileInfo.setFileSize(fileSize);
            fileInfoDAO.insert(fileInfo);
        } catch (IOException | AtlasBaseException e) {
            LOG.error("归档失败", e);
        }
    }

    public PageResult getList(String name, int limit, int offset) {
        String query = queryEscape(name);
        List<FileInfoVO> fileInfoList = fileInfoDAO.getFileInfoList(query, limit, offset);
        PageResult pageResult = new PageResult();
        pageResult.setLists(fileInfoList);
        pageResult.setTotalSize(CollectionUtils.isEmpty(fileInfoList) ? 0 : fileInfoList.get(0).getTotal());
        pageResult.setCurrentSize(fileInfoList.size());
        pageResult.setOffset(offset);
        return pageResult;
    }

    public void createFileRecord(String upload, FileInfoPath fileInfoPath, File file) {
        try {
            String fileName = redisUtil.get(upload);
            String path = hdfsService.uploadFile(new FileInputStream(file), fileName, upload);
            Long fileSize = file.length();
            FileInfo fileInfo = createPojo(fileName, path, fileInfoPath);
            fileInfo.setFileSize(fileSize);
            fileInfoDAO.insert(fileInfo);
        } catch (IOException | AtlasBaseException e) {
            LOG.error("归档失败", e);
        } finally {
            redisUtil.delKey(upload);
        }
    }

    public void createFileRecordByFile(File file, String fileName, FileInfoPath fileInfoPath) {
        try {
            String path = hdfsService.uploadFile(new FileInputStream(file), fileName, UUID.randomUUID().toString());
            long fileSize = file.length();
            FileInfo fileInfo = createPojo(fileName, path, fileInfoPath);
            fileInfo.setFileSize(fileSize);
            fileInfoDAO.insert(fileInfo);
        } catch (IOException | AtlasBaseException e) {
            LOG.error("归档失败", e);
        }
    }

    public void categoryCreateFileRecord(String upload, int type, File file) {
        if (type == CommonConstant.INDICATORS_CATEGORY_TYPE) {
            createFileRecord(upload, FileInfoPath.INDICATORS_CATEGORY, file);
        }
        if (type == CommonConstant.BUSINESS_CATEGORY_TYPE) {
            createFileRecord(upload, FileInfoPath.BUSINESS_CATEGORY, file);
        }
    }

    private String queryEscape(String name) {
        if (StringUtils.isNotEmpty(name) && (name.contains("(") || name.contains(")"))) {
            String replace = name.replace("(", "\\(");
            replace = replace.replace(")", "\\)");
            return replace;
        } else {
            return name;
        }
    }

    private FileInfo createPojo(String fileName, String path, FileInfoPath fileInfoPath) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setId(UUID.randomUUID().toString());
        fileInfo.setFileName(fileName);
        fileInfo.setFilePath(path);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        fileInfo.setCreateTime(timestamp);
        fileInfo.setUpdateTime(timestamp);
        fileInfo.setDelete(false);
        fileInfo.setBusinessPath(fileInfoPath.getPath());
        fileInfo.setFileType(FilenameUtils.getExtension(fileName));
        fileInfo.setCreateUser(AdminUtils.getUserData().getAccount());
        return fileInfo;
    }

}
