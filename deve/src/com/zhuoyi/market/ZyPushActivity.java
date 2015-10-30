package com.zhuoyi.market;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import com.market.account.login.BaseHtmlActivity;
import com.zhuoyi.market.constant.Constant;

public class ZyPushActivity extends BaseHtmlActivity {
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constant.initMarketUrl(getApplicationContext());
    }
    
    
    @Override
    public void onBackPressed() {
        if (mWebView != null) {
        	mWebView.loadUrl("javascript:if(typeof(zhuoyou_login_webview_back)=='function') {zhuoyou_login_webview_back();}else{zhuoyou_login.zhuoyou_login_close();}");
            mIsLoadBackUrl = true;
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (!exit_flag && mWebView != null) {
                        if (mWebView.canGoBack()) {
                        	mWebView.goBack();
                        } else {
                            startSplash();
                            finish();
                        }
                    }else {
                    }
                    mIsLoadBackUrl = false;
                }
            }, 200);
        } else {
            startSplash();
            finish();
        }
    }
    
    private void startSplash() {
        Intent intent = new Intent(this, Splash.class);
        SharedPreferences settings = getSharedPreferences(Splash.PREFS_NAME, 0);
        Editor editor = settings.edit();
        editor.putBoolean(Splash.FIRST_RUN, false);
        editor.commit();
        if(Splash.getHandler() == null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("showLoadingUI", false);
            startActivity(intent);
        }
    }
}
