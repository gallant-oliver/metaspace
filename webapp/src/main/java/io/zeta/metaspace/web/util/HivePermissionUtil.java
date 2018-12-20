package io.zeta.metaspace.web.util;

import com.google.gson.Gson;
import io.zeta.metaspace.SecurePlusConfig;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.TablePermission;
import io.zeta.metaspace.utils.SSLClient;
import org.apache.atlas.exception.AtlasBaseException;

import java.util.*;

public class HivePermissionUtil {

    public static TablePermission getHivePermission(String db, String table,List<Column> columns ) throws AtlasBaseException {
        Set<String> ALLCLOUMN = new HashSet<>();
        ALLCLOUMN.add("*");
        Set<String> ALLCLOUMN2 = new HashSet<>();
        for (Column column : columns) {
            ALLCLOUMN2.add(column.getColumnName());
        }
        TablePermission tablePermission = new TablePermission();
        if (SecurePlusConfig.getSecurePlusEnable()) {
            String userName = AdminUtils.getUserName();
            String ssoTicket = AdminUtils.getSSOTicket();
            HashMap<String, String> map = new HashMap<>();
            map.put("X-SSO-FullticketId", ssoTicket);
            String session = SSLClient.doGet(SecurePlusConfig.getSecurePlusPrivilegeREST()
                    + "?" + "user=" + userName
                    + "&" + "database=" + db
                    + "&" + "table=" + table, map);
            Gson gson = new Gson();
            Map body = gson.fromJson(session, Map.class);
            Map data = (Map) body.get("data");
            List<Map> privileges = (List<Map>) data.get("privileges");
            Map<Set<String>, Set<String>> allPrivileges = new HashMap<>();
            for (Map privilege : privileges) {
                Set allColumns = new HashSet((List)privilege.get("columns"));
                Set privilegeContents =  new HashSet((List)privilege.get("privilegeContents"));
                allPrivileges.put(allColumns,privilegeContents);
            }
            if(allPrivileges.containsKey(ALLCLOUMN)){
                Set<String> set = allPrivileges.get(ALLCLOUMN);
                if(set.contains("r")){
                    tablePermission.setREAD(true);
                }
                if(set.contains("w")){
                    tablePermission.setWRITE(true);
                }
            }else if(allPrivileges.containsKey(ALLCLOUMN2)){
                Set<String> set = allPrivileges.get(ALLCLOUMN2);
                if(set.contains("r")){
                    tablePermission.setREAD(true);
                }
                if(set.contains("w")){
                    tablePermission.setWRITE(true);
                }
            }
            else{
                tablePermission.setREAD(false);
                tablePermission.setWRITE(false);
            }
        } else {
            tablePermission.setREAD(true);
            tablePermission.setWRITE(true);
        }
        return tablePermission;
    }
}
