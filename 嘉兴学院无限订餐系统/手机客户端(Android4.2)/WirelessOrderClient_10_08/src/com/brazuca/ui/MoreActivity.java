package com.brazuca.ui;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.brazuca.db.BrazucaDBGetInfoUtil;
import com.brazuca.db.BrazucaDBHelper;
import com.brazuca.db.BrazucaDBUtil;
import com.brazuca.entity.UpdateInfo;
import com.brazuca.entity.UserParam;
import com.brazuca.network.HttpUtil;
import com.brazuca.network.NetWorkUtil;
import com.brazuca.ui.animation.MyAnimation;
import com.brazuca.util.ControllersUtil;

public class MoreActivity extends Activity {
	protected final int CALL_FROM_NET_OK = 0x001;// 消息：呼叫成功
	protected final int NET_EXCEPTION = 0x002;// 消息：网络异常
	protected final int CALL_FAILED = 0x003;// 消息：呼叫失败
	protected final int SERVER_EXCEPTION = 0x004;// 消息:服务器异常
	protected final int GET_SAME_VERSION = 0x005;
	protected final int GET_NEW_VERSION = 0x006;
	protected final int GET_UNDATEINFO_ERROR = 0x007;

	private Context mContext;
	private ControllersUtil controllersUtil;
	private LinearLayout llCallService;
	private LinearLayout llFeedback;
	private LinearLayout llCheckNewVersion; // 检测新版本
	private LinearLayout llAbout;
	private TextView tvLoginOut;
	private PopupWindow pwMore;
	private TextView tvCheckTable; // 选桌
	private AlertDialog progressDlg;

