package com.brazuca.ui;

import java.util.ArrayList;

import org.json.JSONArray;
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
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brazuca.db.BrazucaDBHelper;
import com.brazuca.db.BrazucaDBUtil;
import com.brazuca.entity.OrderEntity;
import com.brazuca.entity.RerserveTableEntity;
import com.brazuca.entity.UserParam;
import com.brazuca.network.HttpUtil;
import com.brazuca.ui.adapter.OrderDetailsAdapter;
import com.brazuca.ui.adapter.OrderAdapter;
import com.brazuca.ui.view.PullToRefreshView;
import com.brazuca.ui.view.PullToRefreshView.OnFooterRefreshListener;
import com.brazuca.ui.view.PullToRefreshView.OnHeaderRefreshListener;
import com.brazuca.util.ControllersUtil;

//user+随机数+流水号(流水号为全局递增变量)  服务器重启就生成新的随机数
//切换了用户后，应该刷新菜单??

public class OrderActivity extends Activity implements OnHeaderRefreshListener,
		OnFooterRefreshListener {
	protected final int GET_FROM_NET_OK = 0x001;// 消息:注册成功
	protected final int NET_EXCEPTION = 0x002;// 消息:网络异常
	protected final int GET_FAILED = 0x003;// 消息：注册失败
	protected final int SERVER_EXCEPTION = 0x004;// 消息:服务器异常

	private static final int DELETE_SINGLE_DISH = 1; //删除菜品
	
	private Context mContext;
	private ControllersUtil controllersUtil;
	private RelativeLayout relErrorMsg; // 错误消息RelativeLayout
	private TextView tvErrorMsg; // 错误消息TextView
	private ListView lvAllOrder; // 订单ListView
	private TextView tvCheckTable;  //选桌

	private PullToRefreshView mPullToRefreshView; // 下拉刷新视图

	ArrayList<RerserveTableEntity> listitem; // 存放菜单信息的ArrayList
	private OrderAdapter adapter; // 适配器

	private int deleteItemIndex = 0;
	private boolean isDeteleDish = false; //是否是删除菜品
	private boolean isRefreshHeader = false; // 是否头部刷新

	private ThreadGetOrders mGetOrders; // 获取

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order);
		mContext = OrderActivity.this;
		controllersUtil= new ControllersUtil();
		listitem = new ArrayList<RerserveTableEntity>();

		initViews(); // 初始化控件信息
		setListener();
		setPullViewListner(); // 设置控件的监听事件
		setLvCreateContextMenuListener();
		
		// 如果用户登录了则进行头部刷新
		if (UserParam.isLogin) {
			mPullToRefreshView.headerRefreshing();
	
		}

	}
	
	//设置listview弹出菜单事件监听
	private void setLvCreateContextMenuListener()
	{
		lvAllOrder.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			
			@Override
			public void onCreateContextMenu(ContextMenu menu, View view,
					ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				final int iItemIdx = info.position;
				deleteItemIndex = iItemIdx;
				
				menu.setHeaderTitle("选择功能");
				menu.add(Menu.NONE, DELETE_SINGLE_DISH, 0, "删除订单")
						.setOnMenuItemClickListener(
								new OnMenuItemClickListener() {

									@Override
									public boolean onMenuItemClick(
											MenuItem item) {
										
//										updateDishName = listitem.get(iItemIdx).getDishName();
//						
//										//启动删除菜品线程
//										mDeteleSingleDish = new ThreadUpdateOrders(3);
//										mDeteleSingleDish.start();

										return false;
									}

								});
				
			}
		});
		
		
		lvAllOrder.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position,
					long arg3) {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("order_id", listitem.get(position).getOrderId()+ "");
				bundle.putString("table_id", listitem.get(position).getTableId()+ "");
				bundle.putBoolean("is_submit", listitem.get(position).isSubmit()); //是否提交订单
				bundle.putBoolean("is_empty_food", listitem.get(position).isEmptyFood());//是否点菜了
		
				intent.setClass(OrderActivity.this, OrderDetailsActivity.class);

				intent.putExtras(bundle);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
			}

		});
	}

	@Override
	protected void onResume() {
//		System.out.println("UserParam.isInsertDish="+UserParam.isInsertDish);

		// 如果用户提交了订单，则刷新订单&&如果用户添加了菜品
		if (OrderDetailsActivity.isUpdateOrder || UserParam.isInsertDish) {
			mPullToRefreshView.headerRefreshing();
		}

//		else if(UserParam.isInsertDish)  //
//		{
//			UserParam.isInsertDish = false;
//			mPullToRefreshView.headerRefreshing();
//		}
		super.onResume();
	}

	// 初始化控件
	private void initViews() {
		relErrorMsg = (RelativeLayout) this.findViewById(R.id.rel_error_msg);
		tvErrorMsg = (TextView) this.findViewById(R.id.tv_error_msg);

		mPullToRefreshView = (PullToRefreshView) this.findViewById(R.id.main_pull_refresh_view);// 下拉刷新视图
		lvAllOrder = (ListView)this.findViewById(R.id.lv_reserve_table);
		tvCheckTable = (TextView) this.findViewById(R.id.tv_check_table); //选桌
	}
	
	private void setListener()
	{
		//选桌
		tvCheckTable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// 如果未检测到用户登录，则跳转到登录界面
				if (!UserParam.isLogin) {
					startActivity(new Intent(OrderActivity.this, LoginActivity.class));
				}
				else
					startActivity(new Intent(OrderActivity.this, TableActivity.class));
				
			}
		});
	}
	
	// 设置控件的监听事件
	private void setPullViewListner() {
		mPullToRefreshView.setOnHeaderRefreshListener(this);
		mPullToRefreshView.setOnFooterRefreshListener(this);
	}


	/*
	 * 给ArrayList(listitem)赋值
	 */
	private ArrayList<RerserveTableEntity> createData(String obj) {
		if (listitem.size() > 0)
			listitem.clear();

		try {
			JSONArray jsArray = new JSONArray(obj);
			JSONObject js = null;
			RerserveTableEntity rerserveTableEntity = null;

			for (int i = 0; i < jsArray.length(); i++) {
				rerserveTableEntity = new RerserveTableEntity();
				js = new JSONObject();
				js = jsArray.getJSONObject(i);

				int tableId = js.getInt("table_id");
				String useTime = js.getString("use_time");
				int hour = js.getInt("hour");
				int type = js.getInt("type");
				String orderId = js.getString("order_id");
				boolean isEmptyFood = js.getBoolean("is_empty_food");
				boolean isSubmit = js.getBoolean("is_submit");
				String reserveTime = js.getString("reserve_time");  //预订时间

				rerserveTableEntity.setTableId(tableId);
				rerserveTableEntity.setUseTime(useTime);
				rerserveTableEntity.setType(type);
				rerserveTableEntity.setHour(hour);
				rerserveTableEntity.setOrderId(orderId);
				rerserveTableEntity.setEmptyFood(isEmptyFood);
				rerserveTableEntity.setSubmit(isSubmit);
				rerserveTableEntity.setReserveTime(reserveTime);

				listitem.add(rerserveTableEntity);
			}
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listitem;

	}

	/*
	 * 获取预订餐桌的线程
	 */
	private class ThreadGetOrders extends Thread {
		@Override
		public void run() {
			try {

				String result = getOrders(UserParam.userId);

				if (result.equals("fail")) // 注册失败
				{
					mHandler.obtainMessage(GET_FAILED).sendToTarget();// 发送消息到Handler
				}

				else if (result.equals("服务器异常")) // 服务器异常
				{
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// 发送消息到Handler
				}
				else {
					mHandler.obtainMessage(GET_FROM_NET_OK, result).sendToTarget();// 发送消息到Handler
				}

			}
			catch (Exception e) {
				mGetOrders = null;
			}

			mGetOrders = null;

		}
	}

	// 主线程UI的Handler处理
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case GET_FROM_NET_OK: // 请求成功
				controllersUtil.showToastTwo(mContext, "已更新",
						Toast.LENGTH_SHORT);
				
				String strJson = "";
				strJson = (String) (msg.obj);

				if (listitem.size() > 0)
					listitem.clear();

				adapter = new OrderAdapter(mContext, createData(strJson));

				lvAllOrder.setAdapter(adapter);
	
				if(relErrorMsg.isShown())
					relErrorMsg.setVisibility(View.GONE);
				
				if(isRefreshHeader)
				{
					mPullToRefreshView.onHeaderRefreshComplete();
					isRefreshHeader =false;
				}

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
						getString(R.string.no_order_msg), Toast.LENGTH_SHORT);
				relErrorMsg.setVisibility(View.VISIBLE);
				
				tvErrorMsg.setText(getString(R.string.no_order_msg));
				mPullToRefreshView.onHeaderRefreshComplete();
				if(mPullToRefreshView.isShown())
					mPullToRefreshView.setVisibility(View.GONE);
				break;
			}
			
			if(OrderDetailsActivity.isUpdateOrder ||UserParam.isInsertDish) 
			{
				OrderDetailsActivity.isUpdateOrder = false;
				UserParam.isInsertDish = false;
			}
			
			super.handleMessage(msg);
		}
	};


	// 获取用户预订的餐桌请求
	private String getOrders(int userId) {
		// 查询参数
		String queryString = "user_id="+userId;
		// url
		String url = HttpUtil.BASE_URL + "GetOrdersServlet?"+queryString;
		
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
		mGetOrders = new ThreadGetOrders();
		mGetOrders.start();
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