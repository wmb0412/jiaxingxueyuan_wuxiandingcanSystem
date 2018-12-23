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

//user+�����+��ˮ��(��ˮ��Ϊȫ�ֵ�������)  �����������������µ������
//�л����û���Ӧ��ˢ�²˵�??

public class OrderActivity extends Activity implements OnHeaderRefreshListener,
		OnFooterRefreshListener {
	protected final int GET_FROM_NET_OK = 0x001;// ��Ϣ:ע��ɹ�
	protected final int NET_EXCEPTION = 0x002;// ��Ϣ:�����쳣
	protected final int GET_FAILED = 0x003;// ��Ϣ��ע��ʧ��
	protected final int SERVER_EXCEPTION = 0x004;// ��Ϣ:�������쳣

	private static final int DELETE_SINGLE_DISH = 1; //ɾ����Ʒ
	
	private Context mContext;
	private ControllersUtil controllersUtil;
	private RelativeLayout relErrorMsg; // ������ϢRelativeLayout
	private TextView tvErrorMsg; // ������ϢTextView
	private ListView lvAllOrder; // ����ListView
	private TextView tvCheckTable;  //ѡ��

	private PullToRefreshView mPullToRefreshView; // ����ˢ����ͼ

	ArrayList<RerserveTableEntity> listitem; // ��Ų˵���Ϣ��ArrayList
	private OrderAdapter adapter; // ������

	private int deleteItemIndex = 0;
	private boolean isDeteleDish = false; //�Ƿ���ɾ����Ʒ
	private boolean isRefreshHeader = false; // �Ƿ�ͷ��ˢ��

	private ThreadGetOrders mGetOrders; // ��ȡ

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order);
		mContext = OrderActivity.this;
		controllersUtil= new ControllersUtil();
		listitem = new ArrayList<RerserveTableEntity>();

		initViews(); // ��ʼ���ؼ���Ϣ
		setListener();
		setPullViewListner(); // ���ÿؼ��ļ����¼�
		setLvCreateContextMenuListener();
		
		// ����û���¼�������ͷ��ˢ��
		if (UserParam.isLogin) {
			mPullToRefreshView.headerRefreshing();
	
		}

	}
	
	//����listview�����˵��¼�����
	private void setLvCreateContextMenuListener()
	{
		lvAllOrder.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			
			@Override
			public void onCreateContextMenu(ContextMenu menu, View view,
					ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				final int iItemIdx = info.position;
				deleteItemIndex = iItemIdx;
				
				menu.setHeaderTitle("ѡ����");
				menu.add(Menu.NONE, DELETE_SINGLE_DISH, 0, "ɾ������")
						.setOnMenuItemClickListener(
								new OnMenuItemClickListener() {

									@Override
									public boolean onMenuItemClick(
											MenuItem item) {
										
//										updateDishName = listitem.get(iItemIdx).getDishName();
//						
//										//����ɾ����Ʒ�߳�
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
				bundle.putBoolean("is_submit", listitem.get(position).isSubmit()); //�Ƿ��ύ����
				bundle.putBoolean("is_empty_food", listitem.get(position).isEmptyFood());//�Ƿ�����
		
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

		// ����û��ύ�˶�������ˢ�¶���&&����û�����˲�Ʒ
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

	// ��ʼ���ؼ�
	private void initViews() {
		relErrorMsg = (RelativeLayout) this.findViewById(R.id.rel_error_msg);
		tvErrorMsg = (TextView) this.findViewById(R.id.tv_error_msg);

		mPullToRefreshView = (PullToRefreshView) this.findViewById(R.id.main_pull_refresh_view);// ����ˢ����ͼ
		lvAllOrder = (ListView)this.findViewById(R.id.lv_reserve_table);
		tvCheckTable = (TextView) this.findViewById(R.id.tv_check_table); //ѡ��
	}
	
	private void setListener()
	{
		//ѡ��
		tvCheckTable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// ���δ��⵽�û���¼������ת����¼����
				if (!UserParam.isLogin) {
					startActivity(new Intent(OrderActivity.this, LoginActivity.class));
				}
				else
					startActivity(new Intent(OrderActivity.this, TableActivity.class));
				
			}
		});
	}
	
	// ���ÿؼ��ļ����¼�
	private void setPullViewListner() {
		mPullToRefreshView.setOnHeaderRefreshListener(this);
		mPullToRefreshView.setOnFooterRefreshListener(this);
	}


	/*
	 * ��ArrayList(listitem)��ֵ
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
				String reserveTime = js.getString("reserve_time");  //Ԥ��ʱ��

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
	 * ��ȡԤ���������߳�
	 */
	private class ThreadGetOrders extends Thread {
		@Override
		public void run() {
			try {

				String result = getOrders(UserParam.userId);

				if (result.equals("fail")) // ע��ʧ��
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
				mGetOrders = null;
			}

			mGetOrders = null;

		}
	}

	// ���߳�UI��Handler����
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case GET_FROM_NET_OK: // ����ɹ�
				controllersUtil.showToastTwo(mContext, "�Ѹ���",
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


	// ��ȡ�û�Ԥ���Ĳ�������
	private String getOrders(int userId) {
		// ��ѯ����
		String queryString = "user_id="+userId;
		// url
		String url = HttpUtil.BASE_URL + "GetOrdersServlet?"+queryString;
		
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