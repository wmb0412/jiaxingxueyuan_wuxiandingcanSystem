package com.brazuca.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brazuca.entity.OrderEntity;
import com.brazuca.ui.R;

public class OrderDetailsAdapter extends BaseAdapter{
	private ArrayList<OrderEntity> aListEntity;
	private Context context;
	LayoutInflater mInflater;
	public boolean isClickCount = false;
	public int position;
	
	public OrderDetailsAdapter(Context context ,ArrayList<OrderEntity> aListEntity) {
		this.context = context;
		this.aListEntity = aListEntity;	
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public boolean areAllItemsEnabled() {
		return true;
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
	
	@Override
	public void notifyDataSetChanged() {

		super.notifyDataSetChanged();
	}
	
	
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.order_details_listview_item, null);
			holder = new ViewHolder();
			
			holder.tvDishName = (TextView) convertView.findViewById(R.id.tv_order_dishname);
			holder.tvSinglePrice = (TextView) convertView.findViewById(R.id.tv_order_single_price);
			holder.tvCount = (TextView) convertView.findViewById(R.id.tv_order_count);
			holder.tvSingleTotalPrice = (TextView) convertView.findViewById(R.id.tv_order_single_total_price);		
			holder.relCount =(RelativeLayout) convertView.findViewById(R.id.ll_count);		
			
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();	
		 
		//给item的每项内容设置值
		holder.tvDishName.setText(position+1+"、"+aListEntity.get(position).getDishName());  //名字
		holder.tvSinglePrice.setText(String.valueOf(aListEntity.get(position).getPaySinglePrice())+"元 ");  //单品单价
		holder.tvCount.setText(String.valueOf(aListEntity.get(position).getCount())+" 份");  //单品份数
		holder.tvSingleTotalPrice.setText(String.valueOf(aListEntity.get(position).getPaySinglePrice()*
				aListEntity.get(position).getCount())+"元");
		
		//选择份数
		holder.relCount.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				isClickCount = true;
				OrderDetailsAdapter.this.position = position;
			}
		});
		
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
	    TextView tvDishName;
	    TextView tvSinglePrice;
	    TextView tvCount;
	    RelativeLayout relCount;
	    TextView tvSingleTotalPrice;
//	    WheelView wlCount;
	 }
}


