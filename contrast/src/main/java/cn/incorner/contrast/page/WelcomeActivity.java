package cn.incorner.contrast.page;

import java.util.concurrent.TimeUnit;

import org.xutils.view.annotation.ContentView;

import android.os.Bundle;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.R;

/**
 * 欢迎 页面
 * 
 * @author yeshimin
 */
@ContentView(R.layout.activity_welcome)
public class WelcomeActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				gotoActivity(MainActivity.class);
				finish();
			}
		}).start();
	}

}
