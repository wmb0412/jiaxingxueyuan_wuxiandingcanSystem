package com.brazuca.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.brazuca.network.HttpUtil;
import com.brazuca.network.NetWorkUtil;
import com.brazuca.ui.animation.MyAnimation;
import com.brazuca.util.ControllersUtil;
import com.brazuca.util.ValidateUtil;
import com.brazuca.view.control.MyClickableSpan;

public class RegisterActivity extends Activity {
	protected final int REGISTER_FROM_NET_OK = 0x001;// 消息:注册成功
	protected final int NET_EXCEPTION = 0x002;// 消息:网络异常
	protected final int REGISTER_FAILED = 0x003;// 消息：注册失败
	protected final int SERVER_EXCEPTION = 0x004;// 消息:服务器异常
	private Context mContext;
	public static LinearLayout llRegisterParent;
	private LinearLayout llUsername;
	private LinearLayout llPassword;
	private LinearLayout llRepassword;

	private AutoCompleteTextView edUsername;
	private EditText edPassword;
	private EditText edRepassword;
	private ImageView ivHead;
	private ImageView ivPwd;
	private ImageView ivRepwd;
	private Button btnBack;
	private Button btnRegister;
	private TextView tvAgreements;
	private SpannableString spannableString;

	private PopupWindow pwRegister;

	private ThreadRegister mRegisterThread; // 全局线程

	private String strUsername = "";
	private String strPassword = "";
	private String strRePassword = "";

