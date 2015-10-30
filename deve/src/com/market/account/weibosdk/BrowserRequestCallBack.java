package com.market.account.weibosdk;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

abstract interface BrowserRequestCallBack
{
  public abstract void onPageStartedCallBack(WebView paramWebView, String paramString, Bitmap paramBitmap);

  public abstract boolean shouldOverrideUrlLoadingCallBack(WebView paramWebView, String paramString);

  public abstract void onPageFinishedCallBack(WebView paramWebView, String paramString);

  public abstract void onReceivedErrorCallBack(WebView paramWebView, int paramInt, String paramString1, String paramString2);

  public abstract void onReceivedSslErrorCallBack(WebView paramWebView, SslErrorHandler paramSslErrorHandler, SslError paramSslError);
}