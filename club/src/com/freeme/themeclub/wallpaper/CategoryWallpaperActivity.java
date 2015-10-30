package com.freeme.themeclub.wallpaper;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.freeme.themeclub.R;

public class CategoryWallpaperActivity extends Activity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_category_wallpaper, null);
        view.findViewById(R.id.controls).setVisibility(View.GONE);
        view.findViewById(R.id.contact_background_sizer).setVisibility(View.GONE);
        setContentView(view);
        Intent intent=getIntent();
        setTitle(intent.getStringExtra("title"));
        getActionBar().setDisplayHomeAsUpEnabled(true);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        NewestWallpaperFragment fragment=new NewestWallpaperFragment();
        fragment.setNoMask(true);
        fragment.style=intent.getStringExtra("style");
        fragment.categoryContext = this;
        fragmentTransaction.add(R.id.fragment_container, fragment);
        
        fragmentTransaction.commit();
        fragment.categoryLoadData();
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
}
