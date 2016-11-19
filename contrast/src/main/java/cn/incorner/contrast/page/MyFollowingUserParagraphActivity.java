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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.BaseFragmentActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.blur.Blur;
import cn.incorner.contrast.blur.FastBlur;
import cn.incorner.contrast.data.adapter.MyFollowingUserParagraphAdapter;
import cn.incorner.contrast.data.entity.FollowingEntity;
import cn.incorner.contrast.data.entity.ParagraphEntity;
import cn.incorner.contrast.data.entity.ParagraphResultEntity;
import cn.incorner.contrast.data.entity.StatusResultEntity;
import cn.incorner.contrast.data.entity.UserInfoEntity;
import cn.incorner.contrast.util.CommonUtil;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.util.TT;
import cn.incorner.contrast.view.CircleImageView;
import cn.incorner.contrast.view.CustomRefreshFramework;
import cn.incorner.contrast.view.HeaderFooterGridView;
import cn.incorner.contrast.view.RefreshingAnimationView;
import cn.incorner.contrast.view.CustomRefreshFramework.OnRefreshingListener;
import cn.incorner.contrast.view.CustomRefreshFramework.OnTouchMoveListener;
import cn.incorner.contrast.view.RefreshingAnimationView.IRefreshingAnimationView;

import com.alibaba.fastjson.JSON;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

