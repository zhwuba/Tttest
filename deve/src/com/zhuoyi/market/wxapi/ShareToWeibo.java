package com.zhuoyi.market.wxapi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.market.account.login.BaseActivity_Html5;
import com.market.account.netutil.JavaScriptOperation;
import com.market.account.weibosdk.AccessTokenKeeper;
import com.market.account.weibosdk.Constants;
import com.market.account.weibosdk.RequestListener;
import com.market.account.weibosdk.StatusesAPI;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.market.account.weibosdk.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.exception.WeiboShareException;
import com.sina.weibo.sdk.utils.Utility;
import com.zhuoyi.market.R;

public class ShareToWeibo {
	  /** 微博微博分享接口实例 */
    public static IWeiboShareAPI mWeiboShareAPI;
    
    private Context mContext;
    
    private String mWebUrl;
    private String mDesc;
    private Bitmap mBitmap;

    public final static int SHARE_SUCCESS = 0;
    public final static int SHARE_CACEL = -1;
    public final static int SHARE_FAILED = -2;

	public final static int SHARE_REDPACKET = 0;
	public final static int SHARE_ACHIV = 1;
	public final static int SHARE_ACHIV_TEXT = 2;
	
	private static int SHARE_TYPE = -1;

	/** 登陆认证对应的listener */
    private AuthListener mLoginListener = new AuthListener();
    /** 创建授权认证信息 */
    private AuthInfo authInfo = null;
    /** 微博授权认证回调 */
    private WeiboAuthListener mAuthListener;
    
    
	public ShareToWeibo(Context context){
    	mContext = context;
    	mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(mContext, Constants.APP_KEY);
    	mWeiboShareAPI.registerApp();
    	/**	web api		**/
    	 // 创建授权认证信息
        authInfo = new AuthInfo(mContext, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        mAuthListener = mLoginListener;
    }

	public byte[] Bitmap2Bytes(Bitmap bm) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
    	return baos.toByteArray();
    }
    
    /**
     * 创建图片消息对象。
     * 
     * @return 图片消息对象。
     */
    private ImageObject getImageObj()
    {
        ImageObject imageObject = new ImageObject();
        Bitmap bmp = mBitmap;
        if (bmp != null)
        {
            imageObject.setImageObject(bmp);
        }

        return imageObject;
    }
    public String getmDesc() {
		return mDesc;
	}
    
    public String getmWebUrl() {
		return mWebUrl;
	}
    
    
    /**
     * 获取分享的文本模板。
     * 
     * @return 分享的文本模板
     */
    private String getSharedText()
    {
        int formatId = R.string.weibosdk_demo_share_text_template;
        String format = mContext.getString(formatId);
        String text = format;
        return text;
    }

    /**
     * 创建文本消息对象。
     * 
     * @return 文本消息对象。
     */
    private TextObject getTextObj()
    {
        TextObject textObject = new TextObject();
        if (TextUtils.isEmpty(getmDesc())) {
            textObject.text = getSharedText();
        }else {
            textObject.text = getmDesc();
        }
        return textObject;
    }

