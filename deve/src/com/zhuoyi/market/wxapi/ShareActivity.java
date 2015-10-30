package com.zhuoyi.market.wxapi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseRequest;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboDownloadListener;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboShareException;
import com.sina.weibo.sdk.utils.Utility;
import com.zhuoyi.market.R;
import com.market.account.constant.Constant;
import com.market.account.login.BaseActivity_Html5;
import com.market.account.netutil.JavaScriptOperation;
import com.market.account.utils.BitMapUtils;
import com.market.account.weibosdk.Constants;

public class ShareActivity extends Activity implements IWeiboHandler.Response {

	private GridView gridView;
	private ShareAppsAdapter adapter;
	private PackageManager pm;
	private boolean isWXInstalled = false;
	private boolean isWBInstalled = false;
	private String shareStr;
	private String shareUrl;
	private String appName;
	private Bitmap thumb;
	private byte[] mImageByte;
	/** 微博微博分享接口实例 */
	private IWeiboShareAPI mWeiboShareAPI = null;
	private BaseRequest mBaseRequest = null;
	/** 分享图片 */
	private ImageView mImageView;

	private String mWebUrl;
	private String mDesc;

	public final static int SHARE_REDPACKET = 0;
	public final static int SHARE_ACHIV = 1;

	public final static int SHARE_SUCCESS = 0;
	public final static int SHARE_CACEL = -1;
	public final static int SHARE_FAILED = -2;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// 当 Activity 被重新初始化时（该 Activity 处于后台时，可能会由于内存不足被杀掉了），
		// 需要调用 {@link IWeiboShareAPI#handleWeiboResponse} 来接收微博客户端返回的数据。
		// 执行成功，返回 true，并调用 {@link IWeiboHandler.Response#onResponse}；
		// 失败返回 false，不调用上述回调
		// if (savedInstanceState != null)
		// {

