package com.brazuca.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brazuca.entity.UserParam;
import com.brazuca.network.HttpUtil;
import com.brazuca.network.NetWorkUtil;
import com.brazuca.ui.imageload.ImageLoader;
import com.brazuca.util.ControllersUtil;

public class DishDetailsActivity extends Activity{
	protected final  int PUSH_TO_ORDER_OK = 0x001;// 消息：加入菜单成功
	protected final  int NET_EXCEPTION = 0x002;// 消息：网络异常
	protected final  int PUSH_TO_ORDER_FAILED = 0x003;// 消息：加入菜单失败
	protected final  int SERVER_EXCEPTION = 0x004;// 消息:服务器异常
	
	private Context mContext;
	private ImageLoader imageLoader;
	private ImageView ivDish;
	private TextView tvDishDetailPushToOrder; // 加入菜单
	private RelativeLayout relOriPrice;  //原价
	private TextView tvDishCurPrice; // 当前价格
	private TextView tvDishOriPrice; // 现价价格
	private TextView tvDishDetailsName; // 食品名字
	private TextView tvDishDetailsIntroduction; // 食品介绍
	private TextView tvDishDetailsOrderedTimes; // 被点次数
	private String picUrl = "";
	private String dishName;  //菜品名字
	private String dishCurPrice;  //菜品现价
	private String dishOriPrice;  //菜品原价
	private String orderTimes;  //被点次数
	
	
	private ControllersUtil controllersUtil;
	public ThreadPushToOrderDetails mPushToOrderDetailsThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dish_details);
		mContext = DishDetailsActivity.this;
		controllersUtil = new ControllersUtil();
		imageLoader = new ImageLoader(mContext);
		initViews();

		// 获取HomeActivity传来的值，并给相应控件设置属性
		Bundle bundle = new Bundle();
		bundle = getIntent().getExtras();
		
		if (bundle != null) {
			dishName = bundle.getString("dish_name");  //菜品名字
			dishCurPrice = bundle.getString("dish_cur_price");  //菜品原价
			dishOriPrice = bundle.getString("dish_ori_price");
			picUrl = bundle.getString("dish_pic_url");
			
			orderTimes = bundle.getString("order_times");  //被点次数
			
			tvDishCurPrice.setText("￥" + dishCurPrice);
			if(Double.parseDouble(dishCurPrice) < Double.parseDouble(dishOriPrice)) //现价小于原价
			{
				relOriPrice.setVisibility(View.VISIBLE);
				tvDishOriPrice.setText("￥" +dishOriPrice);
			}
			
			tvDishDetailsName.setText(dishName);
			tvDishDetailsIntroduction.setText(bundle
					.getString("dish_introduction"));
			
			if(Integer.parseInt(orderTimes)==0)
				tvDishDetailsOrderedTimes.setText("被其它食客点过 "+orderTimes+" 次，" +
						"您将成为我们店第一个品尝此美味的食客");  //被点次数
			else
				tvDishDetailsOrderedTimes.setText("被其它食客点过 "+orderTimes+" 次");  //被点次数
		}

		setViews();
	}

	// 初始化控件
	private void initViews() {
		ivDish = (ImageView) findViewById(R.id.iv_dish_details_img);

		tvDishDetailPushToOrder = (TextView) findViewById(R.id.tv_detail_push_to_order); // 加入菜单
		relOriPrice = (RelativeLayout)this.findViewById(R.id.rel_ori_price);  //原价
		tvDishCurPrice = (TextView) findViewById(R.id.tv_dish_cur_price); // 当前价格
		tvDishOriPrice	= (TextView) findViewById(R.id.tv_dish_ori_price);  //原价
		
		tvDishDetailsName = (TextView) findViewById(R.id.tv_dish_details_name); // 食品名字
		tvDishDetailsIntroduction = (TextView) findViewById(R.id.tv_dish_details_introduction); // 食品介绍
		tvDishDetailsOrderedTimes = (TextView) findViewById(R.id.tv_dish_details_ordered_times); // 被点次数
	}
	
	

	// 设置控件的属性
	private void setViews() {
		imageLoader.DisplayImage(HttpUtil.BASE_URL + picUrl, ivDish); // 设置食品的图片

		// 加入菜单点击事件
		tvDishDetailPushToOrder.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					
					if(!UserParam.isLogin)  //未登录则跳转到登录界面
					{
						startActivity(new Intent(DishDetailsActivity.this, LoginActivity.class));
					}
					else if(UserParam.orderId.equals("")||UserParam.orderId==null)
					{
						startActivity(new Intent(DishDetailsActivity.this, TableActivity.class));
					}
					else
					{
						//启动请求加入才菜单线程
						mPushToOrderDetailsThread = new ThreadPushToOrderDetails();
						mPushToOrderDetailsThread.start();
					}
				}
				return false;
			}
		});
	}

	// 请求加入菜单线程
	private class ThreadPushToOrderDetails extends Thread {

		@Override
		public void run() {
			Log.d("ThreadPushToOrder", "ThreadPushToOrder");
			try {

				// 检测网络连接
				if (NetWorkUtil.checkNetworkInfo(mContext) == NetWorkUtil.DISCONNECTED) {
					mHandler.obtainMessage(NET_EXCEPTION).sendToTarget();// 发送消息到Handler
					return;
				}

				String strGetResponse = pushToOrderDetails
				(UserParam.orderId, dishName,dishCurPrice);  //请求加入菜单

				if (strGetResponse.equals("success")) // 呼叫服务员成功
				{
					mHandler.obtainMessage(PUSH_TO_ORDER_OK).sendToTarget();// 发送消息到Handler
				}
				else if (strGetResponse.equals("fail")) {
					mHandler.obtainMessage(PUSH_TO_ORDER_FAILED).sendToTarget();// 发送消息到Handler
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
	
	//主线程UI的Handler处理
	Handler	mHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
			case PUSH_TO_ORDER_OK:   //加入菜单成功
				UserParam.isInsertDish = true;
				controllersUtil.showToast(mContext, "添加成功", Toast.LENGTH_SHORT);
			
				break;

			case NET_EXCEPTION:  //网络异常
				
				controllersUtil.showToast(mContext, getString(R.string.network_exception), Toast.LENGTH_SHORT);
				//startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));//进入无线网络配置界面
				break; 
			case SERVER_EXCEPTION:   //服务器异常
				
				controllersUtil.showToast(mContext, getString(R.string.server_exception), Toast.LENGTH_SHORT);
				break;
			case PUSH_TO_ORDER_FAILED:   //加入菜单失败
				
				controllersUtil.showToast(mContext,"添加失败", Toast.LENGTH_SHORT);
				break;
			}
			 super.handleMessage(msg);  
		}
		
	};

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
}
