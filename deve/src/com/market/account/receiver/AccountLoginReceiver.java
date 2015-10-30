package com.market.account.receiver;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.json.JSONObject;

import com.market.account.utils.BitMapUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

public class AccountLoginReceiver extends BroadcastReceiver {

	private Handler mHandler;
	private static final int ZHUOYOU_USER_INFO = 1000;

	public AccountLoginReceiver(Handler handler) {
		this.mHandler = handler;
	}

	private static String logoUrl = "";
	private static String userName = "";
	private static int recode;
	private String desc;
	private String checkIn;
	private boolean fromUserCenter = false;


	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("zhuoyou.android.account.SEND_USER_INFO") 
				|| intent.getAction().equals("zhuoyou.android.account.USERCENTER_CHECKIN")) {
			Bundle bundle = intent.getBundleExtra("userLogin");
			fromUserCenter = bundle.getBoolean("fromUserCenter");
			String logoCache = bundle.getString("logoCache");
			desc = intent.getExtras().getString("desc");
			String jsonUserInfo = intent.getStringExtra("userInfo");
			checkIn = intent.getStringExtra("checkIn");
			String tmpLogoUrl = null;
			try {
				JSONObject jo = new JSONObject(jsonUserInfo);
				String tmpUserName = jo.getString("nickname");
				int tmpRecode = jo.getInt("recode");
				tmpLogoUrl = jo.has("logoUrl") ? jo.getString("logoUrl") : null;
				userName = tmpUserName;
				recode = tmpRecode;
			} catch (Exception e) {
				e.printStackTrace();
			}
			new UISetterAsyncTask().execute(logoCache, tmpLogoUrl);

		}

	}


	public static void clearBufferLogoUrl() {
		logoUrl = "";
	}

	class UISetterAsyncTask extends AsyncTask<String, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(String... params) {
			if (TextUtils.isEmpty(params[0]) || TextUtils.isEmpty(params[1])) {
				return null;
			}

			if (!TextUtils.isEmpty(params[0])) {
				logoUrl = params[0];
				return BitmapFactory.decodeFile(params[0]);
			} else {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				try {
					URL url = new URL(params[0]);
					InputStream is = (InputStream) url.getContent();

					byte[] buffer = new byte[1024];
					int len = 0;
					while ((len = is.read(buffer)) != -1) {
						os.write(buffer, 0, len);
					}
					is.close();
					os.flush();
					os.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				byte[] data = os.toByteArray();
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
						data.length);
				logoUrl = params[1];
				return bitmap;
			}
		}


		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Message message = new Message();
			message.what = ZHUOYOU_USER_INFO;
			Bundle bundle = new Bundle();
			try {
				// Splash.mInstance.setLoginAccountState(result, userName,
				// recode);
				if (result != null) {
					bundle.putByteArray("result",
							BitMapUtils.createBitByteArray(result));
				}
				bundle.putString("nickname", userName);
				bundle.putInt("recode", recode);
				bundle.putString("desc", desc);
				bundle.putString("checkIn", checkIn);
				bundle.putBoolean("fromUserCenter", fromUserCenter);
				message.setData(bundle);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			mHandler.sendMessage(message);
		}

	}

}
