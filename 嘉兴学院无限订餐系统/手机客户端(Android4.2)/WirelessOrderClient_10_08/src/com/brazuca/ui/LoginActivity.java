package com.brazuca.ui;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.brazuca.db.BrazucaDBHelper;
import com.brazuca.db.BrazucaDBUtil;
import com.brazuca.entity.UserParam;
import com.brazuca.network.HttpUtil;
import com.brazuca.network.NetWorkUtil;
import com.brazuca.ui.animation.MyAnimation;
import com.brazuca.util.ControllersUtil;
import com.brazuca.util.ValidateUtil;

public class LoginActivity extends Activity {
	protected final  int LOGIN_FROM_NET_OK = 0x001;// ��Ϣ:��¼�ɹ�
	protected final  int NET_EXCEPTION = 0x002;// ��Ϣ:��¼�쳣
	protected final  int LOGIN_FAILED = 0x003;// ��Ϣ����¼ʧ��
	protected final  int SERVER_EXCEPTION = 0x004;// ��Ϣ:�������쳣
	private Context mContext;
	private LinearLayout llUsername;
	private LinearLayout llPassword;
	
    private AutoCompleteTextView edUsername;
    private EditText edPassword;
    private ImageView ivHead;
    private ImageView ivPwd;
    private Button btnBack;
    private Button btnLogin;
    
    private ControllersUtil controllersUtil;
    private ThreadLogin mLoginThread;   //ȫ���߳�
     
    private String strUsername = "";
    private String strPassword = "";
	public static boolean reBack = false;
	public static boolean isLoginSuccess = false;
	
	private String[] email = {};
	private String preInput;
	
