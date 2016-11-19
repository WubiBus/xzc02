package cn.incorner.contrast;

import org.xutils.x;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import cn.incorner.contrast.util.PrefUtil;

/**
 * 基类Activity
 * 
 * @author yeshimin
 */
public class BaseActivity extends Activity {

	public static int[] BG_COLOR;
	public static int[] TEXT_COLOR;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		x.view().inject(this);
	}

	/**
	 * 是否已登录
	 */
	public boolean isLogined() {
		return PrefUtil.getBooleanValue(Config.PREF_IS_LOGINED);
	}

	public static boolean sIsLogined() {
		return PrefUtil.getBooleanValue(Config.PREF_IS_LOGINED);
	}

	/**
	 * 跳转页面（静态方法） MainActivity继承自FragmentActivity，不想对它进行修改，所以添加这个方法以供调用
	 */
	public static void sGotoActivity(Context context, Class<? extends Activity> cls) {
		sGotoActivity(context, cls, R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
	}

	/**
	 * 跳转页面，可以指定跳转动画（静态方法） MainActivity继承自FragmentActivity，不想对它进行修改，所以添加这个方法以供调用
	 */
	public static void sGotoActivity(Context context, Class<? extends Activity> cls, int enterAnim,
			int exitAnim) {
		Intent intent = new Intent();
		intent.setClass(context, cls);
		sGotoActivity(context, intent, enterAnim, exitAnim);
	}

	/**
	 * 跳转页面(intent,静态)
	 */
	public static void sGotoActivity(Context context, Intent intent) {
		sGotoActivity(context, intent, R.anim.anim_slide_in_from_right,
				R.anim.anim_slide_out_to_left);
	}

	/**
	 * 跳转页面(intent,静态),可以指定跳转动画
	 */
	public static void sGotoActivity(Context context, Intent intent, int enterAnim, int exitAnim) {
		context.startActivity(intent);
		((Activity) context).overridePendingTransition(enterAnim, exitAnim);
	}

	/**
	 * 带（默认）动画效果的finish（静态方法）
	 */
	public static void sFinishActivity(Context context) {
		((Activity) context).finish();
		((Activity) context).overridePendingTransition(R.anim.anim_slide_in_from_left,
				R.anim.anim_slide_out_to_right);
	}

	/**
	 * 跳转页面
	 */
	public void gotoActivity(Class<? extends Activity> cls) {
		gotoActivity(cls, R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
	}

	/**
	 * 跳转页面，可以指定跳转动画
	 */
	public void gotoActivity(Class<? extends Activity> cls, int enterAnim, int exitAnim) {
		Intent intent = new Intent();
		intent.setClass(this, cls);
		gotoActivity(intent, enterAnim, exitAnim);

	}

	/**
	 * 跳转页面(intent)
	 */
	public void gotoActivity(Intent intent) {
		gotoActivity(intent, R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
	}

	/**
	 * 跳转页面(intent),可以指定跳转动画
	 */
	public void gotoActivity(Intent intent, int enterAnim, int exitAnim) {
		startActivity(intent);
		overridePendingTransition(enterAnim, exitAnim);
	}

	/**
	 * 添加默认的退出动画
	 */
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// 设置退出动画
		overridePendingTransition(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
