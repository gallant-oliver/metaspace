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
package io.zeta.metaspace.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;

import com.google.gson.Gson;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class SSLClient {

    private static Logger logger = Logger.getLogger(SSLClient.class);
    private static final String TICKET_KEY = "X-SSO-FullticketId";

    /**
     * get请求
     * @return
     */
    public static String doGet(String url, Map<String,String> map) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            //发送get请求
            HttpGet request = new HttpGet(url);
            Set<Map.Entry<String, String>> entries = map.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                request.setHeader(entry.getKey(),entry.getValue());
            }

            HttpResponse response = client.execute(request);

            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的json字符串数据**/
                String strResult = EntityUtils.toString(response.getEntity());

                return strResult;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }



    /**
     * post请求(用于key-value格式的参数)
     * @param url
     * @param params
     * @return
     */
    public static String doPost(String url, Map params){

        BufferedReader in = null;
        try {
            // 定义HttpClient
            HttpClient client = HttpClientBuilder.create().build();
            // 实例化HTTP方法
            HttpPost request = new HttpPost();
            request.setURI(new URI(url));

            //设置参数
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            for (Iterator iter = params.keySet().iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                String value = String.valueOf(params.get(name));
                nvps.add(new BasicNameValuePair(name, value));

                //System.out.println(name +"-"+value);
            }
            request.setEntity(new UrlEncodedFormEntity(nvps,HTTP.UTF_8));

            HttpResponse response = client.execute(request);
            int code = response.getStatusLine().getStatusCode();
            if(code == 200){	//请求成功
                in = new BufferedReader(new InputStreamReader(response.getEntity()
                        .getContent(),"utf-8"));
                StringBuffer sb = new StringBuffer("");
                String line = "";
                String NL = System.getProperty("line.separator");
                while ((line = in.readLine()) != null) {
                    sb.append(line + NL);
                }

                in.close();

                return sb.toString();
            }
            else{	//
                System.out.println("状态码：" + code);
                return null;
            }
        }
        catch(Exception e){
            e.printStackTrace();

            return null;
        }
    }

    /**
     * post请求（用于请求json格式的参数）
     * @param url
     * @param json
     * @return
     */
    public static String doPost(String url, String json) {
        try {
            // 定义HttpClient
            org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();
            PostMethod postMethod = new PostMethod(url);
            String ticket = AdminUtils.getSSOTicket();
            postMethod.addRequestHeader(new org.apache.commons.httpclient.Header(TICKET_KEY, ticket));
            RequestEntity requestEntity = new StringRequestEntity(json);
            postMethod.setRequestEntity(requestEntity);
            int result = httpClient.executeMethod(postMethod);
            if (result == org.apache.commons.httpclient.HttpStatus.SC_OK) {
                InputStream in = postMethod.getResponseBodyAsStream();
                return getResponseStr(in);
            } else {
                throw new Exception("HTTP ERROR Status: " + postMethod.getStatusCode() + ":" + postMethod.getStatusText());
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String doPut(String url, String json) {
        try {
            PutMethod putMethod = new PutMethod(url);
            // 定义HttpClient
            org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();

            String ticket = AdminUtils.getSSOTicket();
            putMethod.addRequestHeader(new org.apache.commons.httpclient.Header(TICKET_KEY, ticket));
            RequestEntity requestEntity = new StringRequestEntity(json);
            putMethod.setRequestEntity(requestEntity);

            int result = httpClient.executeMethod(putMethod);
            if (result == org.apache.commons.httpclient.HttpStatus.SC_OK) {
                InputStream in = putMethod.getResponseBodyAsStream();
                return getResponseStr(in);
            } else {
                throw new Exception("HTTP ERROR Status: " + putMethod.getStatusCode() + ":" + putMethod.getStatusText());
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
    /**
     * delete
     * @return
     */
    public static String doDelete(String url, Map<String,String> map) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpDelete request = new HttpDelete(url);
            Set<Map.Entry<String, String>> entries = map.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                request.setHeader(entry.getKey(),entry.getValue());
            }

            HttpResponse response = client.execute(request);

            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的json字符串数据**/
                String strResult = EntityUtils.toString(response.getEntity());

                return strResult;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
