package com.freeme.themeclub.theme.onlinetheme;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.internal.util.AsyncImageCache;
import com.freeme.themeclub.BackScrollFragment;
import com.freeme.themeclub.CustomToast;
import com.freeme.themeclub.MainActivity;
import com.freeme.themeclub.R;
import com.freeme.themeclub.banner.AutoScrollLoopBanner;
import com.freeme.themeclub.banner.BannerItemContainer;
import com.freeme.themeclub.lockscreen.EssenceLockscreenFragment;
import com.freeme.themeclub.lockscreen.LockscreenFragment;
import com.freeme.themeclub.lockscreen.NewestLockscreenFragment;
import com.freeme.themeclub.lockscreen.PopularLockscreenFragment;
import com.freeme.themeclub.statisticsdata.db.StatisticDBHelper;
import com.freeme.themeclub.theme.ThemeFragment;
import com.freeme.themeclub.theme.onlinetheme.OnlineThemesFragment.GetOnlineThemeData;
import com.freeme.themeclub.theme.onlinetheme.download.DownloadManagerHelper;
import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;
import com.freeme.themeclub.theme.onlinetheme.util.NetworkUtil;
import com.freeme.themeclub.theme.onlinetheme.util.OnlineThemesUtils;
import com.freeme.themeclub.theme.onlinetheme.util.PreferencesUtils;
import com.freeme.themeclub.theme.onlinetheme.util.ResultUtil;
import com.freeme.themeclub.wallpaper.OnlineWallpaperDetailActivity;
import com.freeme.themeclub.wallpaper.base.IntentConstants;
import com.freeme.themeclub.wallpaper.util.OnlineUtils;

