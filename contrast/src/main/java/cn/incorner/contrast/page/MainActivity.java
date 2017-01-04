package cn.incorner.contrast.page;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.igexin.sdk.PushManager;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.socialize.UMShareAPI;

import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.BaseFragmentActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.blur.Blur;
import cn.incorner.contrast.blur.FastBlur;
import cn.incorner.contrast.data.adapter.TopicGridViewAdapter;
import cn.incorner.contrast.data.entity.AnonymousRegEntity;
import cn.incorner.contrast.data.entity.BannerEntity;
import cn.incorner.contrast.data.entity.BannerResultEntity;
import cn.incorner.contrast.data.entity.StatusResultEntity;
import cn.incorner.contrast.data.entity.TopicEntity;
import cn.incorner.contrast.data.entity.TopicResultEntity;
import cn.incorner.contrast.data.entity.UserInfoEntity;
import cn.incorner.contrast.util.AnimationUtil;
import cn.incorner.contrast.util.CommonUtil;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.DataCleanManager;
import cn.incorner.contrast.util.DensityUtil;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.util.QDataModule;
import cn.incorner.contrast.util.TT;
import cn.incorner.contrast.view.BottomPopupWindow;
import cn.incorner.contrast.view.CircleImageView;
import cn.incorner.contrast.view.CustomRefreshFramework;
import cn.incorner.contrast.view.CustomRefreshFramework.OnRefreshingListener;
import cn.incorner.contrast.view.CustomRefreshFramework.OnTouchMoveListener;
import cn.incorner.contrast.view.HeaderFooterGridView;
import cn.incorner.contrast.view.MessageWithOkCancelFragment2;
import cn.incorner.contrast.view.ReboundHorizontalScrollView;
import cn.incorner.contrast.view.RefreshingAnimationView;
import cn.incorner.contrast.view.RefreshingAnimationView.IRefreshingAnimationView;
import cn.incorner.contrast.view.SweepingView;

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseFragmentActivity implements OnTouchMoveListener,
        OnClickListener, AMapLocationListener, IRefreshingAnimationView, QDataModule.OnMessageReceiveListener, QDataModule.OnFindGoneListener {

    private static final String TAG = "MainActivity";

    // 上一个页面，仅针对“个人”部分
    public static int LAST_PAGE = -1;

    private static final String FRAG_MAIN_TAG = "fragMainTag";
    private static final String FRAG_MINE_TAG = "fragMineTag";
    private static final String FRAG_PERSON_TAG = "fragPersonTag";
    private static final int FRAG_MAIN_FLAG = 1;
    private static final int FRAG_MINE_FLAG = 2;
    //新添加的
    private static final int FRAG_PERSON_FLAG = 3;

    //当前页面
    private int currentFragment;

    // 选取图片标识符
    private static final int REQUEST_CODE_PICK = 1;
    private static final int REQUEST_CODE_CAPTURE = 2;
    private static final int REQUEST_CODE_CUT = 3;
    // 发布
    public static final int REQUEST_CODE_POST = 4;

    @ViewInject(R.id.rl_root)
    private RelativeLayout rlRoot;
    @ViewInject(R.id.fl_container)
    private FrameLayout flContainer;
    @ViewInject(R.id.ll_top_container)
    private LinearLayout llTopContainer;
    @ViewInject(R.id.ll_main_nav_container)
    private LinearLayout llMainNavContainer;
    @ViewInject(R.id.tv_selected)
    private TextView tvSelected;
    @ViewInject(R.id.tv_richincontent)
    private TextView tvRichInContent;
    @ViewInject(R.id.tv_find)
    private TextView tvFind;
    @ViewInject(R.id.tv_all)
    private TextView tvAll;
    @ViewInject(R.id.hsv_category_container_2)
    private ReboundHorizontalScrollView hsvCategoryContainer2;
    @ViewInject(R.id.tv_my_paragraph)
    private TextView tvMyParagraph;
    @ViewInject(R.id.tv_my_like)
    private TextView tvMyLike;
    @ViewInject(R.id.tv_my_following_user)
    private TextView tvMyFollowingUser;
    @ViewInject(R.id.tv_my_all_topic)
    private TextView tvMyAllTopic;
    @ViewInject(R.id.tv_my_follower_user)
    private TextView tvMyFollowerUser;
    @ViewInject(R.id.rl_head)
    private RelativeLayout rlHead;
    @ViewInject(R.id.civ_head)
    private CircleImageView civHead;
    @ViewInject(R.id.iv_msg)
    private ImageView ivMsg;
    @ViewInject(R.id.rl_bottom_container)
    private RelativeLayout rlBottomContainer;
    @ViewInject(R.id.ll_bottom_container)
    private LinearLayout llBottomContainer;
    @ViewInject(R.id.v_post)
    private View vPost;
    @ViewInject(R.id.rl_left)
    private RelativeLayout rlLeft;
    @ViewInject(R.id.rl_right)
    private RelativeLayout rlRight;

    // 所有话题面板部分
    @ViewInject(R.id.rl_all_find_board)
    private RelativeLayout rlAllFindBoard;
    @ViewInject(R.id.crl_container)
    private CustomRefreshFramework crlContainer;

    //话题面板
    @ViewInject(R.id.gv_content)
    private HeaderFooterGridView gvContent;
    // 数据相关
    private List<TopicEntity> listTopic = new ArrayList<TopicEntity>();
    private TopicGridViewAdapter findAdapter;

    // 用户个人面板部分
    @ViewInject(R.id.rl_user_board)
    private RelativeLayout rlUserBoard;
    @ViewInject(R.id.rl_back)
    private RelativeLayout rlBack;
    @ViewInject(R.id.civ_big_head)
    private CircleImageView civBigHead;
    @ViewInject(R.id.ll_info_container)
    private LinearLayout llInfoContainer;
    @ViewInject(R.id.et_nickname)
    private EditText etNickname;
    @ViewInject(R.id.iv_clear_nickname)
    private ImageView ivClearNickname;
    @ViewInject(R.id.et_signature)
    private EditText etSignature;
    @ViewInject(R.id.iv_clear_signature)
    private ImageView ivClearSignature;
    @ViewInject(R.id.et_job_title)
    private EditText etJobTitle;
    @ViewInject(R.id.iv_clear_job_title)
    private ImageView ivClearJobTitle;
    @ViewInject(R.id.tv_born)
    private TextView tvBorn;
    @ViewInject(R.id.tv_gender)
    private TextView tvGender;
    @ViewInject(R.id.et_email)
    private EditText etEmail;
    @ViewInject(R.id.iv_clear_email)
    private ImageView ivClearEmail;
    @ViewInject(R.id.rl_1)
    private RelativeLayout rlCircleRoot;


    private RelativeLayout rlContrastAmount;
    private RelativeLayout rlSvContrastAmount;
    private TextView tvContrastAmount;
    private TextView tvContrastAmountName;
    private RelativeLayout rlFollowerAmount;
    private RelativeLayout rlSvFollowerAmount;
    private TextView tvFollowerAmount;
    private TextView tvFollowerAmountName;
    private RelativeLayout rlScoreAmount;
    private RelativeLayout rlSvScoreAmount;
    private TextView tvScoreAmount;
    private TextView tvScoreAmountName;
    @ViewInject(R.id.btn_update_password)
    private Button btnUpdatePassword;
    @ViewInject(R.id.btn_clear_cache)
    private Button btnClearCache;
    @ViewInject(R.id.btn_logout)
    private Button btnLogout;
    @ViewInject(R.id.tv_feedback)
    private TextView tvFeedback;

    // 修改密码部分
    @ViewInject(R.id.ll_update_password_container)
    private LinearLayout llUpdatePasswordContainer;
    @ViewInject(R.id.iv_close_password_container)
    private ImageView ivClosePasswordContainer;
    @ViewInject(R.id.et_old_password)
    private EditText etOldPassword;
    @ViewInject(R.id.iv_clear_old_password)
    private ImageView ivClearOldPassword;
    @ViewInject(R.id.et_new_password)
    private EditText etNewPassword;
    @ViewInject(R.id.iv_clear_new_password)
    private ImageView ivClearNewPassword;
    @ViewInject(R.id.et_confirm_password)
    private EditText etConfirmPassword;
    @ViewInject(R.id.iv_clear_confirm_password)
    private ImageView ivClearConfirmPassword;
    // 选择头像
    private BottomPopupWindow bpwSelectHead;
    // 选择性别
    private BottomPopupWindow bpwGenderSelector;

    // 搜索面板相关
    @ViewInject(R.id.et_search_paragraph)
    private EditText etSearchParagraph;
    @ViewInject(R.id.et_search_user)
    private EditText etSearchUser;

    // 刷新视图
    @ViewInject(R.id.rav_refreshing_view)
    private RefreshingAnimationView ravRefreshingView;

    //导航上的页面名称
    @ViewInject(R.id.page_name)
    private TextView pageName;

    //新写的返回键
    @ViewInject(R.id.new_rl_back)
    private RelativeLayout newRlBack;

    private FragmentManager fmManager;
    private MainFragment fragMain;
    private MineFragment fragMine;
    private PersonDetailFragment fragPersonDetail;

    private Blur blur;

    // 高德定位
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    // 轮播图 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    //轮播图数据的集合
    private List<BannerEntity> listFindBanner = new ArrayList<BannerEntity>();
    // 轮播图的点的集合
    private List<View> dots = new ArrayList<View>();

    private LinearLayout dots_ll;// 用来展示点的容器

    private View topView;

    private RollViewPager rollView;

    private LinearLayout top_news_viewpager;// 用来展示轮播图的容器
    // 轮播图 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    private View viewTitle;

    /**
     * 数据
     */
    // 用户信息数据
    private UserInfoEntity userInfoEntity;
    // 上传头像的本地文件地址
    private Uri avatarUri;

    SweepingView svContrastAmount, svFollowerAmount, svScoreAmount;

    private boolean circleLyoutType = false;// false circle_defined_layout.xml ;true
    // circle_defined_layout2.xml

    private String UMENG_APPKEY = "57075df9e0f55a4c5700097c";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUmengAnalytics();
        init(savedInstanceState);
        loadData();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private Bundle savedInstance;

    private void init(Bundle savedInstance) {
        blur = new Blur(this);
        initStaticColor();

        // init frag
        this.savedInstance = savedInstance;
        if (isLogined()) {
            initFragmentPage(savedInstance);
            initTopNavContainer();
        }

        //初始化所有话题面板
        initAllTopicBoard();
        //初始化选择头像面板
        initSelectHeadBoard();
        //初始化性别选择面板
        initSelectGenderBoard();
        //初始化文本框（设置监听）
        initEditText();
        //设置键盘 显示/隐藏 监听（键盘非全屏模式也算隐藏）
        setOnSoftKeyBoardVisibleChangeListener();
        //初始化高德定位
        initAMapLocation();
        QDataModule.getInstance().addMessageReceiveListener(this);
        QDataModule.getInstance().addFindGoneListener(this);

    }

    /**
     * 初始化颜色
     */
    private void initStaticColor() {
        if (BaseActivity.BG_COLOR != null && BaseActivity.TEXT_COLOR != null) {
            return;
        }

        // 初始化颜色数据
        TypedArray taBgColor = getResources().obtainTypedArray(R.array.bg_color);
        TypedArray taTextColor = getResources().obtainTypedArray(R.array.text_color);
        BaseActivity.BG_COLOR = new int[taBgColor.length()];
        BaseActivity.TEXT_COLOR = new int[taTextColor.length()];
        for (int i = 0; i < BaseActivity.BG_COLOR.length; i++) {
            BaseActivity.BG_COLOR[i] = taBgColor.getColor(i, 0);
            BaseActivity.TEXT_COLOR[i] = taTextColor.getColor(i, 0);
        }
        taBgColor.recycle();
        taTextColor.recycle();
    }

    /**
     * 初始化页面
     */
    private void initFragmentPage(Bundle savedInstance) {
        DD.d(TAG, "initFragmentPage()");

        fmManager = getSupportFragmentManager();
        fragMain = (MainFragment) fmManager.findFragmentByTag(FRAG_MAIN_TAG);
        fragMine = (MineFragment) fmManager.findFragmentByTag(FRAG_MINE_TAG);
        fragPersonDetail = (PersonDetailFragment) fmManager.findFragmentByTag(FRAG_PERSON_TAG);

        if (fragMain == null) {
            fragMain = new MainFragment();
        }
        if (fragMine == null) {
            fragMine = new MineFragment();
        }
        if (fragPersonDetail == null) {
            fragPersonDetail = new PersonDetailFragment();
        }
        fmManager.beginTransaction().add(R.id.fl_container, fragMain, FRAG_MAIN_TAG)
                .add(R.id.fl_container, fragPersonDetail, FRAG_PERSON_TAG)
                .add(R.id.fl_container, fragMine, FRAG_MINE_TAG)
                .show(fragMain)
                .hide(fragPersonDetail)
                .hide(fragMine)
                .commitAllowingStateLoss();
        switchPage(FRAG_MAIN_FLAG);
    }

    /**
     * 切换页面
     */
    private void switchPage(int page) {
        currentFragment = page;

        if (page == FRAG_MAIN_FLAG) {
            rlLeft.setSelected(true);
            rlRight.setSelected(false);
            llTopContainer.setVisibility(View.VISIBLE);
            llMainNavContainer.setVisibility(View.VISIBLE);
            hsvCategoryContainer2.setVisibility(View.GONE);

            //清空回退栈
//            getSupportFragmentManager().popBackStack();

            //隐藏页面名字和新的返回键
            pageName.setVisibility(View.GONE);
            newRlBack.setVisibility(View.GONE);

            setMainNavSelector(R.id.tv_selected);
            rlAllFindBoard.setVisibility(View.GONE);
            // 设置fragment
            if (fragMain.isVisible()) {
                fragMain.refreshData();
            } else {
                fmManager.beginTransaction().show(fragMain).hide(fragPersonDetail).hide(fragMine)
                        .commitAllowingStateLoss();
            }
        } else if (page == FRAG_PERSON_FLAG) {
            /*rlLeft.setSelected(false);
            rlRight.setSelected(true);
			llMainNavContainer.setVisibility(View.GONE);
			hsvCategoryContainer2.setVisibility(View.VISIBLE);
			rlAllFindBoard.setVisibility(View.GONE);
			// 设置fragment
			if (fragMine.isAdded()) {
				fmManager.beginTransaction().show(fragMine).hide(fragMain)
						.commitAllowingStateLoss();
			} else {
				fmManager.beginTransaction().add(R.id.fl_container, fragMine, FRAG_MINE_TAG)
						.show(fragMine).hide(fragMain).commitAllowingStateLoss();
			}*/

            rlLeft.setSelected(false);
            rlRight.setSelected(true);
            llMainNavContainer.setVisibility(View.GONE);
            hsvCategoryContainer2.setVisibility(View.GONE);
            rlAllFindBoard.setVisibility(View.GONE);
            llTopContainer.setVisibility(View.GONE);
            // 设置fragment
            fmManager.beginTransaction().show(fragPersonDetail).hide(fragMain).hide(fragMine)
                    .commitAllowingStateLoss();
        } else {
            rlLeft.setSelected(false);
            rlRight.setSelected(true);
            llMainNavContainer.setVisibility(View.GONE);
            hsvCategoryContainer2.setVisibility(View.GONE);
            rlAllFindBoard.setVisibility(View.GONE);
            llTopContainer.setVisibility(View.GONE);
            // 设置fragment
            fmManager.beginTransaction().show(fragMine).hide(fragMain).hide(fragPersonDetail)
                    .commitAllowingStateLoss();
        }
    }

    private void initTopNavContainer() {
        DD.d(TAG, "initTopNavContainer()");

        tvSelected.setOnClickListener(fragMain);
        tvRichInContent.setOnClickListener(fragMain);
        tvAll.setOnClickListener(fragMain);

        ViewGroup vgMineContainer = (ViewGroup) hsvCategoryContainer2.getChildAt(0);
        int mineChildCount = vgMineContainer.getChildCount();
        for (int i = 0; i < mineChildCount; ++i) {
            vgMineContainer.getChildAt(i).setOnClickListener(fragMine);
        }
    }

    public void setMainNavSelector(int id) {
        DD.d(TAG, "setMainNavSelector()");

        TextPaint tpSelected = tvSelected.getPaint();
        tpSelected.setFakeBoldText(false);
        TextPaint tpRichInContent = tvRichInContent.getPaint();
        tpRichInContent.setFakeBoldText(false);
        TextPaint tpAll = tvAll.getPaint();
        tpAll.setFakeBoldText(false);
        TextPaint tpFind = tvFind.getPaint();
        tpFind.setFakeBoldText(false);
        tvSelected.setTextColor(getResources().getColor(R.color.text_gray));
        tvRichInContent.setTextColor(getResources().getColor(R.color.text_gray));
        tvAll.setTextColor(getResources().getColor(R.color.text_gray));
        tvFind.setTextColor(getResources().getColor(R.color.text_gray));
        switch (id) {
            case R.id.tv_selected:
                tpSelected.setFakeBoldText(true);
                tvSelected.setTextColor(getResources().getColor(R.color.main_top_title_text));
                break;
            case R.id.tv_richincontent:
                tpRichInContent.setFakeBoldText(true);
                tvRichInContent.setTextColor(getResources().getColor(R.color.main_top_title_text));
                break;
            case R.id.tv_all:
                if (!isLogined()) {
                    gotoActivity(LoginTransitionActivity.class);
                    finish();
                    return;
                } else {
                    tpAll.setFakeBoldText(true);
                }
                tvAll.setTextColor(getResources().getColor(R.color.main_top_title_text));
                break;
        }
        tvSelected.setText("有趣");
        tvRichInContent.setText("有料");
        tvAll.setText("关注");
    }

    public void setMineNavSelector(int id) {
        DD.d(TAG, "setMineNavSelector(), id: " + id);

        TextPaint tpMyParagraph = tvMyParagraph.getPaint();
        tpMyParagraph.setFakeBoldText(false);
        TextPaint tpMyLike = tvMyLike.getPaint();
        tpMyLike.setFakeBoldText(false);
        TextPaint tpMyFollowingUser = tvMyFollowingUser.getPaint();
        tpMyFollowingUser.setFakeBoldText(false);
        TextPaint tpMyAllTopic = tvMyAllTopic.getPaint();
        tpMyAllTopic.setFakeBoldText(false);
        TextPaint tpMyFollowerUser = tvMyFollowerUser.getPaint();
        tpMyFollowerUser.setFakeBoldText(false);
        tvMyParagraph.setTextColor(getResources().getColor(R.color.text_gray));
        tvMyLike.setTextColor(getResources().getColor(R.color.text_gray));
        tvMyFollowingUser.setTextColor(getResources().getColor(R.color.text_gray));
        tvMyAllTopic.setTextColor(getResources().getColor(R.color.text_gray));
        tvMyFollowerUser.setTextColor(getResources().getColor(R.color.text_gray));
        switch (id) {
            case R.id.tv_my_paragraph:
                tpMyParagraph.setFakeBoldText(true);
                tvMyParagraph.setTextColor(getResources().getColor(R.color.main_top_title_text));
                break;
            case R.id.tv_my_like:
                tpMyLike.setFakeBoldText(true);
                tvMyLike.setTextColor(getResources().getColor(R.color.main_top_title_text));
                break;
            case R.id.tv_my_following_user:
                tpMyFollowingUser.setFakeBoldText(true);
                tvMyFollowingUser.setTextColor(getResources().getColor(R.color.main_top_title_text));
                break;
            case R.id.tv_my_all_topic:
                tpMyAllTopic.setFakeBoldText(true);
                tvMyAllTopic.setTextColor(getResources().getColor(R.color.main_top_title_text));
                break;
            case R.id.tv_my_follower_user:
                tpMyFollowerUser.setFakeBoldText(true);
                tvMyFollowerUser.setTextColor(getResources().getColor(R.color.main_top_title_text));
                break;
        }
        tvMyParagraph.setText("我的大作");
        tvMyLike.setText("我喜欢的");
        tvMyFollowingUser.setText("我的关注");
        tvMyAllTopic.setText("我的话题");
        tvMyFollowerUser.setText("我的粉丝");
    }

    /**
     * 初始化所有话题面板
     */
    private void initAllTopicBoard() {
        DD.d(TAG, "initAllTopicBoard()");

        // 初始化刷新框架
        View vHeader = getLayoutInflater().inflate(R.layout.view_custom_refresh_framework_header, null);
        topView = getLayoutInflater().inflate(R.layout.layout_roll_view, null);
        dots_ll = (LinearLayout) topView.findViewById(R.id.dots_ll);
        top_news_viewpager = (LinearLayout) topView.findViewById(R.id.top_news_viewpager);

        viewTitle = getLayoutInflater().inflate(R.layout.view_title, null);

        gvContent.addHeaderView(vHeader);
        gvContent.addHeaderView(topView);
        gvContent.addHeaderView(viewTitle);
        crlContainer.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                DD.d("tag", "onGlobalLayout()");
                crlContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                crlContainer.setHeaderContainerHeight(getTopContainerHeight());

            }
        });
        crlContainer.setOnRefreshingListener(new OnRefreshingListener() {
            @Override
            public void onRefreshing() {
                refreshFindFromServer();
            }

            @Override
            public void onLoadMore() {
                loadMoreFindFromServer();
            }

            @Override
            public void onRefreshFinish() {
                findAdapter.notifyDataSetChanged();
            }
        });
        crlContainer.setOnTouchMoveListener(new OnTouchMoveListener() {
            private long lastBlurTime;
            private long currentTime;

            public void onVerticalMoving() {
                currentTime = System.currentTimeMillis();
                if (currentTime - lastBlurTime < 100) {
                    return;
                }
                lastBlurTime = currentTime;

                crlContainer.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        DD.d(TAG, "onPreDraw()");

                        crlContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                        crlContainer.buildDrawingCache();
                        return false;
                    }
                });
            }

            public void onMoveUp() {
            }

            public void onMoveDown() {
            }
        });

        findAdapter = new TopicGridViewAdapter(listTopic, getLayoutInflater());
        gvContent.setAdapter(findAdapter);
    }

    TranslateAnimation tagShowTopicNav;
    TranslateAnimation taHideTopicNav;

    /**
     * 初始化选择头像面板
     */
    private void initSelectHeadBoard() {
        DD.d(TAG, "initSelectHeadBoard()");

        View vContent = getLayoutInflater().inflate(R.layout.view_select_head, null);
        bpwSelectHead = new BottomPopupWindow(this, findViewById(R.id.rl_root), vContent,
                R.style.style_share_board_window);

        // 设置点击监听
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.rl_take_photo:
                        takePhoto();
                        break;
                    case R.id.rl_select_photo:
                        selectPhoto();
                        break;
                    case R.id.rl_cancel:
                        // 下面有dismiss操作
                        break;
                }
                bpwSelectHead.dismiss();
            }
        };

        vContent.findViewById(R.id.rl_take_photo).setOnClickListener(listener);
        vContent.findViewById(R.id.rl_select_photo).setOnClickListener(listener);
        vContent.findViewById(R.id.rl_cancel).setOnClickListener(listener);
    }

    /**
     * 初始化性别选择面板
     */
    private void initSelectGenderBoard() {
        DD.d(TAG, "initSelectGenderBoard()");

        View vContent = getLayoutInflater().inflate(R.layout.view_gender_selector, null);
        bpwGenderSelector = new BottomPopupWindow(this, findViewById(R.id.rl_root), vContent,
                R.style.style_share_board_window);

        // 设置点击监听
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.rl_male:
                        userInfoEntity.setUserSex(Config.MALE);
                        tvGender.setText(Config.getGender(userInfoEntity.getUserSex()));
                        break;
                    case R.id.rl_female:
                        userInfoEntity.setUserSex(Config.FEMALE);
                        tvGender.setText(Config.getGender(userInfoEntity.getUserSex()));
                        break;
                    case R.id.rl_cancel:
                        // 下面有dismiss操作
                        break;
                }
                bpwGenderSelector.dismiss();
            }
        };

        vContent.findViewById(R.id.rl_male).setOnClickListener(listener);
        vContent.findViewById(R.id.rl_female).setOnClickListener(listener);
        vContent.findViewById(R.id.rl_cancel).setOnClickListener(listener);
    }

    private View vCurrentFocusedView = null;

    @Event(value = {R.id.et_nickname, R.id.et_signature, R.id.et_job_title, R.id.et_email,
            R.id.et_old_password, R.id.et_new_password, R.id.et_confirm_password}, type = OnFocusChangeListener.class)
    private void onFocusChange(View v, boolean hasFocus) {
        DD.d(TAG, "onFocusChange(), hasFocus: " + hasFocus);

        if (hasFocus) {
            vCurrentFocusedView = v;
            switch (v.getId()) {
                case R.id.et_nickname:
                    if (etNickname.getText().length() > 0) {
                        ivClearNickname.setVisibility(View.VISIBLE);
                    } else {
                        ivClearNickname.setVisibility(View.GONE);
                    }
                    break;
                case R.id.et_signature:
                    if (etSignature.getText().length() > 0) {
                        ivClearSignature.setVisibility(View.VISIBLE);
                    } else {
                        ivClearSignature.setVisibility(View.GONE);
                    }
                    break;
                case R.id.et_job_title:
                    if (etJobTitle.getText().length() > 0) {
                        ivClearJobTitle.setVisibility(View.VISIBLE);
                    } else {
                        ivClearJobTitle.setVisibility(View.GONE);
                    }
                    break;
                case R.id.et_email:
                    if (etEmail.getText().length() > 0) {
                        ivClearEmail.setVisibility(View.VISIBLE);
                    } else {
                        ivClearEmail.setVisibility(View.GONE);
                    }
                    break;
                case R.id.et_old_password:
                    if (etOldPassword.getText().length() > 0) {
                        ivClearOldPassword.setVisibility(View.VISIBLE);
                    } else {
                        ivClearOldPassword.setVisibility(View.GONE);
                    }
                    break;
                case R.id.et_new_password:
                    if (etNewPassword.getText().length() > 0) {
                        ivClearNewPassword.setVisibility(View.VISIBLE);
                    } else {
                        ivClearNewPassword.setVisibility(View.GONE);
                    }
                    break;
                case R.id.et_confirm_password:
                    if (etConfirmPassword.getText().length() > 0) {
                        ivClearConfirmPassword.setVisibility(View.VISIBLE);
                    } else {
                        ivClearConfirmPassword.setVisibility(View.GONE);
                    }
                    break;
            }
        } else {
            switch (v.getId()) {
                case R.id.et_nickname:
                    ivClearNickname.setVisibility(View.GONE);
                    break;
                case R.id.et_signature:
                    ivClearSignature.setVisibility(View.GONE);
                    break;
                case R.id.et_job_title:
                    ivClearJobTitle.setVisibility(View.GONE);
                    break;
                case R.id.et_email:
                    ivClearEmail.setVisibility(View.GONE);
                    break;
                case R.id.et_old_password:
                    ivClearOldPassword.setVisibility(View.GONE);
                    break;
                case R.id.et_new_password:
                    ivClearNewPassword.setVisibility(View.GONE);
                    break;
                case R.id.et_confirm_password:
                    ivClearConfirmPassword.setVisibility(View.GONE);
                    break;
            }
        }
    }

    /**
     * 初始化文本框（设置监听）
     */
    private void initEditText() {
        DD.d(TAG, "initEditText()");

        etNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (vCurrentFocusedView == null || etNickname == null
                        || vCurrentFocusedView.getId() != etNickname.getId()) {
                    ivClearNickname.setVisibility(View.GONE);
                } else {
                    if (s.length() > 0) {
                        ivClearNickname.setVisibility(View.VISIBLE);
                    } else {
                        ivClearNickname.setVisibility(View.GONE);
                    }
                }
            }
        });

        etSignature.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (vCurrentFocusedView == null || etSignature == null
                        || vCurrentFocusedView.getId() != etSignature.getId()) {
                    ivClearSignature.setVisibility(View.GONE);
                } else {
                    if (s.length() > 0) {
                        ivClearSignature.setVisibility(View.VISIBLE);
                    } else {
                        ivClearSignature.setVisibility(View.GONE);
                    }
                }
            }
        });

        etJobTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (vCurrentFocusedView == null || etJobTitle == null
                        || vCurrentFocusedView.getId() != etJobTitle.getId()) {
                    ivClearJobTitle.setVisibility(View.GONE);
                } else {
                    if (s.length() > 0) {
                        ivClearJobTitle.setVisibility(View.VISIBLE);
                    } else {
                        ivClearJobTitle.setVisibility(View.GONE);
                    }
                }
            }
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (vCurrentFocusedView == null || etEmail == null
                        || vCurrentFocusedView.getId() != etEmail.getId()) {
                    ivClearEmail.setVisibility(View.GONE);
                } else {
                    if (s.length() > 0) {
                        ivClearEmail.setVisibility(View.VISIBLE);
                    } else {
                        ivClearEmail.setVisibility(View.GONE);
                    }
                }
            }
        });

        etOldPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (vCurrentFocusedView == null || etOldPassword == null
                        || vCurrentFocusedView.getId() != etOldPassword.getId()) {
                    ivClearOldPassword.setVisibility(View.GONE);
                } else {
                    if (s.length() > 0) {
                        ivClearOldPassword.setVisibility(View.VISIBLE);
                    } else {
                        ivClearOldPassword.setVisibility(View.GONE);
                    }
                }
            }
        });

        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (vCurrentFocusedView == null || etNewPassword == null
                        || vCurrentFocusedView.getId() != etNewPassword.getId()) {
                    ivClearNewPassword.setVisibility(View.GONE);
                } else {
                    if (s.length() > 0) {
                        ivClearNewPassword.setVisibility(View.VISIBLE);
                    } else {
                        ivClearNewPassword.setVisibility(View.GONE);
                    }
                }
            }
        });

        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (vCurrentFocusedView == null || etConfirmPassword == null
                        || vCurrentFocusedView.getId() != etConfirmPassword.getId()) {
                    ivClearConfirmPassword.setVisibility(View.GONE);
                } else {
                    if (s.length() > 0) {
                        ivClearConfirmPassword.setVisibility(View.VISIBLE);
                    } else {
                        ivClearConfirmPassword.setVisibility(View.GONE);
                    }
                }
            }
        });

        tvFeedback.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
    }

    /**
     * 设置键盘 显示/隐藏 监听（键盘非全屏模式也算隐藏）
     */
    private boolean lastVisible = false;

    private void setOnSoftKeyBoardVisibleChangeListener() {
        final View decorView = getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect rect = new Rect();
                        decorView.getWindowVisibleDisplayFrame(rect);
                        int displayHight = rect.bottom - rect.top;
                        int hight = decorView.getHeight();
                        boolean visible = (double) displayHight / hight < 0.8;

                        if (visible != lastVisible) {
                            onSoftKeyBoardVisibleChanged(visible);
                        }
                        lastVisible = visible;
                    }
                });
    }

    /**
     * 键盘显示状态改变回调
     */
    private void onSoftKeyBoardVisibleChanged(boolean visible) {
        Log.d(TAG, "onSoftKeyBoardVisibleChanged(), visible: " + visible);

        if (!visible) {
            etNickname.clearFocus();
            etSignature.clearFocus();
            etJobTitle.clearFocus();
            etEmail.clearFocus();
            etOldPassword.clearFocus();
            etNewPassword.clearFocus();
            etConfirmPassword.clearFocus();
        }
    }

    /**
     * 初始化高德定位
     */
    private void initAMapLocation() {
        DD.d(TAG, "initAMapLocation()");

        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = new AMapLocationClientOption();
        // 设置定位模式为低功耗模式
        locationOption.setLocationMode(AMapLocationMode.Battery_Saving);
        // 设置定位监听
        locationClient.setLocationListener(this);

        initOption();
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
    }

    private void addCircleViews() {
        svContrastAmount = new SweepingView(this);
        svContrastAmount.setRoundWidth(20f);
        svFollowerAmount = new SweepingView(this);
        svFollowerAmount.setRoundWidth(20f);
        svScoreAmount = new SweepingView(this);
        svScoreAmount.setRoundWidth(20f);

        if (rlSvContrastAmount == null) {
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            View v = getLayoutInflater().inflate(R.layout.circle_defined_layout, null);
            circleLyoutType = false;

            rlContrastAmount = (RelativeLayout) v.findViewById(R.id.rl_contrast_amount);
            rlSvContrastAmount = (RelativeLayout) v.findViewById(R.id.rl_sv_contrast_amount);
            tvContrastAmount = (TextView) v.findViewById(R.id.tv_contrast_amount);
            tvContrastAmountName = (TextView) v.findViewById(R.id.tv_contrast_amount_name);

            rlFollowerAmount = (RelativeLayout) v.findViewById(R.id.rl_follower_amount);
            rlSvFollowerAmount = (RelativeLayout) v.findViewById(R.id.rl_sv_follower_amount);
            tvFollowerAmount = (TextView) v.findViewById(R.id.tv_follower_amount);
            tvFollowerAmountName = (TextView) v.findViewById(R.id.tv_follower_amount_name);

            rlScoreAmount = (RelativeLayout) v.findViewById(R.id.rl_score_amount);
            rlSvScoreAmount = (RelativeLayout) v.findViewById(R.id.rl_sv_score_amount);
            tvScoreAmount = (TextView) v.findViewById(R.id.tv_score_amount);
            tvScoreAmountName = (TextView) v.findViewById(R.id.tv_score_amount_name);
            tvContrastAmount.setText(userInfoEntity != null ? String.valueOf(userInfoEntity.getParagraphCount()) : "0");
            tvFollowerAmount.setText(userInfoEntity != null ? String.valueOf(userInfoEntity.getFollowerCount()) : "0");
            tvScoreAmount.setText(userInfoEntity != null ? String.valueOf(userInfoEntity.getScore()) : "0");
            setTextSize(userInfoEntity);
            rlCircleRoot.addView(v, param);
        }

        RelativeLayout.LayoutParams paramSvContrastAmount, paramSvFollowerAmount, paramSvScoreAmount;
        if (!circleLyoutType) {
            paramSvContrastAmount = new RelativeLayout.LayoutParams(DensityUtil.dip2px(this, 100),
                    DensityUtil.dip2px(this, 100));
            rlSvContrastAmount.setLayoutParams(paramSvContrastAmount);
            paramSvFollowerAmount = new RelativeLayout.LayoutParams(DensityUtil.dip2px(this, 130),
                    DensityUtil.dip2px(this, 130));
            rlSvFollowerAmount.setLayoutParams(paramSvFollowerAmount);
            paramSvScoreAmount = new RelativeLayout.LayoutParams(DensityUtil.dip2px(this, 100),
                    DensityUtil.dip2px(this, 100));
            rlSvScoreAmount.setLayoutParams(paramSvScoreAmount);
        } else {
            paramSvContrastAmount = new RelativeLayout.LayoutParams(DensityUtil.dip2px(this, 130),
                    DensityUtil.dip2px(this, 130));
            rlSvContrastAmount.setLayoutParams(paramSvContrastAmount);
            paramSvFollowerAmount = new RelativeLayout.LayoutParams(DensityUtil.dip2px(this, 110),
                    DensityUtil.dip2px(this, 110));
            rlSvFollowerAmount.setLayoutParams(paramSvFollowerAmount);
            paramSvScoreAmount = new RelativeLayout.LayoutParams(DensityUtil.dip2px(this, 110),
                    DensityUtil.dip2px(this, 110));
            rlSvScoreAmount.setLayoutParams(paramSvScoreAmount);
        }

        rlSvContrastAmount.addView(svContrastAmount);
        // rlSvScoreAmount.addView(svScoreAmount);
    }

    private void removeCircleViews() {
        rlSvContrastAmount.removeAllViews();
        svContrastAmount = null;
        rlSvFollowerAmount.removeAllViews();
        svFollowerAmount = null;
        rlSvScoreAmount.removeAllViews();
        svScoreAmount = null;
    }

    /**
     * 加载数据
     */
    private void loadData() {
        DD.d(TAG, "loadData()");
        // 加载用户数据
        if (isLogined()) {
            loadUserInfo();
        } else if (PrefUtil.getIntValue(Config.PREF_IS_ANONYMOUS) == 1
                || TextUtils.isEmpty(PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN))) {
            // gotoActivity(LoginTransitionActivity.class);
            // finish();
            anonymousReg();
        }
    }

    /**
     * 匿名注册
     */
    private void anonymousReg() {
        DD.d(TAG, "anonymousReg()");

        RequestParams params = new RequestParams(Config.PATH_ANONYMOUS_REG);
        params.setAsJsonContent(true);
        params.setBodyContent("{}");
        x.http().post(params, new CommonCallback<JSONObject>() {
            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                DD.d(TAG, "onError()");
            }

            @Override
            public void onFinished() {
                DD.d(TAG, "onFinished()");
                initFragmentPage(savedInstance);
                initTopNavContainer();
            }

            @Override
            public void onSuccess(JSONObject result) {
                DD.d(TAG, "onSuccess(), result: " + result.toString());

                AnonymousRegEntity entity = JSON.parseObject(result.toString(),
                        AnonymousRegEntity.class);
                if ("0".equals(entity.getStatus())) {
                    PrefUtil.setStringValue(Config.PREF_ACCESS_TOKEN, entity.getAccessToken());
                    PrefUtil.setIntValue(Config.PREF_USER_ID, entity.getUserId());
                    PrefUtil.setIntValue(Config.PREF_IS_ANONYMOUS, 1);
                }
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
                DD.d(TAG, "用户信息数据.......................onSuccess(), result: " + result);

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
                                civBigHead.setImageBitmap(bitmap);
                            }
                        });
                etNickname.setText(userInfoEntity.getNickname());
                etSignature.setText(userInfoEntity.getUserSignature());
                etJobTitle.setText(userInfoEntity.getJobTitle());
                tvBorn.setText(CommonUtil.getShortFormatDateString(userInfoEntity.getBirthday()));
                tvGender.setText(Config.getGender(userInfoEntity.getUserSex()));
                etEmail.setText(userInfoEntity.getEmail());
                loadCircleData(userInfoEntity);
            }
        });
    }

    /**
     * 设置用户数据
     */
    private void setUserInfo() {
        DD.d(TAG, "setUserInfo()");

        final String nickname = etNickname.getText().toString().replaceAll(" ", "");
        final String userSignature = etSignature.getText().toString();
        final String jobTitle = etJobTitle.getText().toString();
        final String email = etEmail.getText().toString();

        RequestParams params = new RequestParams(Config.PATH_SET_INFO);
        params.setAsJsonContent(true);
        params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
        params.addParameter("nickname", nickname);
        params.addParameter("userSignature", userSignature);
        params.addParameter("birthday", userInfoEntity.getBirthday());
        params.addParameter("jobTitle", jobTitle);
        params.addParameter("userSex", userInfoEntity.getUserSex());
        params.addParameter("email", email);

        x.http().post(params, new CommonCallback<JSONObject>() {
            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
            }

            @Override
            public void onFinished() {
                TT.show(MainActivity.this, "修改成功");
            }

            @Override
            public void onSuccess(JSONObject result) {
                DD.d(TAG, "onSuccess(), result: " + result);

                StatusResultEntity entity = JSON.parseObject(result.toString(),
                        StatusResultEntity.class);
                if ("0".equals(entity.getStatus())) {
                    userInfoEntity.setNickname(nickname);
                    userInfoEntity.setUserSignature(userSignature);
                    userInfoEntity.setJobTitle(jobTitle);
                    userInfoEntity.setEmail(email);
                    PrefUtil.setStringValue(Config.PREF_NICKNAME, nickname);
                    PrefUtil.setStringValue(Config.PREF_USER_SIGNATURE, userSignature);
                    PrefUtil.setStringValue(Config.PREF_JOB_TITLE, jobTitle);
                    PrefUtil.setStringValue(Config.PREF_EMAIL, email);
                    PrefUtil.setStringValue(Config.PREF_BIRTHDAY, userInfoEntity.getBirthday());
                    PrefUtil.setIntValue(Config.PREF_USER_SEX, userInfoEntity.getUserSex());

                    initUserBoardView();
                }
            }
        });
    }

    /**
     * 从个人面板返回的时候，个人信息是否被修改
     */
    private boolean hasUserInfoModified() {
        if (userInfoEntity != null) {
            final String nickname = etNickname.getText().toString();
            final String userSignature = etSignature.getText().toString();
            final String jobTitle = etJobTitle.getText().toString();
            final String email = etEmail.getText().toString();
            final String birthday = userInfoEntity.getBirthday();
            final int gender = userInfoEntity.getUserSex();

            if (!nickname.equals(PrefUtil.getStringValue(Config.PREF_NICKNAME))
                    || !userSignature.equals(PrefUtil.getStringValue(Config.PREF_USER_SIGNATURE))
                    || !jobTitle.equals(PrefUtil.getStringValue(Config.PREF_JOB_TITLE))
                    || !email.equals(PrefUtil.getStringValue(Config.PREF_EMAIL))
                    || !birthday.equals(PrefUtil.getStringValue(Config.PREF_BIRTHDAY))
                    || gender != PrefUtil.getIntValue(Config.PREF_USER_SEX)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        DD.d(TAG, "takePhoto()");

        // 拍照获取
        Intent intentCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intentCapture, REQUEST_CODE_CAPTURE);
    }

    /**
     * 选择照片
     */
    private void selectPhoto() {
        DD.d(TAG, "selectPhoto()");

        // 从相册选取
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK);
    }

    /**
     * 裁剪图片
     */
    private void cutPicture(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 320);
        intent.putExtra("outputY", 320);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUEST_CODE_CUT);
    }

    public int getTopContainerHeight() {
        return llTopContainer.getHeight();
    }

    TranslateAnimation taShowTop;
    TranslateAnimation taShowBottom;
    TranslateAnimation taHideTop;
    TranslateAnimation taHideBottom;

    /**
     * 显示浮动视图
     */
    public void showFloatingView() {
        if (llTopContainer.getVisibility() == View.VISIBLE
                && rlBottomContainer.getVisibility() == View.VISIBLE) {
            return;
        }

        if (taShowTop == null) {
            taShowTop = new TranslateAnimation(0, 0, -llTopContainer.getHeight(), 0);
            taShowTop.setDuration(250);
            llTopContainer.setAnimation(taShowTop);
            taShowTop.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    llTopContainer.setVisibility(View.VISIBLE);
                }
            });
        }
        llTopContainer.startAnimation(taShowTop);

        if (taShowBottom == null) {
            taShowBottom = new TranslateAnimation(0, 0, rlBottomContainer.getHeight(), 0);
            taShowBottom.setDuration(250);
            rlBottomContainer.setAnimation(taShowBottom);
            taShowBottom.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    rlBottomContainer.setVisibility(View.VISIBLE);
                }
            });
        }
        rlBottomContainer.startAnimation(taShowBottom);
    }

    /**
     * 隐藏浮动视图
     */
    private void hideFloatingView() {
        if (llTopContainer.getVisibility() == View.GONE
                && rlBottomContainer.getVisibility() == View.GONE) {
            return;
        }

        if (taHideTop == null) {
            taHideTop = new TranslateAnimation(0, 0, 0, -llTopContainer.getHeight());
            taHideTop.setDuration(250);
            llTopContainer.setAnimation(taHideTop);
            taHideTop.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    llTopContainer.setVisibility(View.GONE);
                }
            });
        }
        llTopContainer.startAnimation(taHideTop);

        if (taHideBottom == null) {
            taHideBottom = new TranslateAnimation(0, 0, 0, rlBottomContainer.getHeight());
            taHideBottom.setDuration(250);
            rlBottomContainer.setAnimation(taHideBottom);
            taHideBottom.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    rlBottomContainer.setVisibility(View.GONE);
                }
            });
        }
        rlBottomContainer.startAnimation(taHideBottom);
    }

    /**
     * 显示个人面板
     */
    private void showUserBoard() {
        DD.d(TAG, "showUserBoard()");
        rlAllFindBoard.setVisibility(View.GONE);
        rlUserBoard.setVisibility(View.VISIBLE);
        rlBottomContainer.setVisibility(View.GONE);
        // 启动动画
        rlBordInAndOut(true);
    }

    private void initUserBoardView() {
        DD.d(TAG, "initUserBoardView()");

        // init view
        llUpdatePasswordContainer.setVisibility(View.GONE);
        btnUpdatePassword.setText("修改密码");
        etOldPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");
        //
        if (userInfoEntity != null) {
            etNickname.setText(userInfoEntity.getNickname());
            etSignature.setText(userInfoEntity.getUserSignature());
            etJobTitle.setText(userInfoEntity.getJobTitle());
            tvBorn.setText(CommonUtil.getShortFormatDateString(userInfoEntity.getBirthday()));
            tvGender.setText(Config.getGender(userInfoEntity.getUserSex()));
            etEmail.setText(userInfoEntity.getEmail());
            tvContrastAmount.setText(String.valueOf(userInfoEntity.getParagraphCount()));
            tvFollowerAmount.setText(String.valueOf(userInfoEntity.getFollowerCount()));
            tvScoreAmount.setText(String.valueOf(userInfoEntity.getScore()));
        }

        ivClearNickname.setVisibility(View.GONE);
        ivClearSignature.setVisibility(View.GONE);
        ivClearJobTitle.setVisibility(View.GONE);
        ivClearEmail.setVisibility(View.GONE);
    }

    /**
     * 隐藏个人面板
     */
    private void hideUserBoard() {
        DD.d(TAG, "hideUserBoard()");
        rlBordInAndOut(false);
    }

    /**
     * 注销
     */
    private void logout() {
        DD.d(TAG, "logout()");

        RequestParams params = new RequestParams(Config.PATH_LOGOUT);
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
                DD.d(TAG, "onSuccess(), result: " + result);
            }
        });
    }

    /**
     * doBlur
     */
    private void doBlur() {
        // DD.d(TAG, "doBlur()");

        flContainer.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                // DD.d(TAG, "onPreDraw()");

                flContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                flContainer.buildDrawingCache();
                Bitmap bitmap = flContainer.getDrawingCache();

                blur(bitmap, llTopContainer, llTopContainer.getTop(), true);
                blur(bitmap, llBottomContainer,
                        flContainer.getHeight() - llBottomContainer.getHeight(), false);

                return false;
            }
        });
    }

    /**
     * blur
     */
    private void blur(Bitmap bitmap, View view, int viewTop, boolean useNativeCode) {
        if (bitmap == null) {
            return;
        }

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        int viewWidth = view.getMeasuredWidth();
        int viewHeight = view.getMeasuredHeight();
        float scaleFactor = 8;
        float radius = 20;

        // DD.d(TAG, "bitmapWidth: " + bitmapWidth + ", bitmapHeight: " + bitmapHeight
        // + ", viewWidth: " + viewWidth + ", viewHeight: " + viewHeight + ", viewTop: "
        // + viewTop);

        Bitmap overlay = Bitmap.createBitmap((int) (viewWidth / scaleFactor),
                (int) (viewHeight / scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(0, -viewTop);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor, 0, viewTop);

        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);

        if (useNativeCode) {
            overlay = blur.blur(overlay, true);
        } else {
            overlay = FastBlur.doBlur(overlay, (int) radius, true);
        }
        view.setBackgroundDrawable(new BitmapDrawable(getResources(), overlay));
    }

    /**
     * 上传头像
     */
    private void uploadAvatar(Uri uri) {
        DD.d(TAG, "uploadAvatar()");

        File file = new File(CommonUtil.getFilePathFromUri(this, avatarUri));
        if (!file.exists()) {
            return;
        }
        String accessToken = PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN);
        final String avatarName = UUID.randomUUID().toString();

        RequestParams params = new RequestParams(Config.PATH_SET_AVATAR_IMAGE);
        params.setMultipart(true);
        params.addParameter("accessToken", accessToken);
        params.addParameter("avatarName", avatarName);
        params.addParameter("userAvatar", file);
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

                StatusResultEntity entity = JSON.parseObject(result.toString(),
                        StatusResultEntity.class);
                if ("0".equals(entity.getStatus())) {
                    PrefUtil.setStringValue(Config.PREF_AVATAR_NAME, avatarName);
                    TT.show(MainActivity.this, "上传成功");
                }
            }
        });
    }

    @Event(value = R.id.ll_top_container)
    private void onTopClick(View v) {
        // do nothing, just for intercept click event
    }

    @Event(value = R.id.rl_bottom_container)
    private void onBottomClick(View v) {
        // do nothing, just for intercept click event
    }

    @Event(value = R.id.rl_head)
    private void onHeadClick(View v) {
        DD.d(TAG, "onHeadClick()");

        if (isLogined()) {
            showUserBoard();
        } else {
            gotoActivity(LoginTransitionActivity.class);
            finish();
        }
    }

    @Event(value = R.id.iv_msg)
    private void onMsgClick(View v) {
        DD.d(TAG, "onMsgClick()");

        if (!isLogined()) {
            gotoActivity(LoginTransitionActivity.class);
            finish();
            return;
        }
        ivMsg.setImageResource(R.drawable.msg_unreceive);
        gotoActivity(MessageActivity.class);
    }

    @Event(value = R.id.v_post)
    private void onPostClick(View v) {
        DD.d(TAG, "onPostClick()");

        if (!isLogined()) {
            gotoActivity(LoginTransitionActivity.class);
            finish();
            return;
        }
        Intent intent = new Intent();
        intent.setClass(this, PostActivity.class);
        startActivityForResult(intent, REQUEST_CODE_POST);
    }

    @Event(value = R.id.rl_left)
    private void onLeftClick(View v) {
        DD.d(TAG, "onLeftClick()");

        switchPage(FRAG_MAIN_FLAG);
        if (fragMain != null && fragMain.isVisible()) {
            fragMain.scrollTop();
        }
    }

    @Event(value = R.id.rl_right)
    private void onRightClick(View v) {
        DD.d(TAG, "onRightClick()");

        if (!isLogined()) {
            gotoActivity(LoginTransitionActivity.class);
            finish();
            return;
        }

        // 如果上次页面停留在以下相应页面，则此次切换进行恢复
        if (MainActivity.LAST_PAGE == -1) {
            switchPage(FRAG_PERSON_FLAG);
        } else {
            switchToMineFragAndLoadSpecifiedTypeData(MainActivity.LAST_PAGE);
        }
    }

    // 所有话题面板相关 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    @Event(value = R.id.tv_find)
    private void onFindClick(View v) {
        DD.d(TAG, "onFindClick()");


        if (listFindBanner.isEmpty()) {
            // 加载banner数据
            loadFindBanner();
        }

        TextPaint tpSelected = tvSelected.getPaint();
        tpSelected.setFakeBoldText(false);
        TextPaint tpRichInContent = tvRichInContent.getPaint();
        tpRichInContent.setFakeBoldText(false);
        TextPaint tpAll = tvAll.getPaint();
        tpAll.setFakeBoldText(false);
        TextPaint tpFind = tvFind.getPaint();
        tpFind.setFakeBoldText(true);
        tvSelected.setTextColor(getResources().getColor(R.color.text_gray));
        tvRichInContent.setTextColor(getResources().getColor(R.color.text_gray));
        tvAll.setTextColor(getResources().getColor(R.color.text_gray));

        tvFind.setTextColor(getResources().getColor(R.color.main_top_title_text));
        rlAllFindBoard.setVisibility(View.VISIBLE);
        llBottomContainer.setVisibility(View.VISIBLE);
        gvContent.setSelection(0);
        // refreshTopicFromServer();
        crlContainer.refreshWithoutAnim();
    }

    /**
     * 从服务器刷新话题数据
     */

    private void refreshFindFromServer() {
        DD.d(TAG, "refreshFindFromServer()");

        RequestParams params = new RequestParams(Config.PATH_GET_TOPIC);
        params.setAsJsonContent(true);
        params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
        params.addParameter("from", 0);
        params.addParameter("rows", 40);
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

                TopicResultEntity entity = JSON.parseObject(result.toString(),
                        TopicResultEntity.class);
                if ("0".equals(entity.getStatus())) {
                    listTopic.clear();
                    listTopic.addAll(entity.getTopics());
                }
            }
        });
    }

    /**
     * 从服务器加载更多话题数据
     */
    private void loadMoreFindFromServer() {
        DD.d(TAG, "loadMoreFindFromServer()");

        RequestParams params = new RequestParams(Config.PATH_GET_TOPIC);
        params.setAsJsonContent(true);
        params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
        params.addParameter("from", listTopic.size());
        params.addParameter("rows", 40);
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

                TopicResultEntity entity = JSON.parseObject(result.toString(),
                        TopicResultEntity.class);
                if ("0".equals(entity.getStatus())) {
                    listTopic.addAll(entity.getTopics());
                }
            }
        });
    }

    @Event(value = R.id.rl_all_find_board)
    private void onAllTopicBoardClick(View v) {
        // do nothing, just for intercept click event
    }

    @Event(value = R.id.gv_content, type = AdapterView.OnItemClickListener.class)
    private void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DD.d(TAG, "onItemClick(), position: " + position);

        Intent intent = new Intent();
        intent.setClass(this, TopicSpecifiedListActivity.class);
        intent.putExtra("topicName", listTopic.get(position).getTopicName());
        gotoActivity(intent);
    }

    // 所有话题面板相关 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    // 个人面板相关 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    @Event(value = R.id.rl_user_board)
    private void onUserBoardClick(View v) {
        DD.d(TAG, "onUserBoardClick()");

        if (llUpdatePasswordContainer.getVisibility() == View.VISIBLE) {
            llUpdatePasswordContainer.setVisibility(View.GONE);
        } else if (lastVisible) {
            hideSoftInput(this, rlUserBoard);
        } else {
            hideUserBoard();
        }
    }

    @Event(value = R.id.civ_big_head)
    private void onBigHeadClick(View v) {
        DD.d(TAG, "onBigHeadClick()");

        if (lastVisible) {
            hideSoftInput(this, etNickname);
        }
        bpwSelectHead.show();
    }

    @Event(value = R.id.ll_info_container)
    private void onInfoContainerClick(View v) {
        DD.d(TAG, "onInfoContainerClick()");

        // do nothing, just for intercepting click event
    }

    //个人页面上的返回键
    @Event(value = R.id.rl_back)
    private void onBackClick(View v) {
        DD.d(TAG, "onBackClick()");

        if (llUpdatePasswordContainer.getVisibility() == View.VISIBLE) {
            llUpdatePasswordContainer.setVisibility(View.GONE);
        } else if (lastVisible) {
            hideSoftInput(this, rlUserBoard);
        } else {
            // 判断邮箱格式
            String email = etEmail.getText().toString();
            if (!TextUtils.isEmpty(email)) {
                Pattern p = Pattern.compile(Config.PATTERN_EMAIL);
                Matcher m = p.matcher(email);
                if (!m.find()) {
                    //TT.show(this, "邮箱不符合规则");
                    return;
                }
            }

            hideUserBoard();
            if (hasUserInfoModified()) {
                setUserInfo();
            }
        }
        switchPage(FRAG_PERSON_FLAG);
        //显示底部
        rlBottomContainer.setVisibility(View.VISIBLE);
    }

    @Event(value = R.id.tv_born)
    private void onBornClick(View v) {
        DD.d(TAG, "onBornClick()");

        int year = 1971;
        int month = 0;
        int day = 1;
        String born = tvBorn.getText().toString();
        if (TextUtils.isEmpty(born)) {
            born = "1971-01-01";
        } else {
            year = CommonUtil.getYear(born);
            month = CommonUtil.getMonth(born);
            day = CommonUtil.getDay(born);
        }
        DD.d(TAG, "year: " + year + ", month: " + month + ", day: " + day);
        new DatePickerDialog(this, new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                DD.d(TAG, "onDateSet(), year: " + year + ", month: " + monthOfYear + ", day: "
                        + dayOfMonth);
                String s = CommonUtil.getYYYYMMDDDate(year, ++monthOfYear, dayOfMonth);
                updateBornDate(s);
            }
        }, year, month, day).show();
    }

    //新写的返回键监听
    @Event(value = R.id.new_rl_back)
    private void onNewBackClick(View v) {
        MainActivity.LAST_PAGE = -1;
        onRightClick(null);
        llTopContainer.setVisibility(View.GONE);
    }

    /**
     * 更新出生日期
     */
    private void updateBornDate(String bornDate) {
        DD.d(TAG, "updateBornDate(), bornDate: " + bornDate);

        tvBorn.setText(bornDate);
        userInfoEntity.setBirthday(bornDate + " 00:00:00");
    }

    @Event(value = R.id.tv_gender)
    private void onGenderClick(View v) {
        DD.d(TAG, "onGenderClick()");

        if (lastVisible) {
            hideSoftInput(this, tvGender);
        }
        bpwGenderSelector.show();
    }

    @Event(value = R.id.iv_clear_nickname)
    private void onClearNicknameClick(View v) {
        DD.d(TAG, "onClearNickname()");

        etNickname.setText("");
    }

    @Event(value = R.id.iv_clear_signature)
    private void onClearSignature(View v) {
        DD.d(TAG, "onClearSignature()");

        etSignature.setText("");
    }

    @Event(value = R.id.iv_clear_job_title)
    private void onClearJobTitle(View v) {
        DD.d(TAG, "onClearJobTitle()");

        etJobTitle.setText("");
    }

    @Event(value = R.id.iv_clear_email)
    private void onClearEmailClick(View v) {
        DD.d(TAG, "onClearEmail()");

        etEmail.setText("");
    }

    @Event(value = R.id.btn_update_password)
    private void onUpdatePasswordClick(View v) {
        DD.d(TAG, "onUpdatePasswordClick()");

        if (llUpdatePasswordContainer.getVisibility() == View.VISIBLE) {
            doUpdatePassword();
        } else {
            llUpdatePasswordContainer.setVisibility(View.VISIBLE);
            btnUpdatePassword.setText("确定");
        }
    }

    /**
     * 准备修改密码
     */
    private void doUpdatePassword() {
        DD.d(TAG, "doUpdatePassword()");

        String oldPassword = etOldPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)
                || TextUtils.isEmpty(confirmPassword)) {
            TT.show(this, "密码不能为空");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            TT.show(this, "两次密码不一致");
            return;
        }

        if (newPassword.length() < 6) {
            TT.show(this, "新密码长度小于6位");
            return;
        }

        if (newPassword.equals(oldPassword)) {
            TT.show(this, "新密码不能与旧密码重复");
            return;
        }

        updatePassword(oldPassword, newPassword);
    }

    /**
     * 更新密码
     */
    private void updatePassword(String oldPassword, String newPassword) {
        DD.d(TAG, "updatePassword(), oldPassword: " + oldPassword + ", newPassword: " + newPassword);

        RequestParams params = new RequestParams(Config.PATH_SET_PASSWORD);
        params.setAsJsonContent(true);
        params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
        params.addParameter("password", CommonUtil.md5(oldPassword));
        params.addParameter("newPassword", CommonUtil.md5(newPassword));
        x.http().post(params, new CommonCallback<JSONObject>() {
            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
            }

            @Override
            public void onFinished() {
                TT.show(MainActivity.this, "修改成功");
            }

            @Override
            public void onSuccess(JSONObject result) {
                DD.d(TAG, "onSuccess(), result: " + result.toString());

                StatusResultEntity entity = JSON.parseObject(result.toString(),
                        StatusResultEntity.class);
                if ("0".equals(entity.getStatus())) {
                    TT.show(MainActivity.this, "修改密码成功");
                    llUpdatePasswordContainer.setVisibility(View.GONE);
                    btnUpdatePassword.setText("修改密码");
                    etOldPassword.setText("");
                    etNewPassword.setText("");
                    etConfirmPassword.setText("");
                } else {
                    TT.show(MainActivity.this, "修改密码失败");
                }
            }
        });
    }

    @Event(value = R.id.btn_clear_cache)
    private void onClearCacheClick(View v) {
        DD.d(TAG, "onClearCacheClick()");

        long totalCacheSize = DataCleanManager.getTotalCacheLongSize(this);
        if (totalCacheSize == 0) {
            TT.show(this, "已经没有需要清理的缓存了");
            return;
        }

        final MessageWithOkCancelFragment2 messageFrag = new MessageWithOkCancelFragment2(
                new MessageWithOkCancelFragment2.Callback() {
                    @Override
                    public void onOk() {
                        DataCleanManager.clearAllCache(MainActivity.this);
                    }

                    @Override
                    public void onCancel() {
                    }
                }, R.layout.frag_message_with_ok_cancel_clear_cache);
        messageFrag.setOnCallback2(new MessageWithOkCancelFragment2.Callback2() {
            @Override
            public void onCreated() {
                try {
                    messageFrag.setContent(DataCleanManager.getTotalCacheSize(MainActivity.this));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        messageFrag.show(getSupportFragmentManager(), "");
    }

    @Event(value = R.id.btn_logout)
    private void onLogoutClick(View v) {
        DD.d(TAG, "onLogoutClick()");

        new MessageWithOkCancelFragment2(new MessageWithOkCancelFragment2.Callback() {
            @Override
            public void onOk() {
                logout();
                PrefUtil.setBooleanValue(Config.PREF_IS_LOGINED, false);
                PrefUtil.setStringValue(Config.PREF_ACCESS_TOKEN, "");
                gotoActivity(LoginTransitionActivity.class);
                finish();
            }

            @Override
            public void onCancel() {
            }
        }, R.layout.frag_message_with_ok_cancel_logout).show(getSupportFragmentManager(), "");
    }

    @Event(value = R.id.tv_feedback)
    private void OnFeedback(View v) {
        Intent intent = new Intent();
        intent.setClass(this, SendPrivateMessagesActivity.class);
        intent.putExtra("userId", 65130);
        intent.putExtra("head", "");
        intent.putExtra("nickname", "无比官方");
        startActivity(intent);
    }

    @Event(value = R.id.iv_close_password_container)
    private void onClosePasswordContainerClick(View v) {
        DD.d(TAG, "onClosePasswordContainerClick()");

        llUpdatePasswordContainer.setVisibility(View.GONE);
        btnUpdatePassword.setText("修改密码");
    }

    /**
     * 显示输入法
     */
    private void showSoftInput(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(etSearchParagraph, InputMethodManager.SHOW_FORCED);
    }

    /**
     * 隐藏输入法
     */
    private void hideSoftInput(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // 个人面板相关 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    @Event(value = {R.id.et_search_paragraph, R.id.et_search_user}, type = OnEditorActionListener.class)
    private boolean onSearchEditorAction(TextView v, int actionId, KeyEvent event) {
        DD.d(TAG, "onEditorAction()");

        if (EditorInfo.IME_ACTION_SEARCH == actionId) {
            doSearch();
        }
        return true;
    }

    ;

    private void doSearch() {
        DD.d(TAG, "doSearch()");

        if (etSearchParagraph.hasFocus()) {
            hideSoftInput(this, etSearchParagraph);

            String paragraphName = etSearchParagraph.getText().toString();
            if (TextUtils.isEmpty(paragraphName)) {
                return;
            }
            Intent intent = new Intent();
            intent.setClass(this, SearchParagraphActivity.class);
            intent.putExtra("keyword", paragraphName);
            flag = true;
            gotoActivity(intent);
        } else if (etSearchUser.hasFocus()) {
            hideSoftInput(this, etSearchUser);

            String username = etSearchUser.getText().toString();
            if (TextUtils.isEmpty(username)) {
                return;
            }
            Intent intent = new Intent();
            intent.setClass(this, SearchUserActivity.class);
            intent.putExtra("keyword", username);
            flag = true;
            gotoActivity(intent);
        }
    }

    // 标识，用户从搜索结果页面回来时，隐藏搜索面板，并清空搜索框
    boolean flag = false;

    @Override
    protected void onResume() {
        DD.d(TAG, "onResume()");

        if (flag) {
//			rlSearchBoard.setVisibility(View.GONE);
            etSearchParagraph.setText("");
            etSearchUser.setText("");
            flag = false;
        }
        super.onResume();
    }

    // 搜索面板相关 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    // RefreshingAnimationView相关 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    public void showRefreshingAnimationView() {
        ravRefreshingView.setRefreshing(true);
    }

    public void hideRefreshingAnimationView() {
        ravRefreshingView.setRefreshing(false);
    }

    // RefreshingAnimationView相关 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    @Override
    public void onMoveUp() {
        DD.d(TAG, "onMoveUp()");
        hideFloatingView();
    }

    @Override
    public void onMoveDown() {
        DD.d(TAG, "onMoveDown()");
        showFloatingView();
    }

    private long lastBlurTime;
    private long currentTime;

    @Override
    public void onVerticalMoving() {
        currentTime = System.currentTimeMillis();
        if (currentTime - lastBlurTime < 100) {
            return;
        }
        lastBlurTime = currentTime;

        doBlur();
    }

    // 退出程序时间戳
    private long EXIT_WAITING_TIME = 2000;
    private long backkeyPressedTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (!this.isFinishing()) {
                if (rlUserBoard.getVisibility() == View.VISIBLE) {
                    rlBordInAndOut(false);
                } else {
                    if ((System.currentTimeMillis() - backkeyPressedTime) < EXIT_WAITING_TIME) {
                        finish();
                    } else {
                        backkeyPressedTime = System.currentTimeMillis();
                        TT.show(this, "再按一次退出程序");
                    }
                }
            }
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
        QDataModule.getInstance().removeMessageReceiveListener(this);
        QDataModule.getInstance().removeOnFindGoneListener(this);
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        DD.d(TAG, "onLocationChanged()");

        if (aMapLocation != null) {
            String city = aMapLocation.getCity();
            String district = aMapLocation.getDistrict();
            PrefUtil.setStringValue(Config.PREF_USER_CITY, city);
            PrefUtil.setStringValue(Config.PREF_USER_DISTRICT, district);
        }
    }

    // 根据控件的选择，重新设置定位参数
    private void initOption() {
        DD.d(TAG, "initOption()");

        locationOption.setGpsFirst(false);
        // 单次定位
        locationOption.setOnceLocation(true);
        // 设置是否需要显示地址信息
        locationOption.setNeedAddress(true);
    }

    // 发布作品成功后，在该页面显示刷新动画并刷新最新数据
    private void onPostSuccess() {
        DD.d(TAG, "onPostSuccess()");

        showRefreshingAnimationView();
        if (currentFragment == FRAG_MAIN_FLAG) {
            fragMain.onPostSuccess();
        } else {
            fragMine.onPostSuccess();
        }
    }

    /**
     * 切换到 mineFragment ，并且加载指定类型的数据
     */
    public void switchToMineFragAndLoadSpecifiedTypeData(int current) {
        DD.d(TAG, "switchToMineFragAndLoadSpecifiedTypeData()");

        currentFragment = FRAG_MINE_FLAG;
        switchPage(currentFragment);
        fragMine.setCurrentPage(current);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_PICK:
                    if (data != null) {
                        avatarUri = data.getData();
                        // 裁剪图片
                        cutPicture(avatarUri);
                    }
                    break;
                case REQUEST_CODE_CAPTURE:
                    if (data != null) {
                        if (data.getData() != null) {
                            avatarUri = data.getData();
                        } else {
                            Bundle bundle = data.getExtras();
                            Bitmap bitmap = (Bitmap) bundle.get("data");
                            avatarUri = Uri.parse(MediaStore.Images.Media.insertImage(
                                    getContentResolver(), bitmap, null, null));
                        }
                        // 裁剪图片
                        cutPicture(avatarUri);
                    }
                    cutPicture(avatarUri);
                    break;
                case REQUEST_CODE_CUT:
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap bitmap = extras.getParcelable("data");
                        if (bitmap != null) {
                            civBigHead.setImageBitmap(bitmap);
                            civHead.setImageBitmap(bitmap);
                            uploadAvatar(avatarUri);
                        }
                    }
                    break;
                case REQUEST_CODE_POST:
                    onPostSuccess();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        try {
            UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private void loadCircleData(UserInfoEntity userInfoEntity) {

        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        View v = null;
        if (userInfoEntity.getParagraphCount() <= userInfoEntity.getFollowerCount()) {
            v = getLayoutInflater().inflate(R.layout.circle_defined_layout, null);
            circleLyoutType = false;
        } else {
            v = getLayoutInflater().inflate(R.layout.circle_defined_layout2, null);
            circleLyoutType = true;
        }
        rlContrastAmount = (RelativeLayout) v.findViewById(R.id.rl_contrast_amount);
        rlSvContrastAmount = (RelativeLayout) v.findViewById(R.id.rl_sv_contrast_amount);
        tvContrastAmount = (TextView) v.findViewById(R.id.tv_contrast_amount);
        tvContrastAmountName = (TextView) v.findViewById(R.id.tv_contrast_amount_name);

        rlFollowerAmount = (RelativeLayout) v.findViewById(R.id.rl_follower_amount);
        rlSvFollowerAmount = (RelativeLayout) v.findViewById(R.id.rl_sv_follower_amount);
        tvFollowerAmount = (TextView) v.findViewById(R.id.tv_follower_amount);
        tvFollowerAmountName = (TextView) v.findViewById(R.id.tv_follower_amount_name);

        rlScoreAmount = (RelativeLayout) v.findViewById(R.id.rl_score_amount);
        rlSvScoreAmount = (RelativeLayout) v.findViewById(R.id.rl_sv_score_amount);
        tvScoreAmount = (TextView) v.findViewById(R.id.tv_score_amount);
        tvScoreAmountName = (TextView) v.findViewById(R.id.tv_score_amount_name);
        tvContrastAmount.setText(String.valueOf(userInfoEntity.getParagraphCount()));
        tvFollowerAmount.setText(String.valueOf(userInfoEntity.getFollowerCount()));
        tvScoreAmount.setText(String.valueOf(userInfoEntity.getScore()));
        setTextSize(userInfoEntity);
        rlCircleRoot.addView(v, param);
    }

    private void rlBordInAndOut(boolean b) {
        if (b) {
            addCircleViews();
            setCircleColor();
            ScaleAnimation scaleAnimation = (ScaleAnimation) AnimationUtil.getScaleInAnimation(600);
            scaleAnimation.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ScaleAnimation scaleAnimation2 = (ScaleAnimation) AnimationUtil
                            .getScaleInAnimation2(2000);
                    rlContrastAmount.startAnimation(scaleAnimation2);
                }
            });
            rlContrastAmount.startAnimation(scaleAnimation);

            rlSvFollowerAmount.addView(svFollowerAmount);
            rlSvScoreAmount.addView(svScoreAmount);
            ScaleAnimation scaleAnimationSvFollowerAmount = (ScaleAnimation) AnimationUtil
                    .getScaleInAnimation(600);
            scaleAnimationSvFollowerAmount.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ScaleAnimation scaleAnimationSvFollowerAmount2 = (ScaleAnimation) AnimationUtil
                            .getScaleInAnimation2(2000);
                    rlFollowerAmount.startAnimation(scaleAnimationSvFollowerAmount2);
                }
            });
            ScaleAnimation scaleAnimationScoreAmount = (ScaleAnimation) AnimationUtil
                    .getScaleInAnimation(600);
            scaleAnimationScoreAmount.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ScaleAnimation scaleAnimationScoreAmount2 = (ScaleAnimation) AnimationUtil
                            .getScaleInAnimation2(2000);
                    rlScoreAmount.startAnimation(scaleAnimationScoreAmount2);
                }
            });
            rlFollowerAmount.startAnimation(scaleAnimationSvFollowerAmount);
            rlScoreAmount.startAnimation(scaleAnimationScoreAmount);
        } else {
            outCircleTransfer();
        }
    }

    private void setCircleColor() {
        int n = (int) (Math.random() * 6 + 1);
        switch (n) {
            case 1:
                svContrastAmount.setCricleColor(getResources().getColor(R.color.login_blue));
                tvContrastAmount.setTextColor(svContrastAmount.getCricleColor());
                tvContrastAmountName.setTextColor(svContrastAmount.getCricleColor());

                svFollowerAmount.setCricleColor(getResources().getColor(R.color.login_yellow));
                tvFollowerAmount.setTextColor(svFollowerAmount.getCricleColor());
                tvFollowerAmountName.setTextColor(svFollowerAmount.getCricleColor());

                svScoreAmount.setCricleColor(getResources().getColor(R.color.login_red));
                tvScoreAmount.setTextColor(svScoreAmount.getCricleColor());
                tvScoreAmountName.setTextColor(svScoreAmount.getCricleColor());
                break;
            case 2:
                svContrastAmount.setCricleColor(getResources().getColor(R.color.login_blue));
                tvContrastAmount.setTextColor(svContrastAmount.getCricleColor());
                tvContrastAmountName.setTextColor(svContrastAmount.getCricleColor());

                svFollowerAmount.setCricleColor(getResources().getColor(R.color.login_red));
                tvFollowerAmount.setTextColor(svFollowerAmount.getCricleColor());
                tvFollowerAmountName.setTextColor(svFollowerAmount.getCricleColor());

                svScoreAmount.setCricleColor(getResources().getColor(R.color.login_yellow));
                tvScoreAmount.setTextColor(svScoreAmount.getCricleColor());
                tvScoreAmountName.setTextColor(svScoreAmount.getCricleColor());
                break;
            case 3:
                svContrastAmount.setCricleColor(getResources().getColor(R.color.login_red));
                tvContrastAmount.setTextColor(svContrastAmount.getCricleColor());
                tvContrastAmountName.setTextColor(svContrastAmount.getCricleColor());

                svFollowerAmount.setCricleColor(getResources().getColor(R.color.login_blue));
                tvFollowerAmount.setTextColor(svFollowerAmount.getCricleColor());
                tvFollowerAmountName.setTextColor(svFollowerAmount.getCricleColor());

                svScoreAmount.setCricleColor(getResources().getColor(R.color.login_yellow));
                tvScoreAmount.setTextColor(svScoreAmount.getCricleColor());
                tvScoreAmountName.setTextColor(svScoreAmount.getCricleColor());
                break;
            case 4:
                svContrastAmount.setCricleColor(getResources().getColor(R.color.login_red));
                tvContrastAmount.setTextColor(svContrastAmount.getCricleColor());
                tvContrastAmountName.setTextColor(svContrastAmount.getCricleColor());

                svFollowerAmount.setCricleColor(getResources().getColor(R.color.login_yellow));
                tvFollowerAmount.setTextColor(svFollowerAmount.getCricleColor());
                tvFollowerAmountName.setTextColor(svFollowerAmount.getCricleColor());

                svScoreAmount.setCricleColor(getResources().getColor(R.color.login_blue));
                tvScoreAmount.setTextColor(svScoreAmount.getCricleColor());
                tvScoreAmountName.setTextColor(svScoreAmount.getCricleColor());
                break;
            case 5:
                svContrastAmount.setCricleColor(getResources().getColor(R.color.login_yellow));
                tvContrastAmount.setTextColor(svContrastAmount.getCricleColor());
                tvContrastAmountName.setTextColor(svContrastAmount.getCricleColor());

                svFollowerAmount.setCricleColor(getResources().getColor(R.color.login_red));
                tvFollowerAmount.setTextColor(svFollowerAmount.getCricleColor());
                tvFollowerAmountName.setTextColor(svFollowerAmount.getCricleColor());

                svScoreAmount.setCricleColor(getResources().getColor(R.color.login_blue));
                tvScoreAmount.setTextColor(svScoreAmount.getCricleColor());
                tvScoreAmountName.setTextColor(svScoreAmount.getCricleColor());
                break;
            case 6:
                svContrastAmount.setCricleColor(getResources().getColor(R.color.login_yellow));
                tvContrastAmount.setTextColor(svContrastAmount.getCricleColor());
                tvContrastAmountName.setTextColor(svContrastAmount.getCricleColor());

                svFollowerAmount.setCricleColor(getResources().getColor(R.color.login_blue));
                tvFollowerAmount.setTextColor(svFollowerAmount.getCricleColor());
                tvFollowerAmountName.setTextColor(svFollowerAmount.getCricleColor());

                svScoreAmount.setCricleColor(getResources().getColor(R.color.login_red));
                tvScoreAmount.setTextColor(svScoreAmount.getCricleColor());
                tvScoreAmountName.setTextColor(svScoreAmount.getCricleColor());
                break;
            default:
                break;
        }
    }

    private void outCircleTransfer() {
        ScaleAnimation scaleAnimation = (ScaleAnimation) AnimationUtil.getScaleOutAnimation();
        scaleAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rlUserBoard.setVisibility(View.GONE);
                removeCircleViews();
            }
        });
        int n = (int) (Math.random() * 3 + 1);
        switch (n) {
            case 1:
                rlContrastAmount.startAnimation(scaleAnimation);
                break;
            case 2:
                rlFollowerAmount.startAnimation(scaleAnimation);
                break;
            case 3:
                rlScoreAmount.startAnimation(scaleAnimation);
                break;
            default:
                break;
        }
    }

    private void setTextSize(UserInfoEntity userInfoEntity) {
        if (userInfoEntity != null) {
            if (userInfoEntity.getParagraphCount() > userInfoEntity.getFollowerCount()) {
                tvContrastAmount.setTextSize(DensityUtil.dip2px(this, 15));
                if (userInfoEntity.getFollowerCount() < userInfoEntity.getScore()) {
                    tvFollowerAmount.setTextSize(DensityUtil.dip2px(this, 10));
                    tvScoreAmount.setTextSize(DensityUtil.dip2px(this, 12));
                } else {
                    tvFollowerAmount.setTextSize(DensityUtil.dip2px(this, 10));
                    tvScoreAmount.setTextSize(DensityUtil.dip2px(this, 10));
                }
            }
        } else {
            tvContrastAmount.setTextSize(DensityUtil.dip2px(this, 10));
            tvFollowerAmount.setTextSize(DensityUtil.dip2px(this, 15));
            tvScoreAmount.setTextSize(DensityUtil.dip2px(this, 10));
        }

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_contrast_amount:
            case R.id.rl_follower_amount:
                rlBordInAndOut(false);
                break;
            default:
                break;
        }
    }

    ;

    private void initUmengAnalytics() {
        AnalyticsConfig.setAppkey(MainActivity.this, UMENG_APPKEY);
        PushManager.getInstance().initialize(this.getApplicationContext());
    }


    //收到消息
    @Override
    public void onReceived() {
        ivMsg.setImageResource(R.drawable.msg_receive);
    }

    @Override
    public void onReceivedFindGone() {
        rlAllFindBoard.setVisibility(View.GONE);
    }


    // banner相关 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    private void loadFindBanner() {
        DD.d(TAG, "loadFindBanner()");

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
                DD.d(TAG, "onSuccess(), 重写的result: " + result.toString());

                // 显示banner
                BannerResultEntity entity = JSON.parseObject(result.toString(),
                        BannerResultEntity.class);
                if ("0".equals(entity.getStatus())) {

                    for (BannerEntity be : entity.getBanners()) {
                        //listFindBanner.clear();
                        listFindBanner.add(be);
                    }

                    // 初始化点
                    initDots(listFindBanner.size());// 初始化点

                    //轮播图图片数据集合
                    List<String> imageLists = new ArrayList<String>();

                    for (BannerEntity item : listFindBanner) {
                        //titleLists.add(item.title);// 获取轮播图标题数据
                        imageLists.add(item.getPicName());// 获取轮播图的图片数据
                    }

                    rollView = new RollViewPager(MainActivity.this, dots,
                            new RollViewPager.OnRollViewClickListener() {

                                @Override
                                public void onRollViewClick() {
                                    int bannerPosition = rollView.getCurrentItem();
                                    int bp = bannerPosition % listFindBanner.size();
                                    Intent intent = new Intent();
                                    intent.setClass(MainActivity.this, TopicSpecifiedListActivity.class);
                                    intent.putExtra("topicName", listFindBanner.get(bp).getTagName());
                                    BaseActivity.sGotoActivity(MainActivity.this, intent);

                                }
                            });
                    rollView.setPullRefreshParent(crlContainer);
                    //把轮播图添加到布局中
                    top_news_viewpager.addView(rollView);
                    rollView.setImages(imageLists);
                    //开始轮播图
                    rollView.start();
                }
            }
        });
    }

    // 动态创建轮播图的点
    private void initDots(int size) {
        dots.clear();// 清空之前的所有点
        dots_ll.removeAllViews();// 清空之前的所有点
        for (int i = 0; i < size; i++) {
            View view = new View(MainActivity.this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    DensityUtil.dip2px(MainActivity.this, 5), DensityUtil.dip2px(
                    MainActivity.this, 5));
            params.leftMargin = DensityUtil.dip2px(MainActivity.this, 5);
            view.setLayoutParams(params);
            if (i == 0) {
                view.setBackgroundResource(R.drawable.shape__oval__banner_indicator_selected);
            } else {
                view.setBackgroundResource(R.drawable.shape__oval__banner_indicator_unselected);
            }
            dots_ll.addView(view);
            dots.add(view);
        }
    }

    // banner相关 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    public void updateName(String text) {

        pageName.setText(text);
    }

    public void showPageName() {
        pageName.setVisibility(View.VISIBLE);
    }

    //显示返回键
    public void showBack() {

        newRlBack.setVisibility(View.VISIBLE);

    }

    // 当点击个人页面的“设置”
    public void onSettingClick() {
        switchPage(FRAG_MAIN_FLAG);
        showUserBoard();
    }
}
