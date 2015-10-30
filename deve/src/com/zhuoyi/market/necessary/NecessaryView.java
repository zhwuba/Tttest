package com.zhuoyi.market.necessary;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.AppInfoBto;
import com.market.net.data.AssemblyInfoBto;
import com.market.net.request.GetTopicReq;
import com.market.net.response.GetTopicResp;
import com.market.net.utils.StartNetReqUtils;
import com.market.statistics.ReportFlag;
import com.zhuoyi.market.R;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.MarketUtils;

public class NecessaryView implements OnClickListener{
	
	private int channelIndex;
	private int topicIndex;

	private Context mContext;
	private View mView_Necessary;
	private Button btn_onekey_install;
	private ExpandableListView mListView_list;
	private Button btn_refresh; 
	private LinearLayout mLinearLayout_refresh;
	private LinearLayout mLinearLayout_loading;
	private ImageView mAllInstalledImage = null;
	
	private View mView_Footer;
	private ProgressBar mFooterProgress;
	private TextView mFooterText;
	
	private int mCurrentDataNum = 0;
	private GetTopicReq mTopicReq;
	private DownloadCallBackInterface mDownloadCallBack;
	
	public static List<AppInfoBto> mInstallAppList = null;
	private Map<String, List<AppInfoBto>> mChildList;
    private ArrayList<String> mGroupList;
    private NecessaryInstallAdapter mNecessaryInstallAdapter;
    
    private int mPosition;
    
    private final int VISIBLE_LOADING = 0;
    private final int VISIBLE_REFRESH = 1;
    private final int VISIBLE_DATA = 2;
    private final int VISIBLE_ALLINSTALLED = 3;
	
	private final int MSG_UPDATE_PAGE = 0; 
	private final int MSG_REFRESH_LIST = 1;
	