@ContentView(R.layout.activity_myfollowinguser)
public class MyFollowingUserParagraphActivity extends BaseFragmentActivity
		implements OnTouchMoveListener, IRefreshingAnimationView {

	private static final String TAG = "UserParagraphListActivity";

	@ViewInject(R.id.ll_top_container)
	private LinearLayout llTopContainer;
	@ViewInject(R.id.rl_back)
	private RelativeLayout rlBack;
	@ViewInject(R.id.civ_head)
	private CircleImageView civHead;
	@ViewInject(R.id.tv_nickname)
	private TextView tvNickname;
	@ViewInject(R.id.ll_contrast_info)
	private LinearLayout llContrastInfo;
	@ViewInject(R.id.tv_contrast_amount)
	private TextView tvContrastAmount;
	@ViewInject(R.id.tv_follower_amount)
	private TextView tvFollowerAmount;
	@ViewInject(R.id.tv_score_amount)
	private TextView tvScoreAmount;
	@ViewInject(R.id.tv_signature)
	private TextView tvSignature;
	@ViewInject(R.id.ll_more_user_info)
	private LinearLayout llMoreUserInfo;
	@ViewInject(R.id.tv_born)
	private TextView tvBorn;
	@ViewInject(R.id.tv_gender)
	private TextView tvGender;
	@ViewInject(R.id.ll_contact)
	private LinearLayout llContact;
	@ViewInject(R.id.tv_contact)
	private TextView tvContact;
	@ViewInject(R.id.rl_follow)
	private RelativeLayout rlFollow;
	@ViewInject(R.id.iv_follow)
	private ImageView ivFollow;
	@ViewInject(R.id.rl_bottom_container)
	private RelativeLayout rlBottomContainer;
	@ViewInject(R.id.ll_bottom_container)
	private LinearLayout llBottomContainer;
	@ViewInject(R.id.iv_send)
	private ImageView ivSend;
	@ViewInject(R.id.crl_container)
	private CustomRefreshFramework crlContainer;
	@ViewInject(R.id.gv_content)
	private HeaderFooterGridView gvContent;

	// 刷新视图
	@ViewInject(R.id.rav_refreshing_view)
	private RefreshingAnimationView ravRefreshingView;


	private ArrayList<ParagraphEntity> listParagraph = new ArrayList<ParagraphEntity>();
	private ParagraphResultEntity paragraphResultEntity;
	private MyFollowingUserParagraphAdapter adapter;

	private int userId;
	private String head;
	private String nickname;
	private UserInfoEntity userInfoEntity;
	private boolean hasLoadedUserInfo = false;
	private Blur blur;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		userId = getIntent().getIntExtra("userId", -1);
		head = getIntent().getStringExtra("head");
		nickname = getIntent().getStringExtra("nickname");

		init();
		loadData();
	}

	private void init() {
		DD.d(TAG, "init()");

		blur = new Blur(this);

		// 初始化刷新框架
		View vHeader = getLayoutInflater().inflate(
				R.layout.view_custom_refresh_framework_header, null);
		gvContent.addHeaderView(vHeader);
		crlContainer.setOnRefreshingListener(new OnRefreshingListener() {
			@Override
			public void onRefreshing() {
				/*if (!hasLoadedUserInfo) {
					loadUserInfoFromServer();
				}
				refreshContrastListFromServer();*/

				// TODO: 2016/9/26 改过之后的
				if (hasLoadedUserInfo) {
					refreshContrastListFromServer();
				} else {
					loadUserInfoFromServer();
				}
			}

			@Override
			public void onLoadMore() {
				if (!hasLoadedUserInfo) {
					loadUserInfoFromServer();
				}
				loadMoreContrastListFromServer();
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
						getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
						crlContainer.setHeaderContainerHeight(llTopContainer.getHeight());
						Bitmap bitmap = crlContainer.getDrawingCache();
						blur(bitmap, llBottomContainer,crlContainer.getHeight() - llBottomContainer.getHeight(), false);
					}
				});

		adapter = new MyFollowingUserParagraphAdapter(listParagraph, getLayoutInflater());
		gvContent.setAdapter(adapter);

		setBasicView();
	}

	private void setBasicView() {
		DD.d(TAG, "setBasicView()");

		if (!TextUtils.isEmpty(head)) {
			x.image().loadDrawable(Config.getHeadFullPath(head), null,
					new CommonCallback<Drawable>() {
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
						public void onSuccess(Drawable drawable) {
							Bitmap bitmap = ((BitmapDrawable) drawable)
									.getBitmap();
							civHead.setImageBitmap(bitmap);
						}
					});
		}
		tvNickname.setText(nickname);
	}

	private void setMoreView() {
		DD.d(TAG, "setMoreView()");

		llContrastInfo.setVisibility(View.VISIBLE);
		tvContrastAmount.setText(userInfoEntity.getParagraphCount() + "幅大作");
		tvFollowerAmount.setText(userInfoEntity.getFollowerCount() + "人关注");
		tvScoreAmount.setText(userInfoEntity.getScore() + "积分");

		if (TextUtils.isEmpty(userInfoEntity.getUserSignature())) {
			tvSignature.setVisibility(View.GONE);
		} else {
			tvSignature.setVisibility(View.VISIBLE);
			tvSignature.setText(userInfoEntity.getUserSignature());
		}

		if (!TextUtils.isEmpty(userInfoEntity.getBirthday())) {
			llMoreUserInfo.setVisibility(View.VISIBLE);
			tvBorn.setText(CommonUtil.getShortFormatDateString(userInfoEntity
					.getBirthday()));
			tvGender.setText(Config.getGender(userInfoEntity.getUserSex()));
		}
		if(!TextUtils.isEmpty(userInfoEntity.getEmail())){
			llContact.setVisibility(View.VISIBLE);
			tvContact.setText(userInfoEntity.getEmail());
		}
		updateFollowState(userInfoEntity.getFollowBefore());

		llTopContainer.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						llTopContainer.getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
						crlContainer.setHeaderContainerHeight(llTopContainer
								.getHeight());
					}
				});
	}

	private void updateFollowState(int followState) {
		DD.d(TAG, "updateFollowState(), followState: " + followState);

		userInfoEntity.setFollowBefore(followState);
		if (userInfoEntity.getFollowBefore() == 1) {
			ivFollow.setImageResource(R.drawable.followed);
		} else {
			ivFollow.setImageResource(R.drawable.unfollowed);
		}
	}

	private void loadData() {
		DD.d(TAG, "loadData()");

		// 加载用户个人信息
		// loadUserInfoFromServer();
		// 刷新用户对比度列表
		// refreshContrastListFromServer();
		crlContainer.refreshWithoutAnim();
	}

	/**
	 * 从服务器加载用户个人信息
	 */
	private void loadUserInfoFromServer() {
		DD.d(TAG, "loadUserInfoFromServer()");

		RequestParams params = new RequestParams(Config.PATH_GET_INFO);
		params.setAsJsonContent(true);
		params.addParameter("accessToken",
				PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("userId", userId);
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

				hasLoadedUserInfo = true;

				userInfoEntity = JSON.parseObject(result.toString(),
						UserInfoEntity.class);
				setMoreView();

				// TODO: 2016/9/26 改过之后的
				// 先设置完 moreView，然后在加载contrast列表，否则有时会出现 时序导致的错误
				refreshContrastListFromServer();
			}
		});
	}

	/**
	 * 从服务器刷新用户对比度列表
	 */
	private void refreshContrastListFromServer() {
		DD.d(TAG, "refreshContrastListFromServer()");

		RequestParams params = new RequestParams(
				Config.PATH_GET_PARAGRAPH_BY_USER_ID);
		params.setAsJsonContent(true);
		params.addParameter("accessToken",
				PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("userId", userId);
		params.addParameter("from", 0);
		params.addParameter("row", 32);
		params.addParameter("timestamp",
				CommonUtil.getDefaultFormatCurrentTime());
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
				DD.d(TAG, "onSuccess(), result: " + result);

				paragraphResultEntity = JSON.parseObject(result.toString(),
						ParagraphResultEntity.class);
				String status = paragraphResultEntity.getStatus();
				List<ParagraphEntity> list = paragraphResultEntity
						.getParagraphs();
				if ("0".equals(status) && list != null && !list.isEmpty()) {
					listParagraph.clear();
					listParagraph.addAll(list);
				}
			}
		});
	}

	/**
	 * 从服务器加载更多用户对比度列表
	 */
	private void loadMoreContrastListFromServer() {
		DD.d(TAG, "loadMoreContrastListFromServer()");

		RequestParams params = new RequestParams(
				Config.PATH_GET_PARAGRAPH_BY_USER_ID);
		params.setAsJsonContent(true);
		params.addParameter("accessToken",
				PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("userId", userId);
		params.addParameter("from", listParagraph.size());
		params.addParameter("row", 16);
		params.addParameter("timestamp",
				CommonUtil.getDefaultFormatCurrentTime());
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
				DD.d(TAG, "onSuccess(), result: " + result);

				paragraphResultEntity = JSON.parseObject(result.toString(),
						ParagraphResultEntity.class);
				String status = paragraphResultEntity.getStatus();
				List<ParagraphEntity> list = paragraphResultEntity
						.getParagraphs();
				if ("0".equals(status) && list != null && !list.isEmpty()) {
					listParagraph.addAll(list);
				}
			}
		});
	}

	@Event(value = R.id.ll_top_container)
	private void onTopContainerClick(View v) {
		// do nothing, just for intercept click event
	}

	@Event(value = R.id.rl_back)
	private void onBackClick(View v) {
		DD.d(TAG, "onBackClick()");

		finish();
	}

	@Event(value = R.id.rl_follow)
	private void onFollowClick(View v) {
		DD.d(TAG, "onFollowClick()");

		if (!isLogined()) {
			gotoActivity(LoginTransitionActivity.class);
			finish();
			return;
		}

		if (userInfoEntity.getFollowBefore() == 1) {
			cancelFollow();
		} else {
			addFollow();
		}
	}

	/**
	 * 添加关注
	 */
	private void addFollow() {
		DD.d(TAG, "addFollow()");

		RequestParams params = new RequestParams(Config.PATH_ADD_FOLLOW);
		params.setAsJsonContent(true);
		params.addParameter("accessToken",
				PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("followUserId", userId);
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
				DD.d(TAG, "onSuccess(), result: " + result.toString());

				StatusResultEntity entity = JSON.parseObject(result.toString(),
						StatusResultEntity.class);
				if ("0".equals(entity.getStatus())) {
					updateFollowState(1);
					TT.show(MyFollowingUserParagraphActivity.this, "已关注");
				}
			}
		});
	}

	/**
	 * 取消关注
	 */
	private void cancelFollow() {
		DD.d(TAG, "cancelFollow()");

		RequestParams params = new RequestParams(Config.PATH_CANCEL_FOLLOW);
		params.setAsJsonContent(true);
		params.addParameter("accessToken",
				PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("cancelUserId", userId);
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
				DD.d(TAG, "onSuccess(), result: " + result.toString());

				StatusResultEntity entity = JSON.parseObject(result.toString(),
						StatusResultEntity.class);
				if ("0".equals(entity.getStatus())) {
					updateFollowState(0);
					TT.show(MyFollowingUserParagraphActivity.this, "停止关注");
				}
			}
		});
	}

	// 滚动监听、毛玻璃效果等
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**
	 * doBlur
	 */
	private void doBlur() {
		// DD.d(TAG, "doBlur()");

		crlContainer.getViewTreeObserver().addOnPreDrawListener(
				new OnPreDrawListener() {
					@Override
					public boolean onPreDraw() {
						// DD.d(TAG, "onPreDraw()");

						crlContainer.getViewTreeObserver()
								.removeOnPreDrawListener(this);
						crlContainer.buildDrawingCache();
						Bitmap bitmap = crlContainer.getDrawingCache();

						blur(bitmap, llTopContainer, llTopContainer.getTop(),
								false);
						return false;
					}
				});
	}

	/**
	 * blur
	 */
	private void blur(Bitmap bitmap, View view, int viewTop,
			boolean useNativeCode) {
		if (bitmap == null) {
			return;
		}

		int bitmapWidth = bitmap.getWidth();
		int bitmapHeight = bitmap.getHeight();
		int viewWidth = view.getMeasuredWidth();
		int viewHeight = view.getMeasuredHeight();
		float scaleFactor = 8;
		float radius = 20;

		// DD.d(TAG, "bitmapWidth: " + bitmapWidth + ", bitmapHeight: " +
		// bitmapHeight
		// + ", viewWidth: " + viewWidth + ", viewHeight: " + viewHeight +
		// ", viewTop: "
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
		if (llTopContainer.getVisibility() == View.VISIBLE) {
			return;
		}

		if (taShowTop == null) {
			taShowTop = new TranslateAnimation(0, 0,
					-llTopContainer.getHeight(), 0);
			taShowTop.setDuration(250);
			llTopContainer.setAnimation(taShowTop);
			taShowTop.setAnimationListener(new AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					llTopContainer.setVisibility(View.VISIBLE);
				}
			});
		}
		llTopContainer.startAnimation(taShowTop);
		
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

	/**
	 * 隐藏浮动视图
	 */
	private void hideFloatingView() {
		if (llTopContainer.getVisibility() == View.GONE) {
			return;
		}

		if (taHideTop == null) {
			taHideTop = new TranslateAnimation(0, 0, 0,
					-llTopContainer.getHeight());
			taHideTop.setDuration(250);
			llTopContainer.setAnimation(taHideTop);
			taHideTop.setAnimationListener(new AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					llTopContainer.setVisibility(View.GONE);
				}
			});
		}
		llTopContainer.startAnimation(taHideTop);
		
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

	// 滚动监听、毛玻璃效果等
	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	/**
	 * 点击大图
	 */
	private void onImageClick(int position) {
		DD.d(TAG, "onImageClick(), position: " + position);

		Intent intent = new Intent();
		intent.setClass(this, ContrastCommentActivity.class);
		intent.putExtra("paragraph", listParagraph.get(position));
		gotoActivity(intent);
	}

	/**
	 * 点击qq
	 */
	private void onQqClick(int position) {
		DD.d(TAG, "onQqClick(), position: " + position);

		new ShareAction(this).setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.SINA,
				SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE).open();
	}

	/**
	 * 点击weixin
	 */
	private void onWeixinClick(int position) {
		DD.d(TAG, "onWeixin(), position: " + position);

		new ShareAction(this).setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.SINA,
				SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE).open();
	}

	/**
	 * 点击weibo
	 */
	private void onWeiboClick(int position) {
		DD.d(TAG, "onWeiboClick(), position: " + position);

		new ShareAction(this).setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.SINA,
				SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE).open();
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

	// RefreshingAnimationView相关
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	public void showRefreshingAnimationView() {
		ravRefreshingView.setRefreshing(true);
	}

	public void hideRefreshingAnimationView() {
		ravRefreshingView.setRefreshing(false);
	}

	// RefreshingAnimationView相关
	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case MyParagraphActivity.REQUEST_CODE:
				List<ParagraphEntity> listResult = data.getParcelableArrayListExtra("list");
				if (listResult != null && !listResult.isEmpty()) {
					listParagraph.clear();
					listParagraph.addAll(listResult);
					adapter.notifyDataSetChanged();
				}
				break;
			}
		} else {
			try {
				UMShareAPI.get(this).onActivityResult(requestCode, resultCode,data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Event(value = R.id.gv_content, type = AdapterView.OnItemClickListener.class)
	private void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (listParagraph.size() > 0 && userInfoEntity != null) {
//			Intent intent = new Intent();
//			intent.setClass(MyFollowingUserParagraphActivity.this,UserParagraphListActivity.class);
			ParagraphEntity paragraphEntity = listParagraph.get(position);
//			intent.putExtra("userId", paragraphEntity.getUserId());
//			intent.putExtra("head", userInfoEntity.getAvatarName());
//			intent.putExtra("nickname", userInfoEntity.getNickname());
//			BaseActivity.sGotoActivity(MyFollowingUserParagraphActivity.this,intent);
			Intent intent = new Intent();
		    intent.setClass(MyFollowingUserParagraphActivity.this, UserParagraphActivity.class);
			intent.putExtra("userId", paragraphEntity.getUserId());
			intent.putExtra("head", userInfoEntity.getAvatarName());
			intent.putExtra("nickname", userInfoEntity.getNickname());
			intent.putExtra("position", position);
			intent.putParcelableArrayListExtra("list", (ArrayList<ParagraphEntity>) listParagraph);
			startActivityForResult(intent, UserParagraphActivity.REQUEST_CODE);
		}
	}
	
	@Event(value= R.id.iv_send)
	private void onSendPrivateMsg(View v){
		DD.d(TAG, "点击了发私信.........................................................");
		if(isLogined()) {
			Intent intent = new Intent();
			intent.setClass(this, SendPrivateMessagesActivity.class);
			intent.putExtra("userId", userId);
			intent.putExtra("head",head);
			intent.putExtra("nickname",nickname);
			startActivity(intent);
		} else {
			//跳转到登录页面
			gotoActivity(LoginTransitionActivity.class);
		}

	}

}