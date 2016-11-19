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
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.incorner.contrast.BaseFragmentActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.blur.Blur;
import cn.incorner.contrast.blur.FastBlur;
import cn.incorner.contrast.data.adapter.ContrastListAdapter;
import cn.incorner.contrast.data.entity.CommentResultEntity;
import cn.incorner.contrast.data.entity.ParagraphCommentEntity;
import cn.incorner.contrast.data.entity.ParagraphEntity;
import cn.incorner.contrast.data.entity.ParagraphResultEntity;
import cn.incorner.contrast.util.CommonUtil;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.view.CustomRefreshFramework;
import cn.incorner.contrast.view.CustomRefreshFramework.OnRefreshingListener;
import cn.incorner.contrast.view.CustomRefreshFramework.OnTouchMoveListener;
import cn.incorner.contrast.view.RefreshingAnimationView;
import cn.incorner.contrast.view.RefreshingAnimationView.IRefreshingAnimationView;
import cn.incorner.contrast.view.ScrollListenerListView;

import com.alibaba.fastjson.JSON;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

/**
 * 搜索对比度 页面
 * 
 * @author yeshimin
 */
@ContentView(R.layout.activity_search_paragraph)
public class SearchParagraphActivity extends BaseFragmentActivity implements OnTouchMoveListener,
		IRefreshingAnimationView {

	private static final String TAG = "SearchParagraphActivity";

	@ViewInject(R.id.crl_container)
	private CustomRefreshFramework crlContainer;
	@ViewInject(R.id.lv_content)
	private ScrollListenerListView lvContent;
	@ViewInject(R.id.ll_nav_container)
	private LinearLayout llNavContainer;
	@ViewInject(R.id.rl_back)
	private RelativeLayout rlBack;
	@ViewInject(R.id.tv_keyword)
	private TextView tvKeyword;

	// 刷新视图
	@ViewInject(R.id.rav_refreshing_view)
	private RefreshingAnimationView ravRefreshingView;

	private List<ParagraphEntity> list = new ArrayList<ParagraphEntity>();
	private ContrastListAdapter adapter;
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
			tvKeyword.setText("包含“" + keyword + "”的大作");
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
		lvContent.addHeaderView(vHeader);
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

		adapter = new ContrastListAdapter((ArrayList<ParagraphEntity>) list, getLayoutInflater(),
				new OnContrastListItemClickListener());
		lvContent.setAdapter(adapter);
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

		RequestParams params = new RequestParams(Config.PATH_GET_PARAGRAPH_BY_TAG);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("type", 0);
		params.addParameter("tag", keyword);
		params.addParameter("from", 0);
		params.addParameter("row", Config.LOAD_COUNT);
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

				ParagraphResultEntity entity = JSON.parseObject(result.toString(),
						ParagraphResultEntity.class);
				if ("0".equals(entity.getStatus())) {
					list.clear();
					list.addAll(entity.getParagraphs());
				}
			}
		});
	}

	/**
	 * 从服务器加载更多刷新数据
	 */
	private void loadMoreDataFromServer() {
		DD.d(TAG, "loadMoreDataFromServer()");

		RequestParams params = new RequestParams(Config.PATH_GET_PARAGRAPH_BY_TAG);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("type", 0);
		params.addParameter("tag", keyword);
		params.addParameter("from", list.size());
		params.addParameter("row", Config.LOAD_COUNT);
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
				DD.d(TAG, "onSuccess()");

				ParagraphResultEntity entity = JSON.parseObject(result.toString(),
						ParagraphResultEntity.class);
				if ("0".equals(entity.getStatus())) {
					list.addAll(entity.getParagraphs());
				}
			}
		});
	}

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

	/**
	 * 点击大图
	 */
	private void onImageClick(int position) {
		DD.d(TAG, "onImageClick(), position: " + position);

		Intent intent = new Intent();
		intent.setClass(this, ContrastCommentActivity.class);
		intent.putExtra("paragraph", list.get(position));
		gotoActivity(intent);
	}

	/**
	 * 点击小头像
	 */
	private void onHeadClick(int position) {
		DD.d(TAG, "onHeadClick(), position: " + position);

		Intent intent = new Intent();
		intent.setClass(this, UserParagraphListActivity.class);
		ParagraphEntity entity = list.get(position);
		intent.putExtra("userId", entity.getUserId());
		intent.putExtra("head", entity.getUserAvatarName());
		intent.putExtra("nickname", entity.getUserNickname());
		gotoActivity(intent);
	}

	/**
	 * 点击qq
	 */
	private void onQqClick(int position) {
		DD.d(TAG, "onQqClick(), position: " + position);

		new ShareAction(this).setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.SINA, SHARE_MEDIA.WEIXIN,
				SHARE_MEDIA.WEIXIN_CIRCLE).open();
	}

	/**
	 * 点击weixin
	 */
	private void onWeixinClick(int position) {
		DD.d(TAG, "onWeixin(), position: " + position);

		new ShareAction(this).setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.SINA, SHARE_MEDIA.WEIXIN,
				SHARE_MEDIA.WEIXIN_CIRCLE).open();
	}

	/**
	 * 点击weibo
	 */
	private void onWeiboClick(int position) {
		DD.d(TAG, "onWeiboClick(), position: " + position);

		new ShareAction(this).setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.SINA, SHARE_MEDIA.WEIXIN,
				SHARE_MEDIA.WEIXIN_CIRCLE).open();
	}

	/**
	 * 内容列表项内部视图点击监听类
	 */
	private class OnContrastListItemClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.ll_image_container:
			case R.id.iv_compatible_image:
				onImageClick((int) v.getTag());
				break;
			case R.id.civ_head:
				onHeadClick((int) v.getTag());
				break;
			case R.id.ll_qq:
				onQqClick((int) v.getTag());
				break;
			case R.id.ll_weixin:
				onWeixinClick((int) v.getTag());
				break;
			case R.id.ll_weibo:
				onWeiboClick((int) v.getTag());
				break;
			}
		}
	}

	@Override
	protected void onResume() {
		DD.d(TAG, "onResume()");

		if (adapter.getCount() > 0 && lvContent.getChildCount() > 0) {
			int position = lvContent.getFirstVisiblePosition();
			int childCount = lvContent.getChildCount();
			DD.d(TAG, "position: " + position);
			DD.d(TAG, "childCount: " + childCount);
			if (position == 0) {
				childCount--;
			} else {
				position--;
			}
			int[] arrPos = new int[childCount];
			for (int i = 0; i < childCount; ++i) {
				arrPos[i] = position + i;
			}

			// 因为可能同时显示多个item，所以要对每个item进行操作
			for (int i = 0; i < arrPos.length; ++i) {
				final ParagraphEntity paragraphEntity = list.get(arrPos[i]);
				String paragraphReplyId = paragraphEntity.getParagraphReplyId();

				RequestParams params = new RequestParams(
						Config.PATH_GET_COMMENTS_BY_PARAGRAPH_REPLY_IDS);
				params.setAsJsonContent(true);
				params.addParameter("accessToken",
						PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
				params.addParameter("paragraphReplyId", paragraphReplyId);
				params.addParameter("from", 0);
				params.addParameter("rows", Config.LOAD_COUNT * 50);
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
					}

					@Override
					public void onSuccess(JSONObject result) {
						DD.d(TAG, "onSuccess(), result: " + result);
						final CommentResultEntity entity = JSON.parseObject(result.toString(),
								CommentResultEntity.class);
						if ("0".equals(entity.getStatus())) {
							List<ParagraphCommentEntity> listComment = paragraphEntity
									.getComments();
							listComment.clear();
							listComment.addAll(entity.getComments());
							paragraphEntity.setCommentCount(listComment.size());
							adapter.notifyDataSetChanged();
						}
					}
				});
			}
		}

		super.onResume();
	}

	// RefreshingAnimationView相关 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	public void showRefreshingAnimationView() {
		ravRefreshingView.setRefreshing(true);
	}

	public void hideRefreshingAnimationView() {
		ravRefreshingView.setRefreshing(false);
	}

	// RefreshingAnimationView相关 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
