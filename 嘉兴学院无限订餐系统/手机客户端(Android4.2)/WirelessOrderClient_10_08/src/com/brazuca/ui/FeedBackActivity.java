package com.brazuca.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.brazuca.entity.UserParam;
import com.brazuca.network.HttpUtil;
import com.brazuca.util.ControllersUtil;

public class FeedBackActivity extends Activity {
	private static final String TAG="FeedBackActivity";
	protected final int SUBMIT_TO_NET_OK = 0x001;// 消息:提交反馈成功
	protected final int NET_EXCEPTION = 0x002;// 消息:网络异常
	protected final int SUBMIT_FAILED = 0x003;// 消息：提交反馈失败
	protected final int SERVER_EXCEPTION = 0x004;// 消息:服务器异常
	private static final int numberMax = 120;
	private Context mContext;
	private TextView tvBack;
	private TextView tvSubmit;
	private TextView tvNumber;
	private EditText edFeedback;
	private String strFeedBack;
	private ControllersUtil controllersUtil;
	private AlertDialog dlgProgress;
	private AlertDialog confrimDlg;

	private ThreadFeedback mThreadFee;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);
		mContext = FeedBackActivity.this;
		dlgProgress = new AlertDialog.Builder(mContext).create();
		confrimDlg  = new AlertDialog.Builder(mContext).create();

		controllersUtil = new ControllersUtil();
		initViews();
		setListner();
	}

	private void initViews() {
		tvBack = (TextView) findViewById(R.id.tv_feedback_back);
		tvSubmit = (TextView) findViewById(R.id.tv_feedback_submit);
		edFeedback = (EditText) findViewById(R.id.ed_feedback);
		tvNumber = (TextView) findViewById(R.id.tv_number); // 字数

		tvNumber.setText(numberMax + "");
	}

	private boolean validateFb(EditText editText) {
		if (editText.length() > 120 || editText.length() < 1)
			return false;
		else
			return true;
	}

	private void setListner() {
		// 监听EditText字数的改变
		edFeedback.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				int curNumber = numberMax - s.length();

				tvNumber.setText(curNumber + "");
			}
		});
		// 清空tvNumber时间
		tvNumber.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				edFeedback.setText("");

			}
		});
		// 提交反馈
		tvSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (!UserParam.isLogin) {
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putString("from_ac", TAG);

					intent.setClass(mContext, LoginActivity.class);

					intent.putExtras(bundle);
					startActivity(intent);
				}
				else if (validateFb(edFeedback)) {
					strFeedBack = edFeedback.getText().toString().trim();
					// 启动提交反馈线程
					mThreadFee = new ThreadFeedback();
					mThreadFee.start();
					controllersUtil.showProgressWindow(dlgProgress, "正在提交反馈");
				}
				else {
					controllersUtil.showToast(mContext,
							getString(R.string.fbError), Toast.LENGTH_SHORT);
				}
			}
		});

		// 返回按钮
		tvBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				controllersUtil.showConfirmBack(confrimDlg, "确认要退出反馈吗？", FeedBackActivity.this);
			}
		});
	}

	// 发送反馈线程
	private class ThreadFeedback extends Thread {
		@Override
		public void run() {

			try {
				// 检测网络连接
				// if(NetWorkUtil.checkNetworkInfo(mContext) ==
				// NetWorkUtil.DISCONNECTED)
				// {
				// mHandler.obtainMessage(NET_EXCEPTION).sendToTarget();//
				// 发送消息到Handler
				// isConnectNetIng = false; //网络连接断开
				// return;
				// }
				String strRegisterResult = callService(UserParam.userId,
						strFeedBack);

				if (strRegisterResult.equals("success")) // 注册成功
				{
					mHandler.obtainMessage(SUBMIT_TO_NET_OK).sendToTarget();// 发送消息到Handler
				}
				else if (strRegisterResult.equals("fail")) // 注册失败
				{
					mHandler.obtainMessage(SUBMIT_FAILED).sendToTarget();// 发送消息到Handler
				}

				else if (strRegisterResult.equals("服务器异常")) // 服务器异常
				{
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// 发送消息到Handler
				}

			}
			catch (Exception e) {
				mThreadFee = null;
			}
			mThreadFee = null;
		}
	}

	// 主线程UI的Handler处理
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case SUBMIT_TO_NET_OK: // 提交反馈成功

				controllersUtil.showToastTwo(mContext,
						getString(R.string.feedback_submit_success),
						Toast.LENGTH_SHORT);

				break;

			case NET_EXCEPTION: // 网络异常

				controllersUtil.showToast(mContext,
						getString(R.string.network_exception),
						Toast.LENGTH_SHORT);
				// startActivity(new
				// Intent(Settings.ACTION_WIRELESS_SETTINGS));//进入无线网络配置界面
				break;
			case SERVER_EXCEPTION: // 服务器异常

				controllersUtil.showToast(mContext,
						getString(R.string.server_exception),
						Toast.LENGTH_SHORT);
				break;
			case SUBMIT_FAILED: // 提交反馈失败

				controllersUtil.showToast(mContext,
						getString(R.string.feedback_submit_fail),
						Toast.LENGTH_SHORT);
				break;
			}
			
			if (dlgProgress.isShowing()) {
				controllersUtil.hideProgressWindow(dlgProgress);
			}

			super.handleMessage(msg);
		}

	};

	// 提交反馈
	private String callService(int userId, String feedback) {
		// 查询参数
		String callServiceString = "userid=" + String.valueOf(userId)
				+ "&feedback=" + feedback;
		// url
		String url = HttpUtil.BASE_URL + "FeedbackServlet?"
				+ callServiceString;
		// 查询返回结果
		return HttpUtil.queryStringForPost(url);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		switch (keyCode) {

		case KeyEvent.KEYCODE_BACK:
			controllersUtil.showConfirmBack(confrimDlg, "确认要退出反馈吗？", FeedBackActivity.this);
			break;
		default:
			break;
		}
		return true;
	}
}
