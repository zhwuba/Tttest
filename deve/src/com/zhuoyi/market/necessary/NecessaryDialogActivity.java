package com.zhuoyi.market.necessary;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.market.download.baseActivity.DownloadTabBaseActivity;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.net.data.AppInfoBto;
import com.market.statistics.ReportFlag;
import com.market.view.AnimLayout;
import com.zhuoyi.market.R;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.necessary.NecessaryFirstInAdapter.ItemClickCallBack;
import com.zhuoyi.market.utils.MarketUtils;

public class NecessaryDialogActivity extends DownloadTabBaseActivity implements ItemClickCallBack, DownloadCallBackInterface{
	
	private AnimLayout mAnimLayout = null;
	private ListView mListView;
	private TextView mTextView_count;
	private TextView mTextView_size;
	private Button mButton_installAll;
	
	private ArrayList<AppInfoBto> mInstallAllList;
	private int mSelectNum = 0;
	public static boolean isSelect[] = new boolean[80];
	private NecessaryFirstInAdapter mNecessaryFirstInAdapter;
	private long mSelectFillSize;
	
	private int mTopicId = -1;
	
	private final int HANDLER_INSTALL_FINISH = 0;
	private final int HANDLER_FOR_FINISH = 1;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HANDLER_INSTALL_FINISH:
				Toast.makeText(getBaseContext(), getString(R.string.installed_necessary_toast, mSelectNum), Toast.LENGTH_SHORT).show();
				finish();
				break;
			case HANDLER_FOR_FINISH:
				finish();
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.necessary_dialog);
		findview();
		initview();
		startMoveLayout();
		
	}
	
	
	public void findview() {
		mTopicId = MarketUtils.getTopicId(0, 2);
		mAnimLayout = (AnimLayout) findViewById(R.id.anim_layout);
		mListView = (ListView) findViewById(R.id.move_list_view);
		mTextView_count = (TextView) findViewById(R.id.move_text2);
		mTextView_size = (TextView) findViewById(R.id.move_text4);
		mButton_installAll = (Button) findViewById(R.id.move_button);
	}
	
	
	public void initview() {
		setDialogParam();
		mAnimLayout.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mAnimLayout != null && mAnimLayout.getAnimFinish()) {
					mAnimLayout.setAndStartAnimById(R.anim.fade_bottom_out, true);
					mAnimLayout.setCloseActivity(NecessaryDialogActivity.this);
				}
				return true;
			}
		});
		mAnimLayout.setAndStartAnimById(R.anim.fade_bottom_in, true);
		mAnimLayout.setCloseActivity(null);
		mButton_installAll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mSelectNum == 0) {
					Toast.makeText(getBaseContext(), R.string.installed_necessary_toast2, Toast.LENGTH_SHORT).show();
				} else {
					startInsatll((ArrayList<AppInfoBto>) mInstallAllList.clone());
				}
			}
		});
	}
	
	
	private void startMoveLayout() {
		mInstallAllList = new ArrayList<AppInfoBto>();
		List<AppInfoBto> installList = NecessaryFirstInRecommend.getInstallAppList();
		if(installList == null)
			installList = NecessaryView.mInstallAppList;
		if(installList == null) {
			mHandler.sendEmptyMessage(HANDLER_FOR_FINISH);
			return;
		}
		int num = installList.size();
		AppInfoBto appInfo;
        for (int i = 0; i < num; i++) {
            appInfo = installList.get(i);
            if (appInfo.getIsShow() && MarketUtils.checkApkShouldShowInList(this, appInfo.getPackageName(), appInfo.getVersionCode())) {
                mInstallAllList.add(installList.get(i));
            }
        }
        mSelectNum = mInstallAllList.size();
        if(mSelectNum == 0) {
        	mHandler.sendEmptyMessage(HANDLER_FOR_FINISH);
			return;
        }
        
        ArrayList<AppInfoBto> layerData = new ArrayList<AppInfoBto>();
        List<List<AppInfoBto>> mProductsLayer = new ArrayList<List<AppInfoBto>>();
        for (int i = 0; mInstallAllList != null && i < mSelectNum; i++) {
            appInfo = mInstallAllList.get(i);
            layerData.add(appInfo);
            if (layerData.size() == 3) {
                mProductsLayer.add(layerData);
                layerData = new ArrayList<AppInfoBto>();
            }
        }
        if (layerData.size() > 0)
            mProductsLayer.add(layerData);

        mSelectFillSize = 0;
        for (int i = 0; i < mSelectNum; i++) {
            isSelect[i] = true;
            mSelectFillSize = mSelectFillSize + mInstallAllList.get(i).getFileSize();
        }
        mTextView_count.setText("" + mSelectNum);
        mTextView_size.setText(MarketUtils.humanReadableByteCount(mSelectFillSize, false));
        if (mNecessaryFirstInAdapter == null) {
            mNecessaryFirstInAdapter = new NecessaryFirstInAdapter(this, mProductsLayer, (ItemClickCallBack) this);
            mListView.setAdapter(mNecessaryFirstInAdapter);
        }
        mNecessaryFirstInAdapter.setMyList(mProductsLayer);
        mNecessaryFirstInAdapter.notifyDataSetChanged();
	}
	
	
	private void startInsatll(final ArrayList<AppInfoBto> allSelectedList) {
        new Thread() {
            public void run() {
                int num = allSelectedList.size();
                for (int i = 0; i < num; i++) {
                    if (isSelect[i]) {

                        String packageName = allSelectedList.get(i).getPackageName();
                        String appName = allSelectedList.get(i).getName();
                        String md5 = allSelectedList.get(i).getMd5();
                        String url = allSelectedList.get(i).getDownUrl();
                        int appId = allSelectedList.get(i).getRefId();
                        String type = ReportFlag.FROM_NULL;
                        long totalSize = allSelectedList.get(i).getFileSize();
                        try {
                            addDownloadApkWithoutNotify(packageName, appName, md5, url, Integer.toString(mTopicId),
                                type, allSelectedList.get(i).getVersionCode(), appId, totalSize);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Message msg = new Message();
                msg.what = HANDLER_INSTALL_FINISH;
                mHandler.sendMessageDelayed(msg, 700);
            }
        }.start();
    }
	
	
	private void setDialogParam() {
		Window window = getWindow();
		WindowManager.LayoutParams layoutParams = window.getAttributes();
		layoutParams.width = LayoutParams.FILL_PARENT;
		layoutParams.height = layoutParams.FILL_PARENT;
		layoutParams.gravity = Gravity.BOTTOM;
		window.setAttributes(layoutParams);
	}
	
	@Override
    public void onBackPressed() {
        if (mAnimLayout != null && mAnimLayout.getAnimFinish()) {
            mAnimLayout.setAndStartAnimById(R.anim.fade_bottom_out, true);
            mAnimLayout.setCloseActivity(this);
        }
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mAnimLayout != null) {
		    mAnimLayout.releaseRes();
		    mAnimLayout = null;
		}
		if (mNecessaryFirstInAdapter != null) {
            mNecessaryFirstInAdapter.freeImageCache();
            mNecessaryFirstInAdapter = null;
        }
		if (mInstallAllList != null) {
            mInstallAllList.clear();
        }
		if (mHandler != null) {
            mHandler = null;
        }
	}


	@Override
	public void startDownloadApp(String pacName, String appName,
			String filePath, String md5, String url, String topicId,
			String type, int verCode, int appId, long totalSize) {
		try {
            addDownloadApk(pacName, appName, md5, url, topicId, type, verCode, appId, totalSize);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
		
	}


	@Override
	public void startIconAnimation(String pacName, int versionCode,
			Drawable drawable, int fromX, int fromY) {
		
	}


	@Override
	public boolean downloadPause(String pkgName, int verCode) {
		return false;
	}


	@Override
	public void leftIetmClick(int position, ImageView select) {
		int which = position * 3;

        if (isSelect[which]) {
            isSelect[which] = false;
            select.setImageResource(R.drawable.onekey_install_unselect);

            mSelectFillSize = mSelectFillSize - mInstallAllList.get(which).getFileSize();
            mSelectNum--;

            mTextView_count.setText("" + mSelectNum);
            mTextView_size.setText(MarketUtils.humanReadableByteCount(mSelectFillSize, false));

        } else {
            isSelect[which] = true;
            select.setImageResource(R.drawable.onekey_install_select);

            mSelectFillSize = mSelectFillSize + mInstallAllList.get(which).getFileSize();
            mSelectNum++;

            mTextView_count.setText("" + mSelectNum);
            mTextView_size.setText(MarketUtils.humanReadableByteCount(mSelectFillSize, false));

        }
		
	}
	
	
	@Override
	public void middleIetmClick(int position, ImageView select) {
		int which = position * 3 + 1;

        if (isSelect[which]) {
            isSelect[which] = false;
            select.setImageResource(R.drawable.onekey_install_unselect);

            mSelectFillSize = mSelectFillSize - mInstallAllList.get(which).getFileSize();
            mSelectNum--;

            mTextView_count.setText("" + mSelectNum);
            mTextView_size.setText(MarketUtils.humanReadableByteCount(mSelectFillSize, false));

        } else {
            isSelect[which] = true;
            select.setImageResource(R.drawable.onekey_install_select);

            mSelectFillSize = mSelectFillSize + mInstallAllList.get(which).getFileSize();
            mSelectNum++;

            mTextView_count.setText("" + mSelectNum);
            mTextView_size.setText(MarketUtils.humanReadableByteCount(mSelectFillSize, false));

        }
		
	}


	@Override
	public void rightIetmClick(int position, ImageView select) {
		int which = position * 3 + 2;

        if (isSelect[which]) {
            isSelect[which] = false;
            select.setImageResource(R.drawable.onekey_install_unselect);

            mSelectFillSize = mSelectFillSize - mInstallAllList.get(which).getFileSize();
            mSelectNum--;

            mTextView_count.setText("" + mSelectNum);
            mTextView_size.setText(MarketUtils.humanReadableByteCount(mSelectFillSize, false));

        } else {
            isSelect[which] = true;
            select.setImageResource(R.drawable.onekey_install_select);

            mSelectFillSize = mSelectFillSize + mInstallAllList.get(which).getFileSize();
            mSelectNum++;

            mTextView_count.setText("" + mSelectNum);
            mTextView_size.setText(MarketUtils.humanReadableByteCount(mSelectFillSize, false));

        }
		
	}


	@Override
	protected void onDownloadServiceBind() {
		
	}


	@Override
	protected void onApkDownloading(DownloadEventInfo eventInfo) {
		
	}


	@Override
	protected void onNoEnoughSpace(DownloadEventInfo eventInfo) {
		
	}


	@Override
	protected void onSdcardLost(DownloadEventInfo eventInfo) {
		
	}


	@Override
	protected void onDownloadHttpError(DownloadEventInfo eventInfo) {
		
	}


	@Override
	protected void onDownloadProgressUpdate(DownloadEventInfo eventInfo) {
		
	}


	@Override
	protected void onDownloadComplete(DownloadEventInfo eventInfo) {
		
	}


	@Override
	protected void onInstalling(DownloadEventInfo eventInfo) {
		
	}


	@Override
	protected void onInstallSuccess(DownloadEventInfo eventInfo) {
		
	}


	@Override
	protected void onInstallFailed(DownloadEventInfo eventInfo) {
		
	}


	@Override
	protected void onFileNotFound(DownloadEventInfo eventInfo) {
		
	}


	@Override
	protected void onFileNotUsable(DownloadEventInfo eventInfo) {
		
	}
	
}
