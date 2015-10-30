package com.zhuoyi.market.topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.market.behaviorLog.UserLogSDK;
import com.market.download.baseActivity.DownloadBaseActivity;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.util.Util;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.AppInfoBto;
import com.market.net.data.AssemblyInfoBto;
import com.market.net.request.GetApkListByPageReq;
import com.market.net.request.GetTopicReq;
import com.market.net.response.GetApkListByPageResp;
import com.market.net.response.GetTopicResp;
import com.market.net.utils.StartNetReqUtils;
import com.market.statistics.ReportFlag;
import com.market.view.CommonSubtitleView;
import com.market.view.PressInstallButtonAnimView;
import com.zhuoyi.market.R;
import com.zhuoyi.market.adapter.SingleLineItemAdapter;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.utils.gallery.BitmapUtiles;

public class TopicInfoActivity extends DownloadBaseActivity implements OnScrollListener, OnItemClickListener, DownloadCallBackInterface {

	private final int UPDATE_PAGE_MSG = 1;
	private final int REFRESH_LIST = 2;
	private int mGetListIndex = 1;
	private int mBaseNumber = 0;
	private int mMaxNumber = 16;
	
	private int mStartIndex;
	private int mEndIndex;
	private int mCID;
	private String mTopicName;
	private String mTopicInfo;
	private String mTopicImage;
	private ImageView mTopic_image;
	private TextView mTopic_name,mTopic_info;
	private ListView mApp_listView;
	private ArrayList<AppInfoBto> mApp_list;
	private SingleLineItemAdapter mApp_adapter;
	private View mFooter;
	private ProgressBar mFooterProgress = null;
    private TextView mFooterText = null;
	private LinearLayout mTopic_info_loading,mTopic_info_refresh;
	private Button mApp_refresh;
	private Handler mHandler;
	private boolean mGetListDataFail = false;
	private boolean mRefreshFinished = true;
	// header view
	private View mHeaderView;
	private boolean mIsToBottom = false;
    private GetTopicReq mTopicReq;
    private int mAssemblyId = -1;
    private String mImageUrl = "";
    private AsyncImageCache mAsyncImageCache;
    
    private Context mContext = null;
    private int mMapWidth,mMapHeight;
    
    private DownloadCallBackInterface mDownloadCallBackInterface = null;
    
    private CommonSubtitleView mTitleLayout = null;
    private PressInstallButtonAnimView mPressInstallButtonAnimView = null;
    private int[] mDownloadLocation = {0,0};
    private int mStatusBarHeight = 0;
    
    private String mLogDes = null;
    
