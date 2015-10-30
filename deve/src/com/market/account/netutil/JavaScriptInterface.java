package com.market.account.netutil;

public interface JavaScriptInterface {

	public void zhuoyou_login_logout();

	/**	update userinfo	**/
	public void zhuoyou_login_update_userinfo(String userInfo);

	/** share to weixin screenshot	**/
	public void zhuoyou_login_shareToWXTimeLine();
	
	/** share to weibo screenshot	**/
	public void zhuoyou_login_shareToWeibo();

	/**
	 * share to weixin 
	 * @param desc	the description of share
	 * @param share_url the linkUrl of share
	 */
	public void zhuoyou_login_shareToWXTimeLine(String desc, String share_url);
	
	/**
	 * share to weibo 
	 * @param desc	the description of share
	 * @param share_url the linkUrl of share
	 */
	public void zhuoyou_login_shareToWeibo(String desc, String share_url);

	/**
	 * open a new activity that contains a webview
	 * @param url	the url of webview loaded
	 * @param titleName	the activity's title name on the top
	 * @param callback	when activity closed,this method will callback 
	 */
	public void zhuoyou_login_goto(String url,String titleName, String callback);
	
	/**
	 * open a new activity that contains a webview
	 * @param url	the url of webview loaded
	 * @param titleName	the activity's title name on the top
	 */
	public void zhuoyou_login_goto(String url,String titleName);

	/**
	 * query a apk's download state
	 * @param pkgName	apk's package name
	 * @param verCode	apk's version code
	 * @return	the download state of the apk
	 * 
	 * 	<p> STATE_READY = 0;<p>
		<p> STATE_DOWNLOADING = STATE_READY + 1;</p>
		<p> STATE_NETWORK_DISCONNECT = STATE_DOWNLOADING + 1;</p>
		<p> STATE_DOWNLOAD_PAUSE = STATE_NETWORK_DISCONNECT + 1;</p>
		<p> STATE_DOWNLOAD_FAILED = STATE_DOWNLOAD_PAUSE + 1;</p>
		<p> STATE_DOWNLOAD_COMPLETE = STATE_DOWNLOAD_FAILED + 1;</p>
		<p> STATE_INSTALLING = STATE_DOWNLOAD_COMPLETE + 1;</p>
		<p> STATE_INSTALLED = STATE_INSTALLING + 1;</p>
		<p> STATE_INSTALL_FAILED = STATE_INSTALLED + 1;</p>
		<p> STATE_CANCEL = STATE_INSTALL_FAILED + 1;</p>
	 */
	public int zhuoyou_login_download_state(String pkgName,String verCode);

	
	public String get_openid();
	public String get_token();
	
	/** get MD5 sign **/
	public String generate_sign(String params);

	/**	goto login activity	**/
	public void zhuoyou_login_authenticator();

	/** start app	**/
	public void zhuoyou_login_start_app(String pkgName);

	/**	close this activity	**/
	public void zhuoyou_login_close();

	/** return the apk's version code by package name **/
	public int zhuoyou_login_getApkVersionCode(String packageName);

	/** update the userinfo	**/
	public void zhuoyou_login_update_userinfo();

	/**
	 * bind a third account
	 * @param utype must = openqq / openweibo
	 */
	public void zhuoyou_login_auth(String utype);

	/**
	 * begin download apk by apk info
	 * @param info	must a json format
	 * @return
	 */
	public int zhuoyou_login_download(String info);

	public int zhuoyou_login_apk_detail(String info, int score);

	public void zhuoyou_login_update_title(String title);

	public int zhuoyou_login_download_progress(String info);
	
	/**
	 * 详情界面分享应用
	 */
	public void zhuoyou_login_share_app(String appName, String shareUrl);
	
	
	/**
	 * 收藏应用
	 * @return	
	 * <li>true ：收藏成功
	 * <li>false ：收藏失败
	 */
	boolean zhuoyou_login_app_collect(boolean isCollect,String appInfo);
	
	/**
	 * 获取搜藏状态
	 * @return 
	 * <li>true : 已收藏
	 * <li>false : 未收藏
	 */
	public boolean zhuoyou_login_get_collect_status(String packageName);

	/**
	 * 获取手机 uuid
	 * @return
	 */
	public String zhuoyou_login_get_uuid();
	
	/**
	 * 是否有等待发放的奖品
	 * @param hasGift
	 * @return 
	 * <li>true : 有
	 * <li>false : 无
	 */
	public void zhuoyou_login_hasGift_receive();
	
	
	/**
	 * 获取跳转地址
	 * @param url
	 */
	public void zhuoyou_present(String url);
	
	/**
	 * 是否显示跳转按钮
	 * @param show
	 */
	public void zhuoyou_present_show(boolean show);
	
	
	/**
	 * 复制文本
	 */
	public boolean zhuoyou_copy_text(String needCopy);
	
	/**
	 * 绑定手机号结果
	 * @param result
	 * @param phone
	 */
	public void zhuoyou_bindMobile_result(boolean result, String phone);
	
	public void zhuoyou_javascript_available();
	public void zhuoyou_web_refresh();
	
	public int zhuoyou_show_image();
	
	public String zhuoyou_get_marketInfo();
}
