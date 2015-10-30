package com.zhuoyi.market.discovery;

import java.util.HashMap;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.DiscoverInfoBto;
import com.market.net.request.GetDiscoverReq;
import com.market.net.response.GetDiscoverResp;
import com.market.net.response.GetMarketFrameResp;
import com.market.net.utils.StartNetReqUtils;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.utils.FrameInfoCache;

/**
 * {发现}
 *  <br>
 * Create on : 2015-9-17 下午7:27:58<br>
 * @author pc<br>
 * @version zhuoyiStore v0.0.1
 * 
 */
public class DiscoverUtils {

    public static final int GET_DISCOVER_DATA = 1;
    public static final int GET_MORE_DATA = 2;
    public int mTopicIndex = -1;
    private DiscoverActivity mContext;
    private Handler mHandler;
    private String marketId = "";
    
    /**
     * 刷新结束的flag,防止反复点击刷新按钮
     */
    private boolean mRefreshFinished = false;
    private boolean mIsLoadingMore = false;
    private boolean isBottom = false;
    
    public DiscoverUtils(DiscoverActivity context) {
        this.mContext = context;
        GetMarketFrameResp marketFrameResp = MarketApplication.getMarketFrameResp();
        marketId = marketFrameResp.getMarketId();
        init();
    }

    private void init() {
        mHandler = new Handler() {
            public void handleMessage(Message message) {
                HashMap<String, Object> map = null;
                List<DiscoverInfoBto> mDiscoverInfos = null;

                switch (message.what) {
                case GET_DISCOVER_DATA:
                    GetDiscoverResp mDiscoverResp = null;
                    map = (HashMap<String, Object>) message.obj;
                    if(map != null && map.size()>0) {
                        mDiscoverResp = (GetDiscoverResp) map.get("discoverResp");
                        map.clear();
                    } 
                    if (mContext == null) { 
                        break;
                    }
                    if(mDiscoverResp != null ) {
                        mDiscoverInfos = mDiscoverResp.getDiscoverList();
                        if (mDiscoverInfos != null && mDiscoverInfos.size()>0) {
                            if (mDiscoverResp.getTopicIndex() != null) {
                                mTopicIndex = mDiscoverResp.getTopicIndex();
                            }
                        	mContext.initViewData(mDiscoverInfos);
                        	mContext.show(DiscoverActivity.DISCOVER_VIEW);
                        } else {
                    		mContext.show(DiscoverActivity.REFRESH_VIEW);
                        }
                        if (mTopicIndex == -1) {
                            isBottom = true;
                        }
                    } else {
                		mContext.show(DiscoverActivity.REFRESH_VIEW);
                    }
                    mRefreshFinished = true;
                    break;
                case GET_MORE_DATA:
                    GetDiscoverResp mRefreshData = null;
                    map = (HashMap<String, Object>) message.obj;
                    if(map != null && map.size()>0) {
                        mRefreshData = (GetDiscoverResp) map.get("discoverResp");
                        map.clear();
                    } 
                    if(mRefreshData != null && mContext != null) {
                        mDiscoverInfos = mRefreshData.getDiscoverList();
                        if (mRefreshData.getTopicIndex() != null) {
                            mTopicIndex = mRefreshData.getTopicIndex();
                        }
                    	mContext.initViewData(mDiscoverInfos);
                    	if (mTopicIndex == -1) {
                            isBottom = true;
                        }
                    } 
                    mIsLoadingMore = false;
                    mRefreshFinished = true;
                    break;
                }
                super.handleMessage(message);
            }
        };
    }

    public void requestDiscoverData(int topicIndex,int whatCode){
        if (isBottom) {
            return;
        }
        try {
            GetDiscoverReq discoverReq = new GetDiscoverReq();
            discoverReq.setMarketId(marketId);
            discoverReq.setTopicIndex(topicIndex);
            discoverReq.setIsWide(0);
            discoverReq.setTerminalInfo(SenderDataProvider.generateTerminalInfo(mContext));
            String contents = SenderDataProvider.buildToJSONData(mContext,MessageCode.GET_DISCOVER_DATA,discoverReq);
            StartNetReqUtils.execListByPageRequest(mHandler, GET_DISCOVER_DATA, MessageCode.GET_DISCOVER_DATA, contents);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void reload() {
        if (mRefreshFinished) {
            mRefreshFinished = false;
            requestDiscoverData(0,GET_DISCOVER_DATA);
        }
    }
    
    public void load() {
		if (mIsLoadingMore) {
			return;
		}
		loadMore();
	}
    
    private void loadMore() {
    	mIsLoadingMore = true;
        requestDiscoverData(mTopicIndex,GET_MORE_DATA);
    }

    public void freeDatas(){
        if (mHandler != null) {
            mHandler = null;
        }
        mContext = null;
    }
}
