package com.freeme.themeclub.wallpaper.base;

import com.freeme.themeclub.wallpaper.ResourceHelper;
import com.freeme.themeclub.wallpaper.resource.ResourceSet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public abstract class ResourceDetailActivity extends Activity {
	
	protected Bundle mMetaData = null;
	
	protected ResourceSet mResourceSet = null;
    protected String mResourceSetPackage = null;
    protected String mResourceSetSubpackage = null;
    
    protected int mResourceGroup = 0;
    protected int mResourceIndex = 0;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
        if (intent != null) {
            mMetaData = intent.getBundleExtra(IntentConstants.EXTRA_META_DATA);
        }
        
        if (mMetaData == null) {
            mMetaData = buildDefaultMetaData(new Bundle(), intent.getAction());
            intent.putExtra(IntentConstants.EXTRA_META_DATA, mMetaData);
        }
        
        pickMetaData(mMetaData);
        
        mResourceGroup = mMetaData.getInt(IntentConstants.EXTRA_RESOURCE_GROUP);
        mResourceIndex = mMetaData.getInt(IntentConstants.EXTRA_RESOURCE_INDEX);
	}

	protected void pickMetaData(Bundle metaData) {
        mResourceSetPackage = mMetaData.getString(IntentConstants.EXTRA_RESOURCE_SET_PACKAGE);
        mResourceSetSubpackage = mMetaData.getString(IntentConstants.EXTRA_RESOURCE_SET_SUBPACKAGE);
        
        if (mResourceSetSubpackage == null) {
            mResourceSetSubpackage = ".single";
        }
        
        mResourceSet = ResourceSet.getInstance(mResourceSetPackage + mResourceSetSubpackage);
    }
	
	protected Bundle buildDefaultMetaData(Bundle metaData, String action) {
        return ResourceHelper.buildDefaultMetaData(metaData, action, this);
        /*/ XXX TEST
		DetailTest.initMetaData(metaData);
		DetailTest.initResourceSet();
		return metaData;
		//*/
    }
}
