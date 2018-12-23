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
	protected final  int PUSH_TO_ORDER_OK = 0x001;// ��Ϣ������˵��ɹ�
	protected final  int NET_EXCEPTION = 0x002;// ��Ϣ�������쳣
	protected final  int PUSH_TO_ORDER_FAILED = 0x003;// ��Ϣ������˵�ʧ��
	protected final  int SERVER_EXCEPTION = 0x004;// ��Ϣ:�������쳣
	
	private Context mContext;
	private ImageLoader imageLoader;
	private ImageView ivDish;
	private TextView tvDishDetailPushToOrder; // ����˵�
	private RelativeLayout relOriPrice;  //ԭ��
	private TextView tvDishCurPrice; // ��ǰ�۸�
	private TextView tvDishOriPrice; // �ּۼ۸�
	private TextView tvDishDetailsName; // ʳƷ����
	private TextView tvDishDetailsIntroduction; // ʳƷ����
	private TextView tvDishDetailsOrderedTimes; // �������
	private String picUrl = "";
	private String dishName;  //��Ʒ����
	private String dishCurPrice;  //��Ʒ�ּ�
	private String dishOriPrice;  //��Ʒԭ��
	private String orderTimes;  //�������
	
	
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

		// ��ȡHomeActivity������ֵ��������Ӧ�ؼ���������
		Bundle bundle = new Bundle();
		bundle = getIntent().getExtras();
		
		if (bundle != null) {
			dishName = bundle.getString("dish_name");  //��Ʒ����
			dishCurPrice = bundle.getString("dish_cur_price");  //��Ʒԭ��
			dishOriPrice = bundle.getString("dish_ori_price");
			picUrl = bundle.getString("dish_pic_url");
			
			orderTimes = bundle.getString("order_times");  //�������
			
			tvDishCurPrice.setText("��" + dishCurPrice);
			if(Double.parseDouble(dishCurPrice) < Double.parseDouble(dishOriPrice)) //�ּ�С��ԭ��
			{
				relOriPrice.setVisibility(View.VISIBLE);
				tvDishOriPrice.setText("��" +dishOriPrice);
			}
			
			tvDishDetailsName.setText(dishName);
			tvDishDetailsIntroduction.setText(bundle
					.getString("dish_introduction"));
			
			if(Integer.parseInt(orderTimes)==0)
				tvDishDetailsOrderedTimes.setText("������ʳ�͵�� "+orderTimes+" �Σ�" +
						"������Ϊ���ǵ��һ��Ʒ������ζ��ʳ��");  //�������
			else
				tvDishDetailsOrderedTimes.setText("������ʳ�͵�� "+orderTimes+" ��");  //�������
		}

		setViews();
	}

	// ��ʼ���ؼ�
	private void initViews() {
		ivDish = (ImageView) findViewById(R.id.iv_dish_details_img);

		tvDishDetailPushToOrder = (TextView) findViewById(R.id.tv_detail_push_to_order); // ����˵�
		relOriPrice = (RelativeLayout)this.findViewById(R.id.rel_ori_price);  //ԭ��
		tvDishCurPrice = (TextView) findViewById(R.id.tv_dish_cur_price); // ��ǰ�۸�
		tvDishOriPrice	= (TextView) findViewById(R.id.tv_dish_ori_price);  //ԭ��
		
		tvDishDetailsName = (TextView) findViewById(R.id.tv_dish_details_name); // ʳƷ����
		tvDishDetailsIntroduction = (TextView) findViewById(R.id.tv_dish_details_introduction); // ʳƷ����
		tvDishDetailsOrderedTimes = (TextView) findViewById(R.id.tv_dish_details_ordered_times); // �������
	}
	
	

	// ���ÿؼ�������
	private void setViews() {
		imageLoader.DisplayImage(HttpUtil.BASE_URL + picUrl, ivDish); // ����ʳƷ��ͼƬ

		// ����˵�����¼�
		tvDishDetailPushToOrder.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					
					if(!UserParam.isLogin)  //δ��¼����ת����¼����
					{
						startActivity(new Intent(DishDetailsActivity.this, LoginActivity.class));
					}
					else if(UserParam.orderId.equals("")||UserParam.orderId==null)
					{
						startActivity(new Intent(DishDetailsActivity.this, TableActivity.class));
					}
					else
					{
						//�����������Ų˵��߳�
						mPushToOrderDetailsThread = new ThreadPushToOrderDetails();
						mPushToOrderDetailsThread.start();
					}
				}
				return false;
			}
		});
	}

	// �������˵��߳�
	private class ThreadPushToOrderDetails extends Thread {

		@Override
		public void run() {
			Log.d("ThreadPushToOrder", "ThreadPushToOrder");
			try {

				// �����������
				if (NetWorkUtil.checkNetworkInfo(mContext) == NetWorkUtil.DISCONNECTED) {
					mHandler.obtainMessage(NET_EXCEPTION).sendToTarget();// ������Ϣ��Handler
					return;
				}

				String strGetResponse = pushToOrderDetails
				(UserParam.orderId, dishName,dishCurPrice);  //�������˵�

				if (strGetResponse.equals("success")) // ���з���Ա�ɹ�
				{
					mHandler.obtainMessage(PUSH_TO_ORDER_OK).sendToTarget();// ������Ϣ��Handler
				}
				else if (strGetResponse.equals("fail")) {
					mHandler.obtainMessage(PUSH_TO_ORDER_FAILED).sendToTarget();// ������Ϣ��Handler
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
	
	//���߳�UI��Handler����
	Handler	mHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
			case PUSH_TO_ORDER_OK:   //����˵��ɹ�
				UserParam.isInsertDish = true;
				controllersUtil.showToast(mContext, "��ӳɹ�", Toast.LENGTH_SHORT);
			
				break;

			case NET_EXCEPTION:  //�����쳣
				
				controllersUtil.showToast(mContext, getString(R.string.network_exception), Toast.LENGTH_SHORT);
				//startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));//���������������ý���
				break; 
			case SERVER_EXCEPTION:   //�������쳣
				
				controllersUtil.showToast(mContext, getString(R.string.server_exception), Toast.LENGTH_SHORT);
				break;
			case PUSH_TO_ORDER_FAILED:   //����˵�ʧ��
				
				controllersUtil.showToast(mContext,"���ʧ��", Toast.LENGTH_SHORT);
				break;
			}
			 super.handleMessage(msg);  
		}
		
	};

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
}
