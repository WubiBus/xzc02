package cn.incorner.contrast.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

/**
 * Preference工具类
 * 
 * @author yeshimin
 */
public class PrefUtil {
	
	private static final String TAG = "PrefUtils";
	private static SharedPreferences pref;
	private static SharedPreferences.Editor editor;

	/**
	 * 初始化 （！！！必须先调用此方法 ！！！）
	 */
	public static void init(Context context, String prefName) {
		if ((pref == null || editor == null) 
				&& (context != null) && (prefName != null)) {
			pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
			editor = pref.edit();
			editor.commit();
		} else {
			Log.e(TAG, "init failed");
		}
	}
	
	/**
	 * 读写数据，String版本
	 */
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	public static void setStringValue(String key, String value) {
		if (pref == null || editor == null) {
			Log.e(TAG, "not init, please call init() first");
			return;
		}
		
		if (TextUtils.isEmpty(key) || value == null) {
			Log.w(TAG, "key or value can not be null or empty");
		}
		
		editor.putString(key, value);
		editor.commit();
	}
	
	public static String getStringValue(String key) {
		if (pref == null || editor == null) {
			Log.e(TAG, "not init, please call init() first");
			return null;
		}
		
		if (TextUtils.isEmpty(key)) {
			Log.w(TAG, "key or value can not be null or empty");
			return "";
		}
		
		return pref.getString(key, "");
	}
	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	
	/**
	 * 读写数据，int版本
	 */
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	public static void setIntValue(String key, int value) {
		if (pref == null || editor == null) {
			Log.e(TAG, "not init, please call init() first");
			return;
		}
		
		if (TextUtils.isEmpty(key)) {
			Log.w(TAG, "key can not be null or empty");
			return;
		}
		
		editor.putInt(key, value);
		editor.commit();
		
	}
	
	public static int getIntValue(String key) {
		if (pref == null || editor == null) {
			Log.e(TAG, "not init, please call init() first");
			return 0;
		}
		
		if (TextUtils.isEmpty(key)) {
			Log.w(TAG, "key can not be null or empty");
			return 0;
		}
		
		return pref.getInt(key, 0);
	}
	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	
	/**
	 * 读写数据，boolean版本
	 */
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	public static void setBooleanValue(String key, boolean value) {
		if (pref == null || editor == null) {
			Log.e(TAG, "not init, please call init() first");
			return;
		}
		
		if (TextUtils.isEmpty(key)) {
			Log.w(TAG, "key can not be null or empty");
			return;
		}
		
		editor.putBoolean(key, value);
		editor.commit();
		
	}
	
	public static boolean getBooleanValue(String key) {
		if (pref == null || editor == null) {
			Log.e(TAG, "not init, please call init() first");
			return true;
		}
		
		if (TextUtils.isEmpty(key)) {
			Log.w(TAG, "key can not be null or empty");
			return true;
		}
		
		return pref.getBoolean(key, false);
	}
	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	
}
