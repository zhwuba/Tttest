package com.market.net;

import java.util.HashMap;
import java.util.Map;

import com.market.net.split.ApkCheckSelfUpdateCodec;
import com.market.net.split.CommitUserCommentCodec;
import com.market.net.split.GetAssociativeWordCodec;
import com.market.net.split.GetCategoryDetailCodec;
import com.market.net.split.GetDetailInfoCodec;
import com.market.net.split.GetDetailRecommendAppCodec;
import com.market.net.split.GetDiscoverInfoCodec;
import com.market.net.split.GetDownloadRecommendAppCodec;
import com.market.net.split.GetIntegralInfoCodec;
import com.market.net.split.GetListByPageCodec;
import com.market.net.split.GetSubjectCodec;
import com.market.net.split.GetWallpaperListCodec;
import com.market.net.split.GetWallpaperDetailCodec;
import com.market.net.split.GetMarketFrameCodec;
import com.market.net.split.GetModelListByPageCodec;
import com.market.net.split.GetModelTopicCodec;
import com.market.net.split.GetSearchAppListCodec;
import com.market.net.split.GetServerInfoCodec;
import com.market.net.split.GetStartupPageCodec;
import com.market.net.split.GetStaticSearchAppCodec;
import com.market.net.split.GetTopListCodec;
import com.market.net.split.GetUpdateAppsListCodec;
import com.market.net.split.GetUserCommentCodec;
import com.market.net.split.GetUserFeedbackCodec;
import com.market.net.split.GuessYouLikeCodec;

public class DataCodecFactory 
{

    private static final Map<Integer, DataCodec> mCodecs;
    // private static final Map<Integer, DataBuilder> mDataBuilder;
    static
    {
        mCodecs = new HashMap<Integer, DataCodec>();
        mCodecs.put(MessageCode.APK_CHECK_SELF_UPDATE, new ApkCheckSelfUpdateCodec());
        mCodecs.put(MessageCode.GET_SERVER_INFO, new GetServerInfoCodec());
        mCodecs.put(MessageCode.GET_STARET_PAGE, new GetStartupPageCodec());
        mCodecs.put(MessageCode.GET_MARKET_FRAME, new GetMarketFrameCodec());
        mCodecs.put(MessageCode.GET_TOPIC_LIST, new GetTopListCodec());
        mCodecs.put(MessageCode.GET_MODEL_APK_LIST_BY_PAGE, new GetModelListByPageCodec());
        mCodecs.put(MessageCode.GET_APK_LIST_BY_PAGE, new GetListByPageCodec());
        mCodecs.put(MessageCode.GET_APK_DETAIL, new GetDetailInfoCodec());
        mCodecs.put(MessageCode.GET_RECOMMEND_APPS, new GetDetailRecommendAppCodec());
        mCodecs.put(MessageCode.GUESS_YOU_LIKE, new GuessYouLikeCodec());
		mCodecs.put(MessageCode.GET_ASSOCIATIVE_WORD, new GetAssociativeWordCodec());
        mCodecs.put(MessageCode.GET_STATIC_SEARCH_APP, new GetStaticSearchAppCodec());
        mCodecs.put(MessageCode.GET_USER_COMMENT_REQ, new GetUserCommentCodec());
        mCodecs.put(MessageCode.GET_DOWNLOAD_RECOMMEND_APPS, new GetDownloadRecommendAppCodec());
        mCodecs.put(MessageCode.COMMIT_USER_COMMENT_REQ, new CommitUserCommentCodec());
        mCodecs.put(MessageCode.GET_SEARCH_APP, new GetSearchAppListCodec());
        mCodecs.put(MessageCode.GET_APPS_UPDATE, new GetUpdateAppsListCodec());
        mCodecs.put(MessageCode.CHECK_APP_VALID, new GetUpdateAppsListCodec());
        mCodecs.put(MessageCode.GET_USER_FEEDBACK, new GetUserFeedbackCodec());
        mCodecs.put(MessageCode.GET_SOFT_GAME_TOPIC, new GetTopListCodec());
        mCodecs.put(MessageCode.GET_SOFT_GAME_DETAIL, new GetCategoryDetailCodec());
        mCodecs.put(MessageCode.GET_INTEGRAL_INFO_REQ, new GetIntegralInfoCodec());
        mCodecs.put(MessageCode.GET_APK_DETAIL_BY_PACKNAME_REQ, new GetDetailInfoCodec());
        mCodecs.put(MessageCode.GET_DISCOVER_DATA, new GetDiscoverInfoCodec());
        mCodecs.put(MessageCode.GET_MODEL_TOPIC_REQ, new GetModelTopicCodec());
        mCodecs.put(MessageCode.GET_WALLPAPER_LIST_REQ, new GetWallpaperListCodec());
        mCodecs.put(MessageCode.GET_WALLPAPER_DETAIL_REQ, new GetWallpaperDetailCodec());
        mCodecs.put(MessageCode.GET_SUBJECT_DATA_REQ, new GetSubjectCodec());
        mCodecs.put(MessageCode.NONE, null);
    }

    public static DataCodec fetchDataCodec(int messageCode)
    {
        if (mCodecs.containsKey(messageCode))
            return mCodecs.get(messageCode);
        else
            return null;

    }
}
