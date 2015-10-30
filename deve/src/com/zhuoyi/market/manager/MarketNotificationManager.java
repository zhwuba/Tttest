package com.zhuoyi.market.manager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.zhuoyi.market.R;
import com.zhuoyi.market.Splash;
import com.zhuoyi.market.appManage.update.MarketUpdateActivity;
import com.zhuoyi.market.appManage.update.StartUpdateActivity;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.cleanTrash.TrashActivity;
import com.zhuoyi.market.utils.gallery.BitmapUtiles;

public class MarketNotificationManager
{
	public static final int MAX_UPDATE_SHOWICON_NUM = 5;

	public static final int NOTIFY_ID_RECEIVER_GIFT = 0x1000;
	public static final int NOTIFY_ID_APP_UPDATE = 0x1001;
	public static final int NOTIFY_ID_WIFI_HAS_UPDATE = 0x1002;
	public static final int NOTIFY_ID_WIFI_HAS_DOWNLOAD = 0x1003;
	public static final int NOTIFY_ID_DOWNLOADING = 0x1004;
	public static final int NOTIFY_ID_INSTALL_SUCCESS_1 = 0x1005;
	public static final int NOTIFY_ID_INSTALL_SUCCESS_2 = 0x1006;
	public static final int NOTIFY_ID_TRASH_CLEAN = 0x1007;
	
	public static int mNotifyInstallSuccess = NOTIFY_ID_INSTALL_SUCCESS_1;

	public static final String NOTIFICATION_BIGCONTENTVIEW_NAME = "bigContentView";
	public static final int Build_VERSION_CODES_ICE_CREAM_SANDWICH= 14;
	public static final int Build_VERSION_CODES_JELLY_BEAN = 16;
	public static final int Build_VERSION_CODES_LOLLIPOP = 21;

	private static MarketNotificationManager instance;
	private NotificationManager mNotificationManager;
	private int[] ivUpdateIconId;
	private Context mContext;

	public static synchronized MarketNotificationManager get()
	{
		if (instance == null)
		{
			instance = new MarketNotificationManager();
		}
		return instance;
	}

