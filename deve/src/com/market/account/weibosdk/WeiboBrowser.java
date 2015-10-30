package com.market.account.weibosdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sina.weibo.sdk.component.AuthRequestParam;
import com.sina.weibo.sdk.component.BrowserRequestParamBase;
import com.sina.weibo.sdk.component.WeiboCallbackManager;
import com.sina.weibo.sdk.component.view.LoadingBar;
import com.sina.weibo.sdk.utils.LogUtil;
import com.sina.weibo.sdk.utils.NetworkHelper;
import com.sina.weibo.sdk.utils.ResourceManager;

public class WeiboBrowser extends Activity
  implements BrowserRequestCallBack
{
  private static final String TAG = WeiboBrowser.class.getName();
  public static final String BROWSER_CLOSE_SCHEME = "sinaweibo://browser/close";
  public static final String BROWSER_WIDGET_SCHEME = "sinaweibo://browser/datatransfer";
  private String mSpecifyTitle;
  private String mHtmlTitle;
  private boolean isLoading;
  private String mUrl;
  private boolean isErrorPage;
  private TextView mLeftBtn;
  private TextView mTitleText;
  private WebView mWebView;
  private LoadingBar mLoadingBar;
  private LinearLayout mLoadErrorView;
  private Button mLoadErrorRetryBtn;
  private BrowserRequestParamBase mRequestParam;
  private WeiboWebViewClient mWeiboWebViewClient;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!initDataFromIntent(getIntent())) {
            finish();
            return;
        }

        setContentView();
        initWebView();
        openUrl(this.mUrl);
    }

  private boolean initDataFromIntent(Intent data)
  {
    this.mRequestParam = createBrowserRequestParam(data.getExtras());
    if (this.mRequestParam == null) {
      return false;
    }

    this.mUrl = this.mRequestParam.getUrl();
    if (TextUtils.isEmpty(this.mUrl)) {
      return false;
    }
    LogUtil.d(TAG, "LOAD URL : " + this.mUrl);

    this.mSpecifyTitle = this.mRequestParam.getSpecifyTitle();

    return true;
  }

  private void openUrl(String url) {
    this.mWebView.loadUrl(url);
  }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
