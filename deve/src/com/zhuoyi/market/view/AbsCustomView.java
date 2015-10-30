package com.zhuoyi.market.view;

import com.market.behaviorLog.UserLogSDK;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

/**
 * 各自定义view的抽象类
 * @author JLu
 *
 */
public abstract class AbsCustomView {

    protected Context mContext;
    private String mLogDes;
    protected String mTitleName;
    
    protected boolean mIsStatistic = false;
    
    protected AbsCustomView(Context context) {
        mContext = context;
    };
    
    
    public void openStatisticsWhenEntry() {
        mIsStatistic = true;
    }
    
    
    public boolean isStatisticsOpen() {
        return mIsStatistic;
    }
    
    
    public void setLogDes(String logDes) {
        mLogDes = logDes;
    }
    
    
    public void setTitleName(String titleName) {
        mTitleName = titleName;
    }
    
    
    /**
     * 该方法将执行用户行为记录，它必须被子类调用，覆盖该方法时务必注意这一点
     */
    public void exitView() {
        if (mLogDes != null) {
            UserLogSDK.logActivityExit(mContext, mLogDes);
        }
    }
    
    
	/**
	 * 具体实现类的实例化方法，该方法将执行用户行为记录，它必须被子类调用，覆盖该方法时务必注意这一点
	 */
	public void entryView() {
	    if (mLogDes != null) {
            UserLogSDK.logActivityEntry(mContext, mLogDes);
        }
	};
	
	
	/**
     * 销毁资源，该方法将释放Context，它必须被子类调用，覆盖该方法时务必注意这一点
     */
    public void freeViewResource() {
        mContext = null;
    };
	
	
	/**
	 * 返回自定义view的最外层布局
	 * @return View
	 */
	public abstract View getRootView();
	
	
	/**
	 * view状态改变,通知刷新
	 * @param pkgName 包名
	 */
	public abstract void notifyDataSetChanged(String pkgName);
	
	/**
	 * 返回自定义view中listview的引用,可以不实现
	 * @return ListView
	 */
	public ListView getListView() {
		return null;
	}
}
