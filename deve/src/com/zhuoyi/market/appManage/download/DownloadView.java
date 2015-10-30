package com.zhuoyi.market.appManage.download;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.userDownload.DownloadManager;
import com.market.download.userDownload.DownloadPool;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.AppInfoBto;
import com.market.net.request.GetDownloadRecommendAppReq;
import com.market.net.response.GetDownloadRecommendAppResp;
import com.market.net.utils.RequestAsyncTask;
import com.market.view.MyGridView;
import com.zhuoyi.market.R;
import com.zhuoyi.market.Splash;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.utils.MarketUtils;

public class DownloadView implements OnScrollListener {

	private MarketDownloadActivity mActivity;
	private Context mContext;
	private View mView;
	private ExpandableListView mExpandableListView;
	private DownloadExpandableAdapter mDownloadExpandableAdapter;
	private DisplayDownloadDataStorage mDisplayDownloadDataStorage = null;
	private boolean updateListView = true;
	
	//后台通知前台做事情
	public final static int ACTION_DEFAULT = 0;            //只做刷新
	public final static int ACTION_UPDATE = 1;             //更新数据后立即刷新
	public final static int ACTION_UPDATE_DELAY = 2;       //更新数据后延迟刷新
	public final static int ACTION_COMPLETE = 3;           //下载完成
	public final static int ACTION_INSTALLED = 4;          //安装完成
	public final static int ACTION_REMOVE = 5;             //移除
	
	//下载推荐
	public static final int HANDLER_GET_RECOMMEND_APP = 20;
	public final static int HANDLER_ADD_TO_DOWNLOAD = 21;
	private final static int HANDLER_UPDATE = 22;
	private int notUpdateCount = 0;
	
	private View mRecommendView = null;
	private View mRecommendTitle = null;
	private ProgressBar mAssistImage = null;
	private MyGridView mRecommendation_gridView = null;
	private TextView mRecommend_textView = null;
	private static ArrayList<AppInfoBto> mRecommendation_list = null;
	private DownloadRecommendAdapter mRecommendation_adapter = null;
	private RequestAsyncTask mAsyncTask = null;
	private static String mNewPacName = null;
	private static long mOldUpdateTime = 0;
	private int mNextRecommendReqTime = 1*60*1000;
	private boolean mRecommendReqFinish = true;

	private static int mOldDownloadingNum = 0;
	
	private WeakReference<DownloadInterface> mCallBack = null;
	
