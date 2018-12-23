package com.brazuca.ui;

import org.json.JSONException;
import org.json.JSONObject;

import com.brazuca.entity.UserParam;
import com.brazuca.network.HttpUtil;
import com.brazuca.util.ControllersUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class WelcomeActivity extends Activity {
	protected final  int NET_OK = 0x001;// ��Ϣ:��ȡ�ɹ�
	protected final  int NET_EXCEPTION = 0x002;// ��Ϣ:�����쳣
	protected final  int NET_FAILED = 0x003;// ��Ϣ����ȡʧ��
	protected final  int SERVER_EXCEPTION = 0x004;// ��Ϣ:�������쳣
	private Context mContext;

	private Button btnWelcomeLogin;
	private Button btnWelcomeRegister;
	private Button btnWelcomeScan;
	private LinearLayout llUserZone;
	private TextView tvUserCount;
	
	private static int screenHeight = 0;// ��Ļ�߶�(pixel)
	private static int screenWidth = 0;// ��Ļ���(pixel)
	private static int statusBarHeight = 0;
	private ControllersUtil controllersUtil;
	
	private ThreadGetUserInfo mGetUserInfoThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		mContext = WelcomeActivity.this;
		controllersUtil = new ControllersUtil();

		initViews();
		setListner();
		
		initWindowParams(this);
		
		mGetUserInfoThread = new ThreadGetUserInfo();
		mGetUserInfoThread.start();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	// ��ʼ���ؼ�
	private void initViews() {

		btnWelcomeLogin = (Button) findViewById(R.id.btn_welcome_login);
		btnWelcomeRegister = (Button) findViewById(R.id.btn_welcome_register); // ע��
		btnWelcomeScan = (Button) findViewById(R.id.btn_welcome_scan); // ע��
		
		llUserZone = (LinearLayout)findViewById(R.id.ll_welcome_login_or_register_zone);
		
		tvUserCount = (TextView)findViewById(R.id.tv_users_count);  ///�û���
	}
	
	//�����û���
	private void setUserCount(String obj)
	{
		JSONObject jsObject;
		try {
			jsObject = new JSONObject(obj);
			int count = jsObject.getInt("user_count");
			tvUserCount.setText(count+"  λ�û��������");
			
			tvUserCount.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_user_count),
					null, null, null);
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	//��ȡ�û�����
	private  class ThreadGetUserInfo extends Thread
    {
    	@Override
    	public void run() {

    		try {
	
    			String strResult = getUserInfo();
    			
    			if (strResult.equals("fail"))  //��ȡʧ��
    			{ 
    				mHandler.obtainMessage(NET_FAILED).sendToTarget();// ������Ϣ��Handler
    			}
    			
    			else if(strResult.equals("�������쳣")) //�������쳣
    			{
    				mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
    			}
    			else  //��ȡ�ɹ�
    			{
    				mHandler.obtainMessage(NET_OK,strResult).sendToTarget();// ������Ϣ��Handler
    			}
    			
			}
			catch (Exception e) {
				mGetUserInfoThread = null;
			}
			mGetUserInfoThread = null;
    	}
    }
	
	//���߳�UI��Handler����
	Handler	mHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NET_OK:
				String obj=(String)msg.obj;
				
				
				setUserCount(obj);
				break;

			case NET_EXCEPTION:  //�����쳣
				controllersUtil.showToast(mContext, getString(R.string.network_exception), Toast.LENGTH_SHORT);
				//startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));//���������������ý���
				break; 
			case SERVER_EXCEPTION:   //�������쳣
				
				controllersUtil.showToast(mContext, getString(R.string.server_exception), Toast.LENGTH_SHORT);
				break;

			}
			 super.handleMessage(msg);  
		}
		
	};

	// ���ü�����
	private void setListner() {
		
		btnWelcomeLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(WelcomeActivity.this, LoginActivity.class) );  
			}
		});

		// ע�ᰴť�¼�
		btnWelcomeRegister.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class) );  
			}
		});
		
		btnWelcomeScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(WelcomeActivity.this, MainActivity.class) );  
			}
		});

	}
	
	//��ȡ�û���Ϣ
	private String getUserInfo(){
		
		// url
		String url = HttpUtil.BASE_URL+"GetUserCountServlet?";
		// ��ѯ���ؽ��
		return HttpUtil.queryStringForPost(url);
    }
	
	// ��ʼ����Ļ�����Ϣ
	private void initWindowParams(Activity context) {
		
		if (screenWidth == 0 || screenHeight == 0 || statusBarHeight == 0) {
			DisplayMetrics dm = new DisplayMetrics();
			context.getWindowManager().getDefaultDisplay().getMetrics(dm);
			screenHeight = dm.heightPixels;
			screenWidth = dm.widthPixels;
		}
	}
	
	private int getStatusBarHeight()
	{
		Rect frame = new Rect();    
		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);     
		return statusBarHeight = frame.top;
	}

}
