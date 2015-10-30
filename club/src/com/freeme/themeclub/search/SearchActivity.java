package com.freeme.themeclub.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.util.AsyncImageCache;

import com.freeme.themeclub.CustomToast;
import com.freeme.themeclub.MainActivity;
import com.freeme.themeclub.R;
import com.freeme.themeclub.theme.onlinetheme.OnlineThemesDetailActivity;
import com.freeme.themeclub.theme.onlinetheme.OnlineThemesFragment;
import com.freeme.themeclub.theme.onlinetheme.OnlineThemesFragment.GetAdData;
import com.freeme.themeclub.theme.onlinetheme.OnlineThemesFragment.GetOnlineThemeData;
import com.freeme.themeclub.theme.onlinetheme.download.DownloadManagerHelper;
import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;
import com.freeme.themeclub.theme.onlinetheme.util.NetworkUtil;
import com.freeme.themeclub.theme.onlinetheme.util.OnlineThemesUtils;
import com.freeme.themeclub.theme.onlinetheme.util.PreferencesUtils;
import com.freeme.themeclub.theme.onlinetheme.util.ResultUtil;
import com.freeme.themeclub.theme.onlinetheme.util.ThemeClubRequestParams;
import com.freeme.themeclub.wallpaper.OnlineWallpaperDetailActivity;
import com.freeme.themeclub.wallpaper.base.IntentConstants;
import com.freeme.themeclub.wallpaper.util.OnlineUtils;

public class SearchActivity extends Activity implements OnClickListener,OnItemClickListener,OnScrollListener{

    public static final String SEARCH_TYPE = "search_type";

    public static final String WALLPAPER_ID = "id";
    public static final String WALLPAPER_NAME = "name";
    public static final String WALLPAPER_THUMB_URL = "dnUrlS";
    public static final String WALLPAPER_ORIGNAL_URL = "dnUrlX";
    public static final String WALLPAPER_DOWNLOAD_COUNT = "dnCnt";
    public static final String WALLPAPER_MODIFY_TIME = "modifyTime";

    private String type;
    private GridView mThemeGridView;
    private GridView mWallpaperGridView;
    private TextView mSearchTypeTxt;
    private LinearLayout mSearch_loading;
    private LinearLayout mSearchBtn;
    private EditText mSearchKey;
    private PopupWindow mPopupWindow;
    private LinearLayout mSearchArea;
    private ViewGroup mPopupWindowContentView;
    private LinearLayout mSearchTypeArea;
    private LinearLayout mFooter;
    private LinearLayout mRefresh_linearLayout_id;
    private RelativeLayout mNoData;

    private OnlineThemesAdapter themesAdapter;
    private OnlineWallpaperAdapter wallpaperAdapter;

    private List<String> spinnerData;

    private ArrayList<Map<String, Object>> mListData;

    private AsyncImageCache mAsyncImageCache;
    private DownloadManagerHelper mDownloadHelp;
    private DownloadReceiver completeReceiver;
    private DeletePackageReceiver deletePackageReceiver;

    private Context mContext;
    private boolean isOnlineThemes;
    private boolean isOnlineLockscreens;
    private boolean isOnlineWallpaper;

    private boolean mCanSearch;

    private boolean mRefreshFinished = false;
    private int mPage_Index = 0;
    private int mStart_numer = 0;
    private int mPage_numer = 9;

    private ArrayList<SearchTask> searchTaskList = new ArrayList<SearchActivity.SearchTask>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = getBaseContext();
        mAsyncImageCache = AsyncImageCache.from(this);

        initView();
        initListener();

        Intent intent = getIntent();
        type = intent.getStringExtra(SEARCH_TYPE);
        mSearchTypeTxt.setText(type);

        mDownloadHelp = new DownloadManagerHelper(
                (DownloadManager)getSystemService(mContext.DOWNLOAD_SERVICE));

        completeReceiver = new DownloadReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction(OnlineThemesDetailActivity.ACTION_DELETE_DOWNLOAD);
        registerReceiver(completeReceiver, filter);

