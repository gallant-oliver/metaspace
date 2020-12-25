/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package io.zeta.metaspace.web.task.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.zeta.metaspace.model.measure.Measure;
import io.zeta.metaspace.model.measure.MeasureLivyResult;
import io.zeta.metaspace.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.kerberos.client.KerberosRestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
@Slf4j
public class LivyTaskSubmitHelper {
    private static final String REQUEST_BY_HEADER = "X-Requested-By";
    private static final int SLEEP_TIME;


    // Current number of tasks
    private RestTemplate restTemplate = new RestTemplate();

    private static final String url;

    private static final boolean isNeedKerberos;

    private static final String userPrincipal;
    private static final String keyTabLocation;

    public static final String MeasureEnv;

    public static Map<String, Object> SparkConfig;

    private static final int appIdRetryCount;
    private static String hdfsOutBasePath;


    static {
        try {
            Configuration configuration = ApplicationProperties.get();
            url = configuration.getString("livy.uri");
            SLEEP_TIME = configuration.getInt("livy.retry.sleep.time", 5000);
            isNeedKerberos = configuration.getBoolean("livy.need.kerberos");
            userPrincipal = isNeedKerberos ? configuration.getString("livy.server.auth.kerberos.principal") : null;
            keyTabLocation = isNeedKerberos ? configuration.getString("livy.server.auth.kerberos.keytab") : null;
            appIdRetryCount = isNeedKerberos ? configuration.getInt("livy.task.appId.retry.count") : 3;
            log.info("Livy uri : {} ,Need Kerberos: {} , kerberos : {} {}", url, isNeedKerberos, userPrincipal, keyTabLocation);

            SparkConfig = GsonUtils.getInstance().fromJson(new InputStreamReader(new FileSystemResource(System.getProperty("atlas.conf") + "/sparkConfig.json").getInputStream(), UTF_8), new TypeToken<Map<String, Object>>() {
            }.getType());
            MeasureEnv = IOUtils.toString(new FileSystemResource(System.getProperty("atlas.conf") + "/measureEnv.json").getInputStream(), UTF_8);

            JsonArray sinks = GsonUtils.getInstance().fromJson(MeasureEnv, JsonObject.class).get("sinks").getAsJsonArray();
            for (JsonElement sink : sinks) {
                if ("HDFS".equalsIgnoreCase(sink.getAsJsonObject().get("type").getAsString())) {
                    hdfsOutBasePath = sink.getAsJsonObject().get("config").getAsJsonObject().get("path").getAsString();
                    break;
                }
            }
            if (hdfsOutBasePath == null || hdfsOutBasePath.isEmpty()) {
                throw new AtlasBaseException("找不到 hdfs 输出路径");
            }
        } catch (Exception e) {
            log.error("初始化 livy 工具类失败", e);
            throw new AtlasBaseException(e);
        }
    }

    public static String getHdfsOutPath(String taskId, Long timestamp, String fileName) {
        return hdfsOutBasePath +
                "/" +
                taskId +
                "/" +
                timestamp +
                "/" +
                fileName;
    }


    public static String getOutName(String name) {
        return "_OUT_" + name.replace("-","_");
    }

