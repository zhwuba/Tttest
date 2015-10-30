package com.zhuoyi.market.appManage.download;

import com.market.statistics.ReportFlag;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.AsyncImageCache;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DelTipDialog extends Dialog implements OnClickListener {
    
    /***************************************************
     * style    icon    name    tip    delPkg    buttons
     * 0        y       y       y       n           y
     * 1        y       y       y       y           y
     * 2        n       n       y       y           y
     **************************************************/
    public final static int TIP_STYLE_0 = 0;
    public final static int TIP_STYLE_1 = TIP_STYLE_0 + 1;
    public final static int TIP_STYLE_2 = TIP_STYLE_1 + 1;
    public final static int TIP_STYLE_MAX = TIP_STYLE_2;
    
    private Context mContext = null;
    private OnDelTipClickListener mOnDelTipClickListener = null;
    
    private ImageView mIcon = null;
    private TextView mName = null;
    private TextView mTip = null;
    private LinearLayout mDel = null;
    private ImageView mDelImage = null;
    private Button mCancle = null;
    private Button mOk = null;
    
    private String mPkgName = null;
    private int mVerCode = 0;
    private boolean mDownloading = true;
    
    private boolean mDelFile = true;


    public DelTipDialog(Context context, OnDelTipClickListener listener) {
        super(context, R.style.MyMarketDialog);
        // TODO Auto-generated constructor stub
        mContext = context;
        mOnDelTipClickListener = listener;
        
        this.setCancelable(false);
        
        setContentView(R.layout.del_tip_dialog);
        initView(mContext);
    }
    
    
    /**
     * 初始化布局
     */
    private void initView(Context context) {
        mIcon = (ImageView) findViewById(R.id.del_tip_icon);
        mName = (TextView) findViewById(R.id.del_tip_name);
        mTip = (TextView) findViewById(R.id.del_tip_tip);
        mDel = (LinearLayout) findViewById(R.id.del_tip_del);
        mDelImage = (ImageView) findViewById(R.id.del_tip_del_image);
        mCancle = (Button) findViewById(R.id.del_tip_cancle);
        mOk = (Button) findViewById(R.id.del_tip_ok);
        
        mDelImage.setOnClickListener(this);
        mCancle.setOnClickListener(this);
        mOk.setOnClickListener(this);
    }
    
    
    public void setData(int style, String pkgName, String flag, String appName, int verCode, boolean fileExist, boolean installed) {

        mPkgName = pkgName;
        mVerCode = verCode;
        
        if ("com.zhuoyi.market".equals(mPkgName)) {
            appName = mContext.getString(R.string.market_about);
        }
        
        switch(style) {
        case TIP_STYLE_0:
            mDownloading = true;
            setIcon(pkgName,flag);
            setName(appName);
            setTip((String)mContext.getString(R.string.dm_del_tip_tip1));
            setDel(false);
            break;
        case TIP_STYLE_1:
            mDownloading = false;
            setIcon(pkgName,flag);
            setName(appName);
            if (fileExist) {
                if (installed) {
                    setTip(mContext.getString(R.string.dm_del_tip_tip4));
                } else {
                    setTip(mContext.getString(R.string.dm_del_tip_tip2));
                }
                setDel(true);
            } else {
                setTip(mContext.getString(R.string.dm_del_tip_tip4));
                setDel(false);
            }
            break;
        case TIP_STYLE_2:
            mDownloading = false;
            setIcon(null,null);
            setName(null);
            setTip(mContext.getString(R.string.dm_del_tip_tip3));
            if (fileExist) {
                setDel(true);
            } else {
                setDel(false);
            }
            break;
        default:
            break;
        }
    }

    
    private void setIcon(String pkgName, String flag) {
        if (TextUtils.isEmpty(pkgName)) {
            if (mIcon.getVisibility() == View.VISIBLE) {
                mIcon.setVisibility(View.GONE); 
            } 
        } else {
            if (mIcon.getVisibility() != View.VISIBLE) {
                mIcon.setVisibility(View.VISIBLE); 
            }
    
            if ("com.zhuoyi.market".equals(pkgName)) {
                mIcon.setImageResource(R.drawable.icon);
            } else {
                int picId = R.drawable.picture_bg1_big;
                /*if (flag != null && (flag.contains(ReportFlag.FROM_HOMEPAGE)
                        || flag.contains(ReportFlag.FROM_DOWN_GIFT)
                        || flag.contains(ReportFlag.FROM_TYD_LAUNCHER)
                        || flag.contains("/1/HomeRecommend")
                        || flag.contains("/13/DownloadRegard"))) {
                    picId = R.drawable.picture_bg1_big;
                }*/
        
                AsyncImageCache.from(mContext).displayImage(true, mIcon, picId, 
                        new AsyncImageCache.GeneralImageGenerator(pkgName,null), true);
            }  
        }      
    }
    
    
    private void setName(String name) {
        if (TextUtils.isEmpty(name)) {
            if (mName.getVisibility() == View.VISIBLE) {
                mName.setVisibility(View.GONE);
            }
        } else {
            if (mName.getVisibility() != View.VISIBLE) {
                mName.setVisibility(View.VISIBLE);
            }
            mName.setText(name);
        }
    }
    
    
    private void setTip(String tip) {
        
        if (TextUtils.isEmpty(tip)) {
            if (mTip.getVisibility() == View.VISIBLE) {
                mTip.setVisibility(View.GONE);
            }
        } else {
            if (mTip.getVisibility() != View.VISIBLE) {
                mTip.setVisibility(View.VISIBLE);
            }
            mTip.setText(tip);
        }
    }
    
    
    private void setDel(boolean need) {
        if (need) {
            if (mDel.getVisibility() != View.VISIBLE) {
                mDel.setVisibility(View.VISIBLE);
            }
            mDelFile = true;
            mDelImage.setBackgroundResource(R.drawable.dm_del_tip_del_image);
        } else {
            if (mDel.getVisibility() == View.VISIBLE) {
                mDel.setVisibility(View.GONE);
            }
            mDelFile = false;
        }
    }
    
    
    public String getCurAppFlag() {
        return mPkgName + mVerCode;
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        switch (id) {
        case R.id.del_tip_cancle:
            dismiss();
            break;
        case R.id.del_tip_ok:
            if (mOnDelTipClickListener != null)
                mOnDelTipClickListener.onOkClick(mPkgName, mVerCode, mDownloading, mDelFile);
            dismiss();
            break;
        case R.id.del_tip_del_image:
            if (mDelFile) {
                mDelImage.setBackgroundResource(R.drawable.dm_del_tip_del_image_unselect);
                mDelFile = false;
            } else {
                mDelImage.setBackgroundResource(R.drawable.dm_del_tip_del_image);
                mDelFile = true;
            }
            break;
        }
    }
    
    
    public interface OnDelTipClickListener {
        public void onOkClick(String pkgName, int verCode, boolean downloading, boolean delFile);
    }
}
