package com.zhuoyi.market.topic;

import com.market.view.CommonLoadingManager;
import com.zhuoyi.market.R;
import com.zhuoyi.market.R.id;
import com.zhuoyi.market.R.layout;
import com.zhuoyi.market.utils.MarketUtils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

public class TopicWebInfoActivity extends Activity {
	
	private WebView mWebView;
	private RelativeLayout mErrorPage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_topic_web_info);
		mErrorPage = (RelativeLayout) findViewById(R.id.url_error);
		mWebView = (WebView)findViewById(R.id.topic_web_info);
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				view.loadUrl(url);
				return true;
//				return super.shouldOverrideUrlLoading(view, url);
			}
			
		});
		String url = getIntent().getStringExtra("webUrl");
		if(url != null) {
			mWebView.loadUrl(url);
		} else {
			mErrorPage.setVisibility(View.VISIBLE);
		}
	}
	
	
    @Override
    protected void onResume() {
        
    	CommonLoadingManager.get().showLoadingAnimation(this);
        super.onResume();
    }
	
	

}