public class CategoryThemesListFragment extends BackScrollFragment implements 
OnItemClickListener , OnTouchListener{

    private static final String TAG = "OnlineThemesActivity";

    // request param
    public static final String THEME_VERSION = "v500";
    public static final String PROJECT_NAME = "koobee";

    private AsyncImageCache mAsyncImageCache;
    private DownloadReceiver completeReceiver;
    private DeletePackageReceiver deletePackageReceiver;
    private LinearLayout mFooter;
    private LinearLayout mSearch_loading;
    public  LinearLayout mRefresh_linearLayout_id;
    private GridView mGridView;
    private OnlineThemesMyAdapter mAdapter;
    private boolean mSyncFlag = false;
    private boolean mIsScrolling = false;
    private boolean mRefreshFinished = false;
    private boolean mGetListDataFail = false;

    // page show
    private int mPage_Index = 0;
    private int mStart_numer = 0;
    private int mPage_numer = 9;
    private Handler mHandler;
    private final int REFRESH_LIST = 2;
    public ArrayList<Map<String, Object>> mListData;
    private static ArrayList<Map<String, Object>> adListData;
    private static ArrayList<Map<String, Object>> adDetailData;
    private int mWifiOpenStatus = 0;
    private int mSavaInDbCount = 0;
    public boolean isOnlineThemes;
    public boolean isOnlineLockscreens;

    public int msgCode;
    public String sort="01";
    public String bout="0";

    private Activity mActivity;
    private Context mContext;
    private static int flag;

    public int temp;

    private int subType2;
    public String subType;

    public int serialNum;
    FragmentManager fragmentManager ;

    private StatisticDBHelper mStatisticDBHelper;
    private ScrollView scrollView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mContext = mActivity.getBaseContext();
        mAsyncImageCache = AsyncImageCache.from(mActivity);
        mWifiOpenStatus = NetworkUtil.getAPNType(mContext);
        sort="02";
        serialNum=4;
        OnlineThemesUtils.savaDownloadCount(mSavaInDbCount);

        new GetOnlineThemeData().execute();
        mHandler = new Handler() {
            public void handleMessage(Message message) {
                super.handleMessage(message);
                switch (message.what) {
                case REFRESH_LIST:
                    if (mIsScrolling == false && mAdapter != null)
                        mAdapter.notifyDataSetChanged();
                    break;
                }
            }
        };

        mDownloadHelp = new DownloadManagerHelper(
                (DownloadManager) mActivity
                .getSystemService(mContext.DOWNLOAD_SERVICE));
        completeReceiver = new DownloadReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction(OnlineThemesDetailActivity.ACTION_DELETE_DOWNLOAD);
        mActivity.registerReceiver(completeReceiver, filter);

        deletePackageReceiver = new DeletePackageReceiver();
        IntentFilter filter2 = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
        filter2.addDataScheme("package");
        mActivity.registerReceiver(deletePackageReceiver, filter2);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.category_oneline_themes,
                null);

        findViews(contentView);
        mListData = new ArrayList<Map<String, Object>>();
        setEmptyViewIfNoNet(mGridView);
        return contentView;
    }

    @Override
    public void onDestroy() {
        mAsyncImageCache.stop();
        mActivity.unregisterReceiver(completeReceiver);
        mActivity.unregisterReceiver(deletePackageReceiver);
        super.onDestroy();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private void setEmptyViewIfNoNet(final GridView listView) {
        mSearch_loading.setVisibility(View.VISIBLE);
        mGridView.setVisibility(View.GONE);
    }

    private void findViews(View v) {
        mGridView = (GridView) v.findViewById(R.id.theme_grid);
        mFooter = (LinearLayout) v.findViewById(R.id.grid_foot);
        mFooter.setVisibility(View.GONE);
        mGridView.setOnItemClickListener(this);
        scrollView = (ScrollView)v.findViewById(R.id.scrollview);
        scrollView.setOnTouchListener(this);

        mSearch_loading = (LinearLayout) v.findViewById(R.id.search_loading);
        mRefresh_linearLayout_id = (LinearLayout) v
                .findViewById(R.id.refresh_linearLayout_id);
        mRefresh_linearLayout_id.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mWifiOpenStatus = NetworkUtil.getAPNType(mContext);
                new GetOnlineThemeData().execute();
            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {

        Intent intent = new Intent(mContext,
                OnlineThemesDetailActivity.class);
        if (null != mAdapter && null != mListData && mListData.size() > 0) {
            intent.putExtra("list_id", position );
            intent.putExtra("mlistData", mListData);
            intent.putExtra("isOnlineLockscreen", isOnlineLockscreens);
        }
        startActivityForResult(intent, 10000);

    }

    public class GetOnlineThemeData extends
    AsyncTask<Object, Object, List<Map<String, Object>>> {
        protected List<Map<String, Object>> doInBackground(Object... params) {
            String result = null;

            List<Map<String, Object>> list = null;

            if (mPage_Index == 0 && mWifiOpenStatus == -1) {
                result = OnlineThemesUtils.getListViewData("OnlineThemes"+isOnlineThemes+serialNum+subType+".cfg");
            } else {
                try {
                    JSONObject paraInfo = new JSONObject();
                    paraInfo.put("mf", PROJECT_NAME);
                    paraInfo.put("lcd", OnlineThemesUtils
                            .getAvailableResolutionForThisDevice(
                                    mContext,
                                    getResources().getStringArray(
                                            R.array.resolution_array))[0]);
                    paraInfo.put("ver", THEME_VERSION);
                    if(isOnlineLockscreens){
                        paraInfo.put("ver", "v600");
                    }
                    paraInfo.put("type", "01");
                    // paraInfo.put("dev", "X9");
                    paraInfo.put("sort", sort);
                    paraInfo.put("bout", bout);
                    paraInfo.put("subType", subType);
                    paraInfo.put("from", String.valueOf(mPage_Index));
                    paraInfo.put("to",
                            String.valueOf(mPage_Index + mPage_numer));

                    JSONObject jsObject = new JSONObject();
                    jsObject.put("head", NetworkUtil.buildHeadData(msgCode));
                    jsObject.put("body", paraInfo.toString());
                    String contents = jsObject.toString();
                    String url = MessageCode.SERVER_URL;
                    result = NetworkUtil.accessNetworkByPost(url, contents);
                    Log.w("yzy_theme_result",result);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (mPage_Index == 0&&result!=null)
                    OnlineThemesUtils.saveListViewData(result,
                            "OnlineThemes"+isOnlineThemes+serialNum+subType+".cfg");
            }

            if (result != null) {
                if (isOnlineThemes) {
                    list = ResultUtil.splitThemeServerListData(result);
                } else if (isOnlineLockscreens) {
                    list = ResultUtil.splitLockScreenServerListData(result);
                } else {
                    throw new RuntimeException();
                }
            }

            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    long downloadId = PreferencesUtils.getLong(mContext,
                            ((Integer) list.get(i).get("id")).toString());
                    list.get(i).put(
                            "isDownloaded",
                            OnlineThemesUtils.checkInstalled(mContext,
                                    (String) list.get(i).get("packageName")));
                }
            }

            return list;
        }

        protected void onPostExecute(List<Map<String, Object>> list) {
            if (list != null) {
                if(list.size()==0){
                    CustomToast.showToast(mContext, R.string.no_more_data, 3000);
                }
                mSearch_loading.setVisibility(View.GONE);

                mRefresh_linearLayout_id.setVisibility(View.GONE);

                if (mGridView.getVisibility() == View.GONE)
                    mGridView.setVisibility(View.VISIBLE);

                mGetListDataFail = false;

                for (int i = 0; i < list.size(); i++) {
                    mListData.add(list.get(i));
                }
                if (list.size() < mPage_numer || list.size() == 0) {
                    //mGridView.setOnScrollListener(null);
                    mIsScrolling = false;
                }
                if (mPage_Index == 0) {
                    mAdapter = new OnlineThemesMyAdapter(mContext, mListData,
                            mGridView);
                    mGridView.setAdapter(mAdapter);
                } else {
                    try {
                        if (mIsScrolling == false && mAdapter != null)
                            mHandler.sendEmptyMessage(REFRESH_LIST);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mRefreshFinished = true;
            } else {
                mGetListDataFail = true;

                mSearch_loading.setVisibility(View.GONE);

                if (mListData.size() == 0) {
                    mRefresh_linearLayout_id.setVisibility(View.VISIBLE);
                    mGridView.setVisibility(View.GONE);
                } else {
                    mRefresh_linearLayout_id.setVisibility(View.GONE);
                    mRefreshFinished = true;
                }
            }
            mFooter.setVisibility(View.GONE);
            super.onPostExecute(list);
            mSyncFlag = true;
        }
    }

    public class OnlineThemesMyAdapter extends BaseAdapter {
        private Context mContext;
        private List<Map<String, Object>> list;

        private boolean mIsRefrashing = false;

        private int mImageWidth;
        private int mImageHeight;

        public OnlineThemesMyAdapter(Context context,
                List<Map<String, Object>> list, GridView gridView) {
            this.mContext = context;
            this.list = list;
            mGridView = gridView;

            mImageWidth = context.getResources().getDimensionPixelSize(
                    R.dimen.theme_preview_w);
            mImageHeight = context.getResources().getDimensionPixelSize(
                    R.dimen.theme_preview_h);

        }

        public void setMyList(List<Map<String, Object>> list) {
            this.list = list;
        }

        public int getCount() {
            return list.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            String imageUrl = "";
            if (mIsRefrashing)
                return null;
            else
                mIsRefrashing = true;
            try {
                if(convertView == null){
                    convertView = OnlineThemesUtils.getContentViewByLayout(
                            mContext, R.layout.theme_item);
                    holder = new ViewHolder();
                    holder.icon = (ImageView) convertView
                            .findViewById(R.id.icon);
                    holder.status = (ImageView) convertView
                            .findViewById(R.id.status);
                    holder.text = (TextView) convertView
                            .findViewById(R.id.text);
                    holder.downloadTimes = (TextView) convertView
                            .findViewById(R.id.download_times);
                    convertView.setTag(holder);
                }else{
                    holder = (ViewHolder) convertView.getTag();
                }
                Map<String, Object> map = list.get(position);

                imageUrl = (String) map.get("logoUrl");
                mAsyncImageCache.displayImage(holder.icon,
                        R.drawable.theme_no_default, mImageWidth,
                        mImageHeight,
                        new AsyncImageCache.NetworkImageGenerator(imageUrl,
                                imageUrl));

                holder.status.setImageResource(R.drawable.status_downloaded);
                String isInstalled = map.get("isDownloaded").toString();
                if (Boolean.valueOf(isInstalled)) {
                    holder.status.setVisibility(View.VISIBLE);
                } else {
                    holder.status.setVisibility(View.INVISIBLE);
                }
                holder.text.setText(map.get("name").toString());
                holder.downloadTimes.setText(map.get("dnCnt")+
                        getResources().getString(R.string.uses_times));


            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            mIsRefrashing = false;
            return convertView;
        }

        class ViewHolder {
            ImageView icon;
            ImageView status;
            TextView text;
            TextView downloadTimes;
        }
    }

    class DeletePackageReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String packageName=intent.getDataString().substring(8);
            for (Map<String, Object> map : mListData) {
                if(((String)map.get("packageName")).equals(packageName)){
                    Log.w("yzy","onReceive"+"come...");
                    map.put("isDownloaded", false);
                    mAdapter.notifyDataSetChanged();
                    break;
                }
            }

        }

    }


    class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                long completeDownloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (mAdapter != null && mListData != null
                        && mListData.size() > 0)
                    for (Map<String, Object> map : mListData) {
                        long downloadId = PreferencesUtils.getLong(mContext,
                                ((Integer) map.get("id")).toString());

                        if (downloadId == completeDownloadId) {
                            if (hasDownloaded(downloadId)) {
                                map.put("isDownloaded", true);
                                mAdapter.notifyDataSetChanged();
                            }
                            break;
                        }
                    }
            } else {
                int id = intent.getIntExtra("id", -1);
                for (Map<String, Object> map : mListData) {
                    if ((Integer) map.get("id") == id) {
                        map.put("isDownloaded", false);
                        mAdapter.notifyDataSetChanged();
                        break;
                    }
                }

            }
        }
    }

    DownloadManagerHelper mDownloadHelp;

    private boolean hasDownloaded(long downloadId) {
        int status = mDownloadHelp.getStatusById(downloadId);
        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
        case 10000:
            Log.w("yzy", "categorythemeslist");
            getActivity().setResult(10000);
            getActivity().finish();
            break;

        default:
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            break;
        case MotionEvent.ACTION_MOVE:
            break;
        case MotionEvent.ACTION_UP:
            if(NetworkUtil.getAPNType(mContext)==-1){
                CustomToast.showToast(getActivity(), getString(
                        R.string.check_wlan), 3000);
            }
            if (OnlineUtils.scrollButtom(v,mSearch_loading)) {                              
                mIsScrolling = false;
                if (mRefreshFinished) {
                    //                if (!mGetListDataFail) {
                    mStart_numer = mStart_numer + 1;
                    mPage_Index = mStart_numer * mPage_numer;
                    mRefreshFinished = false;
                    mFooter.setVisibility(View.VISIBLE);
                    new GetOnlineThemeData().executeOnExecutor(MainActivity.fixedThreadPool);
                    //                }
                }
            }
        default:
            break;
        }
        return false;
    }
}
