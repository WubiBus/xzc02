package cn.incorner.contrast.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.ex.DbException;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.BaseFragment;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.adapter.ImageGridViewAdapter;
import cn.incorner.contrast.data.adapter.MyFollowerUserEntryListAdapter;
import cn.incorner.contrast.data.adapter.MyFollowingUserEntryListAdapter;
import cn.incorner.contrast.data.adapter.TopicGridViewAdapter;
import cn.incorner.contrast.data.entity.FollowerEntity;
import cn.incorner.contrast.data.entity.FollowerUserListEntity;
import cn.incorner.contrast.data.entity.FollowingEntity;
import cn.incorner.contrast.data.entity.FollowingUserListEntity;
import cn.incorner.contrast.data.entity.ParagraphCommentEntity;
import cn.incorner.contrast.data.entity.ParagraphEntity;
import cn.incorner.contrast.data.entity.ParagraphResultEntity;
import cn.incorner.contrast.data.entity.TopicEntity;
import cn.incorner.contrast.data.entity.TopicResultEntity;
import cn.incorner.contrast.util.CommonUtil;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.util.TT;
import cn.incorner.contrast.view.CustomRefreshFramework;
import cn.incorner.contrast.view.CustomRefreshFramework.OnRefreshingListener;
import cn.incorner.contrast.view.HeaderFooterGridView;
import cn.incorner.contrast.view.RefreshingAnimationView.IRefreshingAnimationView;

import com.alibaba.fastjson.JSON;

/**
 * 主页面第二个fragment
 * 
 * @author yeshimin
 */
@ContentView(R.layout.frag_mine)
public class MineFragment extends BaseFragment implements OnClickListener {

	private static final String TAG = "MainActivity.MineFragment";

	private static final int MY_PARAGRAPH = 1;
	private static final int MY_LIKE_PARAGRAPH = 2;
	private static final int MY_FOLLOWING_USER = 3;
	private static final int MY_ALL_TOPIC = 4;
	private static final int MY_FOLLOWER_USER = 5;

	@ViewInject(R.id.crl_container)
	private CustomRefreshFramework crlContainer;
	@ViewInject(R.id.gv_content)
	private HeaderFooterGridView gvContent;


	//添加关注的布局
	@ViewInject(R.id.tv_main_mine)
	private TextView tv_MainMine;

	private List<ParagraphEntity> listMyParagraph = new ArrayList<ParagraphEntity>();
	private List<ParagraphEntity> listMyLikeParagraph = new ArrayList<ParagraphEntity>();
	private List<FollowingEntity> listMyFollowingUser = new ArrayList<FollowingEntity>();
	private List<TopicEntity> listMyAllTopic = new ArrayList<TopicEntity>();
	private List<FollowerEntity> listMyFollowerUser = new ArrayList<FollowerEntity>();
	private ParagraphResultEntity myParagraphResultEntity;
	private ParagraphResultEntity myLikeParagraphResultEntity;
	private FollowingUserListEntity myFollowingUserListResultEntity;
	private TopicResultEntity myAllTopicResultEntity;
	private FollowerUserListEntity myFollowerUserListResultEntity;
	private ImageGridViewAdapter myParagraphAdapter;
	private ImageGridViewAdapter myLikeParagraphAdapter;
	private MyFollowingUserEntryListAdapter myFollowingUserAdapter;
	private TopicGridViewAdapter myAllTopicAdapter;
	private MyFollowerUserEntryListAdapter myFollowerUserAdapter;

	//进来默认 当前页面
	private int currentPage = MY_FOLLOWING_USER;
	private MainActivity activity;

	//int strKey = getArguments().getInt("key");
	//private int currentPage = strKey;

