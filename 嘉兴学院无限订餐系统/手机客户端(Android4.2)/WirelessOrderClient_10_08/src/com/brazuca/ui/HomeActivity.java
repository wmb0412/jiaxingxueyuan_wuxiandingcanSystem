package com.brazuca.ui;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.brazuca.entity.HomeDishEntity;
import com.brazuca.entity.PopItemClasifyEntity;
import com.brazuca.entity.UserParam;
import com.brazuca.network.HttpUtil;
import com.brazuca.network.NetWorkUtil;
import com.brazuca.ui.adapter.HomeDishAdapter;
import com.brazuca.ui.adapter.PopItemClasifyAdapter;
import com.brazuca.ui.adapter.PopItemSortAdapter;
import com.brazuca.ui.animation.MyAnimation;
import com.brazuca.ui.view.PullToRefreshView;
import com.brazuca.ui.view.PullToRefreshView.OnFooterRefreshListener;
import com.brazuca.ui.view.PullToRefreshView.OnHeaderRefreshListener;
import com.brazuca.util.ControllersUtil;
import com.brazuca.util.UtilModule;
import com.brazuca.util.sort.DishPriceSortAsc;
import com.brazuca.util.sort.DishPriceSortDesc;
import com.brazuca.util.sort.DishTimeSort;

