package com.pullrefreshbyscroller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.pullrefreshbyscroller.scroller.RefreshLayoutBase;
import com.pullrefreshbyscroller.scroller.impl.RefreshListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    ListView mListView;

    RefreshListView mRefreshListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        mRefreshListView = new RefreshListView(this);
        mListView = mRefreshListView.getContentView();

        List<String> datas = new ArrayList<String>();
        for(int i = 0; i < 20; i++){
            datas.add("Item-"+i);
        }

        mListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datas));

        // 下拉刷新
        mRefreshListView.setOnRefreshListener(new RefreshLayoutBase.OnRefreshListener() {

            @Override
            public void onRefresh() {

                Toast.makeText(getApplicationContext(), "refresh", Toast.LENGTH_SHORT).show();
                mRefreshListView.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mRefreshListView.refreshComplete();
                    }
                }, 2000);
            }
        });
        // 上拉自动加载
        mRefreshListView.setOnLoadListener(new RefreshLayoutBase.OnLoadListener() {

            @Override
            public void onLoadMore() {
                Toast.makeText(getApplicationContext(), "load more", Toast.LENGTH_SHORT).show();
                mRefreshListView.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mRefreshListView.loadComplete();
                    }
                }, 1500);
            }
        });

        setContentView(mRefreshListView);
    }
}
