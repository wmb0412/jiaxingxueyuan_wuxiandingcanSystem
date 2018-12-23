package com.brazuca.ui;

import com.brazuca.db.BrazucaDBGetInfoUtil;
import com.brazuca.entity.UserParam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

public class SplashActivity extends Activity {

	private long m_dwSplashTime = 3000;
	private boolean m_bPaused = false;
	private boolean m_bSplashActive = true;

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		Thread splashTimer = new Thread() {
			public void run() {
				try {
					// wait loop
					long ms = 0;
					while (m_bSplashActive && ms < m_dwSplashTime) {
						sleep(100);

						if (!m_bPaused)
							ms += 100;
					}
					
					//�ж����ݿ��ǳ��Ƿ����
					UserParam.nickname = BrazucaDBGetInfoUtil.getDBNickname(SplashActivity.this);
					if (!UserParam.nickname.equals("")) {
						UserParam.isLogin = true;  //�����û�Ϊ��¼״̬
						// Bundle bundle = new Bundle();
						// bundle.putString("nickname", strNickname);
						Intent intent = new Intent(SplashActivity.this,
								MainActivity.class);
						// intent.putExtras(bundle);
						startActivity(intent);
					}
					else {
						Intent intent = new Intent(SplashActivity.this,
								WelcomeActivity.class);
						startActivity(intent);
					}
					
					SplashActivity.this.finish();
				}
				catch (Exception ex) {
					Log.e("Splash", ex.getMessage());
				}
				finally {
					finish();
				}
			}
		};
		splashTimer.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		m_bPaused = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		m_bPaused = false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			m_bSplashActive = false;
			break;
		case KeyEvent.KEYCODE_BACK:
			/* �����˳����� */
			/* System.exit(0); */
			/* android.os.Process.killProcess(android.os.Process.myPid()); */
			android.os.Process.killProcess(android.os.Process.myPid());
			break;
		default:
			break;
		}
		return true;
	}

}
