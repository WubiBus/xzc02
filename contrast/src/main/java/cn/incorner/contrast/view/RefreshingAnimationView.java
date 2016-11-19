package cn.incorner.contrast.view;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import cn.incorner.contrast.R;

/**
 * 刷新动画视图
 * 
 * @author yeshimin
 */
public class RefreshingAnimationView extends ImageView {

	private static final BitmapFactory.Options options = new BitmapFactory.Options();

	private int[] arrResId;
	private int duration = 50;
	private int currentFrameIndex = 0;
	private Bitmap bitmap;
	private Bitmap lastBitmap;
	private Bitmap nextBitmap;
	private long decodeStartTime = 0;
	private long decodeUsedTime = 0;
	// 用于刷新动画最少执行一个周期
	private boolean oneShot = false;
	private boolean oneShotCompleted = false;

	public RefreshingAnimationView(Context context) {
		super(context);
	}

	public RefreshingAnimationView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RefreshingAnimationView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		initView();
		initAnimation();
		super.onFinishInflate();
	}

	private void initView() {
		setBackgroundColor(0x88888888);
		setVisibility(View.GONE);
		setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
	}

	private void initAnimation() {
		arrResId = new int[] { R.drawable.loading0001, R.drawable.loading0002,
				R.drawable.loading0003, R.drawable.loading0004, R.drawable.loading0005,
				R.drawable.loading0006, R.drawable.loading0007, R.drawable.loading0008,
				R.drawable.loading0009, R.drawable.loading0010, R.drawable.loading0011,
				R.drawable.loading0012, R.drawable.loading0013, R.drawable.loading0014,
				R.drawable.loading0015, R.drawable.loading0016, R.drawable.loading0017,
				R.drawable.loading0018, R.drawable.loading0019, R.drawable.loading0020,
				R.drawable.loading0021, R.drawable.loading0022, R.drawable.loading0023,
				R.drawable.loading0024, R.drawable.loading0025, R.drawable.loading0026,
				R.drawable.loading0027, R.drawable.loading0028, R.drawable.loading0029,
				R.drawable.loading0030 };
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inPurgeable = true;
		options.inInputShareable = true;
		// 初始化
		try {
			reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * （初始化）还原参数
	 */
	private void reset() throws Exception {
		currentFrameIndex = 0;
		decodeStartTime = 0;
		decodeUsedTime = 0;
		oneShot = false;
		oneShotCompleted = false;
		// 释放图片
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (lastBitmap != null && !lastBitmap.isRecycled()) {
					lastBitmap.recycle();
					lastBitmap = null;
				}
				if (bitmap != null && !bitmap.isRecycled()) {
					bitmap.recycle();
					bitmap = null;
				}
				if (nextBitmap != null && !nextBitmap.isRecycled()) {
					nextBitmap.recycle();
					nextBitmap = null;
				}
			}
		}).start();
	}

	/**
	 * 开始/停止 刷新
	 */
	public void setRefreshing(boolean needRefresh) {
		if (needRefresh) {
			showAnimation();
		} else {
			oneShot = true;
			stopRefreshingAtLeastOneShot();
		}
	}

	/**
	 * 刷新动画最少执行一个周期
	 */
	private void stopRefreshingAtLeastOneShot() {
		if (oneShot && oneShotCompleted) {
			hideAnimation();
		}
	}

	/**
	 * 开始动画
	 */
	private void startAnimation() {
		try {
			InputStream is = getResources().openRawResource(arrResId[currentFrameIndex]);
			nextBitmap = BitmapFactory.decodeStream(is, null, options);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		nextFrame();
	}

	/**
	 * 下一帧
	 */
	private void nextFrame() {
		// 设置图片
		lastBitmap = bitmap;
		bitmap = nextBitmap;
		nextBitmap = null;
		if (bitmap != null && !bitmap.isRecycled()) {
			setImageBitmap(bitmap);
		}

		// oneshot相关
		if (currentFrameIndex == arrResId.length - 1) {
			oneShotCompleted = true;
		}
		if (oneShot && oneShotCompleted) {
			stopRefreshingAtLeastOneShot();
			return;
		}

		// 解析下一张图片
		decodeStartTime = System.currentTimeMillis();
		currentFrameIndex = ++currentFrameIndex % arrResId.length;
		try {
			InputStream is = getResources().openRawResource(arrResId[currentFrameIndex]);
			nextBitmap = BitmapFactory.decodeStream(is, null, options);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		// 释放上一张图片
		if (lastBitmap != null && !lastBitmap.isRecycled()) {
			try {
				lastBitmap.recycle();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				lastBitmap = null;
			}
		}
		decodeUsedTime = System.currentTimeMillis() - decodeStartTime;
		postDelayed(runnable, (decodeUsedTime >= duration) ? 0 : (duration - decodeUsedTime));
	}

	/**
	 * 用户切换的Runnable
	 */
	private Runnable runnable = new Runnable() {
		public void run() {
			nextFrame();
		}
	};

	private void showAnimation() {
		setVisibility(View.VISIBLE);
		startAnimation();
	}

	private void hideAnimation() {
		removeCallbacks(runnable);
		setVisibility(View.GONE);
		// （初始化）还原参数
		try {
			reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public interface IRefreshingAnimationView {
		public void showRefreshingAnimationView();

		public void hideRefreshingAnimationView();
	}

}
