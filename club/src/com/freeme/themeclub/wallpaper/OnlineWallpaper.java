package com.freeme.themeclub.wallpaper;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.android.internal.util.AsyncImageCache;
import com.freeme.themeclub.BackScrollFragment;
import com.freeme.themeclub.CustomToast;
import com.freeme.themeclub.MainActivity;
import com.freeme.themeclub.R;
import com.freeme.themeclub.banner.AutoScrollLoopBanner;
import com.freeme.themeclub.banner.BannerItemContainer;
import com.freeme.themeclub.lockscreen.LockscreenFragment;
import com.freeme.themeclub.statisticsdata.LocalUtil;
import com.freeme.themeclub.statisticsdata.db.StatisticDBHelper;
import com.freeme.themeclub.theme.ThemeFragment;
import com.freeme.themeclub.theme.onlinetheme.EssenceThemesFragment;
import com.freeme.themeclub.theme.onlinetheme.NewestThemesFragment;
import com.freeme.themeclub.theme.onlinetheme.OnlineThemesDetailActivity;
import com.freeme.themeclub.theme.onlinetheme.PopularThemesFragment;
import com.freeme.themeclub.theme.onlinetheme.OnlineThemesFragment.GetAdDetail;
import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;
import com.freeme.themeclub.theme.onlinetheme.util.OnlineThemesUtils;
import com.freeme.themeclub.theme.onlinetheme.util.ResultUtil;
import com.freeme.themeclub.wallpaper.base.IntentConstants;
import com.freeme.themeclub.wallpaper.util.NetworkUtil;
import com.freeme.themeclub.wallpaper.util.OnlineUtils;

