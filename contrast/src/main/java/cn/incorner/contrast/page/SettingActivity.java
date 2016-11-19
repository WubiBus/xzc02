package cn.incorner.contrast.page;

import java.util.Calendar;

import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.StatusResultEntity;
import cn.incorner.contrast.util.CommonUtil;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.util.TT;
import cn.incorner.contrast.view.BottomPopupWindow;

import com.alibaba.fastjson.JSON;

/**
 * 设置 页面
 * 
 * @author yeshimin
 */
@ContentView(R.layout.activity_setting)
public class SettingActivity extends BaseActivity {

	private static final String TAG = "SettingActivity";

	@ViewInject(R.id.rl_back)
	private RelativeLayout rlBack;
	@ViewInject(R.id.rl_save)
	private RelativeLayout rlSave;
	@ViewInject(R.id.et_nickname)
	private EditText etNickname;
	@ViewInject(R.id.et_job_title)
	private EditText etJobTitle;
	@ViewInject(R.id.et_signature)
	private EditText etSignature;
	@ViewInject(R.id.tv_born)
	private TextView tvBorn;
	@ViewInject(R.id.tv_gender)
	private TextView tvGender;
	@ViewInject(R.id.et_contact)
	private EditText etContact;
	@ViewInject(R.id.btn_update_password)
	private Button btnUpdatePassword;
	@ViewInject(R.id.ll_password_container)
	private LinearLayout llPasswordContainer;
	@ViewInject(R.id.et_old_password)
	private EditText etOldPassword;
	@ViewInject(R.id.et_new_password)
	private EditText etNewPassword;
	@ViewInject(R.id.et_confirm_password)
	private EditText etConfirmPassowrd;
	@ViewInject(R.id.btn_goto_update_password)
	private Button btnGotoUpdatePassword;

	// 选择头像
	private BottomPopupWindow bpwGenderSelector;

	private String nickname;
	private String userSignature;
	private String birthday;
	private String jobTitle;
	private int userSex;
	private String email;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		nickname = PrefUtil.getStringValue(Config.PREF_NICKNAME);
		userSignature = PrefUtil.getStringValue(Config.PREF_USER_SIGNATURE);
		birthday = PrefUtil.getStringValue(Config.PREF_BIRTHDAY);
		jobTitle = PrefUtil.getStringValue(Config.PREF_JOB_TITLE);
		userSex = PrefUtil.getIntValue(Config.PREF_USER_SEX);
		email = PrefUtil.getStringValue(Config.PREF_EMAIL);

