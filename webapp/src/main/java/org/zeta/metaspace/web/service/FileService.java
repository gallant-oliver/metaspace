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

package org.zeta.metaspace.web.service;

import com.google.common.collect.Lists;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.utils.DateUtils;
import org.apache.atlas.utils.PageUtils;
import org.zeta.metaspace.web.util.HdfsUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.security.AccessControlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class FileService {

    private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

    /**
     * 查找文件或文件夹
     *
     * @param filePath         文件路径
     * @param modificationDate 文件最后修改日期
     * @param owner            文件拥有人
     * @return
     * @throws Exception
     */
    public static Pair<Integer, List<FileStatus>> listStatus(String filePath, String modificationDate, String owner, int offset, int limit,
                                                             String orderBy, String sortBy) throws Exception {
        List<FileStatus> list = Arrays.stream(HdfsUtils.listStatus(filePath))
                .filter(fileStatus -> {
                    try {
                        return match(fileStatus, null, modificationDate, owner);
                    } catch (AtlasBaseException e) {
                        throw new RuntimeException();
                    }
                })
                .collect(Collectors.toList());
        List<FileStatus> all = Lists.newArrayList(list.iterator());
        if (StringUtils.isNotBlank(orderBy)) {
            Collections.sort(all, new FileComparator(orderBy, sortBy));
        }

        List<FileStatus> pageList = PageUtils.pageList(all, offset, limit);

        return Pair.of(list.size(), pageList);
    }

    private static class FileComparator implements Comparator<FileStatus> {

        private String orderBy;
        private String sortBy;

        public FileComparator(String orderBy, String sortBy) {
            this.orderBy = orderBy;
            this.sortBy = sortBy;
        }

        @Override
        public int compare(FileStatus o1, FileStatus o2) {
            if ("fileName".equals(orderBy)) {
                if ("desc".equals(sortBy)) {
                    return o2.getPath().getName().compareTo(o1.getPath().getName());
                } else {
                    return o1.getPath().getName().compareTo(o2.getPath().getName());
                }
            } else {
                throw new RuntimeException("orderBy " + orderBy + " is not support");
            }
        }
    }

    /**
     * [递归]查找文件
     *
     * @param fileName         文件名
     * @param modificationDate 文件最后修改日期，格式：yyyy-MM-dd
     * @param owner            文件拥有人
     * @param recursive        是否递归查找
     * @return
     * @throws Exception
     */
    public static Pair<Integer, List<FileStatus>> listFiles(String fileName, String modificationDate, String owner, int offset, int limit, boolean recursive, String orderBy, String sortBy) throws Exception {
        List<FileStatus> all = new ArrayList<>();
        RemoteIterator<LocatedFileStatus> files = HdfsUtils.listFiles("/", recursive);
        boolean hasNext = true;
        while (hasNext) {
            try {
                hasNext = files.hasNext();
                LocatedFileStatus file = files.next();
                if (match(file, fileName, modificationDate, owner)) {
                    all.add(file);
                }
            } catch (NoSuchElementException e) {
                //遍历到最后一个文件直接next导致的，忽略
            } catch (AccessControlException e) {
                //忽略没权限的文件
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        if (StringUtils.isNotBlank(orderBy)) {
            Collections.sort(all, new FileComparator(orderBy, sortBy));
        }
        List<FileStatus> pageList = PageUtils.pageList(all, offset, limit);

        return Pair.of(all.size(), pageList);
    }


    /**
     * 搜索或列出文件或文件夹
     * @param file
     * @param fileName
     * @param modificationDate
     * @param owner
     * @return
     */
    private static boolean match(FileStatus file, String fileName, String modificationDate, String owner) throws AtlasBaseException {


        if(!HdfsUtils.canAccess(file.getPath().toUri().getPath(),"r")){
            return false;
        }
        if (StringUtils.isNotBlank(fileName)) {
            boolean fileNameMatch = file.getPath().getName().contains(fileName);
            if (!fileNameMatch) {
                return false;
            }
        }

        if (StringUtils.isNotBlank(modificationDate)) {
            boolean dateMatch = DateUtils.inDay(modificationDate, file.getModificationTime());
            if (!dateMatch) {
                return false;
            }
        }

        if (StringUtils.isNotBlank(owner)) {
            boolean ownerMatch = file.getOwner().contains(owner);
            if (!ownerMatch) {
                return false;
            }
        }

        return true;
    }


}
