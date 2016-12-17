package cn.incorner.contrast.page;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.BaseFragmentActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.Constant;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.adapter.SelectImageFilterAdapter;
import cn.incorner.contrast.data.entity.ParagraphEntity;
import cn.incorner.contrast.data.entity.PostParagraphEntity;
import cn.incorner.contrast.data.entity.StatusResultEntity;
import cn.incorner.contrast.util.ADIWebUtils;
import cn.incorner.contrast.util.BitMapUtil;
import cn.incorner.contrast.util.CommonUtil;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.DensityUtil;
import cn.incorner.contrast.util.GPUImageFilterTools;
import cn.incorner.contrast.util.GPUImageFilterTools.FilterAdjuster;
import cn.incorner.contrast.util.GPUImageFilterTools.FilterType;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.util.TT;
import cn.incorner.contrast.view.MessageWithOkCancelFragment2;
import cn.incorner.contrast.view.ReboundHorizontalScrollView;
import cn.incorner.contrast.view.RefreshingAnimationView;
import cn.incorner.contrast.view.TouchImageView;
import cn.incorner.contrast.view.TouchImageView.OnImageViewClickListener;
import cn.incorner.contrast.view.WrapFlowViewGroup;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * 创作 页面
 *
 * @author yeshimin
 */
@ContentView(R.layout.activity_post)
public class PostActivity extends BaseFragmentActivity {

    private static final String TAG = "PostActivity";

    public final Pattern PATTERN_COLOR_RGB = Pattern.compile(Config.PATTERN_COLOR_RGB);
    public final Pattern PATTERN_COLOR_INDEX = Pattern.compile(Config.PATTERN_COLOR_INDEX);

    // 布局样式：横版、竖版
    private static final int LAYOUT_STYLE_HORIZONTAL = LinearLayout.VERTICAL;
    private static final int LAYOUT_STYLE_VERTICAL = LinearLayout.HORIZONTAL;

    public static final int PIC_WIDTH = Constant.SCREEN_WIDTH;
    public static final int PIC_HEIGHT = Constant.SCREEN_WIDTH;

    private final int select_left = 0x10; // 选择左边图片
    private final int select_right = 0x11; // 选择右边图片

    private String path_left; // 左边图片路径
    private String path_right; // 右边图片路径

    private Bitmap bitmap_left_old;// 左边图片 原bitmap
    private Bitmap bitmap_right_old;// 右边图片 原bitmap
    private Bitmap bitmap_left_filter;// 左边图片bitmap
    private Bitmap bitmap_right_filter;// 右边图片bitmap

    @ViewInject(value = R.id.sv_container)
    private ScrollView svContainer;
    @ViewInject(value = R.id.ll_image_and_desc_container)
    private LinearLayout llImageAndDescContainer;
    @ViewInject(value = R.id.ll_top_desc_container)
    private LinearLayout llTopDescContainer;
    @ViewInject(value = R.id.ll_bottom_desc_container)
    private LinearLayout llBottomDescContainer;
    @ViewInject(value = R.id.rl_desc_container_1)
    private RelativeLayout rlDescContainer1;
    @ViewInject(value = R.id.rl_desc_container_2)
    private RelativeLayout rlDescContainer2;
    @ViewInject(value = R.id.ll_image_container)
    private LinearLayout llImageContainer;
    @ViewInject(value = R.id.rl_image_container_1)
    private RelativeLayout rlImageContainer1;
    @ViewInject(value = R.id.rl_image_container_2)
    private RelativeLayout rlImageContainer2;
    @ViewInject(value = R.id.iv_image_1)
    private TouchImageView ivImage1;
    @ViewInject(value = R.id.iv_image_2)
    private TouchImageView ivImage2;
    @ViewInject(value = R.id.et_desc_1)
    private EditText etDesc1;
    @ViewInject(value = R.id.et_desc_2)
    private EditText etDesc2;
    @ViewInject(value = R.id.rl_topic_container)
    private RelativeLayout rlTopicContainer;
    @ViewInject(value = R.id.et_topic)
    private EditText etTopic;
    @ViewInject(value = R.id.hsv_selected_tag_container)
    private ReboundHorizontalScrollView hsvSelectedTagContainer;
    @ViewInject(value = R.id.ll_selected_tag_container)
    private LinearLayout llSelectedTagContainer;
    @ViewInject(value = R.id.ll_bottom_container)
    private LinearLayout llBottomContainer;
    @ViewInject(value = R.id.rl_tag_container)
    private RelativeLayout rlTagContainer;
    @ViewInject(value = R.id.wfvg_container)
    private WrapFlowViewGroup wfvgContainer;
    @ViewInject(value = R.id.rl_desc_function_container_1)
    private RelativeLayout rlDescFunctionContainer1;
    @ViewInject(value = R.id.ll_add_comment_1)
    private LinearLayout llAddComment1;
    @ViewInject(value = R.id.ll_add_href_1)
    private LinearLayout llAddHref1;
    @ViewInject(value = R.id.tv_add_href_1)
    private TextView tvAddHref1;
    @ViewInject(value = R.id.ll_clear_desc_1)
    private LinearLayout llClearDesc1;
    @ViewInject(value = R.id.tv_clear_desc_1)
    private TextView tvClearDesc1;
    @ViewInject(value = R.id.ll_original_1)
    private LinearLayout llOriginal1;
    @ViewInject(value = R.id.tv_original_1)
    private TextView tvOriginal1;
    @ViewInject(value = R.id.ll_high_energy_1)
    private LinearLayout llHighEnergy1;
    @ViewInject(value = R.id.tv_high_energy_1)
    private TextView tvHighEnergy1;
    @ViewInject(value = R.id.ll_18_forbid_1)
    private LinearLayout ll18Forbid1;
    @ViewInject(value = R.id.tv_18_forbid_1)
    private TextView tv18Forbid1;
    @ViewInject(value = R.id.rl_desc_function_container_2)
    private RelativeLayout rlDescFunctionContainer2;
    @ViewInject(value = R.id.ll_add_comment_2)
    private LinearLayout llAddComment2;
    @ViewInject(value = R.id.ll_add_href_2)
    private LinearLayout llAddHref2;
    @ViewInject(value = R.id.tv_add_href_2)
    private TextView tvAddHref2;
    @ViewInject(value = R.id.ll_clear_desc_2)
    private LinearLayout llClearDesc2;
    @ViewInject(value = R.id.tv_clear_desc_2)
    private TextView tvClearDesc2;
    @ViewInject(value = R.id.ll_mark_hide_2)
    private LinearLayout llMarkhide;
    @ViewInject(value = R.id.ll_mark_original_2)
    private LinearLayout llMarkOriginal;
    @ViewInject(value = R.id.ll_original_2)
    private LinearLayout llOriginal2;
    @ViewInject(value = R.id.tv_original_2)
    private TextView tvOriginal2;
    @ViewInject(value = R.id.rl_function_container)
    private RelativeLayout rlFunctionContainer;
    @ViewInject(value = R.id.rl_back)
    private RelativeLayout rlBack;
    @ViewInject(value = R.id.rl_delete)
    private RelativeLayout rlDelete;
    @ViewInject(value = R.id.rl_post)
    private RelativeLayout rlPost;
    @ViewInject(value = R.id.rl_rotate)
    private RelativeLayout rlRotate;
    @ViewInject(value = R.id.iv_rotate)
    private ImageView ivRotate;
    @ViewInject(value = R.id.rl_switch_color)
    private RelativeLayout rlSwitchColor;

    // 当前布局样式（默认横版）
    private int currentLayoutStyle = LAYOUT_STYLE_HORIZONTAL;
    // 当前输入焦点
    private View vLastFocus;

    // 标签(属于什么话题)
    private String[] arrTag = {
            "#真相大揭秘", "#萌翻全场", "#撞脸大赛", "#长姿势", "#万万没想到", "#时光机", "#在路上", "#看完想shi", "#内牛满面", "#神奇的脑洞", "#康帅博", "#pos机"
    };
    private String[] newarrtag;

    private PopupWindow popupWindow;
    private SelectImageFilterAdapter imageFilterAdapter;

    private int[] bgColor;
    private int[] textColor;

    private List<String> listTag = new ArrayList<String>();
    private String desc1;
    private String desc2;
    private int color1;
    private int color2;
    private int outerColor1;
    private int outerColor2;
    private String imageName1;
    private String imageName2;
    private File imageFile1;
    private File imageFile2;

    // recreate 相关
    @ViewInject(value = R.id.rl_other_user_image_container_1)
    private RelativeLayout rlOtherUserImageContainer1;
    @ViewInject(value = R.id.iv_other_user_image_1)
    private ImageView ivOtherUserImage1;
    @ViewInject(value = R.id.iv_recreate_1)
    private ImageView ivRecreate1;
    @ViewInject(value = R.id.rl_other_user_image_container_2)
    private RelativeLayout rlOtherUserImageContainer2;
    @ViewInject(value = R.id.iv_other_user_image_2)
    private ImageView ivOtherUserImage2;
    @ViewInject(value = R.id.iv_recreate_2)
    private ImageView ivRecreate2;
    // recreate 兼容部分
    @ViewInject(R.id.rl_compatible_other_user_image_container)
    private RelativeLayout rlCompatibleOtherUserImageContainer;
    @ViewInject(R.id.iv_compatible_other_user_image)
    private ImageView ivCompatibleOtherUserImage;
    @ViewInject(R.id.iv_compatible_recreate)
    private ImageView ivCompatibleRecreate;

    // 是否 编辑
    private boolean isEdit = false;
    // 是否 借题发挥
    private boolean isRecreate = false;
    // 从“话题列表”页面传过来的话题
    private String topicNameFromTopicListPage;
    // 是否使用别人的图片
    private boolean useOtherUserImage1 = false;
    private boolean useOtherUserImage2 = false;
    // 兼容模式 相关
    private boolean isCompatibleMode = false;
    private boolean useCompatibleOtherUserImage = false;
    private String compatibleImageName;
    private File compatibleImageFile;
    private ParagraphEntity paragraphEntity;
    // 还原上一次编辑的数据 相关
    private int lastOrientation = LAYOUT_STYLE_HORIZONTAL;
    private String lastImagePath1;
    private String lastImagePath2;
    private String lastDesc1;
    private String lastDesc2;
    private String lastTag;

    // 滤镜相关
    private GPUImage gpuImage;
    private List<FilterType> listFilter = new ArrayList<FilterType>();
    private int[] arrFilterAdjusterValue = new int[]{-1, -1, 10, 40, 90, -1, 20, 50};

