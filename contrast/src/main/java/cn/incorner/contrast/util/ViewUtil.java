package cn.incorner.contrast.util;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

public class ViewUtil {
	  /**
     * 打开键盘
     *
     * @param context
     * @param view
     */
    public static void openKeyBoard(Context context, View view) {
        InputMethodManager imm = null;
        if (context != null) {
            imm = (InputMethodManager) context.getSystemService(Service.INPUT_METHOD_SERVICE);
        }
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

    /**
     * 隐藏界面标题栏
     *
     * @param activity
     */
    public static void hideTitle(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    /**
     * 全屏
     *
     * @param activity
     */
    public static void fullScreen(Activity activity) {
        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
