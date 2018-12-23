package com.brazuca.ui.animation;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.brazuca.ui.R;

public class MyAnimation extends View{
	
	public MyAnimation(Context context) {
		super(context);
	}
	
	public static final int CLEAR_ANIMATION= 0; //清除动画
	public static final int PUSH_DOWN_IN = 1; //从下往上出现
	public static final int PUSH_DOWN_OUT = 2; //从上往下消失
	
	public static final int PUSH_UP_IN = 16; //从上往下出现
	public static final int PUSH_UP_OUT = 17; //从上往下消失

	public static final int SHAKE_INFINITE= 4; //不停抖动
	public static final int ALPHA_IN= 5; //淡入
	public static final int ALPHA_OUT= 6; //淡出
	public static final int SHAKE_X= 7; //沿x轴抖动
	public static final int SHAKE_Y= 8; //沿Y轴抖动
	public static final int SLIDE_LEFT_IN= 11; //左边滑进
	public static final int SLIDE_LEFT_OUT= 12; //右边滑出
	public static final int SLIDE_RIGHT_IN= 13; //右边滑进
	public static final int SLIDE_RIGHT_OUT= 14; //右边滑出
	public static final int FRONT_BACK_SCALE = 15;  //不停地翻牌
	static boolean flag = false;
	 
	private static Animation cdAnimation;  
	
	//设置动画
	public static void setTransAnimation(int iType,final View view,final Context context) {	
		switch (iType) {
		case SHAKE_INFINITE:   //iphone抖动效果
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.shake_updown);
			cdAnimation.reset();
			cdAnimation.setFillAfter(true);
			view.startAnimation(cdAnimation);
			break;
		case PUSH_DOWN_IN:  //从下往上出现
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.push_down_in);
			view.startAnimation(cdAnimation);
			break;
		case PUSH_DOWN_OUT:	//从上往下消失
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.push_down_out);	
			view.startAnimation(cdAnimation);
			break;			
		case PUSH_UP_IN:
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.push_up_in);
			view.startAnimation(cdAnimation);
			break;
			
		case PUSH_UP_OUT:
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.push_up_out);
			view.startAnimation(cdAnimation);
			break;
		case SLIDE_LEFT_IN:  //左边滑进
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.slide_left_in);
			view.startAnimation(cdAnimation);
			break;
		case SLIDE_LEFT_OUT:  //左边滑出
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.slide_left_out);
			view.startAnimation(cdAnimation);
			break;
		case SLIDE_RIGHT_IN:  //右边滑进
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.slide_right_in);
			view.startAnimation(cdAnimation);
			break;
		case SLIDE_RIGHT_OUT:  //右边滑出
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.slide_right_out);
			view.startAnimation(cdAnimation);		
			
		case SHAKE_X:	//沿x轴抖动
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.shake_x);
			view.startAnimation(cdAnimation);		
			break;
		case SHAKE_Y:   //沿y轴抖动
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.shake_y);
			view.startAnimation(cdAnimation);		
			break;
		case FRONT_BACK_SCALE:   //不停地翻牌
			
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.back_scale);
			view.startAnimation(cdAnimation);	
			cdAnimation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation arg0) {
					// TODO Auto-generated method stub
					flag = !flag;
					int anim = 0 ;
					if(flag)
						anim = R.anim.front_scale;
					else
						anim = R.anim.back_scale;
					
					cdAnimation = AnimationUtils.loadAnimation(context,anim);
					
					view.startAnimation(cdAnimation);	
					
				}
			});
			
			break;
		default:
			break;
			
		}
	}
	
}
