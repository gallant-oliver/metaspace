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
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.log4j.Logger;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
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

    private static Logger logger = Logger.getLogger(OKHttpClient.class);
    private static final String TICKET_KEY = "X-SSO-FullticketId";
    private static OkHttpClient client;
    static {
        client = new OkHttpClient();
        client.setConnectTimeout(1, TimeUnit.SECONDS);
    }

    /**
     * get请求
     * @return
     */
    public static String doGet(String url,Map<String,String> queryParamMap, Map<String,String> headerMap) {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();

            if(Objects.nonNull(queryParamMap)) {
                Set<Map.Entry<String, String>> queryParamEntries = queryParamMap.entrySet();
                for (Map.Entry<String, String> entry : queryParamEntries) {
                    urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
                }
            }
            String queryUrl = urlBuilder.build().toString();

            Request.Builder builder = new Request.Builder()
                    .url(queryUrl);

            if(Objects.nonNull(headerMap)) {
                Set<Map.Entry<String, String>> headerEntries = headerMap.entrySet();
                for (Map.Entry<String, String> entry : headerEntries) {
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            Request request = builder.build();
            Call call = client.newCall(request);
            Response response = call.execute();

            /**请求发送成功，并得到响应**/
            if (response.isSuccessful()) {
                /**读取服务器返回过来的json字符串数据**/
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String doPost(String url, String json) {
        try {
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
            String ticket = AdminUtils.getSSOTicket();
            Request request = new Request.Builder()
                                .url(url)
                                .addHeader(TICKET_KEY, ticket)
                                .post(body)
                                .build();
            Call call = client.newCall(request);
            Response response = call.execute();

            if (response.isSuccessful()) {
                InputStream in = response.body().byteStream();
                return getResponseStr(in);
            } else {
                throw new Exception("HTTP ERROR Status: " + response.code() + ":" + response.message());
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String doPut(String url, String json) {
        try {
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
            String ticket = AdminUtils.getSSOTicket();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader(TICKET_KEY, ticket)
                    .put(body)
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();

            if (response.isSuccessful()) {
                InputStream in = response.body().byteStream();
                return getResponseStr(in);
            } else {
                throw new Exception("HTTP ERROR Status: " + response.code() + ":" + response.message());
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * delete
     * @return
     */
    public static String doDelete(String url, Map<String,String> map) {
        try {
            Request.Builder builder = new Request.Builder()
                    .url(url);

            if(Objects.nonNull(map)) {
                Set<Map.Entry<String, String>> headerEntries = map.entrySet();
                for (Map.Entry<String, String> entry : headerEntries) {
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            Request request = builder.delete().build();
            Call call = client.newCall(request);
            Response response = call.execute();


            /**请求发送成功，并得到响应**/
            if (response.isSuccessful()) {
                /**读取服务器返回过来的json字符串数据**/
                String strResult = response.body().string();
                return strResult;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
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
}