		// 创建微博分享接口实例
		if (ShareToWeibo.mWeiboShareAPI != null) {

			ShareToWeibo.mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
		} else {

			mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this,
					Constants.APP_KEY);
			mWeiboShareAPI.registerApp();
			mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
		}
	}


	/**
	 * @see {@link Activity#onNewIntent}
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
		// 来接收微博客户端返回的数据；执行成功，返回 true，并调用
		// {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
		mWeiboShareAPI.handleWeiboResponse(intent, this);
	}


	/**
	 * 第三方应用发送请求消息到微博，唤起微博分享界面。
	 * 
	 * @see {@link #sendMultiMessage} 或者 {@link #sendSingleMessage}
	 */
	private void sendMessage(boolean hasText, boolean hasImage,
			boolean hasWebpage, boolean hasMusic, boolean hasVideo,
			boolean hasVoice) {

		if (mWeiboShareAPI.isWeiboAppSupportAPI()) {
			int supportApi = mWeiboShareAPI.getWeiboAppSupportAPI();
			if (supportApi >= 10351 /* ApiUtils.BUILD_INT_VER_2_2 */) {
				sendMultiMessage(hasText, hasImage, hasWebpage, hasMusic,
						hasVideo, hasVoice);
			} else {
				sendSingleMessage(hasText, hasImage, hasWebpage, hasMusic,
						hasVideo/* , hasVoice */);
			}
		} else {
			Toast.makeText(this, R.string.weibosdk_demo_not_support_api_hint,
					Toast.LENGTH_SHORT).show();
		}
	}


	/**
	 * 第三方应用发送请求消息到微博，唤起微博分享界面。 注意：当
	 * {@link IWeiboShareAPI#getWeiboAppSupportAPI()} >= 10351 时，支持同时分享多条消息，
	 * 同时可以分享文本、图片以及其它媒体资源（网页、音乐、视频、声音中的一种）。
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
	private void sendMultiMessage(boolean hasText, boolean hasImage,
			boolean hasWebpage, boolean hasMusic, boolean hasVideo,
			boolean hasVoice) {

		// 1. 初始化微博的分享消息
		WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
		if (hasText) {
			weiboMessage.textObject = getTextObj();
		}

		if (hasImage) {
			weiboMessage.imageObject = getImageObj();
		}

		// 用户可以分享其它媒体资源（网页、音乐、视频、声音中的一种）
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
		mWeiboShareAPI.sendRequest(ShareActivity.this,request);
	}


	/**
	 * 第三方应用发送请求消息到微博，唤起微博分享界面。 当{@link IWeiboShareAPI#getWeiboAppSupportAPI()}
	 * < 10351 时，只支持分享单条消息，即 文本、图片、网页、音乐、视频中的一种，不支持Voice消息。
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
	private void sendSingleMessage(boolean hasText, boolean hasImage,
			boolean hasWebpage, boolean hasMusic, boolean hasVideo/*
																 * , boolean
																 * hasVoice
																 */) {

		// 1. 初始化微博的分享消息
		// 用户可以分享文本、图片、网页、音乐、视频中的一种
		WeiboMessage weiboMessage = new WeiboMessage();
		if (hasText) {
			weiboMessage.mediaObject = getTextObj();
		}
		if (hasImage) {
			weiboMessage.mediaObject = getImageObj();
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
		/*
		 * if (hasVoice) { weiboMessage.mediaObject = getVoiceObj(); }
		 */

		// 2. 初始化从第三方到微博的消息请求
		SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
		// 用transaction唯一标识一个请求
		request.transaction = String.valueOf(System.currentTimeMillis());
		request.message = weiboMessage;

		// 3. 发送请求消息到微博，唤起微博分享界面
		mWeiboShareAPI.sendRequest(ShareActivity.this,request);
	}


	/**
	 * 获取分享的文本模板。
	 * 
	 * @return 分享的文本模板
	 */
	private String getSharedText() {
		int formatId = R.string.weibosdk_demo_share_text_template;
		String format = getString(formatId);
		String text = format;
		/*
		 * String demoUrl = getString(R.string.weibosdk_demo_app_url); if
		 * (mTextCheckbox.isChecked() || mImageCheckbox.isChecked()) { format =
		 * getString(R.string.weibosdk_demo_share_text_template); } if
		 * (mShareWebPageView.isChecked()) { format =
		 * getString(R.string.weibosdk_demo_share_webpage_template); text =
		 * String.format(format,
		 * getString(R.string.weibosdk_demo_share_webpage_demo), demoUrl); } if
		 * (mShareMusicView.isChecked()) { format =
		 * getString(R.string.weibosdk_demo_share_music_template); text =
		 * String.format(format,
		 * getString(R.string.weibosdk_demo_share_music_demo), demoUrl); } if
		 * (mShareVideoView.isChecked()) { format =
		 * getString(R.string.weibosdk_demo_share_video_template); text =
		 * String.format(format,
		 * getString(R.string.weibosdk_demo_share_video_demo), demoUrl); } if
		 * (mShareVoiceView.isChecked()) { format =
		 * getString(R.string.weibosdk_demo_share_voice_template); text =
		 * String.format(format,
		 * getString(R.string.weibosdk_demo_share_voice_demo), demoUrl); }
		 */

		return text;
	}


	/**
	 * 创建文本消息对象。
	 * 
	 * @return 文本消息对象。
	 */
	private TextObject getTextObj() {
		TextObject textObject = new TextObject();
		textObject.text = getSharedText();
		return textObject;
	}


	/**
	 * 创建图片消息对象。
	 * 
	 * @return 图片消息对象。
	 */
	private ImageObject getImageObj() {
		ImageObject imageObject = new ImageObject();
		// BitmapDrawable bitmapDrawable = (BitmapDrawable)
		// mImageView.getDrawable();
		Bitmap bmp = BitMapUtils.convertFileToBitmap();
		if (bmp != null) {
			imageObject.setImageObject(bmp);
		}

		return imageObject;
	}


	@SuppressWarnings("unchecked")
	private List<ResolveInfo> getShareableAppsInfo(Intent intent) {
		List<ResolveInfo> resolveInfos = (List<ResolveInfo>) intent.getExtras()
				.get("shareAppList");

		ResolveInfo weibo = null, weixin = null, qq = null;
		for (int i = 0; i < resolveInfos.size(); i++) {
			ResolveInfo resolveInfo = resolveInfos.get(i);
			String name = resolveInfo.activityInfo.name;
			if (name.equals("com.sina.weibo.EditActivity")) {
				weibo = resolveInfo;
				isWBInstalled = true;
				resolveInfos.remove(i--);
			}
			if (name.equals("com.tencent.mm.ui.tools.ShareImgUI")) {
				weixin = resolveInfo;
				isWXInstalled = true;
				resolveInfos.remove(i--);
			}
			if (name.equals("com.tencent.mobileqq.activity.JumpActivity")) {
				qq = resolveInfo;
				resolveInfos.remove(i--);
			}
			if (name.equals("com.tencent.mobileqq.activity.qfileJumpActivity")) {
				resolveInfos.remove(i--);
			}
			if (name.equals("com.zm.aee.wxin.WXTimelineActivity")) {
				resolveInfos.remove(i--);
			}
			if (name.equals("com.qihoo.appstore.activities.MainActivity")) {
				resolveInfos.remove(i--);
			}
		}
		if (qq != null)
			resolveInfos.add(0, qq);
		if (weibo != null)
			// resolveInfos.add(0, weibo);
			if (weixin != null)
				resolveInfos.add(0, weixin);

		return resolveInfos;
	}


	// private void shareToWXTimeLine()
	// {
	// ShareToWeixin shareToWeixin = new ShareToWeixin(ShareActivity.this);
	// if(ShareToWeixin.api.isWXAppInstalled()){
	// shareToWeixin.SharetoWX(true);
	// }else{
	// Toast.makeText(getBaseContext(), R.string.weixin_uninstall,
	// Toast.LENGTH_SHORT).show();
	// }
	// }

	private void shareToSinaWeibo(int type) {
		try {
			// 检查微博客户端环境是否正常，如果未安装微博，弹出对话框询问用户下载微博客户端
			if (mWeiboShareAPI.isWeiboAppSupportAPI()) {

				// 注册第三方应用 到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
				// 但该附件栏集成分享权限需要合作申请，详情请查看 Demo 提示
				mWeiboShareAPI.registerApp();
				switch (type) {
				case SHARE_ACHIV:
					sendMessage(false, true, false, false, false, false);
					break;
				case SHARE_REDPACKET:
					sendMessage(false, false, true, false, false, false);
				default:
					break;
				}
			} else {
				Toast.makeText(getBaseContext(),
						R.string.weibosdk_demo_toast_share_uninstall,
						Toast.LENGTH_SHORT).show();
			}
		} catch (WeiboShareException e) {
			e.printStackTrace();
			Toast.makeText(ShareActivity.this, e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}


	private void shareToApp(ResolveInfo resolveInfo) {
		if (resolveInfo != null) {
			ComponentName cp = new ComponentName(
					resolveInfo.activityInfo.packageName,
					resolveInfo.activityInfo.name);
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setComponent(cp);
			// intent.setType("text/plain");
			// intent.putExtra(Intent.EXTRA_SUBJECT, R.string.software_share);
			// intent.putExtra(Intent.EXTRA_TEXT, shareStr);

			intent.setType("image/*");
			File file = new File(BitMapUtils.getScreenShot());
			Uri uri = Uri.fromFile(file);
			intent.putExtra(Intent.EXTRA_STREAM, uri);

			this.startActivity(intent);
		}
	}


	private String getAppName() {
		String appName = "";
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(
					this.getPackageName(), 0);
			appName = pi.applicationInfo.loadLabel(getPackageManager())
					.toString();
			if (appName == null || appName.length() <= 0) {
				return "";
			}
		} catch (Exception e) {

		}
		return appName;
	}


	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis())
				: type + System.currentTimeMillis();
	}


	private byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] result = null;

		if (bmp == null)
			return null;

		try {
			bmp.compress(CompressFormat.PNG, 100, output);
			if (needRecycle) {
				bmp.recycle();
			}
			result = output.toByteArray();

		} catch (Exception e) {

		}

		try {
			if (output != null)
				output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	class ShareAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return 0;
		}


		@Override
		public Object getItem(int position) {
			return null;
		}


		@Override
		public long getItemId(int position) {
			return 0;
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return null;
		}

	}

	class ShareAppsAdapter extends BaseAdapter {

		private List<ResolveInfo> resolveInfos;
		private int addition = 0;
		private LayoutInflater inflater;


		public ShareAppsAdapter() {
			inflater = LayoutInflater.from(ShareActivity.this);
			this.resolveInfos = resolveInfos;
		}


		@Override
		public int getCount() {
			return 2;
			// return isWXInstalled? (resolveInfos.size()+addition) :
			// resolveInfos.size();
		}


		@Override
		public Object getItem(int position) {
			return resolveInfos.get(position);
		}


		@Override
		public long getItemId(int position) {
			return position;
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = inflater.inflate(R.layout.layout_share_item, null);
			ImageView appIcon = (ImageView) convertView
					.findViewById(R.id.share_app_icon);
			TextView appName = (TextView) convertView
					.findViewById(R.id.share_app_name);
			if (position == 0) {
				appIcon.setImageResource(R.drawable.wx_timeline);
				appName.setText(R.string.time_line);
			} else if (position == 1) {
				appIcon.setImageResource(R.drawable.sina_icon);
				appName.setText(R.string.sina_weibo);
			}
			// else if (!isWXInstalled && !isWBInstalled)
			// {
			// showSysShareApps(appIcon, appName, position);
			// }
			// else
			// {
			// showSysShareApps(appIcon, appName, position - 1);
			// }
			return convertView;
		}


		private void showSysShareApps(ImageView appIcon, TextView appName,
				int position) {
			ResolveInfo resolveInfo = resolveInfos.get(position);
			appIcon.setImageDrawable(resolveInfo.loadIcon(pm));
			String name = resolveInfo.activityInfo.name;
			if (name.equals("com.sina.weibo.EditActivity")) {
				appName.setText(R.string.sina_weibo);
			} else if (name.equals("com.tencent.mm.ui.tools.ShareImgUI")) {
				appName.setText(R.string.weixin);
			} else if (name
					.equals("com.tencent.mobileqq.activity.JumpActivity")) {
				appName.setText(R.string.qq);
			} else {
				appName.setText(resolveInfo.loadLabel(pm));
			}

		}

	}


	// class MyOnItemClickListener implements OnItemClickListener
	// {
	// private List<ResolveInfo> resolveInfos;

	// public MyOnItemClickListener()
	// {
	// this.resolveInfos = resolveInfos;
	// }
	//
	// @Override
	// public void onItemClick(AdapterView<?> arg0, View arg1, int position,
	// long arg3)
	// {
	// if (position == 0)
	// {
	// shareToWXTimeLine();
	// finish();
	// }
	// else if (position == 1)
	// {
	// shareToSinaWeibo();
	// }
	// else if (!isWXInstalled && !isWBInstalled)
	// {
	// shareToApp(resolveInfos.get(position));
	// }
	// else
	// {
	// shareToApp(resolveInfos.get(position - 1));
	// }
	// }

	// }

	/**
	 * 接收微客户端博请求的数据。 当微博客户端唤起当前应用并进行分享时，该方法被调用。
	 * 
	 * @param baseRequest
	 *            微博请求数据对象
	 * @see {@link IWeiboShareAPI#handleWeiboRequest}
	 */
	/** weibo response **/
	@Override
	public void onResponse(BaseResponse baseResp) {
		switch (baseResp.errCode) {
		case WBConstants.ErrorCode.ERR_OK:
			Toast.makeText(this, R.string.share_success, Toast.LENGTH_SHORT)
					.show();
			sendShareCallBack(SHARE_SUCCESS);
			break;
		case WBConstants.ErrorCode.ERR_CANCEL:
			Toast.makeText(this, R.string.share_cancel, Toast.LENGTH_SHORT)
					.show();
			sendShareCallBack(SHARE_CACEL);
			break;
		case WBConstants.ErrorCode.ERR_FAIL:
			Toast.makeText(this, R.string.share_fail, Toast.LENGTH_SHORT)
					.show();
			sendShareCallBack(SHARE_FAILED);
			break;
		default:
			Toast.makeText(this, R.string.share_fail, Toast.LENGTH_SHORT)
					.show();
			sendShareCallBack(SHARE_FAILED);
			break;
		}
		finish();
	}


	public void sendShareCallBack(int share) {
		Intent intent = new Intent(BaseActivity_Html5.SHARE_CALLBACK);
		intent.putExtra("isShare", share);
		sendBroadcast(intent);
	}


	/**
	 * 创建多媒体（网页）消息对象。
	 * 
	 * @return 多媒体（网页）消息对象。
	 */
	private WebpageObject getWebpageObj() {
		WebpageObject mediaObject = new WebpageObject();
		mediaObject.identify = Utility.generateGUID();
		mediaObject.title = mDesc;
		mediaObject.description = mDesc;
		// 设置 Bitmap 类型的图片到视频对象里
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_login_account);
		mediaObject.thumbData = Bitmap2Bytes(bitmap);
		mediaObject.setThumbImage(BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_login_account));
		mediaObject.actionUrl = mWebUrl;
		mediaObject.defaultText = mDesc;
		return mediaObject;
	}


	public byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.finish();
		return super.onTouchEvent(event);
	}

	/**
	 * 创建多媒体（音乐）消息对象。
	 * 
	 * @return 多媒体（音乐）消息对象。
	 */
	// private MusicObject getMusicObj() {
	// // 创建媒体消息
	// MusicObject musicObject = new MusicObject();
	// musicObject.identify = Utility.generateGUID();
	// musicObject.title = mShareMusicView.getTitle();
	// musicObject.description = mShareMusicView.getShareDesc();
	//
	// // 设置 Bitmap 类型的图片到视频对象里
	// musicObject.setThumbImage(mShareMusicView.getThumbBitmap());
	// musicObject.actionUrl = mShareMusicView.getShareUrl();
	// musicObject.dataUrl = "www.weibo.com";
	// musicObject.dataHdUrl = "www.weibo.com";
	// musicObject.duration = 10;
	// musicObject.defaultText = "Music 默认文案";
	// return musicObject;
	// }

	/**
	 * 创建多媒体（视频）消息对象。
	 * 
	 * @return 多媒体（视频）消息对象。
	 */
	// private VideoObject getVideoObj() {
	// // 创建媒体消息
	// VideoObject videoObject = new VideoObject();
	// videoObject.identify = Utility.generateGUID();
	// videoObject.title = mShareVideoView.getTitle();
	// videoObject.description = mShareVideoView.getShareDesc();
	//
	// // 设置 Bitmap 类型的图片到视频对象里
	// videoObject.setThumbImage(mShareVideoView.getThumbBitmap());
	// videoObject.actionUrl = mShareVideoView.getShareUrl();
	// videoObject.dataUrl = "www.weibo.com";
	// videoObject.dataHdUrl = "www.weibo.com";
	// videoObject.duration = 10;
	// videoObject.defaultText = "Vedio 默认文案";
	// return videoObject;
	// }

	/**
	 * 创建多媒体（音频）消息对象。
	 * 
	 * @return 多媒体（音乐）消息对象。
	 */
	// private VoiceObject getVoiceObj() {
	// // 创建媒体消息
	// VoiceObject voiceObject = new VoiceObject();
	// voiceObject.identify = Utility.generateGUID();
	// voiceObject.title = mShareVoiceView.getTitle();
	// voiceObject.description = mShareVoiceView.getShareDesc();
	//
	// // 设置 Bitmap 类型的图片到视频对象里
	// voiceObject.setThumbImage(mShareVoiceView.getThumbBitmap());
	// voiceObject.actionUrl = mShareVoiceView.getShareUrl();
	// voiceObject.dataUrl = "www.weibo.com";
	// voiceObject.dataHdUrl = "www.weibo.com";
	// voiceObject.duration = 10;
	// voiceObject.defaultText = "Voice 默认文案";
	// return voiceObject;
	// }
}