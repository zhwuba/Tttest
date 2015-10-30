package com.market.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuoyi.market.R;
import com.zhuoyi.market.search.SearchActivity;
import com.zhuoyi.market.utils.MarketUtils;

/**
 * 副标题
 * @author dream.zhou
 *
 */
public class CommonSubtitleView extends LinearLayout {
    
    private Context mContext = null;
    private View mMainView = null;
    private ImageView mSearchBtn = null;
    private ImageView mBackBtn = null;
    private TextView mName = null;
    private TextView mRightCommontText;
    private CommonTitleDownloadView mDownloadView = null;
    
    public CommonSubtitleView(Context context) {
        super(context);
        initView(context);
    }
    
    
    public CommonSubtitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    
    public CommonSubtitleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }
    
    
    public void showSearchBtn(boolean isShow) {
    	if(isShow) {
    		mSearchBtn.setVisibility(View.VISIBLE);
    		mDownloadView.setVisibility(View.VISIBLE);
    	} else {
    		mSearchBtn.setVisibility(View.GONE);
    		mDownloadView.setVisibility(View.GONE);
    	}
    }
    

    private void initView(Context context){
    	mContext = context;
    	setOrientation(VERTICAL);
        MarketUtils.setTitleLayout(this, mContext);
    	
        LayoutInflater tLayoutInflater = LayoutInflater.from(mContext);
        mMainView = tLayoutInflater.inflate(R.layout.common_subtitle_view, this , true);
        
        mDownloadView = (CommonTitleDownloadView) mMainView
                .findViewById(R.id.title_download);
        mRightCommontText = (TextView) mMainView.findViewById(R.id.subtitle_right_tip);
        
        mBackBtn = (ImageView) mMainView.findViewById(R.id.subtitle_back);
        mBackBtn.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ((Activity)mContext).finish();
            }
            
        });
        
        mSearchBtn = (ImageView) mMainView.findViewById(R.id.subtitle_search);
        mSearchBtn.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SearchActivity.class);
                mContext.startActivity(intent);
            }
            
        });
        
        mName = (TextView) mMainView.findViewById(R.id.subtitle_name);
        setSubtitleName((String)this.getTag());
    }
    
    
    public void setBackBtnClickListener(OnClickListener listener) {
        mBackBtn.setOnClickListener(listener);
    }
    
    
    public void setSubtitleName(String name){
        if(name == null)name = "";
        mName.setText(name);
    }
    
    
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
    
    public TextView getRightTextView() {
    	return mRightCommontText;
    }
}
