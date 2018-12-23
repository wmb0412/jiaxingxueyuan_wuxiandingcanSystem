package com.brazuca.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;

public class NetWorkUtil {
	public static int MOBLIE_DISCONNECTED = 0x001;
	public static int MOBLIE_UNKNOWN = 0x002;
	public static int WIFI_DISCONNECTED = 0x003;
	public static int WIFI_UNKNOWN = 0x004;
	public static int DISCONNECTED = 0x005;
	public static int UNKNOWN = 0x006;
	
	public static int checkNetworkInfo(Context context)
    {
        ConnectivityManager conMan = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //mobile 3G Data Network
        State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        
        //wifi
        State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if(mobile == State.DISCONNECTED && wifi == State.DISCONNECTED)
        {
        	return DISCONNECTED;
        }
        else
        	return UNKNOWN;
        
//        else if(mobile == State.UNKNOWN)
//        {
//        	return MOBLIE_UNKNOWN;
//        }
//
//        if(wifi == State.DISCONNECTED)
//        {
//        	return WIFI_DISCONNECTED;
//        }
//        else if(wifi == State.UNKNOWN)
//        {
//        	return WIFI_UNKNOWN;
//        }
        
//        return UNKNOWN;
    }
}
