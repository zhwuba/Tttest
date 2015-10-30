package com.zhuoyi.market.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RejectedExecutionException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.market.behaviorLog.LogDefined;
import com.market.behaviorLog.UserLogSDK;
import com.market.download.baseActivity.DownloadBaseActivity;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.userDownload.DownloadManager;
import com.market.download.util.Util;
import com.market.net.DataCodecFactory;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.AppInfoBto;
import com.market.net.data.HotSearchInfoBto;
import com.market.net.data.KeyWordInfoBto;
import com.market.net.request.GetAssociativeWordReq;
import com.market.net.request.GetStaticSearchAppReq;
import com.market.net.request.SearchAppReq;
import com.market.net.response.GetAssociativeWordResp;
import com.market.net.response.GetMarketFrameResp;
import com.market.net.response.GetStaticSearchAppResp;
import com.market.net.response.SearchAppResp;
import com.market.net.utils.OpenUrlPostUtils;
import com.market.net.utils.RequestAsyncTask;
import com.market.net.utils.StartNetReqUtils;
import com.market.statistics.ReportFlag;
import com.market.view.CommonTitleDownloadView;
import com.zhuoyi.market.R;
import com.zhuoyi.market.Splash;
import com.zhuoyi.market.adapter.SingleLineItemAdapter;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.search.ShakeManager.OnShakeListener;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.FrameInfoCache;
import com.zhuoyi.market.utils.MarketUtils;

/**
 * 搜索页面
 * @author JLu
 *
 */
public class SearchActivity extends DownloadBaseActivity implements OnScrollListener,DownloadCallBackInterface {   
	private int mStartIndex;
	private int mEndIndex;
	/**
	 * 进入市场后是否首次进入搜索页面
	 */
	private static boolean mIsFirstEntry = true;
	/**
	 * 返回按钮
	 */
	private ImageView mBackBtn;
	/**
	 * 搜索按钮
	 */
	private ImageView mSearchBtn;
	/**
	 * 搜索框
	 */
	private EditText mSearchEdit;
	/**
	 * 全民热搜页
	 */
	private LinearLayout mHotWordsLay;
	/**
	 * 热词gridview
	 */
	private GridView mHotWordGridView;
	/**
	 * 换一换按钮
	 */
	private Button mChangeBtn;
	/**
	 * 热词gridview的adapter
	 */
	private SearchHotWordsAdapter hotwordsAdapter;
	/**
	 * 搜索结果listView
	 */
	private ListView mSearchResultListView;
	/**
	 * 搜索结果listView的adapter
	 */
	private SingleLineItemAdapter mSearchResultAdapter;
	/**
	 * progressBar
	 */
	private LinearLayout mSearch_loading;
	/**
	 * 请求数据记录的起始下标
	 */
	private int mRequestCursor = 0;
	/**
	 * 请求数据页数
	 */
	private int mPageNumber = 0;
	/**
	 * 每一页请求的数据个数
	 */
	private static final int REQUEST_PAGE_SIZE = 16;
	/**
	 * 判断一次数据是否加载完毕,防止onScroll的时候一直重复请求数据
	 */
	private boolean mRefreshFinished = true;
	/**
	 * 服务端的数据数据是否到底了
	 */
	private boolean mIsToBottom = false;
	private View mFooter;
	private ProgressBar mFooterProgress = null;
	private TextView mFooterText = null;

	/**
	 * 搜索请求关键词
	 */
	private String mInputKeyword;

	private static final int GET_HOTWORDS_LIST = 1;
	private static final int GET_SEARCH_FIRSTPAGE_RESULT = 2;
	private static final int GET_SEARCH_NOT_FIRSTPAGE_RESULT = 3;
	private static final int GET_ASSOCIATIVE_WORDS = 4;
	
	private static final int HOTWORDS_VIEW = 0;
	private static final int LOADING_VIEW = 1;
	private static final int ATTACH_VIEW = 2;
	private static final int SEARCH_RESULT_VIEW = 3;

	private SearchAppReq searchReq = null;
	private RequestAsyncTask mAsyncTask;

	/**
	 * 输入推荐列表外层布局
	 */
	private LinearLayout mAttachLayout = null;
	/**
	 * 输入推荐列表adapter的数据
	 */
	private List<KeyWordInfoBto> mSearchAttachList = null;
	/**
	 * 输入推荐列表listview
	 */
	private ListView mSearchAttachListview = null;
	/**
	 * 输入推荐列表adapter
	 */
	private SearchAttachAdapter mSearchAttachAdapter = null;
	
	private String mKeyWord = null;
	private static HashMap<String, List<KeyWordInfoBto>> mOldSearchAttachList = null;
	private static List<String> mOldSearchAttachKeyWords = null;
	private final int OLD_SAVE_SEARCH_WORDS_MAX_SIZE = 20;
	private final int HANDLER_MSG_SEARCH_ATTACH = 1000;
	/**
	 * 是否显示输入推荐的标志,二维码返回结果,点击输入推荐本身某一词条后,点击热词按钮
	 * 这三种情况不显示"输入推荐列表"
	 * 在设置edittext内容之前设置该值
	 */
	private boolean mIsSearchAttach = true;
	

	/**
	 * 清除输入按钮
	 */
	private ImageView mClearBtn = null;

	private Context mContext = null;
	private DownloadCallBackInterface mDownloadCallBackInterface = null;

	//added by JLu for yaoyiyao
	private ShakeManager mShakeManager;
	private Vibrator mVibrator;
	//end

	/**
	 * 下发的所有热搜词数据
	 */
	private List<HotSearchInfoBto> mAllHotWordsList;
	
	/**
	 * 当前页的热搜词数据
	 */
	private List<HotSearchInfoBto> mCurrentHotWordsList;

	/**
	 * 每个热词页要显示的个数
	 */
	private static final int PAGE_SIZE = 9;
	/**
	 * 热词页码
	 */
	private int pageCount = 0;
	
	/**
	 * 是否从外部传入搜索条件进来
	 */
	private boolean mIsExternal = false; 
	
	/**
	 * ListView判断是否正在滚动 
	 */
	private boolean mIsScrolling = false;
	
