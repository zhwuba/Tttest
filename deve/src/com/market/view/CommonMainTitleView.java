package com.market.view;

import java.io.File;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.market.account.authenticator.AuthenticatorActivity;
import com.market.account.constant.Constant;
import com.market.account.dao.UserInfo;
import com.market.account.utils.PropertyFileUtils;
import com.market.behaviorLog.LogDefined;
import com.market.behaviorLog.UserLogSDK;
import com.market.net.data.HotSearchInfoBto;
import com.zhuoyi.market.R;
import com.zhuoyi.market.mine.MarketMineActivity;
import com.zhuoyi.market.search.SearchActivity;
import com.zhuoyi.market.search.SearchUtils;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;

/**
 * 主标题
 * 
 * @author dream.zhou
 */
public class CommonMainTitleView extends LinearLayout {
	
	private final String LOGO_FILE_PATH = PropertyFileUtils.getSDPath() + Constant.USERLOGO_PATH;
	private Drawable mUserLogogroudDrawable;
	private Bitmap mLogoBitmap = null;
	private Animation mAnimation;
	private boolean isStillScale = false;

    private Context mContext = null;
    private View mMainView = null;
    private ImageView mImageView_title_logo = null;

    private LinearLayout mSearchBar = null;
    private AnimTextView mAnimHotWords = null;
    /**
     * 热搜词数据
     */
    private List<HotSearchInfoBto> mHotSearchInfoList = null;
    private ImageView mSearchBtn = null;

    private CommonTitleDownloadView mDownloadView = null;

    private int mHotWordCursor = -1;
    private final int HANDLER_SEARCH_HOT_WORD = 1000;
    private final int HANDLER_UPDATE_LOGO = 1001;
    private final int HANDLER_STILL_SCALE = 1002;

    private static boolean mNeedHotWordChange = true;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
            case HANDLER_SEARCH_HOT_WORD:

                if (mNeedHotWordChange) {
                    try {
                        mAnimHotWords.next();
                        mAnimHotWords.setText(mHotSearchInfoList.get(++mHotWordCursor).getText());
                    } catch (IndexOutOfBoundsException e) {
                        mHotWordCursor = 0;
                        mAnimHotWords.setText(mHotSearchInfoList.get(mHotWordCursor).getText());
                    }
                }

                if (mHandler != null)
                    mHandler.sendEmptyMessageDelayed(HANDLER_SEARCH_HOT_WORD, 5000);
                break;
            case HANDLER_UPDATE_LOGO:
            	setLogo();
            	break;
            case HANDLER_STILL_SCALE:
            	if(!isStillScale)
            		return;
            	mImageView_title_logo.clearAnimation();
            	mImageView_title_logo.startAnimation(mAnimation);
            	
