package com.pullrefreshbyscroller.base;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pullrefreshbyscroller.R;
import com.pullrefreshbyscroller.scroller.RefreshLayoutBase;

/**
 * Created by YSH on 2017/5/9.
 */

public abstract class PullRefreshBase <T extends View> extends LinearLayout implements
        AbsListView.OnScrollListener {

    protected T mContentView;//内容视图
    protected ViewGroup mHeaderView;
    protected ViewGroup mFooterView;

    protected RefreshLayoutBase.OnLoadListener mPullRefreshListener;//下拉刷新
    protected RefreshLayoutBase.OnLoadListener mLoadListener;//加载更多
    protected LayoutInflater mInflater;
    protected int mHeaderHeight;

    public static final int STATUS_IDLE = 0;
    public static final int STATUS_PULL_TO_REFRESH = 1;
    public static final int STATUS_RELEASE_TO_REFRESH = 2;
    public static final int STATUS_REFRESHING = 3;
    public static final int STATUS_LOADING = 4;

    protected int mCurrentStatus = STATUS_IDLE;

    protected int mYDistance = 0;
    protected int mYDown = 0;//按下时候Y坐标
    /**
     * 滑动的距离阈值，超过这个阈值则认为有效滑动
     */
    protected int mTouchSlop = 0;

    protected ImageView mArrowImageView;
    protected boolean isArrowUp = false;
    protected TextView mTipsTextView;
    protected TextView mTimeTextView;
    protected ProgressBar mFooterProgressBar;
    protected ProgressBar mHeaderProgressBar;
    protected TextView mFooterTextView;
    protected int mFooterHeight;
    protected int mHeaderViewHeight;
    protected int msrcHeight = 0;


    public PullRefreshBase(Context context) {
        this(context, null);
    }

    public PullRefreshBase(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mInflater = LayoutInflater.from(context);
        setOrientation(LinearLayout.VERTICAL);
        initLayout(context);
    }

    private void initLayout(Context context) {
        initHeaderView();
        initContentView();
        setContentView(mContentView);
        initFooterView();

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        msrcHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 获取header view, footer view的高度
     * @see android.widget.LinearLayout#onLayout(boolean, int, int, int, int)
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if(changed && mHeaderViewHeight <= 0){
            mHeaderViewHeight = mHeaderView.getHeight();
            adjustHeaderPadding(-mHeaderViewHeight);

            mFooterHeight = mFooterView.getHeight();
            adjustFooterPadding(-mFooterHeight);

        }

    }

    private void adjustFooterPadding(int bottomPadding) {
        mFooterView.setPadding(mFooterView.getPaddingLeft(),0,
                mFooterView.getPaddingRight(), bottomPadding);
    }

    private void adjustHeaderPadding(int topPadding) {
        mHeaderView.setPadding(mHeaderView.getPaddingLeft(), topPadding,
                mHeaderView.getPaddingRight(), 0);
    }

    private void initHeaderView() {
        mHeaderView = (ViewGroup) mInflater.inflate(R.layout.pull_to_refresh_header, null);
        mHeaderView.setBackgroundColor(Color.RED);

        mHeaderProgressBar = (ProgressBar) mHeaderView.findViewById(R.id.pull_to_refresh_progress);
        mArrowImageView = (ImageView) mHeaderView.findViewById(R.id.pull_to_arrow_image);
        mTipsTextView = (TextView) mHeaderView.findViewById(R.id.pull_to_refresh_text);
        mTimeTextView = (TextView) mHeaderView.findViewById(R.id.pull_to_refresh_updated_at);
        this.addView(mHeaderView, 0);
    }

    protected abstract void initContentView();

    private void setContentView(T view) {
        mContentView = view;
        //使contentView MATCH_PARENT
        LinearLayout.LayoutParams layoutParams = (LayoutParams) mContentView.getLayoutParams();
        if(layoutParams == null){
            layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }
        layoutParams.bottomMargin = 0;
        layoutParams.weight = 1.0f;
        mContentView.setLayoutParams(layoutParams);
        this.addView(mContentView, 1);
    }

    public T getContentView() { return mContentView; }

    private void initFooterView() {
        mFooterView = (ViewGroup) mInflater.inflate(R.layout.pull_to_refresh_footer, null);
        mFooterProgressBar = (ProgressBar) findViewById(R.id.pull_to_loading_progress);
        mFooterTextView = (TextView) findViewById(R.id.pull_to_loading_text);
        this.addView(mFooterView, 2);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onTouchEvent will be called and we do the actual
         * scrolling there.
         */
        final int action = MotionEventCompat.getActionMasked(ev);
        // Always handle the case of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Do not intercept touch event, let the child handle it
            return false;
        }

        switch (action) {

            case MotionEvent.ACTION_DOWN:
                mYDown = (int) ev.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:

                break;

        }

        // Do not intercept touch event, let the child handle it
        return false;
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
