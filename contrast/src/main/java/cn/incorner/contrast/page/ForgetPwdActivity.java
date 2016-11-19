package cn.incorner.contrast.page;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.GetAccessResultEntity;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.TT;

import com.alibaba.fastjson.JSON;

@ContentView(R.layout.activity_forgetpwd)
public class ForgetPwdActivity extends BaseActivity {
	private static final String TAG = "ForgetPwdActivity";
	
	private  Pattern patternPhone = Pattern.compile(Config.PATTERN_PHONE);
	private  Pattern patternEmail = Pattern.compile(Config.PATTERN_EMAIL);
	private ForgetPwdActivity mActivity;
	private ProgressDialog pdDialog;
	@ViewInject(R.id.et_loginname)
	private EditText etLoginname;
	@ViewInject(R.id.tv_findpwd)
	private TextView tvFindpwd;
	@ViewInject(R.id.ll_back)
	private LinearLayout llBack;
	private String loginameStr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = this;
		initProgressDialog();
	}
	
	@Event(value = R.id.tv_findpwd)
	private void onFindpwdClick(View v){
		loginameStr = etLoginname.getText().toString().trim();
		if(!TextUtils.isEmpty(loginameStr)){
				Matcher matcherPhone = patternPhone.matcher(loginameStr);
				Matcher matcherEmail = patternEmail.matcher(loginameStr);
				if (!matcherPhone.find() && !matcherEmail.find()) {
					TT.show(mActivity, "请输入正确的手机或邮箱");
					return;
				}
			    findPwd();
		}else{
			TT.show(mActivity, "请输入正确的手机或邮箱");
		}
		
	}
	
	@Event(value = R.id.ll_back)
	private void onBackClick(View v){
		this.finish();
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
	 * 找回密码
	 */
	private void findPwd() {
		DD.d(TAG, "findPwd()");

		pdDialog.setMessage("正在发送...");
		pdDialog.show();

		RequestParams params = new RequestParams(Config.PATH_GET_FINDPWD);
		params.setAsJsonContent(true);
		params.addParameter("userAccount", loginameStr);
		x.http().post(params, new CommonCallback<JSONObject>() {
			@Override
			public void onCancelled(CancelledException arg0) {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
				DD.d(TAG, "onError(), e: " + arg0.getMessage());
				TT.show(mActivity, "网络错误");
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
					TT.show(mActivity, "系统已成功为您修改密码，请查看您的手机或邮箱");
				} else {
					TT.show(mActivity, "您输入的手机或邮箱没有注册过无比账号");
				}
			}
		});
	}

}
