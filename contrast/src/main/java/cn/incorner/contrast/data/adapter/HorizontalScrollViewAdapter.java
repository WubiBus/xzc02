package cn.incorner.contrast.data.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.incorner.contrast.R;
import cn.incorner.contrast.util.DensityUtil;
import cn.incorner.contrast.util.ImageLoader;
import cn.incorner.contrast.util.ImageLoader.onBitmapLoadedListener;

public class HorizontalScrollViewAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private ArrayList<String> mDatas;
	private ImageLoader imageLoader;

	public HorizontalScrollViewAdapter(Context context, ArrayList<String> bitmapList) {
		this.mContext = context;
		mInflater = LayoutInflater.from(context);
		this.mDatas = bitmapList;
		imageLoader = ImageLoader.getInstance(mContext);
	}

	public int getCount() {
		return mDatas.size();
	}

	public Object getItem(int position) {
		return mDatas.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_imageview, parent, false);
			viewHolder.mImg = (ImageView) convertView.findViewById(R.id.imageView1);

			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.mImg
					.getLayoutParams();
			params.height = parent.getHeight();
			params.width = params.height;
			viewHolder.mImg.setLayoutParams(params);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final String path = this.mDatas.get(position);
		// viewHolder.mImg.setImageResource(R.drawable.ic_launcher);
		viewHolder.mImg.setTag(path);
		imageLoader.loadImage(viewHolder.mImg, path, DensityUtil.dip2px(mContext, 80),
				DensityUtil.dip2px(mContext, 80), new onBitmapLoadedListener() {
					@Override
					public void displayImage(ImageView view, Bitmap bitmap) {
						String imagePath = view.getTag().toString();
						if (imagePath.equals(path)) {
							view.setImageBitmap(bitmap);
						}
					}
				});
		return convertView;
	}

	private class ViewHolder {
		ImageView mImg;
	}

}
