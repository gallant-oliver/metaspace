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
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/7/19 10:46
 */
package io.zeta.metaspace.utils;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import io.zeta.metaspace.MetaspaceConfig;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/19 10:46
 */
public class OKHttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(OKHttpClient.class);
    private static OkHttpClient client;
    private static int okHttpTimeout;
    static {
        client = new OkHttpClient().setSslSocketFactory(SSLSocketClient.getSSLSocketFactory())
                .setHostnameVerifier(SSLSocketClient.getHostnameVerifier());
        client.setConnectTimeout(5, TimeUnit.SECONDS);
    }


    /**
     * get请求
     * @return
     */
    public static String doGet(String url,Map<String,String> queryParamMap, Map<String,String> headerMap) throws AtlasBaseException {
        return doGet(url, queryParamMap, headerMap,-1);
    }

    /**
     * get请求
     * @return
     */
    public static String doGet(String url,Map<String,String> queryParamMap, Map<String,String> headerMap, int times) throws AtlasBaseException {
        try {
            String queryUrl = buildUrl(url, queryParamMap);

            Headers headers = buildHeaders(headerMap);
            Request request = new Request.Builder()
                    .url(queryUrl).headers(headers).build();
            return getResponse(request, times);
        } catch(AtlasBaseException e){
            throw e;
        }catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e, "请求失败"+e.getMessage());
        }
    }


    public static String doPost(String url, String json, Map<String, Object> headerMap) throws AtlasBaseException {
       return doPost(url,headerMap,null,json);
    }


    public static String doPost(String url, Map<String, Object> headerMap,Map<String,Object> queryParamMap, String json) throws AtlasBaseException {
        return doPost(url,headerMap,queryParamMap,json,-1);
    }

    public static String doPost(String url, Map<String, Object> headerMap,Map<String,Object> queryParamMap, String json, int times) throws AtlasBaseException {
        try {
            //requestBody
            String queryUrl = buildUrl(url, queryParamMap);
            Headers headers = buildHeaders(headerMap);
            Request.Builder builder = new Request.Builder()
                    .url(queryUrl)
                    .headers(headers);

            if(StringUtils.isNotBlank(json)){
                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
                builder.post(body);
            }else{
                builder.post(RequestBody.create(null, ""));
            }
            Request request = builder.build();
            return getResponse(request, times);
        }catch (AtlasBaseException e){
            throw e;
        }catch(Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e, "请求失败"+e.getMessage());
        }
    }

    public static Headers buildHeaders(Map<String, ? extends Object> headerMap) {
        Headers.Builder headerBuilder = new Headers.Builder();
        if(MapUtils.isNotEmpty(headerMap)){
            Iterator<? extends Map.Entry<String, ?>> headerIterator = headerMap.entrySet().iterator();
            headerIterator.forEachRemaining(header -> {
                headerBuilder.add(header.getKey(), String.valueOf(header.getValue()));
            });
        }
        return headerBuilder.build();
    }

    public static String buildUrl(String url, Map<String,? extends  Object> queryParamMap) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        if(Objects.nonNull(queryParamMap)) {


            Set<? extends Map.Entry<String, ?>> queryParamEntries = queryParamMap.entrySet();

            for (Map.Entry<String, ?> entry : queryParamEntries) {
                urlBuilder.addQueryParameter(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return urlBuilder.build().toString();
    }

    public static String doPut(String url, String json, Map<String, Object> headerMap) throws AtlasBaseException {
        return doPut(url, json, headerMap, -1);
    }

    public static String doPut(String url, String json, Map<String, Object> headerMap, int times) throws AtlasBaseException {
        try {

            Request.Builder builder = new Request.Builder().url(url);
            if(MapUtils.isNotEmpty(headerMap)){
                Headers headers = buildHeaders(headerMap);
                builder.headers(headers);
            }
            if(StringUtils.isNotBlank(json)){
                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
                builder.put(body);
            }else{
                builder.put(RequestBody.create(null, ""));
            }
            Request request = builder.build();
            return getResponse(request, times);
        } catch(AtlasBaseException e){
            throw e;
        } catch(Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "请求失败"+e.getMessage());
        }
    }

    /**
     * delete
     * @return
     */
    public static String doDelete(String url) throws AtlasBaseException {
        return doDelete(url, null,null, -1);
    }

    /**
     * delete
     * @return
     */
    public static String doDelete(String url, Map<String,String> map) throws AtlasBaseException {
        return doDelete(url, map,null, -1);
    }

    /**
     * delete
     * @return
     */
    public static String doDelete(String url, Map<String,String> map, String bodyJson) throws AtlasBaseException {
        return doDelete(url, map,bodyJson, -1);
    }

    /**
     * delete
     * @return
     */
    public static String doDelete(String url, Map<String,String> map, String bodyJson, int times) throws AtlasBaseException {
        try {
            Request.Builder builder = new Request.Builder()
                    .url(url);
            if (StringUtils.isNotEmpty(bodyJson)){
                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyJson);
                builder.delete(body);
            }else {
                builder.delete();
            }
            if(Objects.nonNull(map)) {
                map.put("Content-Type","application/json");
                Set<Map.Entry<String, String>> headerEntries = map.entrySet();
                for (Map.Entry<String, String> entry : headerEntries) {
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            Request request = builder.build();
            return getResponse(request, times);
        } catch(AtlasBaseException e){
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"请求失败"+e.getMessage());
        }
    }

    public static String getResponseStr(InputStream in) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return URLDecoder.decode(baos.toString(), "UTF-8");
        } catch (Exception e) {
            throw e;
        }
    }


    public static String getResponse(Request request, int times) throws AtlasBaseException, AtlasException {

        int count=1;
        OkHttpClient client = OKHttpClient.client;
        okHttpTimeout = MetaspaceConfig.getOkHttpTimeout();
        client.setReadTimeout(okHttpTimeout, TimeUnit.SECONDS);
        if(-1==times){
            times = ApplicationProperties.get().getInt("okhttp.retries", 3);
        }
        while(true){
            try {
                Call call = client.newCall(request);
                Response response = call.execute();
                InputStream in = response.body().byteStream();
                return getResponseStr(in);
            } catch (Exception e) {
                if (count<times){
                    LOG.error("第"+count +"次请求失败：", e);
                    client = new OkHttpClient().setSslSocketFactory(SSLSocketClient.getSSLSocketFactory())
                            .setHostnameVerifier(SSLSocketClient.getHostnameVerifier());
                    client.setConnectTimeout(5+count, TimeUnit.SECONDS);
                    client.setReadTimeout(30+count*10, TimeUnit.SECONDS);
                    count++;
                    continue;
                }
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e, "请求失败"+e.getMessage());
            }
        }
    }
}
