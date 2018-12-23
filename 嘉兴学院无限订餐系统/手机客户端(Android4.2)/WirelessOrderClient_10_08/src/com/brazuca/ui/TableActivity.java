package com.brazuca.ui;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.brazuca.entity.RerserveTableEntity;
import com.brazuca.entity.TableEntity;
import com.brazuca.entity.UserParam;
import com.brazuca.network.HttpUtil;
import com.brazuca.ui.adapter.UnfinishedOrderAdapter;
import com.brazuca.ui.adapter.TableAdapter;
import com.brazuca.ui.view.PullToRefreshView;
import com.brazuca.ui.view.PullToRefreshView.OnFooterRefreshListener;
import com.brazuca.ui.view.PullToRefreshView.OnHeaderRefreshListener;
import com.brazuca.util.ControllersUtil;
import com.brazuca.util.UtilModule;

public class TableActivity extends Activity implements OnHeaderRefreshListener,
		OnFooterRefreshListener,OnDateSetListener {
	protected final int PULL_TO_REFRESH_HEADER = 1; // 头部刷新
	protected final int PULL_TO_REFRESH_FOOTER = 2; // 底部刷新

	protected final int GET_FROM_NET_OK = 0x001;// 消息:获取成功
	protected final int NET_EXCEPTION = 0x002;// 消息:网络异常
	protected final int GET_FAILED = 0x003;// 消息：获取失败
	protected final int SERVER_EXCEPTION = 0x004;// 消息:服务器异常
	protected final int RESERVE_TABLE_SUCCESS = 0x005;// 消息:服务器异常
	protected final int RESERVE_TABLE_FAIL = 0x006;// 消息:服务器异常

	private static int PICK_TABLE = 1;
	private static int PICK_TIME = 2;

	private Context mContext;
	private Button btnBack;
	private Button btnNextStep; // 下一步
	private TextView tvTopBar;
	private TextView tvSkip; // 跳过选桌子界面
	private PullToRefreshView mPullToRefreshView;
	private TimePicker timePicker;
	private DatePicker datePicker;
	private TextView tvDatatimeShow; //选择时间显示
	private GridView gridViewTable;
	private TableAdapter tableAdapter;
	private ArrayList<TableEntity> listitem;
	private ArrayList<RerserveTableEntity> listitemReserveTables;
	
	private ControllersUtil controllersUtil;
	private AlertDialog dlgProgress;

	private ThreadGetTableList mGetTableListThread; // 获取饭桌状态线程
	private ThreadGetTableList mGetRerserveTableThread; // 请求获取用户已定桌号等信息线程
	private ThreadRerserveTable mRerserveTableThread; // 获取饭桌状态线程
	

	private int tableIdSelected; // 被用户点击预订的饭桌id
	private int iRefreshType;
	private int iPickStep = PICK_TIME; // 选择的步骤
	
	private int getTableType = 1;
	
	private String selectDate="";
	private String selectHour="";
	private int type;   //订餐种类
	private int solidMonthOfYear;
	private int solidDayOfMonth;
	
	private int curYear;
	private int curMonthOfYear ;
	private int curDayOfMonth ;
	private int curHours ;
	private int curMinutes;
	
	private Calendar calendar = Calendar.getInstance();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_table);
		mContext = TableActivity.this;
		listitem = new ArrayList<TableEntity>();
		controllersUtil = new ControllersUtil();
		dlgProgress = new AlertDialog.Builder(mContext).create();
		
		initTime();
		initViews();
		setListener();
		setTimePicker();
		setDatePicker();
		type = UtilModule.getNiningType(curHours);
		selectHour = curHours+"";

		tvTopBar.setText("选择就餐时间");
		
		//获取用户已经预订的桌号等信息
		System.out.println("TableAc|UserParam.isLogin="+UserParam.isLogin);
		if(UserParam.isLogin)
		{
			mGetRerserveTableThread = new ThreadGetTableList(2);
			mGetRerserveTableThread.start();
		}
	}

	private void changeView(View... args) {
		for (int i = 0; i < args.length; i++) {
			if (i == 0)
				args[i].setVisibility(View.VISIBLE);
			else
				args[i].setVisibility(View.GONE);
		}
	}
	//初始化系统时间
	private void initTime()
	{
		 curYear = calendar.get(Calendar.YEAR);
		 
		 curMonthOfYear = calendar.get(Calendar.MONTH);
		 solidMonthOfYear = curMonthOfYear;
		 curDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		 solidDayOfMonth = curDayOfMonth;
		 curHours = calendar.get(Calendar.HOUR_OF_DAY);
		 curMinutes = calendar.get(Calendar.MINUTE);
	}

	// 初始化控件
	private void initViews() {
//		llSelectTime = (LinearLayout) findViewById(R.id.ll_select_time); // 选择订餐时间
		mPullToRefreshView = (PullToRefreshView) findViewById(R.id.table_pull_refresh_view);
		gridViewTable = (GridView) findViewById(R.id.gridview_table);
		btnBack = (Button) this.findViewById(R.id.btn_back);
		btnNextStep = (Button) this.findViewById(R.id.btn_next_step);
		timePicker = (TimePicker) findViewById(R.id.tp_time); // 时间选择器
		datePicker = (DatePicker) findViewById(R.id.dp_time); // 日期选择器
		tvDatatimeShow = (TextView) this.findViewById(R.id.tv_datatime_show);  //选择时间显示

		tvTopBar = (TextView) this.findViewById(R.id.tv_topbar); // 顶部栏

		tvSkip = (TextView) findViewById(R.id.tv_table_skip);
	}

	// 设置日期选择器的值
	private void setDatePicker() {
		System.out.println("curMonthOfYear="+curMonthOfYear);
		// 日期改变监听
		datePicker.init(curYear, curMonthOfYear, curDayOfMonth,
				new OnDateChangedListener() {			
					@Override
					public void onDateChanged(DatePicker view, int year, int monthOfYear,
							int dayOfMonth) {
						if(monthOfYear<solidMonthOfYear)
							return;
						else if(monthOfYear == solidMonthOfYear && dayOfMonth<solidDayOfMonth)
							return;
						
						curMonthOfYear = monthOfYear+1;
						curDayOfMonth = dayOfMonth;
						
						tvDatatimeShow.setText(year+"年"+curMonthOfYear+"月"+curDayOfMonth+"日" +
								curHours+"时"+curMinutes+"分");
						
						selectDate = 
							UtilModule.getFormatTime(curYear, curMonthOfYear, curDayOfMonth);
						
						
					}
				});
	}

	// 设置时间选择器的值
	private void setTimePicker() {
		timePicker.setIs24HourView(true);
		// set current time

		timePicker.setCurrentHour(curHours);
		timePicker.setCurrentMinute(curMinutes);

		// 时间改变监听
		timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				System.out.println("curMonthOfYear"+(curMonthOfYear));
				System.out.println("solidMonthOfYear"+solidMonthOfYear);
				System.out.println("solidDayOfMonth"+solidDayOfMonth);
				System.out.println("curDayOfMonth"+curDayOfMonth);
				
				if(curMonthOfYear<solidMonthOfYear || curDayOfMonth<solidDayOfMonth)
				{
					return;
				}
				else
				{
					curHours = hourOfDay;
					curMinutes = minute;
					tvDatatimeShow.setText(curYear+"年"+(curMonthOfYear+1)+"月"+curDayOfMonth+"日" +
							curHours+"时"+curMinutes+"分");
					
					selectDate = 
						UtilModule.getFormatTime(curYear, curMonthOfYear + 1, curDayOfMonth);
					System.out.println("tp|selectDate="+selectDate);
					
					System.out.println("curHours="+curHours);
					System.out.println("type="+type);
					type = UtilModule.getNiningType(curHours);
					selectHour = curHours+"";
				}

			}
		});
	}

	// 设置控件监听器
	private void setListener() {
		mPullToRefreshView.setOnHeaderRefreshListener(this);
		mPullToRefreshView.setOnFooterRefreshListener(this);

		// gridview的响应事件
		gridViewTable.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long arg3) {

				if (((TableEntity) parent.getItemAtPosition(position))
						.isTableStatus()) {
					controllersUtil.showToast(mContext,
							getString(R.string.table_in), Toast.LENGTH_SHORT);
				}
				else {
					tableIdSelected = ((TableEntity) parent
							.getItemAtPosition(position)).getTableId(); // 设置被用户准备预订的饭桌id号

					final AlertDialog  confrimDlg = new AlertDialog.Builder(mContext).create();
					
					confrimDlg.show();

					Window window = confrimDlg.getWindow();

					window.setContentView(R.layout.bg_confirm_dialog);

					TextView tvConfrimMsg = (TextView) window
							.findViewById(R.id.tv_confrim_msg);
					tvConfrimMsg.setText("你选择是"+tableIdSelected+
							"号桌，确认要提交么？");  
					// 为确认按钮添加事件
					TextView ok = (TextView) window.findViewById(R.id.btn_ok);
					TextView cancel = (TextView) window.findViewById(R.id.btn_cancel);

					ok.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							// 走请求定桌子的线程
							mRerserveTableThread = new ThreadRerserveTable();
							mRerserveTableThread.start();
							controllersUtil.showProgressWindow(dlgProgress, "正在选桌，请稍后");
							confrimDlg.dismiss();
						}
					});

					cancel.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							confrimDlg.dismiss();
						}
					});

					
				}

			}

		});
		
		// 下一步
		btnNextStep.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (iPickStep == PICK_TIME) {
						if(selectDate.equals(""))
						{
							controllersUtil.showToast(mContext, "您还没有选择就餐时间呢",
									Toast.LENGTH_SHORT);
							return false;
						}
						else if(type<=0)
						{
							controllersUtil.showToast(mContext, "您选择的时间段无法订餐，请选择其他时段",
									Toast.LENGTH_SHORT);
							return false;
						}
						
						// 启动获取桌子信息的线程
						mPullToRefreshView.headerRefreshing();
						
						iPickStep = PICK_TABLE;
						changeView(mPullToRefreshView, timePicker);
						tvTopBar.setText("选择就餐餐桌");
					}
					else if (iPickStep == PICK_TABLE) {
						if (UserParam.tableId < 1) {
							controllersUtil.showToast(mContext, "您还没有选择餐桌呢",
									Toast.LENGTH_SHORT);
							return false;
						}

					}

				}
				return false;
			}
		});

		// 返回按钮
		btnBack.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (iPickStep == PICK_TABLE) {
						iPickStep = PICK_TIME;
						changeView(timePicker, mPullToRefreshView);
						tvTopBar.setText("选择就餐时间");
					}
					else if (iPickStep == PICK_TIME) {
						TableActivity.this.finish();
					}

				}
				return false;
			}
		});
		

		// 跳过选桌子步骤
		tvSkip.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					final AlertDialog dlg = new AlertDialog.Builder(mContext)
							.create();

					dlg.show();

					Window window = dlg.getWindow();

					window.setContentView(R.layout.bg_confirm_dialog);

					TextView tvConfrimMsg = (TextView) window
							.findViewById(R.id.tv_confrim_msg);
					tvConfrimMsg.setText("确认跳过选桌子吗？您将不能点菜，只能浏览菜品");
					// 为确认按钮添加事件
					TextView ok = (TextView) window.findViewById(R.id.btn_ok);
					TextView cancel = (TextView) window
							.findViewById(R.id.btn_cancel);

					ok.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							dlg.dismiss();
							startActivity(new Intent(mContext,
									MainActivity.class));
							TableActivity.this.finish();
							// TableActivity.this.finish();
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
				return false;
			}
		});
	}

	// 向服务器请求订饭桌线程
	private class ThreadRerserveTable extends Thread {
		@Override
		public void run() {
			try {
				String result = "";
				
				String orderId = ""+UtilModule.getCurTime(3)+"_"+UserParam.userId;  //订单号:时间+用户id号
				UserParam.orderId = orderId;
System.out.println("ThreadRerserveTable");
System.out.println("ThreadRerserveTable|UserParam.orderId="+UserParam.orderId);
System.out.println("ThreadRerserveTable|UserParam.userId="+UserParam.userId);
				result = setReserveTable(UserParam.orderId,UserParam.userId,tableIdSelected,selectDate,selectHour,type);

System.out.println("ThreadRerserveTable|result="+result);
				if (result.equals("success")) // 请求成功
				{
					mHandler.obtainMessage(RESERVE_TABLE_SUCCESS)
							.sendToTarget();// 发送消息到Handler
				}
				else if (result.equals("fail")) // 请求失败
				{
					mHandler.obtainMessage(RESERVE_TABLE_FAIL).sendToTarget();// 发送消息到Handler
				}

				else if (result.equals("服务器异常")) // 服务器异常
				{
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// 发送消息到Handler
				}

			}
			catch (Exception e) {
				mRerserveTableThread = null;
			}

			mRerserveTableThread = null;
		}
	}

	// 得到饭桌状态的请求线程
	private class ThreadGetTableList extends Thread {
		private int type;
		
		private ThreadGetTableList(int type)
		{
			this.type = type;
		}
		@Override
		public void run() {
			try {
				String result = "";
				
				switch (type) {
				case 1:
					getTableType = 1;
					result = getListTables(selectDate,type);
					break;
				case 2:
					getTableType = 2;
System.out.println("TableAc|UserParam.userId"+UserParam.userId);
					result = getUserReserverdTables(UserParam.userId);
					break;

				default:
					break;
				}

				if (result.equals("fail")) // 请求失败
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
				mGetTableListThread = null;
			}
			mGetTableListThread = null;
		}
	}

	// 主线程UI的Handler处理
	Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			System.out.println("handler|getTableType="+getTableType);
			switch (msg.what) {
			
			case GET_FROM_NET_OK: // 请求成功
				if(getTableType == 1)
				{
					controllersUtil.showToastTwo(mContext, "已更新",
							Toast.LENGTH_SHORT);
					String strJson = "";
					strJson = (String) (msg.obj);

					tableAdapter = new TableAdapter(mContext, createData(strJson));
					gridViewTable.setAdapter(tableAdapter);
				}
				else if(getTableType == 2)
				{

					String strJson = "";
					strJson = (String) (msg.obj);  //jsons数据加载到listview中
					
					final AlertDialog dlg = new AlertDialog.Builder(mContext).create();

					dlg.show();

					Window window = dlg.getWindow();

					window.setContentView(R.layout.bg_listview_dialog);
					
					ListView lvUnfinishedOrder = (ListView)window.findViewById(R.id.lv_user_tables);
					if(listitemReserveTables==null)
					{
						listitemReserveTables = new ArrayList<RerserveTableEntity>();
					}
					
					UnfinishedOrderAdapter unfinishedOrderAdapter = 
						new UnfinishedOrderAdapter(mContext, createDialogData(strJson));
					
					lvUnfinishedOrder.setAdapter(unfinishedOrderAdapter);
					
					lvUnfinishedOrder.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> adapterView, View view,
								int position, long arg3) {
							
							//UserParam.tableId = listitemReserveTables.get(position).getTableId();
							UserParam.orderId = listitemReserveTables.get(position).getOrderId();
						
							dlg.dismiss();
							startActivity(new Intent(mContext,
									MainActivity.class));
							TableActivity.this.finish();
						}
						
						
					});
					
					// 为取消按钮添加事件
					TextView cancel = (TextView) window.findViewById(R.id.btn_cancel);

					cancel.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							dlg.dismiss();
							
						}
					});
				}
					
				break;

			case RESERVE_TABLE_SUCCESS: // 预订饭桌成功
				controllersUtil.showToast(mContext, "预订饭桌成功，现在您就可以订餐了",
						Toast.LENGTH_SHORT);

