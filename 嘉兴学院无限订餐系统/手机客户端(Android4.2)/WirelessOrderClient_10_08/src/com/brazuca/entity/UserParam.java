package com.brazuca.entity;

/**
 * FileName:UserParam.java
 * @author yukai
 * �û�������--�����û��ĸ�����������
 */
public class UserParam {
	public static boolean isLogin=false;
	public static int userId=0;
	public static int tableId=0;
	public static String username="";
	public static String password="";
	public static String nickname="";  //�ǳ�
	public static String age="";
	public static int vipType= 0;  //�û��ȼ���0-�ǻ�Ա��1--��ͨ����9.5�ۣ���2--����--9�ۣ�3--��8.5�ۣ�4--�׽�8��
	public static double balance=0.0d;  //�˻����
	
	public static boolean isInsertDish=false; //�Ƿ�����˲�Ʒ
	
	public static String orderId="";  //��ǰ����Ķ�����
}
