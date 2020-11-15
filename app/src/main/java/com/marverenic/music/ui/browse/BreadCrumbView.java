package com.marverenic.music.ui.browse;

import android.content.Context;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.marverenic.music.R;

import java.util.List;

public class BreadCrumbView<T> extends HorizontalScrollView {

    private static final int SCROLL_PADDING_DP = 32;

    private LinearLayout mBreadCrumbContainer;
    private BreadCrumb<T>[] mBreadCrumbs;
    private int mSelectedIndex;
    private int mScrollPaddingPx;

    @Nullable
    private OnBreadCrumbClickListener<T> mListener;

    @InverseBindingAdapter(attribute = "selectedCrumb")
    public static <T> T getSelected(BreadCrumbView<T> view) {
        return view.getBreadCrumb(view.mSelectedIndex).getData();
    }

    @BindingAdapter("selectedCrumb")
    public static <T> void setSelectedBreadCrumb(BreadCrumbView<T> view, T data) {
        for (int i = 0; i < view.getBreadCrumbCount(); i++) {
            if (view.getBreadCrumb(i).getData().equals(data)) {
                view.setSelectedBreadCrumb(i);
                return;
            }
        }
    }

    @BindingAdapter(value = "selectedCrumbAttrChanged")
    public static <T> void setListeners(BreadCrumbView<T> view,
                                        InverseBindingListener inverseBindingListener) {
        view.setBreadCrumbClickListener(breadCrumb -> inverseBindingListener.onChange());
    }

    public BreadCrumbView(Context context) {
        super(context);
        init();
    }

    public BreadCrumbView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BreadCrumbView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mScrollPaddingPx = (int) (getResources().getDisplayMetrics().density * SCROLL_PADDING_DP);

        mBreadCrumbContainer = new LinearLayout(getContext());
        mBreadCrumbContainer.setOrientation(LinearLayout.HORIZONTAL);
        addView(mBreadCrumbContainer);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 1) {
            throw new IllegalStateException("BreadCrumbView should not have any children");
        }
    }

    private void scrollToActiveCrumb() {
        if (!isLayoutRequested()) {
            performScrollToActiveCrumb();
        } else {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    performScrollToActiveCrumb();
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    private void performScrollToActiveCrumb() {
        if (mSelectedIndex < 0 || mSelectedIndex >= mBreadCrumbs.length) {
            return;
        }

        View active = mBreadCrumbContainer.getChildAt(mSelectedIndex);

        boolean startVisible = getScrollX() <= active.getLeft();
        boolean endVisible = getScrollX() + getWidth() >= active.getRight();

        if (!startVisible || !endVisible) {
            smoothScrollTo(active.getLeft() - mScrollPaddingPx, 0);
        }
    }

    public void setBreadCrumbs(List<BreadCrumb<T>> breadCrumbs) {
        //noinspection unchecked
        mBreadCrumbs = breadCrumbs.toArray((BreadCrumb<T>[]) new BreadCrumb[breadCrumbs.size()]);
        mBreadCrumbContainer.removeAllViews();

        for (int i = 0; i < mBreadCrumbs.length; i++) {
            BreadCrumb<T> breadCrumb = mBreadCrumbs[i];
            breadCrumb.setView(createBreadCrumbView(i != mBreadCrumbs.length - 1));
            breadCrumb.getView().setSelected(i == mBreadCrumbs.length - 1);
            mBreadCrumbContainer.addView(breadCrumb.getView().root);
        }

        mSelectedIndex = breadCrumbs.size() - 1;
        scrollToActiveCrumb();
    }

    public BreadCrumb<T> getBreadCrumb(int index) {
        return mBreadCrumbs[index];
    }

    public int getBreadCrumbCount() {
        return mBreadCrumbs.length;
    }

    public void setSelectedBreadCrumb(int index) {
        for (int i = 0; i < mBreadCrumbs.length; i++) {
            mBreadCrumbs[i].setSelected(i == index);
        }

        if (mSelectedIndex != index) {
            mSelectedIndex = index;
            scrollToActiveCrumb();
        }
    }

    private BreadCrumbViewHolder createBreadCrumbView(boolean showSeparator) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View breadCrumb = inflater.inflate(R.layout.view_bread_crumb, this, false);
        BreadCrumbViewHolder viewHolder = new BreadCrumbViewHolder(breadCrumb);

        viewHolder.label.setOnClickListener(this::onBreadCrumbClick);
        viewHolder.separator.setVisibility(showSeparator ? View.VISIBLE : View.GONE);

        return viewHolder;
    }

    private void onBreadCrumbClick(View view) {
        requestLayout();
        for (int i = 0; i < mBreadCrumbs.length; i++) {
            if (mBreadCrumbs[i].getView().label == view) {
                setSelectedBreadCrumb(i);

                if (mListener != null) mListener.onBreadcrumbClick(mBreadCrumbs[i]);
                return;
            }
        }
    }

    public void setBreadCrumbClickListener(@Nullable OnBreadCrumbClickListener<T> listener) {
        mListener = listener;
    }

    interface OnBreadCrumbClickListener<T> {
        void onBreadcrumbClick(BreadCrumb<T> breadCrumb);
    }

    public static class BreadCrumb<T> {

        private String mName;
        private T mData;

        private BreadCrumbViewHolder mView;

        public BreadCrumb(String name, T data) {
            mName = name;
            mData = data;
        }

        public T getData() {
            return mData;
        }

        final BreadCrumbViewHolder getView() {
            return mView;
        }

        final void setView(BreadCrumbViewHolder view) {
            mView = view;
            mView.label.setText(mName);
        }

        final void setSelected(boolean isSelected) {
            mView.setSelected(isSelected);
        }

    }

    private static class BreadCrumbViewHolder {

        final View root;
        final TextView label;
        final ImageView separator;

        BreadCrumbViewHolder(View breadCrumbView) {
            root = breadCrumbView;
            label = root.findViewById(R.id.bread_crumb_label);
            separator = root.findViewById(R.id.bread_crumb_divider);

            setSelected(false);
        }

        void setSelected(boolean selected) {
            label.setAlpha(selected ? 1.0f : 0.7f);
        }

    }

}
