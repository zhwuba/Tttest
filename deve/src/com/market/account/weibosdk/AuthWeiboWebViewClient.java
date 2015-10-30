package com.market.account.weibosdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.component.AuthRequestParam;
import com.sina.weibo.sdk.component.WeiboSdkBrowser;
import com.sina.weibo.sdk.exception.WeiboAuthException;
import com.sina.weibo.sdk.utils.Utility;

class AuthWeiboWebViewClient extends WeiboWebViewClient
{
  private Activity mAct;
  private AuthRequestParam mAuthRequestParam;
  private WeiboAuthListener mListener;
  private boolean isCallBacked = false;

  public AuthWeiboWebViewClient(Activity activity, AuthRequestParam requestParam) {
    this.mAct = activity;
    this.mAuthRequestParam = requestParam;
    this.mListener = this.mAuthRequestParam.getAuthListener();
  }

  public void onPageStarted(WebView view, String url, Bitmap favicon)
  {
    if (this.mCallBack != null) {
      this.mCallBack.onPageStartedCallBack(view, url, favicon);
    }

    AuthInfo authInfo = this.mAuthRequestParam.getAuthInfo();
    if ((url.startsWith(authInfo.getRedirectUrl())) && 
      (!this.isCallBacked)) {
      this.isCallBacked = true;
      handleRedirectUrl(url);
      view.stopLoading();

      WeiboSdkBrowser.closeBrowser(this.mAct, 
        this.mAuthRequestParam.getAuthListenerKey(), null);
      return;
    }

    super.onPageStarted(view, url, favicon);
  }

  public boolean shouldOverrideUrlLoading(WebView view, String url)
  {
    if (this.mCallBack != null) {
      this.mCallBack.shouldOverrideUrlLoadingCallBack(view, url);
    }

    if (url.startsWith("sms:")) {
      Intent sendIntent = new Intent("android.intent.action.VIEW");
      sendIntent.putExtra("address", url.replace("sms:", ""));
      sendIntent.setType("vnd.android-dir/mms-sms");
      this.mAct.startActivity(sendIntent);
      return true;
    }
    if (url.startsWith("sinaweibo://browser/close")) {
      if (this.mListener != null) {
        this.mListener.onCancel();
      }
      WeiboSdkBrowser.closeBrowser(this.mAct, 
        this.mAuthRequestParam.getAuthListenerKey(), null);
      return true;
    }
    return super.shouldOverrideUrlLoading(view, url);
  }

  public void onPageFinished(WebView view, String url)
  {
    if (this.mCallBack != null) {
      this.mCallBack.onPageFinishedCallBack(view, url);
    }
    super.onPageFinished(view, url);
  }

  public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
  {
    if (this.mCallBack != null) {
      this.mCallBack.onReceivedErrorCallBack(view, errorCode, description, failingUrl);
    }
    super.onReceivedError(view, errorCode, description, failingUrl);
  }

  public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
  {
    if (this.mCallBack != null) {
      this.mCallBack.onReceivedSslErrorCallBack(view, handler, error);
    }
    super.onReceivedSslError(view, handler, error);
  }

  private void handleRedirectUrl(String url)
  {
    Bundle values = Utility.parseUrl(url);

    String errorType = values.getString("error");
    String errorCode = values.getString("error_code");
    String errorDescription = values.getString("error_description");

    if ((errorType == null) && (errorCode == null)) {
      if (this.mListener != null) {
        this.mListener.onComplete(values);
      }
    }
    else if (this.mListener != null)
      this.mListener.onWeiboException(
        new WeiboAuthException(errorCode, errorType, errorDescription));
  }
}