public class HomeActivity extends Activity implements OnHeaderRefreshListener,
		OnFooterRefreshListener, OnItemClickListener {
	protected static final int EVERY_TIME_GET_LIST_COUNT = 25; // ÿ�λ�ȡ��listview����ĿΪ25��
	protected static final int PULL_TO_REFRESH_HEADER = 1; // ͷ��ˢ��
	protected static final int PULL_TO_REFRESH_FOOTER = 2; // �ײ�ˢ��

	protected static final int GET_FROM_NET_OK = 0x001;// ��Ϣ:ע��ɹ�
	protected static final int NET_EXCEPTION = 0x002;// ��Ϣ:�����쳣
	protected static final int GET_FAILED = 0x003;// ��Ϣ��ע��ʧ��
	protected static final int SERVER_EXCEPTION = 0x004;// ��Ϣ:�������쳣
	protected static final int NO_MORE_DISH = 0x005; // ��Ϣ��û�в�Ʒ��
	protected static final int GET_DISH_COUNT_FROM_NET_OK = 0x006;
	protected final int PUSH_TO_ORDER_DETAILS_OK = 0x007;// ��Ϣ��������ϸ�˵��ɹ�
	protected final int PUSH_TO_ORDER_DETAILS_FAILED = 0x008;// ��Ϣ��������ϸ�˵�ʧ��
	protected final int PUSH_TO_ORDER_DETAILS = 0x009; // ��Ϣ�����ͼ�����ϸ�˵�����
	protected final int START_ACTIVITY_lOGIN = 0x010; // ��Ϣ���򿪵�¼����
	protected final int START_ACTIVITY_TABLE = 0x011;// ��Ϣ����ѡ���������

	private Context mContext;
	private static final int NO_SHOW = 0;
	private static final int LEFT_SHOW = 1;
	private static final int RIGHT_SHOW = 2;

	private static int screenHeight = 0;// ��Ļ�߶�(pixel)
	private static int screenWidth = 0;// ��Ļ���(pixel)
	private static int statusBarHeight = 0;
	private PopupWindow pwClasify; // ��Ʒ���൯���˵�
	private PopupWindow pwSort; // ��Ʒ����
	private ListView lvClasify;
	private ListView lvSort;

	private AlertDialog dlg;
	private ImageView ivBackTop; // ��listview������������ť

	private PullToRefreshView mPullToRefreshView; // ����ˢ����ͼ
	private PullToRefreshView mSaleToRefreshView; // ����ˢ����ͼ
	private PullToRefreshView mSpecialToRefreshView; // ����ˢ����ͼ
	private PullToRefreshView mDrinkToRefreshView; // ����ˢ����ͼ
	private PullToRefreshView mMainFoodToRefreshView; // ����ˢ����ͼ
	private PullToRefreshView mUnitToRefreshView; // ����ˢ����ͼ

	private ListView lvDish; // ʳƷ�б�
	private ListView lvSale; // �ؼ��б�
	private ListView lvDrink; // ��ˮ�б�
	private ListView lvSpecial;
	private ListView lvMainFood;
	private ListView lvUnit;

	ArrayList<PopItemClasifyEntity> listitemLeft;
	ArrayList<String> listitemRight;
	ArrayList<HomeDishEntity> listitemDish;
	ArrayList<HomeDishEntity> listitemSaleDish;
	ArrayList<HomeDishEntity> listitemSpecialDish;
	ArrayList<HomeDishEntity> listitemDrinkDish;
	ArrayList<HomeDishEntity> listitemmFoodDish;
	ArrayList<HomeDishEntity> listitemUnitDish;

	HomeDishAdapter homeDishAdapter;
	HomeDishAdapter homeSaleDishAdapter;
	HomeDishAdapter homeDrinkDishAdapter;
	HomeDishAdapter homemFoodDishAdapter;

	private RelativeLayout relTopBar;
	private LinearLayout llClacify;
	private LinearLayout llSort;
	private LinearLayout llTopMenuBar;
	private TextView tvTopBar;
	private TextView tvCheckTable;  //ѡ��
 	private TextView tvClaContent;
	private TextView tvSortContent;
	private ImageView ivClaArrow;
	private ImageView ivSortArrow;
	private int iClickType = 0;
	private static int startIndex = 0;
	private static int endIndex = 25;
	private int startIndexSale = 0;
	private int endIndexSale = 25;
	private int startIndexDrink = 0;
	private int endIndexDrink = 25;
	private int startIndexmFood = 0;
	private int endIndexmFood = 25;

	private int iLvDishCount = 0; // ��Ʒlistview����Ŀ��
	private int iLvSaleDishCount = 0; // �ؼ�listview����Ŀ��
	private int iLvDrinkDishCount = 0;
	private int iLvmFoodDishCount = 0;

	private int iAllRefreshType = 1; // ˢ������--1��ͷ��ˢ�£�2--�ײ�ˢ��
	private int iSaleRefreshType = 1; // ˢ������--1��ͷ��ˢ�£�2--�ײ�ˢ��
	private int iSpecialRefreshType = 1; // ˢ������--1��ͷ��ˢ�£�2--�ײ�ˢ��
	private int iDrinkRefreshType = 1; // ˢ������--1��ͷ��ˢ�£�2--�ײ�ˢ��
	private int iMainFoodRefreshType = 1; // ˢ������--1��ͷ��ˢ�£�2--�ײ�ˢ��
	private int iUnitRefreshType = 1; // ˢ������--1��ͷ��ˢ�£�2--�ײ�ˢ��
	private int iListType = 1; // �б����ͣ�1--ȫ�����ݣ�2--�ؼ۲ˣ�3--���Ʋˣ�4--��ˮ��5--��ʳ��6--�ײ�
	// private boolean isFirstRefreshAll = false; //�Ƿ�˵˵��һ��ˢ��ȫ���б�
	private int iNotificationProgress = 0;

	private int[] iClassificationSingleTotalCount={};  //������ÿ����������Ŀ

	private ControllersUtil controllersUtil;

	private ThreadGetListDish mGetListAllDishThread;
	private ThreadGetListDish mGetListSaleDishThread;
	private ThreadGetListDish mGetListDrinkDishThread;
	private ThreadGetListDish mGetListmFoodDishThread;

	private ThreadPushToOrderDetails mPushToOrderDetailsThread; // ������ϸ�˵�
	// public ThreadGetDishCount mGetDishCountThread; //��ȡ��Ʒ����ĸ���

	// �����˵���item ������
	private String[] lv_classify_item_content = { "ȫ��", "�ؼ۲�", "���Ʋ�", "��ˮ",
			"��ʳ", "�ײ�" };

	// �����˵���item ������
	private String[] lv_sort_item_content = { "Ĭ������", "�۸����", "�۸����", "�������",
			"���·���" };

	private String strDefaultDishSort = lv_sort_item_content[0];
	private String strDishAllSort = strDefaultDishSort;
	private String strDishSaleSort = strDefaultDishSort;
	private String strDishDrinkSort = strDefaultDishSort;
	private String strDishmMFoodSort = strDefaultDishSort;

	private MediaPlayer mediaPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// savedInstanceState.
		// TODO Auto-generated method stub
		System.out.println("onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		mContext = HomeActivity.this;
		mediaPlayer = MediaPlayer.create(mContext, R.raw.newdishtoast);

		// ��ʼ��mPullToRefreshView
		initPullView();

		initWindowParams(this);
		controllersUtil = new ControllersUtil();
		dlg = new AlertDialog.Builder(mContext).create();

		// ʵ����ArrayList
		listitemLeft = new ArrayList<PopItemClasifyEntity>();
		listitemRight = new ArrayList<String>();
		listitemDish = new ArrayList<HomeDishEntity>();

		initListViews(); // ��ʼ�� ListView�ؼ�
		initViews(); // ��ʼ���ؼ�

		setListViewListner(); // ��ListView���ü����¼�
		setListner(); // ���ؼ����ü����¼�

		setPullViewListner(); // ��������������ˢ�µļ����¼�

//		if (!UserParam.nickname.equals("")) {
//			tvTopBar.setText(UserParam.nickname);
//		}
//		else {
//			tvTopBar.setText(getString(R.string.app_name));
//		}

		if (iLvDishCount == 0) {
			startIndex = 0;
			endIndex = EVERY_TIME_GET_LIST_COUNT;
		}

		mPullToRefreshView.headerRefreshing();

		// mGetListAllDishThread = new ThreadGetListAllDish();
		// mGetListAllDishThread.start();
	}

	// ��ʼ�������б�
	private void initPullView() {
		mPullToRefreshView = (PullToRefreshView) findViewById(R.id.main_pull_refresh_view);// ����ˢ����ͼ
		mSaleToRefreshView = (PullToRefreshView) findViewById(R.id.sale_pull_refresh_view);// ����ˢ����ͼ
		mDrinkToRefreshView = (PullToRefreshView) findViewById(R.id.drink_pull_refresh_view);// ����ˢ����ͼ
		mSpecialToRefreshView = (PullToRefreshView) findViewById(R.id.special_pull_refresh_view);// ����ˢ����ͼ
		mMainFoodToRefreshView = (PullToRefreshView) findViewById(R.id.main_food_pull_refresh_view);// ����ˢ����ͼ
		mUnitToRefreshView = (PullToRefreshView) findViewById(R.id.unit_pull_refresh_view);// ����ˢ����ͼ
	}

	// ��������������ˢ�µļ����¼�
	private void setPullViewListner() {
		mPullToRefreshView.setOnHeaderRefreshListener(this);
		mPullToRefreshView.setOnFooterRefreshListener(this);

		mSaleToRefreshView.setOnHeaderRefreshListener(this);
		mSaleToRefreshView.setOnFooterRefreshListener(this);

		mDrinkToRefreshView.setOnHeaderRefreshListener(this);
		mDrinkToRefreshView.setOnFooterRefreshListener(this);

		mSpecialToRefreshView.setOnHeaderRefreshListener(this);
		mSpecialToRefreshView.setOnFooterRefreshListener(this);

		mMainFoodToRefreshView.setOnHeaderRefreshListener(this);
		mMainFoodToRefreshView.setOnFooterRefreshListener(this);

		mUnitToRefreshView.setOnHeaderRefreshListener(this);
		mUnitToRefreshView.setOnFooterRefreshListener(this);
	}

	// ��ʼ�� ListView�ؼ�
	private void initListViews() {
		lvDish = (ListView) findViewById(R.id.lv_home);
		lvSale = (ListView) findViewById(R.id.lv_sale);
		lvDrink = (ListView) findViewById(R.id.lv_drink);

		lvSpecial = (ListView) findViewById(R.id.lv_special);
		lvMainFood = (ListView) findViewById(R.id.lv_main_food);
		lvUnit = (ListView) findViewById(R.id.lv_unit);
	}

	// ��ʼ�� �ؼ�
	private void initViews() {
		ivBackTop = (ImageView) findViewById(R.id.iv_back_top);
		relTopBar = (RelativeLayout) findViewById(R.id.rel_topbar);

		llTopMenuBar = (LinearLayout) findViewById(R.id.ll_top_menu_bar);
		llClacify = (LinearLayout) findViewById(R.id.ll_clasify); // ����
		llSort = (LinearLayout) findViewById(R.id.ll_sort); // ����

		tvCheckTable = (TextView) this.findViewById(R.id.tv_check_table); //ѡ��

		tvTopBar = (TextView) findViewById(R.id.tv_home_activity_topbar);
		tvClaContent = (TextView) findViewById(R.id.tv_cla_item_content);
		tvSortContent = (TextView) findViewById(R.id.tv_sort_item_content);

		ivClaArrow = (ImageView) findViewById(R.id.iv_cla_arrow);
		ivSortArrow = (ImageView) findViewById(R.id.iv_sort_arrow);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		System.out.println("onResume");

		super.onResume();
		// ͨ���û���¼�Ժ�������̧ͷΪ�û��ǳ�
		Bundle bundle = new Bundle();
		bundle = getIntent().getExtras();
		if (bundle != null) {
			tvTopBar.setText(bundle.getString("nickname"));
		}
		else
			tvTopBar.setText(getString(R.string.app_name));

	}

	// listview��item��Ӧ�¼�
	private void setListViewListner() {
		lvDish.setOnItemClickListener(this);
		lvSale.setOnItemClickListener(this);
		lvDrink.setOnItemClickListener(this);
		lvMainFood.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long arg3) {
		Intent intent = null;
		Bundle bundle = null;

		switch (adapterView.getId()) {

		case R.id.lv_home: // ȫ��
			intent = new Intent();
			bundle = new Bundle();
			bundle.putString("dish_id", listitemDish.get(position).getDishId()
					+ "");
			bundle.putString("dish_cur_price", listitemDish.get(position)
					.getCurPrice() + "");
			bundle.putString("dish_ori_price", listitemDish.get(position)
					.getOriPrice() + "");
			bundle.putString("dish_name", listitemDish.get(position)
					.getDishName() + "");
			bundle.putString("dish_introduction", listitemDish.get(position)
					.getIntroduction() + "");
			bundle.putString("dish_pic_url", listitemDish.get(position)
					.getPicUrl() + "");
			bundle.putString("order_times", listitemDish.get(position)
					.getOrderTimes() + "");
			intent.setClass(HomeActivity.this, DishDetailsActivity.class);

			intent.putExtras(bundle);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			break;

		case R.id.lv_sale: // �ؼ۲�
			intent = new Intent();
			bundle = new Bundle();
			bundle.putString("dish_cur_price", listitemSaleDish.get(position)
					.getCurPrice() + "");
			bundle.putString("dish_ori_price", listitemSaleDish.get(position)
					.getOriPrice() + "");
			bundle.putString("dish_name", listitemSaleDish.get(position)
					.getDishName() + "");
			bundle.putString("dish_introduction", listitemSaleDish
					.get(position).getIntroduction() + "");
			bundle.putString("dish_pic_url", listitemSaleDish.get(position)
					.getPicUrl() + "");
			bundle.putString("order_times", listitemSaleDish.get(position)
					.getOrderTimes() + "");
			intent.setClass(HomeActivity.this, DishDetailsActivity.class);

			intent.putExtras(bundle);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			break;
		case R.id.lv_drink: // ��ˮ
			intent = new Intent();
			bundle = new Bundle();
			bundle.putString("dish_cur_price", listitemDrinkDish.get(position)
					.getCurPrice() + "");
			bundle.putString("dish_ori_price", listitemDrinkDish.get(position)
					.getOriPrice() + "");
			bundle.putString("dish_name", listitemDrinkDish.get(position)
					.getDishName() + "");
			bundle.putString("dish_introduction",
					listitemDrinkDish.get(position).getIntroduction() + "");
			bundle.putString("dish_pic_url", listitemDrinkDish.get(position)
					.getPicUrl() + "");
			bundle.putString("order_times", listitemDrinkDish.get(position)
					.getOrderTimes() + "");
			intent.setClass(HomeActivity.this, DishDetailsActivity.class);

			intent.putExtras(bundle);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			break;
		case R.id.lv_main_food: // ��ʳ
			intent = new Intent();
			bundle = new Bundle();
			bundle.putString("dish_cur_price", listitemmFoodDish.get(position)
					.getCurPrice() + "");
			bundle.putString("dish_ori_price", listitemmFoodDish.get(position)
					.getOriPrice() + "");
			bundle.putString("dish_name", listitemmFoodDish.get(position)
					.getDishName() + "");
			bundle.putString("dish_introduction",
					listitemmFoodDish.get(position).getIntroduction() + "");
			bundle.putString("dish_pic_url", listitemmFoodDish.get(position)
					.getPicUrl() + "");
			bundle.putString("order_times", listitemmFoodDish.get(position)
					.getOrderTimes() + "");
			intent.setClass(HomeActivity.this, DishDetailsActivity.class);

			intent.putExtras(bundle);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			break;

		default:
			break;
		}

	}
	
	/*
	 * ��ÿɼ���Listview
	 */
	private ListView getVisibleListview(int iListType) {
		switch (iListType) {
		case 1:
			return lvDish;

		case 2:

			return lvSale;
		case 3:

			return lvSpecial;
		case 4:

			return lvDrink;
		case 5:

			return lvMainFood;
		case 6:

			return lvUnit;

		default:
			break;
		}
		return null;
	}

	// ���ü����¼�
	private void setListner() {
		//ѡ��
		tvCheckTable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// ���δ��⵽�û���¼������ת����¼����
				if (!UserParam.isLogin) {
					startActivity(new Intent(HomeActivity.this, LoginActivity.class));
				}
				else
					startActivity(new Intent(HomeActivity.this, TableActivity.class));
				
			}
		});
		
		//��������¼�
		ivBackTop.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View parent, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//					getVisibleListview(iListType).scrollTo(0, 0);
					getVisibleListview(iListType).setSelection(0);
				}
				return false;
			}
		});

		// ��Ʒ���൯���˵��¼�
		llClacify.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View parent, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

					iClickType = LEFT_SHOW;
					changPopState(parent, iClickType);
				}
				return false;
			}
		});

		// ���򵯳��˵��¼�
		llSort.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View parent, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					iClickType = RIGHT_SHOW;
					changPopState(parent, iClickType);
				}
				return false;
			}
		});
	}

	// չʾ��ͬ��ListView
	private void showListView(int iListType) {
//		System.out.println("showListView:" + iListType);
		
		switch (iListType) {

		case 1: // ȫ��
			if (listitemDish == null)
				listitemDish = new ArrayList<HomeDishEntity>();

			changeListViewVisivility(mPullToRefreshView, mSaleToRefreshView,
					mSpecialToRefreshView, mMainFoodToRefreshView,
					mDrinkToRefreshView, mUnitToRefreshView);
			
			if (listitemDish.size() < 1) {
				ivBackTop.setVisibility(View.GONE);
				// �����̻߳�ȡ�ؼ۲��б�
				mGetListAllDishThread = new ThreadGetListDish(1);
				mGetListSaleDishThread.start();
				
			}
			tvSortContent.setText(strDishAllSort);

			break;
		case 2: // �ؼ۲�
			if (listitemSaleDish == null)
				listitemSaleDish = new ArrayList<HomeDishEntity>();

			changeListViewVisivility(mSaleToRefreshView, mPullToRefreshView,
					mSpecialToRefreshView, mMainFoodToRefreshView,
					mDrinkToRefreshView, mUnitToRefreshView);
			if (listitemSaleDish.size() < 1) {
				ivBackTop.setVisibility(View.GONE);
				// �����̻߳�ȡ�ؼ۲��б�
				mSaleToRefreshView.headerRefreshing();
			}
			tvSortContent.setText(strDishSaleSort);

			break;
		case 3:

			break;
		case 4: // ��ˮ
			if (listitemDrinkDish == null)
				listitemDrinkDish = new ArrayList<HomeDishEntity>();

			changeListViewVisivility(mDrinkToRefreshView, mSaleToRefreshView,
					mPullToRefreshView, mSpecialToRefreshView,
					mMainFoodToRefreshView, mUnitToRefreshView);
			if (listitemDrinkDish.size() < 1) {
				ivBackTop.setVisibility(View.GONE);
				// �����̻߳�ȡ�ؼ۲��б�

				mDrinkToRefreshView.headerRefreshing();
			}
			tvSortContent.setText(strDishDrinkSort);
			break;

		case 5: // ��ʳ
			if (listitemmFoodDish == null)
				listitemmFoodDish = new ArrayList<HomeDishEntity>();

			changeListViewVisivility(mMainFoodToRefreshView,
					mSaleToRefreshView, mPullToRefreshView,
					mSpecialToRefreshView, mDrinkToRefreshView,
					mUnitToRefreshView);
			if (listitemmFoodDish.size() < 1) {
				ivBackTop.setVisibility(View.GONE);
				// �����̻߳�ȡ��ʳ�б�
				mMainFoodToRefreshView.headerRefreshing();
			}
			tvSortContent.setText(strDishmMFoodSort);
			break;
		case 6:

			break;

		default:
			break;
		}

	}

	// lvClasify��Ӧ�¼�
	public void lvClasifyMenuEvent() {

		lvClasify.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long arg3) {

				if (pwClasify != null) {
					pwClasify.dismiss();
				}

				tvClaContent.setText(lv_classify_item_content[position]);
				iListType = position + 1;
				showListView(iListType);

				// showNotification(lv_classify_item_content[position]);
			}
		});
	}

	// ��Ʒ��������
	private void sortClassification(int position) {
		System.out.println("sortClassification=" + iListType);
		switch (iListType) {
		/*
		 * ȫ��������
		 */
		case 1:

			switch (position) {
			case 0: // Ĭ������

				break;
			case 1: // �۸����

				// �Լ۸���н�������
				Collections.sort(listitemDish, new DishPriceSortDesc());
				homeDishAdapter = new HomeDishAdapter(mContext, listitemDish,
						iListType);
				lvDish.setAdapter(homeDishAdapter);

				break;
			case 2:
				// �Լ۸������������
				Collections.sort(listitemDish, new DishPriceSortAsc());
				homeDishAdapter = new HomeDishAdapter(mContext, listitemDish,
						iListType);
				lvDish.setAdapter(homeDishAdapter);

				// showNotification(lv_sort_item_content[position]);
				break;
			case 3:

				break;
			case 4: // ���·���

				Collections.sort(listitemDish, new DishTimeSort());
				homeDishAdapter = new HomeDishAdapter(mContext, listitemDish,
						iListType);
				lvDish.setAdapter(homeDishAdapter);
				break;

			default:
				break;
			}

			break;
		/*
		 * �ؼ۲�����
		 */
		case 2:
			switch (position) {
			case 0: // Ĭ������

				break;
			case 1: // �۸����

				// �Լ۸���н�������
				Collections.sort(listitemSaleDish, new DishPriceSortDesc());
				homeSaleDishAdapter = new HomeDishAdapter(mContext,
						listitemSaleDish, iListType);
				lvSale.setAdapter(homeSaleDishAdapter);

				break;
			case 2:
				// �Լ۸������������
				Collections.sort(listitemSaleDish, new DishPriceSortAsc());
				homeSaleDishAdapter = new HomeDishAdapter(mContext,
						listitemSaleDish, iListType);
				lvSale.setAdapter(homeSaleDishAdapter);

				// showNotification(lv_sort_item_content[position]);
				break;
			case 3:

				break;
			case 4: // ���·���
				Collections.sort(listitemSaleDish, new DishTimeSort());
				homeSaleDishAdapter = new HomeDishAdapter(mContext,
						listitemSaleDish, iListType);
				lvSale.setAdapter(homeSaleDishAdapter);
				break;

			default:
				break;
			}

			break;
		case 3:

			break;
		/*
		 * ��ˮ����
		 */
		case 4:
			switch (position) {
			case 0: // Ĭ������

				break;
			case 1: // �۸����

				// �Լ۸���н�������
				Collections.sort(listitemDrinkDish, new DishPriceSortDesc());
				homeDrinkDishAdapter = new HomeDishAdapter(mContext,
						listitemDrinkDish, iListType);
				lvDrink.setAdapter(homeDrinkDishAdapter);

				break;
			case 2:
				// �Լ۸������������
				Collections.sort(listitemDrinkDish, new DishPriceSortAsc());
				homeDrinkDishAdapter = new HomeDishAdapter(mContext,
						listitemDrinkDish, iListType);
				lvDrink.setAdapter(homeDrinkDishAdapter);

				break;
			case 3:

				break;
			case 4: // ���·���
				Collections.sort(listitemDrinkDish, new DishTimeSort());
				homeDrinkDishAdapter = new HomeDishAdapter(mContext,
						listitemDrinkDish, iListType);
				lvDrink.setAdapter(homeDrinkDishAdapter);
				break;

			default:
				break;
			}

			break;
		/*
		 * ��ʳ����
		 */
		case 5:

			switch (position) {
			case 0: // Ĭ������

				break;
			case 1: // �۸����

				// �Լ۸���н�������
				Collections.sort(listitemmFoodDish, new DishPriceSortDesc());
				homemFoodDishAdapter = new HomeDishAdapter(mContext,
						listitemmFoodDish, iListType);
				lvMainFood.setAdapter(homemFoodDishAdapter);

				break;
			case 2:
				// �Լ۸������������
				Collections.sort(listitemmFoodDish, new DishPriceSortAsc());
				homemFoodDishAdapter = new HomeDishAdapter(mContext,
						listitemmFoodDish, iListType);
				lvMainFood.setAdapter(homemFoodDishAdapter);

				break;
			case 3:

				break;
			case 4: // ���·���
				Collections.sort(listitemmFoodDish, new DishTimeSort());
				homemFoodDishAdapter = new HomeDishAdapter(mContext,
						listitemmFoodDish, iListType);
				lvMainFood.setAdapter(homemFoodDishAdapter);
				break;

			default:
				break;
			}

			break;
		case 6:

			break;

		default:
			break;
		}
	}

	/*
	 * ��¼ÿ��listview��һ��ʹ�õ�����ʽ
	 */
	private void setLastSortLable(int iListType, String strLastSortLable) {
		switch (iListType) {
		case 1:
			strDishAllSort = strLastSortLable;
			break;
		case 2:
			strDishSaleSort = strLastSortLable;
			break;
		case 3:

			break;
		case 4:
			strDishDrinkSort = strLastSortLable;
			break;
		case 5:
			strDishmMFoodSort = strLastSortLable;
			break;
		case 6:

			break;

		default:
			break;
		}
	}

	// lvSort��Ӧ�¼�
	public void lvSortMenuEvent() {

		lvSort.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long arg3) {

				String strLastSortLable = lv_sort_item_content[position];

				tvSortContent.setText(strLastSortLable);
				if (pwSort != null) {
					pwSort.dismiss();
				}

				setLastSortLable(iListType, strLastSortLable); // ��¼ÿ��listview��һ��ʹ�õ�����ʽ

				sortClassification(position); // ��Ʒ��������

			}
		});
	}

	// ��ȡȫ����Ʒ�߳�
	private class ThreadGetListDish extends Thread {
		private int iType;

		public ThreadGetListDish(int iListType) // �̹߳��캯��
		{
			this.iType = iListType;
		}

		@Override
		public void run() {
			try {
				System.out.println("ThreadGetListAllDish");
				String result = "";
				Log.d("ThreadGetListAllDish", "ThreadGetListAllDish");

				switch (iType) {
				case 1: // ȫ���б�

					if (iAllRefreshType == PULL_TO_REFRESH_HEADER)
						result = getListDishes(0, EVERY_TIME_GET_LIST_COUNT);

					else if (iAllRefreshType == PULL_TO_REFRESH_FOOTER)
						result = getListDishes(startIndex, endIndex);

					if (result.equals("fail")) // ����ʧ��
					{
						mHandler.obtainMessage(GET_FAILED).sendToTarget();// ������Ϣ��Handler
					}
					else if (result.equals("nomoredish")) // û�в�Ʒ
					{
						mHandler.obtainMessage(NO_MORE_DISH).sendToTarget();// ������Ϣ��Handler
					}

					else if (result.equals("�������쳣")) // �������쳣
					{
						mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
					}
					else {
						mHandler.obtainMessage(GET_FROM_NET_OK, result)
								.sendToTarget();// ������Ϣ��Handler
					}

					break;
				case 2: // �ؼ۲�
					if (iSaleRefreshType == PULL_TO_REFRESH_HEADER) {
						result = getListDishes(0, EVERY_TIME_GET_LIST_COUNT);
					}

					else if (iSaleRefreshType == PULL_TO_REFRESH_FOOTER)
						result = getListDishes(startIndexSale, endIndexSale);

					if (result.equals("fail")) // ����ʧ��
					{
						mHandler.obtainMessage(GET_FAILED).sendToTarget();// ������Ϣ��Handler
					}
					else if (result.equals("nomoredish")) // û�в�Ʒ
					{
						mHandler.obtainMessage(NO_MORE_DISH).sendToTarget();// ������Ϣ��Handler
					}

					else if (result.equals("�������쳣")) // �������쳣
					{
						mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
					}
					else {
						mHandler.obtainMessage(GET_FROM_NET_OK, result)
								.sendToTarget();// ������Ϣ��Handler
					}
					break;
				case 3: // ���Ʋ�

					break;
				case 4: // ��ˮ
					if (iDrinkRefreshType == PULL_TO_REFRESH_HEADER) {
						result = getListDishes(0, EVERY_TIME_GET_LIST_COUNT);
					}

					else if (iDrinkRefreshType == PULL_TO_REFRESH_FOOTER)
						result = getListDishes(startIndexDrink, endIndexDrink);

					if (result.equals("fail")) // ����ʧ��
					{
						mHandler.obtainMessage(GET_FAILED).sendToTarget();// ������Ϣ��Handler
					}
					else if (result.equals("nomoredish")) // û�в�Ʒ
					{
						mHandler.obtainMessage(NO_MORE_DISH).sendToTarget();// ������Ϣ��Handler
					}

					else if (result.equals("�������쳣")) // �������쳣
					{
						mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
					}
					else {
						mHandler.obtainMessage(GET_FROM_NET_OK, result)
								.sendToTarget();// ������Ϣ��Handler
					}
					break;
				case 5: // ��ʳ
					if (iMainFoodRefreshType == PULL_TO_REFRESH_HEADER) {
						result = getListDishes(0, EVERY_TIME_GET_LIST_COUNT);
					}

					else if (iMainFoodRefreshType == PULL_TO_REFRESH_FOOTER)
						result = getListDishes(startIndexmFood, endIndexmFood);

					if (result.equals("fail")) // ����ʧ��
					{
						mHandler.obtainMessage(GET_FAILED).sendToTarget();// ������Ϣ��Handler
					}
					else if (result.equals("nomoredish")) // û�в�Ʒ
					{
						mHandler.obtainMessage(NO_MORE_DISH).sendToTarget();// ������Ϣ��Handler
					}

					else if (result.equals("�������쳣")) // �������쳣
					{
						mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
					}
					else {
						mHandler.obtainMessage(GET_FROM_NET_OK, result)
								.sendToTarget();// ������Ϣ��Handler
					}
					break;
				case 6: // �ײ�

					break;

				default:
					break;
				}

			}
			catch (Exception e) {
				switch (iType) {
				case 1:
					mGetListAllDishThread = null;
					break;
				case 2:
					mGetListSaleDishThread = null;
					break;

				case 3:

					break;

				case 4:
					mGetListDrinkDishThread = null;
					break;

				case 5:
					mGetListmFoodDishThread = null;
					break;

				case 6:

					break;

				default:
					break;
				}

			}

			switch (iType) {
			case 1:
				mGetListAllDishThread = null;
				break;
			case 2:
				mGetListSaleDishThread = null;
				break;

			case 3:

				break;

			case 4:
				mGetListDrinkDishThread = null;
				break;

			case 5:
				mGetListmFoodDishThread = null;
				break;

			case 6:

				break;

			default:
				break;
			}

		}
	}

	// �õ���Ʒ����ĸ����߳�
	// private class ThreadGetDishCount extends Thread {
	// @Override
	// public void run() {
	// try {
	// String result = "";
	// System.out.println("ThreadGetDishCount");
	//
	// result = getDishCount();
	//
	// if (result.equals("fail")) // ����ʧ��
	// {
	// // mHandler.obtainMessage(GET_FAILED).sendToTarget();// ������Ϣ��Handler
	// }
	//
	// else if (result.equals("�������쳣")) // �������쳣
	// {
	// // mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();//
	// ������Ϣ��Handler
	// }
	// else {
	// mHandler.obtainMessage(GET_DISH_COUNT_FROM_NET_OK, result)
	// .sendToTarget();// ������Ϣ��Handler
	// }
	//
	// }
	// catch (Exception e) {
	// mGetDishCountThread = null;
	// }
	//
	// mGetDishCountThread = null;
	//
	// }
	// }

	/*
	 * ��listitemDish�������
	 */
	private ArrayList<HomeDishEntity> createListViewData(String obj,
			int iListType) {

		JSONArray jsArray = null;
		try {
			jsArray = new JSONArray(obj);
		}
		catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HomeDishEntity homeDishEntity = null;

		switch (iListType) {
		case 1: // ȫ��
			if (iAllRefreshType == PULL_TO_REFRESH_HEADER
					&& listitemDish.size() > 0) {
				listitemDish.clear();
			}

			try {

				for (int i = 0; i < jsArray.length(); i++) {
					homeDishEntity = new HomeDishEntity();
					JSONObject js = new JSONObject();
					js = jsArray.getJSONObject(i);
					int dishId = js.getInt("dishId");
					String dishName = js.getString("dishName");
					double curPrice = js.getDouble("currentPrice");
					double oriPrice = js.getDouble("originalPrice");
					String pictureUrl = js.getString("pictureUrl");
					// String price = String.valueOf(dPrice);
					String introduction = js.getString("introduction");
					String timeToMarket = js.get("timeToMarket").toString(); // ����ʱ��
					int orderTimes = js.getInt("orderTimes"); // �������

					homeDishEntity.setDishId(dishId);
					homeDishEntity.setDishName(dishName);
					homeDishEntity.setCurPrice(curPrice);
					homeDishEntity.setOriPrice(oriPrice);
					homeDishEntity.setIntroduction(introduction);
					homeDishEntity.setPicUrl(pictureUrl);
					homeDishEntity.setTimeToMarket(timeToMarket);
					homeDishEntity.setOrderTimes(orderTimes);

					listitemDish.add(homeDishEntity);
				}

			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return listitemDish;
		case 2: // �ؼ۲�
			if (iSaleRefreshType == PULL_TO_REFRESH_HEADER
					&& listitemSaleDish.size() > 0) {
				listitemSaleDish.clear();
			}
			try {

				for (int i = 0; i < jsArray.length(); i++) {
					homeDishEntity = new HomeDishEntity();
					JSONObject js = new JSONObject();
					js = jsArray.getJSONObject(i);
					// int dishId = js.getInt("dishId");
					String dishName = js.getString("dishName");
					double curPrice = js.getDouble("currentPrice");
					double oriPrice = js.getDouble("originalPrice");
					String pictureUrl = js.getString("pictureUrl");
					String introduction = js.getString("introduction");
					String timeToMarket = js.get("timeToMarket").toString(); // ����ʱ��
					int orderTimes = js.getInt("orderTimes"); // �������

					homeDishEntity.setTimeToMarket(timeToMarket);
					homeDishEntity.setOrderTimes(orderTimes);

					homeDishEntity.setDishName(dishName);
					homeDishEntity.setCurPrice(curPrice);
					homeDishEntity.setOriPrice(oriPrice);
					homeDishEntity.setIntroduction(introduction);
					homeDishEntity.setPicUrl(pictureUrl);

					listitemSaleDish.add(homeDishEntity);
				}

			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return listitemSaleDish;
		case 3:

			break;
		case 4: // ��ˮ
			if (iDrinkRefreshType == PULL_TO_REFRESH_HEADER
					&& listitemDrinkDish.size() > 0) {
				listitemDrinkDish.clear();
			}
			try {

				for (int i = 0; i < jsArray.length(); i++) {
					homeDishEntity = new HomeDishEntity();
					JSONObject js = new JSONObject();
					js = jsArray.getJSONObject(i);
					// int dishId = js.getInt("dishId");
					String dishName = js.getString("dishName");
					double curPrice = js.getDouble("currentPrice");
					double oriPrice = js.getDouble("originalPrice");
					String pictureUrl = js.getString("pictureUrl");
					String introduction = js.getString("introduction");
					String timeToMarket = js.get("timeToMarket").toString(); // ����ʱ��
					int orderTimes = js.getInt("orderTimes"); // �������

					homeDishEntity.setTimeToMarket(timeToMarket);
					homeDishEntity.setOrderTimes(orderTimes);

					homeDishEntity.setDishName(dishName);
					homeDishEntity.setCurPrice(curPrice);
					homeDishEntity.setOriPrice(oriPrice);
					homeDishEntity.setIntroduction(introduction);
					homeDishEntity.setPicUrl(pictureUrl);

					listitemDrinkDish.add(homeDishEntity);
				}

			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return listitemDrinkDish;
		case 5: // ��ʳ
			if (iMainFoodRefreshType == PULL_TO_REFRESH_HEADER
					&& listitemmFoodDish.size() > 0) {
				listitemmFoodDish.clear();
			}
			try {

				for (int i = 0; i < jsArray.length(); i++) {
					homeDishEntity = new HomeDishEntity();
					JSONObject js = new JSONObject();
					js = jsArray.getJSONObject(i);
					// int dishId = js.getInt("dishId");
					String dishName = js.getString("dishName");
					double curPrice = js.getDouble("currentPrice");
					double oriPrice = js.getDouble("originalPrice");
					String pictureUrl = js.getString("pictureUrl");
					String introduction = js.getString("introduction");
					String timeToMarket = js.get("timeToMarket").toString(); // ����ʱ��
					int orderTimes = js.getInt("orderTimes"); // �������

					homeDishEntity.setTimeToMarket(timeToMarket);
					homeDishEntity.setOrderTimes(orderTimes);

					homeDishEntity.setDishName(dishName);
					homeDishEntity.setCurPrice(curPrice);
					homeDishEntity.setOriPrice(oriPrice);
					homeDishEntity.setIntroduction(introduction);
					homeDishEntity.setPicUrl(pictureUrl);

					listitemmFoodDish.add(homeDishEntity);
				}

			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return listitemmFoodDish;
		case 6:

			break;

		default:
			break;
		}

		return null;
	}

	private void setPullToViewComplete(int iListType) {
		switch (iListType) {
		case 1:
			mPullToRefreshView.onFooterRefreshComplete();
			break;
		case 2:
			mSaleToRefreshView.onFooterRefreshComplete();
			break;
		case 3:
			mSpecialToRefreshView.onFooterRefreshComplete();
			break;
		case 4:
			mDrinkToRefreshView.onFooterRefreshComplete();
			break;
		case 5:
			mMainFoodToRefreshView.onFooterRefreshComplete();
			break;
		case 6:
			mUnitToRefreshView.onFooterRefreshComplete();
			break;

		default:
			break;
		}
	}

	// �ı�ListView�Ŀɼ���
	private void changeListViewVisivility(View... args) {

		for (int i = 0; i < args.length; i++) {
			if (i == 0) {
				args[i].setVisibility(View.VISIBLE);
				MyAnimation.setTransAnimation(MyAnimation.PUSH_UP_IN, args[i],
						mContext);
			}
			else {
				if (args[i].isShown()) {
					MyAnimation.setTransAnimation(MyAnimation.PUSH_DOWN_OUT,
							args[i], mContext);
				}
				args[i].setVisibility(View.GONE);
			}
		}

		// args[1].setVisibility(View.GONE);
		// args[2].setVisibility(View.GONE);
		// args[3].setVisibility(View.GONE);
		// args[4].setVisibility(View.GONE);
		// args[5].setVisibility(View.GONE);
	}

	/*
	 * ����ϵͳ��Ϣ���߳�
	 */
	class ThreadSysMsgEvent extends Thread {
		private int iType;

		public ThreadSysMsgEvent(int iType) {
			this.iType = iType;
		}

		@Override
		public void run() {
			while (true) {
				try {

					if (getClickAdapter(iType).isClickPush) {
						if (!UserParam.isLogin) {
							mHandler.obtainMessage(START_ACTIVITY_lOGIN)
									.sendToTarget();// ������Ϣ��Handler
						}
						else if (UserParam.orderId.equals("")||UserParam.orderId==null) {
							mHandler.obtainMessage(START_ACTIVITY_TABLE)
									.sendToTarget();// ������Ϣ��Handle

						}
						else {
							mHandler.obtainMessage(PUSH_TO_ORDER_DETAILS)
									.sendToTarget();// ������Ϣ��Handler

						}

						getClickAdapter(iListType).isClickPush = false;
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

	// ��õ����ArrayList
	private ArrayList<HomeDishEntity> getClickList(int iListType) {
		switch (iListType) {
		case 1:
			return listitemDish;
		case 2:
			return listitemSaleDish;
		case 3:
			return listitemSpecialDish;

		case 4:
			return listitemDrinkDish;
		case 5:

			return listitemmFoodDish;
		case 6:
			return listitemmFoodDish;
		}
		return null;
	}

	// ��õ����ArrayList
	private HomeDishAdapter getClickAdapter(int iListType) {
		switch (iListType) {
		case 1:
			return homeDishAdapter;
		case 2:
			return homeSaleDishAdapter;
		case 3:
			// return home;
			//
		case 4:
			return homeDrinkDishAdapter;
		case 5:

			return homemFoodDishAdapter;
		case 6:
			// return listitemmFoodDish;
		}
		return null;
	}

	// ���������ϸ�˵��߳�
	private class ThreadPushToOrderDetails extends Thread {

		@Override
		public void run() {
			Log.d("ThreadPushToOrder", "ThreadPushToOrder");
			try {
				Thread.sleep(100);
				iNotificationProgress++;
				if (iNotificationProgress == 100)
					iNotificationProgress = 0;

				// �����������
				if (NetWorkUtil.checkNetworkInfo(mContext) == NetWorkUtil.DISCONNECTED) {
					mHandler.obtainMessage(NET_EXCEPTION).sendToTarget();// ������Ϣ��Handler
					return;
				}

System.out.println("ThreadPushToOrder|UserParam.orderId="+UserParam.orderId);
				String strGetResponse = pushToOrderDetails(
						UserParam.orderId,
						getClickList(iListType).get(
								getClickAdapter(iListType).position)
								.getDishName(),
						getClickList(iListType).get(
								getClickAdapter(iListType).position)
								.getCurPrice()
								+ "");

				// �������˵�

				if (strGetResponse.equals("success")) // ���з���Ա�ɹ�
				{
					mHandler.obtainMessage(PUSH_TO_ORDER_DETAILS_OK)
							.sendToTarget();// ������Ϣ��Handler
				}
				else if (strGetResponse.equals("fail")) {
					mHandler.obtainMessage(PUSH_TO_ORDER_DETAILS_FAILED)
							.sendToTarget();// ������Ϣ��Handler
				}
				else if (strGetResponse.equals("�������쳣")) {
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// ������Ϣ��Handler
				}

			}
			catch (Exception e) {
				mPushToOrderDetailsThread = null;
			}
			mPushToOrderDetailsThread = null;
		}
	}

	// ���߳�UI��Handler����
	Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case START_ACTIVITY_lOGIN:
				startActivity(new Intent(HomeActivity.this, LoginActivity.class));
				break;

			case START_ACTIVITY_TABLE:
				startActivity(new Intent(HomeActivity.this, TableActivity.class));
				break;
			case PUSH_TO_ORDER_DETAILS:

				dlg.show();

				Window window = dlg.getWindow();

				window.setContentView(R.layout.bg_progressbar_dialog);

				TextView tvProgressMsg = (TextView) window
						.findViewById(R.id.tv_progress_msg);
				tvProgressMsg.setText("����Ŭ������˵�");

				showNotification("�����˶���");
				// ����������ϸ�˵��߳�
				mPushToOrderDetailsThread = new ThreadPushToOrderDetails();
				mPushToOrderDetailsThread.start();
				break;

			case PUSH_TO_ORDER_DETAILS_OK: // ����˵��ɹ�
				iNotificationProgress = 100;
				UserParam.isInsertDish = true;
				controllersUtil.showToast(mContext, "��ӳɹ�", Toast.LENGTH_SHORT);
				break;

			case PUSH_TO_ORDER_DETAILS_FAILED: // ����˵�ʧ��
				controllersUtil.showToast(mContext, "���ʧ��", Toast.LENGTH_SHORT);
				break;

			case GET_FROM_NET_OK: // ����ɹ�

				String strJson = "";
				strJson = (String) (msg.obj);
				Log.d("mHandler:iListType", "" + iListType);
				
				ivBackTop.setVisibility(View.VISIBLE);
				switch (iListType) {

				case 1: // ȫ��
					changeListViewVisivility(mPullToRefreshView,
							mSaleToRefreshView, mSpecialToRefreshView,
							mMainFoodToRefreshView, mDrinkToRefreshView,
							mUnitToRefreshView);

					homeDishAdapter = new HomeDishAdapter(mContext,
							createListViewData(strJson, iListType), iListType);
					lvDish.setAdapter(homeDishAdapter);
					iLvDishCount = lvDish.getCount(); // ��ȡlistview����Ŀ��
					if (iLvDishCount == 25) {
						startIndex = endIndex;
						endIndex += EVERY_TIME_GET_LIST_COUNT;
					}

					if (iAllRefreshType == PULL_TO_REFRESH_FOOTER) {
						mPullToRefreshView.onFooterRefreshComplete();
						lvDish.setSelection(iLvDishCount
								- EVERY_TIME_GET_LIST_COUNT);
						startIndex = endIndex;
						endIndex += EVERY_TIME_GET_LIST_COUNT;

					}
					else if (iAllRefreshType == PULL_TO_REFRESH_HEADER) {
						mPullToRefreshView
								.onHeaderRefreshComplete(getString(R.string.pull_to_refresh_time_title)
										+ UtilModule.getCurTime(1));

						mPullToRefreshView.onFooterRefreshComplete();
						mediaPlayer.start(); // ��������
					}

					new Thread(new ThreadSysMsgEvent(iListType)).start(); // �������"���"��ť�߳�
					break;
				case 2: // �ؼ۲�

					homeSaleDishAdapter = new HomeDishAdapter(mContext,
							createListViewData(strJson, iListType), iListType);
					Log.d("mHandler:strJson", "" + strJson);
					lvSale.setAdapter(homeSaleDishAdapter);
					iLvSaleDishCount = lvSale.getCount(); // ��ȡlistview����Ŀ��
					Log.d("mHandler:iLvSaleDishCount", "" + iLvSaleDishCount);

					if (iLvSaleDishCount <= 25) {
						startIndexSale = endIndexSale;
						endIndexSale += EVERY_TIME_GET_LIST_COUNT;
					}
					Log.d("mHandler:iSaleRefreshType", "" + iSaleRefreshType);
					if (iSaleRefreshType == PULL_TO_REFRESH_FOOTER) {
						mSaleToRefreshView.onFooterRefreshComplete();

						lvSale.setSelection(iLvSaleDishCount
								- EVERY_TIME_GET_LIST_COUNT);
						startIndexSale = endIndexSale;
						endIndexSale += EVERY_TIME_GET_LIST_COUNT;

					}
					else if (iSaleRefreshType == PULL_TO_REFRESH_HEADER) {
						mSaleToRefreshView
								.onHeaderRefreshComplete(getString(R.string.pull_to_refresh_time_title)
										+ UtilModule.getCurTime(1));
						mediaPlayer.start(); // ��������
					}
					new Thread(new ThreadSysMsgEvent(iListType)).start(); // �������"���"��ť�߳�
					break;
				case 3:

					break;
				case 4: // ��ˮ

					homeDrinkDishAdapter = new HomeDishAdapter(mContext,
							createListViewData(strJson, iListType), iListType);
					Log.d("mHandler:��ˮ|strJson", "" + strJson);
					lvDrink.setAdapter(homeDrinkDishAdapter);
					iLvDrinkDishCount = lvDrink.getCount(); // ��ȡlistview����Ŀ��
					Log.d("mHandler:iLvDrinkDishCount", "" + iLvDrinkDishCount);

					if (iLvDrinkDishCount <= 25) {
						startIndexDrink = endIndexDrink;
						endIndexDrink += EVERY_TIME_GET_LIST_COUNT;
					}
					Log.d("mHandler:iDrinkRefreshType", "" + iDrinkRefreshType);
					if (iDrinkRefreshType == PULL_TO_REFRESH_FOOTER) {
						mDrinkToRefreshView.onFooterRefreshComplete();

						lvDrink.setSelection(iLvSaleDishCount
								- EVERY_TIME_GET_LIST_COUNT);

						startIndexDrink = endIndexDrink;
						endIndexDrink += EVERY_TIME_GET_LIST_COUNT;

					}
					else if (iDrinkRefreshType == PULL_TO_REFRESH_HEADER) {
						mDrinkToRefreshView
								.onHeaderRefreshComplete(getString(R.string.pull_to_refresh_time_title)
										+ UtilModule.getCurTime(1));
						mediaPlayer.start(); // ��������
					}
					new Thread(new ThreadSysMsgEvent(iListType)).start(); // �������"���"��ť�߳�
					break;
				case 5: // ��ʳ

					homemFoodDishAdapter = new HomeDishAdapter(mContext,
							createListViewData(strJson, iListType), iListType);
					Log.d("mHandler:��ʳ|strJson", "" + strJson);
					lvMainFood.setAdapter(homemFoodDishAdapter);
					iLvmFoodDishCount = lvMainFood.getCount(); // ��ȡlistview����Ŀ��
					Log.d("mHandler:iLvmFoodDishCount", "" + iLvmFoodDishCount);

					if (iLvmFoodDishCount <= 25) {
						startIndexmFood = endIndexDrink;
						endIndexmFood += EVERY_TIME_GET_LIST_COUNT;
					}

					if (iMainFoodRefreshType == PULL_TO_REFRESH_FOOTER) {
						mMainFoodToRefreshView.onFooterRefreshComplete();

						lvMainFood.setSelection(-EVERY_TIME_GET_LIST_COUNT);

						startIndexmFood = endIndexmFood;
						endIndexmFood += EVERY_TIME_GET_LIST_COUNT;

					}
					else if (iDrinkRefreshType == PULL_TO_REFRESH_HEADER) {
						mMainFoodToRefreshView
								.onHeaderRefreshComplete(getString(R.string.pull_to_refresh_time_title)
										+ UtilModule.getCurTime(1));
						mediaPlayer.start(); // ��������
					}
					new Thread(new ThreadSysMsgEvent(iListType)).start(); // �������"���"��ť�߳�
					break;
				case 6:

					break;

				default:
					break;
				}

				break;
			case NO_MORE_DISH:
				controllersUtil.showToast(mContext,
						getString(R.string.no_more_dish), Toast.LENGTH_SHORT);

				setPullToViewComplete(iListType);
				// mPullToRefreshView.onFooterRefreshComplete();
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
						getString(R.string.login_fail), Toast.LENGTH_SHORT);
				break;
			}

			// ��dialog���ɼ�
			if (dlg.isShowing())
				dlg.dismiss();

			super.handleMessage(msg);
		}

	};

	// ����popupwindow״̬
	public void changPopState(View parent, int iType) {

		if (iType == LEFT_SHOW) {
			initPWClaify(parent);
			ivClaArrow.setBackgroundResource(R.drawable.ic_deal_arrow_up);
		}
		else if (iType == RIGHT_SHOW) {
			initPWSort(parent);
			ivSortArrow.setBackgroundResource(R.drawable.ic_deal_arrow_up);
		}

	}

	// ��ʼ��PWClaify��ֵ
	private void initPWClaify(View parent) {

		if (pwClasify == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = layoutInflater.inflate(R.layout.popwindow, null);

			lvClasify = (ListView) view.findViewById(R.id.lv_pop); // չʾѡ���listview

			PopItemClasifyAdapter adapter = null;

			if (listitemLeft.size() > 0)
				adapter = new PopItemClasifyAdapter(mContext, listitemLeft);

			else
				adapter = new PopItemClasifyAdapter(mContext,
						CreateLeftData(getDishCount()));

			lvClasify.setAdapter(adapter); // ��listview����������

			lvClasify.setItemsCanFocus(false);
			lvClasify.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

			int lvPopHeight = 0;
			int lvItemCount = lvClasify.getCount();
			View listItem;

			// ��ȡlistview�ĸ߶�
			for (int j = 0; j < lvItemCount; j++) {
				listItem = adapter.getView(j, null, lvClasify);
				listItem.measure(0, 0); // ��������View �Ŀ��
				lvPopHeight += (listItem.getMeasuredHeight() + lvItemCount
						* (lvClasify.getDividerHeight()));
			}

			if (lvPopHeight >= setPopHeight()) // ���listview�ĸ߶ȴ��ڿհ�����ĸ߶�
			{
				pwClasify = new PopupWindow(view, setPopWidth(), setPopHeight());
			}
			else {
				pwClasify = new PopupWindow(view, setPopWidth(),
						LayoutParams.WRAP_CONTENT);
			}

			pwClasify.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.bg_category_item_normal));
			pwClasify.setFocusable(true);
			pwClasify.setOutsideTouchable(false);
		}

		// ����lvPopupwindow��Ӧ�¼�
		lvClasifyMenuEvent();
		pwClasify.update();
		pwClasify.showAsDropDown(parent);

		// ����popmenu�ر�
		pwClasify.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {

				ivClaArrow.setBackgroundResource(R.drawable.ic_deal_arrow_down);
				iClickType = NO_SHOW;
			}
		});
	}

	// ��ʼ��PWSort��ֵ
	private void initPWSort(View parent) {

		if (pwSort == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = layoutInflater.inflate(R.layout.popwindow, null);

			lvSort = (ListView) view.findViewById(R.id.lv_pop); // չʾѡ���listview

			PopItemSortAdapter adapter = null;

			if (listitemRight.size() > 0)
				adapter = new PopItemSortAdapter(mContext, listitemRight);

			else
				adapter = new PopItemSortAdapter(mContext, CreateRightData());

			lvSort.setAdapter(adapter); // ��listview����������

			lvSort.setItemsCanFocus(false);
			lvSort.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

			int lvPopHeight = 0;
			int lvItemCount = lvSort.getCount();
			View listItem;

			// ��ȡlistview�ĸ߶�
			for (int j = 0; j < lvItemCount; j++) {
				listItem = adapter.getView(j, null, lvSort);
				listItem.measure(0, 0); // ��������View �Ŀ��
				lvPopHeight += (listItem.getMeasuredHeight() + lvItemCount
						* (lvSort.getDividerHeight()));
			}

			if (lvPopHeight >= setPopHeight()) // ���listview�ĸ߶ȴ��ڿհ�����ĸ߶�
			{
				pwSort = new PopupWindow(view, setPopWidth(), setPopHeight());
			}
			else {
				pwSort = new PopupWindow(view, setPopWidth(),
						LayoutParams.WRAP_CONTENT);
			}

			pwSort.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.bg_category_item_normal));
			pwSort.setFocusable(true);
			pwSort.setOutsideTouchable(false);
		}

		// ����lvPopupwindow��Ӧ�¼�
		lvSortMenuEvent();
		pwSort.update();
		pwSort.showAsDropDown(parent);

		// ����popmenu�ر�
		pwSort.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				ivSortArrow
						.setBackgroundResource(R.drawable.ic_deal_arrow_down);
				iClickType = NO_SHOW;
			}
		});
	}

	// ���õ������ڵĿ��
	private int setPopWidth() {
		int width = (screenWidth) / 2;
		return width;
	}

	// ���õ������ڵĸ߶�
	private int setPopHeight() {
		int height = screenHeight
				- (llTopMenuBar.getHeight() + relTopBar.getHeight()
						+ MainActivity.radioGroup.getHeight() + getStatusBarHeight());
		return height;
	}

	// ��ArrayList��ֵ
	public ArrayList<PopItemClasifyEntity> CreateLeftData(String result) {

		String strItemContent = "";
		PopItemClasifyEntity mapItem;

		try {
			JSONArray jsArray = new JSONArray(result);

			// ��Ʒ����ѡ��
			for (int i = 0; i < lv_classify_item_content.length; i++) {
				strItemContent = lv_classify_item_content[i];

				int dishCount = jsArray.getInt(i);

				mapItem = new PopItemClasifyEntity(strItemContent, dishCount+ "");

				Object object = null;
				object = mapItem;
				mapItem = null;
				listitemLeft.add((PopItemClasifyEntity) object);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return listitemLeft;
	}

	// ��ArrayList��ֵ
	public ArrayList<String> CreateRightData() {

		String strItemContent = "";
		String mapItem = null;

		// ��Ʒ����
		for (int i = 0; i < lv_sort_item_content.length; i++) {
			strItemContent = lv_sort_item_content[i];
			mapItem = strItemContent;

			Object object = null;
			object = mapItem;
			mapItem = null;
			listitemRight.add((String) object);
		}

		return listitemRight;
	}

	// ��ʼ����Ļ�����Ϣ
	private void initWindowParams(Activity context) {

		if (screenWidth == 0 || screenHeight == 0 || statusBarHeight == 0) {
			DisplayMetrics dm = new DisplayMetrics();
			context.getWindowManager().getDefaultDisplay().getMetrics(dm);
			screenHeight = dm.heightPixels;
			screenWidth = dm.widthPixels;
		}
	}

	// ���״̬���ĸ߶�
	private int getStatusBarHeight() {
		Rect frame = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		return statusBarHeight = frame.top;
	}

	private void showNotification(String text) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(
				R.drawable.queue_icon_send, text, System.currentTimeMillis());
		notification.flags = Notification.FLAG_AUTO_CANCEL;// Notification.FLAG_AUTO_CANCEL;

		Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);

		// PendingIntent
		PendingIntent contentIntent = PendingIntent.getActivity(
				HomeActivity.this, R.string.app_name, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// notification.
		// notification.setLatestEventInfo(HomeActivity.this, text,
		// "Hello,there,I'm john.", contentIntent);
		RemoteViews rv = new RemoteViews(mContext.getPackageName(),
				R.drawable.notification_view);
		// rv.setImageViewResource(R.id.image, R.drawable.chat);
		rv.setTextViewText(R.id.notification_title, "����Ŭ������˵�");
		rv.setTextViewText(R.id.notification_percent, "%"
				+ iNotificationProgress);
		rv.setProgressBar(R.id.notificationProgress, 100,
				iNotificationProgress, false);
		notification.contentView = rv;
		notification.contentIntent = contentIntent;

		notificationManager.notify(R.string.app_name, notification);
		iNotificationProgress = 0;
	}

	// �õ�����ĸ���
	public String getDishCount() {
		String url = HttpUtil.BASE_URL + "GetDishCountServlet?";

		// ��ѯ���ؽ��
		return HttpUtil.queryStringForPost(url);
	}

	private String getListDishes(int startIndex, int endIndex) {
		Log.i("from_index", "" + startIndex);
		Log.i("end_index","" + endIndex);
		String queryString = "start_index=" + String.valueOf(startIndex)
				+ "&end_index=" + String.valueOf(endIndex);
		// url
		String url = "";
		System.out.println("getListDishes=" + iListType);
		switch (iListType) {
		case 1: // ȫ��
			url = HttpUtil.BASE_URL + "ListAllDishesServlet?"
					+ queryString;

			break;
		case 2: // �ؼ۲�
			url = HttpUtil.BASE_URL + "ListAllSaleDishesServlet?"
					+ queryString;

			break;
		case 3:

			break;
		case 4: // ��ˮ
			url = HttpUtil.BASE_URL + "ListAllDrinkDishesServlet?"
					+ queryString;
			break;
		case 5: // ��ʳ
			url = HttpUtil.BASE_URL + "ListAllMFoodDishesServlet?"
					+ queryString;
			break;
		case 6:

			break;

		default:
			break;
		}

		// ��ѯ���ؽ��
		return HttpUtil.queryStringForPost(url);
	}

	// �������˵�
	private String pushToOrderDetails(String orderId,String dishName,
			String dishPrice) {
		// ��ѯ����
		String queryString = "order_id=" + orderId
				+ "&dish_name=" + dishName + "&dish_price=" + dishPrice;
		// url
		String url = HttpUtil.BASE_URL + "PushToOrderDetailsServlet?"
				+ queryString;
		// ��ѯ���ؽ��
		return HttpUtil.queryStringForPost(url);
	}

	// �ײ�����ˢ��
	@Override
	public void onFooterRefresh(PullToRefreshView view) {
		switch (iListType) {
		case 1:
			iAllRefreshType = PULL_TO_REFRESH_FOOTER;
			// ����ˢ���б��߳�
			mGetListAllDishThread = new ThreadGetListDish(1);
			mGetListAllDishThread.start();
			break;

		case 2:
			iSaleRefreshType = PULL_TO_REFRESH_FOOTER;
			mGetListSaleDishThread = new ThreadGetListDish(2);
			mGetListSaleDishThread.start();
			break;

		case 3:
			break;

		case 4:// ��ˮ

			iDrinkRefreshType = PULL_TO_REFRESH_FOOTER;
			mGetListDrinkDishThread = new ThreadGetListDish(4);
			mGetListDrinkDishThread.start();
			break;
		case 5:
			iMainFoodRefreshType = PULL_TO_REFRESH_FOOTER;
			mGetListmFoodDishThread = new ThreadGetListDish(5);
			mGetListmFoodDishThread.start();
			break;
		case 6:
			break;
		default:
			break;
		}
	}

	// ��������ˢ��
	@Override
	public void onHeaderRefresh(PullToRefreshView view) {

		switch (iListType) {

		case 1: // ȫ��
			iAllRefreshType = PULL_TO_REFRESH_HEADER;
			// ����ˢ���б��߳�
			mGetListAllDishThread = new ThreadGetListDish(1);
			mGetListAllDishThread.start();
			break;

		case 2: // �ؼ۲�
			iSaleRefreshType = PULL_TO_REFRESH_HEADER;
			mGetListSaleDishThread = new ThreadGetListDish(2);
			mGetListSaleDishThread.start();
			break;

		case 3:
			break;

		case 4:// ��ˮ
			iDrinkRefreshType = PULL_TO_REFRESH_HEADER;
			mGetListDrinkDishThread = new ThreadGetListDish(4);
			mGetListDrinkDishThread.start();
			break;

		case 5: // ��ʳ
			iMainFoodRefreshType = PULL_TO_REFRESH_HEADER;
			mGetListmFoodDishThread = new ThreadGetListDish(5);
			mGetListmFoodDishThread.start();
			break;

		case 6:
			break;
		default:
			break;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		System.out.println("onDestroy");
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		System.out.println("onPause");
		super.onPause();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		System.out.println("onRestart");
		super.onRestart();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		System.out.println("onStart");
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		System.out.println("onStop");
		super.onStop();
	}

}