//				UserParam.tableId = tableIdSelected; // 设置用户桌号
				
				mPullToRefreshView.headerRefreshing();
				controllersUtil.hideProgressWindow(dlgProgress);
				
				
				startActivity(new Intent(mContext,
						MainActivity.class));
				TableActivity.this.finish();
				break;
			case RESERVE_TABLE_FAIL:
				controllersUtil.showToast(mContext, "预订饭桌失败，您刚才预订的桌子被他人抢先预订",
						Toast.LENGTH_LONG);
				controllersUtil.hideProgressWindow(dlgProgress);
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
						getString(R.string.get_table_info_from_net_fail),
						Toast.LENGTH_SHORT);
				break;
			}

			if (iRefreshType == PULL_TO_REFRESH_FOOTER) {
				mPullToRefreshView.onFooterRefreshComplete();

			}
			else if (iRefreshType == PULL_TO_REFRESH_HEADER) {
				mPullToRefreshView
						.onHeaderRefreshComplete(getString(R.string.pull_to_refresh_time_title)
								+ UtilModule.getCurTime(1)); // 设置更新时间
			}
			
			if (dlgProgress.isShowing()) {
				controllersUtil.hideProgressWindow(dlgProgress);
			}
			super.handleMessage(msg);
		}

	};

	/*
	 * 给listitem添加数据
	 */
	private ArrayList<TableEntity> createData(String obj) {
		if (listitem.size() > 0)
			listitem.clear();
		
		try {
			JSONArray jsArray = new JSONArray(obj);
			JSONObject js = null;
			TableEntity tableEntity = null;

			for (int i = 0; i < jsArray.length(); i++) {
				tableEntity = new TableEntity();
				js = new JSONObject();
				js = jsArray.getJSONObject(i);

				String tableId = js.getString("table_id");
				boolean tableStatus = js.getBoolean("table_status");

				tableEntity.setTableId(Integer.parseInt(tableId));
				tableEntity.setTableStatus(tableStatus);

				listitem.add(tableEntity);
			}
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listitem;
	}
	
	/*
	 * 给listitem添加数据
	 */
	private ArrayList<RerserveTableEntity> createDialogData(String obj) {
		if (listitemReserveTables.size() > 0)
			listitemReserveTables.clear();

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

				rerserveTableEntity.setTableId(tableId);
				rerserveTableEntity.setUseTime(useTime);
				rerserveTableEntity.setType(type);
				rerserveTableEntity.setHour(hour);
				rerserveTableEntity.setOrderId(orderId);
				rerserveTableEntity.setEmptyFood(isEmptyFood);

				listitemReserveTables.add(rerserveTableEntity);
			}
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listitemReserveTables;
	}
	
	// 获取用户预订的餐桌请求
	private String getUserReserverdTables(int userId) {
		// 查询参数
		String queryString = "user_id="+userId;
		// url
		String url = HttpUtil.BASE_URL + "GetUnfinishTablesServlet?"+queryString;
		
		// 查询返回结果
		return HttpUtil.queryStringForPost(url);
	}

	// 获取饭桌状态请求
	private String getListTables(String usetime,int type) {
		// 查询参数
		String queryString = "use_time="+usetime+"&type="+type;
		// url
		String url = HttpUtil.BASE_URL + "ListAllTablesServlet?"+queryString;
		
		// 查询返回结果
		return HttpUtil.queryStringForPost(url);
	}

	// 预订桌子请求
	private String setReserveTable(String orderId,int userId,int tableId,String useTime,String hour,int type) {

		String queryString = "user_id=" + String.valueOf(userId)+"&table_id=" + String.valueOf(tableId)+
		"&order_id=" + orderId+
		"&use_time=" + useTime + 
		"&hour=" + hour + 
		"&type=" + String.valueOf(type);
		
		// url                                     RerserveTableServlet
		String url = HttpUtil.BASE_URL + "RerserveTableServlet?"+ queryString;

		// 查询返回结果
		return HttpUtil.queryStringForPost(url);
	}

	@Override
	public void onFooterRefresh(PullToRefreshView view) {
		// TODO Auto-generated method stub
		iRefreshType = PULL_TO_REFRESH_FOOTER;
		mPullToRefreshView.onFooterRefreshComplete();
	}

	@Override
	public void onHeaderRefresh(PullToRefreshView view) {
		// TODO Auto-generated method stub
		iRefreshType = PULL_TO_REFRESH_HEADER;
		// 启动刷新列表线程;
		mGetTableListThread = new ThreadGetTableList(1);
		mGetTableListThread.start();
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
		switch (view.getId()) {
		case R.id.dp_time:
			
			break;

		default:
			break;
		}
		
	}

}
