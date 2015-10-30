//package com.freeme.themeclub.lockscreen;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import org.json.JSONObject;
//
//import com.freeme.themeclub.BackScrollFragment;
//import com.freeme.themeclub.BackScrollManager;
//import com.freeme.themeclub.R;
//import com.freeme.themeclub.BackScrollManager.ScrollableHeader;
//import com.freeme.themeclub.homepage.HomepageFragment.GetAdData;
//import com.freeme.themeclub.homepage.HomepageFragment.GetOnlineThemeData;
//import com.freeme.themeclub.statisticsdata.LocalUtil;
//import com.freeme.themeclub.statisticsdata.db.StatisticDBHelper;
//import com.freeme.themeclub.theme.ThemeFragment;
//import com.freeme.themeclub.theme.onlinetheme.CategoryActivity;
//import com.freeme.themeclub.theme.onlinetheme.CategoryThemesAdapter;
//import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;
//import com.freeme.themeclub.theme.onlinetheme.util.NetworkUtil;
//import com.freeme.themeclub.theme.onlinetheme.util.OnlineThemesUtils;
//import com.freeme.themeclub.theme.onlinetheme.util.PreferencesUtils;
//import com.freeme.themeclub.theme.onlinetheme.util.ResultUtil;
//
//import android.app.Fragment;
//import android.app.FragmentManager;
//import android.content.Context;
//import android.content.Intent;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.provider.Settings;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AbsListView;
//import android.widget.AdapterView;
//import android.widget.GridView;
//import android.widget.LinearLayout;
//import android.widget.AdapterView.OnItemClickListener;
//
//public class CategoryLockscreenFragment extends BackScrollFragment{
//
//    public static String IS_ONLINE_THEMES="isOnlineThemes";
//    public static String IS_ONLINE_LOCKSCREEN="isOnlineLockScreen";
//    public static String MSGCODE="messageCode";
//
//    public View view;
//    public ArrayList<Map<String, Object>> mListData;
//    CategoryThemesAdapter adapter;
//    GridView gridView;
//    private LinearLayout mSearch_loading;
//    public  LinearLayout mRefresh_linearLayout_id;
//    private boolean fresh;
//    
//    private StatisticDBHelper mStatisticDBHelper;
//    
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mListData = new ArrayList<Map<String, Object>>();
//        mStatisticDBHelper=StatisticDBHelper.getInstance(getActivity());
//        
//    }
//    
//    public void loadData(){
//        if(!fresh){
//            fresh=true;
//            new GetCategoryData().execute();
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//            Bundle savedInstanceState) {
//        View contentView=inflater.inflate(R.layout.fragment_category, null);
//        findViews(contentView);
//        gridView=(GridView) contentView.findViewById(R.id.gridView_category);
//        adapter=new CategoryThemesAdapter(getActivity(),mListData);
//        gridView.setAdapter(adapter);
//        gridView.setOnItemClickListener(new OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
//                    long arg3) {
//                if(position<2){
//                    return ;
//                }
//                Intent intent=new Intent(getActivity(),CategoryActivity.class);
//                intent.putExtra(IS_ONLINE_THEMES, false);
//                intent.putExtra(IS_ONLINE_LOCKSCREEN, true);
//                intent.putExtra(MSGCODE, MessageCode.GET_LOCKSCREEN_LIST_BY_TAG_REQ);
//                intent.putExtra("title", mListData.get(position-2).get("name")+"");
//                intent.putExtra("subType", mListData.get(position-2).get("code")+"");
//                
//                String name =mListData.get(position-2).get("name").toString();
//                String infoStr =LocalUtil.saveStatisticInfo(LocalUtil.CLICK_ACTION_ID, LocalUtil.LOCKS_CLICK_CATEGORY, name,System.currentTimeMillis());
//                mStatisticDBHelper.intserStatisticdataToDB(infoStr);
//                startActivity(intent);
//            }
//        });
//        if(view !=null){
//            initHeaderView();
//        }
//        
//        return contentView;
//    }
//    
//    private void findViews(View v) {
//        mSearch_loading = (LinearLayout) v.findViewById(R.id.search_loading);
//        mRefresh_linearLayout_id = (LinearLayout) v
//                .findViewById(R.id.refresh_linearLayout_id);
//
//        v.findViewById(R.id.set_wlan).setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent intent =  new Intent(Settings.ACTION_WIFI_SETTINGS);  
//                startActivity(intent);
//            }
//        });
//        v.findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                mRefresh_linearLayout_id.setVisibility(View.GONE);
//                mSearch_loading.setVisibility(View.VISIBLE);
//                new GetCategoryData().execute();
//            }
//        });
//    }
//    
//    private void initHeaderView(){
//        BackScrollManager.bind(
//                new ScrollableHeader() {
//                    private View mControls = view.findViewById(R.id.controls);
//                    private View mPhoto = view.findViewById(R.id.contact_background_sizer);
//                    private View mHeader = view.findViewById(R.id.photo_text_bar);
//
//                    @Override
//                    public void setOffset(int offset) {
//                        mControls.setY(-offset);
//                        mOffset_lockscreen=offset;
//                    }
//                    
//                    @Override
//                    public int getOffsetNotChange(){
//                        return mPhoto.getHeight();
//                    }
//
//                    @Override
//                    public int getMaximumScrollableHeaderOffset() {
//                        if (mHeader.getVisibility() == View.VISIBLE) {
//                            return mPhoto.getHeight() - mHeader.getHeight();
//                        } else {
//                            return mPhoto.getHeight() + 1;
//                        }
//                    }
//                },
//                gridView,CategoryLockscreenFragment.this);
//    }
//
//    public void setView(View v) {
//        view=v;
//        if(gridView!=null){
//            initHeaderView();
//        }
//        
//    }
//
//    public class GetCategoryData extends AsyncTask<Object, Object, List<Map<String, Object>>> {
//        protected List<Map<String, Object>> doInBackground(Object... params) {
//            String result = null;
//
//            List<Map<String, Object>> list = null;
//            if (getAPNType(getActivity()) == -1){
//                result = OnlineThemesUtils.getListViewData("LockscreenCategory.cfg");
//            }else{
//                try{
//                    int msgCode =MessageCode.GET_CATEGORY_LIST_BY_TAG_REQ;
//                    JSONObject paraInfo = new JSONObject();
//                    paraInfo.put("type", "02");
//                    JSONObject jsObject = new JSONObject();
//                    jsObject.put("head", NetworkUtil.buildHeadData(msgCode));
//                    jsObject.put("body", paraInfo.toString());
//                    String contents = jsObject.toString();
//                    String url = MessageCode.SERVER_URL;
//                    result = NetworkUtil.accessNetworkByPost(url, contents);
//                }catch (Exception e) {
//                    e.printStackTrace();
//                }
//                OnlineThemesUtils.saveListViewData(result,
//                        "LockscreenCategory.cfg");
//            }
//            list = ResultUtil.splitCategoryServerListData(result,false);
//            if(list != null){
//                for (int i = 0; i < list.size(); i++) {  
//                    list.get(i).put("isDownloaded", OnlineThemesUtils.checkInstalled(getActivity(), (String)list.get(i).get("packageName")));
//                }
//            }
//            return list;
//        }
//
//        protected void onPostExecute(List<Map<String, Object>> list) {
//            if (list != null) {
//                mSearch_loading.setVisibility(View.GONE);
//
//                mRefresh_linearLayout_id.setVisibility(View.GONE);
//                if(view!=null){
//                    view.findViewById(R.id.controls).setVisibility(View.VISIBLE);
//                }
//
//                if (gridView.getVisibility() == View.GONE)
//                    gridView.setVisibility(View.VISIBLE);
//
//                for (int i = 0; i < list.size(); i++) {                 
//                    mListData.add(list.get(i));
//                }
//                adapter.notifyDataSetChanged();
//            }  else{
//                mSearch_loading.setVisibility(View.GONE);
//
//                if (mListData.size() == 0) {
//                    mRefresh_linearLayout_id.setVisibility(View.VISIBLE);
//                    gridView.setVisibility(View.GONE);
//                } else {
//                    mRefresh_linearLayout_id.setVisibility(View.GONE);
//                }
//            }
//            super.onPostExecute(list);
//        }
//    }
//
//    public void scrollGridView(int position){
//        if(gridView!=null){
//            gridView.scrollListBy( mOffset_lockscreen -map_lockscreen.get(3));
//            map_lockscreen.put(3, mOffset_lockscreen);
//        }
//        
//    }
//
//    @Override
//    public void onScrollStateChanged(AbsListView arg0, int arg1) {
//        map_lockscreen.put(3, mOffset_lockscreen);
//    }
//
//    public int getAPNType(Context context) {
//        int netType = -1;
//        if(context==null){
//            return netType;
//        }
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
//}