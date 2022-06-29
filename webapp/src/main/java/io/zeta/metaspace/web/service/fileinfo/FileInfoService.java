package io.zeta.metaspace.web.service.fileinfo;

import io.zeta.metaspace.web.dao.fileinfo.FileInfoDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文件归档Service层
 *
 * @author w
 */
@Service
public class FileInfoService {
    @Autowired
    private FileInfoDAO fileInfoDAO;

}
