package com.brazuca.view.control;

import com.brazuca.ui.R;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

public class PopDialog extends AlertDialog {
	private Context context;;

	public PopDialog(Context context) {
		super(context);
		this.context = context;
	}

	public PopDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.drawable.pop_dialog_single_sure);
	}

	@Override
	public void setTitle(CharSequence title) {
		// TODO Auto-generated method stub
		super.setTitle(title);
	}

	
	



}
