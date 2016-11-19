package cn.incorner.contrast.util;

import android.util.Log;

/**
 * 调试 Log 辅助类
 * 
 * @author yeshimin
 */
public class DD {
	
	private static boolean isDebug = false;
	
	public static void setDebug(boolean debug) {
		isDebug = debug;
	}
	
	private static boolean isDebugMode() {
		return isDebug;
	}

	/**
	 * 调用系统Log
	 * @param tag 标签
	 * @param msg 信息
	 */
	public static void d(String tag, String msg) {
		if (!isDebugMode()) return;
		
		Log.d(tag, msg);
	}
	
	/**
	 * msg 类型为 long 的版本
	 * @param tag 标签
	 * @param msg 信息
	 */
	public static void d(String tag, long msg) {
		d(tag, String.valueOf(msg));
	}
	
	/**
	 * msg 类型为 boolean 的版本
	 * @param tag 标签
	 * @param msg 信息
	 */
	public static void d(String tag, boolean msg) {
		d(tag, String.valueOf(msg));
	}

}
