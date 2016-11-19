package cn.incorner.contrast.page;

import java.io.File;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback.CacheCallback;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.GetAccessResultEntity;
import cn.incorner.contrast.data.entity.RegisterResultEntity;
import cn.incorner.contrast.data.entity.ThirdPartyRegOrLoginEntity;
import cn.incorner.contrast.util.AnimationUtil;
import cn.incorner.contrast.util.CommonUtil;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.DataCacheUtil;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.util.TT;
import cn.incorner.contrast.util.ViewUtil;
import com.alibaba.fastjson.JSON;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import cn.incorner.contrast.page.ForgetPwdActivity;

/**
 * 登录页面
 * 
 * @author horace
 */
@ContentView(R.layout.activity_login2)
public class LoginActivity2 extends BaseActivity {

	private static final String TAG = "LoginActivity";

	private static final Pattern patternPhone = Pattern.compile(Config.PATTERN_PHONE);
	private static final Pattern patternEmail = Pattern.compile(Config.PATTERN_EMAIL);

	private LoginActivity2 mActivity;

	// 登录区域
	@ViewInject(R.id.ll_anonymous_login)
	private LinearLayout llAnonymousLogin;
	@ViewInject(R.id.ll_login_username)
	private LinearLayout llLoginUsername;
	@ViewInject(R.id.ll_login_password)
	private LinearLayout llLoginPassword;
	@ViewInject(R.id.et_username)
	private EditText etUsername;
	@ViewInject(R.id.et_password)
	private EditText etPassword;
	@ViewInject(R.id.rl_main_right)
	private RelativeLayout rlMainRight;
	@ViewInject(R.id.rl_main_left)
	private RelativeLayout rlMainLeft;
	// 注册区域
	@ViewInject(R.id.ll_register_username)
	private LinearLayout llRegisterUsername;
	@ViewInject(R.id.ll_register_password)
	private LinearLayout llRegisterPassword;
	@ViewInject(R.id.et_register_username)
	private EditText etRegisterUsername;
	@ViewInject(R.id.et_nickname)
	private EditText etRegisterNickname;
	@ViewInject(R.id.et_register_password)
	private EditText etRegisterPassword;
	@ViewInject(R.id.et_confirm_password)
	private EditText etConfirmPassword;
	@ViewInject(R.id.ll_send_verify_code)
	private LinearLayout llSendVerifyCode;
	@ViewInject(R.id.btn_send_verify_code)
	private Button btnSendVerifyCode;
	@ViewInject(R.id.et_verify_code)
	private EditText etVerifyCode;

	// 第三方
	@ViewInject(R.id.ll_thirdparty)
	private LinearLayout llThirdparty;
	@ViewInject(R.id.ll_bottom)
	private LinearLayout llBottom;
	@ViewInject(R.id.iv_weixin)
	private ImageView ivWeixin;
	@ViewInject(R.id.iv_qq)
	private ImageView ivQq;
	@ViewInject(R.id.iv_weibo)
	private ImageView ivWeibo;
	
	//登录注册按钮
	@ViewInject(R.id.btn_login)
	private Button btnLogin;
	@ViewInject(R.id.btn_register)
	private Button btnRegister;
	
	//忘记密码
	@ViewInject(R.id.tv_forgetpwd)
	private TextView tvForgetpwd;

	private ProgressDialog pdDialog;
	private String username;
	private String password;
	private String registerUsername;
	private String registerPassword;
	private String confirmPassword;
	private String nickname;

	private UMShareAPI umShareAPI;
	private UMAuthListener umAuthListener;
	private UMAuthListener umAuthListener2;

	public Handler handlerEdit = new Handler();

	private String randomVerifyCode;

	private CountDownTimer timer;
	
	private int stateValue =0;
	
	private boolean isFirst = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = this;
		Intent intent = getIntent();
		if (intent != null) {
		    stateValue = intent.getIntExtra(LoginTransitionActivity.LOGIN_ACTION, 0);
			if (stateValue == LoginTransitionActivity.REGISTER_CODE) {
				registerClick();
			} else if (stateValue == LoginTransitionActivity.LOGIN_CODE) {
				loginClick();
			}
		}
		init();

		etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					loginAction();
				}
				return false;
			}

		});

		etConfirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					registerAction();
				}
				return false;
			}

		});
	}

	private void init() {
		DD.d(TAG, "init()");
		tvForgetpwd.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG );//初始化就把忘记密码的下划线画上
		ScaleAnimation scaleAnimation = (ScaleAnimation) AnimationUtil.getScaleLoginInAnimation(2500);
		scaleAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationEnd(Animation animation) {}
		});
		rlMainLeft.startAnimation(AnimationUtil.getScaleLoginInAnimation2(1500));
		rlMainRight.startAnimation(AnimationUtil.getScaleLoginInAnimation2(2500));

		initProgressDialog();
		umShareAPI = UMShareAPI.get(this);
		umAuthListener = new MyUMAuthListener();
		umAuthListener2 = new MyUMAuthListener2();

		etRegisterUsername.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() == 11) {
					// TODO判断是否符合手机号，符合显示
					// 检查用户名
					Matcher matcherPhone = patternPhone.matcher(s);
					if (!matcherPhone.find()) {
						return;
					}
					llSendVerifyCode.setVisibility(View.VISIBLE);
					btnSendVerifyCode.setVisibility(View.VISIBLE);
					etVerifyCode.setVisibility(View.VISIBLE);
				} else {// TODO 直接隐藏
					llSendVerifyCode.setVisibility(View.GONE);
					btnSendVerifyCode.setVisibility(View.GONE);
					etVerifyCode.setVisibility(View.GONE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		createRandomCodeAndInitTimer();
	}

	/**
	 * 初始化进度框
	 */
	private void initProgressDialog() {
		DD.d(TAG, "initProgressDialog()");

		pdDialog = new ProgressDialog(mActivity);
		pdDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pdDialog.setCanceledOnTouchOutside(false);
	}

	/**
	 * 点击验证码按钮
	 */
	@Event(value = R.id.btn_send_verify_code)
	private void onSendVeriyCode(View v) {
		randomVerifyCode = null;
		randomVerifyCode = String.valueOf(new Random().nextInt(10000));
		sendVerifyCode();
		timer.start();
		btnSendVerifyCode.setClickable(false);
	}

	@Event(value = R.id.ll_anonymous_login)
	private void onAnonymousLoginClick(View v) {
		DD.d(TAG, "onAnonymousLoginClick()");
		finish();
	}

	@Event(value = R.id.iv_weixin)
	private void onWechatClick(View v) {
		DD.d(TAG, "onWechatClick()");

		umShareAPI.doOauthVerify(this, SHARE_MEDIA.WEIXIN, umAuthListener);
	}

	@Event(value = R.id.iv_qq)
	private void onQqClick(View v) {
		DD.d(TAG, "onQqClick()");

		umShareAPI.doOauthVerify(this, SHARE_MEDIA.QQ, umAuthListener);
	}

	@Event(value = R.id.iv_weibo)
	private void onWeiboClick(View v) {
		DD.d(TAG, "onWeiboClick()");

		umShareAPI.doOauthVerify(this, SHARE_MEDIA.SINA, umAuthListener);
	}
	
	@Event(value = R.id.tv_forgetpwd)
	private void onForgetpwdClick(View v){
		DD.d(TAG, "onForgetPwdCLick()");
		gotoActivity(ForgetPwdActivity.class);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
			try {
				UMShareAPI.get(this).onActivityResult(requestCode, resultCode,data);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	/**
	 * 登录
	 */
	private void login() {
		DD.d(TAG, "login()");

		pdDialog.setMessage("正在登录...");
		pdDialog.show();

		RequestParams params = new RequestParams(Config.PATH_GET_ACCESS);
		params.setAsJsonContent(true);
		params.addParameter("loginName", username);
		params.addParameter("password", CommonUtil.md5(password));
		x.http().post(params, new CommonCallback<JSONObject>() {
			@Override
			public void onCancelled(CancelledException arg0) {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
				DD.d(TAG, "onError(), e: " + arg0.getMessage());
				TT.show(LoginActivity2.this, "网络错误");
			}

			@Override
			public void onFinished() {
				DD.d(TAG, "onFinished()");
				pdDialog.dismiss();
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result);

				GetAccessResultEntity entity = JSON.parseObject(result.toString(),
						GetAccessResultEntity.class);
				if ("0".equals(entity.getStatus())) {
					PrefUtil.setBooleanValue(Config.PREF_IS_LOGINED, true);
					PrefUtil.setStringValue(Config.PREF_ACCESS_TOKEN, entity.getAccessToken());
					PrefUtil.setIntValue(Config.PREF_USER_ID, entity.getUserId());
					PrefUtil.setIntValue(Config.PREF_IS_ANONYMOUS, 0);
					if (isFirst) {
						gotoActivity(RecomendActivity.class);
					} else {
						gotoActivity(MainActivity.class);
					}
					setResult(101);
					finish();
				} else {
					TT.show(LoginActivity2.this, "登录失败，请检查用户名和密码");
				}
			}
		});
	}


	public void gotoMainActivity() {
		DD.d(TAG, "gotoMainActivity()");
//		gotoActivity(MainActivity.class);
		gotoActivity(RecomendActivity.class);
		setResult(101);
		finish();
	}

	/**
	 * 授权监听
	 */
	private class MyUMAuthListener implements UMAuthListener {
		@Override
		public void onCancel(SHARE_MEDIA platform, int action) {
		}

		@Override
		public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
			DD.d(TAG, "auth.onComplete()");
			umShareAPI.getPlatformInfo(LoginActivity2.this, platform, umAuthListener2);
		}

		@Override
		public void onError(SHARE_MEDIA platform, int action, Throwable arg2) {
		}
	}

	/**
	 * 获取用户信息监听
	 */
	private class MyUMAuthListener2 implements UMAuthListener {
		@Override
		public void onCancel(SHARE_MEDIA platform, int action) {
		}

		@Override
		public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
			DD.d(TAG, "getinfo.onComplete()");
			if (data != null) {
				DD.d(TAG, "data: " + data.toString());
				DataCacheUtil.putStringData(getExternalCacheDir().getAbsolutePath(),
						platform.toString(), data.toString());
				if (platform == SHARE_MEDIA.WEIXIN) {
					thirdPartyRegAndLoginByWeixin(data);
				} else if (platform == SHARE_MEDIA.QQ) {
					thirdPartyRegAndLoginByQq(data);
				} else if (platform == SHARE_MEDIA.SINA) {
					thirdPartyRegAndLoginByWeibo(data);
				}
			}
		}

		@Override
		public void onError(SHARE_MEDIA platform, int action, Throwable arg2) {
		}
	}

	/**
	 * 使用weixin注册登录
	 */
	private void thirdPartyRegAndLoginByWeixin(final Map<String, String> data) {
		DD.d(TAG, "thirdPartyRegAndLoginByWeixin()");

		pdDialog.setMessage("正在登录");
		pdDialog.show();

		// 先获取头像文件，用户注册
		x.image().loadFile(data.get("headimgurl"), null, new CacheCallback<File>() {
			@Override
			public void onSuccess(File file) {
				DD.d(TAG, "onSuccess(), file.path: " + file.getAbsolutePath());
				thirdPartyRegByWeixin(data, file);
			}

			@Override
			public void onFinished() {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
				pdDialog.dismiss();
			}

			@Override
			public void onCancelled(CancelledException arg0) {
				pdDialog.dismiss();
			}

			@Override
			public boolean onCache(File file) {
				DD.d(TAG, "onCache(), file.path: " + file.getAbsolutePath());
				thirdPartyRegByWeixin(data, file);
				return false;
			}
		});
	}

	private void thirdPartyRegByWeixin(final Map<String, String> data, File file) {
		DD.d(TAG, "thirdPartyRegByWeixin()");

		String uid = data.get("unionid");
		String nickname = data.get("nickname");
		final String userAvatar = UUID.randomUUID().toString();
		int type = Config.THIRD_PARTY_TYPE_WEIXIN;
		String idfa = "";
		String accessToken = PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN);

		RequestParams params = new RequestParams(Config.PATH_THIRD_PARTY_REGISTER);
		params.setMultipart(true);
		params.addParameter("uid", uid);
		params.addParameter("nickname", nickname);
		params.addParameter("userAvatar", userAvatar);
		params.addParameter("type", type);
		params.addParameter("idfa", idfa);
		params.addParameter("userAvatar", file);
		if (!TextUtils.isEmpty(accessToken)) {
			params.addParameter("accessToken", accessToken);
		}
		DD.d(TAG, "uid: " + uid);
		DD.d(TAG, "nickname: " + nickname);
		DD.d(TAG, "userAvatar: " + userAvatar);
		DD.d(TAG, "type: " + type);
		DD.d(TAG, "idfa: " + idfa);
		DD.d(TAG, "accessToken: " + accessToken);
		x.http().post(params, new CommonCallback<JSONObject>() {
			@Override
			public void onCancelled(CancelledException arg0) {
				pdDialog.dismiss();
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
				pdDialog.dismiss();
			}

			@Override
			public void onFinished() {
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result.toString());

				ThirdPartyRegOrLoginEntity entity = JSON.parseObject(result.toString(),
						ThirdPartyRegOrLoginEntity.class);
				if ("0".equals(entity.getStatus()) || "502".equals(entity.getStatus())) {
					// 保存头像
					PrefUtil.setStringValue(Config.PREF_AVATAR_NAME, userAvatar);
					thirdPartyLoginByWeixin(data);
				}
			}
		});
	}

	private void thirdPartyLoginByWeixin(final Map<String, String> data) {
		DD.d(TAG, "thirdPartyLoginByWeixin()");

		final String uid = data.get("unionid");
		final int type = Config.THIRD_PARTY_TYPE_WEIXIN;

		RequestParams params = new RequestParams(Config.PATH_THIRD_PARTY_LOGIN);
		params.setAsJsonContent(true);
		params.addParameter("uid", uid);
		params.addParameter("type", type);
		x.http().post(params, new CommonCallback<JSONObject>() {
			@Override
			public void onCancelled(CancelledException arg0) {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
			}

			@Override
			public void onFinished() {
				pdDialog.dismiss();
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result.toString());

				ThirdPartyRegOrLoginEntity entity = JSON.parseObject(result.toString(),
						ThirdPartyRegOrLoginEntity.class);
				if ("0".equals(entity.getStatus())) {
					PrefUtil.setStringValue(Config.PREF_THIRD_PARTY_UID, uid);
					PrefUtil.setIntValue(Config.PREF_THIRD_PARTY_TYPE, type);
					PrefUtil.setBooleanValue(Config.PREF_IS_LOGINED, true);
					PrefUtil.setStringValue(Config.PREF_ACCESS_TOKEN, entity.getAccessToken());
					PrefUtil.setIntValue(Config.PREF_USER_ID, entity.getUserId());
					// PrefUtil.setIntValue(Config.PREF_USER_SEX,
					// Config.getGenderCode(data.get("gender")));
					PrefUtil.setStringValue(Config.PREF_USERNAME, data.get("nickname"));
					gotoMainActivity();
				}
			}
		});
	}

	/**
	 * 使用qq注册登录
	 */
	private void thirdPartyRegAndLoginByQq(final Map<String, String> data) {
		DD.d(TAG, "thirdPartyRegAndLoginByQq()");

		pdDialog.setMessage("正在登录");
		pdDialog.show();

		// 先获取头像文件，用户注册
		x.image().loadFile(data.get("profile_image_url"), null, new CacheCallback<File>() {
			@Override
			public void onSuccess(File file) {
				DD.d(TAG, "onSuccess(), file.path: " + file.getAbsolutePath());
				thirdPartyRegByQq(data, file);
			}

			@Override
			public void onFinished() {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
				pdDialog.dismiss();
			}

			@Override
			public void onCancelled(CancelledException arg0) {
				pdDialog.dismiss();
			}

			@Override
			public boolean onCache(File file) {
				DD.d(TAG, "onCache(), file.path: " + file.getAbsolutePath());
				thirdPartyRegByQq(data, file);
				return false;
			}
		});
	}

	private void thirdPartyRegByQq(final Map<String, String> data, File file) {
		DD.d(TAG, "thirdPartyRegQq()");

		String uid = data.get("openid");
		String nickname = data.get("screen_name");
		final String userAvatar = UUID.randomUUID().toString();
		int type = Config.THIRD_PARTY_TYPE_QQ;
		String idfa = "";
		String accessToken = PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN);

		RequestParams params = new RequestParams(Config.PATH_THIRD_PARTY_REGISTER);
		params.setMultipart(true);
		params.addParameter("uid", uid);
		params.addParameter("nickname", nickname);
		params.addParameter("userAvatar", userAvatar);
		params.addParameter("type", type);
		params.addParameter("idfa", idfa);
		params.addParameter("userAvatar", file);
		if (!TextUtils.isEmpty(accessToken)) {
			params.addParameter("accessToken", accessToken);
		}
		DD.d(TAG, "uid: " + uid);
		DD.d(TAG, "nickname: " + nickname);
		DD.d(TAG, "userAvatar: " + userAvatar);
		DD.d(TAG, "type: " + type);
		DD.d(TAG, "idfa: " + idfa);
		DD.d(TAG, "accessToken: " + accessToken);
		x.http().post(params, new CommonCallback<JSONObject>() {
			@Override
			public void onCancelled(CancelledException arg0) {
				pdDialog.dismiss();
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
				pdDialog.dismiss();
			}

			@Override
			public void onFinished() {
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result.toString());

				ThirdPartyRegOrLoginEntity entity = JSON.parseObject(result.toString(),
						ThirdPartyRegOrLoginEntity.class);
				if ("0".equals(entity.getStatus()) || "502".equals(entity.getStatus())) {
					// 保存头像
					PrefUtil.setStringValue(Config.PREF_AVATAR_NAME, userAvatar);
					thirdPartyLoginByQq(data);
				}
			}
		});
	}

	private void thirdPartyLoginByQq(final Map<String, String> data) {
		DD.d(TAG, "thirdPartyLoginByQq()");

		final String uid = data.get("openid");
		final int type = Config.THIRD_PARTY_TYPE_QQ;

		RequestParams params = new RequestParams(Config.PATH_THIRD_PARTY_LOGIN);
		params.setAsJsonContent(true);
		params.addParameter("uid", uid);
		params.addParameter("type", type);
		x.http().post(params, new CommonCallback<JSONObject>() {
			@Override
			public void onCancelled(CancelledException arg0) {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
			}

			@Override
			public void onFinished() {
				pdDialog.dismiss();
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result.toString());

				ThirdPartyRegOrLoginEntity entity = JSON.parseObject(result.toString(),
						ThirdPartyRegOrLoginEntity.class);
				if ("0".equals(entity.getStatus())) {
					PrefUtil.setStringValue(Config.PREF_THIRD_PARTY_UID, uid);
					PrefUtil.setIntValue(Config.PREF_THIRD_PARTY_TYPE, type);
					PrefUtil.setBooleanValue(Config.PREF_IS_LOGINED, true);
					PrefUtil.setStringValue(Config.PREF_ACCESS_TOKEN, entity.getAccessToken());
					PrefUtil.setIntValue(Config.PREF_USER_ID, entity.getUserId());
					PrefUtil.setIntValue(Config.PREF_USER_SEX,
							Config.getGenderCode(data.get("gender")));
					PrefUtil.setStringValue(Config.PREF_USERNAME, data.get("screen_name"));
					gotoMainActivity();
				}
			}
		});
	}

	/**
	 * 使用weibo注册登录
	 */
	private void thirdPartyRegAndLoginByWeibo(final Map<String, String> data) {
		DD.d(TAG, "thirdPartyRegAndLoginByWeibo()");

		// 先获取头像文件，用户注册所需
		x.image().loadFile(data.get("profile_image_url"), null, new CacheCallback<File>() {
			@Override
			public void onSuccess(File file) {
				DD.d(TAG, "onSuccess(), file.path: " + file.getAbsolutePath());
				thirdPartyRegByWeibo(data, file);
			}

			@Override
			public void onFinished() {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
			}

			@Override
			public void onCancelled(CancelledException arg0) {
			}

			@Override
			public boolean onCache(File file) {
				DD.d(TAG, "onCache(), file.path: " + file.getAbsolutePath());
				thirdPartyRegByWeibo(data, file);
				return false;
			}
		});

		String uid = data.get("openid");
		String nickname = data.get("screen_name");
		String userAvatar = UUID.randomUUID().toString();
		int type = Config.THIRD_PARTY_TYPE_QQ;
		String idfa = "";
		String accessToken = PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN);

		RequestParams params = new RequestParams(Config.PATH_THIRD_PARTY_REGISTER);
		params.setAsJsonContent(true);
		params.addParameter("uid", uid);
		params.addParameter("nickname", nickname);
		params.addParameter("userAvatar", userAvatar);
		params.addParameter("type", type);
		params.addParameter("idfa", idfa);
		if (!TextUtils.isEmpty(accessToken)) {
			params.addParameter("accessToken", accessToken);
		}
		DD.d(TAG, "uid: " + uid);
		DD.d(TAG, "nickname: " + nickname);
		DD.d(TAG, "userAvatar: " + userAvatar);
		DD.d(TAG, "type: " + type);
		DD.d(TAG, "idfa: " + idfa);
		DD.d(TAG, "accessToken: " + accessToken);
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

				ThirdPartyRegOrLoginEntity entity = JSON.parseObject(result.toString(),
						ThirdPartyRegOrLoginEntity.class);
				if ("0".equals(entity.getStatus()) || "502".equals(entity.getStatus())) {
					thirdPartyLoginByQq(data);
				}
			}
		});
	}

	private void thirdPartyRegByWeibo(final Map<String, String> data, File file) {
		DD.d(TAG, "thirdPartyRegByWeibo()");

		String uid = data.get("uid");
		String nickname = data.get("screen_name");
		final String userAvatar = UUID.randomUUID().toString();
		int type = Config.THIRD_PARTY_TYPE_WEIBO;
		String idfa = "";
		String accessToken = PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN);

		RequestParams params = new RequestParams(Config.PATH_THIRD_PARTY_REGISTER);
		params.setMultipart(true);
		params.addParameter("uid", uid);
		params.addParameter("nickname", nickname);
		params.addParameter("userAvatar", userAvatar);
		params.addParameter("type", type);
		params.addParameter("idfa", idfa);
		params.addParameter("userAvatar", file);
		if (!TextUtils.isEmpty(accessToken)) {
			params.addParameter("accessToken", accessToken);
		}
		DD.d(TAG, "uid: " + uid);
		DD.d(TAG, "nickname: " + nickname);
		DD.d(TAG, "userAvatar: " + userAvatar);
		DD.d(TAG, "type: " + type);
		DD.d(TAG, "idfa: " + idfa);
		DD.d(TAG, "accessToken: " + accessToken);
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

				ThirdPartyRegOrLoginEntity entity = JSON.parseObject(result.toString(),
						ThirdPartyRegOrLoginEntity.class);
				if ("0".equals(entity.getStatus()) || "502".equals(entity.getStatus())) {
					// 保存头像
					PrefUtil.setStringValue(Config.PREF_AVATAR_NAME, userAvatar);
					thirdPartyLoginByWeibo(data);
				}
			}
		});
	}

	private void thirdPartyLoginByWeibo(final Map<String, String> data) {
		DD.d(TAG, "thirdPartyLoginByWeibo()");

		final String uid = data.get("uid");
		final int type = Config.THIRD_PARTY_TYPE_WEIBO;

		RequestParams params = new RequestParams(Config.PATH_THIRD_PARTY_LOGIN);
		params.setAsJsonContent(true);
		params.addParameter("uid", uid);
		params.addParameter("type", type);
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

				ThirdPartyRegOrLoginEntity entity = JSON.parseObject(result.toString(),
						ThirdPartyRegOrLoginEntity.class);
				if ("0".equals(entity.getStatus())) {
					PrefUtil.setStringValue(Config.PREF_THIRD_PARTY_UID, uid);
					PrefUtil.setIntValue(Config.PREF_THIRD_PARTY_TYPE, type);
					PrefUtil.setBooleanValue(Config.PREF_IS_LOGINED, true);
					PrefUtil.setStringValue(Config.PREF_ACCESS_TOKEN, entity.getAccessToken());
					PrefUtil.setIntValue(Config.PREF_USER_ID, entity.getUserId());
					PrefUtil.setStringValue(Config.PREF_USERNAME, data.get("screen_name"));
					int gender = 0;
					try {
						gender = Integer.parseInt(data.get("gender"));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					PrefUtil.setIntValue(Config.PREF_USER_SEX, gender);
					gotoMainActivity();
				}
			}
		});
	}

	/**
	 * 注册
	 */
	private void register() {
		DD.d(TAG, "register()");
		pdDialog.setMessage("正在注册...");
		pdDialog.show();

		RequestParams params = new RequestParams(Config.PATH_REGISTER);
		params.setAsJsonContent(true);
		params.addParameter("loginName", registerUsername);
		params.addParameter("password", CommonUtil.md5(registerPassword));
		params.addParameter("nickname", nickname);
		String accessToken = PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN);
		if (!TextUtils.isEmpty(accessToken)) {
			params.addParameter("accessToken", accessToken);
		}
		x.http().post(params, new CommonCallback<JSONObject>() {
			@Override
			public void onCancelled(CancelledException arg0) {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
			}

			@Override
			public void onFinished() {
				DD.d(TAG, "onFinished()");
				pdDialog.dismiss();
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result);

				RegisterResultEntity entity = JSON.parseObject(result.toString(),
						RegisterResultEntity.class);
				if ("0".equals(entity.getStatus())) {
//					PrefUtil.setBooleanValue(Config.PREF_IS_LOGINED, true);
//					PrefUtil.setStringValue(Config.PREF_ACCESS_TOKEN, entity.getAccessToken());
//					PrefUtil.setIntValue(Config.PREF_USER_ID, entity.getUserId());
//					gotoMainActivity();
					isFirst = true;
					etUsername.setText(etRegisterNickname.getText().toString());
					llRegisterUsername.setVisibility(View.GONE);
					llRegisterPassword.setVisibility(View.GONE);
					llSendVerifyCode.setVisibility(View.GONE);
					etVerifyCode.setVisibility(View.GONE);
					llLoginUsername.setVisibility(View.VISIBLE);
					llLoginPassword.setVisibility(View.VISIBLE);
					etRegisterUsername.setText("");
					etRegisterPassword.setText("");
					etConfirmPassword.setText("");
					etRegisterNickname.setText("");
					llBottom.setVisibility(View.VISIBLE);
					btnLogin.setVisibility(View.VISIBLE);
					btnRegister.setVisibility(View.GONE);
					llThirdparty.setVisibility(View.VISIBLE);
					tvForgetpwd.setVisibility(View.VISIBLE);
					TT.show(mActivity, "注册成功，请登录");
					handlerEdit.postDelayed(new Runnable() {
						@Override
						public void run() {
							etPassword.requestFocus();
							etPassword.setFocusable(true);
							etPassword.setFocusableInTouchMode(true);
							ViewUtil.openKeyBoard(mActivity, etPassword);
						}
					}, 500);
				} else if ("502".equals(entity.getStatus())) {
					TT.show(LoginActivity2.this, "手机号/邮箱和名字都重复了，太巧了吧！");
				}
			}
		});
	}

	private void loginAction() {
		username = etUsername.getText().toString();
		password = etPassword.getText().toString();
		// 检查数据
		if (TextUtils.isEmpty(username)) {
			TT.show(mActivity, "请输入您的手机号或邮箱或大名");
			return;
		}
		if (TextUtils.isEmpty(password)) {
			TT.show(mActivity, "请输入密码");
			return;
		}
		if (!TextUtils.isEmpty(password)) {
			if (password.length() < 6) {
				TT.show(mActivity, "密码长度不足6位");
				return;
			}
		}
		login();
	}

	private void registerAction() {
		registerUsername = etRegisterUsername.getText().toString();
		registerPassword = etRegisterPassword.getText().toString();
		confirmPassword = etConfirmPassword.getText().toString();
		nickname = etRegisterNickname.getText().toString();
		// 检查数据
		if (TextUtils.isEmpty(registerUsername)) {
			TT.show(mActivity, "请输入手机号或邮箱");
			return;
		}
		if (TextUtils.isEmpty(nickname)) {
			TT.show(mActivity, "请输入您的大名");
			return;
		}
		if (TextUtils.isEmpty(registerPassword)) {
			TT.show(mActivity, "请输入设置密码");
			return;
		}
		if (TextUtils.isEmpty(confirmPassword)) {
			TT.show(mActivity, "请再次输入设置密码");
			return;
		}
		if (registerPassword.length() < 6 || confirmPassword.length() < 6) {
			TT.show(mActivity, "密码长度不足6位");
		}
		if (etVerifyCode.getVisibility() == View.VISIBLE) {
			if (!randomVerifyCode.equals(etVerifyCode.getText().toString().trim())) {
				TT.show(mActivity, "验证码不正确");
				return;
			}
		}
		// 检查用户名
		Matcher matcherPhone = patternPhone.matcher(registerUsername);
		Matcher matcherEmail = patternEmail.matcher(registerUsername);
		if (!matcherPhone.find() && !matcherEmail.find()) {
			TT.show(mActivity, "请核对您的手机号或邮箱拼写！");
			return;
		}
		if (!registerPassword.equals(confirmPassword)) {
			TT.show(mActivity, "两次密码不一致");
			return;
		}
		register();
	}

	/**
	 * 发送验证码
	 */
	private void sendVerifyCode() {
		DD.d(TAG, "sendVerifyCode()");
		RequestParams params = new RequestParams(Config.PATH_GET_SENDCODE);
		params.setAsJsonContent(true);
		params.addParameter("phoneNum", etRegisterUsername.getText().toString().trim());
		params.addParameter("code", randomVerifyCode);
		x.http().post(params, new CommonCallback<JSONObject>() {
			@Override
			public void onCancelled(CancelledException arg0) {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
				DD.d(TAG, "onError(), e: " + arg0.getMessage());
				TT.show(LoginActivity2.this, "网络错误");
			}

			@Override
			public void onFinished() {
				DD.d(TAG, "onFinished()");
				pdDialog.dismiss();
			}

			@Override
			public void onSuccess(JSONObject result) {
			}
		});
	}

	/**
	 * 随机生成一个验证码并初始化计时器
	 */
	private void createRandomCodeAndInitTimer() {
		DD.d(TAG, "createRandomCodeAndInitTimer()");
		timer = new CountDownTimer(60000, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				btnSendVerifyCode.setText((millisUntilFinished / 1000) + "s后重新发送");
			}

			@Override
			public void onFinish() {
				btnSendVerifyCode.setText("重新发送");
				btnSendVerifyCode.setClickable(true);
			}
		};
	}

	@Override
	protected void onDestroy() {
		DD.d(TAG, "onDestroy()");

		timer.cancel();
		super.onDestroy();
	}
	
	@Event(value = R.id.btn_login)
    private void onLoginClick(View v){
		DD.d(TAG, "onLoginClick()");
		loginAction();
	}

	@Event(value = R.id.btn_register)
	private void onRegisterClick(View v){
		DD.d(TAG, "onRegisterClick()");
		registerAction();
	}
	
	private void loginClick(){
		DD.d(TAG, "loginClick()");
		llBottom.setVisibility(View.VISIBLE);
		btnLogin.setVisibility(View.VISIBLE);
		btnRegister.setVisibility(View.GONE);
		llThirdparty.setVisibility(View.VISIBLE);
		tvForgetpwd.setVisibility(View.VISIBLE);
		if (!etUsername.getText().toString().isEmpty()
				|| !etPassword.getText().toString().isEmpty()) {
			loginAction();
		} else {
			llRegisterUsername.setVisibility(View.GONE);
			llRegisterPassword.setVisibility(View.GONE);
			llSendVerifyCode.setVisibility(View.GONE);
			etVerifyCode.setVisibility(View.GONE);
			llLoginUsername.setVisibility(View.VISIBLE);
			llLoginPassword.setVisibility(View.VISIBLE);
			etRegisterUsername.setText("");
			etRegisterPassword.setText("");
			etConfirmPassword.setText("");
			etRegisterNickname.setText("");
			TT.show(mActivity, "请输入您的手机号或邮箱或大名");
			handlerEdit.postDelayed(new Runnable() {
				@Override
				public void run() {
					etUsername.requestFocus();
					etUsername.setFocusable(true);
					etUsername.setFocusableInTouchMode(true);
					ViewUtil.openKeyBoard(mActivity, etUsername);
				}
			}, 500);
		}
	
	}
	
	private void registerClick(){
		llBottom.setVisibility(View.VISIBLE);
		btnLogin.setVisibility(View.GONE);
		btnRegister.setVisibility(View.VISIBLE);
		llThirdparty.setVisibility(View.INVISIBLE);
		tvForgetpwd.setVisibility(View.INVISIBLE);
		DD.d(TAG, "registerClick()");
		if (!etRegisterUsername.getText().toString().isEmpty()
				|| !etRegisterNickname.getText().toString().isEmpty()
				|| !etRegisterPassword.getText().toString().isEmpty()
				|| !etConfirmPassword.getText().toString().isEmpty()) {
			registerAction();
		} else {
			llRegisterUsername.setVisibility(View.VISIBLE);
			llRegisterPassword.setVisibility(View.VISIBLE);
			llSendVerifyCode.setVisibility(View.GONE);
			etVerifyCode.setVisibility(View.GONE);
			llLoginUsername.setVisibility(View.GONE);
			llLoginPassword.setVisibility(View.GONE);
			etUsername.setText("");
			etPassword.setText("");
			TT.show(mActivity, "请输入您的手机号或邮箱");
			handlerEdit.postDelayed(new Runnable() {
				@Override
				public void run() {
					etRegisterUsername.requestFocus();
					etRegisterUsername.setFocusable(true);
					etRegisterUsername.setFocusableInTouchMode(true);
					ViewUtil.openKeyBoard(mActivity, etRegisterUsername);
				}
			}, 500);
		}
	}
}
