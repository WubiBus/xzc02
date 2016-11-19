package cn.incorner.contrast.page;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback.CancelledException;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import com.alibaba.fastjson.JSON;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.adapter.RecomendAdapter;
import cn.incorner.contrast.data.entity.RecommendEntity;
import cn.incorner.contrast.data.entity.RecommendListEntity;
import cn.incorner.contrast.data.entity.StatusResultEntity;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.util.TT;
import cn.incorner.contrast.view.ScrollListenerListView;

@ContentView(R.layout.activity_recommend)
public class RecomendActivity extends BaseActivity {
	
	private static final String TAG = "RecomendActivity";
	
	private RecomendActivity mAcitivty;
	
	private RecomendAdapter recommendAdapter;
	
	private List<RecommendEntity> list = new ArrayList<RecommendEntity>();
	
	@ViewInject(R.id.lv_content)
	private ListView lvContent;
	@ViewInject(R.id.tv_goto)
	private TextView tvGoto;
	@ViewInject(R.id.tv_change)
	private TextView tvChange;
	
	private int fromValue = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAcitivty = this;
		tvGoto.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG );
		tvChange.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG );
		recommendAdapter = new RecomendAdapter(list, getLayoutInflater());
		lvContent.setAdapter(recommendAdapter);
		loadData();
	}
	
	@Event(value = R.id.tv_goto)
	private void OnGotoClick(View v){
		//跳转到主页
		gotoActivity(MainActivity.class);
		//直接回到当前页
		//finish();
	}
	
	@Event(value = R.id.tv_change)
	private void OnChangeClick(View v){
		loadMoreData();
	}
	
	
	/**
	 * 从服务器加载数据
	 */
	private void loadData() {
		DD.d(TAG, "loadData()");
		RequestParams params = new RequestParams(Config.PATH_GET_RECOMMEND_USER);
		params.setAsJsonContent(true);
		params.addParameter("accessToken",PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("from", fromValue);
		params.addParameter("row", 5);
		x.http().post(params, new CommonCallback<JSONObject>() {
			@Override
			public void onCancelled(CancelledException arg0) {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
			}

			@Override
			public void onFinished() { 
				// crlContainer.setRefreshing(false);
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result.toString());
				RecommendListEntity entity = JSON.parseObject(result.toString(),RecommendListEntity.class);
				List<RecommendEntity> listTemp = new ArrayList<RecommendEntity>();
				if ("0".equals(entity.getStatus())) {
					listTemp = entity.getUsers();
					list.clear();
					list.addAll(listTemp);
				}
				recommendAdapter.notifyDataSetChanged();
				fromValue++;
			}
		});
	}
	
	/**
	 * 从服务器加载数据
	 */
	private void loadMoreData() {
		DD.d(TAG, "loadMoreData()");
		RequestParams params = new RequestParams(Config.PATH_GET_RECOMMEND_USER);
		params.setAsJsonContent(true);
		params.addParameter("accessToken",PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("from", fromValue*5);
		params.addParameter("row", 5);
		x.http().post(params, new CommonCallback<JSONObject>() {
			@Override
			public void onCancelled(CancelledException arg0) {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
			}

			@Override
			public void onFinished() { 
				// crlContainer.setRefreshing(false);
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result.toString());
				RecommendListEntity entity = JSON.parseObject(result.toString(),RecommendListEntity.class);
				List<RecommendEntity> listTemp = new ArrayList<RecommendEntity>();
				if ("0".equals(entity.getStatus())) {
					list.clear();
					listTemp = entity.getUsers();
					list.addAll(listTemp);
				}
				recommendAdapter.notifyDataSetChanged();
				fromValue++;
			}
		});
	}
}
