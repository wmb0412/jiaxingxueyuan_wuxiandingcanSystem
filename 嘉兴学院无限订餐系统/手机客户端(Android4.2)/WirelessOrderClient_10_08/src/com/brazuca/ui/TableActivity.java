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
	protected final int PULL_TO_REFRESH_HEADER = 1; // ͷ��ˢ��
	protected final int PULL_TO_REFRESH_FOOTER = 2; // �ײ�ˢ��

	protected final int GET_FROM_NET_OK = 0x001;// ��Ϣ:��ȡ�ɹ�
	protected final int NET_EXCEPTION = 0x002;// ��Ϣ:�����쳣
	protected final int GET_FAILED = 0x003;// ��Ϣ����ȡʧ��
	protected final int SERVER_EXCEPTION = 0x004;// ��Ϣ:�������쳣
	protected final int RESERVE_TABLE_SUCCESS = 0x005;// ��Ϣ:�������쳣
	protected final int RESERVE_TABLE_FAIL = 0x006;// ��Ϣ:�������쳣

	private static int PICK_TABLE = 1;
	private static int PICK_TIME = 2;

	private Context mContext;
	private Button btnBack;
	private Button btnNextStep; // ��һ��
	private TextView tvTopBar;
	private TextView tvSkip; // ����ѡ���ӽ���
	private PullToRefreshView mPullToRefreshView;
	private TimePicker timePicker;
	private DatePicker datePicker;
	private TextView tvDatatimeShow; //ѡ��ʱ����ʾ
	private GridView gridViewTable;
	private TableAdapter tableAdapter;
	private ArrayList<TableEntity> listitem;
	private ArrayList<RerserveTableEntity> listitemReserveTables;
	
	private ControllersUtil controllersUtil;
	private AlertDialog dlgProgress;

	private ThreadGetTableList mGetTableListThread; // ��ȡ����״̬�߳�
	private ThreadGetTableList mGetRerserveTableThread; // �����ȡ�û��Ѷ����ŵ���Ϣ�߳�
	private ThreadRerserveTable mRerserveTableThread; // ��ȡ����״̬�߳�
	

	private int tableIdSelected; // ���û����Ԥ���ķ���id
	private int iRefreshType;
	private int iPickStep = PICK_TIME; // ѡ��Ĳ���
	
	private int getTableType = 1;
	
	private String selectDate="";
	private String selectHour="";
	private int type;   //��������
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

		tvTopBar.setText("ѡ��Ͳ�ʱ��");
		
		//��ȡ�û��Ѿ�Ԥ�������ŵ���Ϣ
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
	//��ʼ��ϵͳʱ��
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

	// ��ʼ���ؼ�
	private void initViews() {
//		llSelectTime = (LinearLayout) findViewById(R.id.ll_select_time); // ѡ�񶩲�ʱ��
		mPullToRefreshView = (PullToRefreshView) findViewById(R.id.table_pull_refresh_view);
		gridViewTable = (GridView) findViewById(R.id.gridview_table);
		btnBack = (Button) this.findViewById(R.id.btn_back);
		btnNextStep = (Button) this.findViewById(R.id.btn_next_step);
		timePicker = (TimePicker) findViewById(R.id.tp_time); // ʱ��ѡ����
		datePicker = (DatePicker) findViewById(R.id.dp_time); // ����ѡ����
		tvDatatimeShow = (TextView) this.findViewById(R.id.tv_datatime_show);  //ѡ��ʱ����ʾ

		tvTopBar = (TextView) this.findViewById(R.id.tv_topbar); // ������

		tvSkip = (TextView) findViewById(R.id.tv_table_skip);
	}

	// ��������ѡ������ֵ
	private void setDatePicker() {
		System.out.println("curMonthOfYear="+curMonthOfYear);
		// ���ڸı����
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
						
						tvDatatimeShow.setText(year+"��"+curMonthOfYear+"��"+curDayOfMonth+"��" +
								curHours+"ʱ"+curMinutes+"��");
						
						selectDate = 
							UtilModule.getFormatTime(curYear, curMonthOfYear, curDayOfMonth);
						
						
					}
				});
	}

	// ����ʱ��ѡ������ֵ
	private void setTimePicker() {
		timePicker.setIs24HourView(true);
		// set current time

		timePicker.setCurrentHour(curHours);
		timePicker.setCurrentMinute(curMinutes);

		// ʱ��ı����
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
					tvDatatimeShow.setText(curYear+"��"+(curMonthOfYear+1)+"��"+curDayOfMonth+"��" +
							curHours+"ʱ"+curMinutes+"��");
					
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

	// ���ÿؼ�������
	private void setListener() {
		mPullToRefreshView.setOnHeaderRefreshListener(this);
		mPullToRefreshView.setOnFooterRefreshListener(this);

		// gridview����Ӧ�¼�
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
							.getItemAtPosition(position)).getTableId(); // ���ñ��û�׼��Ԥ���ķ���id��

					final AlertDialog  confrimDlg = new AlertDialog.Builder(mContext).create();
					
					confrimDlg.show();

					Window window = confrimDlg.getWindow();

					window.setContentView(R.layout.bg_confirm_dialog);

					TextView tvConfrimMsg = (TextView) window
							.findViewById(R.id.tv_confrim_msg);
					tvConfrimMsg.setText("��ѡ����"+tableIdSelected+
							"������ȷ��Ҫ�ύô��");  
					// Ϊȷ�ϰ�ť����¼�
					TextView ok = (TextView) window.findViewById(R.id.btn_ok);
					TextView cancel = (TextView) window.findViewById(R.id.btn_cancel);

					ok.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							// ���������ӵ��߳�
							mRerserveTableThread = new ThreadRerserveTable();
							mRerserveTableThread.start();
							controllersUtil.showProgressWindow(dlgProgress, "����ѡ�������Ժ�");
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
		
		// ��һ��
		btnNextStep.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (iPickStep == PICK_TIME) {
						if(selectDate.equals(""))
						{
							controllersUtil.showToast(mContext, "����û��ѡ��Ͳ�ʱ����",
									Toast.LENGTH_SHORT);
							return false;
						}
						else if(type<=0)
						{
							controllersUtil.showToast(mContext, "��ѡ���ʱ����޷����ͣ���ѡ������ʱ��",
									Toast.LENGTH_SHORT);
							return false;
						}
						
						// ������ȡ������Ϣ���߳�
						mPullToRefreshView.headerRefreshing();
						
						iPickStep = PICK_TABLE;
						changeView(mPullToRefreshView, timePicker);
						tvTopBar.setText("ѡ��ͲͲ���");
					}
					else if (iPickStep == PICK_TABLE) {
						if (UserParam.tableId < 1) {
							controllersUtil.showToast(mContext, "����û��ѡ�������",
									Toast.LENGTH_SHORT);
							return false;
						}

					}

				}
				return false;
			}
		});

		// ���ذ�ť
		btnBack.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (iPickStep == PICK_TABLE) {
						iPickStep = PICK_TIME;
						changeView(timePicker, mPullToRefreshView);
						tvTopBar.setText("ѡ��Ͳ�ʱ��");
					}
					else if (iPickStep == PICK_TIME) {
						TableActivity.this.finish();
					}

				}
				return false;
			}
		});
		

		// ����ѡ���Ӳ���
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
					tvConfrimMsg.setText("ȷ������ѡ�������������ܵ�ˣ�ֻ�������Ʒ");
					// Ϊȷ�ϰ�ť����¼�
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

	// ����������󶩷����߳�
	private class ThreadRerserveTable extends Thread {
		@Override
		public void run() {
			try {
				String result = "";
				
				String orderId = ""+UtilModule.getCurTime(3)+"_"+UserParam.userId;  //������:ʱ��+�û�id��
				UserParam.orderId = orderId;
System.out.println("ThreadRerserveTable");
System.out.println("ThreadRerserveTable|UserParam.orderId="+UserParam.orderId);
System.out.println("ThreadRerserveTable|UserParam.userId="+UserParam.userId);
				result = setReserveTable(UserParam.orderId,UserParam.userId,tableIdSelected,selectDate,selectHour,type);

System.out.println("ThreadRerserveTable|result="+result);
				if (result.equals("success")) // ����ɹ�
				{
					mHandler.obtainMessage(RESERVE_TABLE_SUCCESS)
							.sendToTarget();// ������Ϣ��Handler
				}
				else if (result.equals("fail")) // ����ʧ��
				{
					mHandler.obtainMessage(RESERVE_TABLE_FAIL).sendToTarget();// ������Ϣ��Handler
				}

				else if (result.equals("�������쳣")) // �������쳣
				{
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
				}

			}
			catch (Exception e) {
				mRerserveTableThread = null;
			}

			mRerserveTableThread = null;
		}
	}

	// �õ�����״̬�������߳�
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

				if (result.equals("fail")) // ����ʧ��
				{
					mHandler.obtainMessage(GET_FAILED).sendToTarget();// ������Ϣ��Handler
				}

				else if (result.equals("�������쳣")) // �������쳣
				{
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
				}
				else {
					mHandler.obtainMessage(GET_FROM_NET_OK, result).sendToTarget();// ������Ϣ��Handler
				}
			}
			catch (Exception e) {
				mGetTableListThread = null;
			}
			mGetTableListThread = null;
		}
	}

	// ���߳�UI��Handler����
	Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			System.out.println("handler|getTableType="+getTableType);
			switch (msg.what) {
			
			case GET_FROM_NET_OK: // ����ɹ�
				if(getTableType == 1)
				{
					controllersUtil.showToastTwo(mContext, "�Ѹ���",
							Toast.LENGTH_SHORT);
					String strJson = "";
					strJson = (String) (msg.obj);

					tableAdapter = new TableAdapter(mContext, createData(strJson));
					gridViewTable.setAdapter(tableAdapter);
				}
				else if(getTableType == 2)
				{

					String strJson = "";
					strJson = (String) (msg.obj);  //jsons���ݼ��ص�listview��
					
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
					
					// Ϊȡ����ť����¼�
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

			case RESERVE_TABLE_SUCCESS: // Ԥ�������ɹ�
				controllersUtil.showToast(mContext, "Ԥ�������ɹ����������Ϳ��Զ�����",
						Toast.LENGTH_SHORT);

//				UserParam.tableId = tableIdSelected; // �����û�����
				
				mPullToRefreshView.headerRefreshing();
				controllersUtil.hideProgressWindow(dlgProgress);
				
				
				startActivity(new Intent(mContext,
						MainActivity.class));
				TableActivity.this.finish();
				break;
			case RESERVE_TABLE_FAIL:
				controllersUtil.showToast(mContext, "Ԥ������ʧ�ܣ����ղ�Ԥ�������ӱ���������Ԥ��",
						Toast.LENGTH_LONG);
				controllersUtil.hideProgressWindow(dlgProgress);
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
								+ UtilModule.getCurTime(1)); // ���ø���ʱ��
			}
			
			if (dlgProgress.isShowing()) {
				controllersUtil.hideProgressWindow(dlgProgress);
			}
			super.handleMessage(msg);
		}

	};

	/*
	 * ��listitem�������
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
	 * ��listitem�������
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
	
	// ��ȡ�û�Ԥ���Ĳ�������
	private String getUserReserverdTables(int userId) {
		// ��ѯ����
		String queryString = "user_id="+userId;
		// url
		String url = HttpUtil.BASE_URL + "GetUnfinishTablesServlet?"+queryString;
		
		// ��ѯ���ؽ��
		return HttpUtil.queryStringForPost(url);
	}

	// ��ȡ����״̬����
	private String getListTables(String usetime,int type) {
		// ��ѯ����
		String queryString = "use_time="+usetime+"&type="+type;
		// url
		String url = HttpUtil.BASE_URL + "ListAllTablesServlet?"+queryString;
		
		// ��ѯ���ؽ��
		return HttpUtil.queryStringForPost(url);
	}

	// Ԥ����������
	private String setReserveTable(String orderId,int userId,int tableId,String useTime,String hour,int type) {

		String queryString = "user_id=" + String.valueOf(userId)+"&table_id=" + String.valueOf(tableId)+
		"&order_id=" + orderId+
		"&use_time=" + useTime + 
		"&hour=" + hour + 
		"&type=" + String.valueOf(type);
		
		// url                                     RerserveTableServlet
		String url = HttpUtil.BASE_URL + "RerserveTableServlet?"+ queryString;

		// ��ѯ���ؽ��
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
		// ����ˢ���б��߳�;
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
