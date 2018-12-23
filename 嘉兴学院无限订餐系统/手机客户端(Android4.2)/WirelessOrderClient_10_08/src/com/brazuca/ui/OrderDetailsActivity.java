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
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brazuca.entity.OrderEntity;
import com.brazuca.entity.UserParam;
import com.brazuca.network.HttpUtil;
import com.brazuca.ui.adapter.OrderDetailsAdapter;
import com.brazuca.ui.view.PullToRefreshView;
import com.brazuca.ui.view.PullToRefreshView.OnFooterRefreshListener;
import com.brazuca.ui.view.PullToRefreshView.OnHeaderRefreshListener;
import com.brazuca.util.ControllersUtil;

//user+�����+��ˮ��(��ˮ��Ϊȫ�ֵ�������)  �����������������µ������
//�л����û���Ӧ��ˢ�²˵�??

public class OrderDetailsActivity extends Activity implements
		OnHeaderRefreshListener, OnFooterRefreshListener {
	protected final int GET_FROM_NET_OK = 0x001;// ��Ϣ:ע��ɹ�
	protected final int NET_EXCEPTION = 0x002;// ��Ϣ:�����쳣
	protected final int GET_FAILED = 0x003;// ��Ϣ��ע��ʧ��
	protected final int SERVER_EXCEPTION = 0x004;// ��Ϣ:�������쳣
	protected final int START_TO_SELECT_SINGLE_COUNT_AC = 0X005; // ��ת��ѡ��Ʒ����Activity
	protected final int INSERT_ORDER_SUCCESS = 0x006; // ��Ϣ���ύ�����ɹ�
	protected final int INSERT_ORDER_FAILED = 0x007; // ��Ϣ���ύ����ʧ��
	protected final int UPDATE_ORDER_SUCCESS = 0x008; // ��Ϣ�����¶����ɹ�
	protected final int UPDATE_ORDER_FAILED = 0x009; // ��Ϣ�����¶���ʧ��
	protected final int GET_USER_INFO_OK = 0x010; // ��Ϣ:��ȡ�û����ϳɹ�
	protected final int GET_USER_INFO_FAILED = 0x011; // ��Ϣ��ȡ�û�����ʧ��

	private static final int DELETE_SINGLE_DISH = 1; // ɾ����Ʒ

	private static final int ORDER_DETAILS_SHOW = 1;
	private static final int SELECT_PAY_TYPE = 2;
	private static final int FINAL_SUBMIT = 3;

	private static final int ACCOUNT_PAY = 1; // �˻�֧��
	private static final int ONLINE_PAY = 2; // ����֧��
	private static final int RECEPTION_PAY = 3; // ǰ̨֧��

	private Context mContext;
	private ControllersUtil controllersUtil;
	private RelativeLayout relErrorMsg; // ������ϢRelativeLayout
	private TextView tvErrorMsg; // ������ϢTextView
	private ListView lvOrderDetails; // ����ListView

	private TextView tvSubmitOrder; // �ύ����
	private TextView tvBack; // ���ذ�ť
	private TextView tvOrderCount; // ��Ʒ����
	private TextView tvPayPrice; // ֧���ܼ�TextView
	private TextView tvPayType; // ֧������
	private TextView tvOrderTopBar; // ������
	private RelativeLayout relOrderDetails; // ��������
	private RelativeLayout relSelectCountParent; // ѡ��֧������
	private RadioGroup radioGroup;

	private int iPayType = ACCOUNT_PAY; // ֧����ʽ��Ĭ��֧����ʽ�� ���˻�֧��"
	// private int orderId; // �����µĶ�����
	private double newBalance=0d;
	private double totalpayPrice = 0d; // �����µĶ�����
	private int dishClassifcationCount; // ���µ��ĵ�Ʒ��
	private PullToRefreshView mPullToRefreshView; // ����ˢ����ͼ

	ArrayList<OrderEntity> listitem; // ��Ų˵���Ϣ��ArrayList
	private OrderDetailsAdapter adapter; // ������

	private int deleteItemIndex = 0;
	private boolean isDeteleDish = false; // �Ƿ���ɾ����Ʒ
	private boolean isRefreshHeader = false; // �Ƿ�ͷ��ˢ��
	private static int iStep = ORDER_DETAILS_SHOW; // ҳ��Ĳ���
	private static int checkId = R.id.rb_account_pay;
	private String updateDishName; // �����µĵ�Ʒ����
	private int updateCount; // �����²�Ʒ������

	private String orderId = "";
	private int tableId = 0;
	private boolean isSubmit = false;
	private boolean isEmptyFood = false;

	public static boolean isUpdateOrder = false;

	private ThreadGetOrders mGetOrderThread; // ��ȡ�˵��߳�ʵ��
	private ThreadUpdateOrders mUpdateOrderCount; // ���²˵��߳�ʵ��
	private ThreadUpdateOrders mDeteleSingleDish; // ɾ����Ʒ�߳�ʵ��
	private ThreadInsertOrders mInsertOrders; // ���붩�����߳�ʵ��
	private ThreadGetUserInfo mGetUserInfoThread; // ��ȡ�û������߳�
	private ThreadUpdateUserInfo threadUpdateUserInfo;

	private AlertDialog dlgProgress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order_details);
		mContext = OrderDetailsActivity.this;
		dlgProgress = new AlertDialog.Builder(mContext).create();
		
		controllersUtil = new ControllersUtil();
		listitem = new ArrayList<OrderEntity>();

		initViews(); // ��ʼ���ؼ���Ϣ
		setPullViewListner(); // ���ÿؼ��ļ����¼�
		setLvCreateContextMenuListener();

		// ��ȡHomeActivity������ֵ��������Ӧ�ؼ���������
		Bundle bundle = new Bundle();
		bundle = getIntent().getExtras();

		if (bundle != null) {

			orderId = bundle.getString("order_id"); // ������
			tableId = Integer.parseInt(bundle.getString("table_id"));
			isSubmit = bundle.getBoolean("is_submit");// �Ƿ��ύ����
			isEmptyFood = bundle.getBoolean("is_empty_food");// �Ƿ�����

			System.out.println("isSubmit=" + isSubmit);

			if (!isEmptyFood) // ����˵�û��
			{
				changeViewVisibility();
				return;
			}
		}

		// ����û���¼�������ͷ��ˢ��
		if (UserParam.isLogin) {
			mPullToRefreshView.headerRefreshing();
		}
		// ������ύ��������ĵ�һ������--����������Ϣҳ�棬���÷��ؼ����ɼ�
		if (iStep == ORDER_DETAILS_SHOW) {
			tvBack.setVisibility(View.GONE);
		}
	}

	// ���ÿؼ��Ŀɼ���
	private void changeViewVisibility() {
		tvBack.setVisibility(View.GONE);
		tvSubmitOrder.setVisibility(View.GONE);
		relErrorMsg.setVisibility(View.VISIBLE);
		tvErrorMsg.setText(getString(R.string.no_dish_msg));
		relOrderDetails.setVisibility(View.GONE);
	}

	// ����listview�����˵��¼�����
	private void setLvCreateContextMenuListener() {
		lvOrderDetails.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View view,
					ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				final int iItemIdx = info.position;
				deleteItemIndex = iItemIdx;

				menu.setHeaderTitle("ѡ����");
				menu.add(Menu.NONE, DELETE_SINGLE_DISH, 0, "ɾ����Ʒ")
						.setOnMenuItemClickListener(
								new OnMenuItemClickListener() {

									@Override
									public boolean onMenuItemClick(MenuItem item) {

										updateDishName = listitem.get(iItemIdx)
												.getDishName();

										// ����ɾ����Ʒ�߳�
										mDeteleSingleDish = new ThreadUpdateOrders(
												3);
										mDeteleSingleDish.start();

										return false;
									}

								});
			}
		});
	}

	@Override
	protected void onResume() {
		System.out.println("UserParam.isInsertDish=" + UserParam.isInsertDish);

		// ����û������˵�Ʒ�����ĸ��ģ���ˢ�²˵�����Ϣ
		if (SelectSingleCountActivity.isFromSelectCount) {
			// listitem.get(adapter.position).setCount(SelectSingleCountActivity.curCount+1);
			// //adapter.notifyDataSetChanged();
			// adapter = new OrderAdapter(mContext, listitem);
			// adapter.notifyDataSetInvalidated();

			SelectSingleCountActivity.isFromSelectCount = false;

			updateDishName = listitem.get(adapter.position).getDishName();
			updateCount = SelectSingleCountActivity.curCount + 1;

			// �������·����߳�
			mUpdateOrderCount = new ThreadUpdateOrders(1);
			mUpdateOrderCount.start();

		}

		else if (UserParam.isInsertDish) // ����û�����˲�Ʒ
		{
			UserParam.isInsertDish = false;
			mGetOrderThread = new ThreadGetOrders();
			mGetOrderThread.start();
		}

		super.onResume();
	}

	// ��ʼ���ؼ�
	private void initViews() {
		relErrorMsg = (RelativeLayout) findViewById(R.id.rel_error_msg);
		tvErrorMsg = (TextView) findViewById(R.id.tv_error_msg);

		mPullToRefreshView = (PullToRefreshView) findViewById(R.id.order_pull_refresh_view);// ����ˢ����ͼ
		tvSubmitOrder = (TextView) findViewById(R.id.tv_submit_order); // �ύ����
		tvBack = (TextView) findViewById(R.id.tv_submit_back);

		tvOrderCount = (TextView) findViewById(R.id.tv_order_count_msg);
		tvPayPrice = (TextView) findViewById(R.id.tv_pay_price_msg);
		tvPayType = (TextView) findViewById(R.id.tv_pay_type_msg);
		tvOrderTopBar = (TextView) findViewById(R.id.tv_order_topbar);

		lvOrderDetails = (ListView) findViewById(R.id.lv_order);

		relOrderDetails = (RelativeLayout) findViewById(R.id.rel_order_details);
		relSelectCountParent = (RelativeLayout) findViewById(R.id.rel_select_count_parent);
		radioGroup = (RadioGroup) findViewById(R.id.rg_select_pay_type);

		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				checkId = checkedId;
				System.out.println("checkId=" + checkId);
				// group.getc
			}
		});
	}

	// �ж�֧������
	private String getPayPureType(int iType) {
		switch (iType) {
		case R.id.rb_account_pay:
			iPayType = ACCOUNT_PAY;
			return "�˺�֧��";
		case R.id.rb_online_pay:
			iPayType = ONLINE_PAY;
			return "����֧��";
		case R.id.rb_reception_pay:
			iPayType = RECEPTION_PAY;
			return "ǰ̨֧��";

		}
		return "";
	}

	// ������ϸ�˵���Ϣ��ֵ
	private void setViews() {
		tvOrderCount.setText(dishClassifcationCount + "");
		tvPayPrice.setText(totalpayPrice + "Ԫ");
		tvPayType.setText(getPayPureType(checkId));
		// tvOrderDetails.setText("���ܹ�����"+dishClassifcationCount+
		// "�ֲˣ���֧��"+totalpayPrice+"Ԫ\n"+getPayType(checkId));

	}

	// private void setNextStepViews(int iStep)
	// {
	// switch (iStep) {
	// case ORDER_DETAILS_SHOW:
	// iStep = SELECT_PAY_TYPE;
	// tvOrderTopBar.setText("��ѡ��֧������");
	// tvBack.setVisibility(View.VISIBLE);
	//
	// changeView(relSelectCountParent,mPullToRefreshView,relOrderDetails);
	// break;
	// case SELECT_PAY_TYPE:
	// iStep = FINAL_SUBMIT;
	// tvOrderTopBar.setText("��������");
	// setViews();
	// changeView2(mPullToRefreshView,relOrderDetails,relSelectCountParent);
	// tvSubmitOrder.setText("�ύ����");
	// break;
	//
	// case FINAL_SUBMIT:
	// mInsertOrders = new ThreadInsertOrders();
	// mInsertOrders.start();
	// break;
	//
	// default:
	// break;
	// }
	// }

	// ���ÿؼ��ļ����¼�
	private void setPullViewListner() {
		mPullToRefreshView.setOnHeaderRefreshListener(this);
		mPullToRefreshView.setOnFooterRefreshListener(this);

		// �ύ������Ӧ�¼�
		tvSubmitOrder.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (iStep == ORDER_DETAILS_SHOW) {
					iStep = SELECT_PAY_TYPE;
					tvOrderTopBar.setText("ѡ��֧����ʽ");
					tvBack.setVisibility(View.VISIBLE);

					changeView(relSelectCountParent, mPullToRefreshView,
							relOrderDetails);
				}
				else if (iStep == SELECT_PAY_TYPE) {
					iStep = FINAL_SUBMIT;
					tvOrderTopBar.setText("��������");
					setViews();
					changeView2(mPullToRefreshView, relOrderDetails,
							relSelectCountParent);
					tvSubmitOrder.setText("�ύ����");
				}
				else if (iStep == FINAL_SUBMIT) // �ύ
				{

					final AlertDialog dlg = new AlertDialog.Builder(
							mContext).create();

					dlg.show();

					Window window = dlg.getWindow();

					window.setContentView(R.layout.bg_confirm_dialog);

					TextView tvConfrimMsg = (TextView) window
							.findViewById(R.id.tv_confrim_msg);
					tvConfrimMsg.setText("ȷ���ύ������");
					// Ϊȷ�ϰ�ť����¼�
					TextView ok = (TextView) window
							.findViewById(R.id.btn_ok);
					TextView cancel = (TextView) window
							.findViewById(R.id.btn_cancel);

					ok.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							dlg.dismiss();

							switch (iPayType) {
							case ACCOUNT_PAY:
								if (!isBalacePlus(UserParam.balance)) // �˻�����
								{
									controllersUtil
											.showToast(
													mContext,
													getString(R.string.balace_negative),
													Toast.LENGTH_SHORT);
									return;
								}

								break;
							case ONLINE_PAY:
								break;
							case RECEPTION_PAY:
								break;

							default:
								break;
							}

							// �����ύ�����߳�
							mInsertOrders = new ThreadInsertOrders();
							mInsertOrders.start();
							controllersUtil.showProgressWindow(dlgProgress, "�����ύ����");
						}
					});

					cancel.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							dlg.dismiss();
						}
					});

				}
				
			}
		});

		// ������Ӧ�¼�
		tvBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (iStep == FINAL_SUBMIT) {
					// setStepViews(iStep);
					iStep = SELECT_PAY_TYPE;
					tvOrderTopBar.setText("ѡ��֧����ʽ");
					changeView(relSelectCountParent, mPullToRefreshView,
							relOrderDetails);

					tvSubmitOrder.setText("��һ��");
				}
				else if (iStep == SELECT_PAY_TYPE) {
					iStep = ORDER_DETAILS_SHOW;
					setViews();

					changeView2(mPullToRefreshView, relOrderDetails,
							relSelectCountParent);

					tvSubmitOrder.setText("��һ��");
					tvBack.setVisibility(View.GONE);
					tvOrderTopBar.setText(getString(R.string.app_name));
				}

			}
		});

	}

	// �ж��˻�����Ƿ����
	private boolean isBalacePlus(double balance) {
		if (balance >= totalpayPrice) {
			return true;
		}
		else
			return false;
	}

	// private void showConfrimDialog() {
	//
	// }

	/*
	 * �ı�ؼ��Ŀɼ���
	 */
	private void changeView(View... args) {
		args[0].setVisibility(View.VISIBLE);
		args[1].setVisibility(View.GONE);
		args[2].setVisibility(View.GONE);
	}

	/*
	 * �ı�ؼ��Ŀɼ���2
	 */
	private void changeView2(View... args) {
		args[0].setVisibility(View.VISIBLE);
		args[1].setVisibility(View.VISIBLE);
		args[2].setVisibility(View.GONE);
	}

	/*
	 * ��ArrayList(listitem)��ֵ
	 */
	private ArrayList<OrderEntity> createData(String obj) {
		try {
			JSONArray jsArray = new JSONArray(obj);
			OrderEntity entity = null;

			dishClassifcationCount = jsArray.length();
			totalpayPrice = 0d; // ˢ��ǰ�����ܼ۵���0

			for (int i = 0; i < dishClassifcationCount; i++) {
				entity = new OrderEntity();
				JSONObject js = new JSONObject();
				js = jsArray.getJSONObject(i);

				String dishName = js.getString("dish_name");
				int count = Integer.parseInt(js.getString("count"));
				double paySinglePrice = Double.parseDouble(js
						.getString("pay_single_price")); // ��Ʒ�۸�
				double paySingleTotalPrice = paySinglePrice * count; // ��Ʒ�ܼ۸�

				totalpayPrice += paySingleTotalPrice; // �ܼ۸�

				entity.setDishName(dishName);
				entity.setCount(count);
				entity.setPaySinglePrice(paySinglePrice);

				listitem.add(entity);
			}

		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listitem;

	}

	/*
	 * ���²˵��߳�
	 */
	private class ThreadUpdateOrders extends Thread {
		private int updateType;

		public ThreadUpdateOrders(int updateType) {
			this.updateType = updateType;
		}

		@Override
		public void run() {
			try {
				String result = "";

				result = updateOrders(orderId, updateDishName, updateCount,
						updateType);

				if (result.equals("fail")) // ���¶���ʧ��
				{
					mHandler.obtainMessage(UPDATE_ORDER_FAILED).sendToTarget();// ������Ϣ��Handler
				}

				else if (result.equals("�������쳣")) // �������쳣
				{
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
				}
				else // ���¶����ɹ�
				{
					if (updateType == 3) // ɾ����Ʒ����
					{
						isDeteleDish = true;
					}
					mHandler.obtainMessage(UPDATE_ORDER_SUCCESS, result)
							.sendToTarget();// ������Ϣ��Handler
				}

			}
			catch (Exception e) {
				mUpdateOrderCount = null;
			}

			mUpdateOrderCount = null;

		}
	}

	/*
	 * �ύ�����߳�
	 */
	private class ThreadInsertOrders extends Thread {
		@Override
		public void run() {
			try {
				String result ="";
				if(iPayType == ACCOUNT_PAY)
				{
					result = insertOrders(orderId, UserParam.userId,
							tableId, totalpayPrice, true, getPayPureType(checkId),
							false);
				}
				else
				{
					result = insertOrders(orderId, UserParam.userId,
							tableId, totalpayPrice, false, getPayPureType(checkId),
							false);
				}
				

				if (result.equals("fail")) // �ύ����ʧ��
				{
					mHandler.obtainMessage(INSERT_ORDER_FAILED).sendToTarget();// ������Ϣ��Handler
				}

				else if (result.equals("�������쳣")) // �������쳣
				{
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
				}
				else // �ύ�����ɹ�
				{
					mHandler.obtainMessage(INSERT_ORDER_SUCCESS, result)
							.sendToTarget();// ������Ϣ��Handler
				}

			}
			catch (Exception e) {
				mInsertOrders = null;
			}

			mInsertOrders = null;

		}
	}

	/*
	 * ��ȡ��ϸ�˵��߳�
	 */
	private class ThreadGetOrders extends Thread {
		@Override
		public void run() {
			try {

				String result = getOrdersDetails(orderId);

				if (result.equals("fail")) // ע��ʧ��
				{
					mHandler.obtainMessage(GET_FAILED).sendToTarget();// ������Ϣ��Handler
				}

				else if (result.equals("�������쳣")) // �������쳣
				{
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
				}
				else {
					mHandler.obtainMessage(GET_FROM_NET_OK, result)
							.sendToTarget();// ������Ϣ��Handler
				}

			}
			catch (Exception e) {
				mGetOrderThread = null;
			}

			mGetOrderThread = null;

		}
	}

	/*
	 * ����Activity����ת
	 */
	private void startToNewActivity() {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("single_count", listitem.get(adapter.position)
				.getCount() + "");

		intent.setClass(OrderDetailsActivity.this,
				SelectSingleCountActivity.class);

		intent.putExtras(bundle);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
	}

	/*
	 * ����ϵͳ��Ϣ���߳�
	 */
	class ThreadSysMsgEvent extends Thread {
		@Override
		public void run() {
			while (true) {
				try {

					if (adapter.isClickCount && !isSubmit) {
						mHandler.obtainMessage(START_TO_SELECT_SINGLE_COUNT_AC)
								.sendToTarget();// ������Ϣ��Handler
					}

					Thread.sleep(200);
				}
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}
	
	// �����û������߳�
	private class ThreadUpdateUserInfo extends Thread {
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

				newBalance = UserParam.balance - totalpayPrice;  //ʣ���˻��ʽ�
				String strLoginResult = updateUserInfo(UserParam.userId,1,newBalance); // ��ѯ�û����ݿ�

				if (strLoginResult.equals("fail")) {
					mHandler.obtainMessage(GET_USER_INFO_FAILED).sendToTarget();// ������Ϣ��Handler
				}
				else if (strLoginResult.equals("�������쳣")) {
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
				}
				else {
					mHandler.obtainMessage(GET_USER_INFO_OK, strLoginResult)
							.sendToTarget(); // ����װ��json����strLoginResult������Ϣ��Handler
				}

			}
			catch (Exception e) {
				threadUpdateUserInfo = null;
			}
			threadUpdateUserInfo = null;
		}
	}

	// ��ȡ�û������߳�
	private class ThreadGetUserInfo extends Thread {
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

				String strLoginResult = getUserInfo(UserParam.userId); // ��ѯ�û����ݿ�

				if (strLoginResult != null && strLoginResult.equals("null")) {
					mHandler.obtainMessage(GET_USER_INFO_FAILED).sendToTarget();// ������Ϣ��Handler
				}
				else if (strLoginResult.equals("�������쳣")) {
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
				}
				else {
					mHandler.obtainMessage(GET_USER_INFO_OK, strLoginResult)
							.sendToTarget(); // ����װ��json����strLoginResult������Ϣ��Handler
				}

			}
			catch (Exception e) {
				mGetUserInfoThread = null;
			}
			mGetUserInfoThread = null;
		}
	}

	// ��ȡ�û���Ϣ�����ݸ�ʽ--json����
	private void setUserInfoFromNet(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			int vipType = jsonObject.getInt("vip_type");
			double balance = jsonObject.getDouble("balance");

			UserParam.vipType = vipType;
			UserParam.balance = balance;
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// ���߳�UI��Handler����
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case GET_USER_INFO_OK: // �ɹ���ȡ�û�����

				String strUser = "";
				strUser = (String) (msg.obj);
				setUserInfoFromNet(strUser); // �����û�����

				break;
			case GET_USER_INFO_FAILED:

				break;

			case UPDATE_ORDER_SUCCESS:
				controllersUtil.showToast(mContext, "���³ɹ�", Toast.LENGTH_SHORT);

				mPullToRefreshView.headerRefreshing();
				isUpdateOrder = true;
				setViews();
				break;

			case UPDATE_ORDER_FAILED:
				controllersUtil.showToast(mContext, "���·���ʧ��",
						Toast.LENGTH_SHORT);
				break;

			case INSERT_ORDER_SUCCESS:

				controllersUtil.showToast(mContext, "�ύ�����ɹ���������ζ��",
						Toast.LENGTH_SHORT);
				isUpdateOrder = true;
				threadUpdateUserInfo = new ThreadUpdateUserInfo();
				threadUpdateUserInfo.start();
				tvSubmitOrder.setVisibility(View.GONE);
				tvBack.setVisibility(View.GONE);
				lvOrderDetails.setLongClickable(false);
				lvOrderDetails.setClickable(false);
				isSubmit = true;
				break;
			case INSERT_ORDER_FAILED:
				
				controllersUtil.showToast(mContext, "�ύ����ʧ�ܣ��������ύ",
						Toast.LENGTH_SHORT);
				break;

			case START_TO_SELECT_SINGLE_COUNT_AC:

				startToNewActivity();
				adapter.isClickCount = false;
				break;

			case GET_FROM_NET_OK: // ����ɹ�

				String strJson = "";
				strJson = (String) (msg.obj);

				if (listitem.size() > 0)
					listitem.clear();

				adapter = new OrderDetailsAdapter(mContext, createData(strJson));

				lvOrderDetails.setAdapter(adapter);

				if (isSubmit) {
					lvOrderDetails.setClickable(false);
					lvOrderDetails.setLongClickable(false);
				}

				setViews();
				new Thread(new ThreadSysMsgEvent()).start(); // ������������߳�
				if (isRefreshHeader) {
					mPullToRefreshView.onHeaderRefreshComplete();
					isRefreshHeader = false;
				}

				if (isSubmit) // ��������ύ�ˣ������û��������ύ����
				{
					tvBack.setVisibility(View.GONE);
					tvSubmitOrder.setVisibility(View.GONE);
				}
				else {
					// ������ȡ�û������߳�
					mGetUserInfoThread = new ThreadGetUserInfo();
					mGetUserInfoThread.start();
				}

				// if(!mPullToRefreshView.isShown())
				// mPullToRefreshView.setVisibility(View.VISIBLE);
				//
				// if(!relOrderDetails.isShown())
				// relOrderDetails.setVisibility(View.VISIBLE);
				//
				// if(!tvSubmitOrder.isShown())
				// tvSubmitOrder.setVisibility(View.VISIBLE);

				// if(relErrorMsg.isShown())
				// relErrorMsg.setVisibility(View.GONE);
				//
				// if(tvBack.isShown()&&iStep == ORDER_DETAILS_SHOW)
				// tvBack.setVisibility(View.GONE);

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
			case GET_FAILED: // ����ʧ��

				controllersUtil.showToast(mContext,
						getString(R.string.no_dish_msg), Toast.LENGTH_SHORT);
				relErrorMsg.setVisibility(View.VISIBLE);
				relOrderDetails.setVisibility(View.GONE);
				tvErrorMsg.setText(getString(R.string.no_dish_msg));
				tvSubmitOrder.setVisibility(View.GONE);
				mPullToRefreshView.onHeaderRefreshComplete();
				if (mPullToRefreshView.isShown())
					mPullToRefreshView.setVisibility(View.GONE);
				break;
			}
			if(dlgProgress.isShowing())
			{
				controllersUtil.hideProgressWindow(dlgProgress);
			}
			
			super.handleMessage(msg);
		}
	};

	/*
	 * ��������ύ���²˵�����
	 */
	private String updateOrders(String orderId, String dishName, int count,
			int iType) {
		String queryString = "dish_name=" + dishName + "&count="
				+ String.valueOf(count) + "&i_type=" + String.valueOf(iType)
				+ "&order_id=" + orderId;

		// url
		String url = HttpUtil.BASE_URL + "UpdateOrderServlet?"
				+ queryString;

		// ��ѯ���ؽ��
		return HttpUtil.queryStringForPost(url);
	}

	/*
	 * ��������ύ������Ϣ����
	 */
	private String insertOrders(String orderId, int userId, int tableId,
			double totalPrice, boolean isPayed, String payType,
			boolean isResponseAll) {

		String queryString = "user_id=" + String.valueOf(userId) + "&table_id="
				+ String.valueOf(tableId) + "&order_id=" + orderId
				+ "&total_price=" + String.valueOf(totalPrice) + "&is_payed="
				+ String.valueOf(isPayed) + "&pay_type=" + payType
				+ "&is_response_all=" + String.valueOf(isResponseAll);
		// url
		String url = HttpUtil.BASE_URL + "PushToOrderServlet?"
				+ queryString;

		// ��ѯ���ؽ��
		return HttpUtil.queryStringForPost(url);
	}

	/*
	 * ���������ȡ������Ϣ����
	 */
	private String getOrdersDetails(String orderId) {

		String queryString = "order_id=" + orderId;
		// url
		String url = HttpUtil.BASE_URL + "GetOrdersDetailsServlet?"
				+ queryString;

		// ��ѯ���ؽ��
		return HttpUtil.queryStringForPost(url);
	}

	// ��ȡ�õ��˻���Vip�ȼ�����
	private String getUserInfo(int userId) {
		// ��ѯ����
		String queryString = "user_id=" + userId;
		// url
		String url = HttpUtil.BASE_URL + "GetUserInfoServlet?"
				+ queryString;

		// ��ѯ���ؽ��
		return HttpUtil.queryStringForPost(url);
	}
	
	// �����û�����
	private String updateUserInfo(int userId,int type,double newBalance) {
		// ��ѯ����
		String queryString = "user_id=" + userId + "&type=" + type+ 
		"&new_balance=" + String.valueOf(newBalance);
		// url
		String url = HttpUtil.BASE_URL + "UpdateUserInfoServlet?"
				+ queryString;

		// ��ѯ���ؽ��
		return HttpUtil.queryStringForPost(url);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.brazuca.ui.view.PullToRefreshView.OnHeaderRefreshListener#onHeaderRefresh
	 * (com.brazuca.ui.view.PullToRefreshView)
	 */
	@Override
	public void onHeaderRefresh(PullToRefreshView view) {
		isRefreshHeader = true;
		mGetOrderThread = new ThreadGetOrders();
		mGetOrderThread.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.brazuca.ui.view.PullToRefreshView.OnFooterRefreshListener#onFooterRefresh
	 * (com.brazuca.ui.view.PullToRefreshView)
	 */
	@Override
	public void onFooterRefresh(PullToRefreshView view) {
		mPullToRefreshView.onFooterRefreshComplete();
	}
}