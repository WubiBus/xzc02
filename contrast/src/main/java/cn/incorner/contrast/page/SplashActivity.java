package cn.incorner.contrast.page;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.Constant;
import cn.incorner.contrast.R;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.util.ViewUtil;


@ContentView(R.layout.activity_splash)
public class SplashActivity extends BaseActivity{
	
	private SplashActivity mActivity;
	private Animation alpha;
	@ViewInject(R.id.rel_param)
	private RelativeLayout relParam;
	private int isFirst = -1;//0代表已经启动过，1代表第一次启动
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mActivity =this;
		ViewUtil.hideTitle(mActivity);
		ViewUtil.fullScreen(mActivity);
		super.onCreate(savedInstanceState);
		initUI();
	}
	
	
	 protected void initUI() {

	        alpha = new AlphaAnimation(0.1f, 1.0f);
	        alpha.setDuration(4000);
	        alpha.setAnimationListener(new AnimationListener() {

	            @Override
	            public void onAnimationStart(Animation animation) {
	            }

	            @Override
	            public void onAnimationRepeat(Animation animation) {
	            }

	            @Override
	            public void onAnimationEnd(Animation animation) {
	            	isFirst = PrefUtil.getIntValue(Constant.IS_FIRST);
	            	if(isFirst==1){
	            		startActivity(new Intent(mActivity, MainActivity.class));
	            		finish();
	            	}else if(isFirst==0){
	            		PrefUtil.setIntValue(Constant.IS_FIRST, 1);
	            		startActivity(new Intent(mActivity, GuideActivity.class));
	            		finish();
	            	}
	            }
	        });
	        relParam.startAnimation(alpha);
	    }

}
