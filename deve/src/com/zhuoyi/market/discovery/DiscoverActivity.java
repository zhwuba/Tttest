package com.zhuoyi.market.discovery;

import java.util.List;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.market.behaviorLog.LogDefined;
import com.market.behaviorLog.UserLogSDK;
import com.market.download.baseActivity.DownloadTabBaseActivity;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.net.data.DiscoverInfoBto;
import com.zhuoyi.market.R;
import com.zhuoyi.market.Splash;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.MarketUtils;

/**
 * 新的发现Activity
 * @author JLu
 *
 */
public class DiscoverActivity extends DownloadTabBaseActivity implements DownloadCallBackInterface {

    public static final int DISCOVER_VIEW = 0;
    public static final int LOADING_VIEW = 1;
    public static final int REFRESH_VIEW = 2;
    private RecyclerView mRecyclerView;
    private DiscoverAdapter mAdapter;
    private DiscoverUtils mDiscoverUtils;
    private LinearLayout mRequstLoading;
    private LinearLayout mRefreshLayout;
    private Button mRefreshButton;
    private LinearLayoutManager mLayoutManager;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_discover);
        View baseView = findViewById(R.id.base_layout);
		MarketUtils.setBaseLayout(baseView, this.getApplicationContext());
		findView();
	}


	@Override
    protected void onResume() {
        super.onResume();
        UserLogSDK.logActivityEntry(getApplicationContext(), UserLogSDK.getKeyDes(LogDefined.ACTIVITY_FIND));
    }


    @Override
    protected void onPause() {
        UserLogSDK.logActivityExit(getApplicationContext(), UserLogSDK.getKeyDes(LogDefined.ACTIVITY_FIND));
        super.onPause();
    }


    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if (mRecyclerView != null) {
    	    mRecyclerView.removeAllViews();
    	}
    	if (mAdapter != null) {
            mAdapter.freeDatas();
            mAdapter = null;
        }
    	if (mDiscoverUtils != null) {
            mDiscoverUtils.freeDatas();
            mDiscoverUtils = null;
        }
    }

    
    @Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (mAdapter != null) {
        	mAdapter.notifyDataChanged();
		}
	}


	private void findView() {
		mRecyclerView = (RecyclerView) findViewById(R.id.listview);
		mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new MyDecoration(getResources().getDimensionPixelSize(R.dimen.discover_item_divider_height)));
		mAdapter = new DiscoverAdapter(this,this);
		mRecyclerView.setAdapter(mAdapter);
		mRequstLoading = (LinearLayout) findViewById(R.id.search_loading);
		mRefreshLayout = (LinearLayout) findViewById(R.id.refresh_linearLayout);

		mDiscoverUtils = new DiscoverUtils(DiscoverActivity.this);
		mDiscoverUtils.requestDiscoverData(0,DiscoverUtils.GET_DISCOVER_DATA);
		
		mRefreshButton = (Button) findViewById(R.id.refresh_btn);
		mRefreshButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (MarketUtils.getAPNType(getApplicationContext()) == -1) {
					Toast.makeText(DiscoverActivity.this, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
					return;
				}
				show(LOADING_VIEW);
				mDiscoverUtils.reload();
			}
		});
		
		mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(int scrollState) {
			}

			@Override
			public void onScrolled(int dx, int dy) {
				int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = mLayoutManager.getItemCount();
                //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
                if (lastVisibleItem >= totalItemCount - 4 && dy > 0) {
                    if(mDiscoverUtils.mTopicIndex == -1){
                         return;
                    } 
                    mDiscoverUtils.load();
                }
			}
        });
		show(LOADING_VIEW);
	}
	
	
	public void initViewData(List<DiscoverInfoBto> mDiscoverInfos){
	    if (mDiscoverInfos == null) {
            return;
        }
	    mAdapter.addDatas(mDiscoverInfos);
	    mAdapter.setmTopicIndex(mDiscoverUtils.mTopicIndex);
	    mAdapter.notifyDataSetChanged();
	}
	
	
    /**
	 * 切换页面显示状态
	 * @param whichView 要显示的页面flag
	 */
	public void show(int whichView) {
		switch(whichView) {
		case DISCOVER_VIEW:
			mRecyclerView.setVisibility(View.VISIBLE);
			mRequstLoading.setVisibility(View.GONE);
			mRefreshLayout.setVisibility(View.GONE);
			break;
		case LOADING_VIEW:
			mRecyclerView.setVisibility(View.GONE);
			mRequstLoading.setVisibility(View.VISIBLE);
			mRefreshLayout.setVisibility(View.GONE);
			break;
		case REFRESH_VIEW:
			mRecyclerView.setVisibility(View.GONE);
			mRequstLoading.setVisibility(View.GONE);
			mRefreshLayout.setVisibility(View.VISIBLE);
			break;
        default:
            break;
		}
	}

	@Override
	public void startDownloadApp(String pacName, String appName,
			String filePath, String md5, String url, String topicId,
			String type, int verCode, int appId, long totalSize) {
		try {
		    addDownloadApkWithoutNotify(pacName, appName, md5, url, topicId, type, verCode, appId, totalSize);
		    if (mAdapter != null) {
	            mAdapter.notifyDataChanged();
	        }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
		
	}

	@Override
	public void startIconAnimation(String pacName, int versionCode,
			Drawable drawable, int fromX, int fromY) {
	    Splash.startDownloadAnim(pacName, versionCode, drawable, fromX, fromY);
	}

	@Override
	public boolean downloadPause(String pkgName, int verCode) {
		try {
		    if (mAdapter != null) {
                mAdapter.notifyDataChanged();
            }
			return pauseDownloadApk(pkgName, verCode);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected void onDownloadServiceBind() {
		
	}

	@Override
	protected void onApkDownloading(DownloadEventInfo eventInfo) {
		
	}

	@Override
	protected void onNoEnoughSpace(DownloadEventInfo eventInfo) {
	    if (mAdapter != null) {
            mAdapter.notifyDataChanged();
        }
	}

	@Override
	protected void onSdcardLost(DownloadEventInfo eventInfo) {
	    if (mAdapter != null) {
            mAdapter.notifyDataChanged();
        }
	}

	@Override
	protected void onDownloadHttpError(DownloadEventInfo eventInfo) {
	    if (mAdapter != null) {
            mAdapter.notifyDataChanged();
        }
	}

	@Override
	protected void onDownloadProgressUpdate(DownloadEventInfo eventInfo) {
	}

	@Override
	protected void onDownloadComplete(DownloadEventInfo eventInfo) {
	    if (mAdapter != null) {
            mAdapter.notifyDataChanged();
        }
	}

	@Override
	protected void onInstalling(DownloadEventInfo eventInfo) {
	    if (mAdapter != null) {
            mAdapter.notifyDataChanged();
        }
	}

	@Override
	protected void onInstallSuccess(DownloadEventInfo eventInfo) {
	    if (mAdapter != null) {
            mAdapter.notifyDataChanged();
        }
	}

	@Override
	protected void onInstallFailed(DownloadEventInfo eventInfo) {
		
	}

	@Override
	protected void onFileNotFound(DownloadEventInfo eventInfo) {
	    if (mAdapter != null) {
            mAdapter.notifyDataChanged();
        }
	}

	@Override
	protected void onFileNotUsable(DownloadEventInfo eventInfo) {
	    if (mAdapter != null) {
            mAdapter.notifyDataChanged();
        }
	}
	
	public class MyDecoration extends ItemDecoration {

	    private int mDividerHeight = 0;
	    
	    public MyDecoration(int dividerHeight) {
	        this.mDividerHeight = dividerHeight;
        }
	    
	    @Override
	    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
	    	if(itemPosition == 0) {
	    		outRect.set(0, getResources().getDimensionPixelSize(R.dimen.common_listview_padding_top), 0, 0);
	    	} else {
	    		outRect.set(0, mDividerHeight, 0, 0);
	    	}
	    }
	}
	
}
