package com.brazuca.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.brazuca.entity.HomeSaleActivityEntity;
import com.brazuca.entity.PopItemClasifyEntity;
import com.brazuca.ui.R;
import com.brazuca.util.ControllersUtil;

public class HomeSaleActivityAdapter implements ListAdapter{
	private ArrayList<HomeSaleActivityEntity> aListEntity;
	private Context context;
	LayoutInflater mInflater;
	PopItemClasifyEntity entity;
	ControllersUtil cUtil = new ControllersUtil();
	
	
	public HomeSaleActivityAdapter(Context context ,ArrayList<HomeSaleActivityEntity> aListEntity) {
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
			convertView = LayoutInflater.from(context).inflate(R.layout.sale_activity_listview_item, null);
			holder = new ViewHolder();
			
			holder.tvSaleContent = (TextView) convertView.findViewById(R.id.tv_sale_content);
			holder.tvStartTime = (TextView) convertView.findViewById(R.id.tv_start_time);
			holder.tvEndTime = (TextView) convertView.findViewById(R.id.tv_end_time);
			
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();	
		
		//给item的每项内容设置值
		holder.tvSaleContent.setText(position+1+"、"+aListEntity.get(position).getSaleContent());
		holder.tvStartTime.setText(aListEntity.get(position).getStartTime().toString().substring(0, 10));
		holder.tvEndTime.setText(aListEntity.get(position).getEndTime().toString().substring(0, 10));
		
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
	    TextView tvSaleContent;
	    TextView tvStartTime;
	    TextView tvEndTime;
	 }
}


