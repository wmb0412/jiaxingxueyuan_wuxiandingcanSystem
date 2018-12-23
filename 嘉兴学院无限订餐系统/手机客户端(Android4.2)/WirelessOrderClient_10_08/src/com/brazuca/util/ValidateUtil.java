package com.brazuca.util;

public class ValidateUtil {
	
	//登录和注册输入框的验证
    public static boolean validateInfo(String strContent)
    {
    	
    	if(strContent.equals(""))
    	{
    		return false;
    	}
    	else
    		return true;
    }
    
	//登录和注册输入框的验证
    public static boolean validateLength(String strContent)
    {
    	
    	if(strContent.length()<6)
    	{
    		return false;
    	}
    	else
    		return true;
    }
    
  //注册输入框密码和确认是否相等的验证
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
