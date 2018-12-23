package com.brazuca.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.brazuca.entity.PopItemClasifyEntity;
import com.brazuca.ui.R;

public class PopItemSortAdapter implements ListAdapter{
	private ArrayList<String> aListEntity;
	private Context context;
	LayoutInflater mInflater;
	PopItemClasifyEntity entity;
	
	public PopItemSortAdapter(Context context ,ArrayList<String> aListEntity) {
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
			convertView = LayoutInflater.from(context).inflate(R.layout.pw_sort_listview_item, null);
			holder = new ViewHolder();
			
			holder.title = (TextView) convertView.findViewById(R.id.tv_sort_title);
			holder.parent = (LinearLayout) convertView.findViewById(R.id.ll_pop_sort_parent);
			
		
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();	
		
		//给item的每项内容设置值
		holder.title.setText(aListEntity.get(position).toString());
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
		LinearLayout parent;
	    TextView title;
	 }
}