    private boolean mIsPassInstall = true;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_topic_info);
		
		mContext = this.getApplicationContext();
		mDownloadCallBackInterface = this;
		
		mCID = getIntent().getIntExtra("mCID",-1);
		mTopicName = getIntent().getStringExtra("mTopicName");
		mTopicInfo = getIntent().getStringExtra("mTopicInfo");
		mTopicImage = getIntent().getStringExtra("mTopicImage");
		mImageUrl = getIntent().getStringExtra("imageUrl");
		mIsPassInstall = getIntent().getBooleanExtra("isPassInstall", true);

		mLogDes = UserLogSDK.getSpecialDetailActivityDes(Integer.toString(mCID), mTopicName);
		
		findViews();
        mTopicReq = new GetTopicReq();
		mTopic_info_loading.setVisibility(View.VISIBLE);
		mApp_listView.addHeaderView(mHeaderView);
		mTopic_name.setText(mTopicName);
		mTopic_info.setText(mTopicInfo);
		
		mMapWidth = mContext.getResources().getDimensionPixelSize(R.dimen.default_pic_item_width);
		mMapHeight = mContext.getResources().getDimensionPixelSize(R.dimen.default_pic_item_height);

		int defaultPic = -1;
		if(MarketUtils.isNoPicModelReally()) {
			defaultPic = R.drawable.logo_no_network;
		} else {
			defaultPic = R.drawable.logo_no_network_withtxt;
		}

		AsyncImageCache.from(getApplicationContext()).displayImage(true,true,mTopic_image, defaultPic,mMapWidth,mMapHeight,
				new AsyncImageCache.NetworkImage565Generator(MarketUtils.getImgUrlKey(mImageUrl),mImageUrl), false, true,false,"topic");


		mApp_list = new ArrayList<AppInfoBto>();
		mHandler = new Handler(){
			public void handleMessage(Message message){
	            HashMap<String, Object> map;
	            List<AppInfoBto> list = null;
				int getDataSuccess = 0;
				switch (message.what){
                case UPDATE_PAGE_MSG:
                    int errorCodes = message.arg1;
                    GetTopicResp resp = null;
                    map = (HashMap<String, Object>)message.obj;                                       
                    if(map!=null && map.size()>0){                      
                        resp = (GetTopicResp)map.get("topicResp");
                        list = getTopicApplist(resp);
                    }
                    
                    int firstDataNum = 0;
                    if(list != null && list.size() > 0){
                        int firstListSize = list.size();
                        for (int i = 0; i < firstListSize; i++){
                        	AppInfoBto appInfo = list.get(i);
                        	appInfo.setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(),false));

                        	if(appInfo.getIsShow()){
                        		mApp_list.add(appInfo);  
                        		firstDataNum++;
                        	} else if(!mIsPassInstall) {
                        		mApp_list.add(appInfo);  
                        		firstDataNum++;
                        	}
                        }
                        
                        if(mGetListIndex==1 && mApp_adapter==null){
                            mApp_adapter = new SingleLineItemAdapter(getBaseContext(), mApp_list, mDownloadCallBackInterface, Util.getWaterFlowLayoutId());
                            mApp_adapter.setReportFlag(ReportFlag.FROM_NULL);
                            mApp_adapter.setTopicId(mCID);
                            mApp_listView.addFooterView(mFooter);
                            mApp_listView.setAdapter(mApp_adapter); 
                            mApp_adapter.notifyDataSetChanged();
                        }
                        mGetListDataFail = false;
                    }else{
                        mGetListDataFail = true;
                    }
                    mRefreshFinished = true;
                    if(!mGetListDataFail && firstDataNum == 0){
                        startGetTopicData();
                    }else{
                        setLayoutVisibility(true);
                    }
                    break;
				case REFRESH_LIST:
                    GetApkListByPageResp listResp = null;
                    AppInfoBto appInfo;
                    map = (HashMap<String, Object>)message.obj;
					getDataSuccess = message.arg1;	
					mGetListDataFail = true;
					int currDataNum = 0;
					if(getDataSuccess == 1) {
						if (map != null){
                            listResp = (GetApkListByPageResp) map.get("listByPage");
                            list = listResp.getAppList();
                        }
						if(list != null && list.size() > 0){
						    int listSize = list.size();
    						for(int i=0; i<listSize; i++){
                                appInfo = list.get(i);
                                appInfo.setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(),false));
                                if(appInfo.getIsShow()){
                                    mApp_list.add(appInfo); 
                                    currDataNum++;
                                }
                            }
    						
    						if(listSize < mMaxNumber){
    							mIsToBottom = true;
    							mFooterProgress.setVisibility(View.GONE);
                                mFooterText.setText(mContext.getString(R.string.loaded_all_data));
    						}

    						if(currDataNum > 0){
    							mApp_adapter.setDatas(mApp_list);
    							mApp_adapter.notifyDataSetChanged();
    						}
    						mGetListDataFail = false;
						}else{
						    mIsToBottom = true;
                            mFooterProgress.setVisibility(View.GONE);
                            mFooterText.setText(mContext.getString(R.string.loaded_all_data));
						}
					}
					mRefreshFinished = true;
					if(!mGetListDataFail && currDataNum == 0){
                        startGetTopicData();
                    }else{
                        setLayoutVisibility(true);
                    }
					break;
				}
			}
		};
		startGetTopicData();
	}


    @Override
    protected void onResume() {
        super.onResume();
        
        UserLogSDK.logActivityEntry(mContext, mLogDes);
    }


    @Override
    protected void onPause() {
        UserLogSDK.logActivityExit(mContext, mLogDes);
        
        super.onPause();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        if (hasFocus) {
            if(mApp_adapter != null)
                mApp_adapter.notifyDataSetChanged();
            
            if (mTitleLayout != null)
                mTitleLayout.setDownloadStatus();
        }
        
        if(mDownloadLocation[0] == 0 || mDownloadLocation[1] == 0){
            
            Rect frame = new Rect();  
            getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);  
            mStatusBarHeight = frame.top; 
            int downloadWidth = mTitleLayout.getDownloadWidth();
            int downloadHeight = mTitleLayout.getDownloadHeight();

            mDownloadLocation = mTitleLayout.getDownloadLocation();
            mDownloadLocation[0] = mDownloadLocation[0] - downloadWidth/4;
            mDownloadLocation[1] = mDownloadLocation[1] - downloadHeight/2;
        }
        super.onWindowFocusChanged(hasFocus);
    }
	

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		int CID = getIntent().getIntExtra("mCID",-1);
		if (CID != -1 && CID != mCID)
		{
			if (null != mApp_list)
			{
				mApp_list.clear();
			}
			if(mApp_adapter!=null)
			{
				mApp_adapter = null;
			}
			mCID = CID;
			mTopicName = getIntent().getStringExtra("mTopicName");
			mTopicInfo = getIntent().getStringExtra("mTopicInfo");
			mTopicImage = getIntent().getStringExtra("mTopicImage");
			Bitmap bitmap = BitmapUtiles.convertFileToBitmap(mTopicImage);
			mTopic_name.setText(mTopicName);
			mTopic_info.setText(mTopicInfo);
			if (bitmap == null) {
				mTopic_image.setBackgroundResource(R.drawable.default_pic);
			} else {
				mTopic_image.setBackgroundDrawable(new BitmapDrawable(bitmap)); 
			}		
			startGetTopicData();
		}
	}

	@Override
	public void onDestroy() {
		if(mApp_adapter!=null)
		    mApp_adapter.freeImageCache();
		if(mAsyncImageCache!=null)
		    mAsyncImageCache.stop();
		mAsyncImageCache = null;
		
		if (mTitleLayout != null) {
		    mTitleLayout.unRegisteredReceiver();
		}
		super.onDestroy();
	}

	public void findViews() {
		mHeaderView = getLayoutInflater().inflate(R.layout.layout_topic_info_header, null);
		mTopic_image = (ImageView) mHeaderView.findViewById(R.id.topic_image);
		mTopic_name = (TextView) mHeaderView.findViewById(R.id.topic_name);
		mTopic_info = (TextView) mHeaderView.findViewById(R.id.topic_info);
		mApp_listView = (ListView) findViewById(R.id.app_list);
		//mApp_listView.setDivider(getResources().getDrawable(R.drawable.diliver));
		mApp_listView.setOnScrollListener(this);
		mApp_listView.setOnItemClickListener(this);
		mFooter = LayoutInflater.from(getBaseContext()).inflate(R.layout.foot, null);
		mFooterProgress = (ProgressBar) mFooter.findViewById(R.id.footer_progress);
        mFooterText = (TextView) mFooter.findViewById(R.id.footer_textview);
		mTopic_info_loading = (LinearLayout) findViewById(R.id.topic_info_loading);
		mTopic_info_refresh = (LinearLayout) findViewById(R.id.topic_info_refresh);
		mApp_refresh = (Button) findViewById(R.id.refresh_btn);
		mApp_refresh.setOnClickListener(new OnClickListener(){
 
			@Override
			public void onClick(View v) {
				if (MarketUtils.getAPNType(getApplicationContext()) == -1){
					Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
					return;
				}
				startGetTopicData();
			}
			
		});
		
		mTitleLayout = (CommonSubtitleView) findViewById(R.id.title_layout);
		mTitleLayout.showSearchBtn(true);
		mTitleLayout.registeredReceiver();
		mPressInstallButtonAnimView = (PressInstallButtonAnimView)findViewById(R.id.common_download_anim);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// see the detail of the app
		
		if(position==0)
			return;
		else
			position = position -1;
		if(mApp_listView!=null && parent.getId()==mApp_listView.getId())
		{
			try
			{
				AppInfoBto mAppInfoBto = (AppInfoBto) mApp_list.get(position) ;
				MarketUtils.startAppDetailActivity(getApplicationContext(), mAppInfoBto, ReportFlag.FROM_NULL, mCID);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) { 
		case OnScrollListener.SCROLL_STATE_IDLE:
			if(mApp_adapter!=null)
			{		
				mApp_adapter.allowRefreshIcon(true);
				asyncLoadImage();
				//mApp_adapter.notifyDataSetChanged();
			}
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
		case OnScrollListener.SCROLL_STATE_FLING:
			if(mApp_adapter!=null)
				mApp_adapter.allowRefreshIcon(false);
			break;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		
	    String contents = "";
	    switch (view.getId()){
		case R.id.app_list:
			mStartIndex = firstVisibleItem;
			mEndIndex = firstVisibleItem + visibleItemCount;
			if (mEndIndex >= totalItemCount){
				mEndIndex = totalItemCount - 1;
			}

			if (mApp_listView.getVisibility() == View.VISIBLE 
			        && !mIsToBottom 
			        && view.getLastVisiblePosition()+MarketUtils.LESS_ITEM_NUM >= (view.getCount() - 1) 
			        && mRefreshFinished
			        && mAssemblyId!=-1
			        && MarketUtils.getAPNType(getBaseContext()) != -1){

				if(!mGetListDataFail){
					mBaseNumber = mBaseNumber +1;
					mGetListIndex = mBaseNumber * mMaxNumber;
				}

				mRefreshFinished = false;
                GetApkListByPageReq req= new GetApkListByPageReq();
                req.setAssemblyId(mAssemblyId);
                req.setStart(mGetListIndex);
                req.setFixedLength(mMaxNumber);
                contents = SenderDataProvider.buildToJSONData(getApplicationContext(),MessageCode.GET_APK_LIST_BY_PAGE,req);
                StartNetReqUtils.execListByPageRequest(mHandler, REFRESH_LIST,MessageCode.GET_APK_LIST_BY_PAGE,contents);
			}
			break;
		}
	}

    private void startGetTopicData() {
        setLayoutVisibility(false);
        if(mApp_adapter != null){
            if (mIsToBottom == false && mRefreshFinished && mAssemblyId !=-1){
                if(!mGetListDataFail){
                    mBaseNumber = mBaseNumber +1;
                    mGetListIndex = mBaseNumber * mMaxNumber;
                }
                mRefreshFinished = false;
                GetApkListByPageReq req= new GetApkListByPageReq();
                req.setAssemblyId(mAssemblyId);
                req.setStart(mGetListIndex);
                req.setFixedLength(mMaxNumber);
                String contents = SenderDataProvider.buildToJSONData(getApplicationContext(),MessageCode.GET_APK_LIST_BY_PAGE,req);
                StartNetReqUtils.execListByPageRequest(mHandler, REFRESH_LIST,MessageCode.GET_APK_LIST_BY_PAGE,contents);
            }
        }else{
            if(mRefreshFinished){
                try
                {
                    mRefreshFinished = false;
                    mTopicReq.setChannelIndex(1);
                    mTopicReq.setTopicIndex(0);
                    mTopicReq.setTopicId(mCID);
                    mTopicReq.setItem_name(mTopicName);
                    String contents = SenderDataProvider.buildToJSONData(getApplicationContext(),MessageCode.GET_TOPIC_LIST,mTopicReq);
                    StartNetReqUtils.execListByPageRequest(mHandler,UPDATE_PAGE_MSG, MessageCode.GET_TOPIC_LIST, contents);
                }catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        } 
    }

    private List<AppInfoBto> getTopicApplist(GetTopicResp resp) {
        List<AssemblyInfoBto> assemblyList = resp.getAssemblyList();
        AssemblyInfoBto assemblyInfo = null;

        if (assemblyList == null || assemblyList.size() <= 0)
            return null;
        assemblyInfo = assemblyList.get(0);
        if (assemblyInfo == null)
            return null;

        mAssemblyId = assemblyInfo.getAssemblyId();

        if (assemblyInfo.getAppInfoListSize() <= 0)
            return null;

        return assemblyInfo.getAppInfoList();

    }

    private void setLayoutVisibility(boolean refreshButtonVisible) {
        if(mApp_list != null && mApp_list.size() > 0){
            if(mTopic_info_refresh.getVisibility() == View.VISIBLE){
                mTopic_info_refresh.setVisibility(View.GONE);
            }
            if(mTopic_info_loading.getVisibility() == View.VISIBLE){
                mTopic_info_loading.setVisibility(View.GONE);
            }
            if(mApp_listView.getVisibility() == View.GONE){
                mApp_listView.setVisibility(View.VISIBLE);
            }
        }else{
            if(mApp_listView.getVisibility() == View.VISIBLE){
                mApp_listView.setVisibility(View.GONE);
            }
            if(refreshButtonVisible){
                if(mTopic_info_loading.getVisibility() == View.VISIBLE){
                    mTopic_info_loading.setVisibility(View.GONE);
                }
                if(mTopic_info_refresh.getVisibility() == View.GONE){
                    mTopic_info_refresh.setVisibility(View.VISIBLE);
                }
            }else{
                if(mTopic_info_refresh.getVisibility() == View.VISIBLE){
                    mTopic_info_refresh.setVisibility(View.GONE);
                }
                if(mTopic_info_loading.getVisibility() == View.GONE){
                    mTopic_info_loading.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    
    
    private void asyncLoadImage() {
		if(mApp_adapter == null) {
			return;
		}
		ImageView imageView = null;
		AppInfoBto info = null;
		
		for (; mStartIndex <= mEndIndex; mStartIndex++) {
			info = (AppInfoBto) mApp_adapter.getItem(mStartIndex);
			if (info == null) {
				continue;
			}
			
			imageView = (ImageView) mApp_listView.findViewWithTag(info.getPackageName());
			if(imageView == null) {
				continue;
			}
			
			if (mApp_adapter.isAllowRefreshIcon() == false) {
				break;
			}
			
			int defaultResId = R.drawable.picture_bg1_big;
			int resId = (Integer) imageView.getTag(R.id.tag_image_resid);
			if (resId == defaultResId) {
				AsyncImageCache.from(mContext).displayImage(
						mApp_adapter.isAllowRefreshIcon(),
						imageView,
						R.drawable.picture_bg1_big,
						new AsyncImageCache.NetworkImageGenerator(info
								.getPackageName(), info.getImgUrl()), true);
			}
		}    
	}
    

	@Override
	protected void onDownloadServiceBind() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onApkDownloading(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onNoEnoughSpace(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
	    if(mApp_adapter != null)
            mApp_adapter.notifyDataSetChanged();
		
	}

	@Override
	protected void onSdcardLost(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
	    if(mApp_adapter != null)
            mApp_adapter.notifyDataSetChanged();
		
	}

	@Override
	protected void onDownloadHttpError(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
	    if(mApp_adapter != null)
            mApp_adapter.notifyDataSetChanged();
		
	}

	@Override
	protected void onDownloadProgressUpdate(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDownloadComplete(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
       if(mApp_adapter != null)
            mApp_adapter.notifyDataSetChanged();
		
	}

	@Override
	protected void onInstalling(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
		if(mApp_adapter != null)
		    mApp_adapter.notifyDataSetChanged();
		
	}
	@Override
	protected void onInstallSuccess(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
	  if(mApp_adapter != null)
	    mApp_adapter.notifyDataSetChanged();
	    
	}
	@Override
	protected void onInstallFailed(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onFileNotFound(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
       if(mApp_adapter != null)
            mApp_adapter.notifyDataSetChanged();
		
	}

	@Override
	protected void onFileNotUsable(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
	    if(mApp_adapter != null)
	        mApp_adapter.notifyDataSetChanged();
		
	}

    @Override
    public void startDownloadApp(String pacName, String appName,
            String filePath, String md5, String url, String topicId, String type, int verCode,
            int appId, long totalSize) {
        // TODO Auto-generated method stub
        try {
            addDownloadApkWithoutNotify(pacName, appName, md5, url, topicId, type, verCode, appId, totalSize);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }   
    }

    @Override
    public void startIconAnimation(String pacName, int versionCode,
            Drawable drawable, int fromX, int fromY) {
        // TODO Auto-generated method stub
        if(mPressInstallButtonAnimView != null)
            mPressInstallButtonAnimView.startDownloadAnim(
                pacName, 
                versionCode, 
                drawable, 
                fromX, 
                mDownloadLocation[0], 
                (fromY-mStatusBarHeight), 
                mDownloadLocation[1]);
    }


	@Override
	public boolean downloadPause(String pkgName, int verCode) {
		try {
			return pauseDownloadApk(pkgName, verCode);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

}
