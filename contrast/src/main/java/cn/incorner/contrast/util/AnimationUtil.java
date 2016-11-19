package cn.incorner.contrast.util;

import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class AnimationUtil {
	/** 
     * 缩放动画 (有大变小)
     * @return 
     */  
    public static Animation getScaleInAnimation(long t) {  
        //实例化 ScaleAnimation 主要是缩放效果  
        //参数：fromX-动画开始前，x坐标   toX-动画结束后x坐标  
        //fromY-动画开始前，Y坐标  toY-动画结束后Y坐标  
        //pivotXType - 为动画相对于物件的X坐标的参照物   pivotXValue - 值  
        //pivotYType - 为动画相对于物件的Y坐标的参照物   pivotYValue - 值  
        Animation animation = new ScaleAnimation(2f, 1.1f, 2f, 1.1f,   
                Animation.RELATIVE_TO_SELF, 0.5f,  
                Animation.RELATIVE_TO_SELF, 0.5f);  
        //设置动画插值器 被用来修饰动画效果,定义动画的变化率 
        animation.setInterpolator(new DecelerateInterpolator(1.5f));  
        //设置动画执行时间  
        animation.setDuration(t);  
        return animation;  
    } 
    
    public static Animation getScaleInAnimation2(long t) {  
        //实例化 ScaleAnimation 主要是缩放效果  
        //参数：fromX-动画开始前，x坐标   toX-动画结束后x坐标  
        //fromY-动画开始前，Y坐标  toY-动画结束后Y坐标  
        //pivotXType - 为动画相对于物件的X坐标的参照物   pivotXValue - 值  
        //pivotYType - 为动画相对于物件的Y坐标的参照物   pivotYValue - 值  
        Animation animation = new ScaleAnimation(1.1f, 1.0f, 1.1f, 1.0f,   
                Animation.RELATIVE_TO_SELF, 0.5f,  
                Animation.RELATIVE_TO_SELF, 0.5f);  
        //设置动画插值器 被用来修饰动画效果,定义动画的变化率 
        animation.setInterpolator(new LinearInterpolator());  
        //设置动画执行时间  
        animation.setDuration(t);  
        return animation;  
    } 
    
    /** 
     * 缩放动画 (有小变大)
     * @return 
     */  
    public static Animation getScaleOutAnimation() {  
        //实例化 ScaleAnimation 主要是缩放效果  
        //参数：fromX-动画开始前，x坐标   toX-动画结束后x坐标  
        //fromY-动画开始前，Y坐标  toY-动画结束后Y坐标  
        //pivotXType - 为动画相对于物件的X坐标的参照物   pivotXValue - 值  
        //pivotYType - 为动画相对于物件的Y坐标的参照物   pivotYValue - 值  
        Animation animation = new ScaleAnimation(1.0f, 4f, 1.0f, 4f,   
                Animation.RELATIVE_TO_SELF, 0.5f,  
                Animation.RELATIVE_TO_SELF, 0.5f);  
        //设置动画插值器 被用来修饰动画效果,定义动画的变化率   
        animation.setInterpolator(new DecelerateInterpolator());  
        //设置动画执行时间  
        animation.setDuration(100);  
        return animation;  
    } 
    
    /** 
     * 平移动画
     * @return 
     */  
    public static Animation getTranslateLoginInAnimation(long t) {  
       
    	
		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.01f, Animation.RELATIVE_TO_PARENT,
				0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		animation.setDuration(t);
		return animation;
    } 
    
    /**
     * 缩放动画 (有大变小)
     * @param t
     * @return
     */
    
    public static Animation getScaleLoginInAnimation(long t) { 
    	 //实例化 ScaleAnimation 主要是缩放效果  
        //参数：fromX-动画开始前，x坐标   toX-动画结束后x坐标  
        //fromY-动画开始前，Y坐标  toY-动画结束后Y坐标  
        //pivotXType - 为动画相对于物件的X坐标的参照物   pivotXValue - 值  
        //pivotYType - 为动画相对于物件的Y坐标的参照物   pivotYValue - 值  
        Animation animation = new ScaleAnimation(1.15f, 1.0f, 1.15f, 1.0f,   
                Animation.RELATIVE_TO_SELF, 0.5f,  
                Animation.RELATIVE_TO_SELF, 0.5f);  
        //设置动画插值器 被用来修饰动画效果,定义动画的变化率   
        animation.setInterpolator(new LinearInterpolator());  
        //设置动画执行时间  
        animation.setDuration(t);  
        return animation;  
    }
    
    public static Animation getScaleLoginInAnimation2(long t) { 
   	 //实例化 ScaleAnimation 主要是缩放效果  
       //参数：fromX-动画开始前，x坐标   toX-动画结束后x坐标  
       //fromY-动画开始前，Y坐标  toY-动画结束后Y坐标  
       //pivotXType - 为动画相对于物件的X坐标的参照物   pivotXValue - 值  
       //pivotYType - 为动画相对于物件的Y坐标的参照物   pivotYValue - 值  
       Animation animation = new ScaleAnimation(1.02f, 1.0f, 1.02f, 1.0f,   
               Animation.RELATIVE_TO_PARENT, 0.5f,  
               Animation.RELATIVE_TO_PARENT, 0.5f);  
       //设置动画插值器 被用来修饰动画效果,定义动画的变化率   
       animation.setInterpolator(new LinearInterpolator());  
       //设置动画执行时间  
       animation.setDuration(t);  
       return animation;  
   }
    
}
