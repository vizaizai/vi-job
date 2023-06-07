package com.github.vizaizai.worker.utils;

import com.github.vizaizai.logging.LoggerFactory;
import org.slf4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * HTTP工具类
 * @author liaochongwei
 * @date 2023/5/9 16:43
 */
public class HttpUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    private static final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    }};


    /**
     * POST请求
     * @param httpUrl url
     * @param body 请求体
     * @return 响应内容
     */
    public static String doPost(String httpUrl, Object body, long timeout) {
        final URL url;
        final HttpURLConnection connection;
        try {
            url = new URL(httpUrl);
            connection = (HttpURLConnection) url.openConnection();
            // ssl
            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection sslConnection = (HttpsURLConnection) connection;
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                sslConnection.setSSLSocketFactory(sc.getSocketFactory());
                sslConnection.setHostnameVerifier((hostname, session) -> true);
            }

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout((int) timeout);
            connection.setReadTimeout((int) timeout);
            connection.setAllowUserInteraction(false);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setChunkedStreamingMode(8192);

            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.connect();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));
            writer.write(JSONUtils.toJSONString(body));
            writer.close();

            StringBuilder result = new StringBuilder();
            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    result.append(line);
                }
            }else {
                logger.error("Http status code error,code: {}, message: {}",responseCode,connection.getResponseMessage());
            }
            return result.toString();
        }catch (Exception e) {
            logger.error("Http request error: {}", e.getMessage());
        }
        return null;
    }
}
