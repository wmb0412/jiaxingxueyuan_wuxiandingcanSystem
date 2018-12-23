package com.brazuca.ui;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;

import com.brazuca.db.BrazucaDBGetInfoUtil;
import com.brazuca.entity.UserParam;

public class MainActivity extends TabActivity implements
		OnCheckedChangeListener {
	private static Context mainContext;
	private TabHost mTabHost;
	public static RadioGroup radioGroup;
	private static int iLastChenkId;
	RadioButton tb1;
	RadioButton tb2;
	RadioButton tb3;
	RadioButton tb4;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		mainContext = getApplicationContext();

		// ʵ����TabHost
		mTabHost = this.getTabHost();
		// ���ѡ�
		mTabHost.addTab(mTabHost.newTabSpec("ONE").setIndicator("ONE")
				.setContent(new Intent(this, HomeActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TWO").setIndicator("TWO")
				.setContent(new Intent(this, SaleActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("THREE").setIndicator("THREE")
				.setContent(new Intent(this, OrderActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("FOUR").setIndicator("FOUR")
				.setContent(new Intent(this, MoreActivity.class)));

		radioGroup = (RadioGroup) findViewById(R.id.main_radio);
		// ע���¼�
		radioGroup.setOnCheckedChangeListener(this);

		// //��ʼ���ؼ�
		// initViews();
		// //���ü�����
		// setListner();

		tb1 = (RadioButton) findViewById(R.id.radio_button1);
		tb2 = (RadioButton) findViewById(R.id.radio_button2);
		tb3 = (RadioButton) findViewById(R.id.radio_button3);
		tb4 = (RadioButton) findViewById(R.id.radio_button4);
	}

	@Override
	protected void onResume() {
		if (LoginActivity.reBack) {
			
//			tb3.setChecked(false);
			switch (iLastChenkId) {
			case 1:
				tb1.setChecked(true);
				break;

			case 2:
				tb2.setChecked(true);
				break;

			case 4:
				tb4.setChecked(true);
				break;
			}

		}
		super.onResume();
	}

	@Override
	public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

		switch (checkedId) {
		case R.id.radio_button1:
			mTabHost.setCurrentTabByTag("ONE");
			radioGroup.check(R.id.radio_button1);
			iLastChenkId = 1;
			
			// �����ݿ��ȡ�ǳ�
			if(!BrazucaDBGetInfoUtil.getDBUserId(mainContext).equals(""))
			{
				String userId = BrazucaDBGetInfoUtil.getDBUserId(mainContext);
				if(!userId.equals("") || userId!=null)
				{
					UserParam.isLogin = true;
					UserParam.userId = Integer.parseInt(userId);
				}			
			}


			System.out.println("UserParam.userId" + UserParam.userId);
			break;
		case R.id.radio_button2:
			mTabHost.setCurrentTabByTag("TWO");
			radioGroup.check(R.id.radio_button2);
			iLastChenkId = 2;
			break;
		case R.id.radio_button3:
			// ���δ��⵽�û���¼������ת����¼����
			if (!UserParam.isLogin) {
				startActivity(new Intent(MainActivity.this, LoginActivity.class));
			}
			else {
				mTabHost.setCurrentTabByTag("THREE");
				radioGroup.check(R.id.radio_button3);
				iLastChenkId = 3;
			}

			break;
		case R.id.radio_button4:
			mTabHost.setCurrentTabByTag("FOUR");
			radioGroup.check(R.id.radio_button4);
			iLastChenkId = 4;
			break;
		}

	}

}
