package com.brazuca.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UtilModule {

	/**
	 * 
	 * @param iTimeType��1����mm/dd HH:mm��2����2011-09-19 22:11:00 Ϊ MySql DateTime
	 * @return
	 */
	public static String getCurTime(int iTimeType) {
		Calendar calendar = Calendar.getInstance();

		int iYear = calendar.get(Calendar.YEAR);
		int iDay = calendar.get(Calendar.DAY_OF_MONTH);
		int iMonth = calendar.get(Calendar.MONTH) + 1;
		int iHour = calendar.get(Calendar.HOUR_OF_DAY);
		int iMinute = calendar.get(Calendar.MINUTE);
		int iSecond = calendar.get(Calendar.SECOND);

		String strTime = "";
		if (iTimeType == 1) { // ʱ���ʽ��mm/dd
			if (iMonth < 10)
				strTime = "0" + iMonth;
			else
				strTime = "" + iMonth;
			if (iDay < 10)
				strTime = strTime + "/0" + iDay;
			else
				strTime = strTime + "/" + iDay;
			if (iHour < 10)
				strTime = strTime + " 0" + iHour;
			else
				strTime = strTime + " " + iHour;
			if (iMinute < 10)
				strTime = strTime + ":0" + iMinute;
			else
				strTime = strTime + ":" + iMinute;
		}
		else if (iTimeType == 2) {
			strTime = iYear + "";
			if (iMonth < 10)
				strTime = strTime + "-0" + iMonth;
			else
				strTime = strTime + "-" + iMonth;
			if (iDay < 10)
				strTime = strTime + "-0" + iDay;
			else
				strTime = strTime + "-" + iDay;
			if (iHour < 10)
				strTime = strTime + " 0" + iHour;
			else
				strTime = strTime + " " + iHour;
			if (iMinute < 10)
				strTime = strTime + ":0" + iMinute;
			else
				strTime = strTime + ":" + iMinute;
			if (iSecond < 10)
				strTime = strTime + ":0" + iSecond;
			else
				strTime = strTime + ":" + iSecond;
		}
		else if (iTimeType == 3) {
			strTime = iYear + "";
			if (iMonth < 10)
				strTime = strTime + "0" + iMonth;
			else
				strTime = strTime  + iMonth;
			if (iDay < 10)
				strTime = strTime + "0" + iDay;
			else
				strTime = strTime + iDay;
			if (iHour < 10)
				strTime = strTime + "0" + iHour;
			else
				strTime = strTime + iHour;
			if (iMinute < 10)
				strTime = strTime + "0" + iMinute;
			else
				strTime = strTime + iMinute;
			if (iSecond < 10)
				strTime = strTime + "0" + iSecond;
			else
				strTime = strTime + iSecond;
		}

		return strTime;
	}

	/**
	 * ��ȡ��ʽ��ʱ�䣺2011-09-19
	 * @param iYear
	 * @param iMonth
	 * @param iDay
	 * @return
	 */
	
	public static String getFormatTime(int iYear, int iMonth, int iDay) {
		String strTime = iYear + "";
		if (iMonth < 10)
			strTime = strTime + "-0" + iMonth;
		else
			strTime = strTime + "-" + iMonth;
		if (iDay < 10)
			strTime = strTime + "-0" + iDay;
		else
			strTime = strTime + "-" + iDay;

		return strTime;
	}

	/**
	 * 
	 * @param iHour
	 *            ���û�ѡ��ľͲ�'Сʱ��"
	 * @return �Ͳ�����
	 */
	public static int getNiningType(int iHour) {
		if (iHour < 10)
			return -1; // ���Ϸ�
		else if (iHour >= 10 && iHour <= 13)
			return 1; // �з�

		else if (iHour > 13 && iHour <= 16)
			return 2; // ���緹
		else if (iHour > 16 && iHour <= 18)
			return 3; // ����
		else if (iHour > 18 && iHour <= 20)
			return 4; // ����
		else if (iHour > 20)
			return -1; // ���Ϸ�
		else
			return 0; // ���Ϸ�
	}

	/**
	 * �ض�ʱ���ǰ��Ƚ�
	 * @param DATE1
	 * @param DATE2
	 * @return
	 */
	public static int compareDateTime(String DATE1, String DATE2) {
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try {
			Date dt1 = df.parse(DATE1);
			Date dt2 = df.parse(DATE2);
			if (dt1.getTime() > dt2.getTime()) {
				System.out.println("dt1 ��dt2ǰ");
				return 1;
			}
			else if (dt1.getTime() < dt2.getTime()) {
				System.out.println("dt1��dt2��");
				return -1;
			}
			else {
				return 0;
			}
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0;
	}

}
