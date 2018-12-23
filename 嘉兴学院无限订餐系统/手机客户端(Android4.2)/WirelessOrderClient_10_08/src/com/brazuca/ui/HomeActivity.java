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
	protected static final int EVERY_TIME_GET_LIST_COUNT = 25; // 每次获取的listview的条目为25个
	protected static final int PULL_TO_REFRESH_HEADER = 1; // 头部刷新
	protected static final int PULL_TO_REFRESH_FOOTER = 2; // 底部刷新

	protected static final int GET_FROM_NET_OK = 0x001;// 消息:注册成功
	protected static final int NET_EXCEPTION = 0x002;// 消息:网络异常
	protected static final int GET_FAILED = 0x003;// 消息：注册失败
	protected static final int SERVER_EXCEPTION = 0x004;// 消息:服务器异常
	protected static final int NO_MORE_DISH = 0x005; // 消息：没有菜品了
	protected static final int GET_DISH_COUNT_FROM_NET_OK = 0x006;
	protected final int PUSH_TO_ORDER_DETAILS_OK = 0x007;// 消息：加入详细菜单成功
	protected final int PUSH_TO_ORDER_DETAILS_FAILED = 0x008;// 消息：加入详细菜单失败
	protected final int PUSH_TO_ORDER_DETAILS = 0x009; // 消息：发送加入详细菜单命令
	protected final int START_ACTIVITY_lOGIN = 0x010; // 消息：打开登录界面
	protected final int START_ACTIVITY_TABLE = 0x011;// 消息：打开选择餐桌界面

	private Context mContext;
	private static final int NO_SHOW = 0;
	private static final int LEFT_SHOW = 1;
	private static final int RIGHT_SHOW = 2;

	private static int screenHeight = 0;// 屏幕高度(pixel)
	private static int screenWidth = 0;// 屏幕宽度(pixel)
	private static int statusBarHeight = 0;
	private PopupWindow pwClasify; // 菜品分类弹出菜单
	private PopupWindow pwSort; // 菜品排序
	private ListView lvClasify;
	private ListView lvSort;

	private AlertDialog dlg;
	private ImageView ivBackTop; // 让listview滚到到顶部按钮

	private PullToRefreshView mPullToRefreshView; // 下拉刷新视图
	private PullToRefreshView mSaleToRefreshView; // 下拉刷新视图
	private PullToRefreshView mSpecialToRefreshView; // 下拉刷新视图
	private PullToRefreshView mDrinkToRefreshView; // 下拉刷新视图
	private PullToRefreshView mMainFoodToRefreshView; // 下拉刷新视图
	private PullToRefreshView mUnitToRefreshView; // 下拉刷新视图

	private ListView lvDish; // 食品列表
	private ListView lvSale; // 特价列表
	private ListView lvDrink; // 酒水列表
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
	private TextView tvCheckTable;  //选桌
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

	private int iLvDishCount = 0; // 菜品listview的条目数
	private int iLvSaleDishCount = 0; // 特价listview的条目数
	private int iLvDrinkDishCount = 0;
	private int iLvmFoodDishCount = 0;

	private int iAllRefreshType = 1; // 刷新类型--1：头部刷新；2--底部刷新
	private int iSaleRefreshType = 1; // 刷新类型--1：头部刷新；2--底部刷新
	private int iSpecialRefreshType = 1; // 刷新类型--1：头部刷新；2--底部刷新
	private int iDrinkRefreshType = 1; // 刷新类型--1：头部刷新；2--底部刷新
	private int iMainFoodRefreshType = 1; // 刷新类型--1：头部刷新；2--底部刷新
	private int iUnitRefreshType = 1; // 刷新类型--1：头部刷新；2--底部刷新
	private int iListType = 1; // 列表类型：1--全部数据；2--特价菜；3--招牌菜；4--酒水；5--主食；6--套餐
	// private boolean isFirstRefreshAll = false; //是否说说第一次刷新全部列表
	private int iNotificationProgress = 0;

	private int[] iClassificationSingleTotalCount={};  //分类中每个类别的总数目

	private ControllersUtil controllersUtil;

	private ThreadGetListDish mGetListAllDishThread;
	private ThreadGetListDish mGetListSaleDishThread;
	private ThreadGetListDish mGetListDrinkDishThread;
	private ThreadGetListDish mGetListmFoodDishThread;

	private ThreadPushToOrderDetails mPushToOrderDetailsThread; // 插入详细菜单
	// public ThreadGetDishCount mGetDishCountThread; //获取菜品分类的个数

	// 弹出菜单的item 的内容
	private String[] lv_classify_item_content = { "全部", "特价菜", "招牌菜", "酒水",
			"主食", "套餐" };

	// 弹出菜单的item 的内容
	private String[] lv_sort_item_content = { "默认排序", "价格最高", "价格最低", "人气最高",
			"最新发布" };

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

		// 初始化mPullToRefreshView
		initPullView();

		initWindowParams(this);
		controllersUtil = new ControllersUtil();
		dlg = new AlertDialog.Builder(mContext).create();

		// 实例化ArrayList
		listitemLeft = new ArrayList<PopItemClasifyEntity>();
		listitemRight = new ArrayList<String>();
		listitemDish = new ArrayList<HomeDishEntity>();

		initListViews(); // 初始化 ListView控件
		initViews(); // 初始化控件

		setListViewListner(); // 给ListView设置监听事件
		setListner(); // 给控件设置监听事件

		setPullViewListner(); // 设置上拉和下拉刷新的监听事件

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

	// 初始化下拉列表
	private void initPullView() {
		mPullToRefreshView = (PullToRefreshView) findViewById(R.id.main_pull_refresh_view);// 下拉刷新视图
		mSaleToRefreshView = (PullToRefreshView) findViewById(R.id.sale_pull_refresh_view);// 下拉刷新视图
		mDrinkToRefreshView = (PullToRefreshView) findViewById(R.id.drink_pull_refresh_view);// 下拉刷新视图
		mSpecialToRefreshView = (PullToRefreshView) findViewById(R.id.special_pull_refresh_view);// 下拉刷新视图
		mMainFoodToRefreshView = (PullToRefreshView) findViewById(R.id.main_food_pull_refresh_view);// 下拉刷新视图
		mUnitToRefreshView = (PullToRefreshView) findViewById(R.id.unit_pull_refresh_view);// 下拉刷新视图
	}

	// 设置上拉和下拉刷新的监听事件
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

	// 初始化 ListView控件
	private void initListViews() {
		lvDish = (ListView) findViewById(R.id.lv_home);
		lvSale = (ListView) findViewById(R.id.lv_sale);
		lvDrink = (ListView) findViewById(R.id.lv_drink);

		lvSpecial = (ListView) findViewById(R.id.lv_special);
		lvMainFood = (ListView) findViewById(R.id.lv_main_food);
		lvUnit = (ListView) findViewById(R.id.lv_unit);
	}

	// 初始化 控件
	private void initViews() {
		ivBackTop = (ImageView) findViewById(R.id.iv_back_top);
		relTopBar = (RelativeLayout) findViewById(R.id.rel_topbar);

		llTopMenuBar = (LinearLayout) findViewById(R.id.ll_top_menu_bar);
		llClacify = (LinearLayout) findViewById(R.id.ll_clasify); // 分类
		llSort = (LinearLayout) findViewById(R.id.ll_sort); // 排序

		tvCheckTable = (TextView) this.findViewById(R.id.tv_check_table); //选桌

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
		// 通过用户登录以后，则设置抬头为用户昵称
		Bundle bundle = new Bundle();
		bundle = getIntent().getExtras();
		if (bundle != null) {
			tvTopBar.setText(bundle.getString("nickname"));
		}
		else
			tvTopBar.setText(getString(R.string.app_name));

	}

	// listview的item响应事件
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

		case R.id.lv_home: // 全部
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

		case R.id.lv_sale: // 特价菜
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
		case R.id.lv_drink: // 酒水
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
		case R.id.lv_main_food: // 主食
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
	 * 获得可见的Listview
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

	// 设置监听事件
	private void setListner() {
		//选桌
		tvCheckTable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// 如果未检测到用户登录，则跳转到登录界面
				if (!UserParam.isLogin) {
					startActivity(new Intent(HomeActivity.this, LoginActivity.class));
				}
				else
					startActivity(new Intent(HomeActivity.this, TableActivity.class));
				
			}
		});
		
		//监听点击事件
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

		// 菜品分类弹出菜单事件
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

		// 排序弹出菜单事件
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

	// 展示不同的ListView
	private void showListView(int iListType) {
//		System.out.println("showListView:" + iListType);
		
		switch (iListType) {

		case 1: // 全部
			if (listitemDish == null)
				listitemDish = new ArrayList<HomeDishEntity>();

			changeListViewVisivility(mPullToRefreshView, mSaleToRefreshView,
					mSpecialToRefreshView, mMainFoodToRefreshView,
					mDrinkToRefreshView, mUnitToRefreshView);
			
			if (listitemDish.size() < 1) {
				ivBackTop.setVisibility(View.GONE);
				// 启动线程获取特价菜列表
				mGetListAllDishThread = new ThreadGetListDish(1);
				mGetListSaleDishThread.start();
				
			}
			tvSortContent.setText(strDishAllSort);

			break;
		case 2: // 特价菜
			if (listitemSaleDish == null)
				listitemSaleDish = new ArrayList<HomeDishEntity>();

			changeListViewVisivility(mSaleToRefreshView, mPullToRefreshView,
					mSpecialToRefreshView, mMainFoodToRefreshView,
					mDrinkToRefreshView, mUnitToRefreshView);
			if (listitemSaleDish.size() < 1) {
				ivBackTop.setVisibility(View.GONE);
				// 启动线程获取特价菜列表
				mSaleToRefreshView.headerRefreshing();
			}
			tvSortContent.setText(strDishSaleSort);

			break;
		case 3:

			break;
		case 4: // 酒水
			if (listitemDrinkDish == null)
				listitemDrinkDish = new ArrayList<HomeDishEntity>();

			changeListViewVisivility(mDrinkToRefreshView, mSaleToRefreshView,
					mPullToRefreshView, mSpecialToRefreshView,
					mMainFoodToRefreshView, mUnitToRefreshView);
			if (listitemDrinkDish.size() < 1) {
				ivBackTop.setVisibility(View.GONE);
				// 启动线程获取特价菜列表

				mDrinkToRefreshView.headerRefreshing();
			}
			tvSortContent.setText(strDishDrinkSort);
			break;

		case 5: // 主食
			if (listitemmFoodDish == null)
				listitemmFoodDish = new ArrayList<HomeDishEntity>();

			changeListViewVisivility(mMainFoodToRefreshView,
					mSaleToRefreshView, mPullToRefreshView,
					mSpecialToRefreshView, mDrinkToRefreshView,
					mUnitToRefreshView);
			if (listitemmFoodDish.size() < 1) {
				ivBackTop.setVisibility(View.GONE);
				// 启动线程获取主食列表
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

	// lvClasify响应事件
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

	// 菜品分配排序
	private void sortClassification(int position) {
		System.out.println("sortClassification=" + iListType);
		switch (iListType) {
		/*
		 * 全部菜排序
		 */
		case 1:

			switch (position) {
			case 0: // 默认排序

				break;
			case 1: // 价格最高

				// 对价格进行降序排序
				Collections.sort(listitemDish, new DishPriceSortDesc());
				homeDishAdapter = new HomeDishAdapter(mContext, listitemDish,
						iListType);
				lvDish.setAdapter(homeDishAdapter);

				break;
			case 2:
				// 对价格进行升序排序
				Collections.sort(listitemDish, new DishPriceSortAsc());
				homeDishAdapter = new HomeDishAdapter(mContext, listitemDish,
						iListType);
				lvDish.setAdapter(homeDishAdapter);

				// showNotification(lv_sort_item_content[position]);
				break;
			case 3:

				break;
			case 4: // 最新发布

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
		 * 特价菜排序
		 */
		case 2:
			switch (position) {
			case 0: // 默认排序

				break;
			case 1: // 价格最高

				// 对价格进行降序排序
				Collections.sort(listitemSaleDish, new DishPriceSortDesc());
				homeSaleDishAdapter = new HomeDishAdapter(mContext,
						listitemSaleDish, iListType);
				lvSale.setAdapter(homeSaleDishAdapter);

				break;
			case 2:
				// 对价格进行升序排序
				Collections.sort(listitemSaleDish, new DishPriceSortAsc());
				homeSaleDishAdapter = new HomeDishAdapter(mContext,
						listitemSaleDish, iListType);
				lvSale.setAdapter(homeSaleDishAdapter);

				// showNotification(lv_sort_item_content[position]);
				break;
			case 3:

				break;
			case 4: // 最新发布
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
		 * 酒水排序
		 */
		case 4:
			switch (position) {
			case 0: // 默认排序

				break;
			case 1: // 价格最高

				// 对价格进行降序排序
				Collections.sort(listitemDrinkDish, new DishPriceSortDesc());
				homeDrinkDishAdapter = new HomeDishAdapter(mContext,
						listitemDrinkDish, iListType);
				lvDrink.setAdapter(homeDrinkDishAdapter);

				break;
			case 2:
				// 对价格进行升序排序
				Collections.sort(listitemDrinkDish, new DishPriceSortAsc());
				homeDrinkDishAdapter = new HomeDishAdapter(mContext,
						listitemDrinkDish, iListType);
				lvDrink.setAdapter(homeDrinkDishAdapter);

				break;
			case 3:

				break;
			case 4: // 最新发布
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
		 * 主食排序
		 */
		case 5:

			switch (position) {
			case 0: // 默认排序

				break;
			case 1: // 价格最高

				// 对价格进行降序排序
				Collections.sort(listitemmFoodDish, new DishPriceSortDesc());
				homemFoodDishAdapter = new HomeDishAdapter(mContext,
						listitemmFoodDish, iListType);
				lvMainFood.setAdapter(homemFoodDishAdapter);

				break;
			case 2:
				// 对价格进行升序排序
				Collections.sort(listitemmFoodDish, new DishPriceSortAsc());
				homemFoodDishAdapter = new HomeDishAdapter(mContext,
						listitemmFoodDish, iListType);
				lvMainFood.setAdapter(homemFoodDishAdapter);

				break;
			case 3:

				break;
			case 4: // 最新发布
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
	 * 记录每个listview上一次使用的排序方式
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

	// lvSort响应事件
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

				setLastSortLable(iListType, strLastSortLable); // 记录每个listview上一次使用的排序方式

				sortClassification(position); // 菜品分配排序

			}
		});
	}

	// 获取全部菜品线程
	private class ThreadGetListDish extends Thread {
		private int iType;

		public ThreadGetListDish(int iListType) // 线程构造函数
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
				case 1: // 全部列表

					if (iAllRefreshType == PULL_TO_REFRESH_HEADER)
						result = getListDishes(0, EVERY_TIME_GET_LIST_COUNT);

					else if (iAllRefreshType == PULL_TO_REFRESH_FOOTER)
						result = getListDishes(startIndex, endIndex);

					if (result.equals("fail")) // 请求失败
					{
						mHandler.obtainMessage(GET_FAILED).sendToTarget();// 发送消息到Handler
					}
					else if (result.equals("nomoredish")) // 没有菜品
					{
						mHandler.obtainMessage(NO_MORE_DISH).sendToTarget();// 发送消息到Handler
					}

					else if (result.equals("服务器异常")) // 服务器异常
					{
						mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// 发送消息到Handler
					}
					else {
						mHandler.obtainMessage(GET_FROM_NET_OK, result)
								.sendToTarget();// 发送消息到Handler
					}

					break;
				case 2: // 特价菜
					if (iSaleRefreshType == PULL_TO_REFRESH_HEADER) {
						result = getListDishes(0, EVERY_TIME_GET_LIST_COUNT);
					}

					else if (iSaleRefreshType == PULL_TO_REFRESH_FOOTER)
						result = getListDishes(startIndexSale, endIndexSale);

					if (result.equals("fail")) // 请求失败
					{
						mHandler.obtainMessage(GET_FAILED).sendToTarget();// 发送消息到Handler
					}
					else if (result.equals("nomoredish")) // 没有菜品
					{
						mHandler.obtainMessage(NO_MORE_DISH).sendToTarget();// 发送消息到Handler
					}

					else if (result.equals("服务器异常")) // 服务器异常
					{
						mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// 发送消息到Handler
					}
					else {
						mHandler.obtainMessage(GET_FROM_NET_OK, result)
								.sendToTarget();// 发送消息到Handler
					}
					break;
				case 3: // 招牌菜

					break;
				case 4: // 酒水
					if (iDrinkRefreshType == PULL_TO_REFRESH_HEADER) {
						result = getListDishes(0, EVERY_TIME_GET_LIST_COUNT);
					}

					else if (iDrinkRefreshType == PULL_TO_REFRESH_FOOTER)
						result = getListDishes(startIndexDrink, endIndexDrink);

					if (result.equals("fail")) // 请求失败
					{
						mHandler.obtainMessage(GET_FAILED).sendToTarget();// 发送消息到Handler
					}
					else if (result.equals("nomoredish")) // 没有菜品
					{
						mHandler.obtainMessage(NO_MORE_DISH).sendToTarget();// 发送消息到Handler
					}

					else if (result.equals("服务器异常")) // 服务器异常
					{
						mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// 发送消息到Handler
					}
					else {
						mHandler.obtainMessage(GET_FROM_NET_OK, result)
								.sendToTarget();// 发送消息到Handler
					}
					break;
				case 5: // 主食
					if (iMainFoodRefreshType == PULL_TO_REFRESH_HEADER) {
						result = getListDishes(0, EVERY_TIME_GET_LIST_COUNT);
					}

					else if (iMainFoodRefreshType == PULL_TO_REFRESH_FOOTER)
						result = getListDishes(startIndexmFood, endIndexmFood);

					if (result.equals("fail")) // 请求失败
					{
						mHandler.obtainMessage(GET_FAILED).sendToTarget();// 发送消息到Handler
					}
					else if (result.equals("nomoredish")) // 没有菜品
					{
						mHandler.obtainMessage(NO_MORE_DISH).sendToTarget();// 发送消息到Handler
					}

					else if (result.equals("服务器异常")) // 服务器异常
					{
						mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// 发送消息到Handler
					}
					else {
						mHandler.obtainMessage(GET_FROM_NET_OK, result)
								.sendToTarget();// 发送消息到Handler
					}
					break;
				case 6: // 套餐

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

	// 得到菜品分类的个数线程
	// private class ThreadGetDishCount extends Thread {
	// @Override
	// public void run() {
	// try {
	// String result = "";
	// System.out.println("ThreadGetDishCount");
	//
	// result = getDishCount();
	//
	// if (result.equals("fail")) // 请求失败
	// {
	// // mHandler.obtainMessage(GET_FAILED).sendToTarget();// 发送消息到Handler
	// }
	//
	// else if (result.equals("服务器异常")) // 服务器异常
	// {
	// // mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();//
	// 发送消息到Handler
	// }
	// else {
	// mHandler.obtainMessage(GET_DISH_COUNT_FROM_NET_OK, result)
	// .sendToTarget();// 发送消息到Handler
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
	 * 给listitemDish添加数据
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
		case 1: // 全部
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
					String timeToMarket = js.get("timeToMarket").toString(); // 上市时间
					int orderTimes = js.getInt("orderTimes"); // 被点次数

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
		case 2: // 特价菜
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
					String timeToMarket = js.get("timeToMarket").toString(); // 上市时间
					int orderTimes = js.getInt("orderTimes"); // 被点次数

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
		case 4: // 酒水
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
					String timeToMarket = js.get("timeToMarket").toString(); // 上市时间
					int orderTimes = js.getInt("orderTimes"); // 被点次数

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
		case 5: // 主食
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
					String timeToMarket = js.get("timeToMarket").toString(); // 上市时间
					int orderTimes = js.getInt("orderTimes"); // 被点次数

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

	// 改变ListView的可见性
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
	 * 监听系统消息的线程
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
									.sendToTarget();// 发送消息到Handler
						}
						else if (UserParam.orderId.equals("")||UserParam.orderId==null) {
							mHandler.obtainMessage(START_ACTIVITY_TABLE)
									.sendToTarget();// 发送消息到Handle

						}
						else {
							mHandler.obtainMessage(PUSH_TO_ORDER_DETAILS)
									.sendToTarget();// 发送消息到Handler

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

	// 获得点击的ArrayList
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

	// 获得点击的ArrayList
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

	// 请求加入详细菜单线程
	private class ThreadPushToOrderDetails extends Thread {

		@Override
		public void run() {
			Log.d("ThreadPushToOrder", "ThreadPushToOrder");
			try {
				Thread.sleep(100);
				iNotificationProgress++;
				if (iNotificationProgress == 100)
					iNotificationProgress = 0;

				// 检测网络连接
				if (NetWorkUtil.checkNetworkInfo(mContext) == NetWorkUtil.DISCONNECTED) {
					mHandler.obtainMessage(NET_EXCEPTION).sendToTarget();// 发送消息到Handler
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

				// 请求加入菜单

				if (strGetResponse.equals("success")) // 呼叫服务员成功
				{
					mHandler.obtainMessage(PUSH_TO_ORDER_DETAILS_OK)
							.sendToTarget();// 发送消息到Handler
				}
				else if (strGetResponse.equals("fail")) {
					mHandler.obtainMessage(PUSH_TO_ORDER_DETAILS_FAILED)
							.sendToTarget();// 发送消息到Handler
				}
				else if (strGetResponse.equals("服务器异常")) {
					mHandler.obtainMessage(SERVER_EXCEPTION).sendToTarget();// 发送消息到Handler
				}

			}
			catch (Exception e) {
				mPushToOrderDetailsThread = null;
			}
			mPushToOrderDetailsThread = null;
		}
	}

	// 主线程UI的Handler处理
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
				tvProgressMsg.setText("正在努力加入菜单");

				showNotification("加入点菜队列");
				// 启动插入详细菜单线程
				mPushToOrderDetailsThread = new ThreadPushToOrderDetails();
				mPushToOrderDetailsThread.start();
				break;

			case PUSH_TO_ORDER_DETAILS_OK: // 加入菜单成功
				iNotificationProgress = 100;
				UserParam.isInsertDish = true;
				controllersUtil.showToast(mContext, "添加成功", Toast.LENGTH_SHORT);
				break;

			case PUSH_TO_ORDER_DETAILS_FAILED: // 加入菜单失败
				controllersUtil.showToast(mContext, "添加失败", Toast.LENGTH_SHORT);
				break;

			case GET_FROM_NET_OK: // 请求成功

				String strJson = "";
				strJson = (String) (msg.obj);
				Log.d("mHandler:iListType", "" + iListType);
				
				ivBackTop.setVisibility(View.VISIBLE);
				switch (iListType) {

				case 1: // 全部
					changeListViewVisivility(mPullToRefreshView,
							mSaleToRefreshView, mSpecialToRefreshView,
							mMainFoodToRefreshView, mDrinkToRefreshView,
							mUnitToRefreshView);

					homeDishAdapter = new HomeDishAdapter(mContext,
							createListViewData(strJson, iListType), iListType);
					lvDish.setAdapter(homeDishAdapter);
					iLvDishCount = lvDish.getCount(); // 获取listview的条目数
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
						mediaPlayer.start(); // 播放声音
					}

					new Thread(new ThreadSysMsgEvent(iListType)).start(); // 启动点击"点菜"按钮线程
					break;
				case 2: // 特价菜

					homeSaleDishAdapter = new HomeDishAdapter(mContext,
							createListViewData(strJson, iListType), iListType);
					Log.d("mHandler:strJson", "" + strJson);
					lvSale.setAdapter(homeSaleDishAdapter);
					iLvSaleDishCount = lvSale.getCount(); // 获取listview的条目数
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
						mediaPlayer.start(); // 播放声音
					}
					new Thread(new ThreadSysMsgEvent(iListType)).start(); // 启动点击"点菜"按钮线程
					break;
				case 3:

					break;
				case 4: // 酒水

					homeDrinkDishAdapter = new HomeDishAdapter(mContext,
							createListViewData(strJson, iListType), iListType);
					Log.d("mHandler:酒水|strJson", "" + strJson);
					lvDrink.setAdapter(homeDrinkDishAdapter);
					iLvDrinkDishCount = lvDrink.getCount(); // 获取listview的条目数
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
						mediaPlayer.start(); // 播放声音
					}
					new Thread(new ThreadSysMsgEvent(iListType)).start(); // 启动点击"点菜"按钮线程
					break;
				case 5: // 主食

					homemFoodDishAdapter = new HomeDishAdapter(mContext,
							createListViewData(strJson, iListType), iListType);
					Log.d("mHandler:主食|strJson", "" + strJson);
					lvMainFood.setAdapter(homemFoodDishAdapter);
					iLvmFoodDishCount = lvMainFood.getCount(); // 获取listview的条目数
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
						mediaPlayer.start(); // 播放声音
					}
					new Thread(new ThreadSysMsgEvent(iListType)).start(); // 启动点击"点菜"按钮线程
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
						getString(R.string.login_fail), Toast.LENGTH_SHORT);
				break;
			}

			// 让dialog不可见
			if (dlg.isShowing())
				dlg.dismiss();

			super.handleMessage(msg);
		}

	};

	// 更改popupwindow状态
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

	// 初始化PWClaify的值
	private void initPWClaify(View parent) {

		if (pwClasify == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = layoutInflater.inflate(R.layout.popwindow, null);

			lvClasify = (ListView) view.findViewById(R.id.lv_pop); // 展示选择的listview

			PopItemClasifyAdapter adapter = null;

			if (listitemLeft.size() > 0)
				adapter = new PopItemClasifyAdapter(mContext, listitemLeft);

			else
				adapter = new PopItemClasifyAdapter(mContext,
						CreateLeftData(getDishCount()));

			lvClasify.setAdapter(adapter); // 给listview建立适配器

			lvClasify.setItemsCanFocus(false);
			lvClasify.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

			int lvPopHeight = 0;
			int lvItemCount = lvClasify.getCount();
			View listItem;

			// 获取listview的高度
			for (int j = 0; j < lvItemCount; j++) {
				listItem = adapter.getView(j, null, lvClasify);
				listItem.measure(0, 0); // 计算子项View 的宽高
				lvPopHeight += (listItem.getMeasuredHeight() + lvItemCount
						* (lvClasify.getDividerHeight()));
			}

			if (lvPopHeight >= setPopHeight()) // 如果listview的高度大于空白区域的高度
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

		// 调用lvPopupwindow响应事件
		lvClasifyMenuEvent();
		pwClasify.update();
		pwClasify.showAsDropDown(parent);

		// 监听popmenu关闭
		pwClasify.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {

				ivClaArrow.setBackgroundResource(R.drawable.ic_deal_arrow_down);
				iClickType = NO_SHOW;
			}
		});
	}

	// 初始化PWSort的值
	private void initPWSort(View parent) {

		if (pwSort == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = layoutInflater.inflate(R.layout.popwindow, null);

			lvSort = (ListView) view.findViewById(R.id.lv_pop); // 展示选择的listview

			PopItemSortAdapter adapter = null;

			if (listitemRight.size() > 0)
				adapter = new PopItemSortAdapter(mContext, listitemRight);

			else
				adapter = new PopItemSortAdapter(mContext, CreateRightData());

			lvSort.setAdapter(adapter); // 给listview建立适配器

			lvSort.setItemsCanFocus(false);
			lvSort.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

			int lvPopHeight = 0;
			int lvItemCount = lvSort.getCount();
			View listItem;

			// 获取listview的高度
			for (int j = 0; j < lvItemCount; j++) {
				listItem = adapter.getView(j, null, lvSort);
				listItem.measure(0, 0); // 计算子项View 的宽高
				lvPopHeight += (listItem.getMeasuredHeight() + lvItemCount
						* (lvSort.getDividerHeight()));
			}

			if (lvPopHeight >= setPopHeight()) // 如果listview的高度大于空白区域的高度
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

		// 调用lvPopupwindow响应事件
		lvSortMenuEvent();
		pwSort.update();
		pwSort.showAsDropDown(parent);

		// 监听popmenu关闭
		pwSort.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				ivSortArrow
						.setBackgroundResource(R.drawable.ic_deal_arrow_down);
				iClickType = NO_SHOW;
			}
		});
	}

	// 设置弹出窗口的宽度
	private int setPopWidth() {
		int width = (screenWidth) / 2;
		return width;
	}

	// 设置弹出窗口的高度
	private int setPopHeight() {
		int height = screenHeight
				- (llTopMenuBar.getHeight() + relTopBar.getHeight()
						+ MainActivity.radioGroup.getHeight() + getStatusBarHeight());
		return height;
	}

	// 给ArrayList赋值
	public ArrayList<PopItemClasifyEntity> CreateLeftData(String result) {

		String strItemContent = "";
		PopItemClasifyEntity mapItem;

		try {
			JSONArray jsArray = new JSONArray(result);

			// 菜品分类选择
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

	// 给ArrayList赋值
	public ArrayList<String> CreateRightData() {

		String strItemContent = "";
		String mapItem = null;

		// 菜品排序
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

	// 初始化屏幕相关信息
	private void initWindowParams(Activity context) {

		if (screenWidth == 0 || screenHeight == 0 || statusBarHeight == 0) {
			DisplayMetrics dm = new DisplayMetrics();
			context.getWindowManager().getDefaultDisplay().getMetrics(dm);
			screenHeight = dm.heightPixels;
			screenWidth = dm.widthPixels;
		}
	}

	// 获得状态栏的高度
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
		rv.setTextViewText(R.id.notification_title, "正在努力加入菜单");
		rv.setTextViewText(R.id.notification_percent, "%"
				+ iNotificationProgress);
		rv.setProgressBar(R.id.notificationProgress, 100,
				iNotificationProgress, false);
		notification.contentView = rv;
		notification.contentIntent = contentIntent;

		notificationManager.notify(R.string.app_name, notification);
		iNotificationProgress = 0;
	}

	// 得到分类的个数
	public String getDishCount() {
		String url = HttpUtil.BASE_URL + "GetDishCountServlet?";

		// 查询返回结果
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
		case 1: // 全部
			url = HttpUtil.BASE_URL + "ListAllDishesServlet?"
					+ queryString;

			break;
		case 2: // 特价菜
			url = HttpUtil.BASE_URL + "ListAllSaleDishesServlet?"
					+ queryString;

			break;
		case 3:

			break;
		case 4: // 酒水
			url = HttpUtil.BASE_URL + "ListAllDrinkDishesServlet?"
					+ queryString;
			break;
		case 5: // 主食
			url = HttpUtil.BASE_URL + "ListAllMFoodDishesServlet?"
					+ queryString;
			break;
		case 6:

			break;

		default:
			break;
		}

		// 查询返回结果
		return HttpUtil.queryStringForPost(url);
	}

	// 请求加入菜单
	private String pushToOrderDetails(String orderId,String dishName,
			String dishPrice) {
		// 查询参数
		String queryString = "order_id=" + orderId
				+ "&dish_name=" + dishName + "&dish_price=" + dishPrice;
		// url
		String url = HttpUtil.BASE_URL + "PushToOrderDetailsServlet?"
				+ queryString;
		// 查询返回结果
		return HttpUtil.queryStringForPost(url);
	}

	// 底部上拉刷新
	@Override
	public void onFooterRefresh(PullToRefreshView view) {
		switch (iListType) {
		case 1:
			iAllRefreshType = PULL_TO_REFRESH_FOOTER;
			// 启动刷新列表线程
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

		case 4:// 酒水

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

	// 顶部下拉刷新
	@Override
	public void onHeaderRefresh(PullToRefreshView view) {

		switch (iListType) {

		case 1: // 全部
			iAllRefreshType = PULL_TO_REFRESH_HEADER;
			// 启动刷新列表线程
			mGetListAllDishThread = new ThreadGetListDish(1);
			mGetListAllDishThread.start();
			break;

		case 2: // 特价菜
			iSaleRefreshType = PULL_TO_REFRESH_HEADER;
			mGetListSaleDishThread = new ThreadGetListDish(2);
			mGetListSaleDishThread.start();
			break;

		case 3:
			break;

		case 4:// 酒水
			iDrinkRefreshType = PULL_TO_REFRESH_HEADER;
			mGetListDrinkDishThread = new ThreadGetListDish(4);
			mGetListDrinkDishThread.start();
			break;

		case 5: // 主食
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
