package com.zhuoyi.market;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.market.account.authenticator.AuthenticatorActivity;
import com.market.account.constant.Constant;
import com.market.account.dao.GetUserInfo;
import com.market.account.dao.GetUserInfo.GetUserInfoListener;
import com.market.account.dao.UserInfo;
import com.market.account.login.BaseActivity_Html5;
import com.market.account.login.BaseHtmlActivity;
import com.market.account.netutil.FormFile;
import com.market.account.netutil.SocketHttpRequester;
import com.market.account.utils.MD5Util;
import com.market.account.utils.PropertyFileUtils;
import com.market.view.LoadingProgressDialog;
import com.widget.time.JudgeDate;
import com.widget.time.ScreenInfo;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.utils.WheelMain;

/**
 * 编辑个人信息
 * @author Administrator
 *
 */
public class PersonalInfoActivity extends Activity implements OnClickListener{
	
	private ImageView mImageView_UserLogo;
	private ImageView mImageView_Phone;
	private TextView mNickName;
	private TextView mGender;
	private TextView mBirthday;
	private TextView mAddress;
	private TextView mMobile;
	private LinearLayout mLinearLayout_logo;
	private RelativeLayout mRelativeLayout_NickName;
	private RelativeLayout mRelativeLayout_Gender;
	private RelativeLayout mRelativeLayout_Birthday;
	private RelativeLayout mRelativeLayout_Address;
	private RelativeLayout mRelativeLayout_Mobile;
	private RelativeLayout mRelativeLayout_Password;
	private Button mButton_Submit;
	
	private int PHOTO_ALBUM = 1;
	private int PHOTO_CAMERA = 2;
	private int PHOTO_RESULT = 3;
	private int ACTIVITY_ADDRESS = 4;
	private int ACTIVITY_PASSWORD = 5;
	private int BIND_MOBILE = 6;
	
	private String nickName;
	private String userName;//绑定的手机号
	private String gender;
	private String birthday;
	private Bitmap userLogo;
	private String openId;
	private String token;
	private String sign;
	private String logoUrl;
	private String cmobile;
	private String cname;
	private String caddress;
	private GetUserInfo mGetUserInfo;
	private EditUserInfo mEditUserInfo;
	private String logoTempPath = "";
	private Bitmap mUserlogoBackBitmap;
	private Drawable mUserlogoBackDrawable;
	private String LOGO_PATH = PropertyFileUtils.getSDPath() + Constant.USERLOGO;
	private String LOGO_BASE_PATH = PropertyFileUtils.getSDPath() + Constant.USERLOGO_BASE_PATH;
	private final String LOGO_FILE_PATH = PropertyFileUtils.getSDPath() + Constant.USERLOGO_PATH;
	private String LOGO_ZOOM_FILE_PATH = LOGO_BASE_PATH + "temp.png";
	
	private LoadingProgressDialog mProgressDialog;
	private WheelMain wheelMain;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
	
	private static final int UPDATE_LOGO = 0;
	private static final int GET_USERINFO_FAIL = 1;
	
	private Handler mHandler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			if(isFinishing())
				return;
			switch (msg.what) {
			case UPDATE_LOGO:
				if(new File(LOGO_FILE_PATH).exists()) {
					Bitmap bitmap = BitmapFactory.decodeFile(LOGO_FILE_PATH);
					if(bitmap != null) {
						userLogo = AsyncImageCache.getCroppedRoundBitmap(bitmap, (int)getResources().getDimension(R.dimen.myself_user_logo_width));
						bitmap.recycle();
						mImageView_UserLogo.setImageBitmap(AsyncImageCache.addBorder(userLogo));
					}
				}
				break;
			case GET_USERINFO_FAIL:
				finish();
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null)
			logoTempPath = savedInstanceState.getString("logoTempPath");
		setContentView(R.layout.personalinfo_show);
		
		token = UserInfo.get_token(this);
		
