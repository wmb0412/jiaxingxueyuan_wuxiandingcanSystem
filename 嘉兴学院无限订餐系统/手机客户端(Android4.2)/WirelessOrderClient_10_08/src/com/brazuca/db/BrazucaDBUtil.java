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

	// ��ʼ�����ݱ�
	public void initDataTable() {
		open();
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BrazucaDBHelper.TB_USER);
		brazucaDBHelper.onCreate(sqLiteDatabase);
		// ��ʼ���û���Ϣ��
		insert(BrazucaDBHelper.TB_USER,"", "","","","","false");
	}

	// �õ��ֶ�Ĭ��ֵ
	public String setDBDefaultData(String strTable, String strField) {
		String strRet = "";

		if (strTable.equals(BrazucaDBHelper.TB_USER)) { // ��ʼ���û���

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
				strRet = "false"; // Ĭ�ϲ���vip
		}
		return strRet;
	}

	/**
	 * �����ݿ�����
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
	 * �����ݿ���в���һ������
	 * 
	 * @param title
	 *            �ֶ�ֵ
	 * @param body
	 *            �ֶ�ֵ
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

		// contentΪ������е�һ����¼��������HASHMAP�����Լ�ֵ����ʽ�洢��
		// insert������һ���������ݿ�������ڶ����������CONTENTΪ��ʱ������в���һ��NULL,����������Ϊ���������
		long falg = sqLiteDatabase.insert(strTable, null,content);
Log.d("falg",falg+"");
		return falg;
	}
	
	 /** 
     * ɾ�����з��������ļ�¼ 
     * @param rowId ɾ������ 
     * @return �Ƿ�ɾ���ɹ� 
     */  
    public boolean delete(String strTable,String strUserId)  
    {  
        //delete������һ���������ݿ�������ڶ���������ʾ�������,����������Ϊ������?�����ֵ  
        //����ֵ����0��ʾɾ���ɹ�
        return sqLiteDatabase.delete(strTable, USER_ID +"="+strUserId , null)>0;  
    }  
    
    /** 
     * ��ѯȫ�����¼ 
     * @return ���ز�ѯ��ȫ�����¼ 
     */  
    public Cursor getAllRecords()  
    {  
        //��ѯ�����������������м�¼  
        return sqLiteDatabase.query(BrazucaDBHelper.TB_USER, new String[] { KEY_ROWID, KEY_TITLE,  
                KEY_BODY, KEY_CREATED }, null, null, null, null, null);  
    }
    
    /** 
     * ��ѯ�������ļ�¼ 
     * @param rowId ����ֵ 
     * @return ���ز�ѯ��� 
     * @throws SQLException ��ѯʱ�쳣�׳� 
     */  
    public Cursor query(String strTable) throws SQLException 
    {
        //��ѯ��������ֵΪrowId�ļ�¼  
        Cursor mCursor =  
            sqLiteDatabase.query(true, strTable, new String[] {  USERNAME,  
            		PASSWORD, AGE ,NICKNAME,VIP }, null, null, null,  
                null, null, null);  
          
        //mCursor������null,����ʶָ���һ����¼  
        if (mCursor != null) {  
            mCursor.moveToFirst();  
        }  
        return mCursor;  
    }  
    
    /** 
     * �������ݿ� 
     * @param rowId �б�ʶ 
     * @param title ���� 
     * @param body ���� 
     * @return �Ƿ���³ɹ� 
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
  
        //��һ������:���ݿ����,�ڶ����������µ�����,�������������µ�����,���ĸ�����������?�ŵ������  
        return sqLiteDatabase.update(str[0], args, null, null) > 0;  
    }  
    
	// �ر����ݿ�
	public void close() {
		if (sqLiteDatabase.isOpen()) {
			sqLiteDatabase.close();
		}
		if (brazucaDBHelper != null) {
			brazucaDBHelper.close();
		}
	}
}
