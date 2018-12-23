package com.brazuca.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brazuca.entity.TableEntity;
import com.brazuca.ui.R;

public class TableAdapter implements ListAdapter{
	private ArrayList<TableEntity> aListEntity;
	private Context context;
	LayoutInflater mInflater;
	
	public TableAdapter(Context context ,ArrayList<TableEntity> aListEntity) {
		this.context = context;
		this.aListEntity = aListEntity;	
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public boolean areAllItemsEnabled() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.ListAdapter#isEnabled(int)
	 * isEnabled的返回值为true时，listview的itemOnclick才能响应
	 */
	public boolean isEnabled(int arg0) {
		return true;
	}
	
	public int getCount() {
		return aListEntity.size();
	}
	
	public Object getItem(int position) {
		return aListEntity.get(position);
	}
	
	public long getItemId(int position) {
		return position;
	}
	
	public int getItemViewType(int position) {
		return position;
	}
	
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.table_gridview_item, null);
			holder = new ViewHolder();
			
			holder.relTableBackground = (RelativeLayout)convertView.findViewById(R.id.rel_table);
			holder.tvTableId = (TextView) convertView.findViewById(R.id.tv_table_id);
			holder.tvStatus = (TextView)convertView.findViewById(R.id.tv_table_status); 
			holder.ivInFlag = (ImageView)convertView.findViewById(R.id.iv_in_flag);
			 
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();	
		 
		//给item的每项内容设置值
		holder.tvTableId.setText(aListEntity.get(position).getTableId()+"桌");
		if(!aListEntity.get(position).isTableStatus())
		{
			holder.relTableBackground.setBackgroundResource(R.drawable.bg_table_in);
			holder.tvStatus .setText("没人");
			holder.ivInFlag.setBackgroundResource(0);
		}
		else
		{
			holder.relTableBackground.setBackgroundResource(R.drawable.bg_table_absent);
			holder.tvStatus .setText("有人");
			holder.ivInFlag.setBackgroundResource(R.drawable.ic_in_flag);
		}
		
		Log.d("convertView",convertView+"");
		return convertView;	
	}

	public int getViewTypeCount() {
		return aListEntity.size();
	}
	
	public boolean hasStableIds() {
		return false;
	}
	
	public boolean isEmpty() {
		return false;
	}
	
	public void registerDataSetObserver(DataSetObserver observer) {

	}
	
	public void unregisterDataSetObserver(DataSetObserver observer) {
	}
	

	static class ViewHolder {
	    TextView tvTableId;
	    TextView tvStatus;
	    ImageView ivInFlag;
	    RelativeLayout relTableBackground;
	 }
}


