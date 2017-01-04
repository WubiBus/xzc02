package cn.incorner.contrast.page;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.common.Callback;
import org.xutils.ex.DbException;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.incorner.contrast.BaseFragment;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.ParagraphEntity;
import cn.incorner.contrast.data.entity.ParagraphResultEntity;
import cn.incorner.contrast.data.entity.TopicEntity;
import cn.incorner.contrast.data.entity.UserInfoEntity;
import cn.incorner.contrast.util.CommonUtil;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.view.CircleImageView;

/**
 * Created by Chao on 2016/11/26.
 */
@ContentView(R.layout.frag_person_deatail)
public class PersonDetailFragment extends BaseFragment {

    private static final String TAG = "MainActivity.PersonDetailFragment";

    private ParagraphResultEntity myLikeParagraphResultEntity;
    private List<ParagraphEntity> listMyLikeParagraph = new ArrayList<ParagraphEntity>();

    private List<TopicEntity> listMyAllTopic = new ArrayList<TopicEntity>();

    @ViewInject(R.id.rl_back)
    private RelativeLayout rlBack;

    @ViewInject(R.id.civ_head)
    private CircleImageView civHead;

    @ViewInject(R.id.tv_nickname)
    private TextView tvNickname;

    @ViewInject(R.id.tv_contrast_amount)
    private TextView tvContrastAmount;

    @ViewInject(R.id.tv_follower_amount)
    private TextView tvFollowerAmount;

    @ViewInject(R.id.tv_score_amount)
    private TextView tvScoreAmount;

    @ViewInject(R.id.tv_born)
    private TextView tvBorn;

    @ViewInject(R.id.tv_gender)
    private TextView tvGender;

    @ViewInject(R.id.ll_contact)
    private LinearLayout llContact;

    @ViewInject(R.id.tv_contact)
    private TextView tvContact;

    @ViewInject(R.id.rl_guanzhu)
    private RelativeLayout llGuanZhu;

    @ViewInject(R.id.rl_dazuo)
    private RelativeLayout llDaZuo;

    @ViewInject(R.id.rl_xihuan)
    private RelativeLayout llXiHuan;

    @ViewInject(R.id.rl_fensi)
    private RelativeLayout llFenSi;

    @ViewInject(R.id.rl_huati)
    private RelativeLayout llHuaTi;

    @ViewInject(R.id.shezhi)
    private LinearLayout llSheZhi;

    @ViewInject(R.id.tv_guanzhucount)
    private TextView tvGuanzhuCount;

    @ViewInject(R.id.tv_dazuocount)
    private TextView tvDazuoCount;

    @ViewInject(R.id.tv_xihuancount)
    private TextView tvXihuanCount;

    @ViewInject(R.id.tv_fensicount)
    private TextView tvFensiCount;

    @ViewInject(R.id.tv_huaticount)
    private TextView tvHuatiCount;

    //消息
    @ViewInject(R.id.iv_msg)
    private ImageView ivMsg;

    // 用户信息数据
    private UserInfoEntity userInfoEntity;

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
        //先加载用户数据
        loadUserInfo();
        //从服务器刷新 我喜欢的
        refreshMyLikeFromServer();
        //从本地加载 我的话题
        loadAllMyTopicFromLocal();

