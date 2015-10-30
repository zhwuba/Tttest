package com.zhuoyi.market.wallpaper;

import java.util.HashMap;

import com.market.net.SenderDataProvider;
import com.market.net.request.GetWallpaperListReq;
import com.market.net.response.GetWallpaperListResp;
import com.market.net.utils.StartNetReqUtils;
import com.zhuoyi.market.appResident.MarketApplication;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

public class DisplayWallpaperView extends WallpaperView {
	
	public static final int NET_TYPE_NONE= -1;
	public static final int NET_TYPE_CATEGORY_DETAIL = 0;
	public static final int NET_TYPE_HOTEST = 1;
	public static final int NET_TYPE_NEWEST = 2;
	public static final int NET_TYPE_CATEGORY = 3;

    private DisplayWallpaperStorage mDisplayWallpaperStorage = null;
    
    private GetWallpaperListReq mGetWallpaperReq = null;
    private int mNetMsgCode = 0;
    private Context mContext = null;
    private int mIndex = 0;
    private int mNetType = NET_TYPE_NONE;
    private String mCode = null;
    
    private boolean mFirstInReq = true;
    
    private static final int MSG_GET_DATE = 1;
    private Handler mHandler = new Handler() {
        
        @Override
        public void handleMessage(Message message) {
           switch (message.what) {
           case MSG_GET_DATE:
               
        	   HashMap<String, Object> map = (HashMap<String, Object>) message.obj;
               if (map == null) {
                   DisplayWallpaperView.this.setReqSuccess(false);
               } else {
            	   GetWallpaperListResp listResp = (GetWallpaperListResp) map.get("wallpaperList");
            	   if (listResp == null) {
            		   DisplayWallpaperView.this.setReqSuccess(false);
            	   } else {
            		   mIndex = listResp.getIndex();
                       if (mIndex < 0) {
                           DisplayWallpaperView.this.setReqBottom(true);
                       }
                       if (mDisplayWallpaperStorage != null) {
                           mDisplayWallpaperStorage.putWallpaperData(listResp.getAppList());
                           DisplayWallpaperView.this.notifyDataSetChanged();
                       }
                       DisplayWallpaperView.this.setReqSuccess(true);
            	   }
               }
               DisplayWallpaperView.this.setReqFinish(true);
               break;
           }
        }
    };
    

    public DisplayWallpaperView(int type, int netType, int[] wDisplay, int msgCode, String code) {
        super(type, wDisplay[0]);
        // TODO Auto-generated constructor stub
        mContext = MarketApplication.getRootContext();
        mNetMsgCode = msgCode;
        mNetType = netType;
        mCode = code;
        
        mDisplayWallpaperStorage = this.getDisplayWallpaperStorage();
        mGetWallpaperReq= new GetWallpaperListReq();
        //请求窄幅图片
        mGetWallpaperReq.setIsWide(0);
    }
    
    
    private void reqWallpaperInfo() {

        if (mGetWallpaperReq == null || !this.getReqFinish()) return;
        
        mGetWallpaperReq.setIndex(mIndex);
        mGetWallpaperReq.setType(mNetType);
        if (!TextUtils.isEmpty(mCode)) {
        	mGetWallpaperReq.setCode(mCode);
        }
        
        DisplayWallpaperView.this.setReqFinish(false);

        String contents = SenderDataProvider.buildToJSONData(mContext, mNetMsgCode ,mGetWallpaperReq);
        StartNetReqUtils.execListByPageRequest(mHandler, MSG_GET_DATE, mNetMsgCode, contents);
    }
    
    
    public void getDataFirstIn() {
    	if (mFirstInReq) {
	    	this.mFirstReq = true;
	    	reqWallpaperInfo();
	    	mFirstInReq = false;
        }
    }
    

    @Override
    public void getData() {
        // TODO Auto-generated method stub
    	if (!mFirstInReq) {
    		reqWallpaperInfo();
    	}
    }
}
