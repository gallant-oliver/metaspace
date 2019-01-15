package org.apache.atlas.web.util;

import org.apache.atlas.exception.AtlasBaseException;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.RemoteIterator;
import org.testng.annotations.Test;
import io.zeta.metaspace.web.util.HdfsUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

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
public class HdfsUtilsTest {

    public void testListFiles() throws Exception {
        RemoteIterator<LocatedFileStatus> it = HdfsUtils.listFiles("pom.xml", true);
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }

    public void testListStatus() throws Exception {
        Arrays.stream(HdfsUtils.listStatus("/tmp")).forEach(fileStatus -> {
            System.out.println(fileStatus);
        });
    }

    public void testUploadFile() throws IOException, InterruptedException, AtlasBaseException {

        InputStream inputStream = new FileInputStream(new java.io.File("./pom.xml"));
        HdfsUtils.uploadFile(inputStream, "/tmp/pom.xml");
    }

    public static void main(String[] args) {

    }

}