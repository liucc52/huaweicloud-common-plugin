package com.huawei.octopus.jobstatusplugin;

import com.google.gson.Gson;
import com.huawei.octopus.jobstatusplugin.entity.JobStatusEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobEventNotifier {
    private static final Logger LOGGER = Logger.getLogger(JobStatusPluginListener.class.getName());
    public static JobEventNotifier instance = new JobEventNotifier();
    public CloseableHttpAsyncClient httpClient;
    
    private Gson gson = new Gson();

    public static JobEventNotifier getInstance() {
        return instance;
    }

    public JobEventNotifier() {
        try {
            // 忽略证书验证
            TrustStrategy acceptingTrustStrategy = new TrustStrategy() {

                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            };
            SSLContext ctx = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();

            // 连接池配置
            IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                    .setIoThreadCount(200).setConnectTimeout(20000).build();

            SSLIOSessionStrategy sslstray = new SSLIOSessionStrategy(ctx, new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }

            });

            Registry<SchemeIOSessionStrategy> rg = RegistryBuilder.<SchemeIOSessionStrategy> create()
                    .register("http", NoopIOSessionStrategy.INSTANCE).register("https", sslstray).build();
            PoolingNHttpClientConnectionManager ccm = new PoolingNHttpClientConnectionManager(
                    new DefaultConnectingIOReactor(ioReactorConfig), rg);

            // 忽略主机验证
            httpClient = HttpAsyncClients.custom().setSSLContext(ctx).setConnectionManager(ccm).build();

            httpClient.start();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to setup https client", e);

        }
    }

    class NotifyFutureCallBack implements FutureCallback<HttpResponse> {
        private JobStatusEntity jobStatusEntity;
        
        public NotifyFutureCallBack(JobStatusEntity jobStatusEntity) {
            super();
            this.jobStatusEntity=jobStatusEntity;
        }

        @Override
        public void completed(HttpResponse httpResponse) {
            HttpEntity entity = httpResponse.getEntity();
            String res = null;
            try {
                res = EntityUtils.toString(entity);
                LOGGER.info(String.format("notify res: %s, jobId: %s", res, jobStatusEntity.getJobId()));
            } catch (IOException ioe) {
                LOGGER.info(String.format("notify error: %s, jobId: %s", ioe.getMessage(), jobStatusEntity.getJobId()));
            }
            HttpClientUtils.closeQuietly(httpResponse);
        }

        @Override
        public void failed(Exception ex) {
            // TODO Auto-generated method stub

        }

        @Override
        public void cancelled() {
            // TODO Auto-generated method stub

        }

    }

    public boolean sendNotify(String url, JobStatusEntity jobStatusEntity) {
        HttpResponse httpResponse = null;
        String json = gson.toJson(jobStatusEntity);
        LOGGER.info("url: " + url + ", data: " + json);
        try {
            HttpPost req = new HttpPost(url);
            HttpEntity body = new StringEntity(json, ContentType.APPLICATION_JSON);
            req.setHeader("Content-Type", "application/json;charset=UTF-8");
            req.setHeader("Accept", "application/json;charset=UTF-8");

            req.setEntity(body);
            if (null == httpClient) {
                LOGGER.severe("httpClient is null");
                return false;
            }
            httpClient.execute(req, new NotifyFutureCallBack(jobStatusEntity));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send notify as: ", e);
            return false;
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
        }
        return true;
    }
}
