package com.market.account.utils;

import java.util.HashMap;

import com.zhuoyi.encoder.BaseUtils;

public class EncoderAndDecoder {
	private static final String orig_str = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ/+";
	private static final String crypt_str = "FL9fIYOiWUyz/Jr57g8dMtRVaGvX0lm6BwjkxeQ231qSZnEAHpo4+PCscubNhKDT";
	private static final int length = 64;
	private static final char[] orig_chars = { 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 
		'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 
		'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 
		'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 
		'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 
		'Y', 'Z', '+', '/' };

	private static final char[] crypt_chars = { 
		'F', 'L', '9', 'f', 'I', 'Y', 'O', 'i', 'W', 'U', 
		'y', 'z', '/', 'J', 'r', '5', '7', 'g', '8', 'd', 
		'M', 't', 'R', 'V', 'a', 'G', 'v', 'X', '0', 'l', 
		'm', '6', 'B', 'w', 'j', 'k', 'x', 'e', 'Q', '2', 
		'3', '1', 'q', 'S', 'Z', 'n', 'E', 'A', 'H', 'p', 
		'o', '4', '+', 'P', 'C', 's', 'c', 'u', 'b', 'N', 
		'h', 'K', 'D', 'T' };

	private static HashMap<Character, Character> getEncryptMap() {
		HashMap map = new HashMap();
		for (int i = 0; i < "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ/+".length(); ++i) {
			map.put(Character.valueOf(orig_chars[i]), Character.valueOf(crypt_chars[i]));
		}
		return map;
	}

	private static HashMap<Character, Character> getDecryptMap() {
		HashMap map = new HashMap();
		for (int i = 0; i < "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ/+".length(); ++i) {
			map.put(Character.valueOf(crypt_chars[i]), Character.valueOf(orig_chars[i]));
		}
		return map;
	}

	public static String encrypt(String inputStr) {
		if ((inputStr == null) || ("".equals(inputStr))) {
			return inputStr;
		}

		String tempStr = BaseUtils.encodeByBase64(inputStr);

		if(tempStr.indexOf("=")!=-1){

			tempStr = tempStr.substring(0, tempStr.indexOf("="));
		}


		char[] dstChars = new char[tempStr.length()];
		char[] srcChars = new char[tempStr.length()];
		tempStr.getChars(0, tempStr.length(), srcChars, 0);
		HashMap map = getEncryptMap();
		for (int i = 0; i < srcChars.length; ++i) {
			try{
				dstChars[i] = ((Character)map.get(Character.valueOf(srcChars[i]))).charValue();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		String result = String.copyValueOf(dstChars);
		return result;
	}

	public static String decrypt(String inputStr) {
		if ((inputStr == null) || ("".equals(inputStr))) {
			return inputStr;
		}
		char[] dstChars = new char[inputStr.length()];
		char[] srcChars = new char[inputStr.length()];

		inputStr.getChars(0, inputStr.length(), srcChars, 0);
		HashMap map = getDecryptMap();
		for (int i = 0; i < srcChars.length; ++i) {
			dstChars[i] = ((Character)map.get(Character.valueOf(srcChars[i]))).charValue();
		}
		String tempStr = String.copyValueOf(dstChars);
		int remainder = tempStr.length() % 4;
		if (remainder == 2)
			tempStr = tempStr + "==";
		else if (remainder == 3) {
			tempStr = tempStr + "=";
		}
		String result = BaseUtils.decodeByBase64(tempStr);
		return result;
	}
}