		initGenderSelector();
		setView();
	}

	/**
	 * 初始化性别选择面板
	 */
	private void initGenderSelector() {
		DD.d(TAG, "initGenderSelector()");

		View vContent = getLayoutInflater().inflate(R.layout.view_gender_selector, null);
		bpwGenderSelector = new BottomPopupWindow(this, findViewById(R.id.rl_root), vContent,
				R.style.style_share_board_window);

		// 设置点击监听
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.rl_male:
					userSex = 1;
					tvGender.setText(Config.getGender(userSex));
					break;
				case R.id.rl_female:
					userSex = 0;
					tvGender.setText(Config.getGender(userSex));
					break;
				case R.id.rl_cancel:
					// 下面有dismiss操作
					break;
				}
				bpwGenderSelector.dismiss();
			}
		};

		vContent.findViewById(R.id.rl_male).setOnClickListener(listener);
		vContent.findViewById(R.id.rl_female).setOnClickListener(listener);
		vContent.findViewById(R.id.rl_cancel).setOnClickListener(listener);
	}

	private void setView() {
		DD.d(TAG, "setView()");

		etNickname.setText(nickname);
		etJobTitle.setText(jobTitle);
		etSignature.setText(userSignature);
		tvBorn.setText(CommonUtil.getShortFormatDateString(birthday));
		tvGender.setText(Config.getGender(userSex));
		etContact.setText(email);
	}

	/**
	 * 保存数据
	 */
	private void save() {
		DD.d(TAG, "save()");

		RequestParams params = new RequestParams(Config.PATH_SET_INFO);
		params.setAsJsonContent(true);
		DD.d(TAG, "accessToken: " + PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		DD.d(TAG, "nickname: " + nickname);
		DD.d(TAG, "userSignature: " + userSignature);
		DD.d(TAG, "birthday: " + birthday);
		DD.d(TAG, "userSex: " + userSex);
		DD.d(TAG, "email: " + email);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("nickname", nickname);
		params.addParameter("userSignature", userSignature);
		params.addParameter("birthday", birthday);
		params.addParameter("jobTitle", jobTitle);
		params.addParameter("userSex", userSex);
		params.addParameter("email", email);
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

				StatusResultEntity entity = JSON.parseObject(result.toString(),
						StatusResultEntity.class);
				if ("0".equals(entity.getStatus())) {
					PrefUtil.setStringValue(Config.PREF_USERNAME, nickname);
					PrefUtil.setStringValue(Config.PREF_USER_SIGNATURE, userSignature);
					PrefUtil.setStringValue(Config.PREF_BIRTHDAY, birthday);
					PrefUtil.setStringValue(Config.PREF_JOB_TITLE, jobTitle);
					PrefUtil.setIntValue(Config.PREF_USER_SEX, userSex);
					PrefUtil.setStringValue(Config.PREF_EMAIL, email);

					TT.show(SettingActivity.this, "update success");
				}
			}
		});
	}

	/**
	 * 更新密码
	 */
	private void updatePassword(String oldPassword, String newPassword) {
		DD.d(TAG, "updatePassword(), oldPassword: " + oldPassword + ", newPassword: " + newPassword);

		RequestParams params = new RequestParams(Config.PATH_SET_PASSWORD);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("password", CommonUtil.md5(oldPassword));
		params.addParameter("newPassword", CommonUtil.md5(newPassword));
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
					TT.show(SettingActivity.this, "修改密码成功");
					finish();
				}
			}
		});
	}

	/**
	 * 更新出生日期
	 */
	private void updateBornDate(String bornDate) {
		DD.d(TAG, "updateBornDate(), bornDate: " + bornDate);

		birthday = bornDate + " 00:00:00";
		tvBorn.setText(bornDate);
	}

	@Event(value = R.id.rl_back)
	private void onBackClick(View v) {
		DD.d(TAG, "onBackClick()");

		finish();
	}

	@Event(value = R.id.rl_save)
	private void onSaveClick(View v) {
		DD.d(TAG, "onSaveClick()");

		// TODO check
		nickname = etNickname.getText().toString();
		jobTitle = etJobTitle.getText().toString();
		userSignature = etSignature.getText().toString();
		email = etContact.getText().toString();

		save();
	}

	@Event(value = R.id.tv_born)
	private void onBornClick(View v) {
		DD.d(TAG, "onBornClick()");

		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		new DatePickerDialog(this, new OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				DD.d(TAG, "onDateSet(), year: " + year + ", month: " + monthOfYear + ", day: "
						+ dayOfMonth);
				String s = CommonUtil.getYYYYMMDDDate(year, ++monthOfYear, dayOfMonth);
				updateBornDate(s);
			}
		}, year, month, day).show();
	}

	@Event(value = R.id.tv_gender)
	private void onGenderClick(View v) {
		DD.d(TAG, "onGenderClick()");

		bpwGenderSelector.show();
	}

	@Event(value = R.id.btn_update_password)
	private void onUpdatePasswordClick(View v) {
		DD.d(TAG, "onUpdatePasswordClick()");

		btnUpdatePassword.setVisibility(View.GONE);
		llPasswordContainer.setVisibility(View.VISIBLE);
	}

	@Event(value = R.id.btn_goto_update_password)
	private void onGotoUpdatePasswordClick(View v) {
		DD.d(TAG, "onGotoUpdatePasswordClick()");

		// TOCO check
		String oldPassword = etOldPassword.getText().toString();
		String newPassword = etNewPassword.getText().toString();
		String confirmNewPassword = etConfirmPassowrd.getText().toString();
		if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)
				|| TextUtils.isEmpty(confirmNewPassword) || !newPassword.equals(confirmNewPassword)) {
			TT.show(this, "check error");
			return;
		}

		updatePassword(oldPassword, newPassword);
	}

}
