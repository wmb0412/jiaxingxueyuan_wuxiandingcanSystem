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
	
	public static int screenHeight = 0;// 屏幕高度(pixel)
	public static int screenWidth = 0;// 屏幕宽度(pixel)
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
	 * 获取BrazucaApplication类的单例模式的实例
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
		//安装程序时，建数据库表
		BrazucaDBUtil DBUtil = new BrazucaDBUtil(getBaseContext());
		DBUtil.initDataTable();
		DBUtil.close();  //关闭数据库
		
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
