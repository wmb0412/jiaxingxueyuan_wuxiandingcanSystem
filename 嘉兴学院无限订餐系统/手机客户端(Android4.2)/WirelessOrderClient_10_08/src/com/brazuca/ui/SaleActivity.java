package com.brazuca.ui;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.brazuca.entity.HomeSaleActivityEntity;
import com.brazuca.entity.UserParam;
import com.brazuca.network.HttpUtil;
import com.brazuca.ui.adapter.HomeSaleActivityAdapter;
import com.brazuca.util.ControllersUtil;

public class SaleActivity extends Activity{
	protected final  int GET_FROM_NET_OK = 0x001;// 消息:注册成功
	protected final  int NET_EXCEPTION = 0x002;// 消息:网络异常
	protected final  int GET_FAILED = 0x003;// 消息：注册失败
	protected final  int SERVER_EXCEPTION = 0x004;// 消息:服务器异常
	private ControllersUtil controllersUtil;
	
	private Context mContext;
	private ListView lvSale;
	private HomeSaleActivityAdapter adapter;
	private AlertDialog dlg;
	private TextView tvCheckTable;  //选桌
	
	private ThreadGetSale mSaleThread;
	
	ArrayList<HomeSaleActivityEntity> listitemSale;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sale);
		mContext = SaleActivity.this;
		dlg = new AlertDialog.Builder(mContext).create();
		
		controllersUtil = new ControllersUtil();

		listitemSale = new ArrayList<HomeSaleActivityEntity>();
		initViews();
		setListener();
		
		controllersUtil.showProgressWindow(dlg, "正在获取信息");
		mSaleThread = new ThreadGetSale();
		mSaleThread.start();
	}
	
	@Override
	protected void onResume() {
//		System.out.println("SaleActivity|onResume");
//		
//		if(listitemSale.size()<1)
//		{
//			controllersUtil.showProgressWindow(dlg, "正在获取信息");
//			mSaleThread = new ThreadGetSale();
//			mSaleThread.start();
//		}
		
		super.onResume();
	}
	

	private void initViews()
	{
		lvSale = (ListView)findViewById(R.id.lv_sale);	
		tvCheckTable = (TextView) this.findViewById(R.id.tv_check_table); //选桌
	}

	private void setListener()
	{
		//选桌
		tvCheckTable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// 如果未检测到用户登录，则跳转到登录界面
				if (!UserParam.isLogin) {
					startActivity(new Intent(SaleActivity.this, LoginActivity.class));
				}
				else
					startActivity(new Intent(SaleActivity.this, TableActivity.class));
				
			}
		});
	}
	
	private ArrayList<HomeSaleActivityEntity> createDishData(String obj)
	{
		try {
			JSONArray jsArray = new JSONArray(obj);
			HomeSaleActivityEntity entity = null;
			
			for (int i = 0; i < jsArray.length(); i++) {
				entity = new HomeSaleActivityEntity();
				JSONObject js = new JSONObject();
				js = jsArray.getJSONObject(i);

				String saleContent = js.getString("sale_content");
				String startTime = js.getString("starttime");
				String endTime = js.getString("endtime");
				
				entity.setSaleContent(saleContent);
				entity.setStartTime(startTime);
				entity.setEndTime(endTime);
				
				listitemSale.add(entity);
			}

		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listitemSale;

	}
	private class ThreadGetSale extends Thread
	{
		@Override
		public void run() {
			try {
				
				String result = getSaleList();
				
				if (result.equals("fail")) // 注册失败
				{
					mHandler.obtainMessage(GET_FAILED).sendToTarget();// 发送消息到Handler
				}

				else if (result.equals("服务器异常")) // 服务器异常
				{
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// 发送消息到Handler
				}
				else
				{
					mHandler.obtainMessage(GET_FROM_NET_OK, result).sendToTarget();// 发送消息到Handler
				}
				
			}
			catch (Exception e) {
				mSaleThread = null;
			}
			
			mSaleThread = null;
		
		}
	}
	//主线程UI的Handler处理
	Handler	mHandler = new Handler(){
		public void handleMessage(Message msg) {
			if(dlg.isShowing())
				controllersUtil.hideProgressWindow(dlg);
			
			switch (msg.what) {
			
			case GET_FROM_NET_OK:   //请求成功
				
				String strJson = "";
				strJson = (String)(msg.obj);
				
				if(listitemSale.size()<1)
					adapter = new HomeSaleActivityAdapter(mContext, createDishData(strJson));
				else
					adapter = new HomeSaleActivityAdapter(mContext, listitemSale);
						
				lvSale.setAdapter(adapter);
				
				break;

			case NET_EXCEPTION:  //网络异常

				controllersUtil.showToast(mContext, getString(R.string.network_exception), Toast.LENGTH_SHORT);
				//startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));//进入无线网络配置界面
				break; 
			case SERVER_EXCEPTION:   //服务器异常
			
				controllersUtil.showToast(mContext, getString(R.string.server_exception), Toast.LENGTH_SHORT);
				break;
			case GET_FAILED:   //请求失败
				
				controllersUtil.showToast(mContext, getString(R.string.login_fail), Toast.LENGTH_SHORT);
				break;
			}
			
			 super.handleMessage(msg);  
		}
	};
	
	private String getSaleList() {
		// url
		String url = HttpUtil.BASE_URL + "GetSaleListServlet?";
		
		// 查询返回结果
		return HttpUtil.queryStringForPost(url);
	}
	
}
