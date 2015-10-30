package com.freeme.themeclub.updateself;

import java.io.File;

import android.app.AlertDialog;
import android.app.IntentService;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;
import android.widget.Toast;

import com.freeme.themeclub.R;
import com.freeme.themeclub.updateself.HttpManager.NewUpdateInfo;
import com.freeme.themeclub.updateself.UpdateManager.DownloadRes;
import com.freeme.themeclub.updateself.UpdateManager.QueryRes;

public class UpdateSelfService extends IntentService {

    public static final String TAG = "updateService";
    public static final String START_FLAG = "startFlag";
    private UpdateManager mDownManager;
    private Thread mQueryThread;
    private Thread mDownThread;

    private AlertDialog mUpdateAlertDialog = null;

    public UpdateSelfService() {
        super("UpdateSelfService");
        // TODO Auto-generated constructor stub
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDownManager = UpdateManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        if (intent != null) {
            int startFlag = intent.getIntExtra(START_FLAG, -1);
            if (startFlag != -1) {
                if (mHandler.hasMessages(startFlag)) {
                    mHandler.removeMessages(startFlag);
                }
                mHandler.sendEmptyMessage(startFlag);
            }
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        DownloadInfo downInfo = Util.getDownloadInfo(this);
        if (downInfo != null
                && downInfo.state == DownloadInfo.STATE_DOWNLOADING) {
            downInfo.downloadPaused();
            Util.saveDownloadInfo(UpdateSelfService.this, downInfo);
        }
        super.onDestroy();
    }

    public static final int MSG_QUERY_UPDATE = 1;
    public static final int MSG_RESUME_DOWNLOAD = 2;
    public static final int MSG_START_DOWNLOAD = 3;
    public static final int MSG_IGNORE_DATA_TRAFFIC = 4;
    private static final int MSG_UPDATE_DIALOG = 5;
    private static final int MSG_DOWNLOAD_DIALOG = 6;
    private static final int MSG_STOP_SELF = 7;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Util.logI(TAG, "handleMessage", "msg what is:" + msg.what);
            switch (msg.what) {
            case MSG_QUERY_UPDATE:
                queryUpdate();
                break;

            case MSG_RESUME_DOWNLOAD:
                resumeDownNewUpdate();
                break;

            case MSG_START_DOWNLOAD:
                NewUpdateInfo info = Util.getNewInfo(UpdateSelfService.this);
                if (info == null)
                    break;
                if (info.policy == NewUpdateInfo.TYPE_UPDATE_BG) {
                    startDownNewUpdate(info, true);
                } else {
                    startDownNewUpdate(info, false);
                }
                break;
            case MSG_IGNORE_DATA_TRAFFIC:
                DownloadInfo downInfo = Util
                        .getDownloadInfo(UpdateSelfService.this);
                downloadIgnoreDataTraffic(downInfo);
                break;
            case MSG_UPDATE_DIALOG:
                // UpdateSelfService.this.showAlertDialog(Util.getNewInfo(UpdateSelfService.this));
                Intent intent = new Intent(UpdateSelfService.this,
                        UpdateDialogActivity.class);
                startActivitySafely(UpdateSelfService.this, intent,
                        "UpdateDialogActivity");
                break;
            case MSG_DOWNLOAD_DIALOG:
                UpdateSelfService.this.showAlertDialog(Util
                        .getDownloadInfo(UpdateSelfService.this));
                break;
            case MSG_STOP_SELF:
                UpdateSelfService.this.stopSelf();
                break;
            default:
                break;
            }
        }
    };

    public static boolean startActivitySafely(Context context, Intent intent,
            Object tag) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
            return true;
        } catch (SecurityException e) {
            Toast.makeText(context, R.string.activity_not_found,
                    Toast.LENGTH_SHORT).show();
            Log.w(TAG,
                    "Launcher does not have the permission to launch "
                            + intent
                            + ". Make sure to create a MAIN intent-filter for the corresponding activity "
                            + "or use the exported attribute for this activity. "
                            + "tag=" + tag + " intent=" + intent, e);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.activity_not_found,
                    Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
        }
        return false;
    }

    private void queryUpdate() {
        if (mQueryThread != null && mQueryThread.isAlive()) {
            Util.logE(TAG, "onReceive", "queryUpdate still alive!");
            return;
        }

        mQueryThread = new Thread() {
            @Override
            public void run() {
                Util.logE(TAG, "onReceive", "mQueryThread start!");
                int result = mDownManager.queryUpdate();
                Util.logE(TAG, "onReceive", "mQueryThread result = " + result);
                if (result == QueryRes.FOUND_NEW) {
                    NewUpdateInfo info = Util
                            .getNewInfo(UpdateSelfService.this);
                    int resultInt = deleteUnuselessFile(info);
                    if (resultInt > 0) {
                        if (resultInt == 2)
                            resumeDownNewUpdate();
                        return;
                    }
                    Util.clearDownloadInfo(UpdateSelfService.this);
                    if (info != null) {
                        if (info.policy == NewUpdateInfo.TYPE_FORCE_UPDATE) {
                            mHandler.removeMessages(MSG_UPDATE_DIALOG);
                            mHandler.obtainMessage(MSG_UPDATE_DIALOG)
                                    .sendToTarget();

                        } else if (info.policy == NewUpdateInfo.TYPE_DIALOG_NEW) {
                            mHandler.removeMessages(MSG_UPDATE_DIALOG);
                            mHandler.obtainMessage(MSG_UPDATE_DIALOG)
                                    .sendToTarget();

                        } else if (info.policy == NewUpdateInfo.TYPE_UPDATE_BG) {
                            startDownNewUpdate(info, true);
                        }
                    }

                } else if (result == QueryRes.HTTP_ERROR) {
                    // do nothing now

                } else if (result == QueryRes.NO_NEW) {
                    // do nothing now

                }
                Util.logE(TAG, "onReceive", "mQueryThread end!");
            }
        };
        mQueryThread.start();
    }

    private void resumeDownNewUpdate() {
        DownloadInfo downInfo = Util.getDownloadInfo(UpdateSelfService.this);

        File apkFile = downInfo.getApkFile();
        if (apkFile.exists()) {
            String fileMd5 = Util.getFileMd5(apkFile.getAbsolutePath());
            if (fileMd5.equals(downInfo.md5)) {
                installApkFile(downInfo);
                // return;
                this.stopSelf();
            } else {
                downloadUpdate(downInfo);
            }
        } else {
            downloadUpdate(downInfo);
        }
    }

    private void startDownNewUpdate(NewUpdateInfo newInfo, boolean isBg) {
        int downType = DownloadInfo.TYPE_NORMAL_DOWN;
        if (isBg) {
            downType = DownloadInfo.TYPE_DOWNLOAD_BG;
        }

        DownloadInfo downInfo = new DownloadInfo(newInfo.md5, newInfo.fileUrl,
                downType, newInfo.verCode);
        Util.saveDownloadInfo(UpdateSelfService.this, downInfo);
        File apkFile = downInfo.getApkFile();
        if (apkFile.exists()) {
            String fileMd5 = Util.getFileMd5(apkFile.getAbsolutePath());
            if (fileMd5.equals(downInfo.md5)) {
                installApkFile(downInfo);
                this.stopSelf();
            } else {
                downloadUpdate(downInfo);
            }
        } else {
            downloadUpdate(downInfo);
        }
    }

    private void downloadUpdate(DownloadInfo downInfo) {
        /*
         * /just download the file when the wifi is available int result =
         * Util.isWifiToMobileNt(this); if (result == 0) {
         * downloadIgnoreDataTraffic(downInfo); } else if (result == 1) {
         * Util.saveDownloadInfo(UpdateSelfService.this, downInfo);
         * mHandler.removeMessages(MSG_DOWNLOAD_DIALOG);
         * mHandler.obtainMessage(MSG_DOWNLOAD_DIALOG).sendToTarget(); } //
         */
        if (Util.isCurrNetworkAvailable(this)) {
            downloadIgnoreDataTraffic(downInfo);
        } else {
            Util.logV(TAG, "downloadUpdate",
                    "the wifi is not available,abandon to download it");
        }
        // */
    }

    private void downloadIgnoreDataTraffic(final DownloadInfo downInfo) {
        if (mDownThread != null && mDownThread.isAlive()) {
            return;
        }

        mDownThread = new Thread() {
            @Override
            public void run() {
                downInfo.startDownload();
                Util.saveDownloadInfo(UpdateSelfService.this, downInfo);
                int result = mDownManager.downloadUpdate(downInfo);
                Util.logV(TAG, "mDownThread", "result = " + result);
                if (result == DownloadRes.DOWNLOAD_COMPLETE) {
                    downInfo.downloadComplete();
                    Util.saveDownloadInfo(UpdateSelfService.this, downInfo);

                    installApkFile(downInfo);

                } else if (result == DownloadRes.HTTP_ERROR) {
                    downInfo.downloadPaused();
                    Util.saveDownloadInfo(UpdateSelfService.this, downInfo);

                } else if (result == DownloadRes.NO_ENOUGH_SPACE) {
                    downInfo.downloadPaused();
                    Util.saveDownloadInfo(UpdateSelfService.this, downInfo);

                } else if (result == DownloadRes.SDCARD_LOST) {
                    downInfo.downloadPaused();
                    Util.saveDownloadInfo(UpdateSelfService.this, downInfo);

                }
                mHandler.removeMessages(MSG_STOP_SELF);
                mHandler.obtainMessage(MSG_STOP_SELF).sendToTarget();
            }
        };
        mDownThread.start();
    }

    private int deleteUnuselessFile(NewUpdateInfo info) {
        DownloadInfo preDownInfo = Util.getDownloadInfo(UpdateSelfService.this);
        if (preDownInfo == null) {
            Util.logI(TAG, "deleteUnuselessFile",
                    "no history download info exist, do nothing");
            return 0;
        }

        if (info == null) {
            Util.logI(TAG, "deleteUnuselessFile",
                    "no new download info exist, do nothing");
            return 0;
        }

        if (!preDownInfo.md5.equals(info.md5)) {
            Util.logI(TAG, "deleteUnuselessFile",
                    "delete history download info exist, do nothing");
            File preApkFile = preDownInfo.getApkFile();
            if (preApkFile.exists()) {
                preApkFile.delete();
            }
            File preTmpFile = preDownInfo.getDownloadFile();
            if (preTmpFile.exists()) {
                preTmpFile.delete();
            }
        }

        File preApkFile = preDownInfo.getApkFile();
        if (preApkFile.exists()) {
            String fileMd5 = Util.getFileMd5(preApkFile.getAbsolutePath());
            if (fileMd5.equals(preDownInfo.md5)) {
                Intent it = new Intent(
                        "android.intent.action.INSTALL_APK_QUIETLY");
                it.putExtra("package", UpdateSelfService.this.getPackageName());
                sendBroadcast(it);
                boolean installRes = Util.backgroundInstallAPK(preApkFile);
                if (!installRes) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(Uri.fromFile(preApkFile),
                            "application/vnd.android.package-archive");
                    startActivity(intent);
                }
                return 1;
            }
            preApkFile.delete();
        }
        File preTmpFile = preDownInfo.getDownloadFile();
        if (preTmpFile.exists()) {
            if (preTmpFile.length() > 1024) {
                if (preDownInfo.state == DownloadInfo.STATE_PAUSED) {
                    // max loading time for a week
                    boolean loadingTime = (System.currentTimeMillis() - preTmpFile
                            .lastModified()) < 7 * 24 * 3600 * 1000;
                    if (loadingTime)
                        return 2;
                }
            }
            preTmpFile.delete();
        }
        return 0;
    }

    private void installApkFile(DownloadInfo downInfo) {
        File apkFile = downInfo.getApkFile();
        String fileMd5 = Util.getFileMd5(apkFile.getAbsolutePath());
        if (!fileMd5.equals(downInfo.md5)) {
            Util.logE(TAG, "installApkFile", "apk file incorrect, file md5: "
                    + fileMd5);
            Util.logE(TAG, "installApkFile", "apk file incorrect, right md5:"
                    + downInfo.md5);
            return;
        }

        Intent it = new Intent(
                "android.intent.action.ZHUOYOU_INSTALL_APK_QUIETLY");
        it.putExtra("package", UpdateSelfService.this.getPackageName());
        sendBroadcast(it, "com.zhuoyi.app.permission.INTERNEL_FLAG");

        boolean installRes = Util.backgroundInstallAPK(apkFile);
        if (!installRes) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(apkFile),
                    "application/vnd.android.package-archive");
            startActivity(intent);
        }
    }

    private void showAlertDialog(final NewUpdateInfo mUpdateInfo) {
        dismissAlertDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(UpdateSelfService.this,
                        R.style.WidgetTheme));
        if (mUpdateInfo.dTitle == null) {
            builder.setTitle(R.string.update_self_dialog_title);
        } else {
            builder.setTitle(mUpdateInfo.dTitle);
        }

        if (mUpdateInfo.dContent == null) {
            builder.setMessage(R.string.update_self_dialog_content);
        } else {
            builder.setMessage(mUpdateInfo.dContent);
        }

        builder.setPositiveButton(R.string.update_self_dialog_confirm_btn,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // UpdateManager.updateServiceStartDown(getApplicationContext());
                        if (mUpdateInfo.policy == NewUpdateInfo.TYPE_UPDATE_BG) {
                            startDownNewUpdate(mUpdateInfo, true);
                        } else {
                            startDownNewUpdate(mUpdateInfo, false);
                        }
                    }
                });

        builder.setNegativeButton(R.string.update_self_dialog_cancel_btn,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing now
                        // dialog.dismiss();
                        UpdateSelfService.this.stopSelf();
                    }
                });

        // builder.show();
        mUpdateAlertDialog = builder.create();
        mUpdateAlertDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mUpdateAlertDialog.setCanceledOnTouchOutside(false);
        mUpdateAlertDialog.show();
    }

    private void showAlertDialog(final DownloadInfo downInfo) {
        dismissAlertDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(UpdateSelfService.this,
                        R.style.WidgetTheme));
        builder.setTitle(R.string.update_networktip_title);
        builder.setMessage(R.string.update_self_networktip_content);

        builder.setPositiveButton(R.string.update_networktip_yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // UpdateManager.updateServiceDataTraffic(UpdateSelfService.this.getApplicationContext());
                        downloadIgnoreDataTraffic(downInfo);
                    }
                });

        builder.setNegativeButton(R.string.update_networktip_no,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing now
                        // dialog.dismiss();
                        UpdateSelfService.this.stopSelf();
                    }
                });
        mUpdateAlertDialog = builder.create();
        mUpdateAlertDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mUpdateAlertDialog.setCanceledOnTouchOutside(false);
        mUpdateAlertDialog.show();
    }

    public void dismissAlertDialog() {
        if (mUpdateAlertDialog != null) {
            Util.logE(TAG, "dismissAlertDialog",
                    "mUpdateAlertDialog != null,and  mUpdateAlertDialog.isShowing() = "
                            + mUpdateAlertDialog.isShowing());
        } else {
            Util.logE(TAG, "dismissAlertDialog", "mUpdateAlertDialog == null");
        }
        if (mUpdateAlertDialog != null && mUpdateAlertDialog.isShowing()) {
            mUpdateAlertDialog.dismiss();
            mUpdateAlertDialog = null;
        }
    }
}
