package cn.incorner.contrast.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import cn.incorner.contrast.R;

/**
 * 自定义刷新框架
 * 
 * @author yeshimin
 */
public class CustomRefreshFramework extends LinearLayout {

	private static final String TAG = "CustomRefreshFramework";

	// 刷新框架状态
	private static final int STATE_NORMAL = 1;
	private static final int STATE_PULL_DOWN = 2;
	private static final int STATE_PULL_UP = 3;
	// 头部和尾部视图状态
	private static final int STATE_REFRESHING = 0; // 代码触发的刷新状态
	private static final int STATE_PULL_REFRESHING = 1;
	private static final int STATE_PULL_CAN_REFRESH = 2;
	private static final int STATE_PULL_CANT_REFRESH = 3;
	// 刷新内容位置
	private static final int POS_NONE = 0;
	private static final int POS_TOP = 1;
	private static final int POS_BOTTOM = 2;
	private static final int POS_MIDDLE = 3;

	private RelativeLayout rlHeaderContainer;
	private RelativeLayout rlHeaderRefreshContainer;
	private ImageView ivHeaderRefreshView;
	private RelativeLayout rlFooterRefreshContainer;
	private ImageView ivFooterRefreshView;
	private View vContent;
	private AnimationDrawable animDrawableHeader;
	private AnimationDrawable animDrawableFooter;

	private int headerContainerHeight;
	private int headerRefreshContainerHeight;
	private int footerContainerHeight;
	private int footerRefreshContainerHeight;

	private int state = STATE_NORMAL;
	private int lastState = state;
	private int viewState = STATE_PULL_CANT_REFRESH;
	private int pos = POS_NONE;

	private OnRefreshingListener listener;
	private OnTouchMoveListener onTouchMoveListener;

	private float downY;
	private float currentY;
	private float upY;

	private int startX;


	// 用户刷新动画的持续时间，最少1000ms
	private long lastRefreshStartTime = 0;
	private long lastLoadMoreStartTime = 0;

	private boolean shouldIntercept;

	public CustomRefreshFramework(Context context) {
		this(context, null);
	}

