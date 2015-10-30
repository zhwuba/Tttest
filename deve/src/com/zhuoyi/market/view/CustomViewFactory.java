package com.zhuoyi.market.view;

import android.content.Context;

import com.market.behaviorLog.LogDefined;
import com.market.behaviorLog.UserLogSDK;
import com.market.statistics.ReportFlag;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.home.HomeView;
import com.zhuoyi.market.ranklist.RankListView;
import com.zhuoyi.market.utils.MarketUtils;

/**
 * 自定义View工厂
 * @author JLu
 *
 */
public class CustomViewFactory {
	
	public static final int VIEW_SOFT_RECOMMEND = 0;
	public static final int VIEW_SOFT_CATEGORY = 1;
	public static final int VIEW_SOFT_RANK = 2;
	
	public static final int VIEW_GAME_RECOMMEND = 3;
	public static final int VIEW_GAME_CATEGORY = 4;
	public static final int VIEW_GAME_RANK = 5;
	
	public static final int VIEW_BOY_FAVOURITE = 6;
	public static final int VIEW_GIRL_FAVOURITE = 7;
	public static final int VIEW_GUESS_YOU_LIKE = 8;
	
	public static final int VIEW_BEST_IN_GAME = 9;
	public static final int VIEW_BEST_IN_SOFT = 10;
	
	public static final int VIEW_NOVEL_HOT = 11;
	public static final int VIEW_NOVEL_NEW = 12;
	
	public static final int VIEW_SOFT_NEW = 13;
	public static final int VIEW_GAME_GIFT = 14;
	
	public static final int VIEW_OTHERS = 15;
	//榜单
	public static final int VIEW_RANKLIST_ALL = 16;
	
	public static final int VIEW_RANKLIST = 17;
	
	public static final int VIEW_HOME = 18;
	
	private static int mTopicIndex = 0;
	