	private ThreadCallService mCallServiceThread;
	private boolean isConnectNetIng = false;
	private String downNewVersionUrl = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more);
		mContext = MoreActivity.this;
		controllersUtil = new ControllersUtil();
		progressDlg = new AlertDialog.Builder(mContext).create();

		initViews();
		setListner();

		isLoginOutVisile(); // 退出登录按钮是否可见
	}

	@Override
	protected void onResume() {
		if (LoginActivity.isLoginSuccess) {
			tvLoginOut.setVisibility(View.VISIBLE);
			LoginActivity.isLoginSuccess = false;
		}
		super.onResume();
	}

	// 退出登录按钮是否可见
	private void isLoginOutVisile() {
		if (UserParam.userId == 0) {
			String userId = BrazucaDBGetInfoUtil.getDBUserId(mContext);// 获取用户id号
			if (userId.equals("") || userId == null) // 获取不到用户id号，则判断为未登录
			{
				UserParam.isLogin = false;
			}
			else // 已经登录
			{
				UserParam.userId = Integer.parseInt(userId);// 获取用户id号
				UserParam.isLogin = true;
			}
		}

		if (UserParam.isLogin) {
			tvLoginOut.setVisibility(View.VISIBLE);
		}
		else {
			tvLoginOut.setVisibility(View.GONE);
		}
	}

	// 初始化控件
	private void initViews() {
		llCallService = (LinearLayout) findViewById(R.id.ll_call_service);
		llFeedback = (LinearLayout) findViewById(R.id.ll_feedback);
		llCheckNewVersion = (LinearLayout) findViewById(R.id.ll_check_new_version);
		llAbout = (LinearLayout) findViewById(R.id.ll_about);

		tvLoginOut = (TextView) findViewById(R.id.tv_login_out); // 退出登录
		tvCheckTable = (TextView) this.findViewById(R.id.tv_check_table); // 选桌

	}

	// 设置监听事件
	private void setListner() {
		// 选桌
		tvCheckTable.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// 如果未检测到用户登录，则跳转到登录界面
				if (!UserParam.isLogin) {
					startActivity(new Intent(MoreActivity.this,
							LoginActivity.class));
				}
				else
					startActivity(new Intent(MoreActivity.this,
							TableActivity.class));

			}
		});

		// 退出登录
		tvLoginOut.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				BrazucaDBUtil dbUtil = new BrazucaDBUtil(mContext);

				// 删除唯一用户数据
				if (dbUtil.delete(BrazucaDBHelper.TB_USER,
						String.valueOf(UserParam.userId))) {
					controllersUtil.showToast(mContext, "退出登录成功",
							Toast.LENGTH_SHORT);
					tvLoginOut.setVisibility(View.GONE);
					UserParam.isLogin = false; // 登录状态为“不登录”
				}
				else {
					controllersUtil.showToast(mContext, "退出登录异常，请重试",
							Toast.LENGTH_SHORT);
				}

				dbUtil.close(); // 关闭数据库

			}
		});

		// 呼叫服务员
		llCallService.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View parent, MotionEvent motionEvent) {

				if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
					initPopupWindow(llCallService);

					// 启动呼叫服务此线程
					mCallServiceThread = new ThreadCallService();
					mCallServiceThread.start();
				}

				return false;
			}
		});

		// 意见反馈
		llFeedback.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View parent, MotionEvent motionEvent) {

				if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
					startActivity(new Intent(MoreActivity.this,
							FeedBackActivity.class));
				}

				return false;
			}
		});

		// 检测新版本
		llCheckNewVersion.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View parent, MotionEvent motionEvent) {

				if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
					controllersUtil.showProgressWindow(progressDlg,
							"正在检测新版本，请稍后");
					new Thread(new CheckVersionTask()).start();
				}

				return false;
			}
		});

		// 关于界面
		llAbout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View parent, MotionEvent motionEvent) {

				if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
					startActivity(new Intent(MoreActivity.this,
							AboutActivity.class));
				}

				return false;
			}
		});
	}

	/*
	 * 获取当前程序的版本号
	 */
	private String getVersionName() throws Exception {
		// 获取packagemanager的实例
		PackageManager packageManager = getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),
				0);
		return packInfo.versionName;
	}

	/*
	 * 用pull解析器解析服务器返回的xml文件 (xml封装了版本号)
	 */
	public static UpdateInfo getUpdataInfo(InputStream is) throws Exception {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(is, "utf-8");// 设置解析的数据源
		int type = parser.getEventType();
		UpdateInfo info = new UpdateInfo();// 实体
		while (type != XmlPullParser.END_DOCUMENT) {
			switch (type) {
			case XmlPullParser.START_TAG:
				if ("version".equals(parser.getName())) {
					info.setVersion(parser.nextText()); // 获取版本号
				}
				else if ("url".equals(parser.getName())) {
					info.setUrl(parser.nextText()); // 获取要升级的APK文件
				}
				else if ("description".equals(parser.getName())) {
					info.setDescription(parser.nextText()); // 获取该文件的信息
				}
				break;
			}
			type = parser.next();
		}
		return info;
	}

	/*
	 * 从服务器获取xml解析并进行比对版本号
	 */
	public class CheckVersionTask implements Runnable {

		public void run() {
			try {
				// 从资源文件获取服务器 地址
				String path = HttpUtil.BASE_URL + "update.xml";
				// 包装成url的对象
				URL url = new URL(path);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setConnectTimeout(5000);
				InputStream is = conn.getInputStream();
				UpdateInfo info = getUpdataInfo(is);
				String versionNameServer = info.getVersion();
				String versionDescription = info.getDescription();
				downNewVersionUrl = info.getUrl();

				if (versionNameServer.equals(getVersionName())) {
					mHandler.obtainMessage(GET_SAME_VERSION).sendToTarget();// 发送消息到Handler
				}
				else {
					mHandler.obtainMessage(GET_NEW_VERSION,versionDescription).sendToTarget();// 发送消息到Handler
				}
			}
			catch (Exception e) {
				// 待处理
				mHandler.obtainMessage(GET_UNDATEINFO_ERROR).sendToTarget();// 发送消息到Handler

				e.printStackTrace();
			}
		}
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

	// 呼叫服务员线程
	private class ThreadCallService extends Thread {

		@Override
		public void run() {
			Log.d("ThreadCallService", "ThreadCallService");
			try {
				isConnectNetIng = true;

				while (isConnectNetIng) {
					// 检测网络连接
					if (NetWorkUtil.checkNetworkInfo(mContext) == NetWorkUtil.DISCONNECTED) {
						mHandler.obtainMessage(NET_EXCEPTION).sendToTarget();// 发送消息到Handler
						isConnectNetIng = false; // 网络连接断开
						return;
					}

					String strGetResponse = callService("呼叫服务员", "10");

					Log.d("strGetResponse", strGetResponse);

					if (strGetResponse.equals("success")) // 呼叫服务员成功
					{
						mHandler.obtainMessage(CALL_FROM_NET_OK).sendToTarget();// 发送消息到Handler
					}
					else if (strGetResponse.equals("fail")) {
						mHandler.obtainMessage(CALL_FAILED).sendToTarget();// 发送消息到Handler
					}
					else if (strGetResponse.equals("服务器异常")) {
						mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// 发送消息到Handler
					}

					isConnectNetIng = false;
				}

			}
			catch (Exception e) {
				mCallServiceThread = null;
				isConnectNetIng = false;
			}
			mCallServiceThread = null;
		}
	}

	// 主线程UI的Handler处理
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(progressDlg.isShowing())
				progressDlg.dismiss();
			
			switch (msg.what) {
			
			case GET_NEW_VERSION:
				final AlertDialog dlg = new AlertDialog.Builder(mContext)
						.create();

				dlg.show();

				Window window = dlg.getWindow();

				window.setContentView(R.layout.bg_confirm_dialog);

				TextView tvConfrimMsg = (TextView) window
						.findViewById(R.id.tv_confrim_msg);
				
				tvConfrimMsg.setText(msg.obj.toString());
				// 为确认按钮添加事件
				TextView ok = (TextView) window.findViewById(R.id.btn_ok);
				TextView cancel = (TextView) window
						.findViewById(R.id.btn_cancel);

				ok.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						// TODO Auto-generated method stub
							
						Intent intent = new Intent();        
						intent.setAction("android.intent.action.VIEW");    
						Uri content_url = Uri.parse(downNewVersionUrl);   
						intent.setData(content_url);  
						startActivity(intent);
						
						dlg.dismiss();
					}
				});

				cancel.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						dlg.dismiss();
					}
				});

				break;
			case GET_SAME_VERSION:
				controllersUtil.showToast(mContext,
						"没有检测到新版本", Toast.LENGTH_SHORT);
				break;
			case GET_UNDATEINFO_ERROR:
				controllersUtil.showToast(mContext,
						"发生未知异常，请重试", Toast.LENGTH_SHORT);
				break;

			case CALL_FROM_NET_OK: // 呼叫成功

				controllersUtil.showToast(mContext,
						getString(R.string.call_success), Toast.LENGTH_SHORT);
				if (pwMore != null) {
					pwMore.dismiss(); // 关闭popwindow
					pwMore = null;
				}

				// startActivity(new Intent(mContext, MainActivity.class));
				break;

			case NET_EXCEPTION: // 网络异常
				if (pwMore != null)
					pwMore.dismiss(); // 关闭popwindow

				controllersUtil.showToast(mContext,
						getString(R.string.network_exception),
						Toast.LENGTH_SHORT);
				// startActivity(new
				// Intent(Settings.ACTION_WIRELESS_SETTINGS));//进入无线网络配置界面
				break;
			case SERVER_EXCEPTION: // 服务器异常
				if (pwMore != null)
					pwMore.dismiss(); // 关闭popwindow

				controllersUtil.showToast(mContext,
						getString(R.string.server_exception),
						Toast.LENGTH_SHORT);
				break;
			case CALL_FAILED: // 呼叫失败
				if (pwMore != null)
					pwMore.dismiss(); // 关闭popwindow

				controllersUtil.showToast(mContext,
						getString(R.string.call_fail), Toast.LENGTH_SHORT);
				break;
			}
			super.handleMessage(msg);
		}

	};

	// 初始化popWindow的值
	private void initPopupWindow(View parent) {

		if (pwMore == null) {
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
			tvProgressInfo.setText(getString(R.string.calling_service));

			pwMore = new PopupWindow(view, LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT);

			// 给popupwindow设置背景颜色:灰色背景
			ColorDrawable dw = new ColorDrawable(0x7DC0C0C0);
			pwMore.setBackgroundDrawable(dw);

			pwMore.setFocusable(true);
			pwMore.setOutsideTouchable(false);

			// 设置popupwindow出现的动画
			pwMore.setAnimationStyle(android.R.style.Animation_Toast);
		}

		pwMore.showAtLocation(parent, Gravity.CENTER, 0, 0);
		// pwMore.showAsDropDown(parent);

		// 监听popmenu关闭
		pwMore.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				// isConnectNetIng = false;
				// mLoginThread = null;
			}
		});
	}

	// 呼叫服务员
	private String callService(String callMessage, String tableId) {
		// 查询参数
		String callServiceString = "callMessage=" + callMessage + "&tableId="
				+ tableId;
		// url
		String url = HttpUtil.BASE_URL + "CustomerCallServlet?"
				+ callServiceString;
		// 查询返回结果
		return HttpUtil.queryStringForPost(url);
	}
}
