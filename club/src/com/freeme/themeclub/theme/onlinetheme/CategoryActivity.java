package com.freeme.themeclub.theme.onlinetheme;

import com.freeme.themeclub.R;
import com.freeme.themeclub.lockscreen.NewestLockscreenFragment;
import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

public class CategoryActivity extends Activity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_category, null);
        setContentView(view);
        Intent intent=getIntent();
        setTitle(intent.getStringExtra("title"));
        getActionBar().setDisplayHomeAsUpEnabled(true);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        boolean isOnlineTheme=intent.getBooleanExtra(CategoryThemesFragment.IS_ONLINE_THEMES, false);
        CategoryThemesListFragment fragment;
        fragment = new CategoryThemesListFragment();
        fragment.serialNum=4;
        fragment.isOnlineLockscreens=!isOnlineTheme;
        fragment.isOnlineThemes=isOnlineTheme;
        fragment.subType=intent.getStringExtra("subType");
        if(isOnlineTheme){
            fragment.msgCode=MessageCode.GET_THEME_LIST_BY_TAG_REQ;
        }else{
            fragment.msgCode=MessageCode.GET_LOCKSCREEN_LIST_BY_TAG_REQ;
        }
        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            break;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
        case 10000:
            Log.w("yzy", "categoryactivity");
            setResult(10000);
            finish();
            break;

        default:
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
