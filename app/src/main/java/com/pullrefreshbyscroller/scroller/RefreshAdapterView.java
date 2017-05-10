package com.pullrefreshbyscroller.scroller;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListAdapter;

/**
 * Created by YSH on 2017/5/6.
 */

public abstract class RefreshAdapterView<T extends AbsListView> extends RefreshLayoutBase<T> {

    public RefreshAdapterView(Context context) {
        this(context,null);
    }

    public RefreshAdapterView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RefreshAdapterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setAdapter(ListAdapter adapter){
        mContentView.setAdapter(adapter);
    }

    public ListAdapter getAdapter(){
        return mContentView.getAdapter();
    }



}
