package com.market.account.login;

import com.zhuoyi.market.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
/**
 * 
 * @author sunlei
 *
 */
public class BaseActivity_new extends Activity {

    ProgressDialog mDialog = null;
    BaseActivity_new baseActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        super.setContentView(R.layout.base_activity_with_titlebar);
        setTitle(null);
        setLeftButton("", new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mDialog = new ProgressDialog(BaseActivity_new.this);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        baseActivity = this;

    }
    /**
     * 显示dialog框
     * @param content .
     */
    protected void showProgressbar(String content) {
        mDialog.setMessage(content);
        if (!mDialog.isShowing()) {
            if (baseActivity != null && !baseActivity.isFinishing()) {
                mDialog.show();
            }

        }

    }
    /**
     * 取消dialog框
     */
    protected void dismissProgressbar() {

        if (baseActivity != null && !baseActivity.isFinishing()) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        LayoutInflater inflater = LayoutInflater.from(this);
        ((LinearLayout) findViewById(R.id.layout_content)).addView(inflater.inflate(layoutResID, null));
    }
    
    /**
     * 设置左按钮是否显示
     */
    public void setLeftButtonEnable() {
        ((TextView) findViewById(R.id.btn_back_id)).setVisibility(View.VISIBLE);
    }
    /**
     * 设置左按钮的点击事件
     */
    public void setLeftButton(String title, OnClickListener listener) {
        TextView leftButton = (TextView) findViewById(R.id.btn_back_id);
        //leftButton.setText(title);
        leftButton.setOnClickListener(listener);
    }

    public void setLeftButton(int strId, OnClickListener listener) {
        TextView leftButton = (TextView) findViewById(R.id.btn_back_id);
        //leftButton.setText(getResources().getString(strId));
        leftButton.setOnClickListener(listener);
    }

    public void setBarTitle(String title) {
        ((TextView) findViewById(R.id.title_text)).setText(title);
    }
    
}
