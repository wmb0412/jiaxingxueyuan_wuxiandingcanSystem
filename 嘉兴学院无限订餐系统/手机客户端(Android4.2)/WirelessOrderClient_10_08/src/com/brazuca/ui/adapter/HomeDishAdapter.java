package com.brazuca.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brazuca.entity.HomeDishEntity;
import com.brazuca.entity.PopItemClasifyEntity;
import com.brazuca.network.HttpUtil;
import com.brazuca.ui.R;
import com.brazuca.ui.imageload.ImageLoader;
import com.brazuca.util.ControllersUtil;
import com.brazuca.util.UtilModule;

public class HomeDishAdapter implements ListAdapter{
	private ImageLoader imageLoader;
	private ArrayList<HomeDishEntity> aListEntity;
	private Context context;
	public int iType;
	public int position;
	public boolean isClickPush;
	LayoutInflater mInflater;
	PopItemClasifyEntity entity;
	ControllersUtil cUtil = new ControllersUtil();
	
	public HomeDishAdapter(Context context ,ArrayList<HomeDishEntity> aListEntity,int iType) {
		this.iType = iType;
		this.context = context;
		this.aListEntity = aListEntity;	
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    imageLoader = new ImageLoader(context);   //异步加载图片类
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
			convertView = LayoutInflater.from(context).inflate(R.layout.dish_listview_item, null);
			holder = new ViewHolder();
			
			holder.tvDishName = (TextView) convertView.findViewById(R.id.tv_dishname);
			holder.ivDish = (ImageView)convertView.findViewById(R.id.iv_dish);  //菜品图片
			holder.tvCurPrice = (TextView) convertView.findViewById(R.id.tv_cur_price);  //现价
			holder.ivFlag = (ImageView)convertView.findViewById(R.id.iv_flag);
			
//			if(iType == 2)
//			{
			holder.relOriPrice = (RelativeLayout)convertView.findViewById(R.id.rel_ori_price); 
			holder.tvOriPrice = (TextView) convertView.findViewById(R.id.tv_ori_price);  //原价
//			}
			holder.tvPush = (TextView) convertView.findViewById(R.id.tv_push_to_order);	  //加入菜单		
			 
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();	
		
		if(UtilModule.compareDateTime(aListEntity.get(position).getTimeToMarket(), "2012-05-05 00:00:00")>=0)
		{
			holder.ivFlag.setImageResource(R.drawable.ic_dish_new);
		}
		
		//给item的每项内容设置值
		holder.tvDishName.setText(aListEntity.get(position).getDishName());
		holder.tvCurPrice.setText("￥"+String.valueOf(aListEntity.get(position).getCurPrice()));
		if(aListEntity.get(position).getOriPrice()>aListEntity.get(position).getCurPrice())  //原价大于现价
		{
			holder.relOriPrice.setVisibility(View.VISIBLE);
			holder.tvOriPrice.setText("￥"+String.valueOf(aListEntity.get(position).getOriPrice()));  //原价
			holder.ivFlag.setImageResource(R.drawable.ic_dish_sale);
		}
		
		
		
		//根据图片URL去查找内存缓存有没有对应的Bitmap对象，并传递回调方法，如果没有，则等下载完毕回调  
		//异步加载图片
		imageLoader.DisplayImage(HttpUtil.BASE_URL+aListEntity.get(position).getPicUrl(), holder.ivDish);
		
		//加入菜单响应事件
		holder.tvPush.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				HomeDishAdapter.this.position = position;  //给position赋值
				isClickPush = true;
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
	    TextView tvCurPrice;
	    TextView tvOriPrice;
	    RelativeLayout relOriPrice;
	    TextView tvPush;
	    ImageView ivDish;
	    ImageView ivFlag;
	    
//	    public ViewHolder(View baseView)
//	    {
//	    	this.baseView = baseView;
//	    }
//
//		public TextView getTvDishName() {
//			tvDishName = (TextView)baseView.findViewById(R.id.tv_dishname);
//			return tvDishName;
//		}
//		
//		public TextView getTvPrice() {
//			if(tvPrice==null)
//				tvPrice = (TextView)baseView.findViewById(R.id.tv_price);
//			
//			return tvPrice;
//		}
//
//		public TextView getTvPush() {
//			if(tvPush == null)
//				tvPush = (TextView)(baseView).findViewById(R.id.tv_push_to_order);
//			return tvPush;
//		}
//
//
//		public ImageView getIvDish() {
//			if (ivDish == null) {
//				ivDish = (ImageView) baseView.findViewById(R.id.iv_dish);
//			}
//			return ivDish;
//		}
		
	 }
}