        llGuanZhu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).switchToMineFragAndLoadSpecifiedTypeData(
                        MineFragment.MY_FOLLOWING_USER);
                MainActivity.LAST_PAGE = MineFragment.MY_FOLLOWING_USER;
            }
        });

        llDaZuo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).switchToMineFragAndLoadSpecifiedTypeData(
                        MineFragment.MY_PARAGRAPH);
                MainActivity.LAST_PAGE = MineFragment.MY_PARAGRAPH;
            }
        });

        llXiHuan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).switchToMineFragAndLoadSpecifiedTypeData(
                        MineFragment.MY_LIKE_PARAGRAPH);
                MainActivity.LAST_PAGE = MineFragment.MY_LIKE_PARAGRAPH;
            }
        });

        llFenSi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).switchToMineFragAndLoadSpecifiedTypeData(
                        MineFragment.MY_FOLLOWER_USER);
                MainActivity.LAST_PAGE = MineFragment.MY_FOLLOWER_USER;
            }
        });

        llHuaTi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).switchToMineFragAndLoadSpecifiedTypeData(
                        MineFragment.MY_ALL_TOPIC);
                MainActivity.LAST_PAGE = MineFragment.MY_ALL_TOPIC;
            }
        });

        llSheZhi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //跳转到设置页面
                //Intent intent = new Intent();
                //intent.setClass(getContext(),MainActivity.class);
                //int haha = 1;
                //intent.putExtra("haha","设置");
                //getContext().startActivity(intent);
                ((MainActivity) getActivity()).onSettingClick();
            }
        });

        ivMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getContext(), MessageActivity.class);
                getContext().startActivity(intent);
            }
        });

        //点击头像
        civHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转到设置页面
                ((MainActivity) getActivity()).onSettingClick();
            }
        });

        // 如果上次页面停留在以下相应页面，则此次切换进行恢复
        switch (MainActivity.LAST_PAGE) {
            case MineFragment.MY_FOLLOWING_USER: {
                llGuanZhu.performClick();
                MainActivity.LAST_PAGE = MineFragment.MY_FOLLOWING_USER;
            }
            break;
            case MineFragment.MY_PARAGRAPH: {
                llDaZuo.performClick();
                MainActivity.LAST_PAGE = MineFragment.MY_PARAGRAPH;
            }
            break;
            case MineFragment.MY_LIKE_PARAGRAPH: {
                llXiHuan.performClick();
                MainActivity.LAST_PAGE = MineFragment.MY_LIKE_PARAGRAPH;
            }
            break;
            case MineFragment.MY_FOLLOWER_USER: {
                llFenSi.performClick();
                MainActivity.LAST_PAGE = MineFragment.MY_FOLLOWER_USER;
            }
            break;
            case MineFragment.MY_ALL_TOPIC: {
                llHuaTi.performClick();
                MainActivity.LAST_PAGE = MineFragment.MY_ALL_TOPIC;
            }
            break;
        }
    }

    /**
     * 加载用户数据
     */
    private void loadUserInfo() {

        DD.d(TAG, "loadUserInfo()");

        RequestParams params = new RequestParams(Config.PATH_GET_INFO);
        params.setAsJsonContent(true);
        params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
        x.http().post(params, new Callback.CommonCallback<JSONObject>() {
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

                userInfoEntity = JSON.parseObject(result.toString(), UserInfoEntity.class);

                PrefUtil.setStringValue(Config.PREF_NICKNAME, userInfoEntity.getNickname());
                PrefUtil.setStringValue(Config.PREF_USER_SIGNATURE,
                        userInfoEntity.getUserSignature());
                PrefUtil.setStringValue(Config.PREF_JOB_TITLE, userInfoEntity.getJobTitle());
                PrefUtil.setStringValue(Config.PREF_BIRTHDAY, userInfoEntity.getBirthday());
                PrefUtil.setIntValue(Config.PREF_USER_SEX, userInfoEntity.getUserSex());
                PrefUtil.setStringValue(Config.PREF_EMAIL, userInfoEntity.getEmail());
                PrefUtil.setIntValue(Config.PREF_FOLLOW_COUNT, userInfoEntity.getFollowCount());
                PrefUtil.setIntValue(Config.PREF_FRIEND_COUNT, userInfoEntity.getFriendCount());
                PrefUtil.setIntValue(Config.PREF_FOLLOWER_COUNT, userInfoEntity.getFollowerCount());
                PrefUtil.setIntValue(Config.PREF_PARAGRAPH_COUNT,
                        userInfoEntity.getParagraphCount());
                PrefUtil.setStringValue(Config.PREF_AVATAR_NAME, userInfoEntity.getAvatarName());
                PrefUtil.setIntValue(Config.PREF_SCORE, userInfoEntity.getScore());
                PrefUtil.setIntValue(Config.PREF_IS_ANONYMOUS, userInfoEntity.getIsAnonymous());

                // 加载大小头像
                x.image().loadDrawable(Config.getHeadFullPath(userInfoEntity.getAvatarName()),
                        null, new CommonCallback<Drawable>() {
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
                            public void onSuccess(Drawable drawable) {
                                DD.d(TAG, "onSuccess()");
                                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                                civHead.setImageBitmap(bitmap);
                                //civBigHead.setImageBitmap(bitmap);
                            }
                        });
                tvNickname.setText(userInfoEntity.getNickname());
                tvBorn.setText(CommonUtil.getShortFormatDateString(userInfoEntity.getBirthday()));
                tvGender.setText(Config.getGender(userInfoEntity.getUserSex()));
                tvContact.setText(userInfoEntity.getEmail());
                tvContrastAmount.setText(String.valueOf(userInfoEntity.getParagraphCount()) + "幅大作");
                tvFollowerAmount.setText(String.valueOf(userInfoEntity.getFollowerCount()) + "关注");
                tvScoreAmount.setText("积分" + String.valueOf(userInfoEntity.getScore()));

                //关注
                tvGuanzhuCount.setText(String.valueOf(userInfoEntity.getFollowCount()));
                //大作
                tvDazuoCount.setText(String.valueOf(userInfoEntity.getParagraphCount()));
                //粉丝
                tvFensiCount.setText(String.valueOf(userInfoEntity.getFollowerCount()));
                // loadCircleData(userInfoEntity);
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
        x.http().post(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
            }

            @Override
            public void onFinished() {
                // crlContainer.setRefreshing(false);
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
                //喜欢
                tvXihuanCount.setText(String.valueOf(listMyLikeParagraph.size()));
            }
        });
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
            //crlContainer.setRefreshing(false);
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

        List<TopicEntity>
                listTopic = new ArrayList<TopicEntity>();
        for (Map.Entry<String, Integer> entry : mapTopic.entrySet()) {
            TopicEntity topic = new TopicEntity();
            topic.setTopicName(entry.getKey());
            topic.setTopicCount(entry.getValue());
            listTopic.add(topic);
        }
        listMyAllTopic.clear();
        listMyAllTopic.addAll(listTopic);
        //crlContainer.setRefreshing(false);

        //话题
        tvHuatiCount.setText(String.valueOf(listMyAllTopic.size()));
    }

}