    /**
     * 创建多媒体（网页）消息对象。
     * 
     * @return 多媒体（网页）消息对象。
     */
    private WebpageObject getWebpageObj() {
        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = getmDesc();
        mediaObject.description = getmDesc();
        // 设置 Bitmap 类型的图片到视频对象里
//        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_login_account);
//        mediaObject.thumbData = Bitmap2Bytes(bitmap);
        mediaObject.setThumbImage(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon));
        mediaObject.actionUrl = getmWebUrl();
        mediaObject.defaultText = getmDesc();
        return mediaObject;
    }

    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。
     * 
     * @see {@link #sendMultiMessage} 或者 {@link #sendSingleMessage}
     */
    private void sendMessage(boolean hasText, boolean hasImage, boolean hasWebpage, boolean hasMusic, boolean hasVideo, boolean hasVoice)
    {

        if (mWeiboShareAPI.isWeiboAppSupportAPI())
        {
            int supportApi = mWeiboShareAPI.getWeiboAppSupportAPI();
            if (supportApi >= 10351 /* ApiUtils.BUILD_INT_VER_2_2 */)
            {
                sendMultiMessage(hasText, hasImage, hasWebpage, hasMusic, hasVideo, hasVoice);
            }
            else
            {
                sendSingleMessage(hasText, hasImage, hasWebpage, hasMusic, hasVideo/* , hasVoice */);
            }
        }
        else
        {
//            Toast.makeText(this, R.string.weibosdk_demo_not_support_api_hint, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。 注意：当 {@link IWeiboShareAPI#getWeiboAppSupportAPI()} >= 10351 时，支持同时分享多条消息， 同时可以分享文本、图片以及其它媒体资源（网页、音乐、视频、声音中的一种）。
     * 
     * @param hasText
     *            分享的内容是否有文本
     * @param hasImage
     *            分享的内容是否有图片
     * @param hasWebpage
     *            分享的内容是否有网页
     * @param hasMusic
     *            分享的内容是否有音乐
     * @param hasVideo
     *            分享的内容是否有视频
     * @param hasVoice
     *            分享的内容是否有声音
     */
    private void sendMultiMessage(boolean hasText, boolean hasImage, boolean hasWebpage, boolean hasMusic,
        boolean hasVideo, boolean hasVoice) {
        // 1. 初始化微博的分享消息
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        if (hasText) {
            weiboMessage.textObject = getTextObj();
        }
        if (hasImage) {
            weiboMessage.imageObject = getImageObj();
        }
        if (hasWebpage) {
            weiboMessage.mediaObject = getWebpageObj();
        }
        if (hasMusic) {
            // weiboMessage.mediaObject = getMusicObj();
        }
        if (hasVideo) {
            // weiboMessage.mediaObject = getVideoObj();
        }
        if (hasVoice) {
            // weiboMessage.mediaObject = getVoiceObj();
        }

        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        mWeiboShareAPI.sendRequest((Activity)mContext,request);

    }

    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。 当{@link IWeiboShareAPI#getWeiboAppSupportAPI()} < 10351 时，只支持分享单条消息，即 文本、图片、网页、音乐、视频中的一种，不支持Voice消息。
     * 
     * @param hasText
     *            分享的内容是否有文本
     * @param hasImage
     *            分享的内容是否有图片
     * @param hasWebpage
     *            分享的内容是否有网页
     * @param hasMusic
     *            分享的内容是否有音乐
     * @param hasVideo
     *            分享的内容是否有视频
     */
    private void sendSingleMessage(boolean hasText, boolean hasImage, boolean hasWebpage, boolean hasMusic, boolean hasVideo/* , boolean hasVoice */)
    {

        // 1. 初始化微博的分享消息
        // 用户可以分享文本、图片、网页、音乐、视频中的一种
        WeiboMessage weiboMessage = new WeiboMessage();
        if (hasText)
        {
            weiboMessage.mediaObject = getTextObj();
        }
        if (hasImage)
        {
            weiboMessage.mediaObject = getImageObj();
        }
        if (hasWebpage)
        {
             weiboMessage.mediaObject = getWebpageObj();
        }
        if (hasMusic)
        {
//             weiboMessage.mediaObject = getMusicObj();
        }
        if (hasVideo)
        {
//             weiboMessage.mediaObject = getVideoObj();
        }
        /*
         * if (hasVoice) { weiboMessage.mediaObject = getVoiceObj(); }
         */

        // 2. 初始化从第三方到微博的消息请求
        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        mWeiboShareAPI.sendRequest((Activity)mContext,request);
    }
    
    public void setmDesc(String mDesc) {
		this.mDesc = mDesc;
	}
    
    public void setmWebUrl(String mWebUrl) {
		this.mWebUrl = mWebUrl;
	}


    public void shareToSinaWeibo(int type) {
        try {
            SHARE_TYPE = type;
            Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(mContext);
                switch (type) {
                case SHARE_ACHIV:
                    if(accessToken != null && accessToken.isSessionValid()){
                        uploadShare(accessToken);
                    }else{
                        /** web login   **/
                        if(authInfo != null){
                            SsoHandler weiboAuth = new SsoHandler((Activity)mContext, authInfo);
                            weiboAuth.authorizeWeb(mAuthListener);
                        }
                    }
                    break;
                case SHARE_REDPACKET:
                    if(accessToken != null && accessToken.isSessionValid()){
                        uploadUrl(accessToken);
                    }else{
                        /** web login   **/
                        if(authInfo != null){
                            SsoHandler weiboAuth = new SsoHandler((Activity)mContext, authInfo);
                            weiboAuth.authorizeWeb(mAuthListener);
                        }
                    }
                    break;
                case SHARE_ACHIV_TEXT:
                    if(accessToken != null && accessToken.isSessionValid()){
                        uploadImgUrl(accessToken);
                    }else{
                        /** web login   **/
                        if(authInfo != null){
                            SsoHandler weiboAuth = new SsoHandler((Activity)mContext, authInfo);
                            weiboAuth.authorizeWeb(mAuthListener);
                        }
                    }
                    break;
                default:
                    break;
                }
        } catch (WeiboShareException e) {
            e.printStackTrace();
        }
    }
    
    
    public Bitmap getmBitmap() {
		return mBitmap;
	}

    
	public void setmBitmap(Bitmap mBitmap) {
		this.mBitmap = mBitmap;
	}
    /**
     * 创建多媒体（音乐）消息对象。
     * 
     * @return 多媒体（音乐）消息对象。
     */
//    private MusicObject getMusicObj() {
//        // 创建媒体消息
//        MusicObject musicObject = new MusicObject();
//        musicObject.identify = Utility.generateGUID();
//        musicObject.title = mShareMusicView.getTitle();
//        musicObject.description = mShareMusicView.getShareDesc();
//        
//        // 设置 Bitmap 类型的图片到视频对象里
//        musicObject.setThumbImage(mShareMusicView.getThumbBitmap());
//        musicObject.actionUrl = mShareMusicView.getShareUrl();
//        musicObject.dataUrl = "www.weibo.com";
//        musicObject.dataHdUrl = "www.weibo.com";
//        musicObject.duration = 10;
//        musicObject.defaultText = "Music 默认文案";
//        return musicObject;
//    }

    /**
     * 创建多媒体（视频）消息对象。
     * 
     * @return 多媒体（视频）消息对象。
     */
//    private VideoObject getVideoObj() {
//        // 创建媒体消息
//        VideoObject videoObject = new VideoObject();
//        videoObject.identify = Utility.generateGUID();
//        videoObject.title = mShareVideoView.getTitle();
//        videoObject.description = mShareVideoView.getShareDesc();
//        
//        // 设置 Bitmap 类型的图片到视频对象里
//        videoObject.setThumbImage(mShareVideoView.getThumbBitmap());
//        videoObject.actionUrl = mShareVideoView.getShareUrl();
//        videoObject.dataUrl = "www.weibo.com";
//        videoObject.dataHdUrl = "www.weibo.com";
//        videoObject.duration = 10;
//        videoObject.defaultText = "Vedio 默认文案";
//        return videoObject;
//    }

    /**
     * 创建多媒体（音频）消息对象。
     * 
     * @return 多媒体（音乐）消息对象。
     */
//    private VoiceObject getVoiceObj() {
//        // 创建媒体消息
//        VoiceObject voiceObject = new VoiceObject();
//        voiceObject.identify = Utility.generateGUID();
//        voiceObject.title = mShareVoiceView.getTitle();
//        voiceObject.description = mShareVoiceView.getShareDesc();
//        
//        // 设置 Bitmap 类型的图片到视频对象里
//        voiceObject.setThumbImage(mShareVoiceView.getThumbBitmap());
//        voiceObject.actionUrl = mShareVoiceView.getShareUrl();
//        voiceObject.dataUrl = "www.weibo.com";
//        voiceObject.dataHdUrl = "www.weibo.com";
//        voiceObject.duration = 10;
//        voiceObject.defaultText = "Voice 默认文案";
//        return voiceObject;
//    }
    private class AuthListener implements WeiboAuthListener {
        @Override
        public void onComplete(Bundle values) {
            final Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
            if(accessToken != null){
                AccessTokenKeeper.writeAccessToken(mContext, accessToken);
                new Thread(){
                    @Override
                    public void run() {
                        Looper.prepare();
                        switch (SHARE_TYPE) {
                        case SHARE_ACHIV:
                            uploadShare(accessToken);
                            break;
                        case SHARE_REDPACKET:
                            uploadUrl(accessToken);
                            break;
                        case SHARE_ACHIV_TEXT:
                            uploadImgUrl(accessToken);
                            break;
                        default:
                            break;
                        }
                        Looper.loop();
                    }
                }.start();
            }
        }


        @Override
        public void onWeiboException(WeiboException e) {

        }


        @Override
        public void onCancel() {

        }
    }
	
	
	public void uploadUrl(final Oauth2AccessToken accessToken){
		final Dialog dialog = new Dialog(mContext, R.style.Dialog_Translucent);
		View view = View.inflate(mContext,R.layout.layout_share_weibo_webpage, null);
		dialog.setContentView(view);
		dialog.setCancelable(false);
		
		final TextView title = (TextView) view.findViewById(R.id.title);
//		final EditText shareText = (EditText) dialog.findViewById(R.id.text);
		TextView cancel = (TextView) view.findViewById(R.id.share_cancel);
		TextView sure = (TextView) view.findViewById(R.id.share_sure);
		
		if(getmDesc() != null){
			title.setText(getmDesc());
		}
		
		sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				StatusesAPI statusesAPI = new StatusesAPI(mContext, Constants.APP_KEY, accessToken);
				Toast.makeText(mContext, R.string.share_now, Toast.LENGTH_SHORT).show();
				statusesAPI.update(title.getText() + getmWebUrl(), "0.0", "0.0", new RequestListener() {

					@Override
					public void onWeiboException(WeiboException arg0) {
						try {
							JSONObject jsonObject = new JSONObject(arg0.getMessage());
							if(jsonObject.has("error_code")){
								int code = jsonObject.getInt("error_code");
								if(code == 20019){
									Toast.makeText(mContext, R.string.share_repeat_content, Toast.LENGTH_SHORT).show();
								}else{
									Toast.makeText(mContext, R.string.share_fail, Toast.LENGTH_SHORT).show();
								}
							}
						} catch (Exception e) {
							Toast.makeText(mContext, R.string.share_fail, Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
						sendShareCallBack(SHARE_FAILED);
					}

					@Override
					public void onComplete(String arg0) {
						Toast.makeText(mContext, R.string.share_success, Toast.LENGTH_SHORT).show();
						sendShareCallBack(SHARE_SUCCESS);
					}

                    @Override
                    public void onComplete4binary(ByteArrayOutputStream responseOS) {
                        // TODO Auto-generated method stub
                        
                    }

                    @Override
                    public void onIOException(IOException e) {
                        Toast.makeText(mContext, R.string.share_fail+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(WeiboException e) {
                        Toast.makeText(mContext, R.string.share_fail+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
				});
				dialog.dismiss();
			}

		});


		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(mContext, R.string.share_cancel, Toast.LENGTH_SHORT).show();
				sendShareCallBack(SHARE_CACEL);
				dialog.dismiss();
			}
		});

		dialog.show(); 


	}
	
	
	
	
	public void uploadShare(final Oauth2AccessToken accessToken){
		
		final Dialog dialog = new Dialog(mContext,R.style.Dialog_Translucent);
		View view = View.inflate(mContext,R.layout.layout_share_weibo, null);
		dialog.setContentView(view);
		dialog.setCancelable(false);
		ImageView shareImg = (ImageView) dialog.findViewById(R.id.image);
		final EditText shareText = (EditText) dialog.findViewById(R.id.text);
		TextView cancel = (TextView) dialog.findViewById(R.id.share_cancel);
		TextView sure = (TextView) dialog.findViewById(R.id.share_sure);
		
		if(getmBitmap() != null){
			
			shareImg.setImageBitmap(getmBitmap());
		}else{
			return;
		}

		sure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String shareDes = shareText.getText().toString();
				if(TextUtils.isEmpty(shareDes)){
					shareDes = getmDesc();
				}
				
				
				StatusesAPI statusesAPI = new StatusesAPI(mContext, Constants.APP_KEY, accessToken);
				Toast.makeText(mContext, R.string.share_now, Toast.LENGTH_SHORT).show();
				statusesAPI.upload(shareDes, getmBitmap(), "0.0", "0.0", new RequestListener() {

					@Override
					public void onWeiboException(WeiboException arg0) {
						Toast.makeText(mContext, R.string.share_fail, Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onComplete(String arg0) {
						Toast.makeText(mContext, R.string.share_success, Toast.LENGTH_SHORT).show();
					}

                    @Override
                    public void onComplete4binary(ByteArrayOutputStream responseOS) {
                        // TODO Auto-generated method stub
                        
                    }

                    @Override
                    public void onIOException(IOException e) {
                        Toast.makeText(mContext, R.string.share_fail+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(WeiboException e) {
                        Toast.makeText(mContext, R.string.share_fail+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
				});
				dialog.dismiss();
			}
			
		});
			
		 
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(mContext, R.string.share_cancel, Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		});
		
		dialog.show(); 
    }
	
	
	/**	分享回调方法	**/
	public void sendShareCallBack(int share){
		Intent intent = new Intent(BaseActivity_Html5.SHARE_CALLBACK);
		intent.putExtra("isShare", share);
		mContext.sendBroadcast(intent);
	}
	
	
	private void uploadImgUrl(final Oauth2AccessToken accessToken){
		if (mContext == null) {
			return;
		}
		final Dialog dialog = new Dialog(mContext, R.style.Dialog_Translucent);
		View view = View.inflate(mContext.getApplicationContext(),R.layout.layout_share_weibo_webpage, null);
		dialog.setContentView(view);
		dialog.setCancelable(false);
		
		final TextView title = (TextView) view.findViewById(R.id.title);
		ImageView image = (ImageView) view.findViewById(R.id.image);
		TextView cancel = (TextView) view.findViewById(R.id.share_cancel);
		TextView sure = (TextView) view.findViewById(R.id.share_sure);
		if(getmBitmap() != null){
			image.setImageBitmap(getmBitmap());
		}else{
			return;
		}
		if(getmDesc() != null){
			title.setText(getmDesc());
		}
		sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				StatusesAPI statusesAPI = new StatusesAPI(mContext, Constants.APP_KEY, accessToken);
				Toast.makeText(mContext, R.string.share_now, Toast.LENGTH_SHORT).show();
				statusesAPI.upload(title.getText().toString(),getmBitmap(), "0.0", "0.0", new RequestListener() {

					@Override
					public void onWeiboException(WeiboException arg0) {
						try {
							JSONObject jsonObject = new JSONObject(arg0.getMessage());
							if(jsonObject.has("error_code")){
								int code = jsonObject.getInt("error_code");
								if(code == 20019){
									Toast.makeText(mContext, R.string.share_repeat_content, Toast.LENGTH_SHORT).show();
								}else{
									Toast.makeText(mContext, R.string.share_fail, Toast.LENGTH_SHORT).show();
								}
							}
						} catch (Exception e) {
							Toast.makeText(mContext, R.string.share_fail, Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
						sendShareCallBack(SHARE_FAILED);
					}
					@Override
					public void onComplete(String arg0) {
						Toast.makeText(mContext, R.string.share_success, Toast.LENGTH_SHORT).show();
						sendShareCallBack(SHARE_SUCCESS);
					}
                    @Override
                    public void onComplete4binary(ByteArrayOutputStream responseOS) {
                        // TODO Auto-generated method stub
                        
                    }
                    @Override
                    public void onIOException(IOException e) {
                        Toast.makeText(mContext, R.string.share_fail+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(WeiboException e) {
                        Toast.makeText(mContext, R.string.share_fail+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
				});
				dialog.dismiss();
				((Activity)mContext).finish();
			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(mContext, R.string.share_cancel, Toast.LENGTH_SHORT).show();
				sendShareCallBack(SHARE_CACEL);
				dialog.dismiss();
				((Activity)mContext).finish();
			}
		});
		if (mContext != null && !((Activity)mContext).isFinishing()) {
			dialog.show();
		}
	}
	
}