	DbManager.DaoConfig daoConfig = new DbManager.DaoConfig().setDbName("my_paragraph.db")
			.setDbVersion(1).setDbOpenListener(new DbManager.DbOpenListener() {
				@Override
				public void onDbOpened(DbManager db) {
					// 开启WAL, 对写入加速提升巨大
					db.getDatabase().enableWriteAheadLogging();
				}
			}).setDbUpgradeListener(new DbManager.DbUpgradeListener() {
				@Override
				public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
				}
			});

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		init();
	}

	private void init() {
		DD.d(TAG, "init()");

		activity = (MainActivity) getActivity();

		View vHeader = getLayoutInflater().inflate(R.layout.view_custom_refresh_framework_header,
				null);
		gvContent.addHeaderView(vHeader);
		myParagraphAdapter = new ImageGridViewAdapter(listMyParagraph, getLayoutInflater());
		myLikeParagraphAdapter = new ImageGridViewAdapter(listMyLikeParagraph, getLayoutInflater());
		myFollowingUserAdapter = new MyFollowingUserEntryListAdapter(listMyFollowingUser,
				getLayoutInflater(),getActivity());
		myAllTopicAdapter = new TopicGridViewAdapter(listMyAllTopic, getLayoutInflater());
		myFollowerUserAdapter = new MyFollowerUserEntryListAdapter(listMyFollowerUser,
				getLayoutInflater(),getActivity());
		// 默认页面的数据适配器
		gvContent.setAdapter(myFollowingUserAdapter);

		int strKey = getArguments().getInt("key");
		if (strKey != 0) {
			currentPage = strKey;
		} else {
			currentPage = MY_FOLLOWING_USER;
		}

		// 初始化刷新框架
		crlContainer.setOnRefreshingListener(new OnRefreshingListener() {
			@Override
			public void onRefreshing() {
				refreshDataByFlag(currentPage);
			}

			@Override
			public void onLoadMore() {
				loadMoreDataByFlag(currentPage);
			}

			@Override
			public void onRefreshFinish() {
				switch (currentPage) {
				case 1:
					String text1 = "我的大作";
					//onMyParagraphClick();
					//pageName.setVisibility(View.VISIBLE);
					activity.updateName(text1);
					activity.showPageName();
					activity.showBack();
					gvContent.setAdapter(myParagraphAdapter);
					if (listMyParagraph.size() > 0) {
						myParagraphAdapter.notifyDataSetChanged();
					}
					((IRefreshingAnimationView) getActivity()).hideRefreshingAnimationView();
					break;
				case 2:
					String text2 = "我喜欢的";
					activity.updateName(text2);
					activity.showPageName();
					activity.showBack();
					//onMyLikeClick();
					//pageName.setVisibility(View.VISIBLE);
					gvContent.setAdapter(myLikeParagraphAdapter);
					myLikeParagraphAdapter.notifyDataSetChanged();
					break;
				case 3:
					String text3 = "我的关注";
					activity.updateName(text3);
					activity.showPageName();
					activity.showBack();
					//onMyFollowingUserClick();
					//pageName.setVisibility(View.VISIBLE);
					gvContent.setAdapter(myFollowingUserAdapter);
					myFollowingUserAdapter.notifyDataSetChanged();
					break;
				case 4:
					String text4 = "我的话题";
					activity.updateName(text4);
					activity.showPageName();
					activity.showBack();
					//onMyAllTopicClick();
					//pageName.setVisibility(View.VISIBLE);
					gvContent.setAdapter(myAllTopicAdapter);
					myAllTopicAdapter.notifyDataSetChanged();
					break;
				case 5:
					String text5 = "我的粉丝";
					activity.updateName(text5);
					activity.showPageName();
					activity.showBack();
					//onMyFollowerUserClick();
					//pageName.setVisibility(View.VISIBLE);
					gvContent.setAdapter(myFollowerUserAdapter);
					myFollowerUserAdapter.notifyDataSetChanged();
					break;
				}
			}
		});
		crlContainer.setOnTouchMoveListener((MainActivity) getActivity());
		getView().getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
				crlContainer.setHeaderContainerHeight(activity.getTopContainerHeight());

				loadData();
			}
		});

		// TODO: 2016/9/6 下次
		//当点击 添加关注,跳转到推荐页面
		/*tv_MainMine.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(getContext(),RecomendActivity.class);
				getContext().startActivity(intent);
			}
		});*/



	}



	/**
	 * 滚动到顶部
	 */
	public void scrollTop() {
		if (gvContent != null && gvContent.getAdapter() != null) {
			gvContent.setSelection(0);
		}
	}

	private void loadData() {
		DD.d(TAG, "loadData()");

		crlContainer.refreshWithoutAnim();
		activity.setMineNavSelector(R.id.tv_my_following_user);
	}

	/**
	 * 从本地加载 我的作品
	 */
	private void loadMyParagraphFromLocal() {
		DD.d(TAG, "loadMyParagraphFromLocal()");

		DbManager db = x.getDb(daoConfig);
		List<ParagraphEntity> list = null;
		try {
			list = db.selector(ParagraphEntity.class)
					.where("create_user_id", "=", PrefUtil.getIntValue(Config.PREF_USER_ID))
					.findAll();
		} catch (DbException e) {
			e.printStackTrace();
		}
		if (list != null && !list.isEmpty()) {
			listMyParagraph.addAll(list);
			myParagraphAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 根据当前页面标识刷新数据
	 */
	private void refreshDataByFlag(int flag) {
		DD.d(TAG, "refreshDataByFlag(), flag: " + flag);

		switch (flag) {
		case MY_PARAGRAPH:
			refreshMyParagraphFromServer();
			break;
		case MY_LIKE_PARAGRAPH:
			refreshMyLikeFromServer();
			break;
		case MY_FOLLOWING_USER:
			refreshMyFollowingFromServer();
			//并判断 关注的人 的列表是否为空
			/*if (listMyFollowingUser.isEmpty()) {
				TT.show(getContext(),"您的关注列表为空");
				tv_MainMine.setVisibility(View.VISIBLE);
			} else {
				tv_MainMine.setVisibility(View.GONE);
			}*/
			break;
		case MY_ALL_TOPIC:
			loadAllMyTopicFromLocal();
			break;
		case MY_FOLLOWER_USER:
			refreshMyFollowerFromServer();
			break;
		}
	}

	/**
	 * 从本地加载所有我的话题
	 */
	private void loadAllMyTopicFromLocal() {
		DD.d(TAG, "loadAllMyTopicFromLocal()");

		List<ParagraphEntity> listAll = null;
		DbManager db = x.getDb(daoConfig);
		try {
			listAll = db.selector(ParagraphEntity.class)
					.where("create_user_id", "=", PrefUtil.getIntValue(Config.PREF_USER_ID))
					.findAll();
		} catch (DbException e) {
			e.printStackTrace();
		}
		if (listAll == null || listAll.isEmpty()) {
			crlContainer.setRefreshing(false);
			return;
		}

		final Map<String, Integer> mapTopic = new HashMap<String, Integer>();
		final Pattern pattern = Pattern.compile(Config.PATTERN_TAG_3);
		for (final ParagraphEntity entity : listAll) {
			final Matcher matcher = pattern.matcher(entity.getTags());
			while (matcher.find()) {
				final String tag = matcher.group(1);
				Integer i = mapTopic.get(tag);
				if (i == null) {
					mapTopic.put(tag, Integer.valueOf(1));
				} else {
					mapTopic.put(tag, ++i);
				}
			}
		}

		List<TopicEntity> listTopic = new ArrayList<TopicEntity>();
		for (Map.Entry<String, Integer> entry : mapTopic.entrySet()) {
			TopicEntity topic = new TopicEntity();
			topic.setTopicName(entry.getKey());
			topic.setTopicCount(entry.getValue());
			listTopic.add(topic);
		}
		listMyAllTopic.clear();
		listMyAllTopic.addAll(listTopic);
		crlContainer.setRefreshing(false);
	}

	/**
	 * 从服务器刷新 我发布的对比度列表
	 */
	private void refreshMyParagraphFromServer() {
		DD.d(TAG, "refreshMyParagraphFromServer()");

		RequestParams params = new RequestParams(Config.PATH_GET_PARAGRAPH_BY_USER_ID);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("userId", PrefUtil.getIntValue(Config.PREF_USER_ID));
		params.addParameter("from", 0);
		params.addParameter("row", 50);
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
				crlContainer.setRefreshing(false);
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result.toString());

				myParagraphResultEntity = JSON.parseObject(result.toString(),
						ParagraphResultEntity.class);
				String status = myParagraphResultEntity.getStatus();
				List<ParagraphEntity> list = myParagraphResultEntity.getParagraphs();

				DD.d(TAG, "status: " + status);
				if ("0".equals(status) && list != null && !list.isEmpty()) {
					listMyParagraph.clear();
					listMyParagraph.addAll(list);

					DbManager db = x.getDb(daoConfig);
					try {
						db.delete(ParagraphEntity.class);
						db.saveOrUpdate(list);
						for (ParagraphEntity entity : list) {
							String paragraphId = entity.getParagraphId();
							List<ParagraphCommentEntity> listComment = entity.getComments();
							for (ParagraphCommentEntity comment : listComment) {
								comment.setExParagraphId(paragraphId);
							}
							db.saveOrUpdate(listComment);
						}
					} catch (DbException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * 从服务器刷新 我喜欢的对比度列表
	 */
	private void refreshMyLikeFromServer() {
		DD.d(TAG, "refreshMyLikeFromServer()");

		RequestParams params = new RequestParams(Config.PATH_GET_LIKE_PARAGRAPH);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("from", 0);
		params.addParameter("row", 50);
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
				crlContainer.setRefreshing(false);
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result.toString());

				myLikeParagraphResultEntity = JSON.parseObject(result.toString(),
						ParagraphResultEntity.class);
				String status = myLikeParagraphResultEntity.getStatus();
				List<ParagraphEntity> list = myLikeParagraphResultEntity.getParagraphs();

				if ("0".equals(status) && list != null && !list.isEmpty()) {
					listMyLikeParagraph.clear();
					listMyLikeParagraph.addAll(list);
				}
			}
		});
	}

	/**
	 * 从服务器刷新 我关注的人的列表
	 */
	private void refreshMyFollowingFromServer() {
		DD.d(TAG, "refreshMyFollowingFromServer()");

		RequestParams params = new RequestParams(Config.PATH_FOLLOW_LIST);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("userId", PrefUtil.getIntValue(Config.PREF_USER_ID));
		params.addParameter("from", 0);
		params.addParameter("rows", 50);
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
				crlContainer.setRefreshing(false);
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result);

				myFollowingUserListResultEntity = JSON.parseObject(result.toString(),
						FollowingUserListEntity.class);
				String status = myFollowingUserListResultEntity.getStatus();
				List<FollowingEntity> list = myFollowingUserListResultEntity.getFollowings();

				// TODO: 2016/9/1 update by zhichao 
				if ("0".equals(status) && list != null ) {
					listMyFollowingUser.clear();
					listMyFollowingUser.addAll(list);
					myFollowingUserAdapter.notifyDataSetChanged();
				}

			}
		});
	}

	/**
	 * 从服务器刷新 关注我的人的列表
	 */
	private void refreshMyFollowerFromServer() {
		DD.d(TAG, "refreshMyFollowerFromServer()");

		RequestParams params = new RequestParams(Config.PATH_FOLLOWER_LIST);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("userId", PrefUtil.getIntValue(Config.PREF_USER_ID));
		params.addParameter("from", 0);
		params.addParameter("rows", 50);
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
				crlContainer.setRefreshing(false);
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result);

				myFollowerUserListResultEntity = JSON.parseObject(result.toString(),
						FollowerUserListEntity.class);
				String status = myFollowerUserListResultEntity.getStatus();
				List<FollowerEntity> list = myFollowerUserListResultEntity.getFollowers();

				if ("0".equals(status) && list != null && !list.isEmpty()) {
					listMyFollowerUser.clear();
					listMyFollowerUser.addAll(list);
				}
			}
		});
	}

	/**
	 * 根据当前页面标识加载更多数据
	 */
	private void loadMoreDataByFlag(int flag) {
		DD.d(TAG, "loadMoreDataByFlag(), flag: " + flag);

		switch (flag) {
		case MY_PARAGRAPH:
			loadMoreMyParagraphFromServer();
			break;
		case MY_LIKE_PARAGRAPH:
			loadMoreMyLikeFromServer();
			break;
		case MY_FOLLOWING_USER:
			loadMoreMyFollowingFromServer();
			break;
		case MY_ALL_TOPIC:
			loadAllMyTopicFromLocal();
			break;
		case MY_FOLLOWER_USER:
			loadMoreMyFollowerFromServer();
			break;
		}
	}

	/**
	 * 从服务器加载更多 我发布的对比度列表
	 */
	private void loadMoreMyParagraphFromServer() {
		DD.d(TAG, "loadMoreMyParagraphFromServer()");

		RequestParams params = new RequestParams(Config.PATH_GET_PARAGRAPH_BY_USER_ID);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("userId", PrefUtil.getIntValue(Config.PREF_USER_ID));
		params.addParameter("from", getRowFrom(MY_PARAGRAPH));
		params.addParameter("row", 50);
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
				crlContainer.setRefreshing(false);
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result.toString());

				myParagraphResultEntity = JSON.parseObject(result.toString(),
						ParagraphResultEntity.class);
				String status = myParagraphResultEntity.getStatus();
				List<ParagraphEntity> list = myParagraphResultEntity.getParagraphs();

				if ("0".equals(status) && list != null && !list.isEmpty()) {
					listMyParagraph.addAll(list);

					DbManager db = x.getDb(daoConfig);
					try {
						db.saveOrUpdate(list);
						for (ParagraphEntity entity : list) {
							String paragraphId = entity.getParagraphId();
							List<ParagraphCommentEntity> listComment = entity.getComments();
							for (ParagraphCommentEntity comment : listComment) {
								comment.setExParagraphId(paragraphId);
							}
							db.saveOrUpdate(listComment);
						}
					} catch (DbException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * 从服务器加载更多 我喜欢的对比度列表
	 */
	private void loadMoreMyLikeFromServer() {
		DD.d(TAG, "loadMoreMyLikeFromServer()");

		RequestParams params = new RequestParams(Config.PATH_GET_LIKE_PARAGRAPH);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("from", getRowFrom(MY_LIKE_PARAGRAPH));
		params.addParameter("row", 50);
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
				crlContainer.setRefreshing(false);
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result.toString());

				myLikeParagraphResultEntity = JSON.parseObject(result.toString(),
						ParagraphResultEntity.class);
				String status = myLikeParagraphResultEntity.getStatus();
				List<ParagraphEntity> list = myLikeParagraphResultEntity.getParagraphs();

				if ("0".equals(status) && list != null && !list.isEmpty()) {
					listMyLikeParagraph.addAll(list);
				}
			}
		});
	}

	/**
	 * 从服务器加载更多 我关注的人的列表
	 */
	private void loadMoreMyFollowingFromServer() {
		DD.d(TAG, "loadMoreMyFollowingFromServer()");

		RequestParams params = new RequestParams(Config.PATH_FOLLOW_LIST);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("userId", PrefUtil.getIntValue(Config.PREF_USER_ID));
		params.addParameter("from", getRowFrom(MY_FOLLOWING_USER));
		params.addParameter("rows", 50);
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
				crlContainer.setRefreshing(false);
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result);

				myFollowingUserListResultEntity = JSON.parseObject(result.toString(),
						FollowingUserListEntity.class);
				String status = myFollowingUserListResultEntity.getStatus();
				List<FollowingEntity> list = myFollowingUserListResultEntity.getFollowings();

				if ("0".equals(status) && list != null && !list.isEmpty()) {
					listMyFollowingUser.addAll(list);
				}
			}
		});
	}

	/**
	 * 从服务器加载更多 关注我的人的列表
	 */
	private void loadMoreMyFollowerFromServer() {
		DD.d(TAG, "loadMoreMyFollowerFromServer()");

		RequestParams params = new RequestParams(Config.PATH_FOLLOW_LIST);
		params.setAsJsonContent(true);
		params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
		params.addParameter("userId", PrefUtil.getIntValue(Config.PREF_USER_ID));
		params.addParameter("from", getRowFrom(MY_FOLLOWER_USER));
		params.addParameter("rows", 50);
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
				crlContainer.setRefreshing(false);
			}

			@Override
			public void onSuccess(JSONObject result) {
				DD.d(TAG, "onSuccess(), result: " + result);

				myFollowerUserListResultEntity = JSON.parseObject(result.toString(),
						FollowerUserListEntity.class);
				String status = myFollowerUserListResultEntity.getStatus();
				List<FollowerEntity> list = myFollowerUserListResultEntity.getFollowers();

				if ("0".equals(status) && list != null && !list.isEmpty()) {
					listMyFollowerUser.addAll(list);
				}
			}
		});
	}

	/**
	 * 获取加载的起始位置
	 */
	private int getRowFrom(int flag) {
		DD.d(TAG, "getRowFrom(), flag: " + flag);

		int size = 0;
		switch (flag) {
		case MY_PARAGRAPH:
			size = listMyParagraph.size();
			break;
		case MY_LIKE_PARAGRAPH:
			size = listMyLikeParagraph.size();
			break;
		case MY_FOLLOWING_USER:
			size = listMyFollowingUser.size();
			break;
		case MY_ALL_TOPIC:
			// size = listMyFollowingTag.size(); TODO
			break;
		case MY_FOLLOWER_USER:
			size = listMyFollowerUser.size();
			break;
		}
		return size;
	}

	/**
	 * 点击了 我的大作
	 */
	private void onMyParagraphClick() {
		DD.d(TAG, "onMyParagraphClick()");

		if (currentPage == MY_PARAGRAPH) {
			myParagraphAdapter.notifyDataSetChanged();
		} else {
			gvContent.setAdapter(myParagraphAdapter);
		}
		currentPage = MY_PARAGRAPH;

		// 如果当前listMyparagraph没有数据，进行一次本地加载
		if (listMyParagraph.size() == 0) {
			loadMyParagraphFromLocal();
		}

		// refreshDataByFlag(MY_PARAGRAPH);
		crlContainer.refreshWithoutAnim();
	}

	/**
	 * 点击了 我喜欢的
	 */
	private void onMyLikeClick() {
		DD.d(TAG, "onMyLikeClick()");

		if (currentPage == MY_LIKE_PARAGRAPH) {
			myLikeParagraphAdapter.notifyDataSetChanged();
		} else {
			gvContent.setAdapter(myLikeParagraphAdapter);
		}
		currentPage = MY_LIKE_PARAGRAPH;
		// refreshDataByFlag(MY_LIKE_PARAGRAPH);
		crlContainer.refreshWithoutAnim();
	}

	/**
	 * 点击了 我的关注
	 */
	private void onMyFollowingUserClick() {
		DD.d(TAG, "onMyFollowingUserClick()");

		if (currentPage == MY_FOLLOWING_USER) {

			myFollowingUserAdapter.notifyDataSetChanged();

		} else {
			gvContent.setAdapter(myFollowingUserAdapter);
		}
		currentPage = MY_FOLLOWING_USER;
		// refreshDataByFlag(MY_FOLLOWING_USER);
		crlContainer.refreshWithoutAnim();
	}

	/**
	 * 点击了 我所有的话题
	 */
	private void onMyAllTopicClick() {
		DD.d(TAG, "onMyAllTopicClick()");

		if (currentPage == MY_ALL_TOPIC) {
			myAllTopicAdapter.notifyDataSetChanged();
		} else {
			gvContent.setAdapter(myAllTopicAdapter);
		}
		currentPage = MY_ALL_TOPIC;
		if (listMyAllTopic.size() > 0) {
			return;
		}
		// refreshDataByFlag(MY_ALL_TOPIC);
		crlContainer.refreshWithoutAnim();
	}

	/**
	 * 点击了 关注我的人
	 */
	private void onMyFollowerUserClick() {
		DD.d(TAG, "onMyFollowerUserClick()");

		if (currentPage == MY_FOLLOWER_USER) {
			myFollowerUserAdapter.notifyDataSetChanged();
		} else {
			gvContent.setAdapter(myFollowerUserAdapter);
		}
		currentPage = MY_FOLLOWER_USER;
		// refreshDataByFlag(MY_FOLLOWER_USER);
		crlContainer.refreshWithoutAnim();
	}

	public void onPostSuccess() {
		onMyParagraphClick();
		activity.setMineNavSelector(R.id.tv_my_paragraph);
	}

	@Override
	public void onClick(View v) {
		DD.d(TAG, "onClick()");

		switch (v.getId()) {
		case R.id.tv_my_paragraph:
			tv_MainMine.setVisibility(View.GONE);
			onMyParagraphClick();
			activity.setMineNavSelector(v.getId());
			break;
		case R.id.tv_my_like:
			tv_MainMine.setVisibility(View.GONE);
			onMyLikeClick();
			activity.setMineNavSelector(v.getId());
			break;
		case R.id.tv_my_following_user:
			// TODO: 2016/9/6 下次
			//并判断 关注的人 的列表是否为空
			/*if (listMyFollowingUser.isEmpty()) {
				TT.show(getContext(),"您的关注列表为空");
				tv_MainMine.setVisibility(View.VISIBLE);
			} else {
				tv_MainMine.setVisibility(View.GONE);
			}*/
			onMyFollowingUserClick();
			activity.setMineNavSelector(v.getId());
			break;
		case R.id.tv_my_all_topic:
			tv_MainMine.setVisibility(View.GONE);
			onMyAllTopicClick();
			activity.setMineNavSelector(v.getId());
			break;
		case R.id.tv_my_follower_user:
			tv_MainMine.setVisibility(View.GONE);
			onMyFollowerUserClick();
			activity.setMineNavSelector(v.getId());
			break;

		}
	}




	@Event(value = R.id.gv_content, type = AdapterView.OnItemClickListener.class)
	private void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		DD.d(TAG, "onItemClick(), position: " + position);

		Intent intent = new Intent();

		switch (currentPage) {
		case MY_PARAGRAPH:
			intent.setClass(getActivity(), MyParagraphActivity.class);
			intent.putExtra("position", position);
			intent.putParcelableArrayListExtra("list", (ArrayList<ParagraphEntity>) listMyParagraph);
			startActivityForResult(intent, MyParagraphActivity.REQUEST_CODE);
			return;
		case MY_LIKE_PARAGRAPH:
			intent.setClass(getActivity(), MyLikeActivity.class);
			intent.putExtra("position", position);
			intent.putParcelableArrayListExtra("list",
					(ArrayList<ParagraphEntity>) listMyLikeParagraph);
			break;
		case MY_FOLLOWING_USER:
			intent.setClass(getActivity(), MyFollowingUserParagraphActivity.class);
			FollowingEntity followingUser = listMyFollowingUser.get(position);
			intent.putExtra("userId", followingUser.getUserId());
			intent.putExtra("head", followingUser.getAvatarName());
			intent.putExtra("nickname", followingUser.getNickname());
			break;
		case MY_ALL_TOPIC:
			intent.setClass(getActivity(), TopicSpecifiedListActivity.class);
			intent.putExtra("topicName", listMyAllTopic.get(position).getTopicName());
			intent.putExtra("isUserData", true);
			break;
		case MY_FOLLOWER_USER:
			FollowerEntity followerUser = listMyFollowerUser.get(position);
			intent.setClass(getActivity(), MyFollowingUserParagraphActivity.class);
			intent.putExtra("userId", followerUser.getUserId());
			intent.putExtra("head", followerUser.getAvatarName());
			intent.putExtra("nickname", followerUser.getNickname());
			break;
		default:
			return;
		}
		BaseActivity.sGotoActivity(getActivity(), intent);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		DD.d(TAG, "onActivityResult()");

		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case MyParagraphActivity.REQUEST_CODE:
				List<ParagraphEntity> listResult = data.getParcelableArrayListExtra("list");

				// TODO: 2016/8/23 update by xuzhichao
				if (listResult != null ) {
					listMyParagraph.clear();
					listMyParagraph.addAll(listResult);
					myParagraphAdapter.notifyDataSetChanged();
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}



}
