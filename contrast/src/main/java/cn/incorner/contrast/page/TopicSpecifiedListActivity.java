package cn.incorner.contrast.page;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.ex.DbException;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
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
import cn.incorner.contrast.util.DensityUtil;
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
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

/**
 * 指定话题 页面
 * 
 * @author yeshimin
 */
@ContentView(R.layout.activity_topic_specified_list)
public class TopicSpecifiedListActivity extends BaseFragmentActivity implements
		OnTouchMoveListener, IRefreshingAnimationView {

	private static final String TAG = "TopicSpecifiedListActivity";

	@ViewInject(R.id.crl_container)
	private CustomRefreshFramework crlContainer;
	@ViewInject(R.id.lv_content)
	private ScrollListenerListView lvContent;
	@ViewInject(R.id.ll_nav_container)
	private LinearLayout llNavContainer;
	@ViewInject(R.id.rl_back)
	private RelativeLayout rlBack;
	@ViewInject(R.id.tv_topic)
	private TextView tvTopic;
	@ViewInject(R.id.rl_bottom_container)
	private RelativeLayout rlBottomContainer;
	@ViewInject(R.id.ll_bottom_container)
	private LinearLayout llBottomContainer;
	@ViewInject(R.id.iv_post)
	private ImageView ivPost;

	// 刷新视图
	@ViewInject(R.id.rav_refreshing_view)
	private RefreshingAnimationView ravRefreshingView;

	private List<ParagraphEntity> list = new ArrayList<ParagraphEntity>();
	private ContrastListAdapter adapter;
	private String topicName;
	private ParagraphEntity newEntity;
	// 是否是（当前）用户的数据
	private boolean isUserData;

	private Blur blur;

	DbManager.DaoConfig daoConfig = new DbManager.DaoConfig().setDbName("my_paragraph.db")
			.setDbVersion(1).setDbOpenListener(new DbManager.DbOpenListener() {
				@Override
				public void onDbOpened(DbManager db) {
					// 开启WAL, 对写入加速提升巨大
					db.getDatabase().enableWriteAheadLogging();
				}
			}).setDbUpgradeListener(new DbManager.DbUpgradeListener() {
				@Override
				public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
				}
			});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		topicName = getIntent().getStringExtra("topicName");
		newEntity = getIntent().getParcelableExtra("newEntity");
		isUserData = getIntent().getBooleanExtra("isUserData", false);

		// TODO: 2017/1/11 BUG 
		//DD.d(TAG, "主页传来的数据。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。"+ newEntity.getUserNickname());
		if (TextUtils.isEmpty(topicName)) {
			finish();
			return;
		}

		init();
		loadData();
	}

	private void init() {
		DD.d(TAG, "init()");

		blur = new Blur(this);

		tvTopic.setText(topicName);
		if(topicName.indexOf("本周最佳")!=-1){
			llBottomContainer.setVisibility(View.INVISIBLE);
		}
		if (isUserData) {
			rlBottomContainer.setVisibility(View.INVISIBLE);
		} else {
			rlBottomContainer.setVisibility(View.VISIBLE);
		}
		if (topicName.contains(Config.PATTERN_HORN)) {
			ivPost.setImageResource(R.drawable.post_activity);
		}else if(topicName.contains(Config.PATTERN_THISWEEK_GRATE)){
			ivPost.setVisibility(View.GONE);
		}

		// 初始化刷新框架
		View vHeader = getLayoutInflater().inflate(R.layout.view_custom_refresh_framework_header,
				null);
		lvContent.addHeaderView(vHeader);
		crlContainer.setOnRefreshingListener(new OnRefreshingListener() {
			@Override
			public void onRefreshing() {
				refreshData();
			}

			@Override
			public void onLoadMore() {
				loadMoreData();
			}

			@Override
			public void onRefreshFinish() {
				adapter.notifyDataSetChanged();
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

		// refreshData();
		crlContainer.refreshWithoutAnim();
	}

	private void refreshData() {
		DD.d(TAG, "refreshData()");

		if (isUserData) {
			refreshDataFromLocal();
		} else {
			refreshDataFromServer();
		}
	}

	private void loadMoreData() {
		DD.d(TAG, "loadMoreData()");

		if (isUserData) {
			loadMoreDataFromLocal();
		} else {
			loadMoreDataFromServer();
		}
	}

	/**
	 * 从本地刷新数据
	 */
	private void refreshDataFromLocal() {
		DD.d(TAG, "refreshDataFromLocal()");

		DbManager db = x.getDb(daoConfig);
		try {
			List<ParagraphEntity> listLocal = db.selector(ParagraphEntity.class)
					.where("tags", "like", "%" + topicName + ":@(0)%").limit(Config.LOAD_COUNT)
					.findAll();
			if (listLocal != null && !listLocal.isEmpty()) {
				// fill comments
				for (ParagraphEntity entity : listLocal) {
					entity.setComments(entity.getChildren(db));
				}

				list.clear();
				list.addAll(listLocal);
			}
		} catch (DbException e) {
			e.printStackTrace();
		} finally {
			crlContainer.setRefreshing(false);
		}
	}

	/**
	 * 从本地加载更多数据
	 */
	private void loadMoreDataFromLocal() {
		DD.d(TAG, "loadMoreDataFromLocal()");

		DbManager db = x.getDb(daoConfig);
		try {
			List<ParagraphEntity> listLocal = db.selector(ParagraphEntity.class)
					.where("tags", "like", "%" + topicName + ":@(0)%").offset(list.size())
					.limit(Config.LOAD_COUNT).findAll();
			if (listLocal != null && !listLocal.isEmpty()) {
				list.addAll(listLocal);
			}
		} catch (DbException e) {
			e.printStackTrace();
		} finally {
			crlContainer.setRefreshing(false);
		}
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
		params.addParameter("tag", topicName.replace("#", ""));
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
		params.addParameter("tag", topicName);
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

	//点击参与话题
	@Event(value = R.id.iv_post)
	private void onPostClick(View v) {
		DD.d(TAG, "onPostClick()");

		if (!isLogined()) {
			gotoActivity(LoginTransitionActivity.class);
			finish();
			return;
		}
		Intent intent = new Intent();
		intent.setClass(this, PostActivity.class);
		intent.putExtra("topicNameFromTopicListPage", topicName);
		gotoActivity(intent);
	}

	@Event(value = R.id.share_topic)
	private void onShareTopicClick(View v) {
		shareAll(newEntity);
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
				if (!isUserData) {
					blur(bitmap, llBottomContainer,
							crlContainer.getHeight() - llBottomContainer.getHeight(), false);
				}

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

		if (!isUserData) {
			if (taShowBottom == null) {
				taShowBottom = new TranslateAnimation(0, 0, rlBottomContainer.getHeight(), 0);
				taShowBottom.setDuration(250);
				rlBottomContainer.setAnimation(taShowBottom);
				taShowBottom.setAnimationListener(new AnimationListener() {
					public void onAnimationStart(Animation animation) {
					}

					public void onAnimationRepeat(Animation animation) {
					}

					public void onAnimationEnd(Animation animation) {
						rlBottomContainer.setVisibility(View.VISIBLE);
					}
				});
			}
			rlBottomContainer.startAnimation(taShowBottom);
		}
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

		if (!isUserData) {
			if (taHideBottom == null) {
				taHideBottom = new TranslateAnimation(0, 0, 0, rlBottomContainer.getHeight());
				taHideBottom.setDuration(250);
				rlBottomContainer.setAnimation(taHideBottom);
				taHideBottom.setAnimationListener(new AnimationListener() {
					public void onAnimationStart(Animation animation) {
					}

					public void onAnimationRepeat(Animation animation) {
					}

					public void onAnimationEnd(Animation animation) {
						rlBottomContainer.setVisibility(View.GONE);
					}
				});
			}
			rlBottomContainer.startAnimation(taHideBottom);
		}
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

	/**
	 * 点击分享
	 */
	private void shareAll(ParagraphEntity entity) {
		String text = java.net.URLEncoder.encode(topicName);
		final String title = entity.getUserNickname() + "的大作";
		final String targetUrl = Config.PATH_TOP_SHARE + text;
		//final String text = getShareText(getDesc(entity.getParagraphContent()));
		// 设置缩略图
		Bitmap bitmap = adapter.getSharedCacheBitmap();

		UMImage umTempImage;
		if (bitmap != null) {
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, DensityUtil.dip2px(this, 100),
					DensityUtil.dip2px(this, 100));
			umTempImage = new UMImage(this, bitmap);
		} else {
			umTempImage = new UMImage(this, R.drawable.icon);
		}
		final UMImage umImage = umTempImage;

		new ShareAction(this).setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA.WEIXIN,
				SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.SINA)
				.setShareboardclickCallback(new ShareBoardlistener() {
					@Override
					public void onclick(SnsPlatform arg0, SHARE_MEDIA shareMedia) {
						if (SHARE_MEDIA.QQ.equals(shareMedia)) {
							new ShareAction(TopicSpecifiedListActivity.this)
									.setPlatform(SHARE_MEDIA.QQ).withTitle(title).withText(topicName)
									.withTargetUrl(targetUrl).withMedia(umImage).share();
						} else if (SHARE_MEDIA.QZONE.equals(shareMedia)) {
							new ShareAction(TopicSpecifiedListActivity.this)
									.setPlatform(SHARE_MEDIA.QZONE).withTitle(title).withText(topicName)
									.withTargetUrl(targetUrl).withMedia(umImage).share();
						} else if (SHARE_MEDIA.WEIXIN.equals(shareMedia)) {
							new ShareAction(TopicSpecifiedListActivity.this)
									.setPlatform(SHARE_MEDIA.WEIXIN).withTitle(title).withText(topicName)
									.withTargetUrl(targetUrl).withMedia(umImage).share();
						} else if (SHARE_MEDIA.WEIXIN_CIRCLE.equals(shareMedia)) {
							new ShareAction(TopicSpecifiedListActivity.this)
									.setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE).withTitle(title).withText(topicName)
									.withTargetUrl(targetUrl).withMedia(umImage)
									.share();
						} else if (SHARE_MEDIA.SINA.equals(shareMedia)) {
							new ShareAction(TopicSpecifiedListActivity.this)
									.setPlatform(SHARE_MEDIA.SINA).withText(topicName)
									.withTargetUrl(targetUrl).withMedia(umImage).share();
						}
					}
				}).open();
	}

}
