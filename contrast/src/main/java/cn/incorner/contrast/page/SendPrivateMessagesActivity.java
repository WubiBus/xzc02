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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.incorner.contrast.BaseFragmentActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.blur.Blur;
import cn.incorner.contrast.blur.FastBlur;
import cn.incorner.contrast.data.adapter.SendPrivateMessagesAdapter;
import cn.incorner.contrast.data.entity.PrivateMessageEntity;
import cn.incorner.contrast.data.entity.PrivateMessageResultEntity;
import cn.incorner.contrast.data.entity.StatusResultEntity;
import cn.incorner.contrast.data.entity.UserInfoEntity;
import cn.incorner.contrast.util.CommonUtil;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.util.TT;
import cn.incorner.contrast.util.ViewUtil;
import cn.incorner.contrast.view.CircleImageView;
import cn.incorner.contrast.view.CustomRefreshFramework;
import cn.incorner.contrast.view.CustomRefreshFramework.OnRefreshingListener;
import cn.incorner.contrast.view.CustomRefreshFramework.OnTouchMoveListener;
import cn.incorner.contrast.view.RefreshingAnimationView;
import cn.incorner.contrast.view.RefreshingAnimationView.IRefreshingAnimationView;
import cn.incorner.contrast.view.ScrollListenerListView;

import com.alibaba.fastjson.JSON;
import com.umeng.socialize.UMShareAPI;

