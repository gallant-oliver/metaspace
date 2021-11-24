package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.HealthCheckVo;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.model.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查接口
 */
@Service
@Slf4j
public class HealthCheckService {
    @Autowired
    private UserDAO userDAO;

    public HealthCheckVo<Map<String, Object>> healthCheck() {
        HealthCheckVo<Map<String, Object>> healthCheckVo = new HealthCheckVo();
        Map<String, Object> map = new HashMap<>();
        healthCheckVo.setStatus(CommonConstant.UP);
        map.put("db", getDatabaseStatus());
        map.put("sysInfo", getSystemInfo());
        healthCheckVo.setDetails(map);
        return healthCheckVo;
    }

    /**
     * 获取数据库状态
     *
     * @return
     */
    private HealthCheckVo<Map<String, Object>> getDatabaseStatus() {
        HealthCheckVo<Map<String, Object>> healthCheckVo = new HealthCheckVo();
        Map<String, Object> map = new HashMap<>();
        try {
            healthCheckVo.setStatus(CommonConstant.UP);
            map.put("count", userDAO.selectDatabaseCount());
            map.put("database", "PostgreSQL");
            healthCheckVo.setDetails(map);
        } catch (Exception e) {
            log.error("getDatabaseStatus exception is {}", e);
            healthCheckVo.setStatus(CommonConstant.DOWN);
            map.put("error", e.getMessage());
            healthCheckVo.setDetails(map);
        }
        return healthCheckVo;
    }

    /**
     * 获取系统信息
     *
     * @return
     */
    private HealthCheckVo<Map<String, Object>> getSystemInfo() {
        HealthCheckVo<Map<String, Object>> healthCheckVo = new HealthCheckVo();
        Map<String, Object> map = new HashMap<>();
        try {
            //可用内存
            long totalMemory = Runtime.getRuntime().totalMemory();
            // 剩余内存
            long freeMemory = Runtime.getRuntime().freeMemory();
            // 最大可使用内存
            long maxMemory = Runtime.getRuntime().maxMemory();
            healthCheckVo.setStatus(CommonConstant.UP);
            map.put("totalMemory", totalMemory);
            map.put("freeMemory", freeMemory);
            map.put("maxMemory", maxMemory);
            healthCheckVo.setDetails(map);
        } catch (Exception e) {
            log.error("getSystemInfo exception is {}", e);
            healthCheckVo.setStatus(CommonConstant.DOWN);
            map.put("error", e.getMessage());
            healthCheckVo.setDetails(map);
        }
        return healthCheckVo;
    }
}
