package com.freeme.themeclub.wallpaper.base;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
//import android.preference.PreferenceActivity;
import android.view.MenuItem;


public class BasePreferenceActivity extends PreferenceActivity {

	@Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setHomeButtonEnabled(false);
        }
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home: 
        	finish();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}
