package com.brazuca.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.brazuca.entity.RerserveTableEntity;
import com.brazuca.ui.R;

public class UnfinishedOrderAdapter extends BaseAdapter {
	private ArrayList<RerserveTableEntity> aListEntity;
	private Context context;
	LayoutInflater mInflater;
	public boolean isClickCount = false;
	public int position;

	public UnfinishedOrderAdapter(Context context,
			ArrayList<RerserveTableEntity> aListEntity) {
		this.context = context;
		this.aListEntity = aListEntity;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public boolean areAllItemsEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
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
			convertView = LayoutInflater.from(context).inflate(
					R.layout.unfinished_order_listview_item, null);
			holder = new ViewHolder();

			holder.tvTableId = (TextView) convertView
					.findViewById(R.id.tv_table_id);
			holder.tvUseTime = (TextView) convertView
					.findViewById(R.id.tv_use_time);
			holder.tvTypeName = (TextView) convertView
					.findViewById(R.id.tv_type_name);
			holder.tvIndex = (TextView)convertView.findViewById(R.id.tv_index);

			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();

		// 给item的每项内容设置值
		holder.tvTableId.setText(aListEntity.get(position).getTableId()+"#");
		holder.tvUseTime.setText(aListEntity.get(position).getUseTime()+"");
		
		holder.tvIndex.setText(""+(position+1));

		switch (aListEntity.get(position).getType()) {

		case 1:
			holder.tvTypeName.setText("中饭"
					+ aListEntity.get(position).getHour() + "点");
			break;
		case 2:
			holder.tvTypeName.setText("下午饭"
					+ aListEntity.get(position).getHour() + "点");
			break;
		case 3:
			holder.tvTypeName.setText("早点晚饭"
					+ aListEntity.get(position).getHour() + "点");
			break;
		case 4:
			holder.tvTypeName.setText("晚点晚饭"
					+ aListEntity.get(position).getHour() + "点");
			break;

		default:
			break;
		}

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
		TextView tvUseTime;
		TextView tvTypeName;
		TextView tvIndex;
	}
}