	private boolean mRefreshFinished = true;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			HashMap<String, Object> map;
			switch (msg.what) {
			case MSG_UPDATE_PAGE:
				GetTopicResp mGetTopicResp = new GetTopicResp();
				map = (HashMap<String, Object>) msg.obj;
				if (map != null && map.size() > 0) {
					mGetTopicResp = (GetTopicResp) map.get("topicResp");
                    map.clear();
                    getTopicApplist(mGetTopicResp);
                } else {
                	mRefreshFinished = true;
                	setViewVisibility(VISIBLE_REFRESH);
                } 
				break;
			case MSG_REFRESH_LIST:
                if (msg.arg1 == 1) {
                	if(mCurrentDataNum == 0) {
                		setViewVisibility(VISIBLE_ALLINSTALLED);
                		mAllInstalledImage.setImageResource(R.drawable.installed_all_apps);
                		return;
                	}
                	setViewVisibility(VISIBLE_DATA);
                    if (mNecessaryInstallAdapter == null) {
                        mNecessaryInstallAdapter = new NecessaryInstallAdapter(mContext, mGroupList, mChildList, mListView_list, mDownloadCallBack);
                        mNecessaryInstallAdapter.setReportFlag(ReportFlag.FROM_NULL);
                        mNecessaryInstallAdapter.setTopicId(MarketUtils.getTopicId(channelIndex, topicIndex));
                        mListView_list.addFooterView(mView_Footer);
                        mListView_list.setAdapter(mNecessaryInstallAdapter);
                    }
                    if (mCurrentDataNum > 0) {
                        mNecessaryInstallAdapter.setInstallList(mChildList, mGroupList);
                        mNecessaryInstallAdapter.notifyDataSetChanged();
                    }
                } else {
                	setViewVisibility(VISIBLE_LOADING);
                	startRequestData();
                }
                mRefreshFinished = true;
                break;
			default:
				break;
			}
		};
	};
	
	public void notifyDataSetChanged() {
		if (mNecessaryInstallAdapter != null) {
			mNecessaryInstallAdapter.setInstallList(mChildList, mGroupList);
			mNecessaryInstallAdapter.notifyDataSetChanged();
		}
        
    }
	
	public NecessaryView(Context context, DownloadCallBackInterface downloadCallback,int position, int channelIndex, int topicIndex) {
		this.mContext = context;
		this.mPosition = position;
		this.channelIndex = channelIndex;
		this.topicIndex = topicIndex;
		mDownloadCallBack = downloadCallback;
		if (mContext == null) {
			mContext = MarketApplication.getRootContext();
		}
		mView_Necessary = LayoutInflater.from(mContext).inflate(R.layout.necessary_main_view, null);
		mTopicReq = new GetTopicReq();
		
	}
	
	public View getView() {
		return mView_Necessary;
	}
	
	
	public void entryNecessaryView() {
		findview();
		initview();
	}
	

	private void findview() {
		mView_Footer = LayoutInflater.from(mContext).inflate(R.layout.foot, null);
        mFooterProgress = (ProgressBar) mView_Footer.findViewById(R.id.footer_progress);
        mFooterText = (TextView) mView_Footer.findViewById(R.id.footer_textview);
        mFooterProgress.setVisibility(View.GONE);
        mFooterText.setText(mContext.getString(R.string.loaded_all_data));
		
		btn_onekey_install = (Button) mView_Necessary.findViewById(R.id.one_key_install);
		mListView_list = (ExpandableListView) mView_Necessary.findViewById(R.id.listview_necessary);
		mListView_list.setGroupIndicator(null);
		mListView_list.setTag(R.id.list_position, mPosition);
		mLinearLayout_refresh = (LinearLayout) mView_Necessary.findViewById(R.id.necessary_refresh_layout);
		mLinearLayout_loading = (LinearLayout) mView_Necessary.findViewById(R.id.necessary_loading);
		btn_refresh = (Button) mView_Necessary.findViewById(R.id.refresh_btn);
		mAllInstalledImage = (ImageView) mView_Necessary.findViewById(R.id.installed_all_apps_image);
		
	}
	
	
	private void initview() {
		
		btn_refresh.setOnClickListener(this);
		btn_onekey_install.setOnClickListener(this);
		setViewVisibility(VISIBLE_LOADING);
		mInstallAppList = new ArrayList<AppInfoBto>();
		
		startRequestData();
	}
	
	
	private void startRequestData() {
		if (mRefreshFinished) {
			try {
				mRefreshFinished = false;
				 mTopicReq.setChannelIndex(channelIndex);
				 mTopicReq.setTopicIndex(topicIndex);
				String contents = SenderDataProvider.buildToJSONData(mContext, MessageCode.GET_TOPIC_LIST, mTopicReq);
				StartNetReqUtils.execListByPageRequest(mHandler, MSG_UPDATE_PAGE, MessageCode.GET_TOPIC_LIST, contents);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private void getTopicApplist(GetTopicResp resp) {
        mGroupList = new ArrayList<String>();
        mChildList = new HashMap<String, List<AppInfoBto>>();
        int result = 0;
        Message msg = null;
        try {
            while (true) {
                List<AssemblyInfoBto> assemblyList = resp.getAssemblyList();
                if (assemblyList == null || assemblyList.size() <= 0)
                    break;
                for (AssemblyInfoBto assemblyInfoBto : assemblyList) {
                    List<AppInfoBto> appInfoList = assemblyInfoBto.getAppInfoList();
                    if (appInfoList == null) {
                        continue;
                    }
                    for (Iterator<AppInfoBto> iterator = appInfoList.iterator(); iterator.hasNext();) {
                        AppInfoBto appInfo = (AppInfoBto) iterator.next();
                        appInfo.setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(), false));
                        if (appInfo.getIsShow()) {
                            mInstallAppList.add(appInfo);
                            mCurrentDataNum++;
                        } else {
                            iterator.remove();
                        }
                    }
                    if (appInfoList != null && appInfoList.size() > 0) {
                        mGroupList.add(assemblyInfoBto.getAssName());
                        mChildList.put(assemblyInfoBto.getAssName(), appInfoList);
                    }
                }
                result = 1;
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        msg = new Message();
        msg.what = MSG_REFRESH_LIST;
        msg.arg1 = result;
        if(mHandler != null)
        	mHandler.sendMessage(msg);
    }
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.one_key_install:
			Intent intent = new Intent(mContext, NecessaryDialogActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			mContext.startActivity(intent);
			break;
		case R.id.refresh_btn:
			if (MarketUtils.getAPNType(mContext) == -1) {
				Toast.makeText(mContext, mContext.getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
				return;
			}
			startRequestData();
			mLinearLayout_loading.setVisibility(View.VISIBLE);
			mLinearLayout_refresh.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}
	
	
	public void setViewVisibility(int flag) {
		if(flag == VISIBLE_LOADING) {
			mLinearLayout_loading.setVisibility(View.VISIBLE);
			mLinearLayout_refresh.setVisibility(View.GONE);
			mListView_list.setVisibility(View.GONE);
			btn_onekey_install.setVisibility(View.GONE);
			mAllInstalledImage.setVisibility(View.GONE);
		} else if(flag == VISIBLE_REFRESH){
			mLinearLayout_loading.setVisibility(View.GONE);
			mLinearLayout_refresh.setVisibility(View.VISIBLE);
			mListView_list.setVisibility(View.GONE);
			btn_onekey_install.setVisibility(View.GONE);
			mAllInstalledImage.setVisibility(View.GONE);
		} else if(flag == VISIBLE_DATA){
			mLinearLayout_loading.setVisibility(View.GONE);
			mLinearLayout_refresh.setVisibility(View.GONE);
			mListView_list.setVisibility(View.VISIBLE);
			btn_onekey_install.setVisibility(View.VISIBLE);
			mAllInstalledImage.setVisibility(View.GONE);
		} else if(flag == VISIBLE_ALLINSTALLED){
			mLinearLayout_loading.setVisibility(View.GONE);
			mLinearLayout_refresh.setVisibility(View.GONE);
			mListView_list.setVisibility(View.GONE);
			btn_onekey_install.setVisibility(View.GONE);
			mAllInstalledImage.setVisibility(View.VISIBLE);
		}
	}
	
	public void freeViewResource() {
        if(mHandler!=null)
            mHandler = null;
        
        if(mInstallAppList  != null) {
        	mInstallAppList = null;
        }
        
        if(mNecessaryInstallAdapter!=null) {
            mNecessaryInstallAdapter.freeImageCache();
        }
    }

}
