package com.example.springcloud.util;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

public class HttpClientUtil {

    private HttpClientUtil() {

    }

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    public static String sendGet(String url, Map<String, String> params) {
        return sendGet(url, params, null);
    }

    public static String sendGest(String url, Map<String, String> params) {
        return sendGet(url, params, new HashMap<>());
    }

    /**
     * HTTP GET 请求
     *
     * @param url       请求地址
     * @param params    请求参数 Map
     * @param headerMap 请求头
     * @return 请求响应
     */
    public static String sendGet(String url, Map<String, String> params, Map<String, String> headerMap) {
        String result = "";
        BufferedReader in = null;
        InputStream inputStream = null;
        try {
            String lastUrl = "";
            if (url != null) {
                lastUrl = url.trim();
            }
            URIBuilder uri = new URIBuilder(lastUrl);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uri.addParameter(entry.getKey(), entry.getValue());
            }
            URL realUrl = new URL(uri.build().toString());
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            if (headerMap != null && headerMap.size() > 0) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            // 设置通用的请求属性
            conn.connect();
            // 定义BufferedReader输入流来读取URL的响应
            inputStream = conn.getInputStream();
            if (inputStream.available() > 0x640000) {
                throw new BackingStoreException("文件过大");
            }
            in = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            final int size = 1024;
            final int mark = -1;
            char[] chs = new char[size];
            int len = 0;
            while ((len = in.read(chs)) != mark) {
                sb.append(new String(chs, 0, len));
            }
            result = sb.toString();
            logger.debug("返回信息：" + result);
        } catch (Exception e) {
            logger.error("发送GET请求出现异常！" + e);
        } finally {
            // 使用finally块来关闭输入流
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                logger.error("IOException:", ex);
            }
        }
        return result;
    }
    public static String httpPostWhthJson(String jsonString, String url, String appId) {
        boolean isSuccess = false;
        HttpPost post = null;
        HttpResponse response = null;
        String r = "";
        try {
            SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(SSLContexts.custom().loadTrustMaterial(null,
                    new TrustSelfSignedStrategy()).build(),
                    NoopHostnameVerifier.INSTANCE);
            HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(scsf).build();
            post = new HttpPost(url);
            //构造消息头
            post.setHeader("Content-Type", "application/json;charset=utf-8");
            post.setHeader("Connection", "Close");
            String sessionId = getSessionId();
            post.setHeader("SessionId", sessionId);
            post.setHeader("appid", appId);
            //构建消息实体
            StringEntity entity = new StringEntity(jsonString, Charset.forName("UTF-8"));
            entity.setContentEncoding("UTF-8");
            //发送json格式请求数据
            post.setEntity(entity);
            response = httpClient.execute(post);
            //校验返回码
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 302) {
                Header header = response.getFirstHeader("location"); // 跳转的目标地址是在 HTTP-HEAD 中的
                String newuri = header.getValue(); // 这就是跳转后的地址，再向这个地址发出新申请，以便得到跳转后的信息是啥。
                logger.info("newuri" + newuri);
                logger.info("statusCode" + statusCode);

                post = new HttpPost(newuri);
                post.setHeader("Content-Type", "application/json;charset=UTF-8");

                entity = new StringEntity(jsonString, Charset.forName("UTF-8"));
                entity.setContentType("text/json");

                post.setEntity(entity);

                response = httpClient.execute(post);
                statusCode = response.getStatusLine().getStatusCode();
                logger.info("statusCode" + statusCode);
                int retCode = 0;
                String sessendnId = "";
                //返回码中含有retCode和会话Id
                isSuccess = isSuccess(response);
            } else if (statusCode != HttpStatus.SC_OK) {
                logger.info("请求出错：" + statusCode);
                isSuccess = false;
            } else {
                isSuccess = isSuccess(response);
            }
            if (isSuccess) {
                r = EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            isSuccess = false;
        } finally {
            if (post != null) {
                try {
                    post.releaseConnection();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.info(e.getMessage());
                }
            }
        }

        return r;
    }
    private static boolean isSuccess(HttpResponse response) {
        boolean isSuccess;
        int retCode = 0;
        String sessendnId = "";
        //返回码中含有retCode和会话Id
        for (Header header : response.getAllHeaders()) {
            if ("retCode".equals(header.getName())) {
                retCode = Integer.valueOf(header.getValue());
            }
            if ("SessionId".equals(header.getName())) {
                sessendnId = header.getValue();
            }
        }
        if (retCode != 0) {
            logger.info("error return code,sessionId:" + sessendnId + ";\tretCode:" + retCode);
            isSuccess = false;
        } else {
            isSuccess = true;
        }
        return isSuccess;
    }

    private static String getSessionId() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }
}