import android.R.anim;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class OnlineWallpaper extends BackScrollFragment implements
OnItemClickListener,OnTouchListener{
    private String[] sortString;
    private List<Map<String, Object>> thumbListData = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> specialData;
    private ArrayAdapter<String> sortAdapter = null;
    private WallpaperThumbAdapter mThumbAdapter = null;
    private LinearLayout gridLoadingLayout;
    private GridView thumbGridView = null;
    private Resources mResources;

    private final String[] mSaveDataName = { "OnlineSpecialDate.cfg",
            "OnlineSortDate.cfg", "OnlineList" };
    private final String TAG = "OnlineWallpaper";

    private AsyncImageCache mAsyncImageCache;

    private FrameLayout parentView;
    private LinearLayout loadingLayout;
    private ProgressBar mLoadingBar;
    private LinearLayout refreshLayout;
    private View dataLayout;

    private int flag;
    private int sortSelect = 0;
    private int orderSelect = 1;
    private int setSelect = 1;
    private boolean sortFirst = true;
    private boolean orderFirst = true;
    private int startNum = 0;
    private final int pageNumber = 10;
    private int pageIndex = 0;

    private boolean mGetListDataFail;
    private boolean isGetingDataNow;

    public View view;
    private boolean noMask;
    public int msgCode;
    public String style;
    public String sort = "01";
    public int bout = 0;
    private boolean mIsScrolling;

    private int subType;
    public int serialNum;

    public static final String WALLPAPER_ID = "id";
    public static final String WALLPAPER_NAME = "name";
    public static final String WALLPAPER_THUMB_URL = "dnUrlS";
    public static final String WALLPAPER_ORIGNAL_URL = "dnUrlX";
    public static final String WALLPAPER_DOWNLOAD_COUNT = "dnCnt";
    public static final String WALLPAPER_MODIFY_TIME = "modifyTime";

    private List<Map<String, Object>> adListData;
    private List<Map<String, Object>> adDetailData;
    private AutoScrollLoopBanner banner;
    private FragmentManager fragmentManager;
    public boolean loadAds = true;
    private boolean fresh;

    private StatisticDBHelper mStatisticDBHelper;

    public Context categoryContext;

    public OnlineWallpaper() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        parentView = (FrameLayout) inflater.inflate(
                R.layout.wallpaper_online_nodata, container, false);
        loadingLayout = (LinearLayout) parentView
                .findViewById(R.id.loading_layout);
        refreshLayout = (LinearLayout) parentView
                .findViewById(R.id.refresh_linearLayout_id);

        parentView.findViewById(R.id.set_wlan).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(Settings.ACTION_WIFI_SETTINGS);  
                startActivity(intent);
            }
        });
        parentView.findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                refreshLayout.setVisibility(View.GONE);
                loadingLayout.setVisibility(View.VISIBLE);
                initDataLayout(getActivity());
                new GetListData().execute();
                loadAD();
                isGetingDataNow = true;
            }
        });

        mLoadingBar = (ProgressBar) parentView.findViewById(R.id.loading_pb);
        return parentView;
    }

    public void initDataLayout(Context context) {
        dataLayout = LayoutInflater.from(context).inflate(
                R.layout.wallpaper_online_view, null);

        if (gridLoadingLayout != null) {
            gridLoadingLayout.setVisibility(View.GONE);
        }

        initViews(dataLayout);
        startNum = 0;
        pageIndex = 0;
    }

    public void loadData(){
        if(!fresh){
            fresh=true;
            new GetListData().execute();
            if (loadAds) {
                if(banner != null){
                    banner.removeAllViews();
                }
                new GetAdData().execute();
            }
            isGetingDataNow = true;
        }

    }
    
    private synchronized void loadAD(){
        if(banner != null){
            banner.removeAllViews();
        }
        new GetAdData().executeOnExecutor(MainActivity.fixedThreadPool);
    }

    public void categoryLoadData(){
        new GetListData().execute();
        isGetingDataNow = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        flag = 0/*
         * getActivity().getIntent().getIntExtra(
         * IntentConstants.EXTRA_RESOURCE_FLAG, -1)
         */;
        mResources = getResources();
        sortString = mResources.getStringArray(R.array.sort_array);
        mAsyncImageCache = AsyncImageCache.from(getActivity());
        initDataLayout(getActivity());

        mStatisticDBHelper=StatisticDBHelper.getInstance(getActivity());
    }

    @Override
    public void onDestroy() {
        mAsyncImageCache.stop();
        super.onDestroy();
    }

    private void initViews(View dataView) {
        banner = (AutoScrollLoopBanner) dataView.findViewById(R.id.logo_banner);
        if(noMask){
            dataView.findViewById(R.id.adver).setVisibility(View.GONE);
        }
        gridLoadingLayout = (LinearLayout) dataView
                .findViewById(R.id.grid_loading);
        thumbGridView = (GridView) dataView.findViewById(R.id.preview_thumb_gv);

        mSetAdapters();
        mSetListeners();
    }

    private void mSetListeners() {
        thumbGridView.setOnItemClickListener(this);
        dataLayout.setOnTouchListener(this);
    }

    private void mSetAdapters() {
        mThumbAdapter = new WallpaperThumbAdapter(getActivity(), thumbListData,
                mAsyncImageCache);
        thumbGridView.setAdapter(mThumbAdapter);
    }

    public class GetListData extends AsyncTask<Object, Object, String> {
        @Override
        protected String doInBackground(Object... params) {
            String result = null;
            isGetingDataNow=true;
            if (OnlineUtils.getNetWorkType(getActivity()==null?categoryContext:getActivity()) == -1
                    && pageIndex == 0) {
                result = OnlineUtils.getStringData(mSaveDataName[2]
                        + sortSelect + "_" + setSelect + "_" + orderSelect
                        + serialNum + style + ".cfg");
            } else {
                try {
                    // int msgCode = MessageCode.GET_WALLPAPER_LIST_BY_TAG_REQ;
                    JSONObject paraInfo = new JSONObject();
                    paraInfo.put("mf", "koobee");
                    paraInfo.put(
                            "lcd",
                            OnlineUtils.getAvailableResolutionForThisDevice(
                                    getActivity(),
                                    mResources
                                    .getStringArray(R.array.resolution_array))[setSelect]);
                    if (sortSelect != 0) {
                        paraInfo.put("style", sortSelect + "");
                    }
                    paraInfo.put("style", style);
                    paraInfo.put("sort", sort);
                    paraInfo.put("bout", bout);
                    paraInfo.put("from", String.valueOf(startNum));
                    paraInfo.put("to",
                            "" + String.valueOf(startNum + pageNumber));
                    JSONObject jsObject = new JSONObject();
                    jsObject.put("head", NetworkUtil.buildHeadData(msgCode));
                    jsObject.put("body", paraInfo.toString());
                    String contents = jsObject.toString();
                    String url = MessageCode.SERVER_URL;
                    result = NetworkUtil.accessNetworkByPost(url, contents);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (pageIndex == 0) {
                    OnlineUtils.saveStringData(result, mSaveDataName[2]
                            + sortSelect + "_" + setSelect + "_" + orderSelect
                            + serialNum + style + ".cfg");
                }
            }
            Log.v(TAG, "Listresult:" + result);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            List<Map<String, Object>> listData;
            listData = OnlineUtils.splitThumbListJSON(result);
            if (pageIndex == 0) {
                thumbListData.removeAll(thumbListData);
            }
            if (listData != null) {
                if(listData.size()==0){
                    CustomToast.showToast(getActivity(), R.string.no_more_data, 3000);
                }
                parentView.removeView(dataLayout);
                parentView.addView(dataLayout);
                for (Map map : listData) {
                    thumbListData.add(map);
                }
                if (listData.size() == 0 || listData.size() < pageNumber) {
                    dataLayout.setOnTouchListener(null);
                }
                mGetListDataFail = false;
                mThumbAdapter.setAdapterData(thumbListData);
                mThumbAdapter.notifyDataSetChanged();
                if(view!=null){
                    view.findViewById(R.id.controls).setVisibility(View.VISIBLE);
                }
            } else {
                mGetListDataFail = true;
                refreshLayout.setVisibility(View.VISIBLE);
                loadingLayout.setVisibility(View.GONE);
            }

            gridLoadingLayout.setVisibility(View.GONE);
            thumbGridView.setVisibility(View.VISIBLE);
            mLoadingBar.setVisibility(View.GONE);

            isGetingDataNow = false;
        }
    }

    private boolean noData(List<Map<String, Object>>... datas) {
        for (int i = 0; i < datas.length; i++) {
            if (datas[i] != null && datas[i].size() > 0)
                return false;
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View item, int position,
            long arg3) {

        Intent intent = new Intent(this.getActivity(),
                OnlineWallpaperDetailActivity.class);
        if (mThumbAdapter != null) {
            List<Map<String, Object>> mCurrentData = mThumbAdapter
                    .getAdapterData();
            intent.putExtra(OnlineUtils.MyClickPosition, position );
            intent.putExtra(OnlineUtils.MyListData,
                    (ArrayList<Map<String, Object>>) mCurrentData);
            intent.putExtra(IntentConstants.EXTRA_RESOURCE_FLAG, flag);
            intent.putExtra(OnlineUtils.MySetSelect, setSelect);

            if(this instanceof NewestWallpaperFragment){
                String name = thumbListData.get(position ).get("name").toString();
                wallStatisticData(LocalUtil.WALL_CLICK_NEWS,name,System.currentTimeMillis());
            }/*else if(this instanceof EssenceWallpaperFragment){
                    String name = thumbListData.get(position ).get("name").toString();
                    wallStatisticData(LocalUtil.WALL_CLICK_ESSENCE,name,System.currentTimeMillis());
                }*/else if(this instanceof PopularWallpaperFragment){
                    String name = thumbListData.get(position ).get("name").toString();
                    wallStatisticData(LocalUtil.WALL_CLICK_POPULAR,name,System.currentTimeMillis());
                } 
            startActivityForResult(intent, 100);
        }

    }

    private void wallStatisticData(String opid,String name,long time) {		
        String infoStr = LocalUtil.saveStatisticInfo(LocalUtil.CLICK_ACTION_ID, opid, name,
                time);
        mStatisticDBHelper.intserStatisticdataToDB(infoStr);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            break;
        case MotionEvent.ACTION_MOVE:
            break;
        case MotionEvent.ACTION_UP:
            if (com.freeme.themeclub.theme.onlinetheme.util.NetworkUtil.getAPNType(getActivity()) == -1) {
                CustomToast.showToast(getActivity(), getString(R.string.check_wlan), 3000);
            }
            if (OnlineUtils.scrollButtom(view,mLoadingBar)) {  
                if(!isGetingDataNow){
                    if(!mGetListDataFail){
                        pageIndex++;
                        startNum = pageIndex*pageNumber;
                    }
                    mLoadingBar.bringToFront();
                    mLoadingBar.setVisibility(View.VISIBLE); 
                    new GetListData().executeOnExecutor(MainActivity.fixedThreadPool);
                    isGetingDataNow = true;
                }
            }
        default:
            break;
        }
        return false;
    }

    public class WallpaperThumbAdapter extends BaseAdapter {
        private Context mContext;
        private List<Map<String, Object>> mDataList;
        private LayoutInflater mLayoutInflater;
        private AsyncImageCache mAsyncImageCache;
        private Resources res;
        private boolean mNarrowSreen = false;
        private boolean mFirst = true;

        public WallpaperThumbAdapter(Context context,
                List<Map<String, Object>> dataList,
                AsyncImageCache mAsyncImageCache) {
            this.mContext = context;
            this.mDataList = dataList;
            this.mAsyncImageCache = mAsyncImageCache;
            mLayoutInflater = LayoutInflater.from(mContext);
            res = mContext.getResources();
        }

        public void setSreen(boolean flag) {
            mNarrowSreen = flag;
        }

        public void setFlagFirst() {
            mFirst = true;
        }

        public void setAdapterData(List<Map<String, Object>> dataList) {
            this.mDataList = dataList;
        }

        public List<Map<String, Object>> getAdapterData() {
            return mDataList;
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ThumbHolder mThumbHolder = null;
            if(convertView==null){
                convertView=mLayoutInflater.inflate(R.layout.wallpaper_thumb_griditem, null);
                mThumbHolder=new ThumbHolder();

                mThumbHolder.previewThumb = (ImageView) convertView
                        .findViewById(R.id.preview_thumb_item);
                mThumbHolder.status = (ImageView) convertView
                        .findViewById(R.id.thumb_status_iv);

                convertView.setTag(mThumbHolder);
            }else{
                mThumbHolder=(ThumbHolder) convertView.getTag();
            }

            if (!mNarrowSreen)
                mThumbHolder.previewThumb
                .setLayoutParams(new LayoutParams(
                        res.getDimensionPixelSize(R.dimen.wide_thumb_preview_w),
                        res.getDimensionPixelSize(R.dimen.wide_thumb_preview_h)));
            if(mDataList!=null){
                Map<String, Object> thumbItem = mDataList.get(position );
                String thumbUrl = (String) thumbItem.get(WALLPAPER_THUMB_URL);
                mAsyncImageCache.displayImage(
                        mThumbHolder.previewThumb,
                        R.drawable.wallpaper_no_default2,
                        res.getDimensionPixelSize(mNarrowSreen ? R.dimen.thumb_preview_w
                                : R.dimen.wide_thumb_preview_w),
                                res.getDimensionPixelSize(mNarrowSreen ? R.dimen.thumb_preview_h
                                        : R.dimen.wide_thumb_preview_h),
                                        new AsyncImageCache.NetworkImageGenerator(
                                                thumbUrl, thumbUrl));
                mThumbHolder.status
                .setVisibility(OnlineUtils.checkIsDownLoaded(thumbItem
                        .get(WALLPAPER_ID)
                        + (String) thumbItem.get(WALLPAPER_NAME)) ? View.VISIBLE
                                : View.GONE);
            }


            return convertView;
        }

        class ThumbHolder {
            ImageView previewThumb;
            ImageView status;
        }
    }

    public void setNoMask(boolean b) {
        noMask = b;
    }

    public class GetAdData extends
    AsyncTask<Object, Object, List<Map<String, Object>>> {
        protected List<Map<String, Object>> doInBackground(Object... params) {
            String result = null;
            List<Map<String, Object>> list = null;

            if (OnlineUtils.getNetWorkType(getActivity()) == -1) {
                result = OnlineThemesUtils
                        .getListViewData("SmallAdvertisments.cfg");
            } else {
                try {
                    JSONObject paraInfo = new JSONObject();
                    paraInfo.put("lcd", OnlineThemesUtils
                            .getAvailableResolutionForThisDevice(
                                    getActivity(),
                                    getResources().getStringArray(
                                            R.array.resolution_array))[0]);
                    paraInfo.put("homePage", 0);
                    JSONObject jsObject = new JSONObject();
                    jsObject.put("head", NetworkUtil
                            .buildHeadData(MessageCode.GET_AD_LIST_BY_TAG_REQ));
                    jsObject.put("body", paraInfo.toString());
                    String contents = jsObject.toString();
                    String url = MessageCode.SERVER_URL;
                    result = NetworkUtil.accessNetworkByPost(url, contents);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                OnlineThemesUtils.saveListViewData(result,
                        "SmallAdvertisments.cfg");
            }
            if (result != null) {
                list = ResultUtil.splitADServerListData(result);
            }

            return list;
        }

        protected void onPostExecute(List<Map<String, Object>> list) {
            adListData = new ArrayList<Map<String, Object>>();
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    adListData.add(list.get(i));
                    addNewsLogo(list.get(i).get("adverUrl") + "", i);
                }
                super.onPostExecute(list);
            }
        }
    }

    private long lastClick;
    public void addNewsLogo(String imgUrl, final int position) {
        View child = null;
        if (TextUtils.isEmpty(imgUrl)) {
            child = initBinner(R.drawable.newest_banner_default);
        } else {
            imgUrl = encodeUrl(imgUrl) + "";
            child = initBinner(imgUrl, R.drawable.newest_banner_default);
            if (child == null) {
                return;
            }
            child.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (com.freeme.themeclub.theme.onlinetheme.util.NetworkUtil.getAPNType(getActivity()) == -1) {
                        CustomToast.showToast(getActivity(), getString(R.string.check_wlan),
                                3000);
                    }
                    if (System.currentTimeMillis() - lastClick >= 1000)  
                    {  
                        lastClick = System.currentTimeMillis(); 
                        new GetAdDetail().execute(position);
                    }

                    String name=adListData.get(position).get("adverName").toString();
                    String infoStr=LocalUtil.saveStatisticInfo(LocalUtil.CLICK_ACTION_ID, LocalUtil.WALL_CLICK_AD, name, System.currentTimeMillis());
                    mStatisticDBHelper.intserStatisticdataToDB(infoStr);
                }
            });
        }
        banner.addView(child);
        banner.setDataReady(true);

        if (banner.getChildCount() > 1) {
            banner.startAutoScroll();
        }
    }

    public static URL encodeUrl(String path) {

        try {
            path = URLEncoder.encode(path, "utf-8");
            path = path.replace("%2F", "/");
            path = path.replace("%3A", ":");
            URL u = new URL(path);
            return u;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public View initBinner(String url, int defaultResId) {
        if (getActivity() == null) {
            return null;
        }
        int width = getResources().getDimensionPixelSize(R.dimen.banner_width);
        int height = getResources().getDimensionPixelSize(R.dimen.banner_height);
        ImageView logo = new ImageView(getActivity());
        logo.setScaleType(ScaleType.FIT_XY);
        BannerItemContainer container = new BannerItemContainer(getActivity());
        mAsyncImageCache.displayImage(logo, defaultResId,width,height,
                new AsyncImageCache.NetworkImageGenerator(url, url));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        container.addView(logo, params);
        return container;
    }

    public View initBinner(int resId) {
        ImageView logo = new ImageView(getActivity());
        logo.setImageResource(resId);
        logo.setScaleType(ScaleType.CENTER);
        BannerItemContainer container = new BannerItemContainer(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        container.addView(logo, params);
        return container;
    }

    public class GetAdDetail extends
    AsyncTask<Object, Object, List<Map<String, Object>>> {
        protected List<Map<String, Object>> doInBackground(Object... params) {
            String result = null;
            List<Map<String, Object>> list = null;
            try {
                JSONObject paraInfo = new JSONObject();
                subType = (Integer) adListData.get((Integer) params[0]).get(
                        "subType");
                paraInfo.put("subType", subType);
                paraInfo.put("subId",
                        adListData.get((Integer) params[0]).get("subId"));
                JSONObject jsObject = new JSONObject();
                jsObject.put("head", NetworkUtil
                        .buildHeadData(MessageCode.GET_AD_DETAIL_BY_TAG_REQ));
                jsObject.put("body", paraInfo.toString());
                String contents = jsObject.toString();
                String url = MessageCode.SERVER_URL;
                result = NetworkUtil.accessNetworkByPost(url, contents);

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (result != null) {
                if (subType == 1) {
                    list = ResultUtil.splitThemeDetailServerListData(result);
                }
                if (subType == 2) {
                    list = ResultUtil.splitScreenDetailServerListData(result);
                }
                if (subType == 3) {
                    list = ResultUtil.splitWallpaperDetailListJSON(result);
                }
            }
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    list.get(i).put(
                            "isDownloaded",
                            OnlineThemesUtils.checkInstalled(getActivity(),
                                    (String) list.get(i).get("packageName")));
                }
            }
            return list;
        }

        protected void onPostExecute(List<Map<String, Object>> list) {
            if (list != null) {
                adDetailData = new ArrayList<Map<String, Object>>();
                for (int i = 0; i < list.size(); i++) {
                    adDetailData.add(list.get(i));
                    if (subType == 3) {
                        Intent intent = new Intent(getActivity(),
                                OnlineWallpaperDetailActivity.class);

                        intent.putExtra(OnlineUtils.MyClickPosition, i);
                        intent.putExtra(OnlineUtils.MyListData,
                                (ArrayList<Map<String, Object>>) adDetailData);
                        intent.putExtra(IntentConstants.EXTRA_RESOURCE_FLAG,
                                flag);
                        intent.putExtra(OnlineUtils.MySetSelect, setSelect);
                        startActivity(intent);
                    }

                    if (subType == 1 || subType == 2) {
                        Intent intent = new Intent(getActivity(),
                                OnlineThemesDetailActivity.class);
                        intent.putExtra("list_id", i);
                        intent.putExtra("mlistData",
                                (ArrayList<Map<String, Object>>) adDetailData);
                        intent.putExtra("isOnlineLockscreen",
                                subType == 1 ? false : true);

                        startActivity(intent);
                    }
                }

                super.onPostExecute(list);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
        case 100:
            if (mThumbAdapter != null)
                mThumbAdapter.notifyDataSetChanged();
            break;
        default:
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
