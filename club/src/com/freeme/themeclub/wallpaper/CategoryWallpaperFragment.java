package com.freeme.themeclub.wallpaper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import com.freeme.themeclub.BackScrollFragment;
import com.freeme.themeclub.R;
import com.freeme.themeclub.statisticsdata.LocalUtil;
import com.freeme.themeclub.statisticsdata.db.StatisticDBHelper;
import com.freeme.themeclub.theme.onlinetheme.CategoryThemesFragment.GetCategoryData;
import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;
import com.freeme.themeclub.theme.onlinetheme.util.NetworkUtil;
import com.freeme.themeclub.theme.onlinetheme.util.OnlineThemesUtils;
import com.freeme.themeclub.theme.onlinetheme.util.ResultUtil;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;

public class CategoryWallpaperFragment extends BackScrollFragment{

    public static String MSGCODE="onlineWallpaper";
    public ArrayList<Map<String, Object>> mListData;
    CategoryWallpaperAdapter adapter;
    private GridView gridView;
    private LinearLayout mSearch_loading;
    public  LinearLayout mRefresh_linearLayout_id;
    private boolean fresh;

    private StatisticDBHelper mStatisticDBHelper;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListData = new ArrayList<Map<String, Object>>();
        mStatisticDBHelper=StatisticDBHelper.getInstance(getActivity());

    }

    public void loadData(){
        if(!fresh){
            fresh=true;
            new GetCategoryData().execute();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View contentView=inflater.inflate(R.layout.fragment_category, null);
        findViews(contentView);
        gridView=(GridView) contentView.findViewById(R.id.gridView_category);
        adapter=new CategoryWallpaperAdapter(getActivity(),mListData);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                    long arg3) {
                Intent intent=new Intent(getActivity(),CategoryWallpaperActivity.class);
                intent.putExtra(MSGCODE, MessageCode.GET_WALLPAPER_LIST_BY_TAG_REQ);
                intent.putExtra("title", mListData.get(position).get("name")+"");
                intent.putExtra("style", mListData.get(position).get("code")+"");

                String name =mListData.get(position).get("name").toString();
                String infoStr =LocalUtil.saveStatisticInfo(
                        LocalUtil.CLICK_ACTION_ID, 
                        LocalUtil.WALL_CLICK_CATEGORY, 
                        name,
                        System.currentTimeMillis());
                mStatisticDBHelper.intserStatisticdataToDB(infoStr);

                startActivity(intent);
            }
        });
        return contentView;
    }

    private void findViews(View v) {
        mSearch_loading = (LinearLayout) v.findViewById(R.id.search_loading);
        mRefresh_linearLayout_id = (LinearLayout) v
                .findViewById(R.id.refresh_linearLayout_id);
        v.findViewById(R.id.set_wlan).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(Settings.ACTION_WIFI_SETTINGS);  
                startActivity(intent);
            }
        });
        v.findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mRefresh_linearLayout_id.setVisibility(View.GONE);
                mSearch_loading.setVisibility(View.VISIBLE);
                new GetCategoryData().execute();
            }
        });
    }

    public class GetCategoryData extends AsyncTask<Object, Object, List<Map<String, Object>>> {
        protected List<Map<String, Object>> doInBackground(Object... params) {
            String result = null;

            List<Map<String, Object>> list = null;

            if (NetworkUtil.getAPNType(getActivity()) == -1){
                result = OnlineThemesUtils.getListViewData("WallpaperCategory.cfg");
            }else{
                try{
                    int msgCode =MessageCode.GET_CATEGORY_LIST_BY_TAG_REQ;

                    JSONObject paraInfo = new JSONObject();
                    paraInfo.put("type", "03");
                    JSONObject jsObject = new JSONObject();
                    jsObject.put("head", NetworkUtil.buildHeadData(msgCode));
                    jsObject.put("body", paraInfo.toString());
                    String contents = jsObject.toString();
                    String url = MessageCode.SERVER_URL;
                    result = NetworkUtil.accessNetworkByPost(url, contents);
                    Log.w("yzywallpaper", result);
                }catch (Exception e) {
                    e.printStackTrace();
                }
                OnlineThemesUtils.saveListViewData(result,
                        "WallpaperCategory.cfg");
            }
            list = ResultUtil.splitCategoryServerListData(result,true);
            if(list != null){
                for (int i = 0; i < list.size(); i++) {  
                    list.get(i).put("isDownloaded", OnlineThemesUtils.checkInstalled(getActivity(), (String)list.get(i).get("packageName")));
                }
            }
            return list;
        }

        protected void onPostExecute(List<Map<String, Object>> list) {
            if (list != null) {
                mSearch_loading.setVisibility(View.GONE);

                mRefresh_linearLayout_id.setVisibility(View.GONE);

                if (gridView.getVisibility() == View.GONE)
                    gridView.setVisibility(View.VISIBLE);

                for (int i = 0; i < list.size(); i++) {  
                    if((Integer)list.get(i).get("beautifyShow")==1){
                        mListData.add(list.get(i));
                    }

                }
                adapter.notifyDataSetChanged();
            } else{
                mSearch_loading.setVisibility(View.GONE);

                if (mListData.size() == 0) {
                    mRefresh_linearLayout_id.setVisibility(View.VISIBLE);
                    gridView.setVisibility(View.GONE);
                } else {
                    mRefresh_linearLayout_id.setVisibility(View.GONE);
                }
            }
            super.onPostExecute(list);
        }
    }
    
}
