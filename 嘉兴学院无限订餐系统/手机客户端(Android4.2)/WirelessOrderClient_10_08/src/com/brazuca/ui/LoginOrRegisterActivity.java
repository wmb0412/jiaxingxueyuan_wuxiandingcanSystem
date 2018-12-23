package com.brazuca.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brazuca.network.HttpUtil;
import com.brazuca.util.ControllersUtil;

public class LoginOrRegisterActivity extends Activity {
	private Context mContext;
	private LinearLayout llUsername;
	private LinearLayout llPassword;
	private LinearLayout llRepassword;
	private TextView tvTopBar;
	
    private EditText edUsername;
    private EditText edPassword;
    private EditText edRepassword;
    private ImageView ivHead;
    private ImageView ivPwd;
    private ImageView ivRepwd;
    private Button btnLogin;
    private Button btnRegister;
    
    
    private String strUsername = "";
    private String strPassword = "";
    private boolean isLogin = true; // true——登录；false——注册
    
    private ControllersUtil controllersUtil;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_login_or_register);
        controllersUtil = new ControllersUtil();
        mContext = getBaseContext();
        
        //初始化控件
        initViews();  
        //设置监听器
        setListner();
    }
    
    private void changeType(boolean flag)
    {
    	//登录
    	if(flag)
    	{
    		llUsername.setVisibility(View.VISIBLE);
    		llPassword.setVisibility(View.VISIBLE);
    		llRepassword.setVisibility(View.GONE);
    		btnLogin.setVisibility(View.VISIBLE);
    		tvTopBar.setText(R.string.btn_login_text);
    		isLogin = false;
    	}
    	//注册
    	else
    	{
    		llUsername.setVisibility(View.VISIBLE);
    		llPassword.setVisibility(View.VISIBLE);
    		llRepassword.setVisibility(View.VISIBLE);
    		btnLogin.setVisibility(View.GONE);
    		tvTopBar.setText(R.string.btn_register_text);
    		isLogin = true;
    	}
    }
    
   //初始化控件
    private void initViews()
    {
    	llUsername = (LinearLayout)findViewById(R.id.ll_username);  
    	llPassword= (LinearLayout)findViewById(R.id.ll_password);
    	llRepassword= (LinearLayout)findViewById(R.id.ll_repassword);
    	tvTopBar = (TextView)findViewById(R.id.tv_topbar);
    	
    	ivHead = (ImageView)findViewById(R.id.iv_head);
    	ivPwd = (ImageView)findViewById(R.id.iv_pwd);
    	ivRepwd = (ImageView)findViewById(R.id.iv_repwd);
    	
    	edUsername = (EditText)findViewById(R.id.ed_username);
    	edPassword = (EditText)findViewById(R.id.ed_password);
    	edRepassword= (EditText)findViewById(R.id.ed_repassword);

    	btnLogin = (Button)findViewById(R.id.btn_login);
    	btnRegister = (Button)findViewById(R.id.btn_register);  //切换到注册
    }
    
    //设置监听器
    private void setListner()
    {
    	btnLogin.setOnClickListener(new OnClickListener() {
    		

			@Override
			public void onClick(View arg0) {
				strUsername=edUsername.getText().toString().trim();
				strPassword=edPassword.getText().toString().trim();
				
				if(validateLoginInfo()==0)
				{
					controllersUtil.showToast(mContext,"登录成功",Toast.LENGTH_SHORT);
				}
				else if(validateLoginInfo()==1)
				{
					controllersUtil.showToast(mContext,"用户名不能为空",Toast.LENGTH_SHORT);
				}
				else if(validateLoginInfo()==2)
				{
					controllersUtil.showToast(mContext,"密码不能为空",Toast.LENGTH_SHORT);
				}
				else if(validateLoginInfo()==3)
				{
					controllersUtil.showToast(mContext,"网络异常",Toast.LENGTH_SHORT);
				}
			}
		});
    	
    	//注册按钮事件
    	btnRegister.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				changeType(isLogin);
			}
		});
    	
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
    	
    	edRepassword.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean flag) {
				// TODO Auto-generated method stub
				if(!flag)
				{
					ivRepwd.setBackgroundResource(R.drawable.account_bg_password_off);
					llRepassword.setBackgroundResource(R.drawable.bg_edit_unselected);
				}
				else
				{
					ivRepwd.setBackgroundResource(R.drawable.account_bg_password_on);
					llRepassword.setBackgroundResource(R.drawable.bg_edit_selected);
				}
			}
		});
    	
    	
    }
    
    private int validateLoginInfo()
    {
    	String strLoginResult=query(strUsername,strPassword);
    	
    	if(strUsername.equals(""))
    	{
    		return 1;
    	}
    	else if(strPassword.equals(""))
    	{
    		return 2;
    	}
    	else if(strLoginResult!=null&&strLoginResult.equals("0"))
    	{
    		return 3;
    	}
    	else{
			return 0;
		}
    }
   
    
 // 根据用户名称密码查询
	private String query(String username,String password){
		// 查询参数
		String queryString = "username="+username+"&password="+password;
		// url
		String url = HttpUtil.BASE_URL+"LoginServlet?"+queryString;
		// 查询返回结果
		return HttpUtil.queryStringForPost(url);
    }
}