package com.market.account.authenticator;

import java.util.HashSet;
import java.util.Set;

import com.market.account.authenticator.AccountUpdate.IAccountListener;

/**
 * 帐号登录/登出通知者
 * @author Huangyn
 *
 */
public class AccountManager {
	
	private Set<IAccountListener> mAccountListenerSet;
	private static AccountManager mAccountManager;
	
	private AccountManager() {
	}
	
	
	public static AccountManager getInstance() {
		if(mAccountManager == null) {
			mAccountManager = new AccountManager();
		}
		return mAccountManager;
	}
	
	
	protected void addAccountListener(IAccountListener accountListener) {
		if(accountListener != null) {
			if(mAccountListenerSet == null) {
				mAccountListenerSet = new HashSet<AccountUpdate.IAccountListener>();
			}
			mAccountListenerSet.add(accountListener);
		}
	}
	
	
	protected void removeAccountListener(IAccountListener accountListener) {
		if(accountListener != null && mAccountListenerSet != null) {
			if(mAccountListenerSet.contains(accountListener)) {
				mAccountListenerSet.remove(accountListener);
				if(mAccountListenerSet.isEmpty()) {
					mAccountListenerSet = null;
					mAccountManager = null;
				}
			}
		}
	}
	
	
	/**
	 *	登录帐号成功后,调用此方法通知所有监听者
	 */
	public void onAccountLogin() {
		if(mAccountListenerSet != null) {
			for(IAccountListener accountListener : mAccountListenerSet) {
				accountListener.onLogin();
			}
		}
	}
	

	
	/**
	 *	注销帐号后,调用此方法通知所有监听者
	 */
	public void onAccountLogout() {
		if(mAccountListenerSet != null) {
			for(IAccountListener accountListener : mAccountListenerSet) {
				accountListener.onLogout();
			}
		}
	}
	
	
}
