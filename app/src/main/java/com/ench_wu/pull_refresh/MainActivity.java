package com.ench_wu.pull_refresh;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ench_wu.pull_refresh.View.PullRefresh;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private PullRefresh lv_pullRefresh;
    private ArrayList<String> list;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            myAdapter.notifyDataSetChanged();
            lv_pullRefresh.resetHeaderState();
        }
    };
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDate();
        initUI();

    }


    public void initUI() {
        setContentView(R.layout.activity_main);
        lv_pullRefresh = (PullRefresh) findViewById(R.id.lv_pullRefresh);

        myAdapter = new MyAdapter();
        lv_pullRefresh.setAdapter(myAdapter);
        lv_pullRefresh.setOnPullRefreshListener(new PullRefresh.onPullRefreshListener() {
            @Override
            public void onPullRefresh() {
                requestDateFromService(false);
            }

            @Override
            public void onLoadingMore() {
                requestDateFromService(true);
            }
        });
    }

    /**
     * 模拟服向务器请求数据
     * @param isLoadingMore
     */
    public void requestDateFromService(final boolean isLoadingMore) {
        new Thread() {
            @Override
            public void run() {

                SystemClock.sleep(2000);
                if (isLoadingMore) {
                    list.add("加载更多出来的数据");
                } else {
                    list.add(0, "下拉刷新获得的数据");
                }

                handler.sendEmptyMessage(0);

            }
        }.start();
    }

    private void initDate() {
        list = new ArrayList<String>();
        for (int i = 0; i < 50; i++) {
            list.add("这个是-----" + "  " + i);
        }


    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = convertView.inflate(MainActivity.this, R.layout.item_listview, null);
                holder.item_list_view = (TextView) convertView.findViewById(R.id.item_list_view);
                convertView.setTag(holder);
            }
            holder.item_list_view.setText(list.get(position));
            return convertView;
        }
    }

    static class ViewHolder {
        TextView item_list_view;
    }
}
