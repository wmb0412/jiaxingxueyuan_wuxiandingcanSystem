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

//user+随机数+流水号(流水号为全局递增变量)  服务器重启就生成新的随机数
//切换了用户后，应该刷新菜单??

public class OrderDetailsActivity extends Activity implements
		OnHeaderRefreshListener, OnFooterRefreshListener {
	protected final int GET_FROM_NET_OK = 0x001;// 消息:注册成功
	protected final int NET_EXCEPTION = 0x002;// 消息:网络异常
	protected final int GET_FAILED = 0x003;// 消息：注册失败
	protected final int SERVER_EXCEPTION = 0x004;// 消息:服务器异常
	protected final int START_TO_SELECT_SINGLE_COUNT_AC = 0X005; // 跳转到选择单品个数Activity
	protected final int INSERT_ORDER_SUCCESS = 0x006; // 消息：提交订单成功
	protected final int INSERT_ORDER_FAILED = 0x007; // 消息：提交订单失败
	protected final int UPDATE_ORDER_SUCCESS = 0x008; // 消息：更新订单成功
	protected final int UPDATE_ORDER_FAILED = 0x009; // 消息：更新订单失败
	protected final int GET_USER_INFO_OK = 0x010; // 消息:获取用户资料成功
	protected final int GET_USER_INFO_FAILED = 0x011; // 消息获取用户资料失败

	private static final int DELETE_SINGLE_DISH = 1; // 删除菜品

	private static final int ORDER_DETAILS_SHOW = 1;
	private static final int SELECT_PAY_TYPE = 2;
	private static final int FINAL_SUBMIT = 3;

	private static final int ACCOUNT_PAY = 1; // 账户支付
	private static final int ONLINE_PAY = 2; // 在线支付
	private static final int RECEPTION_PAY = 3; // 前台支付

	private Context mContext;
	private ControllersUtil controllersUtil;
	private RelativeLayout relErrorMsg; // 错误消息RelativeLayout
	private TextView tvErrorMsg; // 错误消息TextView
	private ListView lvOrderDetails; // 订单ListView

	private TextView tvSubmitOrder; // 提交订单
	private TextView tvBack; // 返回按钮
	private TextView tvOrderCount; // 单品数量
	private TextView tvPayPrice; // 支付总价TextView
	private TextView tvPayType; // 支付类型
	private TextView tvOrderTopBar; // 顶部栏
	private RelativeLayout relOrderDetails; // 订单详情
	private RelativeLayout relSelectCountParent; // 选择支付类型
	private RadioGroup radioGroup;

	private int iPayType = ACCOUNT_PAY; // 支付方式：默认支付方式是 “账户支付"
	// private int orderId; // 待更新的订单号
	private double newBalance=0d;
	private double totalpayPrice = 0d; // 待更新的订单号
	private int dishClassifcationCount; // 已下单的单品数
	private PullToRefreshView mPullToRefreshView; // 下拉刷新视图

	ArrayList<OrderEntity> listitem; // 存放菜单信息的ArrayList
	private OrderDetailsAdapter adapter; // 适配器

	private int deleteItemIndex = 0;
	private boolean isDeteleDish = false; // 是否是删除菜品
	private boolean isRefreshHeader = false; // 是否头部刷新
	private static int iStep = ORDER_DETAILS_SHOW; // 页面的步骤
	private static int checkId = R.id.rb_account_pay;
	private String updateDishName; // 待更新的单品名字
	private int updateCount; // 待更新菜品的数量

	private String orderId = "";
	private int tableId = 0;
	private boolean isSubmit = false;
	private boolean isEmptyFood = false;

	public static boolean isUpdateOrder = false;

	private ThreadGetOrders mGetOrderThread; // 获取菜单线程实例
	private ThreadUpdateOrders mUpdateOrderCount; // 更新菜单线程实例
	private ThreadUpdateOrders mDeteleSingleDish; // 删除菜品线程实例
	private ThreadInsertOrders mInsertOrders; // 插入订单的线程实例
	private ThreadGetUserInfo mGetUserInfoThread; // 获取用户资料线程
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

		initViews(); // 初始化控件信息
		setPullViewListner(); // 设置控件的监听事件
		setLvCreateContextMenuListener();

		// 获取HomeActivity传来的值，并给相应控件设置属性
		Bundle bundle = new Bundle();
		bundle = getIntent().getExtras();

		if (bundle != null) {

			orderId = bundle.getString("order_id"); // 订单号
			tableId = Integer.parseInt(bundle.getString("table_id"));
			isSubmit = bundle.getBoolean("is_submit");// 是否提交订单
			isEmptyFood = bundle.getBoolean("is_empty_food");// 是否点菜了

			System.out.println("isSubmit=" + isSubmit);

			if (!isEmptyFood) // 如果菜单没菜
			{
				changeViewVisibility();
				return;
			}
		}

		// 如果用户登录了则进行头部刷新
		if (UserParam.isLogin) {
			mPullToRefreshView.headerRefreshing();
		}
		// 如果是提交订单步骤的第一个步骤--订单粗略信息页面，则让返回键不可见
		if (iStep == ORDER_DETAILS_SHOW) {
			tvBack.setVisibility(View.GONE);
		}
	}

	// 设置控件的可见性
	private void changeViewVisibility() {
		tvBack.setVisibility(View.GONE);
		tvSubmitOrder.setVisibility(View.GONE);
		relErrorMsg.setVisibility(View.VISIBLE);
		tvErrorMsg.setText(getString(R.string.no_dish_msg));
		relOrderDetails.setVisibility(View.GONE);
	}

	// 设置listview弹出菜单事件监听
	private void setLvCreateContextMenuListener() {
		lvOrderDetails.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View view,
					ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				final int iItemIdx = info.position;
				deleteItemIndex = iItemIdx;

				menu.setHeaderTitle("选择功能");
				menu.add(Menu.NONE, DELETE_SINGLE_DISH, 0, "删除菜品")
						.setOnMenuItemClickListener(
								new OnMenuItemClickListener() {

									@Override
									public boolean onMenuItemClick(MenuItem item) {

										updateDishName = listitem.get(iItemIdx)
												.getDishName();

										// 启动删除菜品线程
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

		// 如果用户进行了单品数量的更改，则刷新菜单的信息
		if (SelectSingleCountActivity.isFromSelectCount) {
			// listitem.get(adapter.position).setCount(SelectSingleCountActivity.curCount+1);
			// //adapter.notifyDataSetChanged();
			// adapter = new OrderAdapter(mContext, listitem);
			// adapter.notifyDataSetInvalidated();

			SelectSingleCountActivity.isFromSelectCount = false;

			updateDishName = listitem.get(adapter.position).getDishName();
			updateCount = SelectSingleCountActivity.curCount + 1;

			// 启动更新份数线程
			mUpdateOrderCount = new ThreadUpdateOrders(1);
			mUpdateOrderCount.start();

		}

		else if (UserParam.isInsertDish) // 如果用户添加了菜品
		{
			UserParam.isInsertDish = false;
			mGetOrderThread = new ThreadGetOrders();
			mGetOrderThread.start();
		}

		super.onResume();
	}

	// 初始化控件
	private void initViews() {
		relErrorMsg = (RelativeLayout) findViewById(R.id.rel_error_msg);
		tvErrorMsg = (TextView) findViewById(R.id.tv_error_msg);

		mPullToRefreshView = (PullToRefreshView) findViewById(R.id.order_pull_refresh_view);// 下拉刷新视图
		tvSubmitOrder = (TextView) findViewById(R.id.tv_submit_order); // 提交订单
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

	// 判断支付类型
	private String getPayPureType(int iType) {
		switch (iType) {
		case R.id.rb_account_pay:
			iPayType = ACCOUNT_PAY;
			return "账号支付";
		case R.id.rb_online_pay:
			iPayType = ONLINE_PAY;
			return "在线支付";
		case R.id.rb_reception_pay:
			iPayType = RECEPTION_PAY;
			return "前台支付";

		}
		return "";
	}

	// 设置详细菜单信息的值
	private void setViews() {
		tvOrderCount.setText(dishClassifcationCount + "");
		tvPayPrice.setText(totalpayPrice + "元");
		tvPayType.setText(getPayPureType(checkId));
		// tvOrderDetails.setText("您总共点了"+dishClassifcationCount+
		// "种菜，需支付"+totalpayPrice+"元\n"+getPayType(checkId));

	}

	// private void setNextStepViews(int iStep)
	// {
	// switch (iStep) {
	// case ORDER_DETAILS_SHOW:
	// iStep = SELECT_PAY_TYPE;
	// tvOrderTopBar.setText("请选择支付类型");
	// tvBack.setVisibility(View.VISIBLE);
	//
	// changeView(relSelectCountParent,mPullToRefreshView,relOrderDetails);
	// break;
	// case SELECT_PAY_TYPE:
	// iStep = FINAL_SUBMIT;
	// tvOrderTopBar.setText("订单详情");
	// setViews();
	// changeView2(mPullToRefreshView,relOrderDetails,relSelectCountParent);
	// tvSubmitOrder.setText("提交订单");
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

	// 设置控件的监听事件
	private void setPullViewListner() {
		mPullToRefreshView.setOnHeaderRefreshListener(this);
		mPullToRefreshView.setOnFooterRefreshListener(this);

		// 提交订单响应事件
		tvSubmitOrder.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (iStep == ORDER_DETAILS_SHOW) {
					iStep = SELECT_PAY_TYPE;
					tvOrderTopBar.setText("选择支付方式");
					tvBack.setVisibility(View.VISIBLE);

					changeView(relSelectCountParent, mPullToRefreshView,
							relOrderDetails);
				}
				else if (iStep == SELECT_PAY_TYPE) {
					iStep = FINAL_SUBMIT;
					tvOrderTopBar.setText("订单详情");
					setViews();
					changeView2(mPullToRefreshView, relOrderDetails,
							relSelectCountParent);
					tvSubmitOrder.setText("提交订单");
				}
				else if (iStep == FINAL_SUBMIT) // 提交
				{

					final AlertDialog dlg = new AlertDialog.Builder(
							mContext).create();

					dlg.show();

					Window window = dlg.getWindow();

					window.setContentView(R.layout.bg_confirm_dialog);

					TextView tvConfrimMsg = (TextView) window
							.findViewById(R.id.tv_confrim_msg);
					tvConfrimMsg.setText("确认提交订单吗？");
					// 为确认按钮添加事件
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
								if (!isBalacePlus(UserParam.balance)) // 账户余额不足
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

							// 启动提交订单线程
							mInsertOrders = new ThreadInsertOrders();
							mInsertOrders.start();
							controllersUtil.showProgressWindow(dlgProgress, "正在提交订单");
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

		// 返回响应事件
		tvBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (iStep == FINAL_SUBMIT) {
					// setStepViews(iStep);
					iStep = SELECT_PAY_TYPE;
					tvOrderTopBar.setText("选择支付方式");
					changeView(relSelectCountParent, mPullToRefreshView,
							relOrderDetails);

					tvSubmitOrder.setText("下一步");
				}
				else if (iStep == SELECT_PAY_TYPE) {
					iStep = ORDER_DETAILS_SHOW;
					setViews();

					changeView2(mPullToRefreshView, relOrderDetails,
							relSelectCountParent);

					tvSubmitOrder.setText("下一步");
					tvBack.setVisibility(View.GONE);
					tvOrderTopBar.setText(getString(R.string.app_name));
				}

			}
		});

	}

	// 判断账户余额是否充足
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
	 * 改变控件的可见性
	 */
	private void changeView(View... args) {
		args[0].setVisibility(View.VISIBLE);
		args[1].setVisibility(View.GONE);
		args[2].setVisibility(View.GONE);
	}

	/*
	 * 改变控件的可见性2
	 */
	private void changeView2(View... args) {
		args[0].setVisibility(View.VISIBLE);
		args[1].setVisibility(View.VISIBLE);
		args[2].setVisibility(View.GONE);
	}

	/*
	 * 给ArrayList(listitem)赋值
	 */
	private ArrayList<OrderEntity> createData(String obj) {
		try {
			JSONArray jsArray = new JSONArray(obj);
			OrderEntity entity = null;

			dishClassifcationCount = jsArray.length();
			totalpayPrice = 0d; // 刷新前，让总价等于0

			for (int i = 0; i < dishClassifcationCount; i++) {
				entity = new OrderEntity();
				JSONObject js = new JSONObject();
				js = jsArray.getJSONObject(i);

				String dishName = js.getString("dish_name");
				int count = Integer.parseInt(js.getString("count"));
				double paySinglePrice = Double.parseDouble(js
						.getString("pay_single_price")); // 单品价格
				double paySingleTotalPrice = paySinglePrice * count; // 单品总价格

				totalpayPrice += paySingleTotalPrice; // 总价格

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
	 * 更新菜单线程
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

				if (result.equals("fail")) // 更新订单失败
				{
					mHandler.obtainMessage(UPDATE_ORDER_FAILED).sendToTarget();// 发送消息到Handler
				}

				else if (result.equals("服务器异常")) // 服务器异常
				{
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// 发送消息到Handler
				}
				else // 更新订单成功
				{
					if (updateType == 3) // 删除菜品类型
					{
						isDeteleDish = true;
					}
					mHandler.obtainMessage(UPDATE_ORDER_SUCCESS, result)
							.sendToTarget();// 发送消息到Handler
				}

			}
			catch (Exception e) {
				mUpdateOrderCount = null;
			}

			mUpdateOrderCount = null;

		}
	}

	/*
	 * 提交订单线程
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
				

				if (result.equals("fail")) // 提交订单失败
				{
					mHandler.obtainMessage(INSERT_ORDER_FAILED).sendToTarget();// 发送消息到Handler
				}

				else if (result.equals("服务器异常")) // 服务器异常
				{
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// 发送消息到Handler
				}
				else // 提交订单成功
				{
					mHandler.obtainMessage(INSERT_ORDER_SUCCESS, result)
							.sendToTarget();// 发送消息到Handler
				}

			}
			catch (Exception e) {
				mInsertOrders = null;
			}

			mInsertOrders = null;

		}
	}

	/*
	 * 获取详细菜单线程
	 */
	private class ThreadGetOrders extends Thread {
		@Override
		public void run() {
			try {

				String result = getOrdersDetails(orderId);

				if (result.equals("fail")) // 注册失败
				{
					mHandler.obtainMessage(GET_FAILED).sendToTarget();// 发送消息到Handler
				}

				else if (result.equals("服务器异常")) // 服务器异常
				{
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// 发送消息到Handler
				}
				else {
					mHandler.obtainMessage(GET_FROM_NET_OK, result)
							.sendToTarget();// 发送消息到Handler
				}

			}
			catch (Exception e) {
				mGetOrderThread = null;
			}

			mGetOrderThread = null;

		}
	}

	/*
	 * 进行Activity的跳转
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
	 * 监听系统消息的线程
	 */
	class ThreadSysMsgEvent extends Thread {
		@Override
		public void run() {
			while (true) {
				try {

					if (adapter.isClickCount && !isSubmit) {
						mHandler.obtainMessage(START_TO_SELECT_SINGLE_COUNT_AC)
								.sendToTarget();// 发送消息到Handler
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
	
	// 更新用户数据线程
	private class ThreadUpdateUserInfo extends Thread {
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

				newBalance = UserParam.balance - totalpayPrice;  //剩余账户资金
				String strLoginResult = updateUserInfo(UserParam.userId,1,newBalance); // 查询用户数据库

				if (strLoginResult.equals("fail")) {
					mHandler.obtainMessage(GET_USER_INFO_FAILED).sendToTarget();// 发送消息到Handler
				}
				else if (strLoginResult.equals("服务器异常")) {
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// 发送消息到Handler
				}
				else {
					mHandler.obtainMessage(GET_USER_INFO_OK, strLoginResult)
							.sendToTarget(); // 发送装有json对象（strLoginResult）的消息到Handler
				}

			}
			catch (Exception e) {
				threadUpdateUserInfo = null;
			}
			threadUpdateUserInfo = null;
		}
	}

	// 获取用户数据线程
	private class ThreadGetUserInfo extends Thread {
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

				String strLoginResult = getUserInfo(UserParam.userId); // 查询用户数据库

				if (strLoginResult != null && strLoginResult.equals("null")) {
					mHandler.obtainMessage(GET_USER_INFO_FAILED).sendToTarget();// 发送消息到Handler
				}
				else if (strLoginResult.equals("服务器异常")) {
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// 发送消息到Handler
				}
				else {
					mHandler.obtainMessage(GET_USER_INFO_OK, strLoginResult)
							.sendToTarget(); // 发送装有json对象（strLoginResult）的消息到Handler
				}

			}
			catch (Exception e) {
				mGetUserInfoThread = null;
			}
			mGetUserInfoThread = null;
		}
	}

	// 获取用户信息，数据格式--json数据
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

	// 主线程UI的Handler处理
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case GET_USER_INFO_OK: // 成功获取用户数据

				String strUser = "";
				strUser = (String) (msg.obj);
				setUserInfoFromNet(strUser); // 设置用户数据

				break;
			case GET_USER_INFO_FAILED:

				break;

			case UPDATE_ORDER_SUCCESS:
				controllersUtil.showToast(mContext, "更新成功", Toast.LENGTH_SHORT);

				mPullToRefreshView.headerRefreshing();
				isUpdateOrder = true;
				setViews();
				break;

			case UPDATE_ORDER_FAILED:
				controllersUtil.showToast(mContext, "更新份数失败",
						Toast.LENGTH_SHORT);
				break;

			case INSERT_ORDER_SUCCESS:

				controllersUtil.showToast(mContext, "提交订单成功，静候美味吧",
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
				
				controllersUtil.showToast(mContext, "提交订单失败，请重新提交",
						Toast.LENGTH_SHORT);
				break;

			case START_TO_SELECT_SINGLE_COUNT_AC:

				startToNewActivity();
				adapter.isClickCount = false;
				break;

			case GET_FROM_NET_OK: // 请求成功

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
				new Thread(new ThreadSysMsgEvent()).start(); // 启动点击次数线程
				if (isRefreshHeader) {
					mPullToRefreshView.onHeaderRefreshComplete();
					isRefreshHeader = false;
				}

				if (isSubmit) // 如果订单提交了，则让用户不能再提交订单
				{
					tvBack.setVisibility(View.GONE);
					tvSubmitOrder.setVisibility(View.GONE);
				}
				else {
					// 启动获取用户资料线程
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
			case GET_FAILED: // 请求失败

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
	 * 向服务器提交更新菜单参数
	 */
	private String updateOrders(String orderId, String dishName, int count,
			int iType) {
		String queryString = "dish_name=" + dishName + "&count="
				+ String.valueOf(count) + "&i_type=" + String.valueOf(iType)
				+ "&order_id=" + orderId;

		// url
		String url = HttpUtil.BASE_URL + "UpdateOrderServlet?"
				+ queryString;

		// 查询返回结果
		return HttpUtil.queryStringForPost(url);
	}

	/*
	 * 向服务器提交订单信息参数
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

		// 查询返回结果
		return HttpUtil.queryStringForPost(url);
	}

	/*
	 * 向服务器获取订单信息参数
	 */
	private String getOrdersDetails(String orderId) {

		String queryString = "order_id=" + orderId;
		// url
		String url = HttpUtil.BASE_URL + "GetOrdersDetailsServlet?"
				+ queryString;

		// 查询返回结果
		return HttpUtil.queryStringForPost(url);
	}

	// 获取用的账户余额、Vip等级请求
	private String getUserInfo(int userId) {
		// 查询参数
		String queryString = "user_id=" + userId;
		// url
		String url = HttpUtil.BASE_URL + "GetUserInfoServlet?"
				+ queryString;

		// 查询返回结果
		return HttpUtil.queryStringForPost(url);
	}
	
	// 更新用户资料
	private String updateUserInfo(int userId,int type,double newBalance) {
		// 查询参数
		String queryString = "user_id=" + userId + "&type=" + type+ 
		"&new_balance=" + String.valueOf(newBalance);
		// url
		String url = HttpUtil.BASE_URL + "UpdateUserInfoServlet?"
				+ queryString;

		// 查询返回结果
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