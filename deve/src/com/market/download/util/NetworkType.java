package com.market.download.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetworkType {
    /**
     * int value: 0
     */
    public static final int NOT_AVAILABLE = 0;
    /**
     * int value: 3
     */
    public static final int WIFI = 3;
    /**
     * int value: 1
     */
    public static final int GPRS_2G = 1;
    /**
     * int value: 2
     */
    public static final int GPRS_3G = 2;
    /**
     * int value: 4
     */
    public static final int GPRS_4G = 4;
    /**
     * int value: 5
     */
    public static final int GPRS_5G = 5;
    
    
    /**
     * string value: "wifi"
     */
    private static final String TYPE_WIFI = "wifi";
    /**
     * string value: "unknown"
     */
    private static final String TYPE_NO_NET = "unknown";
    /**
     * string value: "2G"
     */
    private static final String TYPE_2G = "2G";
    /**
     * string value: "3G"
     */
    private static final String TYPE_3G = "3G";
    /**
     * string value: "4G"
     */
    private static final String TYPE_4G = "4G";
    /**
     * string value: "5G"
     */
    private static final String TYPE_5G = "5G";
    
    
    private static final int NETWORK_TYPE_WIFI = -101;
    /** Network type is unknown */
    private static final int NETWORK_TYPE_UNKNOWN = 0;
    /** Current network is GPRS */
    private static final int NETWORK_TYPE_GPRS = 1;
    /** Current network is EDGE */
    private static final int NETWORK_TYPE_EDGE = 2;
    /** Current network is UMTS */
    private static final int NETWORK_TYPE_UMTS = 3;
    /** Current network is CDMA: Either IS95A or IS95B */
    private static final int NETWORK_TYPE_CDMA = 4;
    /** Current network is EVDO revision 0 */
    private static final int NETWORK_TYPE_EVDO_0 = 5;
    /** Current network is EVDO revision A */
    private static final int NETWORK_TYPE_EVDO_A = 6;
    /** Current network is 1xRTT */
    private static final int NETWORK_TYPE_1xRTT = 7;
    /** Current network is HSDPA */
    private static final int NETWORK_TYPE_HSDPA = 8;
    /** Current network is HSUPA */
    private static final int NETWORK_TYPE_HSUPA = 9;
    /** Current network is HSPA */
    private static final int NETWORK_TYPE_HSPA = 10;
    /** Current network is iDen */
    private static final int NETWORK_TYPE_IDEN = 11;
    /** Current network is EVDO revision B */
    private static final int NETWORK_TYPE_EVDO_B = 12;
    /** Current network is LTE */
    private static final int NETWORK_TYPE_LTE = 13;
    /** Current network is eHRPD */
    private static final int NETWORK_TYPE_EHRPD = 14;
    /** Current network is HSPA+ */
    private static final int NETWORK_TYPE_HSPAP = 15;
    
    
    /**
     * for get current available and connected network type
     * 
     * @param context
     * @return {@link #NOT_AVAILABLE}<br/>
     *         {@link #WIFI}<br/>
     *         {@link #GPRS_2G}<br/>
     *         {@link #GPRS_3G}<br/>
     *         {@link #GPRS_4G}<br/>
     *         {@link #GPRS_5G}<br/>
     */
    public static int getNetworkType(Context context) {
//        int netStatus = NOT_AVAILABLE;
//        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        NetworkInfo info = connectMgr.getActiveNetworkInfo();
//
//        if (info != null && info.isConnected() && info.isAvailable()) {
//            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
//                // wifi network
//                netStatus = WIFI;
//            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
//                int subType = info.getSubtype();
//                if (subType == TelephonyManager.NETWORK_TYPE_UMTS
//                        || subType == TelephonyManager.NETWORK_TYPE_HSDPA
//                        || subType == TelephonyManager.NETWORK_TYPE_EVDO_0
//                        || subType == TelephonyManager.NETWORK_TYPE_EVDO_A) {
//                    // 3G network
//                    netStatus = GPRS_3G;
//                } else {
//                    // 2G or 2.5G
//                    netStatus = GPRS_2G;
//                }
//            }
//        }
//        return netStatus;
        
        
        int networkType = NETWORK_TYPE_UNKNOWN;  
        try {  
        	ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo info = connectMgr.getActiveNetworkInfo();
            if (info != null && info.isAvailable() && info.isConnected()) {
                int type = info.getType();
                if (type == ConnectivityManager.TYPE_WIFI) {
                	networkType = NETWORK_TYPE_WIFI;
                } else if (type == ConnectivityManager.TYPE_MOBILE) {
                    TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                    networkType = telephonyManager.getNetworkType();
                }
            } else {
                return NOT_AVAILABLE;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return getNetworkClassByType(networkType);
    }
    
    
    private static int getNetworkClassByType(int networkType) {  
        switch (networkType) {
        case NETWORK_TYPE_WIFI:
            return WIFI;
            
        case NETWORK_TYPE_GPRS:
        case NETWORK_TYPE_EDGE:
        case NETWORK_TYPE_CDMA:
        case NETWORK_TYPE_1xRTT:
        case NETWORK_TYPE_IDEN:
            return GPRS_2G;
            
        case NETWORK_TYPE_UMTS:
        case NETWORK_TYPE_EVDO_0:
        case NETWORK_TYPE_EVDO_A:
        case NETWORK_TYPE_HSDPA:
        case NETWORK_TYPE_HSUPA:
        case NETWORK_TYPE_HSPA:
        case NETWORK_TYPE_EVDO_B:
        case NETWORK_TYPE_EHRPD:
        case NETWORK_TYPE_HSPAP:
            return GPRS_3G;
            
        case NETWORK_TYPE_LTE:
            return GPRS_4G;
            
        default:
            return GPRS_5G;
        }
    }
    

    /**
     * check current network is available or not
     * 
     * @param context
     * @return if network is available, return true<br/>
     *         if network is not available, return false
     */
    public static boolean isNetworkAvailable(Context context) {
        int currType = getNetworkType(context);
        if (currType == NOT_AVAILABLE) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * check wifi is available or not
     * 
     * @param context
     * @return if wifi is available, return true<br/>
     *         if wifi is not available, return false
     */
    public static boolean isWifiAvailable(Context context) {
        int currType = getNetworkType(context);
        if (currType == WIFI) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * for get current available network type description string
     * 
     * @param context
     * @return {@link #TYPE_NO_NET}<br/>
     *         {@link #TYPE_WIFI}<br/>
     *         {@link #TYPE_2G}<br/>
     *         {@link #TYPE_3G}<br/>
     */
    public static String getNetTypeString(Context context) {
        int currType = getNetworkType(context);
        switch(currType) {
        case NOT_AVAILABLE:
        	return TYPE_NO_NET;
        case WIFI:
        	return TYPE_WIFI;
        case GPRS_2G:
        	return TYPE_2G;
        case GPRS_3G:
        	return TYPE_3G;
        case GPRS_4G:
        	return TYPE_4G;
        case GPRS_5G:
        	return TYPE_5G;
        } 

        return "unknown";
    }
}
