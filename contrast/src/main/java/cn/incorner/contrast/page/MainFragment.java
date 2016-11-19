package cn.incorner.contrast.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.BaseFragment;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.Constant;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.adapter.BannerPagerAdapter;
import cn.incorner.contrast.data.adapter.ContrastListAdapter;
import cn.incorner.contrast.data.entity.BannerEntity;
import cn.incorner.contrast.data.entity.BannerResultEntity;
import cn.incorner.contrast.data.entity.CommentResultEntity;
import cn.incorner.contrast.data.entity.FollowerEntity;
import cn.incorner.contrast.data.entity.ParagraphCommentEntity;
import cn.incorner.contrast.data.entity.ParagraphEntity;
import cn.incorner.contrast.data.entity.ParagraphResultEntity;
import cn.incorner.contrast.util.CommonUtil;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.DensityUtil;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.util.QDataModule;
import cn.incorner.contrast.util.TT;
import cn.incorner.contrast.view.CustomRefreshFramework;
import cn.incorner.contrast.view.CustomRefreshFramework.OnRefreshingListener;
import cn.incorner.contrast.view.RefreshingAnimationView.IRefreshingAnimationView;
import cn.incorner.contrast.view.ScrollListenerListView;

import com.alibaba.fastjson.JSON;
import com.umeng.socialize.UMShareAPI;

/**
 * 主页面第一个fragment
 * 
 * @author yeshimin
 */
@ContentView(R.layout.frag_main)
public class MainFragment extends BaseFragment implements OnClickListener {

	private static final String TAG = "MainActivity.MainFragment";

	@ViewInject(R.id.crl_container)
	private CustomRefreshFramework crlContainer;
	@ViewInject(R.id.lv_content)
	private ScrollListenerListView lvContent;

	//添加关注的布局
	@ViewInject(R.id.tv_main_mine)
	private TextView tv_MainMine;

	// banner相关
	private RelativeLayout rlBannerContainer;
	private ViewPager vpContent;
	private LinearLayout llIndicator;
	private List<BannerEntity> listBanner = new ArrayList<BannerEntity>();
	private List<BannerEntity> listOriginBanner = new ArrayList<BannerEntity>();
	private BannerPagerAdapter bannerPagerAdapter;

	private MainActivity activity;
	private ArrayList<ParagraphEntity> listParagraph = new ArrayList<ParagraphEntity>();
	private ContrastListAdapter adapter;

