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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.blur.Blur;
import cn.incorner.contrast.blur.FastBlur;
import cn.incorner.contrast.data.adapter.SearchUserResultListAdapter;
import cn.incorner.contrast.data.entity.FollowerEntity;
import cn.incorner.contrast.data.entity.SearchUserEntity;
import cn.incorner.contrast.data.entity.SearchUserResultEntity;
import cn.incorner.contrast.util.CommonUtil;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.view.CustomRefreshFramework;
import cn.incorner.contrast.view.CustomRefreshFramework.OnRefreshingListener;
import cn.incorner.contrast.view.CustomRefreshFramework.OnTouchMoveListener;
import cn.incorner.contrast.view.HeaderFooterGridView;
import cn.incorner.contrast.view.RefreshingAnimationView;

import com.alibaba.fastjson.JSON;

/**
 * 搜索用户 页面
 * 
 * @author yeshimin
 */
@ContentView(R.layout.activity_search_user)
public class SearchUserActivity extends BaseActivity implements OnTouchMoveListener {

	private static final String TAG = "SearchUserActivity";

	@ViewInject(R.id.crl_container)
	private CustomRefreshFramework crlContainer;
	@ViewInject(R.id.gv_content)
	private HeaderFooterGridView gvContent;
	@ViewInject(R.id.ll_nav_container)
	private LinearLayout llNavContainer;
	@ViewInject(R.id.rl_back)
	private RelativeLayout rlBack;
	@ViewInject(R.id.tv_keyword)
	private TextView tvKeyword;

	// 刷新视图
	@ViewInject(R.id.rav_refreshing_view)
	private RefreshingAnimationView ravRefreshingView;

	private List<SearchUserEntity> list = new ArrayList<SearchUserEntity>();
	private SearchUserResultListAdapter adapter;
	private String keyword;

	private Blur blur;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		keyword = getIntent().getStringExtra("keyword");
		if (TextUtils.isEmpty(keyword)) {
			finish();
			return;
		} else {
			tvKeyword.setText("包含“" + keyword + "”的人");
		}

