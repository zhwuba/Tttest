package com.market.account.user;

/**
 * Class User.
 * 
 * 2013-3-16
 * 
 * @author lsun
 */

public class User {

	/** if use telphone to login, username is telephone number */
	private String nickname;
	private String username;
	private String password;
	private String regtype;
	private int result = -1;
	private String desc;
	private String UID;
	private String TOKEN;
	private int recode = 0;
	private String logoUrl;
	private String expires_in;
	private int level;
	private String openKey;
	private String openid;
	private String gender;
	private int age;
	private String birthday;
	private String receive_name;
	private String receive_phone;
	private String receive_address;


	public String getBirthday() {
		return birthday;
	}


	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}


	public String getReceive_name() {
		return receive_name;
	}


	public void setReceive_name(String receive_name) {
		this.receive_name = receive_name;
	}


	public String getReceive_phone() {
		return receive_phone;
	}


	public void setReceive_phone(String receive_phone) {
		this.receive_phone = receive_phone;
	}


	public String getReceive_address() {
		return receive_address;
	}


	public void setReceive_address(String receive_address) {
		this.receive_address = receive_address;
	}


	public String getNickname() {
		return nickname;
	}


	public void setNickname(String nickname) {
		this.nickname = nickname;
	}


	public String getOpenid() {
		return openid;
	}


	public void setOpenid(String openid) {
		this.openid = openid;
	}


	public String getOpenKey() {
		return openKey;
	}


	public void setOpenKey(String openKey) {
		this.openKey = openKey;
	}


	public int getLevel() {
		return level;
	}


	public void setLevel(int level) {
		this.level = level;
	}


	public String getGender() {
		return gender;
	}


	public void setGender(String gender) {
		this.gender = gender;
	}


	public int getAge() {
		return age;
	}


	public void setAge(int age) {
		this.age = age;
	}


	public String getExpires_in() {
		return expires_in;
	}


	public void setExpires_in(String expires_in) {
		this.expires_in = expires_in;
	}


	public int getRecode() {
		return recode;
	}


	public void setRecode(int recode) {
		this.recode = recode;
	}


	public String getLogoUrl() {
		return logoUrl;
	}


	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getRegtype() {
		return regtype;
	}


	public void setRegtype(String regtype) {
		this.regtype = regtype;
	}


	public int getResult() {
		return result;
	}


	public void setResult(int result) {
		this.result = result;
	}


	public String getDesc() {
		return desc;
	}


	public void setDesc(String desc) {
		this.desc = desc;
	}


	public String getUID() {
		return UID;
	}


	public void setUID(String uID) {
		UID = uID;
	}


	public String getTOKEN() {
		return TOKEN;
	}


	public void setTOKEN(String tOKEN) {
		TOKEN = tOKEN;
	}


	public User() {

	}

}