	private Handler mHandler = new Handler(){
		
		@Override
		public void handleMessage(Message msg){
		    switch(msg.what){
		    case HANDLER_ADD_TO_DOWNLOAD:
		        DownloadEventInfo data = (DownloadEventInfo)msg.obj;
		        DownloadEventInfo memData = DownloadManager.getEventInfo(mContext, data.getPkgName(), data.getVersionCode());
		        if (memData == null) {
		        	break;
		        }
		        if (mDisplayDownloadDataStorage != null)
		            mDisplayDownloadDataStorage.addData2DisplayList(memData);
		        break;
		    case HANDLER_UPDATE:
		        notUpdateCount = 0;
		        if(mDownloadExpandableAdapter!=null)
                    mDownloadExpandableAdapter.notifyDataSetChanged();
		        break;
            case HANDLER_GET_RECOMMEND_APP:
                GetDownloadRecommendAppResp appResp = null;
                List<AppInfoBto> appList = new ArrayList<AppInfoBto>();
                HashMap<String, Object> map = (HashMap<String, Object>)msg.obj;

                int count = 4;
                if(map!=null && map.size()>0) {
                    appResp = (GetDownloadRecommendAppResp)map.get("downloadRecommendInfo");
                    appList = appResp.getAppList();
                    mNextRecommendReqTime = appResp.getNextReqTime()*1000;
                }

                if (appList != null && appList.size()>0) {  
                    if(appList.size()<4) {
                        count = appList.size();
                    }
                    
                    if(mRecommendation_list.size() > 0) {
                        mRecommendation_list.clear();
                    }
                    
                    AppInfoBto appInfo = null;
                    for(int i=0;i<count;i++) {
                        appInfo = appList.get(i);
                        appInfo.setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(),false));
                        mRecommendation_list.add(appInfo);
                    }
                }
                if(mRecommendation_list.size() > 0){
                    mRecommendation_gridView.setVisibility(View.VISIBLE);
                    mRecommend_textView.setVisibility(View.GONE);
                    mAssistImage.setVisibility(View.GONE); 
                    mRecommendation_adapter.notifyDataSetChanged();
                }else{
                    mRecommend_textView.setVisibility(View.VISIBLE);
                    mRecommendation_gridView.setVisibility(View.GONE);
                    mAssistImage.setVisibility(View.GONE);
                }
                mRecommendReqFinish = true;
                break;
		    default:
		        break;
		    }
		}
	};
	

	public DownloadView(Context context, DownloadInterface callBack) {
		mActivity = (MarketDownloadActivity)context;
		mContext = context.getApplicationContext();
		mCallBack = new WeakReference<DownloadInterface>(callBack);
		LayoutInflater tLayoutInflater = LayoutInflater.from(mContext);
		mView = tLayoutInflater.inflate(R.layout.layout_download_manage, null);
		mRecommendView = tLayoutInflater.inflate(R.layout.download_recommend_view, null);
		mRecommendTitle = tLayoutInflater.inflate(R.layout.download_recommend_title, null);
		
		mDisplayDownloadDataStorage = new DisplayDownloadDataStorage();

		DownloadPool.setDownloadViewHandler(mHandler);
	}

	
	public View getMyView() {
		return mView;
	}

	
	public void entryDownloadView() {
	    findViews();
		initViews();
	}

	
	public void notifyDataSetChanged(int actionDownload, final DownloadEventInfo data) {

	    if (mDisplayDownloadDataStorage == null) return;
	    
        switch(actionDownload){
        case ACTION_DEFAULT:
            if(mDownloadExpandableAdapter!=null)
                mDownloadExpandableAdapter.notifyDataSetChanged();
            break;
        case ACTION_UPDATE:
            mDisplayDownloadDataStorage.updateDownloadInfo(data);
            if(mDownloadExpandableAdapter!=null)
                mDownloadExpandableAdapter.notifyDataSetChanged();
            break;
        case ACTION_UPDATE_DELAY:
            if (updateListView){
                mDisplayDownloadDataStorage.updateDownloadInfo(data);
                
                if (mHandler.hasMessages(HANDLER_UPDATE)) {
                    mHandler.removeMessages(HANDLER_UPDATE);
                    notUpdateCount++;
                }
                
                if (notUpdateCount > 4) {
                    notUpdateCount = 0;
                    if(mDownloadExpandableAdapter!=null)
                        mDownloadExpandableAdapter.notifyDataSetChanged(); 
                } else {
                    Message msg = new Message();
                    msg.what = HANDLER_UPDATE;
                    mHandler.sendMessageDelayed(msg, 200); 
                }
            }else{
                new Thread(){
                    public void run(){
                        mDisplayDownloadDataStorage.updateDownloadInfo(data);
                    }
                }.start();
            }
            
            break;
        case ACTION_COMPLETE:
            mDisplayDownloadDataStorage.downloadComplete(data);
            if(mDownloadExpandableAdapter!=null) {
                mDownloadExpandableAdapter.notifyDataSetChanged();
            }
            if(mDisplayDownloadDataStorage.isDownload()){
                getRecommendData();
            }
            break;
        case ACTION_INSTALLED:
            mDisplayDownloadDataStorage.updateCompleteInfo(data);
            if(mDownloadExpandableAdapter!=null) {
                mDownloadExpandableAdapter.notifyDataSetChanged();
            }
            break;
        case ACTION_REMOVE:
            mDisplayDownloadDataStorage.removeFromDownload(data);
            if(mDownloadExpandableAdapter!=null) {
                mDownloadExpandableAdapter.notifyDataSetChanged();
            }
            break;
        default:
            break;
        }
	}
	

	public void freeViewResource() {
	    
	    if (mDisplayDownloadDataStorage != null) {
	        mDisplayDownloadDataStorage.freeResource();
	        mDisplayDownloadDataStorage = null;
	    }
	    
	    DownloadPool.setDownloadViewHandler(null);
	}
	
	
	public void downloadComplete(String flag) {
	    if (mDownloadExpandableAdapter != null) {
	        mDownloadExpandableAdapter.dismissDialog(flag);
	        mDownloadExpandableAdapter.setItemHideView(flag);
	    }
	}
	
	
	public void getRecommendData() {
	    
	    boolean needGetData = true;
	    
	    //两次更新时间间隔短，不再更新
	    long curTime = System.currentTimeMillis();
	    if((curTime - mOldUpdateTime) < mNextRecommendReqTime) {
	        needGetData = false;
	    }

	    //上次更新请求没有结束，不再请求
	    if(!mRecommendReqFinish){
	        needGetData = false;
	    }
	    
	    //两次请求数据相同，不再请求，但是如果下载列表为空，必须去请求
	    String newPacName = null;
	    if(mDisplayDownloadDataStorage != null && mDisplayDownloadDataStorage.isDownload()) {
	        newPacName = DownloadPool.getNewDownloadPacName();
	    } else {
	        mNewPacName = null;
	    }
	    if(mNewPacName != null && mNewPacName.equals(newPacName)) {
	        needGetData = false;
	    }
	    
	    if(needGetData) {
            mNewPacName = newPacName;
            mOldUpdateTime = curTime;
    
            mRecommendReqFinish = false;
            mRecommend_textView.setVisibility(View.GONE);
            mRecommendation_gridView.setVisibility(View.GONE);
            mAssistImage.setVisibility(View.VISIBLE);
            GetDownloadRecommendAppReq appReq = new GetDownloadRecommendAppReq();
            appReq.setRecommendAppId(DownloadPool.getNewDownloadAppId());
            appReq.setRecommendPckName(mNewPacName);
            appReq.setFilterPckName(getDownloadAndInstallAppName());
    
            String contents = SenderDataProvider.buildToJSONData(mContext,MessageCode.GET_DOWNLOAD_RECOMMEND_APPS,appReq);
            mAsyncTask = new RequestAsyncTask(mHandler, HANDLER_GET_RECOMMEND_APP,contents);
            try {
            	mAsyncTask.execute(Constant.MARKET_URL, MessageCode.GET_DOWNLOAD_RECOMMEND_APPS);
            } catch(RejectedExecutionException e) {
            	mRecommend_textView.setVisibility(View.VISIBLE);
                mRecommendation_gridView.setVisibility(View.GONE);
                mAssistImage.setVisibility(View.GONE);
            }
	    } else {
            if(mRecommendation_list.size() > 0) {
                mRecommend_textView.setVisibility(View.GONE);
                mRecommendation_gridView.setVisibility(View.VISIBLE);
                mAssistImage.setVisibility(View.GONE);
            } else {
                mRecommend_textView.setVisibility(View.VISIBLE);
                mRecommendation_gridView.setVisibility(View.GONE);
                mAssistImage.setVisibility(View.GONE);
            }
        }
	}
	
	
	private String getDownloadAndInstallAppName(){
	    
	    String pacName = "";
	    ConcurrentHashMap<String, DownloadEventInfo> downloadEvents = DownloadPool.getAllDownloadEvent(mContext);
	    Iterator iter = downloadEvents.entrySet().iterator();
	    Map.Entry entry = null;
	    DownloadEventInfo eventInfo = null;
	    String name = null;
        while(iter.hasNext()){
            entry = (Map.Entry)iter.next();
            eventInfo = (DownloadEventInfo)entry.getValue();
            name = eventInfo.getPkgName();
            if(!pacName.contains(name)){
                pacName = name + ";" +pacName;
            }
        }

        SharedPreferences settings = mContext.getSharedPreferences(Splash.PREFS_NAME, 0);
        String installedApkName = settings.getString("installed_apk_name", "");
        pacName = pacName + installedApkName;

	    return pacName;
	}
	
	
	private void findViews(){
	    if(mRecommendation_list == null){
	        mRecommendation_list = new ArrayList<AppInfoBto>();
	    }
	    mRecommend_textView = (TextView) mRecommendView.findViewById(R.id.download_recommend_text);
        mAssistImage = (ProgressBar)mRecommendView.findViewById(R.id.download_recommend_progress);
        mRecommendation_gridView = (MyGridView)mRecommendView.findViewById(R.id.download_recommend_gridview);
        mRecommendation_adapter = new DownloadRecommendAdapter(mContext, mRecommendation_list);
        mRecommendation_gridView.setAdapter(mRecommendation_adapter); 
	}

	
	private void initViews() {	

		if(mExpandableListView == null) {
			mExpandableListView = (ExpandableListView) mView.findViewById(R.id.list);
			mExpandableListView.setOnScrollListener(this);
		}
		mExpandableListView.setGroupIndicator(null);

		//adapter
		mDownloadExpandableAdapter = new DownloadExpandableAdapter(mActivity, mDisplayDownloadDataStorage, mExpandableListView, mCallBack);
		mDownloadExpandableAdapter.setRecommendView(mRecommendView, mRecommendTitle);
		mExpandableListView.setAdapter(mDownloadExpandableAdapter);

		for(int i = 0; i < mDownloadExpandableAdapter.getGroupCount(); i++) {              
			mExpandableListView.expandGroup(i);     		                          
		} 
		mExpandableListView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) 
			{
				// TODO Auto-generated method stub
				return true;
			}

		});
		
		refreshDownArraysData();
	}
	
	
	public void windowFocusChanged(boolean hasFocus){
	    int curSize = 0;
	    if (mDisplayDownloadDataStorage != null) 
	        curSize = mDisplayDownloadDataStorage.getDownloadSize();
	    if(hasFocus){
    	    if(mOldDownloadingNum != curSize){
    	        if(mDownloadExpandableAdapter!=null) {
            	    mDownloadExpandableAdapter.setDownloadNum();
                    mDownloadExpandableAdapter.setDownloadedNum();
                    mDownloadExpandableAdapter.setDownloadRecommendTitle();
                }
            }
	    }else{
	        mOldDownloadingNum = curSize;
        }
	}

	
	public void refreshDownArraysData(){
	    if (mDisplayDownloadDataStorage != null)
	        mDisplayDownloadDataStorage.reInitAllInfo();
		getRecommendData();
		if(mDownloadExpandableAdapter!=null)
		    mDownloadExpandableAdapter.notifyDataSetChanged();
	} 


	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		switch (scrollState) { 
		case OnScrollListener.SCROLL_STATE_IDLE:
			updateListView = true;
            notifyDataSetChanged(DownloadView.ACTION_DEFAULT, null);
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
		case OnScrollListener.SCROLL_STATE_FLING:
			updateListView = false;
			break;
		}
	}
}