//            mWebView.goBack();
//            return true;
//        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                this.mRequestParam.execRequest(this, 3);
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    /*public boolean onKeyUp(int keyCode, KeyEvent event)
    {
      if (keyCode == 4) {
        this.mRequestParam.execRequest(this, 
          3);
        finish();
        return true;
      }
      return super.onKeyUp(keyCode, event);
    }*/
  
  @SuppressLint({"SetJavaScriptEnabled"})
  private void initWebView()
  {
    this.mWebView.getSettings().setJavaScriptEnabled(true);

    this.mWebView.getSettings().setSavePassword(false);
    this.mWebView.setWebViewClient(this.mWeiboWebViewClient);
    this.mWebView.setWebChromeClient(new WeiboChromeClient());
    this.mWebView.requestFocus();
    this.mWebView.setScrollBarStyle(0);
  }

  private void setTopNavTitle() {
    this.mTitleText.setText(this.mSpecifyTitle);
    this.mLeftBtn.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View v) {
          WeiboBrowser.this.mRequestParam.execRequest(WeiboBrowser.this, 
          3);
          WeiboBrowser.this.finish();
      }
    });
  }

  private void updateTitleName()
  {
    String showTitle = "";

    if (!TextUtils.isEmpty(this.mHtmlTitle))
      showTitle = this.mHtmlTitle;
    else if (!TextUtils.isEmpty(this.mSpecifyTitle)) {
      showTitle = this.mSpecifyTitle;
    }

    this.mTitleText.setText(showTitle);
  }

  private void setContentView() {

    RelativeLayout contentLy = new RelativeLayout(this);
    contentLy.setLayoutParams(new ViewGroup.LayoutParams(
      -1, 
      -1));
    contentLy.setBackgroundColor(-1);

    LinearLayout titleBarLy = new LinearLayout(this);
    titleBarLy.setId(1);
    titleBarLy.setOrientation(1);
    titleBarLy.setLayoutParams(
      new ViewGroup.LayoutParams(
      -1, 
      -2));

    RelativeLayout titleBar = new RelativeLayout(this);
    titleBar.setLayoutParams(new ViewGroup.LayoutParams(
      -1, 
      ResourceManager.dp2px(this, 45)));
    titleBar.setBackgroundDrawable(ResourceManager.getNinePatchDrawable(
      this, "weibosdk_navigationbar_background.9.png"));

    this.mLeftBtn = new TextView(this);
    this.mLeftBtn.setClickable(true);
    this.mLeftBtn.setTextSize(2, 17.0F);
    this.mLeftBtn.setTextColor(ResourceManager.createColorStateList(-32256, 1728020992));
    this.mLeftBtn.setText(ResourceManager.getString(this, "Close", "关闭", "关闭"));
    RelativeLayout.LayoutParams leftBtnLp = new RelativeLayout.LayoutParams(
      -2, 
      -2);
    leftBtnLp.addRule(5);
    leftBtnLp.addRule(15);
    leftBtnLp.leftMargin = ResourceManager.dp2px(this, 10);
    leftBtnLp.rightMargin = ResourceManager.dp2px(this, 10);
    this.mLeftBtn.setLayoutParams(leftBtnLp);
    titleBar.addView(this.mLeftBtn);

    this.mTitleText = new TextView(this);
    this.mTitleText.setTextSize(2, 18.0F);
    this.mTitleText.setTextColor(-11382190);
    this.mTitleText.setEllipsize(TextUtils.TruncateAt.END);
    this.mTitleText.setSingleLine(true);
    this.mTitleText.setGravity(17);
    this.mTitleText.setMaxWidth(ResourceManager.dp2px(this, 160));
    RelativeLayout.LayoutParams titleTextLy = new RelativeLayout.LayoutParams(
      -2, 
      -2);
    titleTextLy.addRule(13);
    this.mTitleText.setLayoutParams(titleTextLy);
    titleBar.addView(this.mTitleText);

    TextView shadowBar = new TextView(this);
    shadowBar.setLayoutParams(new LinearLayout.LayoutParams(
      -1, ResourceManager.dp2px(this, 2)));
    shadowBar.setBackgroundDrawable(
      ResourceManager.getNinePatchDrawable(this, "weibosdk_common_shadow_top.9.png"));

    this.mLoadingBar = new LoadingBar(this);
    this.mLoadingBar.setBackgroundColor(0);
    this.mLoadingBar.drawProgress(0);
    LinearLayout.LayoutParams loadingBarLy = new LinearLayout.LayoutParams(
      -1, ResourceManager.dp2px(this, 3));
    this.mLoadingBar.setLayoutParams(loadingBarLy);

    titleBarLy.addView(titleBar);
    titleBarLy.addView(shadowBar);
    titleBarLy.addView(this.mLoadingBar);

    this.mWebView = new WebView(this);
    this.mWebView.setBackgroundColor(-1);
    RelativeLayout.LayoutParams webViewLp = new RelativeLayout.LayoutParams(
      -1, 
      -1);
    webViewLp.addRule(3, 1);
    this.mWebView.setLayoutParams(webViewLp);

    this.mLoadErrorView = new LinearLayout(this);
    this.mLoadErrorView.setVisibility(8);
    this.mLoadErrorView.setOrientation(1);
    this.mLoadErrorView.setGravity(17);
    RelativeLayout.LayoutParams mLoadErrorViewLp = new RelativeLayout.LayoutParams(
      -1, 
      -1);
    mLoadErrorViewLp.addRule(3, 1);
    this.mLoadErrorView.setLayoutParams(mLoadErrorViewLp);

    ImageView loadErrorImg = new ImageView(this);
    loadErrorImg.setImageDrawable(ResourceManager.getDrawable(this, "weibosdk_empty_failed.png"));
    LinearLayout.LayoutParams loadErrorImgLp = new LinearLayout.LayoutParams(
      -2, 
      -2);
    loadErrorImgLp.leftMargin = (loadErrorImgLp.topMargin = 
      loadErrorImgLp.rightMargin = 
      loadErrorImgLp.bottomMargin = 
      ResourceManager.dp2px(this, 8));
    loadErrorImg.setLayoutParams(loadErrorImgLp);
    this.mLoadErrorView.addView(loadErrorImg);

    TextView loadErrorContent = new TextView(this);
    loadErrorContent.setGravity(1);
    loadErrorContent.setTextColor(-4342339);
    loadErrorContent.setTextSize(2, 14.0F);
    loadErrorContent.setText(ResourceManager.getString(this, 
      "A network error occurs, please tap the button to reload", 
      "网络出错啦，请点击按钮重新加载", 
      "網路出錯啦，請點擊按鈕重新載入"));
    LinearLayout.LayoutParams loadErrorContentLp = new LinearLayout.LayoutParams(
      -2, 
      -2);
    loadErrorContent.setLayoutParams(loadErrorContentLp);
    this.mLoadErrorView.addView(loadErrorContent);

    this.mLoadErrorRetryBtn = new Button(this);
    this.mLoadErrorRetryBtn.setGravity(17);
    this.mLoadErrorRetryBtn.setTextColor(-8882056);
    this.mLoadErrorRetryBtn.setTextSize(2, 16.0F);
    this.mLoadErrorRetryBtn.setText(ResourceManager.getString(this, 
      "channel_data_error", 
      "重新加载", 
      "重新載入"));
    this.mLoadErrorRetryBtn.setBackgroundDrawable(ResourceManager.createStateListDrawable(this, 
      "weibosdk_common_button_alpha.9.png", 
      "weibosdk_common_button_alpha_highlighted.9.png"));
    LinearLayout.LayoutParams loadErrorRetryBtnLp = new LinearLayout.LayoutParams(
      ResourceManager.dp2px(this, 142), 
      ResourceManager.dp2px(this, 46));
    loadErrorRetryBtnLp.topMargin = ResourceManager.dp2px(this, 10);
    this.mLoadErrorRetryBtn.setLayoutParams(loadErrorRetryBtnLp);
    this.mLoadErrorRetryBtn.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View v) {
          WeiboBrowser.this.openUrl(WeiboBrowser.this.mUrl);
          WeiboBrowser.this.isErrorPage = false;
      }
    });
    this.mLoadErrorView.addView(this.mLoadErrorRetryBtn);

    contentLy.addView(titleBarLy);
    contentLy.addView(this.mWebView);
    contentLy.addView(this.mLoadErrorView);

    setContentView(contentLy);

    setTopNavTitle();
  }

  protected void refreshAllViews() {
    if (this.isLoading)
      setViewLoading();
    else
      setViewNormal();
  }

  private void setViewNormal()
  {
    updateTitleName();
    this.mLoadingBar.setVisibility(8);
  }

  private void setViewLoading()
  {
    this.mTitleText.setText(ResourceManager.getString(this, 
      "Loading....", "加载中....", "載入中...."));
    this.mLoadingBar.setVisibility(0);
  }

  private void handleReceivedError(WebView view, int errorCode, String description, String failingUrl)
  {
    if (!failingUrl.startsWith("sinaweibo")) {
      this.isErrorPage = true;
      promptError();
    }
  }

  private void promptError() {
    this.mLoadErrorView.setVisibility(0);
    this.mWebView.setVisibility(8);
  }

  private void hiddenErrorPrompt() {
    this.mLoadErrorView.setVisibility(8);
    this.mWebView.setVisibility(0);
  }

  private boolean isWeiboCustomScheme(String url)
  {
    if (TextUtils.isEmpty(url)) {
      return false;
    }
    if ("sinaweibo".equalsIgnoreCase(Uri.parse(url).getAuthority())) {
      return true;
    }
    return false;
  }

  protected void onResume()
  {
    super.onResume();
  }

  protected void onDestroy()
  {
    NetworkHelper.clearCookies(this);
    super.onDestroy();
  }


  private BrowserRequestParamBase createBrowserRequestParam(Bundle data) {
    BrowserRequestParamBase result = null;
      AuthRequestParam authRequestParam = new AuthRequestParam(this);
      authRequestParam.setupRequestParam(data);
      installAuthWeiboWebViewClient(authRequestParam);
      result = authRequestParam;
      return result;
  }

  private void installAuthWeiboWebViewClient(AuthRequestParam param) {
    this.mWeiboWebViewClient = new AuthWeiboWebViewClient(this, param);
    this.mWeiboWebViewClient.setBrowserRequestCallBack(this);
  }


  public void onPageStartedCallBack(WebView view, String url, Bitmap favicon)
  {
    LogUtil.d(TAG, "onPageStarted URL: " + url);
    this.mUrl = url;
    if (!isWeiboCustomScheme(url))
    {
      this.mHtmlTitle = "";
    }
  }

  public boolean shouldOverrideUrlLoadingCallBack(WebView view, String url)
  {
    LogUtil.i(TAG, "shouldOverrideUrlLoading URL: " + url);
    return false;
  }

  public void onPageFinishedCallBack(WebView view, String url)
  {
    LogUtil.d(TAG, "onPageFinished URL: " + url);
    if (this.isErrorPage) {
      promptError();
    } else {
      this.isErrorPage = false;
      hiddenErrorPrompt();
    }
  }

  public void onReceivedErrorCallBack(WebView view, int errorCode, String description, String failingUrl)
  {
    LogUtil.d(TAG, "onReceivedError: errorCode = " + errorCode + 
      ", description = " + description + 
      ", failingUrl = " + failingUrl);
    handleReceivedError(view, errorCode, description, failingUrl);
  }

  public void onReceivedSslErrorCallBack(WebView view, SslErrorHandler handler, SslError error)
  {
    LogUtil.d(TAG, "onReceivedSslErrorCallBack.........");
    handler.proceed();//加上此句可忽略证书过期问题，或者证书错误，发行机构不合法等导致的webview无法加载url
  }

  public static void closeBrowser(Activity act, String authListenerKey, String widgetRequestCallbackKey) {
    WeiboCallbackManager manager = WeiboCallbackManager.getInstance(
      act.getApplicationContext());
    if (!TextUtils.isEmpty(authListenerKey)) {
      manager.removeWeiboAuthListener(authListenerKey);
      act.finish();
    }
    if (!TextUtils.isEmpty(widgetRequestCallbackKey)) {
      manager.removeWidgetRequestCallback(widgetRequestCallbackKey);
      act.finish();
    }
  }

  private class WeiboChromeClient extends WebChromeClient
  {
    private WeiboChromeClient()
    {
    }

    public void onProgressChanged(WebView view, int newProgress)
    {
      WeiboBrowser.this.mLoadingBar.drawProgress(newProgress);
      if (newProgress == 100) {
          WeiboBrowser.this.isLoading = false;
          WeiboBrowser.this.refreshAllViews();
      }
      else if (!WeiboBrowser.this.isLoading) {
          WeiboBrowser.this.isLoading = true;
          WeiboBrowser.this.refreshAllViews();
      }
    }

    public void onReceivedTitle(WebView view, String title)
    {
      if (!WeiboBrowser.this.isWeiboCustomScheme(WeiboBrowser.this.mUrl)) {
          WeiboBrowser.this.mHtmlTitle = title;
          WeiboBrowser.this.updateTitleName();
      }
    }
  }
}