package cn.incorner.contrast.page;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.adapter.WoLaiContrastListAdapter;
import cn.incorner.contrast.data.entity.MulitVersionEntity;
import cn.incorner.contrast.data.entity.ParagraphEntity;
import cn.incorner.contrast.util.CommonUtil;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.DensityUtil;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.view.CustomRefreshFramework;
import cn.incorner.contrast.view.RefreshingAnimationView;
import cn.incorner.contrast.view.ScrollAbleViewPager;
import cn.incorner.contrast.view.ScrollListenerListView;

/**
 * Created by 超 on 2016/10/4.
 */

@ContentView(R.layout.activity_contrast_detail_wolai)
public class WoLaiContrastDetailActivity extends BaseActivity implements CustomRefreshFramework.OnTouchMoveListener, RefreshingAnimationView.IRefreshingAnimationView, View.OnClickListener, WoLaiContrastListAdapter.OnViewPageSetUpCallBack, AbsListView.OnScrollListener {

    private static final String TAG = "WoLaiContrastDetailActivity";
    private ParagraphEntity mEntity;
    private List<ParagraphEntity> list = new ArrayList<ParagraphEntity>();
    private int mOrientation;
    @ViewInject(R.id.wolai_lv_content)
    private ScrollListenerListView lvContent;
    @ViewInject(R.id.tv_title)
    private TextView tvTitle;
    @ViewInject(R.id.mulit_iv_back)
    private ImageView ivBack;
    @ViewInject(R.id.mulit_iv_comeon_me)
    private ImageView ivComeonMe;
    @ViewInject(R.id.mulit_iv_comeon_share)
    private ImageView ivComeonShare;
    @ViewInject(R.id.mulit_iv_arrow_left)
    private ImageView ivArrowLeft;
    @ViewInject(R.id.mulit_iv_arrow_right)
    private ImageView ivArrowRight;
    private int mTitleBackColor;
    private ColorStateList mTitleTextColor;
    private int mResultBackColor;
    private ColorStateList mResultTextColor;
    private TabLayout mTlUsers;
    private WoLaiContrastListAdapter mAdapter;
    private List<ParagraphEntity> mParagraphEntitylist;
    private ScrollAbleViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWoLai();
        //第一次提交
    }

    private void initWoLai() {
        final int userId = PrefUtil.getIntValue(Config.PREF_USER_ID);
        mEntity = getIntent().getParcelableExtra("paragraph3");
        mOrientation = getIntent().getIntExtra("orientation", LinearLayout.HORIZONTAL);
        mTitleBackColor = getIntent().getIntExtra("titleBackColor", -1);
        mTitleTextColor = getIntent().getParcelableExtra("titleTextColor");
        mResultBackColor = getIntent().getIntExtra("resultBackColor", -1);
        mResultTextColor = getIntent().getParcelableExtra("resultTextColor");
        DD.d(TAG, "commentCount: " + mEntity.getCommentCount());
        initView();
        initData();
    }

    private void initView() {
        mTlUsers = (TabLayout) findViewById(R.id.mulit_tl_users);
        tvTitle.setText(mEntity.getSeriesCount() + " " + getResources().getString(R.string.version));
        ivBack.setOnClickListener(this);
        ivComeonMe.setOnClickListener(this);
        ivComeonShare.setOnClickListener(this);
        ivArrowLeft.setOnClickListener(this);
        ivArrowRight.setOnClickListener(this);
        lvContent.setOnScrollListener(this);
    }

    private void initData() {
        ArrayList paragraphEntitys = new ArrayList();
        mEntity.hasSeened = true;
        paragraphEntitys.add(mEntity);
        mAdapter = new WoLaiContrastListAdapter(paragraphEntitys, getLayoutInflater(), this, mOrientation, mTitleTextColor, mTitleBackColor, mResultTextColor, mResultBackColor);
        mAdapter.setOnViewPageSetUpCallBack(this);
        lvContent.setAdapter(mAdapter);
        getParagraphVersionFromServer();
    }

    @Override
    public void onVerticalMoving() {

    }

    @Override
    public void onMoveUp() {

    }

    @Override
    public void onMoveDown() {

    }

    @Override
    public void showRefreshingAnimationView() {

    }

    @Override
    public void hideRefreshingAnimationView() {

    }

    /**
     * onItem click
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mulit_iv_back:
                onBackPressed();
                break;
            case R.id.mulit_iv_comeon_me:
                startWoLaiActivity();
                break;
            case R.id.mulit_iv_comeon_share:
                shareAll(mEntity);
                break;
            case R.id.mulit_iv_arrow_left:
                showLastPage();
                break;
            case R.id.mulit_iv_arrow_right:
                showNextPage();
                break;
            default:
                break;
        }
    }

    private void showNextPage() {
        if (mViewPager != null) {
            int currentItem = mViewPager.getCurrentItem();
            int count = mViewPager.getAdapter().getCount();
            if (currentItem < count) {
                currentItem++;
                mViewPager.setCurrentItem(currentItem);
            }
        }
    }

    private void showLastPage() {
        if (mViewPager != null) {
            int currentItem = mViewPager.getCurrentItem();
            int count = mViewPager.getAdapter().getCount();
            if (currentItem >= 1) {
                currentItem--;
                mViewPager.setCurrentItem(currentItem);
            }
        }
    }

    private void startWoLaiActivity() {
        Intent intent = new Intent();
        intent.setClass(this, PostActivity.class);
        //如果传的是 paragraph2 的话就是 我来
        intent.putExtra("paragraph2", mEntity);
        startActivityForResult(intent, MainActivity.REQUEST_CODE_POST);
    }

    @Override
    public void onViewPageSetUpCallBack(ScrollAbleViewPager viewPager) {
        mViewPager = viewPager;
        viewPager.addOnPageChangeListener(new MulitTabLayoutChangeListener(mTlUsers));
        mTlUsers.setOnTabSelectedListener(new MulitViewPageChangeListener(viewPager));
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean canScrollVertically = ViewCompat.canScrollVertically(lvContent, -1);
        if (canScrollVertically) {
            if (ivArrowLeft.getVisibility() != View.GONE) {
                ivArrowLeft.setVisibility(View.GONE);
                if (mViewPager != null) {
                    mViewPager.setShouldInterpt(false);
                }
            }
            if (ivArrowRight.getVisibility() != View.GONE) {
                ivArrowRight.setVisibility(View.GONE);
                if (mViewPager != null) {
                    mViewPager.setShouldInterpt(false);
                }
            }
        } else {
            if (ivArrowRight.getVisibility() != View.VISIBLE) {
                ivArrowRight.setVisibility(View.VISIBLE);
                if (mViewPager != null) {
                    mViewPager.setShouldInterpt(true);
                }
            }
            if (ivArrowLeft.getVisibility() != View.VISIBLE) {
                ivArrowLeft.setVisibility(View.VISIBLE);
                if (mViewPager != null) {
                    mViewPager.setShouldInterpt(true);
                }
            }
        }
    }

    public class MulitTabLayoutChangeListener extends TabLayout.TabLayoutOnPageChangeListener {
        public MulitTabLayoutChangeListener(TabLayout tabLayout) {
            super(tabLayout);
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            ParagraphEntity entity = mParagraphEntitylist.get(position);
            ParagraphEntity entityShow = mAdapter.getData().get(0);
            entityShow.setOriginName(entity.getOriginName());
            entityShow.setOriginAuthor(entity.getOriginAuthor());
            entityShow.setCreateTime(entity.getCreateTime());
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }
    }

    public class MulitViewPageChangeListener extends TabLayout.ViewPagerOnTabSelectedListener {


        public MulitViewPageChangeListener(ViewPager viewPager) {
            super(viewPager);
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            super.onTabSelected(tab);
        }
    }

    /**
     * 点击qq
     */
    private void shareAll(ParagraphEntity entity) {
        final String title = entity.getUserNickname() + "的大作";
        final String targetUrl = Config.PATH_SHARE + entity.getParagraphReplyId();
        final String text = getShareText(getDesc(entity.getParagraphContent()));
        // 设置缩略图
        Bitmap bitmap = mAdapter.getSharedCacheBitmap();
        UMImage umTempImage;
        if (bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, DensityUtil.dip2px(this, 100),
                    DensityUtil.dip2px(this, 100));
            umTempImage = new UMImage(this, bitmap);
        } else {
            umTempImage = new UMImage(this, R.drawable.icon);
        }
        final UMImage umImage = umTempImage;

        new ShareAction(this).setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.SINA)
                .setShareboardclickCallback(new ShareBoardlistener() {
                    @Override
                    public void onclick(SnsPlatform arg0, SHARE_MEDIA shareMedia) {
                        if (SHARE_MEDIA.QQ.equals(shareMedia)) {
                            new ShareAction(WoLaiContrastDetailActivity.this).setPlatform(SHARE_MEDIA.QQ)
                                    .withTitle(title).withText(text).withTargetUrl(targetUrl)
                                    .withMedia(umImage).share();
                        } else if (SHARE_MEDIA.QZONE.equals(shareMedia)) {
                            new ShareAction(WoLaiContrastDetailActivity.this).setPlatform(SHARE_MEDIA.QZONE)
                                    .withTitle(title).withText(text).withTargetUrl(targetUrl)
                                    .withMedia(umImage).share();
                        } else if (SHARE_MEDIA.WEIXIN.equals(shareMedia)) {
                            new ShareAction(WoLaiContrastDetailActivity.this).setPlatform(SHARE_MEDIA.WEIXIN)
                                    .withTitle(title).withText(text).withTargetUrl(targetUrl)
                                    .withMedia(umImage).share();
                        } else if (SHARE_MEDIA.WEIXIN_CIRCLE.equals(shareMedia)) {
                            new ShareAction(WoLaiContrastDetailActivity.this)
                                    .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE).withTitle(title)
                                    .withText(text).withTargetUrl(targetUrl).withMedia(umImage)
                                    .share();
                        } else if (SHARE_MEDIA.SINA.equals(shareMedia)) {
                            new ShareAction(WoLaiContrastDetailActivity.this).setPlatform(SHARE_MEDIA.SINA)
                                    .withText(text).withTargetUrl(targetUrl).withMedia(umImage).share();
                        }
                    }
                }).open();
    }

    private String getShareText(String[] arrDesc) {
        if (arrDesc == null || arrDesc.length < 2) {
            return "";
        }
        return arrDesc[0] + " | " + arrDesc[1];
    }

    private String[] getDesc(String content) {
        String[] arrDesc = new String[2];

        // 提取并设置内容
        // 不用空格做分隔符是因为内容中可能会有分隔符，所以使用 "] [" 做分隔符
        String[] arrContent = content.trim().split("\\] \\[");
        if (arrContent.length != 4) {
            return arrDesc;
        }
        // 修复格式
        arrContent[0] = arrContent[0] + "]";
        arrContent[1] = "[" + arrContent[1] + "]";
        arrContent[2] = "[" + arrContent[2] + "]";
        arrContent[3] = "[" + arrContent[3];

        for (int i = 0; i < arrContent.length; i++) {
            int index = arrContent[i].indexOf("]");
            String sRegular = "\\" + arrContent[i].substring(0, index) + "\\]";
            arrContent[i] = arrContent[i].replaceAll(sRegular, "");
        }

        arrDesc[0] = arrContent[0];
        arrDesc[1] = arrContent[1];
        return arrDesc;
    }

    /**
     * 从服务器刷新 我发布的对比度列表
     */
    private void getParagraphVersionFromServer() {
        RequestParams params = new RequestParams(
                Config.PATH_GET_MULIT_VERSION);
        params.setAsJsonContent(true);
        params.addParameter("seriesId", mEntity.getSeriesId());
        params.addParameter("accessToken",
                PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
        params.addParameter("userId", PrefUtil.getIntValue(Config.PREF_USER_ID));
        params.addParameter("timestamp",
                CommonUtil.getDefaultFormatCurrentTime());
        params.addParameter("from", 0);
        params.addParameter("row", 10);
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
                DD.d("WoLaiContrastListAdapter", "onSuccess(), result: " + result.toString());
                MulitVersionEntity entity = JSON.parseObject(
                        result.toString(), MulitVersionEntity.class);
                String status = entity.getStatus();
                mParagraphEntitylist = entity.getParagraphs();
                if ("0".equals(status)) {
                    mAdapter.updateViewPageData(mParagraphEntitylist);
                    updateMulitUserInfo(mParagraphEntitylist);
                }
            }
        });
    }

    private void updateMulitUserInfo(List<ParagraphEntity> newList) {
        mTlUsers.setTabMode(TabLayout.MODE_SCROLLABLE);
        for (int i = 0; i < newList.size(); i++) {
            ParagraphEntity entity = newList.get(i);
            TabLayout.Tab tab = mTlUsers.newTab();
            tab.setCustomView(R.layout.layout_mulit_tab_user_head);
            mTlUsers.addTab(tab);
            final ImageView ivHead = (ImageView) tab.getCustomView().findViewById(R.id.mulit_iv_head);
            x.image().loadDrawable(Config.getHeadFullPath(entity.getUserAvatarName()), null,
                    new Callback.CommonCallback<Drawable>() {
                        @Override
                        public void onCancelled(CancelledException arg0) {
                        }

                        @Override
                        public void onError(Throwable arg0, boolean arg1) {
                            ivHead.setImageResource(R.drawable.default_avatar);
                        }

                        @Override
                        public void onFinished() {
                        }

                        @Override
                        public void onSuccess(Drawable drawable) {
                            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                            ivHead.setImageBitmap(bitmap);
                        }
                    });
        }
    }
}
