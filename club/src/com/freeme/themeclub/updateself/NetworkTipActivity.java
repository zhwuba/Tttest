package com.freeme.themeclub.updateself;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.freeme.themeclub.R;

public class NetworkTipActivity extends Activity {
    public static final String TAG = "NetworkTipActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.update_networktip_title);
        builder.setMessage(R.string.update_self_networktip_content);

        builder.setPositiveButton(R.string.update_networktip_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UpdateManager.updateServiceDataTraffic(getApplicationContext());
                finish();
            }
        });

        builder.setNegativeButton(R.string.update_networktip_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing now
                finish();
            }
        });
        builder.show();
        super.onResume();
    }

}