    // 发布动画
    @ViewInject(R.id.rav_refreshing_view)
    private RefreshingAnimationView ravRefreshingView;
    private boolean mShouldOpenMulit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    /**
     * 还原上一次编辑的数据
     */
    private void resumeData() {
        DD.d(TAG, "resumeData()");

        lastOrientation = PrefUtil.getIntValue(Config.PREF_LAST_ORIENTATION);
        lastImagePath1 = PrefUtil.getStringValue(Config.PREF_LAST_IMAGE_PATH_1);
        lastImagePath2 = PrefUtil.getStringValue(Config.PREF_LAST_IMAGE_PATH_2);
        lastDesc1 = PrefUtil.getStringValue(Config.PREF_LAST_DESC_1);
        lastDesc2 = PrefUtil.getStringValue(Config.PREF_LAST_DESC_2);
        lastTag = PrefUtil.getStringValue(Config.PREF_LAST_TAG);

        DD.d(TAG, "lastOrientation: " + lastOrientation);
        DD.d(TAG, "lastImagePath1: " + lastImagePath1);
        DD.d(TAG, "lastImagePath2: " + lastImagePath2);
        DD.d(TAG, "lastDesc1: " + lastDesc1);
        DD.d(TAG, "lastDesc2: " + lastDesc2);
        DD.d(TAG, "lastTag: " + lastTag);

        if (lastOrientation == LAYOUT_STYLE_VERTICAL) {
            switchLayoutStyle();
        }
        if (!TextUtils.isEmpty(lastImagePath1)) {
            x.image().loadDrawable(lastImagePath1, null, new CommonCallback<Drawable>() {
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
                    bitmap_left_old = ((BitmapDrawable) drawable).getBitmap();
                    ivImage1.setBitmap(bitmap_left_old);
                    ivImage1.setVisibility(View.VISIBLE);
                }
            });
        }
        if (!TextUtils.isEmpty(lastImagePath2)) {
            x.image().loadDrawable(lastImagePath2, null, new CommonCallback<Drawable>() {
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
                    bitmap_right_old = ((BitmapDrawable) drawable).getBitmap();
                    ivImage2.setBitmap(bitmap_right_old);
                    ivImage2.setVisibility(View.VISIBLE);
                }
            });
        }
        if (!TextUtils.isEmpty(lastDesc1)) {
            etDesc1.setText(lastDesc1);
            adjustDescSize(lastDesc1, etDesc1);
        }
        if (!TextUtils.isEmpty(lastDesc2)) {
            etDesc2.setText(lastDesc2);
            adjustDescSize(lastDesc2, etDesc2);
        }
        if (!TextUtils.isEmpty(lastTag)) {
            final String[] arrTag = lastTag.split(",");
            final StringBuilder sbTag = new StringBuilder();
            for (final String tag : arrTag) {
                sbTag.append(tag + ":@(0),");
            }
            resumeTag(sbTag.toString());
        }
    }

    /**
     * 借题发挥
     */
    private void initRecreateParagraph() {
        DD.d(TAG, "recreateParagraph()");

        // 从“话题列表”页面传过来的话题
        topicNameFromTopicListPage = getIntent().getStringExtra("topicNameFromTopicListPage");
        if (!TextUtils.isEmpty(topicNameFromTopicListPage)) {
            topicNameFromTopicListPage = "#" + topicNameFromTopicListPage + ":@(0)";
            resumeTag(topicNameFromTopicListPage);
            return;
        }

        paragraphEntity = getIntent().getParcelableExtra("paragraph");
        if (paragraphEntity == null) {
            // 还原上一次编辑的数据，如果有的话
            resumeData();
            return;
        }

        isEdit = getIntent().getBooleanExtra("isEdit", false);
        isRecreate = true;
        useOtherUserImage1 = true;
        useOtherUserImage2 = true;

        // 初始化
        String content = paragraphEntity.getParagraphContent();
        if (content.contains("[up]")) {
            switchLayoutStyle();
        }
        DD.d(TAG, "picName: " + paragraphEntity.getPicName());
        String[] arrPicName = parsePicName(paragraphEntity.getPicName());
        if (arrPicName != null) {
            if (arrPicName.length == 2) {
                isCompatibleMode = false;
                rlCompatibleOtherUserImageContainer.setVisibility(View.GONE);
                rlOtherUserImageContainer1.setVisibility(View.VISIBLE);
                rlOtherUserImageContainer2.setVisibility(View.VISIBLE);
                x.image().bind(ivOtherUserImage1, Config.getContrastFullPath(arrPicName[0]));
                x.image().bind(ivOtherUserImage2, Config.getContrastFullPath(arrPicName[1]));
            } else if (arrPicName.length == 1) {
                isCompatibleMode = true;
                useCompatibleOtherUserImage = true;
                rlOtherUserImageContainer1.setVisibility(View.GONE);
                rlOtherUserImageContainer2.setVisibility(View.GONE);
                rlCompatibleOtherUserImageContainer.setVisibility(View.VISIBLE);
                x.image().bind(ivCompatibleOtherUserImage,
                        Config.getContrastFullPath(arrPicName[0]));
            }
        }

        // 提取并设置内容
        // 不用空格做分隔符是因为内容中可能会有分隔符，所以使用 "] [" 做分隔符
        String[] arrContent = content.trim().split("\\] \\[");
        if (arrContent.length != 4) {
            return;
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
        etDesc1.setText(arrContent[0]);
        etDesc2.setText(arrContent[1]);
        parseDescTextColor(arrContent[2], arrContent[3]);

        // 提取并设置标签
        String tags = paragraphEntity.getTags();
        resumeTag(tags);
    }


    /**
     * 我来
     */
    private void initMeRecreateParagraph() {
        DD.d(TAG, "recreateParagraph()");

        // 从“话题列表”页面传过来的话题
        topicNameFromTopicListPage = getIntent().getStringExtra("topicNameFromTopicListPage");
        if (!TextUtils.isEmpty(topicNameFromTopicListPage)) {
            topicNameFromTopicListPage = "#" + topicNameFromTopicListPage + ":@(0)";
            resumeTag(topicNameFromTopicListPage);
            return;
        }

        paragraphEntity = getIntent().getParcelableExtra("paragraph2");
        if (paragraphEntity == null) {
            // 还原上一次编辑的数据，如果有的话
            resumeData();
            return;
        }

        isEdit = getIntent().getBooleanExtra("isEdit", false);
        isRecreate = true;
        useOtherUserImage1 = true;
        useOtherUserImage2 = true;

        // 初始化
        String content = paragraphEntity.getParagraphContent();
        if (content.contains("[up]")) {
            switchLayoutStyle();
        }
        DD.d(TAG, "picName: " + paragraphEntity.getPicName());
        String[] arrPicName = parsePicName(paragraphEntity.getPicName());
        if (arrPicName != null) {
            if (arrPicName.length == 2) {
                isCompatibleMode = false;
                rlCompatibleOtherUserImageContainer.setVisibility(View.GONE);
                rlOtherUserImageContainer1.setVisibility(View.VISIBLE);
                rlOtherUserImageContainer2.setVisibility(View.VISIBLE);
                x.image().bind(ivOtherUserImage1, Config.getContrastFullPath(arrPicName[0]));
                x.image().bind(ivOtherUserImage2, Config.getContrastFullPath(arrPicName[1]));
            } else if (arrPicName.length == 1) {
                isCompatibleMode = true;
                useCompatibleOtherUserImage = true;
                rlOtherUserImageContainer1.setVisibility(View.GONE);
                rlOtherUserImageContainer2.setVisibility(View.GONE);
                rlCompatibleOtherUserImageContainer.setVisibility(View.VISIBLE);
                x.image().bind(ivCompatibleOtherUserImage,
                        Config.getContrastFullPath(arrPicName[0]));
            }
        }

        // 提取并设置内容
        // 不用空格做分隔符是因为内容中可能会有分隔符，所以使用 "] [" 做分隔符
        String[] arrContent = content.trim().split("\\] \\[");
        if (arrContent.length != 4) {
            return;
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

        //etDesc1.setText(arrContent[0]);
        //etDesc2.setText(arrContent[1]);
        //不设置字体
        etDesc1.setText("");
        etDesc2.setText("");
        parseDescTextColor(arrContent[2], arrContent[3]);

        // 提取并设置标签
        String tags = paragraphEntity.getTags();
        resumeTag(tags);

        //把 删除/旋转/颜色选择 隐藏掉
        rlDelete.setVisibility(View.GONE);
        rlRotate.setVisibility(View.GONE);
        rlSwitchColor.setVisibility(View.GONE);
        ivRecreate1.setVisibility(View.GONE);
        ivRecreate2.setVisibility(View.GONE);
        ivCompatibleRecreate.setVisibility(View.GONE);

        rlImageContainer1.setClickable(false);
        rlImageContainer2.setClickable(false);
    }


    /**
     * 恢复标签
     */
    private void resumeTag(String tags) {
        DD.d(TAG, "resumeTag(), tags" + tags);

        if (!TextUtils.isEmpty(tags)) {
            Pattern pattern = Pattern.compile(Config.PATTERN_TAG);
            Matcher matcher = pattern.matcher(tags);
            while (matcher.find()) {
                listTag.add("#" + matcher.group(1));
            }
            // 设置etTopic
            String sTopic = "";
            int size = listTag.size();
            for (int i = 0; i < size; ++i) {
                if (i == size - 1) {
                    sTopic += listTag.get(i);
                } else {
                    sTopic += listTag.get(i) + "，";
                }
            }
            DD.d(TAG, "sTopic: " + sTopic);
            etTopic.setText(sTopic);
            refreshSelectedTag();
        }
    }

    /**
     * 解析并设置描述文字颜色
     */
    private void parseDescTextColor(String desc1, String desc2) {
        DD.d(TAG, "parseDescTextColor(), desc1: " + desc1 + ", desc2: " + desc2);

        if (TextUtils.isEmpty(desc1) || TextUtils.isEmpty(desc2)) {
            return;
        }

        Matcher mColorRgb1 = PATTERN_COLOR_RGB.matcher(desc1);
        Matcher mColorIndex1 = PATTERN_COLOR_INDEX.matcher(desc1);
        if (mColorRgb1.find()) {
            try {
                int color = Color.rgb(Integer.parseInt(mColorRgb1.group(1)),
                        Integer.parseInt(mColorRgb1.group(2)),
                        Integer.parseInt(mColorRgb1.group(3)));
                outerColor1 = color;
                rlImageContainer1.setBackgroundColor(color);
                etDesc1.setBackgroundColor(color);
                if (getGrayLevel(color) > 192) {
                    etDesc1.setTextColor(Color.BLACK);
                } else {
                    etDesc1.setTextColor(Color.WHITE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (mColorIndex1.find()) {
            try {
                int colorIndex = Integer.parseInt(mColorIndex1.group(1));
                rlImageContainer1.setBackgroundColor(BaseActivity.BG_COLOR[colorIndex]);
                etDesc1.setBackgroundColor(BaseActivity.BG_COLOR[colorIndex]);
                switch (colorIndex) {
                    case 4:
                    case 10:
                    case 11:
                    case 19:
                        colorIndex = 18;
                        etDesc1.setTextColor(BaseActivity.BG_COLOR[colorIndex]);
                        break;
                    case 5:
                    case 8:
                        colorIndex = 1;
                        etDesc1.setTextColor(BaseActivity.BG_COLOR[colorIndex]);
                        break;
                    default:
                        if (getGrayLevel(BaseActivity.BG_COLOR[colorIndex]) > 192) {
                            etDesc1.setTextColor(Color.BLACK);
                        } else {
                            etDesc1.setTextColor(Color.WHITE);
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Matcher mColorRgb2 = PATTERN_COLOR_RGB.matcher(desc2);
        Matcher mColorIndex2 = PATTERN_COLOR_INDEX.matcher(desc2);
        if (mColorRgb2.find()) {
            try {
                int color = Color.rgb(Integer.parseInt(mColorRgb2.group(1)),
                        Integer.parseInt(mColorRgb2.group(2)),
                        Integer.parseInt(mColorRgb2.group(3)));
                outerColor2 = color;
                rlImageContainer2.setBackgroundColor(color);
                etDesc2.setBackgroundColor(color);
                if (getGrayLevel(color) > 192) {
                    etDesc2.setTextColor(Color.BLACK);
                } else {
                    etDesc2.setTextColor(Color.WHITE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (mColorIndex2.find()) {
            try {
                int colorIndex = Integer.parseInt(mColorIndex2.group(1));
                rlImageContainer2.setBackgroundColor(BaseActivity.BG_COLOR[colorIndex]);
                etDesc2.setBackgroundColor(BaseActivity.BG_COLOR[colorIndex]);
                switch (colorIndex) {
                    case 4:
                    case 10:
                    case 11:
                    case 19:
                        colorIndex = 18;
                        etDesc2.setTextColor(BaseActivity.BG_COLOR[colorIndex]);
                        break;
                    case 5:
                    case 8:
                        colorIndex = 1;
                        etDesc2.setTextColor(BaseActivity.BG_COLOR[colorIndex]);
                        break;
                    default:
                        if (getGrayLevel(BaseActivity.BG_COLOR[colorIndex]) > 192) {
                            etDesc2.setTextColor(Color.BLACK);
                        } else {
                            etDesc2.setTextColor(Color.WHITE);
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int getGrayLevel(int color) {
        return (int) (Color.red(color) * 0.299 + Color.green(color) * 0.587
                + Color.blue(color) * 0.114);
    }

    private void init() {
        // 初始化颜色数据
        TypedArray taBgColor = getResources().obtainTypedArray(R.array.bg_color);
        TypedArray taTextColor = getResources().obtainTypedArray(R.array.text_color);
        bgColor = new int[taBgColor.length()];
        textColor = new int[taTextColor.length()];
        for (int i = 0; i < bgColor.length; i++) {
            bgColor[i] = taBgColor.getColor(i, 0);
            textColor[i] = taTextColor.getColor(i, 0);
        }
        taBgColor.recycle();
        taTextColor.recycle();
        // 设置随机颜色
        switchRandomColor();
        // 初始化标签
        initTag();
        // 设置键盘监听
        setOnSoftKeyBoardVisibleChangeListener();
        // desc输入监听
        setDescOnTextChangedListener();
        // 初始化滤镜
        initGPUImageFilter();
        // 初始化 desc function 文字
        initDescFunctionText();
        //是否跳转到多个版本页面
        if (getIntent() != null) {
            mShouldOpenMulit = getIntent().getBooleanExtra("openMulit", false);
        }
    }

    /**
     * 初始化 desc function 文字
     */
    private void initDescFunctionText() {
        tvAddHref1.setText(Config.EMOJI_HREF + "添加超链接");
        tvOriginal1.setText(Config.EMOJI_CAMERA + "原创图片");
        tvHighEnergy1.setText(Config.EMOJI_WARN + "前方高能慎点");
        tv18Forbid1.setText(Config.EMOJI_18_FORBID + "18禁");
        tvAddHref2.setText(Config.EMOJI_HREF + "添加超链接");
        tvOriginal2.setText(Config.EMOJI_CAMERA + "原创图片");
    }

    /**
     * 初始化滤镜
     */
    private void initGPUImageFilter() {
        gpuImage = new GPUImage(this);
        listFilter.add(FilterType.RGB);
        listFilter.add(FilterType.GRAYSCALE);
        listFilter.add(FilterType.HUE);
        listFilter.add(FilterType.BRIGHTNESS);
        listFilter.add(FilterType.WHITE_BALANCE);
        listFilter.add(FilterType.COLOR_BALANCE);
        listFilter.add(FilterType.SEPIA);
        listFilter.add(FilterType.BILATERAL_BLUR);
    }

    /**
     * 设置desc文本变化监听
     */
    private void setDescOnTextChangedListener() {
        DD.d(TAG, "setDescOnTextChangedListener()");

        etDesc1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 删除换行符
                try {
                    for (int i = s.length(); i > 0; i--) {
                        if (("\n").equals(s.subSequence(i - 1, i).toString())) {
                            s.replace(i - 1, i, "");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                DD.d(TAG, "onTextChanged(), s: " + s.toString() + ", c: " + count);

                adjustDescSize(s.toString(), etDesc1);
            }
        });
        etDesc2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 删除换行符
                try {
                    for (int i = s.length(); i > 0; i--) {
                        if (("\n").equals(s.subSequence(i - 1, i).toString())) {
                            s.replace(i - 1, i, "");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                DD.d(TAG, "onTextChanged(), s: " + s.toString() + ", c: " + count);

                adjustDescSize(s.toString(), etDesc2);
            }
        });
    }

    /**
     * 调整描述字体大小
     */
    private void adjustDescSize(String desc, EditText editText) {
        DD.d(TAG, "adjustDescSize(), desc: " + desc);

        if (desc.length() <= Config.DESC_TEXT_LENGTH_LARGE_LIMIT) {
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, Config.DESC_TEXT_SIZE_LARGE_SP);
        } else if (desc.length() <= Config.DESC_TEXT_LENGTH_MEDIUM_LIMIT) {
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, Config.DESC_TEXT_SIZE_MEDIUM_SP);
        } else if (desc.length() <= Config.DESC_TEXT_LENGTH_MAX_LIMIT) {
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, Config.DESC_TEXT_SIZE_SMALL_SP);
        } else {
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, Config.DESC_TEXT_SIZE_MEDIUM_SP);
        }
    }

    /**
     * 初始化标签
     */
    private void initTag() {
        LayoutInflater inflater = getLayoutInflater();
        OnTagClickListener listener = new OnTagClickListener();
        if (PrefUtil.getStringValue(Config.PREF_SAVE_TAG) != "") {
            newarrtag = getMergeArray(
                    PrefUtil.getStringValue(Config.PREF_SAVE_TAG).replace("[", "").replace("]", "")
                            .split(","), arrTag);
        } else {
            newarrtag = arrTag;
        }

        for (int i = 0; i < newarrtag.length; ++i) {
            ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.item_tag, null);
            RelativeLayout rl = (RelativeLayout) vg.findViewById(R.id.rl_tag);
            TextView tv = (TextView) vg.findViewById(R.id.tv_tag);
            tv.setText(newarrtag[i]);
            vg.removeView(rl);
            wfvgContainer.addView(rl);
            // 设置点击事件
            tv.setTag(i);
            tv.setOnClickListener(listener);
        }
    }

    public String[] getMergeArray(String[] al, String[] bl) {
        String[] a = al;
        String[] b = bl;
        String[] c = new String[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    /**
     * tag点击事件监听类
     */
    private class OnTagClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            int index = (int) v.getTag();
            String tags = etTopic.getText().toString();
            if (TextUtils.isEmpty(tags)) {
                tags += newarrtag[index];
            } else {
                tags += "，" + newarrtag[index];
            }
            etTopic.setText(tags);
            etTopic.setSelection(tags.length());
        }
    }

    /**
     * 设置键盘 显示/隐藏 监听（键盘非全屏模式也算隐藏）
     */
    private boolean lastVisible = false;
    private boolean hasFixHeight = false;
    private boolean hasFixTagHeight = false;
    private boolean hasInitRecreateParagraph = false;
    private boolean meRecreateParagraph = false;


    private void setOnSoftKeyBoardVisibleChangeListener() {
        final View decorView = getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
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

                if (!hasFixHeight) {
                    // 固定图文区域的高度
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) llImageAndDescContainer
                            .getLayoutParams();
                    params.height = llImageAndDescContainer.getHeight();
                    llImageAndDescContainer.setLayoutParams(params);
                    hasFixHeight = true;
                }

                if (!hasFixTagHeight) {
                    // fix rl_tag_container's height
                    ((ViewGroup) wfvgContainer.getParent()).removeView(wfvgContainer);
                    rlTagContainer.addView(wfvgContainer);
                    LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) rlTagContainer
                            .getLayoutParams();
                    p.height = wfvgContainer.getWrapHeight();
                    rlTagContainer.setLayoutParams(p);
                    hasFixTagHeight = true;
                }

                // recreate
                if (!hasInitRecreateParagraph) {
                    hasInitRecreateParagraph = true;
                    initRecreateParagraph();
                }

                // TODO: 2016/9/29
                //我来
                if (!meRecreateParagraph) {
                    meRecreateParagraph = true;
                    initMeRecreateParagraph();
                }
            }
        });
    }

    /**
     * 键盘显示状态改变回调
     */
    private void onSoftKeyBoardVisibleChanged(boolean visible) {
        DD.d(TAG, "onSoftKeyBoardVisibleChanged(), visible: " + visible);
        if (!visible) {
            etDesc1.clearFocus();
            etDesc2.clearFocus();
            etTopic.clearFocus();

            llBottomContainer.setVisibility(View.VISIBLE);
            rlTopicContainer.setVisibility(View.VISIBLE);
            rlTagContainer.setVisibility(View.GONE);
            rlDescFunctionContainer1.setVisibility(View.GONE);
            rlDescFunctionContainer2.setVisibility(View.GONE);
            rlFunctionContainer.setVisibility(View.VISIBLE);

            // 选中的标签，视图变换
            refreshSelectedTag();
        } else {
            // 用于修复“etTopic失去焦点后，etDesc1自动获取焦点所导致布局错误的bug”
            if (etDesc1.hasFocus()) {
                llBottomContainer.setVisibility(View.VISIBLE);
                rlTopicContainer.setVisibility(View.GONE);
                rlTagContainer.setVisibility(View.GONE);
                rlDescFunctionContainer1.setVisibility(View.VISIBLE);
                rlDescFunctionContainer2.setVisibility(View.GONE);
                rlFunctionContainer.setVisibility(View.GONE);
            } else if (etDesc2.hasFocus()) {
                llBottomContainer.setVisibility(View.VISIBLE);
                rlTopicContainer.setVisibility(View.GONE);
                rlTagContainer.setVisibility(View.GONE);
                rlDescFunctionContainer1.setVisibility(View.GONE);
                rlDescFunctionContainer2.setVisibility(View.VISIBLE);
                rlFunctionContainer.setVisibility(View.GONE);
            } else if (etTopic.hasFocus()) {
                llBottomContainer.setVisibility(View.VISIBLE);
                rlTopicContainer.setVisibility(View.VISIBLE);
                rlTagContainer.setVisibility(View.VISIBLE);
                rlDescFunctionContainer1.setVisibility(View.GONE);
                rlDescFunctionContainer2.setVisibility(View.GONE);
                rlFunctionContainer.setVisibility(View.GONE);
            }

            if (currentLayoutStyle == LAYOUT_STYLE_VERTICAL && etDesc1.hasFocus()) {
                svContainer.postDelayed(new Runnable() {
                    public void run() {
                        svContainer.smoothScrollBy(0, -2000);
                    }
                }, 300);
            } else {
                svContainer.postDelayed(new Runnable() {
                    public void run() {
                        svContainer.smoothScrollBy(0, 1000);
                    }
                }, 300);
                // svContainer.smoothScrollBy(0, 1000);
            }

            // 选中的标签，视图变换
            llSelectedTagContainer.setVisibility(View.GONE);
            hsvSelectedTagContainer.setVisibility(View.GONE);
        }
    }

    /**
     * 刷新选中的标签
     */
    private void refreshSelectedTag() {
        DD.d(TAG, "refreshSelectedTag()");

        String sSplit = "，";
        String sPrefix = "#";
        String selectedTags = etTopic.getText().toString();
        if (TextUtils.isEmpty(selectedTags)) {
            return;
        }

        if (!isEdit && !isRecreate && TextUtils.isEmpty(topicNameFromTopicListPage)) {
            DD.d(TAG, "save tags");
            // 保存数据
            lastTag = selectedTags;
            PrefUtil.setStringValue(Config.PREF_LAST_TAG, selectedTags);
        }

        hsvSelectedTagContainer.setVisibility(View.VISIBLE);
        llSelectedTagContainer.setVisibility(View.VISIBLE);
        llSelectedTagContainer.removeAllViews();
        String[] arrTag = selectedTags.split(sSplit);
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < arrTag.length; ++i) {
            DD.d(TAG, "arrTag[i]: " + arrTag[i]);
            if (!sPrefix.equals(arrTag[i].trim().substring(0, 1))) {
                arrTag[i] = sPrefix + arrTag[i];
            }
            // 创建并添加view
            ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.item_tag_selected, null);
            TextView tv = (TextView) vg.findViewById(R.id.tv_tag);
            tv.setText(arrTag[i]);
            llSelectedTagContainer.addView(vg);
        }
        // 调整标签位置
        llSelectedTagContainer.getViewTreeObserver()
                .addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    boolean hasDone = false;

                    @Override
                    public void onGlobalLayout() {
                        DD.d(TAG, "onGlobalLayout()");

                        if (hasDone) {
                            return;
                        } else {
                            hasDone = true;
                        }

                        int width1 = llSelectedTagContainer.getWidth() - llSelectedTagContainer
                                .getPaddingLeft();
                        int width2 = hsvSelectedTagContainer.getWidth();
                        if (width1 < width2) {
                            int leftPadding = (width2 - width1) / 2;
                            llSelectedTagContainer.setPadding(leftPadding, 0, 0, 0);
                        } else {
                            llSelectedTagContainer.setPadding(0, 0, 0, 0);
                        }
                    }
                });

        // 设置etTopic
        String sTopic = "";
        listTag.clear();
        for (int i = 0; i < arrTag.length; ++i) {
            if (!arrTag[i].equals("#本周最佳") && !arrTag[i].equals("#每日精选")) {
                listTag.add(arrTag[i]);
                if (i == arrTag.length - 1) {
                    sTopic += arrTag[i];
                } else {
                    sTopic += arrTag[i] + sSplit;
                }
            }
        }
        etTopic.setText(sTopic);
    }

    // 为了解决，edit或recreate进来为上下图片结构的时候造成的布局错乱
    private boolean isDesc1HasFirstFocus = true;

    /**
     * 设置输入焦点监听
     */
    @Event(value = {R.id.et_desc_1, R.id.et_desc_2, R.id.et_topic}, type = OnFocusChangeListener.class)
    private void onFocusChange(View v, boolean hasFocus) {
        DD.d(TAG, "onFocusChange(), hasFocus: " + hasFocus);

        switch (v.getId()) {
            case R.id.et_desc_1:
                if (hasFocus) {
                    vLastFocus = etDesc1;
                    if (isDesc1HasFirstFocus && currentLayoutStyle == LAYOUT_STYLE_VERTICAL) {
                        isDesc1HasFirstFocus = false;
                        return;
                    }
                    llBottomContainer.setVisibility(View.VISIBLE);
                    rlTopicContainer.setVisibility(View.GONE);
                    rlTagContainer.setVisibility(View.GONE);
                    rlDescFunctionContainer1.setVisibility(View.VISIBLE);
                    rlDescFunctionContainer2.setVisibility(View.GONE);
                    rlFunctionContainer.setVisibility(View.GONE);
                    // svContainer.smoothScrollBy(0, llBottomDescContainer.getHeight());
                    if (currentLayoutStyle == LAYOUT_STYLE_VERTICAL) {
                        svContainer.postDelayed(new Runnable() {
                            public void run() {
                                svContainer.smoothScrollBy(0, -2000);
                            }
                        }, 300);
                    }
                }
                break;
            case R.id.et_desc_2:
                if (hasFocus) {
                    vLastFocus = etDesc2;

                    llBottomContainer.setVisibility(View.VISIBLE);
                    rlTopicContainer.setVisibility(View.GONE);
                    rlTagContainer.setVisibility(View.GONE);
                    rlDescFunctionContainer1.setVisibility(View.GONE);
                    rlDescFunctionContainer2.setVisibility(View.VISIBLE);
                    rlFunctionContainer.setVisibility(View.GONE);
                    svContainer.smoothScrollBy(0, llBottomDescContainer.getHeight());
                }
                break;
            case R.id.et_topic:
                if (hasFocus) {
                    vLastFocus = etTopic;

                    llBottomContainer.setVisibility(View.VISIBLE);
                    rlTopicContainer.setVisibility(View.VISIBLE);
                    rlTagContainer.setVisibility(View.VISIBLE);
                    rlDescFunctionContainer1.setVisibility(View.GONE);
                    rlDescFunctionContainer2.setVisibility(View.GONE);
                    rlFunctionContainer.setVisibility(View.GONE);
                    svContainer.postDelayed(new Runnable() {
                        public void run() {
                            svContainer.smoothScrollBy(0, rlTagContainer.getHeight());
                        }
                    }, 300);
                }
                break;
        }
    }

    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    float downX;
    float downY;
    float upX;
    float upY;

    private double calcMoveDistance() {
        return Math.sqrt(Math.pow(upX - downX, 2) + Math.pow(upY - downY, 2));
    }

    @Event(value = R.id.hsv_selected_tag_container, type = OnTouchListener.class)
    private boolean onHsvSelectedTagContainerTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                upX = event.getX();
                upY = event.getY();
                // 只在“点击”的时候进行以下操作
                if (calcMoveDistance() < DensityUtil.dip2px(this, 10)) {
                    hsvSelectedTagContainer.performClick();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Event(value = R.id.hsv_selected_tag_container)
    private void onHsvSelectedTagContainerClick(View v) {
        DD.d(TAG, "onHsvSelectedTagContainerClick()");

        onSoftKeyBoardVisibleChanged(true);
        showSoftInput(this, etTopic);
        etTopic.requestFocus();
        etTopic.setSelection(etTopic.getText().length());
    }

    /**
     * 显示输入法
     */
    private void showSoftInput(Context context, View view) {
        DD.d(TAG, "showSoftInput()");

        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    /**
     * 切换随机颜色
     */
    private void switchRandomColor() {
        Random random = new Random(System.currentTimeMillis());
        int r1 = random.nextInt(bgColor.length);
        int r2 = random.nextInt(bgColor.length);
        // 防止两种颜色相同
        if (r1 == r2) {
            r2 = ++r2 % bgColor.length;
        }
        color1 = r1;
        color2 = r2;
        rlImageContainer1.setBackgroundColor(bgColor[r1]);
        rlImageContainer2.setBackgroundColor(bgColor[r2]);
        etDesc1.setBackgroundColor(bgColor[r1]);
        etDesc2.setBackgroundColor(bgColor[r2]);
        // hint 设置透明度
        etDesc1.setHintTextColor(textColor[r1] & 0x00ffffff | 0x66000000);
        etDesc2.setHintTextColor(textColor[r2] & 0x00ffffff | 0x66000000);
        etDesc1.setTextColor(textColor[r1]);
        etDesc2.setTextColor(textColor[r2]);
    }

    /**
     * 1 表示左边 2表示右边 选择滤镜
     *
     * @param index
     */
    public void selectImageFilterPop(int index) {
        if (popupWindow != null && popupWindow.isShowing()) {// 先取消
            popupWindow.dismiss();
            popupWindow = null;
            return;
        }
        String path;
        if (index == 1) {
            path = path_left;
            if (ADIWebUtils.isNvl(path_left)) {
                ADIWebUtils.showToast(this, "请先选择图片");
                return;
            }
        } else {
            path = path_right;
            if (ADIWebUtils.isNvl(path_right)) {
                ADIWebUtils.showToast(this, "请先选择图片");
                return;
            }
        }
        View contentView = LayoutInflater.from(this).inflate(R.layout.popwindow_image_filter, null);
        if (currentLayoutStyle == LAYOUT_STYLE_HORIZONTAL) {
            popupWindow = new PopupWindow(contentView, PostActivity.PIC_WIDTH / 2,
                    PostActivity.PIC_HEIGHT);
        } else {
            popupWindow = new PopupWindow(contentView, PostActivity.PIC_WIDTH,
                    PostActivity.PIC_WIDTH / 2);
        }
        popupWindow.setContentView(contentView);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        initSelectImageFilterPop(contentView, path, index);
        if (index == 1) {// 左边
            int[] location = new int[2];
            rlImageContainer1.getLocationOnScreen(location);
            popupWindow.showAtLocation(rlImageContainer1, Gravity.NO_GRAVITY, location[0],
                    location[1]);
        } else {// 右边
            int[] location = new int[2];
            rlImageContainer2.getLocationOnScreen(location);
            popupWindow.showAtLocation(rlImageContainer2, Gravity.NO_GRAVITY, location[0],
                    location[1]);
        }
    }

    /**
     * 初始化选择滤镜
     *
     * @param index 1 表示左边 2表示右边
     */
    private void initSelectImageFilterPop(View contentView, String path, final int index) {
        GridView image_filter_lv = (GridView) contentView.findViewById(R.id.image_filter_lv);
        if (imageFilterAdapter != null) {
            imageFilterAdapter.recycleAllBitmap();// 释放上一次资源
        }
        imageFilterAdapter = new SelectImageFilterAdapter(this, listFilter, arrFilterAdjusterValue,
                path, currentLayoutStyle == 1 ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        if (currentLayoutStyle == LAYOUT_STYLE_HORIZONTAL) {
            image_filter_lv.setNumColumns(2);
        } else {
            image_filter_lv.setNumColumns(4);
        }
        image_filter_lv.setAdapter(imageFilterAdapter);
        image_filter_lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (index) {
                    case 1:// 左边
                        // 回收上一次的滤镜效果
                        if (bitmap_left_filter != null && !bitmap_left_filter.isRecycled()) {
                            bitmap_left_filter.recycle();
                            bitmap_left_filter = null;
                        }
                        bitmap_left_filter = diejia(bitmap_left_old, position);
                        // pic_left.setImageBitmap(bitmap_left_filter);
                        ivImage1.setFilter(bitmap_left_filter);
                        popupWindow.dismiss();
                        break;
                    case 2:// 右边
                        // 回收上一次的滤镜效果
                        if (bitmap_right_filter != null && !bitmap_right_filter.isRecycled()) {
                            bitmap_right_filter.recycle();
                            bitmap_right_filter = null;
                        }
                        bitmap_right_filter = diejia(bitmap_right_old, position);
                        // pic_right.setImageBitmap(bitmap_right_filter);
                        ivImage2.setFilter(bitmap_right_filter);
                        popupWindow.dismiss();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 图片叠加
     *
     * @param b
     * @return
     */
    public Bitmap diejia(Bitmap b, int position) {
        if (b == null || b.isRecycled()) {// 资源不存在获取被回收
            return null;
        }
        b = b.copy(Bitmap.Config.RGB_565, true);//
        Canvas canvas = new Canvas(b);
        // 叠加新图b2
        // 注意此时绘制坐标是相对于图片b
        // canvas.drawColor(colors[position]);

        // 设置滤镜
        GPUImageFilter filter = GPUImageFilterTools
                .createFilterForType(this, listFilter.get(position));
        int adjusterValue = arrFilterAdjusterValue[position];
        if (adjusterValue >= 0) {
            new FilterAdjuster(filter).adjust(adjusterValue);
        }
        gpuImage.setFilter(filter);
        b = gpuImage.getBitmapWithFilterApplied(b);
        canvas.drawBitmap(b, 0, 0, new Paint());

        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return b;
    }

    private void showHandlerPicWindow(int index) {
        if (popupWindow != null && popupWindow.isShowing()) {// 先取消
            popupWindow.dismiss();
            popupWindow = null;
            return;
        }
        View contentView;
        if (index == 1) {
            if (currentLayoutStyle == LAYOUT_STYLE_HORIZONTAL) {// 水平
                contentView = LayoutInflater.from(this)
                        .inflate(R.layout.pop_window_handler_pic_left, null);
                popupWindow = new PopupWindow(contentView, PostActivity.PIC_WIDTH / 2,
                        LayoutParams.WRAP_CONTENT);
            } else {
                contentView = LayoutInflater.from(this)
                        .inflate(R.layout.pop_window_handler_pic_left2, null);
                popupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT,
                        PostActivity.PIC_HEIGHT / 2);
            }
        } else {
            if (currentLayoutStyle == LAYOUT_STYLE_HORIZONTAL) {// 水平
                contentView = LayoutInflater.from(this)
                        .inflate(R.layout.pop_window_handler_pic_right, null);
                popupWindow = new PopupWindow(contentView, PostActivity.PIC_WIDTH / 2,
                        LayoutParams.WRAP_CONTENT);
            } else {
                contentView = LayoutInflater.from(this)
                        .inflate(R.layout.pop_window_handler_pic_right2, null);
                popupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT,
                        PostActivity.PIC_HEIGHT / 2);
            }
        }

        popupWindow.setContentView(contentView);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        initHandlerPicPop(contentView, index);
        if (index == 1) {// 左边
            if (currentLayoutStyle == LAYOUT_STYLE_HORIZONTAL) {// 水平
                int[] location = new int[2];
                rlImageContainer1.getLocationOnScreen(location);
                Log.e("=============",
                        "x==" + location[0] + "y==" + location[1] + "==popupWindow=" + popupWindow
                                .getHeight());
                popupWindow.showAtLocation(rlImageContainer1, Gravity.NO_GRAVITY, location[0],
                        location[1] - popupWindow.getHeight() + PostActivity.PIC_HEIGHT
                                - DensityUtil.dip2px(this, 45));
                // popupWindow.showAtLocation(left_layout,Gravity.BOTTOM ,0,0);
            } else {
                int[] location = new int[2];
                rlImageContainer1.getLocationOnScreen(location);
                Log.e("=============", "x==" + location[0] + "y==" + location[1]);
                popupWindow.showAtLocation(rlImageContainer1, Gravity.NO_GRAVITY,
                        location[0] - popupWindow.getWidth(), location[1]);
            }
        } else {// 右边
            if (currentLayoutStyle == LAYOUT_STYLE_HORIZONTAL) {// 水平
                int[] location = new int[2];
                rlImageContainer2.getLocationOnScreen(location);
                popupWindow.showAtLocation(rlImageContainer2, Gravity.NO_GRAVITY, location[0],
                        location[1] - popupWindow.getHeight() + PostActivity.PIC_HEIGHT
                                - DensityUtil.dip2px(this, 45));
            } else {
                int[] location = new int[2];
                rlImageContainer2.getLocationOnScreen(location);
                popupWindow.showAtLocation(rlImageContainer2, Gravity.NO_GRAVITY,
                        location[0] - popupWindow.getWidth(), location[1]);
            }
        }
    }

    /**
     * 初始化 处理图片操作
     */
    private void initHandlerPicPop(View contentView, int index) {
        ImageView handler_filter = (ImageView) contentView.findViewById(R.id.handler_filter);
        ImageView handler_turn_left_right = (ImageView) contentView
                .findViewById(R.id.handler_turn_left_right);
        ImageView handler_turn_top_buttom = (ImageView) contentView
                .findViewById(R.id.handler_turn_top_buttom);
        ImageView handler_change = (ImageView) contentView.findViewById(R.id.handler_change);
        MyPicHandler myPicHandler = new MyPicHandler(index);
        handler_filter.setOnClickListener(myPicHandler);
        handler_turn_left_right.setOnClickListener(myPicHandler);
        handler_turn_top_buttom.setOnClickListener(myPicHandler);
        handler_change.setOnClickListener(myPicHandler);
    }

    public class MyPicHandler implements OnClickListener {
        private int index;

        public MyPicHandler(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View v) {
            if (popupWindow != null) {
                popupWindow.dismiss();
                popupWindow = null;
            }
            switch (v.getId()) {
                case R.id.handler_filter:// 滤镜
                    selectImageFilterPop(index);
                    break;
                case R.id.handler_turn_left_right:// 左右旋转
                    if (index == 1) {
                        Log.e("===============", "左===90");
                        // pic_left.rotaleBitmap(90);
                        ivImage1.zuoyoujingxiang();
                    } else {
                        Log.e("===============", "右===90");
                        // pic_right.rotaleBitmap(90);
                        ivImage2.zuoyoujingxiang();
                    }
                    break;
                case R.id.handler_turn_top_buttom:// 上下旋转
                    if (index == 1) {
                        Log.e("===============", "左===180");
                        // pic_left.rotaleBitmap(180);
                        ivImage1.shangxiajingxiang();
                    } else {
                        Log.e("===============", "右===180");
                        // pic_right.rotaleBitmap(180);
                        ivImage2.shangxiajingxiang();
                    }
                    break;
                case R.id.handler_change:// 更换图片
                    if (index == 1) {
                        Intent intent = new Intent(PostActivity.this, PopWindowSelectCaremer.class);
                        startActivityForResult(intent, select_left);
                    } else {
                        Intent intent = new Intent(PostActivity.this, PopWindowSelectCaremer.class);
                        startActivityForResult(intent, select_right);
                    }
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * 添加图片
     */
    private void addImage(int viewId) {
        if (viewId == R.id.rl_image_container_1) {
            Intent intent = new Intent(this, PopWindowSelectCaremer.class);
            startActivityForResult(intent, select_left);
        } else if (viewId == R.id.rl_image_container_2) {
            Intent intent = new Intent(this, PopWindowSelectCaremer.class);
            startActivityForResult(intent, select_right);
        }
    }

    /**
     * 切换布局样式（横版、竖版）
     */
    private void switchLayoutStyle() {
        if (currentLayoutStyle == LAYOUT_STYLE_HORIZONTAL) {
            // 切换描述布局
            View vDescContainer1 = llBottomDescContainer.findViewById(R.id.rl_desc_container_1);
            llBottomDescContainer.removeViewAt(0);
            llTopDescContainer.addView(vDescContainer1);
            llTopDescContainer.setVisibility(View.VISIBLE);
            // 切换图片布局
            llImageContainer.setOrientation(LinearLayout.VERTICAL);
            LayoutParams params = rlImageContainer1.getLayoutParams();
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            params.height = 0;
            rlImageContainer1.setLayoutParams(params);
            rlImageContainer2.setLayoutParams(params);

            currentLayoutStyle = LAYOUT_STYLE_VERTICAL;
        } else if (currentLayoutStyle == LAYOUT_STYLE_VERTICAL) {
            // 切换描述布局
            View vDescContainer1 = llTopDescContainer.findViewById(R.id.rl_desc_container_1);
            llTopDescContainer.removeViewAt(0);
            llTopDescContainer.setVisibility(View.GONE);
            llBottomDescContainer.addView(vDescContainer1, 0);
            // 切换图片布局
            llImageContainer.setOrientation(LinearLayout.HORIZONTAL);
            LayoutParams params = rlImageContainer1.getLayoutParams();
            params.width = 0;
            params.height = LinearLayout.LayoutParams.MATCH_PARENT;
            rlImageContainer1.setLayoutParams(params);
            rlImageContainer1.setLayoutParams(params);

            currentLayoutStyle = LAYOUT_STYLE_HORIZONTAL;
        }
        lastOrientation = currentLayoutStyle;
        PrefUtil.setIntValue(Config.PREF_LAST_ORIENTATION, lastOrientation);
    }

    /**
     * 组装paragraphContent
     */
    private String getParagraphContent() {
        DD.d(TAG, "paragraphContent()");

        String c1 = "[colorA]";
        String c2 = "[colorB]";
        String d1;
        String d2;
        if (currentLayoutStyle == LAYOUT_STYLE_HORIZONTAL) {
            d1 = "[left]";
            d2 = "[right]";
        } else {
            d1 = "[up]";
            d2 = "[down]";
        }
        return d1 + desc1 + d1 + " " + d2 + desc2 + d2 + " " + c1 + getColor1() + c1 + " " + c2
                + getColor2() + c2;
    }

    /**
     * 获取颜色1
     */
    private String getColor1() {
        DD.d(TAG, "getColor1()");

        if (outerColor1 != 0) {
            return Color.red(outerColor1) + ";" + Color.green(outerColor1) + ";" + Color
                    .blue(outerColor1);
        } else {
            return String.valueOf(color1);
        }
    }

    /**
     * 获取颜色2
     */
    private String getColor2() {
        DD.d(TAG, "getColor2()");

        if (outerColor2 != 0) {
            return Color.red(outerColor2) + ";" + Color.green(outerColor2) + ";" + Color
                    .blue(outerColor2);
        } else {
            return String.valueOf(color2);
        }
    }

    /**
     * 组装tags
     */
    private String getTags() {
        DD.d(TAG, "getTags()");

        if (listTag.size() == 0) {
            listTag.add("#无对比-无真相");
        }

        StringBuilder sb = new StringBuilder();
        int size = listTag.size();
        for (int i = 0; i < size; ++i) {
            sb.append(listTag.get(i));
            if (i == size - 1) {
                sb.append(":@(0)");
            } else {
                sb.append(":@(0),");
            }
        }
        return sb.toString();
    }

    /**
     * 获取图片名（组装后）
     */
    private String getPicName() {
        return imageName1 + ";" + imageName2;
    }

    /**
     * 获取图片1 2 名称
     */
    private String[] parsePicName(String picName) {
        if (picName == null || TextUtils.isEmpty(picName)) {
            return null;
        }
        return picName.split(";");
    }

    @Event(value = R.id.rl_other_user_image_container_1)
    private void onOtherUserImageContainerClick1(View v) {
        // intercept click event
    }

    @Event(value = R.id.rl_other_user_image_container_2)
    private void onOtherUserImageContainerClick2(View v) {
        // intercept click event
    }

    @Event(value = R.id.iv_recreate_1)
    private void onRecreateClick1(View v) {
        DD.d(TAG, "onRecreateClick1()");

        useOtherUserImage1 = false;
        rlOtherUserImageContainer1.setVisibility(View.GONE);
        // 弹出图片选择框
        addImage(R.id.rl_image_container_1);
    }

    @Event(value = R.id.iv_recreate_2)
    private void onRecreateClick2(View v) {
        DD.d(TAG, "onRecreateClick2()");

        useOtherUserImage2 = false;
        rlOtherUserImageContainer2.setVisibility(View.GONE);
        // 弹出图片选择框
        addImage(R.id.rl_image_container_2);
    }

    @Event(value = R.id.iv_compatible_recreate)
    private void onCompatibleRecreate(View v) {
        DD.d(TAG, "onCompatibleRecreate()");

        isCompatibleMode = false;
        useCompatibleOtherUserImage = false;
        rlCompatibleOtherUserImageContainer.setVisibility(View.GONE);
    }

    @Event(value = R.id.iv_image_1, type = OnImageViewClickListener.class)
    private void onImageView1Click() {
        showHandlerPicWindow(1);
    }

    @Event(value = R.id.iv_image_2, type = OnImageViewClickListener.class)
    private void onImageView2Click() {
        showHandlerPicWindow(2);
    }

    @Event(value = {R.id.rl_image_container_1, R.id.rl_image_container_2})
    private void onAddImageClick(View v) {
        addImage(v.getId());
    }

    // desc输入功能标签相关 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    private boolean checkExistFlag(String s, String flag) {
        if (!TextUtils.isEmpty(s) && !TextUtils.isEmpty(flag) && s.contains(flag)) {
            return true;
        }
        return false;
    }

    @Event(value = R.id.ll_add_comment_1)
    private void onAddCommentClick1(View v) {
        // 检查是否已存在注释标识符
        if (checkExistFlag(etDesc1.getText().toString(), Config.PATTERN_ADD_COMMENT)) {
            TT.show(this, "一段文字只能包含一个附注");
            return;
        }
        int selectionIndex = etDesc1.getSelectionStart();
        if (selectionIndex < 0) {
            etDesc1.getText().insert(0, Config.PATTERN_ADD_COMMENT);
        } else {
            etDesc1.getText().insert(selectionIndex, Config.PATTERN_ADD_COMMENT);
        }
    }

    @Event(value = R.id.ll_add_href_1)
    private void onAddHrefClick1(View v) {
        // 检查是否已存在 超链接标识符
        if (checkExistFlag(etDesc1.getText().toString(), Config.PATTERN_ADD_HREF)) {
            TT.show(this, "一段文字只能包含一个链接");
            return;
        }
        int selectionIndex = etDesc1.getSelectionStart();
        if (selectionIndex < 0) {
            etDesc1.getText().insert(0, Config.PATTERN_ADD_HREF);
        } else {
            etDesc1.getText().insert(selectionIndex, Config.PATTERN_ADD_HREF);
        }
    }

    @Event(value = R.id.ll_clear_desc_1)
    private void onClearDescClick1(View v) {
        new MessageWithOkCancelFragment2(new MessageWithOkCancelFragment2.Callback() {
            @Override
            public void onOk() {
                etDesc1.getText().clear();
            }

            @Override
            public void onCancel() {
            }
        }, R.layout.frag_message_with_ok_cancel_clear_desc).show(getSupportFragmentManager(), "");
    }

    @Event(value = R.id.ll_original_1)
    private void onOriginalClick1(View v) {
        String desc = etDesc1.getText().toString();
        if (checkExistFlag(desc, Config.PATTERN_ORIGINAL)) {
            TT.show(this, "一张图片只需要标记一次原创");
            return;
        }
        if (!checkExistFlag(desc, Config.PATTERN_ADD_COMMENT)) {
            int selectionIndex = etDesc1.getSelectionStart();
            if (selectionIndex < 0) {
                etDesc1.getText().insert(0, Config.PATTERN_ADD_COMMENT);
            } else {
                etDesc1.getText().insert(selectionIndex, Config.PATTERN_ADD_COMMENT);
            }
        }
        etDesc1.getText().append(Config.PATTERN_ORIGINAL);
    }

    @Event(value = R.id.ll_high_energy_1)
    private void onHightEnergyClick1(View v) {
        String desc = etDesc1.getText().toString();
        if (checkExistFlag(desc, Config.PATTERN_HIGH_ENERGY)) {
            TT.show(this, "只需标记一次高能");
            return;
        }
        if (!checkExistFlag(desc, Config.PATTERN_ADD_COMMENT)) {
            int selectionIndex = etDesc1.getSelectionStart();
            if (selectionIndex < 0) {
                etDesc1.getText().insert(0, Config.PATTERN_ADD_COMMENT);
            } else {
                etDesc1.getText().insert(selectionIndex, Config.PATTERN_ADD_COMMENT);
            }
        }
        etDesc1.getText().append(Config.PATTERN_HIGH_ENERGY);
    }

    @Event(value = R.id.ll_18_forbid_1)
    private void on18ForbidClick1(View v) {
        String desc = etDesc1.getText().toString();
        if (checkExistFlag(desc, Config.PATTERN_18_FORBID)) {
            TT.show(this, "只需标记一次预警");
            return;
        }
        if (!checkExistFlag(desc, Config.PATTERN_ADD_COMMENT)) {
            int selectionIndex = etDesc1.getSelectionStart();
            if (selectionIndex < 0) {
                etDesc1.getText().insert(0, Config.PATTERN_ADD_COMMENT);
            } else {
                etDesc1.getText().insert(selectionIndex, Config.PATTERN_ADD_COMMENT);
            }
        }
        etDesc1.getText().append(Config.PATTERN_18_FORBID);
    }

    @Event(value = R.id.ll_add_comment_2)
    private void onAddCommentClick2(View v) {
        // 检查是否已存在注释标识符
        if (checkExistFlag(etDesc2.getText().toString(), Config.PATTERN_ADD_COMMENT)) {
            TT.show(this, "一段文字只能包含一个附注");
            return;
        }
        int selectionIndex = etDesc2.getSelectionStart();
        if (selectionIndex < 0) {
            etDesc2.getText().insert(0, Config.PATTERN_ADD_COMMENT);
        } else {
            etDesc2.getText().insert(selectionIndex, Config.PATTERN_ADD_COMMENT);
        }
    }

    @Event(value = R.id.ll_add_href_2)
    private void onAddHrefClick2(View v) {
        // 检查是否已存在 超链接标识符
        if (checkExistFlag(etDesc2.getText().toString(), Config.PATTERN_ADD_HREF)) {
            TT.show(this, "一段文字只能包含一个链接");
            return;
        }
        int selectionIndex = etDesc2.getSelectionStart();
        if (selectionIndex < 0) {
            etDesc2.getText().insert(0, Config.PATTERN_ADD_HREF);
        } else {
            etDesc2.getText().insert(selectionIndex, Config.PATTERN_ADD_HREF);
        }
    }

    @Event(value = R.id.ll_clear_desc_2)
    private void onClearDescClick2(View v) {
        new MessageWithOkCancelFragment2(new MessageWithOkCancelFragment2.Callback() {
            @Override
            public void onOk() {
                etDesc2.getText().clear();
            }

            @Override
            public void onCancel() {
            }
        }, R.layout.frag_message_with_ok_cancel_clear_desc).show(getSupportFragmentManager(), "");
    }

    @Event(value = R.id.ll_mark_original_2)
    private void onMarkOriginalClick(View v) {
        if (checkExistFlag(etDesc2.getText().toString(), Config.PATTERN_MARK_ORIGINAL)) {
            TT.show(this, "一张图片只需要标记一原创");
            return;
        }
        etDesc2.getText().append(Config.PATTERN_MARK_ORIGINAL);
    }

    @Event(value = R.id.ll_mark_hide_2)
    private void onMarkNohideClick(View v) {
        if (checkExistFlag(etDesc2.getText().toString(), Config.PATTERN_HIDDEN)) {
            return;
        }
        etDesc2.getText().append(Config.PATTERN_HIDDEN);
    }


    @Event(value = R.id.ll_original_2)
    private void onOriginalClick2(View v) {
        String desc = etDesc2.getText().toString();
        if (checkExistFlag(desc, Config.PATTERN_ORIGINAL)) {
            TT.show(this, "一张图片只需要标记一原创");
            return;
        }
        if (!checkExistFlag(desc, Config.PATTERN_ADD_COMMENT)) {
            int selectionIndex = etDesc2.getSelectionStart();
            if (selectionIndex < 0) {
                etDesc2.getText().insert(0, Config.PATTERN_ADD_COMMENT);
            } else {
                etDesc2.getText().insert(selectionIndex, Config.PATTERN_ADD_COMMENT);
            }
        }
        etDesc2.getText().append(Config.PATTERN_ORIGINAL);
    }

    // desc输入功能标签相关 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    @Event(value = R.id.rl_back)
    private void onBackClick(View v) {
        finish();
    }

    @Event(value = R.id.rl_delete)
    private void onDeleteClick(View v) {
        DD.d(TAG, "onDeleteClick()");

        new MessageWithOkCancelFragment2(new MessageWithOkCancelFragment2.Callback() {
            @Override
            public void onOk() {
                clearData();
            }

            @Override
            public void onCancel() {
            }
        }, R.layout.frag_message_with_ok_cancel_clear_contrast)
                .show(getSupportFragmentManager(), "");
    }

    /**
     * 清除数据
     */
    private void clearData() {
        DD.d(TAG, "clearData()");

        etDesc1.setText("");
        etDesc2.setText("");
        etTopic.setText("");
        llSelectedTagContainer.removeAllViews();
        llSelectedTagContainer.setVisibility(View.GONE);
        hsvSelectedTagContainer.setVisibility(View.GONE);
        refreshSelectedTag();
        rlOtherUserImageContainer1.setVisibility(View.GONE);
        rlOtherUserImageContainer2.setVisibility(View.GONE);
        ivImage1.setVisibility(View.GONE);
        ivImage2.setVisibility(View.GONE);
        // 恢复布局
        if (currentLayoutStyle == LAYOUT_STYLE_VERTICAL) {
            switchLayoutStyle();
        }

        // 清除本地缓存数据
        clearLocalData();
    }

    /**
     * 清除本地数据
     */
    private void clearLocalData() {
        DD.d(TAG, "clearLocalData()");
        if (lastTag != null) {
            List<String> savearrTag = new ArrayList<String>();
            String[] arrLastTag = lastTag.split("，");
            for (int i = 0; i < arrLastTag.length; i++) {
                if (arrLastTag[i].indexOf("#") == -1) {
                    if (("#" + arrLastTag[i]).indexOf(newarrtag.toString()) == -1) {
                        savearrTag.add("#" + arrLastTag[i]);
                    }
                } else {
                    if ((arrLastTag[i]).indexOf(newarrtag.toString()) == -1) {
                        savearrTag.add(arrLastTag[i]);
                    }
                }
            }
            //			String[] savedArr = PrefUtil.getStringValue(Config.PREF_SAVE_TAG).replace("[", "").replace("]", "").split(",");
            //			if(PrefUtil.getStringValue(Config.PREF_SAVE_TAG)!=""){
            //				String[] currentArr = (String[])savearrTag.toArray();
            //				for (int i = 0; i < currentArr.length; i++) {
            //					if (currentArr[i].indexOf("#") == -1) {
            //						if (("#" + currentArr[i]).indexOf(savedArr.toString()) == -1) {
            //
            //						}
            //					}else {
            //						if ((currentArr[i]).indexOf(savedArr.toString()) == -1) {
            //							savearrTag.add(arrLastTag[i]);
            //						}
            //					}
            //
            //				}
            //
            ////				 newarrtag = getMergeArray(PrefUtil.getStringValue(Config.PREF_SAVE_TAG).replace("[", "").replace("]", "").split(","),(String[]) savearrTag.toArray());
            //			}else{
            //				savedArr = savearrTag;
            //			}
            //
            ////			else{
            ////				newarrtag = arrTag;
            ////			}


            PrefUtil.setStringValue(Config.PREF_SAVE_TAG, savearrTag.toString());
        }

        lastOrientation = LAYOUT_STYLE_HORIZONTAL;
        lastImagePath1 = "";
        lastImagePath2 = "";
        lastDesc1 = "";
        lastDesc2 = "";
        lastTag = "";
        PrefUtil.setIntValue(Config.PREF_LAST_ORIENTATION, lastOrientation);
        PrefUtil.setStringValue(Config.PREF_LAST_IMAGE_PATH_1, lastImagePath1);
        PrefUtil.setStringValue(Config.PREF_LAST_IMAGE_PATH_2, lastImagePath2);
        PrefUtil.setStringValue(Config.PREF_LAST_DESC_1, lastDesc1);
        PrefUtil.setStringValue(Config.PREF_LAST_DESC_2, lastDesc2);
        PrefUtil.setStringValue(Config.PREF_LAST_TAG, lastTag);
    }

    @Event(value = R.id.rl_post)
    private void onPostClick(View v) {
        DD.d(TAG, "onPostClick()");

        desc1 = etDesc1.getText().toString();
        desc2 = etDesc2.getText().toString();

        //取消 发帖默认遮挡
        //		if(!desc2.contains(Config.PATTERN_HIDDEN)){
        //			desc2 = desc2+Config.PATTERN_NO_HIDDEN;
        //		}
        if ((!isCompatibleMode && !useOtherUserImage1 && bitmap_left_old == null) || (
                !isCompatibleMode && !useOtherUserImage2 && bitmap_right_old == null)) {
            TT.show(this, "无图无真相！");
            return;
        }
        if (TextUtils.isEmpty(desc1) || TextUtils.isEmpty(desc2)) {
            TT.show(this, "用文字画龙点睛！");
            return;
        }

        // 显示刷新动画
        ravRefreshingView.setRefreshing(true);

        imageName1 = UUID.randomUUID().toString();
        imageName2 = UUID.randomUUID().toString();
        compatibleImageName = imageName1;
        try {
            imageFile1 = File.createTempFile(UUID.randomUUID().toString(), null);
            imageFile2 = File.createTempFile(UUID.randomUUID().toString(), null);
            compatibleImageFile = imageFile1;
            if (imageFile1 == null || imageFile2 == null || !imageFile1.exists() || !imageFile2
                    .exists()) {
                ravRefreshingView.setRefreshing(false);
                TT.show(this, "创建本地临时文件失败");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            ravRefreshingView.setRefreshing(false);
            TT.show(this, "创建本地临时文件失败");
            return;
        }
        //如果是兼容模式
        if (isCompatibleMode) {
            postCompatibleContrast();
        } else {
            // 如果是 借题发挥，并且使用其他用户的图片，就要取不同区域的bitmap
            Bitmap bitmap1;
            Bitmap bitmap2;
            if (useOtherUserImage1) {
                ivOtherUserImage1.buildDrawingCache();
                bitmap1 = ivOtherUserImage1.getDrawingCache();
            } else {
                ivImage1.buildDrawingCache();
                bitmap1 = ivImage1.getDrawingCache();
            }
            if (useOtherUserImage2) {
                ivOtherUserImage2.buildDrawingCache();
                bitmap2 = ivOtherUserImage2.getDrawingCache();
            } else {
                ivImage2.buildDrawingCache();
                bitmap2 = ivImage2.getDrawingCache();
            }
            if (bitmap1 != null && bitmap2 != null) {
                final Bitmap fBitmap1 = bitmap1;
                final Bitmap fBitmap2 = bitmap2;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CommonUtil.saveBitmap(imageFile1, fBitmap1);
                        CommonUtil.saveBitmap(imageFile2, fBitmap2);
                        RequestParams params = new RequestParams(Config.PATH_UPLOAD_CONTRAST);
                        params.setMultipart(true);
                        params.addBodyParameter("accessToken",
                                PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
                        params.addBodyParameter("paragraphContent", getParagraphContent());
                        params.addBodyParameter("createUserId",
                                PrefUtil.getIntValue(Config.PREF_USER_ID) + "");
                        params.addBodyParameter("originName",
                                PrefUtil.getStringValue(Config.PREF_NICKNAME));
                        params.addBodyParameter("originAuthor", CommonUtil.getUserLocation());
                        params.addBodyParameter("tags", getTags());
                        params.addBodyParameter("createTime",
                                CommonUtil.getDefaultFormatCurrentTime());
                        params.addBodyParameter("picName", getPicName());
                        params.addBodyParameter("paragraphReplyId", UUID.randomUUID().toString());

                        // if (isEdit) {
                        // if (!useOtherUserImage1) {
                        // params.addBodyParameter();("leftPic", imageFile1);
                        // }
                        // if (!useOtherUserImage2) {
                        // params.addBodyParameter();("rightPic", imageFile2);
                        // }
                        // } else {
                        params.addBodyParameter("leftPic", imageFile1);
                        params.addBodyParameter("rightPic", imageFile2);
                        // }

                        if (isEdit) {
                            params.addBodyParameter("paragraphId",
                                    paragraphEntity.getParagraphId());
                            params.addBodyParameter("isModify", 1 + "");
                            params.addBodyParameter("createTime", paragraphEntity.getCreateTime());
                        } else {
                            params.addBodyParameter("paragraphId", UUID.randomUUID().toString());
                            params.addBodyParameter("isModify", 0 + "");
                            params.addParameter("seriesId", paragraphEntity.getSeriesId());
                        }
                        x.http().post(params, new CommonCallback<JSONObject>() {
                            @Override
                            public void onCancelled(CancelledException arg0) {
                            }

                            @Override
                            public void onError(Throwable arg0, boolean arg1) {
                            }

                            @Override
                            public void onFinished() {
                                ravRefreshingView.setRefreshing(false);
                            }

                            @Override
                            public void onSuccess(JSONObject result) {
                                DD.d(TAG, "onSuccess(), result: " + result.toString());
                                StatusResultEntity entity = JSON
                                        .parseObject(result.toString(), StatusResultEntity.class);
                                if ("0".equals(entity.getStatus())) {
                                    if (lastTag != null) {
                                        if (lastTag.indexOf("本周最佳") != -1) {
                                            TT.show(PostActivity.this,
                                                    "'本周最佳'为无比活动内部字符，无法作为话题使用，感谢您的理解!");
                                        }
                                        if (lastTag.indexOf("每日精选") != -1) {
                                            TT.show(PostActivity.this,
                                                    "'每日精选'为无比活动内部字符，无法作为话题使用，感谢您的理解!");
                                        }
                                    }
                                    // 为了不保存数据
                                    isEdit = true;
                                    setResult(RESULT_OK);
                                    finish();
                                    if (mShouldOpenMulit) {
                                        startMulitVersionActivity();
                                    }
                                }
                            }
                        });
                    }
                }).start();
            }
        }

    }

    private void startMulitVersionActivity() {
        Intent intent2 = new Intent(this, WoLaiContrastDetailActivity.class);
        int color1 = ((ColorDrawable) etDesc1.getBackground()).getColor();
        ColorStateList textColors1 = etDesc1.getTextColors();
        int color2 = ((ColorDrawable) etDesc2.getBackground()).getColor();
        ColorStateList textColors2 = etDesc2.getTextColors();
        intent2.putExtra("paragraph3", paragraphEntity);
        intent2.putExtra("orientation", currentLayoutStyle);
        intent2.putExtra("titleBackColor", color1);
        intent2.putExtra("titleTextColor", textColors1);
        intent2.putExtra("resultBackColor", color2);
        intent2.putExtra("resultTextColor", textColors2);
        startActivity(intent2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearData();
    }

    /**
     * 发布兼容的对比度
     */
    private void postCompatibleContrast() {
        DD.d(TAG, "postCompatibleContrast()");

        ivCompatibleOtherUserImage.buildDrawingCache();
        Bitmap bitmap = ivCompatibleOtherUserImage.getDrawingCache();
        CommonUtil.saveBitmap(compatibleImageFile, bitmap);

        PostParagraphEntity postParagraphEntity = new PostParagraphEntity();
        if (isEdit) {
            postParagraphEntity.setParagraphId(paragraphEntity.getParagraphId());
        } else {
            postParagraphEntity.setParagraphId(UUID.randomUUID().toString());
        }
        postParagraphEntity.setParagraphContent(getParagraphContent());
        postParagraphEntity.setCreateUserId(PrefUtil.getIntValue(Config.PREF_USER_ID));
        // if (isRecreate) {
        // postParagraphEntity.setOriginName(paragraphEntity.getOriginName());
        // } else {
        postParagraphEntity.setOriginName(PrefUtil.getStringValue(Config.PREF_NICKNAME));
        // }
        postParagraphEntity.setOriginAuthor(CommonUtil.getUserLocation());
        postParagraphEntity.setTags(getTags());
        postParagraphEntity.setCreateTime(CommonUtil.getDefaultFormatCurrentTime());
        postParagraphEntity.setPicName(compatibleImageName);
        postParagraphEntity.setParagraphReplyId(UUID.randomUUID().toString());
        List<PostParagraphEntity> listPostParagraph = new ArrayList<PostParagraphEntity>();
        listPostParagraph.add(postParagraphEntity);

        RequestParams params = new RequestParams(Config.PATH_UPLOAD_PARAGRAPHS);
        params.setAsJsonContent(true);
        params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
        params.addParameter("paragraphs", listPostParagraph);
        if (isEdit) {
            params.addParameter("isModify", 1);
            postParagraphEntity.setCreateTime(paragraphEntity.getCreateTime());
        }
        x.http().post(params, new CommonCallback<JSONObject>() {
            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
            }

            @Override
            public void onFinished() {
                ravRefreshingView.setRefreshing(false);
            }

            @Override
            public void onSuccess(JSONObject result) {
                DD.d(TAG, "onSuccess(), result: " + result.toString());

                StatusResultEntity entity = JSON
                        .parseObject(result.toString(), StatusResultEntity.class);
                if ("0".equals(entity.getStatus())) {
                    // 上传图片
                    uploadImage();
                    setResult(RESULT_OK);
                    finish();
                    if (mShouldOpenMulit) {
                        startMulitVersionActivity();
                    }
                }
            }
        });
    }

    /**
     * 上传图片
     */
    private void uploadImage() {
        DD.d(TAG, "uploadImage()");

        RequestParams params = new RequestParams(Config.PATH_UPLOAD_PARAGRAPH_PIC);
        params.setMultipart(true);
        params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
        params.addParameter("picName", compatibleImageName);
        params.addParameter("picSizeTag", 1);
        params.addParameter("paragraphPic", compatibleImageFile);
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

                StatusResultEntity entity = JSON
                        .parseObject(result.toString(), StatusResultEntity.class);
                if ("0".equals(entity.getStatus())) {
                    // do nothing
                }
            }
        });
    }

    private float currentDegrees = 0f;

    @Event(value = R.id.rl_rotate)
    private void onRotateClick(View v) {
        DD.d(TAG, "onRotateClick()");

        RotateAnimation rotateAnimation = new RotateAnimation(currentDegrees, currentDegrees += 90f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(300);
        ivRotate.setAnimation(rotateAnimation);
        ivRotate.startAnimation(rotateAnimation);

        // TODO
        // if (useOtherUserImage) {
        // new MessageWithOkCancelFragment2(new MessageWithOkCancelFragment2.Callback() {
        // @Override
        // public void onOk() {
        // useOtherUserImage = false;
        // rlOtherUserImageContainer.setVisibility(View.GONE);
        // switchLayoutStyle();
        // }
        //
        // @Override
        // public void onCancel() {
        // }
        // }).show(getSupportFragmentManager(), "");
        // } else {
        switchLayoutStyle();
        // }
    }

    @Event(value = R.id.rl_switch_color)
    private void onSwitchColorClick(View v) {
        outerColor1 = 0;
        outerColor2 = 0;
        switchRandomColor();
    }

    public String getBitmapPath(Uri uri) {
        String[] pojo = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.managedQuery(uri, pojo, null, null, null);
        if (cursor != null) {
            Log.e("==图片大小===", "====图库===");
            int colunm_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(colunm_index);
            /***
             * 这里加这样一个判断主要是为了第三方的软件选择，比如：使用第三方的文件管理器的话，你选择的文件就不一定是图片了， 这样的话，我们判断文件的后缀名
             * 如果是图片格式的话，那么才可以
             */
            if (path.endsWith("jpg") || path.endsWith("png") || path.endsWith("jpeg")) {
                return path;
            }
        } else {
            Log.e("==图片大小===", "====本地===");
            try {
                File file = new File(new URI(uri.toString()));
                return file.getAbsolutePath();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void finish() {
        DD.d(TAG, "finish()");

        if (!isEdit && !isRecreate && TextUtils.isEmpty(topicNameFromTopicListPage)) {
            // save desc
            PrefUtil.setStringValue(Config.PREF_LAST_DESC_1, etDesc1.getText().toString());
            PrefUtil.setStringValue(Config.PREF_LAST_DESC_2, etDesc2.getText().toString());
        }
        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
            case select_left:
                switch (resultCode) {
                    case 1:
                    case 2:
                        path_left = getBitmapPath(data.getData());
                        break;
                    case 3:
                        int position = data.getIntExtra("position", -1);
                        if (position != -1) {
                            path_left = PopWindowSelectCaremer.photosList.get(position);
                        }
                        break;
                    default:
                        return;
                }
                bitmap_left_old = BitMapUtil.getBitmap(path_left, PostActivity.PIC_HEIGHT);
                // ivImage1.setImageBitmap(bitmap_left_old);
                // ivImage1.setBackgroundColor(Color.TRANSPARENT);
                ivImage1.setBitmap(bitmap_left_old);
                ivImage1.setVisibility(View.VISIBLE);
                // 保存图片路径
                lastImagePath1 = path_left;
                PrefUtil.setStringValue(Config.PREF_LAST_IMAGE_PATH_1, lastImagePath1);
                DD.d(TAG, "lastImagePath1: " + lastImagePath1);
                break;
            case select_right:
                switch (resultCode) {
                    case 1:
                    case 2:
                        path_right = getBitmapPath(data.getData());
                        break;
                    case 3:
                        int position = data.getIntExtra("position", -1);
                        if (position != -1) {
                            path_right = PopWindowSelectCaremer.photosList.get(position);
                        }
                        break;
                    default:
                        return;
                }
                bitmap_right_old = BitMapUtil.getBitmap(path_right, PostActivity.PIC_HEIGHT);
                // ivImage2.setImageBitmap(bitmap_right_old);
                // ivImage2.setBackgroundColor(Color.TRANSPARENT);
                ivImage2.setBitmap(bitmap_right_old);
                ivImage2.setVisibility(View.VISIBLE);
                // 保存图片路径
                lastImagePath2 = path_right;
                DD.d(TAG, "lastImagePath2: " + lastImagePath2);
                PrefUtil.setStringValue(Config.PREF_LAST_IMAGE_PATH_2, lastImagePath2);
                break;
            default:
                break;
        }
    }

}
