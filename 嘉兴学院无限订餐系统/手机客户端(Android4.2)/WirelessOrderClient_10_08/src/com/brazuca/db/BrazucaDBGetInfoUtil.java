package com.brazuca.db;

import android.content.Context;
import android.database.Cursor;

public class BrazucaDBGetInfoUtil {
	
	/**
	 * 
	 * @param context
	 * @return ���� ���ݿ� �û��ǳ�
	 */
	public static String getDBNickname(Context context) {
		BrazucaDBUtil DBUtil = new BrazucaDBUtil(context);
		Cursor cursor = DBUtil.query(BrazucaDBHelper.TB_USER);

		String strNickname = "";  //�û��ǳ�

		int strRecordNum = cursor.getCount();
		// ��ѯ�ɹ�
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

			if (cursor != null) // �ر��α�
				cursor.close();

			
		}
		DBUtil.close();
		
		return strNickname.trim().toString();
	}
	
	/**
	 * 
	 * @param context
	 * @return ���� ���ݿ� �û�id��
	 */
	public static String getDBUserId(Context context) {
		BrazucaDBUtil DBUtil = new BrazucaDBUtil(context);
		Cursor cursor = DBUtil.query(BrazucaDBHelper.TB_USER);

		String strUserID = "";  //�û��ǳ�

		int strRecordNum = cursor.getCount();
		// ��ѯ�ɹ�
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

			if (cursor != null) // �ر��α�
				cursor.close();

			
		}
		
		DBUtil.close();
		
		return strUserID.trim().toString();
	}
	
}
