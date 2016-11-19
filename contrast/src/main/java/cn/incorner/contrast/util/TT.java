package cn.incorner.contrast.util;

import android.content.Context;
import android.widget.Toast;

/**
 * 信息提示工具
 * 
 * @author yeshimin
 */
public class TT {

	public static final int LONG = Toast.LENGTH_LONG;
	public static final int SHORT = Toast.LENGTH_SHORT;

	public static void show(Context context, String content) {
		Toast.makeText(context, content, SHORT).show();
	}

	public static void show(Context context, int content) {
		Toast.makeText(context, content, SHORT).show();
	}

	public static void show(Context context, String content, int length) {
		Toast.makeText(context, content, length).show();
	}

}
