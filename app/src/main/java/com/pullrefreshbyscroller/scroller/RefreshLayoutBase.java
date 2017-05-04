package com.pullrefreshbyscroller.scroller;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.TextView;

import com.pullrefreshbyscroller.R;

/**
 * Created by YSH on 2017/5/3.
 */

public abstract class RefreshLayoutBase<T extends View> extends ViewGroup implements AbsListView.OnScrollListener{

    protected Scroller mScroller;//滚动控制

    protected View mHeaderView;
    protected View mFooterView;
    protected T mContentView;

    protected int mYOffset;//本次触摸 Y 轴坐标的偏移量
    protected int mInitScrollY = 0;//最初的滚动位置，第一次布局时滚动header的高度的距离
    protected int mLastY = 0;//最后一次触摸 Y 轴坐标

    public static final int STATUS_IDLE = 0;//空闲状态
    public static final int STATUS_PULL_TO_REFRESH = 1;//下拉或者上拉，但是没有达到可以刷新的状态
    public static final int STATUS_RELEASE_TO_REFRESH = 2;//下拉或者上拉状态
    public static final int STATUS_REFRESHING = 3;//刷新中
    public static final int STATUS_LOADING = 4;//加载中

    protected int mCurrentStatus = STATUS_IDLE;

    private ImageView mArrowImageView;//header中的箭头图标
    private boolean isArrowUp;//箭头是否向上
    private TextView mTipsTextView;//header 中的文本提示
    private TextView mTimeTextView;//header 中的时间提示
    private ProgressBar mProgressBar;//进度条
    private int mScreenHeight;//屏幕高度
    private int mHeaderHeight;// Header 高度

    protected OnRefreshListener mOnRefreshListener;//下拉刷新监听
    protected OnLoadListener mLoadListener;//加载更多回调






    public RefreshLayoutBase(Context context) {
        this(context, null);
    }

    public RefreshLayoutBase(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayoutBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);

        mScroller = new Scroller(context);

        mScreenHeight = context.getResources().getDisplayMetrics().heightPixels;//获取屏幕高度
        mHeaderHeight = mScreenHeight / 4;//header 高度为屏幕高度四分之一
        
        initLayout(context);
    }

    protected final void initLayout(Context context){
        //headerView
        setupHeaderView(context);
        //设置内容视图
        setupContentView(context);
        //设置布局参数
        setDefaultContentLayoutParams();
        //添加 contentView 布局
        addView(mContentView);
        //设置底部视图
        setupFooterView(context);

    }

    /**
     * 设置布局参数
     * 给 ContentView 宽高设置为 match_parent
     */
    private void setDefaultContentLayoutParams() {
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        mContentView.setLayoutParams(params);

    }

    /**
     * 初始化 footerView
     * @param context
     */
    private void setupFooterView(Context context) {
        mFooterView = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_footer,
                this, false);
        addView(mFooterView);
    }

    /**
     * 内容视图
     */
    protected abstract void setupContentView(Context context);


    /**
     * 初始化 header
     * @param context
     */
    private void setupHeaderView(Context context) {
        mHeaderView = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header, this, false);
        mHeaderView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, mHeaderHeight));
        mHeaderView.setBackgroundColor(Color.RED);
        //header 高度为屏幕 1/4，但是，他只有 100px 的有效显示区域
        mHeaderView.setPadding(0, mHeaderHeight - 100, 0, 0);//左上右下
        addView(mHeaderView);

        mArrowImageView = (ImageView) mHeaderView.findViewById(R.id.pull_to_arrow_image);
        mTipsTextView = (TextView) mHeaderView.findViewById(R.id.pull_to_refresh_text);
        mTimeTextView = (TextView) mHeaderView.findViewById(R.id.pull_to_refresh_updated_at);
        mProgressBar = (ProgressBar) mHeaderView.findViewById(R.id.pull_to_refresh_progress);
    }

    /**if isTop return true
     * 达到顶部继续下拉则拦截事件
     * @return
     */
    protected abstract boolean isTop();

    /**
     * if isBottom return true
     * 达到底部触发加载更多
     * @return
     */
    protected abstract boolean isBottom();


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    /**
     * 设置下拉刷新的监听器
     * @param listener
     */
    public void setOnRefreshListener(OnRefreshListener listener){ mOnRefreshListener = listener; }

    /**
     * 设置滑动到底部加载更多的监听器
     * @param listener
     */
    public void setOnLoadListener(OnLoadListener listener) { mLoadListener = listener; }


    public interface OnRefreshListener{
        /**
         * 刷新
         */
        public void onRefresh();
    }

    public interface OnLoadListener{

        /**
         * 加载更多
         */
        public void onLoadMore();
    }


}
