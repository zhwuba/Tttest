package com.freeme.themeclub.theme.onlinetheme;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.freeme.themeclub.BackScrollFragment;
import com.freeme.themeclub.R;
import com.freeme.themeclub.statisticsdata.LocalUtil;
import com.freeme.themeclub.statisticsdata.db.StatisticDBHelper;
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
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class CategoryThemesFragment extends BackScrollFragment{

    public static String IS_ONLINE_THEMES="isOnlineThemes";
    public static String IS_ONLINE_LOCKSCREEN="isOnlineLockScreen";
    public static String MSGCODE="messageCode";
    public ArrayList<Map<String, Object>> mListData;
    CategoryThemesAdapter adapter;
    GridView gridView;
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
        adapter=new CategoryThemesAdapter(getActivity(),mListData);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position,
                    long id) {

                Intent intent=new Intent(getActivity(),CategoryActivity.class);
                intent.putExtra(IS_ONLINE_THEMES, true);
                intent.putExtra(IS_ONLINE_LOCKSCREEN, false);
                intent.putExtra(MSGCODE, MessageCode.GET_THEME_LIST_BY_TAG_REQ);
                intent.putExtra("title", mListData.get(position).get("name")+"");
                intent.putExtra("subType", mListData.get(position).get("code")+"");
                
                String name =mListData.get(position).get("name").toString();
                String infoStr =LocalUtil.saveStatisticInfo(LocalUtil.CLICK_ACTION_ID, 
                        LocalUtil.THEME_CLICK_CATEGORY, name,System.currentTimeMillis());
                mStatisticDBHelper.intserStatisticdataToDB(infoStr);
                
                startActivityForResult(intent, 10000);
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
                result = OnlineThemesUtils.getListViewData("ThemeCategory.cfg");
            }else{
                try{
                    int msgCode = MessageCode.GET_CATEGORY_LIST_BY_TAG_REQ;
                    JSONObject paraInfo = new JSONObject();
                    paraInfo.put("type", "01");
                    JSONObject jsObject = new JSONObject();
                    jsObject.put("head", NetworkUtil.buildHeadData(msgCode));
                    jsObject.put("body", paraInfo.toString());
                    String contents = jsObject.toString();
                    String url = MessageCode.SERVER_URL;
                    result = NetworkUtil.accessNetworkByPost(url, contents);
                    Log.w("yzy", "themesresult = "+result);
                }catch (Exception e) {
                    e.printStackTrace();
                }
                OnlineThemesUtils.saveListViewData(result,
                        "ThemeCategory.cfg");
            }
            
            list = ResultUtil.splitCategoryServerListData(result,false);
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
                    mListData.add(list.get(i));
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
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
        case 10000:
            getActivity().finish();
            break;

        default:
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}