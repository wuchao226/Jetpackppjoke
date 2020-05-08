package com.wuc.jetpackppjoke.exoplayer;

import android.view.ViewGroup;

/**
 * @author wuchao
 * @date 2020/4/28 23:30
 * @desciption 视频播放的 接口
 */
public interface IPlayTarget {
    /**
     * 得到 PlayerView 所在的容器，得到 View 后才能在列表滚动的时候去检测它的位置是否满足自动播放
     *
     * @return ViewGroup
     */
    ViewGroup getOwner();

    /**
     * 活跃状态 视频可播放(满足自动播放时回调)
     */
    void onActive();

    /**
     * 非活跃状态，暂停它(列表滚出屏幕时回调，恢复状态停止播放)
     */
    void inActive();

    /**
     * 当前 PlayTarget 是否在播放，帮助我们完成自动播放检测逻辑
     *
     * @return boolean
     */
    boolean isPlaying();
}
