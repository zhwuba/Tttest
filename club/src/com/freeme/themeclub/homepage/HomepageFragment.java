//package com.freeme.themeclub.homepage;
//
//import java.io.BufferedReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.URL;
//import java.net.URLEncoder;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import org.json.JSONObject;
//
//import android.app.ActionBar.LayoutParams;
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.app.DownloadManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.graphics.Bitmap;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.provider.Settings;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AbsListView;
//import android.widget.AbsListView.OnScrollListener;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.BaseAdapter;
//import android.widget.FrameLayout;
//import android.widget.GridView;
//import android.widget.ImageView;
//import android.widget.ImageView.ScaleType;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.android.internal.util.AsyncImageCache;
//import com.freeme.themeclub.BackScrollFragment;
//import com.freeme.themeclub.BackScrollManager;
//import com.freeme.themeclub.CustomToast;
//import com.freeme.themeclub.LoadOuterData;
//import com.freeme.themeclub.MainActivity;
//import com.freeme.themeclub.R;
//import com.freeme.themeclub.BackScrollManager.ScrollableHeader;
//import com.freeme.themeclub.ThemeClubSystemProperties;
//import com.freeme.themeclub.banner.AutoScrollLoopBanner;
//import com.freeme.themeclub.banner.BannerItemContainer;
//import com.freeme.themeclub.statisticsdata.LocalUtil;
//import com.freeme.themeclub.statisticsdata.db.StatisticDBHelper;
//import com.freeme.themeclub.theme.onlinetheme.OnlineThemesDetailActivity;
//import com.freeme.themeclub.theme.onlinetheme.override;
//import com.freeme.themeclub.theme.onlinetheme.OnlineThemesFragment.GetAdDetail;
//import com.freeme.themeclub.theme.onlinetheme.download.DownloadManagerHelper;
//import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;
//import com.freeme.themeclub.theme.onlinetheme.util.NetworkUtil;
//import com.freeme.themeclub.theme.onlinetheme.util.OnlineThemesUtils;
//import com.freeme.themeclub.theme.onlinetheme.util.PreferencesUtils;
//import com.freeme.themeclub.theme.onlinetheme.util.ResultUtil;
//import com.freeme.themeclub.wallpaper.OnlineWallpaperDetailActivity;
//import com.freeme.themeclub.wallpaper.base.IntentConstants;
//import com.freeme.themeclub.wallpaper.util.OnlineUtils;
//
//import com.adroi.sdk.AdSize;
//import com.adroi.sdk.AdView;
//import com.adroi.sdk.AdViewListener;
//
//public class HomepageFragment extends BackScrollFragment implements OnScrollListener,
//OnItemClickListener,LoadOuterData{
//
//    private static final String TAG = "OnlineThemesActivity";
//
//    public static final String THEME_VERSION = "v500";
//    public static final String PROJECT_NAME = "koobee";
//
//    private AsyncImageCache mAsyncImageCache;
//    private DownloadReceiver completeReceiver;
//    private DeletePackageReceiver deletePackageReceiver;
//
//    private LinearLayout mFooter;
//    private LinearLayout mSearch_loading;
//    private LinearLayout mRefresh_linearLayout_id;
//    private GridView mGridView;
//    private OnlineThemesMyAdapter mAdapter;
//    private boolean mSyncFlag = false;
//    private boolean mIsScrolling = false;
//    private boolean mRefreshFinished = false;
//    private boolean mGetListDataFail = false;
//    private final int DLG_NOT_NET = 1;
//
//    // page show
//    private int mPage_Index = 0;
//    private int mStart_numer = 0;
//    private int mPage_numer = 9;
//    private Handler mHandler;
//    private final int REFRESH_LIST = 2;
//    private static ArrayList<Map<String, Object>> mListData;
//    private ArrayList<Map<String, Object>> adListData=new ArrayList<Map<String,Object>>();
//    private ArrayList<Map<String, Object>> adDetailData=new ArrayList<Map<String,Object>>();
//    private int mWifiOpenStatus = 0;
//    private int mSavaInDbCount = 0;
//
//    public int msgCode;
//
//    private Activity mActivity;
//    private Context mContext;
//
//    public View view;
//    private View mControls;
//    private int subType;
//
//    private AutoScrollLoopBanner banner;
//    private boolean fresh;
//
//    private StatisticDBHelper mStatisticDBHelper;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mActivity=getActivity();
//        mContext=mActivity.getBaseContext();
//        mAsyncImageCache = AsyncImageCache.from(mActivity);
//        mWifiOpenStatus = getAPNType(mContext);
//
//        mStatisticDBHelper=StatisticDBHelper.getInstance(mActivity);
//
//        OnlineThemesUtils.savaDownloadCount(mSavaInDbCount);
//
//        mHandler = new Handler() {
//            public void handleMessage(Message message) {
//                super.handleMessage(message);
//                switch (message.what) {
//                case REFRESH_LIST:
//                    if (mIsScrolling == false && mAdapter != null)
//                        mAdapter.notifyDataSetChanged();
//                    break;
//                }
//            }
//        };
//
//        mDownloadHelp = new DownloadManagerHelper((DownloadManager) mActivity.getSystemService(mContext.DOWNLOAD_SERVICE));
//        completeReceiver = new DownloadReceiver();
//
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
//        filter.addAction(OnlineThemesDetailActivity.ACTION_DELETE_DOWNLOAD);
//        mActivity.registerReceiver(completeReceiver, filter);
//
//        deletePackageReceiver = new DeletePackageReceiver();
//        IntentFilter filter2 = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
//        filter2.addDataScheme("package");
//        mActivity.registerReceiver(deletePackageReceiver, filter2);
//        setHasOptionsMenu(true);
//
//    }
//
//    public void loadOuterData(){
//        if(!fresh){
//            fresh=true;
//
//            new GetAdData().executeOnExecutor(MainActivity.fixedThreadPool);
//            new GetOnlineThemeData().execute();
//
//        }        
//    }
//
//    @Override
//    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
//            Bundle savedInstanceState) {
//        final View contentView=inflater.inflate(R.layout.homepage, null);
//        findViews(contentView);
//        mListData = new ArrayList<Map<String, Object>>();
//        setEmptyViewIfNoNet(mGridView);
//        BackScrollManager.bind(
//                new ScrollableHeader() {
//                    private View mControls = contentView.findViewById(R.id.controls);
//                    private View mPhoto = contentView.findViewById(R.id.contact_background_sizer);
//                    private View mHeader = contentView.findViewById(R.id.photo_text_bar);
//
//                    @Override
//                    public void setOffset(int offset) {
//                        mControls.setY(-offset);
//                    }
//
//                    public int getOffsetNotChange(){
//                        return mPhoto.getHeight();
//                    }
//
//                    @Override
//                    public int getMaximumScrollableHeaderOffset() {
//                        if (mHeader.getVisibility() == View.VISIBLE) {
//                            return mPhoto.getHeight() + 1;
//                        } else {
//                            return mPhoto.getHeight() + 1;
//                        }
//                    }
//                },
//                mGridView,HomepageFragment.this);
//        return contentView;
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        Log.w("yzy" ,"destroy fragment " + HomepageFragment.class.getSimpleName() + "  " + this);
//    }
//
//    @Override
//    public void onDestroy() {
//        mAsyncImageCache.stop();     
//        mActivity.unregisterReceiver(completeReceiver);
//        mActivity.unregisterReceiver(deletePackageReceiver);
//        super.onDestroy();
//    }
//
//    private void setEmptyViewIfNoNet(final GridView listView) {
//        mSearch_loading.setVisibility(View.VISIBLE);
//        mGridView.setVisibility(View.GONE);
//    }
//
//    private void findViews(View view) {
//        mGridView = (GridView) view.findViewById(R.id.theme_grid); 
//        mFooter = (LinearLayout) view.findViewById(R.id.grid_foot);
//        mFooter.setVisibility(View.GONE);
//        mGridView.setOnItemClickListener(this);
//
//        mControls=(RelativeLayout) view.findViewById(R.id.controls);
//        mSearch_loading = (LinearLayout) view.findViewById(R.id.search_loading);
//        mRefresh_linearLayout_id = (LinearLayout) view.findViewById(R.id.refresh_linearLayout_id);
//
//        view.findViewById(R.id.set_wlan).setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent intent =  new Intent(Settings.ACTION_WIFI_SETTINGS);  
//                startActivity(intent);
//            }
//        });
//        view.findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                mWifiOpenStatus = getAPNType(mContext);
//                mRefresh_linearLayout_id.setVisibility(View.GONE);
//                mSearch_loading.setVisibility(View.VISIBLE);
//                banner.removeAllViews();
//
//                new GetOnlineThemeData().executeOnExecutor(MainActivity.fixedThreadPool);
//                new GetAdData().executeOnExecutor(MainActivity.fixedThreadPool);
//                //                getADroiAD();
//            }
//        });
//
//        banner = (AutoScrollLoopBanner) view.findViewById(R.id.logo_banner);
//
//    }
//
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position,
//            long id) {
//        if(position>=3){
//            Intent intent = new Intent(mContext,
//                    OnlineThemesDetailActivity.class);
//            if (null != mAdapter && null != mListData && mListData.size() > 0) {
//                intent.putExtra("list_id", position-3);
//                intent.putExtra("mlistData", mListData);
//                intent.putExtra("isOnlineLockscreen", false);
//
//                String name=mListData.get(position-3).get("name").toString();
//                String infoStr =LocalUtil.saveStatisticInfo(LocalUtil.CLICK_ACTION_ID, LocalUtil.HOME_CLICK_HOT, name,System.currentTimeMillis());
//                mStatisticDBHelper.intserStatisticdataToDB(infoStr);
//            }
//            startActivityForResult(intent, 10000);
//        }
//    }
//
//    @Override
//    public void onScroll(AbsListView view, int firstVisibleItem,
//            int visibleItemCount, int totalItemCount) {
//    }
//
//    @Override
//    public void onScrollStateChanged(AbsListView view, int scrollState) {
//        switch (scrollState) {
//        case OnScrollListener.SCROLL_STATE_IDLE:
//            if(getAPNType(mContext)==-1){
//                CustomToast.showToast(getActivity(), getActivity().getResources().getString(R.string.check_wlan),3000);
//            }
//            mIsScrolling = false;
//
//            if (view.getLastVisiblePosition() + mPage_numer >= (view.getCount() - 1)
//                    && mRefreshFinished) {
//                //                if (!mGetListDataFail) {
//                mStart_numer = mStart_numer + 1;
//                mPage_Index = mStart_numer * mPage_numer;
//                mRefreshFinished = false; 
//                mFooter.setVisibility(View.VISIBLE);
//                new GetOnlineThemeData().executeOnExecutor(MainActivity.fixedThreadPool);
//                //                }               
//            }
//            break;
//        case OnScrollListener.SCROLL_STATE_FLING:
//            mIsScrolling = true;
//            break;
//        case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
//            break;
//        }
//    }
//
//    public int getAPNType(Context context) {
//        int netType = -1;
//        ConnectivityManager connMgr = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//
//        if (networkInfo == null) {
//            return netType;
//        }
//        int nType = networkInfo.getType();
//        if (nType == ConnectivityManager.TYPE_MOBILE) {
//            String netString = networkInfo.getExtraInfo().toLowerCase();
//            if (netString.equals("cmnet") || netString.equals("uninet")) {
//                netType = 3;
//            } else {
//                netType = 2;
//            }
//        } else if (nType == ConnectivityManager.TYPE_WIFI) {
//            netType = 1;
//        }
//        return netType;
//    }
//
//    public class GetOnlineThemeData extends AsyncTask<Object, Object, List<Map<String, Object>>> {
//        protected List<Map<String, Object>> doInBackground(Object... params) {
//            String result = null;
//
//            List<Map<String, Object>> list = null;
//
//            if (mPage_Index == 0 && mWifiOpenStatus == -1){
//                result = OnlineThemesUtils.getListViewData("homepage.cfg");
//            }else{
//
//                try{
//                    msgCode = MessageCode.GET_THEME_LIST_BY_TAG_REQ;
//                    JSONObject paraInfo = new JSONObject();
//                    paraInfo.put("mf", PROJECT_NAME);
//                    paraInfo.put("lcd", OnlineThemesUtils.getAvailableResolutionForThisDevice(mContext, getResources().getStringArray(R.array.resolution_array))[0]);
//                    paraInfo.put("ver", THEME_VERSION);
//                    paraInfo.put("type", "01");
//                    paraInfo.put("sort", "01");
//                    paraInfo.put("from", String.valueOf(mPage_Index));
//                    paraInfo.put("to", String.valueOf(mPage_Index+mPage_numer));
//
//                    JSONObject jsObject = new JSONObject();
//                    jsObject.put("head", NetworkUtil.buildHeadData(msgCode));
//                    jsObject.put("body", paraInfo.toString());
//                    String contents = jsObject.toString();
//                    String url = MessageCode.SERVER_URL;
//                    result = NetworkUtil.accessNetworkByPost(url, contents);
//                    Log.w("yzy", "homepage result = "+result);
//
//                }catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                if (mPage_Index == 0)
//                    OnlineThemesUtils.saveListViewData(result,
//                            "homepage.cfg");
//            }
//
//            if(result != null){
//                list = ResultUtil.splitThemeServerListData(result);
//            }
//
//            if(list != null){
//                for (int i = 0; i < list.size(); i++) {  
//                    list.get(i).put("isDownloaded", OnlineThemesUtils.checkInstalled(mContext, (String)list.get(i).get("packageName")));
//                }
//            }
//
//            Log.v(TAG, "result:"+result);
//            return list;
//        }
//
//        protected void onPostExecute(List<Map<String, Object>> list) {
//            Map<String, Object> map;
//            if (list != null) {
//
//                if(list.size()==0){
//                    CustomToast.showToast(mContext, R.string.no_more_data, 3000);
//                }
//                mSearch_loading.setVisibility(View.GONE);
//
//                mRefresh_linearLayout_id.setVisibility(View.GONE);
//                mControls.setVisibility(View.VISIBLE);
//
//                if (mGridView.getVisibility() == View.GONE)
//                    mGridView.setVisibility(View.VISIBLE);
//
//                mGetListDataFail = false;
//
//                for (int i = 0; i < list.size(); i++) {                 
//                    mListData.add(list.get(i));
//                }
//                if (list.size() < mPage_numer || list.size() == 0) {
//                    //mGridView.setOnScrollListener(null);
//                    mIsScrolling = false;
//                }
//                if (mPage_Index == 0) {
//                    mAdapter = new OnlineThemesMyAdapter(
//                            mContext, mListData, mGridView);
//                    mGridView.setAdapter(mAdapter);
//                } else {
//                    try {
//                        if (mIsScrolling == false && mAdapter != null)
//                            mHandler.sendEmptyMessage(REFRESH_LIST);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                mRefreshFinished = true;
//            } else {
//                mGetListDataFail = true;
//
//                mSearch_loading.setVisibility(View.GONE);
//
//                if (mListData.size() == 0) {
//                    mRefresh_linearLayout_id.setVisibility(View.VISIBLE);
//                    mGridView.setVisibility(View.GONE);
//                } else {
//                    mRefresh_linearLayout_id.setVisibility(View.GONE);
//                    mRefreshFinished = true;
//                }
//            }
//            mFooter.setVisibility(View.GONE);
//            super.onPostExecute(list);
//            mSyncFlag = true;
//        }
//    }
//
//    public class OnlineThemesMyAdapter extends BaseAdapter {
//        private Context mContext;
//        private List<Map<String, Object>> list;
//
//        private boolean mIsRefrashing = false;
//
//        private int mImageWidth;
//        private int mImageHeight;
//
//        public OnlineThemesMyAdapter(Context context,
//                List<Map<String, Object>> list, GridView gridView) {
//            this.mContext = context;
//            this.list = list;
//            mGridView = gridView;
//
//            mImageWidth = context.getResources().getDimensionPixelSize(
//                    R.dimen.theme_preview_w);
//            mImageHeight = context.getResources().getDimensionPixelSize(
//                    R.dimen.theme_preview_h);
//
//        }
//
//        public void setMyList(List<Map<String, Object>> list) {
//            this.list = list;
//        }
//
//        public int getCount() {
//            return list.size()+3;
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            return position<3?0:1;
//        }
//
//        public int getItemViewId(int position) {
//            return position < 3 ? R.layout.homepage_top_item : R.layout.theme_item;
//        }
//
//        @Override
//        public int getViewTypeCount() {
//            return 2;
//        }
//
//        public Object getItem(int position) {
//            return position;
//        }
//
//        public long getItemId(int position) {
//            return 0;
//        }
//
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ViewHolder holder;
//            if(convertView==null){
//                convertView=OnlineThemesUtils.getContentViewByLayout(mContext,getItemViewId(position));
//                holder=new ViewHolder();
//                if(position>=3){
//                    holder.icon = (ImageView) convertView
//                            .findViewById(R.id.icon);
//                    holder.status = (ImageView) convertView
//                            .findViewById(R.id.status);
//                    holder.text = (TextView) convertView
//                            .findViewById(R.id.text);
//                }
//                convertView.setTag(getItemViewId(position),holder);
//            }else{
//                holder=(ViewHolder) convertView.getTag(getItemViewId(position));
//            }
//            if(position>=3){
//                String imageUrl = "";
//                Bitmap bitmap;
//                if (mIsRefrashing)
//                    return null;
//                else
//                    mIsRefrashing = true;
//
//                Map<String, Object> map = list.get(position-3);
//
//                imageUrl = (String)map.get("logoUrl");
//                mAsyncImageCache.displayImage(holder.icon, R.drawable.theme_no_default,
//                        mImageWidth, mImageHeight,
//                        new AsyncImageCache.NetworkImageGenerator(imageUrl, imageUrl));
//
//                holder.status.setImageResource(R.drawable.status_downloaded);
//                String isInstalled = map.get("isDownloaded").toString();
//                if (Boolean.valueOf(isInstalled)) {
//                    holder.status.setVisibility(View.VISIBLE);
//                } else {
//                    holder.status.setVisibility(View.INVISIBLE);
//                }
//                holder.text.setText(map.get("name").toString());
//            }
//
//            mIsRefrashing = false;
//            return convertView;
//        }
//
//        class ViewHolder {
//            ImageView icon;
//            ImageView status;
//            TextView text;
//        }
//    }
//
//    class DownloadReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if(intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){
//                long completeDownloadId = intent.getLongExtra(
//                        DownloadManager.EXTRA_DOWNLOAD_ID, -1);
//                if(mAdapter != null && mListData != null && mListData.size()>0)
//                    for(Map<String,Object> map : mListData){
//                        //long downloadId = PreferencesUtils.getLong(OnlineThemesActivity.this, ((Integer)map.get("id")).toString());
//                        long downloadId = PreferencesUtils.getLong(mContext, ((Integer)map.get("id")).toString());
//
//                        if(downloadId == completeDownloadId){
//                            if(hasDownloaded(downloadId)){
//                                map.put("isDownloaded", true);
//                                mAdapter.notifyDataSetChanged();
//                            }
//                            break;
//                        }
//                    }
//            }else{
//                int id = intent.getIntExtra("id", -1);
//                for(Map map : mListData){
//                    if((Integer)map.get("id") == id){
//                        map.put("isDownloaded", false);
//                        mAdapter.notifyDataSetChanged();
//                        break;
//                    }
//                }
//            }
//        }
//    }
//
//    DownloadManagerHelper mDownloadHelp;
//    private boolean hasDownloaded(long downloadId){
//        int status = mDownloadHelp.getStatusById(downloadId);
//        if(status == DownloadManager.STATUS_SUCCESSFUL){
//            return true;
//        }else{
//            return false;
//        }
//    }
//
//    public void setView(View v) {
//        view=v;
//    }
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        menu.clear();
//    }
//
//    @Override
//    public void onPrepareOptionsMenu(Menu menu) {
//        super.onPrepareOptionsMenu(menu);
//        menu.clear();
//    }
//
//    private Handler mAdHandler = new Handler(){ 
//        @Override 
//        public void handleMessage(Message msg) { 
//            if(msg.what == 1){
//                AdView.preLoad(getActivity(), "97509260"); 
//                final AdView droiAdView = new AdView(mContext, AdSize.Banner,
//                        "sb17c48d");
//                Log.w(TAG, ""+droiAdView);
//                droiAdView.setLayoutParams(new LayoutParams(690,280));
//                droiAdView.setListener(new AdViewListener() {
//                    @Override
//                    public void onEvent(String arg0) {
//                    }
//
//                    @Override
//                    public void onAdShow() {
//                        Log.d(TAG,"interstial onAdShow");
//                    }
//
//                    @Override
//                    public void onAdReady() {
//                        Log.d(TAG,"interstial onAdReady");
//
//                    }
//
//                    @Override
//                    public void onAdFailed(String arg0) {
//                        Log.d(TAG,"interstial onAdFailed:"+arg0);               
//                    }
//
//                    @Override
//                    public void onAdClick() {
//                        Log.d(TAG,"interstial onAdClick:");
//                    }
//
//                    @Override
//                    public void onDismiss() {
//                        Log.d(TAG,"interstial onDismiss:");
//                    }
//                });
//
//                final RelativeLayout r = new RelativeLayout(mContext);
//                r.addView(droiAdView);
//                banner.addView(r);
//            }
//        } 
//    }; 
//
//    private void getADroiAD(){
//        if(ThemeClubSystemProperties.DROI_ADROI_SUPPORT){
//            AdSwitchUtil.getAdSwitch(mContext,mAdHandler);
//        }
//
//    }
//
//    public class GetAdData extends 
//    AsyncTask<Object, Object, List<Map<String, Object>>> {
//        protected List<Map<String, Object>> doInBackground(Object... params) {
//            String result = null;
//            List<Map<String, Object>> list = null;
//
//            if (mWifiOpenStatus == -1){
//                result = OnlineThemesUtils.getListViewData("LargeAdvertisments.cfg");
//            }else{
//
//                try {
//                    JSONObject paraInfo = new JSONObject();
//                    paraInfo.put("lcd", OnlineThemesUtils
//                            .getAvailableResolutionForThisDevice(
//                                    getActivity(),
//                                    getResources().getStringArray(
//                                            R.array.resolution_array))[0]);
//                    paraInfo.put("homePage", 1);
//                    JSONObject jsObject = new JSONObject();
//
//                    jsObject.put("head", NetworkUtil.buildHeadData(MessageCode.GET_AD_LIST_BY_TAG_REQ));
//                    jsObject.put("body", paraInfo.toString());
//                    String contents = jsObject.toString();
//                    String url = MessageCode.SERVER_URL;
//                    result = NetworkUtil.accessNetworkByPost(url, contents);
//                    Log.w("LargeAdvertisments", "LargeAdvertisments"+result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                OnlineThemesUtils.saveListViewData(result,
//                        "LargeAdvertisments.cfg");
//            }
//            if (result != null) {
//                list = ResultUtil.splitADServerListData(result);
//            } 
//
//            return list;
//        }
//
//        protected void onPostExecute(List<Map<String, Object>> list) {
//            if (list != null) {
//                Log.w("list.size()", "list.size()"+list.size());
//                for (int i = 0; i < list.size(); i++) {
//                    adListData.add(list.get(i));
//                    addNewsLogo(list.get(i).get("adverUrl")+"",i);
//                }
//                getADroiAD();
//                super.onPostExecute(list);
//            }
//        }
//    }
//    private long lastClick;
//    public void addNewsLogo(String imgUrl,final int position) {
//        View child = null;
//        if (TextUtils.isEmpty(imgUrl)) {
//            child = initBinner(R.drawable.homepage_banner_default);
//        } else {
//            imgUrl=encodeUrl(imgUrl)+"";
//            child = initBinner(imgUrl, R.drawable.homepage_banner_default);
//            child.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View arg0) {
//                    if(getAPNType(getActivity())==-1){
//                        CustomToast.showToast(getActivity(), getActivity().getResources().getString(R.string.check_wlan),3000);
//                    }
//                    if (System.currentTimeMillis() - lastClick >= 1000)  
//                    {  
//                        lastClick = System.currentTimeMillis(); 
//                        new GetAdDetail().executeOnExecutor(MainActivity.fixedThreadPool,position);
//                    }
//
//                    String name=adListData.get(position).get("adverName").toString();
//                    String infoStr=LocalUtil.saveStatisticInfo(LocalUtil.CLICK_ACTION_ID, LocalUtil.HOME_CLICK_AD, name, System.currentTimeMillis());
//                    mStatisticDBHelper.intserStatisticdataToDB(infoStr);
//                }
//            });
//        }
//        banner.addView(child);
//        banner.setDataReady(true);
//
//        if (banner.getChildCount() > 1) {
//            banner.startAutoScroll();
//        }
//    }
//
//    public View initBinner(String url, int defaultResId) {
//        int width=690;
//        int height=280;
//        ImageView logo = new ImageView(mContext);
//        logo.setScaleType(ScaleType.FIT_XY);
//        BannerItemContainer container = new BannerItemContainer(mContext);
//        mAsyncImageCache.displayImage(logo, defaultResId, width,height,
//                new AsyncImageCache.NetworkImageGenerator(url, url));
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.MATCH_PARENT,
//                FrameLayout.LayoutParams.MATCH_PARENT);
//        container.addView(logo, params);
//        return container;
//    }
//
//    public static URL encodeUrl(String path) {
//
//        try {
//            path = URLEncoder.encode(path, "utf-8");
//            path = path.replace("%2F", "/");
//            path = path.replace("%3A", ":");
//            URL u=new URL(path);
//            return u;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    public View initBinner(int resId) {
//        ImageView logo = new ImageView(mContext);
//        logo.setImageResource(resId);
//        logo.setScaleType(ScaleType.CENTER);
//        BannerItemContainer container = new BannerItemContainer(mContext);
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
//                FrameLayout.LayoutParams.MATCH_PARENT);
//        container.addView(logo, params);
//        return container;
//    }
//
//    public class GetAdDetail extends
//    AsyncTask<Object, Object, List<Map<String, Object>>> {
//        protected List<Map<String, Object>> doInBackground(Object... params) {
//            String result = null;
//            List<Map<String, Object>> list = null;
//            try {
//                JSONObject paraInfo = new JSONObject();
//                subType=(Integer)adListData.get((Integer)params[0]).get("subType");
//                paraInfo.put("subType",subType);
//                paraInfo.put("subId", adListData.get((Integer)params[0]).get("subId"));
//                JSONObject jsObject = new JSONObject();
//                jsObject.put("head", NetworkUtil.buildHeadData(MessageCode.GET_AD_DETAIL_BY_TAG_REQ));
//                jsObject.put("body", paraInfo.toString());
//                String contents = jsObject.toString();
//                String url = MessageCode.SERVER_URL;
//                result = NetworkUtil.accessNetworkByPost(url, contents);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            if (result != null) {
//                if(subType==1){
//                    list = ResultUtil.splitThemeDetailServerListData(result);
//                }
//                if(subType==2){
//                    list = ResultUtil.splitScreenDetailServerListData(result);
//                }
//                if(subType==3){
//                    list = ResultUtil.splitWallpaperDetailListJSON(result);
//                }
//
//            } 
//            if(list != null){
//                for (int i = 0; i < list.size(); i++) {  
//                    list.get(i).put("isDownloaded", OnlineThemesUtils.checkInstalled(mContext, (String)list.get(i).get("packageName")));
//                }
//            }
//            return list;
//        }
//
//        protected void onPostExecute(List<Map<String, Object>> list) {
//            if (list != null) {
//                adDetailData=new ArrayList<Map<String,Object>>();
//                for (int i = 0; i < list.size(); i++) {
//                    adDetailData.add(list.get(i));
//                    if(subType==1||subType==2){
//                        Intent intent = new Intent(mContext,
//                                OnlineThemesDetailActivity.class);
//                        intent.putExtra("list_id", i);
//                        intent.putExtra("mlistData", adDetailData);
//                        intent.putExtra("isOnlineLockscreen", subType==1?false:true);
//                        startActivity(intent);
//                    }
//                    if(subType==3){
//                        Intent intent = new Intent(getActivity(),
//                                OnlineWallpaperDetailActivity.class);
//                        intent.putExtra(OnlineUtils.MyClickPosition, i);
//                        intent.putExtra(OnlineUtils.MyListData,adDetailData);
//                        intent.putExtra(IntentConstants.EXTRA_RESOURCE_FLAG, 0);
//                        startActivity(intent);
//                    }
//                }
//                super.onPostExecute(list);
//            }
//        }
//
//    }
//    class DeletePackageReceiver extends BroadcastReceiver{
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String packageName=intent.getDataString().substring(8);
//            for (Map<String, Object> map : mListData) {
//                if(((String)map.get("packageName")).equals(packageName)){
//                    map.put("isDownloaded", false);
//                    mAdapter.notifyDataSetChanged();
//                    break;
//                }
//            }
//
//        }
//
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (resultCode) {
//        case 10000:
//            getActivity().finish();
//            break;
//
//        default:
//            break;
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//}
//
