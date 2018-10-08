package com.huawei.octopus.jobstatusplugin.util;

import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/*import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;*/

public class HttpClientUtil {

    private static final int timeOut = 20 * 1000;
    private static final int keepAlivedTimeout = 0 ;
    private static final int retryCount = 3;
    private static final int maxTotal = 200;
    private static final int maxPerRoute = 100;

    private static CloseableHttpClient httpClient = null;
    private static final Logger LOGGER = Logger.getLogger(HttpClientUtil.class.getName());

    static{
    	try{
    		httpClient = createHttpClient(maxTotal, maxPerRoute);
    	}catch(IOException e){
    		LOGGER.log(Level.SEVERE, "create http client failed.", e);
    	}
    }
    
    public static CloseableHttpResponse doGet(String url) throws IOException{
    	return doGet(url, null);
    }
    
    public static CloseableHttpResponse doGet(String url, Map<String,String> headers) throws IOException{
    	return doGet(url, headers, HttpClientContext.create());
    }
    
    public static CloseableHttpResponse doGet(String url, Map<String, String> headers, 
    		HttpClientContext context)throws IOException{
    	String formUrl = Normalizer.normalize(url, Form.NFKC);
    	LOGGER.log(Level.INFO, "do Get to: "+formUrl);
    	return doExecuteHttpRequest(configRequestBase(new HttpGet(formUrl), headers, null), context);
    }
    
    public static CloseableHttpResponse doPost(String url, Map<String,String> headers, HttpEntity entity) throws IOException{
    	return doPost(url, headers, entity, HttpClientContext.create());
    }
    
    public static CloseableHttpResponse doPost(String url, Map<String,String> headers, HttpEntity entity, 
    		HttpClientContext context) throws IOException{
    	//String formUrl = Normalizer.normalize(url, Form.NFKC);
    	LOGGER.log(Level.INFO, "doPost");
    	return doExecuteHttpRequest(configRequestBase(new HttpPost(url), headers, entity), context);
    }
    
    public static String consumeResponse(CloseableHttpResponse response) throws IOException{
    	 try {
             HttpEntity entity = response.getEntity();
             String result = EntityUtils.toString(entity, "utf-8");
             EntityUtils.consume(entity);
             return Normalizer.normalize(result, Form.NFKC);
         } finally {
             HttpClientUtils.closeQuietly(response);
         }
    }
    

    public static HttpClientContext createBasicAuthContext(String url, String username, String password){
    	HttpRequestBase request = new HttpGet(url);
    	URI uri = request.getURI();
		HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
    	CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(new AuthScope(host),
				new UsernamePasswordCredentials(username, password));
		AuthCache authCache = new BasicAuthCache();
		authCache.put(host, new BasicScheme());
		HttpClientContext ctx = HttpClientContext.create();
		ctx.setAuthCache(authCache);
		ctx.setCredentialsProvider(credentialsProvider);
		return ctx;
    }

    private static HttpRequestBase configRequestBase(HttpRequestBase requestBase, Map<String, String> headers,
                                                     HttpEntity entity) {
    	configTimeout(requestBase);
    	setHeaders(requestBase, headers);
    	if(entity != null){
    		((HttpPost)requestBase).setEntity(entity);
    	}
    	return requestBase;
    }
    
    private static void configTimeout(HttpRequestBase httpRequestBase) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(timeOut)	//此超时表示连接池满了后新请求等待空闲连接的时间
                .setConnectTimeout(timeOut)
                .setSocketTimeout(timeOut)			//设置socket读取超时时间，解决请求卡死在socketRead0方法
                .setCircularRedirectsAllowed(true)	// 解决CircularRedirectException
                .build();
        httpRequestBase.setConfig(requestConfig);
    }
    
    private static void setHeaders(HttpRequestBase httpRequestBase, Map<String,String> headers){
//    	httpRequestBase.setHeader("domain_tag", DevCloudTokenStore.getDomainName());
    	if(headers != null){
    		for(Entry<String,String> entry : headers.entrySet()){
    			httpRequestBase.setHeader(Normalizer.normalize(entry.getKey(), Form.NFKC), 
    					Normalizer.normalize(entry.getValue(), Form.NFKC));
    		}
    	}
    }

    
    private static CloseableHttpResponse doExecuteHttpRequest(HttpRequestBase request, HttpClientContext context) throws IOException{
    	request.setHeader("Content-Type", "application/json");
    	return getHttpClient().execute(request, context);
    }
    
    private static CloseableHttpClient getHttpClient(){
        return httpClient;
    }

    /**
     * 创建Http连接池
     * @param maxTotal	最大连接数		
     * @param maxPerRoute	每个Host的最大连接数
     * @return
     * @throws IOException  创建失败抛出此异常
     */
    private static CloseableHttpClient createHttpClient(int maxTotal, int maxPerRoute) throws IOException {
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslsf = null;
        try{ 
        	sslsf = new SSLConnectionSocketFactory(
    				SSLContextBuilder.create()
    				.loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build(),
    		NoopHostnameVerifier.INSTANCE);
        }catch(NoSuchAlgorithmException | KeyStoreException | KeyManagementException e){
        	throw new IOException("create SSLConnectionSocketFactory failed.", e);
        }
        
        ConnectionKeepAliveStrategy connectionKeepAliveStrategy = new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
            	long ret = keepAlivedTimeout;
            	HeaderIterator it = httpResponse.headerIterator(HTTP.CONN_KEEP_ALIVE);
                while (it.hasNext()) {
                	Header h = it.nextHeader();
                    String param = h.getName();
                    String value = h.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        try {
                        	long timeout = Long.parseLong(value);
                        	if(timeout > 0){
                        		ret = timeout * 1000;
                        	}
                        	break;
                        } catch(NumberFormatException ignore) {
                        	LOGGER.log(Level.SEVERE, ignore.getMessage());
                        }
                    }
                }
                return ret;
            }
        };
        Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory> create().register("http", plainsf)
                .register("https", sslsf).build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager( registry);
        cm.setMaxTotal(maxTotal);
        cm.setDefaultMaxPerRoute(maxPerRoute);

        // 请求重试处理
        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
            public boolean retryRequest(IOException exception,
                    int executionCount, HttpContext context) {
                if (executionCount >= retryCount) {// 如果已经重试了指定次数，就放弃
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                    return false;
                }
                if (exception instanceof InterruptedIOException) {// 超时
                    return false;
                }
                if (exception instanceof UnknownHostException) {// 目标服务器不可达
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                    return false;
                }
                if (exception instanceof SSLException) {// SSL握手异常
                    return false;
                }
                if (exception instanceof HttpHostConnectException){	//连不上目标主机
                	return false;
                }

                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                // 如果请求是幂等的，就再次尝试
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    return true;
                }
                return false;
            }
        };
        return HttpClients.custom()
                .setConnectionManager(cm)
                .setRetryHandler(httpRequestRetryHandler)
                .setKeepAliveStrategy(connectionKeepAliveStrategy)	//增加keepalived策略，避免大部分NoHttpResponseException
                .build();
    }
}
