package com.zhuoyi.market.wxapi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage.IMediaObject;
import com.tencent.mm.sdk.platformtools.Util;
import com.zhuoyi.market.R;
import com.market.account.constant.Constant;

public class ShareToWeixin {

    private Context mcontext;
    public static IWXAPI api;
    public ShareToWeixin(Context context)
    {
        mcontext = context;
        api = WXAPIFactory.createWXAPI(mcontext, APP_ID, true);
    }
    
	public static final String APP_ID = "wxa25e8139f70d5e44";
	public static final String APP_KEY = "6ea23d541e61bb83186876f63a7bcaa9";
	//public static IWXAPI api = WXAPIFactory.createWXAPI(context, APP_ID, true);;
	private static final int THUMB_SIZE = 150;

	public static void regToWx(){
	    if (api != null)
        {
	        api.registerApp(APP_ID);
        }
		
	}

	public void SharetoWX(boolean isFriend,Bitmap bmp) {
		regToWx();
//		Bitmap bmp = BitMapUtils.convertFileToBitmap();
		if (bmp == null)
			return;
		WXImageObject imageObject = new WXImageObject(bmp);
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imageObject;
		msg.title = mcontext.getResources().getString(R.string.tip_share_pic);
		int scaleHeight = bmp.getHeight() * THUMB_SIZE / bmp.getWidth();
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, scaleHeight, true);
		bmp.recycle();
		msg.thumbData = Util.bmpToByteArray(thumbBmp, true);  // 缩略图

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("img");
		req.message = msg;
		req.scene = isFriend ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		api.sendReq(req);
	}
	
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}

	
	/***********************************************
	 * 将网页分享到微信
	 ***********************************************/	
	public void shareWebPageToWX(Context context, boolean isFriend, String wapUrl, String title, String summary, byte[] data){
		regToWx();
		WXWebpageObject webObject = new WXWebpageObject();
		webObject.webpageUrl = wapUrl;
		shareToWX(context, isFriend, webObject, data, title, summary);
	}
	
	
	public static void shareToWX(Context context, boolean isFriend, IMediaObject mediaObject, byte[] data, String title,String summary){
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = mediaObject;
		if(summary != null && summary.length() > 1024)
			summary = summary.substring(0, 1024);
		if(summary != null && !summary.equals(""))
			msg.description = summary;
		
		if(title != null && title.length() > 512)
			title = title.substring(0, 512);	
		if(title != null && !title.equals(""))
			msg.title = title;
		
		Bitmap bitmap;
		if(data != null){
			int length = data.length;
			Log.e("shareToWX", "length == "+length);
			int SampleSize = 1;
			if(length == 0)
				bitmap = null;
			else
			{
				if(length % (180*1024) == 0)
					SampleSize = length/(180*1024);
				else
					SampleSize = length/(180*1024) + 1;
				
				BitmapFactory.Options options=new BitmapFactory.Options(); 
				options.inSampleSize = SampleSize;
				bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,options);
			}
			
			if(bitmap != null)
				msg.setThumbImage(bitmap);	//通过位图对象设置缩略图
			else 
			{
//				if(isFriend == false)
//					msg.setThumbImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_wx));
			}
		}else{
			msg.setThumbImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon));
		}
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.scene = isFriend?SendMessageToWX.Req.WXSceneTimeline:SendMessageToWX.Req.WXSceneSession;
		req.message = msg;

		api.sendReq(req);
	}
}
