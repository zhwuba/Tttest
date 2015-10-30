package com.market.view;

import com.zhuoyi.market.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class LoadingProgressDialog extends ProgressDialog {

    private TextView mTextView = null;
    private View mView = null;
    
    public LoadingProgressDialog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        mView = LayoutInflater.from(context).inflate(R.layout.loading_progress_dialog, null);
        mTextView = (TextView) mView.findViewById(R.id.loading_message);
    }


    public LoadingProgressDialog(Context context, int theme) {
        super(context, theme);
        // TODO Auto-generated constructor stub
        mView = LayoutInflater.from(context).inflate(R.layout.loading_progress_dialog, null);
        mTextView = (TextView) mView.findViewById(R.id.loading_message);
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);  
    }
    
    
    @Override
    public void setMessage(CharSequence message) {
        if (mTextView != null)
            mTextView.setText(message);
    }

}
