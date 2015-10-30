package com.zhuoyi.market;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MarketDialog extends Dialog implements OnClickListener
{
    private Context mContext;
    private TextView mDialog_title;
    private TextView mDialog_content;
    private Button mLeftButton;
    private Button mRightButton;
    private Handler mHandler;
    private String mContentString;
    private String mTitleString;
    private int mWhat;
    private int mType = -1;
    public static  final int FORCE_UPDATE = 1;
    public static  final int FORCE_CLOSE = 99;
    public MarketDialog(Context context,Handler handler,int what,String content,String title)
    {
        super(context);
        this.mContext = context;
        mHandler = handler;
        mWhat = what;
        mContentString = content;
        mTitleString = title;
    }
    public MarketDialog(Context context, int theme,Handler handler,int what,String content,String title)
    {
        super(context, theme);
        this.mContext = context;
        mHandler = handler;
        mWhat = what;
        mContentString = content;
        mTitleString = title;
    }
    public MarketDialog(Context context, int theme,Handler handler,int what,String content,String title,int type)
    {
        super(context, theme);
        this.mContext = context;
        mHandler = handler;
        mWhat = what;
        mContentString = content;
        mTitleString = title;
        mType = type;
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.market_dialog);
        findView();
        
        //强制更新，点击屏幕，对话框不消失
        if (mType == FORCE_UPDATE)
            this.setCancelable(false);
    }
    public void findView()
    {
        mDialog_title = (TextView)findViewById(R.id.dialog_title);
        mDialog_title.setText(mTitleString);
        mDialog_content = (TextView)findViewById(R.id.tip_text);
        mDialog_content.setText(mContentString);
        mLeftButton = (Button) findViewById(R.id.tip_dialog_ok_button);
        mRightButton = (Button) findViewById(R.id.tip_dialog_cancel_button);

        mLeftButton.setOnClickListener(this);
        mRightButton.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        
        int id = v.getId();
        switch(id)
        {
            case R.id.tip_dialog_ok_button:     
                if(mHandler!=null)
                    mHandler.sendEmptyMessage(mWhat);
                dismiss();
                break;
                
            case R.id.tip_dialog_cancel_button:
                
                dismiss();
                if(mType==FORCE_UPDATE)
                {
                    //强行退出
                    Intent intent = new Intent(mContext, Splash.class);
                    intent.putExtra("isClose", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mContext.startActivity(intent);
                	
                }else if(mType == FORCE_CLOSE) {
                	if(mHandler!=null) {
                		Message msg = mHandler.obtainMessage();
                		msg.what = mWhat;
                		msg.arg1 = -1;
                		mHandler.sendMessage(msg);
                	}
                }
                
                break;
                
            default:
                break; 
        } 
    }
    @Override
    public void onBackPressed()
    {
        // TODO Auto-generated method stub
        
    }
}
