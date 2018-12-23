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
	protected final int SUBMIT_TO_NET_OK = 0x001;// ��Ϣ:�ύ�����ɹ�
	protected final int NET_EXCEPTION = 0x002;// ��Ϣ:�����쳣
	protected final int SUBMIT_FAILED = 0x003;// ��Ϣ���ύ����ʧ��
	protected final int SERVER_EXCEPTION = 0x004;// ��Ϣ:�������쳣
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
		tvNumber = (TextView) findViewById(R.id.tv_number); // ����

		tvNumber.setText(numberMax + "");
	}

	private boolean validateFb(EditText editText) {
		if (editText.length() > 120 || editText.length() < 1)
			return false;
		else
			return true;
	}

	private void setListner() {
		// ����EditText�����ĸı�
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
		// ���tvNumberʱ��
		tvNumber.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				edFeedback.setText("");

			}
		});
		// �ύ����
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
					// �����ύ�����߳�
					mThreadFee = new ThreadFeedback();
					mThreadFee.start();
					controllersUtil.showProgressWindow(dlgProgress, "�����ύ����");
				}
				else {
					controllersUtil.showToast(mContext,
							getString(R.string.fbError), Toast.LENGTH_SHORT);
				}
			}
		});

		// ���ذ�ť
		tvBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				controllersUtil.showConfirmBack(confrimDlg, "ȷ��Ҫ�˳�������", FeedBackActivity.this);
			}
		});
	}

	// ���ͷ����߳�
	private class ThreadFeedback extends Thread {
		@Override
		public void run() {

			try {
				// �����������
				// if(NetWorkUtil.checkNetworkInfo(mContext) ==
				// NetWorkUtil.DISCONNECTED)
				// {
				// mHandler.obtainMessage(NET_EXCEPTION).sendToTarget();//
				// ������Ϣ��Handler
				// isConnectNetIng = false; //�������ӶϿ�
				// return;
				// }
				String strRegisterResult = callService(UserParam.userId,
						strFeedBack);

				if (strRegisterResult.equals("success")) // ע��ɹ�
				{
					mHandler.obtainMessage(SUBMIT_TO_NET_OK).sendToTarget();// ������Ϣ��Handler
				}
				else if (strRegisterResult.equals("fail")) // ע��ʧ��
				{
					mHandler.obtainMessage(SUBMIT_FAILED).sendToTarget();// ������Ϣ��Handler
				}

				else if (strRegisterResult.equals("�������쳣")) // �������쳣
				{
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
				}

			}
			catch (Exception e) {
				mThreadFee = null;
			}
			mThreadFee = null;
		}
	}

	// ���߳�UI��Handler����
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case SUBMIT_TO_NET_OK: // �ύ�����ɹ�

				controllersUtil.showToastTwo(mContext,
						getString(R.string.feedback_submit_success),
						Toast.LENGTH_SHORT);

				break;

			case NET_EXCEPTION: // �����쳣

				controllersUtil.showToast(mContext,
						getString(R.string.network_exception),
						Toast.LENGTH_SHORT);
				// startActivity(new
				// Intent(Settings.ACTION_WIRELESS_SETTINGS));//���������������ý���
				break;
			case SERVER_EXCEPTION: // �������쳣

				controllersUtil.showToast(mContext,
						getString(R.string.server_exception),
						Toast.LENGTH_SHORT);
				break;
			case SUBMIT_FAILED: // �ύ����ʧ��

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

	// �ύ����
	private String callService(int userId, String feedback) {
		// ��ѯ����
		String callServiceString = "userid=" + String.valueOf(userId)
				+ "&feedback=" + feedback;
		// url
		String url = HttpUtil.BASE_URL + "FeedbackServlet?"
				+ callServiceString;
		// ��ѯ���ؽ��
		return HttpUtil.queryStringForPost(url);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		switch (keyCode) {

		case KeyEvent.KEYCODE_BACK:
			controllersUtil.showConfirmBack(confrimDlg, "ȷ��Ҫ�˳�������", FeedBackActivity.this);
			break;
		default:
			break;
		}
		return true;
	}
}