	private MarketNotificationManager()
	{
		mContext = MarketApplication.getRootContext();
		ivUpdateIconId = new int[] { R.id.updateIcon1, R.id.updateIcon2, R.id.updateIcon3, R.id.updateIcon4,
				R.id.updateIcon5 };
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void cancel(int notifyId)
	{
		mNotificationManager.cancel(notifyId);
	}

	public void cancelAll()
	{
		mNotificationManager.cancelAll();
	}

	public void notify(int notifyId, Notification notification)
	{
		mNotificationManager.cancel(notifyId);
		mNotificationManager.notify(notifyId, notification);
	}

	/**
	 * 推荐单个应用更新
	 * 
	 * @param title
	 * @param tickerText
	 * @param content
	 */
	public void notifyUpdateByApp(Drawable drawable, String title, String tickerText, String content)
	{

		Intent intent = new Intent(mContext, MarketUpdateActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		notifyCommon(drawable, title, tickerText, content, PendingIntent.getActivity(mContext, 0, intent, 0), -1,
				Notification.FLAG_AUTO_CANCEL);
	}

	public void notifyCommon(String title, String tickerText, String content, PendingIntent pIntent, int notifyId,
			int flag)
	{
		try {
			notifyCommon(mContext.getResources().getDrawable(R.drawable.icon), title, tickerText, content, pIntent,
					notifyId, flag);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void notifyCommon(Drawable drawable, String title, String tickerText, String content, PendingIntent pIntent,
			int notifyId, int flag)
	{
		RemoteViews mNromalContentView = new RemoteViews(mContext.getPackageName(), getNormalLayout());
		mNromalContentView.setImageViewBitmap(R.id.ivIcon, BitmapUtiles.drawable2Bitmap(drawable));
		mNromalContentView.setTextViewText(R.id.tvTitle, title);
		mNromalContentView.setTextViewText(R.id.tvDescript, content);
		mNromalContentView.setLong(R.id.time, "setTime", System.currentTimeMillis());
		notifyAdapterKoobee(mNromalContentView, R.id.kubiPlace);
		setTextColor(mNromalContentView, R.id.tvTitle, MarketNotificationHelper.get().getTitleColor());
		setTextColor(mNromalContentView, R.id.tvDescript, MarketNotificationHelper.get().getTextColor());
		setTextColor(mNromalContentView, R.id.time, MarketNotificationHelper.get().getTextColor());

		Notification notification = null;
		if (TextUtils.isEmpty(tickerText))
		{
			notification = new NotificationCompat.Builder(mContext).setSmallIcon(R.drawable.icon_notify)
					.setContentTitle(title).setContentText(content).setWhen(System.currentTimeMillis())
					.setContent(mNromalContentView).setContentIntent(pIntent).build();
		}
		else
		{
			notification = new NotificationCompat.Builder(mContext).setSmallIcon(R.drawable.icon_notify)
					.setTicker(tickerText).setContentTitle(title).setContentText(content)
					.setWhen(System.currentTimeMillis()).setContent(mNromalContentView).setContentIntent(pIntent)
					.build();
		}

		notification.flags = flag;
		notify(notifyId, notification);
	}

	public void notifyReceiveGift()
	{
		Intent intent = new Intent(mContext, Splash.class);

		String title = mContext.getString(R.string.notify_receiver_gift);
		String des = mContext.getString(R.string.notify_click_to_read);
		RemoteViews mNromalContentView = new RemoteViews(mContext.getPackageName(), getNormalLayout());
		mNromalContentView.setImageViewResource(R.id.ivIcon, R.drawable.icon);
		mNromalContentView.setTextViewText(R.id.tvTitle, title);
		mNromalContentView.setTextViewText(R.id.tvDescript, des);
		notifyAdapterKoobee(mNromalContentView, R.id.kubiPlace);
		setTextColor(mNromalContentView, R.id.tvTitle, MarketNotificationHelper.get().getTitleColor());
		setTextColor(mNromalContentView, R.id.tvDescript, MarketNotificationHelper.get().getTextColor());

		Notification notification = new NotificationCompat.Builder(mContext).setSmallIcon(R.drawable.icon_notify)
				.setContentTitle(title).setContentText(des).setContent(mNromalContentView)
				.setContentIntent(PendingIntent.getActivity(mContext, 0, intent, 0)).build();

		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notify(NOTIFY_ID_RECEIVER_GIFT, notification);
	}

	/**
	 * 
	 * Pay attention : The best size of list bitmap is no more than {@value MarketNotificationManager #MAX_UPDATE_SHOWICON_NUM}.
	 * 
	 */
	public void notifyAppUpdate(List<Bitmap> listUpdateIcon, int updateAppNum)
	{
		if (listUpdateIcon != null && listUpdateIcon.size() > 0)
		{
			RemoteViews mContentView = new RemoteViews(mContext.getPackageName(), getNotificationWithIconLayout());
			int size = listUpdateIcon.size();
			for (int i = 0; i < MAX_UPDATE_SHOWICON_NUM; i++)
			{
				if (i < size)
				{
					mContentView.setViewVisibility(ivUpdateIconId[i], View.VISIBLE);
					mContentView.setImageViewBitmap(ivUpdateIconId[i], listUpdateIcon.get(i));
				}
				else
				{
					mContentView.setViewVisibility(ivUpdateIconId[i], View.GONE);
				}
			}

			mContentView.setTextViewText(R.id.notifyTitle,
					String.format(mContext.getString(R.string.notify_update_app_num), updateAppNum));
			int des = updateAppNum > MAX_UPDATE_SHOWICON_NUM ? R.string.notify_has_above_five_app_update
					: R.string.notify_has_app_update;
			mContentView.setTextViewText(R.id.updateDescript, mContext.getString(des));
			notifyAdapterKoobee(mContentView, R.id.kubiPlace);
			setTextColor(mContentView, R.id.notifyTitle, MarketNotificationHelper.get().getTitleColor());
			setTextColor(mContentView, R.id.updateDescript, MarketNotificationHelper.get().getTextColor());

			Intent intent2 = new Intent(mContext, StartUpdateActivity.class);
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContentView.setOnClickPendingIntent(R.id.update_all_btn,
					PendingIntent.getActivity(mContext, 0, intent2, 0));

			Intent intent = new Intent(mContext, MarketUpdateActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			Notification notification = new NotificationCompat.Builder(mContext).setContent(mContentView)
					.setContentTitle(String.format(mContext.getString(R.string.notify_update_app_num), updateAppNum))
					.setContentText(mContext.getString(R.string.notify_click_to_read))
					.setContentIntent(PendingIntent.getActivity(mContext, 0, intent, 0))
					.setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.update_notify).build();

			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notify(NOTIFY_ID_APP_UPDATE, notification);
		}
	}

	/**
	 * 自更新状态栏提醒
	 * 
	 * @param versionCode
	 * @param title
	 * @param descript
	 * @param btnStr
	 * @param intent
	 */
	public void notifyWifiUpdte(String versionCode, String title, String descript, String btnStr, Intent intent)
	{
		String version = mContext.getString(R.string.notify_wifi_update_version) + versionCode;

		if (TextUtils.isEmpty(title))
		{
			title = mContext.getString(R.string.notify_wifi_update_title);
		}

		RemoteViews mNromalContentView = new RemoteViews(mContext.getPackageName(), getNormalLayout());
		mNromalContentView.setImageViewResource(R.id.ivIcon, R.drawable.icon);
		mNromalContentView.setTextViewText(R.id.tvTitle, title);
		mNromalContentView.setTextViewText(R.id.tvDescript, version);
		notifyAdapterKoobee(mNromalContentView, R.id.kubiPlace);
		setTextColor(mNromalContentView, R.id.tvTitle, MarketNotificationHelper.get().getTitleColor());
		setTextColor(mNromalContentView, R.id.tvDescript, MarketNotificationHelper.get().getTextColor());

		Notification notification = new NotificationCompat.Builder(mContext).setContentText(version)
				.setWhen(System.currentTimeMillis()).setContent(mNromalContentView).setContentTitle(title)
				.setSmallIcon(R.drawable.icon_notify).setPriority(NotificationCompat.PRIORITY_MAX)
				.setContentIntent(PendingIntent.getActivity(mContext, 0, intent, 0)).build();

		if (Build.VERSION.SDK_INT >= Build_VERSION_CODES_JELLY_BEAN)
		{
			RemoteViews mBigContentView = new RemoteViews(mContext.getPackageName(), R.layout.notification_wifi_update);
			mBigContentView.setTextViewText(R.id.tvTitle, title);
			mBigContentView.setTextViewText(R.id.tvVersion, version);
			mBigContentView.setTextViewText(R.id.tvDescript, dealWithWifiUpdateDes(descript));
			mBigContentView.setTextViewText(R.id.tvNotification, btnStr);
			notifyAdapterKoobee(mBigContentView, R.id.kubiPlace);
			setTextColor(mNromalContentView, R.id.tvTitle, MarketNotificationHelper.get().getTitleColor());
			setTextColor(mNromalContentView, R.id.tvDescript, MarketNotificationHelper.get().getTextColor());
			setTextColor(mNromalContentView, R.id.tvVersion, MarketNotificationHelper.get().getTextColor());
			reflectiBigContent(notification, mBigContentView);
		}

		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notify(NOTIFY_ID_WIFI_HAS_UPDATE, notification);
	}
	
	
	private void notifyButtonView(String title, String tip, String buttonText, Drawable drawable, int smallIcon, int notifyId, PendingIntent pIntent) {
	    RemoteViews mContentView = new RemoteViews(mContext.getPackageName(), getButtonViewLayout());
	    if (title != null) {
	        mContentView.setTextViewText(R.id.tvTitle, title);
	    }
        mContentView.setTextViewText(R.id.tvTip, tip);
        mContentView.setTextViewText(R.id.tvOpen, buttonText);
        mContentView.setImageViewBitmap(R.id.notifyImage, BitmapUtiles.drawable2Bitmap(drawable));
        notifyAdapterKoobee(mContentView, R.id.kubiPlace);
        setTextColor(mContentView, R.id.tvTitle, MarketNotificationHelper.get().getTitleColor());
        setTextColor(mContentView, R.id.tvTip, MarketNotificationHelper.get().getTextColor());
        
        String ticker = (title == null ? "" : title) + tip;

        Notification notification = null;
        if (Build.VERSION.SDK_INT < Build_VERSION_CODES_ICE_CREAM_SANDWICH) {
        	notification = new NotificationCompat.Builder(mContext)
        		.setWhen(System.currentTimeMillis())
        		.setContentIntent(pIntent)
        		.setSmallIcon(smallIcon)
        		.setTicker(ticker).build();
        } else {
        	mContentView.setOnClickPendingIntent(R.id.tvOpen, pIntent);
        	notification = new NotificationCompat.Builder(mContext)
            	.setWhen(System.currentTimeMillis())
            	.setSmallIcon(smallIcon)
            	.setTicker(ticker).build();
        }
        
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.contentView = mContentView;
        notify(notifyId,notification);
        
	}
	
	
	/**
	 * 包安装成功后提示
	 * @param title application name
	 * @param pkgName package name
	 * @param iconBitmap package icon
	 */
	public void notifyInstallSuccess(String title, String pkgName, Drawable iconBitmap) {
	    String tip = mContext.getString(R.string.notify_tip_install_success);
        String buttonText = mContext.getString(R.string.open);
	    
        Intent intent = new Intent();
        intent.setAction("com.zhuoyi.market.installed");
        intent.putExtra("pkg_name", pkgName);
        intent.putExtra("notify_id", mNotifyInstallSuccess);
        PendingIntent pIntent = PendingIntent.getBroadcast(mContext, mNotifyInstallSuccess, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        if (iconBitmap == null) {
        	iconBitmap = mContext.getResources().getDrawable(R.drawable.icon);
        }
        notifyButtonView(title, tip, buttonText, iconBitmap, R.drawable.icon_notify, mNotifyInstallSuccess, pIntent);
        
        if (NOTIFY_ID_INSTALL_SUCCESS_1 == mNotifyInstallSuccess) {
            mNotifyInstallSuccess = NOTIFY_ID_INSTALL_SUCCESS_2;
        } else {
            mNotifyInstallSuccess = NOTIFY_ID_INSTALL_SUCCESS_1;
        }

	}
	
	
	public boolean notifyTrashClean(long trashSize, int availProgress) {
	    String tip = null;
	    if (availProgress < 30) {
	        tip = mContext.getString(R.string.trash_notify_space_not_enough);
	    } else {
	        long mbSize = trashSize / (1024 * 1024);
	        if (mbSize <= 50) {
	            return false;
	        }
	        String sizeStr = null;
	        if (mbSize > 1024) {
	            float gbSize = ((float)(mbSize * 100 / 1024)) / 100;
	            sizeStr = Float.toString(gbSize) + "G";
	        } else {
	            sizeStr = Long.toString(mbSize) + "M";
	        }
	        tip = mContext.getString(R.string.trash_notify_trash_size, sizeStr);
	    }
	    String buttonText = mContext.getString(R.string.trash_notify_button_text);
	    
	    Intent intent = new Intent(mContext, TrashActivity.class);
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    
	    notifyButtonView(null, tip, buttonText, mContext.getResources().getDrawable(R.drawable.trash_icon), R.drawable.trash_notify, NOTIFY_ID_TRASH_CLEAN, pIntent);
	    return true;
	}
	
	
	public void cancelTrashCleanNotify() {
	    mNotificationManager.cancel(NOTIFY_ID_TRASH_CLEAN);
	}
	
	

	private void setTextColor(RemoteViews view, int id, Integer color)
	{
		if (color != null && Build.VERSION.SDK_INT < Build_VERSION_CODES_LOLLIPOP)
		{
			view.setTextColor(id, color);
		}
	}
	
	
    private int getButtonViewLayout() {
        int layoutId = -1;
        if (isTydMobile() && Build.VERSION.SDK_INT >= Build_VERSION_CODES_LOLLIPOP) {
            layoutId = R.layout.notification_button_view_koobee_l;
        } else {
            layoutId = R.layout.notification_button_view;
        }
        return layoutId;
    }
	

	private int getNormalLayout()
	{
		int layoutId = -1;

		if (isTydMobile() && Build.VERSION.SDK_INT >= Build_VERSION_CODES_LOLLIPOP)
		{
			layoutId = R.layout.notification_normal_koobee_l;
		}
		else
		{
			layoutId = R.layout.notification_normal;
		}
		return layoutId;
	}

	private int getNotificationWithIconLayout()
	{
		int layoutId = -1;

		if (isTydMobile() && Build.VERSION.SDK_INT >= Build_VERSION_CODES_LOLLIPOP)
		{
			layoutId = R.layout.notification_update_with_icon_kobee_l;
		}
		else
		{
			layoutId = R.layout.notification_update_with_icon;
		}
		return layoutId;
	}

	private void notifyAdapterKoobee(RemoteViews view, int id)
	{
		
		if (isTydMobile() && Build.VERSION.SDK_INT < Build_VERSION_CODES_LOLLIPOP)
		{
			view.setViewVisibility(id, View.GONE);
		}
	}
	
	public boolean isTydMobile()
	{
		return !getOsName().equals("");
	}

	private String getOsName()
	{
		String osName = "";
		String osVer = "";
		try
		{
			Class<?> classType = Class.forName("android.os.SystemProperties");
			Method getMethod = classType.getDeclaredMethod("get", String.class);
			osName = (String) getMethod.invoke(classType, "ro.build.freemeos_label");
			osVer = (String) getMethod.invoke(classType, "ro.build.version.freemeos");
		}
		catch (Exception e)
		{
		}
		return osName + osVer;
	}

	/**
	 * 
	 * This method is compatible with the sdk version above {@value MarketNotificationManager#Build_VERSION_CODES_JELLY_BEAN}
	 * 
	 */
	private void reflectiBigContent(Notification notification, RemoteViews view)
	{
		try
		{
			Field localField = notification.getClass().getDeclaredField(NOTIFICATION_BIGCONTENTVIEW_NAME);
			if (localField != null)
			{
				localField.setAccessible(true);
				localField.set(notification, view);
			}

		}
		catch (Exception e)
		{
		}
	}

	private String dealWithWifiUpdateDes(String descript)
	{
		// TODO:
		return descript;
	}

}
