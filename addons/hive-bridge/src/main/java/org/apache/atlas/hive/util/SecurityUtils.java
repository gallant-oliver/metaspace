package org.apache.atlas.hive.util;

import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

public class SecurityUtils {

    public static UserGroupInformation getUGI() throws IOException {
        String doAs = System.getenv("HADOOP_USER_NAME");
        if (doAs != null && doAs.length() > 0) {
            return UserGroupInformation.createProxyUser(doAs, UserGroupInformation.getLoginUser());
        }
        return UserGroupInformation.getCurrentUser();
    }
}
