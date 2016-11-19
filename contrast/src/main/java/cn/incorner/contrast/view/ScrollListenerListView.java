package cn.incorner.contrast.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * 滚动监听ListView
 * 
 * @author yeshimin
 */
public class ScrollListenerListView extends ListView {

	private static final String TAG = "ScrollListenerListView";



	public ScrollListenerListView(Context context) {
		this(context, null);
		init();
	}

	public ScrollListenerListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		init();
	}

	public ScrollListenerListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		this.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
					int totalItemCount) {
				if (listener != null) {
					listener.onScroll();
				}
			}
		});

		this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					downY = event.getY();
					break;
				case MotionEvent.ACTION_UP:
					upY = event.getY();
					float dY = upY - downY;
					if (dY > 0) {
						// 下滑
						if (listener != null) {
							listener.onScrollDown();
						}
					} else if (dY < 0) {
						// 上滑
						if (listener != null) {
							listener.onScrollUp();
						}
					}
					break;
				}

				return false;
			}
		});
	}

	private OnListViewScrollListener listener;

	public void setOnListViewScrollListener(OnListViewScrollListener listener) {
		this.listener = listener;
	}

	private float downY;
	private float upY;

	public interface OnListViewScrollListener extends CustomScrollListener {
	}



}
