package com.zhuoyi.market.discovery;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.market.net.data.CornerIconInfoBto;
import com.market.net.data.DiscoverAppInfoBto;
import com.zhuoyi.market.R;
import com.zhuoyi.market.Splash;
import com.zhuoyi.market.appdetail.AppDetailInfoActivity;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.discovery.DiscoverAdapter.NormalHolder;
import com.zhuoyi.market.utils.AppOperatorUtils;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.wallpaper.WallpaperDetail;

public class NormalRecylerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private WeakReference<DownloadCallBackInterface> mDownloadCallBack;
    private int mAdapterType = 0;
    private List<DiscoverAppInfoBto> mDiscoverAppInfoBtos = null;
    private int mWallpaperWidth,mWallpaperHeight;
    private int mNovelWidth,mNovelHeight;
    private AsyncImageCache mAsyncImageCache;
    private String mTopicName;
    private NormalHolder mNormalHolder;
    private boolean mRefreshIcon = true;
    private String mReportFlag = "";
    private int mTopicId = -1;

    public NormalRecylerAdapter(Context context, DownloadCallBackInterface callBack, int adapterType,String topicName,NormalHolder normalHolder) {
        super();
        this.mContext = context;
        mDownloadCallBack = new WeakReference<DownloadCallBackInterface>(callBack);
        this.mAdapterType = adapterType;
        this.mTopicName = topicName;
        this.mNormalHolder = normalHolder;
        mDiscoverAppInfoBtos = new ArrayList<DiscoverAppInfoBto>();
        mAsyncImageCache = AsyncImageCache.from(mContext);
    }

    public void addDiacoverAppData(List<DiscoverAppInfoBto> discoverAppInfoBtos){
        if (this.mDiscoverAppInfoBtos == null) {
            this.mDiscoverAppInfoBtos = new ArrayList<DiscoverAppInfoBto>();
        }
        this.mDiscoverAppInfoBtos.addAll(discoverAppInfoBtos);
    }
    

    public void setReportFlag(String flag) {
        mReportFlag = flag;
    }


    public void setTopicId(int topicId) {
        mTopicId = topicId;
    }

    
    public void allowRefreshIcon(boolean status) {
        mRefreshIcon = status;
    }
    
    
    public boolean isAllowRefreshIcon() {
        return mRefreshIcon;
    }
    

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = null;
        if (mAdapterType == DiscoverAdapter.TYPE_WALLPAPER) {
            view = View.inflate(viewGroup.getContext(), R.layout.discover_wallpaper_item, null);
            mWallpaperWidth = mContext.getResources().getDimensionPixelSize(R.dimen.discover_wallpaper_width);
            mWallpaperHeight = mContext.getResources().getDimensionPixelSize(R.dimen.discover_wallpaper_height);
            return new WallPaperHolder(view);
        }
        if (mAdapterType == DiscoverAdapter.TYPE_NOVEL) {
        	mNovelWidth = mContext.getResources().getDimensionPixelSize(R.dimen.discover_novel_cover_img_width);
        	mNovelHeight = mContext.getResources().getDimensionPixelSize(R.dimen.discover_novel_cover_img_height);
            view = View.inflate(viewGroup.getContext(), R.layout.discover_hotnovel_item, null);
            return new NovelHolder(view);
        }
        view = View.inflate(viewGroup.getContext(), R.layout.discover_app_item, null);
        return new AppHolder(view);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (mAdapterType == DiscoverAdapter.TYPE_NOVEL) {
            bindNovelItem((NovelHolder) viewHolder, position);
        } else if (mAdapterType == DiscoverAdapter.TYPE_WALLPAPER) {
            bindWallPaperItem((WallPaperHolder) viewHolder, position);
        } else {
            bindAppItem((AppHolder) viewHolder, position);
        }
    }


    private void bindAppItem(AppHolder viewHolder, int position) {
        DiscoverAppInfoBto appInfoBto = mDiscoverAppInfoBtos.get(position);
        viewHolder.app_name.setText(appInfoBto.getAppName());
        CornerIconInfoBto cornerIcon = appInfoBto.getCornerMarkInfo();
        if(cornerIcon.getType() > 0) {
            viewHolder.app_corner.setVisibility(View.VISIBLE);
            AsyncImageCache.from(mContext).displayImage(viewHolder.app_corner, cornerIcon,0);
        } else {
            viewHolder.app_corner.setVisibility(View.GONE);
        }
        mAsyncImageCache.displayImage(mRefreshIcon, viewHolder.app_img, R.drawable.picture_bg1_big,
            new AsyncImageCache.NetworkImageGenerator(appInfoBto.getPackageName(), appInfoBto.getImageUrl()), true);
        initBtnState(viewHolder.state_app_btn, appInfoBto, viewHolder.app_img);
        viewHolder.app_view.setOnClickListener(new AppOnClickListener(appInfoBto));
    }

    
    public void initBtnState(TextView install,DiscoverAppInfoBto appInfo,ImageView icon) {
        AppOperatorUtils.initBtnState(mContext, install, appInfo.getPackageName(), appInfo.getVersionCode(), icon);
        install.setOnClickListener(new AppOperatorUtils.CommonAppClick(mContext, appInfo, mDownloadCallBack, Integer.toString(mTopicId), mReportFlag,false));
    }
    
    
    private void bindWallPaperItem(WallPaperHolder viewHolder, int position) {
        final DiscoverAppInfoBto wallpaperInfo = mDiscoverAppInfoBtos.get(position);
        /** 显示壁纸图片 */
        if (TextUtils.isEmpty(wallpaperInfo.getThumbImageUrl())) {
            viewHolder.wallpaper_img.setImageResource(R.drawable.discover_wallpaper_default_bg);
        }else {
            mAsyncImageCache.displayImage(mRefreshIcon,false,viewHolder.wallpaper_img, R.drawable.discover_wallpaper_default_bg,mWallpaperWidth,mWallpaperHeight,
                new AsyncImageCache.NetworkImage565Generator(wallpaperInfo.getThumbImageUrl(), wallpaperInfo.getThumbImageUrl()), false,true,false,null);
        }
        /** 显示壁纸图片---end */
        
        viewHolder.wallpaper_img.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(mContext, WallpaperDetail.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				intent.putExtra("download_url", wallpaperInfo.getImageUrl());
				intent.putExtra("download_name", wallpaperInfo.getAppName());
	            intent.putExtra("download_id", wallpaperInfo.getAppId());
				mContext.startActivity(intent);
            }
        });
    }


    private void bindNovelItem(NovelHolder viewHolder, int position) {
        DiscoverAppInfoBto novelInfo = mDiscoverAppInfoBtos.get(position);
        if (TextUtils.isEmpty(novelInfo.getImageUrl())) {
            viewHolder.novel_img.setImageResource(R.drawable.novel_default_pic);
        }else {
            mAsyncImageCache.displayImage(mRefreshIcon,false, viewHolder.novel_img, R.drawable.novel_default_pic,mNovelWidth,mNovelHeight,
                new AsyncImageCache.NetworkImage565Generator(MarketUtils.getImgUrlKey(novelInfo.getPackageName()),novelInfo.getImageUrl()), true, false,false,null);
        }
        viewHolder.novel_name.setText(novelInfo.getAppName());
        initBtnState(viewHolder.install_btn, novelInfo, viewHolder.novel_img);
        viewHolder.novel_view.setOnClickListener(new AppOnClickListener(novelInfo));
        
    }


    public void asyncLoadImage(int mStartIndex,int mEndIndex,RecyclerView recyclerView){
        if (mAdapterType == DiscoverAdapter.TYPE_WALLPAPER) {
            asyncLoadWallPaper(mStartIndex, mEndIndex, recyclerView);
        } else if (mAdapterType == DiscoverAdapter.TYPE_NOVEL) {
            asyncLoadNovel(mStartIndex, mEndIndex, recyclerView);
        } else {
            asyncLoadApp(mStartIndex, mEndIndex, recyclerView);
        }
    }
    
    private void asyncLoadWallPaper(int mStartIndex,int mEndIndex,RecyclerView recyclerView){
        ImageView imageView = null;
        DiscoverAppInfoBto info = null;
        for (; mStartIndex <= mEndIndex; mStartIndex++) {
            info = getItem(mStartIndex);
            if (info == null || TextUtils.isEmpty(info.getThumbImageUrl())) {
                continue;
            }
            imageView = (ImageView) recyclerView.findViewWithTag(info.getThumbImageUrl());
            if(imageView == null) {
                continue;
            }
            if (isAllowRefreshIcon() == false) {
                break;
            }
            int defaultResId = R.drawable.discover_wallpaper_default_bg;
            int resId = (Integer) imageView.getTag(R.id.tag_image_resid);
            if (resId == defaultResId) {
                mAsyncImageCache.displayImage(isAllowRefreshIcon(),false,imageView, defaultResId,mWallpaperWidth,mWallpaperHeight,
                    new AsyncImageCache.NetworkImage565Generator(info.getThumbImageUrl(), info.getThumbImageUrl()), false,true,false,null);
            }
        }
    }
    
    
    private void asyncLoadApp(int mStartIndex,int mEndIndex,RecyclerView recyclerView){
        ImageView imageView = null;
        DiscoverAppInfoBto info = null;
        for (; mStartIndex <= mEndIndex; mStartIndex++) {
            info = getItem(mStartIndex);
            if (info == null || TextUtils.isEmpty(info.getPackageName())) {
                continue;
            }
            imageView = (ImageView) recyclerView.findViewWithTag(info.getPackageName());
            if(imageView == null) {
                continue;
            }
            if (isAllowRefreshIcon() == false) {
                break;
            }
            int defaultResId = R.drawable.picture_bg1_big;
            int resId = (Integer) imageView.getTag(R.id.tag_image_resid);
            if (resId == defaultResId) {
                mAsyncImageCache.displayImage(isAllowRefreshIcon(),imageView,defaultResId,
                    new AsyncImageCache.NetworkImageGenerator(info.getPackageName(), info.getImageUrl()), true);
            }
        }
    }
    
    
    private void asyncLoadNovel(int mStartIndex,int mEndIndex,RecyclerView recyclerView){
        ImageView imageView = null;
        DiscoverAppInfoBto info = null;
        for (; mStartIndex <= mEndIndex; mStartIndex++) {
            info = getItem(mStartIndex);
            if (info == null || TextUtils.isEmpty(info.getPackageName())) {
                continue;
            }
            imageView = (ImageView) recyclerView.findViewWithTag(info.getPackageName());
            if(imageView == null) {
                continue;
            }
            if (isAllowRefreshIcon() == false) {
                break;
            }
            int defaultResId = R.drawable.novel_default_pic;
            int resId = (Integer) imageView.getTag(R.id.tag_image_resid);
            if (resId == defaultResId) {
                mAsyncImageCache.displayImage(isAllowRefreshIcon(),false,imageView,defaultResId,mNovelWidth,mNovelHeight,
                    new AsyncImageCache.NetworkImage565Generator(MarketUtils.getImgUrlKey(info.getPackageName()),info.getImageUrl()), true, false,false,null);
            }
        }
    }
    
    
    @Override
    public int getItemCount() {
        return mDiscoverAppInfoBtos == null ? 0 : mDiscoverAppInfoBtos.size();
    }

    public DiscoverAppInfoBto getItem(int position) {
        try {
            return mDiscoverAppInfoBtos==null ? null : mDiscoverAppInfoBtos.get(position);
        } catch(IndexOutOfBoundsException e) {
            return null;
        }
    }
    
    private class AppOnClickListener implements OnClickListener {
        private DiscoverAppInfoBto appInfo;


        public AppOnClickListener(DiscoverAppInfoBto appInfo) {
            this.appInfo = appInfo;
        }


        @Override
        public void onClick(View v) {
            startAppDetailActivity(mContext, appInfo, mReportFlag, -1);
            if (mNormalHolder.mARedDot.getVisibility() == View.VISIBLE) {
                mNormalHolder.mARedDot.setVisibility(View.GONE);
                MarketUtils.mRedDotCount--;
                MarketUtils.setDiscoverSPBoolean(mContext, mTopicName, false);
            }
            if (MarketUtils.mRedDotCount == 0) {
                Intent clearNotify = new Intent(Splash.CLEAR_NOTIFY);
                mContext.sendBroadcast(clearNotify);
            }
        }

    }

    
    private void startAppDetailActivity(Context context, DiscoverAppInfoBto appInfo, String fromPath, int topicId) {
        if (appInfo == null || context == null)
            return;

        String activityUrl = appInfo.getActivityUrl();
        if (activityUrl != null && !activityUrl.equals("")) {
            activityUrl = activityUrl + "?apk_id=" + appInfo.getAppId() + "&activity_id="
                + appInfo.getCornerMarkInfo().getType();
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        startDetailActivity(context, appInfo.getAppId(), fromPath, topicId, activityUrl);
    }

    private void startDetailActivity(Context context, int refId, String fromPath, int topicId, String activityUrl) {
        if (context != null) {
            Intent intent = new Intent(context, AppDetailInfoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("refId", refId);
            intent.putExtra("from_path", fromPath);
            intent.putExtra("topicId", topicId);
            intent.putExtra("fromInner", true);
            intent.putExtra("activity_url", activityUrl);
            context.startActivity(intent);
        }
    }

    
    public class AppHolder extends RecyclerView.ViewHolder {
        public RelativeLayout app_view;
        public TextView app_name;
        public TextView state_app_btn;
        public ImageView app_img;
        public ImageView app_corner;

        public AppHolder(View itemView) {
            super(itemView);
            app_view = (RelativeLayout) itemView.findViewById(R.id.app_view);
            app_name = (TextView) itemView.findViewById(R.id.app_name);
            state_app_btn = (TextView) itemView.findViewById(R.id.state_app_btn);
            app_img = (ImageView) itemView.findViewById(R.id.app_img);
            app_corner = (ImageView) itemView.findViewById(R.id.app_corner);
        }
    }

    public class WallPaperHolder extends RecyclerView.ViewHolder {

        private ImageView wallpaper_img;

        
        public WallPaperHolder(View itemView) {
            super(itemView);
            wallpaper_img = (ImageView) itemView.findViewById(R.id.wallpaper_img);
        }
    }

    public class NovelHolder extends RecyclerView.ViewHolder {

        private LinearLayout novel_view;
        private ImageView novel_img;
        private TextView novel_name;
        private TextView install_btn;


        public NovelHolder(View itemView) {
            super(itemView);
            novel_view = (LinearLayout) itemView.findViewById(R.id.novel_view);
            novel_img = (ImageView) itemView.findViewById(R.id.novel_img);
            novel_name = (TextView) itemView.findViewById(R.id.novel_name);
            install_btn = (TextView) itemView.findViewById(R.id.install_btn);
        }
    }
    
    public void freeDatas(){
        if (mDiscoverAppInfoBtos != null) {
            mDiscoverAppInfoBtos.clear();
            mDiscoverAppInfoBtos = null;
        }
        if (mAsyncImageCache != null) {
            mAsyncImageCache.stop();
            mAsyncImageCache.releaseRes();
            mAsyncImageCache = null;
        }
        mContext = null;
    }

}
