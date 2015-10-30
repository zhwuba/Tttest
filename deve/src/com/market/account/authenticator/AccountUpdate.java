package com.market.account.authenticator;

/**
 * 帐号登录状态监听类
 * @author Huangyn
 * @see 建议在Activity的onCreate()方法中注册的监听,在OnDestory()方法中注销监听
 */
public class AccountUpdate {

	private AccountManager mAccountManager;
	private IAccountListener mAccountListener;
	
	public AccountUpdate() {
		mAccountManager = AccountManager.getInstance();
	}
	
	public void unregisterUpdateListener() {
		if(mAccountListener != null) {
			mAccountManager.removeAccountListener(mAccountListener);
		}
	}
	
	
	public void registerUpdateListener(IAccountListener accountListener) {
		if(accountListener != null) {
			mAccountListener = accountListener;
			mAccountManager.addAccountListener(mAccountListener);
		}
		
	}


	public interface IAccountListener{

		public void onLogin();

		public void onLogout();
	}



}
