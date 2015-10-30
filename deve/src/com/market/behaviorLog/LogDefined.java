package com.market.behaviorLog;

public class LogDefined {
	
	/*
	 * start define the activity key
	 * log Example:
	 *  {'ac':1, 'view':'{'key':'Main'}', 'show':1, 'time':2000}
	 *  'ac':1 means this is a activity show and time type 
	 *  'view' means the activity description
	 *  'show' means activity entry count
	 *  'time' means activity display millis
	 */
	/**
	 * the 'view' value about main activity<br/>
	 * Example:	'view':'{'key':'Main'}'
	 */
	public static final String ACTIVITY_MAIN = "Main";
	/**
	 * the 'view' value about rank list activity<br/>
     * Example: 'view':'{'key':'RankList'}'
	 */
	public static final String ACTIVITY_RANK_LIST = "RankList";        //new
	/**
	 * the 'view' value about rank child activity<br/>
	 * Example: 'view':'{'key':'RankChild', 'id':356, 'name':'renqipaihangbang'}'
	 */
	public static final String ACTIVITY_RANK_CHILD = "RankChild";      //new
	/**
	 * the 'view' value about soft advice activity<br/>
	 * Example:	'view':'{'key':'SofAdvs'}'
	 */
	public static final String ACTIVITY_SOFT_ADVICE = "SofAdvs";
	/**
	 * the 'view' value about soft sort activity<br/>
	 * Example:	'view':'{'key':'SofSort'}'
	 */
	public static final String ACTIVITY_SOFT_SORT = "SofSort";
	/**
	 * the 'view' value about soft new activity<br/>
     * Example: 'view':'{'key':'SofNew'}'
	 */
	public static final String ACTIVITY_SOFT_NEW = "SofNew";           //new
	/**
	 * the 'view' value about soft rank activity<br/>
	 * Example:	'view':'{'key':'SofRank'}'
	 */
	public static final String ACTIVITY_SOFT_RANK = "SofRank";
	/**
	 * the 'view' value about game advice activity<br/>
	 * Example:	'view':'{'key':'GamAdcs'}'
	 */
	public static final String ACTIVITY_GAME_ADVICE = "GamAdvs";
	/**
	 * the 'view' value about game sort activity<br/>
	 * Example:	'view':'{'key':'GamSort'}'
	 */
	public static final String ACTIVITY_GAME_SORT = "GamSort";
	/**
	 * the 'view' value about game rank activity<br/>
	 * Example:	'view':'{'key':'GamRank'}'
	 */
	public static final String ACTIVITY_GAME_RANK = "GamRank";
	/**
	 * the 'view' value about find activity<br/>
	 * Example:	'view':'{'key':'Find'}'
	 */
	public static final String ACTIVITY_FIND = "Find";
	/**
	 * the 'view' value about find activity<br/>
	 * Example: 'view':'{'key':'FindChild', id:475}'
	 */
	public static final String ACTIVITY_FIND_CHILD = "FindChild";          //new
	/**
	 * the 'view' value about novel hot activity<br/>
	 * Example:	'view':'{'key':'NovHot'}'
	 */
	public static final String ACTIVITY_NOVEL_HOT = "NovHot";
	/**
	 * the 'view' value about novel new activity<br/>
	 * Example:	'view':'{'key':'NovNew'}'
	 */
	public static final String ACTIVITY_NOVEL_NEW = "NovNew";
	/**
	 * the 'view' value about boy love activity<br/>
	 * Example:	'view':'{'key':'BoyJoy'}'
	 */
	public static final String ACTIVITY_BOY_JOY = "BoyJoy";
	/**
	 * the 'view' value about girl love activity<br/>
	 * Example:	'view':'{'key':'GirlJoy'}'
	 */
	public static final String ACTIVITY_GIRL_JOY = "GirlJoy";
	/**
	 * the 'view' value about guess like activity<br/>
	 * Example:	'view':'{'key':'GuesJoy'}'
	 */
	public static final String ACTIVITY_GUESS_JOY = "GuesJoy";
	/**
	 * the 'view' value about special activity entry from main activity<br/>
	 * Example:	'view':'{'key':'MainClub'}'
	 */
//	public static final String ACTIVITY_MAIN_CLUB = "MainClub";
	/**
	 * the 'view' value about special activity entry from find activity<br/>
	 * Example:	'view':'{'key':'FindClub'}'
	 */
	public static final String ACTIVITY_FIND_CLUB = "FindClub";
	/**
	 * the 'view' value about fasion game activity<br/>
	 * Example:	'view':'{'key':'inGame'}'
	 */
	public static final String ACTIVITY_IN_GAME = "inGame";
	/**
	 * the 'view' value about fasion soft activity<br/>
	 * Example:	'view':'{'key':'inSoft'}'
	 */
	public static final String ACTIVITY_IN_SOFT = "inSoft";
	/**
	 * the 'view' value about necessary activity<br/>
	 * Example:	'view':'{'key':'Gotta'}'
	 */
	public static final String ACTIVITY_GOTTA = "Gotta";
	/**
	 * the 'view' value about application detail activity<br/>
	 * Example:	'view':'{'key':'ApDetl', 'id':'1001', 'package':'com.android.test1', 'name':'testApp'}'
	 */
	public static final String ACTIVITY_APK_DETAIL = "ApDetl";
	/**
	 * the 'view' value about special detail activity<br/>
	 * Example:	'view':'{'key':'ClubDetl', 'id':'120', 'name':'welcome to hell'}'
	 */
	public static final String ACTIVITY_CLUB_DETAIL = "ClubDetl";
	/**
	 * the 'view' value about game sort detail activity<br/>
	 * Example:	'view':'{'key':'GamClas', 'two':'role playing', 'third':'3D'}'
	 */
	public static final String ACTIVITY_GAME_CLASS = "GamClas";
	/**
	 * the 'view' value about soft sort detail activity<br/>
	 * Example:	'view':'{'key':'SofClas', 'two':'read', 'third':'new'}'
	 */
	public static final String ACTIVITY_SOFT_CLASS = "SofClas";
	/**
     * the 'view' value about wall paper hot activity<br/>
     * Example: 'view':'{'key':'WalPapHot'}'
     */
	public static final String ACTIVITY_WALL_PAPER_HOT = "WalPapHot";          //new
	/**
     * the 'view' value about wall paper new activity<br/>
     * Example: 'view':'{'key':'WalPapNew'}'
     */
	public static final String ACTIVITY_WALL_PAPER_NEW = "WalPapNew";          //new
	/**
     * the 'view' value about wall paper sort activity<br/>
     * Example: 'view':'{'key':'WalPapSort'}'
     */
	public static final String ACTIVITY_WALL_PAPER_SORT = "WalPapSort";        //new
	/**
	 * the 'view' value about wall paper class list activity<br/>
     * Example: 'view':'{'key':'WalPapClass', 'name':'fengjing', 'code':''}'
	 */
	public static final String ACTIVITY_WALL_PAPER_CLASS = "WalPapClass";      //new
	
	
	
