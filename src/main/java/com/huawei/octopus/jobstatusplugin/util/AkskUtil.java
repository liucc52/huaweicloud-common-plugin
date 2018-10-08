package com.huawei.octopus.jobstatusplugin.util;

import com.apimgt.sdk.DefaultRequest;
import com.apimgt.sdk.Request;
import com.apimgt.sdk.auth.credentials.BasicCredentials;
import com.apimgt.sdk.auth.signer.Signer;
import com.apimgt.sdk.http.HttpMethodName;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AkskUtil {
	
	private static final Logger LOGGER = Logger.getLogger(AkskUtil.class.getName());
	
	public static Map<String, String> getAkskHeaders(String ak,
			String sk,
			String requestUrl,
			HttpMethodName method,
			String data,
			Map<String, String> headers)
	{
		Map<String, String> requestHeaders = null;
	    try {
	    	String serviceName = "serviceName";
	    	String regionName = "regionName";
	    	Request request = new DefaultRequest(serviceName);
	    	URL url = new URL(requestUrl);
	    	request.setEndpoint(url.toURI());
	    	request.setHttpMethod(method);
	    	if(data != null && !data.isEmpty())
	    	{
	    		InputStream content = new ByteArrayInputStream(data.getBytes("UTF-8"));
	    		request.setContent(content);
	    	}
	    	String parameters = null;
	        if (requestUrl.contains("?")) {
	            parameters = requestUrl.substring(requestUrl.indexOf("?") + 1);
	            Map parametersmap = new HashMap<String, String>();
	            if (null != parameters && !"".equals(parameters)) {
	                        String[] parameterarray = parameters.split("&");
	                        for (String p : parameterarray) {
	                            String key = p.split("=")[0];
	                            String value = p.split("=")[1];
	                            parametersmap.put(key, value);
	                        }
	                        request.setParameters(parametersmap);
	              }
	        }
	        if (headers == null) {
	        	headers = new HashMap<String, String>();
	        }
	        headers.put("sk", sk);
	        request.setHeaders(headers);
      	    Signer signer = SignerFactory.getSigner(serviceName, regionName);
      	    signer.sign(request, new BasicCredentials(ak, sk));
      	    requestHeaders = request.getHeaders();
      	    return requestHeaders;
		} catch (Throwable t) {
			LOGGER.log(Level.SEVERE, "getAkskHeaders error", t);
			return null;
		}
	}

}
