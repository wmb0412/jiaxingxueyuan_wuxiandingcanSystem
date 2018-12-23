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
	
	public static final int CLEAR_ANIMATION= 0; //�������
	public static final int PUSH_DOWN_IN = 1; //�������ϳ���
	public static final int PUSH_DOWN_OUT = 2; //����������ʧ
	
	public static final int PUSH_UP_IN = 16; //�������³���
	public static final int PUSH_UP_OUT = 17; //����������ʧ

	public static final int SHAKE_INFINITE= 4; //��ͣ����
	public static final int ALPHA_IN= 5; //����
	public static final int ALPHA_OUT= 6; //����
	public static final int SHAKE_X= 7; //��x�ᶶ��
	public static final int SHAKE_Y= 8; //��Y�ᶶ��
	public static final int SLIDE_LEFT_IN= 11; //��߻���
	public static final int SLIDE_LEFT_OUT= 12; //�ұ߻���
	public static final int SLIDE_RIGHT_IN= 13; //�ұ߻���
	public static final int SLIDE_RIGHT_OUT= 14; //�ұ߻���
	public static final int FRONT_BACK_SCALE = 15;  //��ͣ�ط���
	static boolean flag = false;
	 
	private static Animation cdAnimation;  
	
	//���ö���
	public static void setTransAnimation(int iType,final View view,final Context context) {	
		switch (iType) {
		case SHAKE_INFINITE:   //iphone����Ч��
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.shake_updown);
			cdAnimation.reset();
			cdAnimation.setFillAfter(true);
			view.startAnimation(cdAnimation);
			break;
		case PUSH_DOWN_IN:  //�������ϳ���
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.push_down_in);
			view.startAnimation(cdAnimation);
			break;
		case PUSH_DOWN_OUT:	//����������ʧ
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
		case SLIDE_LEFT_IN:  //��߻���
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.slide_left_in);
			view.startAnimation(cdAnimation);
			break;
		case SLIDE_LEFT_OUT:  //��߻���
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.slide_left_out);
			view.startAnimation(cdAnimation);
			break;
		case SLIDE_RIGHT_IN:  //�ұ߻���
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.slide_right_in);
			view.startAnimation(cdAnimation);
			break;
		case SLIDE_RIGHT_OUT:  //�ұ߻���
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.slide_right_out);
			view.startAnimation(cdAnimation);		
			
		case SHAKE_X:	//��x�ᶶ��
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.shake_x);
			view.startAnimation(cdAnimation);		
			break;
		case SHAKE_Y:   //��y�ᶶ��
			cdAnimation = AnimationUtils.loadAnimation(context,R.anim.shake_y);
			view.startAnimation(cdAnimation);		
			break;
		case FRONT_BACK_SCALE:   //��ͣ�ط���
			
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
