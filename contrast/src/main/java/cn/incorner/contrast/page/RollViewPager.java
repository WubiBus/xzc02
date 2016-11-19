package cn.incorner.contrast.page;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.BannerEntity;
import cn.incorner.contrast.view.CustomRefreshFramework;

public class RollViewPager extends ViewPager {

	private List<String> mImages;// 图片集合
	private List<String> mTitles;// 标题集合

	//private BitmapUtils bitmapUtils;
	private RollAdapter adapter;
	private List<View> mDots;// 点的集合
	private int prevous;// 前一个点的索引位置
	private List<BannerEntity> listFindBanner = new ArrayList<BannerEntity>();
	private Context mContext;

	private OnRollViewClickListener onRolllistener;//点击事件回调接口对象

	//刷新框架实例对象
	private CustomRefreshFramework refreshParent;

	/**
	 * 
	 * @param context
	 * @param dots
	 * @param listener 点击事件的监听
	 */
	public RollViewPager(Context context, List<View> dots,OnRollViewClickListener listener) {
		super(context);
		this.mDots = dots;
		this.onRolllistener = listener;
		//bitmapUtils = new BitmapUtils(getContext());


		this.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				int mDotsPos = position % mDots.size();
				mDots.get(mDotsPos).setBackgroundResource(R.drawable.shape__oval__banner_indicator_selected);
				mDots.get(prevous).setBackgroundResource(R.drawable.shape__oval__banner_indicator_unselected);
				prevous = mDotsPos;
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub

			}
		});
		
		this.setOnTouchListener(new OnTouchListener() {
			
			private long startTime;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					System.out.println("ACTION_DOWN");
					
					//让轮播图暂停
					mHandler.removeCallbacksAndMessages(null);//删除所有任务以及消息
					startTime = System.currentTimeMillis();
					break;
				case MotionEvent.ACTION_UP:
					System.out.println("ACTION_UP");
					//让轮播图继续轮播
					start();
					//记录手指抬起的时间
					long endTime = System.currentTimeMillis();
					if ((endTime - startTime) >80) {//当时间间隔小于500ms则认为是点击事件
						if (onRolllistener != null) {
							//onRolllistener.onRollViewClick();
						}
					}
					else {
						if (onRolllistener != null) {
							onRolllistener.onRollViewClick();
						}
						//onRolllistener.onRollViewClick();
					}
					break;
				case MotionEvent.ACTION_CANCEL://当手指按住控件时，移动到控件外，就会走cancel事件，并且up事件不再调用
					System.out.println("ACTION_CANCEL");
					start();
					break;

				default:
					break;
				}
				return false;
			}
		});
	}
	
	//轮播图点击事件回调接口
	public interface OnRollViewClickListener{
		public void onRollViewClick();
	}

	// 让轮播图轮播
	public void start() {
		if (adapter == null) {
			adapter = new RollAdapter();
			this.setAdapter(adapter);
		}

		// 发送一个空消息
		mHandler.sendEmptyMessageDelayed(0, 2000);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			// 先获得当前页面的位置
			int currentItem = RollViewPager.this.getCurrentItem();
			//int max = Integer.MAX_VALUE;
			int pos = (currentItem + 1);// 取模操作，防止数据越界
			RollViewPager.this.setCurrentItem(pos, false);
			start();
		};
	};
	private int startX;
	private int startY;

	// 传递轮播图标题数据以及显示的空间
	public void setTitles(List<String> titleLists, TextView top_news_title) {
		if (titleLists != null && titleLists.size() > 0
				&& top_news_title != null) {
			top_news_title.setText(titleLists.get(0));
		}

		this.mTitles = titleLists;
	}

	// 传递录播图图片数据集合
	public void setImages(List<String> imageLists) {
		this.mImages = imageLists;
	}

	private class RollAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return Integer.MAX_VALUE;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			// TODO Auto-generated method stub
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container,  int position) {

			View view = View.inflate(getContext(), R.layout.viewpager_item,
					null);

			ImageView image = (ImageView) view.findViewById(R.id.image);
			int pos1 = position % mImages.size();
			x.image().bind(image,"http://api.honeyshare.cn:8888/banner/"+mImages.get(pos1)+".jpg");
			container.addView(view);
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {

			container.removeView((View) object);
		}
	}

	// 事件分发方法
	// 事件分发方法


	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				startX = (int) ev.getX();
				if (refreshParent != null){
					Log.d("dispatch","RollViewPager+设置拦截");
					refreshParent.setShouldIntercept(true);
				}
				break;
			case MotionEvent.ACTION_MOVE:
				int endX = (int) ev.getX();
				//int endY = (int) ev.getY();
				int diffX = Math.abs(endX - startX);
				//int diffY = Math.abs(endY -startY);

				if (diffX > 10) {
					//让轮播图响应手指触摸方法
					//请求父控件是否进行拦截，如果是true，就是父控件不拦截，false则拦截

				}else{
					//让gridview响应

				}

				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				Log.d("dispatch","RollViewPager+取消拦截");
				refreshParent.setShouldIntercept(false);
				break;

		}

		return super.onTouchEvent(ev);
	}

	public void setPullRefreshParent(CustomRefreshFramework refreshParent){
		this.refreshParent = refreshParent;
	}


	public void stop() {//暂停发送消息
		mHandler.removeCallbacksAndMessages(null);
	}


}
