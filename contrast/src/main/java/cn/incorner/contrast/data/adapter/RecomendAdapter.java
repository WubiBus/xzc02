package cn.incorner.contrast.data.adapter;

import java.util.List;

import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback.CancelledException;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import com.alibaba.fastjson.JSON;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.RecommendEntity;
import cn.incorner.contrast.data.entity.StatusResultEntity;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.util.TT;
import cn.incorner.contrast.view.CircleImageView;

public class RecomendAdapter extends BaseAdapter {

	private List<RecommendEntity> list;
	private LayoutInflater inflater;
	private Context context;

	public RecomendAdapter(List<RecommendEntity> list, LayoutInflater inflater) {
		this.list = list;
		this.inflater = inflater;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_recommend, null);
			holder = new ViewHolder();
			holder.civAvatar = (CircleImageView) convertView.findViewById(R.id.civ_avatar);
			holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
			holder.llFollow = (LinearLayout)convertView.findViewById(R.id.ll_follow);
			holder.lvFollow = (ImageView)convertView.findViewById(R.id.iv_follow);
			holder.tvFollow = (TextView) convertView.findViewById(R.id.tv_follow);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final RecommendEntity entity = list.get(position);
		if (context == null) {
			context = holder.civAvatar.getContext();
		}

		holder.civAvatar.setTag(position);
		holder.lvFollow.setImageResource(R.drawable.recommend_unfollowed);
		holder.tvFollow.setText("关注");
		holder.llFollow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				long userId = entity.getUserId();
				if(holder.tvFollow.getText().toString().trim().equals("关注")){
					addFollow(userId);
					holder.lvFollow.setImageResource(R.drawable.recommend_followed);
//					notifyDataSetChanged();
					holder.tvFollow.setText("取消关注");
					TT.show(context, "已关注");
				}else{
					cancelFollow(userId);
					holder.lvFollow.setImageResource(R.drawable.recommend_unfollowed);
//					notifyDataSetChanged();
					holder.tvFollow.setText("关注");
					TT.show(context, "取消关注");
				}
				
			}
		});
	    Picasso.with(context).load(Config.getHeadFullPath(entity.getAvatarName())).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).into(holder.civAvatar);
    
		holder.tvName.setText(entity.getNickname());
		holder.lvFollow.setBackgroundResource(R.drawable.recommend_unfollowed);

		return convertView;
	}

	private class ViewHolder {
		private CircleImageView civAvatar;
		private TextView tvName;
		private TextView tvFollow;
		private LinearLayout llFollow;
		private ImageView lvFollow;
	}
	
	/**
	 * 添加关注
	 */
	private void addFollow(long userId) {

		RequestParams params = new RequestParams(Config.PATH_ADD_FOLLOW);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("followUserId", userId);
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
				StatusResultEntity entity = JSON.parseObject(result.toString(),
						StatusResultEntity.class);
				if ("0".equals(entity.getStatus())) {
//					updateFollowState(1);
//					TT.show(UserParagraphListActivity.this, "已关注");
				}
			}
		});
	}

	/**
	 * 取消关注
	 */
	private void cancelFollow(long userId) {

		RequestParams params = new RequestParams(Config.PATH_CANCEL_FOLLOW);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("cancelUserId", userId);
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
				StatusResultEntity entity = JSON.parseObject(result.toString(),
						StatusResultEntity.class);
				if ("0".equals(entity.getStatus())) {
//					updateFollowState(0);
//					TT.show(UserParagraphListActivity.this, "停止关注");
				}
			}
		});
	}

}
