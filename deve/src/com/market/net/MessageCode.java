package com.market.net;

public class MessageCode 
{
	public static final int NONE = -1;
	public static final int APK_CHECK_SELF_UPDATE = 103001; // apk自更新
	public static final int GET_SERVER_INFO = 100001;  //启动地址获取
	public static final int GET_STARET_PAGE = 101002; //启动页获取
	public static final int GET_RECOMMEND_APPS = 101003; //详情推荐APP
	public static final int GET_APPS_UPDATE = 101004; //市场内应有更新
	public static final int CHECK_APP_VALID = 101005; //易查宝
	public static final int GUESS_YOU_LIKE = 101006; //猜你喜欢
	public static final int GET_ASSOCIATIVE_WORD = 101007; //联系词请求
	public static final int GET_STATIC_SEARCH_APP = 101008; //搜索页推荐应用
	public static final int GET_SEARCH_APP = 101009;  //搜索应用请求
	public static final int GET_MARKET_FRAME = 101027; //市场框架请求  old 101010
	public static final int GET_TAB_CHANNEL = 101011; //频道信息获取
	public static final int GET_TOPIC_LIST = 101012; //栏目专题信息获取
	public static final int GET_APK_DETAIL = 101013; //应用详情信息请求
	public static final int GET_SILENT_DOWNLOAD_REQ = 101014; //应用静默下载请求
	public static final int GET_APK_LIST_BY_PAGE = 101015; //列表分页获取；
	public static final int GET_MODEL_APK_LIST_BY_PAGE = 101028;//首页模块化分页请求
	public static final int COMMIT_USER_COMMENT_REQ = 101019; //用户提交评论
	public static final int GET_USER_COMMENT_REQ = 101020; //应用详情用户评论信息获取
	public static final int GET_DOWNLOAD_RECOMMEND_APPS = 101021; //下载推荐APP
	public static final int GET_DATA_DOWNLOAD_REQ = 102001; //下载结果请求
	public static final int GET_DATA_USER_OPERATE_REQ = 102002; //用户行为请求
	public static final int GET_DATA_TOKEN_REQ = 102003; //Token请求
	public static final int GET_DATA_STAUS_REQ = 102004; //销量统计
	public static final int GET_USER_FEEDBACK = 101022; //用户反馈
	public static final int GET_SOFT_GAME_TOPIC = 101023; //应用或游戏的分类信息请求
	public static final int GET_SOFT_GAME_DETAIL = 101024; //应用或游戏的详细分类信息请求
	public static final int REPORT_OFFLINE_LOG = 101026; //上传离线日志
	public static final int GET_INTEGRAL_INFO_REQ = 101029; //活动信息请求
	public static final int GET_APK_DETAIL_BY_PACKNAME_REQ = 101030; //根据包名的应用详情信息请求
	public static final int GET_DISCOVER_DATA = 101031; //发现数据获取
	public static final int GET_MODEL_TOPIC_REQ = 101035; //榜单信息请求
	public static final int GET_WALLPAPER_LIST_REQ = 101032; //壁纸列表
	public static final int GET_WALLPAPER_DETAIL_REQ = 101033; //壁纸详情
	public static final int GET_SUBJECT_DATA_REQ = 101034; //专题数据
}
