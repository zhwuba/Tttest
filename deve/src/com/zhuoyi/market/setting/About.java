package com.zhuoyi.market.setting;


import java.util.List;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zhuoyi.market.R;
import com.zhuoyi.market.setting.OurTeamAdapter;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.constant.Constant;
import com.market.featureOption.FeatureOption;
import com.market.view.CommonLoadingManager;

public class About extends Activity implements OnClickListener
{
	private ImageView mLogoImage;
	private TextView mVersionname;
	private TextView mAppName;
	private String mVerNameString="";
	private String mAppNameString="";
	private int mVerCode=0;
    private LinearLayout mCopyRightLayout;
    private String mSystemDisplay;
    private int mCount = 0;
    private String mVersionString = "";
    
    private ListView mListView = null;
    private OurTeamAdapter mOurTeamAdapter = null;
    private List<String> mList = null;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_about);	
		 
		mVersionname = (TextView)findViewById(R.id.versionname);
		mSystemDisplay = android.os.Build.MANUFACTURER;
		mLogoImage = (ImageView)findViewById(R.id.zhuoyi_image);
		mLogoImage.setOnClickListener(this);
//		mCopyRightLayout = (LinearLayout)findViewById(R.id.copyright_layout);
//		
//		if(!mSystemDisplay.contains("koobee")) //21 floor
//			mCopyRightLayout.setVisibility(View.GONE);
		
		mAppName = (TextView)findViewById(R.id.app_name);	

		setAppVersionInfo();
		
//		mAppName.setText(mAppNameString); 
		
		if(mVerNameString!=null&&!TextUtils.isEmpty(mVerNameString))
		{
		    mVersionString = "V"+mVerNameString+"_"+mVerCode;
		    if (FeatureOption.RELEASE_VERSION) {
		    	mVersionname.setText(mVersionString);
		    } else {
		    	mVersionname.setText(mVersionString + "_test");
		    }
		}
		
		String[] diffName = getResources().getStringArray(R.array.team_member);
		mListView = (ListView) findViewById(R.id.team_list);
		mOurTeamAdapter = new OurTeamAdapter (this.getApplicationContext(), diffName);
		mListView.setAdapter(mOurTeamAdapter);
		mListView.setSelection(10000);
		
	}
	
	
    @Override
    protected void onResume() {
        
    	CommonLoadingManager.get().showLoadingAnimation(this);
        super.onResume();
    }
	

    @Override 
    public void onClick(View v) { 
    	switch(v.getId()){
    	case R.id.zhuoyi_image:
    	    mCount++;
    	    if(mCount>15 && mCount<17)
    	    {
    	        if(mVerNameString!=null&&!TextUtils.isEmpty(mVerNameString))
    	        {
    	            mVersionString = "V"+mVerNameString+"_"+mVerCode+"_"+Constant.td + "_"+ Constant.CP_ID;
    			    if (FeatureOption.RELEASE_VERSION) {
    			    	mVersionname.setText(mVersionString);
    			    } else {
    			    	mVersionname.setText(mVersionString + "_test");
    			    }
    	        }  
    	    }
    	    break;
    	}
   } 

    /** 
     * 返回当前程序版本名称 
     */  
    public void setAppVersionInfo() {  
        try {  
            // Get the package info  
            PackageInfo pi = getPackageManager().getPackageInfo(this.getPackageName(), 0);//PackageManager.GET_CONFIGURATIONS  
            mVerNameString = pi.versionName;//getString(R.string.version);  
            mAppNameString = pi.applicationInfo.loadLabel(getPackageManager()).toString();
            mVerCode = pi.versionCode;
        } catch (Exception e) {  
        	Log.e("VersionInfo", "Exception", e); 
        }  
    }
}
