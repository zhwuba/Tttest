package com.zhuoyi.market.setting;

import java.util.HashMap;
import java.util.regex.Pattern;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyi.market.R;
import com.market.view.LoadingProgressDialog;
import com.market.account.dao.UserInfo;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.request.UserFeedbackReq;
import com.market.net.response.UserFeedbackResp;
import com.market.net.utils.StartNetReqUtils;

public class MarketFeedbackActivity extends Activity implements OnClickListener{
	
	private TextView opinionText1, opinionText2, opinionText3, opinionText4, opinionText5, opinionText6;
	private EditText mSuggestion, mContact;
	private Button mSubmit;
	private Button mCancel;
	private TextView contactTip;
	
	private int[] mImages = {R.drawable.setting_feedback_unselected, R.drawable.setting_feedback_selected};
	private int[] mIndexs = {0, 0, 0, 0,0,0};
	
	private final int FEEDBACK_RESP = 0;
	private LoadingProgressDialog mProgressDialog;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FEEDBACK_RESP:
				HashMap<String, Object> map = (HashMap<String, Object>) msg.obj;
				if(map == null || map.size() <= 0) {
					if(mProgressDialog.isShowing())
						mProgressDialog.cancel();
					Toast.makeText(getApplicationContext(), getString(R.string.feedback_fail), Toast.LENGTH_SHORT).show();
				} else {
					UserFeedbackResp resp = (UserFeedbackResp) map.get("userFeedbackResp");
					if(resp.getResult().equals("0")) {
						Toast.makeText(getApplicationContext(), getString(R.string.feedback_success), Toast.LENGTH_SHORT).show();
						MarketFeedbackActivity.this.finish();
					} else {
						if(mProgressDialog.isShowing())
							mProgressDialog.cancel();
						Toast.makeText(getApplicationContext(), getString(R.string.feedback_fail), Toast.LENGTH_SHORT).show();
					}
				}
				break;

			default:
				break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.layout_feedback);
		
		findView();
	}

	
	private void findView() {
		mProgressDialog = new LoadingProgressDialog(this);
		mProgressDialog.setMessage(getResources().getString(R.string.feedback_waiting_for_result));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(true);
		
		opinionText1 = (TextView) findViewById(R.id.opinionText1);
		opinionText2 = (TextView) findViewById(R.id.opinionText2);
		opinionText3 = (TextView) findViewById(R.id.opinionText3);
		opinionText4 = (TextView) findViewById(R.id.opinionText4);
		opinionText5 = (TextView) findViewById(R.id.opinionText5);
		opinionText6 = (TextView) findViewById(R.id.opinionText6);
		mSuggestion = (EditText) findViewById(R.id.suggestion);
		mContact = (EditText) findViewById(R.id.contact);
		mSubmit = (Button) findViewById(R.id.submit);
		mCancel = (Button) findViewById(R.id.cancel);
		contactTip = (TextView) findViewById(R.id.contact_tip);
		
		opinionText1.setOnClickListener(this);
		opinionText2.setOnClickListener(this);
		opinionText3.setOnClickListener(this);
		opinionText4.setOnClickListener(this);
		opinionText5.setOnClickListener(this);
		opinionText6.setOnClickListener(this);
		
		mSubmit.setOnClickListener(this);
		mCancel.setOnClickListener(this);
		
		contactTip.setText(Html.fromHtml(getResources().getString(R.string.contact_information_tip)));
	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.opinionText1:
			setOptions(0, opinionText1);
			break;
		case R.id.opinionText2:
			setOptions(1, opinionText2);
			break;
		case R.id.opinionText3:
			setOptions(2, opinionText3);
			break;
		case R.id.opinionText4:
			setOptions(3, opinionText4);
			break;
		case R.id.opinionText5:
			setOptions(4, opinionText5);
			break;
		case R.id.opinionText6:
			setOptions(5, opinionText6);
			break;
		case R.id.submit:
			submitSuggestion();
			break;
		case R.id.cancel:
			finish();
			break;
		default:
			break;
		}
	}


	/**
	 * 设置选项的颜色 图片
	 * @param index
	 * @param tv
	 */
	private void setOptions(int index, TextView tv) {
		mIndexs[index] = 1 - mIndexs[index];
		if(mIndexs[index] == 0) 
			tv.setTextColor(0xff838383);
		else 
			tv.setTextColor(0xff484848);
		Drawable drawable = getResources().getDrawable(mImages[mIndexs[index]]);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tv.setCompoundDrawables(drawable, null, null, null);
		
	}
	
	
	/**
	 * 提交反馈
	 */
	private void submitSuggestion() {
		String content = null;
		String contact = null;
		String openId = null;
		content = mIndexs[0] == 0 ? "" : getString(R.string.feedback_opinion1) + " ";
		content += mIndexs[1] == 0 ? "" : getString(R.string.feedback_opinion2) + " ";
		content += mIndexs[2] == 0 ? "" : getString(R.string.feedback_opinion3) + " ";
		content += mIndexs[3] == 0 ? "" : getString(R.string.feedback_opinion4) + " ";
		content += mIndexs[4] == 0 ? "" : getString(R.string.feedback_opinion5) + " ";
		content += mIndexs[5] == 0 ? "" : getString(R.string.feedback_opinion6) + " ";
		content += mSuggestion.getText().toString();
		
		if(content.equals("")){
			Toast.makeText(getApplicationContext(), getString(R.string.feedback_suggestion_null), Toast.LENGTH_SHORT).show();
			return;
		}
		contact = mContact.getText().toString();
		if(!isPhoneOrEmail(contact)) {
			Toast.makeText(getApplicationContext(), getString(R.string.feedback_contact_info_error), Toast.LENGTH_SHORT).show();
			return;
		}
		mProgressDialog.show();
		                              
		openId = UserInfo.get_openid(getApplicationContext());
		
		UserFeedbackReq req = new UserFeedbackReq();
		req.setContent(content);
		req.setContact(contact);
		req.setOpenId(openId);
		
		String contents = SenderDataProvider.buildToJSONData(getApplicationContext(), MessageCode.GET_USER_FEEDBACK, req);
		StartNetReqUtils.execListByPageRequest(mHandler, FEEDBACK_RESP, MessageCode.GET_USER_FEEDBACK, contents);
	}
	
	
	/**
	 * 判断联系方式是否为手机号或者qq邮箱
	 * @param contact
	 * @return
	 */
	private boolean isPhoneOrEmail(String contact) {
		if(contact == null || contact.equals(""))
			return true;
		return Pattern.compile("[1][358]\\d{9}").matcher(contact).matches() 
			 ||Pattern.compile("^[a-zA-Z][.,_,-,a-zA-Z0-9]{2,17}@(qq|QQ).com$").matcher(contact).matches()
			 ||Pattern.compile("^[1-9][0-9]{4,10}@(qq|QQ).com").matcher(contact).matches();
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mHandler != null)
			mHandler.removeCallbacksAndMessages(null);
	}
	
}
