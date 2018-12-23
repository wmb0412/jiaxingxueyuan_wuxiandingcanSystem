package com.brazuca.entity;

/**
 * FileName:UserParam.java
 * @author yukai
 * 用户参数类--保存用户的各项资料数据
 */
public class UserParam {
	public static boolean isLogin=false;
	public static int userId=0;
	public static int tableId=0;
	public static String username="";
	public static String password="";
	public static String nickname="";  //昵称
	public static String age="";
	public static int vipType= 0;  //用户等级：0-非会员；1--普通卡（9.5折）；2--银卡--9折；3--金卡8.5折；4--白金卡8折
	public static double balance=0.0d;  //账户余额
	
	public static boolean isInsertDish=false; //是否添加了菜品
	
	public static String orderId="";  //当前处理的订单号
}
