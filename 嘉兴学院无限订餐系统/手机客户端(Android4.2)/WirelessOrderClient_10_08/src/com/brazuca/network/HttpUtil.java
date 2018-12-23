package com.brazuca.network;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HttpUtil {
	// 基础URL：192.168.1.101：本机ip地址 10.0.2.2 192.168.107.54 192.168.1.108
	public static final String BASE_URL = "http://10.0.2.2/DiningRoomOnLine/";

	// 获得Get请求对象request
	public static HttpGet getHttpGet(String url) {
		HttpGet request = new HttpGet(url);
		return request;
	}

	// 获得Post请求对象request
	public static HttpPost getHttpPost(String url) {
		HttpPost request = new HttpPost(url);
		return request;
	}

	// 根据请求获得响应对象response
	public static HttpResponse getHttpResponse(HttpGet request)
			throws ClientProtocolException, IOException {
		HttpResponse response = new DefaultHttpClient().execute(request);

		// response.getParams().setc; // 超时设置
		// response.getParams().setIntParameter(
		// HttpConnectionParams.CONNECTION_TIMEOUT, 10000);// 连接超时
		return response;
	}

	// 根据请求获得响应对象response
	public static HttpResponse getHttpResponse(HttpPost request)
			throws ClientProtocolException, IOException {
		HttpResponse response = new DefaultHttpClient().execute(request);
		return response;
	}

	// 发送Post请求，获得响应查询结果
	public static String queryStringForPost(String url) {
		// 根据url获得HttpPost对象
		HttpPost request = HttpUtil.getHttpPost(url);
		Log.d("request", request + "");
		String result = null;
		try {
			// 获得响应对象
			HttpResponse response = HttpUtil.getHttpResponse(request);

			// 判断是否请求成功

			if (response.getStatusLine().getStatusCode() == 200) {
				// 获得响应
				result = EntityUtils.toString(response.getEntity());
				Log.d("result", result);
				return result;
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			result = "服务器异常";
			return result;
		}
		return null;
	}

	// 发送Post请求，获得响应查询结果
	// public static String GetListAllForPost(String url){
	// // 根据url获得HttpPost对象
	// HttpPost request = HttpUtil.getHttpPost(url);
	// JSONArray jsArrayResult = null;
	// try {
	// // 获得响应对象
	// HttpResponse response = HttpUtil.getHttpResponse(request);
	// // 判断是否请求成功
	// if(response.getStatusLine().getStatusCode()==200){
	//
	// // 获得响应
	// jsArrayResult = EntityUtils.toString(response.getEntity().toString()) ;
	// Log.d("result",result+"");
	// return result;
	// }
	// } catch (Exception e) {
	// System.err.println(e.getMessage());
	// result = "服务器异常";
	// return result;
	// }
	// return null;
	// }

	// 获得响应查询结果
	public static String queryStringForPost(HttpPost request) {
		String result = null;
		try {
			// 获得响应对象
			HttpResponse response = HttpUtil.getHttpResponse(request);
			// 判断是否请求成功
			if (response.getStatusLine().getStatusCode() == 200) {
				// 获得响应
				result = EntityUtils.toString(response.getEntity());
				return result;
			}
		}
		catch (ClientProtocolException e) {
			e.printStackTrace();
			result = "网络异常！";
			return result;
		}
		catch (IOException e) {
			e.printStackTrace();
			result = "网络异常！";
			return result;
		}
		return null;
	}

	// 发送Get请求，获得响应查询结果
	public static String queryStringForGet(String url) {
		// 获得HttpGet对象
		HttpGet request = HttpUtil.getHttpGet(url);
		String result = null;
		try {
			// 获得响应对象
			HttpResponse response = HttpUtil.getHttpResponse(request);
			// 判断是否请求成功
			if (response.getStatusLine().getStatusCode() == 200) {
				// 获得响应
				result = EntityUtils.toString(response.getEntity());
				return result;
			}
		}
		catch (ClientProtocolException e) {
			e.printStackTrace();
			result = "网络异常！";
			return result;
		}
		catch (IOException e) {
			e.printStackTrace();
			result = "网络异常！";
			return result;
		}
		return null;
	}
}
