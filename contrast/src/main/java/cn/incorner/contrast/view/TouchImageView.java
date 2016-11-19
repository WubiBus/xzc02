package cn.incorner.contrast.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import cn.incorner.contrast.util.DensityUtil;

public class TouchImageView extends ImageView implements OnTouchListener {

	private static final int POINTER_NONE = 0;
	private static final int POINTER_SINGLE = 1;
	private static final int POINTER_MULTIPLE = 2;

	private int pointerState = POINTER_NONE;
	private Context context;

	public TouchImageView(Context context) {
		this(context, null);
	}

	public TouchImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TouchImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		// init();
	}

	private Bitmap bitmap;

	public void setBitmap(Bitmap bitmap) {
		setImageBitmap(bitmap);
		this.bitmap = bitmap;
		doInit(false);
	}

	public void setFilter(Bitmap filter) {
		if (filter == null) {
			return;
		}
		setImageBitmap(filter);
		this.bitmap = filter;
		doInit(true);
	}

	public void shangxiajingxiang() {
		currentMatrix.postScale(1f, -1f, middlePointerX, middlePointerY);
		setImageMatrix(currentMatrix);
	}

	public void zuoyoujingxiang() {
		currentMatrix.postScale(-1f, 1f, middlePointerX, middlePointerY);
		setImageMatrix(currentMatrix);
	}

	private GestureDetector gestureDetector = new GestureDetector(context,
			new SimpleOnGestureListener() {
				@Override
				public boolean onDown(MotionEvent e) {
					// 捕获Down事件
					return true;
				}

				@Override
				public boolean onSingleTapConfirmed(MotionEvent e) {
					if (imageViewClickListener != null) {
						imageViewClickListener.onImageViewClick();
					}
					return true;
				}
			});
	private OnImageViewClickListener imageViewClickListener;

	public void setOnImageViewClickListener(OnImageViewClickListener listener) {
		this.imageViewClickListener = listener;
	}

	private void doInit(final boolean flag) {
		if (viewWidth > 0 && viewHeight > 0) {
			init(flag);
		} else {
			getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					if (viewWidth == 0 && viewHeight == 0) {
						init(flag);
					}
				}
			});
		}
	}

	private void init(boolean flag) {
		// Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.welcome);
		// TT.show(context, "bitmap.w and h: " + bitmap.getWidth() + "," + bitmap.getHeight());
		setScaleType(ScaleType.MATRIX);
		setBackgroundColor(Color.WHITE);
		// setImageResource(R.drawable.ic_launcher);
		setImageBitmap(bitmap);
		setOnTouchListener(this);

		srcWidth = bitmap.getWidth();
		srcHeight = bitmap.getHeight();
		viewWidth = getWidth();
		viewHeight = getHeight();
		currentMatrix = new Matrix(getImageMatrix());

		if (!flag) {
			// 为了缩放后的图片，长度或宽度不要紧贴着view，要留一些边距
			int paddingValue = DensityUtil.dip2px(context, 20);

			float scale;
			float scale1 = 1.0f * srcWidth / (viewWidth + paddingValue);
			int scaledHeight = (int) (1.0f * srcHeight / scale1);
			int scaledWidth;
			if (scaledHeight >= (viewHeight + paddingValue)) {
				scale = 1 / scale1;
				scaledWidth = (viewWidth + paddingValue);
			} else {
				float scale2 = 1.0f * srcHeight / (viewHeight + paddingValue);
				scaledWidth = (int) (1.0f * srcWidth / scale2);
				scaledHeight = (viewHeight + paddingValue);
				scale = 1 / scale2;
			}
			// TT.show(context, "scale: " + scale + ", scaledWidth: " + scaledWidth
			// + ", scaledHeight: " + scaledHeight);

			currentMatrix.setScale(scale, scale);
			// currentMatrix.postRotate(90f);
			// 设置居中
			float offX = (scaledWidth - viewWidth) / -2.0f;
			float offY = (scaledHeight - viewHeight) / -2.0f;
			currentMatrix.postTranslate(offX, offY);
		}

		// // 镜像 TODO
		// currentMatrix.postScale(1f, -1f, 300f, 600f);

		setImageMatrix(currentMatrix);
		float[] values = new float[9];
		currentMatrix.getValues(values);
		// TT.show(context, values[0] + "," + values[1] + "," + values[2] + "\n" +
		// values[3] + "," + values[4] + "," + values[5] + "\n" +
		// values[6] + "," + values[7] + "," + values[8]);
	}

	// 初始化大小
	int srcWidth;
	int srcHeight;
	int viewWidth;
	int viewHeight;
	// TT.show(context, "1234: " + srcWidth + "," + srcHeight + "," + viewWidth + "," + viewHeight);

	// @Override
	// public void setImageBitmap(Bitmap bitmap) {
	// super.setImageBitmap(bitmap);
	// if (getWidth() == 0) {
	// ViewTreeObserver vto = getViewTreeObserver();
	// vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
	// {
	// public boolean onPreDraw()
	// {
	// // initData();
	// //赋值结束后，移除该监听函数
	// TouchImageView.this.getViewTreeObserver().removeOnPreDrawListener(this);
	// return true;
	// }
	// });
	// }
	// }

	private float primaryPointerX;
	private float primaryPointerY;
	private float slaveryPointerX;
	private float slaveryPointerY;
	private float middlePointerX;
	private float middlePointerY;
	private float oldDistance;
	private float oldDegree;
	private Matrix oldMatrix = new Matrix();
	private Matrix currentMatrix = new Matrix();
	private boolean temp;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			pointerState = POINTER_SINGLE;
			primaryPointerX = event.getX();
			primaryPointerY = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			if (pointerState == POINTER_SINGLE) {
				Log.d("move", "moving");
				if (temp == true) {
					// 判断up的是primary还是slavery
					float newPrimaryPointerX = event.getX();
					float newPrimaryPointerY = event.getY();
					float dXX = newPrimaryPointerX - primaryPointerX;
					float dYY = newPrimaryPointerY - primaryPointerY;
					float newDistance1 = (float) Math.sqrt(dXX * dXX + dYY * dYY);
					float dXXX = newPrimaryPointerX - slaveryPointerX;
					float dYYY = newPrimaryPointerY - slaveryPointerY;
					float newDistance2 = (float) Math.sqrt(dXXX * dXXX + dYYY * dYYY);
					if (newDistance1 > newDistance2) {
						primaryPointerX = slaveryPointerX;
						primaryPointerY = slaveryPointerY;
					}
					temp = false;
				}
				float currentX = event.getX();
				float currentY = event.getY();
				float dX = currentX - primaryPointerX;
				float dY = currentY - primaryPointerY;
				primaryPointerX = currentX;
				primaryPointerY = currentY;
				// TT.show(context, "drag: dX: " + dX + ",dY: " + dY);

				currentMatrix.set(getImageMatrix());
				currentMatrix.postTranslate(dX, dY);
				setImageMatrix(currentMatrix);
			} else if (pointerState == POINTER_MULTIPLE) {
				// 计算平移量
				float lastPrimaryPointerX = primaryPointerX;
				float lastPrimaryPointerY = primaryPointerY;
				float lastSlaveryPointerX = slaveryPointerX;
				float lastSlaveryPointerY = slaveryPointerY;
				primaryPointerX = event.getX(0);
				primaryPointerY = event.getY(0);
				slaveryPointerX = event.getX(1);
				slaveryPointerY = event.getY(1);
				float dXPrimary = primaryPointerX - lastPrimaryPointerX;
				float dYPrimary = primaryPointerY - lastPrimaryPointerY;
				float dXSlavery = slaveryPointerX - lastSlaveryPointerX;
				float dYSlavery = slaveryPointerY - lastSlaveryPointerY;
				float dXShorter = dXPrimary <= dXSlavery ? dXPrimary : dXSlavery;
				float dYShorter = dYPrimary <= dYSlavery ? dYPrimary : dYSlavery;
				Log.d("short", "shortx: " + dXShorter + ", shorty: " + dYShorter);
				float dXOffset = 0f;
				float dYOffset = 0f;
				if (dXPrimary > 0 && dXSlavery > 0 || dXPrimary < 0 && dXSlavery < 0) {
					dXOffset = dXShorter;
				}
				if (dYPrimary > 0 && dYSlavery > 0 || dYPrimary < 0 && dYSlavery < 0) {
					dYOffset = dYShorter;
				}
				oldMatrix.postTranslate(dXOffset, dYOffset);
				currentMatrix.set(oldMatrix);
				// （多指）平移
				currentMatrix.postTranslate(dXOffset, dYOffset);
				// 缩放
				float dX = primaryPointerX - slaveryPointerX;
				float dY = primaryPointerY - slaveryPointerY;
				float newDistance = (float) Math.sqrt(dX * dX + dY * dY);
				float zoomScale = newDistance / oldDistance;
				Log.d("TouchImageView", "oldDistance: " + oldDistance + "newDistance: "
						+ newDistance + ", zoomScale: " + zoomScale);
				currentMatrix.postScale(zoomScale, zoomScale, middlePointerX, middlePointerY);
				// 旋转
				float newDegree = (float) Math.toDegrees(Math.atan2(dY, dX));
				float degree = newDegree - oldDegree;
				currentMatrix.postRotate(degree, middlePointerX, middlePointerY);

				setImageMatrix(currentMatrix);
			}
			break;
		case MotionEvent.ACTION_UP:
			pointerState = POINTER_NONE;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			pointerState = POINTER_MULTIPLE;
			slaveryPointerX = event.getX(1);
			slaveryPointerY = event.getY(1);
			middlePointerX = (primaryPointerX + slaveryPointerX) / 2;
			middlePointerY = (primaryPointerY + slaveryPointerY) / 2;

			double dX = primaryPointerX - slaveryPointerX;
			double dY = primaryPointerY - slaveryPointerY;
			if (dX != 0 && dY != 0) {
				oldDistance = (float) Math.sqrt(dX * dX + dY * dY);
				Log.d("pointer_down", "oldDistance: " + oldDistance);
				oldDegree = (float) Math.toDegrees(Math.atan2(dY, dX));
				oldMatrix.set(getImageMatrix());
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			pointerState = POINTER_SINGLE;
			temp = true;
			break;
		}
		// return true;
		return gestureDetector.onTouchEvent(event);
	}

	public interface OnImageViewClickListener {
		public void onImageViewClick();
	}

}
