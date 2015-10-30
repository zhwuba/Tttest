package com.freeme.themeclub.wallpaper;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import java.util.ArrayList;

import com.freeme.themeclub.R;
import com.freeme.themeclub.wallpaper.base.BasePreferenceActivity;
import com.freeme.themeclub.wallpaper.base.IntentConstants;

public class ThirdPartyPickersActivity extends BasePreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final Intent intent = getIntent();
        
        final Parcelable parcelIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
        final int resourceType = intent.getIntExtra(IntentConstants.EXTRA_RESOURCE_FLAG, -1);
        final ArrayList<ResolveInfo> resolves = 
        		intent.getParcelableArrayListExtra(IntentConstants.EXTRA_RESOLVE_INFO_LIST);
        
        if (!(parcelIntent instanceof Intent) 
        		|| resourceType == -1
        		|| resolves == null || resolves.isEmpty()) {
            finish();
        } else {
            final PackageManager pkgmgr = getPackageManager();
            final PreferenceManager preferMgr = getPreferenceManager();
            
            PreferenceScreen content = preferMgr.createPreferenceScreen(this);
            Object[] args = { 
            		getString(ResourceHelper.getTitleIdByFlag(resourceType)) };
            content.setTitle(getString(R.string.third_party_pickers_activity_title, args));
            
            final Intent targetIntent = (Intent) parcelIntent;
            final int size = resolves.size();
            for (int i = 0; i < size; i++) {
                ResolveInfo ri = resolves.get(i);
                
                Intent childIntent = new Intent(targetIntent);
                childIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP 
                					| Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                
                ActivityInfo ai = ri.activityInfo;
                childIntent.setComponent(new ComponentName(ai.packageName, ai.name));
                
                PreferenceScreen childPrefScn = preferMgr.createPreferenceScreen(this);
                childPrefScn.setIntent(childIntent);
                childPrefScn.setTitle(ri.loadLabel(pkgmgr));
                childPrefScn.setIcon(ri.loadIcon(pkgmgr));
                
                // add child
                content.addPreference(childPrefScn);
            }

            setPreferenceScreen(content);
        }
    }

	@Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.android_slide_out_down);
    }
	
	@Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        startActivity(preference.getIntent());
        finish();
        return true;
    }
}