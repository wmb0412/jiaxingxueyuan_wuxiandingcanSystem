package com.brazuca.util;

import com.brazuca.ui.R;
import com.brazuca.ui.RegisterActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class ControllersUtil {
//	private static final int TOAST_DURATION_SHORT = Toast.LENGTH_SHORT;
//	private static final int TOAST_DURATION_LONG = Toast.LENGTH_LONG;
	
  //展示自定义Toast
	public void showToast(Context context,String strContent,int duration){
		
		View toastRoot = ((Activity) context).getLayoutInflater().inflate(R.drawable.bg_toast_one, null);
		Toast toast=new Toast(context);
		toast.setView(toastRoot);
		TextView tv=(TextView)toastRoot.findViewById(R.id.tvinfo);
		tv.setText(strContent);
		toast.setDuration(duration);
		toast.setGravity(Gravity.CENTER, 0, 30);
		toast.show();
	}
	
	//展示自定义Toast
	public void showToastTwo(Context context,String strContent,int duration){
		
		View toastRoot = ((Activity) context).getLayoutInflater().inflate(R.drawable.bg_toast_two, null);
		Toast toast=new Toast(context);
		toast.setView(toastRoot);
		TextView tv=(TextView)toastRoot.findViewById(R.id.tvinfo);
		tv.setText(strContent);
		toast.setDuration(duration);
		toast.setGravity(Gravity.CENTER, 0, 30);
		toast.show();
	}
	
	public void showProgressWindow(AlertDialog dlg,String strContent)
	{		
		dlg.show();

		Window window = dlg.getWindow();

		window.setContentView(R.layout.bg_progressbar_dialog);

		TextView tvProgressMsg = (TextView) window
				.findViewById(R.id.tv_progress_msg);
		tvProgressMsg.setText(strContent);
	}
	
	public void hideProgressWindow(AlertDialog dlg)
	{
		dlg.hide();
	}
	
	public void showConfirmBack(final AlertDialog dlg,String strContent,final Activity activity) {

		dlg.show();

		Window window = dlg.getWindow();

		window.setContentView(R.layout.bg_confirm_dialog);

		TextView tvConfrimMsg = (TextView) window
				.findViewById(R.id.tv_confrim_msg);
		tvConfrimMsg.setText(strContent);  //
		// 为确认按钮添加事件
		TextView ok = (TextView) window.findViewById(R.id.btn_ok);
		TextView cancel = (TextView) window.findViewById(R.id.btn_cancel);

		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				dlg.dismiss();
				activity.finish();
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
}
