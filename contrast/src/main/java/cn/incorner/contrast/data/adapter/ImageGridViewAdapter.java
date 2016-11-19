package cn.incorner.contrast.data.adapter;

import java.util.List;

import org.xutils.x;

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

/**
 * 图片网格视图 适配器
 * 
 * @author yeshimin
 */
public class ImageGridViewAdapter extends BaseAdapter {

	private static final String TAG = "ImageGridViewAdapter";

	private List<ParagraphEntity> list;
	private LayoutInflater inflater;

	public ImageGridViewAdapter(List<ParagraphEntity> list, LayoutInflater inflater) {
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
			convertView = inflater.inflate(R.layout.item_contrast_image_grid_view, null);
			holder = new ViewHolder();
			holder.rlImageContainer = (RelativeLayout) convertView
					.findViewById(R.id.rl_image_container);
			holder.llImageContainer = (LinearLayout) convertView
					.findViewById(R.id.ll_image_container);
			holder.ivImage1 = (ImageView) convertView.findViewById(R.id.iv_image_1);
			holder.ivImage2 = (ImageView) convertView.findViewById(R.id.iv_image_2);
			holder.ivCompatibleImage = (ImageView) convertView
					.findViewById(R.id.iv_compatible_image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ParagraphEntity entity = list.get(position);
		// 解析图片
		parseImage(entity, holder);

		return convertView;
	}

	/**
	 * 解析图片
	 */
	private void parseImage(ParagraphEntity entity, ViewHolder holder) {
		String picName = entity.getPicName();
		if (TextUtils.isEmpty(picName)) {
			holder.rlImageContainer.setVisibility(View.GONE);
			return;
		}

		String[] arrPicName = picName.split(";");
		if (arrPicName.length == 2) {
			String paragraphContent = entity.getParagraphContent();
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

	private class ViewHolder {
		private RelativeLayout rlImageContainer;
		private LinearLayout llImageContainer;
		private ImageView ivImage1;
		private ImageView ivImage2;
		private ImageView ivCompatibleImage;
	}

}