	public CustomRefreshFramework(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CustomRefreshFramework(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		if (shouldIntercept()) {

			return super.dispatchTouchEvent(ev);
		}

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = ev.getY();
			currentY = downY;

			break;
		case MotionEvent.ACTION_MOVE:
			float lastY = currentY;
			currentY = ev.getY();
			float dY = currentY - lastY;
			pos = getContentPosition();
			// Log.d(TAG, "dY: " + dY + ", scrollY: " + getScrollY() + ", pos: " + pos + ", state: "
			// + state);


			calcVerticalMoving(lastY, currentY);

			// 根据上拉下拉位置改变刷新状态
			if (viewState == STATE_PULL_REFRESHING) {
				return true;
			} else {
				float sy = getScrollY();
				if (state == STATE_PULL_DOWN) {
					if (sy > -headerRefreshContainerHeight) {
						// cant refresh
						if (viewState != STATE_PULL_CANT_REFRESH) {
							viewState = STATE_PULL_CANT_REFRESH;
							// vHeader.setText("pull to refresh");
						}
					} else if (sy < -headerRefreshContainerHeight) {
						// can refresh
						if (viewState != STATE_PULL_CAN_REFRESH) {
							viewState = STATE_PULL_CAN_REFRESH;
							// vHeader.setText("release to refresh");
						}
					}
				} else if (state == STATE_PULL_UP) {
					if (sy < footerRefreshContainerHeight) {
						// cant refresh
						if (viewState != STATE_PULL_CANT_REFRESH) {
							viewState = STATE_PULL_CANT_REFRESH;
							// vFooter.setText("pull to refresh");
						}
					} else if (sy > footerRefreshContainerHeight) {
						// can
						if (viewState != STATE_PULL_CAN_REFRESH) {
							viewState = STATE_PULL_CAN_REFRESH;
							// vFooter.setText("release to refresh");
						}
					}
				}
			}

			if (dY > 0) { // pull down
				switch (state) {
				case STATE_NORMAL:
					switch (pos) {
					case POS_NONE:
					case POS_TOP:
						state = STATE_PULL_DOWN;
						scrollBy(0, (int) -dY / 2);
						return true;
					case POS_BOTTOM:
					case POS_MIDDLE:
						return super.dispatchTouchEvent(ev);
					}
					break;
				case STATE_PULL_DOWN:
					switch (pos) {
					case POS_NONE:
					case POS_TOP:
						scrollBy(0, (int) -dY / 2);
						return true;
					case POS_BOTTOM:
					case POS_MIDDLE:
						return super.dispatchTouchEvent(ev);
					}
					break;
				case STATE_PULL_UP:
					if (getScrollY() <= 0) {
						setScrollY(0);
						return super.dispatchTouchEvent(ev);
					} else {
						dY = dY - getScrollY() > 0 ? getScrollY() : dY;
						scrollBy(0, (int) -dY / 2);
						return true;
					}
				}
			} else if (dY < 0) { // pull up
				switch (state) {
				case STATE_NORMAL:
					switch (pos) {
					case POS_NONE:
					case POS_BOTTOM:
						state = STATE_PULL_UP;
						scrollBy(0, (int) -dY / 2);
						return true;
					case POS_TOP:
						return super.dispatchTouchEvent(ev);
					case POS_MIDDLE:
						return super.dispatchTouchEvent(ev);
					}
					break;
				case STATE_PULL_DOWN:
					if (getScrollY() >= 0) {
						setScrollY(0);
						return super.dispatchTouchEvent(ev);
					} else {
						// Log.d(TAG, "ffff.dY: " + dY);
						dY = dY - getScrollY() < 0 ? getScrollY() : dY;
						// Log.d(TAG, "ffff.dY: " + dY);
						scrollBy(0, (int) -dY / 2);
						return true;
					}
				case STATE_PULL_UP:
					switch (pos) {
					case POS_NONE:
					case POS_BOTTOM:
						scrollBy(0, (int) -dY / 2);
						return true;
					case POS_TOP:
						return super.dispatchTouchEvent(ev);
					case POS_MIDDLE:
						return super.dispatchTouchEvent(ev);
					}
					break;
				}
			} else if (state != STATE_NORMAL) {
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (viewState != STATE_PULL_REFRESHING && viewState != STATE_REFRESHING) {
				lastState = state;
				state = STATE_NORMAL;
			}

			calcTouchMove(ev);

			if (viewState == STATE_PULL_REFRESHING) {
				return true;
			} else if (viewState == STATE_PULL_CAN_REFRESH) {
				viewState = STATE_PULL_REFRESHING;

				if (lastState == STATE_PULL_DOWN) {
					lastRefreshStartTime = System.currentTimeMillis();
					scrollTo(0, -headerRefreshContainerHeight);
					if (animDrawableHeader != null) {
						animDrawableHeader.start();
					}
					if (listener != null) {
						listener.onRefreshing();
					}
				} else {
					lastLoadMoreStartTime = System.currentTimeMillis();
					scrollTo(0, footerRefreshContainerHeight);
					if (animDrawableFooter != null) {
						animDrawableFooter.start();
					}
					if (listener != null) {
						listener.onLoadMore();
					}
				}
				return true;
			}

			if (getScrollY() != 0) {
				scrollTo(0, 0);
				return true;
			}
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	private void calcVerticalMoving(float lastY, float currentY) {
		if (lastY != currentY && onTouchMoveListener != null) {
			onTouchMoveListener.onVerticalMoving();
		}
	}

	private void calcTouchMove(MotionEvent ev) {
		upY = ev.getY();
		float dY = upY - downY;
		if (dY > 10) {
			// 下滑
			if (onTouchMoveListener != null) {
				onTouchMoveListener.onMoveDown();
			}
		} else if (dY < -10) {
			// 上滑
			if (onTouchMoveListener != null) {
				onTouchMoveListener.onMoveUp();
			}
		}
	}

	public void setOnRefreshingListener(OnRefreshingListener listener) {
		this.listener = listener;
	}

	public void setOnTouchMoveListener(OnTouchMoveListener listener) {
		this.onTouchMoveListener = listener;
	}

	/**
	 * 代码设置刷新（非手动触发），不执行动画效果
	 */
	public void refreshWithoutAnim() {
		state = STATE_NORMAL;
		lastState = state;
		viewState = STATE_REFRESHING;
		if (listener != null) {
			listener.onRefreshing();
		}
	}

	public void setRefreshing(boolean refreshing) {
		if (refreshing) {
			// TODO
		} else {
			if (viewState == STATE_PULL_REFRESHING) {
				if (lastState == STATE_PULL_DOWN) {
					if (canFinishRefresh()) {
						scrollTo(0, 0);
						if (animDrawableHeader != null) {
							animDrawableHeader.stop();
						}
						if (listener != null) {
							listener.onRefreshFinish();
						}
						viewState = STATE_PULL_CANT_REFRESH;
					}
				} else if (lastState == STATE_PULL_UP) {
					if (canFinishLoadMore()) {
						scrollTo(0, 0);
						if (animDrawableFooter != null) {
							animDrawableFooter.stop();
						}
						if (listener != null) {
							listener.onRefreshFinish();
						}
						viewState = STATE_PULL_CANT_REFRESH;
					}
				}
			} else if (viewState == STATE_REFRESHING) {
				scrollTo(0, 0);
				if (animDrawableHeader != null) {
					animDrawableHeader.stop();
				}
				if (animDrawableFooter != null) {
					animDrawableFooter.stop();
				}
				if (listener != null) {
					listener.onRefreshFinish();
				}
				viewState = STATE_PULL_CANT_REFRESH;
			} else {
				scrollTo(0, 0);
				if (animDrawableHeader != null) {
					animDrawableHeader.stop();
				}
				if (animDrawableFooter != null) {
					animDrawableFooter.stop();
				}
				if (listener != null) {
					listener.onRefreshFinish();
				}
			}
		}
	}

	/**
	 * 判断能否结束刷新
	 */
	private boolean canFinishRefresh() {
		long pastTime = System.currentTimeMillis() - lastRefreshStartTime;
		if (pastTime > 1000) {
			return true;
		}
		this.postDelayed(new Runnable() {
			public void run() {
				setRefreshing(false);
			}
		}, 1001 - pastTime);
		return false;
	}

	/**
	 * 判断能否结束加载更多
	 */
	private boolean canFinishLoadMore() {
		long pastTime = System.currentTimeMillis() - lastLoadMoreStartTime;
		if (pastTime > 1000) {
			return true;
		}
		this.postDelayed(new Runnable() {
			public void run() {
				setRefreshing(false);
			}
		}, 1001 - pastTime);
		return false;
	}

	/**
	 * 获取刷新内容的滚动位置
	 */
	private int getContentPosition() {
		if (vContent == null) {
			return POS_NONE;
		}

		if (vContent instanceof AbsListView) {
			final AbsListView absListView = (AbsListView) vContent;
			if (absListView.canScrollVertically(1) || absListView.canScrollVertically(-1)) {
				// Log.d(TAG, "aaaa");
				if (isContentTop(vContent)) {
					// Log.d(TAG, "bbbb");
					return POS_TOP;
				} else if (isContentBottom(vContent)) {
					// Log.d(TAG, "cccc");
					return POS_BOTTOM;
				} else {
					// Log.d(TAG, "dddd");
					return POS_MIDDLE;
				}
			} else {
				// Log.d(TAG, "eeee");
				return POS_NONE;
			}
		}
		// Log.d(TAG, "ffff");

		return POS_NONE;
	}

	private boolean isContentTop(View view) {
		if (view instanceof AbsListView) {
			final AbsListView absListView = (AbsListView) view;
			int childCount = absListView.getChildCount();
			if (childCount > 0 && (absListView.getChildAt(0).getTop() == 0)) {
				return true;
			}
			return false;
		}
		return false;
	}

	private boolean isContentBottom(View view) {
		if (view instanceof AbsListView) {
			final AbsListView absListView = (AbsListView) view;

			// int sy = absListView.getScrollY();
			// int top = absListView.getChildAt(0).getTop();
			// int fpos = absListView.getFirstVisiblePosition();
			// // Log.d(TAG, "sy: " + sy + ", top: " + top + ", fpos: " + fpos);

			int childCount = absListView.getChildCount();
			if (childCount > 0) {
				if (absListView.getLastVisiblePosition() == (absListView.getAdapter().getCount() - 1)
						&& absListView.getChildAt(childCount - 1).getBottom() == getHeight()) {
					return true;
				} else {
					return false;
				}
			}
			return false;
		}
		return false;
	}

	public void setHeaderContainerHeight(int height) {
		headerContainerHeight = height;
		if (headerContainerHeight < headerRefreshContainerHeight) {
			headerRefreshContainerHeight = headerContainerHeight;
		}
		if (rlHeaderContainer != null) {
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rlHeaderContainer
					.getLayoutParams();
			params.height = headerContainerHeight;
			rlHeaderContainer.setLayoutParams(params);
		}
	}

	@Override
	protected void onFinishInflate() {
		Log.d(TAG, "onFinishInflate()");

		vContent = getChildAt(0);
		final View vFooter = getChildAt(1);
		addOnLayoutChangeListener(new OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom,
					int oldLeft, int oldTop, int oldRight, int oldBottom) {

				if (rlHeaderContainer == null) {
					ViewGroup vgHeader = (ViewGroup) ((AbsListView) vContent).getAdapter().getView(
							0, null, null);

					if (vgHeader != null) {
						rlHeaderContainer = (RelativeLayout) vgHeader
								.findViewById(R.id.rl_header_container);
						rlHeaderRefreshContainer = (RelativeLayout) rlHeaderContainer
								.findViewById(R.id.rl_header_refresh_container);
						ivHeaderRefreshView = (ImageView) rlHeaderRefreshContainer
								.findViewById(R.id.iv_header_refresh_view);
						headerRefreshContainerHeight = rlHeaderRefreshContainer.getHeight();
						if (headerContainerHeight > 0) {
							if (headerContainerHeight < headerRefreshContainerHeight) {
								headerRefreshContainerHeight = headerContainerHeight;
							}
						} else {
							// 默认两者等高
							headerContainerHeight = headerRefreshContainerHeight;
						}
						RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rlHeaderContainer
								.getLayoutParams();
						params.height = headerContainerHeight;
						rlHeaderContainer.setLayoutParams(params);

						// 初始化header刷新动画
						initHeaderRefreshAnimation();
					}
				}

				if (vFooter != null) {
					ViewGroup vgFooter = (ViewGroup) vFooter;

					rlFooterRefreshContainer = (RelativeLayout) vgFooter
							.findViewById(R.id.rl_footer_refresh_container);
					ivFooterRefreshView = (ImageView) vgFooter
							.findViewById(R.id.iv_footer_refresh_view);
					footerRefreshContainerHeight = rlFooterRefreshContainer.getHeight();
					// 默认两个等高
					footerContainerHeight = footerRefreshContainerHeight;

					// 初始化footer刷新动画
					initFooterRefreshAnimation();
				}
			}
		});

		super.onFinishInflate();
	}

	private void initHeaderRefreshAnimation() {
		animDrawableHeader = (AnimationDrawable) getResources().getDrawable(
				R.drawable.anim_loading_header);
		animDrawableHeader.setOneShot(false);
		if (ivHeaderRefreshView != null) {
			ivHeaderRefreshView.setImageDrawable(animDrawableHeader);
		}
	}

	private void initFooterRefreshAnimation() {
		animDrawableFooter = (AnimationDrawable) getResources().getDrawable(
				R.drawable.anim_loading_footer);
		animDrawableFooter.setOneShot(false);
		if (ivFooterRefreshView != null) {
			ivFooterRefreshView.setImageDrawable(animDrawableFooter);
		}
	}

	public boolean shouldIntercept(){
		return shouldIntercept;
	}

	public void setShouldIntercept(boolean shouldIntercept){
		this.shouldIntercept = shouldIntercept;
	}


	/**
	 * 刷新回调接口
	 */
	public interface OnRefreshingListener {
		public void onRefreshing();

		public void onLoadMore();

		// 在这个方法中执行刷新UI，比如adapter.notifyDataSetChanged()
		public void onRefreshFinish();

		//public void onRefreshFinishComment();
	}

	public interface OnTouchMoveListener {
		public void onVerticalMoving();

		public void onMoveUp();

		public void onMoveDown();
	}

}