	private ControllersUtil controllersUtil;
	private String[] email = {};
	private String preInput;
	private AlertDialog confrimDlg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register);
		controllersUtil = new ControllersUtil();
		mContext = RegisterActivity.this;
		confrimDlg  = new AlertDialog.Builder(mContext).create();
		email = getResources().getStringArray(R.array.common_email);
		// 初始化控件
		initViews();

		registerAgreements();
		// 设置监听器
		setListner();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 检测网络状态
		ThreadCheckNetwork threadNetWork = new ThreadCheckNetwork();
		threadNetWork.start();
	}

	// 初始化控件
	private void initViews() {
		llRegisterParent = (LinearLayout) findViewById(R.id.ll_register_parent);
		llUsername = (LinearLayout) findViewById(R.id.ll_username); // 用户名LinearLayout
		llPassword = (LinearLayout) findViewById(R.id.ll_password); // 密码LinearLayout
		llRepassword = (LinearLayout) findViewById(R.id.ll_repassword); // 确认密码LinearLayout

		ivHead = (ImageView) findViewById(R.id.iv_head);
		ivPwd = (ImageView) findViewById(R.id.iv_pwd);
		ivRepwd = (ImageView) findViewById(R.id.iv_repwd);

		edUsername = (AutoCompleteTextView) findViewById(R.id.ed_username);
		edPassword = (EditText) findViewById(R.id.ed_password);
		edRepassword = (EditText) findViewById(R.id.ed_repassword);

		btnBack = (Button) findViewById(R.id.btn_register_back); // 返回按钮
		btnRegister = (Button) findViewById(R.id.btn_register); // 注册
		tvAgreements = (TextView) findViewById(R.id.tv_agreements); // 用户协议
	}

	// 注册协议
	private void registerAgreements() {
		// 创建一个 SpannableString对象
		spannableString = new SpannableString(getResources().getString(
				R.string.user_agreements_title));

		// 设置下划线
		spannableString.setSpan(new UnderlineSpan(), 7, 15,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		// 超级链接（需要添加setMovementMethod方法附加响应）
		spannableString.setSpan(new MyClickableSpan(mContext), 7, 15,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		tvAgreements.setText(spannableString);
		tvAgreements.setMovementMethod(LinkMovementMethod.getInstance());

	}

	// 设置监听器
	private void setListner() {

		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				controllersUtil.showConfirmBack(confrimDlg, "确认要放弃注册么？", RegisterActivity.this);
			}
		});

		// 注册按钮事件
		btnRegister.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				strUsername = edUsername.getText().toString().trim(); // 用户名
				strPassword = edPassword.getText().toString().trim(); // 密码
				strRePassword = edRepassword.getText().toString().trim(); // 重复密码

				// 对输入框的验证
				if (!ValidateUtil.validateInfo(strUsername)) // 用户名不能为空
				{
					controllersUtil.showToast(mContext,
							getString(R.string.username_cannot_be_null),
							Toast.LENGTH_SHORT);
					MyAnimation.setTransAnimation(MyAnimation.SHAKE_X,
							edUsername, mContext);
				}
				else if (!ValidateUtil.validateInfo(strPassword)) // 密码不能为空
				{
					controllersUtil.showToast(mContext,
							getString(R.string.password_cannot_be_null),
							Toast.LENGTH_SHORT);
					MyAnimation.setTransAnimation(MyAnimation.SHAKE_X,
							edPassword, mContext);
				}
				else if (!ValidateUtil.validateLength(strPassword)) // 密码不能少于6位
				{

					controllersUtil.showToast(mContext,
							getString(R.string.password_cannot_less_six),
							Toast.LENGTH_SHORT);
					MyAnimation.setTransAnimation(MyAnimation.SHAKE_X,
							edPassword, mContext);
				}
				else if (!ValidateUtil.validateInfo(strRePassword)) // 确认密码不能为空
				{
					controllersUtil.showToast(mContext,
							getString(R.string.repassword_cannot_be_null),
							Toast.LENGTH_SHORT);
					MyAnimation.setTransAnimation(MyAnimation.SHAKE_X,
							edRepassword, mContext);
				}
				else if (!ValidateUtil.validateLength(strRePassword)) // 确认密码不能少于6位
				{
					controllersUtil.showToast(mContext,
							getString(R.string.repassword_cannot_less_six),
							Toast.LENGTH_SHORT);
					MyAnimation.setTransAnimation(MyAnimation.SHAKE_X,
							edRepassword, mContext);
				}
				else if (!ValidateUtil.validateEquals(strPassword,
						strRePassword)) // 密码和确认密码不一样
				{
					controllersUtil.showToast(mContext,
							getString(R.string.password_not_equals_repassword),
							Toast.LENGTH_SHORT);
				}
				else // 通过验证，则开始执行注册
				{
					mRegisterThread = new ThreadRegister(); // 实例化登录线程
					mRegisterThread.start(); // 启动登录线程
					initPopupWindow(llRegisterParent);
				}
			}
		});

		edUsername.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean flag) {
				// TODO Auto-generated method stub
				if (!flag) {
					llUsername
							.setBackgroundResource(R.drawable.bg_edit_unselected);
					ivHead.setBackgroundResource(R.drawable.account_bg_user_head_off);
				}
				else {
					llUsername
							.setBackgroundResource(R.drawable.bg_edit_selected);
					ivHead.setBackgroundResource(R.drawable.account_bg_user_head_on);
				}
			}
		});
		// 监听文本改变事件
		edUsername.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

				preInput = arg0.toString();
			}

			@Override
			public void afterTextChanged(Editable s) {

				String input = s.toString();
				int length = email.length;

				if (!preInput.equals(input)) {

					if (input.lastIndexOf("@") == (input.length() - 1)
							&& (input.length() - 1) >= 5) {

						String[] arr = new String[length];
						for (int i = 0; i < length; i++) {
							arr[i] = s + email[i];
						}
						ArrayAdapter<String> adapter = new ArrayAdapter<String>(
								mContext, android.R.layout.simple_list_item_1,
								arr);

						edUsername.setAdapter(adapter);
						edUsername.setText(input);
						edUsername.setSelection(input.length());
					}
				}
			}
		});
		// 监听焦点改变事件
		edPassword.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean flag) {
				// TODO Auto-generated method stub
				if (!flag) {
					ivPwd.setBackgroundResource(R.drawable.account_bg_password_off);
					llPassword
							.setBackgroundResource(R.drawable.bg_edit_unselected);
				}
				else {
					ivPwd.setBackgroundResource(R.drawable.account_bg_password_on);
					llPassword
							.setBackgroundResource(R.drawable.bg_edit_selected);
				}
			}
		});
		// 监听焦点改变事件
		edRepassword.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean flag) {
				// TODO Auto-generated method stub
				if (!flag) {
					ivRepwd.setBackgroundResource(R.drawable.account_bg_password_off);
					llRepassword
							.setBackgroundResource(R.drawable.bg_edit_unselected);
				}
				else {
					ivRepwd.setBackgroundResource(R.drawable.account_bg_password_on);
					llRepassword
							.setBackgroundResource(R.drawable.bg_edit_selected);
				}
			}
		});

	}

	// 检测网络连接线程
	private class ThreadCheckNetwork extends Thread {
		@Override
		public void run() {

			try {
				NetWorkUtil.checkNetworkInfo(mContext);

				if (NetWorkUtil.checkNetworkInfo(mContext) == NetWorkUtil.DISCONNECTED) {
					mHandler.obtainMessage(NET_EXCEPTION).sendToTarget();// 发送消息到Handler
				}
			}
			catch (Exception e) {
			}

		}
	}

	// 注册提交线程
	private class ThreadRegister extends Thread {
		@Override
		public void run() {

			try {
				// 检测网络连接
				// if(NetWorkUtil.checkNetworkInfo(mContext) ==
				// NetWorkUtil.DISCONNECTED)
				// {
				// mHandler.obtainMessage(LOGIN_NET_EXCEPTION).sendToTarget();//
				// 发送消息到Handler
				// isConnectNetIng = false; //网络连接断开
				// return;
				// }
				String strRegisterResult = register(strUsername, strPassword); // 查询用户数据库

				if (strRegisterResult.equals("success")) // 注册成功
				{
					mHandler.obtainMessage(REGISTER_FROM_NET_OK).sendToTarget();// 发送消息到Handler
				}
				else if (strRegisterResult.equals("fail")) // 注册失败
				{
					mHandler.obtainMessage(REGISTER_FAILED).sendToTarget();// 发送消息到Handler
				}

				else if (strRegisterResult.equals("服务器异常")) // 服务器异常
				{
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// 发送消息到Handler
				}

			}
			catch (Exception e) {
				mRegisterThread = null;
			}
			mRegisterThread = null;
		}
	}

	// 主线程UI的Handler处理
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case REGISTER_FROM_NET_OK: // 注册成功

				controllersUtil.showToast(mContext,
						getString(R.string.register_success),
						Toast.LENGTH_SHORT);
				if (pwRegister != null)
					pwRegister.dismiss(); // 关闭popwindow

				RegisterActivity.this.finish();
				startActivity(new Intent(mContext, MainActivity.class));
				break;

			case NET_EXCEPTION: // 网络异常
				if (pwRegister != null)
					pwRegister.dismiss(); // 关闭popwindow

				controllersUtil.showToast(mContext,
						getString(R.string.network_exception),
						Toast.LENGTH_SHORT);
				// startActivity(new
				// Intent(Settings.ACTION_WIRELESS_SETTINGS));//进入无线网络配置界面
				break;
			case SERVER_EXCEPTION: // 服务器异常
				if (pwRegister != null)
					pwRegister.dismiss(); // 关闭popwindow

				controllersUtil.showToast(mContext,
						getString(R.string.server_exception),
						Toast.LENGTH_SHORT);
				break;
			case REGISTER_FAILED: // 注册失败
				if (pwRegister != null)
					pwRegister.dismiss(); // 关闭popwindow

				controllersUtil.showToast(mContext,
						getString(R.string.login_fail), Toast.LENGTH_SHORT);
				break;
			}
			super.handleMessage(msg);
		}

	};

	// 初始化popWindow的值
	private void initPopupWindow(View parent) {

		if (pwRegister == null) {
			// 加载popupwindow的layout布局文件
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = layoutInflater.inflate(
					R.layout.overall_progress_popwindow, null);

			ImageView ivLogo = (ImageView) view
					.findViewById(R.id.iv_progress_logo);
			MyAnimation.setTransAnimation(MyAnimation.SHAKE_INFINITE, ivLogo,
					mContext);
			// 给popupwindow里面的TextView设置文字显示
			TextView tvProgressInfo = (TextView) view
					.findViewById(R.id.tv_progress_info);
			tvProgressInfo.setText(getString(R.string.register_ing));

			pwRegister = new PopupWindow(view, LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT);

			// 给popupwindow设置背景颜色:灰色背景
			ColorDrawable dw = new ColorDrawable(0x7DC0C0C0);
			pwRegister.setBackgroundDrawable(dw);

			pwRegister.setFocusable(true);
			pwRegister.setOutsideTouchable(false);

			// 设置popupwindow出现的动画
			pwRegister.setAnimationStyle(android.R.style.Animation_Toast);
		}
		pwRegister.showAtLocation(parent, Gravity.CENTER, 0, 0);

		// 监听popmenu关闭
		pwRegister.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				mRegisterThread = null;
			}
		});
	}

	// 发送用户注册信息
	private String register(String strUsername, String strPassword) {
		// 查询参数
		String queryString = "username=" + strUsername + "&password="
				+ strPassword;
		// url
		String url = HttpUtil.BASE_URL + "RegisterServlet?"
				+ queryString;

		// 查询返回结果
		return HttpUtil.queryStringForPost(url);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		switch (keyCode) {

		case KeyEvent.KEYCODE_BACK:
			controllersUtil.showConfirmBack(confrimDlg, "确认要放弃注册么？", RegisterActivity.this);
			break;
		default:
			break;
		}
		return true;
	}
}