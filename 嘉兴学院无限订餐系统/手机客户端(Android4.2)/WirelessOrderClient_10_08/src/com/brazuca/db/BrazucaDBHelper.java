package com.brazuca.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author yukai
 * ���ݿ���
 */
public class BrazucaDBHelper extends  SQLiteOpenHelper  {
	public static  final int DB_VERSION = 1;	
	public static  final String DB_NAME = "dbBrazuca";		//���ݿ���
	public static  final String TB_USER = "brzucaUserInfo";   //�û���Ϣ��
	/*
	 * dbName:���ݿ�����version:�汾��Ϣ
	 */
	public BrazucaDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
//		db.execSQL("DROP TABLE IF EXISTS "+TB_USER);
		//���ݱ���SQL���  
	    String DATABASE_CREATE = "Create table  "+TB_USER+
	    		"( _id integer primary key autoincrement, "  + 
	    		"user_id varchar(16) not null, "  + 
	    		"username varchar(32) not null, "  + 
	    		"password varchar(32) not null, "  + 
	    		"age varchar(4) not null, "  +
	    		"nickname varchar(32) not null, "  +
	    		"vip varchar(8) not null, "  +
	    		"created text not null);";  

		//�����û���Ϣ��
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		db.execSQL("DROP TABLE IF EXISTS "+TB_USER);
		onCreate(db);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		super.onOpen(db);
	}
}