	private AlertDialog dlgProgress;
   
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_login);
        controllersUtil = new ControllersUtil();
        mContext = LoginActivity.this;
        dlgProgress = new AlertDialog.Builder(mContext).create();
        
        controllersUtil = new ControllersUtil();
        email = getResources().getStringArray(R.array.common_email);
        
        //��ʼ���ؼ�
        initViews();  
        //���ü�����
        setListner();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	//�������״̬
    	ThreadCheckNetwork threadNetWork = new ThreadCheckNetwork();
        threadNetWork.start();
    }
    
   //��ʼ���ؼ�
    private void initViews()
    {
    	llUsername = (LinearLayout)findViewById(R.id.ll_username);  
    	llPassword= (LinearLayout)findViewById(R.id.ll_password);
    	
    	ivHead = (ImageView)findViewById(R.id.iv_head);
    	ivPwd = (ImageView)findViewById(R.id.iv_pwd);
    	
    	edUsername = (AutoCompleteTextView)findViewById(R.id.ed_username);
    	edPassword = (EditText)findViewById(R.id.ed_password);

    	btnBack = (Button)findViewById(R.id.btn_back);  //���ذ�ť
    	btnLogin = (Button)findViewById(R.id.btn_login);
    }
    
    //���ü�����
    private void setListner()
    {
    	//�����¼���Ӧ
    	btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				reBack = true;
				LoginActivity.this.finish();
			}
		});
    	
    	//��¼�¼���Ӧ
    	btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				strUsername=edUsername.getText().toString().replace(" ", "");  
				strPassword=edPassword.getText().toString().replace(" ", "");

				if(!ValidateUtil.validateInfo(strUsername))
				{
					controllersUtil.showToast(mContext,"�û�������Ϊ��",Toast.LENGTH_SHORT);
					MyAnimation.setTransAnimation(MyAnimation.SHAKE_X, edUsername, mContext);
				}
				else if(!ValidateUtil.validateInfo(strPassword))
				{
					controllersUtil.showToast(mContext,"���벻��Ϊ��",Toast.LENGTH_SHORT);
					MyAnimation.setTransAnimation(MyAnimation.SHAKE_X, edPassword, mContext);
				}
				else
				{
					mLoginThread = new ThreadLogin();  //ʵ������¼�߳�
					mLoginThread.start();	//������¼�߳�
					controllersUtil.showProgressWindow(dlgProgress, "���ڵ�¼�����Ժ�");
				}
			}
		});
    	
    	//�û������뽹��ı�����¼�
    	edUsername.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean flag) {
				// TODO Auto-generated method stub
				if(!flag)
				{
					llUsername.setBackgroundResource(R.drawable.bg_edit_unselected);
					ivHead.setBackgroundResource(R.drawable.account_bg_user_head_off);
				}
				else
				{	
					llUsername.setBackgroundResource(R.drawable.bg_edit_selected);
					ivHead.setBackgroundResource(R.drawable.account_bg_user_head_on);
				}
			}
		});
    	
    	edUsername.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {

				preInput = arg0.toString();
			}
			
			@Override
			public void afterTextChanged(Editable s) {

				String input = s.toString();
				int length = email.length;

				if (!preInput.equals(input)) {
					
					if ( input.lastIndexOf("@") == (input.length() - 1) && (input.length() - 1)>=5 ) {
						
						String[] arr = new String[length];
						for (int i = 0; i < length; i++) {
							arr[i] = s + email[i];
						}
						ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, 
								android.R.layout.simple_list_item_1, arr);
						
						edUsername.setAdapter(adapter);
						edUsername.setText(input);
						edUsername.setSelection(input.length());
					}
				}
			}
		});
    	
    	//�������뽹��ı�����¼�
    	edPassword.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean flag) {
				// TODO Auto-generated method stub
				if(!flag)
				{
					ivPwd.setBackgroundResource(R.drawable.account_bg_password_off);
					llPassword.setBackgroundResource(R.drawable.bg_edit_unselected);
				}
				else
				{
					ivPwd.setBackgroundResource(R.drawable.account_bg_password_on);
					llPassword.setBackgroundResource(R.drawable.bg_edit_selected);
				}
			}
		});

    }
    //��ȡ�û���Ϣ�����ݸ�ʽ--json����
    private void setUserInfo(String string)
    {
    	try {
			JSONObject jsonObject = new JSONObject(string);
			int userId = jsonObject.getInt("user_id");
			String username = jsonObject.getString("username");
			String password = jsonObject.getString("password");
			String nickname = jsonObject.getString("nickname");
			String age = jsonObject.getString("age");
			int vipType = jsonObject.getInt("vip_type");
			double balance = jsonObject.getDouble("balance");
			
			BrazucaDBUtil dbUtil = new BrazucaDBUtil(mContext);
			
			Cursor cursor = dbUtil.query(BrazucaDBHelper.TB_USER);
			int iRecord = cursor.getCount();
			if(iRecord == 1)
			{
				dbUtil.update(BrazucaDBHelper.TB_USER,String.valueOf(userId),username,password,
					age,nickname,String.valueOf(vipType));
				
				System.out.println("update");
			}
			else
			{
				//�����û���Ϣ�����ݿ�
				dbUtil.insert(BrazucaDBHelper.TB_USER,String.valueOf(userId),username,password,
						age,nickname,String.valueOf(vipType));
				System.out.println("insert");
			}

			UserParam.userId = userId;
			UserParam.username = username;
			UserParam.password = password;
			UserParam.nickname = nickname;
			UserParam.age =age;
			UserParam.vipType = vipType;
			UserParam.balance = balance;
			
			UserParam.isLogin = true;
			dbUtil.close();  //�ر����ݿ�

		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    //������������߳�
    private class ThreadCheckNetwork extends Thread
    {
    	@Override
    	public void run() {
  
    		try {
    			NetWorkUtil.checkNetworkInfo(mContext);
//    			Log.d("checkNetworkInfo",NetWorkUtil.checkNetworkInfo(mContext)+"");
    			
    			if(NetWorkUtil.checkNetworkInfo(mContext) == NetWorkUtil.DISCONNECTED)
    			{
    				mHandler.obtainMessage(NET_EXCEPTION).sendToTarget();// ������Ϣ��Handler
    			}	
			}
			catch (Exception e) {
			}

    	}
    }
    //��¼�߳�
    private class ThreadLogin extends Thread
    {
    	@Override
    	public void run() {

    		try {
    			//�����������
//    			if(NetWorkUtil.checkNetworkInfo(mContext) == NetWorkUtil.DISCONNECTED)
//    			{
//    				mHandler.obtainMessage(LOGIN_NET_EXCEPTION).sendToTarget();// ������Ϣ��Handler
//    				isConnectNetIng = false;  //�������ӶϿ�
//    				return;
//    			}	
    			
    			String strLoginResult = query(strUsername, strPassword); // ��ѯ�û����ݿ�
    			
    			if (strLoginResult != null && strLoginResult.equals("null")) {
    				mHandler.obtainMessage(LOGIN_FAILED).sendToTarget();// ������Ϣ��Handler
    			}
    			else if(strLoginResult.equals("�������쳣"))
    			{
    				mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
    			}
    			else
    			{
    				mHandler.obtainMessage(LOGIN_FROM_NET_OK, strLoginResult).sendToTarget();  // ����װ��json����strLoginResult������Ϣ��Handler
    			}
    			
			}
			catch (Exception e) {
				mLoginThread = null;
			}
			mLoginThread = null;
    	}
    }
    
  //���߳�UI��Handler����
	Handler	mHandler = new Handler(){
		public void handleMessage(Message msg) {
			
			if (dlgProgress.isShowing()) {
				controllersUtil.hideProgressWindow(dlgProgress);
			}
			
			switch (msg.what) {
			
			case LOGIN_FROM_NET_OK:   //��¼�ɹ�
				String strJsonObject = (String)msg.obj;
				setUserInfo(strJsonObject);
				
				controllersUtil.showToast(mContext, getString(R.string.login_success), Toast.LENGTH_SHORT);
				isLoginSuccess = true;
				
				Bundle bundle = new Bundle();
				bundle = getIntent().getExtras();
				
				if (bundle != null) {
					String TAG = bundle.getString("from_ac");
					String[] activities = getResources().getStringArray(R.array.activities);
					
					for (int i = 0; i < activities.length; i++) {
						if(TAG.equals(activities[i]))
						{
							LoginActivity.this.finish();
							return;
						}
					}
				}
				
				Intent intent = new  Intent(LoginActivity.this, TableActivity.class);
				startActivity(intent);
				LoginActivity.this.finish();
						
				break;

			case NET_EXCEPTION:  //�����쳣
				
				controllersUtil.showToast(mContext, getString(R.string.network_exception), Toast.LENGTH_SHORT);
				//startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));//���������������ý���
				break; 
			case SERVER_EXCEPTION:

				controllersUtil.showToast(mContext, getString(R.string.server_exception), Toast.LENGTH_SHORT);
				break;
			case LOGIN_FAILED:   //��¼ʧ��
				
				controllersUtil.showToast(mContext, getString(R.string.login_fail), Toast.LENGTH_SHORT);
				break;
			}
			
			 super.handleMessage(msg);  
		}
		
	};
   
    // �����û����������ѯ
	private String query(String strUsername,String strPassword){
		// ��ѯ����
		String queryString = "username="+strUsername+"&password="+strPassword;
		// url
		String url = HttpUtil.BASE_URL+"LoginServlet?"+queryString;
		// ��ѯ���ؽ��
		return HttpUtil.queryStringForPost(url);
    }

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == KeyEvent.KEYCODE_BACK)
		{
			reBack = true;
			LoginActivity.this.finish();
		}
		
		return super.dispatchKeyEvent(event);
	}
}