package com.pullrefreshbyscroller.scroller;

import android.content.Context;
import android.graphics.Color;

import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.TextView;

import com.pullrefreshbyscroller.R;

import java.text.SimpleDateFormat;
import java.util.Date;

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

    public T getContentView() { return mContentView; }

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

    /**
     * 测量 viewGroup 宽高。宽度为用户定义。高度是 header, contentView, footer 三者之和
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int childCount = getChildCount();//子视图个数
        int finalHeight = 0;//最终的高度

        for(int i = 0; i < childCount; i++){
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);//测量每个子视图
            finalHeight += child.getMeasuredHeight();
        }
        //设置下拉刷新组件的尺寸(也就是这个 ViewGroup )
        setMeasuredDimension(width, finalHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 将 header、content、footer 从上到下布局
     * 布局完成后通过 Scroller 滚动到 header 的底部
     * 滑动距离为 header 高度 + 本视图 paddingtop，达到隐藏 header
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        int top = getPaddingTop();
        for(int i=0; i<childCount; i++) {
            View child = getChildAt(i);
            child.layout(0, top, child.getMeasuredWidth(), child.getMeasuredHeight() + top);
            top += child.getMeasuredHeight();
        }
        mInitScrollY = mHeaderView.getMeasuredHeight() + getPaddingTop();
        scrollTo(0, mInitScrollY);

    }

    /**
     * 拦截触摸事件
     * 在 ContentView 滑动到顶部，并且下拉的时候拦截
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //获取触摸事件类型
        final int action = MotionEventCompat.getActionMasked(ev);
        //取消事件或者抬起事件直接返回false
        if(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP){
            return false;
        }

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                mYOffset = (int) (ev.getRawY() - mLastY);
                //如果拉到了顶部，并且是下拉，拦截事件，转到 onTouchEvent 处理下拉刷新
                if(isTop() && mYOffset > 0){
                    return true;
                }
                break;
            default:
                break;
        }


        return false;//false 默认不拦截
    }

    /**
     * 在这里处理下拉刷新或者上拉加载更多
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_MOVE:
                int currentY = (int) event.getRawY();
                mYOffset = currentY - mLastY;
                if(mCurrentStatus != STATUS_LOADING){
                    changeScrollY(mYOffset);
                }

                rotateHeaderArrow();
                changeTips();
                mLastY = currentY;
                break;
            case MotionEvent.ACTION_UP:
                //下拉刷新具体操作
                doRefresh();
                break;
            default:
                break;
        }
        return true;//返回 true，消费该事件
    }

    protected void doRefresh() {
        changeHeaderViewStaus();
        if(mCurrentStatus == STATUS_REFRESHING && mOnRefreshListener != null){
            mOnRefreshListener.onRefresh();
        }
    }

    private void changeHeaderViewStaus() {
        int curScrollY = getScrollY();
        //超过 1/2 则认为是有效的下拉刷新，否则还原
        if(curScrollY < mInitScrollY /2){
            mScroller.startScroll(getScrollX(),curScrollY,0,mHeaderView.getPaddingTop() - curScrollY);
            mCurrentStatus = STATUS_REFRESHING;
            mTipsTextView.setText(R.string.pull_to_refresh_refreshing_label);//加载中
            mArrowImageView.clearAnimation();
            mArrowImageView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        }else{
            mScroller.startScroll(getScrollX(), curScrollY, 0, mInitScrollY - curScrollY);
            mCurrentStatus = STATUS_IDLE;
        }

        invalidate();
    }


    /**
     * 旋转箭头图标
     */
    private void rotateHeaderArrow() {
        if(mCurrentStatus ==  STATUS_REFRESHING){
            return;
        } else if(mCurrentStatus == STATUS_PULL_TO_REFRESH && !isArrowUp){
            return;
        }else if(mCurrentStatus == STATUS_RELEASE_TO_REFRESH && isArrowUp){
            return;
        }

        mProgressBar.setVisibility(View.GONE);
        mArrowImageView.setVisibility(View.VISIBLE);
        float pivoteX = mArrowImageView.getWidth() / 2f;
        float pivoteY = mArrowImageView.getHeight() / 2f;
        float fromDegrees = 0f;
        float toDegrees = 0f;
        if(mCurrentStatus == STATUS_PULL_TO_REFRESH) {
            fromDegrees = 180f;
            toDegrees = 360f;
        } else if(mCurrentStatus == STATUS_RELEASE_TO_REFRESH){
            fromDegrees = 0f;
            toDegrees = 180f;
        }

        RotateAnimation animation = new RotateAnimation(fromDegrees,toDegrees,pivoteX,pivoteY);
        animation.setDuration(100);
        animation.setFillAfter(true);
        mArrowImageView.startAnimation(animation);

        if(mCurrentStatus == STATUS_RELEASE_TO_REFRESH){
            isArrowUp = true;
        } else {
            isArrowUp = false;
        }

    }


    /**
     * 根据当前状态改变 headerView 中的文本提示
     */
    private void changeTips() {
        if(mCurrentStatus == STATUS_PULL_TO_REFRESH){
            mTipsTextView.setText(R.string.pull_to_refresh_pull_label);//下拉刷新
        } else if(mCurrentStatus == STATUS_RELEASE_TO_REFRESH){
            mTipsTextView.setText(R.string.pull_to_refresh_release_label);//松开可以刷新
        }
    }

    /**
     * 根据 distance 改变 headerView 的可见范围
     * @param distance
     */
    private void changeScrollY(int distance) {
        //最大值为 ScrollY （header 隐藏），最小值为 0，header 完全显示
        int curY = getScrollY();

        if (distance > 0 && curY - distance > getPaddingTop()){//下拉
            scrollBy(0, -distance);
        } else if(distance < 0 && curY - distance <= mInitScrollY){//上拉
            scrollBy(0, -distance);
        }

        curY = getScrollY();
        int slop = mInitScrollY /2;
        if(curY > 0 && curY < slop){
            mCurrentStatus = STATUS_RELEASE_TO_REFRESH;//下拉或者上拉状态
        } else if(curY > 0 && curY > slop){
            mCurrentStatus = STATUS_PULL_TO_REFRESH;//没有达到可以刷新状态
            Log.d("RefreshLayoutBase", "changeScrollY: 没有达到可以刷新状态");
        }
    }

    /**
     * 刷新结束时候调用，视图还原为基本状态
     */
    public void refreshComplete(){
        mScroller.startScroll(getScrollX(),getScrollY(),0,mInitScrollY - getScrollY());
        mCurrentStatus = STATUS_IDLE;
        invalidate();
        updateHeaderTimeStamp();

        //100毫秒之后处理arrow和progress，免得太突兀
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                mArrowImageView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
        },100);
    }

    public void loadComplete(){
        mScroller.startScroll(getScrollX(),getScrollY(),0,mInitScrollY - getScrollY());
        mCurrentStatus = STATUS_IDLE;
        invalidate();

    }


    /**
     * 修改header上面上一次刷新的时间
     */
    private void updateHeaderTimeStamp() {
        //设置更新时间
        mTimeTextView.setText(R.string.pull_to_refresh_update_time_label);//上次更新时间
        SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getInstance();
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
        mTimeTextView.append(sdf.format(new Date()));
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    /**
     * 滚动监听，加载更多
     * 滚动到最底部的时候触发操作
     * @param view
     * @param firstVisibleItem
     * @param visibleItemCount
     * @param totalItemCount
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(mLoadListener != null && isBottom() && mScroller.getCurrY() <= mInitScrollY
                && mYOffset <= 0 && mCurrentStatus == STATUS_IDLE) {
            showFooterView();
            doLoadMore();
        }
    }

    /**
     * 执行加载更多操作。（调用接口）
     */
    private void doLoadMore() {
        if(mLoadListener != null){
            mLoadListener.onLoadMore();
        }
    }

    private void showFooterView() {
        startScroll(mFooterView.getMeasuredHeight());
        mCurrentStatus = STATUS_LOADING;
    }

    /**
     * 设置滚动参数
     * @param yOffset
     */
    private void startScroll(int yOffset) {
        mScroller.startScroll(getScrollX(), getScrollY(), 0, yOffset);
        invalidate();
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
