package cn.incorner.contrast.data.adapter;

import java.util.List;
import java.util.Random;

import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;

import com.squareup.picasso.Picasso;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.FollowingEntity;
import cn.incorner.contrast.page.SendPrivateMessagesActivity;
import cn.incorner.contrast.util.TT;
import cn.incorner.contrast.view.CircleImageView;

/**
 * 我关注的人 图片列表 适配器
 * 
 * @author yeshimin
 */
public class MyFollowingUserEntryListAdapter extends BaseAdapter {

	private static final String TAG = "MyFollowingUserEntryListAdapter";

	private List<FollowingEntity> list;
	private LayoutInflater inflater;
	private Context context;
	private Random random = new Random();

	public MyFollowingUserEntryListAdapter(List<FollowingEntity> list, LayoutInflater inflater,Context context) {
		this.list = list;
		this.inflater = inflater;
		this.context = context;
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
			convertView = inflater.inflate(R.layout.item_image_grid_view_2, null);
			holder = new ViewHolder();
			holder.rlMe1 = (RelativeLayout)convertView.findViewById(R.id.rl_me);
			holder.ivImage = (CircleImageView) convertView.findViewById(R.id.iv_image);
			holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tvName2 = (TextView) convertView.findViewById(R.id.tv_name2);
			holder.ivSendprivate = (ImageView) convertView.findViewById(R.id.iv_sendprivate);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final FollowingEntity entity = list.get(position);
		  Picasso.with(context).load(Config.getHeadFullPath(entity.getAvatarName())).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).into(holder.ivImage);
		    
//		if (TextUtils.isEmpty(entity.getAvatarName())) {
//			holder.ivImage.setImageResource(R.drawable.default_avatar);
//		} else {
//			x.image().loadDrawable(Config.getHeadFullPath(entity.getAvatarName()), null,
//					new CommonCallback<Drawable>() {
//						@Override
//						public void onCancelled(CancelledException arg0) {
//						}
//
//						@Override
//						public void onError(Throwable arg0, boolean arg1) {
//							holder.ivImage.setImageResource(R.drawable.default_avatar);
//						}
//
//						@Override
//						public void onFinished() {
//						}
//
//						@Override
//						public void onSuccess(Drawable drawable) {
//							Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
//							holder.ivImage.setImageBitmap(bitmap);
//						}
//					});
//		}
		if (TextUtils.isEmpty(entity.getAvatarName())){
			holder.ivImage.setVisibility(View.INVISIBLE);
			holder.tvName.setVisibility(View.INVISIBLE);
			holder.tvName2.setText(entity.getNickname());
			holder.tvName2.setGravity(Gravity.CENTER);
		} else {
			holder.ivImage.setVisibility(View.VISIBLE);
			holder.tvName.setVisibility(View.VISIBLE);
			holder.tvName.setText(entity.getNickname());
			holder.tvName2.setVisibility(View.GONE);
		}
		
		holder.ivSendprivate.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(context, SendPrivateMessagesActivity.class);
				intent.putExtra("userId", entity.getUserId());
				intent.putExtra("head", entity.getAvatarName());
				intent.putExtra("nickname", entity.getNickname());
				context.startActivity(intent);
			}
		});
		// TODO: 2016/7/28
		//给控件设置背景颜色
		int colorIndex = random.nextInt(99) % 20;
		int bgColor = BaseActivity.BG_COLOR[colorIndex];
		int textColor = BaseActivity.TEXT_COLOR[colorIndex];
		bgColor = Color.argb((int) (Color.alpha(bgColor) * 0.02), Color.red(bgColor),
				Color.green(bgColor), Color.blue(bgColor));
		holder.rlMe1.setBackgroundColor(bgColor);

		return convertView;
	}

	private class ViewHolder {
		private CircleImageView ivImage;
		private TextView tvName;
		private TextView tvName2;
		private ImageView ivSendprivate;
		private RelativeLayout rlMe1;
	}

}
