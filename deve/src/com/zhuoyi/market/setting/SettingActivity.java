package com.zhuoyi.market.setting;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zhuoyi.market.R;
import com.zhuoyi.market.setting.SettingExpandableAdapter;
import com.zhuoyi.market.constant.Constant;
import com.market.view.LoadingProgressDialog;
import com.market.view.CommonLoadingManager;
import com.market.behaviorLog.LogDefined;
import com.market.behaviorLog.UserLogSDK;
import com.market.updateSelf.SelfUpdateInfo;
import com.market.updateSelf.SelfUpdateManager;
import com.market.updateSelf.SelfUpdateManager.SelfUpdateInterface;
import com.market.updateSelf.UpSelfStorage;
import com.zhuoyi.market.utils.MarketUtils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

public class SettingActivity extends Activity implements SelfUpdateInterface {
	
	private long mCacheSize = 0L;
	private long mClearSize = 0L;

	private LoadingProgressDialog mProgressDialog = null;
	private SettingExpandableAdapter mAdapter = null;
	private ExpandableListView mExpandableListView = null;
	private List<Map<String, Object>> groupData = new ArrayList<Map<String,Object>>();
	private List<List<Map<String, Object>>> childData = new ArrayList<List<Map<String,Object>>>();
	
	private static final int HANDLE_CLEAR_CACHE_SHOW = 0;
	private static final int HANDLE_CLEAR_CACHE = 1;
	private static final int HANDLE_UPDATE_CHECK_SIZE = 2;
	
	//指向清除缓存item
	private static final int SETTING_GROUP_SETTING = 1;
	private static final int SETTING_CHILD_CACHE = 4;
	
	//指向自更新、分享、易查宝、关于
	private static final int SETTING_GROUP_HELP = 2;
	private static final int SETTING_CHILD_SEL_UPDATE = 0;
	private static final int SETTING_CHILD_SHARE = 1;
//	private static final int SETTING_CHILD_EASY_CHECK = 2;
	private static final int SETTING_CHILD_ABOUT = 1;
	
	//自更新
	private SelfUpdateManager mSelfUpdateManager = null;
	private boolean mLocalData = false;

