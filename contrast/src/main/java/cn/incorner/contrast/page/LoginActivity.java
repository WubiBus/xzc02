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
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.AnonymousRegEntity;
import cn.incorner.contrast.data.entity.GetAccessResultEntity;
import cn.incorner.contrast.data.entity.RegisterResultEntity;
import cn.incorner.contrast.data.entity.ThirdPartyRegOrLoginEntity;
import cn.incorner.contrast.util.CommonUtil;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.util.TT;

import com.alibaba.fastjson.JSON;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

/**
 * 登录 页面
 * 
 * @author yeshimin
 */
@ContentView(R.layout.activity_login)
public class LoginActivity extends BaseActivity implements TextWatcher {

	private static final String TAG = "LoginActivity";

	private static final Pattern patternPhone = Pattern.compile(Config.PATTERN_PHONE);
	private static final Pattern patternEmail = Pattern.compile(Config.PATTERN_EMAIL);

	// 登录区域
	@ViewInject(R.id.ll_login_container)
	private LinearLayout llLoginContainer;
	@ViewInject(R.id.et_username)
	private EditText etUsername;
	@ViewInject(R.id.et_password)
	private EditText etPassword;
	@ViewInject(R.id.btn_register)
	private Button btnRegister;
	@ViewInject(R.id.btn_login)
	private Button btnLogin;
	@ViewInject(R.id.btn_anonymous)
	private Button btnAnonymous;
	// 注册区域
	@ViewInject(R.id.iv_back_to_login)
	private ImageView ivBackToLogin;
	@ViewInject(R.id.btn_send_verify_code)
	private Button btnSendVerifyCode;
	@ViewInject(R.id.et_verify_code)
	private EditText etVerifyCode;
	@ViewInject(R.id.ll_register_container)
	private LinearLayout llRegisterContainer;
	@ViewInject(R.id.et_register_username)
	private EditText etRegisterUsername;
	@ViewInject(R.id.et_nickname)
	private EditText etNickname;
	@ViewInject(R.id.et_register_password)
	private EditText etRegisterPassword;
	@ViewInject(R.id.et_confirm_password)
	private EditText etConfirmPassword;
	@ViewInject(R.id.et_invite_code)
	private EditText etInviteCode;
	@ViewInject(R.id.iv_ok)
	private ImageView ivOk;
	// 第三方
	@ViewInject(R.id.iv_weixin)
	private ImageView ivWeixin;
	@ViewInject(R.id.iv_qq)
	private ImageView ivQq;
	@ViewInject(R.id.iv_weibo)
	private ImageView ivWeibo;

	private String username;
	private String password;
	private String registerUsername;
	private String registerPassword;
	private String confirmPassword;
	private String nickname;
	private String randomVerifyCode;
	private String userVerifyCode;
	private boolean waitingForCode = false;
	// 是否是手机号
	private boolean isPhoneNum = false;

	private CountDownTimer timer;
	private ProgressDialog pdDialog;

	private UMShareAPI umShareAPI;
	private UMAuthListener umAuthListener;
	private UMAuthListener umAuthListener2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	private void init() {
		DD.d(TAG, "init()");

		initProgressDialog();
		umShareAPI = UMShareAPI.get(this);
		umAuthListener = new MyUMAuthListener();
		umAuthListener2 = new MyUMAuthListener2();

		try {
			// 偶尔会空指针，无解
			etRegisterUsername.addTextChangedListener(this);
		} catch (Exception e) {
			finish();
		}
		// 随机生成一个验证码（该验证流程是有问题的，临时方案）
		createRandomCodeAndInitTimer();
	}

	/**
	 * 初始化进度框
	 */
	private void initProgressDialog() {
		DD.d(TAG, "initProgressDialog()");

		pdDialog = new ProgressDialog(this);
		pdDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pdDialog.setCanceledOnTouchOutside(false);
	}

	/**
	 * 显示验证码相关视图
	 */
	private void showVerifyCodeViews() {
		DD.d(TAG, "showVerifyCodeViews()");

		btnSendVerifyCode.setVisibility(View.VISIBLE);
		etVerifyCode.setVisibility(View.VISIBLE);
	}

