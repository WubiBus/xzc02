package cn.incorner.contrast.data.adapter;

import java.util.ArrayList;

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
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.ParagraphCommentEntity;
import cn.incorner.contrast.page.MyParagraphActivity;
import cn.incorner.contrast.page.UserParagraphListActivity;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.view.CircleImageView;

/**
 * 对比度评论 列表适配器
 * 
 * @author yeshimin
 */
public class ContrastCommentListAdapter extends BaseAdapter {

	private static final String TAG = "ContrastCommentListAdapter";

	private ArrayList<ParagraphCommentEntity> list;
	private LayoutInflater inflater;
	private OnHeadViewClickListener onClickListener;

	public ContrastCommentListAdapter(ArrayList<ParagraphCommentEntity> list,
			LayoutInflater inflater) {
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
			convertView = inflater.inflate(R.layout.item_contrast_comment, null);
			holder = new ViewHolder();
			holder.llRoot = (LinearLayout) convertView.findViewById(R.id.ll_root);
			holder.civHead = (CircleImageView) convertView.findViewById(R.id.civ_head);
			holder.tvNickname = (TextView) convertView.findViewById(R.id.tv_nickname);
			holder.tvDateTime = (TextView) convertView.findViewById(R.id.tv_date_time);
			holder.tvArea = (TextView) convertView.findViewById(R.id.tv_area);
			holder.tvComment = (TextView) convertView.findViewById(R.id.tv_comment);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ParagraphCommentEntity entity = list.get(position);
		if (onClickListener == null) {
			onClickListener = new OnHeadViewClickListener(holder.civHead.getContext());
		}

		// 设置背景颜色
		if (position % 2 == 0) {
			holder.llRoot.setBackgroundColor(Color.WHITE);
		} else {
			holder.llRoot.setBackgroundColor(0x1a888888);
		}

		holder.civHead.setTag(position);
		holder.civHead.setOnClickListener(onClickListener);
		if (entity.getReplyUserisAnonymous() == 1) {
			holder.civHead.setImageResource(R.drawable.default_avatar);
		} else {
			if (TextUtils.isEmpty(entity.getReplyUserPic())) {
				holder.civHead.setImageResource(R.drawable.default_avatar);
			} else {
				x.image().loadDrawable(Config.getHeadFullPath(entity.getReplyUserPic()), null,
						new CommonCallback<Drawable>() {
							@Override
							public void onCancelled(CancelledException arg0) {
							}

							@Override
							public void onError(Throwable arg0, boolean arg1) {
								DD.d(TAG, "onError(), arg0: " + arg0.getMessage());
								holder.civHead.setImageResource(R.drawable.default_avatar);
							}

							@Override
							public void onFinished() {
								DD.d(TAG, "onFinished()");
							}

							@Override
							public void onSuccess(Drawable drawable) {
								DD.d(TAG, "onSuccess()");
								Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
								holder.civHead.setImageBitmap(bitmap);
							}
						});
			}
		}
		if (entity.getReplyUserisAnonymous() == 1) {
			holder.tvNickname.setText("匿名");
		} else {
			holder.tvNickname.setText(entity.getReplyUserNickname());
		}
		holder.tvDateTime.setText(entity.getCreateTime());
		holder.tvArea.setText(entity.getLocation());
		holder.tvComment.setText(entity.getReplyContent());

		return convertView;
	}

	private class ViewHolder {
		private LinearLayout llRoot;
		private CircleImageView civHead;
		private TextView tvNickname;
		private TextView tvDateTime;
		private TextView tvArea;
		private TextView tvComment;
	}

	private class OnHeadViewClickListener implements OnClickListener {
		private Context context;

		public OnHeadViewClickListener(Context context) {
			this.context = context;
		}

		public void onClick(View v) {
			int position = (int) v.getTag();
			ParagraphCommentEntity entity = list.get(position);

			if (PrefUtil.getIntValue(Config.PREF_USER_ID) == entity.getReplyUserId()) {
				BaseActivity.sGotoActivity(context, MyParagraphActivity.class);
			} else {
				Intent intent = new Intent();
				intent.setClass(context, UserParagraphListActivity.class);
				intent.putExtra("userId", entity.getReplyUserId());
				intent.putExtra("head", entity.getReplyUserPic());
				intent.putExtra("nickname", entity.getReplyUserNickname());
				BaseActivity.sGotoActivity(context, intent);
			}
		}
	}

}