	public Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_CLEAR_CACHE_SHOW:
				if(mProgressDialog != null && mProgressDialog.isShowing())
					mProgressDialog.cancel();
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.delete_cache_size_tip, msg.obj), Toast.LENGTH_SHORT).show();
				childData.get(SETTING_GROUP_SETTING).get(SETTING_CHILD_CACHE).put(SettingExpandableAdapter.SETTING_ITEM_NAME_TEXT, "0.00MB");
				mAdapter.notifyDataSetChanged();
				break;
			case HANDLE_CLEAR_CACHE:
				mProgressDialog.setMessage(getResources().getString(R.string.waitting_clear_cache));
				mProgressDialog.show();
				deleteCache();
				break;
			case HANDLE_UPDATE_CHECK_SIZE:
				String cache = (String) msg.obj;
				childData.get(SETTING_GROUP_SETTING).get(SETTING_CHILD_CACHE).put(SettingExpandableAdapter.SETTING_ITEM_NAME_TEXT, cache);
				mAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_setting_new);
		
		mSelfUpdateManager = new SelfUpdateManager(this);
		mSelfUpdateManager.setSelfUpdateInterface(this);
		mSelfUpdateManager.setMustUpdate2TipUpdate(true);
		
		SelfUpdateInfo updateInfo = UpSelfStorage.getSelfUpdateInfo(getApplicationContext());
		if (updateInfo == null) {
		    //检查市场版本是不是需要更新
		    mLocalData = false;
		    mSelfUpdateManager.setNewestTip(false);
		    mSelfUpdateManager.setTipType(SelfUpdateManager.SELF_UPDATE_TIP_TYPE_N);
		    mSelfUpdateManager.selfUpdateRequest(SelfUpdateManager.SELF_UPDATE_REQ_FROM_SETTING_IN);
		} else {
		    //已经检查到需要更新
		    mLocalData = true;
		    mSelfUpdateManager.setLocalData(updateInfo.getTitle(), 
		            updateInfo.getContent(), 
		            updateInfo.getUpdateType(),
		            updateInfo.getVersionCode(),
		            updateInfo.getDownloadUrl(),
		            updateInfo.getMd5());
		}

		//计算缓存大小
		new Thread() {
			
			@Override
			public void run() {
				Message msg = new Message();
				msg.what = HANDLE_UPDATE_CHECK_SIZE;
				msg.obj = getCacheSize();
				mHandler.sendMessage(msg);
			}
		}.start();
		
		mProgressDialog = new LoadingProgressDialog(this);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(true);

		initData();
		
		mExpandableListView = (ExpandableListView) findViewById(R.id.list);
		mAdapter = new SettingExpandableAdapter(getApplicationContext(), childData, groupData, mExpandableListView);
		mExpandableListView.setAdapter(mAdapter);
		mExpandableListView.setGroupIndicator(null);//设置默认的组展开图片不显示
		mExpandableListView.setDivider(null);  //设置分隔线不显示
		mExpandableListView.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				if(groupPosition == SETTING_GROUP_SETTING && childPosition == SETTING_CHILD_CACHE) {
					MyDialog myDialog = new MyDialog(SettingActivity.this,R.style.MyMarketDialog, mHandler, HANDLE_CLEAR_CACHE);
					myDialog.show();
				} else if (groupPosition == SETTING_GROUP_HELP && childPosition == SETTING_CHILD_SEL_UPDATE) {
				    
                    mSelfUpdateManager.setNewestTip(true);
                    mSelfUpdateManager.setTipType(SelfUpdateManager.SELF_UPDATE_TIP_TYPE_0);
                    
					if (mSelfUpdateManager.getChecking()) {
					    mProgressDialog.setMessage(getResources().getString(R.string.waiting_for_newversion));
                        mProgressDialog.show();
					} else {
	                    String update = (String) childData.get(SETTING_GROUP_HELP).get(SETTING_CHILD_SEL_UPDATE).get(SettingExpandableAdapter.SETTING_ITEM_UPDATE_SHOW);
	                    if (!TextUtils.isEmpty(update)) {
	                        mSelfUpdateManager.showUpdateTip();
	                    } else {
	                        mProgressDialog.setMessage(getResources().getString(R.string.waiting_for_newversion));
	                        mProgressDialog.show();
	                        mSelfUpdateManager.selfUpdateRequest(SelfUpdateManager.SELF_UPDATE_REQ_FROM_SETTING_USER);
	                    }
					}
				} 
