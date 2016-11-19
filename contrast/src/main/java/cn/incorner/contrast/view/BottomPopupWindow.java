package cn.incorner.contrast.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.PopupWindow;

/**
 * 底部弹出窗口
 * 
 * @since 2015-12-30 10:57
 * @version 1
 * @author yeshimin | ysm@yeshimin.com
 */
public class BottomPopupWindow extends PopupWindow {
	
	private static final String TAG = "BottomPopupWindow";
	
	private Context context;
	private View vParent;
	private View vContent;
	
    public BottomPopupWindow(Context context) {
    	this(context, null, null);
    }
    
    public BottomPopupWindow(Context context, View vParent, View vContent) {
    	this(context, vParent, vContent, -1);
    }
    
    public BottomPopupWindow(Context context, View vParent, View vContent, int animRes) {
    	super(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    	this.context = context;
    	
    	setParentView(vParent);
    	setContentView(vContent);
    	setAnimationStyle(animRes);
    	
    	init();
    }
    
    /**
     * 初始化PopupWindow
     */
    private void init() {
		setBackgroundDrawable(new ColorDrawable());
		setOutsideTouchable(true);
		setTouchable(true);
		setFocusable(true);
		
    	setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				dismissMaskView();
			}
    	});
    }
    
    /**
     * 设置父视图
     */
    public void setParentView(View view) {
    	this.vParent = view;
    }
	
    /**
     * 设置内容视图
     */
	@Override
	public void setContentView(View contentView) {
		super.setContentView(contentView);
    	vContent = contentView;
	}
    
	/**
	 * 设置动画资源
	 */
    @Override
	public void setAnimationStyle(int animationStyle) {
		super.setAnimationStyle(animationStyle);
	}

    /**
     * 显示弹出窗口
     */
    public void show() {
    	if (vParent == null || vContent == null) {
    		Log.w(TAG, "Please set parent view and content view first");
    		return;
    	}
    	
    	// 背景变暗
    	showMaskView();
    	// 显示弹出窗
    	showAtLocation(vParent, Gravity.BOTTOM, 0, 0);
    }
    
    /**
     * 显示暗淡的背景视图
     */
    private void showMaskView() {
    	WindowManager.LayoutParams params = ((Activity)context).getWindow().getAttributes();
    	params.alpha = 0.4f;
    	((Activity)context).getWindow().setAttributes(params);
    }
    
    /**
     * 隐藏暗淡的背景视图
     */
    private void dismissMaskView() {
    	WindowManager.LayoutParams params = ((Activity)context).getWindow().getAttributes();
    	params.alpha = 1f;
    	((Activity)context).getWindow().setAttributes(params);
    }

}
