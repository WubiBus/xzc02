package cn.incorner.contrast.data.adapter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.NewsInfoEntity;
import cn.incorner.contrast.page.MyFollowingUserParagraphActivity;
import cn.incorner.contrast.page.UserParagraphListActivity;
import cn.incorner.contrast.view.CircleImageView;

/**
 * 用户消息 数据适配器
 * 
 * @author yeshimin
 */
public class MessageAdapter extends BaseAdapter {

	private static final Pattern patternDescUpDown = Pattern.compile(Config.PATTERN_DESC_UP_DOWN);
	private static final Pattern patternDescLeftRight = Pattern
			.compile(Config.PATTERN_DESC_LEFT_RIGHT);

	private List<NewsInfoEntity> list;
	private LayoutInflater inflater;
	private Context context;
	private OnHeadClickListener onHeadClickListener;

	public MessageAdapter(List<NewsInfoEntity> list, LayoutInflater inflater) {
		this.list = list;
		this.inflater = inflater;
		onHeadClickListener = new OnHeadClickListener();
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
			convertView = inflater.inflate(R.layout.item_message, null);
			holder = new ViewHolder();
			holder.llRoot = (LinearLayout) convertView.findViewById(R.id.ll_root);
			holder.civAvatar = (CircleImageView) convertView.findViewById(R.id.civ_avatar);
			holder.tvMsgType = (TextView) convertView.findViewById(R.id.tv_msg_type);
			holder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
			holder.tvPosition = (TextView) convertView.findViewById(R.id.tv_position);
			holder.tvDesc = (TextView) convertView.findViewById(R.id.tv_desc);
			//新增
			holder.tvDesc2 = (TextView) convertView.findViewById(R.id.tv_desc2);
			holder.tvDesc3 = (TextView) convertView.findViewById(R.id.tv_desc3);

			holder.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
			holder.rlImageContainer = (RelativeLayout) convertView.findViewById(R.id.rl_image_container);
			holder.llImageContainer = (LinearLayout) convertView.findViewById(R.id.ll_image_container);
			holder.ivImage1 = (ImageView) convertView.findViewById(R.id.iv_image_1);
			holder.ivImage2 = (ImageView) convertView.findViewById(R.id.iv_image_2);
			holder.ivCompatibleImage = (ImageView) convertView
					.findViewById(R.id.iv_compatible_image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		NewsInfoEntity entity = list.get(position);
		if (context == null) {
			context = holder.civAvatar.getContext();
		}

		// 设置背景颜色
		if (position % 2 == 0) {
			holder.llRoot.setBackgroundColor(Color.WHITE);
		} else {
			holder.llRoot.setBackgroundColor(0x1a888888);
		}

		holder.civAvatar.setTag(position);
		holder.civAvatar.setOnClickListener(onHeadClickListener);
		x.image().loadDrawable(Config.getHeadFullPath(entity.getFromUserAvatarName()), null,
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

		// 解析图片
		parseImage(entity, holder);

		holder.tvContent.setText(entity.getShareContent());
		String nickname = entity.getFromUserNickname();
		String msgType ="";
		switch (entity.getInfoType()) {
		case NewsInfoEntity.INFO_TYPE_LIKE://赞
			msgType = nickname + "喜欢你的大作";
			holder.tvMsgType.setTextColor(Color.BLACK);
			holder.tvTime.setTextColor(Color.BLACK);
			holder.tvDesc.setVisibility(View.VISIBLE);
			holder.tvContent.setVisibility(View.GONE);
			holder.rlImageContainer.setVisibility(View.GONE);
			// TODO: 2016/7/25
			//通过getLocation()获取到位置信息
			holder.tvPosition.setText(entity.getLocation());
			holder.tvPosition.setVisibility(View.VISIBLE);
			break;
		case NewsInfoEntity.INFO_TYPE_DISLIKE:// 讨厌
			msgType = nickname + "反感你的大作";
			holder.tvMsgType.setTextColor(Color.RED);
			holder.tvTime.setTextColor(Color.RED);
			holder.tvDesc.setVisibility(View.VISIBLE);
			holder.tvContent.setVisibility(View.GONE);
			holder.rlImageContainer.setVisibility(View.GONE);
			// TODO: 2016/7/25
			//通过getLocation()获取到位置信息
			holder.tvPosition.setText(entity.getLocation());
			holder.tvPosition.setVisibility(View.VISIBLE);
			break;
		case NewsInfoEntity.INFO_TYPE_COMMENT://评论
			//让名字显示
			msgType = nickname + "的评论";
			//holder.tvMsgType.setVisibility(View.VISIBLE);
			holder.tvDesc2.setVisibility(View.GONE);
			holder.tvDesc.setVisibility(View.GONE);
			//让评论显示
			holder.tvContent.setVisibility(View.VISIBLE);
			holder.tvMsgType.setTextColor(Color.BLACK);
			holder.tvTime.setTextColor(Color.BLACK);
			break;
		case NewsInfoEntity.INFO_TYPE_NEW_WORK://好友新作品通知
			msgType = nickname;
			holder.tvMsgType.setTextColor(Color.BLACK);
			holder.tvTime.setTextColor(Color.BLACK);
			holder.tvDesc.setVisibility(View.GONE);
			holder.tvContent.setVisibility(View.VISIBLE);
			holder.rlImageContainer.setVisibility(View.VISIBLE);
			// TODO: 2016/7/25
			holder.tvPosition.setText(entity.getLocation());
			holder.tvPosition.setVisibility(View.VISIBLE);
			break;
		case NewsInfoEntity.INFO_TYPE_PRIVATEMESSAGE://留言
			msgType = nickname + "的留言";
			holder.tvDesc2.setVisibility(View.GONE);
			holder.tvDesc.setVisibility(View.GONE);
			//holder.tvDesc3.setText(entity.getShareContent());
			//holder.tvDesc3.setVisibility(View.VISIBLE);
			//holder.tvContent.setVisibility(View.GONE);
			holder.tvContent.setVisibility(View.VISIBLE);
			break;
		case NewsInfoEntity.INFO_TYPE_BE_CONCERNED://被关注消息
			msgType = nickname + "关注了你";
			holder.tvMsgType.setVisibility(View.GONE);
			holder.tvDesc2.setVisibility(View.VISIBLE);
			holder.tvDesc2.setText(msgType);
			//holder.tvDesc.setText(msgType);
			//holder.tvContent.setText(msgType);
			//holder.tvDesc.setVisibility(View.VISIBLE);
			//holder.tvContent.setVisibility(View.VISIBLE);
			break;
		case NewsInfoEntity.INFO_TYPE_BE_SELECTED://标为有趣
		case NewsInfoEntity.INFO_TYPE_BE_RECHINCONTENT://标为有料
			msgType = nickname;
			holder.tvMsgType.setVisibility(View.GONE);
			holder.tvTime.setTextColor(context.getResources().getColor(R.color.main_top_title_text));
			holder.tvPosition.setVisibility(View.GONE);
			holder.tvDesc.setVisibility(View.GONE);
			holder.tvContent.setVisibility(View.VISIBLE);
			holder.rlImageContainer.setVisibility(View.VISIBLE);
			holder.tvPosition.setText(entity.getInfo().getOriginAuthor());
			Drawable drawable = context.getResources().getDrawable(R.drawable.icon_yuanjiao);
			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
			holder.civAvatar.setImageBitmap(bitmap);
			holder.tvContent.setText(entity.getInfoType()==NewsInfoEntity.INFO_TYPE_BE_SELECTED?"恭喜您的大作入选为"+"“有趣”":"恭喜您的大作入选为"+"“有料”");
			break;
		default:
			msgType = nickname;
			break;
		}
		holder.tvMsgType.setText(msgType);
		holder.tvTime.setText(entity.getCreateTime());

		// 解析描述
		String desc = parseDesc(entity.getInfo().getParagraphContent());
		holder.tvDesc.setText(desc);

		return convertView;
	}

	/**
	 * 解析图片
	 */
	private void parseImage(NewsInfoEntity entity, ViewHolder holder) {
		String picName = entity.getInfo().getPicName();
		if (TextUtils.isEmpty(picName)) {
			holder.rlImageContainer.setVisibility(View.GONE);
			return;
		}

		String[] arrPicName = picName.split(";");
		if (arrPicName.length == 2) {
			String paragraphContent = entity.getInfo().getParagraphContent();
			if (!TextUtils.isEmpty(paragraphContent)) {
				holder.rlImageContainer.setVisibility(View.VISIBLE);
				holder.llImageContainer.setVisibility(View.VISIBLE);
				holder.ivCompatibleImage.setVisibility(View.GONE);
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.ivImage1
						.getLayoutParams();
				if (paragraphContent.contains("[up]")) {
					holder.llImageContainer.setOrientation(LinearLayout.VERTICAL);
					params.width = LinearLayout.LayoutParams.MATCH_PARENT;
					params.height = 0;
				} else {
					holder.llImageContainer.setOrientation(LinearLayout.HORIZONTAL);
					params.width = 0;
					params.height = LinearLayout.LayoutParams.MATCH_PARENT;
				}
				holder.ivImage1.setLayoutParams(params);
				holder.ivImage2.setLayoutParams(params);
				x.image().bind(holder.ivImage1, Config.getContrastSmallFullPath(arrPicName[0]));
				x.image().bind(holder.ivImage2, Config.getContrastSmallFullPath(arrPicName[1]));
			} else {
				holder.rlImageContainer.setVisibility(View.GONE);
			}
		} else if (arrPicName.length == 1) {
			holder.rlImageContainer.setVisibility(View.VISIBLE);
			holder.llImageContainer.setVisibility(View.GONE);
			holder.ivCompatibleImage.setVisibility(View.VISIBLE);
			x.image()
					.bind(holder.ivCompatibleImage, Config.getContrastSmallFullPath(arrPicName[0]));
		} else {
			holder.rlImageContainer.setVisibility(View.GONE);
		}
	}

	private String parseDesc(String content) {
		if (TextUtils.isEmpty(content)) {
			return "";
		}

		Matcher matcherDescUpDown = patternDescUpDown.matcher(content);
		Matcher matcherDescLeftRight = patternDescLeftRight.matcher(content);
		if (matcherDescUpDown.find() && matcherDescUpDown.groupCount() >= 2) {
			return matcherDescUpDown.group(1) + " | " + matcherDescUpDown.group(2);
		} else if (matcherDescLeftRight.find() && matcherDescLeftRight.groupCount() >= 2) {
			return matcherDescLeftRight.group(1) + " | " + matcherDescLeftRight.group(2);
		}
		return "";
	}

	private class ViewHolder {
		private LinearLayout llRoot;
		private CircleImageView civAvatar;
		private TextView tvMsgType;
		private TextView tvTime;
		private TextView tvPosition;
		private TextView tvDesc;
		private TextView tvDesc2;
		private TextView tvDesc3;
		private TextView tvContent;
		// image
		private RelativeLayout rlImageContainer;
		private LinearLayout llImageContainer;
		private ImageView ivImage1;
		private ImageView ivImage2;
		private ImageView ivCompatibleImage;
	}

	/**
	 * 点击头像监听
	 */
	private class OnHeadClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			int position = (int) v.getTag();
			NewsInfoEntity entity = list.get(position);
			if (entity.getInfoType() != NewsInfoEntity.INFO_TYPE_BE_SELECTED && entity.getInfoType() != NewsInfoEntity.INFO_TYPE_BE_RECHINCONTENT) {
				Intent intent = new Intent();
				intent.setClass(context, MyFollowingUserParagraphActivity.class);
				intent.putExtra("userId", entity.getFromUserId());
				intent.putExtra("head", entity.getFromUserAvatarName());
				intent.putExtra("nickname", entity.getFromUserNickname());
				BaseActivity.sGotoActivity(context, intent);
			}
		}
	}

}