        deletePackageReceiver = new DeletePackageReceiver();
        IntentFilter filter2 = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
        filter2.addDataScheme("package");
        registerReceiver(deletePackageReceiver, filter2);
    }

    private void initView() {
        mSearchArea = (LinearLayout)findViewById(R.id.search_area);
        mSearchTypeTxt = (TextView)findViewById(R.id.search_type);
        mThemeGridView = (GridView)findViewById(R.id.theme_grid);
        mWallpaperGridView = (GridView)findViewById(R.id.wallpaper_grid);
        mSearch_loading = (LinearLayout)findViewById(R.id.search_loading);
        mSearchBtn = (LinearLayout)findViewById(R.id.search_button);
        mSearchKey = (EditText)findViewById(R.id.search_key);
        mPopupWindowContentView = (ViewGroup)LayoutInflater.from(mContext).inflate(
                R.layout.pop_window, null);
        mPopupWindow = new PopupWindow(mPopupWindowContentView,
                LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(
                getResources().getDrawable(R.drawable.search_type_background));
        mSearchTypeArea = (LinearLayout)findViewById(R.id.search_type_area);
        mFooter = (LinearLayout) findViewById(R.id.grid_foot);
        mFooter.setVisibility(View.GONE);
        mRefresh_linearLayout_id = (LinearLayout)findViewById(R.id.refresh_linearLayout_id);
        mNoData = (RelativeLayout)findViewById(R.id.no_data);
    }

    private void initListener() {
        mSearchBtn.setOnClickListener(this);
        mSearchKey.addTextChangedListener(new EditChangedListener());
        mThemeGridView.setOnItemClickListener(this);
        mThemeGridView.setOnScrollListener(this);
        mWallpaperGridView.setOnItemClickListener(this);
        mWallpaperGridView.setOnScrollListener(this);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setTouchInterceptor(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        mSearchTypeArea.setOnClickListener(this);
        for (int i = 0; i < mPopupWindowContentView.getChildCount(); i++) {
            View child = mPopupWindowContentView.getChildAt(i);
            if(child instanceof TextView){
                child.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mSearchTypeTxt.setText(((TextView)v).getText());
                        mPopupWindow.dismiss();
                    }
                });
            }
        }

        findViewById(R.id.set_wlan).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(Settings.ACTION_WIFI_SETTINGS);  
                startActivity(intent);
            }
        });
        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mRefresh_linearLayout_id.setVisibility(View.GONE);
                mSearch_loading.setVisibility(View.VISIBLE);
                obtainData();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.search_type_area:
            mPopupWindow.showAsDropDown(mSearchArea);
            break;
        case R.id.search_button:
            if(mCanSearch){
                hideSoftInput();
                obtainData();
            }
            break;
        }

    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideSoftInput(){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);   
        imm.hideSoftInputFromWindow(mSearchKey.getWindowToken(),0);
    }

    private void obtainData() {
        resetData();
        if(mSearchTypeTxt.getText().equals(getString(R.string.theme))){
            isOnlineThemes = true;
        }else if(mSearchTypeTxt.getText().equals(getString(R.string.lockscreen))){
            isOnlineLockscreens = true;
        }else{
            isOnlineWallpaper = true;
        }
        SearchTask task = new SearchTask();
        task.executeOnExecutor(MainActivity.fixedThreadPool);
        searchTaskList.add(task);
    }

    private void resetSearchTaskList(){
        for (int i = 0; i < searchTaskList.size(); i++) {
            searchTaskList.get(i).cancel(true);
        }
    }

    private void resetData(){
        isOnlineThemes = false;
        isOnlineLockscreens = false;
        isOnlineWallpaper = false;
        mPage_Index = 0;
        mStart_numer = 0;
        mListData = new ArrayList<Map<String, Object>>();
        mSearch_loading.setVisibility(View.VISIBLE);
        mThemeGridView.setVisibility(View.INVISIBLE);
        mWallpaperGridView.setVisibility(View.INVISIBLE);
        mRefresh_linearLayout_id.setVisibility(View.GONE);
        mNoData.setVisibility(View.GONE);
        resetSearchTaskList();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        if(parent == mThemeGridView){
            Intent intent = new Intent(mContext,
                    OnlineThemesDetailActivity.class);
            if (null != themesAdapter && null != mListData && mListData.size() > 0) {
                intent.putExtra("list_id", position );
                intent.putExtra("mlistData", mListData);
                intent.putExtra("isOnlineLockscreen", isOnlineLockscreens);
            }
            startActivity(intent);
            return ;
        }

        if(parent == mWallpaperGridView){
            Intent intent2 = new Intent(this,
                    OnlineWallpaperDetailActivity.class);
            List<Map<String, Object>> mCurrentData = wallpaperAdapter
                    .getAdapterData();
            intent2.putExtra(OnlineUtils.MyClickPosition, position );
            intent2.putExtra(OnlineUtils.MyListData,
                    (ArrayList<Map<String, Object>>) mCurrentData);
            intent2.putExtra(IntentConstants.EXTRA_RESOURCE_FLAG, 0);
            intent2.putExtra(OnlineUtils.MySetSelect, OnlineUtils.WideSreen);

            startActivityForResult(intent2, 100);
        }
    }


    private class SearchTask extends AsyncTask<Object, Object, List<Map<String, Object>>>{

        @Override
        protected List<Map<String, Object>> doInBackground(Object... params) {
            String result = null;

            List<Map<String, Object>> list = null;
            if(NetworkUtil.getAPNType(mContext)==-1){
                return null;
            }
            try {
                JSONObject paraInfo = new JSONObject();
                paraInfo.put(ThemeClubRequestParams.NAMES_CHANNEL, ThemeClubRequestParams.VALUES_CHANNEL);
                paraInfo.put(ThemeClubRequestParams.NAMES_LCD, ThemeClubRequestParams.getLCD(mContext));
                if(isOnlineThemes){
                    paraInfo.put(ThemeClubRequestParams.NAMES_VERSION, ThemeClubRequestParams.VALUES_THEME_VERSION);
                    paraInfo.put(ThemeClubRequestParams.NAMES_TYPE, ThemeClubRequestParams.VALUES_THEME_TYPE);
                }else if(isOnlineLockscreens){
                    paraInfo.put(ThemeClubRequestParams.NAMES_VERSION, ThemeClubRequestParams.VALUES_LOCKSCREEN_VERSION);
                    paraInfo.put(ThemeClubRequestParams.NAMES_TYPE, ThemeClubRequestParams.VALUES_LOCKSCREEN_TYPE);
                }else{
                    paraInfo.put(ThemeClubRequestParams.NAMES_TYPE, ThemeClubRequestParams.VALUES_WALLPAPER_TYPE);
                }
                paraInfo.put(ThemeClubRequestParams.NAMES_CONTENT, mSearchKey.getText());
                paraInfo.put("from", String.valueOf(mPage_Index));
                paraInfo.put("to",
                        String.valueOf(mPage_Index + mPage_numer));
                JSONObject jsObject = new JSONObject();
                jsObject.put("head", NetworkUtil.buildHeadData(MessageCode.GET_SEARCH_RESULT_BY_TAG_REQ));
                jsObject.put("body", paraInfo.toString());
                String contents = jsObject.toString();
                String url = MessageCode.SERVER_URL;
                result = NetworkUtil.accessNetworkByPost(url, contents);
                Log.i("yzy", "paraInfo = "+paraInfo);
                Log.i("yzy", "result = "+result);

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (result != null) {
                if(isOnlineWallpaper){
                    list = OnlineUtils.splitThumbListJSON(result);
                }else{
                    if (isOnlineThemes) {
                        list = ResultUtil.splitThemeServerListData(result);
                    } else {
                        list = ResultUtil.splitLockScreenServerListData(result);
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
                }
            }
            return list;
        }

        protected void onPostExecute(List<Map<String, Object>> list) {
            if(isCancelled()){
                return ;
            }
            mRefreshFinished = true;
            mFooter.setVisibility(View.GONE);
            mSearch_loading.setVisibility(View.GONE);
            if (list != null) {
                if(list.size()==0){
                    if (mListData.size() == 0) {
                        mNoData.setVisibility(View.VISIBLE);
                    }else{
                        CustomToast.showToast(mContext, R.string.no_more_data, 3000);
                    }
                    return ;
                }
                for (int i = 0; i < list.size(); i++) {
                    mListData.add(list.get(i));
                }
                if(isOnlineWallpaper){
                    mWallpaperGridView.setVisibility(View.VISIBLE);
                    wallpaperAdapter = new OnlineWallpaperAdapter(mContext, mListData);
                    mWallpaperGridView.setAdapter(wallpaperAdapter);
                }else{
                    mThemeGridView.setVisibility(View.VISIBLE);
                    themesAdapter = new OnlineThemesAdapter(mContext, mListData);
                    mThemeGridView.setAdapter(themesAdapter);
                }

            }else{
                if(mListData.size() == 0){
                    mRefresh_linearLayout_id.setVisibility(View.VISIBLE);
                }
            }

            super.onPostExecute(list);
        }

    }


    public class OnlineThemesAdapter extends BaseAdapter {
        private Context mContext;
        private List<Map<String, Object>> list;

        private boolean mIsRefrashing = false;

        private int mImageWidth;
        private int mImageHeight;

        public OnlineThemesAdapter(Context context,
                List<Map<String, Object>> list) {
            this.mContext = context;
            this.list = list;

            mImageWidth = context.getResources().getDimensionPixelSize(
                    R.dimen.theme_preview_w);
            mImageHeight = context.getResources().getDimensionPixelSize(
                    R.dimen.theme_preview_h);

        }

        public int getCount() {
            return list.size() ;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public Object getItem(int position) {
            return null;
        }


        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView==null){
                convertView=LayoutInflater.from(mContext).inflate(R.layout.theme_item, null);
                holder=new ViewHolder();

                holder.icon = (ImageView) convertView
                        .findViewById(R.id.icon);
                holder.status = (ImageView) convertView
                        .findViewById(R.id.status);
                holder.text = (TextView) convertView
                        .findViewById(R.id.text);

                convertView.setTag(holder);
            }else{
                holder=(ViewHolder) convertView.getTag();
            }
            String imageUrl = "";

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

            mIsRefrashing = false;
            return convertView;
        }

        class ViewHolder {
            ImageView icon;
            ImageView status;
            TextView text;
        }
    }

    private class OnlineWallpaperAdapter extends BaseAdapter {
        private Context mContext;
        private List<Map<String, Object>> mDataList;
        private LayoutInflater mLayoutInflater;
        private Resources res;

        public OnlineWallpaperAdapter(Context context,
                List<Map<String, Object>> dataList) {
            this.mContext = context;
            this.mDataList = dataList;
            mLayoutInflater = LayoutInflater.from(mContext);
            res = mContext.getResources();
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


            mThumbHolder.previewThumb
            .setLayoutParams(new RelativeLayout.LayoutParams(
                    res.getDimensionPixelSize(R.dimen.wide_thumb_preview_w),
                    res.getDimensionPixelSize(R.dimen.wide_thumb_preview_h)));
            if(mDataList!=null){
                Map<String, Object> thumbItem = mDataList.get(position);
                String thumbUrl = (String) thumbItem.get(WALLPAPER_THUMB_URL);
                mAsyncImageCache.displayImage(
                        mThumbHolder.previewThumb,
                        R.drawable.wallpaper_no_default2,
                        res.getDimensionPixelSize( R.dimen.wide_thumb_preview_w),
                        res.getDimensionPixelSize(R.dimen.wide_thumb_preview_h),
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

    private class EditChangedListener implements TextWatcher {

        @Override  
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }  

        @Override  
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mCanSearch = true;
            if(s == null || s.length() == 0){
                ((ImageView)(mSearchBtn.getChildAt(0))).setImageResource(R.drawable.search_no_content);
                mCanSearch = false;
            }else{
                ((ImageView)(mSearchBtn.getChildAt(0))).setImageResource(R.drawable.search_has_content);
            }
        }  

        @Override  
        public void afterTextChanged(Editable s) {
        }  
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
        case OnScrollListener.SCROLL_STATE_IDLE:

            if(NetworkUtil.getAPNType(mContext)==-1){
                CustomToast.showToast(mContext, getString(R.string.check_wlan), 3000);
            }
            if (view.getLastVisiblePosition()>= (view.getCount() - 1)
                    && mRefreshFinished) {
                mStart_numer = mStart_numer + 1;
                mPage_Index = mStart_numer * mPage_numer;
                mRefreshFinished = false;
                mFooter.setVisibility(View.VISIBLE);
                SearchTask task = new SearchTask();
                task.executeOnExecutor(MainActivity.fixedThreadPool);
                searchTaskList.add(task);
            }
            break;
        case OnScrollListener.SCROLL_STATE_FLING:
            break;
        case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
        case 100:
            if (isOnlineWallpaper && wallpaperAdapter != null)
                wallpaperAdapter.notifyDataSetChanged();
            break;
        default:
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class DeletePackageReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String packageName=intent.getDataString().substring(8);
            for (Map<String, Object> map : mListData) {
                if(((String)map.get("packageName")).equals(packageName)){
                    map.put("isDownloaded", false);
                    themesAdapter.notifyDataSetChanged();
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
                if (themesAdapter != null && mListData != null
                        && mListData.size() > 0)
                    for (Map<String, Object> map : mListData) {
                        long downloadId = PreferencesUtils.getLong(mContext,
                                ((Integer) map.get("id")).toString());

                        if (downloadId == completeDownloadId) {
                            if (hasDownloaded(downloadId)) {
                                map.put("isDownloaded", true);
                                themesAdapter.notifyDataSetChanged();
                            }
                            break;
                        }
                    }
            } else {
                int id = intent.getIntExtra("id", -1);
                for (Map<String, Object> map : mListData) {
                    if ((Integer) map.get("id") == id) {
                        map.put("isDownloaded", false);
                        themesAdapter.notifyDataSetChanged();
                        break;
                    }
                }

            }
        }
    }

    private boolean hasDownloaded(long downloadId) {
        int status = mDownloadHelp.getStatusById(downloadId);
        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDestroy() {
        mAsyncImageCache.stop();
        unregisterReceiver(completeReceiver);
        unregisterReceiver(deletePackageReceiver);
        super.onDestroy();
    }

}
