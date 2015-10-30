package com.zhuoyi.market.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.market.behaviorLog.LogDefined;
import com.market.behaviorLog.UserLogSDK;
import com.market.net.data.DiscoverAppInfoBto;
import com.market.net.data.DiscoverInfoBto;
import com.market.statistics.ReportFlag;
import com.zhuoyi.market.OneColModelActivity;
import com.zhuoyi.market.R;
import com.zhuoyi.market.Splash;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.topic.TopicImgModelActivity;
import com.zhuoyi.market.topic.TopicInfoActivity;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.view.CustomViewFactory;
import com.zhuoyi.market.wallpaper.WallpaperActivity;

public class DiscoverAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_NOVEL = 15;
    public static final int TYPE_WALLPAPER = 12;
    public static final int TYPE_GUESS_YOU_LIKE = 13;
    public static final int TYPE_TOPIC = 14;
    public static final int TYPE_FOOTER = Integer.MAX_VALUE;
    private static final String RED_DOT_TAG = "show";
    private static final String TOPIC_RELEASE_TAG = "topic";
    
    private DiscoverActivity mContext;
    private List<DiscoverInfoBto> mDiscoverInfos = new ArrayList<DiscoverInfoBto>();
    private DownloadCallBackInterface callBack;
    private Intent intent;
    private Map<RecyclerView, NormalRecylerAdapter> mRecylerViewState;
    private int mTopicBigWidth;
    private int mTopicBigHeight;
    private int mTopicSmallSize;
    private int mTopicIndex = -1;
    private AsyncImageCache mAsyncImageCache;
    private int mStartIndex;
    private int mEndIndex;

    public DiscoverAdapter(DiscoverActivity context, DownloadCallBackInterface callBack) {
        mContext = context;
        this.callBack = callBack;
        intent = new Intent();
        mRecylerViewState = new HashMap<RecyclerView, NormalRecylerAdapter>();
        mAsyncImageCache = AsyncImageCache.from(mContext);
    }


    public void addDatas(List<DiscoverInfoBto> discoverInfos){
        if (this.mDiscoverInfos == null) {
            this.mDiscoverInfos = new ArrayList<DiscoverInfoBto>();
        }
        if (discoverInfos != null) {
            this.mDiscoverInfos.addAll(discoverInfos);
        }
    }
    
    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public int getItemCount() {
        return mDiscoverInfos == null ? 1 : mDiscoverInfos.size()+1;
    }


    @Override
    public int getItemViewType(int position) {
        if (position == mDiscoverInfos.size()) {
            return TYPE_FOOTER;
        }
        int type = mDiscoverInfos.get(position).getType();
        return type;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = null;
        if (i == TYPE_FOOTER) {
            view = View.inflate(viewGroup.getContext(),R.layout.discover_foot, null);
            return new FooterHolder(view);
        }
        if (i == TYPE_NOVEL) {
            view = View.inflate(viewGroup.getContext(), R.layout.discover_item_type_4, null);
            return new NormalHolder(view);
        }
        if (i == TYPE_WALLPAPER) {
            view = View.inflate(viewGroup.getContext(), R.layout.discover_item_type_3, null);
            return new NormalHolder(view);
        }
        if (i == TYPE_GUESS_YOU_LIKE) {
            view = View.inflate(viewGroup.getContext(), R.layout.discover_item_type_1, null);
            return new NormalHolder(view);
        }
        if (i == TYPE_TOPIC) {
            view = View.inflate(viewGroup.getContext(), R.layout.discover_item_type_2, null);
            return new TopicHolder(view);
        }
        view = View.inflate(viewGroup.getContext(), R.layout.discover_item_type_1, null);
        return new NormalHolder(view);
    };


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof FooterHolder) {
            bindFooterItem((FooterHolder) viewHolder, i);
        } else if (viewHolder instanceof NormalHolder) {
            bindNormalItem((NormalHolder) viewHolder, i);
        } else if (viewHolder instanceof TopicHolder) {
            bindTopicItem((TopicHolder) viewHolder, i);
        } else {
            bindOtherItem();
        }
    }


    private void bindFooterItem(FooterHolder viewHolder, int i) {
        if (mTopicIndex == -1) { 
            /** 加载完成 */
            viewHolder.mFooterProgress.setVisibility(View.INVISIBLE);
            viewHolder.mFooterText.setText(mContext.getString(R.string.loaded_all_data));
        }
    }


    private void initLayoutManager(final RecyclerView recyclerView,int adapterType,DiscoverInfoBto discoverInfoBto,NormalHolder viewHolder) {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setClipToPadding(false);
        final NormalRecylerAdapter adapter = new NormalRecylerAdapter(mContext, callBack, adapterType,discoverInfoBto.getTopicId()+RED_DOT_TAG,viewHolder);
        adapter.addDiacoverAppData(discoverInfoBto.getAppList());
        String downFlag = ReportFlag.getReportFlag(ReportFlag.FROM_DISCOVERY, -1, false,
                                                   ReportFlag.CHILD_ID_TYPE_TOPICID, discoverInfoBto.topicId);
        
        adapter.setReportFlag(downFlag);
        mRecylerViewState.put(recyclerView, adapter);
        recyclerView.setAdapter(adapter);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            
            @Override
            public void onScrolled(int dx, int dy) {
                mStartIndex = layoutManager.findFirstVisibleItemPosition();
                mEndIndex = layoutManager.findLastVisibleItemPosition();
            }
            
            @Override
            public void onScrollStateChanged(int state) {
                switch (state) { 
                case OnScrollListener.SCROLL_STATE_IDLE:
                    if(adapter!=null) {        
                        adapter.allowRefreshIcon(true);
                        asyncLoadImage(adapter,recyclerView);
                    }
                    break;
                case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                case OnScrollListener.SCROLL_STATE_FLING:
                    if(adapter!=null)
                        adapter.allowRefreshIcon(false);
                    break;
                }
            }
        });
    }


    /**
     * {填充桌面壁纸，热门小说，男女生最爱数据}
     * 
     * @param viewHolder
     * @param position
     */
    private void bindNormalItem(NormalHolder viewHolder, int position) {
        int adapterType = getItemViewType(position);
        viewHolder.mAppTitle.setText(mDiscoverInfos.get(position).getTopicName());
        isShowRedDot(viewHolder.mARedDot, mDiscoverInfos.get(position).getTopicId()+RED_DOT_TAG);
        initLayoutManager(viewHolder.mRecyclerView,adapterType,mDiscoverInfos.get(position),viewHolder);
        viewHolder.mTitle.setOnClickListener(new MyOnClickListener(adapterType,position,viewHolder.mARedDot));
    }


    /**
     * {填充专题数据}
     * 
     * @param viewHolder
     * @param position
     */
    private void bindTopicItem(final TopicHolder viewHolder, final int position) {
    	
    	mTopicBigWidth = mContext.getResources().getDimensionPixelSize(R.dimen.discover_item_width);
    	mTopicBigHeight = mContext.getResources().getDimensionPixelSize(R.dimen.discover_topic_big_height);
    	mTopicSmallSize = mContext.getResources().getDimensionPixelSize(R.dimen.discover_topic_small_size);
    	
        final DiscoverInfoBto discoverInfoBto = mDiscoverInfos.get(position);
        viewHolder.mTopicTitle.setText(discoverInfoBto.getTopicName());
        viewHolder.mTopicTitle.setOnClickListener(new TopicClick(discoverInfoBto,viewHolder));
        isShowRedDot(viewHolder.mTRedDot, discoverInfoBto.getTopicId()+RED_DOT_TAG);
        viewHolder.mTopicAll.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                showAllTopic(viewHolder, discoverInfoBto,position);
            }
        });
        /** 显示专题大图 */
        int defaultPic = -1;
        if(MarketUtils.isNoPicModelReally()) {
            defaultPic = R.drawable.logo_no_network;
        } else {
            defaultPic = R.drawable.logo_no_network_withtxt;
        }

        mAsyncImageCache.displayImage(true,false,viewHolder.mTopicBig, defaultPic,mTopicBigWidth,mTopicBigHeight,
                new AsyncImageCache.NetworkImage565Generator(MarketUtils.getImgUrlKey(discoverInfoBto.getImageUrl()),discoverInfoBto.getImageUrl()), false,true,false,TOPIC_RELEASE_TAG);
        viewHolder.mTopicBig.setOnClickListener(new TopicClick(discoverInfoBto,viewHolder));
        /** 显示专题大图---end */
        
        /** 显示三个专题小图  */
        showSmailTopic(viewHolder,discoverInfoBto.getAppList());
        viewHolder.bottom.setOnClickListener(new TopicClick(discoverInfoBto,viewHolder));
        /** 显示三个专题小图---end  */
    }

    public class TopicClick implements OnClickListener {
        
        private DiscoverInfoBto mDiscoverInfo; 
        private TopicHolder mViewHolder;
        
        public TopicClick(DiscoverInfoBto discoverInfoBto,TopicHolder viewHolder) {
            this.mDiscoverInfo = discoverInfoBto;
            this.mViewHolder = viewHolder;
        }
        
        @Override
        public void onClick(View arg0) {
            Intent intent = new Intent(mContext, TopicInfoActivity.class);
            intent.putExtra("mCID", mDiscoverInfo.getSonTopicId());
            intent.putExtra("mTopicName", mDiscoverInfo.getTitle());
            intent.putExtra("mTopicInfo", mDiscoverInfo.getDesc());
            intent.putExtra("mTopicImage", mDiscoverInfo.getTitle());
            intent.putExtra("imageUrl", mDiscoverInfo.getImageUrl());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            clearItemRedDot(mViewHolder.mTRedDot, mDiscoverInfo.getTopicId() + RED_DOT_TAG);
        }
    };
    
    private void showSmailTopic(TopicHolder viewHolder,List<DiscoverAppInfoBto> appInfos) {
        if (appInfos == null || appInfos.size() == 0) {
            viewHolder.mTopic1.setVisibility(View.GONE);
            viewHolder.mTopic2.setVisibility(View.GONE);
            viewHolder.mTopic3.setVisibility(View.GONE);
            return;
        }
        int defaultPic = R.drawable.topic_small_default;
        viewHolder.mTopic1.setVisibility(View.VISIBLE);
        mAsyncImageCache.displayImage(true, false, viewHolder.mTopic1, defaultPic, mTopicSmallSize, mTopicSmallSize,
            new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(appInfos.get(0).getPackageName()), appInfos.get(0).getImageUrl()), false, true, false, TOPIC_RELEASE_TAG);
        if (appInfos.size() > 1) {
            viewHolder.mTopic2.setVisibility(View.VISIBLE);
            mAsyncImageCache.displayImage(true, false, viewHolder.mTopic2, defaultPic, mTopicSmallSize, mTopicSmallSize,
                new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(appInfos.get(1).getPackageName()), appInfos.get(1).getImageUrl()), false, true, false, TOPIC_RELEASE_TAG);
        }
        if (appInfos.size() > 2) {
            viewHolder.mTopic3.setVisibility(View.VISIBLE);
            mAsyncImageCache.displayImage(true, false, viewHolder.mTopic3, defaultPic, mTopicSmallSize, mTopicSmallSize,
                new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(appInfos.get(2).getPackageName()), appInfos.get(2).getImageUrl()), false, true, false, TOPIC_RELEASE_TAG);
        }
    }


    private void showAllTopic(TopicHolder viewHolder, DiscoverInfoBto discoverInfoBto,int position){
        clearItemRedDot(viewHolder.mTRedDot, discoverInfoBto.getTopicId() + RED_DOT_TAG);
        intent.setClass(mContext, TopicImgModelActivity.class);
        intent.putExtra("titleName", discoverInfoBto.getTopicName());
        intent.putExtra("topicId", discoverInfoBto.getTopicId());
        intent.putExtra("logDes", UserLogSDK.getKeyDes(LogDefined.ACTIVITY_FIND_CLUB));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(intent);
    }
    

    /**
     * {填充其他可配置类型数据}
     */
    private void bindOtherItem() {
    	
    }

    
    private void isShowRedDot(ImageView red_dot, String key){
        boolean isShow = MarketUtils.getDiscoverSPBoolean(mContext, key, false);
        if (isShow) {
            red_dot.setVisibility(View.VISIBLE);
        } else {
            red_dot.setVisibility(View.GONE);
        }
    }
    
    
    private void clearItemRedDot(ImageView red_dot, String key){
        if (red_dot.getVisibility() == View.VISIBLE) {
            red_dot.setVisibility(View.GONE);
            MarketUtils.mRedDotCount--;
            MarketUtils.setDiscoverSPBoolean(mContext, key, false);
        }
        chearChannelRedDot();
    }
    
    
    private void chearChannelRedDot(){
    	if (MarketUtils.mRedDotCount == 0) {
            Intent clearNotify = new Intent(Splash.CLEAR_NOTIFY);
            mContext.sendBroadcast(clearNotify);
        }
    }
    
    public void notifyDataChanged() {
        if (mRecylerViewState != null) {
            Set<RecyclerView> keySet = mRecylerViewState.keySet();
            for (RecyclerView recyclerView : keySet) {
                mRecylerViewState.get(recyclerView).notifyDataSetChanged();
            }
        }
    }
    
    public void freeDatas(){
        if (mRecylerViewState != null) {
            Set<RecyclerView> keySet = mRecylerViewState.keySet();
            for (RecyclerView recyclerView : keySet) {
                NormalRecylerAdapter normalRecylerAdapter = mRecylerViewState.get(recyclerView);
                normalRecylerAdapter.freeDatas();
                normalRecylerAdapter = null;
                recyclerView.removeAllViews();
            }
            mRecylerViewState.clear();
        }
        if (mDiscoverInfos != null) {
            mDiscoverInfos.clear();
            mDiscoverInfos = null;
        }
        if (mAsyncImageCache != null) {
            mAsyncImageCache.stop();
            mAsyncImageCache.releaseRes();
            mAsyncImageCache = null;
        }
        mContext = null;
    }
    
    public int getmTopicIndex() {
        return mTopicIndex;
    }


    public void setmTopicIndex(int mTopicIndex) {
        this.mTopicIndex = mTopicIndex;
    }

    
    private void asyncLoadImage(NormalRecylerAdapter adapter,RecyclerView recyclerView) {
        if(adapter == null) {
            return;
        }
        adapter.asyncLoadImage(mStartIndex, mEndIndex, recyclerView); 
    }
    
    
    /**
     * {适用于热门小说，桌面壁纸，男神最爱，女神最爱...}
     */
    public class NormalHolder extends RecyclerView.ViewHolder {

        private RelativeLayout mTitle;
        private TextView mAppTitle;
        public ImageView mARedDot;
        private RecyclerView mRecyclerView;


        public NormalHolder(View itemView) {
            super(itemView);
            mTitle = (RelativeLayout) itemView.findViewById(R.id.title);
            mAppTitle = (TextView) mTitle.findViewById(R.id.item_title);
            mARedDot = (ImageView) mTitle.findViewById(R.id.red_dot);
            mRecyclerView = (RecyclerView) itemView.findViewById(R.id.recyclerView);
        }
    }

    /**
     * {适用于专题}
     */
    public class TopicHolder extends RecyclerView.ViewHolder {
        private RelativeLayout mTitle;
        private RelativeLayout bottom;
        private TextView mTopicTitle;
        private ImageView mTRedDot;
        private TextView mTopicAll;
        private ImageView mTopicBig;
        private ImageView mTopic1;
        private ImageView mTopic2;
        private ImageView mTopic3;

        public TopicHolder(View itemView) {
            super(itemView);
            mTitle = (RelativeLayout) itemView.findViewById(R.id.title);
            bottom = (RelativeLayout) itemView.findViewById(R.id.bottom);
            mTopicTitle = (TextView) mTitle.findViewById(R.id.item_title);
            mTRedDot = (ImageView) mTitle.findViewById(R.id.red_dot);
            mTopicAll = (TextView) mTitle.findViewById(R.id.item_all);
            mTopicBig = (ImageView) itemView.findViewById(R.id.topic_big);
            mTopic1 = (ImageView) itemView.findViewById(R.id.topic_1);
            mTopic2 = (ImageView) itemView.findViewById(R.id.topic_2);
            mTopic3 = (ImageView) itemView.findViewById(R.id.topic_3);
        }
    }

    /**
     * {适用于其他可配置分类}
     */
    public class OthersHolder extends RecyclerView.ViewHolder {

        public OthersHolder(View itemView) {
            super(itemView);
        }
    }

    public class FooterHolder extends RecyclerView.ViewHolder {

        private TextView mFooterText;
        private ProgressBar mFooterProgress;
        public FooterHolder(View itemView) {
            super(itemView);
            mFooterText = (TextView) itemView.findViewById(R.id.footer_textview);
            mFooterProgress = (ProgressBar) itemView.findViewById(R.id.footer_progress);
        }
    }
    
    
    public class MyOnClickListener implements OnClickListener {
        
        private int mType = 0;
        private int mPosition = 0;
        private ImageView mDot = null;
        private DiscoverInfoBto mDiscoverInfo;
        
        private MyOnClickListener(int type, int position, ImageView dot) {
            this.mPosition = position;
            this.mType = type;
            this.mDot = dot;
            this.mDiscoverInfo = mDiscoverInfos.get(position);
        }

        @Override
        public void onClick(View arg0) {
            switch (mType) {
            case TYPE_WALLPAPER:
                intent.setClass(mContext, WallpaperActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                break;
            case TYPE_NOVEL:
                intent.setClass(mContext, NovelActivity.class);
                intent.putExtra("titleName", mDiscoverInfo.getTopicName());
                intent.putExtra("channelIndex", 4);
                intent.putExtra("topicIndex", mPosition);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                break;
            case TYPE_GUESS_YOU_LIKE:
                intent.setClass(mContext, OneColModelActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("titleName", mDiscoverInfo.getTopicName());
                intent.putExtra("viewType", CustomViewFactory.VIEW_GUESS_YOU_LIKE);
                break;
            default:
                CustomViewFactory.setmTopicIndex(mPosition);
            	if (mDiscoverInfo.getTopicName().equals("男神最爱")) {
            		intent.putExtra("viewType", CustomViewFactory.VIEW_BOY_FAVOURITE);
				} else if (mDiscoverInfo.getTopicName().equals("女神最爱")) {
					intent.putExtra("viewType", CustomViewFactory.VIEW_GIRL_FAVOURITE);
				} else {
					intent.putExtra("viewType", CustomViewFactory.VIEW_OTHERS);
				}
            	intent.setClass(mContext, OneColModelActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("titleName", mDiscoverInfo.getTopicName());
                break;
            }
            mContext.startActivity(intent);
            clearItemRedDot(mDot, mDiscoverInfo.getTopicId() + RED_DOT_TAG);
        }
    }
    
}
