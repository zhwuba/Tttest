package com.zhuoyi.market.setting;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.zhuoyi.market.R;
import com.zhuoyi.market.setting.EasyCheckAdapter;
import com.zhuoyi.market.constant.Constant;
import com.market.statistics.ReportFlag;
import com.market.view.LoadingProgressDialog;
import com.market.download.baseActivity.DownloadBaseActivity;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.userDownload.DownloadManager;
import com.market.net.data.AppInfoBto;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.request.GetAppsUpdateReq;
import com.market.net.response.GetAppsUpdateResp;
import com.market.net.utils.StartNetReqUtils;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.utils.MarketUtils.FileManage;

public class EasyCheckActivity extends DownloadBaseActivity implements OnClickListener
{
    // public static EasyCheckActivity this_;

    private Button mCheckButton;

    private LoadingProgressDialog mProgressDialog;

    private LinearLayout mContent;

    private LinearLayout mListContent;

    private ListView mListView;

    private EasyCheckAdapter mAdapter;

    private View mListHeader;

    private List<AppInfoBto> mAppInfoList = null;

    private static Handler mHandler_EasyCheck;

    public static final int START_CHECK = 1;

    public static final int UPDATE_APK = 2;

    public static final int UPDATE_APK_ERROR = 3;

    public static final int UPDATE_APK_DONE = 4;

    public static final int UPDATE_LIST_VIEW = 5;

    private String mRemovedPackageName = null;
    
    private boolean isChecking = false;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
    	MarketUtils.setSatusBarTranslucent(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_yichabao);

        mAppInfoList = new ArrayList<AppInfoBto>();

        findView();

