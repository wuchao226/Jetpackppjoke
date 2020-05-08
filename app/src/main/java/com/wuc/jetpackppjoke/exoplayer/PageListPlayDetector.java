package com.wuc.jetpackppjoke.exoplayer;

import android.util.Pair;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author wuchao
 * @date 2020/4/28 23:29
 * @desciption 列表视频自动播放 检测逻辑
 */
public class PageListPlayDetector {

    /**
     * 收集一个个的能够进行视频播放的 对象，面向接口
     */
    private List<IPlayTarget> mTargets = new ArrayList<>();
    private RecyclerView mRecyclerView;
    /**
     * 正在播放的那个
     */
    private IPlayTarget mPlayingTarget;
    /**
     * RecyclerView 在屏幕上的位置
     */
    private Pair<Integer, Integer> rvLocation = null;
    private Runnable delayAutoPlay = new Runnable() {
        @Override
        public void run() {
            autoPlay();
        }
    };
    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        /**
         * 数据添加到 RecyclerView 后 回调该方法
         */
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            autoPlay();
        }
    };
    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            //SCROLL_STATE_IDLE 代表RecyclerView现在不是滚动状态
            //SCROLL_STATE_DRAGGING 代表RecyclerView处于被外力引导的滚动状态，比如手指正在拖着进行滚动。
            //SCROLL_STATE_SETTLING 代表RecyclerView处于自动滚动的状态，此时手指已经离开屏幕，RecyclerView的滚动是自身的惯性在维持
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                autoPlay();
            }
        }

        /**
         * 获取RecyclerView的滚动距离
         */
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            //当dx > 0 时，代表手指向左拖动，RecyclerView则从右向左滚动。
            //当dx < 0时，代表手指向右拖动，RecyclerView则从左向右滚动。
            //当dy > 0时，代表手指向上拖动，RecyclerView则从上向下滚动（就是我们最常见的，从顶部开始往下滚动）。
            //当dy < 0时，代表手指向下拖动，RecyclerView则从下向上滚动（就是从列表底部往回挥动）。

            if (dx == 0 && dy == 0) {
                //时序问题。当执行了AdapterDataObserver#onItemRangeInserted  可能还没有被布局到RecyclerView上。
                //所以此时 recyclerView.getChildCount()还是等于0的。
                //等childView 被布局到RecyclerView上之后，会执行onScrolled（）方法
                //并且此时 dx,dy都等于0
                postAutoPlay();
            } else {
                //如果有正在播放的,且滑动时被划出了屏幕 则 停止他
                if (mPlayingTarget != null && mPlayingTarget.isPlaying() && !isTargetInBounds(mPlayingTarget)) {
                    mPlayingTarget.inActive();
                }
            }
        }
    };

    public PageListPlayDetector(LifecycleOwner owner, RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        //监听生命周期
        owner.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    mPlayingTarget = null;
                    mTargets.clear();
                    recyclerView.removeOnScrollListener(mScrollListener);
                    owner.getLifecycle().removeObserver(this);
                }
            }
        });
        //监听有新的数据添加到 RecyclerView
        recyclerView.getAdapter().registerAdapterDataObserver(mDataObserver);
        recyclerView.addOnScrollListener(mScrollListener);
    }

    private void postAutoPlay() {
        mRecyclerView.post(delayAutoPlay);
    }

    /**
     * 自动播放 检测
     */
    private void autoPlay() {
        //判断屏幕上是否已经有视屏类型的 item
        if (mTargets.size() <= 0 || mRecyclerView.getChildCount() <= 0) {
            return;
        }
        //上一个 target 正在播放并且处于屏幕内，不需要检测新的 target
        if (mPlayingTarget != null && mPlayingTarget.isPlaying() && isTargetInBounds(mPlayingTarget)) {
            return;
        }

        IPlayTarget activeTarget = null;
        for (IPlayTarget target : mTargets) {
            //判断 PlayTarget 是否有一半以上的 View 处在屏幕内
            boolean inBounds = isTargetInBounds(target);
            if (inBounds) {
                //找到满足自动播放条件的 target
                activeTarget = target;
                break;
            }
        }
        if (activeTarget != null) {
            //把上一个满足自动播放条件的 target 关闭
            if (mPlayingTarget != null && mPlayingTarget.isPlaying()) {
                //停止播放
                mPlayingTarget.inActive();
            }
            //找到满足自动播放条件的 target，进行全局保存
            mPlayingTarget = activeTarget;
            //播放
            activeTarget.onActive();
        }
    }

    /**
     * 检测 IPlayTarget 所在的 viewGroup 是否至少还有一半的大小在屏幕内
     *
     * @param target IPlayTarget
     * @return boolean
     */
    private boolean isTargetInBounds(IPlayTarget target) {
        //得到 PlayerView 所在的容器
        ViewGroup owner = target.getOwner();
        //RecyclerView 在屏幕上的位置
        ensureRecyclerViewLocation();
        //如果 owner 没有被展示出来或者没有 Attached 到 Window 上面
        if (!owner.isShown() || !owner.isAttachedToWindow()) {
            return false;
        }
        //计算 owner 在屏幕上的位置
        int[] location = new int[2];
        owner.getLocationOnScreen(location);
        //计算 owner 的中心在屏幕上的位置
        int center = location[1] + owner.getHeight() / 2;

        //承载视频播放画面的ViewGroup它需要至少一半的大小 在RecyclerView上下范围内
        return center >= rvLocation.first && center <= rvLocation.second;
    }

    private Pair<Integer, Integer> ensureRecyclerViewLocation() {
        if (rvLocation == null) {
            int[] location = new int[2];
            mRecyclerView.getLocationOnScreen(location);
            int top = location[1];
            int bottom = top + mRecyclerView.getHeight();
            rvLocation = new Pair(top, bottom);
        }
        return rvLocation;
    }

    public void addTarget(IPlayTarget target) {
        mTargets.add(target);
    }

    public void removeTarget(IPlayTarget target) {
        mTargets.remove(target);
    }

    public void onPause() {
        if (mPlayingTarget != null) {
            mPlayingTarget.inActive();
        }
    }

    public void onResume() {
        if (mPlayingTarget != null) {
            mPlayingTarget.onActive();
        }
    }
}
