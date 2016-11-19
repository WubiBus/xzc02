package cn.incorner.contrast.page;

import java.util.Random;

import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.RegisterResultEntity;
import cn.incorner.contrast.util.CommonUtil;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.util.TT;

import com.alibaba.fastjson.JSON;

/**
 * 注册 页面
 * 
 * @author yeshimin
 */
@ContentView(R.layout.activity_register)
public class RegisterActivity extends BaseActivity implements TextWatcher {

	private static final String TAG = "RegisterActivity";

	@ViewInject(R.id.rl_send_verify_code)
	private RelativeLayout rlSendVerifyCode;
	@ViewInject(R.id.tv_send_verify_code)
	private TextView tvSendVerifyCode;
	@ViewInject(R.id.et_username)
	private EditText etUsername;
	@ViewInject(R.id.et_verify_code)
	private EditText etVerifyCode;
	@ViewInject(R.id.et_nickname)
	private EditText etNickname;
	@ViewInject(R.id.et_password)
	private EditText etPassword;
	@ViewInject(R.id.et_confirm_password)
	private EditText etConfirmPassword;
	@ViewInject(R.id.et_invite_code)
	private EditText etInviteCode;
	@ViewInject(R.id.tv_user_protocol)
	private TextView tvUserProtocol;
	@ViewInject(R.id.ll_ok_container)
	private LinearLayout llOkContainer;
	@ViewInject(R.id.iv_ok)
	private ImageView ivOk;

	private String username;
	private String nickname;
	private String password;
	private String randomVerifyCode;
	private String userVerifyCode;
	private boolean waitingForCode = false;

	private CountDownTimer timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	private void init() {
		DD.d(TAG, "init()");

		hideVerifyCodeViews();
		onHideOkView();
		// 设置键盘监听
		setOnSoftKeyBoardVisibleChangeListener();
		etUsername.addTextChangedListener(this);
		// 随机生成一个验证码（该验证流程是有问题的，临时方案）
		createRandomCodeAndInitTimer();
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
				tvSendVerifyCode.setText((millisUntilFinished / 1000) + "s");
			}

			@Override
			public void onFinish() {
				tvSendVerifyCode.setText("send");
				waitingForCode = false;
			}
		};
	}

	/**
	 * 设置键盘 显示/隐藏 监听（键盘非全屏模式也算隐藏）
	 */
	private boolean lastVisible = false;

	private void setOnSoftKeyBoardVisibleChangeListener() {
		final View decorView = getWindow().getDecorView();
		decorView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						Rect rect = new Rect();
						decorView.getWindowVisibleDisplayFrame(rect);
						int displayHight = rect.bottom - rect.top;
						int hight = decorView.getHeight();
						boolean visible = (double) displayHight / hight < 0.8;

						if (visible != lastVisible) {
							onSoftKeyBoardVisibleChanged(visible);
						}
						lastVisible = visible;

					}
				});
	}

	/**
	 * 键盘显示状态改变回调
	 */
	private void onSoftKeyBoardVisibleChanged(boolean visible) {
		Log.d(TAG, "onSoftKeyBoardVisibleChanged(), visible: " + visible);

		if (visible) {
			onShowOkView();
		} else {
			onHideOkView();
		}
	}

	/**
	 * 显示验证码相关视图
	 */
	private void showVerifyCodeViews() {
		DD.d(TAG, "showVerifyCodeViews()");

		rlSendVerifyCode.setVisibility(View.VISIBLE);
		etVerifyCode.setVisibility(View.VISIBLE);
	}

	/**
	 * 隐藏短信验证码相关视图
	 */
	private void hideVerifyCodeViews() {
		DD.d(TAG, "hideVerifyCodeViews()");

		rlSendVerifyCode.setVisibility(View.GONE);
		etVerifyCode.setVisibility(View.GONE);
	}

	/**
	 * 显示确定视图，隐藏协议视图
	 */
	private void onShowOkView() {
		DD.d(TAG, "onShowOkView()");

		llOkContainer.setVisibility(View.VISIBLE);
		tvUserProtocol.setVisibility(View.GONE);
	}

	/**
	 * 隐藏确定视图，显示协议视图
	 */
	private void onHideOkView() {
		DD.d(TAG, "onHideOkView()");

		llOkContainer.setVisibility(View.GONE);
		tvUserProtocol.setVisibility(View.VISIBLE);
	}

	/**
	 * 注册
	 */
	private void register() {
		DD.d(TAG, "register()");

		RequestParams params = new RequestParams(Config.PATH_REGISTER);
		params.setAsJsonContent(true);
		params.addParameter("loginName", username);
		params.addParameter("password", CommonUtil.md5(password));
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
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result);

				RegisterResultEntity entity = JSON.parseObject(result.toString(),
						RegisterResultEntity.class);
				if ("0".equals(entity.getStatus())) {
					TT.show(RegisterActivity.this, "success");
					finish();
				} else {
					TT.show(RegisterActivity.this, "fail");
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

	@Event(value = R.id.rl_send_verify_code)
	private void onSendVerifyCodeClick(View v) {
		DD.d(TAG, "onSendVerifyCodeClick()");

		if (waitingForCode) {
			return;
		}

		RequestParams params = new RequestParams(Config.PATH_CHECK_PHONE_NUMBER);
		params.setAsJsonContent(true);
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
				}
			}
		});
	}

	@Event(value = R.id.iv_ok)
	private void onOkClick(View v) {
		DD.d(TAG, "onOkClick()");

		username = etUsername.getText().toString();
		nickname = etNickname.getText().toString();
		password = etPassword.getText().toString();
		userVerifyCode = etVerifyCode.getText().toString();
		// TODO check
//		if (!randomVerifyCode.equals(userVerifyCode)) {
//			TT.show(this, "验证码错误");
//			return;
//		}
		register();
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		DD.d(TAG, "onTextChanged(), s: " + s.toString());

		// TODO check phone number format
		if (s.length() == 11) {
			username = etUsername.getText().toString();
			showVerifyCodeViews();
		} else {
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
