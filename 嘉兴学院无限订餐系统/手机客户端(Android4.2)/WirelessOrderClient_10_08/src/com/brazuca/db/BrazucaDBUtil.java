package com.brazuca.db;

import com.brazuca.util.UtilModule;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class BrazucaDBUtil {
	public static final String KEY_TITLE = "title";
	public static final String KEY_BODY = "body";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_CREATED = "created";
	public static final String USER_ID = "user_id";
	public static final String USERNAME="username";
	public static final String PASSWORD = "password";
	public static final String AGE = "age";
	public static final String NICKNAME="nickname";
	public static final String VIP="vip";	

	public BrazucaDBHelper brazucaDBHelper;
	public SQLiteDatabase sqLiteDatabase;

	public BrazucaDBUtil(Context context) {
		brazucaDBHelper = new BrazucaDBHelper(context);
		sqLiteDatabase = brazucaDBHelper.getWritableDatabase();
	}

	// 初始化数据表
	public void initDataTable() {
		open();
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BrazucaDBHelper.TB_USER);
		brazucaDBHelper.onCreate(sqLiteDatabase);
		// 初始化用户信息表
		insert(BrazucaDBHelper.TB_USER,"", "","","","","false");
	}

	// 得到字段默认值
	public String setDBDefaultData(String strTable, String strField) {
		String strRet = "";

		if (strTable.equals(BrazucaDBHelper.TB_USER)) { // 初始化用户表

			if (strField.equals("user_id"))
				strRet = "";
			else if (strField.equals("username"))
				strRet = "";
			else if (strField.equals("password"))
				strRet = "";
			else if (strField.equals("nickname"))
				strRet = "";
			else if (strField.equals("age"))
				strRet = "";
			else if (strField.equals("vip"))
				strRet = "false"; // 默认不是vip
		}
		return strRet;
	}

	/**
	 * 打开数据库连接
	 */
	private void open() {
		
		try {
			sqLiteDatabase = brazucaDBHelper.getWritableDatabase();
		}
		catch (SQLiteException ex) {
			sqLiteDatabase = brazucaDBHelper.getReadableDatabase();
		}
	}

	/**
	 * 向数据库表中插入一条数据
	 * 
	 * @param title
	 *            字段值
	 * @param body
	 *            字段值
	 */
	public long insert(String strTable,String StrUserId, String strUsername,
			String strPassword,String strAge,String strNickName,String strVip) {	
		ContentValues content = new ContentValues();
		
		content.put(USER_ID, StrUserId);
		content.put(USERNAME, strUsername);
		content.put(PASSWORD, strPassword);
		content.put(AGE, strAge);
		content.put(NICKNAME, strNickName);
		content.put(VIP, strVip);
		
		content.put(KEY_CREATED, UtilModule.getCurTime(2).toString());

		// content为插入表中的一条记录，类似与HASHMAP，是以键值对形式存储。
		// insert方法第一参数：数据库表名，第二个参数如果CONTENT为空时则向表中插入一个NULL,第三个参数为插入的内容
		long falg = sqLiteDatabase.insert(strTable, null,content);
Log.d("falg",falg+"");
		return falg;
	}
	
	 /** 
     * 删除表中符合条件的记录 
     * @param rowId 删除条件 
     * @return 是否删除成功 
     */  
    public boolean delete(String strTable,String strUserId)  
    {  
        //delete方法第一参数：数据库表名，第二个参数表示条件语句,第三个参数为条件带?的替代值  
        //返回值大于0表示删除成功
        return sqLiteDatabase.delete(strTable, USER_ID +"="+strUserId , null)>0;  
    }  
    
    /** 
     * 查询全部表记录 
     * @return 返回查询的全部表记录 
     */  
    public Cursor getAllRecords()  
    {  
        //查询表中满足条件的所有记录  
        return sqLiteDatabase.query(BrazucaDBHelper.TB_USER, new String[] { KEY_ROWID, KEY_TITLE,  
                KEY_BODY, KEY_CREATED }, null, null, null, null, null);  
    }
    
    /** 
     * 查询带条件的记录 
     * @param rowId 条件值 
     * @return 返回查询结果 
     * @throws SQLException 查询时异常抛出 
     */  
    public Cursor query(String strTable) throws SQLException 
    {
        //查询表中条件值为rowId的记录  
        Cursor mCursor =  
            sqLiteDatabase.query(true, strTable, new String[] {  USERNAME,  
            		PASSWORD, AGE ,NICKNAME,VIP }, null, null, null,  
                null, null, null);  
          
        //mCursor不等于null,将标识指向第一条记录  
        if (mCursor != null) {  
            mCursor.moveToFirst();  
        }  
        return mCursor;  
    }  
    
    /** 
     * 更新数据库 
     * @param rowId 行标识 
     * @param title 内容 
     * @param body 内容 
     * @return 是否更新成功 
     */  
    public boolean update(String...str) {  
    	
        ContentValues args = new ContentValues();  
        args.put(USER_ID, str[1]);
        args.put(USERNAME, str[2]);
        args.put(PASSWORD, str[3]);
        args.put(AGE, str[4]);
        args.put(NICKNAME, str[5]);
        args.put(VIP, str[6]);
        args.put(KEY_CREATED, UtilModule.getCurTime(2));  
  
        //第一个参数:数据库表名,第二个参数更新的内容,第三个参数更新的条件,第四个参数条件带?号的替代者  
        return sqLiteDatabase.update(str[0], args, null, null) > 0;  
    }  
    
	// 关闭数据库
	public void close() {
		if (sqLiteDatabase.isOpen()) {
			sqLiteDatabase.close();
		}
		if (brazucaDBHelper != null) {
			brazucaDBHelper.close();
		}
	}
}
