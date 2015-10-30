package com.freeme.themeclub.wallpaper.os;


public interface AsyncTaskObserver<Params, Progress, Result> {
    void onCancelled();
    void onPreExecute();
    void onProgressUpdate(Progress... values);
    void onPostExecute(Result result);
}