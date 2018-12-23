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
	protected final int CALL_FROM_NET_OK = 0x001;// ��Ϣ�����гɹ�
	protected final int NET_EXCEPTION = 0x002;// ��Ϣ�������쳣
	protected final int CALL_FAILED = 0x003;// ��Ϣ������ʧ��
	protected final int SERVER_EXCEPTION = 0x004;// ��Ϣ:�������쳣
	protected final int GET_SAME_VERSION = 0x005;
	protected final int GET_NEW_VERSION = 0x006;
	protected final int GET_UNDATEINFO_ERROR = 0x007;

	private Context mContext;
	private ControllersUtil controllersUtil;
	private LinearLayout llCallService;
	private LinearLayout llFeedback;
	private LinearLayout llCheckNewVersion; // ����°汾
	private LinearLayout llAbout;
	private TextView tvLoginOut;
	private PopupWindow pwMore;
	private TextView tvCheckTable; // ѡ��
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

		isLoginOutVisile(); // �˳���¼��ť�Ƿ�ɼ�
	}

	@Override
	protected void onResume() {
		if (LoginActivity.isLoginSuccess) {
			tvLoginOut.setVisibility(View.VISIBLE);
			LoginActivity.isLoginSuccess = false;
		}
		super.onResume();
	}

	// �˳���¼��ť�Ƿ�ɼ�
	private void isLoginOutVisile() {
		if (UserParam.userId == 0) {
			String userId = BrazucaDBGetInfoUtil.getDBUserId(mContext);// ��ȡ�û�id��
			if (userId.equals("") || userId == null) // ��ȡ�����û�id�ţ����ж�Ϊδ��¼
			{
				UserParam.isLogin = false;
			}
			else // �Ѿ���¼
			{
				UserParam.userId = Integer.parseInt(userId);// ��ȡ�û�id��
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

	// ��ʼ���ؼ�
	private void initViews() {
		llCallService = (LinearLayout) findViewById(R.id.ll_call_service);
		llFeedback = (LinearLayout) findViewById(R.id.ll_feedback);
		llCheckNewVersion = (LinearLayout) findViewById(R.id.ll_check_new_version);
		llAbout = (LinearLayout) findViewById(R.id.ll_about);

		tvLoginOut = (TextView) findViewById(R.id.tv_login_out); // �˳���¼
		tvCheckTable = (TextView) this.findViewById(R.id.tv_check_table); // ѡ��

	}

	// ���ü����¼�
	private void setListner() {
		// ѡ��
		tvCheckTable.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// ���δ��⵽�û���¼������ת����¼����
				if (!UserParam.isLogin) {
					startActivity(new Intent(MoreActivity.this,
							LoginActivity.class));
				}
				else
					startActivity(new Intent(MoreActivity.this,
							TableActivity.class));

			}
		});

		// �˳���¼
		tvLoginOut.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				BrazucaDBUtil dbUtil = new BrazucaDBUtil(mContext);

				// ɾ��Ψһ�û�����
				if (dbUtil.delete(BrazucaDBHelper.TB_USER,
						String.valueOf(UserParam.userId))) {
					controllersUtil.showToast(mContext, "�˳���¼�ɹ�",
							Toast.LENGTH_SHORT);
					tvLoginOut.setVisibility(View.GONE);
					UserParam.isLogin = false; // ��¼״̬Ϊ������¼��
				}
				else {
					controllersUtil.showToast(mContext, "�˳���¼�쳣��������",
							Toast.LENGTH_SHORT);
				}

				dbUtil.close(); // �ر����ݿ�

			}
		});

		// ���з���Ա
		llCallService.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View parent, MotionEvent motionEvent) {

				if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
					initPopupWindow(llCallService);

					// �������з�����߳�
					mCallServiceThread = new ThreadCallService();
					mCallServiceThread.start();
				}

				return false;
			}
		});

		// �������
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

		// ����°汾
		llCheckNewVersion.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View parent, MotionEvent motionEvent) {

				if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
					controllersUtil.showProgressWindow(progressDlg,
							"���ڼ���°汾�����Ժ�");
					new Thread(new CheckVersionTask()).start();
				}

				return false;
			}
		});

		// ���ڽ���
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
	 * ��ȡ��ǰ����İ汾��
	 */
	private String getVersionName() throws Exception {
		// ��ȡpackagemanager��ʵ��
		PackageManager packageManager = getPackageManager();
		// getPackageName()���㵱ǰ��İ�����0�����ǻ�ȡ�汾��Ϣ
		PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),
				0);
		return packInfo.versionName;
	}

	/*
	 * ��pull�������������������ص�xml�ļ� (xml��װ�˰汾��)
	 */
	public static UpdateInfo getUpdataInfo(InputStream is) throws Exception {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(is, "utf-8");// ���ý���������Դ
		int type = parser.getEventType();
		UpdateInfo info = new UpdateInfo();// ʵ��
		while (type != XmlPullParser.END_DOCUMENT) {
			switch (type) {
			case XmlPullParser.START_TAG:
				if ("version".equals(parser.getName())) {
					info.setVersion(parser.nextText()); // ��ȡ�汾��
				}
				else if ("url".equals(parser.getName())) {
					info.setUrl(parser.nextText()); // ��ȡҪ������APK�ļ�
				}
				else if ("description".equals(parser.getName())) {
					info.setDescription(parser.nextText()); // ��ȡ���ļ�����Ϣ
				}
				break;
			}
			type = parser.next();
		}
		return info;
	}

	/*
	 * �ӷ�������ȡxml���������бȶ԰汾��
	 */
	public class CheckVersionTask implements Runnable {

		public void run() {
			try {
				// ����Դ�ļ���ȡ������ ��ַ
				String path = HttpUtil.BASE_URL + "update.xml";
				// ��װ��url�Ķ���
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
					mHandler.obtainMessage(GET_SAME_VERSION).sendToTarget();// ������Ϣ��Handler
				}
				else {
					mHandler.obtainMessage(GET_NEW_VERSION,versionDescription).sendToTarget();// ������Ϣ��Handler
				}
			}
			catch (Exception e) {
				// ������
				mHandler.obtainMessage(GET_UNDATEINFO_ERROR).sendToTarget();// ������Ϣ��Handler

				e.printStackTrace();
			}
		}
	}

	// ������������߳�
	private class ThreadCheckNetwork extends Thread {
		@Override
		public void run() {

			try {
				NetWorkUtil.checkNetworkInfo(mContext);

				if (NetWorkUtil.checkNetworkInfo(mContext) == NetWorkUtil.DISCONNECTED) {
					mHandler.obtainMessage(NET_EXCEPTION).sendToTarget();// ������Ϣ��Handler
				}
			}
			catch (Exception e) {
			}

		}
	}

	// ���з���Ա�߳�
	private class ThreadCallService extends Thread {

		@Override
		public void run() {
			Log.d("ThreadCallService", "ThreadCallService");
			try {
				isConnectNetIng = true;

				while (isConnectNetIng) {
					// �����������
					if (NetWorkUtil.checkNetworkInfo(mContext) == NetWorkUtil.DISCONNECTED) {
						mHandler.obtainMessage(NET_EXCEPTION).sendToTarget();// ������Ϣ��Handler
						isConnectNetIng = false; // �������ӶϿ�
						return;
					}

					String strGetResponse = callService("���з���Ա", "10");

					Log.d("strGetResponse", strGetResponse);

					if (strGetResponse.equals("success")) // ���з���Ա�ɹ�
					{
						mHandler.obtainMessage(CALL_FROM_NET_OK).sendToTarget();// ������Ϣ��Handler
					}
					else if (strGetResponse.equals("fail")) {
						mHandler.obtainMessage(CALL_FAILED).sendToTarget();// ������Ϣ��Handler
					}
					else if (strGetResponse.equals("�������쳣")) {
						mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
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

	// ���߳�UI��Handler����
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
				// Ϊȷ�ϰ�ť����¼�
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
						"û�м�⵽�°汾", Toast.LENGTH_SHORT);
				break;
			case GET_UNDATEINFO_ERROR:
				controllersUtil.showToast(mContext,
						"����δ֪�쳣��������", Toast.LENGTH_SHORT);
				break;

			case CALL_FROM_NET_OK: // ���гɹ�

				controllersUtil.showToast(mContext,
						getString(R.string.call_success), Toast.LENGTH_SHORT);
				if (pwMore != null) {
					pwMore.dismiss(); // �ر�popwindow
					pwMore = null;
				}

				// startActivity(new Intent(mContext, MainActivity.class));
				break;

			case NET_EXCEPTION: // �����쳣
				if (pwMore != null)
					pwMore.dismiss(); // �ر�popwindow

				controllersUtil.showToast(mContext,
						getString(R.string.network_exception),
						Toast.LENGTH_SHORT);
				// startActivity(new
				// Intent(Settings.ACTION_WIRELESS_SETTINGS));//���������������ý���
				break;
			case SERVER_EXCEPTION: // �������쳣
				if (pwMore != null)
					pwMore.dismiss(); // �ر�popwindow

				controllersUtil.showToast(mContext,
						getString(R.string.server_exception),
						Toast.LENGTH_SHORT);
				break;
			case CALL_FAILED: // ����ʧ��
				if (pwMore != null)
					pwMore.dismiss(); // �ر�popwindow

				controllersUtil.showToast(mContext,
						getString(R.string.call_fail), Toast.LENGTH_SHORT);
				break;
			}
			super.handleMessage(msg);
		}

	};

	// ��ʼ��popWindow��ֵ
	private void initPopupWindow(View parent) {

		if (pwMore == null) {
			// ����popupwindow��layout�����ļ�
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = layoutInflater.inflate(
					R.layout.overall_progress_popwindow, null);

			ImageView ivLogo = (ImageView) view
					.findViewById(R.id.iv_progress_logo);
			MyAnimation.setTransAnimation(MyAnimation.SHAKE_INFINITE, ivLogo,
					mContext);

			// ��popupwindow�����TextView����������ʾ
			TextView tvProgressInfo = (TextView) view
					.findViewById(R.id.tv_progress_info);
			tvProgressInfo.setText(getString(R.string.calling_service));

			pwMore = new PopupWindow(view, LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT);

			// ��popupwindow���ñ�����ɫ:��ɫ����
			ColorDrawable dw = new ColorDrawable(0x7DC0C0C0);
			pwMore.setBackgroundDrawable(dw);

			pwMore.setFocusable(true);
			pwMore.setOutsideTouchable(false);

			// ����popupwindow���ֵĶ���
			pwMore.setAnimationStyle(android.R.style.Animation_Toast);
		}

		pwMore.showAtLocation(parent, Gravity.CENTER, 0, 0);
		// pwMore.showAsDropDown(parent);

		// ����popmenu�ر�
		pwMore.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				// isConnectNetIng = false;
				// mLoginThread = null;
			}
		});
	}

	// ���з���Ա
	private String callService(String callMessage, String tableId) {
		// ��ѯ����
		String callServiceString = "callMessage=" + callMessage + "&tableId="
				+ tableId;
		// url
		String url = HttpUtil.BASE_URL + "CustomerCallServlet?"
				+ callServiceString;
		// ��ѯ���ؽ��
		return HttpUtil.queryStringForPost(url);
	}
}
