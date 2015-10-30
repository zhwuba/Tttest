package com.zhuoyi.market.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.zhuoyi.market.R;
import com.market.account.login.BaseActivity_Html5;
import com.market.account.netutil.JavaScriptOperation;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

	public final static int SHARE_SUCCESS = 0;
    public final static int SHARE_CACEL = -1;
    public final static int SHARE_FAILED = -2;
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        String APP_ID = "wxa25e8139f70d5e44";
		IWXAPI api= WXAPIFactory.createWXAPI(this, APP_ID, false);
		api.registerApp(APP_ID);
        api.handleIntent(getIntent(), this);  
    }  
	
	@Override
	public void onReq(BaseReq req) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResp(BaseResp resp) {
		switch (resp.errCode) {  
		case BaseResp.ErrCode.ERR_OK:  
			Toast.makeText(this, R.string.share_success, Toast.LENGTH_SHORT).show();
			sendShareCallBack(SHARE_SUCCESS);
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:  
			Toast.makeText(this, R.string.share_cancel, Toast.LENGTH_SHORT).show();
			sendShareCallBack(SHARE_CACEL);
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:  
			Toast.makeText(this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
			sendShareCallBack(SHARE_FAILED);
			break;  
		default:
			Toast.makeText(this, R.string.share_fail, Toast.LENGTH_SHORT).show();
			sendShareCallBack(SHARE_FAILED);
			break;  
		}
		finish();
	}

	public void sendShareCallBack(int isShare) {
		Intent intent = new Intent(BaseActivity_Html5.SHARE_CALLBACK);
		intent.putExtra("isShare", isShare);
		sendBroadcast(intent);
	}
}
