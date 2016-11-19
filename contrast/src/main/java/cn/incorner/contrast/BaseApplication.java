package cn.incorner.contrast;

import org.xutils.BuildConfig;
import org.xutils.x;

import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.PrefUtil;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.umeng.socialize.PlatformConfig;

/**
 * 基类 Application
 * 
 * @author yeshimin
 */
public class BaseApplication extends Application {

	private static final String TAG = "BaseApplication";

	public static Context context;

	@Override
	public void onCreate() {
		super.onCreate();

		context = this;

		// 打开打印调试信息开关
		DD.setDebug(true);

		initScreenArgs();

		PrefUtil.init(context, "contrast");
		// 初始化 Universal-Image-Loader
		initUniversalImageLoader();
		// 初始化xUtils框架
		initXUtils();
		// 初始化友盟
		initUmeng();
	}

	/**
	 * 初始化屏幕宽高
	 */
	private void initScreenArgs() {
		DisplayMetrics outMetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(outMetrics);
		Constant.SCREEN_WIDTH = outMetrics.widthPixels;
		Constant.SCREEN_HEIGHT = outMetrics.heightPixels;
	}

	/**
	 * 初始化 Universal-Image-Loader
	 */
	private void initUniversalImageLoader() {

		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point pOutSize = new Point();
		display.getSize(pOutSize);
		int screenWidth = pOutSize.x;
		int screenHeight = pOutSize.y;

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
				.diskCacheExtraOptions(screenWidth, screenHeight, null)
				.denyCacheImageMultipleSizesInMemory()
				.memoryCache(new LruMemoryCache(2 * 1024 * 1024)).memoryCacheSize(2 * 1024 * 1024)
				.diskCacheSize(50 * 1024 * 1024).diskCacheFileCount(100).writeDebugLogs() // TODO
																							// remove
																							// for
																							// release
																							// app
				.build();
		ImageLoader.getInstance().init(config);
	}

	/**
	 * 初始化xUtils框架
	 */
	private void initXUtils() {
		x.Ext.init(this);
		x.Ext.setDebug(BuildConfig.DEBUG);
	}

	/**
	 * 初始化Umeng
	 */
	private void initUmeng() {
		PlatformConfig.setWeixin("wxe0e4535ab158dc60", "a57c010e8777c6cf9cd2b800d0061915");
		PlatformConfig.setSinaWeibo("2733324896", "24e35635cd89aa89cad3869ebcf09542");
		PlatformConfig.setQQZone("1105270229", "1Pz3e5iBtKhWicxu");
	}

}
