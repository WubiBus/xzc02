package cn.incorner.contrast;

import java.io.File;

import android.os.Environment;

public class Constant {
	public static int SCREEN_WIDTH;//屏幕宽度
	public static int SCREEN_HEIGHT;//屏幕高度
	
	//头像放置路径
		public static final String PICTURE_TMP_PATH = Environment
				.getExternalStorageDirectory().getPath()
				+ File.separator
				+ "constarts";
		
		// SDCard路径
		public static final String SD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

		// 图片存储路径
		public static final String BASE_PATH = SD_PATH + "/YiHui/";
		 // 缓存图片路径
		public static final String BASE_IMAGE_CACHE = BASE_PATH + "cache/images/";
		
		public static final String USER_NICKNAME = "皮条";
		public static String USER_ID;
		public static String USER_TOKEN;
		

	    /** 是否第一次启动 */
	    public static final String IS_FIRST = "is_first";
	 
}
