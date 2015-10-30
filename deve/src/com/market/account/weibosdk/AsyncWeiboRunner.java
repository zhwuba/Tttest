package com.market.account.weibosdk;

import android.content.Context;
import android.os.AsyncTask;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.WeiboParameters;

public class AsyncWeiboRunner
{
  private Context mContext;

  public AsyncWeiboRunner(Context context)
  {
    this.mContext = context;
  }

  @Deprecated
  public void requestByThread(final String url, final WeiboParameters params, final String httpMethod, final RequestListener listener)
  {
    new Thread()
    {
      public void run() {
        try {
          String resp = HttpManager.openUrl(AsyncWeiboRunner.this.mContext, url, httpMethod, params);
          if (listener != null)
            listener.onComplete(resp);
        }
        catch (WeiboException e) {
          if (listener != null)
            listener.onWeiboException(e);
        }
      }
    }
    .start();
  }

  public String request(String url, WeiboParameters params, String httpMethod)
    throws WeiboException
  {
    return HttpManager.openUrl(this.mContext, url, httpMethod, params);
  }

  public void requestAsync(String url, WeiboParameters params, String httpMethod, RequestListener listener)
  {
    new RequestRunner(this.mContext, url, params, httpMethod, listener).execute(new Void[1]);
  }

  private static class AsyncTaskResult<T>
  {
    private T result;
    private WeiboException error;

    public T getResult()
    {
      return this.result;
    }

    public WeiboException getError() {
      return this.error;
    }

    public AsyncTaskResult(T result)
    {
      this.result = result;
    }

    public AsyncTaskResult(WeiboException error)
    {
      this.error = error;
    }
  }

  private static class RequestRunner extends AsyncTask<Void, Void, AsyncWeiboRunner.AsyncTaskResult<String>>
  {
    private final Context mContext;
    private final String mUrl;
    private final WeiboParameters mParams;
    private final String mHttpMethod;
    private final RequestListener mListener;

    public RequestRunner(Context context, String url, WeiboParameters params, String httpMethod, RequestListener listener)
    {
      this.mContext = context;
      this.mUrl = url;
      this.mParams = params;
      this.mHttpMethod = httpMethod;
      this.mListener = listener;
    }

    protected AsyncWeiboRunner.AsyncTaskResult<String> doInBackground(Void[] params)
    {
      try {
        String result = HttpManager.openUrl(this.mContext, this.mUrl, this.mHttpMethod, this.mParams);
        return new AsyncWeiboRunner.AsyncTaskResult(result);
      }
      catch (WeiboException e) {
        return new AsyncWeiboRunner.AsyncTaskResult(e);
      }
    }

    protected void onPreExecute()
    {
    }

    protected void onPostExecute(AsyncWeiboRunner.AsyncTaskResult<String> result)
    {
      WeiboException exception = result.getError();
      if (exception != null)
        this.mListener.onWeiboException(exception);
      else
        this.mListener.onComplete((String)result.getResult());
    }
  }
}