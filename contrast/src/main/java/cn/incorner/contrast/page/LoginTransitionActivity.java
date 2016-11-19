package cn.incorner.contrast.page;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.R;
import cn.incorner.contrast.util.DD;

/*
 * 登录过渡页
 */
@ContentView(R.layout.activity_logintransition)
public class LoginTransitionActivity extends BaseActivity{
	private static final String TAG = "LoginTransitionActivity";
	
	@ViewInject(R.id.iv_close)
	private ImageView ivClose;
	@ViewInject(R.id.tv_register)
	private TextView tvRegister;
	@ViewInject(R.id.tv_login)
	private TextView tvLogin;
	
	public static int REGISTER_CODE = 101;
	public static int LOGIN_CODE = 102;
	public static String LOGIN_ACTION = "login_action";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Event(value = R.id.iv_close)
	private void onCloseClick(View v) {
		DD.d(TAG, "onCloseClick()");
		gotoActivity(MainActivity.class);
		finish();
	}
	
	@Event(value = R.id.tv_register)
	private void onRegisterClick(View v) {
		DD.d(TAG, "onRegisterClick()");
		Intent intent = new Intent(this,LoginActivity2.class);
		intent.putExtra(LOGIN_ACTION, REGISTER_CODE);
		startActivityForResult(intent, 100);
	}
	
	
	@Event(value = R.id.tv_login)
	private void onLoginClick(View v) {
		DD.d(TAG, "onLoginClick()");
		Intent intent = new Intent(this,LoginActivity2.class);
		intent.putExtra(LOGIN_ACTION, LOGIN_CODE);
		startActivityForResult(intent, 100);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==100){
			switch (resultCode) {
			case 101:
//				finish();
				break;

			default:
				break;
			}
		}
	}
	
}
