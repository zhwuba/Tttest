package com.freeme.themeclub.updateself;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.freeme.themeclub.R;
import com.freeme.themeclub.updateself.HttpManager.NewUpdateInfo;

public class ForceUpdateActivity extends Activity {
    public static final String TAG = "forceActivity";

    public static final String ACTION_FINISH_ALL_ACTIVITY = "com.zhuoyi.marketNew.action.finishAllActivity";

    private NewUpdateInfo mUpdateInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUpdateInfo = Util.getNewInfo(this);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (mUpdateInfo == null || mUpdateInfo.dTitle == null) {
            builder.setTitle(R.string.update_self_forceDialog_title);
        } else {
            builder.setTitle(mUpdateInfo.dTitle);
        }

        if (mUpdateInfo == null || mUpdateInfo.dContent == null) {
            builder.setMessage(R.string.update_self_forceDialog_content);
        } else {
            builder.setMessage(mUpdateInfo.dContent);
        }

        builder.setPositiveButton(R.string.update_self_dialog_confirm_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // UpdateManager.startDownloadUpdate(getApplicationContext(),
                // mUpdateInfo);
                UpdateManager.updateServiceStartDown(getApplicationContext());
                finish();
            }
        });

        builder.setNegativeButton(R.string.update_self_dialog_cancel_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        // builder.show();
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
   
}
