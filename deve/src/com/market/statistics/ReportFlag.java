package com.market.statistics;

public class ReportFlag {
	
	public static final String ACTION_EXIST_MARKET = "existMarket";
	
	public static final String ACTION_VIEW_COLUMN = "viewColumn";
	
	
	public static final String TOPIC_NULL = "-1";
	
	public static final String TOPIC_OLD_DATA = "-2";
	
    
    public static final String FROM_NULL = "Null";
    
    public static final String EXTRA_PRE = "extra_";
    
    public static final String FROM_EXTRA_DOWN = EXTRA_PRE + "Group";
    
    public static final String FROM_THIRD_DOWNLOAD = "ThirdDownload";
    
    public static final String FROM_SELF_UPDATE = "update_Group";
    
    public static final String FROM_HOMEPAGE = "Homepage";
    
    public static final String FROM_HOME_AD = "HomeAd";
    
    public static final String FROM_DETAIL = "_Detail";
    
    public static final String FROM_FAVORITE = "Favorite";
    
    public static final String FROM_GUESS_YOU_LIKE = "GuessYouLike";
    
    public static final String FROM_YICHABAO = "YiChaBao";
    
    public static final String FROM_DOWN_MANA_RECOMMEND = "DownManaRecommend";
    
    public static final String FROM_DOWN_MANA = "DownMana";
    
    public static final String FROM_SEARCH = "Search";
    
    public static final String FROM_BACRGROUND_DOWN = "Background";
    
    public static final String FROM_UPDATE_MANA = "UpdateMana";
    
    public static final String FROM_704 = "AutoUpdate";
    
    public static final String FROM_DOWN_GIFT = "DownGift";
    
    public static final String FROM_AUTO_UPDATE = "ZeroTraffic";
    
    public static final String FROM_705 = "705";
    
    public static final String FROM_706 = "706";
    
    public static final String FROM_ENTRY_AD = "EntryAd";
    
    public static final String FROM_TYD_LAUNCHER = "TydLauncher";
    
    public static final String FROM_FAST_RISE_GAME = "FastRiseGame";
    
    public static final String FROM_FAST_RISE_SOFT = "FastRiseSoft";
    
    public static final String FROM_CLOUD_DOWN = "CloudDown";
    
    public static final String FROM_EXTERNEL_INSTALL = "ExternelInstall";
    
    //new
    
    public static final String FROM_EARN_CREDIT = "EarnCredit";
    
    public static final String FROM_GAIN_GIFT = "GainGift";
    
    public static final String FROM_DISCOVERY = "Discovery";
    
    public static final String FROM_SEE_OTHER = "SeeOther";
    
    public static final String FROM_DETAIL_RECOMMEND = "Detail_Recommend";
    
    public static final String FROM_DETAIL_ALLLIKE = "Detail_allLike";
    
    public static final String FLAG_SPLIT_CHARS = "--";
    
    public static final int CHILD_ID_TYPE_TOPICID = 0;
    public static final int CHILD_ID_TYPE_ASSEMBLY = 1;
    
    public static final int CHILD_VIEW_STATUS_OUT = 0;
    public static final int CHILD_VIEW_STATUS_IN = 1;
    
    public static String getReportFlag(String fromFlag, int topicId, boolean inChildView, int childIdType, int childId) {
        String topicIdStr = null;
        String fatherFrom = null;
        String reportFrom = null;
        if (!inChildView) {
            topicIdStr = "Null";
            fatherFrom = "Null";
            reportFrom = fromFlag;
        } else {
            topicIdStr = Integer.toString(topicId);
            fatherFrom = fromFlag;
            
            if (childIdType == CHILD_ID_TYPE_ASSEMBLY) {
                reportFrom = FROM_SEE_OTHER;
            } else {
                reportFrom = FROM_NULL;
            }
        }
        
        String childView = Integer.toString(inChildView ? CHILD_VIEW_STATUS_IN : CHILD_VIEW_STATUS_OUT)
                           + "_" + Integer.toString(childIdType)
                           + "_" + Integer.toString(childId)
                           + "_" + fatherFrom
                           + "_" + topicIdStr;
        return childView + FLAG_SPLIT_CHARS + reportFrom;
    }
    
    
    public static String parserDetailFromFlag(String fromFlag) {
        FromDes fromDes = splitFromFlag(fromFlag);
//        if (fromDes.fromFlag.equals(FROM_SEE_OTHER)) {
//            return FROM_NULL;
//        }
        return fromDes.fromFlag;
    }
    
    
    public static FromDes splitFromFlag(String fromFlag) {
        FromDes fromDes = new FromDes();
        if (fromFlag.contains(FLAG_SPLIT_CHARS)) {
            String splitList[] = fromFlag.split(FLAG_SPLIT_CHARS);
            fromDes.childView = splitList[0];
            fromDes.fromFlag = splitList[1];
        } else {
            fromDes.fromFlag = fromFlag;
            fromDes.childView = null;
        }
        
        return fromDes;
    }
    
    
    public String getDetailDownFrom(String from, String childDes) {
        return childDes + FLAG_SPLIT_CHARS + from;
    }
    
    
    public static class FromDes{
        public String fromFlag;
        public String childView;
    }
}
