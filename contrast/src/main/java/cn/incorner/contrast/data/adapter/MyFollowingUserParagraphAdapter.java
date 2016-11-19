package cn.incorner.contrast.data.adapter;

import java.util.List;

import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;

import com.umeng.socialize.PlatformConfig.GooglePlus;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.ParagraphEntity;

public class MyFollowingUserParagraphAdapter extends BaseAdapter {

	private static final String TAG = "MyFollowingUserParagraphAdapter";

	private List<ParagraphEntity> list;
	private LayoutInflater inflater;

	public MyFollowingUserParagraphAdapter(List<ParagraphEntity> list, LayoutInflater inflater) {
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
			convertView = inflater.inflate(R.layout.item_myfollowuserparagraph, null);
			holder = new ViewHolder();
			holder.rlVertical = (LinearLayout) convertView.findViewById(R.id.rl_vertical);
			holder.rlHorizontal = (LinearLayout) convertView.findViewById(R.id.rl_horizontal);
			
			holder.ivVerticalImage = (ImageView) convertView.findViewById(R.id.iv_vertial_image);
			holder.ivVerticalImage2 = (ImageView) convertView.findViewById(R.id.iv_vertial_image2);
			
			holder.ivHorizontalImage= (ImageView) convertView.findViewById(R.id.iv_horizontal_image);
			holder.ivHorizontalImage2 = (ImageView) convertView.findViewById(R.id.iv_horizontal_image2);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ParagraphEntity entity = list.get(position);
		if (entity.getParagraphContent().contains("up")) {// 上下
			holder.rlVertical.setVisibility(View.VISIBLE);
			holder.rlHorizontal.setVisibility(View.GONE);
			if (entity.getPicName() != null) {
				String[] picStr = entity.getPicName().split(";");
				if (picStr.length == 2 && picStr.length > 0) {
					holder.ivVerticalImage.setVisibility(View.VISIBLE);
					holder.ivVerticalImage2.setVisibility(View.VISIBLE);
					if (TextUtils.isEmpty(picStr[0])) {
						holder.ivVerticalImage.setImageResource(R.drawable.default_avatar);
					} else {
						x.image().bind(holder.ivVerticalImage,Config.getContrastSmallFullPath(picStr[0]));
					}
					if (TextUtils.isEmpty(picStr[1])) {
						holder.ivVerticalImage2.setImageResource(R.drawable.default_avatar);
					} else {
						x.image().bind(holder.ivVerticalImage2,Config.getContrastSmallFullPath(picStr[1]));
					}
				}else if(picStr.length==1){
					holder.ivVerticalImage.setVisibility(View.VISIBLE);
					x.image().bind(holder.ivVerticalImage,Config.getContrastSmallFullPath(picStr[0]));
					holder.ivVerticalImage2.setVisibility(View.GONE);
				}
			}
		} else if (entity.getParagraphContent().contains("left")) {// 左右
			holder.rlVertical.setVisibility(View.GONE);
			holder.rlHorizontal.setVisibility(View.VISIBLE);
			if (entity.getPicName() != null) {
				String[] picStr = entity.getPicName().split(";");
				if (picStr.length == 2 && picStr.length > 0) {
					holder.ivHorizontalImage.setVisibility(View.VISIBLE);
					holder.ivHorizontalImage2.setVisibility(View.VISIBLE);
					if (TextUtils.isEmpty(picStr[0])) {
						holder.ivHorizontalImage.setImageResource(R.drawable.default_avatar);
					} else {
						x.image().bind(holder.ivHorizontalImage,Config.getContrastSmallFullPath(picStr[0]));
					}
					if (TextUtils.isEmpty(picStr[1])) {
						holder.ivHorizontalImage2.setImageResource(R.drawable.default_avatar);
					} else {
						x.image().bind(holder.ivHorizontalImage2,Config.getContrastSmallFullPath(picStr[1]));
					}
				}else if(picStr.length==1){
					holder.ivHorizontalImage.setVisibility(View.VISIBLE);
					x.image().bind(holder.ivHorizontalImage,Config.getContrastSmallFullPath(picStr[0]));
					holder.ivHorizontalImage2.setVisibility(View.GONE);
				}
			}
		}
		return convertView;
	}

	private class ViewHolder {
		private LinearLayout rlVertical;
		private ImageView ivVerticalImage;
		private ImageView ivVerticalImage2;
		private LinearLayout rlHorizontal;
		private ImageView ivHorizontalImage;
		private ImageView ivHorizontalImage2;
	}

}