		mProgressDialog = new LoadingProgressDialog(this);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setMessage(getResources().getString(R.string.personalInfo_getUserInfo));
		mProgressDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				if (mGetUserInfo != null && mGetUserInfo.getStatus() == AsyncTask.Status.RUNNING) {
					Toast.makeText(PersonalInfoActivity.this, getResources().getString(R.string.personalInfo_cancel), Toast.LENGTH_SHORT).show();
					mGetUserInfo.cancel(true);
					mGetUserInfo = null;
					finish();
				}
			}
		});
		
		findView();
		getUserInfo();
	}
	

	/**
	 * 获取当前登录的用户的信息
	 */
	private void getUserInfo() {
		mProgressDialog.show();
		if (mGetUserInfo == null) {

			mGetUserInfo = new GetUserInfo(this);
			GetUserInfoListener mGetUserInfoListener = new GetUserInfoListener() {

				@Override
				public void OnGetUserInfoCallBack(String result) {
					try {
						if (result != null) {
							JSONObject object = new JSONObject(result);
							nickName = object.has("nickname") ? object.getString("nickname") : null;
							userName = object.has("username") ? object.getString("username") : null;
							gender = object.has("gender") ? object.getString("gender") : null;
							birthday = object.has("birthday") ? object.getString("birthday") : null;
							openId = object.has("openid") ? object.getString("openid") : null;
							logoUrl = object.has("avatarurl") ? object.getString("avatarurl") : object.has("avatar") ? object.getString("avatar") : null;
							JSONObject contact = object.has("contact") ? object.getJSONObject("contact") : null;
							if (contact != null) {
								cname = contact.has("cname") ? contact.getString("cname") : null;
								cmobile = contact.has("tel") ? contact.getString("tel") : null;
								caddress = contact.has("address") ? contact.getString("address") : null;
							}

							if (new File(LOGO_FILE_PATH).exists()) {//头像已经存在
								Bitmap bitmap = BitmapFactory.decodeFile(LOGO_FILE_PATH);
								if (bitmap != null) {
									userLogo = AsyncImageCache.getCroppedRoundBitmap(bitmap, (int) getResources().getDimension(R.dimen.myself_user_logo_width));
									bitmap.recycle();
								}
							} else if (logoUrl != null) {//设置过头像未下载
								new Thread(new Runnable() {

									@Override
									public void run() {
										if (mHandler != null) {
											AuthenticatorActivity.downloadUserLogo(logoUrl);
											mHandler.sendEmptyMessage(UPDATE_LOGO);
										}
									}
								}).start();
							} else {//未设置过头像
								Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.usercenter_logo);
								if(bitmap != null) {
									userLogo = AsyncImageCache.getCroppedRoundBitmap(bitmap, (int) getResources().getDimension(R.dimen.myself_user_logo_width));
									bitmap.recycle();
								}
							}
							initView();
						} else {
							if(mProgressDialog != null && mProgressDialog.isShowing())
								mProgressDialog.dismiss();
							Toast.makeText(PersonalInfoActivity.this, getResources().getString(R.string.personalInfo_getUserInfo_fail), Toast.LENGTH_SHORT).show();
							mHandler.sendEmptyMessage(GET_USERINFO_FAIL);
						}
					} catch (JSONException e) {
						if(mProgressDialog != null && mProgressDialog.isShowing())
							mProgressDialog.dismiss();
						e.printStackTrace();
					}
				}
			};
			mGetUserInfo.setGetUserInfoListener(mGetUserInfoListener);
			mGetUserInfo.execute();
		}
	}
	
	
	private void findView() {
		mImageView_UserLogo = (ImageView) findViewById(R.id.userLogo);
		mImageView_Phone = (ImageView) findViewById(R.id.img_phone);
		mNickName = (TextView) findViewById(R.id.nickName);
		mGender = (TextView) findViewById(R.id.gender);
		mBirthday = (TextView) findViewById(R.id.birthday);
		mAddress = (TextView) findViewById(R.id.address);
		mMobile = (TextView) findViewById(R.id.mobile);
		mLinearLayout_logo = (LinearLayout) findViewById(R.id.Linearlayout_logo);
		mRelativeLayout_NickName = (RelativeLayout) findViewById(R.id.RelativeLayout_NickName);
		mRelativeLayout_Gender = (RelativeLayout) findViewById(R.id.RelativeLayout_Gender);
		mRelativeLayout_Birthday = (RelativeLayout) findViewById(R.id.RelativeLayout_Birthday);
		mRelativeLayout_Address = (RelativeLayout) findViewById(R.id.RelativeLayout_Address);
		mRelativeLayout_Mobile = (RelativeLayout) findViewById(R.id.RelativeLayout_Mobile);
		mRelativeLayout_Password = (RelativeLayout) findViewById(R.id.RelativeLayout_Password);
		mButton_Submit = (Button) findViewById(R.id.submit);
		
		mUserlogoBackDrawable = null;
		mUserlogoBackBitmap = null;
		try {
			mUserlogoBackBitmap = MarketUtils.read565Bitmap(getApplicationContext(), R.drawable.personalinfo_logo_bg);
			mUserlogoBackDrawable = new BitmapDrawable(mUserlogoBackBitmap);
			if (mUserlogoBackDrawable != null)
				mLinearLayout_logo.setBackgroundDrawable(mUserlogoBackDrawable);
		} catch (OutOfMemoryError e) {
			System.gc();
		}
	}
	

	private void initView() {
		if(mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();
		mImageView_UserLogo.setOnClickListener(this);
		mRelativeLayout_NickName.setOnClickListener(this);
		mRelativeLayout_Gender.setOnClickListener(this);
		mRelativeLayout_Birthday.setOnClickListener(this);
		mRelativeLayout_Address.setOnClickListener(this);
		mRelativeLayout_Mobile.setOnClickListener(this);
		mRelativeLayout_Password.setOnClickListener(this);
		mButton_Submit.setOnClickListener(this);
		
		mGender.setText(gender == null ? getString(R.string.personalInfo_tip_unselected) : gender.equals("未知") ? getString(R.string.personalInfo_tip_unselected) : gender);
		mBirthday.setText((birthday == null) ? getString(R.string.personalInfo_tip_unselected) : birthday);
		mAddress.setText((caddress == null) ? getString(R.string.personalInfo_address_address_null) : getString(R.string.personalInfo_address_address_saved));
		if(!TextUtils.isEmpty(nickName)) {
			mNickName.setText(nickName);
		}
		if(TextUtils.isEmpty(userName)) {
			mMobile.setText(getString(R.string.personalInfo_unbind_phone));
			mRelativeLayout_Password.setVisibility(View.GONE);
		} else {
			mMobile.setText(userName);
			mImageView_Phone.setVisibility(View.GONE);
			mRelativeLayout_Password.setVisibility(View.VISIBLE);
		}
		if(userLogo != null) {
			mImageView_UserLogo.setImageBitmap(AsyncImageCache.addBorder(userLogo));
		}
	}
	

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.userLogo://点击了头像
			new MyDialog(this, 1, getResources().getStringArray(R.array.personalinfo_dialog_userlogo), R.style.MyMarketDialog).show();
			break;
			
		case R.id.RelativeLayout_NickName://昵称
			showCommonDialog(0);
			break;
			
		case R.id.RelativeLayout_Gender://性别
			new MyDialog(this, 2, getResources().getStringArray(R.array.personalinfo_dialog_gender), R.style.MyMarketDialog).show();
			break;  
			
		case R.id.RelativeLayout_Birthday://生日
			setBirthday();
			break;
			
		case R.id.RelativeLayout_Address://收货地址
			intent = new Intent(this, DeliveryAddressActivity.class);
			intent.putExtra("cname", cname);
			intent.putExtra("cmobile", cmobile);
			intent.putExtra("caddress", caddress);
			intent.putExtra("token", token);
			intent.putExtra("openid", openId);
			startActivityForResult(intent, ACTIVITY_ADDRESS);
			break;
			
		case R.id.RelativeLayout_Mobile:
			if(mRelativeLayout_Password.getVisibility() == View.GONE) {
				intent = new Intent(this, BaseHtmlActivity.class);
				intent.putExtra("forResult", true);
				intent.putExtra("titleName", getResources().getString(R.string.personalInfo_bind_phone_title));
				intent.putExtra("wbUrl", com.zhuoyi.market.constant.Constant.ZHUOYOU_URL_BINEMOBILE);
				startActivityForResult(intent, BIND_MOBILE);
			}
			break;
			
		case R.id.RelativeLayout_Password:// 密码
			intent = new Intent(this, ChangePasswordActivity.class);
			intent.putExtra("phone", userName);
			intent.putExtra("token", token);
			startActivityForResult(intent, ACTIVITY_PASSWORD);
			break;
			
		case R.id.submit://退出账号
			showCommonDialog(1);
			break;
			
		default: 
			break;
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_CANCELED)
			return;
		if(requestCode == PHOTO_ALBUM) {//从相册取
			if(data == null || data.getData() == null)
				Toast.makeText(getApplicationContext(), getString(R.string.personalInfo_getPhoto_fail), Toast.LENGTH_SHORT).show();
			else
				startPhotoZoom(data.getData());
		} else if(requestCode == PHOTO_CAMERA) {//拍照获取
			int angle = readPictureDegree(logoTempPath);
			if(angle != 0)
				rotatePhoto(angle);
			File file = new File(logoTempPath);
			startPhotoZoom(Uri.fromFile(file));
		} else if(requestCode == PHOTO_RESULT) {//裁剪之后
			userLogo = BitmapFactory.decodeFile(LOGO_ZOOM_FILE_PATH);
			if (userLogo == null)
				return;
			userLogo = AsyncImageCache.getCroppedRoundBitmap(
					userLogo,
					(int) getResources().getDimension(
							R.dimen.myself_user_logo_width));
			mImageView_UserLogo.setImageBitmap(AsyncImageCache.addBorder(userLogo));
			mProgressDialog.setMessage(getResources().getString(R.string.personalInfo_waiting_for_result));
			mProgressDialog.show();
			if(mEditUserInfo == null)
				(mEditUserInfo = new EditUserInfo()).execute();
		} else if(requestCode == ACTIVITY_ADDRESS) {//收货地址
			cname = data.getStringExtra("cname");
			cmobile = data.getStringExtra("cmobile");
			caddress = data.getStringExtra("caddress");
			mAddress.setText(getResources().getString(R.string.personalInfo_address_address_saved));
		} else if(requestCode == BIND_MOBILE) {//绑定手机
			if(data != null) {
				if(data.getBooleanExtra("result", false)) {
					Toast.makeText(PersonalInfoActivity.this, getResources().getString(R.string.personalInfo_bind_phone_success), Toast.LENGTH_SHORT).show();
					userName = data.getStringExtra("phone");
					mMobile.setText(userName);
					mImageView_Phone.setVisibility(View.GONE);
					mRelativeLayout_Password.setVisibility(View.VISIBLE);
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	/**
	 * 对图片裁剪
	 * @param uri
	 */
	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		//aspectX,aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		//outputX,outputY 是裁剪的宽高
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 200);
		intent.putExtra("return-data", false);
		
		intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(LOGO_ZOOM_FILE_PATH)));
		
		startActivityForResult(intent, PHOTO_RESULT);
		
	}
	
	
	public int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}
	
	
	/**
	 * 对拍照图片缩放并旋转
	 * @param angle 旋转角度
	 */
	public void rotatePhoto(int angle) {
		
		WindowManager wm = getWindowManager();
		int windowWidth = wm.getDefaultDisplay().getWidth();
        int windowHeight = wm.getDefaultDisplay().getHeight();
        
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;  
		BitmapFactory.decodeFile(logoTempPath, opts);
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
		int bitmapHeight = opts.outHeight;
        int bitmapWidth = opts.outWidth;
        if (bitmapHeight > windowHeight || bitmapWidth > windowWidth) {
            int scaleX = bitmapWidth / windowWidth;
            int scaleY = bitmapHeight / windowHeight;
            if(scaleX > scaleY)//按照水平方向的比例缩放
                opts.inSampleSize = scaleX;
            else               //按照竖直方向的比例缩放
                opts.inSampleSize = scaleY;
        } else {//图片比手机屏幕小 不缩放
            opts.inSampleSize = 1;
        }
		opts.inJustDecodeBounds = false;
		Matrix m = new Matrix();  
        m.postRotate(angle);
        Bitmap bitmap = null;
        try {
        	bitmap = BitmapFactory.decodeFile(logoTempPath, opts);
        	if(bitmap == null)
        		return;
        	bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);// 重新生成图片
        	saveToFile(bitmap);
		} catch (OutOfMemoryError e) {
			System.gc();
		} finally {
			if(bitmap != null)
				bitmap.recycle();
		}
	}
	
	
	/**
	 * 弹出对话框
	 */
	class MyDialog extends Dialog {
		private ListView mListView;
		private String[] data = null;
		private int type = 1;
		public MyDialog(Context context, int type, String[] data, int theme) {
			super(context, theme);
			this.type = type;
			this.data = data;
		}
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.personalinfo_dialog);
			mListView = (ListView) findViewById(R.id.listview_option);
			MyBaseAdapter adapter = new MyBaseAdapter(data, type);
			mListView.setAdapter(adapter);
			mListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if(type == 1) {//头像
						Intent intent = null;
						if(position == 0) {//相册
							deletePhotoAtPathAndName(logoTempPath);
							logoTempPath = LOGO_BASE_PATH + System.currentTimeMillis() + ".png";
							intent = new Intent(Intent.ACTION_PICK, null);
							intent.setType("image/*");
							startActivityForResult(intent, PHOTO_ALBUM);
						} else if(position == 1) {//相机
							if(!new File(LOGO_PATH).exists()) {
								new File(LOGO_BASE_PATH).mkdirs();
							} else if(!new File(LOGO_BASE_PATH).exists()){
								new File(LOGO_BASE_PATH).mkdir();
							} else
								deletePhotoAtPathAndName(logoTempPath);
							String fileName = System.currentTimeMillis() + ".png";
							logoTempPath = LOGO_BASE_PATH + fileName;
							intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(LOGO_BASE_PATH, fileName)));
							startActivityForResult(intent, PHOTO_CAMERA);
						} 
					} else if(type == 2) {//性别
						if(position < data.length - 1) {
							mGender.setText(data[position]);
							mProgressDialog.setMessage(getResources().getString(R.string.personalInfo_waiting_for_result));
							mProgressDialog.show();
							if(mEditUserInfo == null)
								(mEditUserInfo = new EditUserInfo()).execute();
						}
					}
					dismiss();
				}
			});
		}
		
	}
	

	/**
	 * 设置生日
	 */
	private void setBirthday() {
		Calendar calendar = Calendar.getInstance();
		Boolean timeBiggerThanNow = false;
		LayoutInflater inflater = LayoutInflater.from(PersonalInfoActivity.this);
		View timepickerview = inflater.inflate(R.layout.personalinfo_timepicker, null);
		ScreenInfo screenInfo = new ScreenInfo(PersonalInfoActivity.this);
		wheelMain = new WheelMain(timepickerview);
		wheelMain.screenheight = screenInfo.getHeight();
		
		int currYear = calendar.get(Calendar.YEAR);
		int currMonth = calendar.get(Calendar.MONTH);
		int currDay = calendar.get(Calendar.DAY_OF_MONTH);
		wheelMain.setEND_YEAR(calendar.get(Calendar.YEAR));
		wheelMain.setEND_MONTH(calendar.get(Calendar.MONTH));
		wheelMain.setEND_DAY(calendar.get(Calendar.DAY_OF_MONTH));
		
		final String time = mBirthday.getText().toString();
		try {
			if (JudgeDate.isDate(time, "yyyy.MM.dd")) {
				calendar.setTime(dateFormat.parse(time));
				int year = calendar.get(Calendar.YEAR);
				int month = calendar.get(Calendar.MONTH);
				int day = calendar.get(Calendar.DAY_OF_MONTH);
				//设置过的生日比当前日期大
				if(year > currYear || year == currYear && month > currMonth || year == currYear && month == currMonth && day > currDay) {
					timeBiggerThanNow = true;
					wheelMain.initDateTimePicker(currYear, currMonth, currDay);
				} else//不比当前日期大
					wheelMain.initDateTimePicker(year, month, day);
			} else//日期格式不正确
				wheelMain.initDateTimePicker(currYear, currMonth, currDay);
		} catch (ParseException e) {
			//格式转换时出错
			wheelMain.initDateTimePicker(currYear, currMonth, currDay);
			e.printStackTrace();
		}
		
		final Dialog dialog = new Dialog(PersonalInfoActivity.this, R.style.MyMarketDialog);
		TextView mTextView = (TextView) timepickerview.findViewById(R.id.timeerror);
		if(timeBiggerThanNow)
			mTextView.setVisibility(View.VISIBLE);
		else
			mTextView.setVisibility(View.GONE);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setContentView(timepickerview);
		timepickerview.findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mBirthday.setText(wheelMain.getTime());
				dialog.dismiss();
				mProgressDialog.setMessage(getResources().getString(R.string.personalInfo_waiting_for_result));
				mProgressDialog.show();
				if(mEditUserInfo == null)
					(mEditUserInfo = new EditUserInfo()).execute();
			}
		});
		timepickerview.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	
	/**
	 * 头像信息保存到文件
	 * @param is
	 */
	public void saveToFile(Bitmap bitmap) {
		if(!new File(LOGO_PATH).exists()) {
			new File(LOGO_BASE_PATH).mkdirs();
		} else if(!new File(LOGO_BASE_PATH).exists()){
			new File(LOGO_BASE_PATH).mkdir();
		}
		
		File file = new File(logoTempPath);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
			out.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	

	/**
	 * 提交请求，更改个人信息
	 */
	public class EditUserInfo extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			if(isCancelled()) 
				return null;
			JSONObject object = new JSONObject();
			File file;
			FormFile formFile = null;   
			try {
				if ((file = new File(LOGO_ZOOM_FILE_PATH)).exists()) {
					formFile = new FormFile(file.getName(), file, "avatar", null);
				} else if ((file = new File(LOGO_FILE_PATH)).exists())
					formFile = new FormFile(file.getName(), file, "avatar", null);
				object.put("selfeditname", mNickName.getText());
				object.put("gender", mGender.getText());
				object.put("birthday", mBirthday.getText());
				
				Map<String, String> mParams = new HashMap<String, String>();
				sign = MD5Util.md5(openId + token + Constant.SIGNKEY);
				mParams.put("sign", sign);
				mParams.put("openid", openId);
				mParams.put("token", token);	
				mParams.put("data", object.toString());
				return SocketHttpRequester.postExternalFile(Constant.ZHUOYOU_EDIT_USER_INFO, mParams, formFile);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if(mProgressDialog != null && mProgressDialog.isShowing())
				mProgressDialog.dismiss();
			try {
				File file = new File(LOGO_ZOOM_FILE_PATH);
				if(result != null) {
					JSONObject object = new JSONObject(result);
					if(object.getInt("result") == 0) {//修改成功
						if(file.exists())
							file.renameTo(new File(LOGO_FILE_PATH));
						Toast.makeText(PersonalInfoActivity.this, getResources().getString(R.string.personalInfo_success), Toast.LENGTH_SHORT).show();
						nickName = mNickName.getText() + "";
						gender = mGender.getText() + "";
						birthday = mBirthday.getText() + "";
					} else {//修改失败
						setUserInfoShow();
						if(file.exists())
							file.delete();
						Toast.makeText(PersonalInfoActivity.this, getResources().getString(R.string.personalInfo_fail), Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(getApplicationContext(), R.string.server_exception, Toast.LENGTH_SHORT).show();
					setUserInfoShow();
					if(file.exists())
						file.delete();
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			mEditUserInfo = null;
		}
		
	}
	
	
	public void setUserInfoShow() {
		mNickName.setText((nickName == null) ? getString(R.string.personalInfo_tip_unselected) : nickName);
		mGender.setText((gender == null) ? getString(R.string.personalInfo_tip_unselected) : gender);
		mBirthday.setText((birthday == null) ? getString(R.string.personalInfo_tip_unselected) : birthday);
		Bitmap bitmap = BitmapFactory.decodeFile(LOGO_FILE_PATH);
		if(bitmap != null) {
			userLogo = AsyncImageCache.getCroppedRoundBitmap(bitmap, (int)getResources().getDimension(R.dimen.myself_user_logo_width));
			mImageView_UserLogo.setImageBitmap(AsyncImageCache.addBorder(userLogo));
			bitmap.recycle();
			bitmap = null;
		}
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		deletePhotoAtPathAndName(logoTempPath);
		if (mGetUserInfo != null && mGetUserInfo.getStatus() == AsyncTask.Status.RUNNING) {
			mGetUserInfo.cancel(true);
			mGetUserInfo = null;
		}
		if(mEditUserInfo != null && mEditUserInfo.getStatus() == AsyncTask.Status.RUNNING) {
			mEditUserInfo.cancel(true);
			mEditUserInfo = null;
		}
		if(userLogo != null && !userLogo.isRecycled()) {
			userLogo.recycle();
			userLogo = null;
		}
		if(mUserlogoBackDrawable != null) {
			mUserlogoBackDrawable.setCallback(null);
			mUserlogoBackDrawable = null;
		}
		if(mUserlogoBackBitmap != null && !mUserlogoBackBitmap.isRecycled()) {
			mUserlogoBackBitmap.recycle();
			mUserlogoBackBitmap = null;
		}
		if(mHandler != null)
			mHandler.removeCallbacksAndMessages(null);
		
	}
	
	
	public void deletePhotoAtPathAndName(String path) {
		File file = new File(path);
		if(file.exists())
			 file.delete();
	}
	
	
	/**
	 * 弹出对话框
	 * @param type 0-设置昵称 1-退出账号
	 */
	public void showCommonDialog(final int type) {
		final Dialog dialog = new Dialog(PersonalInfoActivity.this, R.style.MyMarketDialog);
		View view = View.inflate(this, R.layout.personalinfo_general_dialog, null);
		
		final Button confirm = (Button) view.findViewById(R.id.ok);
		Button cancel = (Button) view.findViewById(R.id.cancel);
		RelativeLayout ll_nickname = (RelativeLayout) view.findViewById(R.id.ll_inputNickname);
		ImageView img_delete = (ImageView) view.findViewById(R.id.delete_nickname);
		TextView logout_tip = (TextView) view.findViewById(R.id.text_logout);
		final EditText edit_nickname = (EditText) view.findViewById(R.id.edit_nickname);
		if(type == 0) {
			ll_nickname.setVisibility(View.VISIBLE);
			edit_nickname.setText(mNickName.getText());
			edit_nickname.setSelection(mNickName.getText().length());
			edit_nickname.requestFocus();
			popupInput(edit_nickname, true);//弹出输入法
			edit_nickname.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) { }
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
				
				@Override
				public void afterTextChanged(Editable s) {
					if(s.length() < 2) {
						if(s.length() == 1) {
							if(edit_nickname.getText().toString().contains(" "))
								edit_nickname.setText("");
						}
						confirm.setTextColor(getResources().getColor(R.color.account_hint_color));
						confirm.setEnabled(false);
					} else if(s.length() > 24){
						confirm.setTextColor(getResources().getColor(R.color.common_green_color));
						s.delete(edit_nickname.getSelectionStart() - 1, edit_nickname.getSelectionEnd());
						edit_nickname.setText(s);
						edit_nickname.setSelection(edit_nickname.getText().length());
					} else {
						confirm.setTextColor(getResources().getColor(R.color.common_green_color));
						confirm.setEnabled(true);
					}
				}
			});
		} else {
			confirm.setTextColor(getResources().getColor(R.color.common_green_color));
			logout_tip.setVisibility(View.VISIBLE);
		}
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		
		
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				popupInput(edit_nickname, false);
				dialog.dismiss();
			}
		});
		
		confirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				popupInput(edit_nickname, false);
				dialog.dismiss();
				if(type == 0) {
					mNickName.setText(edit_nickname.getText());
					mProgressDialog.setMessage(getResources().getString(R.string.personalInfo_waiting_for_result));
					mProgressDialog.show();
					if(mEditUserInfo == null)
						(mEditUserInfo = new EditUserInfo()).execute();
				} else if(type == 1) {
					logout();
				}
			}
		});
		
		img_delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				edit_nickname.setText("");
			}
		});
	}
	
	
	/**
	 * 退出账号
	 */
	public void logout() {
		mProgressDialog.setMessage(getResources().getString(R.string.exit_account_exiting));
		mProgressDialog.show();
		AccountManager mAccountManager = AccountManager.get(getApplicationContext());
		Account[] accounts = mAccountManager.getAccountsByType(com.market.account.constant.Constant.ACCOUNT_TYPE);
		if (accounts != null && accounts.length >= 1) {
			mAccountManager.removeAccount(accounts[0], new AccountManagerCallback<Boolean>() {

						@Override
						public void run(AccountManagerFuture<Boolean> future) {
							mProgressDialog.dismiss();
							BaseActivity_Html5.deleteUserLogo();
							com.market.account.authenticator.AccountManager.getInstance().onAccountLogout();
							finish();
						}
					}, mHandler);
		} else {
			mProgressDialog.dismiss();
		}
	}
	
	
	public class MyBaseAdapter extends BaseAdapter {
		
		private String[] data;
		private int type;
		private LayoutInflater inflater;
		
		public MyBaseAdapter(String[] data, int type) {
			this.data = data;
			this.type = type;
			inflater = LayoutInflater.from(PersonalInfoActivity.this);
		}

		@Override
		public int getCount() {
			return data.length;
		}

		@Override
		public Object getItem(int position) {
			return data[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = inflater.inflate(R.layout.personalinfo_dialog_item, null);
			TextView mTextView = (TextView) convertView.findViewById(R.id.option);
			mTextView.setText(data[position]);
			if(type == 2)
				if(mGender.getText().equals(data[0]) && position == 0 || mGender.getText().equals(data[1]) && position == 1 || mGender.getText().equals(data[2]) && position == 2)
					mTextView.setTextColor(getResources().getColor(R.color.common_green_color));
			return convertView;
		}
		
	}
	
	
	public void popupInput(final View view, boolean show) {
		Timer timer = new Timer(); // 设置定时器
		final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if(show) {
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					imm.showSoftInput(view, InputMethodManager.RESULT_SHOWN);
				}
			}, 300);
		} else {
			if (view.getWindowToken() != null) { 
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);   
			} 
		}
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("logoTempPath", logoTempPath);
	}
	
}