            	if(mHandler != null)
            		mHandler.sendEmptyMessageDelayed(HANDLER_STILL_SCALE, 6000);
            	break;
            }
        }

    };


    public CommonMainTitleView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        initView(context);
    }


    public CommonMainTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        initView(context);
    }


    public CommonMainTitleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        initView(context);
    }


    private void initView(Context context) {
        mContext = context;
        setOrientation(VERTICAL);
        MarketUtils.setTitleLayout(this, mContext);
        
        LayoutInflater tLayoutInflater = LayoutInflater.from(mContext);
        mMainView = tLayoutInflater.inflate(R.layout.common_main_title_view, this, true);

        mSearchBar = (LinearLayout) mMainView.findViewById(R.id.home_search_bar);
        mSearchBar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 点击搜索框事件
                Intent intent = new Intent(mContext, SearchActivity.class);
                
                if (mHotSearchInfoList != null) {
                    if (mHotWordCursor >= 0 && mHotWordCursor < mHotSearchInfoList.size()){
                        HotSearchInfoBto hotWord = mHotSearchInfoList.get(mHotWordCursor);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("main_search_hint", hotWord);
                        intent.putExtras(bundle);
                    }
                }
                mContext.startActivity(intent);
            }
        });
        mSearchBtn = (ImageView) mMainView.findViewById(R.id.home_search_btn);
        mSearchBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mHotSearchInfoList == null) {
                    return;
                }
                int size = mHotSearchInfoList.size();
                if (mHotWordCursor < 0 || mHotWordCursor >= size)
                    return;
                HotSearchInfoBto hotWord = mHotSearchInfoList.get(mHotWordCursor);
                SearchUtils.doClickHotWord(mContext,hotWord);
                
                //for user behavior log
                UserLogSDK.logCountEvent(mContext, UserLogSDK.getSearchWordDes(LogDefined.COUNT_SCROLL_WORD_CLICK, hotWord.getText()));
            }
        });
        mDownloadView = (CommonTitleDownloadView) mMainView.findViewById(R.id.main_title_download);
        mAnimHotWords = (AnimTextView) mMainView.findViewById(R.id.home_search_animwords);
        
        //设置左上角图片
        mImageView_title_logo = (ImageView) mMainView.findViewById(R.id.main_title_logo);
        mImageView_title_logo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mContext.startActivity(new Intent(mContext, MarketMineActivity.class));
			}
		});
    }
    
    public void setMainBackgroundColor(int color)
    {
    	mMainView.setBackgroundColor(color);
    }
    
    public void setTitleLogo() {
    	mImageView_title_logo.clearAnimation();
    	if(UserInfo.getUserIsLogin(mContext)) {
    		isStillScale = false;
    		JSONObject userObject = UserInfo.getUserInfo(mContext);
			if (new File(LOGO_FILE_PATH).exists()) {
				setLogo();
			} else {
				if(mUserLogogroudDrawable == null){
					mUserLogogroudDrawable = mContext.getResources().getDrawable(R.drawable.usercenter_logo);
				}
				mImageView_title_logo.setBackgroundDrawable(mUserLogogroudDrawable);
				if (userObject.has("logoUrl")) {
					try {
						final String logoUrl = userObject.getString("logoUrl");
						if (!TextUtils.isEmpty(logoUrl)) {
							new Thread(new Runnable() {

								@Override
								public void run() {
									AuthenticatorActivity.downloadUserLogo(logoUrl);
									if (mHandler != null) {
										mHandler.sendEmptyMessage(HANDLER_UPDATE_LOGO);
									}
								}
							}).start();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
    	} else { //未登录
    		isStillScale = true;
    		mImageView_title_logo.setBackgroundResource(R.drawable.scale_logo_pressed);
    		mAnimation = AnimationUtils.loadAnimation(mContext, R.anim.icon_scale);
            mHandler.sendEmptyMessage(HANDLER_STILL_SCALE);
    	}
    }
    
    private int getDisplayMetrics() {
    	DisplayMetrics metric = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;
        return width;
    }
    
    
    private void setLogo() {
    	if(mLogoBitmap != null && !mLogoBitmap.isRecycled()) {
    		mLogoBitmap.recycle();
    		mLogoBitmap = null;
        }
    	
    	try {
    		Bitmap bitmap = BitmapFactory.decodeFile(LOGO_FILE_PATH);
			if(bitmap != null) {
				mLogoBitmap = AsyncImageCache.getCroppedRoundBitmap(bitmap, ((int)getResources().getDimension(R.dimen.title_logo_width)));
				bitmap.recycle();
				int width = 0;
				if(getDisplayMetrics() < 720)
					width = 2;
				else
					width = 3;
				mImageView_title_logo.setBackgroundDrawable(new BitmapDrawable(AsyncImageCache.addBorder(mLogoBitmap, width)));
			}
		} catch(OutOfMemoryError e) {
			System.gc();
			e.printStackTrace();
		}
    }
    
    
    // 热词代码区域start
    public void startHotWordsRolling(List<HotSearchInfoBto> hotSearchList) {
        if (hotSearchList == null || hotSearchList.size() == 0)
            return;
        mHotSearchInfoList = hotSearchList;
        if (mHandler != null)
            mHandler.sendEmptyMessageDelayed(HANDLER_SEARCH_HOT_WORD, 0);
    }


    public static void setNeedHotWordChange(boolean need) {
        if (mNeedHotWordChange != need)
            mNeedHotWordChange = need;
    }
    // 热词代码区域end

    // 下载代码区域start
    public int[] getDownloadLocation() {
        int[] location = new int[2];
        mDownloadView.getLocationInWindow(location);
        return location;
    }


    public int getDownloadWidth() {
        return mDownloadView.getWidth();
    }


    public int getDownloadHeight() {
        return mDownloadView.getHeight();
    }


    public void registeredReceiver() {
        mDownloadView.registeredReceiver();
    }


    public void unRegisteredReceiver() {
        mDownloadView.unRegisteredReceiver();
    }


    public void setDownloadStatus() {
        mDownloadView.setDownloadStatus();
    }


    // 下载代码区域end

    public void releaseRes() {
        mContext = null;
        if (mHandler != null) {
        	if(mHandler.hasMessages(HANDLER_SEARCH_HOT_WORD))
        		mHandler.removeMessages(HANDLER_SEARCH_HOT_WORD);
        	if(mHandler.hasMessages(HANDLER_STILL_SCALE))
        		mHandler.removeMessages(HANDLER_STILL_SCALE);
        }
        mHandler = null;

        if (mHotSearchInfoList != null)
            mHotSearchInfoList.clear();
        mHotSearchInfoList = null;

        if (mAnimHotWords != null) {
            mAnimHotWords.releaseRes();
            mAnimHotWords = null;
        }
        
        if(mLogoBitmap != null && !mLogoBitmap.isRecycled()) {
    		mLogoBitmap.recycle();
    		mLogoBitmap = null;
		}
		if(mUserLogogroudDrawable != null) {
			mUserLogogroudDrawable.setCallback(null);
			mUserLogogroudDrawable = null;
		}
		mImageView_title_logo.clearAnimation();
    }

}