		init();
		loadData();
	}

	private void init() {
		DD.d(TAG, "init()");

		blur = new Blur(this);

		// 初始化刷新框架
		View vHeader = getLayoutInflater().inflate(R.layout.view_custom_refresh_framework_header,
				null);
		gvContent.addHeaderView(vHeader);
		crlContainer.setOnRefreshingListener(new OnRefreshingListener() {
			@Override
			public void onRefreshing() {
				refreshDataFromServer();
			}

			@Override
			public void onLoadMore() {
				loadMoreDataFromServer();
			}

			@Override
			public void onRefreshFinish() {
				DD.d(TAG, "onRefreshFinish()");
				DD.d(TAG, "adapter.size: " + adapter.getCount());
				DD.d(TAG, "list.size: " + list.size());
				adapter.notifyDataSetChanged();
				hideRefreshingAnimationView();
			}
		});
		crlContainer.setOnTouchMoveListener(this);
		getWindow().getDecorView().getViewTreeObserver()
				.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						DD.d("tag", "onGlobalLayout()");
						getWindow().getDecorView().getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
						crlContainer.setHeaderContainerHeight(llNavContainer.getHeight());
						doBlur();
					}
				});

		adapter = new SearchUserResultListAdapter((ArrayList<SearchUserEntity>) list,getLayoutInflater(), SearchUserActivity.this);
		gvContent.setAdapter(adapter);
	}

	private void loadData() {
		DD.d(TAG, "loadData()");

		showRefreshingAnimationView();
		crlContainer.refreshWithoutAnim();
	}

	/**
	 * 从服务器刷新数据
	 */
	private void refreshDataFromServer() {
		DD.d(TAG, "refreshDataFromServer()");

		RequestParams params = new RequestParams(Config.PATH_FIND_USER);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("keyword", keyword);
		params.addParameter("from", 0);
		params.addParameter("rows", 50);
		params.addParameter("timestamp", CommonUtil.getDefaultFormatCurrentTime());
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

				SearchUserResultEntity entity = JSON.parseObject(result.toString(),
						SearchUserResultEntity.class);
				if ("0".equals(entity.getStatus())) {
					list.clear();
					list.addAll(entity.getUserInfos());
				}
			}
		});
	}

	/**
	 * 从服务器加载更多刷新数据
	 */
	private void loadMoreDataFromServer() {
		DD.d(TAG, "loadMoreDataFromServer()");

		RequestParams params = new RequestParams(Config.PATH_FIND_USER);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("keyword", keyword);
		params.addParameter("from", list.size());
		params.addParameter("rows", 50);
		params.addParameter("timestamp", CommonUtil.getDefaultFormatCurrentTime());
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

				SearchUserResultEntity entity = JSON.parseObject(result.toString(),
						SearchUserResultEntity.class);
				if ("0".equals(entity.getStatus())) {
					list.addAll(entity.getUserInfos());
				}
			}
		});
	}

	// RefreshingAnimationView相关 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	public void showRefreshingAnimationView() {
		ravRefreshingView.setRefreshing(true);
	}

	public void hideRefreshingAnimationView() {
		ravRefreshingView.setRefreshing(false);
	}

	// RefreshingAnimationView相关 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	@Event(value = R.id.ll_nav_container)
	private void onNavContainerClick(View v) {
		// do nothing, just for intercept click event
	}

	@Event(value = R.id.rl_bottom_container)
	private void onBottomContainerClick(View v) {
		// do nothing, just for intercept click event
	}

	@Event(value = R.id.rl_back)
	private void onBackClick(View v) {
		DD.d(TAG, "onBackClick()");

		finish();
	}

	@Event(value = R.id.iv_post)
	private void onPostClick(View v) {
		DD.d(TAG, "onPostClick()");

		if (!isLogined()) {
			gotoActivity(LoginTransitionActivity.class);
			finish();
			return;
		}
		gotoActivity(PostActivity.class);
	}

	@Event(value = R.id.gv_content, type = AdapterView.OnItemClickListener.class)
	private void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		DD.d(TAG, "onItemClick(), position: " + position);
		Intent intent = new Intent();
		SearchUserEntity entity = list.get(position);
		intent.setClass(this, MyFollowingUserParagraphActivity.class);
		intent.putExtra("userId", entity.getUserId());
		intent.putExtra("head", entity.getAvatarName());
		intent.putExtra("nickname", entity.getNickname());
		BaseActivity.sGotoActivity(this, intent);
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

				blur(bitmap, llNavContainer, llNavContainer.getTop(), true);
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
	TranslateAnimation taShowBottom;
	TranslateAnimation taHideBottom;

	/**
	 * 显示浮动视图
	 */
	private void showFloatingView() {
		if (llNavContainer.getVisibility() == View.VISIBLE) {
			return;
		}

		if (taShowTop == null) {
			taShowTop = new TranslateAnimation(0, 0, -llNavContainer.getHeight(), 0);
			taShowTop.setDuration(250);
			llNavContainer.setAnimation(taShowTop);
			taShowTop.setAnimationListener(new AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					llNavContainer.setVisibility(View.VISIBLE);
				}
			});
		}
		llNavContainer.startAnimation(taShowTop);
	}

	/**
	 * 隐藏浮动视图
	 */
	private void hideFloatingView() {
		if (llNavContainer.getVisibility() == View.GONE) {
			return;
		}

		if (taHideTop == null) {
			taHideTop = new TranslateAnimation(0, 0, 0, -llNavContainer.getHeight());
			taHideTop.setDuration(250);
			llNavContainer.setAnimation(taHideTop);
			taHideTop.setAnimationListener(new AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					llNavContainer.setVisibility(View.GONE);
				}
			});
		}
		llNavContainer.startAnimation(taHideTop);
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
		hideFloatingView();
	}

	@Override
	public void onMoveDown() {
		showFloatingView();
	}

	// 滚动监听、毛玻璃效果等 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

}
