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
	// ����URL��192.168.1.101������ip��ַ 10.0.2.2 192.168.107.54 192.168.1.108
	public static final String BASE_URL = "http://10.0.2.2/DiningRoomOnLine/";

	// ���Get�������request
	public static HttpGet getHttpGet(String url) {
		HttpGet request = new HttpGet(url);
		return request;
	}

	// ���Post�������request
	public static HttpPost getHttpPost(String url) {
		HttpPost request = new HttpPost(url);
		return request;
	}

	// ������������Ӧ����response
	public static HttpResponse getHttpResponse(HttpGet request)
			throws ClientProtocolException, IOException {
		HttpResponse response = new DefaultHttpClient().execute(request);

		// response.getParams().setc; // ��ʱ����
		// response.getParams().setIntParameter(
		// HttpConnectionParams.CONNECTION_TIMEOUT, 10000);// ���ӳ�ʱ
		return response;
	}

	// ������������Ӧ����response
	public static HttpResponse getHttpResponse(HttpPost request)
			throws ClientProtocolException, IOException {
		HttpResponse response = new DefaultHttpClient().execute(request);
		return response;
	}

	// ����Post���󣬻����Ӧ��ѯ���
	public static String queryStringForPost(String url) {
		// ����url���HttpPost����
		HttpPost request = HttpUtil.getHttpPost(url);
		Log.d("request", request + "");
		String result = null;
		try {
			// �����Ӧ����
			HttpResponse response = HttpUtil.getHttpResponse(request);

			// �ж��Ƿ�����ɹ�

			if (response.getStatusLine().getStatusCode() == 200) {
				// �����Ӧ
				result = EntityUtils.toString(response.getEntity());
				Log.d("result", result);
				return result;
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			result = "�������쳣";
			return result;
		}
		return null;
	}

	// ����Post���󣬻����Ӧ��ѯ���
	// public static String GetListAllForPost(String url){
	// // ����url���HttpPost����
	// HttpPost request = HttpUtil.getHttpPost(url);
	// JSONArray jsArrayResult = null;
	// try {
	// // �����Ӧ����
	// HttpResponse response = HttpUtil.getHttpResponse(request);
	// // �ж��Ƿ�����ɹ�
	// if(response.getStatusLine().getStatusCode()==200){
	//
	// // �����Ӧ
	// jsArrayResult = EntityUtils.toString(response.getEntity().toString()) ;
	// Log.d("result",result+"");
	// return result;
	// }
	// } catch (Exception e) {
	// System.err.println(e.getMessage());
	// result = "�������쳣";
	// return result;
	// }
	// return null;
	// }

	// �����Ӧ��ѯ���
	public static String queryStringForPost(HttpPost request) {
		String result = null;
		try {
			// �����Ӧ����
			HttpResponse response = HttpUtil.getHttpResponse(request);
			// �ж��Ƿ�����ɹ�
			if (response.getStatusLine().getStatusCode() == 200) {
				// �����Ӧ
				result = EntityUtils.toString(response.getEntity());
				return result;
			}
		}
		catch (ClientProtocolException e) {
			e.printStackTrace();
			result = "�����쳣��";
			return result;
		}
		catch (IOException e) {
			e.printStackTrace();
			result = "�����쳣��";
			return result;
		}
		return null;
	}

	// ����Get���󣬻����Ӧ��ѯ���
	public static String queryStringForGet(String url) {
		// ���HttpGet����
		HttpGet request = HttpUtil.getHttpGet(url);
		String result = null;
		try {
			// �����Ӧ����
			HttpResponse response = HttpUtil.getHttpResponse(request);
			// �ж��Ƿ�����ɹ�
			if (response.getStatusLine().getStatusCode() == 200) {
				// �����Ӧ
				result = EntityUtils.toString(response.getEntity());
				return result;
			}
		}
		catch (ClientProtocolException e) {
			e.printStackTrace();
			result = "�����쳣��";
			return result;
		}
		catch (IOException e) {
			e.printStackTrace();
			result = "�����쳣��";
			return result;
		}
		return null;
	}
}
