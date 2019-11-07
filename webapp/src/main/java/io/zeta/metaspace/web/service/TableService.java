package io.zeta.metaspace.web.service;

import io.zeta.metaspace.repository.tablestat.TableStatService;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TableService {
    private static final Logger LOG = LoggerFactory.getLogger(TableService.class);

    public String databaseAndTable(String sql) throws Exception {
        Pattern pattern = Pattern.compile("CREATE[\\s\\S]*TABLE[\\s|\\sIF\\sNOT\\sEXISTS\\s]*([\\S]*\\.[\\S]*)");
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
            while((sqlStr = reader.readLine()) != null) {
                sqlList.add(sqlStr);
            }
            HiveJdbcUtils.execute(sqlList);
            resultMap.put("errorCode", 0);
            resultMap.put("errorMsg", "执行成功");
        } catch (Exception e) {
            resultMap.put("errorCode", -1);
            resultMap.put("errorMsg", "执行失败，[" + e.getMessage() + "]");
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                LOG.warn("关闭文件失败");
            }
            return resultMap;
        }
    }
}
