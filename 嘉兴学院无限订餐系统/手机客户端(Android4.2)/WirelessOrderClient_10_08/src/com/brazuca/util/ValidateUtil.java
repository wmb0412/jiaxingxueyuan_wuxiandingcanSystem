package com.brazuca.util;

public class ValidateUtil {
	
	//��¼��ע����������֤
    public static boolean validateInfo(String strContent)
    {
    	
    	if(strContent.equals(""))
    	{
    		return false;
    	}
    	else
    		return true;
    }
    
	//��¼��ע����������֤
    public static boolean validateLength(String strContent)
    {
    	
    	if(strContent.length()<6)
    	{
    		return false;
    	}
    	else
    		return true;
    }
    
  //ע������������ȷ���Ƿ���ȵ���֤
    public static boolean validateEquals(String password,String rePassword)
    {
    	
    	if(!password.equals(rePassword))
    	{
    		return false;
    	}
    	else
    		return true;
    }
}