	/*
	 * start define show and click event key
	 * log Example:
	 *  {'ac':2, 'event':'{'key':'Main'}', 'show':8, 'click':6}
	 *  'ac':2 means this is a show and click view type 
	 *  'event' means the event description
	 *  'show' means the view show count
	 *  'click' means the view click count
	 */
	/**
	 * the 'event' value about entry ad<br/>
	 * Example:	<br/>
	 * if entry the application detail when click ad<br/>
	 * 'event':'{'key':'EntryAd', 'type':1, 'id':'1001', 'package':'com.android.test1', 'name':'testApp'}'<br/>
	 * <br/>
	 * if entry the special detail when click ad<br/>
	 * 'event':'{'key':'EntryAd', 'type':2, 'id':'120', 'name':'welcome to hell'}'<br/>
	 * <br/>
	 * if entry the web view when click ad<br/>
	 * 'event':'{'key':'EntryAd', 'type':3, 'name':'this is a web url'}'<br/>
	 */
	public static final String VIEW_ENTRY_AD = "EntryAd";
	/**
	 * the 'event' value about main ad user operation<br/>
	 * Example:	<br/>
	 * if entry the application detail when click ad<br/>
	 * 'event':'{'key':'HomAdU', 'type':1, 'id':'1001', 'package':'com.android.test1', 'name':'testApp'}'<br/>
	 * <br/>
	 * if entry the special detail when click ad<br/>
	 * 'event':'{'key':'HomAdU', 'type':2, 'id':'120', 'name':'welcome to hell'}'<br/>
	 * <br/>
	 * if entry the web view when click ad<br/>
	 * 'event':'{'key':'HomAdU', 'type':3, 'name':'this is a web url'}'<br/>
	 */
	public static final String VIEW_HOME_AD = "HomAdU";
	/**
	 * the 'event' value about search hot words<br/>
	 * Example:	<br/>
	 * 'event':'{'key':'SechKW', 'work':'QQ'}'
	 */
	public static final String VIEW_SEARCH_KEY_WORD = "SechKW";
	/**
	 * the 'event' value about wall paper detail, contains events: show, click set wall paper button
	 * Example:    <br/>
     * 'event':'{'key':'WapPapDetail', 'name':'manhua', 'id': 456}'
	 */
	public static final String VIEW_WALL_PAPER_DETAIL = "WalPapDetail";            //new
	
