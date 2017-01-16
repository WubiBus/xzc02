package cn.incorner.contrast.data.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.Config;
import cn.incorner.contrast.R;
import cn.incorner.contrast.blur.FastBlur;
import cn.incorner.contrast.data.entity.DislikeParagraphResultEntity;
import cn.incorner.contrast.data.entity.LikeParagraphResultEntity;
import cn.incorner.contrast.data.entity.ParagraphCommentEntity;
import cn.incorner.contrast.data.entity.ParagraphEntity;
import cn.incorner.contrast.data.entity.StatusResultEntity;
import cn.incorner.contrast.page.ContrastCommentActivity;
import cn.incorner.contrast.page.LoginTransitionActivity;
import cn.incorner.contrast.page.MainActivity;
import cn.incorner.contrast.page.MyParagraphActivity;
import cn.incorner.contrast.page.PostActivity;
import cn.incorner.contrast.page.TopicSpecifiedListActivity;
import cn.incorner.contrast.page.UserParagraphListActivity;
import cn.incorner.contrast.page.WoLaiContrastDetailActivity;
import cn.incorner.contrast.util.CommonUtil;
import cn.incorner.contrast.util.DD;
import cn.incorner.contrast.util.DensityUtil;
import cn.incorner.contrast.util.PrefUtil;
import cn.incorner.contrast.util.StringUtils;
import cn.incorner.contrast.util.TT;
import cn.incorner.contrast.view.CircleImageView;
import cn.incorner.contrast.view.CustomRefreshFramework;
import cn.incorner.contrast.view.MessageWithOkCancelFragment2;
import cn.incorner.contrast.view.ReboundHorizontalScrollView;
import cn.incorner.contrast.view.RefreshingAnimationView;
import cn.incorner.contrast.view.RefreshingAnimationView.IRefreshingAnimationView;
import cn.incorner.contrast.view.ScrollListenerListView;

/**
 * 对比列表适配器
 *
 * @author yeshimin
 */
public class ContrastListAdapter extends BaseAdapter {

    private ViewHolder holder;

    private static final String TAG = "ContrastListAdapter";

    public final Pattern PATTERN_COMMENT_URL = Pattern
            .compile(Config.PATTERN_DESC_WITH_COMMENT_URL);
    public final Pattern PATTERN_COMMENT_URL_2 = Pattern
            .compile(Config.PATTERN_DESC_WITH_COMMENT_URL_2);
    public final Pattern PATTERN_DESC_WITH_COMMENT = Pattern
            .compile(Config.PATTERN_DESC_WITH_COMMENT);
    public final Pattern PATTERN_DESC_WITH_URL = Pattern.compile(Config.PATTERN_DESC_WITH_URL);
    public final Pattern PATTERN_URL = Pattern.compile(Config.PATTERN_URL);
    public final Pattern PATTERN_COLOR_RGB = Pattern.compile(Config.PATTERN_COLOR_RGB);
    public final Pattern PATTERN_COLOR_INDEX = Pattern.compile(Config.PATTERN_COLOR_INDEX);


    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //评论集合
    private ArrayList<ParagraphCommentEntity> listComment = new ArrayList<ParagraphCommentEntity>();
    private ParagraphEntity paragraphEntity;
    @ViewInject(R.id.rav_refreshing_view)
    private RefreshingAnimationView ravRefreshingView;
    //评论列表适配器
    private ContrastCommentListAdapter adapter;
    //是否是（当前）用户的数据
    private boolean isUserData;
    @ViewInject(R.id.crl_container)
    private CustomRefreshFramework crlContainer;
    @ViewInject(R.id.lv_content)
    private ScrollListenerListView lvContent;
    //刷新数据的
    //CustomRefreshFramework.OnRefreshingListener mListener;

    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    protected ArrayList<ParagraphEntity> list;
    private LayoutInflater inflater;
    private OnClickListener listener;
    private Context context;
    private OnDescClickListener onDescClickListener;
    private OnCommentClickListener onCommentClickListener;
    private OnSharePlatformClickListener onSharePlatformClickListener;

    private boolean isLikeBlocked = false;
    private boolean isDislikeBlocked = false;
    private boolean isDownloadBlocked = false;
    private boolean isReportBlocked = false;

