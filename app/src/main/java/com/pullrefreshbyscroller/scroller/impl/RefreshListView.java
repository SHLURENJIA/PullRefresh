package com.pullrefreshbyscroller.scroller.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.pullrefreshbyscroller.scroller.RefreshAdapterView;

/**
 * Created by YSH on 2017/5/6.
 */

public class RefreshListView extends RefreshAdapterView<ListView> {
    public RefreshListView(Context context) {
        this(context,null);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    //设置内容部分
    @Override
    protected void setupContentView(Context context) {
        mContentView = new ListView(context);
        //设置滚动监听
        mContentView.setOnScrollListener(this);
    }

    @Override
    protected boolean isTop() {
        return mContentView.getFirstVisiblePosition() == 0 &&
                getScrollY() <= mHeaderView.getMeasuredHeight();
    }

    @Override
    protected boolean isBottom() {
        return mContentView != null && mContentView.getAdapter() != null
                && mContentView.getLastVisiblePosition() == mContentView.getAdapter().getCount() -1;
    }
}
