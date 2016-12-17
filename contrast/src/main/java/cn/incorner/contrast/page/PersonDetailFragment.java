package cn.incorner.contrast.page;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import cn.incorner.contrast.BaseFragment;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.UserInfoEntity;
import cn.incorner.contrast.util.CommonUtil;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.view.CircleImageView;
import cn.incorner.contrast.view.CustomRefreshFramework;

/**
 * Created by Chao on 2016/11/26.
 */
@ContentView(R.layout.frag_person_deatail)
public class PersonDetailFragment extends BaseFragment {

    private static final String TAG = "MainActivity.PersonDetailFragment";

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

    @ViewInject(R.id.guanzhu)
    private LinearLayout llGuanZhu;

    @ViewInject(R.id.dazuo)
    private LinearLayout llDaZuo;

    @ViewInject(R.id.xihuan)
    private LinearLayout llXiHuan;

    @ViewInject(R.id.fensi)
    private LinearLayout llFenSi;

    @ViewInject(R.id.huati)
    private LinearLayout llHuaTi;

    @ViewInject(R.id.shezhi)
    private LinearLayout llSheZhi;

    // 用户信息数据
    private UserInfoEntity userInfoEntity;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();
    }

    private void init() {
        //先加载用户数据
        loadUserInfo();

        llGuanZhu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*getFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)  //将当前fragment加入到返回栈中
                        .replace(R.id.fl_container, new MineFragment()).commit();*/
                MineFragment mineFragment = new MineFragment();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.fl_container, mineFragment);
                ft.addToBackStack(null);
                Bundle bundle = new Bundle();
                bundle.putInt("key",3);
                mineFragment.setArguments(bundle);
                ft.commit();
            }
        });

        llDaZuo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*getFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)  //将当前fragment加入到返回栈中
                        .replace(R.id.fl_container, new MineFragment()).commit();*/

                MineFragment mineFragment = new MineFragment();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.fl_container, mineFragment);
                ft.addToBackStack(null);
                Bundle bundle = new Bundle();
                bundle.putInt("key",1);
                mineFragment.setArguments(bundle);
                ft.commit();

            }
        });

        llXiHuan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*getFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)  //将当前fragment加入到返回栈中
                        .replace(R.id.fl_container, new MineFragment()).commit();*/

                MineFragment mineFragment = new MineFragment();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.fl_container, mineFragment);
                ft.addToBackStack(null);
                Bundle bundle = new Bundle();
                bundle.putInt("key",2);
                mineFragment.setArguments(bundle);
                ft.commit();

            }
        });

        llFenSi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               /* getFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)  //将当前fragment加入到返回栈中
                        .replace(R.id.fl_container, new MineFragment()).commit();*/

                MineFragment mineFragment = new MineFragment();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.fl_container, mineFragment);
                ft.addToBackStack(null);
                Bundle bundle = new Bundle();
                bundle.putInt("key",5);
                mineFragment.setArguments(bundle);
                ft.commit();

            }
        });

        llHuaTi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*getFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)  //将当前fragment加入到返回栈中
                        .replace(R.id.fl_container, new MineFragment()).commit();*/

                MineFragment mineFragment = new MineFragment();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.fl_container, mineFragment);
                ft.addToBackStack(null);
                Bundle bundle = new Bundle();
                bundle.putInt("key",4);
                mineFragment.setArguments(bundle);
                ft.commit();

            }
        });

        llSheZhi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //跳转到设置页面
                Intent intent = new Intent();
                intent.setClass(getContext(),MainActivity.class);
                int haha = 1;
                intent.putExtra("haha","设置");
                getContext().startActivity(intent);

            }
        });
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
                tvScoreAmount.setText( "积分" + String.valueOf(userInfoEntity.getScore()));
               // loadCircleData(userInfoEntity);
            }
        });

    }
}