	public static AbsCustomView create(int viewType,Context context,DownloadCallBackInterface downloadCallback) {
		if (context == null) context = MarketApplication.getRootContext();
		return create(viewType, context, downloadCallback, -1);
	}

	
	public static AbsCustomView create(int viewType,Context context,DownloadCallBackInterface downloadCallback, int assemblyId) {

	    String downFlag = null;
	    int downTopicId = -1;
	    AbsCustomView view = null;
	    
	    switch(viewType) {
		case VIEW_SOFT_RECOMMEND:
		    view = new HomeView(context,downloadCallback,2,0,"swRecommendFrame",ReportFlag.FROM_NULL,0,false);
		    view.setLogDes(UserLogSDK.getKeyDes(LogDefined.ACTIVITY_SOFT_ADVICE));
		    break;
		    
		case VIEW_SOFT_CATEGORY:
		    view = new CategoryView(context,2,1,"swCategoryFrame",ReportFlag.FROM_NULL).setDetailLogTag(LogDefined.ACTIVITY_SOFT_CLASS);
		    view.setLogDes(UserLogSDK.getKeyDes(LogDefined.ACTIVITY_SOFT_SORT));
		    break;
		    
		case VIEW_SOFT_RANK:
		    view = new OneColView(context,downloadCallback,2,3,"swRankFrame",ReportFlag.FROM_NULL,0);
		    view.setLogDes(UserLogSDK.getKeyDes(LogDefined.ACTIVITY_SOFT_RANK));
			break;
			
		case VIEW_GAME_RECOMMEND:
		    view = new HomeView(context,downloadCallback,3,0,null,ReportFlag.FROM_NULL,0,false);
		    view.setLogDes(UserLogSDK.getKeyDes(LogDefined.ACTIVITY_GAME_ADVICE));
		    break;
		    
		case VIEW_GAME_CATEGORY:
		    view = new CategoryView(context,3,1,null,ReportFlag.FROM_NULL).setDetailLogTag(LogDefined.ACTIVITY_GAME_CLASS);
		    view.setLogDes(UserLogSDK.getKeyDes(LogDefined.ACTIVITY_GAME_SORT));
			break;
			
		case VIEW_GAME_RANK:
		    view = new OneColView(context,downloadCallback,3,3,null,ReportFlag.FROM_NULL,0);
		    view.setLogDes(UserLogSDK.getKeyDes(LogDefined.ACTIVITY_GAME_RANK));
			break;
			
		case VIEW_BOY_FAVOURITE:
		    downTopicId = MarketUtils.getTopicId(4, mTopicIndex);
            downFlag = ReportFlag.getReportFlag(ReportFlag.FROM_DISCOVERY, -1, true, ReportFlag.CHILD_ID_TYPE_TOPICID, downTopicId);
		    view = new OneColView(context,downloadCallback,4,mTopicIndex,null,downFlag,0);
			view.setLogDes(UserLogSDK.getKeyDes(LogDefined.ACTIVITY_BOY_JOY));
			break;
			
		case VIEW_GIRL_FAVOURITE:
		    downTopicId = MarketUtils.getTopicId(4, mTopicIndex);
            downFlag = ReportFlag.getReportFlag(ReportFlag.FROM_DISCOVERY, -1, true, ReportFlag.CHILD_ID_TYPE_TOPICID, downTopicId);
		    view = new OneColView(context,downloadCallback,4,mTopicIndex,null,downFlag,0);
		    view.setLogDes(UserLogSDK.getKeyDes(LogDefined.ACTIVITY_GIRL_JOY));
			break;
			
		case VIEW_OTHERS:
		    downTopicId = MarketUtils.getTopicId(4, mTopicIndex);
		    downFlag = ReportFlag.getReportFlag(ReportFlag.FROM_DISCOVERY, -1, true, ReportFlag.CHILD_ID_TYPE_TOPICID, downTopicId);
            view = new OneColView(context,downloadCallback,4,mTopicIndex,null,downFlag,0);
            view.setLogDes(UserLogSDK.getFindChildActivityDes(downTopicId));
            break;
            
		case VIEW_GUESS_YOU_LIKE:
		    view = new GuessYouLikeView(context, downloadCallback, null);
		    view.setLogDes(UserLogSDK.getKeyDes(LogDefined.ACTIVITY_GUESS_JOY));
			break;
			
		case VIEW_BEST_IN_GAME:
		    view = new OneColView(context,downloadCallback,0,1,null,ReportFlag.FROM_FAST_RISE_GAME,0);
		    view.setLogDes(UserLogSDK.getKeyDes(LogDefined.ACTIVITY_IN_GAME));
			break;
			
		case VIEW_BEST_IN_SOFT:
		    view = new OneColView(context,downloadCallback,0,1,null,ReportFlag.FROM_FAST_RISE_SOFT,1);
		    view.setLogDes(UserLogSDK.getKeyDes(LogDefined.ACTIVITY_IN_SOFT));
			break;
			
		case VIEW_NOVEL_HOT:
		    view = new OneColView(context,downloadCallback,4,1,null,ReportFlag.FROM_NULL,0);
		    view.setLogDes(UserLogSDK.getKeyDes(LogDefined.ACTIVITY_NOVEL_HOT));
			break;
			
		case VIEW_NOVEL_NEW:
		    view = new OneColView(context,downloadCallback,4,2,null,ReportFlag.FROM_NULL,0);
		    view.setLogDes(UserLogSDK.getKeyDes(LogDefined.ACTIVITY_NOVEL_NEW));
			break;
			
		case VIEW_SOFT_NEW:
		    view = new OneColView(context,downloadCallback,2,2,"swNewFrame",ReportFlag.FROM_NULL,0);
            view.setLogDes(UserLogSDK.getKeyDes(LogDefined.ACTIVITY_SOFT_NEW));
            break;
            
		case VIEW_RANKLIST_ALL:
		    int parentTopicId = MarketUtils.getTopicId(0, 1);
		    downFlag = ReportFlag.getReportFlag(ReportFlag.FROM_NULL, parentTopicId, true, ReportFlag.CHILD_ID_TYPE_ASSEMBLY, assemblyId);
			view = new OneColView(context, downloadCallback,0,2, null, downFlag, 0, assemblyId);
			break;
			
		case VIEW_RANKLIST:
			view = new RankListView(context, downloadCallback,0,1, "rankListFrame", ReportFlag.FROM_NULL, 0);
			break;
			
		case VIEW_HOME:
			view = new HomeView(context, downloadCallback,0,0, null, ReportFlag.FROM_HOMEPAGE, 0,true);
			break;
	    }
	    
		return view;
	}


    public static void setmTopicIndex(int mTopicIndex) {
        CustomViewFactory.mTopicIndex = mTopicIndex;
    }
	
}