    /**
     * 判断back按键时是否直接退出
     */
    public boolean mIsFinishDirect = false;
    
    /**
     * 从快捷方式进入，判断back按键时是否直接退出
     */
    public boolean mIsFinishByShortCut = true;
    
    /**
     * 从CommonMainTitle的搜索框进搜索页，读取当前的热词设置搜索框的hint
     */
    public HotSearchInfoBto mMainSearchHint;
    
    public String launcher_from = "";
    
    
    private Timer mTimer = null;
    
    /**
     * 前一次搜索内容
     */
    public String mOldSearchCond = " ";
    
    public static final String FIRST_CREATE_SHORT_CUT = "first_create_short_cut";
    private boolean mShortCutCreate = false;
    
    
    private CommonTitleDownloadView mCommonTitleDownloadView = null;
	
	public void onCreate(Bundle savedInstanceState) {  
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.new_search_main);
		mContext = this.getApplicationContext();
		
		LinearLayout baseView = (LinearLayout) findViewById(R.id.base_layout);
		MarketUtils.setTitleLayout(baseView, mContext);
		
		mDownloadCallBackInterface = this;
		mTimer = new Timer();

		findViews();     

		initSearchAttachView();

		initSearchEdit();

		initShakeManager();

		requestHotData();
		SharedPreferences settings = getSharedPreferences(Splash.PREFS_NAME, 0);
		mShortCutCreate = settings.getBoolean(FIRST_CREATE_SHORT_CUT, false);
        if (!mShortCutCreate) {
            ShortCutUtils.createShortCut(this);
            settings.edit().putBoolean(FIRST_CREATE_SHORT_CUT, true).commit();
        }
	}


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            if(mSearchResultAdapter != null)
                mSearchResultAdapter.notifyDataSetChanged();
            if(mSearchAttachAdapter != null)
            	mSearchAttachAdapter.notifyDataSetChanged();
            
    		if (mCommonTitleDownloadView != null) {
    			mCommonTitleDownloadView.setDownloadStatus();
    		}
        }
        super.onWindowFocusChanged(hasFocus);
    }


	/**
	 * findViews开始
	 */
	private void findViews() {
		
		mCommonTitleDownloadView = (CommonTitleDownloadView) this.findViewById(R.id.title_download);
		mCommonTitleDownloadView.registeredReceiver();
		
		mBackBtn = (ImageView) findViewById(R.id.backImageView);
		//返回按钮事件
		mBackBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mIsFinishDirect) {
					if (!isTaskRoot()) {
						Intent intent = new Intent(SearchActivity.this, Splash.class);
						intent.putExtra("isClose", true);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
				}
				if (launcher_from.equals("search_short_cut")) {
	                doShortCutBackPress();
	                return;
	            }
				finish();
			}

		});
		
		mClearBtn = (ImageView) findViewById(R.id.search_clear_btn);
		mClearBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mAsyncTask != null) {
					mAsyncTask.cancel(true);
				}
				if(mSearchEdit != null) {
					mSearchEdit.setText("");
				}
			}
		});		


		mSearchBtn = (ImageView) findViewById(R.id.btn_search);	
		mSearchBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					disposeSearchBtnEvent(v,-1);
			}
		});

		mHotWordsLay = (LinearLayout) findViewById(R.id.hotwords_layout);
		mHotWordGridView = (GridView) findViewById(R.id.hotwords_gridview);
		hotwordsAdapter = new SearchHotWordsAdapter(mContext,R.layout.search_hotword);
		mHotWordGridView.setAdapter(hotwordsAdapter);
		mHotWordGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
				//点击热搜词项
				disposeSearchBtnEvent(view,position);
			}
		});

		mChangeBtn = (Button)findViewById(R.id.change_btn);
		mChangeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//换一换事件
				shakeChangeWords();
				
				//for record user behavior log
		        String wordDes = null;
		        for(HotSearchInfoBto info : mCurrentHotWordsList) {
		            wordDes = UserLogSDK.getSearchWordDes(LogDefined.VIEW_SEARCH_KEY_WORD, info.getText());
		            UserLogSDK.logViewShowEvent(mContext, wordDes);
		        }
			}
		});

		mSearch_loading = (LinearLayout) findViewById(R.id.search_loading);
		mSearchResultListView = (ListView) findViewById(R.id.searchapp_list);
		mFooter = LayoutInflater.from(this).inflate(R.layout.foot, null);
		mFooterProgress = (ProgressBar) mFooter.findViewById(R.id.footer_progress);
		mFooterText = (TextView) mFooter.findViewById(R.id.footer_textview);
		mSearchResultListView.setOnScrollListener(this);
		mSearchResultListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// see the detail of the app
				if (null != mSearchResultAdapter && position < mSearchResultAdapter.getCount()) {
						AppInfoBto mAppInfoBto = mSearchResultAdapter.getItem(position);
						MarketUtils.startAppDetailActivity(mContext, mAppInfoBto, ReportFlag.FROM_SEARCH);
				}
			}
		});

	}
	

	/**
	 * 请求热词数据
	 */
	private void requestHotData() {
		if(mIsFirstEntry) {
			//请求并缓存
			initData();
		} else {
			//从缓存中读
			GetStaticSearchAppResp hotResp = (GetStaticSearchAppResp) FrameInfoCache.getFrameInfoFromStorage("searchFrame");
			
			if(hotResp != null) {
				HashMap<String,Object> map = new HashMap<String, Object>();
				map.put("staticSearchInfo", hotResp);
				Message msg = new Message();
				msg.what = GET_HOTWORDS_LIST;
				msg.obj = map;
				mHandler.sendMessage(msg);
			} else {
				String contents = SenderDataProvider.buildToJSONData(getApplicationContext(),MessageCode.GET_STATIC_SEARCH_APP,new GetStaticSearchAppReq());
				StartNetReqUtils.execListByPageRequest(mHandler, GET_HOTWORDS_LIST,MessageCode.GET_STATIC_SEARCH_APP,contents);
			}
			
		}
	}

	private void initData() {
		GetMarketFrameResp frameResp = (GetMarketFrameResp) FrameInfoCache.getFrameInfoFromStorage("marketframe");
        if(frameResp == null) {
        	Constant.initMarketUrl(mContext);
        	new Thread(new Runnable() {
				
				@Override
				public void run() {
					if (getMarketFrameInfo(Constant.MARKET_URL)) {
						String contents = SenderDataProvider.buildToJSONData(getApplicationContext(),MessageCode.GET_STATIC_SEARCH_APP,new GetStaticSearchAppReq());
						StartNetReqUtils.execListByPageRequest(mHandler, GET_HOTWORDS_LIST,MessageCode.GET_STATIC_SEARCH_APP,contents);
					}
				}
			}).start();
        	return;
        }
        String contents = SenderDataProvider.buildToJSONData(getApplicationContext(),MessageCode.GET_STATIC_SEARCH_APP,new GetStaticSearchAppReq());
		StartNetReqUtils.execListByPageRequest(mHandler, GET_HOTWORDS_LIST,MessageCode.GET_STATIC_SEARCH_APP,contents);
	}
	
	private boolean getMarketFrameInfo(String url) {
		boolean isSuccessed = false;
        String contents = "";
        String result = "";
        HashMap<String, Object> map = null;
        GetMarketFrameResp marketFrameResp = null;
        try {
            contents = SenderDataProvider.buildToJSONData(getApplicationContext(),
                    MessageCode.GET_MARKET_FRAME, null);
            result = OpenUrlPostUtils.accessNetworkByPost(url, contents, false);

        } catch (IOException e) {
        	e.printStackTrace();
        	return isSuccessed;
        }
        map = (HashMap<String, Object>) DataCodecFactory.fetchDataCodec(
                MessageCode.GET_MARKET_FRAME).splitMySelfData(result);
        if (map != null) {
            marketFrameResp = (GetMarketFrameResp) map.get("marketFrame");
        }
        if (marketFrameResp != null) {
        	MarketUtils.setSharedPreferencesString(mContext,
                    MarketUtils.KEY_MARKET_ID, marketFrameResp.getMarketId());
        	MarketApplication.setMarketFrameResp(marketFrameResp);
            isSuccessed = true;
        }
        return isSuccessed;
    }
	
	/**
	 * 过滤掉热词中已安装的应用
	 * @param originalData 服务端下发的原始数据
	 * @return 过滤后的数据
	 */
	private List<HotSearchInfoBto> filterInstalledApp(List<HotSearchInfoBto> originalData) {
		Iterator<HotSearchInfoBto> iterator = originalData.iterator();
		HotSearchInfoBto searchInfo = null;
		AppInfoBto appInfo;
		while(iterator.hasNext()) {
			searchInfo = iterator.next();
			appInfo = searchInfo.getAppInfo();
			if(appInfo == null) {
				continue;
			}
			if(!appInfo.getIsShow()) {
				iterator.remove();
			}
		}
		return originalData;
	}
	

	/**
	 * 摇一摇换词动作,取得热词的时候会被回调 
	 */
	public void shakeChangeWords() {
		mCurrentHotWordsList = selectPageData();
		hotwordsAdapter.setDatas(mCurrentHotWordsList);
		hotwordsAdapter.notifyDataSetChanged();
	}
	
	
	/**
	 * 从下发的所有数据中筛选出一页的数据
	 */
	private List<HotSearchInfoBto> selectPageData() {
		if(mAllHotWordsList==null) {
			return null;
		}
		int totalSize = mAllHotWordsList.size();
		if(totalSize < PAGE_SIZE) {
			return mAllHotWordsList;
		}
		int start = pageCount * PAGE_SIZE;
		int end = ++pageCount * PAGE_SIZE;
		if(end > totalSize) {
			pageCount = 0;
			//最后一页数据为空时(即totalsize正好是PAGE_SIZE的倍数)，直接跳转到第一页数据
			if (totalSize%PAGE_SIZE == 0) {
			    pageCount++;
                return (List<HotSearchInfoBto>)mAllHotWordsList.subList(0, PAGE_SIZE);
            }
			//最后一页数据不足PAGE_SIZE个,拿前一页的补
			return (List<HotSearchInfoBto>)mAllHotWordsList.subList(start-(end-totalSize), totalSize);
		}
		List<HotSearchInfoBto> subList = (List<HotSearchInfoBto>)mAllHotWordsList.subList(start, end);
		return subList;
	}


	/**
	 * 初始化搜索框
	 */
	private void initSearchEdit() {
		mSearchEdit = (EditText) findViewById(R.id.search_edittext);
		mSearchEdit.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode==KeyEvent.KEYCODE_ENTER && event.getAction()== KeyEvent.ACTION_UP){
					//点击键盘上的搜索键
			        disposeSearchBtnEvent(v,-1);
				}
				return false;
			}
		});

		mSearchEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				try {
					String typeContent = s.toString();
					if (TextUtils.isEmpty(typeContent)) {
						mClearBtn.setVisibility(View.INVISIBLE);
						
						mPageNumber = 0;
						mRequestCursor = 0;	
						show(HOTWORDS_VIEW);
						isShowSoftInput(true);
						mSearchAttachAdapter.notifyDataSetInvalidated();
						mSearchAttachList.clear();
						mIsSearchAttach = true;
					} else {	
						typeContent = typeContent.trim();
						
						mClearBtn.setVisibility(View.VISIBLE);
						
						if(!mIsSearchAttach || TextUtils.isEmpty(typeContent)) {
							return;
						}
						show(ATTACH_VIEW);
						if(mSearchAttachHandler.hasMessages(HANDLER_MSG_SEARCH_ATTACH)) {
							mSearchAttachHandler.removeMessages(HANDLER_MSG_SEARCH_ATTACH);
						}
						//去服务器请求输入提示列表
						Message msgSearch = new Message();
						msgSearch.what = HANDLER_MSG_SEARCH_ATTACH;
						mSearchAttachHandler.sendMessageDelayed(msgSearch, 300);//300延时为了防止删除时每删除一个字符都要请求一次
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});

	}  /**初始化搜索框结束*/


	/**
	 * 初始化摇一摇震动相关
	 */
	private void initShakeManager() {
		mVibrator = (Vibrator)getApplication().getSystemService(VIBRATOR_SERVICE);
		mShakeManager = new ShakeManager(mContext);
		mShakeManager.setOnShakeListener(new OnShakeListener() {
			public void onShake() {
				if(!mHotWordsLay.isShown()) return;
				
				mShakeManager.stop();

				//开始 震动
				mVibrator.vibrate( new long[]{100,200/*,200,100*/}, -1); 
				//第一个｛｝里面是节奏数组， 第二个参数是重复次数，-1为不重复，非-1为从pattern的指定下标开始重复
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						shakeChangeWords();

						//for record user behavior log
				        String wordDes = null;
				        for(HotSearchInfoBto info : mCurrentHotWordsList) {
				            wordDes = UserLogSDK.getSearchWordDes(LogDefined.VIEW_SEARCH_KEY_WORD, info.getText());
				            UserLogSDK.logViewShowEvent(mContext, wordDes);
				        }
						
						mVibrator.cancel();
						mShakeManager.start();
					}
				}, 500);
			}
		});
	}


	/**
	 * 打开或隐藏键盘
	 */
	private void isShowSoftInput(boolean isShow) {
		View view = getWindow().peekDecorView();
		if (view != null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			if(isShow) {
				imm.showSoftInput(mSearchEdit, InputMethodManager.SHOW_IMPLICIT);
			} else {
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
		}
	}


	/**
	 * 点击某个热词,或者输入内容后点击搜索按钮,或键盘上的搜索键走这里
	 * @param v
	 */
	private void disposeSearchBtnEvent(View v,int position) {
	    if (v == null) {
            return;
        }
		//快速重复点击搜索按钮,不处理
		/*if(mAsyncTask!=null && mAsyncTask.getStatus()!= AsyncTask.Status.FINISHED) {
			return;
		}*/
	    mIsFinishByShortCut = false;
		if(mIsScrolling) {
			return;
		}
		
		if(mIsToBottom) {
			mIsToBottom = false;
			mFooterProgress.setVisibility(View.VISIBLE);
			mFooterText.setText(mContext.getString(R.string.loading));
		}

		//如果无网络,提示用户
		if (MarketUtils.getAPNType(mContext) == -1) {
			Toast.makeText(mContext, getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
			return;
		}

		mRefreshFinished = false;

		mSearchResultListView.setOnScrollListener(this);
		mRequestCursor = 0;
		mPageNumber = 0;

		//点击搜索按钮或者键盘搜索键
		if (v.getId() == R.id.btn_search || v.getId() == R.id.search_edittext) {		
		    
			mInputKeyword = mSearchEdit.getText().toString().trim();

			if (TextUtils.isEmpty(mInputKeyword)) {
			    if (mMainSearchHint != null) { 
			        isShowSoftInput(false);
			        doClickHotWord(SearchActivity.this,mMainSearchHint);
                }else {
                    Toast.makeText(mContext, getResources().getString(R.string.search_result_tips), Toast.LENGTH_SHORT).show();
                }
			    return;
			}
			show(LOADING_VIEW);
			isShowSoftInput(false);
			searchRequest(mInputKeyword);
			
			//for record user behavior logs
			UserLogSDK.logCountEvent(mContext, UserLogSDK.getSearchWordDes(LogDefined.COUNT_SEARCH_WORD, mInputKeyword));
		} else {
			isShowSoftInput(false);
			HotSearchInfoBto hotData = mCurrentHotWordsList.get(position);
			doClickHotWord(SearchActivity.this,hotData);
			
			//for record user behavior logs
			String wordDes = UserLogSDK.getSearchWordDes(LogDefined.VIEW_SEARCH_KEY_WORD, hotData.getText());
            UserLogSDK.logViewClickEvent(mContext, wordDes);
		}
	}
	
	public void doClickHotWord(Context context,HotSearchInfoBto hotWord) {
	    if (hotWord == null) {
            return;
        }
	    
        //for user behavior log
        UserLogSDK.logCountEvent(mContext, UserLogSDK.getSearchWordDes(LogDefined.COUNT_SCROLL_WORD_CLICK, hotWord.getText()));
        
        if (hotWord.getType() == HotSearchInfoBto.TYPE_APP_INFO) {
            SearchUtils.jumpToAppDetail(context,hotWord);
            return;
        }
        if (hotWord.getJumpFlag() == HotSearchInfoBto.TYPE_URL) {
            SearchUtils.jumpToUrl(context,hotWord);
            return;
        } 
        
        mInputKeyword = hotWord.getText();
        mIsSearchAttach = false;
        mSearchEdit.setText(mInputKeyword);
        mSearchEdit.setSelection(mInputKeyword.length() > 20 ? 20 : mInputKeyword.length());
        show(LOADING_VIEW);
        searchRequest(mInputKeyword);
        
    }
	
	/**
	 * 搜索由外部传入的关键词,比如二维码或首页滚动词条
	 * @param intent
	 */
	private void disposeExternalRequest(Intent intent) {
		String result = intent.getStringExtra("external_keyword");
		mIsFinishDirect = intent.getBooleanExtra("finish_direct", false);
		mMainSearchHint = (HotSearchInfoBto)intent.getSerializableExtra("main_search_hint");
		if (mMainSearchHint != null) {
		    mSearchEdit.setHint(mMainSearchHint.getText());
        }else {
            mSearchEdit.setHint("");
        }
		String from = intent.getStringExtra("launcher_from");
		if (from != null) {
		    launcher_from = from;
        }
		//仅用于从应用搜索的快捷方式进去判断
		if (launcher_from.equals("search_short_cut")) {
            mSearchEdit.setText("");
            mSearchEdit.setHint(R.string.shortcut_search_hint);
            isShowAD(false);
        }
		if(mIsFinishDirect) {
			isShowAD(false);
		}
		
		if(result != null && !result.equals(mOldSearchCond)) {
			mOldSearchCond = result;
			mIsSearchAttach = false;
			mSearchEdit.setText(result);
			mSearchEdit.setSelection(result.length()>20 ? 20 : result.length());
			searchKeyWords(result);
			mIsExternal = true;
		} else if(TextUtils.isEmpty(mSearchEdit.getText().toString()) && mTimer!=null) {
		    mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					isShowSoftInput(true);
				}
			}, 500);
		}
	}


	//滑动搜索结果列表走这里
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		switch (view.getId()) {

		case R.id.searchapp_list:
		    mStartIndex = firstVisibleItem;
            mEndIndex = firstVisibleItem + visibleItemCount;
            if (mEndIndex >= totalItemCount) {
                mEndIndex = totalItemCount - 1;
            }
            
			//滑动到底部,刷新数据
			if (view.getLastVisiblePosition() >= (view.getCount() - 1) 
					&& mRefreshFinished  && !TextUtils.isEmpty(mInputKeyword) && !mIsToBottom) {
				
				mPageNumber++;
				mRequestCursor = mPageNumber * REQUEST_PAGE_SIZE;
				mRefreshFinished = false;
				searchRequest(mInputKeyword);
			}

			break;

		}
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) { 
		case OnScrollListener.SCROLL_STATE_IDLE: //listview不滚动的时候
			mIsScrolling = false;
			if(mSearchResultAdapter!=null) {		
				mSearchResultAdapter.allowRefreshIcon(true);
				asyncLoadImage();
				//mTopic_adapter.notifyDataSetChanged();
			}
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
		case OnScrollListener.SCROLL_STATE_FLING:
			mIsScrolling = true;
			if(mSearchResultAdapter!=null)
				mSearchResultAdapter.allowRefreshIcon(false);
			break;
		}
	}
	
	@Override  
	protected void onNewIntent(Intent intent) {        
	    super.onNewIntent(intent);  
	    setIntent(intent); 
	}
	
	@Override
	protected void onResume() {
		if (mShakeManager != null) {
			mShakeManager.start();
		}
		disposeExternalRequest(getIntent());
		super.onResume();

		DownloadManager.startServiceReportOffLineLog(this, ReportFlag.ACTION_VIEW_COLUMN, ReportFlag.FROM_SEARCH);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mShakeManager != null) {
			mShakeManager.stop();
		}

	}

	@Override
	public void onDestroy() {    
		super.onDestroy();
		if(mSearchResultAdapter!=null)
			mSearchResultAdapter.freeImageCache();

		if (mShakeManager != null) {
			mShakeManager.stop();
			mShakeManager.destory();
			mShakeManager = null;
		}
		
		if (mCommonTitleDownloadView != null) {
			mCommonTitleDownloadView.unRegisteredReceiver();
		}
		if(mTimer!=null)
		{
			mTimer.cancel();
			mTimer = null;
		}
	}

	@Override
	public void onBackPressed() {
		String textString ="";

		mRefreshFinished = true;
		if(mAsyncTask != null) {
			mAsyncTask.cancel(true);
		}

		if (mIsFinishDirect) {
			if (!isTaskRoot()) {
				Intent intent = new Intent(SearchActivity.this, Splash.class);
				intent.putExtra("isClose", true);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			this.finish();
			return;
		}
		
		
		if(mSearchEdit!=null) {
			textString = mSearchEdit.getText().toString();
		}
		if(!TextUtils.isEmpty(textString)) {
			mSearchEdit.setText("");			
			show(HOTWORDS_VIEW);

				mPageNumber = 0;
				mRequestCursor = 0;
		} else {
		    if (launcher_from.equals("search_short_cut")) {
	            doShortCutBackPress();
	            return;
	        }
			super.onBackPressed();
		}
	}

	public void doShortCutBackPress(){
	    if (mIsFinishByShortCut) {
	        if (!isTaskRoot()) {
                Intent intent = new Intent(SearchActivity.this, Splash.class);
                intent.putExtra("isClose", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }else { //进入首页
            if (isTaskRoot()) {
                Intent intent = new Intent(SearchActivity.this, Splash.class);
                SharedPreferences settings = getSharedPreferences(Splash.PREFS_NAME, 0);
                Editor editor = settings.edit();
                editor.putBoolean(Splash.FIRST_RUN, false);
                editor.commit();
                if(Splash.getHandler() == null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("showLoadingUI", false);
                    startActivity(intent);
                }
            }
        }
	    this.finish();
	}
	
	/**
	 * 做请求数据的动作
	 * @param searchWord  请求搜索的关键词
	 */
	private void searchRequest(String searchWord) {
		searchReq = new SearchAppReq();
		searchReq.setKeyword(searchWord);
		searchReq.setStart(mRequestCursor);
		searchReq.setPageSize(REQUEST_PAGE_SIZE);
		String contents = SenderDataProvider.buildToJSONData(getApplicationContext(),MessageCode.GET_SEARCH_APP,searchReq);
		if(mAsyncTask != null && mAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
			mAsyncTask.cancel(true);
			mAsyncTask = null;
		}
		mAsyncTask = new RequestAsyncTask(mHandler, mRequestCursor == 0 ? GET_SEARCH_FIRSTPAGE_RESULT : GET_SEARCH_NOT_FIRSTPAGE_RESULT, contents);
		try {
			if(Build.VERSION.SDK_INT >= 11) {
				mAsyncTask.executeOnExecutor(MarketUtils.getDataReqExecutor(), Constant.MARKET_URL,MessageCode.GET_SEARCH_APP);
			} else {
				mAsyncTask.execute(Constant.MARKET_URL,MessageCode.GET_SEARCH_APP);
			}
		} catch(RejectedExecutionException e) {
			
		}
	}

	private Handler mHandler = new Handler() {	
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {	
			HashMap<String, Object> map = null;
			switch(msg.what) {
			case GET_HOTWORDS_LIST:
				/**
				 * 获取服务端下发的热搜词数据列表
				 */
				GetStaticSearchAppResp hotResp = null;
				List<HotSearchInfoBto> hotWordsList = null;
				map = (HashMap<String, Object>) msg.obj;
				if (map != null && map.size()>0) {
					hotResp = (GetStaticSearchAppResp) map.get("staticSearchInfo");
				}
				if(hotResp != null) {
					hotWordsList = hotResp.getHotSearchList();
					if(mIsFirstEntry) {
						mIsFirstEntry = false;
						FrameInfoCache.saveFrameInfoToStorage(hotResp, "searchFrame");
					}
				}
				if(hotWordsList!=null && hotWordsList.size()>0) {
					mAllHotWordsList = filterInstalledApp(hotWordsList);
					shakeChangeWords();
					if(!mIsExternal) {
						show(HOTWORDS_VIEW);
					}
				}

				break;
			case GET_SEARCH_FIRSTPAGE_RESULT:
				/**
				 * 获取服务端下发的搜索结果列表
				 */
				SearchAppResp resp = null;
				List<AppInfoBto> appInfoList = null;
				List<AppInfoBto> searchResultData = new ArrayList<AppInfoBto>();
				int isGetDataSuccess = msg.arg1;
				map = (HashMap<String, Object>) msg.obj;

				if(isGetDataSuccess == 1) {//获取数据成功
					if (map != null && map.size()>0) {
						resp = (SearchAppResp) map.get("searchAppListInfo");

						if(resp != null) {
							appInfoList = resp.getAppList();
						}
					}

					if(mRefreshFinished) return;

					if(appInfoList!=null && appInfoList.size() > 0) {
						int listSize = appInfoList.size();
						AppInfoBto appInfo = null;
						for(int i=0; i<listSize ;i++) {
							appInfo = appInfoList.get(i);
							appInfo.setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(),false));
							searchResultData.add(appInfo);
						}

						if(mSearchResultAdapter==null ) {
							mSearchResultAdapter = new SingleLineItemAdapter(SearchActivity.this.getApplicationContext(),
									mDownloadCallBackInterface, Util.getWaterFlowLayoutId());
							mSearchResultAdapter.setReportFlag(ReportFlag.FROM_SEARCH);
							mSearchResultAdapter.setTopicId(-1);
							if(mSearchResultListView.getFooterViewsCount() == 0) {
								mSearchResultListView.addFooterView(mFooter);
							}
							mSearchResultListView.setAdapter(mSearchResultAdapter);	
						}
						
						
						if (listSize > 0 && mSearchResultAdapter != null) {
							mSearchResultAdapter.setDatas(searchResultData);
							mSearchResultAdapter.notifyDataSetChanged();
							
							if (listSize < REQUEST_PAGE_SIZE) { // 数据不足一屏(16个)时，显示加载完毕
								mIsToBottom = true;
								mFooterProgress.setVisibility(View.GONE);
								mFooterText.setText(mContext.getString(R.string.loaded_all_data));
							}
						}
					} else {
						mIsToBottom = true;
						mFooterProgress.setVisibility(View.GONE);
						mFooterText.setText(mContext.getString(R.string.loaded_all_data));
					}
				}

				mRefreshFinished = true;

				if(mRequestCursor==0 && !TextUtils.isEmpty(mSearchEdit.getText().toString())) {
					if(searchResultData != null && searchResultData.size() > 0) {
						mSearchResultListView.setSelection(0);
						show(SEARCH_RESULT_VIEW);
					} else if(isGetDataSuccess == 1) {
						show(HOTWORDS_VIEW);
						Toast.makeText(SearchActivity.this, R.string.search_no_result, Toast.LENGTH_SHORT).show();
					} else {
						show(HOTWORDS_VIEW);
						Toast.makeText(SearchActivity.this, R.string.search_no_network, Toast.LENGTH_SHORT).show();
					}
				}
				break;
			case GET_SEARCH_NOT_FIRSTPAGE_RESULT:
				if(mRequestCursor == 0)
					return;
				SearchAppResp appResp = null;
				List<AppInfoBto> appInfoLists = null;
				List<AppInfoBto> searchResultDatas = new ArrayList<AppInfoBto>();
				int isGetDataResult = msg.arg1;
				map = (HashMap<String, Object>) msg.obj;

				if(isGetDataResult == 1) {//获取数据成功
					if (map != null && map.size()>0) {
						appResp = (SearchAppResp) map.get("searchAppListInfo");

						if(appResp != null) {
							appInfoLists = appResp.getAppList();
						}
					}

					if(mRefreshFinished) return;

					if(appInfoLists!=null && appInfoLists.size() > 0) {
						int listSize = appInfoLists.size();
						AppInfoBto appInfo = null;
						for(int i=0; i<listSize ;i++) {
							appInfo = appInfoLists.get(i);
							appInfo.setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(),false));
							searchResultDatas.add(appInfo);
						}

						if(mSearchResultAdapter==null ) {
							mSearchResultAdapter = new SingleLineItemAdapter(SearchActivity.this.getApplicationContext(),
									mDownloadCallBackInterface, Util.getWaterFlowLayoutId());
							mSearchResultAdapter.setReportFlag(ReportFlag.FROM_SEARCH);
							mSearchResultAdapter.setTopicId(-1);
							if(mSearchResultListView.getFooterViewsCount() == 0) {
								mSearchResultListView.addFooterView(mFooter);
							}
							mSearchResultListView.setAdapter(mSearchResultAdapter);	
						}
						
						
						if (listSize > 0 && mSearchResultAdapter != null) {
							mSearchResultAdapter.addDatas(searchResultDatas);
							mSearchResultAdapter.notifyDataSetChanged();
						}
					} else {
						mIsToBottom = true;
						mFooterProgress.setVisibility(View.GONE);
						mFooterText.setText(mContext.getString(R.string.loaded_all_data));
					}
				}

				mRefreshFinished = true;

				if(mRequestCursor==0 && !TextUtils.isEmpty(mSearchEdit.getText().toString())) {
					if(searchResultDatas != null && searchResultDatas.size() > 0) {
						mSearchResultListView.setSelection(0);
						show(SEARCH_RESULT_VIEW);
					} else if(isGetDataResult == 1) {
						show(HOTWORDS_VIEW);
						Toast.makeText(SearchActivity.this, R.string.search_no_result, Toast.LENGTH_SHORT).show();
					} else {
						show(HOTWORDS_VIEW);
						Toast.makeText(SearchActivity.this, R.string.search_no_network, Toast.LENGTH_SHORT).show();
					}
				}
				break;
			case GET_ASSOCIATIVE_WORDS:
				GetAssociativeWordResp respWords = null;
				map = (HashMap<String, Object>) msg.obj;

				if (map != null && map.size()>0) {
					respWords = (GetAssociativeWordResp) map.get("associativeWords");
					setSearchAttachAdpater(respWords.getAssWords());
				}
			}
		}
	};


	private void asyncLoadImage() {
		if(mSearchResultAdapter == null) {
			return;
		}
		ImageView imageView = null;
		AppInfoBto info = null;
		
		for (; mStartIndex <= mEndIndex; mStartIndex++) {
			info = (AppInfoBto) mSearchResultAdapter.getItem(mStartIndex);
			if (info == null) {
				continue;
			}
			
			imageView = (ImageView) mSearchResultListView.findViewWithTag(info.getPackageName());
			if(imageView == null) {
				continue;
			}
			
			if (mSearchResultAdapter.isAllowRefreshIcon() == false) {
				break;
			}
			
			int defaultResId = R.drawable.picture_bg1_big;
			int resId = (Integer) imageView.getTag(R.id.tag_image_resid);
			if (resId == defaultResId) {
				AsyncImageCache.from(mContext).displayImage(
						mSearchResultAdapter.isAllowRefreshIcon(),
						imageView,
						R.drawable.picture_bg1_big,
						new AsyncImageCache.NetworkImageGenerator(info
								.getPackageName(), info.getImgUrl()), true);
			}
		}    
	}


	//输入推荐相关开始
	/**
	 * 初始化输入推荐列表
	 */
	private void initSearchAttachView() {
		mAttachLayout = (LinearLayout)findViewById(R.id.search_attach_parent);
		MarketUtils.setBaseLayout(mAttachLayout, mContext);

		mSearchAttachList = new ArrayList<KeyWordInfoBto>();
		mSearchAttachAdapter = new SearchAttachAdapter(this.getApplicationContext(), mSearchAttachList,mDownloadCallBackInterface);
		mSearchAttachListview = (ListView)this.findViewById(R.id.search_attach_listview);
		mSearchAttachListview.setAdapter(mSearchAttachAdapter);
		mSearchAttachListview.setOnItemClickListener(new OnItemClickListener() {
			//点击输入提示列表中的某个词条
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				if(mSearchAttachAdapter != null && mSearchAttachAdapter.isSearchApp(position)) {
					//是app进详情
					AppInfoBto appInfo = mSearchAttachList.get(position).getAppInfoBto();
					MarketUtils.startAppDetailActivity(mContext, appInfo, ReportFlag.FROM_SEARCH);
				} else {
					//是文本,作为关键词搜索
					String keyString = mSearchAttachList.get(position).getKey();
					mIsSearchAttach = false;
					mSearchEdit.setText(keyString);
					mSearchEdit.setSelection(keyString.length() > 20 ? 20 :keyString.length());
					searchKeyWords(keyString);
				}
			}

		});
	}
	
	
	/**
	 * 发送推荐列表请求的Handler
	 */
	private Handler mSearchAttachHandler = new Handler() {	
		public void handleMessage(Message msg) {	
			if(msg.what == HANDLER_MSG_SEARCH_ATTACH) {
				//先清空之前的推荐数据
				if(mSearchAttachList != null) {
					mSearchAttachAdapter.notifyDataSetInvalidated();
					mSearchAttachList.clear();
				}
				String keyWords = mSearchEdit.getText().toString().trim();
				if(!TextUtils.isEmpty(keyWords)) {
					getAssociativeWordReq(keyWords);
				}
			}
		}
	};
	
	
	/**
	 * 联网请求输入推荐列表
	 * @param keyWord 请求的关键词
	 */
	private void getAssociativeWordReq(String keyWord) {
		String contents = "";
		mKeyWord = keyWord;

		if(mOldSearchAttachList != null) {
			List<KeyWordInfoBto> oldKeyWords = mOldSearchAttachList.get(keyWord);

			if(oldKeyWords != null) {
				setSearchAttachAdpater(oldKeyWords);
				return;
			}
		}

		GetAssociativeWordReq associativeWordReq = new GetAssociativeWordReq();
		associativeWordReq.setChId(Constant.td);
		associativeWordReq.setKeyWords(keyWord);
		contents = SenderDataProvider.buildToJSONData(getApplicationContext(),MessageCode.GET_ASSOCIATIVE_WORD,associativeWordReq);
		try {
			if(Build.VERSION.SDK_INT >= 11) {
				new RequestAsyncTask(mHandler, GET_ASSOCIATIVE_WORDS,contents).executeOnExecutor(MarketUtils.getDataReqExecutor(), Constant.MARKET_URL,MessageCode.GET_ASSOCIATIVE_WORD);
			} else {
				new RequestAsyncTask(mHandler, GET_ASSOCIATIVE_WORDS,contents).execute(Constant.MARKET_URL,MessageCode.GET_ASSOCIATIVE_WORD);
			}
		} catch (RejectedExecutionException e) {
			
		}

	}
	

	@Override
    protected void onRestart() {
        super.onRestart();
        
        if(mCurrentHotWordsList!=null && mCurrentHotWordsList.size() >0) {
            if (mHotWordsLay.getVisibility() == View.VISIBLE){
                //for record user behavior log
                String wordDes = null;
                for(HotSearchInfoBto info : mCurrentHotWordsList) {
                    wordDes = UserLogSDK.getSearchWordDes(LogDefined.VIEW_SEARCH_KEY_WORD, info.getText());
                    UserLogSDK.logViewShowEvent(mContext, wordDes);
                }
            }
        }
    }


    /**
	 * 设置输入推荐列表的数据
	 * @param keyWordList
	 */
	private void setSearchAttachAdpater(List<KeyWordInfoBto> keyWordList) {
		if(keyWordList == null || keyWordList.size()==0) return;

		saveOldSearchWords(keyWordList);

		mSearchAttachAdapter.notifyDataSetInvalidated();
		mSearchAttachList.clear();
		Iterator<KeyWordInfoBto> iterator = keyWordList.iterator();
		while(iterator.hasNext()) {
			mSearchAttachList.add(iterator.next());
		}
		mSearchAttachAdapter.setDataList(mSearchAttachList);
		mSearchAttachAdapter.notifyDataSetChanged();

	}
	

	/**
	 * 保存老的关键词的输入推荐列表
	 * @param keywordList 某个关键词的搜索推荐列表
	 */
	private void saveOldSearchWords(List<KeyWordInfoBto> keywordList) {

		if(keywordList == null || keywordList.size() <= 0) return;

		if(mOldSearchAttachList == null) {
			mOldSearchAttachList = new HashMap<String, List<KeyWordInfoBto>>();
		}
		if(mOldSearchAttachKeyWords == null) {
			mOldSearchAttachKeyWords = new ArrayList<String>();
		}

		if(mOldSearchAttachList.size() > OLD_SAVE_SEARCH_WORDS_MAX_SIZE) {
			String keyWordsFirst = mOldSearchAttachKeyWords.get(0);
			mOldSearchAttachList.remove(keyWordsFirst);
			mOldSearchAttachKeyWords.remove(0);
		}
		mOldSearchAttachKeyWords.add(mKeyWord);
		mOldSearchAttachList.put(mKeyWord,keywordList);
	}
	//输入推荐相关结束
	

	/**
	 * 搜索关键词  二维码结果和点击输入提示词条走这里
	 * @param keyword
	 */
	private void searchKeyWords(String keyword) {
		mInputKeyword = keyword;
		
		//如果无网络,提示用户
		if (MarketUtils.getAPNType(mContext) == -1) {
			Toast.makeText(SearchActivity.this, getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
			return;
		}

		mRefreshFinished = false;


		mSearchResultListView.setOnScrollListener(this);
		mRequestCursor = 0;
		mPageNumber = 0;

		if (TextUtils.isEmpty(keyword)) {
			//输入内容为空
			Toast.makeText(SearchActivity.this, getResources().getString(R.string.search_result_tips), Toast.LENGTH_SHORT).show();
			return;
		}
		show(LOADING_VIEW);

		isShowSoftInput(false);
		searchRequest(keyword);
	}


	/**
	 * 切换页面显示状态
	 * @param whichView 要显示的页面flag
	 */
	private void show(int whichView) {
		switch(whichView) {
		case HOTWORDS_VIEW:
		    mSearch_loading.setVisibility(View.GONE);
            mAttachLayout.setVisibility(View.GONE);
            mSearchResultListView.setVisibility(View.GONE);
            mIsScrolling = false;
			if(mCurrentHotWordsList!=null && mCurrentHotWordsList.size() >0) {
			    if (mHotWordsLay.getVisibility() != View.VISIBLE) {
			        mHotWordsLay.setVisibility(View.VISIBLE);
	                //for record user behavior log
	                String wordDes = null;
	                for(HotSearchInfoBto info : mCurrentHotWordsList) {
	                    wordDes = UserLogSDK.getSearchWordDes(LogDefined.VIEW_SEARCH_KEY_WORD, info.getText());
	                    UserLogSDK.logViewShowEvent(mContext, wordDes);
	                }
			    }
			}
			
			break;
		case LOADING_VIEW:
			mHotWordsLay.setVisibility(View.GONE);
			mSearch_loading.setVisibility(View.VISIBLE);
			mAttachLayout.setVisibility(View.GONE);
			mSearchResultListView.setVisibility(View.GONE);
			break;
		case ATTACH_VIEW:
			mHotWordsLay.setVisibility(View.GONE);
			mSearch_loading.setVisibility(View.GONE);
			mAttachLayout.setVisibility(View.VISIBLE);
			mSearchResultListView.setVisibility(View.GONE);
			break;
		case SEARCH_RESULT_VIEW:
			mHotWordsLay.setVisibility(View.GONE);
			mSearch_loading.setVisibility(View.GONE);
			mAttachLayout.setVisibility(View.GONE);
			mSearchResultListView.setVisibility(View.VISIBLE);
			break;
		}
	}


	@Override
	public void startDownloadApp(String pacName, String appName,
			String filePath, String md5, String url, String topicId, String type, int verCode,
			int appId, long totalSize) {
		try {
			addDownloadApk(pacName, appName, md5,url,ReportFlag.TOPIC_NULL,ReportFlag.FROM_SEARCH, verCode, appId, totalSize);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}


	@Override
	public void startIconAnimation(String pacName, int versionCode,
			Drawable drawable, int fromX, int fromY) {
		// TODO Auto-generated method stub

	}


	@Override
	protected void onDownloadServiceBind() {
		// TODO Auto-generated method stub

	}


	@Override
	protected void onApkDownloading(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub

	}


	@Override
	protected void onNoEnoughSpace(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
	    if(mSearchResultAdapter != null)
	        mSearchResultAdapter.notifyDataSetChanged();

	}


	@Override
	protected void onSdcardLost(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
	    if(mSearchResultAdapter != null)
	        mSearchResultAdapter.notifyDataSetChanged();

	}


	@Override
	protected void onDownloadHttpError(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
	    if(mSearchResultAdapter != null)
	        mSearchResultAdapter.notifyDataSetChanged();

	}


	@Override
	protected void onDownloadProgressUpdate(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub

	}


	@Override
	protected void onDownloadComplete(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
	    if(mSearchResultAdapter != null)
	        mSearchResultAdapter.notifyDataSetChanged();
	}


	@Override
	protected void onInstalling(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
	    if(mSearchResultAdapter != null)
            mSearchResultAdapter.notifyDataSetChanged();
	}


	@Override
	protected void onInstallSuccess(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
	    if(mSearchResultAdapter != null)
            mSearchResultAdapter.notifyDataSetChanged();
	}


	@Override
	protected void onInstallFailed(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub

	}


	@Override
	protected void onFileNotFound(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
	    if(mSearchResultAdapter != null)
	        mSearchResultAdapter.notifyDataSetChanged();

	}


	@Override
	protected void onFileNotUsable(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
	    if(mSearchResultAdapter != null)
	        mSearchResultAdapter.notifyDataSetChanged();

	}


	@Override
	public boolean downloadPause(String pkgName, int verCode) {
		try {
			return pauseDownloadApk(pkgName, verCode);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

}