@ContentView(R.layout.activity_sendprivatemessages)
public class SendPrivateMessagesActivity extends BaseFragmentActivity implements
		OnTouchMoveListener, IRefreshingAnimationView {

	private static final String TAG = "UserParagraphListActivity";
	private SendPrivateMessagesActivity mActivity;
	@ViewInject(R.id.ll_top_container)
	private LinearLayout llTopContainer;
	@ViewInject(R.id.rl_back)
	private RelativeLayout rlBack;
	@ViewInject(R.id.civ_head)
	private CircleImageView civHead;
	@ViewInject(R.id.tv_nickname)
	private TextView tvNickname;
	@ViewInject(R.id.crl_container)
	private CustomRefreshFramework crlContainer;
	@ViewInject(R.id.sv_content)
	private ScrollListenerListView svContent;
	@ViewInject(R.id.et_send)
	private EditText etSend;
	@ViewInject(R.id.rl_send)
	private RelativeLayout rlSend;

	// 刷新视图
	@ViewInject(R.id.rav_refreshing_view)
	private RefreshingAnimationView ravRefreshingView;

	private ArrayList<PrivateMessageEntity> privateMessageEntities = new ArrayList<PrivateMessageEntity>();
	private PrivateMessageResultEntity privateMessageResultEntity;
	private SendPrivateMessagesAdapter adapter;

	private int userId;
	private String head;
	private String nickname;

	private Blur blur;
	
	public Handler handlerEdit = new Handler();
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = this;
		userId = getIntent().getIntExtra("userId", -1);
		head = getIntent().getStringExtra("head");
		nickname = getIntent().getStringExtra("nickname");

		init();
		loadData();
	}

	private void init() {
		DD.d(TAG, "init()");
		//这个属性会让listview上移,根据需求选择注释或者不注释
		//svContent.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		blur = new Blur(this);

		// 初始化刷新框架
		View vHeader = getLayoutInflater().inflate(
				R.layout.view_custom_refresh_framework_header, null);
		svContent.addHeaderView(vHeader);
		crlContainer.setOnRefreshingListener(new OnRefreshingListener() {
			@Override
			public void onRefreshing() {
				refreshUserPrivateFromServer();
			}

			@Override
			public void onLoadMore() {
				loadMoreUerPrivateFromServer();
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
						getWindow().getDecorView().getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
						crlContainer.setHeaderContainerHeight(llTopContainer
								.getHeight());
					}
				});

		adapter = new SendPrivateMessagesAdapter(privateMessageEntities,getLayoutInflater());
		svContent.setAdapter(adapter);
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
							Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
							civHead.setImageBitmap(bitmap);
						}
					});
		}else if (TextUtils.isEmpty(head) && userId == 65130){
			Drawable drawable = getResources().getDrawable(R.drawable.icon_yuanjiao);
			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
			civHead.setImageBitmap(bitmap);
			tvNickname.setText(nickname);
			tvNickname.setVisibility(View.VISIBLE);
		}
		
		handlerEdit.postDelayed(new Runnable() {
			@Override
			public void run() {
				etSend.requestFocus();
				etSend.setFocusable(true);
				etSend.setFocusableInTouchMode(true);
				ViewUtil.openKeyBoard(mActivity, etSend);
			}
		}, 500);
	}

	private void loadData() {
		DD.d(TAG, "loadData()");

		// 加载用户个人信息
		// loadUserInfoFromServer();
		// 刷新用户对比度列表
		// refreshContrastListFromServer();
		refreshUserPrivateFromServer();
		crlContainer.refreshWithoutAnim();
	}


	/**
	 * 从服务器刷新用户私信列表
	 */
	private void refreshUserPrivateFromServer() {
		DD.d(TAG, "refreshUserPrivateFromServer()");
		RequestParams params = new RequestParams(Config.PATH_GET_USER_PRIVATE);
		params.setAsJsonContent(true);
		params.addParameter("accessToken",PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("userId", userId);
		params.addParameter("from", 0);
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
				DD.d(TAG, "onSuccess(), result: " + result);
				privateMessageResultEntity = JSON.parseObject(result.toString(),PrivateMessageResultEntity.class);
				int status = privateMessageResultEntity.getStatus();
				PrefUtil.setStringValue("headToUser", privateMessageResultEntity.getAvatarName());
				PrefUtil.setStringValue("nicknameToUser", privateMessageResultEntity.getNickname());
				List<PrivateMessageEntity> list = privateMessageResultEntity.getMessageList();
				if (status==0 && list != null && !list.isEmpty()) {
					privateMessageEntities.clear();
					privateMessageEntities.addAll(list);
				}
				adapter.notifyDataSetChanged();
			}
		});
	}

	/**
	 * 从服务器加载更多用户对比度列表
	 */
	private void loadMoreUerPrivateFromServer() {
		DD.d(TAG, "loadMoreUerPrivateFromServer()");
		RequestParams params = new RequestParams(Config.PATH_GET_USER_PRIVATE);
		params.setAsJsonContent(true);
		params.addParameter("accessToken",PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("userId", userId);
		params.addParameter("from", privateMessageEntities.size());
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
				DD.d(TAG, "onSuccess(), result: " + result);
				privateMessageResultEntity = JSON.parseObject(result.toString(),PrivateMessageResultEntity.class);
				int status = privateMessageResultEntity.getStatus();
				List<PrivateMessageEntity> list = privateMessageResultEntity.getMessageList();
				if (status==0 && list != null && !list.isEmpty()) {
					privateMessageEntities.addAll(list);
				}
				adapter.notifyDataSetChanged();
			}
		});
	}
	
	/**
	 * 向服务器发送用户私信
	 */
	private void postUserPrivateToServer() {
		DD.d(TAG, "postUserPrivateToServer()");
		RequestParams params = new RequestParams(Config.PATH_ADD_USER_PRIVATE);
		params.setAsJsonContent(true);
		params.addParameter("accessToken",PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("paragraphReplyId", "");
		params.addParameter("replyContent", etSend.getText().toString().trim());
		params.addParameter("location",CommonUtil.getUserLocation());
		params.addParameter("replyToUserId", userId);
		params.addParameter("atUserIds", "");
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
				StatusResultEntity entity = JSON.parseObject(result.toString(), StatusResultEntity.class);
				if ("0".equals(entity.getStatus())) {
					//成功，刷新数据
					etSend.setText("");
					refreshUserPrivateFromServer();
				}else{
					//失败提示
					TT.show(mActivity, "发送失败");
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

	
	@Event(value = R.id.rl_send)
	private void onSend(View v){
		if(TextUtils.isEmpty(etSend.getText().toString().trim())) {
			TT.show(mActivity, "请输入私信内容");
			return;	
		}else{
		    postUserPrivateToServer();
		}
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


	@Override
	public void onVerticalMoving() {
	}

	@Override
	public void onMoveUp() {
	}

	@Override
	public void onMoveDown() {
	}

	// 滚动监听、毛玻璃效果等
	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	// RefreshingAnimationView相关
	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			UMShareAPI.get(this)
					.onActivityResult(requestCode, resultCode, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Event(value = R.id.sv_content, type = AdapterView.OnItemClickListener.class)
	private void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
//		if (newsInfoEntities.size() > 0 && userInfoEntity != null) {
//		}
	}

	@Override
	public void showRefreshingAnimationView() {
		ravRefreshingView.setRefreshing(true);
	}

	@Override
	public void hideRefreshingAnimationView() {
		ravRefreshingView.setRefreshing(false);
	}

}