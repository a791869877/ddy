package com.websocket.test.util;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

@Component
public class HttpClientUtil {

	/**
	 * get方式
	 * 
	 * @param purl 拼接好的带参数的地址
	 * @return
	 */
	public String getHttp(String purl) {
		String responseMsg = "";

		// 1.构造HttpClient的实例
		HttpClient httpClient = new HttpClient();
		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
		httpClient.getHttpConnectionManager().getParams().setSoTimeout(7000);
		// 用于测试的http接口的url
		String url = purl;
		// 2.创建GetMethod的实例
		GetMethod getMethod = new GetMethod(url);
		// 使用系统系统的默认的恢复策略
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler());
		try {
			// 3.执行getMethod,调用http接口
			httpClient.executeMethod(getMethod);
			// 4.读取内容
			byte[] responseBody = getMethod.getResponseBody();
			// 5.处理返回的内容
			responseMsg = new String(responseBody, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 6.释放连接
			getMethod.releaseConnection();
		}
		return responseMsg;
	}
	/**
	 * post方式
	 * 
	 * @param purl	请求的地址
	 * @param map	传输的数据
	 * @return
	 */
	public String postHttp(String purl, Map<String, String> map) {
		String responseMsg = "";
		// 1.构造HttpClient的实例
		HttpClient httpClient = new HttpClient();
		httpClient.getParams().setContentCharset("UTF-8");
		String url = purl;
		// 2.构造PostMethod的实例
		PostMethod postMethod = new PostMethod(url);
		// 3.把参数值放入到PostMethod对象中
		for (Entry<String, String> entry : map.entrySet()) {  
		    postMethod.addParameter(entry.getKey(), entry.getValue());
		}
		try {
			// 4.执行postMethod,调用http接口
			httpClient.executeMethod(postMethod);// 200
			// 5.读取内容
			responseMsg = postMethod.getResponseBodyAsString().trim();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 7.释放连接
			postMethod.releaseConnection();
		}
		return responseMsg;
	}
	
	/**
	 * post方式参数为json字符串
	 * 
	 * @param purl 请求地址
	 * @param json	body中的json数据
	 * @return
	 */
	public String postHttpJ(String purl, String json) {
		String responseMsg = "";
		
		// 1.构造HttpClient的实例
		HttpClient httpClient = new HttpClient();
		
		httpClient.getParams().setContentCharset("UTF-8");

		// 2.构造PostMethod的实例
		PostMethod postMethod = new PostMethod(purl);
		
		// 3.把参数值放入到PostMethod对象中
			
		postMethod.setRequestBody(json);
		postMethod.addRequestHeader("Content-Type", "application/json;charset=UTF-8");
			
		
		try {
			// 4.执行postMethod,调用http接口
			httpClient.executeMethod(postMethod);// 200
			
			// 5.读取内容
			responseMsg = IOUtils.toString(postMethod.getResponseBodyAsStream(), StandardCharsets.UTF_8).trim();
//			log.info(responseMsg);
			
			// 6.处理返回的内容
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 7.释放连接
			postMethod.releaseConnection();
		}
		return responseMsg;
	}

}