package com.brazuca.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import com.brazuca.ui.adapter.NumericWheelAdapter;
import com.brazuca.ui.view.WheelView;
import com.brazuca.util.ControllersUtil;
import com.brazuca.view.listener.OnWheelChangedListener;
import com.brazuca.view.listener.OnWheelScrollListener;

public class SelectSingleCountActivity extends Activity {
	// protected final int GET_FROM_NET_OK = 0x001;// 消息:获取成功
	// protected final int NET_EXCEPTION = 0x002;// 消息:网络异常
	// protected final int GET_FAILED = 0x003;// 消息：获取失败
	// protected final int SERVER_EXCEPTION = 0x004;// 消息:服务器异常
	// protected final int RESERVE_TABLE_SUCCESS = 0x005;// 消息:服务器异常
	// protected final int RESERVE_TABLE_FAIL = 0x006;// 消息:服务器异常

	private Context mContext;
	private ControllersUtil controllersUtil;
	private WheelView wvSelectSingleCount;
	private TextView tvSubmitCount;
	private boolean countChanged = false;
	private boolean countScrolled = false;
	public static int curCount = 0;
	public static boolean isFromSelectCount = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_single_count);
		mContext = SelectSingleCountActivity.this;

		controllersUtil = new ControllersUtil();
		initViews();
		setViews();

		// 获取HomeActivity传来的值，并给相应控件设置属性
		Bundle bundle = new Bundle();
		bundle = getIntent().getExtras();

		if (bundle != null) {
			String singleCount = bundle.getString("single_count");
			wvSelectSingleCount
					.setCurrentItem(Integer.parseInt(singleCount) - 1); // 设置当前选中值
		}
		else
			wvSelectSingleCount.setCurrentItem(1); // 设置当前选中值

		setListner();
	}

	// 初始化控件
	private void initViews() {
		wvSelectSingleCount = (WheelView) findViewById(R.id.wl_select_single_count);
		tvSubmitCount = (TextView) findViewById(R.id.tv_submit_single_count);
	}

	// 设置控件监听器
	private void setViews() {
		wvSelectSingleCount.setAdapter(new NumericWheelAdapter(1, 6, "%02d"));
		wvSelectSingleCount.setLabel("份");
		wvSelectSingleCount.setCyclic(true);
	}

	private void setListner() {
		wvSelectSingleCount.addChangingListener(wheelListener);
		wvSelectSingleCount.addScrollingListener(scrollListener);

		tvSubmitCount.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				isFromSelectCount = true;
				SelectSingleCountActivity.this.finish();
			}
		});

	}

	OnWheelChangedListener wheelListener = new OnWheelChangedListener() {
		public void onChanged(WheelView wheel, int oldValue, int newValue) {
			if (!countScrolled) {
				countChanged = true;
				curCount = wvSelectSingleCount.getCurrentItem();
				countChanged = false;
			}
		}
	};

	OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
		public void onScrollingStarted(WheelView wheel) {
			countScrolled = true;
		}

		public void onScrollingFinished(WheelView wheel) {
			countScrolled = false;
			countChanged = true;
			curCount = wvSelectSingleCount.getCurrentItem();
			countChanged = false;
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:

			break;
		case KeyEvent.KEYCODE_BACK:
			// isFromSelectCount = true;
			// SelectSingleCountActivity.this.finish();
			break;
		default:
			break;
		}
		return true;
	}

}
