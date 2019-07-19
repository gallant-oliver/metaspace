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

import io.zeta.metaspace.web.util.AdminUtils;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/19 10:46
 */
public class OKHttpClient {

    private static final String TICKET_KEY = "X-SSO-FullticketId";

    public static String doPut(String url, String json) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .followRedirects(false)
                    .build();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
            String ticket = AdminUtils.getSSOTicket();
            Request request = new Request.Builder()
                                .url(url)
                                .addHeader(TICKET_KEY, ticket)
                                .post(body)
                                .build();

            Call call = client.newCall(request);

            Response response = call.execute();

            if (response.code() == org.apache.commons.httpclient.HttpStatus.SC_OK) {
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
