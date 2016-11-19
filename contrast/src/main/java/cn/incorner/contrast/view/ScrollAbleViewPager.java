package cn.incorner.contrast.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author:dongpo 创建时间: 2016/11/6
 * 描述:
 * 修改:
 */
public class ScrollAbleViewPager extends ViewPager {
    private boolean shouldInterpt = true;

    public ScrollAbleViewPager(Context context) {
        super(context);
    }

    public ScrollAbleViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (shouldInterpt) {
            return super.onTouchEvent(ev);
        } else {
            return false;
        }
    }

    public void setShouldInterpt(boolean shouldInterpt) {
        this.shouldInterpt = shouldInterpt;
    }
}
