package io.zeta.metaspace.web.service;

import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.model.table.TableSource;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.web.dao.TableDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TableService {
    private static final Logger LOG = LoggerFactory.getLogger(TableService.class);
    public static final String SQL_REGEX = "CREATE[\\s\\S]*TABLE[\\s|\\sIF\\sNOT\\sEXISTS\\s]*([\\S]*\\.[\\S]*)";

    @Autowired
    private TableDAO tableDAO;

    public String databaseAndTable(String sql) throws Exception {
        Pattern pattern = Pattern.compile(SQL_REGEX);
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "sql格式不正确" + sql);
    }

    public Map importSql(File file) {
        Map resultMap = new HashMap();
        BufferedReader reader = null;
        List<String> sqlList = new ArrayList<>();
        String sqlStr = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            while ((sqlStr = reader.readLine()) != null) {
                sqlList.add(sqlStr);
            }
            AdapterSource adapterSource = AdapterUtils.getHiveAdapterSource();
            Connection connection = adapterSource.getConnection(MetaspaceConfig.getHiveAdmin(), "", MetaspaceConfig.getHiveJobQueueName());
            adapterSource.getNewAdapterExecutor().execute(connection, sqlList);
            resultMap.put("errorCode", 0);
            resultMap.put("errorMsg", "执行成功");
            return resultMap;
        } catch (Exception e) {
            resultMap.put("errorCode", -1);
            resultMap.put("errorMsg", "执行失败，[" + e.getMessage() + "]");
            return resultMap;
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                LOG.warn("关闭文件失败");
            }
        }
    }

    public void execute(String sql) {
        String user = AdminUtils.getUserName();
        String pool = MetaspaceConfig.getHiveJobQueueName();
        AdapterSource adapterSource = AdapterUtils.getHiveAdapterSource();
        Connection connection = adapterSource.getConnection(user, "", pool);
        try {
            adapterSource.getNewAdapterExecutor().execute(connection, sql);
        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            String message = "Permission denied: user=" + user + ", access=WRITE";
            if (stackTrace.contains(message))
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "新建离线表失败," + user + "用户没有权限在此路径新建离线表");
            else
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "新建离线表失败,请检查表单信息和hive服务");
        }
    }

    public TableSource getTableSource(String tableId) {
        return tableDAO.selectTableSource(tableId);
    }
}