    public ContrastListAdapter(ArrayList<ParagraphEntity> list, LayoutInflater inflater,
                               OnClickListener listener) {
        this.list = list;
        this.inflater = inflater;
        this.listener = listener;
        //this.mListener = mListener;
        onDescClickListener = new OnDescClickListener();
        onCommentClickListener = new OnCommentClickListener();
        onSharePlatformClickListener = new OnSharePlatformClickListener();
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
        final int userId = PrefUtil.getIntValue(Config.PREF_USER_ID);
        // TODO: 2017/1/9 ViewHolder怎么修饰都是没问题的s
        //final ViewHolder holder;
        if (convertView == null) {
            int itemLayout = getItemLayout();
            convertView = inflater.inflate(itemLayout, null);
            if (context == null) {
                context = convertView.getContext();
            }
            holder = new ViewHolder();
            setUpHolderView(convertView, holder);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.position = position;
        // set
        final ParagraphEntity entity = list.get(position);
        DD.d(TAG, "commentCount: " + entity.getCommentCount());
        setUpImageDes(holder, entity);
        if (shouldSetUpUser()) {
            setUpUser(holder, entity);
        }
        setUpLikeOrNot(holder, entity);
        setUpPositionTag(position, holder);
        setUpClickEvent(holder, entity);
        setUpAdjustUIBySelf(userId, holder, entity);
        setUpDescription(position, holder);
        setUpComment(position, holder, entity);
        setUpTagSection(holder, entity);
        setUpKeyBoardListener(holder, entity);
        if (shouldSetUpWolaiClick()) {
            setUpWolaiClick(parent, holder, entity);
        }
        return convertView;
    }

    protected boolean shouldSetUpUser() {
        return true;
    }

    protected void setUpUser(final ViewHolder holder, final ParagraphEntity entity) {
        // 发布者头像及昵称、时间、地点 和 操作部分
        if (TextUtils.isEmpty(entity.getUserAvatarName())) {
            holder.civHead.setImageResource(R.drawable.default_avatar);
        } else {
            x.image().loadDrawable(Config.getHeadFullPath(entity.getUserAvatarName()), null,
                    new CommonCallback<Drawable>() {
                        @Override
                        public void onCancelled(CancelledException arg0) {
                        }

                        @Override
                        public void onError(Throwable arg0, boolean arg1) {
                            holder.civHead.setImageResource(R.drawable.default_avatar);
                        }

                        @Override
                        public void onFinished() {
                        }

                        @Override
                        public void onSuccess(Drawable drawable) {
                            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                            holder.civHead.setImageBitmap(bitmap);
                        }
                    });
        }
        holder.tvNickname.setText(entity.getOriginName());
        holder.tvPublishTime.setText(CommonUtil.compareToDate(entity.getCreateTime()));
        holder.tvArea.setText(entity.getOriginAuthor());
        holder.tvArea.setVisibility(
                StringUtils.isEmpty(entity.getOriginAuthor().trim()) ? View.GONE : View.VISIBLE);
        holder.hsvFunctionContainer.smoothScrollTo(entity.functionScrollLocation, 0);
        holder.hsvFunctionContainer.getViewTreeObserver()
                .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        entity.functionScrollLocation = holder.hsvFunctionContainer.getScrollX();
                    }
                });
    }

    private void setUpLikeOrNot(ViewHolder holder, ParagraphEntity entity) {
        // TODO: 2016/8/25
        //给 评论,喜欢,反感 设置数字的颜色
        //holder.tvComment.setText("评论" + (entity.getCommentCount() > 0 ? entity.getCommentCount() : ""));
        String tvcomment = "评论" + (entity.getCommentCount() > 0 ? entity.getCommentCount() : "");
        int a = ((entity.getCommentCount() > 0 ? entity.getCommentCount() : "") + "").length();
        SpannableString sas = new SpannableString(tvcomment);
        sas.setSpan(new ForegroundColorSpan(Color.rgb(255, 226, 0)), 2, 2 + a,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.tvComment.setText(sas);


        //holder.tvLike.setText("喜欢" + (entity.getLikeCount() > 0 ? entity.getLikeCount() : ""));
        String tvlike = "喜欢" + (entity.getLikeCount() > 0 ? entity.getLikeCount() : "");
        int b = ((entity.getLikeCount() > 0 ? entity.getLikeCount() : "") + "").length();
        SpannableString sbs = new SpannableString(tvlike);
        sbs.setSpan(new ForegroundColorSpan(Color.rgb(255, 180, 0)), 2, 2 + b,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.tvLike.setText(sbs);


        //holder.tvDislike.setText("反感" + (entity.getComplaintCount() > 0 ? entity.getComplaintCount() : ""));
        String tvdislike =
                "反感" + (entity.getComplaintCount() > 0 ? entity.getComplaintCount() : "");
        int c = ((entity.getComplaintCount() > 0 ? entity.getComplaintCount() : "") + "").length();
        SpannableString scs = new SpannableString(tvdislike);
        scs.setSpan(new ForegroundColorSpan(Color.rgb(195, 13, 35)), 2, 2 + c,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.tvDislike.setText(scs);


        holder.ivLike.setImageResource(
                entity.getLikeState() > 0 ? R.drawable.xihuan_selected1 : R.drawable.xihuan_normal);
        holder.ivDislike.setImageResource(entity.getComplaintState()
                > 0 ? R.drawable.fangan_selected : R.drawable.fangan_normal);
    }

    protected void setUpImageDes(ViewHolder holder, ParagraphEntity entity) {
        // 图片及描述部分
        long t1_layout = System.currentTimeMillis();
        int layoutOrientation = getLayoutOrientation(entity);
        holder.orientation = layoutOrientation;
        if (layoutOrientation >= 0) {
            if (shouldAjustOrientation()) {
                setLayoutView(layoutOrientation, holder);
            }
            setLayoutStyleAndContent(layoutOrientation, holder, entity);
        }
        long t2_layout = System.currentTimeMillis();
        DD.d(TAG, "d2_layout: " + (t2_layout - t1_layout));
    }

    protected boolean shouldAjustOrientation() {
        return true;
    }

    protected void setUpPositionTag(int position, ViewHolder holder) {
        if (holder.llImageContainer != null) {
            holder.llImageContainer.setTag(position);
        }

        if (holder.ivCompatibleImage != null) {
            holder.ivCompatibleImage.setTag(position);
        }

        if (holder.civHead != null) {
            holder.civHead.setTag(position);
        }

        if (holder.llLike != null) {
            holder.llLike.setTag(position);
        }

        if (holder.llDislike != null) {
            holder.llDislike.setTag(position);
        }

        if (holder.llComment != null) {
            holder.llComment.setTag(position);
        }

        if (holder.llEdit != null) {
            holder.llEdit.setTag(position);
        }

        if (holder.llQQ != null) {
            holder.llQQ.setTag(position);
        }

        if (holder.llQQ != null) {
            holder.llQQ.setTag(R.id.ll_qq, holder);
        }

        if (holder.llWeixin != null) {
            holder.llWeixin.setTag(position);
        }

        if (holder.llWeixin != null) {
            holder.llWeixin.setTag(R.id.ll_weixin, holder);
        }

        if (holder.llWeibo != null) {
            holder.llWeibo.setTag(position);
        }

        if (holder.llWeibo != null) {
            holder.llWeibo.setTag(R.id.ll_weibo, holder);
        }

        if (holder.llRecreate != null) {
            holder.llRecreate.setTag(position);
        }

        if (holder.llDownload != null) {
            holder.llDownload.setTag(position);
        }

        if (holder.llReport != null) {
            holder.llReport.setTag(position);
        }

        if (holder.llDelete != null) {
            holder.llDelete.setTag(position);
        }

        if (holder.llScrollBack != null) {
            holder.llScrollBack.setTag(position);
        }

        if (holder.tvWolai != null) {
            holder.tvWolai.setTag(position);
        }

    }

    protected void setUpClickEvent(final ViewHolder holder, final ParagraphEntity entity) {
        // 点击大图不再需要跳转到评论页面
        // holder.llImageContainer.setOnClickListener(listener);
        // holder.ivCompatibleImage.setOnClickListener(listener);
        if (holder.civHead != null) {
            holder.civHead.setOnClickListener(listener);
        }
        if (holder.llLike != null) {
            holder.llLike.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!isLikeBlocked) {
                        like(list.get((int) v.getTag()));
                    }
                }
            });
        }
        if (holder.llDislike != null) {
            holder.llDislike.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!isDislikeBlocked) {
                        dislike(list.get((int) v.getTag()));
                    }
                }
            });
        }
        if (holder.llComment != null) {
            holder.llComment.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(context, ContrastCommentActivity.class);
                    intent.putExtra("paragraph", entity);
                    intent.putExtra("hasFocus", true);
                    BaseActivity.sGotoActivity(context, intent);
                }
            });
        }
        if (holder.llEdit != null) {
            holder.llEdit.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(context, PostActivity.class);
                    intent.putExtra("paragraph", entity);
                    intent.putExtra("isEdit", true);
                    ((Activity) context)
                            .startActivityForResult(intent, MainActivity.REQUEST_CODE_POST);
                    // BaseActivity.sGotoActivity(context, intent);
                }
            });
        }
        if (holder.llShare != null) {
            holder.llShare.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    holder.hsvFunctionContainer.smoothScrollTo(holder.llMore.getLeft(), 0);
                }
            });
        }
        if (holder.llMore != null) {
            holder.llMore.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    holder.hsvFunctionContainer.smoothScrollTo(holder.llWeibo.getLeft(), 0);
                }
            });
        }
        if (holder.llQQ != null) {
            holder.llQQ.setOnClickListener(onSharePlatformClickListener);
        }

        if (holder.llWeixin != null) {
            holder.llWeixin.setOnClickListener(onSharePlatformClickListener);
        }

        if (holder.llWeibo != null) {
            holder.llWeibo.setOnClickListener(onSharePlatformClickListener);
        }
        //点击借题发挥
        if (holder.llRecreate != null) {
            holder.llRecreate.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!BaseActivity.sIsLogined()) {
                        BaseActivity.sGotoActivity(context, LoginTransitionActivity.class);
                        BaseActivity.sFinishActivity(context);
                        return;
                    }
                    holder.hsvFunctionContainer.setSaveEnabled(true);
                    int position = (int) v.getTag();
                    Intent intent = new Intent();
                    intent.setClass(context, PostActivity.class);
                    intent.putExtra("paragraph", list.get(position));
                    ((Activity) context)
                            .startActivityForResult(intent, MainActivity.REQUEST_CODE_POST);
                    // BaseActivity.sGotoActivity(context, intent);
                }
            });
        }
        if (holder.llDownload != null) {
            holder.llDownload.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!isDownloadBlocked) {
                        download(holder, list.get((int) v.getTag()));
                    }
                }
            });
        }
        if (holder.llReport != null) {
            holder.llReport.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!isReportBlocked) {
                        report(list.get((int) v.getTag()));
                    }
                }
            });
        }
        if (holder.llDelete != null) {
            holder.llDelete.setOnClickListener(new OnClickListener() {
                public void onClick(final View v) {
                    new MessageWithOkCancelFragment2(new MessageWithOkCancelFragment2.Callback() {
                        @Override
                        public void onOk() {
                            delete(list.get((int) v.getTag()), (int) v.getTag());
                        }

                        @Override
                        public void onCancel() {
                        }
                    }, R.layout.frag_message_with_ok_cancel_delete_contrast)
                            .show(((FragmentActivity) context).getSupportFragmentManager(), "");
                }
            });
        }
        if (holder.llScrollBack != null) {
            holder.llScrollBack.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    holder.hsvFunctionContainer.smoothScrollTo(0, 0);
                }
            });
        }
    }

    protected void setUpAdjustUIBySelf(int userId, ViewHolder holder, ParagraphEntity entity) {
        // 跟据是否是自己发布的调整UI
        if (userId == entity.getUserId()) {
            holder.llDislike.setVisibility(View.GONE);
            holder.llReport.setVisibility(View.GONE);
            holder.llEdit.setVisibility(View.VISIBLE);
            holder.llDelete.setVisibility(View.VISIBLE);
        } else {
            holder.llDislike.setVisibility(View.VISIBLE);
            holder.llReport.setVisibility(View.VISIBLE);
            holder.llEdit.setVisibility(View.GONE);
            holder.llDelete.setVisibility(View.GONE);
        }
    }

    protected void setUpDescription(int position, ViewHolder holder) {
        // 描述链接相关
        holder.tvDesc1.setTag(position);
        holder.tvDesc2.setTag(position);
        holder.tvDesc1.setOnClickListener(onDescClickListener);
        holder.tvDesc2.setOnClickListener(onDescClickListener);
    }

    protected void setUpComment(int position, ViewHolder holder, ParagraphEntity entity) {
        // 评论部分
        List<ParagraphCommentEntity> listComment = entity.getComments();
        if (listComment != null && !listComment.isEmpty()) {
            holder.llCommentContainer.setVisibility(View.VISIBLE);

            if (listComment.size() > 5) {
                listComment = new ArrayList<ParagraphCommentEntity>();
                listComment.addAll(entity.getComments().subList(0, 5));
            }
            int size = listComment.size();
            for (int i = 0; i < 5; ++i) {
                if (i < size) {
                    holder.arrCommentContainer[i].setVisibility(View.VISIBLE);
                } else {
                    holder.arrCommentContainer[i].setVisibility(View.GONE);
                }
            }
            for (int i = 0; i < size; ++i) {
                final ParagraphCommentEntity commentEntity = listComment.get(i);
                holder.arrCommentContainer[i].setTag(position);
                holder.arrCommentContainer[i].setOnClickListener(onCommentClickListener);
                holder.arrCommentHead[i].setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (PrefUtil.getIntValue(Config.PREF_USER_ID) == commentEntity
                                .getReplyUserId()) {
                            BaseActivity.sGotoActivity(context, MyParagraphActivity.class);
                        } else {
                            Intent intent = new Intent();
                            intent.setClass(context, UserParagraphListActivity.class);
                            intent.putExtra("userId", commentEntity.getReplyUserId());
                            intent.putExtra("head", commentEntity.getReplyUserPic());
                            intent.putExtra("nickname", commentEntity.getReplyUserNickname());
                            BaseActivity.sGotoActivity(context, intent);
                        }
                    }
                });
                if (commentEntity.getReplyUserisAnonymous() == 1) {
                    holder.arrCommentHead[i].setImageResource(R.drawable.default_avatar);
                } else {
                    if (TextUtils.isEmpty(commentEntity.getReplyUserPic())) {
                        holder.arrCommentHead[i].setImageResource(R.drawable.default_avatar);
                    } else {
                        ImageLoader.getInstance().displayImage(
                                Config.getHeadFullPath(commentEntity.getReplyUserPic()),
                                holder.arrCommentHead[i]);
                    }
                }
                if (commentEntity.getReplyUserisAnonymous() == 1) {
                    holder.arrCommentNickname[i].setText("匿名");
                } else {
                    holder.arrCommentNickname[i].setText(commentEntity.getReplyUserNickname());
                }
                holder.arrCommentDate[i].setText(commentEntity.getCreateTime());
                holder.arrCommentLocation[i].setText(commentEntity.getLocation());
                holder.arrComment[i].setText(commentEntity.getReplyContent());
            }
        } else {
            holder.llCommentContainer.setVisibility(View.GONE);
        }
    }

    protected void setUpTagSection(final ViewHolder holder, final ParagraphEntity entity) {
        // 标签部分
        holder.llTagContainer.removeAllViews();
        String tags = entity.getTags();
        DD.d(TAG, "tags: " + tags);
        if (!TextUtils.isEmpty(tags)) {
            final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = DensityUtil.dip2px(context, 3);
            params.rightMargin = DensityUtil.dip2px(context, 3);
            Pattern pattern = Pattern.compile(Config.PATTERN_TAG);
            final Matcher matcher = pattern.matcher(tags);
            while (matcher.find()) {
                ViewGroup vgTag = (ViewGroup) inflater.inflate(R.layout.item_tag_selected, null);
                TextView tvTag = (TextView) vgTag.findViewById(R.id.tv_tag);
                final String tag = matcher.group(1);
                DD.d(TAG, "tag: " + tag);
                tvTag.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(context, TopicSpecifiedListActivity.class);
                        intent.putExtra("topicName", tag);
                        intent.putExtra("newEntity", entity);
                        if (((Activity) context) instanceof MyParagraphActivity) {
                            intent.putExtra("isUserData", true);
                        }
                        BaseActivity.sGotoActivity(context, intent);
                    }
                });
                // 设置左右间距
                tvTag.setLayoutParams(params);
                tvTag.setText("#" + matcher.group(1).replace("本周最佳", "无比无真相"));
                vgTag.removeView(tvTag);
                holder.llTagContainer.addView(tvTag);
            }
            // 调整标签位置
            holder.llTagContainer.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                        boolean hasDone = false;

                        @Override
                        public void onGlobalLayout() {
                            if (hasDone) {
                                return;
                            }
                            hasDone = true;
                            int width1 = holder.llTagContainer.getWidth() - holder.llTagContainer
                                    .getPaddingLeft();
                            int width2 = holder.hsvTagContainer.getWidth();
                            if (width1 < width2) {
                                int leftPadding = (width2 - width1) / 2;
                                holder.llTagContainer.setPadding(leftPadding, 0, 0, 0);
                            } else {
                                holder.llTagContainer.setPadding(0, 0, 0, 0);
                            }
                        }
                    });
        }
    }

    protected void setUpKeyBoardListener(final ViewHolder holder, final ParagraphEntity entity) {
        //键盘 确认键监听
        // TODO: 2016/8/4
        //adapter = new ContrastCommentListAdapter(listComment, LayoutInflater.from());
        holder.et_answer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //如果点击的是 完成 键
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //获取用户输入的内容
                    String userContent = holder.et_answer.getText().toString();
                    //判断答案是否为空
                    if (!TextUtils.isEmpty(userContent)) {
                        holder.llAnswerQuestionShow.setVisibility(View.GONE);
                        //同时隐藏面具 显示内容
                        hideMask(holder.llMaskContainer);
                        hideMask(holder.tvMask);
                        //提交数据
                        postData(userContent, entity, holder.et_answer);
                    }
                }
                return false;
            }
        });
    }

    protected boolean shouldSetUpWolaiClick() {
        return true;
    }

    protected void setUpWolaiClick(ViewGroup parent, final ViewHolder holder,
                                   final ParagraphEntity entity) {
        //我来 点击监听事件
        if (entity.getSeriesCount() == 1) {
            holder.tvWolai.setBackgroundResource(R.drawable.comeon_me);
            holder.tvWolai.setText("");
        } else {
            holder.tvWolai.setBackgroundResource(R.drawable.shape__radius_4__stroke_white);
            String version = parent.getContext().getResources().getString(R.string.version);
            holder.tvWolai.setText(entity.getSeriesCount() + version);
            int padding = DensityUtil.dip2px(parent.getContext(), 8);
            holder.tvWolai.setPadding(padding, 0, padding, 0);
        }

        holder.tvWolai.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DD.d(TAG, "徐志超 。。。。。。。。。。。。。。。。。。。。。。。。。。。" + entity.getSeriesId());
                if (!BaseActivity.sIsLogined()) {
                    BaseActivity.sGotoActivity(context, LoginTransitionActivity.class);
                    BaseActivity.sFinishActivity(context);
                    return;
                } else if (entity.getSeriesCount() == 1) {
                    DD.d(TAG, "postData().................................." + entity
                            .getSeriesCount());
                    int position2 = holder.position;
                    Intent intent2 = new Intent();
                    intent2.setClass(context, PostActivity.class);
                    //如果传的是 paragraph2 的话就是 我来
                    intent2.putExtra("paragraph2", list.get(position2));
                    intent2.putExtra("openMulit", true);
                    intent2.putExtra("hasSeries", true);
                    intent2.putExtra("orientation", holder.orientation);
                    ((Activity) context)
                            .startActivityForResult(intent2, MainActivity.REQUEST_CODE_POST);
                } else {
                    //跳转到几个版本的页面
                    int position2 = holder.position;
                    Intent intent2 = new Intent();
                    intent2.setClass(context, WoLaiContrastDetailActivity.class);
                    //如果传的是 paragraph3 的话就是 几个版本的页面
                    intent2.putExtra("paragraph3", list.get(position2));
                    intent2.putExtra("orientation", holder.orientation);
                    int color1 = ((ColorDrawable) holder.tvDesc1.getBackground()).getColor();
                    ColorStateList textColors1 = holder.tvDesc1.getTextColors();
                    int color2 = ((ColorDrawable) holder.tvDesc2.getBackground()).getColor();
                    ColorStateList textColors2 = holder.tvDesc2.getTextColors();
                    intent2.putExtra("titleBackColor", color1);
                    intent2.putExtra("titleTextColor", textColors1);
                    intent2.putExtra("resultBackColor", color2);
                    intent2.putExtra("resultTextColor", textColors2);
                    ((Activity) context)
                            .startActivityForResult(intent2, MainActivity.REQUEST_CODE_POST);
                }

            }
        });
    }

    protected void setUpHolderView(View convertView, ViewHolder holder) {
        // 图片及描述部分
        holder.llImageAndDescContainer = (LinearLayout) convertView
                .findViewById(R.id.ll_image_and_desc_container);
        holder.llTopDescContainer = (LinearLayout) convertView
                .findViewById(R.id.ll_top_desc_container);
        holder.llBottomDescContainer = (LinearLayout) convertView
                .findViewById(R.id.ll_bottom_desc_container);
        holder.rlDescContainer1 = (RelativeLayout) convertView
                .findViewById(R.id.rl_desc_container_1);
        holder.rlDescContainer2 = (RelativeLayout) convertView
                .findViewById(R.id.rl_desc_container_2);
        holder.tvDesc1 = (TextView) convertView.findViewById(R.id.tv_desc_1);
        holder.ivUrlFlag1 = (ImageView) convertView.findViewById(R.id.iv_url_flag_1);
        holder.ivUrlFlag2 = (ImageView) convertView.findViewById(R.id.iv_url_flag_2);
        holder.tvDesc2 = (TextView) convertView.findViewById(R.id.tv_desc_2);
        holder.llImageContainer = (LinearLayout) convertView.findViewById(R.id.ll_image_container);
        holder.ivImage1 = (ImageView) convertView.findViewById(R.id.iv_image_1);
        holder.ivImage2 = (ImageView) convertView.findViewById(R.id.iv_image_2);
        holder.ivCompatibleImage = (ImageView) convertView.findViewById(R.id.iv_compatible_image);
        holder.llMaskContainer = (LinearLayout) convertView.findViewById(R.id.ll_mask_container);
        holder.rlMask1 = (RelativeLayout) convertView.findViewById(R.id.rl_mask_1);
        holder.ivHuodong = (ImageView) convertView.findViewById(R.id.iv_huodong);
        holder.ivZuire = (ImageView) convertView.findViewById(R.id.iv_zuire);
        holder.ivJingxuan = (ImageView) convertView.findViewById(R.id.iv_jingxuan);
        holder.ivYuanchuang = (ImageView) convertView.findViewById(R.id.iv_yuanchuang);
        holder.rlMask2 = (RelativeLayout) convertView.findViewById(R.id.rl_mask_2);
        holder.tvMask = (TextView) convertView.findViewById(R.id.tv_mask);

        holder.civHead = (CircleImageView) convertView.findViewById(R.id.civ_head);
        holder.tvNickname = (TextView) convertView.findViewById(R.id.tv_nickname);
        holder.tvPublishTime = (TextView) convertView.findViewById(R.id.tv_publish_time);
        holder.tvArea = (TextView) convertView.findViewById(R.id.tv_area);
        holder.hsvFunctionContainer = (HorizontalScrollView) convertView
                .findViewById(R.id.hsv_function_button_container);
        holder.llLike = (LinearLayout) convertView.findViewById(R.id.ll_like);
        holder.tvComment = (TextView) convertView.findViewById(R.id.tv_comment);
        holder.ivLike = (ImageView) convertView.findViewById(R.id.iv_like);
        holder.tvLike = (TextView) convertView.findViewById(R.id.tv_like);
        holder.ivDislike = (ImageView) convertView.findViewById(R.id.iv_dislike);
        holder.tvDislike = (TextView) convertView.findViewById(R.id.tv_dislike);
        holder.llDislike = (LinearLayout) convertView.findViewById(R.id.ll_dislike);
        holder.llComment = (LinearLayout) convertView.findViewById(R.id.ll_comment);
        holder.llEdit = (LinearLayout) convertView.findViewById(R.id.ll_edit);
        holder.llShare = (LinearLayout) convertView.findViewById(R.id.ll_share);
        holder.llMore = (LinearLayout) convertView.findViewById(R.id.ll_more);
        holder.llQQ = (LinearLayout) convertView.findViewById(R.id.ll_qq);
        holder.llWeixin = (LinearLayout) convertView.findViewById(R.id.ll_weixin);
        holder.llWeibo = (LinearLayout) convertView.findViewById(R.id.ll_weibo);
        holder.llRecreate = (LinearLayout) convertView.findViewById(R.id.ll_recreate);
        holder.llDownload = (LinearLayout) convertView.findViewById(R.id.ll_download);
        holder.llReport = (LinearLayout) convertView.findViewById(R.id.ll_report);
        holder.llDelete = (LinearLayout) convertView.findViewById(R.id.ll_delete);
        holder.llScrollBack = (LinearLayout) convertView.findViewById(R.id.ll_scroll_back);

        //回答问题部分<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        holder.llAnswerQuestionShow = (LinearLayout) convertView
                .findViewById(R.id.ll_answer_question_show);
        holder.tv_question = (TextView) convertView.findViewById(R.id.tv_question);
        holder.et_answer = (EditText) convertView.findViewById(R.id.et_answer);

        //点击显示的按钮
        holder.btnShow = (ImageView) convertView.findViewById(R.id.btn_show);
        //回答问题部分<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

        // 我来
        holder.tvWolai = (TextView) convertView.findViewById(R.id.tv_wolai);

        // 评论部分
        holder.llCommentContainer = (LinearLayout) convertView
                .findViewById(R.id.ll_comment_container);
        holder.llComment1 = (LinearLayout) convertView.findViewById(R.id.ll_comment_1);
        holder.llComment2 = (LinearLayout) convertView.findViewById(R.id.ll_comment_2);
        holder.llComment3 = (LinearLayout) convertView.findViewById(R.id.ll_comment_3);
        holder.llComment4 = (LinearLayout) convertView.findViewById(R.id.ll_comment_4);
        holder.llComment5 = (LinearLayout) convertView.findViewById(R.id.ll_comment_5);
        holder.civCommentHead1 = (CircleImageView) convertView
                .findViewById(R.id.civ_comment_head_1);
        holder.civCommentHead2 = (CircleImageView) convertView
                .findViewById(R.id.civ_comment_head_2);
        holder.civCommentHead3 = (CircleImageView) convertView
                .findViewById(R.id.civ_comment_head_3);
        holder.civCommentHead4 = (CircleImageView) convertView
                .findViewById(R.id.civ_comment_head_4);
        holder.civCommentHead5 = (CircleImageView) convertView
                .findViewById(R.id.civ_comment_head_5);
        holder.tvCommentNickname1 = (TextView) convertView.findViewById(R.id.tv_comment_nickname_1);
        holder.tvCommentNickname2 = (TextView) convertView.findViewById(R.id.tv_comment_nickname_2);
        holder.tvCommentNickname3 = (TextView) convertView.findViewById(R.id.tv_comment_nickname_3);
        holder.tvCommentNickname4 = (TextView) convertView.findViewById(R.id.tv_comment_nickname_4);
        holder.tvCommentNickname5 = (TextView) convertView.findViewById(R.id.tv_comment_nickname_5);
        holder.tvCommentDate1 = (TextView) convertView.findViewById(R.id.tv_comment_date_1);
        holder.tvCommentDate2 = (TextView) convertView.findViewById(R.id.tv_comment_date_2);
        holder.tvCommentDate3 = (TextView) convertView.findViewById(R.id.tv_comment_date_3);
        holder.tvCommentDate4 = (TextView) convertView.findViewById(R.id.tv_comment_date_4);
        holder.tvCommentDate5 = (TextView) convertView.findViewById(R.id.tv_comment_date_5);
        holder.tvCommentLocation1 = (TextView) convertView.findViewById(R.id.tv_comment_location_1);
        holder.tvCommentLocation2 = (TextView) convertView.findViewById(R.id.tv_comment_location_2);
        holder.tvCommentLocation3 = (TextView) convertView.findViewById(R.id.tv_comment_location_3);
        holder.tvCommentLocation4 = (TextView) convertView.findViewById(R.id.tv_comment_location_4);
        holder.tvCommentLocation5 = (TextView) convertView.findViewById(R.id.tv_comment_location_5);
        holder.tvComment1 = (TextView) convertView.findViewById(R.id.tv_comment_1);
        holder.tvComment2 = (TextView) convertView.findViewById(R.id.tv_comment_2);
        holder.tvComment3 = (TextView) convertView.findViewById(R.id.tv_comment_3);
        holder.tvComment4 = (TextView) convertView.findViewById(R.id.tv_comment_4);
        holder.tvComment5 = (TextView) convertView.findViewById(R.id.tv_comment_5);
        holder.arrCommentContainer[0] = holder.llComment1;
        holder.arrCommentContainer[1] = holder.llComment2;
        holder.arrCommentContainer[2] = holder.llComment3;
        holder.arrCommentContainer[3] = holder.llComment4;
        holder.arrCommentContainer[4] = holder.llComment5;
        holder.arrCommentHead[0] = holder.civCommentHead1;
        holder.arrCommentHead[1] = holder.civCommentHead2;
        holder.arrCommentHead[2] = holder.civCommentHead3;
        holder.arrCommentHead[3] = holder.civCommentHead4;
        holder.arrCommentHead[4] = holder.civCommentHead5;
        holder.arrCommentNickname[0] = holder.tvCommentNickname1;
        holder.arrCommentNickname[1] = holder.tvCommentNickname2;
        holder.arrCommentNickname[2] = holder.tvCommentNickname3;
        holder.arrCommentNickname[3] = holder.tvCommentNickname4;
        holder.arrCommentNickname[4] = holder.tvCommentNickname5;
        holder.arrCommentDate[0] = holder.tvCommentDate1;
        holder.arrCommentDate[1] = holder.tvCommentDate2;
        holder.arrCommentDate[2] = holder.tvCommentDate3;
        holder.arrCommentDate[3] = holder.tvCommentDate4;
        holder.arrCommentDate[4] = holder.tvCommentDate5;
        holder.arrCommentLocation[0] = holder.tvCommentLocation1;
        holder.arrCommentLocation[1] = holder.tvCommentLocation2;
        holder.arrCommentLocation[2] = holder.tvCommentLocation3;
        holder.arrCommentLocation[3] = holder.tvCommentLocation4;
        holder.arrCommentLocation[4] = holder.tvCommentLocation5;
        holder.arrComment[0] = holder.tvComment1;
        holder.arrComment[1] = holder.tvComment2;
        holder.arrComment[2] = holder.tvComment3;
        holder.arrComment[3] = holder.tvComment4;
        holder.arrComment[4] = holder.tvComment5;
        // 标签部分
        holder.hsvTagContainer = (ReboundHorizontalScrollView) convertView
                .findViewById(R.id.hsv_tag_container);
        holder.llTagContainer = (LinearLayout) convertView.findViewById(R.id.ll_tag_container);
    }


    //提交数据
    private void postData(final String userContent, final ParagraphEntity entity,
                          final EditText etanswer) {
        //打个log
        DD.d(TAG, "postData().................................." + userContent);

        RequestParams params = new RequestParams(Config.PATH_ADD_COMMENT);
        params.setAsJsonContent(true);
        params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
        params.addParameter("paragraphReplyId", entity.getParagraphReplyId());
        params.addParameter("replyToUserId", entity.getUserId());
        params.addParameter("replyContent", userContent);
        params.addParameter("location", CommonUtil.getUserLocation());
        DD.d(TAG, "params:.............................................................. " + params
                .toJSONString());
        x.http().post(params, new CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObjectResult) {
                DD.d(TAG, "onSuccess(), result: " + jsonObjectResult);

                final StatusResultEntity resultEntity = JSON
                        .parseObject(jsonObjectResult.toString(), StatusResultEntity.class);
                if ("0".equals(resultEntity.getStatus())) {
                    ParagraphCommentEntity commentEntity = new ParagraphCommentEntity();
                    commentEntity.setReplyUserisAnonymous(
                            PrefUtil.getIntValue(Config.PREF_IS_ANONYMOUS));
                    commentEntity.setReplyUserId(PrefUtil.getIntValue(Config.PREF_USER_ID));
                    commentEntity.setReplyContent(userContent);
                    commentEntity.setParagraphReplyId(paragraphEntity.getParagraphReplyId());
                    commentEntity.setReplyUserPic(
                            commentEntity.getReplyUserisAnonymous() == 1 ? "" : PrefUtil
                                    .getStringValue(Config.PREF_AVATAR_NAME));
                    commentEntity
                            .setReplyUserNickname(PrefUtil.getStringValue(Config.PREF_NICKNAME));
                    commentEntity.setCreateTime(CommonUtil.getDefaultFormatCurrentTime());
                    commentEntity.setLocation(CommonUtil.getUserLocation());
                    listComment.add(0, commentEntity);
                    //刷新评论
                    entity.getComments().add(0, commentEntity);
                    notifyDataSetChanged();
                    etanswer.setText("");

                }
            }

            @Override
            public void onError(Throwable throwable, boolean b) {

            }

            @Override
            public void onCancelled(CancelledException e) {

            }

            @Override
            public void onFinished() {
                ravRefreshingView.setRefreshing(false);
            }
        });
    }

    public int getLayoutOrientation(ParagraphEntity entity) {
        mContent = entity.getParagraphContent();
        if (TextUtils.isEmpty(mContent)) {
            return -1;
        }
        // 判断图文是横向布局还是纵向布局
        int orientation = LinearLayout.HORIZONTAL;
        if (mContent.contains("[up]")) {
            orientation = LinearLayout.VERTICAL;
        }
        return orientation;
    }

    protected int getItemLayout() {
        return R.layout.item_contrast_list;
    }

    /**
     * 根据布局信息调整布局
     */
    private String mContent;

    protected boolean shouldSetUpMask() {
        return true;
    }

    protected void setLayoutStyleAndContent(int orientation, final ViewHolder holder,
                                            final ParagraphEntity entity) {
        // 设置图片及蒙板
        if (entity.hasSeened) {
            holder.llMaskContainer.setVisibility(View.GONE);
            holder.tvMask.setVisibility(View.GONE);
        } else {
            holder.llMaskContainer.setVisibility(View.VISIBLE);
            holder.tvMask.setVisibility(View.VISIBLE);

            // 设置圆形标签：活动、最热、精选、原创，如果出现“最热”，则不需要“精选”
            holder.ivHuodong.setVisibility(View.GONE);
            holder.ivZuire.setVisibility(View.GONE);
            holder.ivJingxuan.setVisibility(View.GONE);
            holder.ivYuanchuang.setVisibility(View.GONE);
            if (entity.getTags().contains(Config.PATTERN_HORN)) {
                holder.ivHuodong.setVisibility(View.VISIBLE);
            }
            if (entity.getParagraphScore() > 600) {
                holder.ivZuire.setVisibility(View.VISIBLE);
            }
            //			else if (entity.getContrastSelect() == 1) {
            //				holder.ivJingxuan.setVisibility(View.VISIBLE);
            //			}
            // 原创
            if (mContent.contains(Config.PATTERN_MARK_ORIGINAL)) {
                holder.ivYuanchuang.setVisibility(View.VISIBLE);
            }
        }
        mContent = mContent.replace(Config.PATTERN_MARK_ORIGINAL, "");

        final int maskOrientation = orientation;
        // 判断是否兼容模式
        String[] arrPicName = parsePicName(entity.getPicName());
        if (arrPicName != null) {
            if (arrPicName.length == 2) {
                holder.llImageContainer.setVisibility(View.VISIBLE);
                holder.ivCompatibleImage.setVisibility(View.GONE);
                x.image().bind(holder.ivImage1, Config.getContrastFullPath(arrPicName[0]));
                x.image().bind(holder.ivImage2, Config.getContrastFullPath(arrPicName[1]),
                        new CommonCallback<Drawable>() {
                            @Override
                            public void onSuccess(Drawable drawable) {
                                Bitmap bitmap;
                                Bitmap overlay;
                                int overlayWidth = holder.ivImage2.getWidth();
                                int overlayHeight = holder.ivImage2.getHeight();

                                // holder.ivImage2.buildDrawingCache();
                                // bitmap = holder.ivImage2.getDrawingCache();
                                bitmap = ((BitmapDrawable) drawable).getBitmap();
                                overlay = Bitmap.createBitmap((int) (bitmap.getWidth() / 32f),
                                        (int) (bitmap.getHeight() / 32f), Bitmap.Config.ARGB_8888);
                                Canvas canvas = new Canvas(overlay);
                                canvas.translate(0, 0);
                                canvas.scale(1 / 32f, 1 / 32f);
                                Paint paint = new Paint();
                                paint.setFlags(Paint.FILTER_BITMAP_FLAG);
                                canvas.drawBitmap(bitmap, 0, 0, paint);
                                overlay = FastBlur.doBlur(overlay, 10, true);
                                // overlay = blur.blur(overlay, true);

                                holder.rlMask2.setBackgroundDrawable(new BitmapDrawable(overlay));

                                // TODO: 2016/8/5
                                //点击显示隐藏的内容
                                holder.rlMask2.setOnClickListener(new OnClickListener() {
                                    public void onClick(View v) {
                                        entity.hasSeened = true;
                                        holder.llMaskContainer.setVisibility(View.GONE);
                                        holder.tvMask.setVisibility(View.GONE);

                                        // TODO: 2016/8/23
                                        //此功能暂时屏蔽
                                        /*entity.hasSeened = true;
                                        LogUtil.e(mContent.toString()+"--------------");
										String[] arrContent = mContent.trim().split("\\] \\[");
										// 提取并设置内容
										// 不用空格做分隔符是因为内容中可能会有分隔符，所以使用 "] [" 做分隔符
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
											LogUtil.e(arrContent[i]);
											// TODO: 2016/8/5
											//判断是否包含字符
											if (arrContent[i].contains("<?>")) {
												//showToast("点击holder.rlMask2  you");

												holder.btnShow.setVisibility(View.GONE);
												holder.llAnswerQuestionShow.setVisibility(View.VISIBLE);
												holder.llMaskContainer.setVisibility(View.VISIBLE);
												holder.tvMask.setVisibility(View.VISIBLE);

												//让输入框显示光标
												holder.et_answer.setFocusable(true);
												holder.et_answer.setFocusableInTouchMode(true);
												holder.et_answer.requestFocus();
												holder.et_answer.setInputType(EditorInfo.TYPE_CLASS_TEXT);
                                                //context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
												InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
												imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
												return;
											}else {
												//showToast("点击holder.rlMask2  wu");
												holder.llMaskContainer.setVisibility(View.GONE);
												holder.llAnswerQuestionShow.setVisibility(View.GONE);
												//显示下面的描述
												holder.tvMask.setVisibility(View.GONE);
//												holder.rlMask2.setVisibility(View.GONE);
											}
										}*/

                                    }
                                });
                                holder.tvMask.setOnClickListener(new OnClickListener() {
                                    public void onClick(View v) {
                                        entity.hasSeened = true;
                                        holder.llMaskContainer.setVisibility(View.GONE);
                                        holder.tvMask.setVisibility(View.GONE);
                                        holder.et_answer.setText("");
                                    }
                                });
                            }

                            @Override
                            public void onFinished() {
                            }

                            @Override
                            public void onError(Throwable arg0, boolean arg1) {
                            }

                            @Override
                            public void onCancelled(CancelledException arg0) {
                            }

                        });
            } else if (arrPicName.length == 1) {
                holder.llImageContainer.setVisibility(View.GONE);
                holder.ivCompatibleImage.setVisibility(View.VISIBLE);
                x.image().bind(holder.ivCompatibleImage, Config.getContrastFullPath(arrPicName[0]),
                        new CommonCallback<Drawable>() {
                            @Override
                            public void onSuccess(Drawable drawable) {
                                Bitmap bitmap;
                                Bitmap overlay;

                                // holder.ivCompatibleImage.buildDrawingCache();
                                // bitmap = holder.ivCompatibleImage.getDrawingCache();
                                bitmap = ((BitmapDrawable) drawable).getBitmap();

                                if (maskOrientation == LinearLayout.HORIZONTAL) {
                                    overlay = Bitmap
                                            .createBitmap((int) (bitmap.getWidth() / 2f / 32f),
                                                    (int) (bitmap.getHeight() / 32f),
                                                    Bitmap.Config.ARGB_8888);
                                } else {
                                    overlay = Bitmap.createBitmap((int) (bitmap.getWidth() / 32f),
                                            (int) (bitmap.getHeight() / 2f / 32f),
                                            Bitmap.Config.ARGB_8888);
                                }
                                Canvas canvas = new Canvas(overlay);
                                if (maskOrientation == LinearLayout.HORIZONTAL) {
                                    canvas.scale(1 / 32f, 1 / 32f, 0, 0);
                                    canvas.translate(-bitmap.getWidth() / 2f / 32f, 0);
                                } else {
                                    canvas.scale(1 / 32f, 1 / 32f);
                                    canvas.translate(0, bitmap.getHeight() / 2f / 32f);
                                }

                                Paint paint = new Paint();
                                paint.setFlags(Paint.FILTER_BITMAP_FLAG);
                                canvas.drawBitmap(bitmap, 0, 0, paint);
                                overlay = FastBlur.doBlur(overlay, 10, true);
                                // overlay = blur.blur(overlay, true);

                                holder.rlMask2.setBackgroundDrawable(new BitmapDrawable(overlay));
                                holder.rlMask2.setOnClickListener(new OnClickListener() {
                                    public void onClick(View v) {
                                        entity.hasSeened = true;
                                        holder.llMaskContainer.setVisibility(View.GONE);
                                        holder.tvMask.setVisibility(View.GONE);
                                    }
                                });
                                holder.tvMask.setOnClickListener(new OnClickListener() {
                                    public void onClick(View v) {
                                        entity.hasSeened = true;
                                        holder.llMaskContainer.setVisibility(View.GONE);
                                        holder.tvMask.setVisibility(View.GONE);
                                    }
                                });
                            }

                            @Override
                            public void onFinished() {
                            }

                            @Override
                            public void onError(Throwable arg0, boolean arg1) {
                            }

                            @Override
                            public void onCancelled(CancelledException arg0) {
                            }
                        });
            }
        }
        String[] arrContent = getDesContent(mContent);
        setUpDesColors(holder, entity, arrContent);
        setUpDesContent(holder, entity, arrContent);
    }

    private void setUpDesContent(ViewHolder holder, ParagraphEntity entity, String[] arrContent) {
        if (arrContent == null) {
            return;
        }
        // 设置描述
        DD.d(TAG, "desc1: " + arrContent[0] + ", desc2: " + arrContent[1]);
        parseDesc(arrContent[0], holder.tvDesc1, holder.ivUrlFlag1, entity);
        parseDesc(arrContent[1], holder.tvDesc2, holder.ivUrlFlag2, entity);
        if (arrContent[1].contains(Config.PATTERN_HIDDEN) && !entity.hasSeened) {
            holder.rlMask2.setVisibility(View.VISIBLE);
            holder.tvMask.setVisibility(View.VISIBLE);
        } else {
            holder.rlMask2.setVisibility(View.GONE);
            holder.tvMask.setVisibility(View.GONE);
        }
    }

    private void setUpDesColors(ViewHolder holder, ParagraphEntity entity, String[] arrContent) {
        if (arrContent == null) {
            return;
        }
        // 优化速度
        holder.desc2 = arrContent[1];
        DD.d(TAG, "colorA: " + arrContent[2] + ", colorB: " + arrContent[3]);
        setUpDes1Colors(holder, arrContent[2]);
        setUpDes2Colors(holder, arrContent[3]);
    }

    private void setUpDes2Colors(ViewHolder holder, String colors) {
        Matcher mColorRgb2 = PATTERN_COLOR_RGB.matcher(colors);
        Matcher mColorIndex2 = PATTERN_COLOR_INDEX.matcher(colors);
        if (mColorRgb2.find()) {
            try {
                int color = Color.rgb(Integer.parseInt(mColorRgb2.group(1)),
                        Integer.parseInt(mColorRgb2.group(2)),
                        Integer.parseInt(mColorRgb2.group(3)));
                holder.tvDesc2.setBackgroundColor(color);
                // 设置图片背景颜色
                holder.ivImage2.setBackgroundColor(color);
                holder.tvMask.setBackgroundColor(color);
                if (getGrayLevel(color) > 192) {
                    holder.tvDesc2.setTextColor(Color.BLACK);
                    holder.tvMask.setTextColor(Color.BLACK);
                    // TODO: 2016/8/10  xzc
                    //设置问题的背景颜色
                    holder.tv_question.setTextColor(Color.BLACK);
                } else {
                    holder.tvDesc2.setTextColor(Color.WHITE);
                    holder.tvMask.setTextColor(Color.WHITE);
                    holder.tv_question.setTextColor(Color.WHITE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (mColorIndex2.find()) {
            try {
                int colorIndex = Integer.parseInt(mColorIndex2.group(1));
                DD.d(TAG, "d.colorIndex: " + colorIndex);
                holder.tvDesc2.setBackgroundColor(BaseActivity.BG_COLOR[colorIndex]);
                holder.tvMask.setBackgroundColor(BaseActivity.BG_COLOR[colorIndex]);
                holder.tvDesc2.setTextColor(BaseActivity.TEXT_COLOR[colorIndex]);
                holder.tvMask.setTextColor(BaseActivity.TEXT_COLOR[colorIndex]);
                // 设置图片背景颜色
                holder.ivImage2.setBackgroundColor(BaseActivity.BG_COLOR[colorIndex]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setUpDes1Colors(ViewHolder holder, String colors) {
        Matcher mColorRgb1 = PATTERN_COLOR_RGB.matcher(colors);
        Matcher mColorIndex1 = PATTERN_COLOR_INDEX.matcher(colors);
        if (mColorRgb1.find()) {
            try {
                int color = Color.rgb(Integer.parseInt(mColorRgb1.group(1)),
                        Integer.parseInt(mColorRgb1.group(2)),
                        Integer.parseInt(mColorRgb1.group(3)));
                holder.tvDesc1.setBackgroundColor(color);
                // 设置图片背景颜色
                holder.ivImage1.setBackgroundColor(color);
                if (getGrayLevel(color) > 192) {
                    holder.tvDesc1.setTextColor(Color.BLACK);
                } else {
                    holder.tvDesc1.setTextColor(Color.WHITE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (mColorIndex1.find()) {
            try {
                int colorIndex = Integer.parseInt(mColorIndex1.group(1));
                DD.d(TAG, "b.colorIndex: " + colorIndex);
                holder.tvDesc1.setBackgroundColor(BaseActivity.BG_COLOR[colorIndex]);
                holder.tvDesc1.setTextColor(BaseActivity.TEXT_COLOR[colorIndex]);
                // 设置图片背景颜色
                holder.ivImage1.setBackgroundColor(BaseActivity.BG_COLOR[colorIndex]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    protected String[] getDesContent(String content) {
        // 提取并设置内容
        // 不用空格做分隔符是因为内容中可能会有分隔符，所以使用 "] [" 做分隔符
        String[] arrContent = content.trim().split("\\] \\[");
        if (arrContent.length != 4) {
            return null;
        }

        // 修复格式
        arrContent[0] = arrContent[0] + "]";
        arrContent[1] = "[" + arrContent[1] + "]";
        arrContent[2] = "[" + arrContent[2] + "]";
        arrContent[3] = "[" + arrContent[3];

        // TODO: 2016/8/23
        //暂时屏蔽解析问题的功能
        /*for (int i = 0; i < arrContent.length; i++) {

			int index = arrContent[i].indexOf("]");
			String sRegular = "\\" + arrContent[i].substring(0, index) + "\\]";
			arrContent[i] = arrContent[i].replaceAll(sRegular, "");

			// TODO: 2016/8/5
			//判断是否包含字符
			if (arrContent[i].contains("<?>")) {
					//先获取问题文本并把<?>替换掉
					String questionText = arrContent[i].replaceAll(Config.XZC_PATTERN_QUESTION,"");
					//显示上问题
					holder.tv_question.setText("提问: "+ questionText);
					DD.d(TAG, holder.tv_question.getText().toString());
					holder.llAnswerQuestionShow.setVisibility(View.GONE);

			}else {
				holder.btnShow.setVisibility(View.VISIBLE);
				holder.llAnswerQuestionShow.setVisibility(View.GONE);
			}
		}*/

        for (int i = 0; i < arrContent.length; i++) {
            int index = arrContent[i].indexOf("]");
            String sRegular = "\\" + arrContent[i].substring(0, index) + "\\]";
            arrContent[i] = arrContent[i].replaceAll(sRegular, "");
        }
        return arrContent;
    }

    protected void setLayoutView(int orientation, ViewHolder holder) {
        if (orientation == LinearLayout.HORIZONTAL) {
            long t1_hori_1 = System.currentTimeMillis();
            // 在非 HORIZONTAL 时进行设置
            if (holder.llTopDescContainer.getChildCount() > 0) {
                holder.llTopDescContainer.removeViewAt(0);
                holder.llBottomDescContainer.addView(holder.rlDescContainer1, 0);
                holder.llTopDescContainer.setVisibility(View.GONE);
            }
            // 设置图片区域orientation
            holder.llImageContainer.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.ivImage1
                    .getLayoutParams();
            params.width = 0;
            params.height = LinearLayout.LayoutParams.MATCH_PARENT;
            holder.ivImage1.setLayoutParams(params);
            holder.ivImage2.setLayoutParams(params);
            // 设置蒙板布局
            holder.llMaskContainer.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) holder.rlMask1
                    .getLayoutParams();
            params1.width = 0;
            params1.height = LinearLayout.LayoutParams.MATCH_PARENT;
            holder.rlMask1.setLayoutParams(params1);
            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) holder.rlMask2
                    .getLayoutParams();
            params2.width = 0;
            params2.height = LinearLayout.LayoutParams.MATCH_PARENT;
            holder.rlMask2.setLayoutParams(params2);
            long t1_hori_2 = System.currentTimeMillis();
            DD.d(TAG, "d1_hori_2: " + (t1_hori_2 - t1_hori_1));
        } else {
            long t2_vert_1 = System.currentTimeMillis();
            // 在非 VERTICAL 时进行设置
            if (holder.llTopDescContainer.getChildCount() == 0) {
                holder.llBottomDescContainer.removeViewAt(0);
                holder.llTopDescContainer.addView(holder.rlDescContainer1);
                holder.llTopDescContainer.setVisibility(View.VISIBLE);
            }
            // 设置图片区域orientation
            holder.llImageContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.ivImage1
                    .getLayoutParams();
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            params.height = 0;
            holder.ivImage1.setLayoutParams(params);
            holder.ivImage2.setLayoutParams(params);
            // 设置蒙板布局
            holder.llMaskContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) holder.rlMask1
                    .getLayoutParams();
            params1.height = 0;
            params1.width = LinearLayout.LayoutParams.MATCH_PARENT;
            holder.rlMask1.setLayoutParams(params1);
            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) holder.rlMask2
                    .getLayoutParams();
            params2.height = 0;
            params2.width = LinearLayout.LayoutParams.MATCH_PARENT;
            holder.rlMask2.setLayoutParams(params2);
            long t2_vert_2 = System.currentTimeMillis();
            DD.d(TAG, "d2_vert_2: " + (t2_vert_2 - t2_vert_1));
        }
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

    /**
     * 显示一个覆盖层
     *
     * @param mask
     */
    public void showMask(View mask) {
        if (mask.getVisibility() != View.VISIBLE) {
            mask.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏一个覆盖层
     *
     * @param mask
     */
    public void hideMask(View mask) {
        if (mask.getVisibility() != View.GONE) {
            mask.setVisibility(View.GONE);
        }
    }

    private int getGrayLevel(int color) {
        return (int) (Color.red(color) * 0.299 + Color.green(color) * 0.587
                + Color.blue(color) * 0.114);
    }

    /**
     * 解析描述
     */
    private void parseDesc(String desc, final TextView tvDesc, final ImageView ivUrlFlag,
                           final ParagraphEntity entity) {
        DD.d(TAG, "parseDesc(), desc: " + desc);

        // 过滤掉 “/无需隐藏/" 和 "<?>xx<?>"
        Pattern pQuestion = Pattern.compile(Config.PATTERN_QUESTION);
        Matcher mQuestion = pQuestion.matcher(desc);
        if (mQuestion.find()) {
            desc = desc.replace(mQuestion.group(), "");
        }
        desc = desc.replace(Config.PATTERN_NO_HIDDEN, "");
        desc = desc.replace(Config.PATTERN_HIDDEN, "");

        final Matcher matcherCommentUrl = PATTERN_COMMENT_URL.matcher(desc);
        final Matcher matcherCommentUrl2 = PATTERN_COMMENT_URL_2.matcher(desc);
        final Matcher matcherComment = PATTERN_DESC_WITH_COMMENT.matcher(desc);
        final Matcher matcherUrl = PATTERN_DESC_WITH_URL.matcher(desc);

        String url = "";

        // 匹配注释中带网址的
        if (matcherCommentUrl.find()) {
            int splitIndex1 = desc.indexOf("*");
            int splitIndex2 = desc.indexOf(Config.PATTERN_ADD_HREF);
            final String s1 = matcherCommentUrl.group(1);
            final String s2 = matcherCommentUrl.group(2);
            final String s3 = matcherCommentUrl.group(3);
            String s1s2 = s1 + "\n" + s2;
            if (TextUtils.isEmpty(s1)) {
                s1s2 = " " + s2;
            }

            url = s3;
            // url中可能混杂着”原创图片“、”前方高能“等标签，需要提取出url
            url = getUrl(url);
            // 将过滤掉url之后的标签添加进来
            if (!TextUtils.isEmpty(s3) && !TextUtils.isEmpty(url)) {
                s1s2 += s3.replace(url, "");
            }

            final SpannableStringBuilder ssDesc = new SpannableStringBuilder(s1s2);
            final int styleRes = getDescTextStyle(s1);
            ssDesc.setSpan(new TextAppearanceSpan(tvDesc.getContext(), styleRes), 0, splitIndex1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssDesc.setSpan(new TextAppearanceSpan(tvDesc.getContext(), R.style.style_desc_comment),
                    splitIndex1 + 1, s1s2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssDesc.setSpan(
                    new ForegroundColorSpan(tvDesc.getCurrentTextColor() & 0x00ffffff | 0x99000000),
                    splitIndex1 + 1, s1s2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvDesc.setText(ssDesc, TextView.BufferType.SPANNABLE);
        } else if (matcherCommentUrl2.find()) {
            final String s1 = matcherCommentUrl2.group(1);
            final String s2 = matcherCommentUrl2.group(2);
            final String s3 = matcherCommentUrl2.group(3);
            // 调整格式，与上一种格式相同
            String descTemp = s1 + "*" + s3 + Config.PATTERN_ADD_HREF + s2;
            String s1s2 = s1 + "\n" + s3;

            int splitIndex1 = descTemp.indexOf("*");
            int splitIndex2 = descTemp.indexOf(Config.PATTERN_ADD_HREF);
            if (TextUtils.isEmpty(s1)) {
                s1s2 = " " + s3;
            }

            url = s2;
            // url中可能混杂着”原创图片“、”前方高能“等标签，需要提取出url
            url = getUrl(url);
            // 将过滤掉url之后的标签添加进来
            if (!TextUtils.isEmpty(s2) && !TextUtils.isEmpty(url)) {
                s1s2 += s2.replace(url, "");
            }

            final SpannableStringBuilder ssDesc = new SpannableStringBuilder(s1s2);
            int styleRes = getDescTextStyle(s1);
            ssDesc.setSpan(new TextAppearanceSpan(tvDesc.getContext(), styleRes), 0, splitIndex1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssDesc.setSpan(new TextAppearanceSpan(tvDesc.getContext(), R.style.style_desc_comment),
                    splitIndex1 + 1, s1s2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssDesc.setSpan(
                    new ForegroundColorSpan(tvDesc.getCurrentTextColor() & 0x00ffffff | 0x99000000),
                    splitIndex1 + 1, s1s2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvDesc.setText(ssDesc, TextView.BufferType.SPANNABLE);
        } else if (matcherComment.find()) { // 匹配注释
            int splitIndex = desc.indexOf("*");
            final String s1 = matcherComment.group(1);
            final String s2 = matcherComment.group(2);
            String s1s2 = s1 + "\n" + s2;
            if (TextUtils.isEmpty(s1)) {
                s1s2 = " " + s2;
            }

            final SpannableStringBuilder ssDesc = new SpannableStringBuilder(s1s2);
            final int styleRes = getDescTextStyle(s1);
            ssDesc.setSpan(new TextAppearanceSpan(tvDesc.getContext(), styleRes), 0, splitIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssDesc.setSpan(new TextAppearanceSpan(tvDesc.getContext(), R.style.style_desc_comment),
                    splitIndex + 1, s1s2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssDesc.setSpan(
                    new ForegroundColorSpan(tvDesc.getCurrentTextColor() & 0x00ffffff | 0x99000000),
                    splitIndex + 1, s1s2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvDesc.setText(ssDesc, TextView.BufferType.SPANNABLE);
        } else if (matcherUrl.find()) { // 匹配url
            int splitIndex = desc.indexOf(Config.PATTERN_ADD_HREF);
            String s1 = matcherUrl.group(1);
            String s2 = matcherUrl.group(2);

            url = s2;
            // url中可能混杂着”原创图片“、”前方高能“等标签，需要提取出url
            url = getUrl(url);
            // 将过滤掉url之后的标签添加进来
            if (!TextUtils.isEmpty(s2) && !TextUtils.isEmpty(url)) {
                s1 += s2.replace(url, "");
            }

            final SpannableStringBuilder ssDesc = new SpannableStringBuilder(s1);
            final int styleRes = getDescTextStyle(s1);
            ssDesc.setSpan(new TextAppearanceSpan(tvDesc.getContext(), styleRes), 0, splitIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvDesc.setText(ssDesc, TextView.BufferType.SPANNABLE);
        } else { // 无匹配
            tvDesc.setText(desc);
            final int styleRes = getDescTextStyle(desc);
            switch (styleRes) {
                case R.style.style_desc_normal_large:
                    tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, Config.DESC_TEXT_SIZE_LARGE_SP);
                    break;
                case R.style.style_desc_normal_medium:
                    tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, Config.DESC_TEXT_SIZE_MEDIUM_SP);
                    break;
                case R.style.style_desc_normal_small:
                    tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, Config.DESC_TEXT_SIZE_SMALL_SP);
                    break;
                default:
                    tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, Config.DESC_TEXT_SIZE_MEDIUM_SP);
                    break;
            }
        }

        // 设置url
        if (!TextUtils.isEmpty(url)) {
            if (tvDesc.getId() == R.id.tv_desc_1) {
                entity.url1 = url;
            } else if (tvDesc.getId() == R.id.tv_desc_2) {
                entity.url2 = url;
            }
            ivUrlFlag.setVisibility(View.VISIBLE);
        } else {
            ivUrlFlag.setVisibility(View.GONE);
        }
    }

    /**
     * 过滤得到url
     */
    private String getUrl(String str) {
        DD.d(TAG, "getUrl(), str: " + str);

        if (TextUtils.isEmpty(str)) {
            return str;
        }

        int originalIndex = str.indexOf(Config.PATTERN_ORIGINAL);
        int highEnergyIndex = str.indexOf(Config.PATTERN_HIGH_ENERGY);
        int i8ForbidIndex = str.indexOf(Config.PATTERN_18_FORBID);
        int markOriginalIndex = str.indexOf(Config.PATTERN_MARK_ORIGINAL);

        int firstIndex = -1;
        if (originalIndex >= 0) {
            firstIndex = originalIndex;
        }
        if (highEnergyIndex >= 0 && highEnergyIndex < firstIndex) {
            firstIndex = highEnergyIndex;
        }
        if (i8ForbidIndex >= 0 && i8ForbidIndex < firstIndex) {
            firstIndex = i8ForbidIndex;
        }
        if (markOriginalIndex >= 0 && markOriginalIndex < firstIndex) {
            firstIndex = markOriginalIndex;
        }
        if (firstIndex > 0) {
            return str.substring(0, firstIndex);
        }

        return str;
    }

    public int getDescTextStyle(String s) {
        DD.d(TAG, "getDescTextStyle(), s: " + s);

        int styleRes = R.style.style_desc_normal_medium;
        if (s.length() <= Config.DESC_TEXT_LENGTH_LARGE_LIMIT) {
            styleRes = R.style.style_desc_normal_large;
        } else if (s.length() <= Config.DESC_TEXT_LENGTH_MEDIUM_LIMIT) {
            styleRes = R.style.style_desc_normal_medium;
        } else if (s.length() <= Config.DESC_TEXT_LENGTH_MAX_LIMIT) {
            styleRes = R.style.style_desc_normal_small;
        }
        return styleRes;
    }

    /**
     * 喜欢
     */
    private void like(final ParagraphEntity entity) {
        DD.d(TAG, "like()");

        isLikeBlocked = true;
        // 执行动画
        IRefreshingAnimationView irav = ((IRefreshingAnimationView) context);
        if (irav != null) {
            irav.showRefreshingAnimationView();
        }

        RequestParams params = new RequestParams(Config.PATH_LIKE_PARAGRAPH);
        params.setAsJsonContent(true);
        params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
        params.addParameter("paragraphReplyId", entity.getParagraphReplyId());

        x.http().post(params, new CommonCallback<JSONObject>() {
            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                DD.d(TAG, "onError(), arg0: " + arg0);
            }

            @Override
            public void onFinished() {
                isLikeBlocked = false;
                // 执行动画
                IRefreshingAnimationView irav = ((IRefreshingAnimationView) context);
                if (irav != null) {
                    irav.hideRefreshingAnimationView();
                }
            }

            @Override
            public void onSuccess(JSONObject result) {
                DD.d(TAG, "onSuccess(), result: " + result.toString());

                LikeParagraphResultEntity resultEntity = JSON
                        .parseObject(result.toString(), LikeParagraphResultEntity.class);

                if ("0".equals(resultEntity.getStatus())) {
                    ParagraphEntity paragraphEntity = entity;
                    paragraphEntity.setLikeState(resultEntity.getLike());
                    if (resultEntity.getLike() == 1) {
                        paragraphEntity.setLikeCount(paragraphEntity.getLikeCount() + 1);
                    } else if (resultEntity.getLike() == 0) {
                        paragraphEntity.setLikeCount(paragraphEntity.getLikeCount() - 1);
                    }
                    notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * 不喜欢
     */
    private void dislike(final ParagraphEntity entity) {
        DD.d(TAG, "dislike()");

        isDislikeBlocked = true;
        // 执行动画
        IRefreshingAnimationView irav = ((IRefreshingAnimationView) context);
        if (irav != null) {
            irav.showRefreshingAnimationView();
        }

        RequestParams params = new RequestParams(Config.PATH_HATE_PARAGRAPH);
        params.setAsJsonContent(true);
        params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
        params.addParameter("paragraphReplyId", entity.getParagraphReplyId());

        x.http().post(params, new CommonCallback<JSONObject>() {
            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                DD.d(TAG, "onError(), arg0: " + arg0);
            }

            @Override
            public void onFinished() {
                isDislikeBlocked = false;
                // 执行动画
                IRefreshingAnimationView irav = ((IRefreshingAnimationView) context);
                if (irav != null) {
                    irav.hideRefreshingAnimationView();
                }
            }

            @Override
            public void onSuccess(JSONObject result) {
                DD.d(TAG, "onSuccess(), result: " + result.toString());

                DislikeParagraphResultEntity resultEntity = JSON
                        .parseObject(result.toString(), DislikeParagraphResultEntity.class);
                if ("0".equals(resultEntity.getStatus())) {
                    ParagraphEntity paragraphEntity = entity;
                    paragraphEntity.setComplaintState(resultEntity.getComplaint());
                    if (paragraphEntity.getComplaintState() == 1) {
                        paragraphEntity.setComplaintCount(paragraphEntity.getComplaintCount() + 1);
                    } else if (paragraphEntity.getComplaintState() == 0) {
                        paragraphEntity.setComplaintCount(paragraphEntity.getComplaintCount() - 1);
                    }
                }
                notifyDataSetChanged();
            }
        });
    }

    /**
     * 下载图片
     */
    private void download(final ViewHolder holder, final ParagraphEntity entity) {
        DD.d(TAG, "download()");

        isDownloadBlocked = true;

        new Thread(new Runnable() {
            public void run() {
                holder.llImageAndDescContainer.buildDrawingCache();
                Bitmap bitmap = holder.llImageAndDescContainer.getDrawingCache();
                if (bitmap != null) {
                    saveImage(Config.getContrastFullPath(entity.getPicName()), bitmap);
                } else {
                    showToast("保存图片失败");
                }
                isDownloadBlocked = false;
            }
        }).start();
    }

    private void showToast(final String content) {
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                TT.show(context, content);
            }
        });
    }

    /**
     * 举报
     */
    private void report(final ParagraphEntity entity) {
        DD.d(TAG, "report()");

        isReportBlocked = true;

        RequestParams params = new RequestParams(Config.PATH_COMPLAINT_PARAGRAPH);
        params.setAsJsonContent(true);
        params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
        params.addParameter("paragraphReplyId", entity.getParagraphReplyId());
        x.http().post(params, new CommonCallback<JSONObject>() {
            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
            }

            @Override
            public void onFinished() {
                isReportBlocked = false;
            }

            @Override
            public void onSuccess(JSONObject result) {
                DD.d(TAG, "onSuccess(), result: " + result.toString());

                StatusResultEntity entity = JSON
                        .parseObject(result.toString(), StatusResultEntity.class);
                if ("0".equals(entity.getStatus())) {
                    TT.show(context, "举报成功");
                }
            }
        });
    }

    /**
     * 删除
     */
    private void delete(final ParagraphEntity entity, final int position) {
        DD.d(TAG, "delete()");

        List<JSONObject> listParagraphIds = new ArrayList<JSONObject>();
        JSONObject obj = new JSONObject();
        try {
            obj.putOpt("paragraphId", entity.getParagraphId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        listParagraphIds.add(obj);

        RequestParams params = new RequestParams(Config.PATH_DELETE_PARAGRAPH_BY_PARAGRAPH_ID);
        params.setAsJsonContent(true);
        params.addParameter("accessToken", PrefUtil.getStringValue(Config.PREF_ACCESS_TOKEN));
        params.addParameter("paragraphIds", listParagraphIds);
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
                    list.remove(position);
                    notifyDataSetChanged();
                    DD.d("还剩几条....................................................................",
                            list.size());
                }
            }
        });
    }

    /**
     * 保存图片,已存在就不保存
     */
    private void saveImage(String url, Bitmap bitmap) {
        DD.d(TAG, "saveImage(), url: " + url);

        // 获取存储目录,判断是否有可用外部存储设备
        String storageDir = Config.DOWNLOAD_IMAGE_FILE_PATH;
        if (TextUtils.isEmpty(storageDir)) {
            return;
        }
        File dir = new File(storageDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String storageFileName = getStorageFileName(url);
        if (TextUtils.isEmpty(storageFileName)) {
            return;
        }
        File file = new File(storageDir, storageFileName);
        if (file.exists()) {
            showToast("已存储至您的相册");
            return;
        } else {
            try {
                if (!file.createNewFile()) {
                    showToast("下载失败");
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream fos = null;
        try {
            byte[] bImage = bitmap2Bytes(bitmap);
            fos = new FileOutputStream(file);
            fos.write(bImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } finally {
            if (fos == null) {
                return;
            }
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        showToast("已存储至您的相册");

        // 更新图片到系统图库
        CommonUtil.updateImageToMediaStore(context, file);
    }

    /**
     * bitmap to bytes[]
     */
    private static byte[] bitmap2Bytes(Bitmap bitmap) {
        DD.d(TAG, "bitmap2Bytes()");

        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 获取存储文件名
     */
    private String getStorageFileName(String url) {
        DD.d(TAG, "getStorageFileName(), url: " + url);

        if (TextUtils.isEmpty(url)) {
            return "";
        }

        String lowerCaseUrl = url.toLowerCase(Locale.CHINA);
        String fileName;
        if (lowerCaseUrl.endsWith(".jpg") || lowerCaseUrl.endsWith(".jpeg")) {
            fileName = CommonUtil.md5(url) + ".jpg";
        } else if (lowerCaseUrl.endsWith(".png")) {
            fileName = CommonUtil.md5(url) + ".png";
        } else {
            fileName = CommonUtil.md5(url) + ".jpg";
        }
        return fileName;
    }

    public class ViewHolder {

        public TextView tvWolai;

        //回答问题部分
        public LinearLayout llAnswerQuestionShow;
        public TextView tv_question;
        public EditText et_answer;
        //点击显示的按钮
        public ImageView btnShow;

        // 大图以及描述
        public LinearLayout llImageAndDescContainer;
        public LinearLayout llTopDescContainer;
        public LinearLayout llBottomDescContainer;
        public RelativeLayout rlDescContainer1;
        public RelativeLayout rlDescContainer2;
        public TextView tvDesc1;
        public ImageView ivUrlFlag1;
        public TextView tvDesc2;
        public ImageView ivUrlFlag2;
        public LinearLayout llImageContainer;
        public ImageView ivImage1;
        public ImageView ivImage2;
        public ImageView ivCompatibleImage;
        public LinearLayout llMaskContainer;
        public RelativeLayout rlMask1;
        public ImageView ivHuodong;
        public ImageView ivZuire;
        public ImageView ivJingxuan;
        public ImageView ivYuanchuang;
        public RelativeLayout rlMask2;
        public TextView tvMask;

        public String desc2;

        public CircleImageView civHead;
        public TextView tvNickname;
        public TextView tvPublishTime;
        public TextView tvArea;
        public HorizontalScrollView hsvFunctionContainer;
        public LinearLayout llLike;
        public ImageView ivLike;
        public TextView tvLike;
        public LinearLayout llDislike;
        public ImageView ivDislike;
        public TextView tvDislike;
        public LinearLayout llComment;
        public TextView tvComment;
        public LinearLayout llEdit;
        public LinearLayout llShare;
        public LinearLayout llMore;
        public LinearLayout llQQ;
        public LinearLayout llWeixin;
        public LinearLayout llWeibo;
        public LinearLayout llRecreate;
        public LinearLayout llDownload;
        public LinearLayout llReport;
        public LinearLayout llDelete;
        public LinearLayout llScrollBack;

        // 评论
        public LinearLayout llCommentContainer;
        public LinearLayout llComment1;
        public LinearLayout llComment2;
        public LinearLayout llComment3;
        public LinearLayout llComment4;
        public LinearLayout llComment5;
        public CircleImageView civCommentHead1;
        public CircleImageView civCommentHead2;
        public CircleImageView civCommentHead3;
        public CircleImageView civCommentHead4;
        public CircleImageView civCommentHead5;
        public TextView tvCommentNickname1;
        public TextView tvCommentNickname2;
        public TextView tvCommentNickname3;
        public TextView tvCommentNickname4;
        public TextView tvCommentNickname5;
        public TextView tvCommentDate1;
        public TextView tvCommentDate2;
        public TextView tvCommentDate3;
        public TextView tvCommentDate4;
        public TextView tvCommentDate5;
        public TextView tvCommentLocation1;
        public TextView tvCommentLocation2;
        public TextView tvCommentLocation3;
        public TextView tvCommentLocation4;
        public TextView tvCommentLocation5;
        public TextView tvComment1;
        public TextView tvComment2;
        public TextView tvComment3;
        public TextView tvComment4;
        public TextView tvComment5;
        public LinearLayout[] arrCommentContainer = new LinearLayout[5];
        public CircleImageView[] arrCommentHead = new CircleImageView[5];
        public TextView[] arrCommentNickname = new TextView[5];
        public TextView[] arrCommentDate = new TextView[5];
        public TextView[] arrCommentLocation = new TextView[5];
        public TextView[] arrComment = new TextView[5];
        // 标签
        public ReboundHorizontalScrollView hsvTagContainer;
        public LinearLayout llTagContainer;
        public int position;
        public int orientation;
    }

    private class CommentViewHolder {
        private CircleImageView civHead;
        private TextView tvNickname;
        private TextView tvDateTime;
        private TextView tvArea;
        private TextView tvComment;
    }

    private class OnDescClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            int position = 0;
            try {
                position = (Integer) v.getTag();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            String url;
            if (v.getId() == R.id.tv_desc_1) {
                url = list.get(position).url1;
            } else {
                url = list.get(position).url2;
            }
            if (TextUtils.isEmpty(url)) {
                return;
            }
            DD.d(TAG, "url: " + url);

            Matcher matcherUrl = PATTERN_URL.matcher(url);
            if (matcherUrl.find()) {
                Uri uri = Uri.parse(matcherUrl.group(1));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                BaseActivity.sGotoActivity(context, intent);
            }
        }
    }

    /**
     * 点击评论
     */
    private class OnCommentClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            Intent intent = new Intent();
            intent.setClass(context, ContrastCommentActivity.class);
            intent.putExtra("paragraph", list.get(position));
            BaseActivity.sGotoActivity(context, intent);
        }
    }

    // 分享相关 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    /**
     * 点击各个分享
     */
    private class OnSharePlatformClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            ViewHolder holder = (ViewHolder) v.getTag(v.getId());
            switch (v.getId()) {
                case R.id.ll_qq:
                    onQqClick(position, holder);
                    break;
                case R.id.ll_weixin:
                    onWeixinClick(position, holder);
                    break;
                case R.id.ll_weibo:
                    onWeiboClick(position, holder);
                    break;
            }
        }
    }

    protected String[] getDesc(String content) {
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

    private String getShareText(String[] arrDesc) {
        if (arrDesc == null || arrDesc.length < 2) {
            return "";
        }
        return arrDesc[0] + " | " + arrDesc[1];
    }

    /**
     * 点击qq
     */
    private void onQqClick(int position, final ViewHolder holder) {
        DD.d(TAG, "onQqClick(), position: " + position);

        final ParagraphEntity entity = list.get(position);
        final String title = entity.getUserNickname() + "的大作";
        final String targetUrl = Config.PATH_SHARE + entity.getParagraphReplyId();
        final String text = getShareText(getDesc(entity.getParagraphContent()));
        // 设置缩略图
        Bitmap bitmap;
        if (holder.llImageContainer.getVisibility() == View.VISIBLE) {
            holder.llImageContainer.buildDrawingCache();
            bitmap = holder.llImageContainer.getDrawingCache();
        } else {
            holder.ivCompatibleImage.buildDrawingCache();
            bitmap = holder.ivCompatibleImage.getDrawingCache();
        }
        UMImage umTempImage;
        if (bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, DensityUtil.dip2px(context, 100),
                    DensityUtil.dip2px(context, 100));
            umTempImage = new UMImage(context, bitmap);
        } else {
            umTempImage = new UMImage(context, R.drawable.icon);
        }
        final UMImage umImage = umTempImage;

        new ShareAction((Activity) context).setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE)
                .setShareboardclickCallback(new ShareBoardlistener() {
                    @Override
                    public void onclick(SnsPlatform arg0, SHARE_MEDIA shareMedia) {
                        if (SHARE_MEDIA.QQ.equals(shareMedia)) {
                            new ShareAction((Activity) context).setPlatform(SHARE_MEDIA.QQ)
                                    .withTitle(title).withText(text).withTargetUrl(targetUrl)
                                    .withMedia(umImage).share();
                        } else if (SHARE_MEDIA.QZONE.equals(shareMedia)) {
                            new ShareAction((Activity) context).setPlatform(SHARE_MEDIA.QZONE)
                                    .withTitle(title).withText(text).withTargetUrl(targetUrl)
                                    .withMedia(umImage).share();
                        }
                    }
                }).open();
    }

    /**
     * 点击weixin
     */
    private void onWeixinClick(int position, final ViewHolder holder) {
        DD.d(TAG, "onWeixin(), position: " + position);

        final ParagraphEntity entity = list.get(position);
        final String title = entity.getUserNickname() + "的大作";
        final String targetUrl = Config.PATH_SHARE + entity.getParagraphReplyId();
        final String text = getShareText(getDesc(entity.getParagraphContent()));
        // 设置缩略图
        Bitmap bitmap;
        if (holder.llImageContainer.getVisibility() == View.VISIBLE) {
            holder.llImageContainer.buildDrawingCache();
            bitmap = holder.llImageContainer.getDrawingCache();
        } else {
            holder.ivCompatibleImage.buildDrawingCache();
            bitmap = holder.ivCompatibleImage.getDrawingCache();
        }
        UMImage umTempImage;
        if (bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, DensityUtil.dip2px(context, 100),
                    DensityUtil.dip2px(context, 100));
            umTempImage = new UMImage(context, bitmap);
        } else {
            umTempImage = new UMImage(context, R.drawable.icon);
        }
        final UMImage umImage = umTempImage;

        new ShareAction((Activity) context)
                .setDisplayList(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE)
                .setShareboardclickCallback(new ShareBoardlistener() {
                    @Override
                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA shareMedia) {
                        if (SHARE_MEDIA.WEIXIN.equals(shareMedia)) {
                            new ShareAction((Activity) context).setPlatform(SHARE_MEDIA.WEIXIN)
                                    .withTitle(title).withText(text).withTargetUrl(targetUrl)
                                    .withMedia(umImage).share();
                        } else if (SHARE_MEDIA.WEIXIN_CIRCLE.equals(shareMedia)) {
                            new ShareAction((Activity) context)
                                    .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE).withTitle(title)
                                    .withText(text).withTargetUrl(targetUrl).withMedia(umImage)
                                    .share();
                        }
                    }
                }).open();
    }

    /**
     * 点击weibo
     */
    private void onWeiboClick(int position, final ViewHolder holder) {
        DD.d(TAG, "onWeiboClick(), position: " + position);

        final ParagraphEntity entity = list.get(position);
        final String targetUrl = Config.PATH_SHARE + entity.getParagraphReplyId();
        final String text = getShareText(getDesc(entity.getParagraphContent()));
        // 设置缩略图
        Bitmap bitmap;
        if (holder.llImageContainer.getVisibility() == View.VISIBLE) {
            holder.llImageContainer.buildDrawingCache();
            bitmap = holder.llImageContainer.getDrawingCache();
        } else {
            holder.ivCompatibleImage.buildDrawingCache();
            bitmap = holder.ivCompatibleImage.getDrawingCache();
        }
        UMImage umTempImage;
        if (bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, DensityUtil.dip2px(context, 100),
                    DensityUtil.dip2px(context, 100));
            umTempImage = new UMImage(context, bitmap);
        } else {
            umTempImage = new UMImage(context, R.drawable.icon);
        }
        final UMImage umImage = umTempImage;

        new ShareAction((Activity) context).setDisplayList(SHARE_MEDIA.SINA)
                .setShareboardclickCallback(new ShareBoardlistener() {
                    @Override
                    public void onclick(SnsPlatform snsPlatform, final SHARE_MEDIA shareMedia) {
                        new ShareAction((Activity) context).setPlatform(SHARE_MEDIA.SINA)
                                .withText(text).withTargetUrl(targetUrl).withMedia(umImage).share();
                    }
                }).open();
    }
    // 分享相关 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    public Bitmap getSharedCacheBitmap() {
        Bitmap bitmap;
        if (holder.llImageContainer.getVisibility() == View.VISIBLE) {
            holder.llImageContainer.buildDrawingCache();
            bitmap = holder.llImageContainer.getDrawingCache();
        } else {
            holder.ivCompatibleImage.buildDrawingCache();
            bitmap = holder.ivCompatibleImage.getDrawingCache();
        }
        return bitmap;
    }

}
