package io.zeta.metaspace.web.util;

import com.google.gson.Gson;
import io.zeta.metaspace.SecurePlusConfig;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.TablePermission;
import io.zeta.metaspace.utils.OKHttpClient;
import org.apache.atlas.exception.AtlasBaseException;

import java.util.*;

public class HivePermissionUtil {

    public static TablePermission getHivePermission(String db, String table,List<Column> columns ) throws AtlasBaseException {
        Set<String> allColumn = new HashSet<>();
        allColumn.add("*");
        Set<String> allColumn2 = new HashSet<>();
        for (Column column : columns) {
            allColumn2.add(column.getColumnName());
        }
        TablePermission tablePermission = new TablePermission();
        if (SecurePlusConfig.getSecurePlusEnable()) {
            String userName = AdminUtils.getUserName();
            String ssoTicket = AdminUtils.getSSOTicket();
            HashMap<String, String> map = new HashMap<>();
            map.put("X-SSO-FullticketId", ssoTicket);
            map.put("User-Agent", "Chrome");

            int retryCount = 0;
            String session = null;
            Map<String, String> queryParamMap = new HashMap<>();
            queryParamMap.put("user", userName);
            queryParamMap.put("database", db);
            queryParamMap.put("table", table);
            int retries = 3;
            while (Objects.isNull(session) && retryCount < retries) {
                session = OKHttpClient.doGet(SecurePlusConfig.getSecurePlusPrivilegeREST(),
                                            queryParamMap, map);
                if (Objects.nonNull(session)) {
                    Gson gson = new Gson();
                    Map body = gson.fromJson(session, Map.class);
                    Map data = (Map) body.get("data");
                    List<Map> privileges = (List<Map>) data.get("privileges");
                    if (privileges != null) {
                        Map<Set<String>, Set<String>> allPrivileges = new HashMap<>();
                        for (Map privilege : privileges) {
                            Set allColumns = new HashSet((List) privilege.get("columns"));
                            Set privilegeContents = new HashSet((List) privilege.get("privilegeContents"));
                            allPrivileges.put(allColumns, privilegeContents);
                        }
                        if (allPrivileges.containsKey(allColumn)) {
                            Set<String> set = allPrivileges.get(allColumn);
                            if (set.contains("r")) {
                                tablePermission.setRead(true);
                            }
                            if (set.contains("w")) {
                                tablePermission.setWrite(true);
                            }
                        } else if (allPrivileges.containsKey(allColumn2)) {
                            Set<String> set = allPrivileges.get(allColumn2);
                            if (set.contains("r")) {
                                tablePermission.setRead(true);
                            }
                            if (set.contains("w")) {
                                tablePermission.setWrite(true);
                            }
                        } else {
                            tablePermission.setRead(false);
                            tablePermission.setWrite(false);
                        }

                    } else {
                        tablePermission.setRead(false);
                        tablePermission.setWrite(false);
                    }
                } else {
                    tablePermission.setRead(false);
                    tablePermission.setWrite(false);
                    retryCount++;
                }
            }
        } else {
            tablePermission.setRead(true);
            tablePermission.setWrite(true);
        }
        return tablePermission;
    }
}
