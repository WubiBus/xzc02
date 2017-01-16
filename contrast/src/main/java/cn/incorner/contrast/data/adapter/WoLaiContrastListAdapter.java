package cn.incorner.contrast.data.adapter;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.incorner.contrast.BaseActivity;
import cn.incorner.contrast.R;
import cn.incorner.contrast.data.entity.ParagraphEntity;
import cn.incorner.contrast.page.ContrastCommentActivity;
import cn.incorner.contrast.page.WoLaiContrastDetailActivity;
import cn.incorner.contrast.view.ScrollAbleViewPager;

/**
 * Created by xzc on 2016/10/7.
 */
public class WoLaiContrastListAdapter extends ContrastListAdapter {

    private final int mImageOrientation;
    private final int mTitleBack;
    private final ColorStateList mTitleColor;
    private final int mResultBack;
    private final ColorStateList mResultColor;
    private final BaseActivity mBaseActivity;
    private OnViewPageSetUpCallBack mOnViewPageSetUpCallBack;
    private MulitTextAdapter mAdapter;
    private ViewHolder holder;
    private View mView;
    private String mOriginName;
    private String mOriginAuthor;
    private String mCreateTime;
    private ScrollAbleViewPager mVpMulitText;

    public WoLaiContrastListAdapter(ArrayList<ParagraphEntity> list, BaseActivity baseActivity,
                                    LayoutInflater inflater, View.OnClickListener listener,
                                    int imageOrientation, ColorStateList titleColor, int titleBack,
                                    ColorStateList resultColor, int resultBack) {
        super(list, inflater, listener);
        mImageOrientation = imageOrientation;
        mBaseActivity = baseActivity;
        mTitleBack = titleBack;
        mTitleColor = titleColor;
        mResultBack = resultBack;
        mResultColor = resultColor;
    }

    public void updateEntity(ParagraphEntity entity) {
        list.clear();
        list.add(entity);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mView = super.getView(position, convertView, parent);
        ParagraphEntity entity = list.get(position);
        holder = (ViewHolder) mView.getTag();
        mVpMulitText = (ScrollAbleViewPager) mView.findViewById(R.id.images_vp_text);
        if (mAdapter == null) {
            mAdapter = new MulitTextAdapter(new ArrayList<ParagraphEntity>());
            mVpMulitText.setAdapter(mAdapter);
            if (mOnViewPageSetUpCallBack != null) {
                mOnViewPageSetUpCallBack.onViewPageSetUpCallBack(mVpMulitText);
            }
        }
        setUpMulitUserInfo(entity);
        return mView;
    }

    public void setUpMulitUserInfo(ParagraphEntity entity) {
        TextView tvNickName = (TextView) mView.findViewById(R.id.mulit_tv_nickname);
        TextView tvLocation = (TextView) mView.findViewById(R.id.mulit_tv_location);
        TextView tvCreateDate = (TextView) mView.findViewById(R.id.mulit_tv_create_date);
        mOriginName = entity.getOriginName();
        tvNickName.setText(mOriginName);
        mOriginAuthor = entity.getOriginAuthor();
        tvLocation.setText(mOriginAuthor);
        mCreateTime = entity.getCreateTime();
        mCreateTime = mCreateTime.substring(5, 11);
        tvCreateDate.setText(mCreateTime);
    }

    public ArrayList<ParagraphEntity> getData() {
        return list;
    }

    @Override
    protected boolean shouldAjustOrientation() {
        return false;
    }

    @Override
    protected int getItemLayout() {
        if (mImageOrientation == LinearLayout.VERTICAL) {
            return R.layout.layout_contrast_images_horizontal;
        } else {
            return R.layout.layout_contrast_images_vertical;
        }
    }

    @Override
    protected boolean shouldSetUpUser() {
        return false;
    }

    @Override
    protected boolean shouldSetUpWolaiClick() {
        return false;
    }

    public void updateViewPageData(List<ParagraphEntity> newList) {
        if (mAdapter != null) {
            mAdapter.setData(newList);
            mVpMulitText.setAdapter(mAdapter);
        }
    }

    class MulitTextAdapter extends PagerAdapter {


        private final List<ParagraphEntity> mList;

        MulitTextAdapter(List<ParagraphEntity> newList) {
            mList = newList;
        }

        public void setData(List<ParagraphEntity> newList) {
            mList.clear();
            mList.addAll(newList);
        }

        @Override
        public int getCount() {
            if (mList == null) {
                return 0;
            }
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View child = null;
            if (mImageOrientation == LinearLayout.VERTICAL) {
                child = LayoutInflater.from(container.getContext())
                        .inflate(R.layout.layout_item_mulit_version_horizontal, container, false);
            } else {
                child = LayoutInflater.from(container.getContext())
                        .inflate(R.layout.layout_item_mulit_version_vertical, container, false);
            }
            String paragraphContent = mList.get(position).getParagraphContent();
            String[] desContent = getDesContent(paragraphContent);
            String title = desContent[0];
            String result = desContent[1];
            TextView descTitle = (TextView) child.findViewById(R.id.tv_desc_1);
            descTitle.setTextColor(mTitleColor);
            descTitle.setBackgroundColor(mTitleBack);
            descTitle.setText(title);

            TextView descResult = (TextView) child.findViewById(R.id.tv_desc_2);
            descResult.setTextColor(mResultColor);
            descResult.setBackgroundColor(mResultBack);

            descResult.setText(result);
            container.addView(child);
            return child;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    public void setOnViewPageSetUpCallBack(OnViewPageSetUpCallBack onViewPageSetUpCallBack) {
        mOnViewPageSetUpCallBack = onViewPageSetUpCallBack;
    }

    public interface OnViewPageSetUpCallBack {
        void onViewPageSetUpCallBack(ScrollAbleViewPager viewPager);
    }

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

<<<<<<< HEAD
=======
    @Override
    protected void onClickAddComment(ParagraphEntity entity) {
        Intent intent = new Intent();
        intent.setClass(context, ContrastCommentActivity.class);
        intent.putExtra("paragraph", entity);
        intent.putExtra("hasFocus", true);
        mBaseActivity
                .startActivityForResult(intent, WoLaiContrastDetailActivity.REQUEST_ADD_COMMENT);
    }
>>>>>>> origin/master
}
