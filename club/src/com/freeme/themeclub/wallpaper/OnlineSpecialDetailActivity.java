package com.freeme.themeclub.wallpaper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.util.AsyncImageCache;
import com.freeme.themeclub.R;
import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;
import com.freeme.themeclub.wallpaper.base.IntentConstants;
import com.freeme.themeclub.wallpaper.util.NetworkUtil;
import com.freeme.themeclub.wallpaper.util.OnlineUtils;

public class OnlineSpecialDetailActivity extends Activity implements OnItemClickListener
                ,OnTouchListener{
	private static String TAG = "OnlineSpecialDetailActivity";
	
    private GridView specialGV;
    private View mLoadingLayout;   
    private ProgressBar specialFootBar;
    private View dataLayout;
    
    private String topicId;
    private int flag;
    private String specialName;
    private List<Map<String, Object>> specialList;
    private AsyncImageCache mAsyncImageCache;

    private final String TopicListData = "TopicList";
    private int startNum = 0;
    private final int pageNumber = 9;
    private int pageIndex = 0;
    
    private boolean mGetListDataFail;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallpaper_online_special_detail);
        mAsyncImageCache = AsyncImageCache.from(this);
        Intent intent = getIntent();
        initView(intent);
        specialList = new ArrayList<Map<String,Object>>();
        new getSpecialList().execute();
    }

    @Override
    protected void onDestroy() {
        mAsyncImageCache.stop();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        BaseAdapter specialAdapter = (BaseAdapter)specialGV.getAdapter();
        if(specialAdapter != null)
            specialAdapter.notifyDataSetChanged();
        super.onResume();
    }
    
    private void initView(Intent intent) {
        ((TextView) findViewById(R.id.special_summary)).setText("        "+intent
                .getStringExtra("summary"));
        flag = intent.getIntExtra(IntentConstants.EXTRA_RESOURCE_FLAG, -1);
        setTitle(intent.getStringExtra("name"));
        //mTitlebarTitle.setText(intent.getStringExtra("name"));
        topicId = intent.getStringExtra("topicId");
        mLoadingLayout = findViewById(R.id.special_loading_layout);
        specialGV = (GridView) findViewById(R.id.special_thumb_gv);
        specialGV.setOnItemClickListener(this);
        specialFootBar = (ProgressBar)findViewById(R.id.special_loading_bar);
        dataLayout = findViewById(R.id.special_detail_view);
        dataLayout.setOnTouchListener(this);
    }

    private class getSpecialList extends AsyncTask<Object, Object, String> {
        @Override
        protected String doInBackground(Object... arg0) {
            String result = null;
            if (OnlineUtils.getNetWorkType(OnlineSpecialDetailActivity.this) == -1 && pageIndex == 0) {
                result = OnlineUtils.getStringData(TopicListData + topicId
                        + ".cfg");
            } else {
            	try{
	            	int msgCode = 102001;
	                JSONObject paraInfo = new JSONObject();
	                paraInfo.put("mf", "koobee");
	                paraInfo.put("lcd", OnlineUtils.getAvailableResolutionForThisDevice(OnlineSpecialDetailActivity.this, getResources().getStringArray(R.array.resolution_array))[0]);
	                paraInfo.put("topic", topicId);
	                paraInfo.put("sort", "01");
	                paraInfo.put("isBroad", "0");
	                paraInfo.put("from", String.valueOf(startNum));
	                paraInfo.put("to", ""+String.valueOf(startNum+pageNumber));
	                JSONObject jsObject = new JSONObject();
	                jsObject.put("head", NetworkUtil.buildHeadData(msgCode));
	                jsObject.put("body", paraInfo.toString());
	                String contents = jsObject.toString();
	                String url = MessageCode.SERVER_URL;
	                result = NetworkUtil.accessNetworkByPost(url, contents);
            	}catch (Exception e) {
					e.printStackTrace();
				}
            	if(pageIndex == 0){
	            	OnlineUtils.saveStringData(result, TopicListData + topicId
	                        + ".cfg");
            	}
            }
            Log.v(TAG, "specialresult:"+result);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
        	List<Map<String, Object>> list = OnlineUtils.splitThumbListJSON(result);
            if(list != null){
            	mGetListDataFail = false;
            	for(Map map:list){
            		specialList.add(map);
            	}
            	if(list.size()==0 || list.size() < pageNumber){
            		dataLayout.setOnTouchListener(null);
            	}
            	specialGV.setAdapter(new OnlineUtils.WallpaperThumbAdapter(
	                    OnlineSpecialDetailActivity.this, specialList,
	                    mAsyncImageCache));
            }else{
            	mGetListDataFail = true;
            }
            mLoadingLayout.setVisibility(View.GONE);
            specialFootBar.setVisibility(View.GONE);
            specialGV.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View item, int position, long arg3) {
        Intent intent = new Intent(this, OnlineWallpaperDetailActivity.class);
        if(noData())
            return;
        intent.putExtra(OnlineUtils.MyClickPosition, position);
        intent.putExtra(IntentConstants.EXTRA_RESOURCE_FLAG, flag);
        intent.putExtra(OnlineUtils.MyListData, (ArrayList<Map<String, Object>>)specialList);
        startActivity(intent);
    }
    private boolean noData(){
        return specialList == null || specialList.size() == 0;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            break;
        case MotionEvent.ACTION_MOVE:
            break;
        case MotionEvent.ACTION_UP:
            if (OnlineUtils.scrollButtom(view, specialFootBar)) {
                specialFootBar.setVisibility(View.VISIBLE);
                if(!mGetListDataFail){
	                pageIndex++;
	                startNum = pageIndex*pageNumber;
                }
                new getSpecialList().execute();
            }
            break;
        default:
            break;
        }
        return false;
    }
}
