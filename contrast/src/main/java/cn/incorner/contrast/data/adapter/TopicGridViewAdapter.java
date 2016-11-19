package cn.incorner.contrast.data.adapter;

import java.util.List;
import java.util.Random;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.TopicEntity;
import cn.incorner.contrast.view.SquareLinearLayout;

/**
 * 话题GridView适配器
 * 
 * @author yeshimin
 */
public class TopicGridViewAdapter extends BaseAdapter {

	private List<TopicEntity> list;
	private LayoutInflater inflater;
	private Random random = new Random();

	public TopicGridViewAdapter(List<TopicEntity> list, LayoutInflater inflater) {
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
			convertView = inflater.inflate(R.layout.item_topic_grid_view, null);
			holder = new ViewHolder();
			holder.llTopicContainer = (SquareLinearLayout) convertView
					.findViewById(R.id.ll_topic_container);
			holder.tvTopicName = (TextView) convertView.findViewById(R.id.tv_topic_name);
			holder.tvTopicCount = (TextView) convertView.findViewById(R.id.tv_topic_count);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		TopicEntity entity = list.get(position);

		int colorIndex = random.nextInt(99) % 20;
		int bgColor = BaseActivity.BG_COLOR[colorIndex];
		int textColor = BaseActivity.TEXT_COLOR[colorIndex];
		bgColor = Color.argb((int) (Color.alpha(bgColor) * 0.05), Color.red(bgColor),
				Color.green(bgColor), Color.blue(bgColor));

		holder.llTopicContainer.setBackgroundColor(bgColor);
//		holder.tvTopicName.setTextColor(textColor);
//		holder.tvTopicCount.setTextColor(textColor);

		//话题的名字
		holder.tvTopicName.setText(entity.getTopicName());
		//该话题作品的数量
		holder.tvTopicCount.setText(String.valueOf(entity.getTopicCount()));

		return convertView;
	}

	private class ViewHolder {
		private SquareLinearLayout llTopicContainer;
		private TextView tvTopicName;
		private TextView tvTopicCount;
	}

}
