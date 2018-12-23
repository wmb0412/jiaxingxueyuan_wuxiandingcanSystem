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
	protected final int REGISTER_FROM_NET_OK = 0x001;// ��Ϣ:ע��ɹ�
	protected final int NET_EXCEPTION = 0x002;// ��Ϣ:�����쳣
	protected final int REGISTER_FAILED = 0x003;// ��Ϣ��ע��ʧ��
	protected final int SERVER_EXCEPTION = 0x004;// ��Ϣ:�������쳣
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

	private ThreadRegister mRegisterThread; // ȫ���߳�

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
		// ��ʼ���ؼ�
		initViews();

		registerAgreements();
		// ���ü�����
		setListner();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// �������״̬
		ThreadCheckNetwork threadNetWork = new ThreadCheckNetwork();
		threadNetWork.start();
	}

	// ��ʼ���ؼ�
	private void initViews() {
		llRegisterParent = (LinearLayout) findViewById(R.id.ll_register_parent);
		llUsername = (LinearLayout) findViewById(R.id.ll_username); // �û���LinearLayout
		llPassword = (LinearLayout) findViewById(R.id.ll_password); // ����LinearLayout
		llRepassword = (LinearLayout) findViewById(R.id.ll_repassword); // ȷ������LinearLayout

		ivHead = (ImageView) findViewById(R.id.iv_head);
		ivPwd = (ImageView) findViewById(R.id.iv_pwd);
		ivRepwd = (ImageView) findViewById(R.id.iv_repwd);

		edUsername = (AutoCompleteTextView) findViewById(R.id.ed_username);
		edPassword = (EditText) findViewById(R.id.ed_password);
		edRepassword = (EditText) findViewById(R.id.ed_repassword);

		btnBack = (Button) findViewById(R.id.btn_register_back); // ���ذ�ť
		btnRegister = (Button) findViewById(R.id.btn_register); // ע��
		tvAgreements = (TextView) findViewById(R.id.tv_agreements); // �û�Э��
	}

	// ע��Э��
	private void registerAgreements() {
		// ����һ�� SpannableString����
		spannableString = new SpannableString(getResources().getString(
				R.string.user_agreements_title));

		// �����»���
		spannableString.setSpan(new UnderlineSpan(), 7, 15,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		// �������ӣ���Ҫ���setMovementMethod����������Ӧ��
		spannableString.setSpan(new MyClickableSpan(mContext), 7, 15,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		tvAgreements.setText(spannableString);
		tvAgreements.setMovementMethod(LinkMovementMethod.getInstance());

	}

	// ���ü�����
	private void setListner() {

		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				controllersUtil.showConfirmBack(confrimDlg, "ȷ��Ҫ����ע��ô��", RegisterActivity.this);
			}
		});

		// ע�ᰴť�¼�
		btnRegister.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				strUsername = edUsername.getText().toString().trim(); // �û���
				strPassword = edPassword.getText().toString().trim(); // ����
				strRePassword = edRepassword.getText().toString().trim(); // �ظ�����

				// ����������֤
				if (!ValidateUtil.validateInfo(strUsername)) // �û�������Ϊ��
				{
					controllersUtil.showToast(mContext,
							getString(R.string.username_cannot_be_null),
							Toast.LENGTH_SHORT);
					MyAnimation.setTransAnimation(MyAnimation.SHAKE_X,
							edUsername, mContext);
				}
				else if (!ValidateUtil.validateInfo(strPassword)) // ���벻��Ϊ��
				{
					controllersUtil.showToast(mContext,
							getString(R.string.password_cannot_be_null),
							Toast.LENGTH_SHORT);
					MyAnimation.setTransAnimation(MyAnimation.SHAKE_X,
							edPassword, mContext);
				}
				else if (!ValidateUtil.validateLength(strPassword)) // ���벻������6λ
				{

					controllersUtil.showToast(mContext,
							getString(R.string.password_cannot_less_six),
							Toast.LENGTH_SHORT);
					MyAnimation.setTransAnimation(MyAnimation.SHAKE_X,
							edPassword, mContext);
				}
				else if (!ValidateUtil.validateInfo(strRePassword)) // ȷ�����벻��Ϊ��
				{
					controllersUtil.showToast(mContext,
							getString(R.string.repassword_cannot_be_null),
							Toast.LENGTH_SHORT);
					MyAnimation.setTransAnimation(MyAnimation.SHAKE_X,
							edRepassword, mContext);
				}
				else if (!ValidateUtil.validateLength(strRePassword)) // ȷ�����벻������6λ
				{
					controllersUtil.showToast(mContext,
							getString(R.string.repassword_cannot_less_six),
							Toast.LENGTH_SHORT);
					MyAnimation.setTransAnimation(MyAnimation.SHAKE_X,
							edRepassword, mContext);
				}
				else if (!ValidateUtil.validateEquals(strPassword,
						strRePassword)) // �����ȷ�����벻һ��
				{
					controllersUtil.showToast(mContext,
							getString(R.string.password_not_equals_repassword),
							Toast.LENGTH_SHORT);
				}
				else // ͨ����֤����ʼִ��ע��
				{
					mRegisterThread = new ThreadRegister(); // ʵ������¼�߳�
					mRegisterThread.start(); // ������¼�߳�
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
		// �����ı��ı��¼�
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
		// ��������ı��¼�
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
		// ��������ı��¼�
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

	// ע���ύ�߳�
	private class ThreadRegister extends Thread {
		@Override
		public void run() {

			try {
				// �����������
				// if(NetWorkUtil.checkNetworkInfo(mContext) ==
				// NetWorkUtil.DISCONNECTED)
				// {
				// mHandler.obtainMessage(LOGIN_NET_EXCEPTION).sendToTarget();//
				// ������Ϣ��Handler
				// isConnectNetIng = false; //�������ӶϿ�
				// return;
				// }
				String strRegisterResult = register(strUsername, strPassword); // ��ѯ�û����ݿ�

				if (strRegisterResult.equals("success")) // ע��ɹ�
				{
					mHandler.obtainMessage(REGISTER_FROM_NET_OK).sendToTarget();// ������Ϣ��Handler
				}
				else if (strRegisterResult.equals("fail")) // ע��ʧ��
				{
					mHandler.obtainMessage(REGISTER_FAILED).sendToTarget();// ������Ϣ��Handler
				}

				else if (strRegisterResult.equals("�������쳣")) // �������쳣
				{
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
				}

			}
			catch (Exception e) {
				mRegisterThread = null;
			}
			mRegisterThread = null;
		}
	}

	// ���߳�UI��Handler����
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case REGISTER_FROM_NET_OK: // ע��ɹ�

				controllersUtil.showToast(mContext,
						getString(R.string.register_success),
						Toast.LENGTH_SHORT);
				if (pwRegister != null)
					pwRegister.dismiss(); // �ر�popwindow

				RegisterActivity.this.finish();
				startActivity(new Intent(mContext, MainActivity.class));
				break;

			case NET_EXCEPTION: // �����쳣
				if (pwRegister != null)
					pwRegister.dismiss(); // �ر�popwindow

				controllersUtil.showToast(mContext,
						getString(R.string.network_exception),
						Toast.LENGTH_SHORT);
				// startActivity(new
				// Intent(Settings.ACTION_WIRELESS_SETTINGS));//���������������ý���
				break;
			case SERVER_EXCEPTION: // �������쳣
				if (pwRegister != null)
					pwRegister.dismiss(); // �ر�popwindow

				controllersUtil.showToast(mContext,
						getString(R.string.server_exception),
						Toast.LENGTH_SHORT);
				break;
			case REGISTER_FAILED: // ע��ʧ��
				if (pwRegister != null)
					pwRegister.dismiss(); // �ر�popwindow

				controllersUtil.showToast(mContext,
						getString(R.string.login_fail), Toast.LENGTH_SHORT);
				break;
			}
			super.handleMessage(msg);
		}

	};

	// ��ʼ��popWindow��ֵ
	private void initPopupWindow(View parent) {

		if (pwRegister == null) {
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
			tvProgressInfo.setText(getString(R.string.register_ing));

			pwRegister = new PopupWindow(view, LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT);

			// ��popupwindow���ñ�����ɫ:��ɫ����
			ColorDrawable dw = new ColorDrawable(0x7DC0C0C0);
			pwRegister.setBackgroundDrawable(dw);

			pwRegister.setFocusable(true);
			pwRegister.setOutsideTouchable(false);

			// ����popupwindow���ֵĶ���
			pwRegister.setAnimationStyle(android.R.style.Animation_Toast);
		}
		pwRegister.showAtLocation(parent, Gravity.CENTER, 0, 0);

		// ����popmenu�ر�
		pwRegister.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				mRegisterThread = null;
			}
		});
	}

	// �����û�ע����Ϣ
	private String register(String strUsername, String strPassword) {
		// ��ѯ����
		String queryString = "username=" + strUsername + "&password="
				+ strPassword;
		// url
		String url = HttpUtil.BASE_URL + "RegisterServlet?"
				+ queryString;

		// ��ѯ���ؽ��
		return HttpUtil.queryStringForPost(url);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		switch (keyCode) {

		case KeyEvent.KEYCODE_BACK:
			controllersUtil.showConfirmBack(confrimDlg, "ȷ��Ҫ����ע��ô��", RegisterActivity.this);
			break;
		default:
			break;
		}
		return true;
	}
}