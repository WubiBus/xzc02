package cn.incorner.contrast.page;

import java.util.ArrayList;

import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.adapter.ContrastCommentListAdapter;
import cn.incorner.contrast.data.entity.CommentResultEntity;
import cn.incorner.contrast.data.entity.ParagraphCommentEntity;
import cn.incorner.contrast.data.entity.ParagraphEntity;
import cn.incorner.contrast.data.entity.StatusResultEntity;
import cn.incorner.contrast.util.CommonUtil;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.DensityUtil;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.view.CustomRefreshFramework;
import cn.incorner.contrast.view.CustomRefreshFramework.OnRefreshingListener;
import cn.incorner.contrast.view.CustomRefreshFramework.OnTouchMoveListener;
import cn.incorner.contrast.view.RefreshingAnimationView;
import cn.incorner.contrast.view.ScrollListenerListView;
import cn.incorner.contrast.view.SquareImageView;
import cn.incorner.contrast.view.SquareLinearLayout;

import com.alibaba.fastjson.JSON;

/**
 * 对比度评论 页面
 * 
 * @author yeshimin
 */
@ContentView(R.layout.activity_contrast_comment)
public class ContrastCommentActivity extends BaseActivity implements OnTouchMoveListener {

	private static final String TAG = "ContrastCommentActivity";

	// 缩小后图片的尺寸和margin，单位dp
	private static final int DOWN_SCALED_IMAGE_SIZE = 120;
	private static final int DOWN_SCALED_IMAGE_MARGIN = 40;

	@ViewInject(R.id.v_mask)
	private View vMask;
	@ViewInject(R.id.ll_image_container)
	private SquareLinearLayout llImageContainer;
	@ViewInject(R.id.rl_image_container)
	private RelativeLayout rlImageContainer;
	@ViewInject(R.id.ll_image_wrapper)
	private LinearLayout llImageWrapper;
	@ViewInject(R.id.iv_image_1)
	private ImageView ivImage1;
	@ViewInject(R.id.iv_image_2)
	private ImageView ivImage2;
	@ViewInject(R.id.iv_compatible_image)
	private SquareImageView ivCompatibleImage;
	@ViewInject(R.id.crl_container)
	private CustomRefreshFramework crlContainer;
	@ViewInject(R.id.lv_content)
	private ScrollListenerListView lvContent;
	@ViewInject(R.id.rl_back)
	private RelativeLayout rlBack;
	@ViewInject(R.id.rl_submit)
	private RelativeLayout rlSubmit;
	@ViewInject(R.id.et_input)
	private EditText etInput;
	// 发布动画
	@ViewInject(R.id.rav_refreshing_view)
	private RefreshingAnimationView ravRefreshingView;

	private ArrayList<ParagraphCommentEntity> listComment = new ArrayList<ParagraphCommentEntity>();
	private ContrastCommentListAdapter adapter;
	private String commentContent;
	private ParagraphEntity paragraphEntity;
	private boolean hasFocus = false;
	private String[] arrPicName;
	// 是否兼容模式
	private boolean isCompatibleMode = false;

	private ScaleAnimation downScaleAnimation;
	private ScaleAnimation upScaleAnimation;
	private View vHeader;
	private boolean hasInitCrlContainer = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		paragraphEntity = getIntent().getParcelableExtra("paragraph");
		hasFocus = getIntent().getBooleanExtra("hasFocus", false);
		if (paragraphEntity == null) {
			finish();
			return;
		}
		arrPicName = parsePicName(paragraphEntity.getPicName());
		if (arrPicName != null && arrPicName.length == 1) {
			isCompatibleMode = true;
		}

