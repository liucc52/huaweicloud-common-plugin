package com.huawei.octopus.jobstatusplugin.util;

import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommonUtils {

	private static final Logger _log = Logger.getLogger(CommonUtils.class.getName());

	public static boolean isNullOrEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static boolean isNullOrEmpty(Object obj) {
		return obj == null || String.valueOf(obj).trim().isEmpty();
	}

	public static String replaceNull(String str) {
		return isNullOrEmpty(str) ? "" : str;
	}

	public static boolean isNullOrEmptyList(List<?> list) {
		if (list != null && !list.isEmpty()) {
			return false;
		}
		return true;
	}

	public static <T> boolean isNullOrEmptyArray(T[] array) {
		if (array != null && array.length > 0) {
			return false;
		}
		return true;
	}

	public static boolean isNullOrEmptyMap(Map<?, ?> map) {
		if (map == null || map.isEmpty()) {
			return true;
		}
		return false;
	}


	public static String formatDateToUtcStr(Date date) {
		if (CommonUtils.isNullOrEmpty(date)) {
			return null;
		}
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df2.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		return df2.format(date);
	}

	public static Date formatUtcStringToDate(String utc, boolean dayAdd1) {
		if (CommonUtils.isNullOrEmpty(utc)) {
			return null;
		}
		int onyDayLength = "yyyy-MM-dd".length();
		Date date = null;
		// 如果传入的字符串不含时分秒，则判断转换结果是否需要加一天
		if (utc.length() == onyDayLength) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			try {
				date = sdf.parse(utc);
			} catch (ParseException e) {
				_log.error("format date faild:" + utc);
			}
			if (dayAdd1) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				cal.add(cal.DATE, 1);
				date = cal.getTime();
			}
		} else {
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			df2.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			try {
				Date d = df2.parse(utc);
				return d;
			} catch (ParseException e) {
				_log.error("format utcdate faild:" + utc);
			}
		}
		return date;
	}

	public static void main(String[] args) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = sdf.parse("2017-09-28");
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(cal.DATE, 1);
			date = cal.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(date);

	}

	public static String formatStandardDate(Date longDate) {
		if (longDate == null)
			return "";

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(longDate);
	}

	public static String formatLongDate(Date date) {
		if (null == date) {
			date = new Date();
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		return format.format(date);
	}

	public static String formatWithquote(String[] array, String quote) {
		if (array == null || array.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String str : array) {
			sb.append(quote + str + quote + ",");
		}
		sb.delete(sb.lastIndexOf(","), sb.length());
		return sb.toString();
	}
}