		mHandler_EasyCheck = new Handler() {
			public void handleMessage(Message msg) {
				int index = 0;
				switch (msg.what) {
                case START_CHECK:
                    if (mProgressDialog != null)
                        mProgressDialog.show();
                    break;
                case UPDATE_APK:
                    index =(Integer)msg.obj;
                    updateApk(index);
                    break;
                case UPDATE_APK_ERROR:
                    Toast.makeText(EasyCheckActivity.this, R.string.update_apk_error, Toast.LENGTH_SHORT).show();
                    break;
                case UPDATE_APK_DONE:
                    //Toast.makeText(EasyCheckActivity.this, R.string.update_apk_success, Toast.LENGTH_SHORT).show();
                    index =(Integer)msg.obj;
                    mAppInfoList.remove(index);
                    mAdapter.refreshList(mAppInfoList);
                    break;
                case UPDATE_LIST_VIEW:
                	if(!isChecking)
                		return;
                	isChecking = false;
                	if (mProgressDialog != null && mProgressDialog.isShowing()) {
                	    mProgressDialog.dismiss();
                	}
                    List<AppInfoBto> appInfoList;

                    if (mAppInfoList != null && mAppInfoList.size() > 0)
                        mAppInfoList.clear();

                    Map<String, Object> map = (Map<String, Object>) msg.obj;
                    GetAppsUpdateResp resp = null;
                    if (map != null && map.size() > 0) {
                        resp = (GetAppsUpdateResp) map.get("appsUpdate");
						if (resp != null) {
							appInfoList = resp.getAppList();
							if (appInfoList != null && appInfoList.size() > 0) {
								PackageManager pm = getBaseContext().getPackageManager();
								for (AppInfoBto appInfo : appInfoList) {
									try {
                                        PackageInfo pInfo = pm.getPackageInfo(appInfo.getPackageName(), PackageManager.GET_META_DATA);
                                        appInfo.setDrawable(pInfo.applicationInfo.loadIcon(pm));
                                        appInfo.setName(pInfo.applicationInfo.loadLabel(pm).toString());
                                        appInfo.setFileSizeString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(), false));

									} catch (NameNotFoundException e) {
									}
                                    mAppInfoList.add(appInfo);
                                }
                            }
                        }
                    }

                    if ( false && resp != null && mAppInfoList != null && mAppInfoList.size() > 0) {
                        ShowList(true);

                        mAdapter = new EasyCheckAdapter(EasyCheckActivity.this, mAppInfoList);
                        mListView.addHeaderView(mListHeader);
                        mListView.setAdapter(mAdapter);
					} else {
                        ShowList(false);

                        if (resp != null && resp.getErrorCode() == 0)
                            Toast.makeText(EasyCheckActivity.this, R.string.update_apk_hint_your_device_clean, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(EasyCheckActivity.this, R.string.update_apk_hint_net_error, Toast.LENGTH_SHORT).show();
                    }

                    break;
                default:
                    break;
                }
            }
        };
        registerUninstallAction();
        
        DownloadManager.startServiceReportOffLineLog(getApplicationContext(), ReportFlag.ACTION_VIEW_COLUMN, ReportFlag.FROM_YICHABAO);
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		unRegisterUninstallAction();

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

    
	private void registerUninstallAction() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addDataScheme("package");
		registerReceiver(MyInstalledReceiver, filter);
	}
	

	private void unRegisterUninstallAction() {
		if (MyInstalledReceiver != null) {
			unregisterReceiver(MyInstalledReceiver);
		}
	}
    
	
	private BroadcastReceiver MyInstalledReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
				String packageName = intent.getDataString();
				mRemovedPackageName = packageName;
			}
		}
	};

	private void findView() {
        mCheckButton = (Button) findViewById(R.id.check_button);
        mCheckButton.setOnClickListener(this);

        mContent = (LinearLayout) findViewById(R.id.ycb_content);
        mListContent = (LinearLayout) findViewById(R.id.ycb_list_content);
        mListView = (ListView) findViewById(R.id.ycb_list);
        ShowList(false);

        mListHeader = View.inflate(this, R.layout.ycb_list_header, null);

        mProgressDialog = new LoadingProgressDialog(this);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage(getString(R.string.update_apk_hint_connecting));
        
        mProgressDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				isChecking = false;
				Toast.makeText(EasyCheckActivity.this, getString(R.string.cancel_check), Toast.LENGTH_SHORT).show();
			}
		});
        
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.check_button:
			if(!mProgressDialog.isShowing())
	        	mHandler_EasyCheck.obtainMessage(START_CHECK).sendToTarget();
			if (!isChecking) {
				isChecking = true;
				new Thread(new Runnable() {

					@Override
					public void run() {
						startCheck();
					}
				}).start();
			}
			break;
		}
	}
	
	
	private void startCheck() {
        String contents =  "";
        if(!mProgressDialog.isShowing())
        	mHandler_EasyCheck.obtainMessage(START_CHECK).sendToTarget();

        GetAppsUpdateReq appUpdateReq = new GetAppsUpdateReq();
        contents = SenderDataProvider.buildToJSONData(getApplicationContext(),MessageCode.CHECK_APP_VALID,appUpdateReq);
        StartNetReqUtils.execListByPageRequest(mHandler_EasyCheck, UPDATE_LIST_VIEW, MessageCode.CHECK_APP_VALID,contents);
    }

	private void ShowList(boolean show) {
		if (show) {
			mContent.setVisibility(View.GONE);
			mListContent.setVisibility(View.VISIBLE);
		} else {
			mContent.setVisibility(View.VISIBLE);
			mListContent.setVisibility(View.GONE);
		}
	}

	private void updateApk(int index) {
        String packageName = mAppInfoList.get(index).getPackageName();

        boolean success = MarketUtils.unInstallSilent(packageName);
		if (!success) {
            Uri packageURI = Uri.parse("package:" + packageName);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
            startActivity(uninstallIntent);
		} else {
            download(index);
        }
    }

	private void download(int index) {
        AppInfoBto appInfo = mAppInfoList.get(index);

        String packageName = appInfo.getPackageName();
        String appName = appInfo.getName();
        String downloadUrl = appInfo.getDownUrl();

		try {
			if (addDownloadApk(packageName, appName, appInfo.getMd5(), downloadUrl, ReportFlag.TOPIC_NULL, ReportFlag.FROM_YICHABAO, appInfo.getVersionCode(), appInfo.getRefId(), appInfo.getFileSize())) {
				sendToTarget(UPDATE_APK_DONE, index);
			} else {
				sendToTarget(UPDATE_APK_ERROR, index);
			}
		} catch (RemoteException e) {
            sendToTarget(UPDATE_APK_ERROR, index);
            e.printStackTrace();
        }
    }

	
	public static void sendToTarget(int msg_what, int index) {
        mHandler_EasyCheck.obtainMessage(msg_what, index).sendToTarget();
    }

	
	@Override
	protected void onDownloadServiceBind() {
		if (mRemovedPackageName != null) {
			int index = -1;
			for (int i = 0; i < mAppInfoList.size(); i++) {
				AppInfoBto info = mAppInfoList.get(i);
				if (mRemovedPackageName.contains(info.getPackageName())) {
					index = i;
					break;
				}
			}

            mRemovedPackageName = null;

			if (index == -1) {
                sendToTarget(UPDATE_APK_ERROR, index);
                return;
            }

            download(index);
        }
    }

    @Override
    protected void onApkDownloading(DownloadEventInfo eventInfo)
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onNoEnoughSpace(DownloadEventInfo eventInfo)
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onSdcardLost(DownloadEventInfo eventInfo)
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onDownloadHttpError(DownloadEventInfo eventInfo)
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onDownloadProgressUpdate(DownloadEventInfo eventInfo)
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onDownloadComplete(DownloadEventInfo eventInfo)
    {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onInstalling(DownloadEventInfo eventInfo)
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onInstallSuccess(DownloadEventInfo eventInfo)
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onInstallFailed(DownloadEventInfo eventInfo)
    {
        // TODO Auto-generated method stub

    }

	@Override
	protected void onFileNotFound(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onFileNotUsable(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
		
	}
}