    private String escapeCharacter(String str, String regex) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        String escapeCh = "\\" + regex;
        return str.replaceAll(regex, escapeCh);
    }

    private Map<String, Object> buildLivyArgs(Measure measure, String pool,Map<String,Object> config) throws IOException {
        Map<String, Object> livyArgs = new HashMap<>(SparkConfig);
        livyArgs.putAll(config);

        if (pool != null && !pool.isEmpty()) {
            livyArgs.put("queue", pool);
        }

        String finalMeasureJson = escapeCharacter(GsonUtils.getInstance().toJson(measure), "\\`");
        List<String> args = new ArrayList<>();
        args.add(MeasureEnv);
        args.add(finalMeasureJson);

        livyArgs.put("args", args);
        return livyArgs;
    }

    public MeasureLivyResult post2LivyWithRetry(Measure measure, String pool, Map<String,Object> config) {
        String result = postToLivy(measure, pool, config);
        MeasureLivyResult measureLivyResult = null;
        if (result != null) {
            measureLivyResult = retryLivyGetAppId(result, appIdRetryCount);
        }
        return measureLivyResult;
    }


    protected MeasureLivyResult retryLivyGetAppId(String result, int appIdRetryCount) {

        int retryCount = appIdRetryCount;
        TypeToken<HashMap<String, Object>> type =
                new TypeToken<HashMap<String, Object>>() {
                };
        MeasureLivyResult measureLivyResult = GsonUtils.getInstance().fromJson(result, MeasureLivyResult.class);

        if (retryCount <= 0) {
            return null;
        }

        if (measureLivyResult.getAppId() != null) {
            return measureLivyResult;
        }

        String sessionId = measureLivyResult.getId();
        if (sessionId == null) {
            return null;
        }

        while (retryCount-- > 0) {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
            measureLivyResult = getResultByLivyId(sessionId);
            log.info("retry get livy resultMap: {}, batches id : {}", measureLivyResult, sessionId);

            if (measureLivyResult != null && measureLivyResult.getAppId() != null) {
                break;
            }
        }

        return measureLivyResult;
    }

    public MeasureLivyResult getResultByLivyId(Object sessionId) {
        String livyUri = url + "/" + sessionId;
        String result = getFromLivy(livyUri);
        return GsonUtils.getInstance().fromJson(result, MeasureLivyResult.class);
    }

    public String postToLivy(Measure measure, String pool,Map<String,Object> config) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(REQUEST_BY_HEADER, "admin");

        if (!isNeedKerberos) {
            String result = null;
            try {
                String body = GsonUtils.getInstance().toJson(buildLivyArgs(measure, pool, config)).replaceAll("\\{", "{ ").replaceAll("}", " }");
                HttpEntity<String> springEntity = new HttpEntity<>(body, headers);
                result = restTemplate.postForObject(url, springEntity, String.class);
                log.info(result);
            } catch (HttpClientErrorException e) {
                log.error("Post to livy ERROR. \n  response status : " + e.getMessage()
                        + "\n  response header : " + e.getResponseHeaders()
                        + "\n  response body : " + e.getResponseBodyAsString());
            } catch (Exception e) {
                log.error("Post to livy ERROR. \n {}", e);
            }
            return result;
        } else {

            KerberosRestTemplate restTemplate = new KerberosRestTemplate(keyTabLocation, userPrincipal);
            HttpEntity<String> springEntity = null;
            try {
                String body = GsonUtils.getInstance().toJson(buildLivyArgs(measure, pool, config)).replaceAll("\\{", "{ ").replaceAll("}", " }");
                springEntity = new HttpEntity<>(body, headers);
            } catch (HttpClientErrorException e) {
                log.error("Post to livy ERROR. \n  response status : " + e.getMessage()
                        + "\n  response header : " + e.getResponseHeaders()
                        + "\n  response body : " + e.getResponseBodyAsString());
            } catch (Exception e) {
                log.error("Post to livy ERROR. {}", e.getMessage(), e);
            }
            String result = restTemplate.postForObject(url, springEntity, String.class);
            log.info(result);
            return result;
        }
    }

    public String getFromLivy(String uri) {
        log.info("Get From Livy URI is: " + uri);

        if (!isNeedKerberos) {
            return restTemplate.getForObject(uri, String.class);
        } else {

            KerberosRestTemplate restTemplate = new KerberosRestTemplate(keyTabLocation, userPrincipal);
            String result = restTemplate.getForObject(uri, String.class);
            log.info(result);
            return result;
        }
    }

    public void deleteByLivy(String sessionId) {
        String path = url + "/" + sessionId;
        log.info("Delete by Livy URI is: " + path);

        try {
            if (!isNeedKerberos) {
                new RestTemplate().delete(path);
            } else {
                KerberosRestTemplate restTemplate = new KerberosRestTemplate(keyTabLocation, userPrincipal);
                restTemplate.delete(path);
            }
        } catch (Exception e) {
            log.error("删除 spark 任务失败", e);
        }
    }
}