//				else if (groupPosition == SETTING_GROUP_HELP && childPosition == SETTING_CHILD_SHARE) {
//			        Intent intent = new Intent(getApplication(), MarketFeedbackActivity.class);
//			        startActivity(intent);
//                } 
				else if (groupPosition == SETTING_GROUP_HELP && childPosition == SETTING_CHILD_ABOUT) {
					Intent intent = new Intent(SettingActivity.this, About.class);
					startActivity(intent);
				}
				return false;
			}
		});
		
	}

	
	@Override
	protected void onResume() {
		CommonLoadingManager.get().showLoadingAnimation(this);
		super.onResume();
		
		//for record user behavior log
		UserLogSDK.logCountEvent(this, UserLogSDK.getKeyDes(LogDefined.COUNT_SETTING_VIEW));
	}
	
    
    @Override
    protected void onDestroy() {
        mSelfUpdateManager.releaseRes();
        mSelfUpdateManager = null;
        super.onDestroy();
    }
	
	
	/**
	 * 初始化数据
	 */
	private void initData() {
		Map<String, Object> map = null;
		List<Map<String, Object>> list = null;
		
		//父item数据
		map = new HashMap<String, Object>();
		map.put("classify", getString(R.string.save_flow_setting));
		groupData.add(map);
		map = new HashMap<String, Object>();
		map.put("classify", getString(R.string.application_setting));
		groupData.add(map);
		map = new HashMap<String, Object>();
		map.put("classify", getString(R.string.help));
		groupData.add(map);
		
		//子item数据
		list = new ArrayList<Map<String,Object>>();
		//智能无图模式、零流量更新
		map = new HashMap<String, Object>();
	    map.put(SettingExpandableAdapter.SETTING_ITEM_TYPE, SettingExpandableAdapter.SETTING_ITEM_TYPE_BTN);
	    map.put(SettingExpandableAdapter.SETTING_BTN_FLAG, SettingExpandableAdapter.SETTING_BTN_FLAG_SAVE);
	    map.put(SettingExpandableAdapter.SETTING_ITEM_NAME, getString(R.string.save_flow_mode));
	    map.put(SettingExpandableAdapter.SETTING_ITEM_NAME_SMALL, getString(R.string.without_load_new_pic));
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put(SettingExpandableAdapter.SETTING_ITEM_TYPE, SettingExpandableAdapter.SETTING_ITEM_TYPE_BTN);
        map.put(SettingExpandableAdapter.SETTING_BTN_FLAG, SettingExpandableAdapter.SETTING_BTN_FLAG_UPDATE);
        map.put(SettingExpandableAdapter.SETTING_ITEM_NAME, getString(R.string.setting_auto_update_content));
        map.put(SettingExpandableAdapter.SETTING_ITEM_NAME_SMALL, getString(R.string.power_tip));
		list.add(map);
		
		//稍后下载
//		map = new HashMap<String, Object>();
//        map.put(SettingExpandableAdapter.SETTING_ITEM_TYPE, SettingExpandableAdapter.SETTING_ITEM_TYPE_BTN);
//        map.put(SettingExpandableAdapter.SETTING_BTN_FLAG, SettingExpandableAdapter.SETTING_BTN_FLAG_DELAY);
//        map.put(SettingExpandableAdapter.SETTING_ITEM_NAME, getString(R.string.setting_delay_down_title));
//        map.put(SettingExpandableAdapter.SETTING_ITEM_NAME_SMALL, getString(R.string.setting_delay_down_content));
//        list.add(map);
		
		childData.add(list);
		
		list = new ArrayList<Map<String,Object>>();
		//允许推送更新提示、安装后自动删除安装包、下载路径、最多下载任务、清除缓存
		map = new HashMap<String, Object>();
		map.put(SettingExpandableAdapter.SETTING_ITEM_TYPE, SettingExpandableAdapter.SETTING_ITEM_TYPE_BTN);
		map.put(SettingExpandableAdapter.SETTING_BTN_FLAG, SettingExpandableAdapter.SETTING_BTN_FLAG_PUSH);
		map.put(SettingExpandableAdapter.SETTING_ITEM_NAME, getString(R.string.allow_update_tip));
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put(SettingExpandableAdapter.SETTING_ITEM_TYPE, SettingExpandableAdapter.SETTING_ITEM_TYPE_BTN);
		map.put(SettingExpandableAdapter.SETTING_BTN_FLAG, SettingExpandableAdapter.SETTING_BTN_FLAG_DEL);
		map.put(SettingExpandableAdapter.SETTING_ITEM_NAME, getString(R.string.auto_deletepackage_afterinstall));
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put(SettingExpandableAdapter.SETTING_ITEM_TYPE, SettingExpandableAdapter.SETTING_ITEM_TYPE_TEXT);
		map.put(SettingExpandableAdapter.SETTING_ITEM_NAME_TEXT, Constant.download_dir_name);
		map.put(SettingExpandableAdapter.SETTING_ITEM_NAME, getString(R.string.downloaded_path));
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put(SettingExpandableAdapter.SETTING_ITEM_TYPE, SettingExpandableAdapter.SETTING_ITEM_TYPE_MULTI_BTN);
		map.put(SettingExpandableAdapter.SETTING_ITEM_NAME, getString(R.string.download_task_max_num_new));
		list.add(map);
		
		map = new HashMap<String, Object>();
		map.put(SettingExpandableAdapter.SETTING_ITEM_TYPE, SettingExpandableAdapter.SETTING_ITEM_TYPE_TEXT);
		map.put(SettingExpandableAdapter.SETTING_ITEM_NAME, getString(R.string.clear_cache_tile));
		map.put(SettingExpandableAdapter.SETTING_ITEM_NAME_TEXT, getString(R.string.calculate_cache));
		list.add(map);
		childData.add(list);
		
		list = new ArrayList<Map<String,Object>>();
		//检查更新、分享、易查宝、关于
		map = new HashMap<String, Object>();
		if (mLocalData) {
		    map.put(SettingExpandableAdapter.SETTING_ITEM_UPDATE_SHOW, getString(R.string.update));
		}
		map.put(SettingExpandableAdapter.SETTING_ITEM_TYPE, SettingExpandableAdapter.SETTING_ITEM_TYPE_IMAGE);
		map.put(SettingExpandableAdapter.SETTING_ITEM_IMAGE_ID, R.drawable.novel_arrow);
		map.put(SettingExpandableAdapter.SETTING_ITEM_NAME, getString(R.string.check_update));
		list.add(map);
		
//        map = new HashMap<String, Object>();
//        map.put(SettingExpandableAdapter.SETTING_ITEM_TYPE, SettingExpandableAdapter.SETTING_ITEM_TYPE_IMAGE);
//        map.put(SettingExpandableAdapter.SETTING_ITEM_IMAGE_ID, R.drawable.novel_arrow);
//        map.put(SettingExpandableAdapter.SETTING_ITEM_NAME, getString(R.string.myself_onekey_feedback));
//        list.add(map);
//        
//        map = new HashMap<String, Object>();
//        map.put(SettingExpandableAdapter.SETTING_ITEM_TYPE, SettingExpandableAdapter.SETTING_ITEM_TYPE_IMAGE);
//        map.put(SettingExpandableAdapter.SETTING_ITEM_IMAGE_ID, R.drawable.novel_arrow);
//        map.put(SettingExpandableAdapter.SETTING_ITEM_NAME, getString(R.string.easy_check));
//        list.add(map);
		
		map = new HashMap<String, Object>();
		map.put(SettingExpandableAdapter.SETTING_ITEM_TYPE, SettingExpandableAdapter.SETTING_ITEM_TYPE_IMAGE);
        map.put(SettingExpandableAdapter.SETTING_ITEM_IMAGE_ID, R.drawable.novel_arrow);
        map.put(SettingExpandableAdapter.SETTING_ITEM_NAME, getString(R.string.about));
		list.add(map);
		childData.add(list);
	}
	
	
	/**
	 * 获取缓存大小
	 * @return
	 */
	public String getCacheSize(){
		String sdPath = MarketUtils.FileManage.getSDPath();
		String myPath;
		File myFile;	
		
		myPath = sdPath + Constant.config_path;
		if (TextUtils.isEmpty(sdPath))
			return "0.00MB";
		myFile = new File(myPath);
		if(!myFile.exists())
			return "0.00MB";
		
		mCacheSize = 0L;
		long size = calcuMyPathCacheFile(myFile);

		return String.format("%.2f", (float)size/1024/1024) + "MB";
	}
	
	
	 /**
     * 计算文件大小
     * @param root
     */
    public long calcuMyPathCacheFile(File root) {
        File files[] = root.listFiles();

        if (files != null) 
            for (File f : files) 
                if (f.isDirectory()) 
                    calcuMyPathCacheFile(f);
                else 
                    mCacheSize += f.length();
        
        return mCacheSize;
    }
	
	/**
	 * 删除cache
	 */
	private void deleteCache() {
		String sdPath = MarketUtils.FileManage.getSDPath();
		String myPath;
		final File myFile;
		
		myPath = sdPath + Constant.config_path;
		
		if (TextUtils.isEmpty(sdPath)) {
			if(mProgressDialog != null && mProgressDialog.isShowing())
				mProgressDialog.cancel();
			return;
		}
		myFile = new File(myPath);
		if (!myFile.exists()) {
			myFile.mkdir();
			new File(myPath + "image/").mkdir();
			new File(myPath + "marketframe/").mkdir();
			sendMessageForUpdatePage("0.00");
		} else {
			new Thread(){
				public void run() {
				    List<String> list = getDirectory(myFile);
				    mClearSize = 0L;
					sendMessageForUpdatePage(String.format("%.2f", (float)deleteMyPathCacheFile(myFile) / 1024 / 1024));
					makeNewCache(MarketUtils.FileManage.getSDPath(), list);
				};
			}.start();
		}
	}
	
	/**
	 * 删除并计算删除大小
	 * @param root
	 * @return
	 */
	public Long deleteMyPathCacheFile(File root) {
		File files[] = root.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
				    deleteMyPathCacheFile(f);
				} else {
				    mClearSize += f.length();
					f.delete();	
				}
			}
			root.delete();
		}
		return mClearSize;
	}
	
	/**
	 * 更新显示
	 * @param size
	 */
	public void sendMessageForUpdatePage(String size){
		Message msg = new Message();
		msg.what = HANDLE_CLEAR_CACHE_SHOW;
		msg.obj = size;
		mHandler.sendMessage(msg);
	}
	
	/**
	 * 获取已有路径
	 * @param file
	 * @return
	 */
	private List<String> getDirectory(File file) {
		List<String> list = new ArrayList<String>();
		File[] f;
		if (file != null && (f = file.listFiles()) != null)
			for (File ff : f)
				if (ff.isDirectory())
					list.add(ff.getAbsolutePath());
		return list;
	}
	
	/**
	 * 创建已有路径
	 * @param sdPath
	 * @param list
	 */
	public void makeNewCache(String sdPath, List<String> list){
		File file = new File(sdPath + Constant.download_path+"download/cache/");
		if(!file.exists()){
			file.mkdir();
			for(String path:list){
				file = new File(path);
				file.mkdir();
			}
		}
	}
	
	
	/**
	 * 缓存删除提示
	 * @author nanNan.zhang
	 *
	 */
    class MyDialog extends Dialog {
        
        private Handler mMyHandler = null; 
        private int mMsgWhat = -1;
        
        public MyDialog(Context context, int theme, Handler handler, int msgWhat) {
            super(context, theme);
            // TODO Auto-generated constructor stub
            mMyHandler = handler;
            mMsgWhat = msgWhat;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.market_dialog);
            
            ((TextView) findViewById(R.id.dialog_title)).setText(R.string.clear_cache_tile);
            ((TextView) findViewById(R.id.tip_text)).setText(R.string.clear_cache_content);
            Button btnOk = (Button) findViewById(R.id.tip_dialog_ok_button);
            Button btnCancel = (Button) findViewById(R.id.tip_dialog_cancel_button);
            
            btnOk.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msg = new Message();
                    msg.what = mMsgWhat;
                    mMyHandler.sendMessage(msg);
                    dismiss();
                    mMyHandler = null;
                }
            });
            
            btnCancel.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    mMyHandler = null;
                }
            });
        }
    }


    @Override
    public void updateSelf(int type) {
        // TODO Auto-generated method stub
        
        if(mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.cancel();
        
        if(type == SelfUpdateManager.SELF_UPDATE_TYPE_1
                || type == SelfUpdateManager.SELF_UPDATE_TYPE_2
                || type == SelfUpdateManager.SELF_UPDATE_TYPE_4) {
            childData.get(SETTING_GROUP_HELP).get(SETTING_CHILD_SEL_UPDATE).put(SettingExpandableAdapter.SETTING_ITEM_UPDATE_SHOW, getString(R.string.update));
            mAdapter.notifyDataSetChanged();
        }
        
        //特殊处理，用户手动检查更新，如果是后台更新提示用户
        if(type == SelfUpdateManager.SELF_UPDATE_TYPE_4) {
            mSelfUpdateManager.showUpdateTip();
        }
    }
}