	/*
	 * start define count event key
	 * log Example:
	 *  {'ac':3, 'event':'{'key':'SechNum'}', 'count':8}
	 */
	/**
	 * the 'event' value about wall paper download
     * Example:    <br/>
     * 'event':'{'key':'WalPapDown', 'name':'manhua', 'id': 456}'
	 */
	public static final String COUNT_WALL_PAPER_DOWN = "WalPapDown";             //new
	/**
	 * the 'event' value about search count<br/>
	 * Example:	<br/>
	 * 'event':'{'key':'SechWord', 'word':'QQ'}'
	 */
	public static final String COUNT_SEARCH_WORD = "SechWord";
	/**
	 * the 'event' value about main ad auto display<br/>
	 * Example:	<br/>
	 * if entry the application detail when click ad<br/>
	 * 'event':'{'key':'HomAdA', 'type':1, 'id':'1001', 'package':'com.android.test1', 'name':'testApp'}'<br/>
	 * <br/>
	 * if entry the special detail when click ad<br/>
	 * 'event':'{'key':'HomAdA', 'type':2, 'id':'120', 'name':'welcome to hell'}'<br/>
	 * <br/>
	 * if entry the web view when click ad<br/>
	 * 'event':'{'key':'HomAdA', 'type':3, 'name':'this is a web url'}'<br/>
	 */
	public static final String COUNT_HOME_AD = "HomAdA";
	/**
	 * the 'event' value about settings view count<br/>
	 * Example:	<br/>
	 * 'event':'{'key':'SetView'}'
	 */
	public static final String COUNT_SETTING_VIEW = "SetView";
	/**
	 * the 'event' value about trash clean view count<br/>
     * Example: <br/>
     * 'event':'{'key':'TrashView'}'
	 */
	public static final String COUNT_TRASH_VIEW = "TrashView";         //new
	/**
	 * the 'event' value about mine view count<br/>
     * Example: <br/>
     * 'event':'{'key':'MineView'}'
	 */
	public static final String COUNT_MINE_VIEW = "MineView";           //new
	/**
	 * the 'event' value about update view count<br/>
	 * Example:	<br/>
	 * 'event':'{'key':'UpdaView'}'
	 */
	public static final String COUNT_UPDATE_VIEW = "UpdaView";
	/**
	 * the 'event' value about download view count<br/>
	 * Example:	<br/>
	 * 'event':'{'key':'DownView'}'
	 */
	public static final String COUNT_DOWNLOAD_VIEW = "DownView";
	/**
	 * the 'event' value about favorite view count<br/>
	 * Example:	<br/>
	 * 'event':'{'key':'FavoView'}'
	 */
	public static final String COUNT_FAVORITE_VIEW = "FavoView";
	/**
	 * the 'event' value about scroll words on search editor view<br/>
	 * Example: <br/>
	 * 'event':'{'key':'ScrolWord', 'word':'QQ'}'
	 */
	public static final String COUNT_SCROLL_WORD_CLICK = "ScrolWord";
	/**
     * the 'event' value about check view count<br/>
     * Example: <br/>
     * 'event':'{'key':'CheckView'}'
     */
	public static final String COUNT_CHECK_VIEW = "CheckView";
	
	
	/*
	 * start define action type, 
	 * 1 means activity show count and time
	 * 2 means view show and click event
	 * 3 means count event
	 */
	public static final int AC_TYPE_ACTIVITY = 1;
	public static final int AC_TYPE_VIEW = 2;
	public static final int AC_TYPE_COUNT = 3;
	
	
	/*
	 * start define display type when click view
	 */
	public static final int CIK_TYPE_APK_DETAIL = 1;
	public static final int CIK_TYPE_SPECAIL_DETAIL = 2;
	public static final int CIK_TYPE_WEB_VIEW = 3;
	
	
	public static final String KEY_ACTION = "ac";
	public static final String KEY_ACTIVITY_DES = "view";
	public static final String KEY_EVENT = "event";
	public static final String KEY_SHOW_COUNT = "show";
	public static final String KEY_SHOW_TIME = "time";
	public static final String KEY_COUNT = "count";
	public static final String KEY_KEY = "key";
	public static final String KEY_TYPE = "type";
	public static final String KEY_ID = "id";
	public static final String KEY_NAME = "name";
	public static final String KEY_TWO_SORT = "two";
	public static final String KEY_THIRD_SORT = "third";
	public static final String KEY_PACKAGE_NAME = "package";
	public static final String KEY_CLICK_COUNT = "click";
	public static final String KEY_HOT_WORD = "word";
	public static final String KEY_CODE = "code";
}
