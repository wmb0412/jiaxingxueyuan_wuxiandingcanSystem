package com.brazuca.db;

import android.content.Context;
import android.database.Cursor;

public class BrazucaDBGetInfoUtil {
	
	/**
	 * 
	 * @param context
	 * @return 本地 数据库 用户昵称
	 */
	public static String getDBNickname(Context context) {
		BrazucaDBUtil DBUtil = new BrazucaDBUtil(context);
		Cursor cursor = DBUtil.query(BrazucaDBHelper.TB_USER);

		String strNickname = "";  //用户昵称

		int strRecordNum = cursor.getCount();
		// 查询成功
		if (strRecordNum > 0) {
			System.out.println("strRecordNum=" + strRecordNum);

			for (int i = 0; i < cursor.getColumnCount(); i++) {
				String column = cursor.getString(i);
				System.out.println(cursor.getColumnName(i)+ "=" + column);

				if(cursor.getColumnName(i).equals(BrazucaDBUtil.NICKNAME))
				{
					strNickname = cursor.getString(i);
					System.out.println("strNickname=" + strNickname);
				}
			}

			if (cursor != null) // 关闭游标
				cursor.close();

			
		}
		DBUtil.close();
		
		return strNickname.trim().toString();
	}
	
	/**
	 * 
	 * @param context
	 * @return 本地 数据库 用户id号
	 */
	public static String getDBUserId(Context context) {
		BrazucaDBUtil DBUtil = new BrazucaDBUtil(context);
		Cursor cursor = DBUtil.query(BrazucaDBHelper.TB_USER);

		String strUserID = "";  //用户昵称

		int strRecordNum = cursor.getCount();
		// 查询成功
		if (strRecordNum > 0) {
			System.out.println("strRecordNum=" + strRecordNum);

			for (int i = 0; i < cursor.getColumnCount(); i++) {
				String column = cursor.getString(i);
				System.out.println(cursor.getColumnName(i)+ "=" + column);

				if(cursor.getColumnName(i).equals(BrazucaDBUtil.USER_ID))
				{
					strUserID = cursor.getString(i);
				}
			}

			if (cursor != null) // 关闭游标
				cursor.close();

			
		}
		
		DBUtil.close();
		
		return strUserID.trim().toString();
	}
	
}