	private String currentTag = Config.ALL_FRIEND;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		init();
		loadData();
	}

	private void init() {
		DD.d(TAG, "init()");

		activity = (MainActivity) getActivity();

		View vHeader = getLayoutInflater().inflate(
				R.layout.view_custom_refresh_framework_ex_header, null);
		// banner相关
		initBanner(vHeader);
		lvContent.addHeaderView(vHeader);
		adapter = new ContrastListAdapter(listParagraph, getLayoutInflater(),
				new OnContrastListItemClickListener());
		lvContent.setAdapter(adapter);

		// 初始化刷新框架
		crlContainer.setOnRefreshingListener(new OnRefreshingListener() {
			@Override
			public void onRefreshing() {
				refreshParagraphByTag(currentTag);
			}

			@Override
			public void onLoadMore() {
				loadMoreParagraphByTag(currentTag);
			}

			@Override
			public void onRefreshFinish() {
				adapter.notifyDataSetChanged();
				((IRefreshingAnimationView) getActivity()).hideRefreshingAnimationView();
			}
		});
		crlContainer.setOnTouchMoveListener((MainActivity) getActivity());

		getView().getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				DD.d("tag", "onGlobalLayout()");
				getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
				crlContainer.setHeaderContainerHeight(activity.getTopContainerHeight());

				// 设置banner高度
				setBannerHeight();
				// 默认隐藏banner
				hideBanner();
			}
		});

		// TODO: 2016/8/30 by xuzhichao 
		//当点击 添加关注,跳转到推荐页面
		tv_MainMine.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(getContext(),RecomendActivity.class);
				getContext().startActivity(intent);
			}
		});
	}

	// banner相关 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**
	 * 初始化banner
	 */
	private void initBanner(View vParent) {
		DD.d(TAG, "initBanner()");

		rlBannerContainer = (RelativeLayout) vParent.findViewById(R.id.rl_banner_container);
		llIndicator = (LinearLayout) vParent.findViewById(R.id.ll_indicator);
		vpContent = (ViewPager) vParent.findViewById(R.id.vp_content);
		bannerPagerAdapter = new BannerPagerAdapter(getActivity(), listBanner);
		vpContent.setAdapter(bannerPagerAdapter);
		vpContent.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int state) {
				DD.d(TAG, "onPageScrollStateChanged(), state: " + state);

				// 当滚动状态结束时，才进行setCurrentItem
				if (state == 0) {
					int size = listBanner.size();
					int position = vpContent.getCurrentItem();
					if (position == 0) {
						vpContent.setCurrentItem(size - 2, false);
					} else if (position == (size - 1)) {
						vpContent.setCurrentItem(1, false);
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int position) {
				int pos = position;
				if (position == 0) {
					pos = listBanner.size() - 2;
				} else if (position == (listBanner.size() - 1)) {
					pos = 1;
				}
				// 调整pos与listOriginBanner对应
				--pos;
				setBannerIndicator(pos);
			}
		});

	}

	/**
	 * 设置banner高度
	 */
	private void setBannerHeight() {
		DD.d(TAG, "setBannerHeight()");

		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rlBannerContainer
				.getLayoutParams();
		params.height = calcBannerHeight();
		rlBannerContainer.setLayoutParams(params);
	}

	/**
	 * 计算banner高度
	 */
	private int calcBannerHeight() {
		// 图片的宽高是 750x350
		return (int) (Constant.SCREEN_WIDTH / 750f * 350f);
	}

	/**
	 * 显示banner视图
	 */
	private void showBanner() {
		rlBannerContainer.setVisibility(View.VISIBLE);
	}

	/**
	 * 隐藏banner视图
	 */
	private void hideBanner() {
		rlBannerContainer.setVisibility(View.GONE);
	}

	/**
	 * banner排序
	 */
	private void sortBanner() {
		Collections.sort(listBanner);
	}

	/**
	 * 设置用于无限循环
	 */
	private void setForInfiniteLoop() {
		if (listBanner.size() < 2) {
			return;
		}
		listOriginBanner.clear();
		for (BannerEntity entity : listBanner) {
			listOriginBanner.add(entity);
		}
		BannerEntity firstItem = listBanner.get(0);
		BannerEntity lastItem = listBanner.get(listBanner.size() - 1);
		listBanner.add(0, lastItem);
		listBanner.add(firstItem);
	}

	/**
	 * 初始化banner指示器
	 */
	private void initBannerIndicator() {
		int size = listOriginBanner.size();
		if (size < 2) {
			llIndicator.setVisibility(View.GONE);
			return;
		}
		llIndicator.removeAllViews();
		for (int i = 0; i < size; ++i) {
			ImageView ivIndicator = new ImageView(getActivity());
			ivIndicator.setImageResource(R.drawable.shape__oval__banner_indicator_unselected);
			llIndicator.addView(ivIndicator);

			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ivIndicator
					.getLayoutParams();
			params.width = DensityUtil.dip2px(getActivity(), 8);
			params.height = DensityUtil.dip2px(getActivity(), 8);
			params.leftMargin = DensityUtil.dip2px(getActivity(), 3);
			params.rightMargin = DensityUtil.dip2px(getActivity(), 3);
			ivIndicator.setLayoutParams(params);
		}
	}

	/**
	 * 设置banner指示器
	 */
	private void setBannerIndicator(int position) {
		if (position < 0 || position >= listOriginBanner.size()) {
			return;
		}
		int size = listOriginBanner.size();
		for (int i = 0; i < size; ++i) {
			ImageView ivIndicator = (ImageView) llIndicator.getChildAt(i);
			if (i == position) {
				ivIndicator.setImageResource(R.drawable.shape__oval__banner_indicator_selected);
			} else {
				ivIndicator.setImageResource(R.drawable.shape__oval__banner_indicator_unselected);
			}
		}
	}

	/**
	 * 设置自动轮播
	 */
	private void startAutoScroll() {
		cancelAutoScroll();
		handler.postDelayed(rAutoScroll, Config.BANNER_INTERVAL);
	}

	/**
	 * 取消自动轮播
	 */
	private void cancelAutoScroll() {
		handler.removeCallbacks(rAutoScroll);
	}

	private void autoScroll() {
		if (listOriginBanner.size() < 2) {
			return;
		}
		vpContent.setCurrentItem(vpContent.getCurrentItem() + 1, true);
	}

	private Handler handler = new Handler();
	private Runnable rAutoScroll = new Runnable() {
		public void run() {
			handler.postDelayed(rAutoScroll, Config.BANNER_INTERVAL);
			autoScroll();
		}
	};

	// banner相关 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	/**
	 * 滚动到顶部
	 */
	public void scrollTop() {
		if (lvContent != null && lvContent.getAdapter() != null) {
			lvContent.setSelection(0);
		}
	}

	/**
	 * 从服务器刷新加载对比度列表
	 */
	private void refreshParagraphFromServer(RequestParams params) {
		DD.d(TAG, "refreshParagraphFromServer()");

		// 先从params中把值取出来，不然进行请求后，params会被清空
		final String tag = params.getStringParameter("tag");
		x.http().post(params, new CommonCallback<JSONObject>() {
			@Override
			public void onCancelled(CancelledException arg0) {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
			}

			@Override
			public void onFinished() {
				DD.d(TAG, "onFinished()");
				crlContainer.setRefreshing(false);
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess()");

				parseRefreshData(result, tag);
			}
		});
	}

	/**
	 * 解析刷新的对比度数据
	 */
	private void parseRefreshData(JSONObject result, String tag) {
		DD.d(TAG, "parseRefreshData(), result: " + result.toString() + ", tag: " + tag);
		// DD.d(TAG, "parseRefreshData()");

		ParagraphResultEntity entity = JSON.parseObject(result.toString(),
				ParagraphResultEntity.class);
		if ("0".equals(entity.getStatus())) {
			listParagraph.clear();
			listParagraph.addAll(entity.getParagraphs());
		}
	}

	/**
	 * 从服务器加载更多
	 */
	private void loadMoreParagraphFromServer(RequestParams params) {
		DD.d(TAG, "loadMoreParagraphFromServer()");

		// 先从params中把值取出来，不然进行请求后，params会被清空
		final String tag = params.getStringParameter("tag");
		x.http().post(params, new CommonCallback<JSONObject>() {
			@Override
			public void onCancelled(CancelledException arg0) {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
			}

			@Override
			public void onFinished() {
				crlContainer.setRefreshing(false);
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess()");

				parseLoadMoreData(result, tag);
			}
		});
	}

	/**
	 * 解析 加载更多 的对比度数据
	 */
	private void parseLoadMoreData(JSONObject result, String tag) {
		DD.d(TAG, "parseLoadMoreData(), result: " + result.toString() + ", tag: " + tag);

		ParagraphResultEntity entity = JSON.parseObject(result.toString(),
				ParagraphResultEntity.class);
		if ("0".equals(entity.getStatus()) && !entity.getParagraphs().isEmpty()) {
			listParagraph.addAll(entity.getParagraphs());
		}
	}

	private void loadData() {
		// 默认加载 精选
		onSelectedClick();
		activity.setMainNavSelector(R.id.tv_selected);
	}

	/**
	 * 加载banner数据
	 */
	private void loadBanner() {
		DD.d(TAG, "loadBanner()");

		RequestParams params = new RequestParams(Config.PATH_GET_BANNER);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		x.http().post(params, new CommonCallback<JSONObject>() {
			@Override
			public void onCancelled(CancelledException arg0) {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
			}

			@Override
			public void onFinished() {
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result.toString());

				// 显示banner
				showBanner();

				BannerResultEntity entity = JSON.parseObject(result.toString(),
						BannerResultEntity.class);
				if ("0".equals(entity.getStatus())) {
					listBanner.clear();
					listBanner.addAll(entity.getBanners());
					sortBanner();
					setForInfiniteLoop();
					initBannerIndicator();
					bannerPagerAdapter.notifyDataSetChanged();
					vpContent.setCurrentItem(1, false);
					// 开始自动轮播
					startAutoScroll();
				}
			}
		});
	}

	/**
	 * 点击大图
	 */
	private void onImageClick(int position) {
		DD.d(TAG, "onImageClick(), position: " + position);

		Intent intent = new Intent();
		intent.setClass(getActivity(), ContrastCommentActivity.class);
		intent.putExtra("paragraph", listParagraph.get(position));
		BaseActivity.sGotoActivity(getActivity(), intent);
	}

	/**
	 * 点击小头像
	 */
	private void onHeadClick(int position) {
		DD.d(TAG, "onHeadClick(), position: " + position);

		Intent intent = new Intent();
		intent.setClass(getActivity(), MyFollowingUserParagraphActivity.class);
		ParagraphEntity entity = listParagraph.get(position);
		intent.putExtra("userId", entity.getUserId());
		intent.putExtra("head", entity.getUserAvatarName());
		intent.putExtra("nickname", entity.getUserNickname());
		BaseActivity.sGotoActivity(getActivity(), intent);
	}

	/**
	 * 获取tag类型
	 */
	private int getTypeByTag(String tag) {
		DD.d(TAG, "getTypeByTag(), tag: " + tag);

		if (Config.SELECTED_TAG.equals(tag)) {
			return 1;
		} else if (Config.ALL_FRIEND.equals(tag)) {
			return 2;
		} else if (Config.SELECTED_RICHINCONTENT.equals(tag)) {
			return 3;
		} else {
			return 0;
		}
	}

	/**
	 * 当点击 有趣
	 */
	private void onSelectedClick() {
		DD.d(TAG, "onSelectedClick()");
		QDataModule.getInstance().notifyOnFindGoneListener();
		// 隐藏banner数据
		hideBanner();
		/*if (listBanner.isEmpty()) {
			// 加载banner数据
			loadBanner();
		} else {
			showBanner();
		}*/

		// 滚动到顶部
		scrollTop();

		currentTag = Config.SELECTED_TAG;
		 refreshParagraphByTag(Config.SELECTED_TAG);
		crlContainer.refreshWithoutAnim();
	}
	
	/**
	 * 当点击 有料
	 */
	private void onRichInContentClick() {
		DD.d(TAG, "onRichInContentClick()");
		QDataModule.getInstance().notifyOnFindGoneListener();
		// 隐藏banner数据
		hideBanner();
		// 显示banner数据
		//showBanner();

		// 滚动到顶部
		scrollTop();

		currentTag = Config.SELECTED_RICHINCONTENT;
		 refreshParagraphByTag(Config.SELECTED_RICHINCONTENT);
		crlContainer.refreshWithoutAnim();
	}

	/**
	 * 当点击 关注
	 */
	private void onAllClick() {
		DD.d(TAG, "onAllClick()");
		QDataModule.getInstance().notifyOnFindGoneListener();
		// 隐藏banner数据
		hideBanner();

		// 滚动到顶部
		scrollTop();

		currentTag = Config.ALL_FRIEND;
		 refreshParagraphByTag(Config.ALL_FRIEND);
		crlContainer.refreshWithoutAnim();
	}

	/**
	 * 按标签刷新
	 */
	private void refreshParagraphByTag(String tag) {
		DD.d(TAG, "refreshParagraphByTag(), tag: " + tag);
		RequestParams params = null;
		if (getTypeByTag(tag) != 2) {
		    params = new RequestParams(Config.PATH_GET_PARAGRAPH_BY_TAG);
			params.setAsJsonContent(true);
			params.addParameter("accessToken",PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
			params.addParameter("type", getTypeByTag(tag));
			params.addParameter("tag", tag);
			params.addParameter("from", 0);
			params.addParameter("row", Config.LOAD_COUNT);
			params.addParameter("timestamp",CommonUtil.getDefaultFormatCurrentTime());
		}else{
			params = new RequestParams(Config.PATH_GET_FRIEND);
			params.setAsJsonContent(true);
			params.addParameter("accessToken",PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
			params.addParameter("tag", tag);
			params.addParameter("from", 0);
			params.addParameter("row", Config.LOAD_COUNT);
			params.addParameter("timestamp",CommonUtil.getDefaultFormatCurrentTime());
		}

		refreshParagraphFromServer(params);
	}

	/**
	 * 按标签加载更多
	 */
	private void loadMoreParagraphByTag(String tag) {
		DD.d(TAG, "loadMoreParagraphByTag(), tag: " + tag);

		RequestParams params = new RequestParams(Config.PATH_GET_PARAGRAPH_BY_TAG);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("type", getTypeByTag(tag));
		params.addParameter("tag", tag);
		params.addParameter("from", getRowFrom());
		params.addParameter("row", Config.LOAD_COUNT2);
		params.addParameter("timestamp", CommonUtil.getDefaultFormatCurrentTime());

		loadMoreParagraphFromServer(params);
	}

	/**
	 * 获取加载的起始位置
	 */
	private int getRowFrom() {
		DD.d(TAG, "getRowFrom(), list.size: " + listParagraph.size());

		return listParagraph.size();
	}

	/**
	 * 内容列表项内部视图点击监听类
	 */
	private class OnContrastListItemClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.ll_image_container:
			case R.id.iv_compatible_image:
				onImageClick((int) v.getTag());
				break;
			case R.id.civ_head:
				onHeadClick((int) v.getTag());
				break;
			}
		}
	}

	public void onPostSuccess() {
		onAllClick();
		activity.setMainNavSelector(R.id.tv_all);
	}

	@Override
	public void onClick(View v) {
		DD.d(TAG, "onClick()");

		switch (v.getId()) {
			//当点击 有趣
			case R.id.tv_selected:
			tv_MainMine.setVisibility(View.GONE);
			onSelectedClick();
			activity.setMainNavSelector(v.getId());
			break;
			//当点击 有料
		case R.id.tv_richincontent:
			tv_MainMine.setVisibility(View.GONE);
			onRichInContentClick();
			activity.setMainNavSelector(v.getId());
			break;

			// TODO: 2016/8/31 update by zhichao 
			//当点击 关注
		case R.id.tv_all:
			onAllClick();
			activity.setMainNavSelector(v.getId());
			/*if (listParagraph.isEmpty()) {
				TT.show(getContext(),"您的关注列表为空");
				tv_MainMine.setVisibility(View.VISIBLE);
			} else {
				tv_MainMine.setVisibility(View.GONE);
			}*/
			break;
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		if (currentTag == Config.SELECTED_TAG || !listBanner.isEmpty()) {
			startAutoScroll();
		}
	}

	@Override
	public void onStop() {
		cancelAutoScroll();
		super.onStop();
	}

	@Override
	public void onResume() {
		DD.d(TAG, "onResume()");

		if (adapter.getCount() > 0 && lvContent.getChildCount() > 0) {
			int position = lvContent.getFirstVisiblePosition();
			int childCount = lvContent.getChildCount();
			DD.d(TAG, "position: " + position);
			DD.d(TAG, "childCount: " + childCount);
			if (position == 0) {
				childCount--;
			} else {
				position--;
			}
			int[] arrPos = new int[childCount];
			for (int i = 0; i < childCount; ++i) {
				arrPos[i] = position + i;
			}

			// 因为可能同时显示多个item，所以要对每个item进行操作
			for (int i = 0; i < arrPos.length; ++i) {
				final ParagraphEntity paragraphEntity = listParagraph.get(arrPos[i]);
				String paragraphReplyId = paragraphEntity.getParagraphReplyId();

				RequestParams params = new RequestParams(
						Config.PATH_GET_COMMENTS_BY_PARAGRAPH_REPLY_IDS);
				params.setAsJsonContent(true);
				params.addParameter("accessToken",
						PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
				params.addParameter("paragraphReplyId", paragraphReplyId);
				params.addParameter("from", 0);
				params.addParameter("rows", Config.LOAD_COUNT * 50);
				params.addParameter("timestamp", CommonUtil.getDefaultFormatCurrentTime());
				x.http().post(params, new CommonCallback<JSONObject>() {
					@Override
					public void onCancelled(CancelledException arg0) {
					}

					@Override
					public void onError(Throwable arg0, boolean arg1) {
					}

					@Override
					public void onFinished() {
					}

					@Override
					public void onSuccess(JSONObject result) {
						DD.d(TAG, "onSuccess(), result: " + result);
						final CommentResultEntity entity = JSON.parseObject(result.toString(),
								CommentResultEntity.class);
						if ("0".equals(entity.getStatus())) {
							List<ParagraphCommentEntity> listComment = paragraphEntity
									.getComments();
							listComment.clear();
							listComment.addAll(entity.getComments());
							paragraphEntity.setCommentCount(listComment.size());
							adapter.notifyDataSetChanged();
						}
					}
				});
			}
		}

		super.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			UMShareAPI.get(getActivity()).onActivityResult(requestCode, resultCode, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