		init();
		loadData();
	}

	private String[] parsePicName(String picName) {
		DD.d(TAG, "parsePicName(), picName: " + picName);

		if (picName == null || TextUtils.isEmpty(picName)) {
			return null;
		}
		return picName.split(";");
	}

	private void init() {
		DD.d(TAG, "init()");

		// 初始化刷新框架
		vHeader = getLayoutInflater().inflate(R.layout.view_custom_refresh_framework_header, null);
		lvContent.addHeaderView(vHeader);
		crlContainer.setOnRefreshingListener(new OnRefreshingListener() {
			@Override
			public void onRefreshing() {
				refreshCommentFromServer();
			}

			@Override
			public void onLoadMore() {
				loadMoreCommentFromServer();
			}

			@Override
			public void onRefreshFinish() {
				adapter.notifyDataSetChanged();
			}
		});
		crlContainer.setOnTouchMoveListener(this);

		adapter = new ContrastCommentListAdapter(listComment, getLayoutInflater());
		lvContent.setAdapter(adapter);

		crlContainer.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (!hasInitCrlContainer) {
					hasInitCrlContainer = true;
					LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) crlContainer
							.getLayoutParams();
					params.topMargin = llImageContainer.getBottom() - vHeader.getHeight();
					crlContainer.setLayoutParams(params);
					if (hasFocus) {
						etInput.requestFocusFromTouch();
					}
				}
			}
		});
		etInput.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DD.d(TAG, "onInputClick()");

				downScaleImage();
			}
		});
		etInput.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				downScaleImage();
			}
		});
	}

	private void loadData() {
		DD.d(TAG, "loadData()");

		if (isCompatibleMode) {
			x.image().bind(ivCompatibleImage, Config.getContrastFullPath(arrPicName[0]));
		} else {
			String paragraphContent = paragraphEntity.getParagraphContent();
			if (!TextUtils.isEmpty(paragraphContent) && paragraphContent.contains("[up]")) {
				llImageWrapper.setOrientation(LinearLayout.VERTICAL);
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ivImage1
						.getLayoutParams();
				params.width = LinearLayout.LayoutParams.MATCH_PARENT;
				params.height = 0;
				ivImage1.setLayoutParams(params);
				ivImage2.setLayoutParams(params);
			} else {
				llImageWrapper.setOrientation(LinearLayout.HORIZONTAL);
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ivImage1
						.getLayoutParams();
				params.width = 0;
				params.height = LinearLayout.LayoutParams.MATCH_PARENT;
				ivImage1.setLayoutParams(params);
				ivImage2.setLayoutParams(params);
			}
			x.image().bind(ivImage1, Config.getContrastFullPath(arrPicName[0]));
			x.image().bind(ivImage2, Config.getContrastFullPath(arrPicName[1]));
		}
		crlContainer.refreshWithoutAnim();
	}

	/**
	 * 提交评论
	 */
	private void submitComment(final String commentContent) {
		DD.d(TAG, "submitComment(), commentContent: " + commentContent);

		RequestParams params = new RequestParams(Config.PATH_ADD_COMMENT);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("paragraphReplyId", paragraphEntity.getParagraphReplyId());
		params.addParameter("replyToUserId", paragraphEntity.getUserId());
		params.addParameter("replyContent", commentContent);
		params.addParameter("location", CommonUtil.getUserLocation());
		DD.d(TAG, "params: " + params.toJSONString());
		x.http().post(params, new CommonCallback<JSONObject>() {
			@Override
			public void onCancelled(CancelledException arg0) {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
			}

			@Override
			public void onFinished() {
				ravRefreshingView.setRefreshing(false);
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result);

				StatusResultEntity entity = JSON.parseObject(result.toString(),
						StatusResultEntity.class);
				if ("0".equals(entity.getStatus())) {
					ParagraphCommentEntity commentEntity = new ParagraphCommentEntity();
					commentEntity.setReplyUserisAnonymous(PrefUtil
							.getIntValue(Config.PREF_IS_ANONYMOUS));
					commentEntity.setReplyUserId(PrefUtil.getIntValue(Config.PREF_USER_ID));
					commentEntity.setReplyContent(commentContent);
					commentEntity.setParagraphReplyId(paragraphEntity.getParagraphReplyId());
					commentEntity.setReplyUserPic(commentEntity.getReplyUserisAnonymous() == 1 ? ""
							: PrefUtil.getStringValue(Config.PREF_AVATAR_NAME));
					commentEntity.setReplyUserNickname(PrefUtil
							.getStringValue(Config.PREF_NICKNAME));
					commentEntity.setCreateTime(CommonUtil.getDefaultFormatCurrentTime());
					commentEntity.setLocation(CommonUtil.getUserLocation());
					listComment.add(0, commentEntity);
					adapter.notifyDataSetChanged();
				}
			}
		});
	}

	/**
	 * 从服务器刷新评论数据
	 */
	private void refreshCommentFromServer() {
		DD.d(TAG, "refreshCommentFromServer()");

		RequestParams params = new RequestParams(Config.PATH_GET_COMMENTS_BY_PARAGRAPH_REPLY_IDS);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("paragraphReplyId", paragraphEntity.getParagraphReplyId());
		params.addParameter("from", 0);
		params.addParameter("rows", Config.LOAD_COUNT);
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

				CommentResultEntity entity = JSON.parseObject(result.toString(),
						CommentResultEntity.class);
				if ("0".equals(entity.getStatus()) && entity.getComments() != null
						&& !entity.getComments().isEmpty()) {
					listComment.clear();
					listComment.addAll(entity.getComments());
				}
			}
		});
	}

	/**
	 * 从服务器加载更多评论数据
	 */
	private void loadMoreCommentFromServer() {
		DD.d(TAG, "loadMoreCommentFromServer()");

		RequestParams params = new RequestParams(Config.PATH_GET_COMMENTS_BY_PARAGRAPH_REPLY_IDS);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("paragraphReplyId", paragraphEntity.getParagraphReplyId());
		params.addParameter("from", listComment.size());
		params.addParameter("rows", 20);
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

				CommentResultEntity entity = JSON.parseObject(result.toString(),
						CommentResultEntity.class);
				if ("0".equals(entity.getStatus()) && entity.getComments() != null
						&& !entity.getComments().isEmpty()) {
					listComment.addAll(entity.getComments());
				}
			}
		});
	}

	// 图片缩放相关 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**
	 * 缩小图片
	 */
	private void downScaleImage() {
		DD.d(TAG, "downScaleImage()");

		if (rlImageContainer.getAnimation() != null
				&& rlImageContainer.getAnimation() == downScaleAnimation) {
			return;
		}

		initDownScaleAnimation();
		rlImageContainer.startAnimation(downScaleAnimation);

		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) crlContainer
				.getLayoutParams();
		params.topMargin = vMask.getBottom() - vHeader.getHeight();
		crlContainer.setLayoutParams(params);
	}

	private void initDownScaleAnimation() {
		DD.d(TAG, "initDownScaleAnimation()");

		if (downScaleAnimation == null) {
			float scale = DensityUtil.dip2px(this, DOWN_SCALED_IMAGE_SIZE) * 1.0f
					/ rlImageContainer.getWidth();
			float pivotY = (DensityUtil.dip2px(this, DOWN_SCALED_IMAGE_MARGIN))
					* 1.0f
					/ (llImageContainer.getHeight() - DensityUtil.dip2px(this,
							DOWN_SCALED_IMAGE_SIZE));

			downScaleAnimation = new ScaleAnimation(1.0f, scale, 1.0f, scale,
					Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, pivotY);
			downScaleAnimation.setDuration(200);
			downScaleAnimation.setFillAfter(true);
			rlImageContainer.setAnimation(downScaleAnimation);
		}
	}

	/**
	 * 放大图片
	 */
	private void upScaleImage() {
		DD.d(TAG, "upScaleImage()");

		if (rlImageContainer.getAnimation() != null
				&& rlImageContainer.getAnimation() == upScaleAnimation) {
			return;
		} else if (rlImageContainer.getAnimation() == null) {
			return;
		}

		//隐藏输入法
		hideSoftInput(this, etInput);

		initUpScaleAnimation();
		rlImageContainer.startAnimation(upScaleAnimation);

		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) crlContainer
				.getLayoutParams();
		params.topMargin = llImageContainer.getBottom() - vHeader.getHeight();
		crlContainer.setLayoutParams(params);
	}

	/**
	 * 隐藏输入法
	 */
	private void hideSoftInput(Context context, View view) {
		InputMethodManager inputMethodManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	private void initUpScaleAnimation() {
		DD.d(TAG, "initUpScaleAnimation()");

		if (upScaleAnimation == null) {
			float scale = DensityUtil.dip2px(this, DOWN_SCALED_IMAGE_SIZE) * 1.0f
					/ rlImageContainer.getWidth();
			float pivotY = DensityUtil.dip2px(this, DOWN_SCALED_IMAGE_MARGIN)
					* 1.0f
					/ (llImageContainer.getHeight() - DensityUtil.dip2px(this,
							DOWN_SCALED_IMAGE_SIZE));

			upScaleAnimation = new ScaleAnimation(scale, 1.0f, scale, 1.0f,
					Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, pivotY);
			upScaleAnimation.setDuration(200);
			upScaleAnimation.setFillAfter(true);
			rlImageContainer.setAnimation(upScaleAnimation);
		}
	}

	// 图片缩放相关 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	@Event(value = R.id.rl_back)
	private void onBackClick(View v) {
		DD.d(TAG, "onBackClick()");
		finish();
	}

	@Event(value = R.id.v_mask)
	private void onMaskClick(View v) {
		DD.d(TAG, "onMaskClick()");
	}

	@Event(value = R.id.rl_submit)
	private void onSubmitClick(View v) {
		DD.d(TAG, "onSubmitClick()");

		// TODO: 2016/7/29
		//提交评论时去除空格及换行
		commentContent = etInput.getText().toString().replaceAll(" ","").replaceAll("\n","");
		if (TextUtils.isEmpty(commentContent)) {
			return;
		}

		etInput.setText("");
		// 隐藏软件盘
		hideSoftInput(this, etInput);
		// 执行发布动画
		ravRefreshingView.setRefreshing(true);
		submitComment(commentContent);
	}

	@Override
	public void onMoveUp() {
		DD.d(TAG, "onMoveUp()");

		downScaleImage();
	}

	@Override
	public void onMoveDown() {
		DD.d(TAG, "onMoveDown()");

		upScaleImage();
	}

	@Override
	public void onVerticalMoving() {
	}

}
