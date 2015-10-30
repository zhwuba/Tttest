package com.zhuoyi.market;


import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.market.account.constant.Constant;
import com.market.account.netutil.FormFile;
import com.market.account.netutil.SocketHttpRequester;
import com.market.account.utils.MD5Util;
import com.market.account.utils.PhoneNumUtils;
import com.market.view.LoadingProgressDialog;
import com.zhuoyi.market.utils.MarketUtils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 编辑收货地址
 */
public class DeliveryAddressActivity extends Activity implements OnClickListener{
	
	private EditText mEditText_name;
	private EditText mEditText_phone;
	private EditText mEditText_address;
	private ImageView mImageView_name;
	private ImageView mImageView_phone;
	private ImageView mImageView_address;
	private Button mButton_Save;
	private Button mButton_back;
	
	private String name;
	private String phone;
	private String address;
	private String token;
	private String openId;
	private LoadingProgressDialog mProgressDialog;
	private EditUserInfo mEditUserInfo;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		name = bundle.getString("cname");
		phone = bundle.getString("cmobile");
		address = bundle.getString("caddress");
		token = bundle.getString("token");
		openId = bundle.getString("openid");
		
		setContentView(R.layout.personalinfo_address);
		
		mProgressDialog = new LoadingProgressDialog(this);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setMessage(getResources().getString(R.string.personalInfo_waiting_for_result));
		mProgressDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				Toast.makeText(DeliveryAddressActivity.this, getResources().getString(R.string.personalInfo_cancel), Toast.LENGTH_SHORT).show();
				if(mEditUserInfo != null && mEditUserInfo.getStatus() == AsyncTask.Status.RUNNING) {
					mEditUserInfo.cancel(true);
					mEditUserInfo = null;
				}
			}
		});
		
		findView();
	}

	
	private void findView() {
		mEditText_name = (EditText) findViewById(R.id.editText_name);
		mEditText_phone = (EditText) findViewById(R.id.editText_phone);
		mEditText_address = (EditText) findViewById(R.id.editText_address);
		mImageView_name = (ImageView) findViewById(R.id.img_name);
		mImageView_phone = (ImageView) findViewById(R.id.img_phone);
		mImageView_address = (ImageView) findViewById(R.id.img_address);
		mButton_Save = (Button) findViewById(R.id.submit);
		mButton_back = (Button) findViewById(R.id.back);
		
		if(name != null) {
			mEditText_name.setText(name);
			mEditText_name.setSelection(name.length());
		}
		if(phone != null)
			mEditText_phone.setText(phone);
		if(address != null)
			mEditText_address.setText(address);
		mImageView_name.setOnClickListener(this);
		mImageView_phone.setOnClickListener(this);
		mImageView_address.setOnClickListener(this);
		mButton_Save.setOnClickListener(this);
		mButton_back.setOnClickListener(this);
	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_name:
			mEditText_name.setText("");
			break;
		case R.id.img_phone:
			mEditText_phone.setText("");
			break;
		case R.id.img_address:
			mEditText_address.setText("");
			break;
		case R.id.submit:
			submit();
			break;
		case R.id.back:
			finish();
			break;
		default:
			break;
		}
		
	}


	/**
	 * 确认并保存
	 */
	private void submit() {
		name = mEditText_name.getText().toString();
		phone = mEditText_phone.getText().toString();
		address = mEditText_address.getText().toString();
		
		if(name == null || name.equals("")) {
			Toast.makeText(this, getString(R.string.personalInfo_address_name_null), Toast.LENGTH_SHORT).show();
			return;
		} else if(!PhoneNumUtils.isPhoneNumberValid(phone)) {
			Toast.makeText(this, getString(R.string.feedback_contact_info_error), Toast.LENGTH_SHORT).show();
			return;
		} else if(address == null || address.equals("")) {
			Toast.makeText(this, getString(R.string.personalInfo_address_address_null), Toast.LENGTH_SHORT).show();
			return;
		}
		mProgressDialog.show();
		if(mEditUserInfo == null) {
			mEditUserInfo = new EditUserInfo();
			mEditUserInfo.execute();
		}
		
	}
	
	
	public class EditUserInfo extends AsyncTask<Void, Void, String> {
		
		@Override
		protected String doInBackground(Void... params) {
			String sign = MD5Util.md5(openId + token + Constant.SIGNKEY);
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("cname", name);
				jsonObject.put("cmobile", phone);
				jsonObject.put("caddress", address);
				
				Map<String, String> mParams = new HashMap<String, String>();
				mParams.put("sign", sign);
				mParams.put("openid", openId);
				mParams.put("token", token);	
				mParams.put("data", jsonObject.toString());
				String result = upload(mParams);
				return result;
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		
		private String upload(Map<String, String> map) {
			try {
				FormFile formFile = null;
				String ret = SocketHttpRequester.postExternalFile(Constant.ZHUOYOU_EDIT_USER_INFO, map, formFile);
				return ret;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (mProgressDialog != null && mProgressDialog.isShowing())
				mProgressDialog.dismiss();
			try {
				if (result != null) {
					JSONObject object = new JSONObject(result);
					if(object.getInt("result") == 0) {
						Intent intent = new Intent();
						intent.putExtra("cname", name);
						intent.putExtra("cmobile", phone);
						intent.putExtra("caddress", address);
						setResult(4, intent);
						finish();
						Toast.makeText(DeliveryAddressActivity.this, getResources().getString(R.string.personalInfo_success), Toast.LENGTH_SHORT).show();
					} else
						Toast.makeText(DeliveryAddressActivity.this, getResources().getString(R.string.personalInfo_fail), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), R.string.server_exception, Toast.LENGTH_SHORT).show();
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mEditUserInfo != null && mEditUserInfo.getStatus() == AsyncTask.Status.RUNNING) {
			mEditUserInfo.cancel(true);
			mEditUserInfo = null;
		}
	}
}
