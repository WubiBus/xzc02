package cn.incorner.contrast.page;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.blur.Blur;
import cn.incorner.contrast.blur.FastBlur;
import cn.incorner.contrast.data.adapter.MessageAdapter;
import cn.incorner.contrast.data.entity.NewsInfoEntity;
import cn.incorner.contrast.data.entity.UserNewsListEntity;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.DataCacheUtil;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.view.CustomRefreshFramework;
import cn.incorner.contrast.view.CustomRefreshFramework.OnRefreshingListener;
import cn.incorner.contrast.view.CustomRefreshFramework.OnTouchMoveListener;
import cn.incorner.contrast.view.ScrollListenerListView;

import com.alibaba.fastjson.JSON;

/**
 * 消息 页面
 * 
 * @author yeshimin
 */
@ContentView(R.layout.activity_message)
public class MessageActivity extends BaseActivity implements OnTouchMoveListener {

	private static final String TAG = "MessageActivity";

	@ViewInject(R.id.rl_top_container)
	private RelativeLayout rlTopContainer;
	@ViewInject(R.id.rl_back)
	private RelativeLayout rlBack;
	@ViewInject(R.id.iv_clear)
	private ImageView ivClear;
	@ViewInject(R.id.crl_container)
	private CustomRefreshFramework crlContainer;
	@ViewInject(R.id.lv_content)
	private ScrollListenerListView lvContent;

	private List<NewsInfoEntity> listNewsInfo = new ArrayList<NewsInfoEntity>();
	private MessageAdapter messageAdapter;

	private Blur blur;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
		loadData();
	}

	private void init() {
		DD.d(TAG, "init()");

		blur = new Blur(this);

		// 初始化刷新框架
		View vHeader = getLayoutInflater().inflate(R.layout.view_custom_refresh_framework_header,
				null);
		lvContent.addHeaderView(vHeader);
		crlContainer.setOnRefreshingListener(new OnRefreshingListener() {
			@Override
			public void onRefreshing() {
				loadDataFromServer();
			}

			@Override
			public void onLoadMore() {
				loadDataFromServer();
			}

			@Override
			public void onRefreshFinish() {
				messageAdapter.notifyDataSetChanged();
			}
		});
		crlContainer.setOnTouchMoveListener(this);
		getWindow().getDecorView().getViewTreeObserver()
				.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						getWindow().getDecorView().getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
						crlContainer.setHeaderContainerHeight(rlTopContainer.getHeight());
					}
				});

		messageAdapter = new MessageAdapter(listNewsInfo, getLayoutInflater());
		lvContent.setAdapter(messageAdapter);
		lvContent.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				DD.d(TAG, "onItemClick(), position: " + position);

				--position;
				NewsInfoEntity entity = listNewsInfo.get(position);
