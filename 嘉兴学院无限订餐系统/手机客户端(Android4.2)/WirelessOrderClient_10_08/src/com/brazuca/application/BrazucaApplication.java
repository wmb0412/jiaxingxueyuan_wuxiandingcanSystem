package com.brazuca.application;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Environment;
import android.util.Log;

import com.brazuca.db.BrazucaDBGetInfoUtil;
import com.brazuca.db.BrazucaDBUtil;
import com.brazuca.entity.UserParam;
import com.brazuca.util.ControllersUtil;

public class BrazucaApplication extends Application{
	
	public static int screenHeight = 0;// ��Ļ�߶�(pixel)
	public static int screenWidth = 0;// ��Ļ���(pixel)
	private static BrazucaApplication instance = null;
	private static ControllersUtil controllersUtil=null;
	
	public BrazucaApplication()
	{
		File f = new File(Environment.getExternalStorageDirectory()+"/Brazuca/");
		if(!f.exists()){
			f.mkdir();
		}
	}
	
	public BrazucaApplication(Context context)
	{

	}
	
	/*
	 * ��ȡBrazucaApplication��ĵ���ģʽ��ʵ��
	 */
	public static BrazucaApplication getInstance()
	{
		if(instance == null)
		{
			instance = new BrazucaApplication();
		}
		return instance;
	}
	
	
	public static ControllersUtil getCUInstance()
	{
		if(controllersUtil == null)
		{
			controllersUtil = new ControllersUtil();
		}
		return controllersUtil;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		System.out.println("ApponCreate");
		//��װ����ʱ�������ݿ��
		BrazucaDBUtil DBUtil = new BrazucaDBUtil(getBaseContext());
		DBUtil.initDataTable();
		DBUtil.close();  //�ر����ݿ�
		
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.d("ApponTerminate","onTerminate");
	}
	
}
