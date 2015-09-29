package com.ench_wu.pull_refresh.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ench_wu.pull_refresh.R;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * author:ench_wu
 * Created on 2015/9/24.
 */
public class PullRefresh extends ListView implements AbsListView.OnScrollListener {

    private int headerHeight;
    private View headerView;
    private int startY;

    private final int PULL_REFRESH = 0;
    private final int RELEASE_REFRESH = 1;
    private final int REFRESHING = 2;

    private int Current_state = PULL_REFRESH;
    private ProgressBar pbRotate;
    private TextView tvRefresh;
    private TextView tvTime;
    private ImageView pbArrow;
    private RotateAnimation downAnimation;
    private RotateAnimation upAnimation;
    private int footerHeight;
    private View footerView;
    private boolean isLoadmore = false;


    public PullRefresh(Context context) {
        super(context);
        init();
    }


    public PullRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    private void init() {
        setOnScrollListener(this);
        initHeaderView();
        initAnimation();
        initFooterView();
    }


    private void initHeaderView() {
        headerView = View.inflate(getContext(), R.layout.item_header, null);

        pbArrow = (ImageView) headerView.findViewById(R.id.pb_arrow);
        pbRotate = (ProgressBar) headerView.findViewById(R.id.pb_rotate);
        tvRefresh = (TextView) headerView.findViewById(R.id.tv_refresh);
        tvTime = (TextView) headerView.findViewById(R.id.tv_time);

        headerView.measure(0, 0);
        headerHeight = headerView.getMeasuredHeight();
        System.out.println("headerView" + headerHeight);
        headerView.setPadding(0, -headerHeight, 0, 0);
        addHeaderView(headerView);
        Current_state = PULL_REFRESH;

        System.out.println("get" + getRefreshTime());
    }

    private void initAnimation() {
        downAnimation = new RotateAnimation(-180, -360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        downAnimation.setDuration(300);
        downAnimation.setFillAfter(true);

        upAnimation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        upAnimation.setDuration(300);
        upAnimation.setFillAfter(true);
    }

    private void initFooterView() {
        footerView = View.inflate(getContext(), R.layout.item_footer, null);
        footerView.measure(0, 0);
        footerHeight = footerView.getMeasuredHeight();
        footerView.setPadding(0, -footerHeight, 0, 0);

        addFooterView(footerView);
    }

    /**
     * 触摸事件
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {

            case MotionEvent.ACTION_DOWN:
                startY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (Current_state == REFRESHING) {
                    break;
                }
                int moveY = (int) (ev.getY() - startY);
                int paddingTop = -headerHeight + moveY;
                if (paddingTop > -headerHeight && getFirstVisiblePosition() == 0) {
                    headerView.setPadding(0, paddingTop, 0, 0);

                    if (paddingTop >= 0 && Current_state == PULL_REFRESH) {
                        //从下拉刷新进入松开刷新状态
                        Current_state = RELEASE_REFRESH;
                        refreshHeaderView();
                    } else if (paddingTop < 0 && Current_state == RELEASE_REFRESH) {
                        //进入下拉刷新状态
                        Current_state = PULL_REFRESH;
                        refreshHeaderView();
                    }
                    return true;
                }

                break;
            case MotionEvent.ACTION_UP:
                if (Current_state == PULL_REFRESH) {
                    headerView.setPadding(0, -headerHeight, 0, 0);
                } else if (Current_state == RELEASE_REFRESH) {
                    Current_state = REFRESHING;
                    refreshHeaderView();
                    if (listener != null) {
                        listener.onPullRefresh();
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * headerView 状态
     */
    private void refreshHeaderView() {
        switch (Current_state) {
            case PULL_REFRESH:
                pbArrow.setVisibility(VISIBLE);
                pbRotate.setVisibility(INVISIBLE);
                pbArrow.startAnimation(downAnimation);
                tvRefresh.setText("下拉刷新");
                break;
            case RELEASE_REFRESH:
                pbRotate.setVisibility(INVISIBLE);
                pbArrow.setVisibility(VISIBLE);

                pbArrow.startAnimation(upAnimation);
                tvRefresh.setText("松开刷新");
                break;
            case REFRESHING:
                pbArrow.clearAnimation();
                pbArrow.setVisibility(INVISIBLE);
                pbRotate.setVisibility(VISIBLE);
                tvRefresh.setText("正在刷新...");
                headerView.setPadding(0, 0, 0, 0);
                break;
        }

    }

    /**
     * 重置header和footer状态,外部调用
     */
    public void resetHeaderState() {
        if (isLoadmore){
            footerView.setPadding(0,-footerHeight,0,0);
            isLoadmore = false;
        }else {
            headerView.setPadding(0, -headerHeight, 0, 0);
            Current_state = PULL_REFRESH;
            pbArrow.setVisibility(VISIBLE);
            pbRotate.setVisibility(INVISIBLE);
            tvRefresh.setText("下拉刷新");
            tvTime.setText("最后一次更新为:" + getRefreshTime());
        }

    }

    /**
     * 格式化时间
     * @return
     */
    public String getRefreshTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date());
    }

    /**
     * 暴露接口
     */
    public onPullRefreshListener listener;

    public void setOnPullRefreshListener(onPullRefreshListener listener) {
        this.listener = listener;
    }

    public interface onPullRefreshListener {
        void onPullRefresh();

        void onLoadingMore();
    }

    /**
     * 滑动监听事件
     * @param view
     * @param scrollState
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && getLastVisiblePosition() == (getCount() - 1) && !isLoadmore) {
            footerView.setPadding(0, 0, 0, 0);
            isLoadmore = true;
            setSelection(getCount());
            if (listener != null) {
                listener.onLoadingMore();
            }

        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