//				Intent intent = new Intent();
//				intent.setClass(MessageActivity.this, ContrastDetailActivity.class);
//				intent.putExtra("nickname",entity.getInfo().getUserNickname());
//				intent.putExtra("paragraphId", entity.getInfo().getParagraphId());
//				gotoActivity(intent);
				Intent intent = new Intent();
				switch (entity.getInfoType()) {
				case NewsInfoEntity.INFO_TYPE_LIKE://赞
				case NewsInfoEntity.INFO_TYPE_DISLIKE:// 讨厌
				case NewsInfoEntity.INFO_TYPE_COMMENT://评论
				case NewsInfoEntity.INFO_TYPE_NEW_WORK://好友新作品通知
				case NewsInfoEntity.INFO_TYPE_BE_CONCERNED://被关注消息
				case NewsInfoEntity.INFO_TYPE_BE_SELECTED://标为有趣
				case NewsInfoEntity.INFO_TYPE_BE_RECHINCONTENT://标为有料
				intent.setClass(MessageActivity.this, ContrastDetailActivity.class);
				intent.putExtra("nickname",entity.getInfo().getUserNickname());
				intent.putExtra("paragraphId", entity.getInfo().getParagraphId());
				gotoActivity(intent);
					break;
				case NewsInfoEntity.INFO_TYPE_PRIVATEMESSAGE://留言
					intent.setClass(MessageActivity.this, SendPrivateMessagesActivity.class);
					intent.putExtra("userId", entity.getFromUserId());
					intent.putExtra("head",entity.getFromUserAvatarName());
					intent.putExtra("nickname",entity.getFromUserNickname());
					startActivity(intent);
					break;
				default:
					break;
			}
			}
		});
	}

	private void loadData() {
		DD.d(TAG, "loadData()");

		loadDataFromLocal();
//		 loadDataFromServer();
		crlContainer.refreshWithoutAnim();
	}

	/**
	 * 从本地加载数据（缓存）
	 */
	private void loadDataFromLocal() {
		DD.d(TAG, "loadDataFromLocal()");

		List<NewsInfoEntity> list = null;
		try {
			String data = DataCacheUtil.getStringData(Config.JSON_CACHE_PATH,Config.MESSAGE_CACHE_KEY+PrefUtil.getIntValue(Config.PREF_USER_ID));
			list = JSON.parseArray(data, NewsInfoEntity.class);
		} catch (Exception e) {
			e.printStackTrace();
			DataCacheUtil.clearStringData(Config.JSON_CACHE_PATH, Config.MESSAGE_CACHE_KEY+PrefUtil.getIntValue(Config.PREF_USER_ID));
		}
		if (list == null || list.isEmpty()) {
			return;
		}
		listNewsInfo.clear();
		listNewsInfo.addAll(list);
		messageAdapter.notifyDataSetChanged();
	}

	/**
	 * 保存数据
	 */
	private void save(String data) {
		DD.d(TAG, "save()");

		if (TextUtils.isEmpty(data)) {
			return;
		}

		try {
//			DataCacheUtil.clearStringData(Config.JSON_CACHE_PATH, Config.MESSAGE_CACHE_KEY);
			DataCacheUtil.putStringData(Config.JSON_CACHE_PATH, Config.MESSAGE_CACHE_KEY+PrefUtil.getIntValue(Config.PREF_USER_ID), data);
		} catch (Exception e) {
			e.printStackTrace();
//			DataCacheUtil.clearStringData(Config.JSON_CACHE_PATH, Config.MESSAGE_CACHE_KEY);
		}
	}

	/**
	 * 从服务器加载数据
	 */
	private void loadDataFromServer() {
		DD.d(TAG, "loadDataFromServer()");

		RequestParams params = new RequestParams(Config.PATH_GET_USER_NEWS);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("rows", 1000);
		x.http().post(params, new CommonCallback<JSONObject>() {
			@Override
			public void onCancelled(CancelledException arg0) {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
			}

			@Override
			public void onFinished() {
				crlContainer.setRefreshing(false);
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result.toString());

				UserNewsListEntity entity = JSON.parseObject(result.toString(),
						UserNewsListEntity.class);
				if ("0".equals(entity.getStatus())) {
					listNewsInfo.addAll(0, entity.getNewsInfos());
					save(JSON.toJSONString(listNewsInfo));
				}
			}
		});
	}

	@Event(value = R.id.rl_top_container)
	private void onTopContainerClick(View v) {
		// do nothing, just for intercept click event
	}

	@Event(value = R.id.rl_back)
	private void onBackClick(View v) {
		DD.d(TAG, "onBackClick()");

		finish();
	}

	@Event(value = R.id.iv_clear)
	private void onClearClick(View v) {
		DD.d(TAG, "onClearClick()");

		DataCacheUtil.clearStringData(Config.JSON_CACHE_PATH, Config.MESSAGE_CACHE_KEY+PrefUtil.getIntValue(Config.PREF_USER_ID));
		listNewsInfo.clear();
		messageAdapter.notifyDataSetChanged();
	}

	// 滚动监听、毛玻璃效果等 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**
	 * doBlur
	 */
	private void doBlur() {
		// DD.d(TAG, "doBlur()");

		crlContainer.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				// DD.d(TAG, "onPreDraw()");

				crlContainer.getViewTreeObserver().removeOnPreDrawListener(this);
				crlContainer.buildDrawingCache();
				Bitmap bitmap = crlContainer.getDrawingCache();

				blur(bitmap, rlTopContainer, rlTopContainer.getTop(), false);
				return false;
			}
		});
	}

	/**
	 * blur
	 */
	private void blur(Bitmap bitmap, View view, int viewTop, boolean useNativeCode) {
		if (bitmap == null) {
			return;
		}

		int bitmapWidth = bitmap.getWidth();
		int bitmapHeight = bitmap.getHeight();
		int viewWidth = view.getMeasuredWidth();
		int viewHeight = view.getMeasuredHeight();
		float scaleFactor = 8;
		float radius = 20;

		// DD.d(TAG, "bitmapWidth: " + bitmapWidth + ", bitmapHeight: " + bitmapHeight
		// + ", viewWidth: " + viewWidth + ", viewHeight: " + viewHeight + ", viewTop: "
		// + viewTop);

		Bitmap overlay = Bitmap.createBitmap((int) (viewWidth / scaleFactor),
				(int) (viewHeight / scaleFactor), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(overlay);
		canvas.translate(0, -viewTop);
		canvas.scale(1 / scaleFactor, 1 / scaleFactor, 0, viewTop);

		Paint paint = new Paint();
		paint.setFlags(Paint.FILTER_BITMAP_FLAG);
		canvas.drawBitmap(bitmap, 0, 0, paint);

		if (useNativeCode) {
			overlay = blur.blur(overlay, true);
		} else {
			overlay = FastBlur.doBlur(overlay, (int) radius, true);
		}
		view.setBackgroundDrawable(new BitmapDrawable(getResources(), overlay));
	}

	TranslateAnimation taShowTop;
	TranslateAnimation taHideTop;

	/**
	 * 显示浮动视图
	 */
	private void showFloatingView() {
		if (rlTopContainer.getVisibility() == View.VISIBLE) {
			return;
		}

		if (taShowTop == null) {
			taShowTop = new TranslateAnimation(0, 0, -rlTopContainer.getHeight(), 0);
			taShowTop.setDuration(250);
			rlTopContainer.setAnimation(taShowTop);
			taShowTop.setAnimationListener(new AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					rlTopContainer.setVisibility(View.VISIBLE);
				}
			});
		}
		rlTopContainer.startAnimation(taShowTop);
	}

	/**
	 * 隐藏浮动视图
	 */
	private void hideFloatingView() {
		if (rlTopContainer.getVisibility() == View.GONE) {
			return;
		}

		if (taHideTop == null) {
			taHideTop = new TranslateAnimation(0, 0, 0, -rlTopContainer.getHeight());
			taHideTop.setDuration(250);
			rlTopContainer.setAnimation(taHideTop);
			taHideTop.setAnimationListener(new AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					rlTopContainer.setVisibility(View.GONE);
				}
			});
		}
		rlTopContainer.startAnimation(taHideTop);
	}

	private long lastBlurTime;
	private long currentTime;

	@Override
	public void onVerticalMoving() {
		currentTime = System.currentTimeMillis();
		if (currentTime - lastBlurTime < 100) {
			return;
		}
		lastBlurTime = currentTime;

		doBlur();
	}

	@Override
	public void onMoveUp() {
		DD.d(TAG, "onMoveUp()");
		hideFloatingView();
	}

	@Override
	public void onMoveDown() {
		DD.d(TAG, "onMoveDown()");
		showFloatingView();
	}

	// 滚动监听、毛玻璃效果等 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

}
