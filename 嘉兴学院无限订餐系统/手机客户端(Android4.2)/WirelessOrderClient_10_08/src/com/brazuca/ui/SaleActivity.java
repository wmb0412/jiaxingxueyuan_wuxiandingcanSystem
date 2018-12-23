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
	protected final  int GET_FROM_NET_OK = 0x001;// ��Ϣ:ע��ɹ�
	protected final  int NET_EXCEPTION = 0x002;// ��Ϣ:�����쳣
	protected final  int GET_FAILED = 0x003;// ��Ϣ��ע��ʧ��
	protected final  int SERVER_EXCEPTION = 0x004;// ��Ϣ:�������쳣
	private ControllersUtil controllersUtil;
	
	private Context mContext;
	private ListView lvSale;
	private HomeSaleActivityAdapter adapter;
	private AlertDialog dlg;
	private TextView tvCheckTable;  //ѡ��
	
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
		
		controllersUtil.showProgressWindow(dlg, "���ڻ�ȡ��Ϣ");
		mSaleThread = new ThreadGetSale();
		mSaleThread.start();
	}
	
	@Override
	protected void onResume() {
//		System.out.println("SaleActivity|onResume");
//		
//		if(listitemSale.size()<1)
//		{
//			controllersUtil.showProgressWindow(dlg, "���ڻ�ȡ��Ϣ");
//			mSaleThread = new ThreadGetSale();
//			mSaleThread.start();
//		}
		
		super.onResume();
	}
	

	private void initViews()
	{
		lvSale = (ListView)findViewById(R.id.lv_sale);	
		tvCheckTable = (TextView) this.findViewById(R.id.tv_check_table); //ѡ��
	}

	private void setListener()
	{
		//ѡ��
		tvCheckTable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// ���δ��⵽�û���¼������ת����¼����
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
				
				if (result.equals("fail")) // ע��ʧ��
				{
					mHandler.obtainMessage(GET_FAILED).sendToTarget();// ������Ϣ��Handler
				}

				else if (result.equals("�������쳣")) // �������쳣
				{
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
				}
				else
				{
					mHandler.obtainMessage(GET_FROM_NET_OK, result).sendToTarget();// ������Ϣ��Handler
				}
				
			}
			catch (Exception e) {
				mSaleThread = null;
			}
			
			mSaleThread = null;
		
		}
	}
	//���߳�UI��Handler����
	Handler	mHandler = new Handler(){
		public void handleMessage(Message msg) {
			if(dlg.isShowing())
				controllersUtil.hideProgressWindow(dlg);
			
			switch (msg.what) {
			
			case GET_FROM_NET_OK:   //����ɹ�
				
				String strJson = "";
				strJson = (String)(msg.obj);
				
				if(listitemSale.size()<1)
					adapter = new HomeSaleActivityAdapter(mContext, createDishData(strJson));
				else
					adapter = new HomeSaleActivityAdapter(mContext, listitemSale);
						
				lvSale.setAdapter(adapter);
				
				break;

			case NET_EXCEPTION:  //�����쳣

				controllersUtil.showToast(mContext, getString(R.string.network_exception), Toast.LENGTH_SHORT);
				//startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));//���������������ý���
				break; 
			case SERVER_EXCEPTION:   //�������쳣
			
				controllersUtil.showToast(mContext, getString(R.string.server_exception), Toast.LENGTH_SHORT);
				break;
			case GET_FAILED:   //����ʧ��
				
				controllersUtil.showToast(mContext, getString(R.string.login_fail), Toast.LENGTH_SHORT);
				break;
			}
			
			 super.handleMessage(msg);  
		}
	};
	
	private String getSaleList() {
		// url
		String url = HttpUtil.BASE_URL + "GetSaleListServlet?";
		
		// ��ѯ���ؽ��
		return HttpUtil.queryStringForPost(url);
	}
	
}
