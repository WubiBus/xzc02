package cn.incorner.contrast.data.adapter;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.incorner.contrast.R;
import cn.incorner.contrast.page.PostActivity;
import cn.incorner.contrast.util.BitMapUtil;
import cn.incorner.contrast.util.DensityUtil;
import cn.incorner.contrast.util.GPUImageFilterTools;
import cn.incorner.contrast.util.GPUImageFilterTools.FilterAdjuster;
import cn.incorner.contrast.util.GPUImageFilterTools.FilterType;

public class SelectImageFilterAdapter extends BaseAdapter {
	private GPUImage gpuImage;
	private List<FilterType> listFilter;
	private int[] arrFilterAdjusterValue;
	private String path;
	private LayoutInflater mInflater;
	private ArrayList<Bitmap> allBitmaps = new ArrayList<Bitmap>();
	private Context context;
	private int direction;

	public SelectImageFilterAdapter(Context context, List<FilterType> listFilter,
			int[] arrFilterAdjusterValue, String path, int direction) {
		mInflater = LayoutInflater.from(context);
		gpuImage = new GPUImage(context);
		this.context = context;
		this.listFilter = listFilter;
		this.arrFilterAdjusterValue = arrFilterAdjusterValue;
		this.path = path;
		this.direction = direction;
		initBitmaps();
	}

	public void recycleAllBitmap() {// 回收所有的Bitmap
		for (Bitmap bitmap : allBitmaps) {
			bitmap.recycle();
			bitmap = null;
		}
	}

	private void initBitmaps() {
		int size = listFilter.size();
		for (int i = 0; i < size; ++i) {
			allBitmaps.add(diejia(BitMapUtil.getBitmap(path, DensityUtil.dip2px(context, 50)), i));
		}
	}

	@Override
	public int getCount() {
		return listFilter.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_filter_imageview, parent, false);
			viewHolder.mImg = (ImageView) convertView.findViewById(R.id.imageView1);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if (direction == LinearLayout.HORIZONTAL) {// 水平 2列4行
			LayoutParams params = viewHolder.mImg.getLayoutParams();
			params.height = PostActivity.PIC_HEIGHT / 4;
			params.width = PostActivity.PIC_WIDTH / 4;
			viewHolder.mImg.setLayoutParams(params);
		} else {// 水平 4列两行
			LayoutParams params = viewHolder.mImg.getLayoutParams();
			params.height = PostActivity.PIC_HEIGHT / 4;
			params.width = PostActivity.PIC_WIDTH / 4;
			viewHolder.mImg.setLayoutParams(params);
		}
		viewHolder.mImg.setImageBitmap(allBitmaps.get(position));
		return convertView;
	}

	private class ViewHolder {
		ImageView mImg;
	}

	/**
	 * 图片叠加
	 * 
	 * @param b
	 * @return
	 */
	public Bitmap diejia(Bitmap b, int position) {
		Bitmap newBitmap;
		// 复制之前图片
		newBitmap = b.copy(Bitmap.Config.RGB_565, true);//
		Canvas canvas = new Canvas(newBitmap);

		// 叠加新图，设置滤镜
		GPUImageFilter filter = GPUImageFilterTools.createFilterForType(context,
				listFilter.get(position));
		int adjusterValue = arrFilterAdjusterValue[position];
		if (adjusterValue >= 0) {
			new FilterAdjuster(filter).adjust(adjusterValue);
		}
		gpuImage.setFilter(filter);
		newBitmap = gpuImage.getBitmapWithFilterApplied(newBitmap);
		canvas.drawBitmap(newBitmap, 0, 0, new Paint());

		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return newBitmap;
	}

}
