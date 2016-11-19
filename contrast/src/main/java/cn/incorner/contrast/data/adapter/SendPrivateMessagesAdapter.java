package cn.incorner.contrast.data.adapter;

import java.util.List;
import java.util.regex.Pattern;

import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.ParagraphCommentEntity;
import cn.incorner.contrast.data.entity.PrivateMessageEntity;
import cn.incorner.contrast.page.MyFollowingUserParagraphActivity;
import cn.incorner.contrast.page.MyParagraphActivity;
import cn.incorner.contrast.page.MyParagraphShowActivity;
import cn.incorner.contrast.page.UserParagraphListActivity;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.view.CircleImageView;

public class SendPrivateMessagesAdapter extends BaseAdapter {

	private static final Pattern patternDescUpDown = Pattern.compile(Config.PATTERN_DESC_UP_DOWN);
	private static final Pattern patternDescLeftRight = Pattern
			.compile(Config.PATTERN_DESC_LEFT_RIGHT);

	private List<PrivateMessageEntity> list;
	private LayoutInflater inflater;
	private Context context;
	private OnHeadClickListener onHeadClickListener;


	public SendPrivateMessagesAdapter(List<PrivateMessageEntity> list,LayoutInflater inflater) {
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
			convertView = inflater.inflate(R.layout.item_privatemessage, null);
			holder = new ViewHolder();
			holder.llRoot = (LinearLayout) convertView.findViewById(R.id.ll_root);
			holder.civAvatar = (CircleImageView) convertView.findViewById(R.id.civ_avatar);
			holder.tvNickname = (TextView) convertView.findViewById(R.id.tv_nickname);
			holder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
			holder.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		PrivateMessageEntity entity = list.get(position);
		if (context == null) {
			context = holder.civAvatar.getContext();
		}
		if (onHeadClickListener == null) {
			onHeadClickListener = new OnHeadClickListener(holder.civAvatar.getContext());
		}


		// 设置背景颜色
		if (position % 2 == 0) {
			holder.llRoot.setBackgroundColor(Color.WHITE);
		} else {
			holder.llRoot.setBackgroundColor(0x1a888888);
		}

		holder.civAvatar.setTag(position);
		holder.civAvatar.setOnClickListener(onHeadClickListener);
		x.image().loadDrawable(entity.getFromUserId()==PrefUtil.getIntValue(Config.PREF_USER_ID)?Config.getHeadFullPath(PrefUtil.getStringValue(Config.PREF_AVATAR_NAME)):Config.getHeadFullPath(PrefUtil.getStringValue("headToUser")), null,
				new CommonCallback<Drawable>() {
					@Override
					public void onCancelled(CancelledException arg0) {
					}

					@Override
					public void onError(Throwable arg0, boolean arg1) {
						holder.civAvatar.setImageResource(R.drawable.default_avatar);
					}

					@Override
					public void onFinished() {
					}

					@Override
					public void onSuccess(Drawable drawable) {
						Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
						holder.civAvatar.setImageBitmap(bitmap);
					}
				});
		holder.tvNickname.setText(entity.getFromUserId()==PrefUtil.getIntValue(Config.PREF_USER_ID)?PrefUtil.getStringValue(Config.PREF_NICKNAME):PrefUtil.getStringValue("nicknameToUser"));
					
		holder.tvContent.setText(entity.getReplyContent());
		holder.tvTime.setText(entity.getCreateTime());
		return convertView;
	}

	private class ViewHolder {
		private LinearLayout llRoot;
		private CircleImageView civAvatar;
		private TextView tvNickname;
		private TextView tvTime;
		private TextView tvContent;
	}

	/**
	 * 点击头像监听
	 */
	private class OnHeadClickListener implements OnClickListener {
		private Context context;

		public OnHeadClickListener(Context context) {
			this.context = context;
		}
		@Override
		public void onClick(View v) {

			int position = (int) v.getTag();
			PrivateMessageEntity entity = list.get(position);

			if (PrefUtil.getIntValue(Config.PREF_USER_ID) == entity.getFromUserId()) {
				BaseActivity.sGotoActivity(context, MyParagraphShowActivity.class);
			} else {
				Intent intent = new Intent();
				intent.setClass(context, MyFollowingUserParagraphActivity.class);
				intent.putExtra("userId", entity.getFromUserId());
				intent.putExtra("head", PrefUtil.getStringValue("headToUser"));
				intent.putExtra("nickname", PrefUtil.getStringValue("nicknameToUser"));
				BaseActivity.sGotoActivity(context, intent);
			}
		}
	}

}
