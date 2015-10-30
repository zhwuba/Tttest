package com.freeme.themeclub.updateself;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.freeme.themeclub.R;
import com.freeme.themeclub.updateself.HttpManager.NewUpdateInfo;

public class UpdateDialogActivity extends Activity{
    public static final String TAG = "forceActivity";

    private NewUpdateInfo mUpdateInfo;
    private AlertDialog mAlertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUpdateInfo = Util.getNewInfo(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUpdateInfo == null) {
            finish();
            return;
        }

        if (mAlertDialog == null) {
            showAlertDialog();
        } else {
            if (!mAlertDialog.isShowing()) {
                mAlertDialog.dismiss();
                showAlertDialog();
            }
        }
    }

    private void showAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

        builder.setPositiveButton(R.string.update_self_dialog_confirm_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UpdateManager.updateServiceStartDown(getApplicationContext());
                finish();
            }
        });

        builder.setNegativeButton(R.string.update_self_dialog_cancel_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing now
                finish();
            }
        });

        mAlertDialog = builder.create();
        mAlertDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mAlertDialog.dismiss();
                    finish();
                }
                return true;
            }
        });
        mAlertDialog.setCanceledOnTouchOutside(false);
        mAlertDialog.show();
    }
}