	/**
	 * 隐藏短信验证码相关视图
	 */
	private void hideVerifyCodeViews() {
		DD.d(TAG, "hideVerifyCodeViews()");

		btnSendVerifyCode.setVisibility(View.GONE);
		etVerifyCode.setVisibility(View.GONE);
	}

	/**
	 * 随机生成一个验证码并初始化计时器
	 */
	private void createRandomCodeAndInitTimer() {
		DD.d(TAG, "createRandomCodeAndInitTimer()");

		randomVerifyCode = String.valueOf(new Random().nextInt(10000));
		timer = new CountDownTimer(60000, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				btnSendVerifyCode.setText((millisUntilFinished / 1000) + "s");
			}

			@Override
			public void onFinish() {
				btnSendVerifyCode.setText("重新发送");
				waitingForCode = false;
			}
		};
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
				TT.show(LoginActivity.this, "网络错误");
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
					gotoMainActivity();
				} else {
					TT.show(LoginActivity.this, "登录失败，请检查用户名和密码");
				}
			}
		});
	}

	public void gotoMainActivity() {
		DD.d(TAG, "gotoMainActivity()");

		gotoActivity(MainActivity.class);
		finish();
	}

	/**
	 * 匿名注册
	 */
	private void anonymousReg() {
		DD.d(TAG, "anonymousReg()");

		pdDialog.setMessage("正在登录...");
		pdDialog.show();

		RequestParams params = new RequestParams(Config.PATH_ANONYMOUS_REG);
		params.setAsJsonContent(true);
		params.setBodyContent("{}");
		x.http().post(params, new CommonCallback<JSONObject>() {
			@Override
			public void onCancelled(CancelledException arg0) {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
				DD.d(TAG, "onError()");
				TT.show(LoginActivity.this, "网络错误");
			}

			@Override
			public void onFinished() {
				DD.d(TAG, "onFinished()");
				pdDialog.dismiss();
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result.toString());

				AnonymousRegEntity entity = JSON.parseObject(result.toString(),
						AnonymousRegEntity.class);
				if ("0".equals(entity.getStatus())) {
					PrefUtil.setStringValue(Config.PREF_ACCESS_TOKEN, entity.getAccessToken());
					PrefUtil.setIntValue(Config.PREF_USER_ID, entity.getUserId());
					PrefUtil.setIntValue(Config.PREF_IS_ANONYMOUS, 1);
					gotoActivity(MainActivity.class);
					finish();
				} else {
					TT.show(LoginActivity.this, "网络错误");
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
					// TT.show(LoginActivity.this, "success");

					// btnSendVerifyCode.setVisibility(View.GONE);
					// ivOk.setVisibility(View.GONE);
					// llRegisterContainer.setVisibility(View.GONE);
					// llLoginContainer.setVisibility(View.VISIBLE);

					PrefUtil.setBooleanValue(Config.PREF_IS_LOGINED, true);
					PrefUtil.setStringValue(Config.PREF_ACCESS_TOKEN, entity.getAccessToken());
					PrefUtil.setIntValue(Config.PREF_USER_ID, entity.getUserId());
					gotoMainActivity();
				} else if ("502".equals(entity.getStatus())) {
					TT.show(LoginActivity.this, "手机号/邮箱和名字都重复了，太巧了吧！");
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		DD.d(TAG, "onDestroy()");

		timer.cancel();
		super.onDestroy();
	}

	/**
	 * 切换到注册视图
	 */
	@Event(value = R.id.btn_register)
	private void onRegisterClick(View v) {
		DD.d(TAG, "onRegisterClick()");

		llLoginContainer.setVisibility(View.GONE);
		llRegisterContainer.setVisibility(View.VISIBLE);

		// 恢复视图
		username = etRegisterUsername.getText().toString();
		if (username.length() == 11) {
			Matcher matcher = patternPhone.matcher(username);
			if (matcher.find()) {
				showVerifyCodeViews();
			}
		}
	}

	/**
	 * 返回到登录视图
	 */
	@Event(value = R.id.iv_back_to_login)
	private void onBackToLoginClick(View v) {
		DD.d(TAG, "onBackToLoginClick()");

		llRegisterContainer.setVisibility(View.GONE);
		llLoginContainer.setVisibility(View.VISIBLE);
		hideVerifyCodeViews();
	}

	@Event(value = R.id.btn_login)
	private void onLoginClick(View v) {
		DD.d(TAG, "onLoginClick()");

		username = etUsername.getText().toString();
		password = etPassword.getText().toString();
		// 检查数据
		if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
			TT.show(this, "请核对您的手机号或邮箱拼写");
			return;
		}

		login();
	}

	@Event(value = R.id.btn_anonymous)
	private void onAnonymousClick(View v) {
		DD.d(TAG, "onAnonymousClick()");

		anonymousReg();
	}

	@Event(value = R.id.btn_send_verify_code)
	private void onSendVerifyCodeClick(View v) {
		DD.d(TAG, "onSendVerifyCodeClick()");

		if (waitingForCode) {
			return;
		}

		RequestParams params = new RequestParams(Config.PATH_CHECK_PHONE_NUMBER);
		params.setAsJsonContent(true);
		DD.d(TAG, "username: " + username);
		DD.d(TAG, "randomVerifyCode: " + randomVerifyCode);
		params.addParameter("phoneNum", username);
		params.addParameter("code", randomVerifyCode);
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

				// StatusResultEntity entity = JSON.parseObject(result.toString(),
				// StatusResultEntity.class);
				if ("0".equals(result.optString("state"))) {
					waitingForCode = true;
					timer.start();
				} else if ("502".equals(result.optString("status"))) {
					TT.show(LoginActivity.this, "该号码已注册");
				}
			}
		});
	}

	@Event(value = R.id.iv_ok)
	private void onOkClick(View v) {
		DD.d(TAG, "onOkClick()");

		registerUsername = etRegisterUsername.getText().toString();
		registerPassword = etRegisterPassword.getText().toString();
		confirmPassword = etConfirmPassword.getText().toString();
		nickname = etNickname.getText().toString();
		userVerifyCode = etVerifyCode.getText().toString();

		// 检查数据
		if (TextUtils.isEmpty(registerUsername) || TextUtils.isEmpty(nickname)
				|| TextUtils.isEmpty(registerPassword) || TextUtils.isEmpty(confirmPassword)) {
			TT.show(this, "手机号或邮箱不能为空！");
			return;
		}
		// 检查用户名
		Matcher matcherPhone = patternPhone.matcher(registerUsername);
		Matcher matcherEmail = patternEmail.matcher(registerUsername);
		if (!matcherPhone.find() && !matcherEmail.find()) {
			TT.show(this, "用户名不符合规则！");
			return;
		}
		if (!registerPassword.equals(confirmPassword)) {
			TT.show(this, "两次密码不一致");
			return;
		}
		if (isPhoneNum && !randomVerifyCode.equals(userVerifyCode)) {
			TT.show(this, "验证码错误");
			return;
		}
		register();
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
	 * 使用weixin注册登录
	 */
	private void thirdPartyRegAndLoginByWeixin(final Map<String, String> data) {
		DD.d(TAG, "thirdPartyRegAndLoginByWeixin()");

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
			}

			@Override
			public void onCancelled(CancelledException arg0) {
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

		String uid = data.get("openid");
		String nickname = data.get("screen_name");
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
					thirdPartyLoginByWeixin(data);
				}
			}
		});
	}

	private void thirdPartyLoginByWeixin(final Map<String, String> data) {
		DD.d(TAG, "thirdPartyLoginByWeixin()");

		final String uid = data.get("openid");
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
	 * 使用qq注册登录
	 */
	private void thirdPartyRegAndLoginByQq(final Map<String, String> data) {
		DD.d(TAG, "thirdPartyRegAndLoginByQq()");

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
			}

			@Override
			public void onCancelled(CancelledException arg0) {
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
	 * 授权监听
	 */
	private class MyUMAuthListener implements UMAuthListener {
		@Override
		public void onCancel(SHARE_MEDIA platform, int action) {
		}

		@Override
		public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
			DD.d(TAG, "auth.onComplete()");
			umShareAPI.getPlatformInfo(LoginActivity.this, platform, umAuthListener2);
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

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		DD.d(TAG, "onTextChanged(), s: " + s.toString());

		if (s.length() == 11) {
			Matcher matcher = patternPhone.matcher(s);
			if (matcher.find()) {
				username = etRegisterUsername.getText().toString();
				isPhoneNum = true;
				showVerifyCodeViews();
			}
		} else {
			isPhoneNum = false;
			hideVerifyCodeViews();
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

}
