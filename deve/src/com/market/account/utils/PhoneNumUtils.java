package com.market.account.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumUtils {

	public static boolean isPhoneNumberValid(String mobiles) {

		// Pattern p =
		// Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		Pattern p = Pattern.compile("^1[34578]{1}[0-9]{1}[0-9]{8}$");

		Matcher m = p.matcher(mobiles);

		return m.matches();

	}


	/**
	 * user java reg to check phone number and replace 86 or +86 only check
	 * start with "+86" or "86" ex +8615911119999 13100009999 replace +86 or 86
	 * with ""
	 * 
	 * @param phoneNum
	 * @return
	 * @throws Exception
	 */
	public static Long checkPhoneNum(String phoneNum) throws Exception {

		Pattern p1 = Pattern.compile("^((\\+{0,1}86){0,1})1[0-9]{10}");
		Matcher m1 = p1.matcher(phoneNum);
		if (m1.matches()) {
			Pattern p2 = Pattern.compile("^((\\+{0,1}86){0,1})");
			Matcher m2 = p2.matcher(phoneNum);
			StringBuffer sb = new StringBuffer();
			while (m2.find()) {
				m2.appendReplacement(sb, "");
			}
			m2.appendTail(sb);
			return Long.parseLong(sb.toString());

		} else {
			throw new Exception("The format of phoneNum " + phoneNum
					+ "  is not correct!Please correct it");
		}

	}
}
