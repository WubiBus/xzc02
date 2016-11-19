package cn.incorner.contrast.data.adapter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xutils.x;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.BannerEntity;
import cn.incorner.contrast.page.TopicSpecifiedListActivity;
import cn.incorner.contrast.util.DD;

public class BannerPagerAdapter extends PagerAdapter {

	private static final String TAG = "BannerPagerAdapter";

	public final Pattern PATTERN_URL = Pattern.compile(Config.PATTERN_URL);

	private Context context;
	private LayoutInflater inflater;
	private List<BannerEntity> list;

	public BannerPagerAdapter(Context context, List<BannerEntity> list) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.list = list;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		DD.d(TAG, "instantiateItem(), position: " + position + ", list.size: " + list.size());

		final View view = inflater.inflate(R.layout.view_banner, null);
		final ImageView ivImage = (ImageView) view.findViewById(R.id.iv_image);
		final View vClickMask = view.findViewById(R.id.v_click_mask);

		BannerEntity entity = list.get(position);
		x.image().bind(ivImage, Config.getBannerFullPath(entity.getPicName()));
		ivImage.setTag(position);
		ivImage.setOnClickListener(listener);
		vClickMask.setTag(position);
		vClickMask.setOnClickListener(listener);
		container.addView(view);
		return view;
	}

	private OnClickListener listener = new OnClickListener() {
		public void onClick(View v) {
			int position = (int) v.getTag();
			switch (v.getId()) {
			case R.id.iv_image:
				Intent intent = new Intent();
				intent.setClass(context, TopicSpecifiedListActivity.class);
				intent.putExtra("topicName", list.get(position).getTagName());
				BaseActivity.sGotoActivity(context, intent);
				break;
			case R.id.v_click_mask:
				Matcher matcherUrl = PATTERN_URL.matcher(list.get(position).getWebUrl());
				if (matcherUrl.find()) {
					Uri uri = Uri.parse(matcherUrl.group(1));
					Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
					BaseActivity.sGotoActivity(context, webIntent);
				}
				break;
			}
		}
	};



}
