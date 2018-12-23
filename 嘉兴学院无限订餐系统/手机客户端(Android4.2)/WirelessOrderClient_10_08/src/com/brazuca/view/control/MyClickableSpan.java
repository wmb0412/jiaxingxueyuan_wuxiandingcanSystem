package com.brazuca.view.control;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

import com.brazuca.ui.R;
import com.brazuca.ui.RegisterActivity;
import com.brazuca.util.ControllersUtil;

public class MyClickableSpan extends ClickableSpan{
	private ControllersUtil controllersUtil ;
	private Context context;
	private PopupWindow pwCommon;
	private Button btnSure;
	
	public MyClickableSpan(Context context)
	{
		controllersUtil = new ControllersUtil();
		this.context = context;
		
	}
	@Override
	public void onClick(View widget) {
		// TODO Auto-generated method stub
		initPopupWindow(RegisterActivity.llRegisterParent);
	}
	
	// 初始化PWClaify的值
	private void initPopupWindow(View parent) {

		if (pwCommon == null) {
			LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = layoutInflater.inflate(R.drawable.pop_dialog_single_sure, null);
			btnSure = (Button)view.findViewById(R.id.btn_agree_sure);
					
			pwCommon = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

			//给popupwindow设置背景颜色:灰色背景
			ColorDrawable dw = new ColorDrawable(0x7DC0C0C0);
			pwCommon.setBackgroundDrawable(dw);
			
			pwCommon.setFocusable(true);
			pwCommon.setOutsideTouchable(false);
		}

		pwCommon.update();
		pwCommon.showAtLocation(parent, Gravity.CENTER, 0, 0);
		pwCommon.showAsDropDown(parent);
		pwCommon.setAnimationStyle(Animation.START_ON_FIRST_FRAME);
		
		btnSure.setOnClickListener(onClickListener);

		// 监听popmenu关闭
		pwCommon.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				
			}
		});
	}
	
	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			pwCommon.dismiss();
		}
	};

}
