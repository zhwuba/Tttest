package com.market.download.updates;

import com.market.download.userDownload.DownStorage;
import com.zhuoyi.market.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AppUpdateSignCheckActivity extends Activity {
    
    private TextView mTipContentView = null;
    private Button mCancelBtn = null;
    private Button mUninstallBtn = null;
    
    private String mPkgName = "";
    private String mAppName = "";
    private String mTipContent = "";
    private String mFileName = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_update_sign_check);
        
        mPkgName = this.getIntent().getStringExtra("pkg_name");
        mAppName = this.getIntent().getStringExtra("app_name");
        mFileName = this.getIntent().getStringExtra("file_name");
        
        mTipContent = this.getString(R.string.app_update_sign_check_tip, mAppName);
        mTipContentView = (TextView) this.findViewById(R.id.tip_content);
        mTipContentView.setText(mTipContent);
        
        mCancelBtn = (Button) this.findViewById(R.id.cancel_button);
        mCancelBtn.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                AppUpdateSignCheckActivity.this.finish();
            }
            
        });
        
        mUninstallBtn = (Button) this.findViewById(R.id.uninstall_button);
        mUninstallBtn.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                DownStorage.putSignCheckInstallInfo(AppUpdateSignCheckActivity.this.getApplicationContext(), mPkgName, mFileName);
                
                Uri uri = Uri.parse("package:" + mPkgName);  
                Intent intent = new Intent(Intent.ACTION_DELETE, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                AppUpdateSignCheckActivity.this.startActivity(intent); 
                
                AppUpdateSignCheckActivity.this.finish();
            }
            
        });
    }
}
