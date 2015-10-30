package com.zhuoyi.market.search;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.market.net.data.AppInfoBto;
import com.market.net.data.HotSearchInfoBto;
import com.market.statistics.ReportFlag;
import com.zhuoyi.market.WebActivity;
import com.zhuoyi.market.utils.MarketUtils;

public class SearchUtils {

    public static void doClickHotWord(Context context,HotSearchInfoBto hotWord) {
        if (hotWord == null) {
            return;
        }
        if (hotWord.getType() == HotSearchInfoBto.TYPE_APP_INFO) {
            jumpToAppDetail(context,hotWord);
            return;
        }
        if (hotWord.getJumpFlag() == HotSearchInfoBto.TYPE_URL) {
            jumpToUrl(context,hotWord);
            return;
        } 
        jumpToHotWordList(context,hotWord);
    }
    
    /**
     * {点击热词，跳转应用详情}.
     * @param hotWord
     */
    public static void jumpToAppDetail(Context mContext,HotSearchInfoBto hotWord) {
        AppInfoBto appInfo = hotWord.getAppInfo();
        if (appInfo != null) {
            MarketUtils.startAppDetailActivity(mContext, appInfo, ReportFlag.FROM_SEARCH, -1);
        }
    }


    /**
     * {点击热词，跳转热词搜索列表}.
     * @param hotWord
     */
    public static void jumpToHotWordList(Context mContext,HotSearchInfoBto hotWord) {
        Intent intent = new Intent(mContext, SearchActivity.class);
        intent.putExtra("external_keyword", hotWord.getText());
        mContext.startActivity(intent);
    }


    /**
     * {点击热词，跳转热词url}.
     * @param hotWord
     */
    public static void jumpToUrl(Context mContext,HotSearchInfoBto hotWord) {
        String url = "";
        if (TextUtils.isEmpty(hotWord.getJumpUrl())) {
            return;
        }
        if (!hotWord.getJumpUrl().startsWith("http")) {
            url = "http://" + hotWord.getJumpUrl();
        }else {
            url = hotWord.getJumpUrl();
        }
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setClass(mContext, WebActivity.class);
        localIntent.putExtra("wbUrl", url);
        localIntent.putExtra("from_path", ReportFlag.FROM_SEARCH);
        localIntent.putExtra("titleName", hotWord.getText());
        mContext.startActivity(localIntent);
    }
    
}
